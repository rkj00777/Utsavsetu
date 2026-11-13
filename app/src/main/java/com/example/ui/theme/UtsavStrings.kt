package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.preferences.AppLanguage

@Immutable
data class UtsavStrings(
    val language: AppLanguage,
    val tabToday: String,
    val tabCalendar: String,
    val tabFestivals: String,
    val tabPanchang: String,
    val tabProfile: String,
    val nextMajorFestival: String,
    val todaysPanchang: String,
    val auspiciousWindow: String,
    val rahuKaalAvoid: String,
    val comingUpInYourTradition: String,
    val prepareForNextFestival: String,
    val startPujaGuide: String,
    val shareGreeting: String,
    val setReminder: String,
    val dailyPanchangHeader: String,
    val sunriseLabel: String,
    val sunsetLabel: String,
    val tithiLabel: String,
    val nakshatraLabel: String,
    val yogaLabel: String,
    val karanaLabel: String,
    val settingsTitle: String,
    // Welcome & Setup Screen Localized Strings
    val stepTitles: List<String> = listOf("Welcome", "Language", "Your City", "Tradition", "Reminders"),
    val exploreFirstCta: String = "Explore First →",
    val heroTagline: String = "Your festivals, beautifully connected.",
    val heroSubtagline: String = "Accurate local Muhurat, authentic regional Vidhi, and timely reminders.",
    val welcomeTitle: String = "Welcome to UtsavSetu",
    val welcomeBody: String = "Designed for families across India so home-state traditions, tithis, and puja timings stay accurate in your city.",
    val welcomePillFestivals: String = "82+ Major Festivals",
    val welcomePillPanchang: String = "City Panchang",
    val welcomePillVidhi: String = "Vidhi & Samagri",
    val welcomeOfflineNote: String = "Works 100% offline. Take 30 seconds to personalise your language, city, and tradition—or tap 'Explore First' above.",
    val stepLanguageTitle: String = "1. Choose Your Language",
    val stepLanguageSubtitle: String = "Selecting a language immediately switches the entire app interface, dates, and greetings.",
    val stepCityTitle: String = "2. Choose Your City (Local Panchang)",
    val stepCitySubtitle: String = "Sunrise, sunset, Rahu Kaal, and Puja Muhurat timings depend on your city's exact coordinates.",
    val useCurrentLocationGps: String = "Use Current Location (GPS)",
    val detectingLocationLabel: String = "Detecting your city via GPS…",
    val topCitiesHeader: String = "Top Metro Cities",
    val moreCitiesHeader: String = "All Indian Cities & Pilgrimage Towns",
    val citySearchPlaceholder: String = "Search city (e.g. Mumbai, Bengaluru, Varanasi, Madurai)…",
    val selectedCityPrefix: String = "Selected",
    val changeButton: String = "Change",
    val stepTraditionTitle: String = "3. Regional Tradition & Calendar Convention",
    val stepTraditionSubtitle: String = "Select one or more family traditions. North Indian traditions follow Purnimanta, while Western, Southern & Eastern traditions follow Amanta.",
    val stepRemindersTitle: String = "4. Timely Festival & Muhurat Reminders",
    val stepRemindersSubtitle: String = "Choose when UtsavSetu should remind your family before upcoming observances.",
    val backButton: String = "Back",
    val nextPrefix: String = "Next",
    val getStartedEnter: String = "Get Started · Enter UtsavSetu",
    val saveDefaultsEnter: String = "Save Defaults & Enter UtsavSetu",
    // Multi-name & Online Discovery Strings
    val alsoKnownAsLabel: String = "Also called",
    val searchOnlineButton: String = "Search Online & Add to UtsavSetu",
    val searchingOnlineLabel: String = "Searching online Panchang & Wikipedia knowledge base…",
    // System-Wide Screen Labels (Today, Calendar, Festivals, Panchang, Detail, Profile)
    val quickActionsHeader: String = "QUICK ACTIONS",
    val upcomingFestivalsHeader: String = "Upcoming Festivals",
    val upcomingFestivalsSubtitlePrefix: String = "Verified dates & muhurats for",
    val seeAllButton: String = "See All",
    val exploreVidhiButton: String = "Explore Vidhi",
    val todayBadge: String = "Today",
    val tomorrowBadge: String = "Tomorrow",
    val inDaysFormat: String = "In %d days",
    val monthViewLabel: String = "Month View",
    val agendaViewLabel: String = "Agenda View",
    val myTraditionsAllLabel: String = "My Traditions (All)",
    val allRegionsLabel: String = "All Regions",
    val browseByCategoryHeader: String = "BROWSE BY CATEGORY",
    val featuredCelebrationHeader: String = "FEATURED CELEBRATION",
    val searchFestivalsPlaceholder: String = "Search any festival (Bhai Dooj, Govardhan, Diwali, Pongal, Ugadi)…",
    val panchangSubtitle: String = "Daily Astronomical & Ritual Timings",
    val panchangDetailsHeader: String = "PANCHANG DETAILS",
    val auspiciousTimingsHeader: String = "AUSPICIOUS TIMINGS (SHUBH MUHURAT)",
    val inauspiciousTimingsHeader: String = "INAUSPICIOUS TIMINGS (AVOID)",
    val importantTimingsHeader: String = "KEY TIMINGS, TITHI & SHUBH MUHURAT",
    val verifiedMeeusBadge: String = "Verified · Meeus Ephemeris",
    val calculatedEstimateBadge: String = "Calculated Estimate",
    val timingParameterColHeader: String = "Panchang Parameter",
    val timingWindowColHeader: String = "Auspicious Timing / Detail (IST)",
    val festivalDateRowLabel: String = "Festival Date",
    val primaryMuhuratRowLabel: String = "Primary Puja Muhurat",
    val additionalMuhuratRowLabel: String = "Muhurat Breakdown",
    val tithiAndPakshaRowLabel: String = "Tithi & Paksha",
    val tithiSpanRowLabel: String = "Tithi Begins & Ends",
    val nakshatraRowLabel: String = "Nakshatra & Deity",
    val sunriseSunsetRowLabel: String = "Sunrise & Sunset",
    val auspiciousKaalRowLabel: String = "Brahma, Abhijit & Godhuli",
    val avoidKaalRowLabel: String = "Rahu Kaal & Yamagandam",
    val lunarMonthRowLabel: String = "Lunar Month (Masa)",
    val shastraRuleRowLabel: String = "Shastra Observance Rule",
    val twentyYearExplorerLabel: String = "20-Year Panchang & Muhurat Explorer (2025–2046)",
    val whyCelebratedHeader: String = "WHY IT IS CELEBRATED & SACRED KATHA",
    val puranicLegendHeader: String = "PURANIC LEGEND & ORIGIN (KATHA)",
    val presidingDeityHeader: String = "PRESIDING DEITY",
    val primaryMoolaMantraHeader: String = "PRIMARY MOOLA MANTRA (CHANT DURING VIDHI)",
    val vratAndNaivedyaHeader: String = "VRAT, FASTING & NAIVEDYA (PRASAD) GUIDE",
    val regionalNamesHeaderSuffix: String = "REGIONAL NAMES ACROSS INDIA",
    val vidhiTabPrefix: String = "Vidhi",
    val samagriTabPrefix: String = "Samagri",
    val mantrasTabPrefix: String = "Mantras",
    val guidedModeButton: String = "Guided Mode",
    val essentialItemsHeader: String = "ESSENTIAL ITEMS",
    val optionalItemsHeader: String = "OPTIONAL & REGIONAL ITEMS",
    val familyNotesHeader: String = "Family Parampara & Custom Notes",
    val japaCounterHeader: String = "SACRED JAPA MALA COUNTER",
    val meaningPrefix: String = "Meaning",
    val languageSectionHeader: String = "LANGUAGE",
    val locationSectionHeader: String = "LOCATION",
    val traditionsSectionHeader: String = "PANCHANG & REGIONAL TRADITIONS",
    val remindersSectionHeader: String = "REMINDERS",
    val appPreferencesSectionHeader: String = "APP PREFERENCES",
    val dataPrivacySectionHeader: String = "DATA & PRIVACY",
    val aboutSectionHeader: String = "ABOUT UTSAVSETU",
    val appearanceThemeLabel: String = "Appearance Theme",
    val textSizeLabel: String = "Accessibility & Text Size",
    val standardTextLabel: String = "Standard Text",
    val largeTextLabel: String = "Comfortable Large"
) {
    val pujaMuhuratLabel: String get() = primaryMuhuratRowLabel
    val lunarMonthLabel: String get() = lunarMonthRowLabel
    val brahmaMuhuratLabel: String get() = when (language) {
        AppLanguage.HINDI -> "ब्रह्म मुहूर्त"
        AppLanguage.MARATHI -> "ब्रह्म मुहूर्त"
        AppLanguage.GUJARATI -> "બ્રહ્મ મુહૂર્ત"
        AppLanguage.TAMIL -> "பிரம்ம முகூர்த்தம்"
        AppLanguage.TELUGU -> "బ్రహ్మ ముహూర్తం"
        AppLanguage.KANNADA -> "ಬ್ರಹ್ಮ ಮುಹೂರ್ತ"
        AppLanguage.BENGALI -> "ব্রহ্ম মুহূর্ত"
        AppLanguage.ENGLISH -> "Brahma Muhurat"
    }
    val abhijitMuhuratLabel: String get() = when (language) {
        AppLanguage.HINDI -> "अभिजित मुहूर्त"
        AppLanguage.MARATHI -> "अभिजित मुहूर्त"
        AppLanguage.GUJARATI -> "અભિજિત મુહૂર્ત"
        AppLanguage.TAMIL -> "அபிஜித் முகூர்த்தம்"
        AppLanguage.TELUGU -> "అభిజిత్ ముహూర్తం"
        AppLanguage.KANNADA -> "ಅಭಿಜಿತ್ ಮುಹೂರ್ತ"
        AppLanguage.BENGALI -> "অভিজিৎ মুহূর্ত"
        AppLanguage.ENGLISH -> "Abhijit Muhurat"
    }
    val rahuKaalLabel: String get() = rahuKaalAvoid
    val viewPujaGuideButton: String get() = startPujaGuide
    val samagriChecklistHeader: String get() = samagriTabPrefix
    val shareFestivalAction: String get() = shareGreeting
    val pujaVidhiHeader: String get() = vidhiTabPrefix
    val remindAction: String get() = setReminder
    val inDaysPrefix: String get() = when (language) {
        AppLanguage.HINDI -> ""
        AppLanguage.MARATHI -> ""
        AppLanguage.GUJARATI -> ""
        AppLanguage.TAMIL -> ""
        AppLanguage.TELUGU -> ""
        AppLanguage.KANNADA -> ""
        AppLanguage.BENGALI -> ""
        AppLanguage.ENGLISH -> "In"
    }
    val daysSuffix: String get() = when (language) {
        AppLanguage.HINDI -> "दिन बाद"
        AppLanguage.MARATHI -> "दिवसांनी"
        AppLanguage.GUJARATI -> "દિવસમાં"
        AppLanguage.TAMIL -> "நாட்களில்"
        AppLanguage.TELUGU -> "రోజుల్లో"
        AppLanguage.KANNADA -> "ದಿನಗಳಲ್ಲಿ"
        AppLanguage.BENGALI -> "দিন পর"
        AppLanguage.ENGLISH -> "days"
    }
    val todaysFestivalBadge: String get() = when (language) {
        AppLanguage.HINDI -> "आज का पर्व"
        AppLanguage.MARATHI -> "आजचा सण"
        AppLanguage.GUJARATI -> "આજનો તહેવાર"
        AppLanguage.TAMIL -> "இன்றைய பண்டிகை"
        AppLanguage.TELUGU -> "నేటి పండుగ"
        AppLanguage.KANNADA -> "ಇಂದಿನ ಹಬ್ಬ"
        AppLanguage.BENGALI -> "আজকের উৎসব"
        AppLanguage.ENGLISH -> "TODAY'S FESTIVAL"
    }
    val todaysObservanceBadge: String get() = when (language) {
        AppLanguage.HINDI -> "आज का व्रत / पर्व"
        AppLanguage.MARATHI -> "आजचे व्रत / सण"
        AppLanguage.GUJARATI -> "આજનું વ્રત / પર્વ"
        AppLanguage.TAMIL -> "இன்றைய விரதம்"
        AppLanguage.TELUGU -> "నేటి వ్రతం"
        AppLanguage.KANNADA -> "ಇಂದಿನ ವ್ರತ"
        AppLanguage.BENGALI -> "আজকের ব্রত"
        AppLanguage.ENGLISH -> "TODAY'S OBSERVANCE"
    }
    val untilLabel: String get() = when (language) {
        AppLanguage.HINDI -> "समाप्ति:"
        AppLanguage.MARATHI -> "समाप्ती:"
        AppLanguage.GUJARATI -> "સમાપ્તિ:"
        AppLanguage.TAMIL -> "முடிவு:"
        AppLanguage.TELUGU -> "ముగింపు:"
        AppLanguage.KANNADA -> "ಅಂತ್ಯ:"
        AppLanguage.BENGALI -> "সমাপ্তি:"
        AppLanguage.ENGLISH -> "Until"
    }
    val fullDayLabel: String get() = when (language) {
        AppLanguage.HINDI -> "पूर्ण दिन"
        AppLanguage.MARATHI -> "संपूर्ण दिवस"
        AppLanguage.GUJARATI -> "આખો દિવસ"
        AppLanguage.TAMIL -> "முழு நாள்"
        AppLanguage.TELUGU -> "రోజంతా"
        AppLanguage.KANNADA -> "ಪೂರ್ಣ ದಿನ"
        AppLanguage.BENGALI -> "সারাদিন"
        AppLanguage.ENGLISH -> "Full day"
    }
    val beginsLabel: String get() = when (language) {
        AppLanguage.HINDI -> "प्रारंभ:"
        AppLanguage.MARATHI -> "प्रारंभ:"
        AppLanguage.GUJARATI -> "પ્રારંભ:"
        AppLanguage.TAMIL -> "தொடக்கம்:"
        AppLanguage.TELUGU -> "ప్రారంభం:"
        AppLanguage.KANNADA -> "ಪ್ರಾರಂಭ:"
        AppLanguage.BENGALI -> "শুরু:"
        AppLanguage.ENGLISH -> "Begins:"
    }
    val endsLabel: String get() = when (language) {
        AppLanguage.HINDI -> "समाप्ति:"
        AppLanguage.MARATHI -> "समाप्ती:"
        AppLanguage.GUJARATI -> "સમાપ્તિ:"
        AppLanguage.TAMIL -> "முடிவு:"
        AppLanguage.TELUGU -> "ముగింపు:"
        AppLanguage.KANNADA -> "ಅಂತ್ಯ:"
        AppLanguage.BENGALI -> "সমাপ্তি:"
        AppLanguage.ENGLISH -> "Ends:"
    }
    val showDetailsLabel: String get() = when (language) {
        AppLanguage.HINDI -> "विस्तृत जानकारी देखें"
        AppLanguage.MARATHI -> "सविस्तर माहिती पहा"
        AppLanguage.GUJARATI -> "વિગતવાર માહિતી જુઓ"
        AppLanguage.TAMIL -> "முழு விவரங்களைக் காண்க"
        AppLanguage.TELUGU -> "పూర్తి వివరాలు చూడండి"
        AppLanguage.KANNADA -> "ಸಂಪೂರ್ಣ ವಿವರಗಳನ್ನು ನೋಡಿ"
        AppLanguage.BENGALI -> "বিস্তারিত তথ্য দেখুন"
        AppLanguage.ENGLISH -> "Show Detailed Info"
    }
    val hideDetailsLabel: String get() = when (language) {
        AppLanguage.HINDI -> "संक्षिप्त करें"
        AppLanguage.MARATHI -> "माहिती लपवा"
        AppLanguage.GUJARATI -> "વિગતો છુપાવો"
        AppLanguage.TAMIL -> "விவரங்களை மறை"
        AppLanguage.TELUGU -> "వివరాలు దాచండి"
        AppLanguage.KANNADA -> "ವಿವರಗಳನ್ನು ಮರೆಮಾಡಿ"
        AppLanguage.BENGALI -> "সংক্ষিপ্ত করুন"
        AppLanguage.ENGLISH -> "Hide Details"
    }
    val moreTimingsDropdownLabel: String get() = when (language) {
        AppLanguage.HINDI -> "संपूर्ण पंचांग तालिका एवं 20-वर्षीय मुहूर्त दर्शक"
        AppLanguage.MARATHI -> "संपूर्ण पंचांग तक्ता व २०-वर्षीय मुहूर्त दर्शक"
        AppLanguage.GUJARATI -> "સંપૂર્ણ પંચાંગ કોષ્ટક અને ૨૦-વર્ષીય મુહૂર્ત"
        AppLanguage.TAMIL -> "முழு பஞ்சாங்க அட்டவணை & 20-ஆண்டு முகூர்த்தம்"
        AppLanguage.TELUGU -> "పూర్తి పంచాంగ పట్టిక & 20-ఏళ్ల ముహూర్తాలు"
        AppLanguage.KANNADA -> "ಸಂಪೂರ್ಣ ಪಂಚಾಂಗ ಕೋಷ್ಟಕ ಮತ್ತು 20-ವರ್ಷಗಳ ಮುಹೂರ್ತ"
        AppLanguage.BENGALI -> "সম্পূর্ণ পঞ্জিকা সারণী ও ২০-বছরের শুভ মুহূর্ত"
        AppLanguage.ENGLISH -> "Full Panchang Table & 20-Year Explorer"
    }
    val filtersAndJumpDropdownLabel: String get() = when (language) {
        AppLanguage.HINDI -> "क्षेत्रीय परंपरा, वर्ष और महीना चुनें"
        AppLanguage.MARATHI -> "परंपरा, वर्ष आणि महिना निवडा"
        AppLanguage.GUJARATI -> "પરંપરા, વર્ષ અને મહિનો પસંદ કરો"
        AppLanguage.TAMIL -> "மரபு, ஆண்டு & மாதம் தேர்வு செய்க"
        AppLanguage.TELUGU -> "సంప్రదాయం, సంవత్సరం & నెల ఎంచుకోండి"
        AppLanguage.KANNADA -> "ಸಂಪ್ರದಾಯ, ವರ್ಷ ಮತ್ತು ತಿಂಗಳು ಆಯ್ಕೆಮಾಡಿ"
        AppLanguage.BENGALI -> "পরম্পরা, বছর ও মাস নির্বাচন করুন"
        AppLanguage.ENGLISH -> "Filter Tradition & Jump to Year / Month"
    }
    val stepMantraAndSamagriLabel: String get() = when (language) {
        AppLanguage.HINDI -> "इस चरण का मंत्र एवं आवश्यक सामग्री"
        AppLanguage.MARATHI -> "या पायरीचा मंत्र आणि आवश्यक सामग्री"
        AppLanguage.GUJARATI -> "આ પગલાનો મંત્ર અને પૂજા સામગ્રી"
        AppLanguage.TAMIL -> "இந்த படியின் மந்திரம் & பூஜை பொருட்கள்"
        AppLanguage.TELUGU -> "ఈ దశ మంత్రం & అవసరమైన సామగ్రి"
        AppLanguage.KANNADA -> "ಈ ಹಂತದ ಮಂತ್ರ ಮತ್ತು ಪೂಜಾ ಸಾಮಗ್ರಿ"
        AppLanguage.BENGALI -> "এই ধাপের মন্ত্র ও প্রয়োজনীয় সামগ্রী"
        AppLanguage.ENGLISH -> "Step Mantra & Required Samagri"
    }
    val yamagandamLabel: String get() = when (language) {
        AppLanguage.HINDI -> "यमगंडम काल"
        AppLanguage.MARATHI -> "यमगंडम काळ"
        AppLanguage.GUJARATI -> "યમગંડમ કાળ"
        AppLanguage.TAMIL -> "யமகண்டம்"
        AppLanguage.TELUGU -> "యమగండం"
        AppLanguage.KANNADA -> "ಯಮಗಂಡ ಕಾಲ"
        AppLanguage.BENGALI -> "যমগণ্ডম কাল"
        AppLanguage.ENGLISH -> "Yamagandam"
    }
    val gulikaKaalLabel: String get() = when (language) {
        AppLanguage.HINDI -> "गुलिक काल"
        AppLanguage.MARATHI -> "गुलिक काळ"
        AppLanguage.GUJARATI -> "ગુલિક કાળ"
        AppLanguage.TAMIL -> "குளிகை காலம்"
        AppLanguage.TELUGU -> "గుళిక కాలం"
        AppLanguage.KANNADA -> "ಗುಳಿಕ ಕಾಲ"
        AppLanguage.BENGALI -> "গুলিক কাল"
        AppLanguage.ENGLISH -> "Gulika Kaal"
    }
    val astronomicalNoteLabel: String get() = verifiedMeeusBadge
    val shastraRuleLabel: String get() = shastraRuleRowLabel
    val appSubtitle: String get() = heroSubtagline
    val tabSamagri: String get() = samagriTabPrefix
    val dayWordSingular: String get() = when (language) {
        AppLanguage.HINDI -> "दिन"
        AppLanguage.MARATHI -> "दिवस"
        AppLanguage.GUJARATI -> "દિવસ"
        AppLanguage.TAMIL -> "நாள்"
        AppLanguage.TELUGU -> "రోజు"
        AppLanguage.KANNADA -> "ದಿನ"
        AppLanguage.BENGALI -> "দিন"
        AppLanguage.ENGLISH -> "DAY"
    }
    val dayWordPlural: String get() = when (language) {
        AppLanguage.HINDI -> "दिन"
        AppLanguage.MARATHI -> "दिवस"
        AppLanguage.GUJARATI -> "દિવસ"
        AppLanguage.TAMIL -> "நாட்கள்"
        AppLanguage.TELUGU -> "రోజులు"
        AppLanguage.KANNADA -> "ದಿನಗಳು"
        AppLanguage.BENGALI -> "দিন"
        AppLanguage.ENGLISH -> "DAYS"
    }
    val stepWordLabel: String get() = when (language) {
        AppLanguage.HINDI -> "चरण"
        AppLanguage.MARATHI -> "पायरी"
        AppLanguage.GUJARATI -> "પગલું"
        AppLanguage.TAMIL -> "படி"
        AppLanguage.TELUGU -> "దశ"
        AppLanguage.KANNADA -> "ಹಂತ"
        AppLanguage.BENGALI -> "ধাপ"
        AppLanguage.ENGLISH -> "Step"
    }
    val minutesShortLabel: String get() = when (language) {
        AppLanguage.HINDI -> "मिनट"
        AppLanguage.MARATHI -> "मिनिटे"
        AppLanguage.GUJARATI -> "મિનિટ"
        AppLanguage.TAMIL -> "நிமிடம்"
        AppLanguage.TELUGU -> "నిమి"
        AppLanguage.KANNADA -> "ನಿಮಿಷ"
        AppLanguage.BENGALI -> "মিনিট"
        AppLanguage.ENGLISH -> "min"
    }
    val resetButtonLabel: String get() = when (language) {
        AppLanguage.HINDI -> "रीसेट"
        AppLanguage.MARATHI -> "रीसेट"
        AppLanguage.GUJARATI -> "રીસેટ"
        AppLanguage.TAMIL -> "மீட்டமை"
        AppLanguage.TELUGU -> "రీసెట్"
        AppLanguage.KANNADA -> "ಮರುಹೊಂದಿಸಿ"
        AppLanguage.BENGALI -> "রিসেট"
        AppLanguage.ENGLISH -> "Reset"
    }
    val offlineSavedNote: String get() = when (language) {
        AppLanguage.HINDI -> "आपकी प्रगति ऑफ़लाइन सुरक्षित रहती है।"
        AppLanguage.MARATHI -> "तुमची प्रगती ऑफलाईन सुरक्षित राहते."
        AppLanguage.GUJARATI -> "તમારી પ્રગતિ ઑફલાઇન સુરક્ષિત રહે છે."
        AppLanguage.TAMIL -> "உங்கள் முன்னேற்றம் ஆஃப்லைனில் சேமிக்கப்படும்."
        AppLanguage.TELUGU -> "మీ పురోగతి ఆఫ్‌లైన్‌లో భద్రపరచబడుతుంది."
        AppLanguage.KANNADA -> "ನಿಮ್ಮ ಪ್ರಗತಿಯನ್ನು ಆಫ್‌ಲೈನ್‌ನಲ್ಲಿ ಉಳಿಸಲಾಗಿದೆ."
        AppLanguage.BENGALI -> "আপনার অগ্রগতি অফলাইনে সংরক্ষিত থাকে।"
        AppLanguage.ENGLISH -> "Progress is saved offline automatically."
    }
    val familyNotesPlaceholder: String get() = when (language) {
        AppLanguage.HINDI -> "पारिवारिक भोग, कुलदेवता पुष्प या निजी पूजा नोट्स लिखें…"
        AppLanguage.MARATHI -> "कौटुंबिक नैवेद्य, कुलदैवत फुले किंवा पूजा टिपा लिहा…"
        AppLanguage.GUJARATI -> "પારિવારિક નૈવેદ્ય, કુળદેવતા પુષ્પ કે પૂજા નોંધ લખો…"
        AppLanguage.TAMIL -> "குடும்ப நைவேத்தியம், குலதெய்வ மலர்கள் அல்லது குறிப்புகளை எழுதுங்கள்…"
        AppLanguage.TELUGU -> "కుటుంబ నైవేద్యం, కులదైవ పుష్పాలు లేదా గమనికలు రాయండి…"
        AppLanguage.KANNADA -> "ಕುಟುಂಬದ ನೈವೇದ್ಯ, ಕುಲದೇವತಾ ಹೂವುಗಳು ಅಥವಾ ಟಿಪ್ಪಣಿಗಳನ್ನು ಬರೆಯಿರಿ…"
        AppLanguage.BENGALI -> "পারিবারিক ভোগ, কুলদেবতার পুষ্প বা নিজস্ব নোট লিখুন…"
        AppLanguage.ENGLISH -> "Add family-specific naivedya, kuldevata flowers, or custom reminders…"
    }
    val googleCloudBackupLabel: String get() = when (language) {
        AppLanguage.HINDI -> "गूगल क्लाउड बैकअप (Google Sync)"
        AppLanguage.MARATHI -> "गूगल क्लाउड बॅकअप (Google Sync)"
        AppLanguage.GUJARATI -> "ગૂગલ ક્લાઉડ બેકઅપ (Google Sync)"
        AppLanguage.TAMIL -> "கூகுள் கிளவுட் பேக்கப் (Google Sync)"
        AppLanguage.TELUGU -> "గూగుల్ క్లౌడ్ బ్యాకప్ (Google Sync)"
        AppLanguage.KANNADA -> "ಗೂಗಲ್ ಕ್ಲೌಡ್ ಬ್ಯಾಕಪ್ (Google Sync)"
        AppLanguage.BENGALI -> "গুগল ক্লাউড ব্যাকআপ (Google Sync)"
        AppLanguage.ENGLISH -> "Google Cloud Backup"
    }
    val signOutLabel: String get() = when (language) {
        AppLanguage.HINDI -> "लॉग आउट"
        AppLanguage.MARATHI -> "बाहेर पडा"
        AppLanguage.GUJARATI -> "લૉગ આઉટ"
        AppLanguage.TAMIL -> "வெளியேறு"
        AppLanguage.TELUGU -> "సైన్ అవుట్"
        AppLanguage.KANNADA -> "ಸೈನ್ ಔಟ್"
        AppLanguage.BENGALI -> "সাইন আউট"
        AppLanguage.ENGLISH -> "Sign Out"
    }
    val clearLocalAndCloudDataLabel: String get() = when (language) {
        AppLanguage.HINDI -> "लोकल और क्लाउड डेटा मिटाएं"
        AppLanguage.MARATHI -> "स्थानिक आणि क्लाउड डेटा हटवा"
        AppLanguage.GUJARATI -> "સ્થાનિક અને ક્લાઉડ ડેટા ભૂંસી નાખો"
        AppLanguage.TAMIL -> "உள்ளூர் & கிளவுட் தரவை அழி"
        AppLanguage.TELUGU -> "లోకల్ & క్లౌడ్ డేటాను తొలగించండి"
        AppLanguage.KANNADA -> "ಸ್ಥಳೀಯ ಮತ್ತು ಕ್ಲೌಡ್ ಡೇಟಾ ಅಳಿಸಿ"
        AppLanguage.BENGALI -> "লোকাল ও ক্লাউড ডেটা মুছুন"
        AppLanguage.ENGLISH -> "Clear Local & Cloud Data"
    }
    val autoThemeLabel: String get() = when (language) {
        AppLanguage.HINDI -> "स्वत: (Auto)"
        AppLanguage.MARATHI -> "ऑटो"
        AppLanguage.GUJARATI -> "ઑટો"
        AppLanguage.TAMIL -> "தானியங்கி"
        AppLanguage.TELUGU -> "ఆటో"
        AppLanguage.KANNADA -> "ಸ್ವಯಂ"
        AppLanguage.BENGALI -> "অটো"
        AppLanguage.ENGLISH -> "Auto"
    }
    val lightThemeLabel: String get() = when (language) {
        AppLanguage.HINDI -> "लाइट"
        AppLanguage.MARATHI -> "लाईट"
        AppLanguage.GUJARATI -> "લાઇટ"
        AppLanguage.TAMIL -> "வெளிச்சம்"
        AppLanguage.TELUGU -> "లైట్"
        AppLanguage.KANNADA -> "ಲೈಟ್"
        AppLanguage.BENGALI -> "লাইট"
        AppLanguage.ENGLISH -> "Light"
    }
    val darkThemeLabel: String get() = when (language) {
        AppLanguage.HINDI -> "डार्क"
        AppLanguage.MARATHI -> "डार्क"
        AppLanguage.GUJARATI -> "ડાર્ક"
        AppLanguage.TAMIL -> "இருள்"
        AppLanguage.TELUGU -> "డార్క్"
        AppLanguage.KANNADA -> "ಡಾರ್ಕ್"
        AppLanguage.BENGALI -> "ডার্ক"
        AppLanguage.ENGLISH -> "Dark"
    }
    val dayBeforeReminderTitle: String get() = when (language) {
        AppLanguage.HINDI -> "1 दिन पूर्व (शाम 7:00 बजे)"
        AppLanguage.MARATHI -> "१ दिवस आधी (संध्याकाळी ७:००)"
        AppLanguage.GUJARATI -> "૧ દિવસ પહેલાં (સાંજે ૭:૦૦)"
        AppLanguage.TAMIL -> "1 நாள் முன்பு (மாலை 7:00)"
        AppLanguage.TELUGU -> "1 రోజు ముందు (సాయంత్రం 7:00)"
        AppLanguage.KANNADA -> "1 ದಿನ ಮೊದಲು (ಸಂಜೆ 7:00)"
        AppLanguage.BENGALI -> "১ দিন আগে (সন্ধ্যা ৭:০০)"
        AppLanguage.ENGLISH -> "7:00 PM (Day Before)"
    }
    val festivalMorningReminderTitle: String get() = when (language) {
        AppLanguage.HINDI -> "पर्व की सुबह (प्रातः 6:30 बजे)"
        AppLanguage.MARATHI -> "सणाच्या सकाळी (सकाळी ६:३०)"
        AppLanguage.GUJARATI -> "તહેવારની સવારે (સવારે ૬:૩૦)"
        AppLanguage.TAMIL -> "பண்டிகை காலை (காலை 6:30)"
        AppLanguage.TELUGU -> "పండుగ ఉదయం (ఉదయం 6:30)"
        AppLanguage.KANNADA -> "ಹಬ್ಬದ ಬೆಳಿಗ್ಗೆ (ಬೆಳಿಗ್ಗೆ 6:30)"
        AppLanguage.BENGALI -> "উৎসবের সকালে (সকাল ৬:৩০)"
        AppLanguage.ENGLISH -> "6:30 AM (Festival Morning)"
    }
    val muhuratStartReminderTitle: String get() = when (language) {
        AppLanguage.HINDI -> "पूजा मुहूर्त से 30 मिनट पूर्व"
        AppLanguage.MARATHI -> "पूजा मुहूर्ताच्या ३० मिनिटे आधी"
        AppLanguage.GUJARATI -> "પૂજા મુહૂર્તથી ૩૦ મિનિટ પહેલાં"
        AppLanguage.TAMIL -> "பூஜை முகூர்த்தத்திற்கு 30 நிமிடம் முன்"
        AppLanguage.TELUGU -> "పూజా ముహూర్తానికి 30 నిమిషాల ముందు"
        AppLanguage.KANNADA -> "ಪೂಜಾ ಮುಹೂರ್ತಕ್ಕೆ 30 ನಿಮಿಷ ಮೊದಲು"
        AppLanguage.BENGALI -> "পূজা মুহূর্তের ৩০ মিনিট আগে"
        AppLanguage.ENGLISH -> "30 Mins Before Puja Muhurat"
    }
    val dayBeforeReminderDesc: String get() = when (language) {
        AppLanguage.HINDI -> "पूजा सामग्री की व्यवस्था के लिए स्मरण"
        AppLanguage.MARATHI -> "पूजा साहित्याची तयारी करण्यासाठी आठवण"
        AppLanguage.GUJARATI -> "પૂજા સામગ્રીની તૈયારી માટે સ્મરણ"
        AppLanguage.TAMIL -> "பூஜை பொருட்கள் தயார் செய்ய நினைவூட்டல்"
        AppLanguage.TELUGU -> "పూజా సామాగ్రి ఏర్పాట్ల కోసం రిమైండర్"
        AppLanguage.KANNADA -> "ಪೂಜಾ ಸಾಮಗ್ರಿ ಸಿದ್ಧತೆಗಾಗಿ ಜ್ಞಾಪನೆ"
        AppLanguage.BENGALI -> "পূজা সামগ্রী প্রস্তুতের রিমাইন্ডার"
        AppLanguage.ENGLISH -> "Reminder to arrange puja samagri"
    }
    val festivalMorningReminderDesc: String get() = when (language) {
        AppLanguage.HINDI -> "दिन का संकल्प एवं शुभ मुहूर्त सारांश"
        AppLanguage.MARATHI -> "दिवसाचा संकल्प आणि शुभ मुहूर्त सारांश"
        AppLanguage.GUJARATI -> "દિવસનો સંકલ્પ અને શુભ મુહૂર્ત સારાંશ"
        AppLanguage.TAMIL -> "அன்றைய சங்கல்பம் & முகூர்த்த சுருக்கம்"
        AppLanguage.TELUGU -> "ఈ రోజు సంకల్పం మరియు ముహూర్తం సారాంశం"
        AppLanguage.KANNADA -> "ದಿನದ ಸಂಕಲ್ಪ ಮತ್ತು ಮುಹೂರ್ತ ಸಾರಾಂಶ"
        AppLanguage.BENGALI -> "দিনের সংকল্প ও শুভ মুহূর্তের সারাংশ"
        AppLanguage.ENGLISH -> "Sankalpa & Muhurat summary for the day"
    }
    val muhuratStartReminderDesc: String get() = when (language) {
        AppLanguage.HINDI -> "शुभ मुहूर्त प्रारंभ होने से पूर्व सूचना"
        AppLanguage.MARATHI -> "शुभ मुहूर्त सुरू होण्यापूर्वी सूचना"
        AppLanguage.GUJARATI -> "શુભ મુહૂર્ત શરૂ થતાં પહેલાં સૂચના"
        AppLanguage.TAMIL -> "சுப முகூர்த்தம் தொடங்கும் முன் அறிவிப்பு"
        AppLanguage.TELUGU -> "శుభ ముహూర్తం ప్రారంభానికి ముందు హెచ్చరిక"
        AppLanguage.KANNADA -> "ಶುಭ ಮುಹೂರ್ತ ಆರಂಭವಾಗುವ ಮೊದಲು ಎಚ್ಚರಿಕೆ"
        AppLanguage.BENGALI -> "শুভ মুহূর্ত শুরুর আগে সতর্কতা"
        AppLanguage.ENGLISH -> "Alert before the auspicious window begins"
    }
    val closeButtonLabel: String get() = when (language) {
        AppLanguage.HINDI -> "बंद करें"
        AppLanguage.MARATHI -> "बंद करा"
        AppLanguage.GUJARATI -> "બંધ કરો"
        AppLanguage.TAMIL -> "மூடு"
        AppLanguage.TELUGU -> "మూసివేయి"
        AppLanguage.KANNADA -> "ಮುಚ್ಚಿ"
        AppLanguage.BENGALI -> "বন্ধ করুন"
        AppLanguage.ENGLISH -> "Close"
    }
    val resetChecklistTitle: String get() = when (language) {
        AppLanguage.HINDI -> "सामग्री चेकलिस्ट रीसेट करें?"
        AppLanguage.MARATHI -> "सामग्री चेकलिस्ट रीसेट करायची?"
        AppLanguage.GUJARATI -> "સામગ્રી ચેકલિસ્ટ રીસેટ કરવી?"
        AppLanguage.TAMIL -> "பூஜை பொருட்கள் பட்டியலை மீட்டமைக்கவா?"
        AppLanguage.TELUGU -> "సామగ్రి చెక్‌లిస్ట్‌ను రీసెట్ చేయాలా?"
        AppLanguage.KANNADA -> "ಸಾಮಗ್ರಿ ಪರಿಶೀಲನಾಪಟ್ಟಿಯನ್ನು ಮರುಹೊಂದಿಸಬೇಕೆ?"
        AppLanguage.BENGALI -> "সামগ্রী চেকলিস্ট রিসেট করবেন?"
        AppLanguage.ENGLISH -> "Reset Samagri Checklist?"
    }
    val resetChecklistMessage: String get() = when (language) {
        AppLanguage.HINDI -> "इससे इस पर्व की सभी चयनित तैयारी सामग्री हट जाएगी। आप इन्हें कभी भी पुनः चिह्नित कर सकते हैं।"
        AppLanguage.MARATHI -> "यामुळे या सणाची सर्व निवडलेली सामग्री रीसेट होईल. आपण ती कधीही पुन्हा निवडू शकता."
        AppLanguage.GUJARATI -> "આનાથી આ તહેવારની તમામ ચકાસાયેલ સામગ્રી હટી જશે. તમે તેને ગમે ત્યારે ફરીથી ચકાસી શકો છો."
        AppLanguage.TAMIL -> "இது இந்த பண்டிகைக்கான அனைத்து தேர்வு செய்யப்பட்ட பொருட்களையும் அழிக்கும். நீங்கள் மீண்டும் எப்போது வேண்டுமானாலும் குறிக்கலாம்."
        AppLanguage.TELUGU -> "ఇది ఈ పండుగ కోసం ఎంచుకున్న అన్ని సన్నాహక వస్తువులను తొలగిస్తుంది. మీరు ఎప్పుడైనా మళ్ళీ గుర్తించవచ్చు."
        AppLanguage.KANNADA -> "ಇದು ಈ ಹಬ್ಬಕ್ಕಾಗಿ ಪರಿಶೀಲಿಸಿದ ಎಲ್ಲಾ ವಸ್ತುಗಳನ್ನು ತೆರವುಗೊಳಿಸುತ್ತದೆ. ನೀವು ಯಾವುದೇ ಸಮಯದಲ್ಲಿ ಮತ್ತೆ ಗುರುತಿಸಬಹುದು."
        AppLanguage.BENGALI -> "এটি এই উৎসবের সমস্ত চিহ্নিত প্রস্তুতি সামগ্রী মুছে ফেলবে। আপনি যে কোনও সময় পুনরায় চিহ্নিত করতে পারেন।"
        AppLanguage.ENGLISH -> "This will clear all checked preparation items for this festival. You can check them off again anytime."
    }
    val cancelButtonLabel: String get() = when (language) {
        AppLanguage.HINDI -> "रद्द करें"
        AppLanguage.MARATHI -> "रद्द करा"
        AppLanguage.GUJARATI -> "રદ કરો"
        AppLanguage.TAMIL -> "ரத்து செய்"
        AppLanguage.TELUGU -> "రద్దు చేయి"
        AppLanguage.KANNADA -> "ರದ್ದುಮಾಡಿ"
        AppLanguage.BENGALI -> "বাতিল করুন"
        AppLanguage.ENGLISH -> "Cancel"
    }
    val finishLaterLabel: String get() = when (language) {
        AppLanguage.HINDI -> "बाद में पूरा करें"
        AppLanguage.MARATHI -> "नंतर पूर्ण करा"
        AppLanguage.GUJARATI -> "પછી પૂર્ણ કરો"
        AppLanguage.TAMIL -> "பின்னர் முடிக்கவும்"
        AppLanguage.TELUGU -> "తరువాత పూర్తి చేయండి"
        AppLanguage.KANNADA -> "ನಂತರ ಪೂರ್ಣಗೊಳಿಸಿ"
        AppLanguage.BENGALI -> "পরে সম্পন্ন করুন"
        AppLanguage.ENGLISH -> "Finish later"
    }
    val shubhSampannaTitle: String get() = when (language) {
        AppLanguage.HINDI -> "शुभ सम्पन्न"
        AppLanguage.MARATHI -> "शुभ संपन्न"
        AppLanguage.GUJARATI -> "શુભ સંપન્ન"
        AppLanguage.TAMIL -> "சுப சம்பன்னம்"
        AppLanguage.TELUGU -> "శుభ సంపన్నం"
        AppLanguage.KANNADA -> "ಶುಭ ಸಂಪನ್ನ"
        AppLanguage.BENGALI -> "শুভ সম্পন্ন"
        AppLanguage.ENGLISH -> "Shubh Sampanna"
    }
    val completedStepsSummaryHeader: String get() = when (language) {
        AppLanguage.HINDI -> "पूर्ण पूजा चरण सारांश"
        AppLanguage.MARATHI -> "पूर्ण झालेल्या पायऱ्यांचा सारांश"
        AppLanguage.GUJARATI -> "પૂર્ણ થયેલ પૂજા પગલાંઓનો સારાંશ"
        AppLanguage.TAMIL -> "நிறைவுற்ற படிகளின் சுருக்கம்"
        AppLanguage.TELUGU -> "పూర్తయిన దశల సారాంశం"
        AppLanguage.KANNADA -> "ಪೂರ್ಣಗೊಂಡ ಹಂತಗಳ ಸಾರಾಂಶ"
        AppLanguage.BENGALI -> "সম্পন্ন পূজা ধাপের সারাংশ"
        AppLanguage.ENGLISH -> "COMPLETED STEPS SUMMARY"
    }
    val shareFestivalBlessingButton: String get() = when (language) {
        AppLanguage.HINDI -> "पूजा आशीर्वाद साझा करें"
        AppLanguage.MARATHI -> "पूजा आशीर्वाद शेअर करा"
        AppLanguage.GUJARATI -> "પૂજા આશીર્વાદ શેર કરો"
        AppLanguage.TAMIL -> "பூஜை ஆசீர்வாதத்தை பகிரவும்"
        AppLanguage.TELUGU -> "పూజా ఆశీర్వాదం పంచుకోండి"
        AppLanguage.KANNADA -> "ಪೂಜಾ ಆಶೀರ್ವಾದ ಹಂಚಿಕೊಳ್ಳಿ"
        AppLanguage.BENGALI -> "পূজার আশীর্বাদ শেয়ার করুন"
        AppLanguage.ENGLISH -> "Share Festival Blessing"
    }
    val returnToDetailsButton: String get() = when (language) {
        AppLanguage.HINDI -> "पर्व विवरण पर लौटें"
        AppLanguage.MARATHI -> "सण तपशीलावर परत जा"
        AppLanguage.GUJARATI -> "તહેવાર વિગતો પર પાછા જાઓ"
        AppLanguage.TAMIL -> "பண்டிகை விவரங்களுக்கு திரும்பவும்"
        AppLanguage.TELUGU -> "పండుగ వివరాలకు తిరిగి వెళ్లండి"
        AppLanguage.KANNADA -> "ಹಬ್ಬದ ವಿವರಗಳಿಗೆ ಹಿಂತಿರುಗಿ"
        AppLanguage.BENGALI -> "উৎসবের বিবরণে ফিরে যান"
        AppLanguage.ENGLISH -> "Return to Festival Details"
    }
    val completePujaButton: String get() = when (language) {
        AppLanguage.HINDI -> "पूजा सम्पन्न करें"
        AppLanguage.MARATHI -> "पूजा संपन्न करा"
        AppLanguage.GUJARATI -> "પૂજા સંપન્ન કરો"
        AppLanguage.TAMIL -> "பூஜை நிறைவு"
        AppLanguage.TELUGU -> "పూజ పూర్తి చేయండి"
        AppLanguage.KANNADA -> "ಪೂಜೆ ಪೂರ್ಣಗೊಳಿಸಿ"
        AppLanguage.BENGALI -> "পূজা সম্পন্ন করুন"
        AppLanguage.ENGLISH -> "Complete Puja"
    }
    val festivalNotFoundOffline: String get() = when (language) {
        AppLanguage.HINDI -> "क्या यह पर्व अभी ऑफ़लाइन सूची में नहीं है?"
        AppLanguage.MARATHI -> "हा सण अजून ऑफलाइन सूचीत नाही का?"
        AppLanguage.GUJARATI -> "શું આ તહેવાર હજી ઑફલાઇન યાદીમાં નથી?"
        AppLanguage.TAMIL -> "இந்த பண்டிகை இன்னும் ஆஃப்லைன் பட்டியலில் இல்லையா?"
        AppLanguage.TELUGU -> "ఈ పండుగ ఇంకా ఆఫ్‌లైన్ జాబితాలో లేదా?"
        AppLanguage.KANNADA -> "ಈ ಹಬ್ಬವು ಇನ್ನೂ ಆಫ್‌ಲೈನ್ ಪಟ್ಟಿಯಲ್ಲಿಲ್ಲವೇ?"
        AppLanguage.BENGALI -> "এই উৎসবটি কি এখনও অফলাইন তালিকায় নেই?"
        AppLanguage.ENGLISH -> "Festival not in offline catalog yet?"
    }
    val browseAllFestivals: String get() = when (language) {
        AppLanguage.HINDI -> "सभी पर्व देखें"
        AppLanguage.MARATHI -> "सर्व सण पहा"
        AppLanguage.GUJARATI -> "બધા તહેવારો જુઓ"
        AppLanguage.TAMIL -> "அனைத்து பண்டிகைகளையும் காண்க"
        AppLanguage.TELUGU -> "అన్ని పండుగలను చూడండి"
        AppLanguage.KANNADA -> "ಎಲ್ಲಾ ಹಬ್ಬಗಳನ್ನು ವೀಕ್ಷಿಸಿ"
        AppLanguage.BENGALI -> "সমস্ত উৎসব দেখুন"
        AppLanguage.ENGLISH -> "Browse All Festivals"
    }
    val noFestivalsFound: String get() = when (language) {
        AppLanguage.HINDI -> "चयनित फ़िल्टर के अनुसार कोई पर्व नहीं मिला।"
        AppLanguage.MARATHI -> "निवडलेल्या फिल्टरनुसार कोणताही सण आढळला नाही."
        AppLanguage.GUJARATI -> "પસંદ કરેલા ફિલ્ટર્સ માટે કોઈ તહેવાર મળ્યો નથી."
        AppLanguage.TAMIL -> "தேர்ந்தெடுக்கப்பட்ட வடிகட்டிகளுக்கு எந்த பண்டிகையும் பொருந்தவில்லை."
        AppLanguage.TELUGU -> "ఎంచుకున్న ఫిల్టర్‌లకు పండుగలు ఏవీ సరిపోలలేదు."
        AppLanguage.KANNADA -> "ಆಯ್ಕೆಮಾಡಿದ ಫಿಲ್ಟರ್‌ಗಳಿಗೆ ಯಾವುದೇ ಹಬ್ಬಗಳು ಹೊಂದಿಕೆಯಾಗುತ್ತಿಲ್ಲ."
        AppLanguage.BENGALI -> "নির্বাচিত ফিল্টারের সাথে কোনও উৎসব মেলেনি।"
        AppLanguage.ENGLISH -> "No festivals match the selected filters."
    }
    val noSavedFestivals: String get() = when (language) {
        AppLanguage.HINDI -> "आपने अभी तक कोई पर्व सहेजा नहीं है।"
        AppLanguage.MARATHI -> "तुम्ही अजून कोणताही सण सेव्ह केलेला नाही."
        AppLanguage.GUJARATI -> "તમે હજી સુધી કોઈ તહેવાર સાચવ્યો નથી."
        AppLanguage.TAMIL -> "நீங்கள் இன்னும் எந்த பண்டிகையையும் சேமிக்கவில்லை."
        AppLanguage.TELUGU -> "మీరు ఇంకా ఏ పండుగలను సేవ్ చేయలేదు."
        AppLanguage.KANNADA -> "ನೀವು ಇನ್ನೂ ಯಾವುದೇ ಹಬ್ಬಗಳನ್ನು ಉಳಿಸಿಲ್ಲ."
        AppLanguage.BENGALI -> "আপনি এখনও কোনও উৎসব সংরক্ষণ করেননি।"
        AppLanguage.ENGLISH -> "You haven't saved any festivals yet."
    }
    val noFestivalsThisMonth: String get() = when (language) {
        AppLanguage.HINDI -> "सक्रिय फ़िल्टर के लिए इस माह कोई प्रमुख पर्व नहीं है।"
        AppLanguage.MARATHI -> "सक्रिय फिल्टरसाठी या महिन्यात कोणताही प्रमुख सण नाही."
        AppLanguage.GUJARATI -> "સક્રિય ફિલ્ટર માટે આ મહિને કોઈ મુખ્ય તહેવાર નથી."
        AppLanguage.TAMIL -> "இந்த மாதத்தில் செயலில் உள்ள வடிகட்டிக்கு முக்கிய பண்டிகைகள் இல்லை."
        AppLanguage.TELUGU -> "సక్రియ ఫిల్టర్ కోసం ఈ నెలలో ప్రధాన పండుగలు లేవు."
        AppLanguage.KANNADA -> "ಸಕ್ರಿಯ ಫಿಲ್ಟರ್‌ಗೆ ಈ ತಿಂಗಳಲ್ಲಿ ಯಾವುದೇ ಪ್ರಮುಖ ಹಬ್ಬಗಳಿಲ್ಲ."
        AppLanguage.BENGALI -> "সক্রিয় ফিল্টারের জন্য এই মাসে কোনও প্রধান উৎসব নেই।"
        AppLanguage.ENGLISH -> "No major festivals in this month for the active filter."
    }
    val lookingForAnotherFestival: String get() = when (language) {
        AppLanguage.HINDI -> "क्या अन्य क्षेत्रीय व्रत या पर्व खोज रहे हैं?"
        AppLanguage.MARATHI -> "दुसरा प्रादेशिक सण किंवा व्रत शोधत आहात?"
        AppLanguage.GUJARATI -> "શું અન્ય પ્રાદેશિક વ્રત કે તહેવાર શોધી રહ્યાં છો?"
        AppLanguage.TAMIL -> "மற்றொரு பிராந்திய விரதம் அல்லது பண்டிகையைத் தேடுகிறீர்களா?"
        AppLanguage.TELUGU -> "మరొక ప్రాంతీయ వ్రతం లేదా పండుగ కోసం చూస్తున్నారా?"
        AppLanguage.KANNADA -> "ಮತ್ತೊಂದು ಪ್ರಾದೇಶಿಕ ವ್ರತ ಅಥವಾ ಹಬ್ಬವನ್ನು ಹುಡುಕುತ್ತಿರುವಿರಾ?"
        AppLanguage.BENGALI -> "অন্য কোনও আঞ্চলিক ব্রত বা উৎসব খুঁজছেন?"
        AppLanguage.ENGLISH -> "Looking for another regional vrat or festival?"
    }
    val updateCatalogButton: String get() = when (language) {
        AppLanguage.HINDI -> "ऐप अपडेट करें"
        AppLanguage.MARATHI -> "अॅप अपडेट करा"
        AppLanguage.GUJARATI -> "ઍપ અપડેટ કરો"
        AppLanguage.TAMIL -> "செயலியை புதுப்பிக்கவும்"
        AppLanguage.TELUGU -> "యాప్ అప్‌డేట్ చేయండి"
        AppLanguage.KANNADA -> "ಆ್ಯಪ್ ಅಪ್‌ಡೇಟ್ ಮಾಡಿ"
        AppLanguage.BENGALI -> "অ্যাপ আপডেট করুন"
        AppLanguage.ENGLISH -> "Update App"
    }
    val resetFilterButton: String get() = when (language) {
        AppLanguage.HINDI -> "फ़िल्टर रीसेट करें"
        AppLanguage.MARATHI -> "फिल्टर रीसेट करा"
        AppLanguage.GUJARATI -> "ફિલ્ટર રીસેટ કરો"
        AppLanguage.TAMIL -> "வடிகட்டியை மீட்டமை"
        AppLanguage.TELUGU -> "ఫిల్టర్‌ను రీసెట్ చేయండి"
        AppLanguage.KANNADA -> "ಫಿಲ್ಟರ್ ಮರುಹೊಂದಿಸಿ"
        AppLanguage.BENGALI -> "ফিল্টার রিসেಟ್ করুন"
        AppLanguage.ENGLISH -> "Reset Filter"
    }
    val playMantraLabel: String get() = when (language) {
        AppLanguage.HINDI -> "मंत्र पाठ सुनें"
        AppLanguage.MARATHI -> "मंत्र पठण ऐका"
        AppLanguage.GUJARATI -> "મંત્ર પાઠ સાંભળો"
        AppLanguage.TAMIL -> "மந்திரம் கேட்கவும்"
        AppLanguage.TELUGU -> "మంత్ర పఠనం వినండి"
        AppLanguage.KANNADA -> "ಮಂತ್ರ ಪಠಣ ಕೇಳಿ"
        AppLanguage.BENGALI -> "মন্ত্র পাঠ শুনুন"
        AppLanguage.ENGLISH -> "Recite & Listen"
    }
    val pauseMantraLabel: String get() = when (language) {
        AppLanguage.HINDI -> "विराम"
        AppLanguage.MARATHI -> "विराम"
        AppLanguage.GUJARATI -> "વિરામ"
        AppLanguage.TAMIL -> "இடைநிறுத்து"
        AppLanguage.TELUGU -> "విరామం"
        AppLanguage.KANNADA -> "ವಿರಾಮ"
        AppLanguage.BENGALI -> "বিরতি"
        AppLanguage.ENGLISH -> "Pause"
    }
    val stopMantraLabel: String get() = when (language) {
        AppLanguage.HINDI -> "रोकें"
        AppLanguage.MARATHI -> "थांबवा"
        AppLanguage.GUJARATI -> "રોકો"
        AppLanguage.TAMIL -> "நிறுத்து"
        AppLanguage.TELUGU -> "ఆపండి"
        AppLanguage.KANNADA -> "ನಿಲ್ಲಿಸಿ"
        AppLanguage.BENGALI -> "থামান"
        AppLanguage.ENGLISH -> "Stop"
    }
    val chantingSpeedLabel: String get() = when (language) {
        AppLanguage.HINDI -> "गति"
        AppLanguage.MARATHI -> "गती"
        AppLanguage.GUJARATI -> "ગતિ"
        AppLanguage.TAMIL -> "வேகம்"
        AppLanguage.TELUGU -> "వేగం"
        AppLanguage.KANNADA -> "ವೇಗ"
        AppLanguage.BENGALI -> "গতি"
        AppLanguage.ENGLISH -> "Speed"
    }
    val tanpuraAmbienceLabel: String get() = when (language) {
        AppLanguage.HINDI -> "तानपूरा नाद"
        AppLanguage.MARATHI -> "तानपुरा नाद"
        AppLanguage.GUJARATI -> "તાનપૂરો નાદ"
        AppLanguage.TAMIL -> "தம்பூரா"
        AppLanguage.TELUGU -> "తంబురా"
        AppLanguage.KANNADA -> "ತಂಬೂರಿ"
        AppLanguage.BENGALI -> "তানপুরা"
        AppLanguage.ENGLISH -> "Tanpura Drone"
    }
    val panditVoiceLabel: String get() = when (language) {
        AppLanguage.HINDI -> "मंत्र पाठ स्वर"
        AppLanguage.MARATHI -> "मंत्र पठण स्वर"
        AppLanguage.GUJARATI -> "મંત્ર પાઠ સ્વર"
        AppLanguage.TAMIL -> "மந்திர உச்சரிப்பு குரல்"
        AppLanguage.TELUGU -> "మంత్ర పఠనం స్వరం"
        AppLanguage.KANNADA -> "ಮಂತ್ರ ಪಠಣ ಸ್ವರ"
        AppLanguage.BENGALI -> "মন্ত্র পাঠ স্বর"
        AppLanguage.ENGLISH -> "Mantra Recitation Voice"
    }
    val malePanditTitle: String get() = when (language) {
        AppLanguage.HINDI -> "पुरुष पुरोहित"
        AppLanguage.MARATHI -> "पुरुष पुरोहित"
        AppLanguage.GUJARATI -> "પુરુષ પુરોહિત"
        AppLanguage.TAMIL -> "ஆண் பூசாரி"
        AppLanguage.TELUGU -> "పురుష పూజారి"
        AppLanguage.KANNADA -> "ಪುರುಷ ಪುರೋಹಿತ"
        AppLanguage.BENGALI -> "পুরুষ পুরোহিত"
        AppLanguage.ENGLISH -> "Male Pandit"
    }
    val malePanditSub: String get() = when (language) {
        AppLanguage.HINDI -> "गंभीर, वैदिक मंत्रोच्चार"
        AppLanguage.MARATHI -> "गंभीर, वैदिक मंत्रोच्चार"
        AppLanguage.GUJARATI -> "ગંભીર, વૈદિક મંત્રોચ્ચાર"
        AppLanguage.TAMIL -> "கம்பீரமான வேத பாராயணம்"
        AppLanguage.TELUGU -> "గంభీరమైన వేద మంత్రోచ్ఛారణ"
        AppLanguage.KANNADA -> "ಗಂಭೀರ ವೈದಿಕ ಮಂತ್ರೋಚ್ಛಾರಣೆ"
        AppLanguage.BENGALI -> "গম্ভীর বৈদিক মন্ত্রোচ্চারণ"
        AppLanguage.ENGLISH -> "Deep Vedic Chanting"
    }
    val femalePanditTitle: String get() = when (language) {
        AppLanguage.HINDI -> "महिला पुरोहित (विदुषी)"
        AppLanguage.MARATHI -> "महिला पुरोहित (विदुषी)"
        AppLanguage.GUJARATI -> "મહિલા પુરોહિત (વિદુષી)"
        AppLanguage.TAMIL -> "பெண் பூசாரி"
        AppLanguage.TELUGU -> "మహిళా పూజారి"
        AppLanguage.KANNADA -> "ಮಹಿಳಾ ಪುರೋಹಿತ"
        AppLanguage.BENGALI -> "মহিলা পুরোহিত (বিদুষী)"
        AppLanguage.ENGLISH -> "Female Pandit"
    }
    val femalePanditSub: String get() = when (language) {
        AppLanguage.HINDI -> "मधुर, शांत भक्तिमय स्वर"
        AppLanguage.MARATHI -> "मधुर, शांत भक्तिमय स्वर"
        AppLanguage.GUJARATI -> "મધુર, શાંત ભક્તિમય સ્વર"
        AppLanguage.TAMIL -> "இனிய பக்தி குரல்"
        AppLanguage.TELUGU -> "మధురమైన భక్తి స్వరం"
        AppLanguage.KANNADA -> "ಮಧುರ ಭಕ್ತಿ ಸ್ವರ"
        AppLanguage.BENGALI -> "মধুর শান্ত ভক্তিময় স্বর"
        AppLanguage.ENGLISH -> "Melodious Devotional Tone"
    }
    val roundLabel: String get() = when (language) {
        AppLanguage.HINDI -> "आवृत्ति"
        AppLanguage.MARATHI -> "फेरी"
        AppLanguage.GUJARATI -> "આવૃત્તિ"
        AppLanguage.TAMIL -> "சுற்று"
        AppLanguage.TELUGU -> "ఆవృత్తి"
        AppLanguage.KANNADA -> "ಸುತ್ತು"
        AppLanguage.BENGALI -> "আবৃত্তি"
        AppLanguage.ENGLISH -> "Round"
    }
    val japaCompletedBadge: String get() = when (language) {
        AppLanguage.HINDI -> "जाप सम्पूर्ण"
        AppLanguage.MARATHI -> "जप संपन्न"
        AppLanguage.GUJARATI -> "જપ સંપૂર્ણ"
        AppLanguage.TAMIL -> "ஜெபம் நிறைவு"
        AppLanguage.TELUGU -> "జపం పూర్తయింది"
        AppLanguage.KANNADA -> "ಜಪ ಪೂರ್ಣಗೊಂಡಿದೆ"
        AppLanguage.BENGALI -> "জপ সম্পূর্ণ"
        AppLanguage.ENGLISH -> "Japa Completed"
    }
}

fun stringsForLanguage(language: AppLanguage): UtsavStrings {
    return when (language) {
        AppLanguage.ENGLISH -> UtsavStrings(
            language = language,
            tabToday = "Today",
            tabCalendar = "Calendar",
            tabFestivals = "Festivals",
            tabPanchang = "Panchang",
            tabProfile = "Profile",
            nextMajorFestival = "NEXT MAJOR FESTIVAL",
            todaysPanchang = "Today's Panchang",
            auspiciousWindow = "Auspicious Window",
            rahuKaalAvoid = "Rahu Kaal (Avoid)",
            comingUpInYourTradition = "Coming Up in Your Tradition",
            prepareForNextFestival = "Prepare for Your Next Festival",
            startPujaGuide = "Start Puja Guide",
            shareGreeting = "Share",
            setReminder = "Set Reminder",
            dailyPanchangHeader = "Daily Panchang",
            sunriseLabel = "Sunrise",
            sunsetLabel = "Sunset",
            tithiLabel = "Tithi",
            nakshatraLabel = "Nakshatra",
            yogaLabel = "Yoga",
            karanaLabel = "Karana",
            settingsTitle = "Profile & Preferences"
        )
        AppLanguage.HINDI -> UtsavStrings(
            language = language,
            tabToday = "आज",
            tabCalendar = "कैलेंडर",
            tabFestivals = "त्योहार",
            tabPanchang = "पंचांग",
            tabProfile = "प्रोफ़ाइल",
            nextMajorFestival = "अगला प्रमुख पर्व",
            todaysPanchang = "आज का पंचांग",
            auspiciousWindow = "शुभ मुहूर्त",
            rahuKaalAvoid = "राहु काल (वर्जित)",
            comingUpInYourTradition = "आपकी परंपरा में आगामी पर्व",
            prepareForNextFestival = "अगले पर्व की पूजा तैयारी",
            startPujaGuide = "पूजा विधि प्रारंभ करें",
            shareGreeting = "साझा करें",
            setReminder = "रिमाइंडर लगाएं",
            dailyPanchangHeader = "दैनिक पंचांग",
            sunriseLabel = "सूर्योदय",
            sunsetLabel = "सूर्यास्त",
            tithiLabel = "तिथि",
            nakshatraLabel = "नक्षत्र",
            yogaLabel = "योग",
            karanaLabel = "करण",
            settingsTitle = "प्रोफ़ाइल और प्राथमिकताएं",
            stepTitles = listOf("स्वागत", "भाषा", "आपका शहर", "परंपरा", "रिमाइंडर"),
            exploreFirstCta = "सीधे ऐप देखें →",
            heroTagline = "आपके पर्व, श्रद्धा और सटीकता के साथ।",
            heroSubtagline = "आपके शहर का सटीक शुभ मुहूर्त, प्रामाणिक पूजा विधि और समय पर रिमाइंडर।",
            welcomeTitle = "उत्सवसेतु (UtsavSetu) में आपका स्वागत है",
            welcomeBody = "भारत के हर शहर में रहने वाले परिवारों के लिए, ताकि आपकी परंपरा, तिथि और पूजा का शुभ मुहूर्त हमेशा सटीक रहे।",
            welcomePillFestivals = "82+ प्रमुख त्योहार",
            welcomePillPanchang = "शहर का पंचांग",
            welcomePillVidhi = "पूजा विधि व सामग्री",
            welcomeOfflineNote = "100% ऑफ़लाइन कार्य करता है। अपनी भाषा, शहर और परंपरा चुनें—या ऊपर 'सीधे ऐप देखें' दबाएं।",
            stepLanguageTitle = "1. अपनी भाषा चुनें",
            stepLanguageSubtitle = "भाषा चुनते ही पूरे ऐप की भाषा तुरंत बदल जाएगी और आगे के सभी स्क्रीन आपकी भाषा में दिखेंगे।",
            stepCityTitle = "2. अपना शहर चुनें (स्थानीय पंचांग)",
            stepCitySubtitle = "सूर्योदय, सूर्यास्त, राहु काल और पूजा मुहूर्त आपके शहर के सटीक स्थान पर निर्भर करते हैं।",
            useCurrentLocationGps = "मेरी वर्तमान लोकेशन (GPS) से शहर चुनें",
            detectingLocationLabel = "GPS से आपका शहर खोजा जा रहा है…",
            topCitiesHeader = "प्रमुख महानगर",
            moreCitiesHeader = "अन्य भारतीय शहर और तीर्थ नगरी",
            citySearchPlaceholder = "शहर खोजें (जैसे दिल्ली, मुंबई, वाराणसी, जयपुर)…",
            selectedCityPrefix = "चयनित शहर",
            changeButton = "बदलें",
            stepTraditionTitle = "3. क्षेत्रीय परंपरा और पंचांग पद्धति",
            stepTraditionSubtitle = "अपने परिवार की परंपरा चुनें। उत्तर भारत में पूर्णिमांत और पश्चिम/दक्षिण/पूर्व भारत में अमांत पंचांग प्रचलित है।",
            stepRemindersTitle = "4. पर्व और मुहूर्त रिमाइंडर",
            stepRemindersSubtitle = "चुनें कि उत्सवसेतु आपको आगामी त्योहारों से पहले कब सूचित करे।",
            backButton = "पीछे",
            nextPrefix = "आगे",
            getStartedEnter = "प्रारंभ करें · उत्सवसेतु में प्रवेश करें",
            saveDefaultsEnter = "सेव करें और उत्सवसेतु शुरू करें",
            alsoKnownAsLabel = "अन्य क्षेत्रीय नाम",
            searchOnlineButton = "ऑनलाइन खोजें और ऐप में जोड़ें",
            searchingOnlineLabel = "ऑनलाइन पंचांग और विश्वकोश में खोजा जा रहा है…",
            quickActionsHeader = "त्वरित सुविधाएं",
            upcomingFestivalsHeader = "आगामी पर्व और त्योहार",
            upcomingFestivalsSubtitlePrefix = "सत्यापित तिथि व शुभ मुहूर्त:",
            seeAllButton = "सभी देखें",
            exploreVidhiButton = "पूजा विधि देखें",
            todayBadge = "आज",
            tomorrowBadge = "कल",
            inDaysFormat = "%d दिन बाद",
            monthViewLabel = "मासिक कैलेंडर",
            agendaViewLabel = "सूची दृश्य",
            myTraditionsAllLabel = "सभी परंपराएं",
            allRegionsLabel = "सभी क्षेत्र",
            browseByCategoryHeader = "श्रेणी के अनुसार खोजें",
            featuredCelebrationHeader = "प्रमुख आगामी पर्व",
            searchFestivalsPlaceholder = "कोई भी पर्व खोजें (भाई दूज, गोवर्धन, दिवाली, छठ, पोंगल)…",
            panchangSubtitle = "दैनिक ज्योतिषीय गणना एवं शुभ मुहूर्त",
            panchangDetailsHeader = "पंचांग विवरण (सारणी)",
            auspiciousTimingsHeader = "शुभ मुहूर्त (चौघड़िया व काल)",
            inauspiciousTimingsHeader = "अशुभ समय (राहु काल - वर्जित)",
            importantTimingsHeader = "प्रमुख समय, तिथि एवं शुभ मुहूर्त (सारणी)",
            verifiedMeeusBadge = "सत्यापित · मीउस पंचांग",
            calculatedEstimateBadge = "गणना आधारित समय",
            timingParameterColHeader = "पंचांग / मुहूर्त विवरण",
            timingWindowColHeader = "शुभ समय एवं अवधि (IST)",
            festivalDateRowLabel = "पर्व की दिनांक",
            primaryMuhuratRowLabel = "मुख्य पूजा शुभ मुहूर्त",
            additionalMuhuratRowLabel = "अन्य मुहूर्त विवरण",
            tithiAndPakshaRowLabel = "तिथि एवं पक्ष",
            tithiSpanRowLabel = "तिथि प्रारंभ एवं समाप्ति",
            nakshatraRowLabel = "नक्षत्र एवं देवता",
            sunriseSunsetRowLabel = "सूर्योदय एवं सूर्यास्त",
            auspiciousKaalRowLabel = "ब्रह्म, अभिजित व गोधूलि मुहूर्त",
            avoidKaalRowLabel = "राहु काल व यमगंडम (वर्जित)",
            lunarMonthRowLabel = "हिन्दू चंद्र मास",
            shastraRuleRowLabel = "शास्त्रोक्त निर्णय नियम",
            twentyYearExplorerLabel = "20-वर्षीय पंचांग एवं मुहूर्त दर्शक (2025–2046)",
            whyCelebratedHeader = "पर्व का महत्व एवं पौराणिक व्रत कथा",
            puranicLegendHeader = "पौराणिक कथा एवं इतिहास",
            presidingDeityHeader = "आराध्य देवी / देवता",
            primaryMoolaMantraHeader = "प्रधान मूल मंत्र (पूजा में जप हेतु)",
            vratAndNaivedyaHeader = "व्रत नियम एवं नैवेद्य (भोग) मार्गदर्शिका",
            regionalNamesHeaderSuffix = "भारत के विभिन्न राज्यों में नाम",
            vidhiTabPrefix = "पूजा विधि",
            samagriTabPrefix = "सामग्री",
            mantrasTabPrefix = "पूजा मंत्र",
            guidedModeButton = "गाइडेड पूजा मोड",
            essentialItemsHeader = "अनिवार्य पूजा सामग्री",
            optionalItemsHeader = "वैकल्पिक एवं क्षेत्रीय सामग्री",
            familyNotesHeader = "पारिवारिक परंपरा एवं निजी नोट्स",
            japaCounterHeader = "डिजिटल जप माला काउंटर",
            meaningPrefix = "भावार्थ",
            languageSectionHeader = "ऐप की भाषा (LANGUAGE)",
            locationSectionHeader = "आपका शहर और स्थान (LOCATION)",
            traditionsSectionHeader = "पंचांग एवं क्षेत्रीय परंपराएं",
            remindersSectionHeader = "पर्व एवं मुहूर्त रिमाइंडर",
            appPreferencesSectionHeader = "ऐप दिखावट और अक्षर आकार (FONT SIZE)",
            dataPrivacySectionHeader = "डेटा और गोपनीयता",
            aboutSectionHeader = "उत्सवसेतु के बारे में",
            appearanceThemeLabel = "ऐप थीम (Light / Dark)",
            textSizeLabel = "अक्षर का आकार (Text Size)",
            standardTextLabel = "सामान्य (Standard)",
            largeTextLabel = "बड़ा और स्पष्ट (Large)"
        )
        AppLanguage.MARATHI -> UtsavStrings(
            language = language,
            tabToday = "आज",
            tabCalendar = "दिनदर्शिका",
            tabFestivals = "सण-उत्सव",
            tabPanchang = "पंचांग",
            tabProfile = "प्रोफाईल",
            nextMajorFestival = "पुढील प्रमुख सण",
            todaysPanchang = "आजचे पंचांग",
            auspiciousWindow = "शुभ मुहूर्त",
            rahuKaalAvoid = "राहू काळ (वर्ज्य)",
            comingUpInYourTradition = "तुमच्या परंपरेतील आगामी सण",
            prepareForNextFestival = "पुढील सणाची पूजा तयारी",
            startPujaGuide = "पूजा विधी सुरू करा",
            shareGreeting = "शेअर करा",
            setReminder = "आठवण लावा",
            dailyPanchangHeader = "दैनिक पंचांग",
            sunriseLabel = "सूर्योदय",
            sunsetLabel = "सूर्यास्त",
            tithiLabel = "तिथी",
            nakshatraLabel = "नक्षत्र",
            yogaLabel = "योग",
            karanaLabel = "करण",
            settingsTitle = "प्रोफाईल आणि पसंती",
            stepTitles = listOf("स्वागत", "भाषा", "तुमचे शहर", "परंपरा", "आठवण"),
            exploreFirstCta = "आधी ॲप पहा →",
            heroTagline = "तुमचे सण आणि परंपरा, आपुलकीने जोडलेले.",
            heroSubtagline = "तुमच्या शहराचे अचूक मुहूर्त, शास्त्रोक्त पूजा विधी आणि वेळेवर आठवण.",
            welcomeTitle = "उत्सवसेतू (UtsavSetu) मध्ये आपले स्वागत आहे",
            welcomeBody = "भारतातील सर्व शहरांमधील कुटुंबांसाठी—जेणेकरून तुमच्या घरची परंपरा, तिथी आणि पूजा मुहूर्त नेहमी अचूक राहतील.",
            welcomePillFestivals = "८२+ प्रमुख सण",
            welcomePillPanchang = "शहर पंचांग",
            welcomePillVidhi = "विधी आणि सामग्री",
            welcomeOfflineNote = "१००% ऑफलाईन चालते. तुमची भाषा, शहर आणि परंपरा निवडा.",
            stepLanguageTitle = "१. तुमची भाषा निवडा",
            stepLanguageSubtitle = "भाषा निवडताच संपूर्ण ॲपची भाषा लगेच बदलेल.",
            stepCityTitle = "२. तुमचे शहर निवडा (स्थानिक पंचांग)",
            stepCitySubtitle = "सूर्योदय, सूर्यास्त, राहू काळ आणि पूजा मुहूर्त तुमच्या शहरावर अवलंबून असतात.",
            useCurrentLocationGps = "सध्याचे लोकेशन (GPS) वापरा",
            detectingLocationLabel = "GPS द्वारे शहर शोधत आहे…",
            topCitiesHeader = "प्रमुख शहरे",
            moreCitiesHeader = "इतर शहरे आणि तीर्थक्षेत्रे",
            citySearchPlaceholder = "शहर शोधा (उदा. मुंबई, पुणे, नाशिक, नागपूर)…",
            selectedCityPrefix = "निवडलेले शहर",
            changeButton = "बदला",
            stepTraditionTitle = "३. प्रादेशिक परंपरा आणि पंचांग पद्धती",
            stepTraditionSubtitle = "तुमच्या कुटुंबाची परंपरा निवडा (अमांत किंवा पौर्णिमांत पंचांग).",
            stepRemindersTitle = "४. सण आणि मुहूर्त आठवण (Reminders)",
            stepRemindersSubtitle = "सणांपूर्वी तुम्हाला कधी आठवण करून द्यायची ते निवडा.",
            backButton = "मागे",
            nextPrefix = "पुढे",
            getStartedEnter = "सुरू करा · उत्सवसेतूमध्ये प्रवेश करा",
            saveDefaultsEnter = "जतन करा आणि उत्सवसेतू सुरू करा",
            alsoKnownAsLabel = "इतर प्रादेशिक नावे",
            searchOnlineButton = "ऑनलाईन शोधा आणि ॲपमध्ये जोडा",
            searchingOnlineLabel = "ऑनलाईन माहिती शोधत आहे…",
            quickActionsHeader = "जलद सुविधा",
            upcomingFestivalsHeader = "आगामी सण आणि उत्सव",
            upcomingFestivalsSubtitlePrefix = "अचूक तिथी व शुभ मुहूर्त:",
            seeAllButton = "सर्व पहा",
            exploreVidhiButton = "पूजा विधी पहा",
            todayBadge = "आज",
            tomorrowBadge = "उद्या",
            inDaysFormat = "%d दिवसांत",
            monthViewLabel = "मासिक दिनदर्शिका",
            agendaViewLabel = "यादी दृश्य",
            myTraditionsAllLabel = "सर्व परंपरा",
            allRegionsLabel = "सर्व विभाग",
            browseByCategoryHeader = "प्रकारानुसार शोधा",
            featuredCelebrationHeader = "प्रमुख आगामी सण",
            searchFestivalsPlaceholder = "कोणताही सण शोधा (भाऊबीज, गोवर्धन, दिवाळी, गणेश चतुर्थी)…",
            panchangSubtitle = "दैनिक खगोलशास्त्रीय व धार्मिक मुहूर्त",
            panchangDetailsHeader = "पंचांग तपशील (तक्ता)",
            auspiciousTimingsHeader = "शुभ मुहूर्त",
            inauspiciousTimingsHeader = "अशुभ वेळ (राहू काळ - वर्ज्य)",
            importantTimingsHeader = "महत्त्वाच्या वेळा, तिथी आणि शुभ मुहूर्त (तक्ता)",
            verifiedMeeusBadge = "प्रमाणित · पंचांग गणना",
            calculatedEstimateBadge = "गणना आधारित वेळ",
            timingParameterColHeader = "पंचांग / मुहूर्त घटक",
            timingWindowColHeader = "शुभ वेळ आणि तपशील (IST)",
            festivalDateRowLabel = "सणाची तारीख",
            primaryMuhuratRowLabel = "मुख्य पूजा शुभ मुहूर्त",
            additionalMuhuratRowLabel = "इतर मुहूर्त तपशील",
            tithiAndPakshaRowLabel = "तिथी आणि पक्ष",
            tithiSpanRowLabel = "तिथी प्रारंभ आणि समाप्ती",
            nakshatraRowLabel = "नक्षत्र आणि देवता",
            sunriseSunsetRowLabel = "सूर्योदय आणि सूर्यास्त",
            auspiciousKaalRowLabel = "ब्रह्म, अभिजित आणि गोधूलि मुहूर्त",
            avoidKaalRowLabel = "राहू काळ आणि यमगंडम (वर्ज्य)",
            lunarMonthRowLabel = "हिंदू चांद्रमास",
            shastraRuleRowLabel = "शास्त्रोक्त निर्णय नियम",
            twentyYearExplorerLabel = "२०-वर्षांचे पंचांग आणि मुहूर्त दर्शक (२०२५–२०४६)",
            whyCelebratedHeader = "सणाचे महत्त्व आणि पौराणिक कथा",
            puranicLegendHeader = "पौराणिक कथा आणि इतिहास",
            presidingDeityHeader = "आराध्य दैवत",
            primaryMoolaMantraHeader = "मुख्य मूल मंत्र (पूजेत जपण्यासाठी)",
            vratAndNaivedyaHeader = "व्रत नियम आणि नैवेद्य मार्गदर्शक",
            regionalNamesHeaderSuffix = "भारतातील विविध राज्यांतील नावे",
            vidhiTabPrefix = "पूजा विधी",
            samagriTabPrefix = "सामग्री",
            mantrasTabPrefix = "पूजा मंत्र",
            guidedModeButton = "गाइडेड पूजा मोड",
            essentialItemsHeader = "अत्यावश्यक पूजा सामग्री",
            optionalItemsHeader = "पर्यायी व प्रादेशिक सामग्री",
            familyNotesHeader = "कौटुंबिक परंपरा आणि टिपा",
            japaCounterHeader = "जप माळ काउंटर",
            meaningPrefix = "भावार्थ",
            languageSectionHeader = "ॲपची भाषा (LANGUAGE)",
            locationSectionHeader = "तुमचे शहर (LOCATION)",
            traditionsSectionHeader = "पंचांग आणि प्रादेशिक परंपरा",
            remindersSectionHeader = "सण आठवण (REMINDERS)",
            appPreferencesSectionHeader = "थीम आणि अक्षरांचा आकार (FONT SIZE)",
            dataPrivacySectionHeader = "डेटा आणि गोपनीयता",
            aboutSectionHeader = "उत्सवसेतूबद्दल माहिती",
            appearanceThemeLabel = "ॲप थीम (Light / Dark)",
            textSizeLabel = "अक्षरांचा आकार (Text Size)",
            standardTextLabel = "सामान्य (Standard)",
            largeTextLabel = "मोठा आणि स्पष्ट (Large)"
        )
        AppLanguage.GUJARATI -> UtsavStrings(
            language = language,
            tabToday = "આજે",
            tabCalendar = "કેલેન્ડર",
            tabFestivals = "તહેવારો",
            tabPanchang = "પંચાંગ",
            tabProfile = "પ્રોફાઇલ",
            nextMajorFestival = "આગામી મુખ્ય તહેવાર",
            todaysPanchang = "આજનું પંચાંગ",
            auspiciousWindow = "શુભ મુહૂર્ત",
            rahuKaalAvoid = "રાહુ કાળ (વર્જ્ય)",
            comingUpInYourTradition = "તમારી પરંપરાના આગામી તહેવારો",
            prepareForNextFestival = "આગામી તહેવારની પૂજા તૈયારી",
            startPujaGuide = "પૂજા વિધિ શરૂ કરો",
            shareGreeting = "શેર કરો",
            setReminder = "રિમાઇન્ડર ગોઠવો",
            dailyPanchangHeader = "દૈનિક પંચાંગ",
            sunriseLabel = "સૂર્યોદય",
            sunsetLabel = "સૂર્યાસ્ત",
            tithiLabel = "તિથિ",
            nakshatraLabel = "નક્ષત્ર",
            yogaLabel = "યોગ",
            karanaLabel = "કરણ",
            settingsTitle = "પ્રોફાઇલ અને પસંદગીઓ",
            stepTitles = listOf("સ્વાગત", "ભાષા", "તમારું શહેર", "પરંપરા", "રિમાઇન્ડર"),
            exploreFirstCta = "પહેલા એપ જુઓ →",
            heroTagline = "તમારા તહેવારો, શ્રદ્ધા અને ચોકસાઈ સાથે.",
            heroSubtagline = "તમારા શહેરનું સચોટ મુહૂર્ત, પૂજા વિધિ અને સમયસર રિમાઇન્ડર.",
            welcomeTitle = "ઉત્સવસેતુ (UtsavSetu) માં આપનું સ્વાગત છે",
            welcomeBody = "ભારતના દરેક શહેરમાં વસતા પરિવારો માટે સચોટ તિથિ, પંચાંગ અને પૂજા મુહૂર્ત.",
            welcomePillFestivals = "૮૨+ મુખ્ય તહેવારો",
            welcomePillPanchang = "શહેરનું પંચાંગ",
            welcomePillVidhi = "પૂજા વિધિ અને સામગ્રી",
            welcomeOfflineNote = "૧૦૦% ઓફલાઇન કાર્ય કરે છે. તમારી ભાષા, શહેર અને પરંપરા પસંદ કરો.",
            stepLanguageTitle = "૧. તમારી ભાષા પસંદ કરો",
            stepLanguageSubtitle = "ભાષા પસંદ કરતા જ સમગ્ર એપની ભાષા તરત જ બદલાઈ જશે.",
            stepCityTitle = "૨. તમારું શહેર પસંદ કરો (સ્થાનિક પંચાંગ)",
            stepCitySubtitle = "સૂર્યોદય, સૂર્યાસ્ત, રાહુ કાળ અને પૂજા મુહૂર્ત તમારા શહેરના સ્થાન પર આધારિત છે.",
            useCurrentLocationGps = "મારું વર્તમાન લોકેશન (GPS) વાપરો",
            detectingLocationLabel = "GPS દ્વારા શહેર શોધી રહ્યું છે…",
            topCitiesHeader = "મુખ્ય શહેરો",
            moreCitiesHeader = "અન્ય શહેરો અને તીર્થધામો",
            citySearchPlaceholder = "શહેર શોધો (દા.ત. અમદાવાદ, સુરત, મુંબઈ)…",
            selectedCityPrefix = "પસંદ કરેલ શહેર",
            changeButton = "બદલો",
            stepTraditionTitle = "૩. પ્રાદેશિક પરંપરા અને પંચાંગ",
            stepTraditionSubtitle = "તમારા પરિવારની પરંપરા પસંદ કરો.",
            stepRemindersTitle = "૪. તહેવાર અને મુહૂર્ત રિમાઇન્ડર",
            stepRemindersSubtitle = "આગામી તહેવારો પહેલાં ક્યારે જાણ કરવી તે પસંદ કરો.",
            backButton = "પાછળ",
            nextPrefix = "આગળ",
            getStartedEnter = "શરૂ કરો · ઉત્સવસેતુમાં પ્રવેશ કરો",
            saveDefaultsEnter = "સેવ કરો અને ઉત્સવસેતુ શરૂ કરો",
            alsoKnownAsLabel = "અન્ય પ્રાદેશિક નામો",
            searchOnlineButton = "ઓનલાઇન શોધો અને એપમાં ઉમેરો",
            searchingOnlineLabel = "ઓનલાઇન માહિતી શોધી રહ્યું છે…",
            quickActionsHeader = "ઝડપી સુવિધાઓ",
            upcomingFestivalsHeader = "આગામી તહેવારો",
            upcomingFestivalsSubtitlePrefix = "સચોટ તિથિ અને શુભ મુહૂર્ત:",
            seeAllButton = "બધા જુઓ",
            exploreVidhiButton = "પૂજા વિધિ જુઓ",
            todayBadge = "આજે",
            tomorrowBadge = "આવતીકાલે",
            inDaysFormat = "%d દિવસમાં",
            monthViewLabel = "માસિક કેલેન્ડર",
            agendaViewLabel = "યાદી દૃશ્ય",
            myTraditionsAllLabel = "બધી પરંપરાઓ",
            allRegionsLabel = "બધા પ્રદેશો",
            browseByCategoryHeader = "શ્રેણી મુજબ શોધો",
            featuredCelebrationHeader = "મુખ્ય આગામી તહેવાર",
            searchFestivalsPlaceholder = "કોઈપણ તહેવાર શોધો (ભાઈ બીજ, ગોવર્ધન, દિવાળી, નવરાત્રી)…",
            panchangSubtitle = "દૈનિક જ્યોતિષીય અને શુભ મુહૂર્ત",
            panchangDetailsHeader = "પંચાંગ વિગતો (કોષ્ટક)",
            auspiciousTimingsHeader = "શુભ મુહૂર્ત",
            inauspiciousTimingsHeader = "અશુભ સમય (રાહુ કાળ - વર્જ્ય)",
            importantTimingsHeader = "મુખ્ય સમય, તિથિ અને શુભ મુહૂર્ત (કોષ્ટક)",
            verifiedMeeusBadge = "પ્રમાણિત · પંચાંગ ગણતરી",
            calculatedEstimateBadge = "ગણતરી આધારિત સમય",
            timingParameterColHeader = "પંચાંગ / મુહૂર્ત વિગત",
            timingWindowColHeader = "શુભ સમય અને વિગત (IST)",
            festivalDateRowLabel = "તહેવારની તારીખ",
            primaryMuhuratRowLabel = "મુખ્ય પૂજા શુભ મુહૂર્ત",
            additionalMuhuratRowLabel = "અન્ય મુહૂર્ત વિગત",
            tithiAndPakshaRowLabel = "તિથિ અને પક્ષ",
            tithiSpanRowLabel = "તિથિ પ્રારંભ અને સમાપ્તિ",
            nakshatraRowLabel = "નક્ષત્ર અને દેવતા",
            sunriseSunsetRowLabel = "સૂર્યોદય અને સૂર્યાસ્ત",
            auspiciousKaalRowLabel = "બ્રહ્મ, અભિજિત અને ગોધૂલિ મુહૂર્ત",
            avoidKaalRowLabel = "રાહુ કાળ અને યમગંડમ (વર્જ્ય)",
            lunarMonthRowLabel = "હિન્દુ ચંદ્ર માસ",
            shastraRuleRowLabel = "શાસ્ત્રોક્ત નિર્ણય નિયમ",
            twentyYearExplorerLabel = "૨૦-વર્ષીય પંચાંગ અને મુહૂર્ત દર્શક (૨૦૨૫–૨૦૪૬)",
            whyCelebratedHeader = "તહેવારનું મહત્વ અને પૌરાણિક કથા",
            puranicLegendHeader = "પૌરાણિક કથા અને ઇતિહાસ",
            presidingDeityHeader = "આરાધ્ય દેવી / દેવતા",
            primaryMoolaMantraHeader = "મુખ્ય મૂળ મંત્ર (પૂજામાં જપ માટે)",
            vratAndNaivedyaHeader = "વ્રત નિયમ અને નૈવેદ્ય (પ્રસાદ) માર્ગદર્શિકા",
            regionalNamesHeaderSuffix = "ભારતના વિવિધ રાજ્યોમાં નામો",
            vidhiTabPrefix = "પૂજા વિધિ",
            samagriTabPrefix = "સામગ્રી",
            mantrasTabPrefix = "પૂજા મંત્ર",
            guidedModeButton = "ગાઇડેડ પૂજા મોડ",
            essentialItemsHeader = "અનિવાર્ય પૂજા સામગ્રી",
            optionalItemsHeader = "વૈકલ્પિક અને પ્રાદેશિક સામગ્રી",
            familyNotesHeader = "પારિવારિક પરંપરા અને નોંધ",
            japaCounterHeader = "જપ માળા કાઉન્ટર",
            meaningPrefix = "ભાવાર્થ",
            languageSectionHeader = "એપની ભાષા (LANGUAGE)",
            locationSectionHeader = "તમારું શહેર (LOCATION)",
            traditionsSectionHeader = "પંચાંગ અને પ્રાદેશિક પરંપરા",
            remindersSectionHeader = "તહેવાર રિમાઇન્ડર",
            appPreferencesSectionHeader = "થીમ અને અક્ષરનું કદ (FONT SIZE)",
            dataPrivacySectionHeader = "ડેટા અને গোપનીયતા",
            aboutSectionHeader = "ઉત્સવસેતુ વિશે",
            appearanceThemeLabel = "એપ થીમ (Light / Dark)",
            textSizeLabel = "અક્ષરનું કદ (Text Size)",
            standardTextLabel = "સામાન્ય (Standard)",
            largeTextLabel = "મોટા અક્ષર (Large)"
        )
        AppLanguage.TAMIL -> UtsavStrings(
            language = language,
            tabToday = "இன்று",
            tabCalendar = "நாட்காட்டி",
            tabFestivals = "பண்டிகைகள்",
            tabPanchang = "பஞ்சாங்கம்",
            tabProfile = "சுயவிவரம்",
            nextMajorFestival = "அடுத்த முக்கிய பண்டிகை",
            todaysPanchang = "இன்றைய பஞ்சாங்கம்",
            auspiciousWindow = "நல்ல நேரம் (முகூர்த்தம்)",
            rahuKaalAvoid = "ராகு காலம் (தவிர்க்க)",
            comingUpInYourTradition = "வரும் பண்டிகைகள்",
            prepareForNextFestival = "பூஜை முன்னேற்பாடு",
            startPujaGuide = "பூஜை முறை தொடங்க",
            shareGreeting = "பகிர்",
            setReminder = "நினைவூட்டல்",
            dailyPanchangHeader = "தினசரி பஞ்சாங்கம்",
            sunriseLabel = "சூரிய உதயம்",
            sunsetLabel = "சூரிய அஸ்தமனம்",
            tithiLabel = "திதி",
            nakshatraLabel = "நட்சத்திரம்",
            yogaLabel = "யோகம்",
            karanaLabel = "கரணம்",
            settingsTitle = "சுயவிவரம் & விருப்பங்கள்",
            stepTitles = listOf("வரவேற்பு", "மொழி", "உங்கள் நகரம்", "மரபு", "நினைவூட்டல்"),
            exploreFirstCta = "உடனே செயலியைப் பார்க்க →",
            heroTagline = "உங்கள் பண்டிகைகளும் மரபுகளும் அழகாக இணைக்கப்பட்டுள்ளன.",
            heroSubtagline = "துல்லியமான உள்ளூர் நல்ல நேரம், பூஜை முறைகள் மற்றும் நினைவூட்டல்கள்.",
            welcomeTitle = "உத்ஸவ்சேது (UtsavSetu) உங்களை வரவேற்கிறது",
            welcomeBody = "உங்கள் நகரத்திற்கு ஏற்ற துல்லியமான திதி, நட்சத்திரம், ராகு காலம் மற்றும் பூஜை நல்ல நேரம்.",
            welcomePillFestivals = "82+ முக்கிய பண்டிகைகள்",
            welcomePillPanchang = "நகர பஞ்சாங்கம்",
            welcomePillVidhi = "பூஜை & பொருட்கள்",
            welcomeOfflineNote = "100% ஆஃப்லைனில் இயங்கும். உங்கள் மொழி, நகரம் மற்றும் மரபைத் தேர்ந்தெடுக்கவும்.",
            stepLanguageTitle = "1. உங்கள் மொழியைத் தேர்ந்தெடுக்கவும்",
            stepLanguageSubtitle = "மொழியைத் தேர்ந்தெடுத்தவுடன் செயலியின் அனைத்து திரைகளும் உடனடியாகத் தமிழ் மொழிக்கு மாறும்.",
            stepCityTitle = "2. உங்கள் நகரத்தைத் தேர்ந்தெடுக்கவும்",
            stepCitySubtitle = "சூரிய உதயம், ராகு காலம் மற்றும் முகூர்த்த நேரங்கள் உங்கள் நகரத்தின் இருப்பிடத்தைப் பொறுத்தது.",
            useCurrentLocationGps = "எனது தற்போதைய இருப்பிடத்தைப் (GPS) பயன்படுத்து",
            detectingLocationLabel = "GPS மூலம் உங்கள் நகரம் கண்டறியப்படுகிறது…",
            topCitiesHeader = "முக்கிய பெருநகரங்கள்",
            moreCitiesHeader = "அனைத்து இந்திய நகரங்கள் & திருத்தலங்கள்",
            citySearchPlaceholder = "நகரத்தைத் தேடுங்கள் (எ.கா. சென்னை, மதுரை, கோயம்புத்தூர்)…",
            selectedCityPrefix = "தேர்ந்தெடுக்கப்பட்டது",
            changeButton = "மாற்று",
            stepTraditionTitle = "3. பிராந்திய மரபு & பஞ்சாங்க முறை",
            stepTraditionSubtitle = "உங்கள் குடும்ப மரபைத் தேர்ந்தெடுக்கவும்.",
            stepRemindersTitle = "4. பண்டிகை & முகூர்த்த நினைவூட்டல்கள்",
            stepRemindersSubtitle = "பண்டிகைகளுக்கு முன் எப்போது நினைவூட்ட வேண்டும் என்பதைத் தேர்ந்தெடுக்கவும்.",
            backButton = "பின்செல்",
            nextPrefix = "அடுத்து",
            getStartedEnter = "தொடங்கவும் · உத்ஸவ்சேதுவிற்குள் நுழைய",
            saveDefaultsEnter = "சேமித்து உத்ஸவ்சேதுவிற்குள் நுழைய",
            alsoKnownAsLabel = "பிற பெயர்கள்",
            searchOnlineButton = "ஆன்லைனில் தேடி செயலியில் சேர்க்கவும்",
            searchingOnlineLabel = "ஆன்லைனில் பண்டிகை விவரங்கள் தேடப்படுகின்றன…",
            quickActionsHeader = "விரைவு வசதிகள்",
            upcomingFestivalsHeader = "வரவிருக்கும் பண்டிகைகள்",
            upcomingFestivalsSubtitlePrefix = "துல்லியமான நல்ல நேரம்:",
            seeAllButton = "அனைத்தும்",
            exploreVidhiButton = "பூஜை முறை",
            todayBadge = "இன்று",
            tomorrowBadge = "நாளை",
            inDaysFormat = "%d நாட்களில்",
            monthViewLabel = "மாத காட்சி",
            agendaViewLabel = "பட்டியல் காட்சி",
            myTraditionsAllLabel = "அனைத்து மரபுகள்",
            allRegionsLabel = "அனைத்து பகுதிகள்",
            browseByCategoryHeader = "பிரிவு வாரியாக",
            featuredCelebrationHeader = "சிறப்பு பண்டிகை",
            searchFestivalsPlaceholder = "பண்டிகையைத் தேடுங்கள் (பொங்கல், தீபாவளி, விநாயகர் சதுர்த்தி)…",
            panchangSubtitle = "தினசரி வானியல் மற்றும் பூஜை நேரங்கள்",
            panchangDetailsHeader = "பஞ்சாங்க விவரங்கள் (அட்டவணை)",
            auspiciousTimingsHeader = "நல்ல நேரம் (சுப முகூர்த்தம்)",
            inauspiciousTimingsHeader = "ராகு காலம் & எமகண்டம் (தவிர்க்க)",
            importantTimingsHeader = "முக்கிய நேரங்கள், திதி & நல்ல நேரம் (அட்டவணை)",
            verifiedMeeusBadge = "சரிபார்க்கப்பட்டது · பஞ்சாங்கம்",
            calculatedEstimateBadge = "கணக்கிடப்பட்ட நேரம்",
            timingParameterColHeader = "பஞ்சாங்கம் / பூஜை விவரம்",
            timingWindowColHeader = "நல்ல நேரம் & விவரம் (IST)",
            festivalDateRowLabel = "பண்டிகை தேதி",
            primaryMuhuratRowLabel = "முதன்மை பூஜை நல்ல நேரம்",
            additionalMuhuratRowLabel = "கூடுதல் முகூர்த்தங்கள்",
            tithiAndPakshaRowLabel = "திதி மற்றும் பக்ஷம்",
            tithiSpanRowLabel = "திதி தொடக்கம் & முடிவு",
            nakshatraRowLabel = "நட்சத்திரம் & தெய்வம்",
            sunriseSunsetRowLabel = "சூரிய உதயம் & அஸ்தமனம்",
            auspiciousKaalRowLabel = "பிரம்ம, அபிஜித் & கோதூளி முகூர்த்தம்",
            avoidKaalRowLabel = "ராகு காலம் & எமகண்டம்",
            lunarMonthRowLabel = "தமிழ் / சந்திர மாதம்",
            shastraRuleRowLabel = "சாஸ்திர திதி விதி",
            twentyYearExplorerLabel = "20-ஆண்டு பஞ்சாங்கம் & முகூர்த்தம் (2025–2046)",
            whyCelebratedHeader = "பண்டிகையின் சிறப்பு & புராண கதை",
            puranicLegendHeader = "புராண வரலாறு (கதை)",
            presidingDeityHeader = "வழிபடும் தெய்வம்",
            primaryMoolaMantraHeader = "முதன்மை மூல மந்திரம் (பூஜைக்கு)",
            vratAndNaivedyaHeader = "விரதம் & நைவேத்தியம் (பிரசாதம்) முறை",
            regionalNamesHeaderSuffix = "இந்தியாவின் பிற மாநிலப் பெயர்கள்",
            vidhiTabPrefix = "பூஜை முறை",
            samagriTabPrefix = "பொருட்கள்",
            mantrasTabPrefix = "மந்திரங்கள்",
            guidedModeButton = "வழிகாட்டி முறை",
            essentialItemsHeader = "முக்கிய பூஜை பொருட்கள்",
            optionalItemsHeader = "கூடுதல் பொருட்கள்",
            familyNotesHeader = "குடும்ப மரபு குறிப்புகள்",
            japaCounterHeader = "ஜப மாலை எண்ணிக்கை",
            meaningPrefix = "பொருள்",
            languageSectionHeader = "மொழி (LANGUAGE)",
            locationSectionHeader = "உங்கள் நகரம் (LOCATION)",
            traditionsSectionHeader = "பஞ்சாங்கம் & மரபுகள்",
            remindersSectionHeader = "நினைவூட்டல்கள் (REMINDERS)",
            appPreferencesSectionHeader = "தோற்றம் & எழுத்து அளவு (FONT SIZE)",
            dataPrivacySectionHeader = "தரவு & தனியுரிமை",
            aboutSectionHeader = "உத்ஸவ்சேது பற்றி",
            appearanceThemeLabel = "தோற்றம் (Light / Dark)",
            textSizeLabel = "எழுத்து அளவு (Text Size)",
            standardTextLabel = "சாதாரண (Standard)",
            largeTextLabel = "பெரிய எழுத்து (Large)"
        )
        AppLanguage.TELUGU -> UtsavStrings(
            language = language,
            tabToday = "ఈరోజు",
            tabCalendar = "క్యాలెండర్",
            tabFestivals = "పండుగలు",
            tabPanchang = "పంచాంగం",
            tabProfile = "ప్రొఫైల్",
            nextMajorFestival = "తదుపరి ప్రధాన పండుగ",
            todaysPanchang = "నేటి పంచాంగం",
            auspiciousWindow = "శుభ ముహూర్తం",
            rahuKaalAvoid = "రాహు కాలం (నివారించండి)",
            comingUpInYourTradition = "రాబోయే పండుగలు",
            prepareForNextFestival = "పూజ సన్నాహాలు",
            startPujaGuide = "పూజా విధానం ప్రారంభించండి",
            shareGreeting = "షేర్ చేయండి",
            setReminder = "రిమైండర్ సెట్ చేయండి",
            dailyPanchangHeader = "దైనిక పంచాంగం",
            sunriseLabel = "సూర్యోదయం",
            sunsetLabel = "సూర్యాస్తమయం",
            tithiLabel = "తిథి",
            nakshatraLabel = "నక్షత్రం",
            yogaLabel = "యోగం",
            karanaLabel = "కరణం",
            settingsTitle = "ప్రొఫైల్ & ప్రాధాన్యతలు",
            stepTitles = listOf("స్వాగతం", "భాష", "మీ నగరం", "సంప్రదాయం", "రిమైండర్లు"),
            exploreFirstCta = "ముందుగా యాప్ చూడండి →",
            heroTagline = "మీ పండుగలు మరియు సంప్రదాయాలు, భక్తితో.",
            heroSubtagline = "మీ నగరానికి ఖచ్చితమైన ముహూర్తం, పూజా విధానం మరియు రిమైండర్లు.",
            welcomeTitle = "ఉత్సవసేతు (UtsavSetu) కు స్వాగతం",
            welcomeBody = "మీ నగరంలో ఖచ్చితమైన తిథి, నక్షత్రం మరియు పూజా ముహూర్తాల కోసం రూపొందించబడింది.",
            welcomePillFestivals = "82+ ప్రధాన పండుగలు",
            welcomePillPanchang = "నగర పంచాంగం",
            welcomePillVidhi = "పూజా విధానం & సామగ్రి",
            welcomeOfflineNote = "100% ఆఫ్‌లైన్‌లో పనిచేస్తుంది. మీ భాష, నగరం మరియు సంప్రదాయాన్ని ఎంచుకోండి.",
            stepLanguageTitle = "1. మీ భాషను ఎంచుకోండి",
            stepLanguageSubtitle = "భాషను ఎంచుకున్న వెంటనే యాప్ మొత్తం ఆ భాషలోకి మారుతుంది.",
            stepCityTitle = "2. మీ నగరాన్ని ఎంచుకోండి (స్థానిక పంచాంగం)",
            stepCitySubtitle = "సూర్యోదయం, రాహు కాలం మరియు పూజా ముహూర్తాలు మీ నగరంపై ఆధారపడి ఉంటాయి.",
            useCurrentLocationGps = "ప్రస్తుత లొకేషన్ (GPS) ఉపయోగించండి",
            detectingLocationLabel = "GPS ద్వారా మీ నగరాన్ని గుర్తిస్తోంది…",
            topCitiesHeader = "ప్రధాన నగరాలు",
            moreCitiesHeader = "ఇతర నగరాలు & పుణ్యక్షేత్రాలు",
            citySearchPlaceholder = "నగరాన్ని వెతకండి (ఉదా. హైదరాబాద్, తిరుపతి, విశాఖపట్నం)…",
            selectedCityPrefix = "ఎంచుకున్న నగరం",
            changeButton = "మార్చండి",
            stepTraditionTitle = "3. ప్రాంతీయ సంప్రదాయం & పంచాంగం",
            stepTraditionSubtitle = "మీ కుటుంబ సంప్రదాయాన్ని ఎంచుకోండి.",
            stepRemindersTitle = "4. పండుగ & ముహూర్త రిమైండర్లు",
            stepRemindersSubtitle = "పండుగలకు ముందు ఎప్పుడు గుర్తు చేయాలో ఎంచుకోండి.",
            backButton = "వెనుకకు",
            nextPrefix = "తదుపరి",
            getStartedEnter = "ప్రారంభించండి · ఉత్సవసేతులోకి ప్రవేశించండి",
            saveDefaultsEnter = "సేవ్ చేసి ప్రారంభించండి",
            alsoKnownAsLabel = "ఇతర ప్రాంతీయ పేర్లు",
            searchOnlineButton = "ఆన్‌లైన్‌లో వెతికి యాప్‌లో జోడించండి",
            searchingOnlineLabel = "ఆన్‌లైన్‌లో పండుగ వివరాలు వెతుకుతోంది…",
            quickActionsHeader = "త్వరిత ఎంపికలు",
            upcomingFestivalsHeader = "రాబోయే పండుగలు",
            upcomingFestivalsSubtitlePrefix = "ఖచ్చితమైన తేదీలు & ముహూర్తాలు:",
            seeAllButton = "అన్నీ చూడండి",
            exploreVidhiButton = "పూజా విధానం",
            todayBadge = "ఈరోజు",
            tomorrowBadge = "రేపు",
            inDaysFormat = "%d రోజుల్లో",
            monthViewLabel = "నెలవారీ వీక్షణ",
            agendaViewLabel = "జాబితా వీక్షణ",
            myTraditionsAllLabel = "అన్ని సంప్రదాయాలు",
            allRegionsLabel = "అన్ని ప్రాంతాలు",
            browseByCategoryHeader = "వర్గాల వారీగా చూడండి",
            featuredCelebrationHeader = "ప్రధాన పండుగ",
            searchFestivalsPlaceholder = "ఏదైనా పండుగను వెతకండి (దీపావళి, ఉగాది, వినాయక చవితి, సంక్రాంతి)…",
            panchangSubtitle = "దైనిక జ్యోతిష్య & శుభ ముహూర్తాలు",
            panchangDetailsHeader = "పంచాంగ వివరాలు (పట్టిక)",
            auspiciousTimingsHeader = "శుభ ముహూర్తాలు",
            inauspiciousTimingsHeader = "అశుభ సమయాలు (రాహు కాలం - నివారించండి)",
            importantTimingsHeader = "ముఖ్య సమయాలు, తిథి & శుభ ముహూర్తం (పట్టిక)",
            verifiedMeeusBadge = "ధృవీకరించబడింది · పంచాంగం",
            calculatedEstimateBadge = "గణించిన సమయం",
            timingParameterColHeader = "పంచాంగం / పూజా వివరం",
            timingWindowColHeader = "శుభ సమయం & వివరాలు (IST)",
            festivalDateRowLabel = "పండుగ తేదీ",
            primaryMuhuratRowLabel = "ప్రధాన పూజా శుభ ముహూర్తం",
            additionalMuhuratRowLabel = "ఇతర ముహూర్త వివరాలు",
            tithiAndPakshaRowLabel = "తిథి మరియు పక్షం",
            tithiSpanRowLabel = "తిథి ప్రారంభం & ముగింపు",
            nakshatraRowLabel = "నక్షత్రం & దైవం",
            sunriseSunsetRowLabel = "సూర్యోదయం & సూర్యాస్తమయం",
            auspiciousKaalRowLabel = "బ్రహ్మ, అభిజిత్ & గోధూళి ముహూర్తం",
            avoidKaalRowLabel = "రాహు కాలం & యమగండం",
            lunarMonthRowLabel = "తెలుగు / చాంద్రమాసం",
            shastraRuleRowLabel = "శాస్త్ర తిథి నియమం",
            twentyYearExplorerLabel = "20-సంవత్సరాల పంచాంగం & ముహూర్తాలు (2025–2046)",
            whyCelebratedHeader = "పండుగ విశిష్టత & పురాణ కథ",
            puranicLegendHeader = "పురాణ గాథ (వ్రత కథ)",
            presidingDeityHeader = "ఆరాధ్య దైవం",
            primaryMoolaMantraHeader = "ప్రధాన మూల మంత్రం (పూజలో జపించేందుకు)",
            vratAndNaivedyaHeader = "వ్రత నియమాలు & నైవేద్యం (ప్రసాదం)",
            regionalNamesHeaderSuffix = "భారతదేశంలోని వివిధ రాష్ట్రాలలో పేర్లు",
            vidhiTabPrefix = "పూజా విధానం",
            samagriTabPrefix = "సామగ్రి",
            mantrasTabPrefix = "మంత్రాలు",
            guidedModeButton = "గైడెడ్ పూజా మోడ్",
            essentialItemsHeader = "ముఖ్యమైన పూజా సామగ్రి",
            optionalItemsHeader = "ఇతర & ప్రాంతీయ సామగ్రి",
            familyNotesHeader = "కుటుంబ ఆచారాలు & నోట్స్",
            japaCounterHeader = "జపమాల కౌంటర్",
            meaningPrefix = "భావం",
            languageSectionHeader = "యాప్ భాష (LANGUAGE)",
            locationSectionHeader = "మీ నగరం (LOCATION)",
            traditionsSectionHeader = "పంచాంగం & సంప్రదాయాలు",
            remindersSectionHeader = "రిమైండర్లు (REMINDERS)",
            appPreferencesSectionHeader = "థీమ్ & అక్షరాల పరిమాణం (FONT SIZE)",
            dataPrivacySectionHeader = "డేటా & గోప్యత",
            aboutSectionHeader = "ఉత్సవసేతు గురించి",
            appearanceThemeLabel = "థీమ్ (Light / Dark)",
            textSizeLabel = "అక్షరాల పరిమాణం (Text Size)",
            standardTextLabel = "సాధారణ (Standard)",
            largeTextLabel = "పెద్ద అక్షరాలు (Large)"
        )
        AppLanguage.KANNADA -> UtsavStrings(
            language = language,
            tabToday = "ಇಂದು",
            tabCalendar = "ಕ್ಯಾಲೆಂಡರ್",
            tabFestivals = "ಹಬ್ಬಗಳು",
            tabPanchang = "ಪಂಚಾಂಗ",
            tabProfile = "ಪ್ರೊಫೈಲ್",
            nextMajorFestival = "ಮುಂದಿನ ಪ್ರಮುಖ ಹಬ್ಬ",
            todaysPanchang = "ಇಂದಿನ ಪಂಚಾಂಗ",
            auspiciousWindow = "ಶುಭ ಮುಹೂರ್ತ",
            rahuKaalAvoid = "ರಾಹು ಕಾಲ (ವರ್ಜ್ಯ)",
            comingUpInYourTradition = "ಮುಂಬರುವ ಹಬ್ಬಗಳು",
            prepareForNextFestival = "ಪೂಜಾ ಸಿದ್ಧತೆ",
            startPujaGuide = "ಪೂಜಾ ವಿಧಾನ ಪ್ರಾರಂಭಿಸಿ",
            shareGreeting = "ಹಂಚಿಕೊಳ್ಳಿ",
            setReminder = "ರಿಮೈಂಡರ್",
            dailyPanchangHeader = "ದೈನಂದಿನ ಪಂಚಾಂಗ",
            sunriseLabel = "ಸೂರ್ಯೋದಯ",
            sunsetLabel = "ಸೂರ್ಯಾಸ್ತ",
            tithiLabel = "ತಿಥಿ",
            nakshatraLabel = "ನಕ್ಷತ್ರ",
            yogaLabel = "ಯೋಗ",
            karanaLabel = "ಕರಣ",
            settingsTitle = "ಪ್ರೊಫೈಲ್ ಮತ್ತು ಆಯ್ಕೆಗಳು",
            stepTitles = listOf("ಸ್ವಾಗತ", "ಭಾಷೆ", "ನಿಮ್ಮ ನಗರ", "ಸಂಪ್ರದಾಯ", "ರಿಮೈಂಡರ್"),
            exploreFirstCta = "ಮೊದಲು ಆಪ್ ನೋಡಿ →",
            heroTagline = "ನಿಮ್ಮ ಹಬ್ಬಗಳು ಮತ್ತು ಸಂಪ್ರದಾಯಗಳ ಸಂಗಮ.",
            heroSubtagline = "ನಿಮ್ಮ ನಗರದ ನಿಖರ ಶುಭ ಮುಹೂರ್ತ, ಪೂಜಾ ವಿಧಾನ ಮತ್ತು ರಿಮೈಂಡರ್‌ಗಳು.",
            welcomeTitle = "ಉತ್ಸವಸೇತು (UtsavSetu) ಗೆ ಸ್ವಾಗತ",
            welcomeBody = "ನಿಮ್ಮ ನಗರದಲ್ಲಿ ನಿಖರವಾದ ತಿಥಿ, ನಕ್ಷತ್ರ ಮತ್ತು ಪೂಜಾ ಮುಹೂರ್ತಗಳಿಗಾಗಿ ವಿನ್ಯಾಸಗೊಳಿಸಲಾಗಿದೆ.",
            welcomePillFestivals = "82+ ಪ್ರಮುಖ ಹಬ್ಬಗಳು",
            welcomePillPanchang = "ನಗರ ಪಂಚಾಂಗ",
            welcomePillVidhi = "ಪೂಜಾ ವಿಧಿ & ಸಾಮಗ್ರಿ",
            welcomeOfflineNote = "100% ಆಫ್‌ಲೈನ್‌ನಲ್ಲಿ ಕಾರ್ಯನಿರ್ವಹಿಸುತ್ತದೆ. ನಿಮ್ಮ ಭಾಷೆ, ನಗರ ಮತ್ತು ಸಂಪ್ರದಾಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ.",
            stepLanguageTitle = "1. ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ",
            stepLanguageSubtitle = "ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿದ ತಕ್ಷಣ ಸಂಪೂರ್ಣ ಆಪ್ ಕನ್ನಡಕ್ಕೆ ಬದಲಾಗುತ್ತದೆ.",
            stepCityTitle = "2. ನಿಮ್ಮ ನಗರವನ್ನು ಆಯ್ಕೆಮಾಡಿ (ಸ್ಥಳೀಯ ಪಂಚಾಂಗ)",
            stepCitySubtitle = "ಸೂರ್ಯೋದಯ, ರಾಹು ಕಾಲ ಮತ್ತು ಪೂಜಾ ಮುಹೂರ್ತಗಳು ನಿಮ್ಮ ನಗರವನ್ನು ಅವಲಂಬಿಸಿರುತ್ತವೆ.",
            useCurrentLocationGps = "ಪ್ರಸ್ತುತ ಸ್ಥಳವನ್ನು (GPS) ಬಳಸಿ",
            detectingLocationLabel = "GPS ಮೂಲಕ ನಿಮ್ಮ ನಗರವನ್ನು ಹುಡುಕಲಾಗುತ್ತಿದೆ…",
            topCitiesHeader = "ಪ್ರಮುಖ ನಗರಗಳು",
            moreCitiesHeader = "ಎಲ್ಲಾ ಭಾರತೀಯ ನಗರಗಳು",
            citySearchPlaceholder = "ನಗರವನ್ನು ಹುಡುಕಿ (ಉದಾ. ಬೆಂಗಳೂರು, ಮೈಸೂರು)…",
            selectedCityPrefix = "ಆಯ್ಕೆಮಾಡಿದ ನಗರ",
            changeButton = "ಬದಲಾಯಿಸಿ",
            stepTraditionTitle = "3. ಪ್ರಾದೇಶಿಕ ಸಂಪ್ರದಾಯ ಮತ್ತು ಪಂಚಾಂಗ",
            stepTraditionSubtitle = "ನಿಮ್ಮ ಕುಟುಂಬದ ಸಂಪ್ರದಾಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ.",
            stepRemindersTitle = "4. ಹಬ್ಬ ಮತ್ತು ಮುಹೂರ್ತ ರಿಮೈಂಡರ್‌ಗಳು",
            stepRemindersSubtitle = "ಹಬ್ಬಗಳ ಮೊದಲು ಯಾವಾಗ ನೆನಪಿಸಬೇಕೆಂದು ಆಯ್ಕೆಮಾಡಿ.",
            backButton = "ಹಿಂದೆ",
            nextPrefix = "ಮುಂದೆ",
            getStartedEnter = "ಪ್ರಾರಂಭಿಸಿ · ಉತ್ಸವಸೇತು ಪ್ರವೇಶಿಸಿ",
            saveDefaultsEnter = "ಉಳಿಸಿ ಮತ್ತು ಪ್ರಾರಂಭಿಸಿ",
            alsoKnownAsLabel = "ಇತರ ಪ್ರಾದೇಶಿಕ ಹೆಸರುಗಳು",
            searchOnlineButton = "ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ ಹುಡುಕಿ ಮತ್ತು ಆಪ್‌ಗೆ ಸೇರಿಸಿ",
            searchingOnlineLabel = "ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ ಹಬ್ಬದ ಮಾಹಿತಿಯನ್ನು ಹುಡುಕಲಾಗುತ್ತಿದೆ…",
            quickActionsHeader = "ತ್ವರಿತ ಆಯ್ಕೆಗಳು",
            upcomingFestivalsHeader = "ಮುಂಬರುವ ಹಬ್ಬಗಳು",
            upcomingFestivalsSubtitlePrefix = "ನಿಖರ ದಿನಾಂಕ ಮತ್ತು ಶುಭ ಮುಹೂರ್ತ:",
            seeAllButton = "ಎಲ್ಲವನ್ನೂ ನೋಡಿ",
            exploreVidhiButton = "ಪೂಜಾ ವಿಧಾನ",
            todayBadge = "ಇಂದು",
            tomorrowBadge = "ನಾಳೆ",
            inDaysFormat = "%d ದಿನಗಳಲ್ಲಿ",
            monthViewLabel = "ಮಾಸಿಕ ನೋಟ",
            agendaViewLabel = "ಪಟ್ಟಿ ನೋಟ",
            myTraditionsAllLabel = "ಎಲ್ಲಾ ಸಂಪ್ರದಾಯಗಳು",
            allRegionsLabel = "ಎಲ್ಲಾ ಪ್ರದೇಶಗಳು",
            browseByCategoryHeader = "ವರ್ಗದ ಮೂಲಕ ಹುಡುಕಿ",
            featuredCelebrationHeader = "ಪ್ರಮುಖ ಹಬ್ಬ",
            searchFestivalsPlaceholder = "ಯಾವುದೇ ಹಬ್ಬವನ್ನು ಹುಡುಕಿ (ದೀಪಾವಳಿ, ಯುಗಾದಿ, ಗೌರಿ ಗಣೇಶ)…",
            panchangSubtitle = "ದೈನಂದಿನ ಜ್ಯೋತಿಷ್ಯ ಮತ್ತು ಶುಭ ಮುಹೂರ್ತ",
            panchangDetailsHeader = "ಪಂಚಾಂಗ ವಿವರಗಳು (ಕೋಷ್ಟಕ)",
            auspiciousTimingsHeader = "ಶುಭ ಮುಹೂರ್ತಗಳು",
            inauspiciousTimingsHeader = "ಅಶುಭ ಸಮಯ (ರಾಹು ಕಾಲ - ವರ್ಜ್ಯ)",
            importantTimingsHeader = "ಪ್ರಮುಖ ಸಮಯ, ತಿಥಿ ಮತ್ತು ಶುಭ ಮುಹೂರ್ತ (ಕೋಷ್ಟಕ)",
            verifiedMeeusBadge = "ದೃಢೀಕರಿಸಲಾಗಿದೆ · ಪಂಚಾಂಗ",
            calculatedEstimateBadge = "ಲೆಕ್ಕಾಚಾರದ ಸಮಯ",
            timingParameterColHeader = "ಪಂಚಾಂಗ / ಪೂಜಾ ವಿವರ",
            timingWindowColHeader = "ಶುಭ ಸಮಯ ಮತ್ತು ವಿವರ (IST)",
            festivalDateRowLabel = "ಹಬ್ಬದ ದಿನಾಂಕ",
            primaryMuhuratRowLabel = "ಮುಖ್ಯ ಪೂಜಾ ಶುಭ ಮುಹೂರ್ತ",
            additionalMuhuratRowLabel = "ಇತರ ಮುಹೂರ್ತ ವಿವರ",
            tithiAndPakshaRowLabel = "ತಿಥಿ ಮತ್ತು ಪಕ್ಷ",
            tithiSpanRowLabel = "ತಿಥಿ ಪ್ರಾರಂಭ ಮತ್ತು ಅಂತ್ಯ",
            nakshatraRowLabel = "ನಕ್ಷತ್ರ ಮತ್ತು ದೇವತೆ",
            sunriseSunsetRowLabel = "ಸೂರ್ಯೋದಯ ಮತ್ತು ಸೂರ್ಯಾಸ್ತ",
            auspiciousKaalRowLabel = "ಬ್ರಹ್ಮ, ಅಭಿಜಿತ್ ಮತ್ತು ಗೋಧೂಳಿ ಮುಹೂರ್ತ",
            avoidKaalRowLabel = "ರಾಹು ಕಾಲ ಮತ್ತು ಯಮಗಂಡಕಾಲ",
            lunarMonthRowLabel = "ಹಿಂದೂ ಚಾಂದ್ರಮಾನ ಮಾಸ",
            shastraRuleRowLabel = "ಶಾಸ್ತ್ರೋಕ್ತ ತಿಥಿ ನಿಯಮ",
            twentyYearExplorerLabel = "20-ವರ್ಷಗಳ ಪಂಚಾಂಗ ಮತ್ತು ಮುಹೂರ್ತ (2025–2046)",
            whyCelebratedHeader = "ಹಬ್ಬದ ಮಹತ್ವ ಮತ್ತು ಪೌರಾಣಿಕ ಕಥೆ",
            puranicLegendHeader = "ಪೌರಾಣಿಕ ಹಿನ್ನೆಲೆ (ವ್ರತ ಕಥೆ)",
            presidingDeityHeader = "ಆರಾಧ್ಯ ದೈವ",
            primaryMoolaMantraHeader = "ಪ್ರಧಾನ ಮೂಲ ಮಂತ್ರ (ಪೂಜೆಯಲ್ಲಿ ಜಪಿಸಲು)",
            vratAndNaivedyaHeader = "ವ್ರತ ನಿಯಮಗಳು ಮತ್ತು ನೈವೇದ್ಯ (ಪ್ರಸಾದ)",
            regionalNamesHeaderSuffix = "ಭಾರತದ ವಿವಿಧ ರಾಜ್ಯಗಳಲ್ಲಿ ಹೆಸರುಗಳು",
            vidhiTabPrefix = "ಪೂಜಾ ವಿಧಿ",
            samagriTabPrefix = "ಸಾಮಗ್ರಿ",
            mantrasTabPrefix = "ಮಂತ್ರಗಳು",
            guidedModeButton = "ಗೈಡೆಡ್ ಪೂಜಾ ಮೋಡ್",
            essentialItemsHeader = "ಅಗತ್ಯ ಪೂಜಾ ಸಾಮಗ್ರಿಗಳು",
            optionalItemsHeader = "ಐಚ್ಛಿಕ ಸಾಮಗ್ರಿಗಳು",
            familyNotesHeader = "ಕುಟುಂಬ ಸಂಪ್ರದಾಯ ಮತ್ತು ಟಿಪ್ಪಣಿಗಳು",
            japaCounterHeader = "ಜಪಮಾಲೆ ಕೌಂಟರ್",
            meaningPrefix = "ಭಾವಾರ್ಥ",
            languageSectionHeader = "ಆಪ್ ಭಾಷೆ (LANGUAGE)",
            locationSectionHeader = "ನಿಮ್ಮ ನಗರ (LOCATION)",
            traditionsSectionHeader = "ಪಂಚಾಂಗ ಮತ್ತು ಸಂಪ್ರದಾಯಗಳು",
            remindersSectionHeader = "ರಿಮೈಂಡರ್‌ಗಳು (REMINDERS)",
            appPreferencesSectionHeader = "ಥೀಮ್ ಮತ್ತು ಅಕ್ಷರದ ಗಾತ್ರ (FONT SIZE)",
            dataPrivacySectionHeader = "ಡೇಟಾ ಮತ್ತು ಗೌಪ್ಯತೆ",
            aboutSectionHeader = "ಉತ್ಸವಸೇತು ಬಗ್ಗೆ",
            appearanceThemeLabel = "ಥೀಮ್ (Light / Dark)",
            textSizeLabel = "ಅಕ್ಷರದ ಗಾತ್ರ (Text Size)",
            standardTextLabel = "ಸಾಮಾನ್ಯ (Standard)",
            largeTextLabel = "ದೊಡ್ಡ ಅಕ್ಷರ (Large)"
        )
        AppLanguage.BENGALI -> UtsavStrings(
            language = language,
            tabToday = "আজ",
            tabCalendar = "ক্যালেন্ডার",
            tabFestivals = "উৎসব",
            tabPanchang = "পঞ্জিকা",
            tabProfile = "প্রোফাইল",
            nextMajorFestival = "পরবর্তী প্রধান উৎসব",
            todaysPanchang = "আজকের পঞ্জিকা",
            auspiciousWindow = "শুভ মুহূর্ত",
            rahuKaalAvoid = "রাহুকাল (বর্জনীয়)",
            comingUpInYourTradition = "আপনার পরম্পরায় আসন্ন উৎসব",
            prepareForNextFestival = "পরবর্তী পূজার প্রস্তুতি",
            startPujaGuide = "পূজা বিধি শুরু করুন",
            shareGreeting = "শেয়ার করুন",
            setReminder = "রিমাইন্ডার দিন",
            dailyPanchangHeader = "দৈনিক পঞ্জিকা",
            sunriseLabel = "সূর্যোদয়",
            sunsetLabel = "সূর্যাস্ত",
            tithiLabel = "তিথি",
            nakshatraLabel = "নক্ষত্র",
            yogaLabel = "যোগ",
            karanaLabel = "করণ",
            settingsTitle = "প্রোফাইল ও পছন্দসমূহ",
            stepTitles = listOf("স্বাগতম", "ভাষা", "আপনার শহর", "পরম্পরা", "রিমাইন্ডার"),
            exploreFirstCta = "সরাসরি অ্যাপ দেখুন →",
            heroTagline = "আপনার উৎসব ও পরম্পরা, শ্রদ্ধার সাথে যুক্ত।",
            heroSubtagline = "আপনার শহরের সঠিক শুভ মুহূর্ত, পূজা বিধি এবং সময়মতো রিমাইন্ডার।",
            welcomeTitle = "উৎসবসেতু (UtsavSetu)-তে স্বাগতম",
            welcomeBody = "আপনার শহরে সঠিক তিথি, নক্ষত্র এবং পূজার শুভ মুহূর্ত পাওয়ার জন্য নির্মিত।",
            welcomePillFestivals = "৮২+ প্রধান উৎসব",
            welcomePillPanchang = "শহরের পঞ্জিকা",
            welcomePillVidhi = "পূজা বিধি ও সামগ্রী",
            welcomeOfflineNote = "১০০% অফলাইনে কাজ করে। আপনার ভাষা, শহর এবং পরম্পরা বেছে নিন।",
            stepLanguageTitle = "১. আপনার ভাষা নির্বাচন করুন",
            stepLanguageSubtitle = "ভাষা নির্বাচন করার সাথে সাথেই সম্পূর্ণ অ্যাপের ভাষা পরিবর্তিত হবে।",
            stepCityTitle = "২. আপনার শহর নির্বাচন করুন (স্থানীয় পঞ্জিকা)",
            stepCitySubtitle = "সূর্যোদয়, রাহুকাল এবং পূজার শুভ মুহূর্ত আপনার শহরের অবস্থানের ওপর নির্ভর করে।",
            useCurrentLocationGps = "বর্তমান লোকেশন (GPS) ব্যবহার করুন",
            detectingLocationLabel = "GPS এর মাধ্যমে আপনার শহর খোঁজা হচ্ছে…",
            topCitiesHeader = "প্রধান শহরসমূহ",
            moreCitiesHeader = "সকল ভারতীয় শহর ও তীর্থস্থান",
            citySearchPlaceholder = "শহর খুঁজুন (যেমন কলকাতা, গুয়াহাটি, দিল্লি)…",
            selectedCityPrefix = "নির্বাচিত শহর",
            changeButton = "পরিবর্তন করুন",
            stepTraditionTitle = "৩. আঞ্চলিক পরম্পরা ও পঞ্জিকা",
            stepTraditionSubtitle = "আপনার পরিবারের পরম্পরা নির্বাচন করুন।",
            stepRemindersTitle = "৪. উৎসব ও শুভ মুহূর্ত রিমাইন্ডার",
            stepRemindersSubtitle = "উৎসবের আগে কখন রিমাইন্ডার পেতে চান তা বেছে নিন।",
            backButton = "পেছনে",
            nextPrefix = "পরবর্তী",
            getStartedEnter = "শুরু করুন · উৎসবসেতুতে প্রবেশ করুন",
            saveDefaultsEnter = "সংরক্ষণ করুন ও প্রবেশ করুন",
            alsoKnownAsLabel = "অন্যান্য আঞ্চলিক নাম",
            searchOnlineButton = "অনলাইনে খুঁজুন এবং অ্যাপে যোগ করুন",
            searchingOnlineLabel = "অনলাইনে উৎসবের তথ্য খোঁজা হচ্ছে…",
            quickActionsHeader = "দ্রুত পরিষেবা",
            upcomingFestivalsHeader = "আসন্ন উৎসব ও পূজা",
            upcomingFestivalsSubtitlePrefix = "সঠিক তারিখ ও শুভ মুহূর্ত:",
            seeAllButton = "সব দেখুন",
            exploreVidhiButton = "পূজা বিধি দেখুন",
            todayBadge = "আজ",
            tomorrowBadge = "আগামীকাল",
            inDaysFormat = "%d দিন পর",
            monthViewLabel = "মাসিক ক্যালেন্ডার",
            agendaViewLabel = "তালিকা দৃশ্য",
            myTraditionsAllLabel = "সকল পরম্পরা",
            allRegionsLabel = "সকল অঞ্চল",
            browseByCategoryHeader = "বিভাগ অনুযায়ী খুঁজুন",
            featuredCelebrationHeader = "বিশেষ আসন্ন উৎসব",
            searchFestivalsPlaceholder = "যেকোনো উৎসব খুঁজুন (ভাইফোঁটা, গোবর্ধন, দীপাবলি, দুর্গাপূজা)…",
            panchangSubtitle = "দৈনিক জ্যোতিষীয় ও পূজার শুভ সময়",
            panchangDetailsHeader = "পঞ্জিকা বিবরণ (সারণী)",
            auspiciousTimingsHeader = "শুভ মুহূর্ত",
            inauspiciousTimingsHeader = "অশুভ সময় (রাহুকাল - বর্জনীয়)",
            importantTimingsHeader = "গুরুত্বপূর্ণ সময়, তিথি ও শুভ মুহূর্ত (সারণী)",
            verifiedMeeusBadge = "যাচাইকৃত · পঞ্জিকা গণনা",
            calculatedEstimateBadge = "গণনাকৃত সময়",
            timingParameterColHeader = "পঞ্জিকা / পূজার বিবরণ",
            timingWindowColHeader = "শুভ সময় ও বিস্তারিত (IST)",
            festivalDateRowLabel = "উৎসবের তারিখ",
            primaryMuhuratRowLabel = "প্রধান পূজার শুভ মুহূর্ত",
            additionalMuhuratRowLabel = "অন্যান্য মুহূর্ত বিবরণ",
            tithiAndPakshaRowLabel = "তিথি ও পক্ষ",
            tithiSpanRowLabel = "তিথি শুরু ও সমাপ্তি",
            nakshatraRowLabel = "নক্ষত্র ও দেবতা",
            sunriseSunsetRowLabel = "সূর্যোদয় ও সূর্যাস্ত",
            auspiciousKaalRowLabel = "ব্রহ্ম, অভিজিৎ ও গোধূলি মুহূর্ত",
            avoidKaalRowLabel = "রাহুকাল ও যমগণ্ডম (বর্জনীয়)",
            lunarMonthRowLabel = "হিন্দু চান্দ্র মাস",
            shastraRuleRowLabel = "শাস্ত্রীয় তিথি নিয়ম",
            twentyYearExplorerLabel = "২০-বছরের পঞ্জিকা ও শুভ মুহূর্ত (২০২৫–২০৪৬)",
            whyCelebratedHeader = "উৎসবের তাৎপর্য ও পৌরাণিক ব্রত কথা",
            puranicLegendHeader = "পৌরাণিক ইতিহাস ও ব্রত কথা",
            presidingDeityHeader = "আরাধ্য দেবতা / দেবী",
            primaryMoolaMantraHeader = "প্রধান মূল মন্ত্র (পূজায় জপের জন্য)",
            vratAndNaivedyaHeader = "ব্রত বিধি ও নৈবেদ্য (ভোগ) নির্দেশিকা",
            regionalNamesHeaderSuffix = "ভারতের বিভিন্ন রাজ্যে নাম",
            vidhiTabPrefix = "পূজা বিধি",
            samagriTabPrefix = "সামগ্রী",
            mantrasTabPrefix = "পূজা মন্ত্র",
            guidedModeButton = "গাইডেড পূজা মোড",
            essentialItemsHeader = "অপরিহার্য পূজা সামগ্রী",
            optionalItemsHeader = "ঐচ্ছিক ও আঞ্চলিক সামগ্রী",
            familyNotesHeader = "পারিবারিক পরম্পরা ও নোট",
            japaCounterHeader = "জপমালা কাউন্টার",
            meaningPrefix = "মর্মার্থ",
            languageSectionHeader = "অ্যাপের ভাষা (LANGUAGE)",
            locationSectionHeader = "আপনার শহর (LOCATION)",
            traditionsSectionHeader = "পঞ্জিকা ও আঞ্চলিক পরম্পরা",
            remindersSectionHeader = "রিমাইন্ডার (REMINDERS)",
            appPreferencesSectionHeader = "থিম ও অক্ষরের আকার (FONT SIZE)",
            dataPrivacySectionHeader = "ডেটা ও গোপনীয়তা",
            aboutSectionHeader = "উৎসবসেতু সম্পর্কে",
            appearanceThemeLabel = "অ্যাপ থিম (Light / Dark)",
            textSizeLabel = "অক্ষরের আকার (Text Size)",
            standardTextLabel = "সাধারণ (Standard)",
            largeTextLabel = "বড় ও স্পষ্ট (Large)"
        )
    }
}

val LocalUtsavStrings = staticCompositionLocalOf {
    stringsForLanguage(AppLanguage.ENGLISH)
}
