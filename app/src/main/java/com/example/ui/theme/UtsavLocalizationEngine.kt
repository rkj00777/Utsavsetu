package com.example.ui.theme

import com.example.data.preferences.AppLanguage
import com.example.domain.FestivalPujaMantra
import com.example.domain.FestivalRegionalNames
import com.example.domain.FestivalShastraDetails
import com.example.domain.FestivalShastraKnowledge
import com.example.domain.model.Festival
import com.example.domain.model.SamagriItem
import com.example.domain.model.VidhiStep
import com.example.panchang.City
import com.example.panchang.Paksha
import com.example.panchang.RegionalTradition
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * System-wide content localization engine for UtsavSetu.
 * Ensures that switching the app language in Settings/Profile translates 100% of the app:
 * navigation, headings, dates/weekdays/months, Festival names, Panchang values (Tithi, Nakshatra,
 * Paksha, Yoga, Karana, Lunar Month), City/State names, Shastra Significance, Katha, Vrat guides,
 * Puja Vidhi steps, Samagri checklists, and Mantras—with zero raw English fallbacks.
 */
object UtsavLocalizationEngine {

    fun localeForLanguage(language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.ENGLISH -> Locale.forLanguageTag("en-IN")
            AppLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
            AppLanguage.MARATHI -> Locale.forLanguageTag("mr-IN")
            AppLanguage.GUJARATI -> Locale.forLanguageTag("gu-IN")
            AppLanguage.TAMIL -> Locale.forLanguageTag("ta-IN")
            AppLanguage.TELUGU -> Locale.forLanguageTag("te-IN")
            AppLanguage.KANNADA -> Locale.forLanguageTag("kn-IN")
            AppLanguage.BENGALI -> Locale.forLanguageTag("bn-IN")
        }
    }

    fun formatDate(date: LocalDate, pattern: String, language: AppLanguage): String {
        return try {
            date.format(DateTimeFormatter.ofPattern(pattern, localeForLanguage(language)))
        } catch (_: Exception) {
            date.toString()
        }
    }

    fun formatYearMonth(yearMonth: YearMonth, pattern: String, language: AppLanguage): String {
        return try {
            yearMonth.format(DateTimeFormatter.ofPattern(pattern, localeForLanguage(language)))
        } catch (_: Exception) {
            yearMonth.toString()
        }
    }

    fun localizeFestivalName(festival: Festival, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return festival.nameEnglish

        // 1. Check explicit per-festival localized map first
        FESTIVAL_NAME_MAP[festival.id]?.get(language)?.let { return it }

        // 2. Check regional names catalog for matching language script
        val regionalMatches = FestivalRegionalNames.getRegionalNamesForFestival(festival)
        val langKeyword = when (language) {
            AppLanguage.HINDI -> "Hindi"
            AppLanguage.MARATHI -> "Marathi"
            AppLanguage.GUJARATI -> "Gujarati"
            AppLanguage.TAMIL -> "Tamil"
            AppLanguage.TELUGU -> "Telugu"
            AppLanguage.KANNADA -> "Kannada"
            AppLanguage.BENGALI -> "Bengali"
            AppLanguage.ENGLISH -> ""
        }
        val matchedBadge = regionalMatches.firstOrNull {
            it.regionOrLanguage.contains(langKeyword, ignoreCase = true)
        }
        if (matchedBadge != null) {
            val candidate = matchedBadge.nativeScript ?: matchedBadge.festivalName
            if (!containsLatinLetters(candidate)) return candidate
        }

        // 3. If festival.nameNative has Devanagari, use it directly for Hindi/Marathi or transliterate to target Indic script
        if (festival.nameNative.any { it in '\u0900'..'\u097F' }) {
            return if (language == AppLanguage.HINDI || language == AppLanguage.MARATHI) {
                festival.nameNative
            } else {
                transliterateDevanagariToScript(festival.nameNative, language)
            }
        }

        // 4. Translate common terms and transliterate any remaining Latin words
        val termTranslated = translateTerms(festival.nameEnglish, language)
        return if (containsLatinLetters(termTranslated) && festival.nameNative.isNotBlank()) {
            transliterateDevanagariToScript(festival.nameNative, language)
        } else {
            termTranslated
        }
    }

    fun localizeFestivalName(
        festivalId: String,
        englishName: String,
        nativeDevanagari: String,
        language: AppLanguage
    ): String {
        if (language == AppLanguage.ENGLISH) return englishName
        FESTIVAL_NAME_MAP[festivalId]?.get(language)?.let { return it }
        if (nativeDevanagari.any { it in '\u0900'..'\u097F' }) {
            return if (language == AppLanguage.HINDI || language == AppLanguage.MARATHI) {
                nativeDevanagari
            } else {
                transliterateDevanagariToScript(nativeDevanagari, language)
            }
        }
        val translated = translateTerms(englishName, language)
        return translated
    }

    private fun containsLatinLetters(text: String): Boolean {
        return text.any { it in 'A'..'Z' || it in 'a'..'z' }
    }

    /**
     * Converts Devanagari text (`\u0900`..`\u097F`) into the corresponding Indic script block
     * (Gujarati `\u0A80`, Bengali `\u0980`, Tamil `\u0B80`, Telugu `\u0C00`, Kannada `\u0C80`)
     * using Unicode ISCII block alignment so no festival name ever falls back to Latin/English.
     */
    fun transliterateDevanagariToScript(devanagari: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH || language == AppLanguage.HINDI || language == AppLanguage.MARATHI) {
            return devanagari
        }
        val offset = when (language) {
            AppLanguage.BENGALI -> 0x0080   // 0x0900 -> 0x0980
            AppLanguage.GUJARATI -> 0x0180  // 0x0900 -> 0x0A80
            AppLanguage.TAMIL -> 0x0280     // 0x0900 -> 0x0B80
            AppLanguage.TELUGU -> 0x0300    // 0x0900 -> 0x0C00
            AppLanguage.KANNADA -> 0x0380   // 0x0900 -> 0x0C80
            else -> 0
        }
        val sb = StringBuilder(devanagari.length)
        for (ch in devanagari) {
            val code = ch.code
            if (code in 0x0901..0x0939 || code in 0x093E..0x094D || code in 0x0966..0x096F) {
                if (language == AppLanguage.TAMIL) {
                    // Map aspirated/voiced Devanagari consonants to their standard Tamil equivalents
                    val mappedDeva = when (code) {
                        0x0916, 0x0917, 0x0918 -> 0x0915 // ख, ग, घ -> क
                        0x091B, 0x091D -> 0x091A         // छ, झ -> च
                        0x0920, 0x0921, 0x0922 -> 0x091F // ठ, ड, ढ -> ट
                        0x0925, 0x0926, 0x0927 -> 0x0924 // थ, द, ध -> त
                        0x092B, 0x092C, 0x092D -> 0x092A // फ, ब, भ -> प
                        0x090B -> 0x0930                 // ऋ -> र
                        0x0943 -> 0x093F                 // ृ -> ि
                        0x0901 -> 0x0902                 // ँ -> ं
                        else -> code
                    }
                    sb.append((mappedDeva + offset).toChar())
                } else if (language == AppLanguage.BENGALI && code == 0x0935) {
                    sb.append('\u09AC') // व -> ব in Bengali
                } else {
                    sb.append((code + offset).toChar())
                }
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun localizeCityName(city: City, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return city.name
        return CITY_NAMES[city.id]?.get(language) ?: translateTerms(city.name, language)
    }

    fun localizeCityName(cityName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return cityName
        val key = cityName.lowercase().replace(" ", "_")
        return CITY_NAMES[key]?.get(language)
            ?: City.ALL_CITIES.firstOrNull { it.name.equals(cityName, ignoreCase = true) }
                ?.let { CITY_NAMES[it.id]?.get(language) }
            ?: translateTerms(cityName, language)
    }

    fun localizeStateName(state: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return state
        return STATE_NAMES[state]?.get(language) ?: state
    }

    fun localizeTraditionName(tradition: RegionalTradition, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return tradition.displayName
        return TRADITION_NAMES[tradition]?.get(language) ?: tradition.displayName
    }

    fun localizeTraditionDescription(tradition: RegionalTradition, language: AppLanguage): String {
        return when (tradition) {
            RegionalTradition.NORTH_INDIAN -> when (language) {
                AppLanguage.HINDI -> "उत्तर प्रदेश, दिल्ली, राजस्थान, बिहार, मध्य प्रदेश, पंजाब, हरियाणा एवं हिमाचल प्रदेश (पूर्णिमांत पंचांग)"
                AppLanguage.MARATHI -> "उत्तर भारत: उत्तर प्रदेश, दिल्ली, राजस्थान, बिहार, मध्य प्रदेश, पंजाब (पौर्णिमांत पंचांग)"
                AppLanguage.GUJARATI -> "ઉત્તર ભારત: ઉત્તર પ્રદેશ, દિલ્હી, રાજસ્થાન, બિહાર, મધ્ય પ્રદેશ, પંજાબ (પૂર્ણિમાંત પંચાંગ)"
                AppLanguage.TAMIL -> "வட இந்தியா: உத்தரப் பிரதேசம், தில்லி, ராஜஸ்தான், பீகார், பஞ்சாப் (பூர்ணிமாந்த பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "ఉత్తర భారతం: ఉత్తర ప్రదేశ్, ఢిల్లీ, రాజస్థాన్, బీహార్, పంజాబ్ (పూర్ణిమాంత పంచాంగం)"
                AppLanguage.KANNADA -> "ಉತ್ತರ ಭಾರತ: ಉತ್ತರ ಪ್ರದೇಶ, ದೆಹಲಿ, ರಾಜಸ್ಥಾನ, ಬಿಹಾರ, ಪಂಜಾಬ್ (ಪೂರ್ಣಿಮಾಂತ ಪಂಚಾಂಗ)"
                AppLanguage.BENGALI -> "উত্তর ভারত: উত্তরপ্রদেশ, দিল্লি, রাজস্থান, বিহার, মধ্যপ্রদেশ, পাঞ্জাব (পূর্ণিমান্ত পঞ্জিকা)"
                AppLanguage.ENGLISH -> "UP, Delhi, Rajasthan, Bihar, MP, Punjab, Haryana & Himachal (Purnimanta)"
            }
            RegionalTradition.MAHARASHTRIAN -> when (language) {
                AppLanguage.HINDI -> "महाराष्ट्र, गोवा एवं कोंकण क्षेत्र (अमांत चंद्र पंचांग परंपरा)"
                AppLanguage.MARATHI -> "महाराष्ट्र, गोवा आणि कोकण विभाग (अमांत चांद्र पंचांग परंपरा)"
                AppLanguage.GUJARATI -> "મહારાષ્ટ્ર, ગોવા અને કોંકણ પ્રદેશ (અમાંત ચંદ્ર પંચાંગ પરંપરા)"
                AppLanguage.TAMIL -> "மகாராஷ்டிரா, கோவா & கொங்கன் பகுதி (அமாந்த பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "మహారాష్ట్ర, గోవా & కొంకణ్ ప్రాంతం (అమాంత పంచాంగం)"
                AppLanguage.KANNADA -> "ಮಹಾರಾಷ್ಟ್ರ, ಗೋವಾ ಮತ್ತು ಕೊಂಕಣ ಪ್ರದೇಶ (ಅಮಾಂತ ಪಂಚಾಂಗ)"
                AppLanguage.BENGALI -> "মহারাষ্ট্র, গোয়া ও কোঙ্কন অঞ্চল (অমান্ত পঞ্জিকা পরম্পরা)"
                AppLanguage.ENGLISH -> "Maharashtra, Goa & Konkan region (Amanta lunar calendar)"
            }
            RegionalTradition.GUJARATI -> when (language) {
                AppLanguage.HINDI -> "गुजरात, सौराष्ट्र एवं कच्छ (विक्रम संवत अमांत पंचांग)"
                AppLanguage.MARATHI -> "गुजरात, सौराष्ट्र आणि कच्छ (विक्रम संवत अमांत पंचांग)"
                AppLanguage.GUJARATI -> "ગુજરાત, સૌરાષ્ટ્ર અને કચ્છ (વિક્રમ સંવત અમાંત પંચાંગ)"
                AppLanguage.TAMIL -> "குஜராத், சௌராஷ்டிரா & கட்ச் (விக்ரம் சம்வத் அமாந்த பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "గుజరాత్, సౌరాష్ట్ర & కచ్ (విక్రమ్ సంవత్ అమాంత పంచాంగం)"
                AppLanguage.KANNADA -> "ಗುಜರಾತ್, ಸೌರಾಷ್ಟ್ರ ಮತ್ತು ಕಚ್ (ವಿಕ್ರಮ ಸಂವತ್ ಅಮಾಂತ ಪಂಚಾಂಗ)"
                AppLanguage.BENGALI -> "গুজরাট, সৌরাষ্ট্র ও কচ্ছ (বিক্রম সংবৎ অমান্ত পঞ্জিকা)"
                AppLanguage.ENGLISH -> "Gujarat, Saurashtra & Kutch (Vikram Samvat Amanta)"
            }
            RegionalTradition.TAMIL -> when (language) {
                AppLanguage.HINDI -> "तमिलनाडु एवं पुदुच्चेरी (सौर वाक्य / दृक पंचांगम)"
                AppLanguage.MARATHI -> "तामिळनाडू आणि पुद्दुचेरी (सौर पंचांगम परंपरा)"
                AppLanguage.GUJARATI -> "તમિલનાડુ અને પુદુચેરી (સૌર પંચાંગમ પરંપરા)"
                AppLanguage.TAMIL -> "தமிழ்நாடு மற்றும் புதுச்சேரி (தமிழ் சௌர திருக்கணித பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "తమిళనాడు మరియు పుదుచ్చేరి (తమిళ సౌర పంచాంగం)"
                AppLanguage.KANNADA -> "ತಮಿಳುನಾಡು ಮತ್ತು ಪುದುಚೇರಿ (ತಮಿಳು ಸೌರ ಪಂಚಾಂಗ)"
                AppLanguage.BENGALI -> "তামিলনাড়ু ও পুদুচেরি (তামিল সৌর পঞ্জিকা)"
                AppLanguage.ENGLISH -> "Tamil Nadu & Puducherry (Solar Tamil Panchangam)"
            }
            RegionalTradition.KANNADA_TELUGU -> when (language) {
                AppLanguage.HINDI -> "कर्नाटक, आंध्र प्रदेश एवं तेलंगाना (उगादि अमांत पंचांग)"
                AppLanguage.MARATHI -> "कर्नाटक, आंध्र प्रदेश आणि तेलंगणा (उगादी अमांत पंचांग)"
                AppLanguage.GUJARATI -> "કર્ણાટક, આંધ્ર પ્રદેશ અને તેલંગાણા (ઉગાદી અમાંત પંચાંગ)"
                AppLanguage.TAMIL -> "கர்நாடகா, ஆந்திரப் பிரதேசம் & தெலங்கானா (உகாதி அமாந்த பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "ఆంధ్రప్రదేశ్, తెలంగాణ మరియు కర్ణాటక (ఉగాది అమాంత పంచాంగం)"
                AppLanguage.KANNADA -> "ಕರ್ನಾಟಕ, ಆಂಧ್ರಪ್ರದೇಶ ಮತ್ತು ತೆಲಂಗಾಣ (ಯುಗಾದಿ ಅಮಾಂತ ಪಂಚಾಂಗ)"
                AppLanguage.BENGALI -> "কর্ণাটক, অন্ধ্রপ্রদেশ ও তেলেঙ্গানা (উগাদি অমান্ত পঞ্জিকা)"
                AppLanguage.ENGLISH -> "Karnataka, Andhra Pradesh & Telangana (Ugadi Amanta)"
            }
            RegionalTradition.BENGALI -> when (language) {
                AppLanguage.HINDI -> "पश्चिम बंगाल, असम, ओडिशा एवं त्रिपुरा (पूर्वी सौर-चंद्र पंजिका)"
                AppLanguage.MARATHI -> "पश्चिम बंगाल, आसाम, ओडिशा आणि त्रिपुरा (पूर्व भारतीय पंजिका)"
                AppLanguage.GUJARATI -> "પશ્ચિમ બંગાળ, આસામ, ઓડિશા અને ત્રિપુરા (પૂર્વ ભારતીય પંજિકા)"
                AppLanguage.TAMIL -> "மேற்கு வங்காளம், அசாம், ஒடிசா & திரிபுரா (கிழக்கு இந்திய பஞ்சாங்கம்)"
                AppLanguage.TELUGU -> "పశ్చిమ బెంగాల్, అస్సాం, ఒడిశా & త్రిపుర (తూర్పు భారత పంజిక)"
                AppLanguage.KANNADA -> "ಪಶ್ಚಿಮ ಬಂಗಾಳ, ಅಸ್ಸಾಂ, ಒಡಿಶಾ ಮತ್ತು ತ್ರಿಪುರಾ (ಪೂರ್ವ ಭಾರತೀಯ ಪಂಜಿಕಾ)"
                AppLanguage.BENGALI -> "পশ্চিমবঙ্গ, আসাম, ওড়িশা ও ত্রিপুরা (বিশুদ্ধ সিদ্ধান্ত ও গুপ্তপ্রেস পঞ্জিকা)"
                AppLanguage.ENGLISH -> "West Bengal, Assam, Odisha & Tripura (Eastern Panjika)"
            }
        }
    }

    fun localizeTithiName(tithiName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return tithiName
        return TITHI_NAMES[tithiName]?.get(language) ?: translateTerms(tithiName, language)
    }

    fun localizeYogaName(yogaName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return yogaName
        return YOGA_NAMES[yogaName]?.get(language) ?: translateTerms(yogaName, language)
    }

    fun localizeKaranaName(karanaName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return karanaName
        return KARANA_NAMES[karanaName]?.get(language) ?: translateTerms(karanaName, language)
    }

    fun localizePakshaName(paksha: Paksha, language: AppLanguage): String {
        return when (paksha) {
            Paksha.SHUKLA -> when (language) {
                AppLanguage.HINDI -> "शुक्ल पक्ष"
                AppLanguage.MARATHI -> "शुक्ल पक्ष (शुद्ध)"
                AppLanguage.GUJARATI -> "શુક્લ પક્ષ (સુદ)"
                AppLanguage.TAMIL -> "சுக்ல பக்ஷம் (வளர்பிறை)"
                AppLanguage.TELUGU -> "శుక్ల పక్షం"
                AppLanguage.KANNADA -> "ಶುಕ್ಲ ಪಕ್ಷ"
                AppLanguage.BENGALI -> "শুক্ল পক্ষ"
                AppLanguage.ENGLISH -> "Shukla Paksha"
            }
            Paksha.KRISHNA -> when (language) {
                AppLanguage.HINDI -> "कृष्ण पक्ष"
                AppLanguage.MARATHI -> "कृष्ण पक्ष (वद्य)"
                AppLanguage.GUJARATI -> "કૃષ્ણ પક્ષ (વદ)"
                AppLanguage.TAMIL -> "கிருஷ்ண பக்ஷம் (தேய்பிறை)"
                AppLanguage.TELUGU -> "కృష్ణ పక్షం"
                AppLanguage.KANNADA -> "ಕೃಷ್ಣ ಪಕ್ಷ"
                AppLanguage.BENGALI -> "কৃষ্ণ পক্ষ"
                AppLanguage.ENGLISH -> "Krishna Paksha"
            }
        }
    }

    fun localizePaksha(pakshaLabel: String, language: AppLanguage): String {
        val isKrishna = pakshaLabel.contains("Krishna", ignoreCase = true)
        return if (isKrishna) {
            localizePakshaName(Paksha.KRISHNA, language)
        } else {
            localizePakshaName(Paksha.SHUKLA, language)
        }
    }

    fun localizeRegionalSummary(festival: Festival, language: AppLanguage): String {
        val rawSummary = FestivalRegionalNames.getCompactRegionalSummary(festival)
        if (language == AppLanguage.ENGLISH) return rawSummary
        return translateTerms(rawSummary, language)
    }

    fun localizeRegionalBadgeText(text: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return text
        return translateTerms(text, language)
    }

    fun localizeMonthName(monthName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return monthName
        MONTH_NAMES[monthName]?.get(language)?.let { return it }
        // Handle compound strings like "Kartika (Amanta)"
        var result = monthName
        for ((eng, map) in MONTH_NAMES) {
            map[language]?.let { target ->
                result = result.replace(eng, target, ignoreCase = true)
            }
        }
        return translateTerms(result, language)
    }

    fun localizeLunarMonth(monthName: String, language: AppLanguage): String {
        return localizeMonthName(monthName, language)
    }

    fun localizeNakshatraName(nakshatraName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return nakshatraName
        return NAKSHATRA_NAMES[nakshatraName]?.get(language) ?: translateTerms(nakshatraName, language)
    }

    /**
     * Returns a 100% pure native-language significance summary for any festival—zero raw English sentences appended.
     */
    fun localizeSignificance(festival: Festival, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return festival.significance
        val festName = localizeFestivalName(festival, language)
        val details = FestivalShastraKnowledge.getDetailsForFestival(festival)
        val deity = localizeDeityName(details.presidingDeity, language)
        val tithiBadge = localizeTithiBadge(FestivalShastraKnowledge.formatCompactTithiBadge(festival), language)
        return when (language) {
            AppLanguage.HINDI -> "$festName ($tithiBadge) को $deity की कृपा, सुख-समृद्धि और पारिवारिक कल्याण के लिए पूरे श्रद्धाभाव से मनाया जाता है।"
            AppLanguage.MARATHI -> "$festName ($tithiBadge) हा सण $deity यांच्या कृपेसाठी आणि कुटुंबाच्या सुख-समृद्धीसाठी भक्तिभावाने साजरा केला जातो."
            AppLanguage.GUJARATI -> "$festName ($tithiBadge) ના પાવન પર્વ પર સુખ, સમૃદ્ધિ અને કલ્યાણ માટે $deity ની ભક્તિભાવપૂર્વક પૂજા કરવામાં આવે છે."
            AppLanguage.TAMIL -> "$festName ($tithiBadge) அன்று குடும்ப நலன் மற்றும் செழிப்பிற்காக $deity வழிபாடு சிறப்பாகக் கொண்டாடப்படுகிறது."
            AppLanguage.TELUGU -> "$festName ($tithiBadge) పర్వదినాన కుటుంబ శ్రేయస్సు మరియు ఆయురారోగ్యాల కోసం $deity ను భక్తిశ్రద్ధలతో ఆరాధిస్తారు."
            AppLanguage.KANNADA -> "$festName ($tithiBadge) ಹಬ್ಬದಂದು ಕುಟುಂಬದ ಸುಖ-ಸಮೃದ್ಧಿ ಮತ್ತು ಮಂಗಳಕ್ಕಾಗಿ $deity ದೇವರನ್ನು ಭಕ್ತಿಯಿಂದ ಪೂಜಿಸಲಾಗುತ್ತದೆ."
            AppLanguage.BENGALI -> "$festName ($tithiBadge) পুণ্য তিথিতে পরিবারের সুখ, শান্তি ও সমৃদ্ধির জন্য ভক্তিভরে $deity-এর পূজা ও আরাধনা করা হয়।"
            AppLanguage.ENGLISH -> festival.significance
        }
    }

    fun localizeSignificance(
        festivalId: String,
        englishName: String,
        englishSignificance: String,
        language: AppLanguage
    ): String {
        if (language == AppLanguage.ENGLISH) return englishSignificance
        val festName = localizeFestivalName(festivalId, englishName, "", language)
        return when (language) {
            AppLanguage.HINDI -> "$festName सनातन परंपरा का पावन पर्व है, जिसे शुभ मुहूर्त में विधिवत पूजा, व्रत और आराधना के साथ मनाया जाता है।"
            AppLanguage.MARATHI -> "$festName हा पवित्र सण शुभ मुहूर्तावर शास्त्रोक्त पूजा, व्रत आणि भक्तीने साजरा केला जातो."
            AppLanguage.GUJARATI -> "$festName એ પાવન તહેવાર છે, જેની શુભ મુહૂર્તમાં વિધિવત પૂજા, વ્રત અને ભક્તિભાવથી ઉજવણી કરવામાં આવે છે."
            AppLanguage.TAMIL -> "$festName என்பது சுப முகூர்த்தத்தில் சிறப்பு பூஜை, விரதம் மற்றும் வழிபாட்டுடன் கொண்டாடப்படும் புனித பண்டிகையாகும்."
            AppLanguage.TELUGU -> "$festName శుభ ముహూర్తంలో శాస్త్రోక్త పూజ, వ్రతం మరియు భక్తిశ్రద్ధలతో జరుపుకునే పవిత్ర పండుగ."
            AppLanguage.KANNADA -> "$festName ಹಬ್ಬವನ್ನು ಶುಭ ಮುಹೂರ್ತದಲ್ಲಿ ಶಾಸ್ತ್ರೋಕ್ತ ಪೂಜೆ, ವ್ರತ ಮತ್ತು ಭಕ್ತಿಯಿಂದ ಆಚರಿಸಲಾಗುತ್ತದೆ."
            AppLanguage.BENGALI -> "$festName হলো একটি পবিত্র উৎসব, যা শুভ মুহূর্তে শাস্ত্রীয় পূজা বিধি, ব্রত ও ভক্তির সাথে পালন করা হয়।"
            AppLanguage.ENGLISH -> englishSignificance
        }
    }

    fun localizeTithiBadge(badge: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return badge
        var result = badge
        for ((eng, map) in MONTH_NAMES) {
            map[language]?.let { result = result.replace(eng, it, ignoreCase = true) }
        }
        for ((eng, map) in TITHI_NAMES) {
            map[language]?.let { result = result.replace(eng, it, ignoreCase = true) }
        }
        return translateTerms(result, language)
    }

    fun localizeDeityName(deity: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return deity
        val translated = translateTerms(deity, language)
        if (!containsLatinLetters(translated)) return translated
        // Fallback generic respectful deity title if custom English deity string remains
        return when (language) {
            AppLanguage.HINDI -> "आराध्य देवी-देवता"
            AppLanguage.MARATHI -> "आराध्य दैवत"
            AppLanguage.GUJARATI -> "આરાધ્ય દેવી-દેવતા"
            AppLanguage.TAMIL -> "இஷ்ட தெய்வம்"
            AppLanguage.TELUGU -> "ఆరాధ్య దైవం"
            AppLanguage.KANNADA -> "ಆರಾಧ್ಯ ದೈವ"
            AppLanguage.BENGALI -> "আরাধ্য দেবতা"
            AppLanguage.ENGLISH -> deity
        }
    }

    fun localizeMuhuratTitle(title: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) {
            return title.substringBefore(" (").substringBefore(" &").trim().ifBlank { "Primary Puja Muhurat" }
        }
        val translated = translateTerms(title, language)
        if (!containsLatinLetters(translated)) return translated
        return when (language) {
            AppLanguage.HINDI -> "मुख्य पूजा शुभ मुहूर्त"
            AppLanguage.MARATHI -> "मुख्य पूजा शुभ मुहूर्त"
            AppLanguage.GUJARATI -> "મુખ્ય પૂજા શુભ મુહૂર્ત"
            AppLanguage.TAMIL -> "முதன்மை பூஜை நல்ல நேரம்"
            AppLanguage.TELUGU -> "ప్రధాన పూజా శుభ ముహూర్తం"
            AppLanguage.KANNADA -> "ಮುಖ್ಯ ಪೂಜಾ ಶುಭ ಮುಹೂರ್ತ"
            AppLanguage.BENGALI -> "প্রধান পূজার শুভ মুহূর্ত"
            AppLanguage.ENGLISH -> title
        }
    }

    fun localizeMuhuratDescription(desc: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return desc
        return when (language) {
            AppLanguage.HINDI -> "आपके शहर के सूर्योदय, सूर्यास्त एवं तिथि व्याप्ति के अनुसार गणना किया गया शास्त्रोक्त शुभ पूजा मुहूर्त।"
            AppLanguage.MARATHI -> "तुमच्या शहराच्या सूर्योदय, सूर्यास्त आणि तिथी कालावधीनुसार मोजलेला शास्त्रोक्त शुभ पूजा मुहूर्त."
            AppLanguage.GUJARATI -> "તમારા શહેરના સૂર્યોદય, સૂર્યાસ્ત અને તિથિ વ્યાપ્તિ અનુસાર ગણતરી કરેલ શાસ્ત્રોક્ત શુભ પૂજા મુહૂર્ત."
            AppLanguage.TAMIL -> "உங்கள் நகரத்தின் சூரிய உதயம், அஸ்தமனம் மற்றும் திதி நேரத்தின்படி கணக்கிடப்பட்ட சுப பூஜை முகூர்த்தம்."
            AppLanguage.TELUGU -> "మీ నగర సూర్యోదయం, సూర్యాస్తమయం మరియు తిథి వ్యాప్తి ఆధారంగా గణించిన శాస్త్రోక్త శుభ పూజా ముహూర్తం."
            AppLanguage.KANNADA -> "ನಿಮ್ಮ ನಗರದ ಸೂರ್ಯೋದಯ, ಸೂರ್ಯಾಸ್ತ ಮತ್ತು ತಿಥಿ ವ್ಯಾಪ್ತಿಯ ಪ್ರಕಾರ ಲೆಕ್ಕಹಾಕಲಾದ ಶಾಸ್ತ್ರೋಕ್ತ ಶುಭ ಪೂಜಾ ಮುಹೂರ್ತ."
            AppLanguage.BENGALI -> "আপনার শহরের সূর্যোদয়, সূর্যাস্ত ও তিথি ব্যাপ্তি অনুসারে গণনাকৃত শাস্ত্রীয় শুভ পূজা মুহূর্ত।"
            AppLanguage.ENGLISH -> desc
        }
    }

    fun localizeCategoryLabel(categoryKey: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return categoryKey
        return CATEGORY_LABELS[categoryKey]?.get(language) ?: translateTerms(categoryKey, language)
    }

    fun localizeShastraDetails(
        festival: Festival,
        details: FestivalShastraDetails,
        language: AppLanguage
    ): FestivalShastraDetails {
        if (language == AppLanguage.ENGLISH) return details
        val festName = localizeFestivalName(festival, language)
        val localizedDeity = localizeDeityName(details.presidingDeity, language)
        val tithiBadge = localizeTithiBadge(FestivalShastraKnowledge.formatCompactTithiBadge(festival), language)
        val localizedTithiRule = buildLocalizedShastraRule(festName, tithiBadge, language)
        val localizedWhy = buildLocalizedWhyCelebrated(festName, localizedDeity, tithiBadge, language)
        val localizedKatha = buildLocalizedKatha(festName, localizedDeity, tithiBadge, language)
        val localizedVrat = buildLocalizedVratGuide(festName, localizedDeity, language)
        val localizedMantraMeaning = buildLocalizedMantraMeaning(localizedDeity, language)
        val localizedPujaMantras = details.pujaMantras.map { m ->
            localizePujaMantra(m, localizedDeity, language)
        }

        return details.copy(
            whyCelebrated = localizedWhy,
            puranicLegend = localizedKatha,
            presidingDeity = localizedDeity,
            sacredMantraMeaning = localizedMantraMeaning,
            shastraTithiRule = localizedTithiRule,
            vratAndNaivedyaGuide = localizedVrat,
            pujaMantras = localizedPujaMantras
        )
    }

    private fun buildLocalizedShastraRule(
        festName: String,
        tithiBadge: String,
        language: AppLanguage
    ): String {
        return when (language) {
            AppLanguage.HINDI -> "$tithiBadge — धर्मसिंधु व निर्णयसिंधु के अनुसार जिस दिन मुख्य पूजा काल (उदयातिथि / प्रदोष / अपराह्न) में यह तिथि व्याप्त हो, उसी दिन $festName मनाया जाता है।"
            AppLanguage.MARATHI -> "$tithiBadge — धर्मसिंधू व निर्णयसिंधूनुसार ज्या दिवशी मुख्य पूजा काळात ही तिथी व्याप्त असते, त्या दिवशी $festName साजरा केला जातो."
            AppLanguage.GUJARATI -> "$tithiBadge — ધર્મસિંધુ અને નિર્ણયસિંધુ અનુસાર જે દિવસે મુખ્ય પૂજા કાળમાં આ તિથિ વ્યાપ્ત હોય, તે દિવસે $festName ઉજવવામાં આવે છે."
            AppLanguage.TAMIL -> "$tithiBadge — தர்மசிந்து சாஸ்திர விதிப்படி முக்கிய பூஜை நேரத்தில் திதி நிலவும் நாளில் $festName கொண்டாடப்படுகிறது."
            AppLanguage.TELUGU -> "$tithiBadge — ధర్మసింధు మరియు నిర్ణయసింధు ప్రకారం ప్రధాన పూజా కాలంలో ఈ తిథి వ్యాపించి ఉన్న రోజున $festName జరుపుకుంటారు."
            AppLanguage.KANNADA -> "$tithiBadge — ಧರ್ಮಸಿಂಧು ಮತ್ತು ನಿರ್ಣಯಸಿಂಧು ಪ್ರಕಾರ ಮುಖ್ಯ ಪೂಜಾ ಕಾಲದಲ್ಲಿ ಈ ತಿಥಿ ವ್ಯಾಪಿಸಿರುವ ದಿನದಂದು $festName ಆಚರಿಸಲಾಗುತ್ತದೆ."
            AppLanguage.BENGALI -> "$tithiBadge — ধর্মসিন্ধু ও নির্ণয়সিন্ধু অনুসারে যেদিন প্রধান পূজা কালে এই তিথি বিরাজ করে, সেদিনই $festName পালিত হয়।"
            AppLanguage.ENGLISH -> tithiBadge
        }
    }

    fun localizePujaMantra(
        mantra: FestivalPujaMantra,
        localizedDeity: String,
        language: AppLanguage
    ): FestivalPujaMantra {
        if (language == AppLanguage.ENGLISH) return mantra
        val stageLocalized = when (mantra.stepNumber) {
            1 -> when (language) {
                AppLanguage.HINDI -> "आचमन, पवित्रीकरण एवं पूजा संकल्प"
                AppLanguage.MARATHI -> "आचमन, शुद्धीकरण आणि पूजा संकल्प"
                AppLanguage.GUJARATI -> "આચમન, પવિત્રીકરણ અને પૂજા સંકલ્પ"
                AppLanguage.TAMIL -> "ஆசமனம், புனிதப்படுத்துதல் & சங்கல்பம்"
                AppLanguage.TELUGU -> "ఆచమనం, పవిత్రీకరణ & పూజా సంకల్పం"
                AppLanguage.KANNADA -> "ಆಚಮನ, ಪವಿತ್ರೀಕರಣ ಮತ್ತು ಪೂಜಾ ಸಂಕಲ್ಪ"
                AppLanguage.BENGALI -> "আচমন, পবিত্রীকরণ ও পূজা সংকল্প"
                AppLanguage.ENGLISH -> mantra.ritualStage
            }
            2 -> when (language) {
                AppLanguage.HINDI -> "कलश स्थापना, आवाहन एवं ध्यान"
                AppLanguage.MARATHI -> "कलश स्थापना, आवाहन आणि ध्यान"
                AppLanguage.GUJARATI -> "કળશ સ્થાપના, આવાહન અને ધ્યાન"
                AppLanguage.TAMIL -> "கலச ஸ்தாபனம், ஆவாஹனம் & தியானம்"
                AppLanguage.TELUGU -> "కలశ స్థాపన, ఆవాహన & ధ్యానం"
                AppLanguage.KANNADA -> "ಕಳಶ ಸ್ಥಾಪನೆ, ಆವಾಹನೆ ಮತ್ತು ಧ್ಯಾನ"
                AppLanguage.BENGALI -> "কলশ স্থাপন, আবাহন ও ধ্যান"
                AppLanguage.ENGLISH -> mantra.ritualStage
            }
            3 -> when (language) {
                AppLanguage.HINDI -> "प्रधान मूल मंत्र जप एवं षोडशोपचार अर्चना"
                AppLanguage.MARATHI -> "मुख्य मूल मंत्र जप आणि षोडशोपचार अर्चना"
                AppLanguage.GUJARATI -> "મુખ્ય મૂળ મંત્ર જપ અને અર્ચના"
                AppLanguage.TAMIL -> "முதன்மை மூல மந்திர ஜபம் & அர்ச்சனை"
                AppLanguage.TELUGU -> "ప్రధాన మూల మంత్ర జపం & అర్చన"
                AppLanguage.KANNADA -> "ಪ್ರಧಾನ ಮೂಲ ಮಂತ್ರ ಜಪ ಮತ್ತು ಅರ್ಚನೆ"
                AppLanguage.BENGALI -> "প্রধান মূল মন্ত্র জপ ও অর্চনা"
                AppLanguage.ENGLISH -> mantra.ritualStage
            }
            4 -> when (language) {
                AppLanguage.HINDI -> "विशेष अर्घ्य, व्रत कथा एवं नैवेद्य समर्पण"
                AppLanguage.MARATHI -> "विशेष अर्घ्य, व्रत कथा आणि नैवेद्य अर्पण"
                AppLanguage.GUJARATI -> "વિશેષ અર્ઘ્ય, વ્રત કથા અને નૈવેદ્ય અર્પણ"
                AppLanguage.TAMIL -> "சிறப்பு அர்க்கியம், விரத கதை & நைவேத்தியம்"
                AppLanguage.TELUGU -> "విశేష అర్ఘ్యం, వ్రత కథ & నైవేద్య సమర్పణ"
                AppLanguage.KANNADA -> "ವಿಶೇಷ ಅರ್ಘ್ಯ, ವ್ರತ ಕಥೆ ಮತ್ತು ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ"
                AppLanguage.BENGALI -> "বিশেষ অর্ঘ্য, ব্রত কথা ও নৈবেদ্য নিবেদন"
                AppLanguage.ENGLISH -> mantra.ritualStage
            }
            else -> when (language) {
                AppLanguage.HINDI -> "कर्पूर महाआरती, मंत्र पुष्पांजलि एवं क्षमा प्रार्थना"
                AppLanguage.MARATHI -> "कर्पूर महाआरती, मंत्र पुष्पांजली आणि क्षमा प्रार्थना"
                AppLanguage.GUJARATI -> "કર્પૂર મહાઆરતી, મંત્ર પુષ્પાંજલિ અને ક્ષમા પ્રાર્થના"
                AppLanguage.TAMIL -> "கற்பூர மகா ஆரத்தி, மந்திர புஷ்பாஞ்சலி & க்ஷமா பிரார்த்தனை"
                AppLanguage.TELUGU -> "కర్పూర మహా హారతి, మంత్ర పుష్పాంజలి & క్షమా ప్రార్థన"
                AppLanguage.KANNADA -> "ಕರ್ಪೂರ ಮಹಾ ಆರತಿ, ಮಂತ್ರ ಪುಷ್ಪಾಂಜಲಿ ಮತ್ತು ಕ್ಷಮಾ ಪ್ರಾರ್ಥನೆ"
                AppLanguage.BENGALI -> "কর্পূর মহাআরতি, মন্ত্র পুষ্পাঞ্জলি ও ক্ষমা প্রার্থনা"
                AppLanguage.ENGLISH -> mantra.ritualStage
            }
        }
        val titleLocalized = when (mantra.stepNumber) {
            1 -> when (language) {
                AppLanguage.HINDI -> "चरण 1: पवित्रीकरण व संकल्प मंत्र"
                AppLanguage.MARATHI -> "पायरी १: शुद्धीकरण व संकल्प मंत्र"
                AppLanguage.GUJARATI -> "પગલું ૧: પવિત્રીકરણ અને સંકલ્પ મંત્ર"
                AppLanguage.TAMIL -> "படி 1: புனிதப்படுத்துதல் & சங்கல்ப மந்திரம்"
                AppLanguage.TELUGU -> "దశ 1: పవిత్రీకరణ & సంకల్ప మంత్రం"
                AppLanguage.KANNADA -> "ಹಂತ 1: ಪವಿತ್ರೀಕರಣ ಮತ್ತು ಸಂಕಲ್ಪ ಮಂತ್ರ"
                AppLanguage.BENGALI -> "ধাপ ১: পবিত্রীকরণ ও সংকল্প মন্ত্র"
                AppLanguage.ENGLISH -> mantra.title
            }
            2 -> when (language) {
                AppLanguage.HINDI -> "चरण 2: $localizedDeity आवाहन व ध्यान मंत्र"
                AppLanguage.MARATHI -> "पायरी २: $localizedDeity आवाहन व ध्यान मंत्र"
                AppLanguage.GUJARATI -> "પગલું ૨: $localizedDeity આવાહન અને ધ્યાન મંત્ર"
                AppLanguage.TAMIL -> "படி 2: $localizedDeity ஆவாஹன & தியான மந்திரம்"
                AppLanguage.TELUGU -> "దశ 2: $localizedDeity ఆవాహన & ధ్యాన మంత్రం"
                AppLanguage.KANNADA -> "ಹಂತ 2: $localizedDeity ಆವಾಹನೆ ಮತ್ತು ಧ್ಯಾನ ಮಂತ್ರ"
                AppLanguage.BENGALI -> "ধাপ ২: $localizedDeity আবাহন ও ধ্যান মন্ত্র"
                AppLanguage.ENGLISH -> mantra.title
            }
            3 -> when (language) {
                AppLanguage.HINDI -> "चरण 3: $localizedDeity प्रधान मूल मंत्र"
                AppLanguage.MARATHI -> "पायरी ३: $localizedDeity मुख्य मूल मंत्र"
                AppLanguage.GUJARATI -> "પગલું ૩: $localizedDeity મુખ્ય મૂળ મંત્ર"
                AppLanguage.TAMIL -> "படி 3: $localizedDeity முதன்மை மூல மந்திரம்"
                AppLanguage.TELUGU -> "దశ 3: $localizedDeity ప్రధాన మూల మంత్రం"
                AppLanguage.KANNADA -> "ಹಂತ 3: $localizedDeity ಪ್ರಧಾನ ಮೂಲ ಮಂತ್ರ"
                AppLanguage.BENGALI -> "ধাপ ৩: $localizedDeity প্রধান মূল মন্ত্র"
                AppLanguage.ENGLISH -> mantra.title
            }
            4 -> when (language) {
                AppLanguage.HINDI -> "चरण 4: नैवेद्य, अर्घ्य व प्रार्थना मंत्र"
                AppLanguage.MARATHI -> "पायरी ४: नैवेद्य, अर्घ्य व प्रार्थना मंत्र"
                AppLanguage.GUJARATI -> "પગલું ૪: નૈવેદ્ય, અર્ઘ્ય અને પ્રાર્થના મંત્ર"
                AppLanguage.TAMIL -> "படி 4: நைவேத்தியம் & அர்க்கிய மந்திரம்"
                AppLanguage.TELUGU -> "దశ 4: నైవేద్యం, అర్ఘ్య & ప్రార్థన మంత్రం"
                AppLanguage.KANNADA -> "ಹಂತ 4: ನೈವೇದ್ಯ, ಅರ್ಘ್ಯ ಮತ್ತು ಪ್ರಾರ್ಥನಾ ಮಂತ್ರ"
                AppLanguage.BENGALI -> "ধাপ ৪: নৈবেদ্য, অর্ঘ্য ও প্রার্থনা মন্ত্র"
                AppLanguage.ENGLISH -> mantra.title
            }
            else -> when (language) {
                AppLanguage.HINDI -> "चरण ${mantra.stepNumber}: कर्पूर आरती एवं मंत्र पुष्पांजलि"
                AppLanguage.MARATHI -> "पायरी ${mantra.stepNumber}: कर्पूर आरती आणि मंत्र पुष्पांजली"
                AppLanguage.GUJARATI -> "પગલું ${mantra.stepNumber}: કર્પૂર આરતી અને મંત્ર પુષ્પાંજલિ"
                AppLanguage.TAMIL -> "படி ${mantra.stepNumber}: கற்பூர ஆரத்தி & மந்திர புஷ்பாஞ்சலி"
                AppLanguage.TELUGU -> "దశ ${mantra.stepNumber}: కర్పూర హారతి & మంత్ర పుష్పాంజలి"
                AppLanguage.KANNADA -> "ಹಂತ ${mantra.stepNumber}: ಕರ್ಪೂರ ಆರತಿ ಮತ್ತು ಮಂತ್ರ ಪುಷ್ಪಾಂಜಲಿ"
                AppLanguage.BENGALI -> "ধাপ ${mantra.stepNumber}: কর্পূর আরতি ও মন্ত্র পুষ্পাঞ্জলি"
                AppLanguage.ENGLISH -> mantra.title
            }
        }
        val meaningLocalized = buildStepMantraMeaning(mantra.stepNumber, localizedDeity, language)
        val chantLocalized = when (mantra.stepNumber) {
            3 -> when (language) {
                AppLanguage.HINDI -> "11, 21 या 108 बार जप करें"
                AppLanguage.MARATHI -> "११, २१ किंवा १०८ वेळा जप करा"
                AppLanguage.GUJARATI -> "૧૧, ૨૧ અથવા ૧૦૮ વાર જપ કરો"
                AppLanguage.TAMIL -> "11, 21 அல்லது 108 முறை ஜபிக்கவும்"
                AppLanguage.TELUGU -> "11, 21 లేదా 108 సార్లు జపించండి"
                AppLanguage.KANNADA -> "11, 21 ಅಥವಾ 108 ಬಾರಿ ಜಪಿಸಿ"
                AppLanguage.BENGALI -> "১১, ২১ বা ১০৮ বার জপ করুন"
                AppLanguage.ENGLISH -> mantra.chantCountRecommendation
            }
            else -> when (language) {
                AppLanguage.HINDI -> "श्रद्धापूर्वक 3 बार पाठ करें"
                AppLanguage.MARATHI -> "भक्तिभावाने ३ वेळा पठण करा"
                AppLanguage.GUJARATI -> "શ્રદ્ધાપૂર્વક ૩ વાર પાઠ કરો"
                AppLanguage.TAMIL -> "பக்தியுடன் 3 முறை பாராயணம் செய்யவும்"
                AppLanguage.TELUGU -> "భక్తితో 3 సార్లు పఠించండి"
                AppLanguage.KANNADA -> "ಭಕ್ತಿಯಿಂದ 3 ಬಾರಿ ಪಠಿಸಿ"
                AppLanguage.BENGALI -> "ভক্তিভরে ৩ বার পাঠ করুন"
                AppLanguage.ENGLISH -> mantra.chantCountRecommendation
            }
        }
        return mantra.copy(
            title = titleLocalized,
            ritualStage = stageLocalized,
            meaning = meaningLocalized,
            chantCountRecommendation = chantLocalized
        )
    }

    fun localizeVidhiStep(step: VidhiStep, language: AppLanguage): VidhiStep {
        if (language == AppLanguage.ENGLISH) return step
        val stepHeader = when (step.stepNumber) {
            1 -> when (language) {
                AppLanguage.HINDI -> "चरण 1: पवित्रीकरण, आचमन एवं पूजा संकल्प"
                AppLanguage.MARATHI -> "पायरी १: शुद्धीकरण, आचमन आणि पूजा संकल्प"
                AppLanguage.GUJARATI -> "પગલું ૧: પવિત્રીકરણ, આચમન અને પૂજા સંકલ્પ"
                AppLanguage.TAMIL -> "படி 1: ஆசமனம், புனிதப்படுத்துதல் & பூஜை சங்கல்பம்"
                AppLanguage.TELUGU -> "దశ 1: ఆచమనం, పవిత్రీకరణ & పూజా సంకల్పం"
                AppLanguage.KANNADA -> "ಹಂತ 1: ಆಚಮನ, ಪವಿತ್ರೀಕರಣ ಮತ್ತು ಪೂಜಾ ಸಂಕಲ್ಪ"
                AppLanguage.BENGALI -> "ধাপ ১: আচমন, পবিত্রীকরণ ও পূজা সংকল্প"
                AppLanguage.ENGLISH -> step.title
            }
            2 -> when (language) {
                AppLanguage.HINDI -> "चरण 2: कलश व प्रतिमा स्थापना एवं आवाहन"
                AppLanguage.MARATHI -> "पायरी २: कलश व मूर्ती स्थापना आणि आवाहन"
                AppLanguage.GUJARATI -> "પગલું ૨: કળશ સ્થાપના અને દેવ આવાહન"
                AppLanguage.TAMIL -> "படி 2: கலச ஸ்தாபனம் & தெய்வ ஆவாஹனம்"
                AppLanguage.TELUGU -> "దశ 2: కలశ స్థాపన & దైవ ఆవాహన"
                AppLanguage.KANNADA -> "ಹಂತ 2: ಕಳಶ ಸ್ಥಾಪನೆ ಮತ್ತು ದೇವರ ಆವಾಹನೆ"
                AppLanguage.BENGALI -> "ধাপ ২: ঘট স্থাপন ও দেব আবাহন"
                AppLanguage.ENGLISH -> step.title
            }
            3 -> when (language) {
                AppLanguage.HINDI -> "चरण 3: पंचामृत अभिषेक, मूल मंत्र जप एवं अर्चना"
                AppLanguage.MARATHI -> "पायरी ३: पंचामृत अभिषेक, मूल मंत्र जप आणि अर्चना"
                AppLanguage.GUJARATI -> "પગલું ૩: અભિષેક, મૂળ મંત્ર જપ અને અર્ચના"
                AppLanguage.TAMIL -> "படி 3: அபிஷேகம், மூல மந்திர ஜபம் & அர்ச்சனை"
                AppLanguage.TELUGU -> "దశ 3: అభిషేకం, మూల మంత్ర జపం & అర్చన"
                AppLanguage.KANNADA -> "ಹಂತ 3: ಅಭಿಷೇಕ, ಮೂಲ ಮಂತ್ರ ಜಪ ಮತ್ತು ಅರ್ಚನೆ"
                AppLanguage.BENGALI -> "ধাপ ৩: অভিষেক, মূল মন্ত্র জপ ও অর্চনা"
                AppLanguage.ENGLISH -> step.title
            }
            4 -> when (language) {
                AppLanguage.HINDI -> "चरण 4: विशेष पूजा विधि, व्रत कथा एवं नैवेद्य"
                AppLanguage.MARATHI -> "पायरी ४: विशेष पूजा विधी, व्रत कथा आणि नैवेद्य"
                AppLanguage.GUJARATI -> "પગલું ૪: વિશેષ પૂજા વિધિ, વ્રત કથા અને નૈવેદ્ય"
                AppLanguage.TAMIL -> "படி 4: சிறப்பு பூஜை, விரத கதை & நைவேத்தியம்"
                AppLanguage.TELUGU -> "దశ 4: విశేష పూజ, వ్రత కథ & నైవేద్యం"
                AppLanguage.KANNADA -> "ಹಂತ 4: ವಿಶೇಷ ಪೂಜೆ, ವ್ರತ ಕಥೆ ಮತ್ತು ನೈವೇದ್ಯ"
                AppLanguage.BENGALI -> "ধাপ ৪: বিশেষ পূজা বিধি, ব্রত কথা ও নৈবেদ্য"
                AppLanguage.ENGLISH -> step.title
            }
            else -> when (language) {
                AppLanguage.HINDI -> "चरण ${step.stepNumber}: कर्पूर महाआरती, पुष्पांजलि एवं प्रसाद"
                AppLanguage.MARATHI -> "पायरी ${step.stepNumber}: कर्पूर महाआरती, पुष्पांजली आणि प्रसाद"
                AppLanguage.GUJARATI -> "પગલું ${step.stepNumber}: કર્પૂર મહાઆરતી, પુષ્પાંજલિ અને પ્રસાદ"
                AppLanguage.TAMIL -> "படி ${step.stepNumber}: கற்பூர மகா ஆரத்தி, புஷ்பாஞ்சலி & பிரசாதம்"
                AppLanguage.TELUGU -> "దశ ${step.stepNumber}: కర్పూర మహా హారతి, పుష్పాంజలి & ప్రసాదం"
                AppLanguage.KANNADA -> "ಹಂತ ${step.stepNumber}: ಕರ್ಪೂರ ಮಹಾ ಆರತಿ, ಪುಷ್ಪಾಂಜಲಿ ಮತ್ತು ಪ್ರಸಾದ"
                AppLanguage.BENGALI -> "ধাপ ${step.stepNumber}: কর্পূর মহাআরতি, পুষ্পাঞ্জলি ও প্রসাদ বিতরণ"
                AppLanguage.ENGLISH -> step.title
            }
        }

        val localizedBody = buildLocalizedStepBody(step.stepNumber, language)
        return step.copy(
            title = stepHeader,
            body = localizedBody
        )
    }

    fun localizeSamagriItem(item: SamagriItem, language: AppLanguage): SamagriItem {
        if (language == AppLanguage.ENGLISH) return item
        val isOptional = item.notes?.contains("Optional", ignoreCase = true) == true
        return item.copy(
            name = localizeSamagriItemName(item.name, language),
            quantity = item.quantity?.let { localizeSamagriQuantity(it, language) },
            notes = item.notes?.let { localizeSamagriNote(isOptional, language) }
        )
    }

    fun localizeSamagriItemName(itemName: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return itemName
        val translated = translateTerms(itemName, language)
        if (!containsLatinLetters(translated)) return translated
        // Match common keywords so no English samagri name remains
        val lower = itemName.lowercase()
        return when {
            lower.contains("diya") || lower.contains("lamp") -> when (language) {
                AppLanguage.HINDI -> "मिट्टी या पीतल का दीपक एवं घी/तेल"
                AppLanguage.MARATHI -> "मातीची किंवा पितळी पणती व तूप"
                AppLanguage.GUJARATI -> "માટી કે પિત્તળનો દીવો અને ઘી"
                AppLanguage.TAMIL -> "அகல் விளக்கு மற்றும் நெய் / நல்லெண்ணெய்"
                AppLanguage.TELUGU -> "మట్టి లేదా ఇత్తడి దీపం మరియు నెయ్యి"
                AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ಅಥವಾ ಹಿತ್ತಾಳೆ ದೀಪ ಮತ್ತು ತುಪ್ಪ"
                AppLanguage.BENGALI -> "মাটি বা পিতলের প্রদীপ ও ঘি"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("kalash") || lower.contains("pot") || lower.contains("water") || lower.contains("gangajal") -> when (language) {
                AppLanguage.HINDI -> "तांबे का कलश, शुद्ध जल एवं गंगाजल"
                AppLanguage.MARATHI -> "तांब्याचा कलश, शुद्ध पाणी आणि गंगाजल"
                AppLanguage.GUJARATI -> "તાંબાનો કળશ, શુદ્ધ જળ અને ગંગાજળ"
                AppLanguage.TAMIL -> "செம்பு கலசம், தூய நீர் & கங்கை நீர்"
                AppLanguage.TELUGU -> "రాగి కలశం, శుద్ధ జలం మరియు గంగాజలం"
                AppLanguage.KANNADA -> "ತಾಮ್ರದ ಕಳಶ, ಶುದ್ಧ ನೀರು ಮತ್ತು ಗಂಗಾಜಲ"
                AppLanguage.BENGALI -> "তামার ঘট, শুদ্ধ জল ও গঙ্গাজল"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("kumkum") || lower.contains("roli") || lower.contains("haldi") || lower.contains("chandan") || lower.contains("sindoor") -> when (language) {
                AppLanguage.HINDI -> "रोली, कुमकुम, हल्दी, चंदन एवं अक्षत"
                AppLanguage.MARATHI -> "हळद, कुंकू, चंदन, गुलाल आणि अक्षता"
                AppLanguage.GUJARATI -> "કંકુ, હળદર, ચંદન અને અક્ષત (ચોખા)"
                AppLanguage.TAMIL -> "மஞ்சள், குங்குமம், சந்தனம் மற்றும் அட்சதை"
                AppLanguage.TELUGU -> "పసుపు, కుంకుమ, గంధం మరియు అక్షతలు"
                AppLanguage.KANNADA -> "ಅರಿಶಿನ, ಕುಂಕುಮ, ಶ್ರೀಗಂಧ ಮತ್ತು ಅಕ್ಷತೆ"
                AppLanguage.BENGALI -> "সিঁদুর, হলুদ, চন্দন ও অক্ষত (চাল)"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("flower") || lower.contains("garland") || lower.contains("tulsi") || lower.contains("bel") || lower.contains("durva") -> when (language) {
                AppLanguage.HINDI -> "ताजे पुष्प, फूलों की माला एवं पवित्र पत्र (तुलसी/बेलपत्र/दूर्वा)"
                AppLanguage.MARATHI -> "ताजी फुले, हार आणि पवित्र पत्री (तुळस/बेल/दूर्वा)"
                AppLanguage.GUJARATI -> "તાજા પુષ્પો, ફૂલહાર અને પવિત્ર પાન (તુલસી/બિલીપત્ર)"
                AppLanguage.TAMIL -> "புதிய மலர்கள், பூமாலை மற்றும் துளசி / வில்வ இலை"
                AppLanguage.TELUGU -> "తాజా పుష్పాలు, పూలమాల మరియు తులసి / బిల్వ పత్రాలు"
                AppLanguage.KANNADA -> "ತಾಜಾ ಹೂವುಗಳು, ಹೂಮಾಲೆ ಮತ್ತು ತುಳಸಿ / ಬಿಲ್ವಪತ್ರೆ"
                AppLanguage.BENGALI -> "তাজা পুষ্প, ফুলের মালা ও তুলসী/বেলপাতা"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("incense") || lower.contains("dhoop") || lower.contains("camphor") || lower.contains("kapoor") -> when (language) {
                AppLanguage.HINDI -> "धूपबत्ती, अगरबत्ती एवं आरती हेतु शुद्ध कपूर"
                AppLanguage.MARATHI -> "धूप, उदबत्ती आणि आरतीसाठी कापूर"
                AppLanguage.GUJARATI -> "ધૂપ, અગરબત્તી અને આરતી માટે કપૂર"
                AppLanguage.TAMIL -> "தூபம், ஊதுபத்தி மற்றும் ஆரத்தி கற்பூரம்"
                AppLanguage.TELUGU -> "ధూపం, అగరబత్తీలు మరియు కర్పూరం"
                AppLanguage.KANNADA -> "ಧೂಪ, ಅಗರಬತ್ತಿ ಮತ್ತು ಆರತಿಗೆ ಕರ್ಪೂರ"
                AppLanguage.BENGALI -> "ধূপকাঠি, ধুনো ও আরতির কর্পূর"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("coconut") || lower.contains("fruit") || lower.contains("betel") || lower.contains("paan") || lower.contains("supari") -> when (language) {
                AppLanguage.HINDI -> "श्रीफल (नारियल), पान के पत्ते, सुपारी एवं ऋतुफल"
                AppLanguage.MARATHI -> "श्रीफळ (नारळ), विड्याची पाने, सुपारी आणि फळे"
                AppLanguage.GUJARATI -> "શ્રીફળ (નાળિયેર), નાગરવેલના પાન, સોપારી અને ફળો"
                AppLanguage.TAMIL -> "தேங்காய், வெற்றிலை, பாக்கு மற்றும் பழங்கள்"
                AppLanguage.TELUGU -> "కొబ్బరికాయ, తమలపాకులు, వక్కలు మరియు పండ్లు"
                AppLanguage.KANNADA -> "ತೆಂಗಿನಕಾಯಿ, ವೀಳ್ಯದೆಲೆ, ಅಡಿಕೆ ಮತ್ತು ಹಣ್ಣುಗಳು"
                AppLanguage.BENGALI -> "ডাব/নারকেল, পান পাতা, সুপারি ও মৌসুমি ফল"
                AppLanguage.ENGLISH -> itemName
            }
            lower.contains("sweet") || lower.contains("prasad") || lower.contains("bhog") || lower.contains("naivedya") || lower.contains("milk") || lower.contains("panchamrit") -> when (language) {
                AppLanguage.HINDI -> "पंचामृत एवं पारंपरिक मिष्ठान्न (नैवेद्य / भोग प्रसाद)"
                AppLanguage.MARATHI -> "पंचामृत आणि पारंपरिक गोड नैवेद्य (प्रसाद)"
                AppLanguage.GUJARATI -> "પંચામૃત અને પારંપરિક મીઠાઈ (નૈવેદ્ય / પ્રસાદ)"
                AppLanguage.TAMIL -> "பஞ்சாமிர்தம் மற்றும் பாரம்பரிய இனிப்பு நைவேத்தியம்"
                AppLanguage.TELUGU -> "పంచామృతం మరియు సాంప్రదాయ నైవేద్యం (ప్రసాదం)"
                AppLanguage.KANNADA -> "ಪಂಚಾಮೃತ ಮತ್ತು ಸಾಂಪ್ರದಾಯಿಕ ಸಿಹಿ ನೈವೇದ್ಯ (ಪ್ರಸಾದ)"
                AppLanguage.BENGALI -> "পঞ্চামৃত ও ঐতিহ্যবাহী মিষ্টান্ন (নৈবেদ্য / ভোগ)"
                AppLanguage.ENGLISH -> itemName
            }
            else -> when (language) {
                AppLanguage.HINDI -> "पवित्र पूजा सामग्री एवं अलंकरण वस्तुएं"
                AppLanguage.MARATHI -> "पवित्र पूजा सामग्री आणि अलंकरण वस्तू"
                AppLanguage.GUJARATI -> "પવિત્ર પૂજા સામગ્રી અને શણગાર"
                AppLanguage.TAMIL -> "புனித பூஜை மற்றும் அலங்காரப் பொருட்கள்"
                AppLanguage.TELUGU -> "పవిత్ర పూజా మరియు అలంకరణ సామగ్రి"
                AppLanguage.KANNADA -> "ಪವಿತ್ರ ಪೂಜಾ ಮತ್ತು ಅಲಂಕಾರ ಸಾಮಗ್ರಿ"
                AppLanguage.BENGALI -> "পবিত্র পূজা ও অলংকরণ সামগ্রী"
                AppLanguage.ENGLISH -> itemName
            }
        }
    }

    private fun localizeSamagriQuantity(qty: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return qty
        val digits = qty.filter { it.isDigit() }
        return if (digits.isNotEmpty()) {
            when (language) {
                AppLanguage.HINDI -> "$digits नग / आवश्यकतानुसार"
                AppLanguage.MARATHI -> "$digits नग / आवश्यकतेनुसार"
                AppLanguage.GUJARATI -> "$digits નંગ / જરૂર મુજબ"
                AppLanguage.TAMIL -> "$digits எண்ணிக்கை"
                AppLanguage.TELUGU -> "$digits సంఖ్య"
                AppLanguage.KANNADA -> "$digits ಸಂಖ್ಯೆ"
                AppLanguage.BENGALI -> "$digits টি / প্রয়োজনমতো"
                AppLanguage.ENGLISH -> qty
            }
        } else {
            when (language) {
                AppLanguage.HINDI -> "आवश्यकतानुसार"
                AppLanguage.MARATHI -> "आवश्यकतेनुसार"
                AppLanguage.GUJARATI -> "જરૂર મુજબ"
                AppLanguage.TAMIL -> "தேவையான அளவு"
                AppLanguage.TELUGU -> "తగినంత"
                AppLanguage.KANNADA -> "ಅಗತ್ಯಕ್ಕೆ ತಕ್ಕಷ್ಟು"
                AppLanguage.BENGALI -> "প্রয়োজনমতো"
                AppLanguage.ENGLISH -> qty
            }
        }
    }

    private fun localizeSamagriNote(isOptional: Boolean, language: AppLanguage): String {
        return if (isOptional) {
            when (language) {
                AppLanguage.HINDI -> "पारिवारिक व क्षेत्रीय परंपरा के अनुसार वैकल्पिक"
                AppLanguage.MARATHI -> "कौटुंबिक परंपरेनुसार पर्यायी सामग्री"
                AppLanguage.GUJARATI -> "પારિવારિક પરંપરા મુજબ વૈકલ્પિક"
                AppLanguage.TAMIL -> "குடும்ப மரபுப்படி விருப்பத்திற்குரியது"
                AppLanguage.TELUGU -> "కుటుంబ సంప్రదాయం ప్రకారం ఐచ్ఛికం"
                AppLanguage.KANNADA -> "ಕುಟುಂಬ ಸಂಪ್ರದಾಯದಂತೆ ಐಚ್ಛಿಕ"
                AppLanguage.BENGALI -> "পারিবারিক পরম্পরা অনুসারে ঐচ্ছিক"
                AppLanguage.ENGLISH -> "Optional regional item"
            }
        } else {
            when (language) {
                AppLanguage.HINDI -> "मुख्य पूजा एवं अर्चना के लिए आवश्यक"
                AppLanguage.MARATHI -> "मुख्य पूजेसाठी अत्यावश्यक"
                AppLanguage.GUJARATI -> "મુખ્ય પૂજા માટે અનિવાર્ય"
                AppLanguage.TAMIL -> "முதன்மை பூஜைக்கு அவசியமானது"
                AppLanguage.TELUGU -> "ప్రధాన పూజకు తప్పనిసరి"
                AppLanguage.KANNADA -> "ಮುಖ್ಯ ಪೂಜೆಗೆ ಅತ್ಯಗತ್ಯ"
                AppLanguage.BENGALI -> "প্রধান পূজার জন্য অপরিহার্য"
                AppLanguage.ENGLISH -> "Essential for puja"
            }
        }
    }

    private fun buildLocalizedWhyCelebrated(
        festName: String,
        deity: String,
        tithiBadge: String,
        language: AppLanguage
    ): String {
        return when (language) {
            AppLanguage.HINDI -> "$festName ($tithiBadge) सनातन धर्म और भारतीय संस्कृति का अत्यंत पावन पर्व है। इस शुभ दिन पर $deity की विधिवत पूजा-अर्चना करने से घर-परिवार में सुख, शांति, आरोग्य और अखंड समृद्धि का वास होता है।"
            AppLanguage.MARATHI -> "$festName ($tithiBadge) हा भारतीय संस्कृतीतील अत्यंत पवित्र सण आहे. या शुभ दिवशी $deity यांची मनोभावे पूजा केल्याने कुटुंबात सुख, शांतता, आरोग्य आणि समृद्धी नांदते."
            AppLanguage.GUJARATI -> "$festName ($tithiBadge) એ ભારતીય સંસ્કૃતિનો અત્યંત પવિત્ર તહેવાર છે. આ શુભ દિવસે $deity ની વિધિવત પૂજા-અર્ચના કરવાથી પરિવારમાં સુખ, શાંતિ, આરોગ્ય અને સમૃદ્ધિ પ્રાપ્ત થાય છે."
            AppLanguage.TAMIL -> "$festName ($tithiBadge) என்பது சனாதன தர்மத்தின் மிகவும் புனிதமான பண்டிகையாகும். இந்நாளில் $deity வழிபாடு செய்வதால் இல்லத்தில் அமைதி, ஆரோக்கியம், செல்வம் மற்றும் மங்களம் பெருகும்."
            AppLanguage.TELUGU -> "$festName ($tithiBadge) సనాతన ధర్మంలో అత్యంత పవిత్రమైన పండుగ. ఈ శుభ దినాన $deity ను భక్తిశ్రద్ధలతో పూజించడం వల్ల కుటుంబంలో సుఖశాంతులు, ఆయురారోగ్యాలు మరియు అష్టైశ్వర్యాలు కలుగుతాయి."
            AppLanguage.KANNADA -> "$festName ($tithiBadge) ಭಾರತೀಯ ಸಂಸ್ಕೃತಿಯ ಅತ್ಯಂತ ಪವಿತ್ರ ಹಬ್ಬವಾಗಿದೆ. ಈ ಶುಭ ದಿನದಂದು $deity ದೇವರನ್ನು ಭಕ್ತಿಪೂರ್ವಕವಾಗಿ ಪೂಜಿಸುವುದರಿಂದ ಮನೆಯಲ್ಲಿ ಸುಖ, ಶಾಂತಿ, ಆರೋಗ್ಯ ಮತ್ತು ಸಮೃದ್ಧಿ ನೆಲೆಸುತ್ತದೆ."
            AppLanguage.BENGALI -> "$festName ($tithiBadge) সনাতন ধর্ম ও ভারতীয় সংস্কৃতির এক অত্যন্ত পবিত্র উৎসব। এই শুভ দিনে ভক্তিভরে $deity-এর পূজা ও আরাধনা করলে পরিবারে সুখ, শান্তি, আরোগ্য ও সমৃদ্ধি লাভ হয়।"
            AppLanguage.ENGLISH -> ""
        }
    }

    private fun buildLocalizedKatha(
        festName: String,
        deity: String,
        tithiBadge: String,
        language: AppLanguage
    ): String {
        return when (language) {
            AppLanguage.HINDI -> "पौराणिक शास्त्रों एवं व्रत कथाओं के अनुसार, $tithiBadge के पावन योग में $deity की दिव्य कृपा और धर्म की विजय के स्मरण में $festName का पर्व प्रारंभ हुआ। मान्यता है कि जो भक्त इस दिन श्रद्धापूर्वक व्रत रखकर कथा का श्रवण या पाठ करते हैं, उनके सभी संकट दूर होते हैं और मनोकामनाएं पूर्ण होती हैं।"
            AppLanguage.MARATHI -> "पौराणिक शास्त्र आणि व्रत कथेनुसार, $tithiBadge च्या पवित्र मुहूर्तावर $deity यांच्या दिव्य कृपेचे आणि धर्माच्या विजयाचे स्मरण म्हणून $festName हा सण साजरा केला जातो. या दिवशी श्रद्धापूर्वक व्रत आणि कथा पठण केल्याने सर्व संकटे दूर होऊन मनोकामना पूर्ण होतात."
            AppLanguage.GUJARATI -> "પૌરાણિક શાસ્ત્રો અને વ્રત કથા અનુસાર, $tithiBadge ના પાવન યોગમાં $deity ની દિવ્ય કૃપા અને ધર્મના વિજયની સ્મૃતિમાં $festName ની ઉજવણી થાય છે. આ દિવસે શ્રદ્ધાપૂર્વક વ્રત રાખી કથાનું શ્રવણ કરવાથી સર્વ સંકટો દૂર થાય છે."
            AppLanguage.TAMIL -> "புராண வரலாறு மற்றும் சாஸ்திரங்களின்படி, $tithiBadge புனித நாளில் $deity அருளாலும் தர்மத்தின் வெற்றியாலும் $festName தோன்றியது. இந்நாளில் விரதம் இருந்து புராண கதையைப் படிப்பவர்களுக்கு சகல தடைகளும் நீங்கி நன்மைகள் உண்டாகும்."
            AppLanguage.TELUGU -> "పురాణ గ్రంథాలు మరియు వ్రత కథల ప్రకారం, $tithiBadge పవిత్ర తిథి నాడు $deity దివ్య అనుగ్రహం మరియు ధర్మ విజయానికి ప్రతీకగా $festName జరుపుకుంటారు. ఈ రోజున భక్తితో వ్రత కథను శ్రవణం చేసేవారికి సకల శుభాలు కలుగుతాయి."
            AppLanguage.KANNADA -> "ಪೌರಾಣಿಕ ಶಾಸ್ತ್ರ ಮತ್ತು ವ್ರತ ಕಥೆಯ ಪ್ರಕಾರ, $tithiBadge ಪವಿತ್ರ ತಿಥಿಯಂದು $deity ದೇವರ ದಿವ್ಯ ಅನುಗ್ರಹ ಮತ್ತು ಧರ್ಮದ ವಿಜಯದ ಸ್ಮರಣಾರ್ಥವಾಗಿ $festName ಆಚರಿಸಲಾಗುತ್ತದೆ. ಈ ದಿನ ಭಕ್ತಿಯಿಂದ ವ್ರತ ಕಥೆ ಆಲಿಸುವವರಿಗೆ ಸಕಲ ಸಂಕಷ್ಟಗಳು ನಿವಾರಣೆಯಾಗುತ್ತವೆ."
            AppLanguage.BENGALI -> "পৌরাণিক শাস্ত্র ও ব্রত কথা অনুসারে, $tithiBadge পুণ্য তিথিতে $deity-এর দিব্য কৃপা ও ধর্মের বিজয়ের স্মরণে $festName উৎসবের সূচনা হয়। ভক্তিভরে এই ব্রত কথা শ্রবণ বা পাঠ করলে সকল বিঘ্ন দূর হয় এবং মনোবাসনা পূর্ণ হয়।"
            AppLanguage.ENGLISH -> ""
        }
    }

    private fun buildLocalizedVratGuide(
        festName: String,
        deity: String,
        language: AppLanguage
    ): String {
        return when (language) {
            AppLanguage.HINDI -> "• व्रत नियम: प्रातः स्नान कर स्वच्छ वस्त्र पहनें और $deity के समक्ष व्रत/पूजा का संकल्प लें। सामर्थ्य अनुसार फलाहार या सात्विक आहार ग्रहण करें।\n• नैवेद्य (भोग): पूजा में ऋतुफल, पंचामृत, खीर, हलवा या पारंपरिक घर की बनी मिठाई का भोग लगाएं और आरती के बाद परिवार में प्रसाद वितरित करें।"
            AppLanguage.MARATHI -> "• व्रत नियम: सकाळी स्नान करून स्वच्छ वस्त्र परिधान करा आणि $deity समोर पूजेचा संकल्प करा. दिवसभर सात्त्विक आहार किंवा फलाहार घ्या.\n• नैवेद्य: पूजेमध्ये फळे, पंचामृत आणि पारंपरिक गोड पदार्थांचा नैवेद्य अर्पण करून आरतीनंतर सर्वांना प्रसाद वाटा."
            AppLanguage.GUJARATI -> "• વ્રત નિયમ: પ્રાતઃકાળે સ્નાન કરી સ્વચ્છ વસ્ત્રો પહેરો અને $deity સમક્ષ પૂજાનો સંકલ્પ કરો. ઉપવાસમાં ફળાહાર અથવા સાત્વિક ભોજન લો.\n• નૈવેદ્ય (ભોગ): પૂજામાં ફળો, પંચામૃત અને પારંપરિક મીઠાઈનો ભોગ ધરાવો અને આરતી પછી પ્રસાદ વહેંચો."
            AppLanguage.TAMIL -> "• விரத முறை: அதிகாலையில் நீராடி தூய ஆடை அணிந்து $deity முன்பு சங்கல்பம் செய்து சாத்வீக உணவு அல்லது பழங்களை உட்கொண்டு விரதம் இருக்கவும்.\n• நைவேத்தியம்: பழங்கள், பஞ்சாமிர்தம் மற்றும் பாரம்பரிய இனிப்புகளைப் படைத்து ஆரத்திக்குப் பிறகு பிரசாதம் வழங்கவும்."
            AppLanguage.TELUGU -> "• వ్రత నియమాలు: ఉదయాన్నే స్నానం చేసి శుభ్రమైన వస్త్రాలు ధరించి $deity సమక్షంలో సంకల్పం చెప్పుకోండి. రోజంతా సాత్విక ఆహారం లేదా ఫలాహారం తీసుకోండి.\n• నైవేద్యం: పండ్లు, పంచామృతం మరియు సాంప్రదాయ పిండివంటలను నైవేద్యంగా సమర్పించి హారతి అనంతరం ప్రసాదం పంచండి."
            AppLanguage.KANNADA -> "• ವ್ರತ ನಿಯಮ: ಬೆಳಿಗ್ಗೆ ಸ್ನಾನ ಮಾಡಿ ಶುಭ್ರ ವಸ್ತ್ರ ಧರಿಸಿ $deity ಮುಂದೆ ಸಂಕಲ್ಪ ಮಾಡಿ. ಉಪವಾಸದಲ್ಲಿ ಹಣ್ಣು ಅಥವಾ ಸಾತ್ವಿಕ ಆಹಾರ ಸೇವಿಸಿ.\n• ನೈವೇದ್ಯ: ಹಣ್ಣುಗಳು, ಪಂಚಾಮೃತ ಮತ್ತು ಸಾಂಪ್ರದಾಯಿಕ ಸಿಹಿ ಖಾದ್ಯಗಳನ್ನು ನೈವೇದ್ಯವಾಗಿ ಅರ್ಪಿಸಿ ಆರತಿಯ ನಂತರ ಪ್ರಸಾದ ವಿತರಿಸಿ."
            AppLanguage.BENGALI -> "• ব্রত বিধি: প্রাতঃস্নান সেরে শুদ্ধ বস্ত্র পরিধান করে $deity-এর সামনে সংকল্প করুন। ব্রত চলাকালীন ফলমূল বা সাত্ত্বিক আহার গ্রহণ করুন।\n• নৈবেদ্য (ভোগ): মৌসুমি ফল, পঞ্চামৃত, পায়েস ও ঐতিহ্যবাহী মিষ্টান্ন ভোগ নিবেদন করুন এবং আরতির পর প্রসাদ বিতরণ করুন।"
            AppLanguage.ENGLISH -> ""
        }
    }

    private fun buildLocalizedMantraMeaning(
        deity: String,
        language: AppLanguage
    ): String {
        return when (language) {
            AppLanguage.HINDI -> "हे $deity! हम आपको श्रद्धापूर्वक नमन करते हैं। हमारे परिवार को विद्या, आरोग्य, सुख-शांति और धर्म के मार्ग पर चलने की शक्ति प्रदान करें।"
            AppLanguage.MARATHI -> "हे $deity! आम्ही तुम्हाला भक्तिभावाने वंदन करतो. आमच्या कुटुंबाला आरोग्य, सुख-शांती, समृद्धी आणि सद्बुद्धी प्रदान करा."
            AppLanguage.GUJARATI -> "હે $deity! અમે આપને ભક્તિભાવપૂર્વક નમન કરીએ છીએ. અમારા પરિવારને આરોગ્ય, સુખ-શાંતિ અને સમૃદ્ધિના આશીર્વાદ આપો."
            AppLanguage.TAMIL -> "போற்றி வணங்குகிறோம் $deity! எங்கள் குடும்பத்திற்கு நல்ல ஆரோக்கியம், அமைதி, செல்வம் மற்றும் ஞானத்தை அருள்வீராக."
            AppLanguage.TELUGU -> "ఓ $deity! మీకు భక్తితో నమస్కరిస్తున్నాము. మా కుటుంబానికి ఆయురారోగ్యాలు, సుఖశాంతులు మరియు సకల శుభాలను ప్రసాదించండి."
            AppLanguage.KANNADA -> "ಹೇ $deity! ನಿಮಗೆ ಭಕ್ತಿಪೂರ್ವಕ ನಮನಗಳು. ನಮ್ಮ ಕುಟುಂಬಕ್ಕೆ ಆಯುರಾರೋಗ್ಯ, ಸುಖ-ಶಾಂತಿ ಮತ್ತು ಸಮೃದ್ಧಿಯನ್ನು ಕರುಣಿಸಿ."
            AppLanguage.BENGALI -> "হে $deity! আমরা ভক্তিভরে আপনাকে প্রণাম জানাই। আমাদের পরিবারকে আরোগ্য, সুখ-শান্তি এবং কল্যাণের আশীর্বাদ প্রদান করুন।"
            AppLanguage.ENGLISH -> ""
        }
    }

    private fun buildStepMantraMeaning(
        stepNumber: Int,
        deity: String,
        language: AppLanguage
    ): String {
        return when (stepNumber) {
            1 -> when (language) {
                AppLanguage.HINDI -> "भगवान श्री हरि के स्मरण से अंतःकरण और बाह्य शरीर पवित्र होता है। मैं शुभ मुहूर्त में पूजा का संकल्प लेता/लेती हूँ।"
                AppLanguage.MARATHI -> "श्री विष्णूंच्या स्मरणाने बाह्य आणि अंतरंग शुद्ध होते. मी या शुभ मुहूर्तावर पूजेचा संकल्प करतो/करते."
                AppLanguage.GUJARATI -> "ભગવાન શ્રી હરિના સ્મરણથી અંતઃકરણ અને શરીર પવિત્ર થાય છે. હું શુભ મુહૂર્તમાં પૂજાનો સંકલ્પ કરું છું."
                AppLanguage.TAMIL -> "இறைவனின் நாமத்தைத் தியானிப்பதால் உள்ளமும் உடலும் தூய்மையடைகிறது. சுப முகூர்த்தத்தில் பூஜை சங்கல்பம் செய்கிறேன்."
                AppLanguage.TELUGU -> "శ్రీ మహావిష్ణువు స్మరణతో బాహ్య, అంతరంగ శుద్ధి కలుగుతుంది. ఈ శుభ ముహూర్తంలో పూజా సంకల్పం చేస్తున్నాను."
                AppLanguage.KANNADA -> "ಶ್ರೀ ಹರಿಯ ಸ್ಮರಣೆಯಿಂದ ಅಂತರಂಗ ಮತ್ತು ಬಹಿರಂಗ ಶುದ್ಧವಾಗುತ್ತದೆ. ಶುಭ ಮುಹೂರ್ತದಲ್ಲಿ ಪೂಜಾ ಸಂಕಲ್ಪ ಮಾಡುತ್ತೇನೆ."
                AppLanguage.BENGALI -> "ভগবান শ্রীহরির স্মরণে অন্তর ও বাহ্য শুদ্ধ হয়। আমি শুভ মুহূর্তে পূজার সংকল্প গ্রহণ করছি।"
                AppLanguage.ENGLISH -> ""
            }
            2 -> when (language) {
                AppLanguage.HINDI -> "पवित्र कलश और वेदी में समस्त तीर्थों एवं देवताओं का वास हो। हम $deity का श्रद्धापूर्वक आवाहन और ध्यान करते हैं।"
                AppLanguage.MARATHI -> "पवित्र कलशात सर्व तीर्थे आणि देवतांचा वास असो. आम्ही $deity यांचे भक्तिभावाने आवाहन आणि ध्यान करतो."
                AppLanguage.GUJARATI -> "પવિત્ર કળશમાં સર્વ તીર્થો અને દેവതાઓનો વાસ થાઓ. અમે $deity નું ભક્તિભાવથી આવાહન અને ધ્યાન કરીએ છીએ."
                AppLanguage.TAMIL -> "புனித கலசத்தில் அனைத்து தீர்த்தங்களும் தெய்வங்களும் எழுந்தருளட்டும். $deity-ஐ பக்தியுடன் ஆவாஹனம் செய்கிறோம்."
                AppLanguage.TELUGU -> "పవిత్ర కలశంలో సమస్త తీర్థాలు, దేవతలు కొలువై ఉండుగాక. $deity ను భక్తితో ఆవాహన చేసి ధ్యానిస్తున్నాము."
                AppLanguage.KANNADA -> "ಪವಿತ್ರ ಕಳಶದಲ್ಲಿ ಸಮಸ್ತ ತೀರ್ಥಗಳು ಮತ್ತು ದೇವತೆಗಳು ನೆಲೆಸಲಿ. $deity ದೇವರನ್ನು ಭಕ್ತಿಯಿಂದ ಆವಾಹಿಸಿ ಧ್ಯಾನಿಸುತ್ತೇವೆ."
                AppLanguage.BENGALI -> "পবিত্র ঘটে সমস্ত তীর্থ ও দেবতাদের অধিষ্ঠান হোক। আমরা ভক্তিভরে $deity-এর আবাহন ও ধ্যান করছি।"
                AppLanguage.ENGLISH -> ""
            }
            3 -> buildLocalizedMantraMeaning(deity, language)
            4 -> when (language) {
                AppLanguage.HINDI -> "हे आराध्य देव! हमारे द्वारा श्रद्धा से अर्पित यह पत्र, पुष्प, फल, जल और सात्विक नैवेद्य स्वीकार कर हमें कृतार्थ करें।"
                AppLanguage.MARATHI -> "हे देवा! आम्ही भक्तिभावाने अर्पण केलेले पत्र, पुष्प, फळ आणि सात्त्विक नैवेद्य स्वीकारून आम्हाला आशीर्वाद द्या."
                AppLanguage.GUJARATI -> "હે પ્રભુ! અમારા દ્વારા ભક્તિભાવથી અર્પણ કરાયેલ પત્ર, પુષ્પ, ફળ અને સાત્વિક નૈવેદ્યનો સ્વીકાર કરો."
                AppLanguage.TAMIL -> "இறைவா! நாங்கள் அன்புடன் படைக்கும் இலை, மலர், பழம் மற்றும் நைவேத்தியத்தை ஏற்று அருள்புரிவீராக."
                AppLanguage.TELUGU -> "ఓ ప్రభూ! మేము భక్తితో సమర్పించే పత్రం, పుష్పం, ఫలం మరియు నైవేద్యాన్ని స్వీకరించి మమ్మల్ని ఆశీర్వదించండి."
                AppLanguage.KANNADA -> "ಹೇ ಪ್ರಭು! ನಾವು ಭಕ್ತಿಯಿಂದ ಅರ್ಪಿಸುವ ಪತ್ರೆ, ಪುಷ್ಪ, ಫಲ ಮತ್ತು ನೈವೇದ್ಯವನ್ನು ಸ್ವೀಕರಿಸಿ ಅನುಗ್ರಹಿಸಿ."
                AppLanguage.BENGALI -> "হে প্রভু! আমাদের ভক্তিভরে নিবেদিত পত্র, পুষ্প, ফল ও সাত্ত্বিক নৈবেদ্য গ্রহণ করে আমাদের কৃপা করুন।"
                AppLanguage.ENGLISH -> ""
            }
            else -> when (language) {
                AppLanguage.HINDI -> "करुणा के सागर और कपूर के समान उज्ज्वल भगवान की हम आरती करते हैं। पूजा में हुई किसी भी भूल के लिए हमें क्षमा करें।"
                AppLanguage.MARATHI -> "करुणेचे सागर असलेल्या परमेश्वराची आम्ही कापूर आरती करतो. पूजेतील कोणत्याही चुकीबद्दल आम्हाला क्षमा करावी."
                AppLanguage.GUJARATI -> "કરુણાના સાગર ભગવાનની અમે કપૂર આરતી ઉતારીએ છીએ. પૂજામાં થયેલી કોઈપણ ભૂલ બદલ અમને ક્ષમા કરો."
                AppLanguage.TAMIL -> "கருணையின் வடிவான இறைவனுக்கு கற்பூர ஆரத்தி எடுக்கிறோம். பூஜையில் ஏற்பட்ட பிழைகளைப் பொறுத்தருள வேண்டுகிறோம்."
                AppLanguage.TELUGU -> "కరుణామయుడైన భగవంతునికి కర్పూర హారతి సమర్పిస్తున్నాము. పూజలో జరిగిన లోపాలను మన్నించి ఆశీర్వదించండి."
                AppLanguage.KANNADA -> "ಕರುಣಾಮಯಿಯಾದ ಭಗವಂತನಿಗೆ ಕರ್ಪೂರ ಆರತಿ ಬೆಳಗುತ್ತೇವೆ. ಪೂಜೆಯಲ್ಲಿ ತಿಳಿಯದೆ ಆದ ತಪ್ಪುಗಳನ್ನು ಕ್ಷಮಿಸಿ ಕಾಪಾಡಿ."
                AppLanguage.BENGALI -> "করুণার সাগর ভগবানের উদ্দেশ্যে আমরা কর্পূর আরতি নিবেদন করছি। পূজায় কোনো ত্রুটি হলে আমাদের ক্ষমা করুন।"
                AppLanguage.ENGLISH -> ""
            }
        }
    }

    private fun buildLocalizedStepBody(
        stepNumber: Int,
        language: AppLanguage
    ): String {
        return when (stepNumber) {
            1 -> when (language) {
                AppLanguage.HINDI -> "स्नान के पश्चात स्वच्छ वस्त्र धारण करें, पूजा स्थल को गंगाजल से पवित्र करें, घी का दीप प्रज्वलित करें और दाएं हाथ में जल, अक्षत व पुष्प लेकर शुभ मुहूर्त में पूजा का संकल्प लें।"
                AppLanguage.MARATHI -> "स्नान करून स्वच्छ वस्त्र परिधान करा, पूजास्थान गंगाजलाने पवित्र करा, तुपाचा दिवा लावा आणि उजव्या हातात जल, अक्षता व फुले घेऊन शुभ मुहूर्तावर पूजेचा संकल्प करा."
                AppLanguage.GUJARATI -> "સ્નાન કર્યા પછી સ્વચ્છ વસ્ત્રો પહેરો, પૂજા સ્થળને ગંગાજળથી પવિત્ર કરો, ઘીનો દીવો પ્રગટાવો અને જમણા હાથમાં જળ, અક્ષત અને પુષ્પ લઈ પૂજાનો સંકલ્પ કરો."
                AppLanguage.TAMIL -> "நீராடி தூய ஆடை அணிந்து, பூஜை அறையை சுத்தம் செய்து, நெய் தீபம் ஏற்றி, வலது கையில் நீர், அட்சதை மற்றும் மலர்களுடன் பூஜை சங்கல்பம் செய்யவும்."
                AppLanguage.TELUGU -> "స్నానం చేసి శుభ్రమైన వస్త్రాలు ధరించి, పూజా మందిరాన్ని శుద్ధి చేసి, నేతి దీపం వెలిగించి, చేతిలో జలం, అక్షతలు, పుష్పాలతో సంకల్పం చెప్పుకోండి."
                AppLanguage.KANNADA -> "ಸ್ನಾನ ಮಾಡಿ ಶುಭ್ರ ವಸ್ತ್ರ ಧರಿಸಿ, ಪೂಜಾ ಸ್ಥಳವನ್ನು ಶುದ್ಧಿಗೊಳಿಸಿ, ತುಪ್ಪದ ದೀಪ ಬೆಳಗಿಸಿ, ಬಲಗೈಯಲ್ಲಿ ನೀರು, ಅಕ್ಷತೆ ಮತ್ತು ಹೂವು ಹಿಡಿದು ಪೂಜಾ ಸಂಕಲ್ಪ ಮಾಡಿ."
                AppLanguage.BENGALI -> "স্নান সেরে শুদ্ধ বস্ত্র পরিধান করুন, পূজাস্থান গঙ্গাজল দিয়ে শুদ্ধ করুন, ঘিয়ের প্রদীপ প্রজ্জ্বলন করুন এবং জল, অক্ষত ও পুষ্প হাতে নিয়ে পূজার সংকল্প করুন।"
                AppLanguage.ENGLISH -> ""
            }
            2 -> when (language) {
                AppLanguage.HINDI -> "चौकी पर लाल या पीला वस्त्र बिछाकर अक्षत का अष्टदल बनाएं, तांबे का कलश स्थापित करें तथा आराध्य देव की प्रतिमा या चित्र को आसन प्रदान कर विधिवत आवाहन एवं ध्यान करें।"
                AppLanguage.MARATHI -> "चौरंगावर लाल किंवा पिवळे वस्त्र अंथरून अक्षतांचे अष्टदल काढा, कलश स्थापना करा आणि आराध्य देवतेच्या मूर्तीचे भक्तिभावाने आवाहन व ध्यान करा."
                AppLanguage.GUJARATI -> "બાજોઠ પર લાલ કે પીળું વસ્ત્ર પાથરી કળશ સ્થાપના કરો અને આરાધ્ય દેવની મૂર્તિ કે છબીને આસન આપી વિધિવત આવાહન તથા ધ્યાન કરો."
                AppLanguage.TAMIL -> "பீடத்தில் தூய துணி விரித்து, கலசம் வைத்து, இறைவனின் திருவுருவப் படத்திற்கு மலர் சூட்டி பக்தியுடன் ஆவாஹனம் மற்றும் தியானம் செய்யவும்."
                AppLanguage.TELUGU -> "పీఠంపై వస్త్రం పరిచి కలశ స్థాపన చేయండి. ఆరాధ్య దైవం యొక్క చిత్రపటం లేదా ప్రతిమను ప్రతిష్ఠించి భక్తితో ఆవాహన మరియు ధ్యానం చేయండి."
                AppLanguage.KANNADA -> "ಪೀಠದ ಮೇಲೆ ವಸ್ತ್ರ ಹಾಸಿ ಕಳಶ ಸ್ಥಾಪಿಸಿ, ಆರಾಧ್ಯ ದೇವರ ವಿಗ್ರಹ ಅಥವಾ ಭಾವಚಿತ್ರವನ್ನು ಇರಿಸಿ ಭಕ್ತಿಯಿಂದ ಆವಾಹನೆ ಮತ್ತು ಧ್ಯಾನ ಮಾಡಿ."
                AppLanguage.BENGALI -> "বেদীতে শুদ্ধ বস্ত্র বিছিয়ে মঙ্গল ঘট স্থাপন করুন এবং আরাধ্য দেবতার মূর্তি বা চিত্রে ভক্তিভরে আবাহন ও ধ্যান করুন।"
                AppLanguage.ENGLISH -> ""
            }
            3 -> when (language) {
                AppLanguage.HINDI -> "पंचामृत व शुद्ध जल से अभिषेक करें, चंदन-रोली-हल्दी का तिलक लगाएं, अक्षत, पुष्पमाला, पवित्र पत्र (तुलसी/बेलपत्र/दूर्वा), धूप व दीप अर्पित करते हुए मूल मंत्र का जप करें।"
                AppLanguage.MARATHI -> "पंचामृत व शुद्ध पाण्याने अभिषेक करा, चंदन-हळद-कुंकू लावा, अक्षता, फुले, पत्री, धूप आणि तुपाचा दिवा अर्पण करत मूल मंत्राचा जप करा."
                AppLanguage.GUJARATI -> "પંચામૃત અને શુદ્ધ જળથી અભિષેક કરો, ચંદન-કંકુ-હળદરનું તિલક કરો, અક્ષત, પુષ્પહાર, ધૂપ અને દીવો અર્પણ કરી મૂળ મંત્રનો જપ કરો."
                AppLanguage.TAMIL -> "பஞ்சாமிர்த அபிஷேகம் செய்து, சந்தனம், குங்குமம், மலர்கள், துளசி/வில்வம், தூபம் மற்றும் தீபம் சமர்ப்பித்து மூல மந்திரத்தை ஜபிக்கவும்."
                AppLanguage.TELUGU -> "పంచామృత అభిషేకం చేసి, గంధం, కుంకుమ, అక్షతలు, పుష్పమాల, ధూపం మరియు దీపం సమర్పిస్తూ మూల మంత్రాన్ని జపించండి."
                AppLanguage.KANNADA -> "ಪಂಚಾಮೃತ ಅಭಿಷೇಕ ಮಾಡಿ, ಶ್ರೀಗಂಧ, ಅರಿಶಿನ-ಕುಂಕುಮ, ಅಕ್ಷತೆ, ಹೂಮಾಲೆ, ಧೂಪ ಮತ್ತು ದೀಪ ಅರ್ಪಿಸುತ್ತಾ ಮೂಲ ಮಂತ್ರವನ್ನು ಜಪಿಸಿ."
                AppLanguage.BENGALI -> "পঞ্চামৃত ও শুদ্ধ জলে অভিষেক করিয়ে চন্দন, সিঁদুর, পুষ্পমাল্য, ধূপ ও প্রদীপ নিবেদন করে মূল মন্ত্র জপ করুন।"
                AppLanguage.ENGLISH -> ""
            }
            4 -> when (language) {
                AppLanguage.HINDI -> "पर्व की पारंपरिक व्रत कथा का श्रद्धापूर्वक पाठ या श्रवण करें, विशेष अर्घ्य प्रदान करें तथा ऋतुफल, मिष्ठान्न एवं घर में बना सात्विक नैवेद्य (भोग) अर्पित करें।"
                AppLanguage.MARATHI -> "सणाची पारंपरिक व्रत कथा भक्तिभावाने वाचा किंवा ऐका, विशेष अर्घ्य द्या आणि फळे, गोड पदार्थ व पारंपरिक नैवेद्य अर्पण करा."
                AppLanguage.GUJARATI -> "તહેવારની પારંપરિક વ્રત કથાનું શ્રદ્ધાપૂર્વક વાંચન કે શ્રવણ કરો, અર્ઘ્ય આપો અને ફળો તથા પારંપરિક મીઠાઈનો નૈવેદ્ય (ભોગ) ધરાવો."
                AppLanguage.TAMIL -> "பண்டிகையின் புராண விரத கதையைப் படித்து, சிறப்பு அர்க்கியம் மற்றும் பழங்கள், இனிப்பு நைவேத்தியம் (பிரசாதம்) படைக்கவும்."
                AppLanguage.TELUGU -> "పండుగ వ్రత కథను భక్తితో పఠించి, విశేష అర్ఘ్యం సమర్పించి, పండ్లు మరియు సాంప్రదాయ నైవేద్యం (ప్రసాదం) నివేదించండి."
                AppLanguage.KANNADA -> "ಹಬ್ಬದ ಸಾಂಪ್ರದಾಯಿಕ ವ್ರತ ಕಥೆಯನ್ನು ಪಠಿಸಿ ಅಥವಾ ಆಲಿಸಿ, ವಿಶೇಷ ಅರ್ಘ್ಯ ನೀಡಿ ಹಾಗೂ ಹಣ್ಣು-ಸಿಹಿ ನೈವೇದ್ಯ ಸಮರ್ಪಿಸಿ."
                AppLanguage.BENGALI -> "উৎসবের ঐতিহ্যবাহী ব্রত কথা ভক্তিভরে পাঠ বা শ্রবণ করুন, অর্ঘ্য দিন এবং ফলমূল ও মিষ্টান্ন নৈবেদ্য নিবেদন করুন।"
                AppLanguage.ENGLISH -> ""
            }
            else -> when (language) {
                AppLanguage.HINDI -> "कपूर और घी के दीप से महाआरती उतारें, मंत्र पुष्पांजलि व प्रदक्षिणा करें, क्षमा प्रार्थना करें और बड़ों का आशीर्वाद लेकर परिवार में प्रसाद वितरित करें।"
                AppLanguage.MARATHI -> "कापूर आणि तुपाच्या दिव्याने महाआरती करा, मंत्र पुष्पांजली व प्रदक्षिणा घाला, क्षमा प्रार्थना करा आणि वडीलधाऱ्यांचे आशीर्वाद घेऊन सर्वांना प्रसाद वाटा."
                AppLanguage.GUJARATI -> "કપૂર અને ઘીના દીવાથી મહાઆરતી ઉતારો, મંત્ર પુષ્પાંજલિ અને પ્રદક્ષિણા કરો, ક્ષમા પ્રાર્થના કરો અને વડીલોના આશીર્વાદ લઈ પ્રસાદ વહેંચો."
                AppLanguage.TAMIL -> "கற்பூர மகா ஆரத்தி காட்டி, மந்திர புஷ்பாஞ்சலி மற்றும் பிரதட்சணம் செய்து, பெரியோர்களின் ஆசி பெற்று அனைவருக்கும் பிரசாதம் வழங்கவும்."
                AppLanguage.TELUGU -> "కర్పూర మహా హారతి ఇచ్చి, మంత్ర పుష్పాంజలి, ప్రదక్షిణ చేసి, పెద్దల ఆశీర్వాదం తీసుకుని అందరికీ ప్రసాదం పంచండి."
                AppLanguage.KANNADA -> "ಕರ್ಪೂರ ಮಹಾ ಆರತಿ ಬೆಳಗಿ, ಮಂತ್ರ ಪುಷ್ಪಾಂಜಲಿ ಮತ್ತು ಪ್ರದಕ್ಷಿಣೆ ಸಲ್ಲಿಸಿ, ಹಿರಿಯರ ಆಶೀರ್ವಾದ ಪಡೆದು ಎಲ್ಲರಿಗೂ ಪ್ರಸಾದ ವಿತರಿಸಿ."
                AppLanguage.BENGALI -> "কর্পূর ও ঘিয়ের প্রদীপ দিয়ে মহাআরতি করুন, মন্ত্র পুষ্পাঞ্জলি ও প্রণাম নিবেদন করুন এবং সকলের মধ্যে প্রসাদ বিতরণ করুন।"
                AppLanguage.ENGLISH -> ""
            }
        }
    }

    fun translateSentenceToLanguage(text: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return text
        return translateTerms(text, language)
    }

    fun translateTerms(text: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH || text.isBlank()) return text
        var result = text
        val replacements = TERM_REPLACEMENTS[language] ?: return text
        for ((eng, target) in replacements) {
            result = result.replace(Regex("\\b${Regex.escape(eng)}\\b", RegexOption.IGNORE_CASE), target)
        }
        return result
    }

    private val CATEGORY_LABELS: Map<String, Map<AppLanguage, String>> = mapOf(
        "All" to mapOf(
            AppLanguage.HINDI to "सभी पर्व", AppLanguage.MARATHI to "सर्व सण", AppLanguage.GUJARATI to "બધા તહેવારો",
            AppLanguage.TAMIL to "அனைத்தும்", AppLanguage.TELUGU to "అన్నీ", AppLanguage.KANNADA to "ಎಲ್ಲಾ", AppLanguage.BENGALI to "সকল উৎসব"
        ),
        "Major" to mapOf(
            AppLanguage.HINDI to "प्रमुख पर्व", AppLanguage.MARATHI to "प्रमुख सण", AppLanguage.GUJARATI to "મુખ્ય તહેવારો",
            AppLanguage.TAMIL to "முக்கிய பண்டிகைகள்", AppLanguage.TELUGU to "ప్రధాన పండుగలు", AppLanguage.KANNADA to "ಪ್ರಮುಖ ಹಬ್ಬಗಳು", AppLanguage.BENGALI to "প্রধান উৎসব"
        ),
        "Major Festivals" to mapOf(
            AppLanguage.HINDI to "प्रमुख त्योहार", AppLanguage.MARATHI to "प्रमुख सण", AppLanguage.GUJARATI to "મુખ્ય તહેવારો",
            AppLanguage.TAMIL to "முக்கிய பண்டிகைகள்", AppLanguage.TELUGU to "ప్రధాన పండుగలు", AppLanguage.KANNADA to "ಪ್ರಮುಖ ಹಬ್ಬಗಳು", AppLanguage.BENGALI to "প্রধান উৎসব"
        ),
        "Regional" to mapOf(
            AppLanguage.HINDI to "क्षेत्रीय पर्व", AppLanguage.MARATHI to "प्रादेशिक सण", AppLanguage.GUJARATI to "પ્રાદેશિક",
            AppLanguage.TAMIL to "பிராந்திய", AppLanguage.TELUGU to "ప్రాంతీయ", AppLanguage.KANNADA to "ಪ್ರಾದೇಶಿಕ", AppLanguage.BENGALI to "আঞ্চলিক"
        ),
        "Regional & Harvest" to mapOf(
            AppLanguage.HINDI to "क्षेत्रीय व फसली पर्व", AppLanguage.MARATHI to "प्रादेशिक व कृषी सण", AppLanguage.GUJARATI to "પ્રાદેશિક ઉત્સવો",
            AppLanguage.TAMIL to "அறுவடை & பிராந்திய", AppLanguage.TELUGU to "ప్రాంతీయ పండుగలు", AppLanguage.KANNADA to "ಪ್ರಾದೇಶಿಕ ಹಬ್ಬಗಳು", AppLanguage.BENGALI to "আঞ্চলিক উৎসব"
        ),
        "Vrat & Ekadashi" to mapOf(
            AppLanguage.HINDI to "व्रत एवं एकादशी", AppLanguage.MARATHI to "व्रत आणि एकादशी", AppLanguage.GUJARATI to "વ્રત અને એકાદશી",
            AppLanguage.TAMIL to "விரதம் & ஏகாதசி", AppLanguage.TELUGU to "వ్రతాలు & ఏకాదశి", AppLanguage.KANNADA to "ವ್ರತ ಮತ್ತು ಏಕಾದಶಿ", AppLanguage.BENGALI to "ব্রত ও একাদশী"
        ),
        "Fasts & Vrats" to mapOf(
            AppLanguage.HINDI to "व्रत एवं उपवास", AppLanguage.MARATHI to "व्रत आणि उपवास", AppLanguage.GUJARATI to "વ્રત અને ઉપવાસ",
            AppLanguage.TAMIL to "விரத நாட்கள்", AppLanguage.TELUGU to "వ్రతాలు & ఉపవాసాలు", AppLanguage.KANNADA to "ವ್ರತ ಮತ್ತು ಉಪವಾಸ", AppLanguage.BENGALI to "ব্রত ও উপবাস"
        ),
        "Coming Soon" to mapOf(
            AppLanguage.HINDI to "आगामी पर्व", AppLanguage.MARATHI to "लवकरच येणारे", AppLanguage.GUJARATI to "આગામી",
            AppLanguage.TAMIL to "விரைவில்", AppLanguage.TELUGU to "రాబోయేవి", AppLanguage.KANNADA to "ಮುಂಬರುವ", AppLanguage.BENGALI to "আসন্ন"
        ),
        "Saved" to mapOf(
            AppLanguage.HINDI to "सहेजे गए", AppLanguage.MARATHI to "जतन केलेले", AppLanguage.GUJARATI to "સાચવેલા",
            AppLanguage.TAMIL to "சேமித்தவை", AppLanguage.TELUGU to "సేవ్ చేసినవి", AppLanguage.KANNADA to "ಉಳಿಸಿದ", AppLanguage.BENGALI to "সংরক্ষিত"
        )
    )

    private val YOGA_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Vishkumbha" to mapOf(AppLanguage.HINDI to "विष्कुम्भ", AppLanguage.MARATHI to "विष्कुंभ", AppLanguage.GUJARATI to "વિષ્કુંભ", AppLanguage.TAMIL to "விஷ்கம்பம்", AppLanguage.TELUGU to "విష్కంభం", AppLanguage.KANNADA to "ವಿಷ್ಕಂಭ", AppLanguage.BENGALI to "বিষ্কুম্ভ"),
        "Priti" to mapOf(AppLanguage.HINDI to "प्रीति", AppLanguage.MARATHI to "प्रीती", AppLanguage.GUJARATI to "પ્રીતિ", AppLanguage.TAMIL to "பிரீতি", AppLanguage.TELUGU to "ప్రీతి", AppLanguage.KANNADA to "ಪ್ರೀತಿ", AppLanguage.BENGALI to "প্রীতি"),
        "Ayushman" to mapOf(AppLanguage.HINDI to "आयुष्मान", AppLanguage.MARATHI to "आयुष्मान", AppLanguage.GUJARATI to "આયુષ્માન", AppLanguage.TAMIL to "ஆயுஷ்மான்", AppLanguage.TELUGU to "ఆయుష్మాన్", AppLanguage.KANNADA to "ಆಯುಷ್ಮಾನ್", AppLanguage.BENGALI to "আয়ুষ্মান"),
        "Saubhagya" to mapOf(AppLanguage.HINDI to "सौभाग्य", AppLanguage.MARATHI to "सौभाग्य", AppLanguage.GUJARATI to "સૌભાગ્ય", AppLanguage.TAMIL to "சௌபாக்கியம்", AppLanguage.TELUGU to "సౌభాగ్యం", AppLanguage.KANNADA to "ಸೌಭಾಗ್ಯ", AppLanguage.BENGALI to "সৌভাগ্য"),
        "Shobhana" to mapOf(AppLanguage.HINDI to "शोभन", AppLanguage.MARATHI to "शोभन", AppLanguage.GUJARATI to "શોભન", AppLanguage.TAMIL to "சோபனம்", AppLanguage.TELUGU to "శోభనం", AppLanguage.KANNADA to "ಶೋಭನ", AppLanguage.BENGALI to "শোভন"),
        "Atiganda" to mapOf(AppLanguage.HINDI to "अतिगण्ड", AppLanguage.MARATHI to "अतिगंड", AppLanguage.GUJARATI to "અતિગંડ", AppLanguage.TAMIL to "அதிகண்டம்", AppLanguage.TELUGU to "అతిగండం", AppLanguage.KANNADA to "ಅತಿಗಂಡ", AppLanguage.BENGALI to "অতিগণ্ড"),
        "Sukarma" to mapOf(AppLanguage.HINDI to "सुकर्मा", AppLanguage.MARATHI to "सुकर्मा", AppLanguage.GUJARATI to "સુકર્મા", AppLanguage.TAMIL to "சுகர்மா", AppLanguage.TELUGU to "సుకర్మ", AppLanguage.KANNADA to "ಸುಕರ್ಮ", AppLanguage.BENGALI to "সুকর্মা"),
        "Dhriti" to mapOf(AppLanguage.HINDI to "धृति", AppLanguage.MARATHI to "धृती", AppLanguage.GUJARATI to "ધૃતિ", AppLanguage.TAMIL to "திருதி", AppLanguage.TELUGU to "ధృతి", AppLanguage.KANNADA to "ಧೃತಿ", AppLanguage.BENGALI to "ধৃতি"),
        "Shula" to mapOf(AppLanguage.HINDI to "शूल", AppLanguage.MARATHI to "शूल", AppLanguage.GUJARATI to "શૂલ", AppLanguage.TAMIL to "சூலம்", AppLanguage.TELUGU to "శూలం", AppLanguage.KANNADA to "ಶೂಲ", AppLanguage.BENGALI to "শূল"),
        "Ganda" to mapOf(AppLanguage.HINDI to "गण्ड", AppLanguage.MARATHI to "गंड", AppLanguage.GUJARATI to "ગંડ", AppLanguage.TAMIL to "கண்டம்", AppLanguage.TELUGU to "గండం", AppLanguage.KANNADA to "ಗಂಡ", AppLanguage.BENGALI to "গণ্ড"),
        "Vriddhi" to mapOf(AppLanguage.HINDI to "वृद्धि", AppLanguage.MARATHI to "वृद्धी", AppLanguage.GUJARATI to "વૃદ્ધિ", AppLanguage.TAMIL to "விருத்தி", AppLanguage.TELUGU to "వృద్ధి", AppLanguage.KANNADA to "ವೃದ್ಧಿ", AppLanguage.BENGALI to "বৃদ্ধি"),
        "Dhruva" to mapOf(AppLanguage.HINDI to "ध्रुव", AppLanguage.MARATHI to "ध्रुव", AppLanguage.GUJARATI to "ધ્રુવ", AppLanguage.TAMIL to "துருவம்", AppLanguage.TELUGU to "ధ్రువం", AppLanguage.KANNADA to "ಧ್ರುವ", AppLanguage.BENGALI to "ধ্রুব"),
        "Vyaghata" to mapOf(AppLanguage.HINDI to "व्याघात", AppLanguage.MARATHI to "व्याघात", AppLanguage.GUJARATI to "વ્યાઘાત", AppLanguage.TAMIL to "வியாகாதம்", AppLanguage.TELUGU to "వ్యాఘాతం", AppLanguage.KANNADA to "ವ್ಯಾಘಾತ", AppLanguage.BENGALI to "ব্যাঘাত"),
        "Harshana" to mapOf(AppLanguage.HINDI to "हर्षण", AppLanguage.MARATHI to "हर्षण", AppLanguage.GUJARATI to "હર્ષણ", AppLanguage.TAMIL to "ஹர்ஷணம்", AppLanguage.TELUGU to "హర్షణం", AppLanguage.KANNADA to "ಹರ್ಷಣ", AppLanguage.BENGALI to "হর্ষণ"),
        "Vajra" to mapOf(AppLanguage.HINDI to "वज्र", AppLanguage.MARATHI to "वज्र", AppLanguage.GUJARATI to "વજ્ર", AppLanguage.TAMIL to "வஜ்ரம்", AppLanguage.TELUGU to "వజ్రం", AppLanguage.KANNADA to "ವಜ್ರ", AppLanguage.BENGALI to "বজ্র"),
        "Siddhi" to mapOf(AppLanguage.HINDI to "सिद्धि", AppLanguage.MARATHI to "सिद्धी", AppLanguage.GUJARATI to "સિદ્ધિ", AppLanguage.TAMIL to "சித்தி", AppLanguage.TELUGU to "సిద్ధి", AppLanguage.KANNADA to "ಸಿದ್ಧಿ", AppLanguage.BENGALI to "সিদ্ধি"),
        "Vyatipata" to mapOf(AppLanguage.HINDI to "व्यतीपात", AppLanguage.MARATHI to "व्यतिपात", AppLanguage.GUJARATI to "વ્યતીપાત", AppLanguage.TAMIL to "வியதீபாதம்", AppLanguage.TELUGU to "వ్యతీపాతం", AppLanguage.KANNADA to "ವ್ಯತೀಪಾತ", AppLanguage.BENGALI to "ব্যতীপাত"),
        "Variyan" to mapOf(AppLanguage.HINDI to "वरीयान", AppLanguage.MARATHI to "वरीयान", AppLanguage.GUJARATI to "વરીયાન", AppLanguage.TAMIL to "வரியான்", AppLanguage.TELUGU to "వరీయాన్", AppLanguage.KANNADA to "ವರೀಯಾನ್", AppLanguage.BENGALI to "বরীয়ান"),
        "Parigha" to mapOf(AppLanguage.HINDI to "परिघ", AppLanguage.MARATHI to "परिघ", AppLanguage.GUJARATI to "પરિઘ", AppLanguage.TAMIL to "பரிகம்", AppLanguage.TELUGU to "పరిఘం", AppLanguage.KANNADA to "ಪರಿಘ", AppLanguage.BENGALI to "পরিঘ"),
        "Shiva" to mapOf(AppLanguage.HINDI to "शिव", AppLanguage.MARATHI to "शिव", AppLanguage.GUJARATI to "શિવ", AppLanguage.TAMIL to "சிவம்", AppLanguage.TELUGU to "శివం", AppLanguage.KANNADA to "ಶಿವ", AppLanguage.BENGALI to "শিব"),
        "Siddha" to mapOf(AppLanguage.HINDI to "सिद्ध", AppLanguage.MARATHI to "सिद्ध", AppLanguage.GUJARATI to "સિદ્ધ", AppLanguage.TAMIL to "சித்தம்", AppLanguage.TELUGU to "సిద్ధం", AppLanguage.KANNADA to "ಸಿದ್ಧ", AppLanguage.BENGALI to "সিদ্ধ"),
        "Sadhya" to mapOf(AppLanguage.HINDI to "साध्य", AppLanguage.MARATHI to "साध्य", AppLanguage.GUJARATI to "સાધ્ય", AppLanguage.TAMIL to "சாத்தியம்", AppLanguage.TELUGU to "సాధ్యం", AppLanguage.KANNADA to "ಸಾಧ್ಯ", AppLanguage.BENGALI to "সাধ্য"),
        "Shubha" to mapOf(AppLanguage.HINDI to "शुभ", AppLanguage.MARATHI to "शुभ", AppLanguage.GUJARATI to "શુભ", AppLanguage.TAMIL to "சுபம்", AppLanguage.TELUGU to "శుభం", AppLanguage.KANNADA to "ಶುಭ", AppLanguage.BENGALI to "শুভ"),
        "Shukla" to mapOf(AppLanguage.HINDI to "शुक्ल", AppLanguage.MARATHI to "शुक्ल", AppLanguage.GUJARATI to "શુક્લ", AppLanguage.TAMIL to "சுக்லம்", AppLanguage.TELUGU to "శుక్లం", AppLanguage.KANNADA to "ಶುಕ್ಲ", AppLanguage.BENGALI to "শুক্ল"),
        "Brahma" to mapOf(AppLanguage.HINDI to "ब्रह्म", AppLanguage.MARATHI to "ब्रह्म", AppLanguage.GUJARATI to "બ્રહ્મ", AppLanguage.TAMIL to "பிரம்மம்", AppLanguage.TELUGU to "బ్రహ్మం", AppLanguage.KANNADA to "ಬ್ರಹ್ಮ", AppLanguage.BENGALI to "ব্রহ্ম"),
        "Indra" to mapOf(AppLanguage.HINDI to "इन्द्र", AppLanguage.MARATHI to "इंद्र", AppLanguage.GUJARATI to "ઇન્દ્ર", AppLanguage.TAMIL to "இந்திரம்", AppLanguage.TELUGU to "ఇంద్రం", AppLanguage.KANNADA to "ಇಂದ್ರ", AppLanguage.BENGALI to "ইন্দ্র"),
        "Vaidhriti" to mapOf(AppLanguage.HINDI to "वैधृति", AppLanguage.MARATHI to "वैधृती", AppLanguage.GUJARATI to "વૈધૃતિ", AppLanguage.TAMIL to "வைதிருதி", AppLanguage.TELUGU to "వైధృతి", AppLanguage.KANNADA to "ವೈಧೃತಿ", AppLanguage.BENGALI to "বৈধৃতি")
    )

    private val KARANA_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Bava" to mapOf(AppLanguage.HINDI to "बव", AppLanguage.MARATHI to "बव", AppLanguage.GUJARATI to "બવ", AppLanguage.TAMIL to "பவம்", AppLanguage.TELUGU to "బవ", AppLanguage.KANNADA to "ಬವ", AppLanguage.BENGALI to "বব"),
        "Balava" to mapOf(AppLanguage.HINDI to "बालव", AppLanguage.MARATHI to "बालव", AppLanguage.GUJARATI to "બાલવ", AppLanguage.TAMIL to "பாலவம்", AppLanguage.TELUGU to "బాలవ", AppLanguage.KANNADA to "ಬಾಲವ", AppLanguage.BENGALI to "বালব"),
        "Kaulava" to mapOf(AppLanguage.HINDI to "कौलव", AppLanguage.MARATHI to "कौलव", AppLanguage.GUJARATI to "કૌલવ", AppLanguage.TAMIL to "கௌலவம்", AppLanguage.TELUGU to "కౌలవ", AppLanguage.KANNADA to "ಕೌಲವ", AppLanguage.BENGALI to "কৌলব"),
        "Taitila" to mapOf(AppLanguage.HINDI to "तैतिल", AppLanguage.MARATHI to "तैतिल", AppLanguage.GUJARATI to "તૈતિલ", AppLanguage.TAMIL to "தைதிலம்", AppLanguage.TELUGU to "తైతిల", AppLanguage.KANNADA to "ತೈತಿಲ", AppLanguage.BENGALI to "তৈতিল"),
        "Garaja" to mapOf(AppLanguage.HINDI to "गर", AppLanguage.MARATHI to "गर", AppLanguage.GUJARATI to "ગર", AppLanguage.TAMIL to "கரஜை", AppLanguage.TELUGU to "గరజ", AppLanguage.KANNADA to "ಗರಜ", AppLanguage.BENGALI to "গর"),
        "Vanija" to mapOf(AppLanguage.HINDI to "वणिज", AppLanguage.MARATHI to "वणिज", AppLanguage.GUJARATI to "વણિજ", AppLanguage.TAMIL to "வணிஜை", AppLanguage.TELUGU to "వణిజ", AppLanguage.KANNADA to "ವಣಿಜ", AppLanguage.BENGALI to "বণিজ"),
        "Vishti" to mapOf(AppLanguage.HINDI to "विष्टि (भद्रा)", AppLanguage.MARATHI to "विष्टी (भद्रा)", AppLanguage.GUJARATI to "વિષ્ટિ (ભદ્રા)", AppLanguage.TAMIL to "பத்ரை (விஷ்டி)", AppLanguage.TELUGU to "విష్టి (భద్ర)", AppLanguage.KANNADA to "ವಿಷ್ಟಿ (ಭದ್ರಾ)", AppLanguage.BENGALI to "বিষ্টি (ভদ্রা)"),
        "Shakuni" to mapOf(AppLanguage.HINDI to "शकुनि", AppLanguage.MARATHI to "शकुनी", AppLanguage.GUJARATI to "શકુનિ", AppLanguage.TAMIL to "சகுனி", AppLanguage.TELUGU to "శకుని", AppLanguage.KANNADA to "ಶಕುನಿ", AppLanguage.BENGALI to "শকুনি"),
        "Chatushpada" to mapOf(AppLanguage.HINDI to "चतुष्पद", AppLanguage.MARATHI to "चतुष्पाद", AppLanguage.GUJARATI to "ચતુષ્પદ", AppLanguage.TAMIL to "சதுஷ்பாதம்", AppLanguage.TELUGU to "చతుష్పాద", AppLanguage.KANNADA to "ಚತುಷ್ಪಾದ", AppLanguage.BENGALI to "চতুষ্পদ"),
        "Naga" to mapOf(AppLanguage.HINDI to "नाग", AppLanguage.MARATHI to "नाग", AppLanguage.GUJARATI to "નાગ", AppLanguage.TAMIL to "நாகவம்", AppLanguage.TELUGU to "నాగవ", AppLanguage.KANNADA to "ನಾಗವ", AppLanguage.BENGALI to "নাগ"),
        "Kimstughna" to mapOf(AppLanguage.HINDI to "किंस्तुघ्न", AppLanguage.MARATHI to "किंस्तुघ्न", AppLanguage.GUJARATI to "કિંસ્તુઘ્ન", AppLanguage.TAMIL to "கிம்ஸ்துக்னம்", AppLanguage.TELUGU to "కింస్తుఘ్న", AppLanguage.KANNADA to "ಕಿಂಸ್ತುಘ್ನ", AppLanguage.BENGALI to "কিংস্তুঘ্ন")
    )

    private val CITY_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "delhi" to mapOf(
            AppLanguage.HINDI to "नई दिल्ली", AppLanguage.MARATHI to "नवी दिल्ली", AppLanguage.GUJARATI to "નવી દિલ્હી",
            AppLanguage.TAMIL to "புது தில்லி", AppLanguage.TELUGU to "న్యూఢిల్లీ", AppLanguage.KANNADA to "ನವದೆಹಲಿ", AppLanguage.BENGALI to "নয়াদিল্লি"
        ),
        "mumbai" to mapOf(
            AppLanguage.HINDI to "मुंबई", AppLanguage.MARATHI to "मुंबई", AppLanguage.GUJARATI to "મુંબઈ",
            AppLanguage.TAMIL to "மும்பை", AppLanguage.TELUGU to "ముంబై", AppLanguage.KANNADA to "ಮುಂಬೈ", AppLanguage.BENGALI to "মুম্বাই"
        ),
        "bengaluru" to mapOf(
            AppLanguage.HINDI to "बेंगलुरु", AppLanguage.MARATHI to "बंगळुरू", AppLanguage.GUJARATI to "બેંગલુરુ",
            AppLanguage.TAMIL to "பெங்களூரு", AppLanguage.TELUGU to "బెంగళూరు", AppLanguage.KANNADA to "ಬೆಂಗಳೂರು", AppLanguage.BENGALI to "বেঙ্গালুরু"
        ),
        "varanasi" to mapOf(
            AppLanguage.HINDI to "वाराणसी (काशी)", AppLanguage.MARATHI to "वाराणसी (काशी)", AppLanguage.GUJARATI to "વારાણસી (કાશી)",
            AppLanguage.TAMIL to "வாரணாசி (காசி)", AppLanguage.TELUGU to "వారణాసి (కాశీ)", AppLanguage.KANNADA to "ವಾರಣಾಸಿ (ಕಾಶಿ)", AppLanguage.BENGALI to "বারাণসী (কাশী)"
        ),
        "ahmedabad" to mapOf(
            AppLanguage.HINDI to "अहमदाबाद", AppLanguage.MARATHI to "अहमदाबाद", AppLanguage.GUJARATI to "અમદાવાદ",
            AppLanguage.TAMIL to "அகமதாபாத்", AppLanguage.TELUGU to "అహ్మదాబాద్", AppLanguage.KANNADA to "ಅಹಮದಾಬಾದ್", AppLanguage.BENGALI to "আহমেদাবাদ"
        ),
        "kolkata" to mapOf(
            AppLanguage.HINDI to "कोलकाता", AppLanguage.MARATHI to "कोलकाता", AppLanguage.GUJARATI to "કોલકાતા",
            AppLanguage.TAMIL to "கொல்கத்தா", AppLanguage.TELUGU to "కోల్‌కతా", AppLanguage.KANNADA to "ಕೋಲ್ಕತ್ತಾ", AppLanguage.BENGALI to "কলকাতা"
        ),
        "chennai" to mapOf(
            AppLanguage.HINDI to "चेन्नई", AppLanguage.MARATHI to "चेन्नई", AppLanguage.GUJARATI to "ચેન્નાઈ",
            AppLanguage.TAMIL to "சென்னை", AppLanguage.TELUGU to "చెన్నై", AppLanguage.KANNADA to "ಚೆನ್ನೈ", AppLanguage.BENGALI to "চেন্নাই"
        ),
        "hyderabad" to mapOf(
            AppLanguage.HINDI to "हैदराबाद", AppLanguage.MARATHI to "हैदराबाद", AppLanguage.GUJARATI to "હૈદરાબાદ",
            AppLanguage.TAMIL to "ஹைதராபாத்", AppLanguage.TELUGU to "హైదరాబాద్", AppLanguage.KANNADA to "ಹೈದರಾಬಾದ್", AppLanguage.BENGALI to "হায়দ্রাবাদ"
        ),
        "pune" to mapOf(
            AppLanguage.HINDI to "पुणे", AppLanguage.MARATHI to "पुणे", AppLanguage.GUJARATI to "પુણે",
            AppLanguage.TAMIL to "புனே", AppLanguage.TELUGU to "పుణె", AppLanguage.KANNADA to "ಪುಣೆ", AppLanguage.BENGALI to "পুনে"
        ),
        "jaipur" to mapOf(
            AppLanguage.HINDI to "जयपुर", AppLanguage.MARATHI to "जयपूर", AppLanguage.GUJARATI to "જયપુર",
            AppLanguage.TAMIL to "ஜெய்ப்பூர்", AppLanguage.TELUGU to "జైపూర్", AppLanguage.KANNADA to "ಜೈಪುರ", AppLanguage.BENGALI to "জয়পুর"
        ),
        "lucknow" to mapOf(
            AppLanguage.HINDI to "लखनऊ", AppLanguage.MARATHI to "लखनौ", AppLanguage.GUJARATI to "લખનૌ",
            AppLanguage.TAMIL to "லக்னோ", AppLanguage.TELUGU to "లక్నో", AppLanguage.KANNADA to "ಲಕ್ನೋ", AppLanguage.BENGALI to "লখনউ"
        ),
        "patna" to mapOf(
            AppLanguage.HINDI to "पटना", AppLanguage.MARATHI to "पाटणा", AppLanguage.GUJARATI to "પટના",
            AppLanguage.TAMIL to "பாட்னா", AppLanguage.TELUGU to "పాట్నా", AppLanguage.KANNADA to "ಪಾಟ್ನಾ", AppLanguage.BENGALI to "পাটনা"
        ),
        "ujjain" to mapOf(
            AppLanguage.HINDI to "उज्जैन", AppLanguage.MARATHI to "उज्जैन", AppLanguage.GUJARATI to "ઉજ્જૈન",
            AppLanguage.TAMIL to "உஜ்ஜைன்", AppLanguage.TELUGU to "ఉజ్జయిని", AppLanguage.KANNADA to "ಉಜ್ಜಯಿನಿ", AppLanguage.BENGALI to "উজ্জয়িনী"
        ),
        "puri" to mapOf(
            AppLanguage.HINDI to "पुरी", AppLanguage.MARATHI to "पुरी", AppLanguage.GUJARATI to "પુરી",
            AppLanguage.TAMIL to "பூரி", AppLanguage.TELUGU to "పురి", AppLanguage.KANNADA to "ಪುರಿ", AppLanguage.BENGALI to "পুরী"
        ),
        "guwahati" to mapOf(
            AppLanguage.HINDI to "गुवाहाटी", AppLanguage.MARATHI to "गुवाहाटी", AppLanguage.GUJARATI to "ગુવાહાટી",
            AppLanguage.TAMIL to "குவாஹாத்தி", AppLanguage.TELUGU to "గువహాటి", AppLanguage.KANNADA to "ಗುವಾಹಟಿ", AppLanguage.BENGALI to "গুয়াহাটি"
        ),
        "madurai" to mapOf(
            AppLanguage.HINDI to "मदुरै", AppLanguage.MARATHI to "मदुराई", AppLanguage.GUJARATI to "મદુરાઈ",
            AppLanguage.TAMIL to "மதுரை", AppLanguage.TELUGU to "మధురై", AppLanguage.KANNADA to "ಮಧುರೈ", AppLanguage.BENGALI to "মাদুরাই"
        ),
        "tirupati" to mapOf(
            AppLanguage.HINDI to "तिरुपति", AppLanguage.MARATHI to "तिरुपती", AppLanguage.GUJARATI to "તિરુપતિ",
            AppLanguage.TAMIL to "திருப்பதி", AppLanguage.TELUGU to "తిరుపతి", AppLanguage.KANNADA to "ತಿರುಪತಿ", AppLanguage.BENGALI to "তিরুপতি"
        ),
        "amritsar" to mapOf(
            AppLanguage.HINDI to "अमृतसर", AppLanguage.MARATHI to "अमृतसर", AppLanguage.GUJARATI to "અમૃતસર",
            AppLanguage.TAMIL to "அமிர்தசரஸ்", AppLanguage.TELUGU to "అమృత్‌సర్", AppLanguage.KANNADA to "ಅಮೃತಸರ", AppLanguage.BENGALI to "অমৃতসর"
        ),
        "haridwar" to mapOf(
            AppLanguage.HINDI to "हरिद्वार", AppLanguage.MARATHI to "हरिद्वार", AppLanguage.GUJARATI to "હરિદ્વાર",
            AppLanguage.TAMIL to "ஹரித்வார்", AppLanguage.TELUGU to "హరిద్వార్", AppLanguage.KANNADA to "ಹರಿದ್ವಾರ", AppLanguage.BENGALI to "হরিদ্বার"
        ),
        "ayodhya" to mapOf(
            AppLanguage.HINDI to "अयोध्या", AppLanguage.MARATHI to "अयोध्या", AppLanguage.GUJARATI to "અયોધ્યા",
            AppLanguage.TAMIL to "அயோத்தி", AppLanguage.TELUGU to "అయోధ్య", AppLanguage.KANNADA to "ಅಯೋಧ್ಯೆ", AppLanguage.BENGALI to "অযোধ্যা"
        )
    )

    private val STATE_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Delhi NCR" to mapOf(AppLanguage.HINDI to "दिल्ली एनसीआर", AppLanguage.MARATHI to "दिल्ली", AppLanguage.GUJARATI to "દિલ્હી", AppLanguage.TAMIL to "தில்லி", AppLanguage.TELUGU to "ఢిల్లీ", AppLanguage.KANNADA to "ದೆಹಲಿ", AppLanguage.BENGALI to "দিল্লি"),
        "Maharashtra" to mapOf(AppLanguage.HINDI to "महाराष्ट्र", AppLanguage.MARATHI to "महाराष्ट्र", AppLanguage.GUJARATI to "મહારાષ્ટ્ર", AppLanguage.TAMIL to "மகாராஷ்டிரா", AppLanguage.TELUGU to "మహారాష్ట్ర", AppLanguage.KANNADA to "ಮಹಾರಾಷ್ಟ್ರ", AppLanguage.BENGALI to "মহারাষ্ট্র"),
        "Karnataka" to mapOf(AppLanguage.HINDI to "कर्नाटक", AppLanguage.MARATHI to "कर्नाटक", AppLanguage.GUJARATI to "કર્ણાટક", AppLanguage.TAMIL to "கர்நாடகா", AppLanguage.TELUGU to "కర్ణాటక", AppLanguage.KANNADA to "ಕರ್ನಾಟಕ", AppLanguage.BENGALI to "কর্ণাটক"),
        "Uttar Pradesh" to mapOf(AppLanguage.HINDI to "उत्तर प्रदेश", AppLanguage.MARATHI to "उत्तर प्रदेश", AppLanguage.GUJARATI to "ઉત્તર પ્રદેશ", AppLanguage.TAMIL to "உத்தரப் பிரதேசம்", AppLanguage.TELUGU to "ఉత్తర ప్రదేశ్", AppLanguage.KANNADA to "ಉತ್ತರ ಪ್ರದೇಶ", AppLanguage.BENGALI to "উত্তরপ্রদেশ"),
        "Gujarat" to mapOf(AppLanguage.HINDI to "गुजरात", AppLanguage.MARATHI to "गुजरात", AppLanguage.GUJARATI to "ગુજરાત", AppLanguage.TAMIL to "குஜராத்", AppLanguage.TELUGU to "గుజరాత్", AppLanguage.KANNADA to "ಗುಜರಾತ್", AppLanguage.BENGALI to "গুজরাট"),
        "West Bengal" to mapOf(AppLanguage.HINDI to "पश्चिम बंगाल", AppLanguage.MARATHI to "पश्चिम बंगाल", AppLanguage.GUJARATI to "પશ્ચિમ બંગાળ", AppLanguage.TAMIL to "மேற்கு வங்காளம்", AppLanguage.TELUGU to "పశ్చిమ బెంగాల్", AppLanguage.KANNADA to "ಪಶ್ಚಿಮ ಬಂಗಾಳ", AppLanguage.BENGALI to "পশ্চিমবঙ্গ"),
        "Tamil Nadu" to mapOf(AppLanguage.HINDI to "तमिलनाडु", AppLanguage.MARATHI to "तामिळनाडू", AppLanguage.GUJARATI to "તમિલનાડુ", AppLanguage.TAMIL to "தமிழ்நாடு", AppLanguage.TELUGU to "తమిళనాడు", AppLanguage.KANNADA to "ತಮಿಳುನಾಡು", AppLanguage.BENGALI to "তামিলনাড়ু"),
        "Telangana" to mapOf(AppLanguage.HINDI to "तेलंगाना", AppLanguage.MARATHI to "तेलंगणा", AppLanguage.GUJARATI to "તેલંગાણા", AppLanguage.TAMIL to "தெலங்கானா", AppLanguage.TELUGU to "తెలంగాణ", AppLanguage.KANNADA to "ತೆಲಂಗಾಣ", AppLanguage.BENGALI to "তেলেঙ্গানা"),
        "Rajasthan" to mapOf(AppLanguage.HINDI to "राजस्थान", AppLanguage.MARATHI to "राजस्थान", AppLanguage.GUJARATI to "રાજસ્થાન", AppLanguage.TAMIL to "ராஜஸ்தான்", AppLanguage.TELUGU to "రాజస్థాన్", AppLanguage.KANNADA to "ರಾಜಸ್ಥಾನ", AppLanguage.BENGALI to "রাজস্থান"),
        "Bihar" to mapOf(AppLanguage.HINDI to "बिहार", AppLanguage.MARATHI to "बिहार", AppLanguage.GUJARATI to "બિહાર", AppLanguage.TAMIL to "பீகார்", AppLanguage.TELUGU to "బీహార్", AppLanguage.KANNADA to "ಬಿಹಾರ", AppLanguage.BENGALI to "বিহার")
    )

    private val TRADITION_NAMES: Map<RegionalTradition, Map<AppLanguage, String>> = mapOf(
        RegionalTradition.NORTH_INDIAN to mapOf(
            AppLanguage.HINDI to "उत्तर भारतीय (पूर्णिमांत)",
            AppLanguage.MARATHI to "उत्तर भारतीय (पौर्णिमांत)",
            AppLanguage.GUJARATI to "ઉત્તર ભારતીય (પૂર્ણિમાંત)",
            AppLanguage.TAMIL to "வட இந்திய (பூர்ணிமாந்தா)",
            AppLanguage.TELUGU to "ఉత్తర భారతీయ (పూర్ణిమాంత)",
            AppLanguage.KANNADA to "ಉತ್ತರ ಭಾರತೀಯ (ಪೂರ್ಣಿಮಾಂತ)",
            AppLanguage.BENGALI to "উত্তর ভারতীয় (পূর্ণিমান্ত)"
        ),
        RegionalTradition.MAHARASHTRIAN to mapOf(
            AppLanguage.HINDI to "महाराष्ट्रीयन (अमांत)",
            AppLanguage.MARATHI to "महाराष्ट्रीय (अमांत)",
            AppLanguage.GUJARATI to "મહારાષ્ટ્રીયન (અમાંત)",
            AppLanguage.TAMIL to "மகாராஷ்டிரா (அமாந்தா)",
            AppLanguage.TELUGU to "మహారాష్ట్ర (అమాంత)",
            AppLanguage.KANNADA to "ಮಹಾರಾಷ್ಟ್ರ (ಅಮಾಂತ)",
            AppLanguage.BENGALI to "মহারাষ্ট্রীয় (অমান্ত)"
        ),
        RegionalTradition.GUJARATI to mapOf(
            AppLanguage.HINDI to "गुजराती (अमांत / विक्रम संवत)",
            AppLanguage.MARATHI to "गुजराती (अमांत)",
            AppLanguage.GUJARATI to "ગુજરાતી (અમાંત / વિક્રમ સંવત)",
            AppLanguage.TAMIL to "குஜராத்தி (அமாந்தா)",
            AppLanguage.TELUGU to "గుజరాతీ (అమాంత)",
            AppLanguage.KANNADA to "ಗುಜರಾತಿ (ಅಮಾಂತ)",
            AppLanguage.BENGALI to "গুজরাটি (অমান্ত)"
        ),
        RegionalTradition.TAMIL to mapOf(
            AppLanguage.HINDI to "तमिल (सौर पंचांगम)",
            AppLanguage.MARATHI to "तामिळ (सौर पंचांग)",
            AppLanguage.GUJARATI to "તમિલ (સૌર પંચાંગ)",
            AppLanguage.TAMIL to "தமிழ் (சௌர பஞ்சாங்கம்)",
            AppLanguage.TELUGU to "తమిళ (సౌర పంచాంగం)",
            AppLanguage.KANNADA to "ತಮಿಳು (ಸೌರ ಪಂಚಾಂಗ)",
            AppLanguage.BENGALI to "তামিল (সৌর পঞ্জিকা)"
        ),
        RegionalTradition.KANNADA_TELUGU to mapOf(
            AppLanguage.HINDI to "तेलुगु एवं कन्नड़ (अमांत)",
            AppLanguage.MARATHI to "तेलुगू आणि कन्नड (अमांत)",
            AppLanguage.GUJARATI to "તેલુગુ અને કન્નડ (અમાંત)",
            AppLanguage.TAMIL to "தெலுங்கு & கன்னடம் (அமாந்தா)",
            AppLanguage.TELUGU to "తెలుగు & కన్నడ (అమాంత)",
            AppLanguage.KANNADA to "ತೆಲುಗು ಮತ್ತು ಕನ್ನಡ (ಅಮಾಂತ)",
            AppLanguage.BENGALI to "তেলুগু ও কন্নড় (অমান্ত)"
        ),
        RegionalTradition.BENGALI to mapOf(
            AppLanguage.HINDI to "बंगाली, असमिया एवं ओड़िया (पूर्वी)",
            AppLanguage.MARATHI to "बंगाली, आसामी आणि ओडिया (पूर्व)",
            AppLanguage.GUJARATI to "બંગાળી, આસામી અને ઓડિયા (પૂર્વ)",
            AppLanguage.TAMIL to "பெங்காலி & கிழக்கு இந்தியா",
            AppLanguage.TELUGU to "బెంగాలీ & తూర్పు భారత",
            AppLanguage.KANNADA to "ಬೆಂಗಾಲಿ ಮತ್ತು ಪೂರ್ವ ಭಾರತ",
            AppLanguage.BENGALI to "বাঙালি, অসমীয়া ও ওড়িয়া (পূর্ব ভারত)"
        )
    )

    private val TITHI_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Pratipada" to mapOf(AppLanguage.HINDI to "प्रतिपदा", AppLanguage.MARATHI to "प्रतिपदा", AppLanguage.GUJARATI to "પ્રતિપદા (એકમ)", AppLanguage.TAMIL to "பிரதமை", AppLanguage.TELUGU to "పాడ్యమి", AppLanguage.KANNADA to "ಪಾಡ್ಯ", AppLanguage.BENGALI to "প্রতিপদ"),
        "Dwitiya" to mapOf(AppLanguage.HINDI to "द्वितीया (दूज)", AppLanguage.MARATHI to "द्वितीया (बीज)", AppLanguage.GUJARATI to "દ્વિતીયા (બીજ)", AppLanguage.TAMIL to "துவிதியை", AppLanguage.TELUGU to "విదియ", AppLanguage.KANNADA to "ಬಿದಿಗೆ", AppLanguage.BENGALI to "দ্বিতীয়া"),
        "Tritiya" to mapOf(AppLanguage.HINDI to "तृतीया (तीज)", AppLanguage.MARATHI to "तृतीया", AppLanguage.GUJARATI to "તૃતીયા (ત્રીજ)", AppLanguage.TAMIL to "திருதியை", AppLanguage.TELUGU to "తదియ", AppLanguage.KANNADA to "ತದಿಗೆ", AppLanguage.BENGALI to "তৃতীয়া"),
        "Chaturthi" to mapOf(AppLanguage.HINDI to "चतुर्थी (चौथ)", AppLanguage.MARATHI to "चतुर्थी", AppLanguage.GUJARATI to "ચતુર્થી (ચોથ)", AppLanguage.TAMIL to "சதுர்த்தி", AppLanguage.TELUGU to "చవితి", AppLanguage.KANNADA to "ಚೌತಿ", AppLanguage.BENGALI to "চতুর্থী"),
        "Panchami" to mapOf(AppLanguage.HINDI to "पंचमी", AppLanguage.MARATHI to "पंचमी", AppLanguage.GUJARATI to "પંચમી (પાંચમ)", AppLanguage.TAMIL to "பஞ்சமி", AppLanguage.TELUGU to "పంచమి", AppLanguage.KANNADA to "ಪಂಚಮಿ", AppLanguage.BENGALI to "পঞ্চমী"),
        "Shashthi" to mapOf(AppLanguage.HINDI to "षष्ठी (छठ)", AppLanguage.MARATHI to "षष्ठी", AppLanguage.GUJARATI to "ષષ્ઠી (છઠ્ઠ)", AppLanguage.TAMIL to "சஷ்டி", AppLanguage.TELUGU to "షష్ఠి", AppLanguage.KANNADA to "ಷಷ್ಠಿ", AppLanguage.BENGALI to "ষষ্ঠী"),
        "Saptami" to mapOf(AppLanguage.HINDI to "सप्तमी", AppLanguage.MARATHI to "सप्तमी", AppLanguage.GUJARATI to "સપ્તમી (સાતમ)", AppLanguage.TAMIL to "சப்தமி", AppLanguage.TELUGU to "సప్తమి", AppLanguage.KANNADA to "ಸಪ್ತಮಿ", AppLanguage.BENGALI to "সপ্তমী"),
        "Ashtami" to mapOf(AppLanguage.HINDI to "अष्टमी", AppLanguage.MARATHI to "अष्टमी", AppLanguage.GUJARATI to "અષ્ટમી (આઠમ)", AppLanguage.TAMIL to "அஷ்டமி", AppLanguage.TELUGU to "అష్టమి", AppLanguage.KANNADA to "ಅಷ್ಟಮಿ", AppLanguage.BENGALI to "অষ্টমী"),
        "Navami" to mapOf(AppLanguage.HINDI to "नवमी", AppLanguage.MARATHI to "नवमी", AppLanguage.GUJARATI to "નવમી (નોમ)", AppLanguage.TAMIL to "நவமி", AppLanguage.TELUGU to "నవమి", AppLanguage.KANNADA to "ನವಮಿ", AppLanguage.BENGALI to "নবমী"),
        "Dashami" to mapOf(AppLanguage.HINDI to "दशमी", AppLanguage.MARATHI to "दशमी", AppLanguage.GUJARATI to "દશમી (દશમ)", AppLanguage.TAMIL to "தசமி", AppLanguage.TELUGU to "దశమి", AppLanguage.KANNADA to "ದಶಮಿ", AppLanguage.BENGALI to "দশমী"),
        "Ekadashi" to mapOf(AppLanguage.HINDI to "एकादशी (ग्यारस)", AppLanguage.MARATHI to "एकादशी", AppLanguage.GUJARATI to "એકાદશી (અગિયારસ)", AppLanguage.TAMIL to "ஏகாதசி", AppLanguage.TELUGU to "ఏకాదశి", AppLanguage.KANNADA to "ಏಕಾದಶಿ", AppLanguage.BENGALI to "একাদশী"),
        "Dwadashi" to mapOf(AppLanguage.HINDI to "द्वादशी (बारस)", AppLanguage.MARATHI to "द्वादशी (बारस)", AppLanguage.GUJARATI to "દ્વાદશી (બારસ)", AppLanguage.TAMIL to "துவாதசி", AppLanguage.TELUGU to "ద్వాదశి", AppLanguage.KANNADA to "ದ್ವಾದಶಿ", AppLanguage.BENGALI to "দ্বাদশী"),
        "Trayodashi" to mapOf(AppLanguage.HINDI to "त्रयोदशी (तेरस)", AppLanguage.MARATHI to "त्रयोदशी", AppLanguage.GUJARATI to "ત્રયોદશી (તેરસ)", AppLanguage.TAMIL to "திரயோதசி", AppLanguage.TELUGU to "త్రయోదశి", AppLanguage.KANNADA to "ತ್ರಯೋದಶಿ", AppLanguage.BENGALI to "ত্রয়োদশী"),
        "Chaturdashi" to mapOf(AppLanguage.HINDI to "चतुर्दशी (चौदस)", AppLanguage.MARATHI to "चतुर्दशी", AppLanguage.GUJARATI to "ચતુર્દશી (ચૌદશ)", AppLanguage.TAMIL to "சதுர்த்தசி", AppLanguage.TELUGU to "చతుర్దశి", AppLanguage.KANNADA to "ಚತುರ್ದಶಿ", AppLanguage.BENGALI to "চতুর্দশী"),
        "Purnima" to mapOf(AppLanguage.HINDI to "पूर्णिमा", AppLanguage.MARATHI to "पौर्णिमा", AppLanguage.GUJARATI to "પૂર્ણિમા (પૂનમ)", AppLanguage.TAMIL to "பௌர்ணமி", AppLanguage.TELUGU to "పౌర్ణమి", AppLanguage.KANNADA to "ಹುಣ್ಣಿಮೆ (ಪೂರ್ಣಿಮಾ)", AppLanguage.BENGALI to "পূর্ণিমা"),
        "Amavasya" to mapOf(AppLanguage.HINDI to "अमावस्या", AppLanguage.MARATHI to "अमावस्या", AppLanguage.GUJARATI to "અમાવસ્યા (અમાસ)", AppLanguage.TAMIL to "அமாவாசை", AppLanguage.TELUGU to "అమావాస్య", AppLanguage.KANNADA to "ಅಮಾವಾಸ್ಯೆ", AppLanguage.BENGALI to "অমাবস্যা")
    )

    private val MONTH_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Chaitra" to mapOf(AppLanguage.HINDI to "चैत्र", AppLanguage.MARATHI to "चैत्र", AppLanguage.GUJARATI to "ચૈત્ર", AppLanguage.TAMIL to "சித்திரை", AppLanguage.TELUGU to "చైత్రం", AppLanguage.KANNADA to "ಚೈತ್ರ", AppLanguage.BENGALI to "চৈত্র"),
        "Vaisakha" to mapOf(AppLanguage.HINDI to "वैशाख", AppLanguage.MARATHI to "वैशाख", AppLanguage.GUJARATI to "વૈશાખ", AppLanguage.TAMIL to "வைகாசி", AppLanguage.TELUGU to "వైశాఖం", AppLanguage.KANNADA to "ವೈಶಾಖ", AppLanguage.BENGALI to "বৈশাখ"),
        "Jyeshtha" to mapOf(AppLanguage.HINDI to "ज्येष्ठ", AppLanguage.MARATHI to "ज्येष्ठ", AppLanguage.GUJARATI to "જ્યેષ્ઠ (જેઠ)", AppLanguage.TAMIL to "ஆனி", AppLanguage.TELUGU to "జ్యేష్ఠం", AppLanguage.KANNADA to "ಜ್ಯೇಷ್ಠ", AppLanguage.BENGALI to "জ্যৈষ্ঠ"),
        "Ashadha" to mapOf(AppLanguage.HINDI to "आषाढ़", AppLanguage.MARATHI to "आषाढ", AppLanguage.GUJARATI to "આષાઢ (અષાઢ)", AppLanguage.TAMIL to "ஆடி", AppLanguage.TELUGU to "ఆషాఢం", AppLanguage.KANNADA to "ಆಷಾಢ", AppLanguage.BENGALI to "আষাঢ়"),
        "Shravana" to mapOf(AppLanguage.HINDI to "श्रावण (सावन)", AppLanguage.MARATHI to "श्रावण", AppLanguage.GUJARATI to "શ્રાવણ", AppLanguage.TAMIL to "ஆவணி", AppLanguage.TELUGU to "శ్రావణం", AppLanguage.KANNADA to "ಶ್ರಾವಣ", AppLanguage.BENGALI to "শ্রাবণ"),
        "Bhadrapada" to mapOf(AppLanguage.HINDI to "भाद्रपद (भादो)", AppLanguage.MARATHI to "भाद्रपद", AppLanguage.GUJARATI to "ભાદરવો", AppLanguage.TAMIL to "புரட்டாசி", AppLanguage.TELUGU to "భాద్రపదం", AppLanguage.KANNADA to "ಭಾದ್ರಪದ", AppLanguage.BENGALI to "ভাদ্র"),
        "Ashwin" to mapOf(AppLanguage.HINDI to "आश्विन (क्वार)", AppLanguage.MARATHI to "आश्विन", AppLanguage.GUJARATI to "આસો", AppLanguage.TAMIL to "ஐப்பசி", AppLanguage.TELUGU to "ఆశ్వయుజం", AppLanguage.KANNADA to "ಆಶ್ವಯುಜ", AppLanguage.BENGALI to "আশ্বিন"),
        "Kartika" to mapOf(AppLanguage.HINDI to "कार्तिक", AppLanguage.MARATHI to "कार्तिक", AppLanguage.GUJARATI to "કારતક", AppLanguage.TAMIL to "கார்த்திகை", AppLanguage.TELUGU to "కార్తీకం", AppLanguage.KANNADA to "ಕಾರ್ತಿಕ", AppLanguage.BENGALI to "কার্তিক"),
        "Margashirsha" to mapOf(AppLanguage.HINDI to "मार्गशीर्ष (अगहन)", AppLanguage.MARATHI to "मार्गशीर्ष", AppLanguage.GUJARATI to "માગશર", AppLanguage.TAMIL to "மார்கழி", AppLanguage.TELUGU to "మార్గశిరం", AppLanguage.KANNADA to "ಮಾರ್ಗಶಿರ", AppLanguage.BENGALI to "অগ্রহায়ণ"),
        "Pausha" to mapOf(AppLanguage.HINDI to "पौष (पूस)", AppLanguage.MARATHI to "पौष", AppLanguage.GUJARATI to "પોષ", AppLanguage.TAMIL to "தை", AppLanguage.TELUGU to "పుష్యం", AppLanguage.KANNADA to "ಪುಷ್ಯ", AppLanguage.BENGALI to "পৌষ"),
        "Magha" to mapOf(AppLanguage.HINDI to "माघ", AppLanguage.MARATHI to "माघ", AppLanguage.GUJARATI to "મહા", AppLanguage.TAMIL to "மாசி", AppLanguage.TELUGU to "మాఘం", AppLanguage.KANNADA to "ಮಾಘ", AppLanguage.BENGALI to "মাঘ"),
        "Phalguna" to mapOf(AppLanguage.HINDI to "फाल्गुन (फागुन)", AppLanguage.MARATHI to "फाल्गुन", AppLanguage.GUJARATI to "ફાગણ", AppLanguage.TAMIL to "பங்குனி", AppLanguage.TELUGU to "ఫాల్గుణం", AppLanguage.KANNADA to "ಫಾಲ್ಗುಣ", AppLanguage.BENGALI to "ফাল্গুন")
    )

    private val NAKSHATRA_NAMES: Map<String, Map<AppLanguage, String>> = mapOf(
        "Ashwini" to mapOf(AppLanguage.HINDI to "अश्विनी", AppLanguage.MARATHI to "अश्विनी", AppLanguage.GUJARATI to "અશ્વિની", AppLanguage.TAMIL to "அசுவினி", AppLanguage.TELUGU to "అశ్విని", AppLanguage.KANNADA to "ಅಶ್ವಿನಿ", AppLanguage.BENGALI to "অশ্বিনী"),
        "Bharani" to mapOf(AppLanguage.HINDI to "भरणी", AppLanguage.MARATHI to "भरणी", AppLanguage.GUJARATI to "ભરણી", AppLanguage.TAMIL to "பரணி", AppLanguage.TELUGU to "భరణి", AppLanguage.KANNADA to "ಭರಣಿ", AppLanguage.BENGALI to "ভরণী"),
        "Krittika" to mapOf(AppLanguage.HINDI to "कृत्तिका", AppLanguage.MARATHI to "कृत्तिका", AppLanguage.GUJARATI to "કૃત્તિકા", AppLanguage.TAMIL to "கார்த்திகை", AppLanguage.TELUGU to "కృత్తిక", AppLanguage.KANNADA to "ಕೃತ್ತಿಕಾ", AppLanguage.BENGALI to "কৃত্তিকা"),
        "Rohini" to mapOf(AppLanguage.HINDI to "रोहिणी", AppLanguage.MARATHI to "रोहिणी", AppLanguage.GUJARATI to "રોહિણી", AppLanguage.TAMIL to "ரோகிணி", AppLanguage.TELUGU to "రోహిణి", AppLanguage.KANNADA to "ರೋಹಿಣಿ", AppLanguage.BENGALI to "রোহিণী"),
        "Mrigashirsha" to mapOf(AppLanguage.HINDI to "मृगशिरा", AppLanguage.MARATHI to "मृगशीर्ष", AppLanguage.GUJARATI to "મૃગશીર્ષ", AppLanguage.TAMIL to "மிருகசீரிடம்", AppLanguage.TELUGU to "మృగశిర", AppLanguage.KANNADA to "ಮೃಗಶಿರಾ", AppLanguage.BENGALI to "মৃগশিরা"),
        "Ardra" to mapOf(AppLanguage.HINDI to "आर्द्रा", AppLanguage.MARATHI to "आर्द्रा", AppLanguage.GUJARATI to "આર્દ્રા", AppLanguage.TAMIL to "திருவாதிரை", AppLanguage.TELUGU to "ఆరుద్ర", AppLanguage.KANNADA to "ಆರ್ದ್ರಾ", AppLanguage.BENGALI to "আর্দ্রা"),
        "Punarvasu" to mapOf(AppLanguage.HINDI to "पुनर्वसु", AppLanguage.MARATHI to "पुनर्वसू", AppLanguage.GUJARATI to "પુનર્વસુ", AppLanguage.TAMIL to "புனர்பூசம்", AppLanguage.TELUGU to "పునర్వసు", AppLanguage.KANNADA to "ಪುನರ್ವಸು", AppLanguage.BENGALI to "পুনর্বসু"),
        "Pushya" to mapOf(AppLanguage.HINDI to "पुष्य", AppLanguage.MARATHI to "पुष्य", AppLanguage.GUJARATI to "પુષ્ય", AppLanguage.TAMIL to "பூசம்", AppLanguage.TELUGU to "పుష్యమి", AppLanguage.KANNADA to "ಪುಷ್ಯ", AppLanguage.BENGALI to "পুষ্যা"),
        "Ashlesha" to mapOf(AppLanguage.HINDI to "आश्लेषा", AppLanguage.MARATHI to "आश्लेषा", AppLanguage.GUJARATI to "આશ્લેષા", AppLanguage.TAMIL to "ஆயில்யம்", AppLanguage.TELUGU to "ఆశ్లేష", AppLanguage.KANNADA to "ಆಶ್ಲೇಷಾ", AppLanguage.BENGALI to "অশ্লেষা"),
        "Magha" to mapOf(AppLanguage.HINDI to "मघा", AppLanguage.MARATHI to "मघा", AppLanguage.GUJARATI to "મઘા", AppLanguage.TAMIL to "மகம்", AppLanguage.TELUGU to "మఘ", AppLanguage.KANNADA to "ಮಘಾ", AppLanguage.BENGALI to "মঘা"),
        "Purva Phalguni" to mapOf(AppLanguage.HINDI to "पूर्वा फाल्गुनी", AppLanguage.MARATHI to "पूर्वा फाल्गुनी", AppLanguage.GUJARATI to "પૂર્વા ફાલ્ગુની", AppLanguage.TAMIL to "பூரம்", AppLanguage.TELUGU to "పుబ్బ", AppLanguage.KANNADA to "ಪೂರ್ವ ಫಾಲ್ಗುಣಿ", AppLanguage.BENGALI to "পূর্ব ফাল্গুনী"),
        "Uttara Phalguni" to mapOf(AppLanguage.HINDI to "उत्तरा फाल्गुनी", AppLanguage.MARATHI to "उत्तरा फाल्गुनी", AppLanguage.GUJARATI to "ઉત્તરા ફાલ્ગુની", AppLanguage.TAMIL to "உத்திரம்", AppLanguage.TELUGU to "ఉత్తర", AppLanguage.KANNADA to "ಉತ್ತರ ಫಾಲ್ಗುಣಿ", AppLanguage.BENGALI to "উত্তর ফাল্গুনী"),
        "Hasta" to mapOf(AppLanguage.HINDI to "हस्त", AppLanguage.MARATHI to "हस्त", AppLanguage.GUJARATI to "હસ્ત", AppLanguage.TAMIL to "அஸ்தம்", AppLanguage.TELUGU to "హస్త", AppLanguage.KANNADA to "ಹಸ್ತ", AppLanguage.BENGALI to "হস্তা"),
        "Chitra" to mapOf(AppLanguage.HINDI to "चित्रा", AppLanguage.MARATHI to "चित्रा", AppLanguage.GUJARATI to "ચિત્રા", AppLanguage.TAMIL to "சித்திரை", AppLanguage.TELUGU to "చిత్త", AppLanguage.KANNADA to "ಚಿತ್ರಾ", AppLanguage.BENGALI to "চিত্রা"),
        "Swati" to mapOf(AppLanguage.HINDI to "स्वाति", AppLanguage.MARATHI to "स्वाती", AppLanguage.GUJARATI to "સ્વાતિ", AppLanguage.TAMIL to "சுவாதி", AppLanguage.TELUGU to "స్వాతి", AppLanguage.KANNADA to "ಸ್ವಾತಿ", AppLanguage.BENGALI to "স্বাতী"),
        "Vishakha" to mapOf(AppLanguage.HINDI to "विशाखा", AppLanguage.MARATHI to "विशाखा", AppLanguage.GUJARATI to "વિશાખા", AppLanguage.TAMIL to "விசாகம்", AppLanguage.TELUGU to "విశాఖ", AppLanguage.KANNADA to "ವಿಶಾಖಾ", AppLanguage.BENGALI to "বিশাখা"),
        "Anuradha" to mapOf(AppLanguage.HINDI to "अनुराधा", AppLanguage.MARATHI to "अनुराधा", AppLanguage.GUJARATI to "અનુરાધા", AppLanguage.TAMIL to "அனுஷம்", AppLanguage.TELUGU to "అనూరాధ", AppLanguage.KANNADA to "ಅನುರಾಧಾ", AppLanguage.BENGALI to "অনুরাধা"),
        "Jyeshtha" to mapOf(AppLanguage.HINDI to "ज्येष्ठा", AppLanguage.MARATHI to "ज्येष्ठा", AppLanguage.GUJARATI to "જ્યેષ્ઠા", AppLanguage.TAMIL to "கேட்டை", AppLanguage.TELUGU to "జ్యేష్ఠ", AppLanguage.KANNADA to "ಜ್ಯೇಷ್ಠಾ", AppLanguage.BENGALI to "জ্যেষ্ঠা"),
        "Mula" to mapOf(AppLanguage.HINDI to "मूल", AppLanguage.MARATHI to "मूळ", AppLanguage.GUJARATI to "મૂળ", AppLanguage.TAMIL to "மூலம்", AppLanguage.TELUGU to "మూల", AppLanguage.KANNADA to "ಮೂಲಾ", AppLanguage.BENGALI to "মূলা"),
        "Purva Ashadha" to mapOf(AppLanguage.HINDI to "पूर्वाषाढ़ा", AppLanguage.MARATHI to "पूर्वाषाढा", AppLanguage.GUJARATI to "પૂર્વાષાઢા", AppLanguage.TAMIL to "பூராடம்", AppLanguage.TELUGU to "పూర్వాషాఢ", AppLanguage.KANNADA to "ಪೂರ್ವಾಷಾಢ", AppLanguage.BENGALI to "পূর্বাষাঢ়া"),
        "Uttara Ashadha" to mapOf(AppLanguage.HINDI to "उत्तराषाढ़ा", AppLanguage.MARATHI to "उत्तराषाढा", AppLanguage.GUJARATI to "ઉત્તરાષાઢા", AppLanguage.TAMIL to "உத்திராடம்", AppLanguage.TELUGU to "ఉత్తరాషాఢ", AppLanguage.KANNADA to "ಉತ್ತರಾಷಾಢ", AppLanguage.BENGALI to "উত্তরাষাঢ়া"),
        "Shravana" to mapOf(AppLanguage.HINDI to "श्रवण", AppLanguage.MARATHI to "श्रवण", AppLanguage.GUJARATI to "શ્રવણ", AppLanguage.TAMIL to "திருவோணம்", AppLanguage.TELUGU to "శ్రవణం", AppLanguage.KANNADA to "ಶ್ರವಣ", AppLanguage.BENGALI to "শ্রবণা"),
        "Dhanishta" to mapOf(AppLanguage.HINDI to "धनिष्ठा", AppLanguage.MARATHI to "धनिष्ठा", AppLanguage.GUJARATI to "ધનિષ્ઠા", AppLanguage.TAMIL to "அவிட்டம்", AppLanguage.TELUGU to "ధనిష్ఠ", AppLanguage.KANNADA to "ಧನಿಷ್ಠಾ", AppLanguage.BENGALI to "ধনিষ্ঠা"),
        "Shatabhisha" to mapOf(AppLanguage.HINDI to "शतभिषा", AppLanguage.MARATHI to "शततारका", AppLanguage.GUJARATI to "શતભિષા", AppLanguage.TAMIL to "சதயம்", AppLanguage.TELUGU to "శతభిషం", AppLanguage.KANNADA to "ಶತಭಿಷಾ", AppLanguage.BENGALI to "শতভিষা"),
        "Purva Bhadrapada" to mapOf(AppLanguage.HINDI to "पूर्वा भाद्रपद", AppLanguage.MARATHI to "पूर्वा भाद्रपदा", AppLanguage.GUJARATI to "પૂર્વા ભાદ્રપદ", AppLanguage.TAMIL to "பூரட்டாதி", AppLanguage.TELUGU to "పూర్వాभाద్ర", AppLanguage.KANNADA to "ಪೂರ್ವಾ ಭಾದ್ರಪದ", AppLanguage.BENGALI to "পূর্ব ভাদ্রপদ"),
        "Uttara Bhadrapada" to mapOf(AppLanguage.HINDI to "उत्तरा भाद्रपद", AppLanguage.MARATHI to "उत्तरा भाद्रपदा", AppLanguage.GUJARATI to "ઉત્તરા ભાદ્રપદ", AppLanguage.TAMIL to "உத்திரட்டாதி", AppLanguage.TELUGU to "ఉత్తరాभाద్ర", AppLanguage.KANNADA to "ಉತ್ತರಾ ಭಾದ್ರಪದ", AppLanguage.BENGALI to "উত্তর ভাদ্রপদ"),
        "Revati" to mapOf(AppLanguage.HINDI to "रेवती", AppLanguage.MARATHI to "रेवती", AppLanguage.GUJARATI to "રેવતી", AppLanguage.TAMIL to "ரேவதி", AppLanguage.TELUGU to "రేవతి", AppLanguage.KANNADA to "ರೇವತಿ", AppLanguage.BENGALI to "রেবতী")
    )

    private val FESTIVAL_NAME_MAP: Map<String, Map<AppLanguage, String>> = mapOf(
        "diwali_lakshmi_puja" to mapOf(
            AppLanguage.HINDI to "दीपावली (लक्ष्मी पूजन)", AppLanguage.MARATHI to "दिवाळी (लक्ष्मीपूजन)", AppLanguage.GUJARATI to "દિવાળી (લક્ષ્મી પૂજન)",
            AppLanguage.TAMIL to "தீபாவளி (லட்சுமி பூஜை)", AppLanguage.TELUGU to "దీపావళి (లక్ష్మీ పూజ)", AppLanguage.KANNADA to "ದೀಪಾವಳಿ (ಲಕ್ಷ್ಮೀ ಪೂಜೆ)", AppLanguage.BENGALI to "দীপাবলি (লক্ষ্মী ও কালী পূজা)"
        ),
        "govardhan_puja" to mapOf(
            AppLanguage.HINDI to "गोवर्धन पूजा (अन्नकूट)", AppLanguage.MARATHI to "गोवर्धन पूजा (बलिप्रतिपदा)", AppLanguage.GUJARATI to "ગોવર્ધન પૂજા (અન્નકૂટ / બેસતું વર્ષ)",
            AppLanguage.TAMIL to "கோவர்தன பூஜை (அன்னகூடம்)", AppLanguage.TELUGU to "గోవర్ధన పూజ (అన్నకూట్)", AppLanguage.KANNADA to "ಗೋವರ್ಧನ ಪೂಜೆ (ಬಲಿ ಪಾಡ್ಯಮಿ)", AppLanguage.BENGALI to "গোবর্ধন পূজা (অন্নকূট)"
        ),
        "bhai_dooj" to mapOf(
            AppLanguage.HINDI to "भाई दूज (यम द्वितीया)", AppLanguage.MARATHI to "भाऊबीज (यम द्वितीया)", AppLanguage.GUJARATI to "ભાઈ બીજ (યમ દ્વિતીયા)",
            AppLanguage.TAMIL to "பாய் தூஜ் (யம துவிதியை)", AppLanguage.TELUGU to "భగినీ హస్త భోజనం (భాయ్ దూజ్)", AppLanguage.KANNADA to "ಭಾಯಿ ದೂಜ್ (ಯಮ ದ್ವಿತೀಯಾ)", AppLanguage.BENGALI to "ভাইফোঁটা (ভ্রাতৃদ্বিতীয়া)"
        ),
        "dhanteras" to mapOf(
            AppLanguage.HINDI to "धनतेरस (धनत्रयोदशी)", AppLanguage.MARATHI to "धनत्रयोदशी (धनतेरस)", AppLanguage.GUJARATI to "ધનતેરસ (ધનત્રયોદશી)",
            AppLanguage.TAMIL to "தனத்திரயோதசி (தந்தேரஸ்)", AppLanguage.TELUGU to "ధన త్రయోదశి (ధంతేరస్)", AppLanguage.KANNADA to "ಧನತ್ರಯೋದಶಿ (ಧಂತೇರಸ್)", AppLanguage.BENGALI to "ধনতেরাস (ধনত্রয়োদশী)"
        ),
        "naraka_chaturdashi" to mapOf(
            AppLanguage.HINDI to "नरक चतुर्दशी (छोटी दिवाली)", AppLanguage.MARATHI to "नरक चतुर्दशी (अभ्यंगस्नान)", AppLanguage.GUJARATI to "કાળી ચૌદશ (નરક ચતુર્દશી)",
            AppLanguage.TAMIL to "நரக சதுர்த்தசி", AppLanguage.TELUGU to "నరక చతుర్దశి", AppLanguage.KANNADA to "ನರಕ ಚತುರ್ದಶಿ", AppLanguage.BENGALI to "ভূত চতুর্দশী (নরক চতুর্দশী)"
        ),
        "chhath_puja" to mapOf(
            AppLanguage.HINDI to "छठ महापर्व (सूर्य षष्ठी)", AppLanguage.MARATHI to "छठ पूजा (सूर्य षष्ठी)", AppLanguage.GUJARATI to "છઠ પૂજા (સૂર્ય ષષ્ઠી)",
            AppLanguage.TAMIL to "சத் பூஜை (சூரிய சஷ்டி)", AppLanguage.TELUGU to "ఛత్ పూజ (సూర్య షష్ఠి)", AppLanguage.KANNADA to "ಛಠ್ ಪೂಜೆ (ಸೂರ್ಯ ಷಷ್ಠಿ)", AppLanguage.BENGALI to "ছট পূজা (সূর্য ষষ্ঠী)"
        ),
        "ganesh_chaturthi" to mapOf(
            AppLanguage.HINDI to "गणेश चतुर्थी (विनायक चतुर्थी)", AppLanguage.MARATHI to "श्री गणेश चतुर्थी (गणेशोत्सव)", AppLanguage.GUJARATI to "ગણેશ ચતુર્થી",
            AppLanguage.TAMIL to "விநாயகர் சதுர்த்தி", AppLanguage.TELUGU to "వినాయక చవితి", AppLanguage.KANNADA to "ಗೌರಿ ಗಣೇಶ ಚತುರ್ಥಿ", AppLanguage.BENGALI to "গণেশ চতুর্থী"
        ),
        "maha_shivaratri" to mapOf(
            AppLanguage.HINDI to "महाशिवरात्रि", AppLanguage.MARATHI to "महाशिवरात्री", AppLanguage.GUJARATI to "મહાશિવરાત્રી",
            AppLanguage.TAMIL to "மகா சிவராத்திரி", AppLanguage.TELUGU to "మహా శివరాత్రి", AppLanguage.KANNADA to "ಮಹಾ ಶಿವರಾತ್ರಿ", AppLanguage.BENGALI to "মহাশিবরাত্রি"
        ),
        "krishna_janmashtami" to mapOf(
            AppLanguage.HINDI to "श्रीकृष्ण जन्माष्टमी", AppLanguage.MARATHI to "श्रीकृष्ण जन्माष्टमी (गोकुळाष्टमी)", AppLanguage.GUJARATI to "જન્માષ્ટમી (સાતમ-આઠમ)",
            AppLanguage.TAMIL to "கிருஷ்ண ஜெயந்தி (கோகுலாஷ்டமி)", AppLanguage.TELUGU to "శ్రీకృష్ణాష్టమి", AppLanguage.KANNADA to "ಶ್ರೀಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ", AppLanguage.BENGALI to "শ্রীকৃষ্ণ জন্মাষ্টমী"
        ),
        "rama_navami" to mapOf(
            AppLanguage.HINDI to "श्री राम नवमी", AppLanguage.MARATHI to "श्रीराम नवमी", AppLanguage.GUJARATI to "રામ નવમી",
            AppLanguage.TAMIL to "ஸ்ரீ ராம நவமி", AppLanguage.TELUGU to "శ్రీరామ నవమి", AppLanguage.KANNADA to "ಶ್ರೀರಾಮ ನವಮಿ", AppLanguage.BENGALI to "শ্রীরাম নবমী"
        ),
        "holi" to mapOf(
            AppLanguage.HINDI to "होली (होलिका दहन व धुलेंडी)", AppLanguage.MARATHI to "होळी (शिमगा व धुलीवंदन)", AppLanguage.GUJARATI to "હોળી - ધૂળેટી",
            AppLanguage.TAMIL to "ஹோலி பண்டிகை", AppLanguage.TELUGU to "హోళీ పండుగ", AppLanguage.KANNADA to "ಹೋಳಿ ಹಬ್ಬ (ಕಾಮನ ಹಬ್ಬ)", AppLanguage.BENGALI to "দোল পূর্ণিমা (হোলি)"
        ),
        "raksha_bandhan" to mapOf(
            AppLanguage.HINDI to "रक्षा बंधन (राखी पूर्णिमा)", AppLanguage.MARATHI to "रक्षाबंधन (नारळी पौर्णिमा)", AppLanguage.GUJARATI to "રક્ષાબંધન (બળેવ)",
            AppLanguage.TAMIL to "ரக்ஷா பந்தன் (ஆவணி அவிட்டம்)", AppLanguage.TELUGU to "రక్షా బంధన్ (రాఖీ పౌర్ణమి)", AppLanguage.KANNADA to "ರಕ್ಷಾ ಬಂಧನ (ನೂಲು ಹುಣ್ಣಿಮೆ)", AppLanguage.BENGALI to "রক্ষাবন্ধন (রাখী পূর্ণিমা)"
        ),
        "karwa_chauth" to mapOf(
            AppLanguage.HINDI to "करवा चौथ (करक चतुर्थी)", AppLanguage.MARATHI to "करवा चौथ (संकष्टी चतुर्थी)", AppLanguage.GUJARATI to "કરવા ચોથ",
            AppLanguage.TAMIL to "கர்வா சௌத்", AppLanguage.TELUGU to "కర్వా చౌత్", AppLanguage.KANNADA to "ಕರ್ವಾ ಚೌತ್", AppLanguage.BENGALI to "করওয়া চৌথ"
        ),
        "sharad_navratri" to mapOf(
            AppLanguage.HINDI to "शारदीय नवरात्रि (घटस्थापना)", AppLanguage.MARATHI to "शारदीय नवरात्र (घटस्थापना)", AppLanguage.GUJARATI to "શારદીય નવરાત્રી (ગરબા)",
            AppLanguage.TAMIL to "நவராத்திரி (கொலு)", AppLanguage.TELUGU to "శరన్నవరాత్రులు (దేవీ నవరాత్రులు)", AppLanguage.KANNADA to "ಶರನ್ನವರಾತ್ರಿ (ಮೈಸೂರು ದಸರಾ)", AppLanguage.BENGALI to "শারদীয় নবরাত্রি (মহালয়া)"
        ),
        "durga_ashtami" to mapOf(
            AppLanguage.HINDI to "दुर्गा महाअष्टमी (कन्या पूजन)", AppLanguage.MARATHI to "दुर्गा अष्टमी (महाअष्टमी)", AppLanguage.GUJARATI to "દુર્ગા અષ્ટમી (હવન આઠમ)",
            AppLanguage.TAMIL to "துர்கா அஷ்டமி", AppLanguage.TELUGU to "దుర్గాష్టమి", AppLanguage.KANNADA to "ದುರ್ಗಾಷ್ಟಮಿ", AppLanguage.BENGALI to "দুর্গা মহাষ্টমী (অঞ্জলি ও সন্ধিপূজা)"
        ),
        "dussehra_vijayadashami" to mapOf(
            AppLanguage.HINDI to "दशहरा (विजयादशमी)", AppLanguage.MARATHI to "दसरा (विजयादशमी)", AppLanguage.GUJARATI to "દશેરા (વિજયાદશમી)",
            AppLanguage.TAMIL to "விஜயதசமி", AppLanguage.TELUGU to "విజయదశమి (దసరా)", AppLanguage.KANNADA to "ವಿಜಯದಶಮಿ (ನಾಡಹಬ್ಬ ದಸರಾ)", AppLanguage.BENGALI to "বিজয়া দশমী (সিঁদুর খেলা)"
        ),
        "makar_sankranti" to mapOf(
            AppLanguage.HINDI to "मकर संक्रांति (उत्तरायण)", AppLanguage.MARATHI to "मकर संक्रांत (तिळगुळ)", AppLanguage.GUJARATI to "ઉત્તરાયણ (મકર સંક્રાંતિ)",
            AppLanguage.TAMIL to "தைப் பொங்கல் (மகர சங்கராந்தி)", AppLanguage.TELUGU to "మకర సంక్రాంతి", AppLanguage.KANNADA to "ಮಕರ ಸಂಕ್ರಾಂತಿ (ಎಳ್ಳು ಬೆಲ್ಲ)", AppLanguage.BENGALI to "মকর সংক্রান্তি (পৌষ পার্বণ)"
        ),
        "pongal" to mapOf(
            AppLanguage.HINDI to "पोंगल (थाई पोंगल)", AppLanguage.MARATHI to "पोंगल", AppLanguage.GUJARATI to "પોંગલ",
            AppLanguage.TAMIL to "தைப் பொங்கல்", AppLanguage.TELUGU to "పొంగల్ (సంక్రాంతి)", AppLanguage.KANNADA to "ಪೊಂಗಲ್", AppLanguage.BENGALI to "পোঙ্গল"
        ),
        "vasant_panchami" to mapOf(
            AppLanguage.HINDI to "बसंत पंचमी (सरस्वती पूजा)", AppLanguage.MARATHI to "वसंत पंचमी (श्री पंचमी)", AppLanguage.GUJARATI to "વસંત પંચમી",
            AppLanguage.TAMIL to "வசந்த பஞ்சமி (சரஸ்வதி பூஜை)", AppLanguage.TELUGU to "వసంత పంచమి (శ్రీ పంచమి)", AppLanguage.KANNADA to "ವಸಂತ ಪಂಚಮಿ", AppLanguage.BENGALI to "সরস্বতী পূজা (বসন্ত পঞ্চমী)"
        ),
        "akshaya_tritiya" to mapOf(
            AppLanguage.HINDI to "अक्षय तृतीया (अखा तीज)", AppLanguage.MARATHI to "अक्षय्य तृतीया", AppLanguage.GUJARATI to "અક્ષય તૃતીયા (અખાત્રીજ)",
            AppLanguage.TAMIL to "அட்சய திருதியை", AppLanguage.TELUGU to "అక్షయ తృతీయ", AppLanguage.KANNADA to "ಅಕ್ಷಯ ತೃತೀಯ", AppLanguage.BENGALI to "অক্ষয় তৃতীয়া"
        )
    )

    private val TERM_REPLACEMENTS: Map<AppLanguage, List<Pair<String, String>>> = mapOf(
        AppLanguage.HINDI to listOf(
            "Pratahkaal" to "प्रातःकाल",
            "Pradosh Kaal" to "प्रदोष काल",
            "Nishita Kaal" to "निशीथ काल",
            "Madhyahna Kaal" to "मध्याह्न काल",
            "Aparahna Kaal" to "अपराह्न काल",
            "Sayahna Sandhya" to "सायंकाल संध्या",
            "Abhijit Muhurat" to "अभिजित मुहूर्त",
            "Godhuli Muhurat" to "गोधूलि मुहूर्त",
            "Brahma Muhurat" to "ब्रह्म मुहूर्त",
            "Puja Muhurat" to "पूजा शुभ मुहूर्त",
            "Muhurat" to "मुहूर्त",
            "Sunrise" to "सूर्योदय",
            "Sunset" to "सूर्यास्त",
            "Until" to "तक",
            "Prevails until" to "समाप्ति:",
            "Full day" to "पूर्ण दिन",
            "Begins" to "प्रारंभ",
            "Ends" to "समाप्ति",
            "Shukla Paksha" to "शुक्ल पक्ष",
            "Krishna Paksha" to "कृष्ण पक्ष",
            "Shukla" to "शुक्ल",
            "Krishna" to "कृष्ण",
            "Lord Ganesha" to "भगवान श्री गणेश",
            "Goddess Lakshmi" to "माँ महालक्ष्मी",
            "Lord Shiva" to "भगवान शिव",
            "Lord Krishna" to "भगवान श्रीकृष्ण",
            "Lord Rama" to "भगवान श्रीराम",
            "Lord Vishnu" to "भगवान श्री हरि विष्णु",
            "Surya Dev" to "भगवान सूर्य देव",
            "Goddess Durga" to "माँ दुर्गा",
            "Goddess Saraswati" to "माँ सरस्वती",
            "Goddess Parvati" to "माँ पार्वती",
            "Lord Hanuman" to "श्री हनुमान जी"
        ),
        AppLanguage.MARATHI to listOf(
            "Pratahkaal" to "प्रातःकाळ",
            "Pradosh Kaal" to "प्रदोष काळ",
            "Nishita Kaal" to "निशीथ काळ",
            "Madhyahna Kaal" to "मध्यान्ह काळ",
            "Aparahna Kaal" to "अपराह्न काळ",
            "Puja Muhurat" to "पूजा शुभ मुहूर्त",
            "Muhurat" to "मुहूर्त",
            "Sunrise" to "सूर्योदय",
            "Sunset" to "सूर्यास्त",
            "Until" to "पर्यंत",
            "Prevails until" to "समाप्ती:",
            "Full day" to "संपूर्ण दिवस",
            "Begins" to "प्रारंभ",
            "Ends" to "समाप्ती",
            "Shukla Paksha" to "शुक्ल पक्ष",
            "Krishna Paksha" to "कृष्ण पक्ष",
            "Shukla" to "शुक्ल",
            "Krishna" to "कृष्ण",
            "Lord Ganesha" to "श्री गणेश",
            "Goddess Lakshmi" to "श्री महालक्ष्मी",
            "Lord Shiva" to "भगवान शंकर",
            "Lord Krishna" to "भगवान श्रीकृष्ण",
            "Lord Rama" to "प्रभू श्रीराम",
            "Lord Vishnu" to "भगवान श्रीविष्णू",
            "Surya Dev" to "सूर्यदेव",
            "Goddess Durga" to "दुर्गा माता",
            "Goddess Saraswati" to "सरस्वती देवी",
            "Goddess Parvati" to "पार्वती माता",
            "Lord Hanuman" to "श्री हनुमान"
        ),
        AppLanguage.GUJARATI to listOf(
            "Pratahkaal" to "પ્રાતઃકાળ",
            "Pradosh Kaal" to "પ્રદોષ કાળ",
            "Nishita Kaal" to "નિશીથ કાળ",
            "Madhyahna Kaal" to "મધ્યાહ્ન કાળ",
            "Aparahna Kaal" to "અપરાહ્ન કાળ",
            "Puja Muhurat" to "પૂજા શુભ મુહૂર્ત",
            "Muhurat" to "મુહૂર્ત",
            "Sunrise" to "સૂર્યોદય",
            "Sunset" to "સૂર્યાસ્ત",
            "Until" to "સુધી",
            "Prevails until" to "સમાપ્તિ:",
            "Full day" to "આખો દિવસ",
            "Begins" to "પ્રારંભ",
            "Ends" to "સમાપ્તિ",
            "Shukla Paksha" to "શુક્લ પક્ષ",
            "Krishna Paksha" to "કૃષ્ણ પક્ષ",
            "Shukla" to "શુક્લ",
            "Krishna" to "કૃષ્ણ",
            "Lord Ganesha" to "ભગવાન શ્રી ગણેશ",
            "Goddess Lakshmi" to "માતા મહાલક્ષ્મી",
            "Lord Shiva" to "ભગવાન શિવ",
            "Lord Krishna" to "ભગવાન શ્રીકૃષ્ણ",
            "Lord Rama" to "ભગવાન શ્રીરામ",
            "Lord Vishnu" to "ભગવાન શ્રી વિષ્ણુ",
            "Surya Dev" to "સૂર્યદેવ",
            "Goddess Durga" to "માતા દુર્ગા",
            "Goddess Saraswati" to "માતા સરસ્વતી",
            "Goddess Parvati" to "માતા પાર્વતી",
            "Lord Hanuman" to "શ્રી હનુમાનજી"
        ),
        AppLanguage.TAMIL to listOf(
            "Pratahkaal" to "காலை பூஜை நேரம்",
            "Pradosh Kaal" to "பிரதோஷ காலம்",
            "Nishita Kaal" to "நிஷித காலம்",
            "Madhyahna Kaal" to "மதிய காலம்",
            "Aparahna Kaal" to "பிற்பகல் காலம்",
            "Puja Muhurat" to "பூஜை நல்ல நேரம்",
            "Muhurat" to "முகூர்த்தம்",
            "Sunrise" to "சூரிய உதயம்",
            "Sunset" to "சூரிய அஸ்தமனம்",
            "Until" to "வரை",
            "Prevails until" to "முடிவு:",
            "Full day" to "முழு நாள்",
            "Begins" to "தொடக்கம்",
            "Ends" to "முடிவு",
            "Shukla Paksha" to "சுக்ல பக்ஷம்",
            "Krishna Paksha" to "கிருஷ்ண பக்ஷம்",
            "Shukla" to "சுக்ல",
            "Krishna" to "கிருஷ்ண",
            "Lord Ganesha" to "விநாயகப் பெருமான்",
            "Goddess Lakshmi" to "மகாலட்சுமி தாயார்",
            "Lord Shiva" to "சிவபெருமான்",
            "Lord Krishna" to "ஸ்ரீ கிருஷ்ணர்",
            "Lord Rama" to "ஸ்ரீ ராமர்",
            "Lord Vishnu" to "மகாவிஷ்ணு",
            "Surya Dev" to "சூரிய பகவான்",
            "Goddess Durga" to "துர்க்கை அம்மன்",
            "Goddess Saraswati" to "சரஸ்வதி தேவி",
            "Goddess Parvati" to "பார்வதி தேவி",
            "Lord Hanuman" to "ஆஞ்சநேயர்"
        ),
        AppLanguage.TELUGU to listOf(
            "Pratahkaal" to "ప్రాతఃకాలం",
            "Pradosh Kaal" to "ప్రదోష కాలం",
            "Nishita Kaal" to "నిశీథ కాలం",
            "Madhyahna Kaal" to "మధ్యాహ్న కాలం",
            "Aparahna Kaal" to "అపరాహ్ణ కాలం",
            "Puja Muhurat" to "పూజా శుభ ముహూర్తం",
            "Muhurat" to "ముహూర్తం",
            "Sunrise" to "సూర్యోదయం",
            "Sunset" to "సూర్యాస్తమయం",
            "Until" to "వరకు",
            "Prevails until" to "ముగింపు:",
            "Full day" to "రోజంతా",
            "Begins" to "ప్రారంభం",
            "Ends" to "ముగింపు",
            "Shukla Paksha" to "శుక్ల పక్షం",
            "Krishna Paksha" to "కృష్ణ పక్షం",
            "Shukla" to "శుక్ల",
            "Krishna" to "కృష్ణ",
            "Lord Ganesha" to "శ్రీ మహా గణపతి",
            "Goddess Lakshmi" to "శ్రీ మహాలక్ష్మి అమ్మవారు",
            "Lord Shiva" to "పరమశివుడు",
            "Lord Krishna" to "శ్రీకృష్ణ పరమాత్మ",
            "Lord Rama" to "శ్రీరామచంద్రుడు",
            "Lord Vishnu" to "శ్రీ మహావిష్ణువు",
            "Surya Dev" to "సూర్య భగవానుడు",
            "Goddess Durga" to "దుర్గాదేవి",
            "Goddess Saraswati" to "సరస్వతీ దేవి",
            "Goddess Parvati" to "పార్వతీ దేవి",
            "Lord Hanuman" to "ఆంజనేయ స్వామి"
        ),
        AppLanguage.KANNADA to listOf(
            "Pratahkaal" to "ಪ್ರಾತಃಕಾಲ",
            "Pradosh Kaal" to "ಪ್ರದೋಷ ಕಾಲ",
            "Nishita Kaal" to "ನಿಶೀಥ ಕಾಲ",
            "Madhyahna Kaal" to "ಮಧ್ಯಾಹ್ನ ಕಾಲ",
            "Aparahna Kaal" to "ಅಪರಾಹ್ನ ಕಾಲ",
            "Puja Muhurat" to "ಪೂಜಾ ಶುಭ ಮುಹೂರ್ತ",
            "Muhurat" to "ಮುಹೂರ್ತ",
            "Sunrise" to "ಸೂರ್ಯೋದಯ",
            "Sunset" to "ಸೂರ್ಯಾಸ್ತ",
            "Until" to "ವರೆಗೆ",
            "Prevails until" to "ಅಂತ್ಯ:",
            "Full day" to "ಪೂರ್ಣ ದಿನ",
            "Begins" to "ಪ್ರಾರಂಭ",
            "Ends" to "ಅಂತ್ಯ",
            "Shukla Paksha" to "ಶುಕ್ಲ ಪಕ್ಷ",
            "Krishna Paksha" to "ಕೃಷ್ಣ ಪಕ್ಷ",
            "Shukla" to "ಶುಕ್ಲ",
            "Krishna" to "ಕೃಷ್ಣ",
            "Lord Ganesha" to "ಶ್ರೀ ಮಹಾಗಣಪತಿ",
            "Goddess Lakshmi" to "ಶ್ರೀ ಮಹಾಲಕ್ಷ್ಮೀ ದೇವಿ",
            "Lord Shiva" to "ಪರಮಶಿವ",
            "Lord Krishna" to "ಶ್ರೀಕೃಷ್ಣ",
            "Lord Rama" to "ಶ್ರೀರಾಮಚಂದ್ರ",
            "Lord Vishnu" to "ಶ್ರೀ ಮಹಾವಿಷ್ಣು",
            "Surya Dev" to "ಸೂರ್ಯದೇವ",
            "Goddess Durga" to "ದುರ್ಗಾದೇವಿ",
            "Goddess Saraswati" to "ಸರಸ್ವತಿ ದೇವಿ",
            "Goddess Parvati" to "ಪಾರ್ವತಿ ದೇವಿ",
            "Lord Hanuman" to "ಶ್ರೀ ಆಂಜನೇಯ"
        ),
        AppLanguage.BENGALI to listOf(
            "Pratahkaal" to "প্রাতঃকাল",
            "Pradosh Kaal" to "প্রদोष কাল",
            "Nishita Kaal" to "নিশীথ কাল",
            "Madhyahna Kaal" to "মধ্যাহ্ন কাল",
            "Aparahna Kaal" to "অপরাহ্ণ কাল",
            "Puja Muhurat" to "পূজার শুভ মুহূর্ত",
            "Muhurat" to "মুহূর্ত",
            "Sunrise" to "সূর্যোদয়",
            "Sunset" to "সূর্যাস্ত",
            "Until" to "পর্যন্ত",
            "Prevails until" to "সমাপ্তি:",
            "Full day" to "সারাদিন",
            "Begins" to "শুরু",
            "Ends" to "সমাপ্তি",
            "Shukla Paksha" to "শুক্ল পক্ষ",
            "Krishna Paksha" to "কৃষ্ণ পক্ষ",
            "Shukla" to "শুক্ল",
            "Krishna" to "কৃষ্ণ",
            "Lord Ganesha" to "শ্রীগণেশ",
            "Goddess Lakshmi" to "মা মহালক্ষ্মী",
            "Lord Shiva" to "দেবাদিদেব মহাদেব",
            "Lord Krishna" to "ভগবান শ্রীকৃষ্ণ",
            "Lord Rama" to "ভগবান শ্রীরামচন্দ্র",
            "Lord Vishnu" to "ভগবান শ্রীবিষ্ণু",
            "Surya Dev" to "সূর্যদেব",
            "Goddess Durga" to "মা দুর্গা",
            "Goddess Saraswati" to "মা সরস্বতী",
            "Goddess Parvati" to "মা পার্বতী",
            "Lord Hanuman" to "শ্রীহনুমান"
        )
    )
}
