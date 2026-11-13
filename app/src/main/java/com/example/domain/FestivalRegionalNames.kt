package com.example.domain

import com.example.data.preferences.AppLanguage
import com.example.domain.model.Festival

data class RegionalNameBadge(
    val regionOrLanguage: String,
    val festivalName: String,
    val nativeScript: String? = null
)

object FestivalRegionalNames {

    private val REGIONAL_NAME_MAP: Map<String, List<RegionalNameBadge>> = mapOf(
        "bhai_dooj" to listOf(
            RegionalNameBadge("North India (Hindi)", "Bhai Dooj", "भाई दूज"),
            RegionalNameBadge("Maharashtra & Goa (Marathi)", "Bhau Beej", "भाऊबीज"),
            RegionalNameBadge("Bengal & East (Bengali)", "Bhai Phonta", "ভাইফোঁটা"),
            RegionalNameBadge("South India (Telugu/Tamil/Kannada)", "Yama Dwitiya / Bhagini Hastha Bhojanam", "యమ ద్వితీయ"),
            RegionalNameBadge("Gujarat (Gujarati)", "Bhai Bij", "ભાઈ બીજ"),
            RegionalNameBadge("Himalayan / Nepal", "Bhai Tika", "भाई टीका")
        ),
        "makar_sankranti" to listOf(
            RegionalNameBadge("North & West India", "Makar Sankranti / Khichdi", "मकर संक्रांति"),
            RegionalNameBadge("Tamil Nadu (Tamil)", "Thai Pongal", "தைப் பொங்கல்"),
            RegionalNameBadge("Gujarat (Gujarati)", "Uttarayan", "ઉત્તરાયણ"),
            RegionalNameBadge("Punjab & Haryana", "Maghi / Lohri", "ਮਾਘੀ / ਲੋਹੜੀ"),
            RegionalNameBadge("Assam", "Magh Bihu / Bhogali Bihu", "মাঘ বিহু"),
            RegionalNameBadge("Andhra & Telangana", "Pedda Panduga / Sankranti", "పెద్ద పండుగ"),
            RegionalNameBadge("Karnataka", "Makara Sankramana / Suggi", "ಮಕರ ಸಂಕ್ರಾಂತಿ"),
            RegionalNameBadge("Bengal", "Poush Sankranti", "পৌষ সংক্রান্তি")
        ),
        "gudi_padwa_ugadi" to listOf(
            RegionalNameBadge("Maharashtra & Konkan", "Gudi Padwa / Samvatsar Padvo", "गुढीपाडवा"),
            RegionalNameBadge("Karnataka, Andhra & Telangana", "Ugadi / Yugadi", "ಯುಗಾದಿ / ఉగాది"),
            RegionalNameBadge("North India", "Chaitra Sukhladi / Chaitra Navratri", "चैत्र नववर्ष"),
            RegionalNameBadge("Sindhi Tradition", "Cheti Chand", "चेटी चंड"),
            RegionalNameBadge("Kashmiri Tradition", "Navreh", "नवरेह"),
            RegionalNameBadge("Manipur", "Sajibu Cheiraoba", "साजिबु चेराओबा")
        ),
        "mesha_sankranti_new_year" to listOf(
            RegionalNameBadge("Tamil Nadu", "Puthandu / Varusha Pirappu", "தமிழ் புத்தாண்டு"),
            RegionalNameBadge("Kerala", "Vishu", "വിഷു"),
            RegionalNameBadge("Punjab & North India", "Baisakhi / Vaisakhi", "बैसाखी"),
            RegionalNameBadge("Bengal & Tripura", "Poila Boishakh (Subho Nababarsha)", "পহেলা বৈশাখ"),
            RegionalNameBadge("Assam", "Bohag Bihu / Rongali Bihu", "ব’হাগ বিহু"),
            RegionalNameBadge("Odisha", "Pana Sankranti / Maha Vishuba", "পণা সংক্রান্তি"),
            RegionalNameBadge("Mithila / Bihar", "Jur Sital / Sattuani", "जूड़ शीतल")
        ),
        "diwali_lakshmi_puja" to listOf(
            RegionalNameBadge("North & West India", "Diwali — Lakshmi Puja", "दीपावली लक्ष्मी पूजा"),
            RegionalNameBadge("Tamil Nadu & South India", "Deepavali", "தீபாவளி / దీపావళి"),
            RegionalNameBadge("Bengal & East India", "Kali Puja / Dipannita", "কালীপূজা ও দীপান্বিতা"),
            RegionalNameBadge("Gujarat", "Chopda Pujan & Lakshmi Puja", "ચોપડા પૂજન"),
            RegionalNameBadge("Maharashtra", "Lakshmi Pujan", "लक्ष्मीपूजन")
        ),
        "dhanteras" to listOf(
            RegionalNameBadge("North & West India", "Dhanteras", "धनतेरस"),
            RegionalNameBadge("Maharashtra & South", "Dhantrayodashi / Dhanatrayodashi", "धनत्रयोदशी"),
            RegionalNameBadge("Ayurveda Tradition", "Dhanvantari Jayanti", "धन्वंतरि जयंती"),
            RegionalNameBadge("Evening Observance", "Yama Deepam / Yama Deepdaan", "यम दीपदान")
        ),
        "govardhan_puja" to listOf(
            RegionalNameBadge("North India & Braj", "Govardhan Puja / Annakut", "गोवर्धन पूजा • अन्नकूट"),
            RegionalNameBadge("Maharashtra", "Diwali Padwa / Bali Pratipada", "दिवाळी पाडवा • बलिप्रतिपदा"),
            RegionalNameBadge("Gujarat", "Bestu Varas (Gujarati New Year)", "બેસ્તુ વરસ • નૂતન વર્ષ"),
            RegionalNameBadge("Karnataka & South", "Balipadyami", "ಬಲಿಪಾಡ್ಯಮಿ")
        ),
        "ganesh_chaturthi" to listOf(
            RegionalNameBadge("Maharashtra & North", "Ganesh Chaturthi / Ganeshotsav", "गणेश चतुर्थी"),
            RegionalNameBadge("Andhra, Telangana & Karnataka", "Vinayaka Chavithi / Gowri Ganesha", "వినాయక చవితి / ಗಣೇಶ ಚತುರ್ಥಿ"),
            RegionalNameBadge("Tamil Nadu", "Vinayagar Chaturthi / Pillaiyar Chaturthi", "விநாயகர் சதுர்த்தி"),
            RegionalNameBadge("Goa & Konkan", "Chavath", "चवथ")
        ),
        "holi_dhulandi" to listOf(
            RegionalNameBadge("North India", "Holi / Dhulandi / Holika Dahan", "होली • धुलेंडी"),
            RegionalNameBadge("Bengal & Odisha", "Dol Purnima / Dol Jatra", "দোল পূর্ণিমা"),
            RegionalNameBadge("Maharashtra & Konkan", "Shimga / Hutashani Purnima", "शिमगा • होळी"),
            RegionalNameBadge("Bihar & Bhojpuri", "Phagwah", "फगुआ"),
            RegionalNameBadge("Tamil Nadu", "Kaman Pandigai / Panguni Uthiram", "காமன் பண்டிகை")
        ),
        "raksha_bandhan" to listOf(
            RegionalNameBadge("North, West & Central India", "Raksha Bandhan / Rakhi Purnima", "रक्षा बंधन"),
            RegionalNameBadge("Maharashtra & Coastal Konkan", "Narali Purnima", "नारळी पौर्णिमा"),
            RegionalNameBadge("Tamil Nadu & Kerala", "Avani Avittam / Upakarma", "ஆவணி அவிட்டம்"),
            RegionalNameBadge("Andhra & Telangana", "Jandhyala Pournami", "జంధ్యాల పౌర్ణమి"),
            RegionalNameBadge("Gujarat", "Balev", "બળેવ"),
            RegionalNameBadge("Odisha", "Gamha Purnima", "गम्हा पूर्णिमा")
        ),
        "vijayadashami_dussehra" to listOf(
            RegionalNameBadge("North India", "Dussehra / Vijayadashami", "दशहरा • विजयादशमी"),
            RegionalNameBadge("Maharashtra & Karnataka", "Dasara / Nadahabba", "दसरा • ನಾಡಹಬ್ಬ ದಸರಾ"),
            RegionalNameBadge("Bengal & East", "Bijoya Dashami & Sindoor Khela", "বিজয়া দশমী"),
            RegionalNameBadge("Tamil Nadu & Kerala", "Ayudha Puja & Vidyarambham", "ஆயுத பூஜை • விஜயதசமி")
        ),
        "vasant_panchami" to listOf(
            RegionalNameBadge("North & West India", "Vasant Panchami / Basant Panchami", "वसंत पंचमी"),
            RegionalNameBadge("Bengal, Odisha & Assam", "Saraswati Puja / Shree Panchami", "সরস্বতী পূজা"),
            RegionalNameBadge("South India", "Sri Panchami / Aksharabhyasam", "శ్రీ పంచమి")
        ),
        "kartik_purnima" to listOf(
            RegionalNameBadge("Varanasi & North India", "Dev Deepawali / Kartik Purnima", "देव दीपावली"),
            RegionalNameBadge("Maharashtra", "Tripurari Purnima", "त्रिपुरारी पौर्णिमा"),
            RegionalNameBadge("Tamil Nadu", "Karthigai Deepam / Pournami", "கார்த்திகை தீபம்"),
            RegionalNameBadge("Odisha", "Boita Bandana / Kartika Purnima", "बोइत बंदाण")
        ),
        "krishna_janmashtami" to listOf(
            RegionalNameBadge("North India", "Krishna Janmashtami", "कृष्ण जन्माष्टमी"),
            RegionalNameBadge("Maharashtra", "Gokulashtami & Dahi Handi", "गोकुळाष्टमी • दहीहंडी"),
            RegionalNameBadge("South India", "Sri Jayanti / Krishnashtami", "శ్రీ జయంతి / கோகுலாஷ்டமி"),
            RegionalNameBadge("Gujarat", "Satam Atham", "જન્માષ્ટમી")
        ),
        "varalakshmi_vratam" to listOf(
            RegionalNameBadge("Andhra, Telangana & Karnataka", "Varalakshmi Vratam / Varamahalakshmi", "వరలక్ష్మి వ్రతం / ವರಮಹಾಲಕ್ಷ್ಮಿ ವ್ರತ"),
            RegionalNameBadge("Tamil Nadu", "Varalakshmi Nombu", "வரலட்சுமி நோன்பு"),
            RegionalNameBadge("North & West India", "Shravana / Hartalika Teej", "हरतालिका तीज")
        ),
        "vat_savitri_vrat" to listOf(
            RegionalNameBadge("Maharashtra & Gujarat", "Vat Purnima / Vadachi Purnima", "वट पौर्णिमा"),
            RegionalNameBadge("North India, Bihar & MP", "Vat Savitri Vrat", "वट सावित्री व्रत"),
            RegionalNameBadge("South India", "Karadaiyan Nombu / Savitri Vrata", "காரடையான் நோன்பு")
        )
    )

    fun getRegionalNamesForFestival(festival: Festival): List<RegionalNameBadge> {
        REGIONAL_NAME_MAP[festival.id]?.let { return it }

        // Fallback: build regional badges dynamically from the festival's aliases
        val cleanAliases = festival.aliases
            .map { it.trim() }
            .filter { it.length >= 3 && !festival.nameEnglish.equals(it, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(6)

        return cleanAliases.map { alias ->
            val capitalized = alias.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
            RegionalNameBadge(
                regionOrLanguage = "Regional Name",
                festivalName = capitalized
            )
        }
    }

    fun getCompactRegionalSummary(festival: Festival): String {
        val badges = getRegionalNamesForFestival(festival)
        if (badges.isNotEmpty()) {
            return badges.joinToString(" · ") { it.festivalName }
        }
        return festival.aliases.take(5).joinToString(" · ") { alias ->
            alias.replaceFirstChar { it.uppercase() }
        }
    }

    fun getLocalizedPrimaryTitle(festival: Festival, language: AppLanguage): String {
        val badges = REGIONAL_NAME_MAP[festival.id] ?: return festival.nameEnglish
        val preferredKeyword = when (language) {
            AppLanguage.HINDI -> "Hindi"
            AppLanguage.MARATHI -> "Maharashtra"
            AppLanguage.GUJARATI -> "Gujarat"
            AppLanguage.TAMIL -> "Tamil"
            AppLanguage.TELUGU -> "Telugu"
            AppLanguage.KANNADA -> "Karnataka"
            AppLanguage.BENGALI -> "Bengal"
            AppLanguage.ENGLISH -> return festival.nameEnglish
        }
        val matched = badges.firstOrNull {
            it.regionOrLanguage.contains(preferredKeyword, ignoreCase = true)
        }
        return if (matched != null && matched.nativeScript != null) {
            "${matched.nativeScript} (${matched.festivalName})"
        } else {
            festival.nameEnglish
        }
    }
}
