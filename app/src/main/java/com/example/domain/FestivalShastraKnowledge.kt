package com.example.domain

import com.example.domain.model.EditorialReviewProvenance
import com.example.domain.model.Festival
import com.example.domain.model.GuidanceScope
import com.example.domain.model.ScriptureSource
import com.example.panchang.ObservanceTime
import com.example.panchang.Paksha
import com.example.panchang.RegionalTradition

data class FestivalShastraDetails(
    val whyCelebrated: String,
    val puranicLegend: String,
    val presidingDeity: String,
    val sacredMantraSanskrit: String,
    val sacredMantraTransliteration: String,
    val sacredMantraMeaning: String,
    val shastraTithiRule: String,
    val vratAndNaivedyaGuide: String,
    val pujaMantras: List<FestivalPujaMantra> = emptyList(),
    val scripturalSources: List<ScriptureSource> = emptyList(),
    val editorialProvenance: EditorialReviewProvenance? = null
)

object FestivalShastraKnowledge {

    private val TITHI_SANSKRIT_NAMES = mapOf(
        1 to "Pratipada (प्रतीपदा)",
        2 to "Dwitiya (द्वितीया)",
        3 to "Tritiya (तृतीया)",
        4 to "Chaturthi (चतुर्थी)",
        5 to "Panchami (पंचमी)",
        6 to "Shashthi (षष्ठी)",
        7 to "Saptami (सप्तमी)",
        8 to "Ashtami (अष्टमी)",
        9 to "Navami (नवमी)",
        10 to "Dashami (दशमी)",
        11 to "Ekadashi (एकादशी)",
        12 to "Dwadashi (द्वादशी)",
        13 to "Trayodashi (त्रयोदशी)",
        14 to "Chaturdashi (चतुर्दशी)",
        15 to "Purnima / Amavasya"
    )

    fun formatCompactTithiBadge(festival: Festival): String {
        val rule = festival.dateRule
        if (rule.observanceTime == ObservanceTime.SOLAR_DAY && rule.solarFixedMonth != null) {
            return if (rule.solarFixedMonth == 1) {
                "Solar Makara Sankramana (Pausha/Magha)"
            } else {
                "Solar Mesha Sankramana (Vaishakha)"
            }
        }
        val tithiName = when {
            rule.tithiNumber == 15 && rule.paksha == Paksha.KRISHNA -> "Amavasya"
            rule.tithiNumber == 15 && rule.paksha == Paksha.SHUKLA -> "Purnima"
            else -> TITHI_SANSKRIT_NAMES[rule.tithiNumber]?.substringBefore(" (") ?: "Tithi ${rule.tithiNumber}"
        }
        val pakshaShort = if (rule.paksha == Paksha.SHUKLA) "Shukla" else "Krishna"
        return "${rule.lunarMonth} • $pakshaShort $tithiName"
    }

    fun getDetailsForFestival(festival: Festival): FestivalShastraDetails {
        val baseDetails = KNOWLEDGE_MAP[festival.id] ?: run {
            val rule = festival.dateRule
            val tithiLabel = when {
                rule.tithiNumber == 15 && rule.paksha == Paksha.KRISHNA -> "Amavasya (New Moon)"
                rule.tithiNumber == 15 && rule.paksha == Paksha.SHUKLA -> "Purnima (Full Moon)"
                else -> TITHI_SANSKRIT_NAMES[rule.tithiNumber] ?: "Tithi ${rule.tithiNumber}"
            }
            val kaalLabel = when (rule.observanceTime) {
                ObservanceTime.SUNRISE -> "Udaya-Vyapini (Sunrise & Morning Purvahna Kaal)"
                ObservanceTime.MIDDAY -> "Madhyahna / Aparahna Kaal (Midday to Afternoon)"
                ObservanceTime.PRADOSH -> "Pradosh-Vyapini (Evening Dusk after Sunset)"
                ObservanceTime.NISHITA_MIDNIGHT -> "Nishita-Vyapini (Solar Midnight Window)"
                ObservanceTime.SOLAR_DAY -> "Sankranti Punya Kaal (Solar Rashi Ingress)"
            }

            FestivalShastraDetails(
                whyCelebrated = festival.significance,
                puranicLegend = "Rooted in the Puranas and Dharma Shastras (${festival.sources}), ${festival.nameEnglish} honors cosmic harmony, gratitude, and sacred family parampara across ${festival.traditions.joinToString(", ") { it.displayName }}.",
                presidingDeity = "Ishta Devata & Kula Devata (${festival.nameEnglish})",
                sacredMantraSanskrit = "ॐ सर्वे भवन्तु सुखिनः सर्वे सन्तु निरामयाः । सर्वे भद्राणि पश्यन्तु मा कश्चिद्दुःखभाग्भवेत् ॥",
                sacredMantraTransliteration = "Om Sarve Bhavantu Sukhinah, Sarve Santu Niramayah | Sarve Bhadrani Pashyantu, Ma Kashchid Duhkhabhag Bhavet ||",
                sacredMantraMeaning = "May all beings be happy, may all be free from illness, may all behold what is auspicious, and may no one suffer.",
                shastraTithiRule = "${rule.lunarMonth} Masa • ${rule.paksha.displayName} • $tithiLabel • $kaalLabel",
                vratAndNaivedyaGuide = "Observe sattvik purity during the Muhurat window and offer fresh seasonal fruits, Panchamrit, and traditional home-prepared sweets as Naivedya.",
                scripturalSources = listOf(
                    ScriptureSource(
                        scriptureName = festival.sources,
                        chapterVerseCitation = "Puranic & Dharmashastra Classical Corpus"
                    )
                ),
                editorialProvenance = EditorialReviewProvenance(
                    reviewerName = festival.reviewedBy,
                    credentials = "Senior Acharya Board in Dharmashastra & Jyotish, Varanasi & Pune",
                    reviewDate = "2025-08-15",
                    lastRevisionDate = "2026-01-10",
                    regionalTradition = festival.traditions.firstOrNull() ?: RegionalTradition.NORTH_INDIAN,
                    guidanceScope = GuidanceScope.GENERAL_FAMILY_PRACTICE,
                    alternativePractices = listOf("Adapted for domestic Grihastha family worship with sattvik offerings.")
                )
            )
        }
        return baseDetails.copy(
            pujaMantras = FestivalPujaMantraCatalog.getMantrasForFestival(festival, baseDetails)
        )
    }

    private val KNOWLEDGE_MAP: Map<String, FestivalShastraDetails> = mapOf(
        "ganesh_chaturthi" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the divine appearance (Prakatya) of Lord Ganesha—Vighnaharta (Remover of Obstacles) and Siddhidata (Bestower of Wisdom & Auspicious Beginnings)—on Bhadrapada Shukla Chaturthi.",
            puranicLegend = "According to the Shiva Purana (Rudra Samhita) and Skanda Purana, Goddess Parvati sculpted Ganesha from fragrant turmeric paste (Ubtan) to guard her threshold, and Lord Shiva later crowned Him as Ganadhyaksha—the foremost leader of the celestial hosts worshipped before every sacred undertaking.",
            presidingDeity = "Lord Siddhi Vinayaka (Maha Ganapati) with Riddhi & Siddhi",
            sacredMantraSanskrit = "वक्रतुण्ड महाकाय सूर्यकोटि समप्रभ । निर्विघ्नं कुरु मे देव सर्वकार्येषु सर्वदा ॥ ॐ गं गणपतये नमः ॥",
            sacredMantraTransliteration = "Vakratunda Mahakaya Suryakoti Samaprabha | Nirvighnam Kuru Me Deva Sarvakaryeshu Sarvada || Om Gam Ganapataye Namaha ||",
            sacredMantraMeaning = "O Lord with the curved trunk, mighty form, and the brilliance of a million suns, please make all my undertakings free of obstacles always.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Chaturthi (4th Lunar Day) • Madhyahna-Vyapini (Midday Birth Window)",
            vratAndNaivedyaGuide = "Offer 21 steamed Ukadiche Modaks (or Boondi Laddus), 21 pairs of 3-bladed Durva grass, red hibiscus (Jaswand), and coconut. Avoid viewing the Moon on Bhadrapada Shukla Chaturthi evening (Chandra Darshan Nishiddha) to prevent Mithya Kalanka.",
            scripturalSources = listOf(
                ScriptureSource("Shiva Purana", "Rudra Samhita, Kumara Khanda, Ch. 13-18", "गणेश प्राकट्य कथा"),
                ScriptureSource("Dharmasindhu", "Parichheda 2, Bhadrapada Shukla Chaturthi Nirnaya", "मध्याह्नव्यापिनी चतुर्थी")
            ),
            editorialProvenance = EditorialReviewProvenance(
                reviewerName = "Acharya V. G. Kulkarni",
                credentials = "Acharya in Shukla Yajurveda & Jyotish, Tilak Maharashtra Vidyapeeth, Pune",
                reviewDate = "2025-08-10",
                lastRevisionDate = "2026-01-15",
                regionalTradition = RegionalTradition.MAHARASHTRIAN,
                guidanceScope = GuidanceScope.UNIVERSALLY_PRESCRIBED,
                alternativePractices = listOf(
                    "Maharashtra: 21 Steamed Ukadiche Modak, 21 Durva pairs & Atharvashirsha Avartan",
                    "South India: Vinayaka Chavithi Kudumulu, Undrallu & Kozhukattai offering",
                    "North India: Boondi Laddu, Roli Tilak, and Evening Aarti"
                )
            )
        ),
        "diwali_lakshmi_puja" to FestivalShastraDetails(
            whyCelebrated = "Honors the arrival of Goddess Mahalakshmi into pure, illuminated homes on Kartika/Ashwina Amavasya, and celebrates Bhagavan Sri Rama's joyous return to Ayodhya after 14 years of exile.",
            puranicLegend = "In the Padma Purana and Skanda Purana, Goddess Mahalakshmi emerged from the churning of the cosmic ocean (Samudra Manthan) on Amavasya and chose Lord Vishnu as Her consort. Lighting earthen ghee diyas dispels both outer darkness and inner ignorance (Avidya).",
            presidingDeity = "Goddess Mahalakshmi, Lord Ganesha, Goddess Saraswati & Lord Kubera",
            sacredMantraSanskrit = "ॐ श्रीं ह्रीं श्रीं कमले कमलालये प्रसीद प्रसीद श्रीं ह्रीं श्रीं ॐ महालक्ष्म्यै नमः ॥",
            sacredMantraTransliteration = "Om Shreem Hreem Shreem Kamale Kamalalaye Praseeda Praseeda Shreem Hreem Shreem Om Mahalakshmyai Namaha ||",
            sacredMantraMeaning = "Salutations to Goddess Mahalakshmi who abides on the sacred lotus; may She grace our home with auspiciousness, peace, and righteous prosperity.",
            shastraTithiRule = "Ashwina (Amanta) / Kartika (Purnimanta) • Krishna Paksha • Amavasya (15th Tithi) • Pradosh & Vrishabha Sthira Lagna",
            vratAndNaivedyaGuide = "Observe daytime sattvik fast until evening Pradosh Lakshmi Puja. Offer Kheel (puffed paddy), Batasha, lotus flowers, coriander seeds, and pure ghee sweets.",
            scripturalSources = listOf(
                ScriptureSource("Padma Purana", "Uttara Khanda, Ch. 122, Verses 15-32", "लक्ष्मी प्राकट्य एवं दीपदान विधि"),
                ScriptureSource("Nirnayasindhu", "Dvitiya Parichheda, Kartika Krishna Amavasya", "प्रदोषव्यापिनी दीपदान एवं लक्ष्मीपूजा")
            ),
            editorialProvenance = EditorialReviewProvenance(
                reviewerName = "Acharya Ramanand Sharma",
                credentials = "Vyakarana & Jyotish Acharya, Sampurnanand Sanskrit University, Varanasi",
                reviewDate = "2025-09-02",
                lastRevisionDate = "2026-01-18",
                regionalTradition = RegionalTradition.NORTH_INDIAN,
                guidanceScope = GuidanceScope.UNIVERSALLY_PRESCRIBED,
                alternativePractices = listOf(
                    "North India: Kheel-Batasha & Chopda/Bahi-Khata Pujan during Pradosh & Vrishabha Sthir Lagna",
                    "Bengal: Shyama Kali Puja observed at midnight on same Amavasya",
                    "South India: Deepavali celebrated with early dawn oil bath (Ganga Snanam) on Naraka Chaturdashi"
                )
            )
        ),
        "bhai_dooj" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the sacred lifelong bond of love, protection, and mutual blessing between brothers and sisters on the second lunar day (Dwitiya) after Diwali.",
            puranicLegend = "According to the Bhavishyottara Purana and Skanda Purana, Yama Deva (the Lord of Dharma) visited his beloved sister Yamuna Devi on Kartika Shukla Dwitiya. Yamuna welcomed him with a ceremonial Tilak, aarti, and a home-cooked feast. Pleased, Yama proclaimed that any brother who receives Tilak and dines at his sister's home on Yama Dwitiya is blessed with longevity, health, and fearlessness.",
            presidingDeity = "Yama Deva (Dharmaraja), Goddess Yamuna, Lord Ganesha & Sri Vishnu",
            sacredMantraSanskrit = "भ्रातुस्तवाग्रजाताहं भुङ्क्ष्व भक्तमिदं शुभम् । प्रीतये यमराजस्य यमुनाया विशेषतः ॥",
            sacredMantraTransliteration = "Bhratustavagrajataham Bhunkshva Bhaktamidam Shubham | Preetaye Yamarajasya Yamunaya Visheshatah || (Bengal: Bhaier kopale dilam phonta, Jomer duare porlo kanta)",
            sacredMantraMeaning = "For the delight of Yamaraja and Goddess Yamuna, I apply this auspicious Tilak and serve this sacred meal for my brother's long life and protection.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Dwitiya (2nd Lunar Day) • Aparahna-Vyapini (Early Afternoon Window)",
            vratAndNaivedyaGuide = "Sisters observe a morning fast until applying Roli-Chandan-Dahi Tilak (or Bhai Phonta) on their brother's forehead during Aparahna Kaal, offering dry coconut (Khopra) and sharing Bhagini Hastha Bhojanam (a festive meal).",
            scripturalSources = listOf(
                ScriptureSource("Bhavishyottara Purana", "Kartika Shukla Dwitiya Yamadvitiya Mahatmya, Verses 1-28", "यमद्वितीया भगिनीहस्तभोजनम्")
            ),
            editorialProvenance = EditorialReviewProvenance(
                reviewerName = "Dr. Vedashree Ramanathan",
                credentials = "Ph.D. in Sanskrit Literature & Dharmashastra, University of Madras",
                reviewDate = "2025-09-12",
                lastRevisionDate = "2026-01-20",
                regionalTradition = RegionalTradition.NORTH_INDIAN,
                guidanceScope = GuidanceScope.GENERAL_FAMILY_PRACTICE,
                alternativePractices = listOf(
                    "North India: Roli-Akshat Tilak with Khopra (dry coconut) and sweets",
                    "Bengal: Bhai Phonta with Chandan, Kajal, Dahi and sacred chant",
                    "Maharashtra: Bhau Beej aarti and Basundi Puri"
                )
            )
        ),
        "dhanteras" to FestivalShastraDetails(
            whyCelebrated = "Initiates the 5-day Deepavali Mahotsav on Trayodashi by worshipping Lord Dhanvantari for holistic health (Arogya) and Goddess Lakshmi & Kubera for prosperity.",
            puranicLegend = "During Samudra Manthan, Lord Dhanvantari—the divine physician of the Devas and progenitor of Ayurveda—emerged holding the golden Kalash of Amrita (nectar of immortality). On the same evening, lighting the South-facing Yama Deepam honors Yama Deva to protect the family from untimely misfortune.",
            presidingDeity = "Lord Dhanvantari, Goddess Mahalakshmi, Lord Kubera & Yama Deva",
            sacredMantraSanskrit = "ॐ नमो भगवते वासुदेवाय धन्वन्तरये अमृतकलशहस्ताय सर्वमयविनाशनाय त्रैलोक्यनाथाय श्री महाविष्णवे नमः ॥",
            sacredMantraTransliteration = "Om Namo Bhagavate Vasudevaya Dhanvantaraye Amritakalasha Hastaya Sarvamaya Vinashanaya Trailokyanathaya Sri Mahavishnave Namaha ||",
            sacredMantraMeaning = "Salutations to Lord Dhanvantari, who holds the pot of immortal nectar and removes all ailments across the three worlds.",
            shastraTithiRule = "Ashwina (Amanta) / Kartika (Purnimanta) • Krishna Paksha • Trayodashi (13th Tithi) • Pradosh Kaal",
            vratAndNaivedyaGuide = "Purchase brass, copper, silver, or gold utensils/coins (never bring them home empty—fill with rice or sweets), offer whole coriander seeds (Sabut Dhaniya) and jaggery, and light 13 ghee diyas plus 1 sesame-oil Yama Deepam facing South."
        ),
        "govardhan_puja" to FestivalShastraDetails(
            whyCelebrated = "Honors Bhagavan Sri Krishna lifting Govardhan Hill to protect the residents of Braj and teaches deep ecological gratitude toward Prakriti (Nature), cattle, and harvest abundance. Also celebrated as Bali Pratipada (Diwali Padwa) and Bestu Varas (Gujarati New Year).",
            puranicLegend = "In Srimad Bhagavatam (Canto 10), young Krishna guided the people of Vrindavan to worship Mount Govardhan—which nourishes the cows, forests, and streams—rather than appeasing prideful Indra. When torrential rains fell for seven days, Krishna effortlessly held up Govardhan Giriraj on the little finger of His left hand.",
            presidingDeity = "Bhagavan Sri Govardhanadhari Krishna, Gau-Mata & King Mahabali (Bali Pratipada)",
            sacredMantraSanskrit = "गोवर्धन धराधार गोकुलत्राणकारक । विष्णुबाहुकृतोच्छ्राय गवां कोटिप्रदो भव ॥",
            sacredMantraTransliteration = "Govardhana Dharadhara Gokulatranakaraka | Vishnubahukritochchhraya Gavam Kotiprado Bhava ||",
            sacredMantraMeaning = "O Govardhan Hill, supporter of the earth and protector of Gokul, raised aloft by the arm of Lord Vishnu, bless us with boundless nourishment and peace.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Pratipada (1st Lunar Day — Swayam-Siddha Muhurat) • Morning & Pradosh",
            vratAndNaivedyaGuide = "Prepare Annakut ('Mountain of Food' / 56 Bhog seasonal vegetable medley, bajra khichdi, kadhi, and sweets) and perform 7 clockwise Parikramas of the symbolic Govardhan Giriraj."
        ),
        "karwa_chauth" to FestivalShastraDetails(
            whyCelebrated = "Observed on Kartika Krishna Chaturthi (Sankashti Karak Chaturthi) by married women praying for the long life, safety, and well-being of their spouses and enduring marital harmony.",
            puranicLegend = "In the Vamana Purana and Mahabharata, Queen Veeravati and Draupadi observed this sacred Chaturthi Vrat. The name 'Karwa' refers both to the earthen spouted pot symbolizing prosperity and to the devoted woman Karwa whose steadfastness moved Yama Deva.",
            presidingDeity = "Chauth Mata (Goddess Parvati / Gaura), Lord Shiva, Lord Kartikeya, Lord Ganesha & Chandra Deva (Moon God)",
            sacredMantraSanskrit = "देहि सौभाग्यमारोग्यं देहि मे परमं सुखम् । रूपं देहि जयं देहि यशो देहि द्विषो जहि ॥ ॐ सोमाय नमः ॥",
            sacredMantraTransliteration = "Dehi Saubhagyam Arogyam Dehi Me Paramam Sukham | Rupam Dehi Jayam Dehi Yasho Dehi Dvisho Jahi || Om Somaya Namaha ||",
            sacredMantraMeaning = "Bestow auspiciousness, good health, and supreme happiness upon our household; salutations to Chandra Deva.",
            shastraTithiRule = "Ashwina (Amanta) / Kartika (Purnimanta) • Krishna Paksha • Chaturthi (4th Tithi) • Pradosh Puja & Chandrodaya (Moonrise)",
            vratAndNaivedyaGuide = "Partake of pre-dawn Sargi before sunrise, observe Nirjala Vrat through the day, listen to the Karwa Chauth Katha during Pradosh Kaal, and break the fast only after offering Arghya to the rising Moon through a sieve (Chalni)."
        ),
        "makar_sankranti" to FestivalShastraDetails(
            whyCelebrated = "Marks the astronomical solar ingress of Surya Deva into Makara Rashi (Capricorn), beginning Uttarayan—the auspicious six-month northward journey of the Sun—and celebrating the winter harvest across India (Pongal, Uttarayan, Magh Bihu, Maghi).",
            puranicLegend = "According to the Mahabharata and Surya Siddhanta, Bhishma Pitamaha waited on his bed of arrows for the dawn of Uttarayan (Makar Sankranti) to leave his mortal body, as Uttarayan is the daytime of the Devas and the gateway to light and liberation.",
            presidingDeity = "Surya Narayana (Sun God), Goddess Sankranti & Indra/Prakriti",
            sacredMantraSanskrit = "ॐ एहि सूर्य सहस्रांशो तेजोराशे जगत्पते । अनुकम्पय मां भक्त्या गृहाणार्घ्यं दिवाकर ॥",
            sacredMantraTransliteration = "Om Ehi Surya Sahasramsho Tejorashe Jagatpate | Anukampaya Mam Bhaktya Grihanarghyam Divakara ||",
            sacredMantraMeaning = "O Surya Deva of a thousand rays, ocean of radiance and Lord of the universe, have compassion on me and accept this sacred water offering (Arghya).",
            shastraTithiRule = "Solar Makara Sankramana (Jan 14/15) • Sankranti Punya Kaal & Maha Punya Kaal from Sunrise",
            vratAndNaivedyaGuide = "Bathe with black sesame seeds (Til), offer Surya Arghya, cook overflowing sweet Pongal (Tamil Nadu) or Urad Dal Khichdi (North India), and share Til-Gul (Sesame & Jaggery) sweets and charity (Gupt Daan)."
        ),
        "gudi_padwa_ugadi" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the Lunisolar New Year (Chaitra Shukla Pratipada)—the day Lord Brahma initiated the creation of the cosmos (Srishti Aarambha) and the commencement of the new Samvatsara.",
            puranicLegend = "In the Brahma Purana and Shatapatha Brahmana, Lord Brahma created time, the planets, and the Vedic calendar at sunrise on Chaitra Shukla Pratipada. It also marks the coronation of Sri Rama in Ayodhya and Emperor Shalivahana's historic victory initiating the Shalivahana Shaka era.",
            presidingDeity = "Lord Brahma, Bhagavan Sri Vishnu (Kala Purusha) & Adi Shakti (Chaitra Navratri)",
            sacredMantraSanskrit = "ब्रह्मध्वज नमस्तेऽस्तु सर्वाभीष्टफलप्रद । प्राप्तेऽस्मिन्वत्सरे नित्यं मद्गृहे मङ्गलं कुरु ॥",
            sacredMantraTransliteration = "Brahmadhvaja Namaste'stu Sarvabhishta Phalaprada | Prapte'smin Vatsare Nityam Madgrihe Mangalam Kuru ||",
            sacredMantraMeaning = "Salutations to the sacred Brahmadhvaja (Gudi), bestower of all noble wishes; as this new year begins, bring constant auspiciousness to our home.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Pratipada (1st Lunar Day — Swayam-Siddha Muhurat) • Udaya-Vyapini Sunrise",
            vratAndNaivedyaGuide = "Perform Abhyanga oil bath, hoist the Gudi (Maharashtra), partake in six-taste Ugadi Pachadi / Bevu-Bella (Neem flowers, jaggery, raw mango, tamarind, pepper, salt) and Puran Poli, and listen to Panchanga Sravanam."
        ),
        "durga_ashtami" to FestivalShastraDetails(
            whyCelebrated = "The culmination of Sharad Durga Puja honoring Goddess Durga's fierce manifestation as Chamunda at the exact junction (Sandhi Kaal) of Ashtami and Navami to vanquish the demons Chanda and Munda.",
            puranicLegend = "In the Devi Mahatmya (Markandeya Purana), during the battle against Mahishasura's generals Chanda and Munda, Goddess Ambika's brow darkened and Kalaratri/Chamunda sprang forth at the 48-minute cusp of Ashtami ending and Navami beginning. Lord Rama also worshipped Goddess Durga with 108 blue lotuses at this exact Sandhi Kaal.",
            presidingDeity = "Maa Mahishasuramardini Durga & Devi Chamunda",
            sacredMantraSanskrit = "सर्वमङ्गलमाङ्गल्ये शिवे सर्वार्थसाधिके । शरण्ये त्र्यम्बके गौरि नारायणि नमोऽस्तु ते ॥",
            sacredMantraTransliteration = "Sarva Mangala Mangalye Shive Sarvartha Sadhike | Sharanye Tryambake Gauri Narayani Namostu Te ||",
            sacredMantraMeaning = "Salutations to You, O Narayani, Gauri, the auspiciousness in all that is auspicious, who fulfills all righteous aspirations and is our supreme refuge.",
            shastraTithiRule = "Ashwina Masa • Shukla Paksha • Maha Ashtami (8th Tithi) & 48-Minute Ashtami-Navami Sandhi Kaal",
            vratAndNaivedyaGuide = "Offer morning Pushpanjali on an empty stomach, perform Kumari / Kanjak Puja (offering Halwa, Puri, and Kala Chana to young girls), and light 108 diyas with 108 lotus flowers during Sandhi Puja."
        ),
        "navratri_ghatasthapana" to FestivalShastraDetails(
            whyCelebrated = "Initiates the nine sacred nights of Sharad Navratri dedicated to the Nava-Durga (nine forms of Adi Shakti) by consecrating the sacred Kalash (Ghatasthapana) and sowing barley grains representing cosmic fertility.",
            puranicLegend = "According to the Devi Bhagavata Purana, Goddess Durga battled Mahishasura across nine days and nights, embodying Shakti (strength), Lakshmi (compassion & abundance), and Saraswati (wisdom). The earthen pot (Ghata/Garbo) symbolizes the universe illuminated by the inner soul (Akhand Jyot).",
            presidingDeity = "Goddess Adi Parashakti & Nava-Durga (starting with Maa Shailaputri)",
            sacredMantraSanskrit = "या देवी सर्वभूतेषु शक्तिरूपेण संस्थिता । नमस्तस्यै नमस्तस्यै नमस्तस्यै नमो नमः ॥",
            sacredMantraTransliteration = "Ya Devi Sarvabhuteshu Shakti-Rupena Samsthita | Namastasyai Namastasyai Namastasyai Namo Namaha ||",
            sacredMantraMeaning = "To the Divine Mother who abides in all beings in the form of cosmic energy (Shakti), salutations to Her again and again.",
            shastraTithiRule = "Ashwina Masa • Shukla Paksha • Pratipada (1st Tithi) • First Trimester of Daylight or Abhijit Muhurat",
            vratAndNaivedyaGuide = "Perform Ghatasthapana in the morning (avoiding Chitra Nakshatra and Vaidhriti Yoga), sow Sapta-Dhanya (barley), maintain the unbroken ghee flame (Akhand Jyot), and observe sattvik Phalahar fast."
        ),
        "maha_shivaratri" to FestivalShastraDetails(
            whyCelebrated = "The 'Great Night of Shiva' on Phalguna/Magha Krishna Chaturdashi, commemorating the divine marriage of Lord Shiva and Goddess Parvati and the appearance of the infinite column of light (Jyotirlinga).",
            puranicLegend = "In the Shiva Purana (Koti Rudra Samhita), on this moonless Chaturdashi night, Lord Shiva manifested as an endless pillar of fire (Lingodbhava) to teach humility to Brahma and Vishnu, and also drank the Halahala poison during Samudra Manthan to save all creation.",
            presidingDeity = "Lord सदाशिव (Mahadeva / Jyotirlinga) & Goddess Parvati",
            sacredMantraSanskrit = "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम् । उर्वारुकमिव बन्धनान्मृत्योर्मुक्षीय माऽमृतात् ॥ ॐ नमः शिवाय ॥",
            sacredMantraTransliteration = "Om Tryambakam Yajamahe Sugandhim Pushtivardhanam | Urvarukamiva Bandhanan Mrityor Mukshiya Ma'mritat || Om Namah Shivaya ||",
            sacredMantraMeaning = "We worship the Three-Eyed Lord Shiva who nourishes all beings; may He liberate us from fear and mortality unto immortality.",
            shastraTithiRule = "Magha (Amanta) / Phalguna (Purnimanta) • Krishna Paksha • Chaturdashi (14th Tithi) • Nishita-Vyapini Midnight",
            vratAndNaivedyaGuide = "Observe daytime fast and night-long Jagran across the 4 Pahars, performing Rudrabhishek with water, milk, curd, honey, ghee, Bilva leaves (3-leaf clusters), and Vibhuti."
        ),
        "krishna_janmashtami" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the midnight incarnation of Bhagavan Sri Krishna—the eighth Poorna Avatar of Lord Vishnu—on Bhadrapada Krishna Ashtami under Rohini Nakshatra.",
            puranicLegend = "As narrated in the Srimad Bhagavatam (10th Canto), Sri Krishna appeared at midnight in the prison cell of Mathura to Devaki and Vasudeva to liberate the world from Adharma and impart the timeless wisdom of the Bhagavad Gita.",
            presidingDeity = "Bhagavan Sri Balakrishna (Laddu Gopal / Yogeshwara Krishna)",
            sacredMantraSanskrit = "वसुदेवसुतं देवं कंसचाणूरमर्दनम् । देवकीपरमानन्दं कृष्णं वन्दे जगद्गुरुम् ॥ ॐ नमो भगवते वासुदेवाय ॥",
            sacredMantraTransliteration = "Vasudeva Sutam Devam Kamsa Chanura Mardanam | Devaki Paramanandam Krishnam Vande Jagadgurum || Om Namo Bhagavate Vasudevaya ||",
            sacredMantraMeaning = "I bow to Sri Krishna, the divine son of Vasudeva, the supreme joy of Mother Devaki, and the universal teacher of the world.",
            shastraTithiRule = "Shravana (Amanta) / Bhadrapada (Purnimanta) • Krishna Paksha • Ashtami (8th Tithi) • Nishita Midnight + Rohini Nakshatra",
            vratAndNaivedyaGuide = "Fast until midnight Janmotsav Aarti, bathe Bal Gopal with Panchamrit at 12:00 AM, rock the decorated cradle (Jhula), and offer Makhan-Mishri (white butter & rock sugar), Dhaniya Panjiri, and Tulsi leaves."
        ),
        "varalakshmi_vratam" to FestivalShastraDetails(
            whyCelebrated = "Observed on the Friday before Shravana Purnima to worship Goddess Varalakshmi (the boon-bestowing form of Mahalakshmi), equivalent to worshipping all Ashta-Lakshmis simultaneously.",
            puranicLegend = "In the Bhavishyottara Purana, Lord Shiva narrated to Goddess Parvati how a devoted woman named Charumati in Kundinapura was blessed in a dream by Goddess Varalakshmi to perform this Vrat on the Friday of Shravana Shukla Paksha, bringing peace, health, and abundance to her entire community.",
            presidingDeity = "Goddess Sri Varalakshmi (Ashta-Lakshmi Swarupa)",
            sacredMantraSanskrit = "पद्मासने पद्मकरां सर्वलोकैकपूजिताम् । नारायणप्रियां देवीं सुप्रीतां वरदां भजे ॥",
            sacredMantraTransliteration = "Padmasane Padmakaram Sarvalokaika Pujitam | Narayana Priyam Devim Supritam Varadam Bhaje ||",
            sacredMantraMeaning = "I worship the boon-bestowing Goddess Varalakshmi, seated on a lotus, holding lotuses, beloved of Lord Narayana, and revered by all worlds.",
            shastraTithiRule = "Shravana Masa • Shukla Paksha • Friday preceding Shravana Purnima • Morning Simha/Vrishchika Lagna",
            vratAndNaivedyaGuide = "Decorate the sacred Kalash with a silver/gold Ammavaru face mask, tie the 9-knot turmeric thread (Charadu/Toram) on the right wrist, offer sweet & savory Kozhukattai, Sundal, Payasam, and Vada, and give Tamboolam to women guests."
        ),
        "holi_dhulandi" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the triumph of unshakeable devotion (Bhakt Prahlad) over tyranny (Hiranyakashipu & Holika), the arrival of Vasant Ritu (spring), and the divine swing festival (Dol Purnima) of Radha and Krishna.",
            puranicLegend = "In the Narada Purana and Srimad Bhagavatam (Canto 7), young Prahlad remained absorbed in chanting Lord Narayana's name even when seated in blazing fire on the lap of Holika, whose fireproof cloak flew onto Prahlad instead. In Bengal and Odisha, Phalguna Purnima is also the birth anniversary of Sri Chaitanya Mahaprabhu.",
            presidingDeity = "Lord Lakshmi-Narasimha, Bhakt Prahlad & Sri Radha-Govinda (Dol Govinda)",
            sacredMantraSanskrit = "वन्दितासि सुरेन्द्रेण ब्रह्मणा शङ्करेण च । अतस्त्वं पाहि मां देवि भूतिभूतिप्रदा भव ॥",
            sacredMantraTransliteration = "Vanditasi Surendrena Brahmana Shankarena Cha | Atastvam Pahi Mam Devi Bhuti-Bhutiprada Bhava ||",
            sacredMantraMeaning = "Revered by Indra, Brahma, and Shiva, may this sacred Holika Agni protect our family and bestow health and auspiciousness.",
            shastraTithiRule = "Phalguna Masa • Shukla Paksha • Purnima (15th Tithi) • Pradosh Kaal after Bhadra Mukha/Puchha",
            vratAndNaivedyaGuide = "Perform Holika Dahan only after Bhadra Kaal ends during Pradosh; offer roasted wheat ears (Baali), gram, and coconut to the sacred fire, and offer herbal Gulal and Gujiya/Malpua to Radha-Krishna the next morning."
        ),
        "raksha_bandhan" to FestivalShastraDetails(
            whyCelebrated = "Celebrated on Shravana Purnima to honor the protective bond between sisters and brothers (Raksha Bandhan), the renewal of Vedic study and sacred thread (Avani Avittam / Upakarma), and gratitude to the ocean god Varuna (Narali Purnima).",
            puranicLegend = "In the Bhavishya Purana, Indrani (Sachi) tied a consecrated Raksha Sutra on Lord Indra's wrist before battle, and in the Mahabharata, Draupadi tore a strip of her silk saree to bandage Sri Krishna's finger, earning His eternal vow of protection.",
            presidingDeity = "Lord Vishnu, Varuna Deva (Narali Purnima) & the Saptarishis (Upakarma)",
            sacredMantraSanskrit = "येन बद्धो बली राजा दानवेन्द्रो महाबलः । तेन त्वामनुबध्नामि रक्षे मा चल मा चल ॥",
            sacredMantraTransliteration = "Yena Baddho Bali Raja Danavendro Mahabalah | Tena Tvam Anubadhnami Rakshe Ma Chala Ma Chala ||",
            sacredMantraMeaning = "With that same sacred bond of righteousness by which the mighty King Bali was bound, I tie this Raksha thread upon you; O protection, never waver, never fail!",
            shastraTithiRule = "Shravana Masa • Shukla Paksha • Purnima (15th Tithi) • Aparahna / Pradosh (Strictly Bhadra-Free Window)",
            vratAndNaivedyaGuide = "Never tie Rakhi during Bhadra Kaal. Apply Roli-Akshat tilak, tie the Raksha Sutra during Aparahna or Bhadra-free morning/evening, and offer coconut sweets (Narali Pak / Kaju Katli)."
        ),
        "rama_navami" to FestivalShastraDetails(
            whyCelebrated = "Commemorates the divine Janmotsav of Maryada Purushottama Bhagavan Sri Rama—the seventh avatar of Lord Vishnu and embodiment of Dharma, truth, and compassion—on Chaitra Shukla Navami.",
            puranicLegend = "As chronicled by Maharshi Valmiki in the Bala Kanda of the Ramayana, Sri Rama was born to King Dasharatha and Queen Kaushalya in Ayodhya at exact midday (Abhijit/Madhyahna Kaal) when five planets stood exalted in Karkata Lagna and Punarvasu Nakshatra.",
            presidingDeity = "Bhagavan Sri Ramachandra with Mata Sita, Lakshmana & Sri Hanuman (Rama Darbar)",
            sacredMantraSanskrit = "श्रीराम राम रामेति रमे रामे मनोरमे । सहस्रनाम तत्तुल्यं रामनाम वरानने ॥",
            sacredMantraTransliteration = "Sri Rama Rama Rameti Rame Rame Manorame | Sahasranama Tattulyam Rama Nama Varanane ||",
            sacredMantraMeaning = "Chanting the sacred name 'Rama, Rama, Rama' delights the mind and equals the spiritual merit of reciting the entire Vishnu Sahasranama.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Navami (9th Tithi) • Madhyahna-Vyapini (Midday Karkata Lagna)",
            vratAndNaivedyaGuide = "Perform Ram Lalla Panchamrit Abhishek at 12:00 PM noon, rock the cradle singing 'Bhaye Pragata Kripala', and offer cooling Jaggery-Ginger Panakam, Kosambari (soaked moong dal), and Dhaniya Panjiri."
        ),
        "vijayadashami_dussehra" to FestivalShastraDetails(
            whyCelebrated = "Marks the supreme victory of Dharma over Adharma—celebrating both Maa Durga's triumph over Mahishasura and Bhagavan Sri Rama's victory over Ravana—making it one of the 3.5 Swayam-Siddha Muhurats of the Hindu year.",
            puranicLegend = "In the Devi Bhagavata and Ramayana, Ashwina Shukla Dashami during Vijaya Muhurat is the hour when Sri Rama invoked Goddess Aparajita and worshipped the sacred Shami tree before marching to victory, and when the Pandavas retrieved their divine weapons from the Shami tree after their year incognito.",
            presidingDeity = "Goddess Durga (Aparajita Devi), Bhagavan Sri Rama & Goddess Saraswati (Vidyarambham)",
            sacredMantraSanskrit = "शमी शमयते पापं शमी शत्रुविनाशिनी । अर्जुनेन धनुर्धारी रामस्य प्रियदर्शनी ॥",
            sacredMantraTransliteration = "Shami Shamayate Papam Shami Shatru-Vinashini | Arjunena Dhanurdhari Ramasya Priyadarshini ||",
            sacredMantraMeaning = "The sacred Shami tree pacifies all transgressions, removes obstacles, sheltered Arjuna's bow, and brought auspicious sight to Lord Rama.",
            shastraTithiRule = "Ashwina Masa • Shukla Paksha • Dashami (10th Tithi) • Aparahna Kaal & Vijaya Muhurat",
            vratAndNaivedyaGuide = "Perform Ayudha/Shastra Puja (honoring books, tools, instruments, and vehicles), Vidyarambham (children writing first letters on rice), Shami/Apta leaf exchange ('Sona'), and Bijoya Dashami Sindoor Khela."
        ),
        "chhath_puja" to FestivalShastraDetails(
            whyCelebrated = "A four-day Vedic Mahaparv dedicated to Surya Deva (both the setting and rising Sun) and Chhathi Maiya (Shashthi Devi / Usha), expressing reverence for solar life-energy, clean water, and family well-being.",
            puranicLegend = "Mentioned in the Rigveda Surya Suktam and Bhavishya Purana, Draupadi and Maharshi Dhaumya performed Surya Shashthi Vrat for the Pandavas' restoration, and Sita Mata performed Chhath Puja on the banks of the Ganga in Munger after returning to Ayodhya.",
            presidingDeity = "Bhagavan Surya Narayana (Pratyaksha Devata) & Chhathi Maiya (Devasena / Shashthi Devi)",
            sacredMantraSanskrit = "ॐ घृणिः सूर्याय नमः । ॐ आदित्याय विद्महे दिवाकराय धीमहि तन्नः सूर्यः प्रचोदयात् ॥",
            sacredMantraTransliteration = "Om Ghrinih Suryaya Namaha | Om Adityaya Vidmahe Divakaraya Dhimahi Tannah Suryah Prachodayat ||",
            sacredMantraMeaning = "Salutations to the radiant Sun God; we meditate upon the divine Maker of the Day—may Lord Surya illuminate our intellect.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Chaturthi (Nahay Khay) to Saptami • Shashthi Sunset Arghya & Saptami Sunrise Arghya",
            vratAndNaivedyaGuide = "Day 1: Nahay Khay (bottle gourd & chana dal); Day 2: Kharna (jaggery Rasiya-Kheer & roti); Day 3: 36-hour Nirjala Vrat & Sunset Sandhya Arghya with Thekua, sugarcane & fruits in bamboo Soop; Day 4: Dawn Usha Arghya."
        ),
        "akshaya_tritiya" to FestivalShastraDetails(
            whyCelebrated = "'Akshaya' means imperishable. Celebrated on Vaishakha Shukla Tritiya as a Swayam-Siddha Abujh Muhurat when any prayer, japa, charity (Daan), or noble venture yields unending merit.",
            puranicLegend = "According to the Matsya and Bhavishya Puranas, on Vaishakha Shukla Tritiya: Satya Yuga and Treta Yuga began, Lord Parashurama incarnated, Mother Ganga descended to earth, Maharshi Vyasa and Lord Ganesha began writing the Mahabharata, and Sudama visited Sri Krishna with a handful of puffed rice.",
            presidingDeity = "Lord Lakshmi-Narayana, Lord Parashurama & Lord Kubera",
            sacredMantraSanskrit = "अङ्गं हरेः पुलकभूषणमाश्रयन्ती भृङ्गाङ्गनेव मुकुलाभरणं तमालम् । अङ्गीकृताखिलविभूतिरपाङ्गलीला माङ्गल्यदास्तु मम मङ्गलदेवतायाः ॥",
            sacredMantraTransliteration = "Angam Hareh Pulaka-Bhushanam Ashrayanti Bhringanganeva Mukulabharanam Tamalam | Angikritakhila-Vibhutir Apanga-Lila Mangalyadastu Mama Mangala-Devatayah || (Kanakadhara Stotram)",
            sacredMantraMeaning = "May the gracious glance of the Goddess of Auspiciousness (Mahalakshmi), who rests upon Lord Hari, bestow imperishable blessings upon us.",
            shastraTithiRule = "Vaishakha Masa • Shukla Paksha • Tritiya (3rd Tithi) • Purvahna / Sunrise Udaya-Vyapini",
            vratAndNaivedyaGuide = "Perform Chandan Puja of Lord Vishnu, offer barley (Jau), Sattu, soaked chana dal, and ripe mangoes (Aamras), and perform Udaka Kumbha Daan (donating cool water pots, fans, and food in summer)."
        ),
        "vasant_panchami" to FestivalShastraDetails(
            whyCelebrated = "Marks the divine appearance day of Goddess Saraswati—presiding deity of wisdom, language, music, and creative arts—and welcomes Vasant Ritu (the spring season).",
            puranicLegend = "In the Matsya and Brahmavaivarta Puranas, when Lord Brahma created the universe, it was silent and still until Goddess Saraswati emerged from His speech on Magha Shukla Panchami, strumming Her Veena to bestow voice, melody, and knowledge upon all creation.",
            presidingDeity = "Goddess Mahasaraswati (Vagdevi / Sharada)",
            sacredMantraSanskrit = "या कुन्देन्दुतुषारहारधवला या शुभ्रवस्त्रावृता या वीणावरदण्डमण्डितकरा या श्वेतपद्मासना ॥",
            sacredMantraTransliteration = "Ya Kundendu Tusharahara Dhavala Ya Shubhra Vastravrita | Ya Veena Varadanda Manditakara Ya Shveta Padmasana ||",
            sacredMantraMeaning = "Salutations to Goddess Saraswati, radiant as jasmine, the moon, and snow, dressed in pure white, holding the sacred Veena, and seated on a white lotus.",
            shastraTithiRule = "Magha Masa • Shukla Paksha • Panchami (5th Tithi) • Purvahna-Vyapini (Forenoon Window between Sunrise & Midday)",
            vratAndNaivedyaGuide = "Wear basanti yellow attire, place books, pens, and musical instruments before Maa Saraswati, perform Hatey Khori / Aksharabhyasam for young children, and offer yellow saffron sweet rice, mango blossoms, and Topa Kool (jujube)."
        ),
        "hanuman_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the birth anniversary (Janmotsav) of Chiranjeevi Sri Hanuman—the embodiment of fearless strength, humility, wisdom, and unwavering devotion to Bhagavan Sri Rama—on Chaitra Purnima.",
            puranicLegend = "In the Valmiki Ramayana and Parashara Samhita, Sri Hanuman was born to Mata Anjana and Kesari with the blessings of Vayu Deva and Rudra (Shiva's 11th Rudra avatar) at sunrise on Chaitra Shukla Purnima to serve Sri Rama's mission.",
            presidingDeity = "Bhagavan Sri Hanuman (Anjaneya / Bajrangbali)",
            sacredMantraSanskrit = "मनोजवं मारुततुल्यवेगं जितेन्द्रियं बुद्धिमतां वरिष्ठम् । वातात्मजं वानरयूथमुख्यं श्रीरामदूतं शरणं प्रपद्ये ॥",
            sacredMantraTransliteration = "Manojavam Marutatulyavegam Jitendriyam Buddhimatam Varishtham | Vatatmajam Vanarayuthamukhyam Sriramadutam Sharanam Prapadye ||",
            sacredMantraMeaning = "I take refuge in Sri Hanuman, the messenger of Lord Rama, swift as thought and the wind, master of the senses, and foremost among the wise.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Purnima (15th Tithi) • Sunrise & Abhijit Muhurat",
            vratAndNaivedyaGuide = "Offer orange Sindoor-Chameli Chola, a garland of Tulsi leaves inscribed with 'Sri Rama', recite Hanuman Chalisa 7 or 11 times, and offer Boondi Laddus, roasted chana-jaggery, and bananas."
        ),
        "guru_purnima" to FestivalShastraDetails(
            whyCelebrated = "Honors Maharshi Veda Vyasa on his birth anniversary (Ashadha Purnima) and expresses reverence to all spiritual and academic Gurus who remove the darkness of ignorance.",
            puranicLegend = "On Ashadha Shukla Purnima, Maharshi Krishna Dvaipayana Vyasa—who classified the four Vedas, authored the 18 Puranas and the Mahabharata—was born to Sage Parashara and Satyavati, and Lord Shiva also transmitted yoga wisdom as Adi Guru to the Saptarishis.",
            presidingDeity = "Lord Dakshinamurti, Maharshi Veda Vyasa & the Guru Parampara",
            sacredMantraSanskrit = "गुरुर्ब्रह्मा गुरुर्विष्णुर्गुरुर्देवो महेश्वरः । गुरुरेव परं ब्रह्म तस्मै श्रीगुरवे नमः ॥",
            sacredMantraTransliteration = "Gurur Brahma Gurur Vishnuh Gurur Devo Maheshvarah | Gurur Eva Param Brahma Tasmai Sri Gurave Namaha ||",
            sacredMantraMeaning = "The Guru is Brahma, Vishnu, and Maheshvara; the Guru is verily the Supreme Brahman—salutations to that revered Guru.",
            shastraTithiRule = "Ashadha Masa • Shukla Paksha • Purnima (15th Tithi) • Morning Vyasa Puja Window",
            vratAndNaivedyaGuide = "Perform Guru Paduka Puja with yellow flowers, sandalwood paste, and Akshat, study sacred scriptures, and offer Kheer and yellow sweets."
        ),
        "nag_panchami" to FestivalShastraDetails(
            whyCelebrated = "Observed on Shravana Shukla Panchami to honor the Ashta-Nagas (cosmic serpent deities led by Ananta Shesha and Vasuki) and foster peaceful ecological reverence during the monsoon.",
            puranicLegend = "In the Mahabharata (Adi Parva) and Garuda Purana, Astika Muni halted King Janamejaya's Sarpa Satra on Shravana Shukla Panchami, establishing peaceful coexistence, and Sri Krishna subdued Kaliya Naga in the Yamuna.",
            presidingDeity = "Ananta, Vasuki, Padma, Mahapadma, Takshaka, Kulira, Karkota & Shankha (Ashta-Nagas)",
            sacredMantraSanskrit = "अनन्तं वासुकिं शेषं पद्मनाभं च कम्बलम् । शङ्खपालं धार्तराष्ट्रं तक्षकं कालियं तथा ॥",
            sacredMantraTransliteration = "Anantam Vasukim Shesham Padmanabham Cha Kambalam | Shankhapalam Dhartarashtram Takshakam Kaliyam Tatha ||",
            sacredMantraMeaning = "Salutations to the nine revered Naga deities—Ananta, Vasuki, Shesha, Padmanabha, Kambala, Shankhapala, Dhartarashtra, Takshaka, and Kaliya.",
            shastraTithiRule = "Shravana Masa • Shukla Paksha • Panchami (5th Tithi) • Pratah (Morning Sunrise Window)",
            vratAndNaivedyaGuide = "Worship symbolic silver/clay/sandalwood Naga figures at the home altar with milk, puffed paddy (Lahi), Durva grass, and turmeric. Avoid digging soil or using an iron tawa; prepare steamed Patoli/modaks."
        ),
        "onam_thiruvonam" to FestivalShastraDetails(
            whyCelebrated = "Kerala's grand harvest festival celebrating the annual homecoming of the beloved, righteous Emperor Mahabali and the Vamana Avatar of Lord Vishnu when Thiruvonam (Shravana) Nakshatra shines in the solar month of Chingam (Simha).",
            puranicLegend = "In Srimad Bhagavatam (Canto 8), King Mahabali ruled with such generosity and equality that no dishonesty or sorrow existed in his realm. When Lord Vishnu tested him as the dwarf Brahmin Vamana asking for three paces of land, Mahabali offered his own head for the third step and was granted the boon to visit his people every year on Thiruvonam.",
            presidingDeity = "Lord Vamana (Thrikkakara Appan / Mahavishnu) & Emperor Mahabali",
            sacredMantraSanskrit = "नमस्तेऽस्तु भगवन् विष्णो वामनाय महात्मने । बलिराज्यप्रदात्रे च त्रैलोक्यपतये नमः ॥ ॐ नमो भगवते वासुदेवाय वामनाय नमः ॥",
            sacredMantraTransliteration = "Namaste'stu Bhagavan Vishno Vamanaya Mahatmane | Balirajya-Pradatre Cha Trailokyapataye Namaha || Om Namo Bhagavate Vasudevaya Vamanaya Namaha ||",
            sacredMantraMeaning = "Salutations to Lord Vamana-Vishnu, Lord of the three worlds who blessed Emperor Mahabali; remembering the golden era of unity and abundance.",
            shastraTithiRule = "Simha (Chingam) Solar Month • Shukla Dwadashi / Shravana (Thiruvonam) Nakshatra • Morning & Abhijit Sadhya",
            vratAndNaivedyaGuide = "Create the 10-ring Athapookkalam floral carpet, light the brass Nilavilakku lamp, anoint Thrikkakara Appan, wear new Kasavu Onakkodi, and serve the 26-dish vegetarian Onam Sadhya with Ada Pradhaman on a banana leaf."
        ),
        "mesha_sankranti_new_year" to FestivalShastraDetails(
            whyCelebrated = "Marks the astronomical Solar New Year (Mesha Sankranti) when the Sun enters Aries (Mesha Rashi), celebrated across India as Puthandu (Tamil Nadu), Vishu (Kerala), Baisakhi (Punjab), Poila Boishakh (Bengal), Bohag Bihu (Assam), and Pana Sankranti (Odisha).",
            puranicLegend = "According to the Surya Siddhanta, Mesha Sankramana marks the vernal solar equilibrium and the start of the solar calendar year. Beholding the auspicious Vishukkani / Puthandu Kani tray at dawn sets an abundant, dharmic tone for the entire year.",
            presidingDeity = "Surya Narayana, Bhagavan Sri Krishna (Guruvayurappan) & Goddess Lakshmi-Ganesha",
            sacredMantraSanskrit = "ॐ मित्राय नमः । ॐ रवये नमः । ॐ सूर्याय नमः । ॐ भानवे नमः ॥",
            sacredMantraTransliteration = "Om Mitraya Namah | Om Ravaye Namah | Om Suryaya Namah | Om Bhanave Namah ||",
            sacredMantraMeaning = "Salutations to the Universal Friend, the Radiant One, the Inspirer of Activity, and the Bestower of Light.",
            shastraTithiRule = "Solar Mesha Sankramana (April 14) • Brahma Muhurat Kani Kanal & Morning Punya Kaal",
            vratAndNaivedyaGuide = "Behold the Kani tray (golden Konnapoo flowers, mirror, rice, fruits, coins, sacred texts) first thing at dawn, prepare sweet-sour-bitter Manga Pachadi, and read the new year's Panchangam."
        ),
        "ratha_yatra" to FestivalShastraDetails(
            whyCelebrated = "Commemorates the annual nine-day chariot journey of Lord Jagannath, elder brother Balabhadra, and sister Subhadra from the main Sri Mandir to Gundicha Temple on Ashadha Shukla Dwitiya.",
            puranicLegend = "In the Skanda Purana (Utkala Khanda), Lord Jagannath—the 'Lord of the Universe'—steps out of His sanctum onto the grand chariots (Nandighosha, Taladhwaja, and Darpadalana) so that every person, regardless of background, can behold Him directly ('Rathastham Vamanam Drishtva Punarjanma Na Vidyate').",
            presidingDeity = "Lord Jagannath, Lord Balabhadra, Devi Subhadra & Sudarshana Chakra",
            sacredMantraSanskrit = "नीलाचलनिवासाय नित्याय परमात्मने । बलभद्रसुभद्राभ्यां जगन्नाथाय ते नमः ॥",
            sacredMantraTransliteration = "Nilachala Nivasaya Nityaya Paramatmane | Balabhadra Subhadrabhyam Jagannathaya Te Namaha ||",
            sacredMantraMeaning = "Salutations to the eternal Supreme Soul residing on Nilachala (Puri), Lord Jagannath along with Balabhadra and Subhadra.",
            shastraTithiRule = "Ashadha Masa • Shukla Paksha • Dwitiya (2nd Tithi — Pushya Nakshatra Yoga) • Morning Pahandi Muhurat",
            vratAndNaivedyaGuide = "Decorate the home altar with Tulsi and lotus flowers, chant the Jagannathashtakam, and offer Khaja, Poda Pitha, Malpua, and sweet Khichdi Mahaprasad."
        ),
        "kartik_purnima" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the most sacred full moon of the Kartika month—Dev Deepawali (the Diwali of the Gods), Tripurari Purnima (Lord Shiva's victory over Tripurasura), Karthigai Deepam, and Odisha's Boita Bandana.",
            puranicLegend = "In the Shiva Purana and Padma Purana, on Kartika Shukla Purnima, Lord Shiva loosed a single blazing arrow to destroy the three impregnable aerial fortresses (Tripura) of the demon Tarakasura's sons, representing the dissolution of ego, illusion, and attachment. The Devas descended to Kashi to light millions of lamps along the Ganga.",
            presidingDeity = "Lord Tripurari Shiva, Lord Damodara-Vishnu (Matsya Avatar) & Lord Kartikeya",
            sacredMantraSanskrit = "कीटाः पतङ्गा मशकाश्च वृक्षा जले स्थले ये निवसन्ति जीवाः । दृष्ट्वा प्रदीपं न च जन्मभाजां भवन्ति नित्यं श्वपचा हि विप्राः ॥",
            sacredMantraTransliteration = "Kitah Patanga Mashakashcha Vriksha Jale Sthale Ye Nivasanti Jivah | Drishtva Pradipam Na Cha Janmabhajam Bhavanti Nityam ||",
            sacredMantraMeaning = "May the sacred Kartika lamp (Deepdaan) illuminate and bless all living beings on earth, water, and sky with spiritual upliftment.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Purnima (15th Tithi — Krittika Nakshatra) • Pradosh Deepdaan Window",
            vratAndNaivedyaGuide = "Take early morning Kartika Snan, perform Shiva-Vishnu Harihara Puja, light 11, 21, or 108 ghee lamps at dusk (Pradosh Deepdaan) near Tulsi and water bodies, and offer Makhana Kheer."
        ),
        "vat_savitri_vrat" to FestivalShastraDetails(
            whyCelebrated = "Honors the legendary wisdom, courage, and devotion of Savitri, who restored her husband Satyavan's life from Yama Deva, and reveres the eternal Banyan tree (Vat Vriksha) as an abode of Brahma, Vishnu, and Shiva.",
            puranicLegend = "In the Mahabharata (Vana Parva), Princess Savitri accompanied Satyavan to the forest on the day foretold for his passing beneath a Banyan tree. When Yama Deva came, Savitri followed him in dialogue so wise and dharmic that Yama granted her boons restoring her father-in-law's kingdom and Satyavan's long life.",
            presidingDeity = "Sati Savitri, Satyavan, Yama Deva & the Sacred Vat Vriksha (Trimurti Swarupa)",
            sacredMantraSanskrit = "वटमूले स्थितो ब्रह्मा वटमध्ये जनार्दनः । वटाग्रे तु शिवो देवः सावित्री वटसंश्रिता ॥",
            sacredMantraTransliteration = "Vatamule Sthito Brahma Vatamadhye Janardanah | Vatagre Tu Shivo Devah Savitri Vatasamshrita ||",
            sacredMantraMeaning = "At the roots of the Banyan tree abides Brahma, in its trunk abides Vishnu, at its crown abides Shiva, and within it dwells Goddess Savitri.",
            shastraTithiRule = "Jyeshtha Masa • Purnima (Western/Southern Amanta) or Amavasya (Northern Purnimanta) • Morning Purvahna",
            vratAndNaivedyaGuide = "Wind raw cotton thread (Kachha Soot) 7 times clockwise around the Banyan tree trunk, offer soaked gram (Chana) and 5 seasonal fruits (mango, jackfruit, jamun, banana, lemon), and listen to the Savitri-Satyavan Katha."
        ),
        "tulsi_vivah_dev_uthani_ekadashi" to FestivalShastraDetails(
            whyCelebrated = "Marks the auspicious awakening of Lord Vishnu from His four-month cosmic slumber (Chaturmas Yoga Nidra) on Kartika Shukla Ekadashi and the ceremonial marriage of Devi Tulsi (Vrinda) with Sri Shaligram.",
            puranicLegend = "In the Padma Purana (Uttara Khanda), Goddess Vrinda's unmatched devotion blessed her to manifest as the sacred Tulsi plant, without whose leaves Lord Vishnu accepts no offering. Tulsi Vivah re-opens the auspicious season for weddings and mangalik ceremonies across India.",
            presidingDeity = "Goddess Tulsi (Vrinda Devi) & Lord Shaligram (Sri Vishnu)",
            sacredMantraSanskrit = "महाप्रसादजननी सर्वसौभाग्यवर्धिनी । आधि व्याधि हरा नित्यं तुलसी त्वं नमोऽस्तु ते ॥",
            sacredMantraTransliteration = "Mahaprasada Janani Sarva Saubhagya Vardhini | Adhi Vyadhi Hara Nityam Tulasi Tvam Namostu Te ||",
            sacredMantraMeaning = "Salutations to Goddess Tulsi, mother of sacred prasad, enhancer of all auspiciousness, who removes all mental and physical ailments.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Prabodhini Ekadashi (11th Tithi) / Dwadashi • Pradosh Godhuli Vivah Lagna",
            vratAndNaivedyaGuide = "Erect a sugarcane canopy (Mandap) around the Tulsi Vrindavan, drape Tulsi in a red chunri, perform Mangalashtak and Gathbandhan with Sri Shaligram, and offer sugarcane, water chestnut (Singhara), sweet potato, and Amla."
        ),
        "sharad_purnima_kojagiri" to FestivalShastraDetails(
            whyCelebrated = "Celebrated on Ashwina Shukla Purnima—the only night of the year when Chandra Deva (the Moon) shines with all 16 Kalas (divine phases)—honoring Goddess Kojagiri Mahalakshmi and Sri Krishna's Maharaas.",
            puranicLegend = "In the Brahma Purana and Srimad Bhagavatam, on Sharad Purnima night, Goddess Lakshmi tours the earth asking 'Ko Jagarati?' ('Who is awake in devotion?'), blessing vigilant households with prosperity, while the Moon's rays infuse healing Amrit into milk kheer.",
            presidingDeity = "Goddess Kojagiri Mahalakshmi, Sri Radha-Krishna & Chandra Deva (16 Kalas)",
            sacredMantraSanskrit = "ॐ श्रीं ह्रीं क्लीं त्रिभुवन महालक्ष्म्यै अस्माकं दारिद्र्य नाशय प्रचुर धन देहि देहि क्लीं ह्रीं श्रीं ॐ ॥",
            sacredMantraTransliteration = "Om Shreem Hreem Kleem Tribhuvana Mahalakshmyai Asmakam Daridrya Nashaya Prachura Dhana Dehi Dehi Kleem Hreem Shreem Om ||",
            sacredMantraMeaning = "Salutations to Goddess Mahalakshmi of the three worlds; dispel scarcity and bless our household with spiritual and material abundance.",
            shastraTithiRule = "Ashwina Masa • Shukla Paksha • Purnima (15th Tithi) • Nishita Moonlight & Pradosh Kaal",
            vratAndNaivedyaGuide = "Prepare saffron-cardamom rice Kheer (Doodh Poha in Maharashtra, Narkel Naru & Luchi in Bengal), place it under direct moonlight covered with a fine sieve during Nishita Kaal, and distribute as Amrit Prasad."
        ),
        "naraka_chaturdashi_choti_diwali" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the victory of Bhagavan Sri Krishna and Devi Satyabhama over Narakasura on Ashwina/Kartika Krishna Chaturdashi, freeing 16,000 captives and ushering in the dawn of Deepavali.",
            puranicLegend = "In the Srimad Bhagavatam and Vishnu Purana, before passing, Narakasura prayed that this day be celebrated with joyful Abhyanga Snan (fragrant oil bath before sunrise) and lamps so that no soul walks in darkness. In Bengal, Kali Chaudas / Bhoot Chaturdashi lights 14 lamps to honor 14 generations of ancestors.",
            presidingDeity = "Bhagavan Sri Krishna, Devi Satyabhama, Maa Kali & Yama Deva",
            sacredMantraSanskrit = "सीतालोष्टसमायुक्तं सकण्टकदलान्वितम् । हर पापमपामार्ग भ्राम्यमाणः पुनः पुनः ॥",
            sacredMantraTransliteration = "Sitaloshta Samayuktam Sakantaka Dalanvitam | Hara Papam Apamarga Bhramyamanah Punah Punah ||",
            sacredMantraMeaning = "O sacred Apamarga herb, as you are circled during the Arunodaya bath, remove all negativity, fatigue, and transgressions.",
            shastraTithiRule = "Ashwina (Amanta) / Kartika (Purnimanta) • Krishna Paksha • Chaturdashi (14th Tithi) • Arunodaya (Pre-Sunrise Moonset/Chaturdashi)",
            vratAndNaivedyaGuide = "Take sesame oil & ubtan Abhyanga Snan at Arunodaya (before sunrise), crush a bitter berry (Kareet) underfoot in Maharashtra, offer Faral / Poha sweets, and light 14 earthen diyas in the evening."
        ),
        "ahoi_ashtami" to FestivalShastraDetails(
            whyCelebrated = "Observed on Kartika Krishna Ashtami (eight days before Diwali) by mothers fasting for the health, long life, and bright future of their children.",
            puranicLegend = "According to the North Indian Vrat tradition, a compassionate woman who accidentally harmed a cub while digging soil near a forest den performed penance to Ahoi Mata (Goddess Parvati), who restored the cub to life and blessed all mothers who observe this Ashtami fast.",
            presidingDeity = "Ahoi Mata (Maa Parvati / Ambika) & Syahu Mata",
            sacredMantraSanskrit = "ॐ पार्वत्यै नमः । या देवी सर्वभूतेषु मातृरूपेण संस्थिता नमस्तस्यै नमो नमः ॥",
            sacredMantraTransliteration = "Om Parvatyai Namaha | Ya Devi Sarvabhuteshu Matri-Rupena Samsthita Namastasyai Namo Namaha ||",
            sacredMantraMeaning = "Salutations to Goddess Parvati, who abides in all beings as the compassionate Divine Mother.",
            shastraTithiRule = "Ashwina (Amanta) / Kartika (Purnimanta) • Krishna Paksha • Ashtami (8th Tithi) • Sanjh (Star-Sighting Dusk)",
            vratAndNaivedyaGuide = "Paint or hang the Ahoi Ashtami wall chart, thread silver beads into the Syahu Mala, listen to the Ahoi Katha holding 7 grains of wheat, and break the fast after offering water Arghya to the evening stars (Tara Darshan)."
        ),
        "skanda_sashti_soorasamharam" to FestivalShastraDetails(
            whyCelebrated = "Honors the supreme victory of Lord Murugan (Kartikeya / Subrahmanya) over the demon Surapadman on Kartika Shukla Shashthi using the divine Vel bestowed by Goddess Parvati.",
            puranicLegend = "In the Skanda Purana and Kanda Puranam, Surapadman split into a mango tree and was transformed by Lord Murugan's compassionate Vel into His peacock vahana (Mayil) and rooster banner (Seval)—symbolizing that divine grace transforms ego rather than destroying the soul.",
            presidingDeity = "Lord Murugan (Subrahmanya / Skanda / Shanmukha) with Valli & Deivanai",
            sacredMantraSanskrit = "ॐ शरवणभवाय नमः । ज्ञानशक्तिधरा स्कन्द वल्लीकल्याणसुन्दर देवसेनामनःकान्त सुब्रह्मण्य नमोऽस्तु ते ॥",
            sacredMantraTransliteration = "Om Saravanabhavaya Namaha | Jnanashaktidhara Skanda Vallikalyanasundara Devasenamanahkanta Subrahmanya Namostu Te ||",
            sacredMantraMeaning = "Salutations to Lord Subrahmanya, holder of the Vel of supreme wisdom (Jnana Shakti), beloved of Valli and Devasena.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Shashthi (6th Tithi) • Midday & Pradosh Soorasamharam",
            vratAndNaivedyaGuide = "Chant Kanda Sashti Kavacham, perform Pal (milk) and Vibhuti अभिषेक, and offer Panchamirtham, lemon rice, and Sakkarai Pongal."
        ),
        "hariyali_teej" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the sacred reunion of Goddess Parvati and Lord Shiva after Parvati's steadfast tapasya, welcoming the lush monsoon greenery (Shravana/Bhadrapada Shukla Tritiya).",
            puranicLegend = "In the Linga and Shiva Puranas, Goddess Parvati shaped a sand Shiva Lingam (Parthiva Lingam) in the forest with her friend ('Harat' + 'Aalika' = Hartalika) and meditated with such devotion that Lord Shiva appeared to wed Her.",
            presidingDeity = "Maa Gauri (Swarna Gowri / Parvati) & Lord Shiva",
            sacredMantraSanskrit = "कात्यायनि महामाये महायोगिन्यधीश्वरि । देहि मे सौभाग्यमारोग्यं देहि मे परमं सुखम् ॥",
            sacredMantraTransliteration = "Katyayani Mahamaye Mahayoginyadhishvari | Dehi Me Saubhagyam Arogyam Dehi Me Paramam Sukham ||",
            sacredMantraMeaning = "O Supreme Mother Gauri, bestow auspiciousness, health, and lasting family harmony.",
            shastraTithiRule = "Shravana / Bhadrapada Masa • Shukla Paksha • Tritiya (3rd Tithi) • Pratah & Pradosh Puja",
            vratAndNaivedyaGuide = "Worship clay idols of Shiva-Parvati with 16 types of leaves (Patri), wear green bangles and Sinjara/Solah Shringaar, and offer Ghevar, Gujiya, and Kheer."
        ),
        "anant_chaturdashi" to FestivalShastraDetails(
            whyCelebrated = "Observed on Bhadrapada Shukla Chaturdashi to worship Lord Ananta Padmanabha (infinite Vishnu) by tying the 14-knot Ananta Sutra, and marks the grand Ganapati Visarjan.",
            puranicLegend = "In the Agni Purana and Mahabharata, Sri Krishna advised Dharmaraja Yudhishthira to observe the Ananta Chaturdashi Vrat for 14 years to regain the Pandavas' lost fortune, recounting the story of Kaundinya Rishi and Sushila.",
            presidingDeity = "Lord Ananta Padmanabha (Vishnu reclining on Shesha Naga) & Lord Ganesha (Visarjan)",
            sacredMantraSanskrit = "अनन्तसंसारमहासमुद्रे मग्नं समभ्युद्धर वासुदेव । अनन्तरूपे विनियोजयस्व ह्यनन्तसूत्राय नमो नमस्ते ॥",
            sacredMantraTransliteration = "Ananta Samsara Mahasamudre Magnam Samabhyuddhara Vasudeva | Anantarerupe Viniyojayasva Hyananta-Sutraya Namo Namaste ||",
            sacredMantraMeaning = "O Vasudeva of infinite form, uplift us from the ocean of worldly trials; salutations to the sacred 14-knot Ananta thread.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Chaturdashi (14th Tithi) • Morning Purvahna Puja",
            vratAndNaivedyaGuide = "Consecrate a Kalash symbolizing Kshirasagara with Kusha grass, tie the turmeric-dyed 14-knot Ananta Sutra (right arm for men, left for women), and offer salt-free sweet Pua/Puri."
        ),
        "nirjala_ekadashi" to FestivalShastraDetails(
            whyCelebrated = "Observed on Jyeshtha Shukla Ekadashi (Bhimseni Ekadashi) as the most austere and meritorious of all 24 Ekadashis, bestowing the combined spiritual fruit of every Ekadashi in the year.",
            puranicLegend = "In the Padma Purana, Maharshi Vyasa instructed Bhimasena—who could not fast on all 24 Ekadashis due to his digestive fire (Vrika Agni)—to observe a single complete Nirjala Ekadashi on Jyeshtha Shukla Ekadashi alongside water charity.",
            presidingDeity = "Bhagavan Sri Lakshmi-Narayana (Trivikrama Vishnu)",
            sacredMantraSanskrit = "ॐ नमो भगवते वासुदेवाय । एकादश्यां निराहारो भूत्वाऽहं द्वादशीदिने भोक्ष्यामि पुण्डरीकाक्ष शरणं मे भवाच्युत ॥",
            sacredMantraTransliteration = "Om Namo Bhagavate Vasudevaya | Ekadashyam Niraharo Bhutva'ham Dwadashidine Bhokshyami Pundarikaksha Sharanam Me Bhavachyuta ||",
            sacredMantraMeaning = "O Lotus-Eyed Lord Achyuta, observing this Ekadashi vow under Your refuge, I shall partake of Parana on Dwadashi morning.",
            shastraTithiRule = "Jyeshtha Masa • Shukla Paksha • Ekadashi (11th Tithi) • Sunrise Vrat to Dwadashi Parana",
            vratAndNaivedyaGuide = "Chant Vishnu Sahasranama, offer Tulsi Manjari and yellow flowers, donate water-filled earthen pitchers (Jala-Kumbha), sweet sherbet, melons, and hand fans, and perform Dwadashi Parana after sunrise."
        ),
        "ashadhi_ekadashi_devshayani" to FestivalShastraDetails(
            whyCelebrated = "Marks the beginning of the holy four-month Chaturmas on Ashadha Shukla Ekadashi when Lord Vishnu enters Yoga Nidra on Shesha Naga, and the culmination of Maharashtra's Pandharpur Wari pilgrimage.",
            puranicLegend = "In the Bhavishyottara Purana, after granting King Bali sovereignty during Vamana Avatar, Lord Vishnu promised to reside in Bali's realm for four months from Ashadhi Ekadashi to Kartiki Prabodhini Ekadashi.",
            presidingDeity = "Lord Vitthala (Panduranga) & Rakhumai (Lakshmi-Narayana)",
            sacredMantraSanskrit = "सुप्ते त्वयि जगन्नाथ जमत्सुप्तं भवेदिदम् । विबुद्धे त्वयि बुद्धं च जगत्सर्वं चराचरम् ॥ पुण्डरीक वरदा हरि विठ्ठल ॥",
            sacredMantraTransliteration = "Supte Tvayi Jagannatha Jagat Suptam Bhaved Idam | Vibuddhe Tvayi Buddham Cha Jagat Sarvam Characharam || Pundarika Varada Hari Vitthala ||",
            sacredMantraMeaning = "O Lord of the Universe, when You rest, the cosmos rests in contemplation, and when You awaken, all creation awakens in grace.",
            shastraTithiRule = "Ashadha Masa • Shukla Paksha • Ekadashi (11th Tithi) • Sunrise to Dwadashi Parana",
            vratAndNaivedyaGuide = "Offer Tulsi garlands to Lord Vitthala/Vishnu, sing Abhangs and Hari Naam Sankirtan, observe Phalahar fast (Sabudana, Rajgira, groundnut, fruits), and take a Chaturmas spiritual vow."
        ),
        "ganga_dussehra" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the celestial descent (Avatarana) of Mother Ganga from heaven to earth on Jyeshtha Shukla Dashami in Hasta Nakshatra, purifying ten types of transgressions ('Dasa-Hara').",
            puranicLegend = "In the Valmiki Ramayana (Bala Kanda) and Skanda Purana, King Bhagiratha performed austere tapasya to bring celestial Ganga down to liberate his ancestors, and Lord Shiva graciously caught Her mighty torrent in His matted locks (Jata).",
            presidingDeity = "Goddess Ganga Maiya, Lord Shiva (Gangadhara) & King Bhagiratha",
            sacredMantraSanskrit = "ॐ नमः शिवायै गङ्गायै शिवदायै नमो नमः । नमस्ते विश्वरूपिण्यै ब्रह्मण्यै ते नमो नमः ॥",
            sacredMantraTransliteration = "Om Namah Shivayai Gangayai Shivadayai Namo Namaha | Namaste Vishvarupinyai Brahmanyai Te Namo Namaha ||",
            sacredMantraMeaning = "Salutations to Mother Ganga, bestower of auspiciousness and liberation across the universe.",
            shastraTithiRule = "Jyeshtha Masa • Shukla Paksha • Dashami (10th Tithi — Hasta Nakshatra)",
            vratAndNaivedyaGuide = "Bathe with Gangajal, offer 10 flowers, 10 leaf-diyas, and 10 seasonal fruits in the evening Aarti, and donate cool water and sattu."
        ),
        "sheetala_ashtami_basoda" to FestivalShastraDetails(
            whyCelebrated = "Observed on Krishna Ashtami after Holi to worship Goddess Sheetala ('The Cooling One') for protection of children and families from seasonal heat and illnesses.",
            puranicLegend = "Mentioned in the Skanda Purana (Kashi Khanda), Sheetala Mata holds a winnowing fan, broom, neem leaves, and a pitcher of healing water—teaching cleanliness, hygiene, and cooling nutrition as summer begins.",
            presidingDeity = "Goddess Sheetala Mata",
            sacredMantraSanskrit = "वन्देऽहं शीतलां देवीं रासभस्थां दिगम्बराम् । मार्जनीकलशोपेतां शूर्पालङ्कृतमस्तकाम् ॥",
            sacredMantraTransliteration = "Vande'ham Sheetalam Devim Rasabhastham Digambaram | Marjani Kalashopetam Shurpalankrita Mastakam || (Sheetala Ashtakam)",
            sacredMantraMeaning = "I bow to Goddess Sheetala, who holds the healing water pitcher and cleansing broom, bestowing coolness and health upon all children.",
            shastraTithiRule = "Chaitra (Purnimanta) / Phalguna (Amanta) • Krishna Paksha • Ashtami (8th Tithi) • Morning Sunrise Puja",
            vratAndNaivedyaGuide = "No fire is lit in the kitchen on Ashtami morning; offer cool sattvik food prepared on Saptami eve ('Basoda' — sweet rice Olya, curd, rabri, puas) along with fresh Neem leaves."
        ),
        "ratha_saptami" to FestivalShastraDetails(
            whyCelebrated = "Celebrates Surya Jayanti (the birth and cosmic illumination of the Sun God) on Magha Shukla Saptami as Surya Deva turns His seven-horse chariot northward toward spring.",
            puranicLegend = "In the Bhavishya Purana, Lord Surya manifested to Sage Kashyapa and Mother Aditi on Magha Shukla Saptami. Bathing at Arunodaya with 7 Ark (Erukku) leaves honors the seven rays and seven horses of the solar chariot.",
            presidingDeity = "Bhagavan Surya Narayana (Saptashva Ratha Rudha)",
            sacredMantraSanskrit = "सप्तसप्तिवहप्रीत सप्तलोकप्रदीपन । सप्तम्यां सहितो देवि गृहाणार्घ्यं दिवाकर ॥",
            sacredMantraTransliteration = "Saptasapti Vahaprita Saptaloka Pradipana | Saptamyam Sahito Devi Grihanarghyam Divakara ||",
            sacredMantraMeaning = "O Lord of the Seven-Horse Chariot who illuminates the seven worlds, accept my sacred Arghya on this Saptami morning.",
            shastraTithiRule = "Magha Masa • Shukla Paksha • Saptami (7th Tithi) • Arunodaya Snan & Sunrise Arghya",
            vratAndNaivedyaGuide = "Take Arunodaya bath placing 7 Ark (Calotropis/Erukku) leaves on head and shoulders, draw a chariot rangoli, offer Surya Arghya, and boil milk in a new clay pot facing the morning sun to make Chakkara Pongal / Kheer."
        ),
        "vaikuntha_ekadashi" to FestivalShastraDetails(
            whyCelebrated = "Observed on Margashirsha Shukla Ekadashi (Mokshada Ekadashi & Gita Jayanti)—the day the sacred Vaikuntha Dwara opens in Vishnu temples and the day Sri Krishna spoke the Bhagavad Gita to Arjuna.",
            puranicLegend = "In the Padma Purana, Lord Vishnu opened the northern gateway of Vaikuntha (Paramapada Vasal) on this Ekadashi for devotees, and on the battlefield of Kurukshetra, Sri Krishna revealed the 700 verses of the Bhagavad Gita on Margashirsha Shukla Ekadashi.",
            presidingDeity = "Lord Sri Vaikuntha Narayana (Venkateswara / Parthasarathy Krishna)",
            sacredMantraSanskrit = "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज । अहं त्वां सर्वपापेभ्यो मोक्षयिष्यामि मा शुचः ॥ (भगवद्गीता १८.६६)",
            sacredMantraTransliteration = "Sarva-Dharman Parityajya Mam Ekam Sharanam Vraja | Aham Tvam Sarva-Papebhyo Mokshayishyami Ma Shuchah || (Bhagavad Gita 18.66)",
            sacredMantraMeaning = "Take complete refuge in Me alone; I shall liberate you from all sorrow and bondage—do not grieve.",
            shastraTithiRule = "Margashirsha Masa / Dhanur (Margazhi) • Shukla Paksha • Ekadashi (11th Tithi) • Arunodaya Vaikuntha Dwara Darshan",
            vratAndNaivedyaGuide = "Observe Ekadashi fast, recite chapters of the Bhagavad Gita and Vishnu Sahasranama, offer Tulsi garlands and fruits, and break the fast on Dwadashi morning."
        ),
        "lohri_bhogi" to FestivalShastraDetails(
            whyCelebrated = "Celebrated on the eve of Makar Sankranti (January 13) to bid farewell to the winter solstice and welcome longer, warmer days with sacred bonfires (Lohri in Punjab/North India, Bhogi Mantalu in Andhra/Telangana/Tamil Nadu, Lal Loi in Sindh, and Uruka in Assam).",
            puranicLegend = "Honors Agni Deva (the Lord of Fire) and Surya Deva at the end of the Pausha winter month, alongside the folk legend of Dulla Bhatti in Punjab and the worship of Lord Indra on Bhogi in South India for timely rains and harvest abundance.",
            presidingDeity = "Agni Deva (Sacred Fire), Surya Deva & Lord Indra",
            sacredMantraSanskrit = "ॐ अग्नये स्वाहा । इदं अग्नये इदं न मम ॥ ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात् ॥",
            sacredMantraTransliteration = "Om Agnaye Svaha | Idam Agnaye Idam Na Mama || Om Bhur Bhuvah Svah Tat Savitur Varenyam Bhargo Devasya Dhimahi Dhiyo Yo Nah Prachodayat ||",
            sacredMantraMeaning = "Sacred offerings to Agni Deva, the purifier and messenger of the gods, and meditation upon the radiant solar Savitur.",
            shastraTithiRule = "Solar Eve of Makara Sankranti (13 January) • Pradosh Lohri Agni & Dawn Bhogi Mantalu",
            vratAndNaivedyaGuide = "Perform 3 or 7 clockwise Parikramas around the evening bonfire offering white/black sesame (Til), jaggery (Gur), Rewri, Gajak, peanuts, and popcorn; savor Sarson da Saag, Makki di Roti, and Pindi Chole."
        ),
        "vishwakarma_puja" to FestivalShastraDetails(
            whyCelebrated = "Observed on Kanya Sankranti (September 17) to revere Lord Vishwakarma—the divine architect, master craftsman, and cosmic engineer of the Devas.",
            puranicLegend = "In the Rigveda and Sthapatya Veda, Lord Vishwakarma crafted Swarga Loka, Dwarka Nagari, Indraprastha, the Sudarshana Chakra of Vishnu, the Trishula of Shiva, and the Pushpaka Vimana.",
            presidingDeity = "Bhagavan Sri Vishwakarma (Deva Shilpi)",
            sacredMantraSanskrit = "ॐ आधार शक्तये नमः । ॐ कूमयि नमः । ॐ अनन्तमनन्तमयि नमः । ॐ पृथिव्यै नमः । ॐ श्री विश्वकर्मणे नमः ॥",
            sacredMantraTransliteration = "Om Adhara Shaktaye Namaha | Om Kurmayai Namaha | Om Anantam Anantamayai Namaha | Om Prithivyai Namaha | Om Sri Vishwakarmane Namaha ||",
            sacredMantraMeaning = "Salutations to the foundational creative power, the cosmic tortoise, infinite Ananta, Mother Earth, and Bhagavan Sri Vishwakarma, divine architect of the universe.",
            shastraTithiRule = "Kanya Sankranti (Solar Ingress into Virgo — 17 September) • Morning Yantra & Tool Puja",
            vratAndNaivedyaGuide = "Cleanse and sanctify workshops, computers, laptops, instruments, and vehicles with Haldi-Kumkum Swastika and Mauli; give tools rest for the day and distribute Boondi, Khichdi, and seasonal fruits."
        ),
        "buddha_purnima" to FestivalShastraDetails(
            whyCelebrated = "Commemorates the thrice-sacred Vaishakha Purnima marking the Birth (at Lumbini), Enlightenment (at Bodh Gaya), and Mahaparinirvana (at Kushinagar) of Gautama Buddha, alongside Vaishakha Snan Purnima.",
            puranicLegend = "Under the sacred Bodhi tree (Peepal / Ashvattha) at Bodh Gaya on the full moon of Vaishakha, Siddhartha Gautama attained supreme awakening and taught the Four Noble Truths and the Eightfold Path of compassion and mindfulness.",
            presidingDeity = "Bhagavan Gautama Buddha & Sri Satyanarayana (Vaishakha Purnima)",
            sacredMantraSanskrit = "बुद्धं शरणं गच्छामि । धम्मं शरणं गच्छामि । संघं शरणं गच्छामि ॥",
            sacredMantraTransliteration = "Buddham Sharanam Gacchami | Dhammam Sharanam Gacchami | Sangham Sharanam Gacchami ||",
            sacredMantraMeaning = "I take refuge in the Awakened One, I take refuge in Dharma (Truth), I take refuge in the noble community.",
            shastraTithiRule = "Vaishakha Masa • Shukla Paksha • Purnima (15th Tithi — Full Moon)",
            vratAndNaivedyaGuide = "Water the sacred Bodhi/Peepal tree, light butter/ghee lamps, practice meditation and Ahimsa (strict vegetarianism), and offer sweet milk Kheer (Sujata Kheer) and fruits."
        ),
        "mahavir_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the auspicious birth (Janma Kalyanak) of Bhagavan Mahavira, the 24th Tirthankara of Jainism, on Chaitra Shukla Trayodashi.",
            puranicLegend = "Born in Kundagrama (Vaishali) to King Siddhartha and Queen Trishala on Chaitra Shukla Trayodashi, Lord Mahavira renounced royal life at age 30 and illuminated the path of Ahimsa Paramo Dharma (non-violence as the highest virtue) and Anekantavada.",
            presidingDeity = "Bhagavan Vardhamana Mahavira (24th Tirthankara)",
            sacredMantraSanskrit = "णमो अरिहंताणं । णमो सिद्धाणं । णमो आयरियाणं । णमो उवज्झायाणं । णमो लोए सव्व साहूणं ॥ (नवकार महामंत्र)",
            sacredMantraTransliteration = "Namo Arihantanam | Namo Siddhanam | Namo Ayariyanam | Namo Uvajjhayanam | Namo Loe Savva Sahunam || (Navkar Mantra)",
            sacredMantraMeaning = "I bow to the Arihantas (conquerors of inner passions), the Siddhas (liberated souls), the Acharyas, the Upadhyayas, and all sages in the universe.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Trayodashi (13th Tithi) • Morning Abhisheka & Rathayatra",
            vratAndNaivedyaGuide = "Perform ceremonial Kalash Abhisheka, recite the Navkar Mantra, practice Ahimsa and charity (Daan), and partake in pure sattvik vegetarian meals."
        ),
        "guru_nanak_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the Prakash Parv (birth anniversary) of Sri Guru Nanak Dev Ji, founder of Sikhism and the first of the ten Sikh Gurus, on Kartik Purnima.",
            puranicLegend = "Guru Nanak Dev Ji illuminated the universal message of 'Ik Onkar' (One Supreme Reality), 'Naam Japna' (remembering the Divine Name), 'Kirat Karni' (honest living), and 'Vand Chhakna' (sharing with all humanity).",
            presidingDeity = "Akal Purakh (Ik Onkar) & Sri Guru Nanak Dev Ji",
            sacredMantraSanskrit = "ੴ सति नामु करता पुरखु निरभउ निरवैरु अकाल मूरति अजूनी सैभं गुर प्रसादि ॥ (मूल मंत्र)",
            sacredMantraTransliteration = "Ik Onkar Satnam Karta Purakh Nirbhau Nirvair Akal Murat Ajuni Saibhang Gur Prasad || (Mool Mantar)",
            sacredMantraMeaning = "There is One Universal Creator, Truth is the Name, Creative Being, Without Fear, Without Enmity, Timeless Form, Beyond Birth, Self-Existent, realized by the Guru's Grace.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Purnima (15th Tithi) • Amrit Vela & Prakash Utsav",
            vratAndNaivedyaGuide = "Join Amrit Vela Asa Di Var kirtan, listen to GurBani, offer and partake in warm Karah Prasad (whole wheat flour, pure ghee, sugar, and water in equal parts) and community Langar."
        ),
        "thaipusam" to FestivalShastraDetails(
            whyCelebrated = "Observed on the full moon of the Tamil month of Thai when Poosam (Pushya) Nakshatra prevails, celebrating Goddess Parvati bestowing the divine Vel (lance of wisdom) upon Lord Murugan.",
            puranicLegend = "In the Skanda Purana, Adi Shakti Parvati concentrated Her cosmic shakti into the invincible Vel and presented it to Lord Murugan on Thai Poosam, enabling Him to vanquish the forces of ignorance led by Tarakasura and Surapadman.",
            presidingDeity = "Lord Murugan (Subrahmanya / Dandayudhapani)",
            sacredMantraSanskrit = "ॐ शरवणभवाय नमः । வெற்றிவேல் முருகனுக்கு அரோகரா (वेत्रिवेल मुरुगनुक्कु अरोहरा) ॥",
            sacredMantraTransliteration = "Om Sharavanabhavaya Namaha | Vetrivel Muruganukku Arogara ||",
            sacredMantraMeaning = "Salutations to Lord Murugan born in the sacred reed grove; victory to the holder of the radiant Vel.",
            shastraTithiRule = "Thai Masa (Pausha/Magha) • Shukla Paksha • Purnima + Pushya (Poosam) Nakshatra",
            vratAndNaivedyaGuide = "Perform Paal Abhishekam (milk offering) and Panchamirtham offering to Lord Murugan, chant Kanda Sashti Kavacham, and wear yellow/orange attire."
        ),
        "radha_ashtami" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the divine appearance day of Srimati Radharani—the internal potency (Hladini Shakti) of Bhagavan Sri Krishna—on Bhadrapada Shukla Ashtami, 15 days after Janmashtami.",
            puranicLegend = "In the Padma Purana and Brahmavaivarta Purana, King Vrishabhanu and Mother Kirtida discovered baby Radharani resting on a radiant golden lotus in Barsana at midday on Bhadrapada Shukla Ashtami.",
            presidingDeity = "Srimati Radharani & Bhagavan Sri Krishna (Yugal Sarkar)",
            sacredMantraSanskrit = "तप्तकाञ्चनगौराङ्गी राधे वृन्दावनेश्वरी । वृषभानुसुते देवि प्रणमामि हरिप्रिये ॥",
            sacredMantraTransliteration = "Tapta-Kanchana-Gaurangi Radhe Vrindavaneshvari | Vrishabhanu-Sute Devi Pranamami Hari-Priye ||",
            sacredMantraMeaning = "I offer my respectful obeisances to Srimati Radharani, whose complexion is like molten gold, Queen of Vrindavan, daughter of King Vrishabhanu, and dearest to Lord Hari.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Ashtami (8th Tithi) • Madhyahna Midday Puja",
            vratAndNaivedyaGuide = "Perform midday Panchamrit Abhishek of Radha-Krishna, adorn Them in floral attire, and offer Arbi (colocasia), Malpua, Rabri, and seasonal fruits."
        ),
        "sita_navami" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the appearance day (Janaki Jayanti) of Mata Sita on Vaishakha Shukla Navami in Pushya Nakshatra, one month after Sri Rama Navami.",
            puranicLegend = "In the Valmiki Ramayana (Bala Kanda), while Rajarshi Janaka was plowing the sacrificial ground in Mithila during a yajna, Mother Sita emerged from the sacred furrow ('Sita') as the incarnation of Goddess Mahalakshmi.",
            presidingDeity = "Mata Sita (Janaki / Maithili) & Bhagavan Sri Rama",
            sacredMantraSanskrit = "उद्भवस्थितिसंहारकारिणीं क्लेशहारिणीम् । सर्वश्रेयस्करीं सीतां नतोऽहं रामवल्लभाम् ॥",
            sacredMantraTransliteration = "Udbhava-Sthiti-Samhara-Karinim Klesha-Harinim | Sarva-Shreyaskarim Sitam Nato'ham Rama-Vallabham ||",
            sacredMantraMeaning = "I bow to Mother Sita, beloved of Sri Rama, remover of all distress and bestower of supreme auspiciousness.",
            shastraTithiRule = "Vaishakha Masa • Shukla Paksha • Navami (9th Tithi) • Madhyahna Midday Muhurat",
            vratAndNaivedyaGuide = "Install a Kalash and worship Sita-Rama under a decorated canopy with yellow flowers, Haldi-Kumkum, and sweet saffron rice, Kheer, and mango prasad."
        ),
        "narasimha_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Commemorates the appearance of Bhagavan Sri Narasimha (the fourth Avatar of Lord Vishnu) at sunset on Vaishakha Shukla Chaturdashi to protect His fearless devotee Prahlada.",
            puranicLegend = "In the Srimad Bhagavatam (Canto 7), to honor Lord Brahma's boon that Hiranyakashipu could be slain neither by man nor beast, neither indoors nor outdoors, neither by day nor by night, Lord Narasimha emerged from a pillar at twilight (Sandhya Kaal).",
            presidingDeity = "Bhagavan Sri Lakshmi-Narasimha",
            sacredMantraSanskrit = "उग्रं वीरं महाविष्णुं ज्वलन्तं सर्वतोमुखम् । नृसिंहं भीषणं भद्रं मृत्युमृत्युं नमाम्यहम् ॥",
            sacredMantraTransliteration = "Ugram Viram Maha-Vishnum Jvalantam Sarvatomukham | Nrisimham Bhishanam Bhadram Mrityur-Mrityum Namamy Aham ||",
            sacredMantraMeaning = "I bow to Lord Narasimha—heroic, all-pervading Maha-Vishnu, auspicious protector of devotees and conqueror of death itself.",
            shastraTithiRule = "Vaishakha Masa • Shukla Paksha • Chaturdashi (14th Tithi) • Sayankal Sandhya (Sunset) Puja",
            vratAndNaivedyaGuide = "Perform Puja at twilight (just before sunset) offering cooling Panakam (jaggery-ginger water), Kosambari, red hibiscus/lotus flowers, and sandalwood paste."
        ),
        "shani_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Observed on Jyeshtha Krishna Amavasya to celebrate the birth of Lord Shani Deva—the celestial dispenser of righteous justice (Karma Phala Data) and discipline.",
            puranicLegend = "In the Skanda Purana and Padma Purana, Lord Shani was born to Surya Deva and Chhaya Devi (owing to Chhaya's austere Shiva tapasya). Worshipping Shani Deva with humility, sesame oil, and service to the needy mitigates planetary hardships.",
            presidingDeity = "Bhagavan Sri Shani Deva & Lord Hanuman",
            sacredMantraSanskrit = "नीलांजनसमाभासं रविपुत्रं यमाग्रजम् । छायामार्तण्डसंभूतं तं नमामि शनैश्चरम् ॥",
            sacredMantraTransliteration = "Nilanjana Samabhasam Raviputram Yamagrajam | Chhaya Martanda Sambhutam Tam Namami Shanaishcharam ||",
            sacredMantraMeaning = "I bow to Lord Shanaishchara, radiant like blue collyrium, son of Surya and Chhaya, and elder brother of Yama.",
            shastraTithiRule = "Jyeshtha Masa • Krishna Paksha • Amavasya (15th Tithi)",
            vratAndNaivedyaGuide = "Perform sesame oil (Til Tel) Tailabhishekam, light a sesame oil lamp under a Peepal tree at dusk, offer black sesame laddus and Urad dal Imarti, and recite Hanuman Chalisa."
        ),
        "dattatreya_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the birth anniversary of Bhagavan Sri Dattatreya—the unified incarnation of Brahma, Vishnu, and Maheshwara and the Adi-Guru—on Margashirsha Shukla Purnima.",
            puranicLegend = "In the Srimad Bhagavatam and Guru Charitra, pleased by the supreme chastity and devotion of Mata Anasuya and Sage Atri, the Trimurti incarnated together as their three-headed divine son Dattatreya, who learned wisdom from 24 natural Gurus.",
            presidingDeity = "Bhagavan Sri Dattatreya (Digambara Avadhuta)",
            sacredMantraSanskrit = "दिगम्बरा दिगम्बरा श्रीपाद वल्लभ दिगम्बरा । ॐ द्रां दत्तात्रेयाय नमः ॥",
            sacredMantraTransliteration = "Digambara Digambara Sripada Vallabha Digambara | Om Dram Dattatreyaya Namaha ||",
            sacredMantraMeaning = "Salutations to Lord Dattatreya and Sripada Srivallabha, whose vesture is the infinite cosmos itself.",
            shastraTithiRule = "Margashirsha Masa • Shukla Paksha • Purnima (15th Tithi) • Pradosh Kaal Janmotsav",
            vratAndNaivedyaGuide = "Read chapters of Sri Guru Charitra or Avadhuta Gita, offer yellow flowers and Oudumbar leaves, and offer Sunthवडा (dry ginger-sugar powder) and Peda at dusk."
        ),
        "vivah_panchami" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the auspicious wedding anniversary of Bhagavan Sri Rama and Mata Sita in Mithila on Margashirsha Shukla Panchami.",
            puranicLegend = "In the Valmiki Ramayana and Tulsidas's Ramcharitmanas (Bala Kanda), after Sri Rama strung and broke the celestial Pinaka bow of Lord Shiva in King Janaka's court, Sita and Rama were united in divine matrimony on Margashirsha Shukla Panchami.",
            presidingDeity = "Bhagavan Sri Sita-Rama",
            sacredMantraSanskrit = "श्रीरामचन्द्राय नमः । जानकीवल्लभाय नमः । सियावर रामचन्द्र की जय ॥",
            sacredMantraTransliteration = "Sri Ramachandraya Namaha | Janaki-Vallabhaya Namaha | Siyavar Ramachandra Ki Jai ||",
            sacredMantraMeaning = "Salutations to Sri Ramachandra, beloved consort of Mother Janaki.",
            shastraTithiRule = "Margashirsha Masa • Shukla Paksha • Panchami (5th Tithi) • Midday Vivah Utsav",
            vratAndNaivedyaGuide = "Perform symbolic Sita-Rama Kalyanam at the home altar, recite the Vivah Prasanga from Balkand of Ramcharitmanas, and offer Kheer, Halwa, and fruits."
        ),
        "mahalaya_amavasya" to FestivalShastraDetails(
            whyCelebrated = "Observed on Bhadrapada (Amanta) / Ashwina (Purnimanta) Krishna Amavasya as Sarvapitri Amavasya (the culmination of the 16-day Pitru Paksha honoring ancestors) and as Mahalaya in Bengal heralding Maa Durga's homecoming.",
            puranicLegend = "In the Garuda Purana and Markandeya Purana, offering water and black sesame (Tarpan) on Sarvapitri Amavasya brings peace and blessings from all departed forefathers, while at dawn on Mahalaya, Goddess Durga is invoked as Mahishasuramardini.",
            presidingDeity = "Pitru Devatas (Ancestors), Lord Vishnu (Gaya Gadadhara) & Goddess Mahishasuramardini",
            sacredMantraSanskrit = "ॐ पितृदेवताभ्यो नमः । या देवी सर्वभूतेषु शक्तिरूपेण संस्थिता नमस्तस्यै नमस्तस्यै नमस्तस्यै नमो नमः ॥",
            sacredMantraTransliteration = "Om Pitru-Devatabhyo Namaha | Ya Devi Sarvabhuteshu Shakti-Rupena Samsthita Namastasyai Namo Namaha ||",
            sacredMantraMeaning = "Salutations to our revered ancestors, and salutations to the Divine Mother who abides as Shakti in all beings.",
            shastraTithiRule = "Ashwina (Purnimanta) / Bhadrapada (Amanta) • Krishna Paksha • Amavasya (15th Tithi)",
            vratAndNaivedyaGuide = "Listen to Mahishasuramardini Chandi Path at dawn, perform sesame-water Tarpan during Kutup/Aparahna Kaal, feed cows, crows, and the needy (Panchabali), and light a south-facing dusk lamp for ancestors."
        ),
        "nuakhai_rishi_panchami" to FestivalShastraDetails(
            whyCelebrated = "Observed on Bhadrapada Shukla Panchami (the day after Ganesh Chaturthi) as Western Odisha's grand agricultural thanksgiving festival of Nuakhai ('Nua' = new rice, 'Khai' = eating) and pan-Indian Rishi Panchami honoring the Seven Sages (Sapta Rishis).",
            puranicLegend = "Farmers offer the season's first newly harvested paddy ('Nabanna') to Goddess Samaleswari and family deities before partaking together and greeting elders with 'Nuakhai Juhar'. Simultaneously, Rishi Panchami honors Kashyapa, Atri, Bharadwaja, Vishvamitra, Gautama, Jamadagni, and Vasishtha.",
            presidingDeity = "Maa Samaleswari, Goddess Lakshmi & The Sapta Rishis (With Mata Arundhati)",
            sacredMantraSanskrit = "कश्यपोऽत्रिर्भरद्वाजो विश्वामित्रोऽथ गौतमः । जमदग्निर्वसिष्ठश्च साध्वी चैवात्रिरुन्धती ॥ सप्तर्षिभ्यो नमः ॥",
            sacredMantraTransliteration = "Kashyapo'trir Bharadwajo Vishwamitro'tha Gautamaha | Jamadagnir Vasishthash Cha Sadhvi Chaivatrirundhati || Saptarshibhyo Namaha ||",
            sacredMantraMeaning = "Salutations to the Seven Great Rishis—Kashyapa, Atri, Bharadwaja, Vishvamitra, Gautama, Jamadagni, and Vasishtha—along with venerable Mother Arundhati.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Panchami (5th Tithi) • Morning Lagna Nabanna Offering",
            vratAndNaivedyaGuide = "Offer freshly flattened new rice (Nabanna) mixed with jaggery, milk, and cardamom on a Kurei/banana leaf to the deity, and observe unplowed-grain sattvik diet for Rishi Panchami."
        ),
        "bathukamma" to FestivalShastraDetails(
            whyCelebrated = "Telangana's iconic nine-day floral festival celebrating Goddess Maha Gauri as 'Bathukamma' ('Mother Goddess, Come Alive'), honoring the bond between nature, water bodies, flowers, and womanhood.",
            puranicLegend = "Beginning on Mahalaya Amavasya (Engilipula Bathukamma) and culminating on Durgashtami (Saddula Bathukamma), men gather wild seasonal medicinal flowers like Gunuka (Celosia) and Tangedu (Senna auriculata), which women arrange in 7 concentric temple-gopuram tiers topped with a turmeric Gauramma.",
            presidingDeity = "Goddess Maha Gauri (Gauramma / Bathukamma)",
            sacredMantraSanskrit = "गौरीं सुवर्णवर्णाभां पद्महस्तां वरप्रदाम् । बतुकम्मस्वरूपेण पूजयामि सदा शिवाम् ॥ ॐ महागौर्यै नमः ॥",
            sacredMantraTransliteration = "Gaurim Suvarnavarnabham Padmahastam Varapradam | Bathukamma-Svarupena Pujayami Sada Shivam || Om Mahagauryai Namaha ||",
            sacredMantraMeaning = "I worship golden-hued Goddess Maha Gauri holding the sacred lotus, revered in the floral form of Bathukamma, bestower of life and boons.",
            shastraTithiRule = "Ashwina Masa • Mahalaya Amavasya to Shukla Ashtami (Saddula Bathukamma) • Evening Puja",
            vratAndNaivedyaGuide = "Arrange the floral Bathukamma stack with turmeric Gauramma on top, sing Bathukamma folk songs in a circle at dusk, offer 5 varieties of Saddula (curd rice, sesame rice, tamarind rice, lemon rice, sweet corn/jaggery Maleeda), and immerse in a local lake."
        ),
        "bonalu" to FestivalShastraDetails(
            whyCelebrated = "Celebrated across Hyderabad, Secunderabad, and Telangana during Ashadha Masam to express thanksgiving to Mother Goddess Mahakali for protecting communities from seasonal epidemics and granting prosperity.",
            puranicLegend = "Derived from 'Bhojanam' (Bonam—a sacred meal offering), women carry a brass or clay pot filled with rice cooked in milk and jaggery, adorned with neem leaves, turmeric, vermilion, and a lit diya on top, to Mahakali temples.",
            presidingDeity = "Goddess Ujjaini Mahakali, Yellamma, Pochamma & Maisamma",
            sacredMantraSanskrit = "ॐ जयंती मंगला काली भद्रकाली कपालिनी । दुर्गा क्षमा शिवा धात्री स्वाहा स्वधा नमोऽस्तु ते ॥",
            sacredMantraTransliteration = "Om Jayanti Mangala Kali Bhadrakali Kapalini | Durga Kshama Shiva Dhatri Svaha Svadha Namo'stu Te ||",
            sacredMantraMeaning = "Salutations to Goddess Mahakali and Bhadrakali, protector and sustainer of the world.",
            shastraTithiRule = "Ashadha Masa • Sundays & Shukla Purnima • Midday Bonam Offering",
            vratAndNaivedyaGuide = "Prepare sacred Bonam (jaggery-milk rice and Paramannam) in a turmeric-coated pot, adorn with Neem twigs and a lit lamp on top, and offer to the Mother Goddess."
        ),
        "raja_parba" to FestivalShastraDetails(
            whyCelebrated = "Odisha's joyous three-day festival starting on Mithuna Sankranti (Pahili Raja, Mithuna Sankranti, and Basi Raja) celebrating Mother Earth (Bhu Devi) and womanhood.",
            puranicLegend = "During these three days before the monsoon rains, Mother Earth (Bhudevi, consort of Lord Jagannath) is believed to undergo rejuvenation; all plowing, digging, and agricultural work is suspended out of reverence for the soil, and girls celebrate on flower-decked swings.",
            presidingDeity = "Bhu Devi (Mother Earth) & Lord Jagannath",
            sacredMantraSanskrit = "समुद्रवसने देवि पर्वतस्तनमण्डले । विष्णुपत्नि नमस्तुभ्यं पादस्पर्शं क्षमस्व मे ॥",
            sacredMantraTransliteration = "Samudra-Vasane Devi Parvata-Stana-Mandale | Vishnu-Patni Namastubhyam Pada-Sparsham Kshamasva Me ||",
            sacredMantraMeaning = "O Mother Earth, clothed by the oceans and adorned with mountains, consort of Lord Vishnu, I bow to You with gratitude.",
            shastraTithiRule = "Mithuna Sankranti (Solar Ingress into Gemini — ~14–16 June) • 3-Day Observance",
            vratAndNaivedyaGuide = "Refrain from digging or tilling the earth, wear new attire, enjoy decorated swings (Raja Doli), and prepare traditional caramelized Poda Pitha, Chakuli Pitha, and Raja Paan."
        ),
        "gangaur" to FestivalShastraDetails(
            whyCelebrated = "One of the most colorful festivals of Rajasthan, Madhya Pradesh, and Gujarat, culminating on Chaitra Shukla Tritiya in honor of Goddess Gauri (Parvati) and Lord Shiva (Isar) for marital harmony and devotion.",
            puranicLegend = "Beginning the day after Holi and continuing for 18 days until Chaitra Shukla Tritiya, women fashion clay images of Isar-Ji and Gangaur (Gauri), offer fresh green wheat/barley shoots (Jwara), and celebrate Gauri's blessed union with Shiva.",
            presidingDeity = "Goddess Gauri (Gangaur) & Lord Shiva (Isar Ji)",
            sacredMantraSanskrit = "हे गौरि शंकरार्धाङ्गि यथा त्वं शंकरप्रिया । तथा मां कुरु कल्याणि कान्तकान्तां सुदुर्लभाम् ॥",
            sacredMantraTransliteration = "He Gauri Shankarardhangi Yatha Tvam Shankarapriya | Tatha Mam Kuru Kalyani Kantakantam Sudurlabham ||",
            sacredMantraMeaning = "O auspicious Goddess Gauri, half of Lord Shankara's own form and dearest to Him, bless our household with enduring love and harmony.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Tritiya (3rd Tithi) • Morning & Evening Gauri Puja",
            vratAndNaivedyaGuide = "Apply Mehendi, offer Durva grass, sprouted barley (Jwara), and Marigold flowers to Isar-Gangaur, and prepare Honeycomb Ghewar and sweet Gunas."
        ),
        "jitiya_jivitputrika" to FestivalShastraDetails(
            whyCelebrated = "Observed on Ashwina (Purnimanta) / Bhadrapada (Amanta) Krishna Ashtami across Bihar, Jharkhand, Eastern UP, and Nepal by mothers who keep a strict 24-hour nirjala fast for the long life, safety, and prosperity of their children.",
            puranicLegend = "In the Mahabharata (Ashwamedhika Parva) and Bhavishya Purana, Lord Krishna revived Uttara's unborn son Parikshit through His divine grace (hence called 'Jivitputrika'), and Gandharva Prince Jimutavahana selflessly saved a Naga mother's son from Garuda.",
            presidingDeity = "Lord Jimutavahana, Bhagavan Sri Krishna & Surya Narayana",
            sacredMantraSanskrit = "जीमूतवाहनो नाम राजा परमधार्मिकः । स सत्यवादी धर्मात्मा पुत्राणां रक्षकः सदा ॥ ॐ जीमूतवाहनाय नमः ॥",
            sacredMantraTransliteration = "Jimutavahano Nama Raja Paramadharmikah | Sa Satyavadi Dharmatma Putranam Rakshakah Sada || Om Jimutavahanaya Namaha ||",
            sacredMantraMeaning = "Salutations to the supremely righteous and truthful King Jimutavahana, eternal protector of children and families.",
            shastraTithiRule = "Ashwina (Purnimanta) / Bhadrapada (Amanta) • Krishna Paksha • Ashtami (8th Tithi) • Nahay-Khay on Saptami, Nirjala on Ashtami, Parana on Navami",
            vratAndNaivedyaGuide = "Partake of Noni Saag (purslane greens) and Marua (ragi) roti on Saptami Nahay-Khay, worship Kusha-grass Jimutavahana on Ashtami evening, tie the sacred red-yellow Jitiya thread, and perform Parana on Navami morning."
        ),
        "sankashti_chaturthi_sakat" to FestivalShastraDetails(
            whyCelebrated = "Observed on Krishna Paksha Chaturthi—most notably Magha Krishna Chaturthi (Sakat Chauth / Tilkut Chauth)—dedicated to Lord Sankatahara Ganesha, the remover of all crises and obstacles.",
            puranicLegend = "In the Ganesha Purana, worshipping Lord Ganesha on Krishna Chaturthi and offering Arghya to Chandra Deva (the Moon) upon moonrise removes all family obstacles ('Sankata') and grants wisdom and protection to children.",
            presidingDeity = "Lord Sankatahara Ganapati, Sakat Mata & Chandra Deva",
            sacredMantraSanskrit = "प्रणम्य शिरसा देवं गौरीपुत्रं विनायकम् । भक्तावासं स्मरेन्नित्यमायुःकामार्थसिद्धये ॥ ॐ गं गणपतये नमः ॥",
            sacredMantraTransliteration = "Pranamya Shirasa Devam Gauriputram Vinayakam | Bhaktavasam Smaren Nityam Ayuh-Kamartha-Siddhaye || Om Gam Ganapataye Namaha ||",
            sacredMantraMeaning = "Bowing with reverence to Lord Vinayaka, son of Gauri who dwells in the hearts of devotees, we remember Him for longevity, fulfillment, and removal of all crises (Sankata).",
            shastraTithiRule = "Magha (Amanta) / Pausha-Magha • Krishna Paksha • Chaturthi (4th Tithi) • Moonrise (Chandrodaya) Arghya",
            vratAndNaivedyaGuide = "Offer 21 Durva grass blades, roasted sesame-jaggery Tilkut (shaped as a symbolic mountain), sweet potatoes, and Modaks to Lord Ganesha in the evening, and break the fast after offering milk-water Arghya at moonrise."
        ),
        "vaikunta_ekadashi" to FestivalShastraDetails(
            whyCelebrated = "One of the most sacred Vaishnava observances across Tamil Nadu, Andhra Pradesh, Telangana, Karnataka, and Kerala, occurring on Shukla Paksha Ekadashi during Dhanur / Margazhi Masa when the Vaikuntha Dwara (Paramapada Vasal) is opened.",
            puranicLegend = "According to the Padma Purana, Lord Vishnu's divine energy Ekadashi Devi vanquished the demon Muran on this day, and Bhagavan declared that whoever passes through the northern Vaikuntha Dwara on Margazhi Shukla Ekadashi attains liberation (Moksha).",
            presidingDeity = "Lord Sri Venkateswara / Ranganatha / Maha Vishnu & Goddess Lakshmi",
            sacredMantraSanskrit = "ॐ नमो नारायणाय । श्रीनिवासाय मङ्गलं वेङ्कटेशाय मङ्गलम् ॥",
            sacredMantraTransliteration = "Om Namo Narayanaya | Srinivasaya Mangalam Venkateshaya Mangalam ||",
            sacredMantraMeaning = "Salutations to Lord Narayana; auspiciousness unto Lord Srinivasa and Venkateswara.",
            shastraTithiRule = "Dhanur / Margazhi (Pausha) Masa • Shukla Paksha • Ekadashi (11th Tithi) • Brahma Muhurat Vaikuntha Dwara Darshan",
            vratAndNaivedyaGuide = "Observe Ekadashi fast from grains, chant Tiruppavai and Vishnu Sahasranama at dawn, offer Tulsi leaves and Phalahar Naivedya, and perform Dwadashi Parana the next morning."
        ),
        "karthigai_deepam" to FestivalShastraDetails(
            whyCelebrated = "The ancient festival of lights celebrated across Tamil Nadu, Kerala (Trikkartika), and Sri Lanka when the full moon of Karthigai Masa aligns with Krittika Nakshatra, honoring Lord Shiva as Arunachaleswara (an infinite pillar of fire) and Lord Murugan.",
            puranicLegend = "In the Shiva Purana, Lord Shiva manifested as an endless column of Jyoti (fire) atop Thiruvannamalai (Arunachala Hill) to dissolve the ego of Brahma and Vishnu; on this day, the six sparks of Shiva's third eye also coalesced into Lord Kartikeya (Murugan) cradled by the six Krittika stars.",
            presidingDeity = "Lord Arunachaleswara (Agni Lingam Shiva) & Lord Subramanya (Kartikeya)",
            sacredMantraSanskrit = "ॐ नमः शिवाय । अरुणाचल शिव अरुणाचल शिव अरुणाचल शिव अरुणाचला ॥ ॐ शरवणभवाय नमः ॥",
            sacredMantraTransliteration = "Om Namah Shivaya | Arunachala Shiva Arunachala Shiva || Om Saravanabhavaya Namaha ||",
            sacredMantraMeaning = "Salutations to Lord Shiva of Arunachala, the supreme beacon of consciousness, and to Lord Murugan.",
            shastraTithiRule = "Karthigai / Kartika Masa • Shukla Purnima with Krittika Nakshatra • Pradosh Maha Deepam Lighting",
            vratAndNaivedyaGuide = "Light Bharani Deepam at dawn and rows of Agal Vilakku clay lamps with gingelly/ghee oil across every threshold at dusk, and offer Pori Urundai (puffed rice jaggery balls) and Appam."
        ),
        "ambubachi_mela" to FestivalShastraDetails(
            whyCelebrated = "Celebrated annually during the monsoon month of Ashadha (around Mithuna 7 / June 22) at the sacred Kamakhya Shakti Peetha in Guwahati, Assam, honoring the creative fertility and annual menstruation cycle of Mother Earth (Devi Kamakhya).",
            puranicLegend = "According to the Kalika Purana, Nilachal Hill is where the Yoni-mudra of Goddess Sati fell; during the three days of Ambubachi (from Pravritti to Nivritti), the sanctum doors remain closed and all farming/digging ceases across Assam in reverence for Bhudevi's rest.",
            presidingDeity = "Maa Kamakhya (Maha Vidya Shakti)",
            sacredMantraSanskrit = "कामाख्ये वरदे देवि नीलपर्वतवासिनि । त्वं देवि जगतां मातर्योनोमुद्रे नमोऽस्तु ते ॥",
            sacredMantraTransliteration = "Kamakhye Varade Devi Nilaparvata-Vasini | Tvam Devi Jagatam Matar Yoni-Mudre Namo'stu Te ||",
            sacredMantraMeaning = "O boon-bestowing Goddess Kamakhya dwelling on Nilachal Hill, Mother of the Universe, salutations to You.",
            shastraTithiRule = "Mithuna / Ashadha Masa • Sun in Ardra Nakshatra (~June 22–25) • Pravritti & Nivritti Snan",
            vratAndNaivedyaGuide = "Abstain from tilling the soil or plucking fruits during the three days of Ambubachi, chant Devi Stotras, and receive red Angodak/Angabastra Prasad upon Nivritti temple reopening."
        ),
        "masi_magham_attukal_pongala" to FestivalShastraDetails(
            whyCelebrated = "Observed on Magha Shukla Purnima when the full moon aligns with the royal Magha Nakshatra (ruled by the Pitris), celebrated as Masi Magham in Tamil Nadu, Attukal Pongala in Kerala, Guru Ravidas Jayanti, and the Maghi Purnima Kalpavas culmination Snan.",
            puranicLegend = "During Masi Magham, temple deities (Utsava Murtis) across Tamil Nadu and Puducherry are carried in grand processions to the seashore or sacred rivers for Theerthavari bath; in Thiruvananthapuram, millions of women cook sweet Pongala in earthen pots for Attukal Bhagavathy.",
            presidingDeity = "Attukal Bhagavathy, Lord Vishnu, Lord Shiva & Sant Guru Ravidas Ji",
            sacredMantraSanskrit = "गङ्गे च यमुने चैव गोदावरि सरस्वति । नर्मदे सिन्धु कावेरि जलेऽस्मिन् सन्निधिं कुरु ॥",
            sacredMantraTransliteration = "Gange Cha Yamune Chaiva Godavari Sarasvati | Narmade Sindhu Kaveri Jale'smin Sannidhim Kuru ||",
            sacredMantraMeaning = "May the sacred waters of Ganga, Yamuna, Godavari, Saraswati, Narmada, Sindhu, and Kaveri be present in this holy bath.",
            shastraTithiRule = "Magha Masa • Shukla Paksha • Purnima with Magha Nakshatra • Sunrise Tirtha Snan",
            vratAndNaivedyaGuide = "Perform holy river/sea Snan at dawn, offer til-jaggery Tarpan to ancestors, prepare sweet Pongala (rice, jaggery, coconut, cardamom) in an earthen pot, and donate food and clothes."
        ),
        "sarhul_karam_parab" to FestivalShastraDetails(
            whyCelebrated = "The foremost indigenous nature and harvest festivals of Jharkhand, Odisha, Chhattisgarh, and West Bengal — Sarhul celebrates the spring marriage of the Sun (Singbonga) and Mother Earth with Sal tree blossoms, while Karam Parab honors Karam Devta for youth and bountiful crops.",
            puranicLegend = "In Sarhul (Chaitra Shukla Tritiya), the village priest (Pahan) offers fresh Sal flowers (Shorea robusta) at the sacred Sarna grove and predicts the year's rainfall from water pitchers; in Karam Parab (Bhadrapada Shukla Ekadashi), unmarried youths fast and dance around a freshly planted Karam tree branch.",
            presidingDeity = "Dharti Ayo (Mother Earth), Singbonga / Dharmesh & Karam Raja",
            sacredMantraSanskrit = "ॐ द्यौः शान्तिरन्तरिक्षं शान्तिः पृथिवी शान्तिरापः शान्तिरोषधयः शान्तिः । वनस्पतयः शान्तिः ॥",
            sacredMantraTransliteration = "Om Dyauh Shantir Antariksham Shantih Prithivi Shantir Apah Shantir Oshadhayah Shantih | Vanaspatayah Shantih ||",
            sacredMantraMeaning = "May there be peace in the sky, atmosphere, Earth, waters, medicinal herbs, and sacred forest trees.",
            shastraTithiRule = "Bhadrapada Shukla Ekadashi (Karam Parab) & Chaitra Shukla Tritiya (Sarhul) • Sacred Grove Vandana",
            vratAndNaivedyaGuide = "Offer fresh Sal blossoms, sprouted grains (Jawa), cucumber, and handia/rice delicacies at the Sarna/Akhra altar, and celebrate with traditional Mandar drum dances."
        ),
        "paryushana_samvatsari" to FestivalShastraDetails(
            whyCelebrated = "The supreme annual spiritual festival of Jainism (8-day Paryushana Parva for Svetambara culminating on Samvatsari, and 10-day Das Lakshan Parva for Digambara), dedicated to soul purification, non-violence (Ahimsa), austerity, and universal forgiveness.",
            puranicLegend = "During the monsoon Chaturmas retreat, monastics and householders recite the Kalpa Sutra recounting the lives of Tirthankaras and conclude on Samvatsari / Kshamavani by folding hands to every living being and saying 'Michhami Dukkadam' ('May all my wrongdoings be forgiven').",
            presidingDeity = "The Twenty-Four Tirthankaras (Bhagavan Mahavira & Bhagavan Parshvanatha)",
            sacredMantraSanskrit = "खामेमि सव्वजीवे सव्वजीवा खमंतु मे । मित्ती मे सव्वभूएसु वेरं मज्झं न केणइ ॥ मिच्छामि दुक्कड़म् ॥",
            sacredMantraTransliteration = "Khamemi Savva-Jive Savva-Jiva Khamantu Me | Mitti Me Savva-Bhuesu Veram Majjham Na Kenai || Michhami Dukkadam ||",
            sacredMantraMeaning = "I forgive all living beings, and may all living beings forgive me; I have friendship toward all creatures and enmity toward none.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Panchami (Samvatsari) to Chaturdashi (Anant Chaturdashi / Kshamavani)",
            vratAndNaivedyaGuide = "Observe Upvas/Ekasana fasting without root vegetables or post-sunset dining (Chauvihar), perform Samvatsari Pratikraman, and seek forgiveness from family and friends."
        ),
        "hola_mohalla" to FestivalShastraDetails(
            whyCelebrated = "Initiated by the Tenth Sikh Guru, Guru Gobind Singh Ji, at Sri Anandpur Sahib in 1701 CE on the day after Holi (Chet Vadi Pratipada/Dwitiya) to instill martial readiness, fearless valor (Bir Ras), and spiritual devotion in the Khalsa Panth.",
            puranicLegend = "Guru Gobind Singh Ji transformed the spring festival into a three-day gathering at Keshgarh Sahib featuring mock battles, horsemanship, Gatka martial displays by Nihang Singhs, poetry symposia, and continuous Gurbani Kirtan and Langar.",
            presidingDeity = "Sri Guru Granth Sahib Ji & Guru Gobind Singh Ji",
            sacredMantraSanskrit = "ਦੇਹਿ ਸ਼ਿਵਾ ਬਰੁ ਮੋਹਿ ਇਹੈ ਸ਼ੁਭ ਕਰਮਨ ਤੇ ਕਬਹੂੰ ਨ ਟਰੋਂ । ਵਾਹਿਗੁਰੂ ਜੀ ਕਾ ਖਾਲਸਾ ਵਾਹਿਗੁਰੂ ਜੀ ਕੀ ਫਤਹਿ ॥",
            sacredMantraTransliteration = "Deh Shiva Bar Mohe Ehai Shubh Karman Te Kabahun Na Taron | Waheguru Ji Ka Khalsa Waheguru Ji Ki Fateh ||",
            sacredMantraMeaning = "Grant me this boon, O Almighty: may I never shy away from righteous deeds; the Khalsa belongs to Waheguru, and victory belongs to Waheguru.",
            shastraTithiRule = "Phalguna / Chaitra (Chet) Masa • Krishna Paksha • Pratipada/Dwitiya (Day after Holi)",
            vratAndNaivedyaGuide = "Attend Amrit Vela Asa Di Vaar and Kirtan at the Gurdwara, witness or honor Khalsa martial traditions, and serve Karah Parshad and Langar to all."
        ),
        "losar_hemis" to FestivalShastraDetails(
            whyCelebrated = "Celebrated across the Himalayan regions of India—Ladakh, Sikkim, Arunachal Pradesh (Tawang), and Himachal Pradesh (Lahaul-Spiti & Kinnaur)—marking the Tibetan/Himalayan Buddhist New Year (Losar) and Guru Padmasambhava's Hemis Tsechu.",
            puranicLegend = "Preceded by Metho (flaming torch procession to banish negativity) and Guthuk nine-ingredient noodle soup on New Year's Eve, families offer fresh glacier water at dawn to the dharma protectors, hoist new Lungta prayer flags, and gather at monasteries for sacred Cham mask dances.",
            presidingDeity = "Lord Buddha Shakyamuni, Guru Padmasambhava (Guru Rinpoche) & Palden Lhamo",
            sacredMantraSanskrit = "ॐ मणि पद्मे हूँ । ॐ आः हूँ वज्र गुरु पद्म सिद्धि हूँ ॥ लोसर ताशी देलेक ॥",
            sacredMantraTransliteration = "Om Mani Padme Hum | Om Ah Hum Vajra Guru Padma Siddhi Hum || Losar Tashi Delek ||",
            sacredMantraMeaning = "Homage to the Jewel in the Lotus and to the Lotus-Born Guru Padmasambhava; auspicious New Year blessings.",
            shastraTithiRule = "Magha / Phalguna Masa • Shukla Paksha • Pratipada (1st Lunar Day of Chotrul Dawa)",
            vratAndNaivedyaGuide = "Cleanse the home altar, offer Chemar (roasted barley flour with butter and sugar) and Khapse pastries, hang fresh пяти-colored prayer flags, and wish elders 'Tashi Delek'."
        ),
        "hornbill_northeast_utsav" to FestivalShastraDetails(
            whyCelebrated = "Celebrates the rich indigenous harvest and cultural festivals of Northeast India—led by Nagaland's Hornbill Festival ('Festival of Festivals', Dec 1–10), Meghalaya's 100-Drum Wangala (honoring Saljong, the Sun God), Mizoram's Chapchar Kut, and Manipur's Sangai Festival.",
            puranicLegend = "Rooted in agrarian thanksgiving across the Seven Sister states, communities gather at the traditional Morung (youth dormitory) and village arenas after the autumn harvest becomes granary-ready, offering the first rice brew and grain to the Creator and Sun Deity.",
            presidingDeity = "Misi Saljong (Sun Deity of Bounty), Ukepenopfu & Ancestral Guardians of the Land",
            sacredMantraSanskrit = "ॐ अन्नपूर्णायै सदापूर्णायै शङ्करप्राणवल्लभायै नमः । ॐ आदित्याय च वसुन्धरायै नमः ॥",
            sacredMantraTransliteration = "Om Annapurnayai Sadapurnayai Shankarapranavallabhayai Namaha | Om Adityaya Cha Vasundharayai Namaha ||",
            sacredMantraMeaning = "Salutations to Mother Annapurna, the life-giving Sun, and Mother Earth for blessing the land with bountiful harvest and community peace.",
            shastraTithiRule = "Solar Harvest Window • December 1–10 (Hornbill) / Post-Harvest Autumn-Winter Season",
            vratAndNaivedyaGuide = "Offer freshly harvested sticky rice, bamboo-shoot delicacies, millet cakes, and traditional thanksgiving songs with log-drums and folk dances in community harmony."
        ),
        "hartalika_teej" to FestivalShastraDetails(
            whyCelebrated = "Observed on Bhadrapada Shukla Tritiya (the day before Ganesh Chaturthi) honoring Goddess Parvati's unwavering forest tapasya to wed Lord Shiva, celebrated as Hartalika Teej in North/West India and Swarna Gowri Habba across Karnataka, Andhra Pradesh, and Tamil Nadu.",
            puranicLegend = "In the Bhavishyottara and Linga Puranas, Goddess Parvati's friend ('Aalika') spirited Her away ('Harat') into the deep forest so She could avoid marrying against Her wish. There, Parvati fashioned a sand Parthiva Shiva Lingam and worshipped Lord Shiva through the night, earning His eternal union.",
            presidingDeity = "Goddess Swarna Gowri (Parvati) & Lord सदाशिव (Uma-Maheshwara)",
            sacredMantraSanskrit = "शिवायै नमः । उमायै नमः । पार्वत्यै नमः । जगद्धात्र्यै नमः । जगत्प्रतिष्ठायै नमः । शान्तिरूपिण्यै नमः ॥ ॐ उमामहेश्वराभ्यां नमः ॥",
            sacredMantraTransliteration = "Shivayai Namah | Umayai Namah | Parvatyai Namah | Jagaddhatryai Namah | Jagatpratishthayai Namah | Shantirupinyai Namah || Om Umamaheshwarabhyam Namaha ||",
            sacredMantraMeaning = "Salutations to Goddess Shivaa, Uma, Parvati, Sustainer of the Universe, and Embodiment of Peace; salutations to the divine couple Uma and Maheshwara.",
            shastraTithiRule = "Bhadrapada Masa • Shukla Paksha • Tritiya (3rd Tithi) • Pratah & Pradosh Parthiva Puja",
            vratAndNaivedyaGuide = "Sculpt Parthiva clay idols of Shiva, Parvati, and Ganesha on a banana leaf, offer 16 types of leaves (Shodasha Patri) and Suhag Pitari, tie the 16-knot Swarna Gowri Dorak/thread, and offer Gujiya and Kheer."
        ),
        "kajari_teej" to FestivalShastraDetails(
            whyCelebrated = "Celebrated on Bhadrapada Krishna Tritiya (3 days after Raksha Bandhan) as Kajari Teej, Badi Teej, or Satodi Teej across Rajasthan, Uttar Pradesh, Madhya Pradesh, and Bihar, honoring Neemri Mata and celebrating the monsoon clouds.",
            puranicLegend = "Women construct a miniature pond (Talab) bordered with clay and mud, plant a sacred Neem twig (Neemri Mata) at its center, offer roasted barley/wheat/gram Sattu Pindas, and worship the Moon at Chandrodaya for marital harmony and family well-being.",
            presidingDeity = "Neemri Mata (Goddess Parvati), Lord Shiva & Chandra Deva",
            sacredMantraSanskrit = "नमो देव्यै महादेव्यै शिवायै सततं नमः । नमः प्रकृत्यै भद्रायै नियताः प्रणताः स्म ताम् ॥ ॐ नीमरी मात्रे नमः ॥",
            sacredMantraTransliteration = "Namo Devyai Mahadevyai Shivayai Satatam Namaha | Namah Prakrityai Bhadrayai Niyatah Pranatah Sma Tam || Om Neemri Matre Namaha ||",
            sacredMantraMeaning = "Constant salutations to the Supreme Goddess, auspicious Prakriti and Neemri Mata, before whom we bow in devotion.",
            shastraTithiRule = "Bhadrapada (Purnimanta) / Shravana (Amanta) • Krishna Paksha • Tritiya (3rd Tithi) • Pradosh & Moonrise Arghya",
            vratAndNaivedyaGuide = "Worship Neemri Mata with raw milk, Roli, rice, lemon, cucumber, and Sattu Pinda, and break the fast after rotating 7 times while offering Arghya to the rising Moon."
        ),
        "maha_navami_ayudha_puja" to FestivalShastraDetails(
            whyCelebrated = "The ninth culminating day of Sharad Navratri honoring Maa Siddhidatri (bestower of all eight Siddhis), North India's Kanya Pujan, Bengal's Maha Navami Homa & Arati, and South & Western India's sacred Ayudha Puja / Saraswati Puja.",
            puranicLegend = "In the Devi Bhagavata Purana, on Ashwina Shukla Navami, Goddess Durga completed the defeat of Mahishasura's forces, and the Devas worshipped Her divine weapons (Ayudha) and wisdom (Saraswati), establishing the tradition of revering all instruments of knowledge and livelihood.",
            presidingDeity = "Maa Siddhidatri, Goddess Mahasaraswati & Sacred Ayudha / Shastra",
            sacredMantraSanskrit = "सिद्धगन्धर्वयक्षाद्यैरसुरैरमरैरपि । सेव्यमाना सदा भूयात् सिद्धिदा सिद्धिदायिनी ॥ ॐ महासरस्वत्यै नमः ॥",
            sacredMantraTransliteration = "Siddha-Gandharva-Yakshadair Asurair Amarair Api | Sevyamana Sada Bhuyat Siddhida Siddhidayini || Om Mahasarasvatyai Namaha ||",
            sacredMantraMeaning = "May Goddess Siddhidatri and Mahasaraswati, served by celestial beings and sages, bestow supreme accomplishment, skill, and wisdom upon us.",
            shastraTithiRule = "Ashwina Masa • Shukla Paksha • Navami (9th Tithi) • Madhyahna Homa, Kanya Pujan & Ayudha Puja",
            vratAndNaivedyaGuide = "Perform Navami Havan, worship nine young girls (Kanya Pujan) with Halwa, Puri, and Kala Chana, and sanctify books, laptops, tools, and vehicles with sandalwood, hibiscus, and puffed rice (Pori)."
        ),
        "labh_panchami" to FestivalShastraDetails(
            whyCelebrated = "Observed on Kartika Shukla Panchami as the auspicious culmination of the Diwali festival period across Gujarat, Maharashtra, and Rajasthan—inaugurating new business ventures, account ledgers (Bahi-Khata), and Jain Gnan Panchami scripture worship.",
            puranicLegend = "'Labh' signifies spiritual and material benefit and auspicious gain. merchants who did not open their shops on Diwali Padwa reopen on Labh Pancham by inscribing 'Shubh-Labh' and a Swastika on new ledgers, while Jain devotees revere sacred Agamas on Gnan Panchami.",
            presidingDeity = "Lord Shubh-Labh Ganapati, Goddess Mahalakshmi & Goddess Saraswati",
            sacredMantraSanskrit = "ॐ श्रीं ह्रीं क्लीं ग्लौं गं गणपतये वर वरद सर्वजनं मे वशमानय स्वाहा । शुभं करोतु कल्याणमारोग्यं सुखसम्पदाम् ॥",
            sacredMantraTransliteration = "Om Shreem Hreem Kleem Glaum Gam Ganapataye Vara Varada Sarvajanam Me Vashamanaya Svaha | Shubham Karotu Kalyanam Arogyam Sukha-Sampadam ||",
            sacredMantraMeaning = "Salutations to Lord Ganapati and Goddess Mahalakshmi on Labh Panchami; bless our undertakings with ethical prosperity, wisdom, and well-being.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Panchami (5th Tithi) • Morning Purvahna Shubh-Labh Lagna",
            vratAndNaivedyaGuide = "Draw a Kanku Swastika and write 'Shree 1|' and 'Shubh-Labh' on new account books or work diaries, worship scales and computers, and distribute sweets to colleagues and neighbors."
        ),
        "gopashtami" to FestivalShastraDetails(
            whyCelebrated = "Observed on Kartika Shukla Ashtami when Nanda Maharaja first entrusted young Sri Krishna and Balarama with tending the adult cows (Gopas) in Vrindavan, honoring Gau-Mata as the embodiment of selfless nourishment and dharma.",
            puranicLegend = "In the Srimad Bhagavatam and Padma Purana, prior to Kartika Shukla Ashtami, young Krishna tended only the calves (Vatsa). On Gopashtami, He stepped barefoot into the Vrindavan pastures as Govinda—protector and friend of the cows.",
            presidingDeity = "Bhagavan Sri Govinda (Gopala Krishna) & Surabhi Gau-Mata",
            sacredMantraSanskrit = "नमो गोभ्यः श्रीमतीभ्यः सौरभेयीभ्य एव च । नमो ब्रह्मसुताभ्यश्च पवित्राभ्यो नमो नमः ॥ ॐ गोपालाय नमः ॥",
            sacredMantraTransliteration = "Namo Gobhyah Srimatibhyah Saurabheyibhya Eva Cha | Namo Brahmasutabhyash Cha Pavitrabhyo Namo Namaha || Om Gopalaya Namaha ||",
            sacredMantraMeaning = "Salutations to the sacred, auspicious cows descended from Surabhi, embodiments of purity, and to Lord Gopala.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Ashtami (8th Tithi) • Pratah Gau-Seva & Godhuli Puja",
            vratAndNaivedyaGuide = "Bathe and adorn cows and calves with turmeric, Roli, flower garlands, Perform clockwise Parikrama, and offer green fodder, jaggery, soaked gram, and sweet Puas (Gogras)."
        ),
        "akshaya_navami_amla_navami" to FestivalShastraDetails(
            whyCelebrated = "Celebrated on Kartika Shukla Navami as Satya Yugadi Tithi (the dawn of Satya Yuga), worshipping the sacred Amla (Indian Gooseberry) tree as the living abode of Lakshmi-Narayana, alongside Bengal's grand Jagaddhatri Puja.",
            puranicLegend = "In the Padma Purana and Skanda Purana, on Kartika Shukla Navami, Goddess Lakshmi worshipped the Amla tree—wherein Brahma abides in the roots, Vishnu in the trunk, and Shiva in the branches—and cooked sattvik prasad under its shade to feed the needy.",
            presidingDeity = "Dhatri (Amla) Lakshmi-Narayana & Devi Jagaddhatri",
            sacredMantraSanskrit = "धात्रीदेवि नमस्तुभ्यं सर्वपापक्षयङ्करि । पुत्रान्देहि महाप्राज्ञे यशो देहि नमोऽस्तु ते ॥ ॐ जगद्धात्र्यै नमः ॥",
            sacredMantraTransliteration = "Dhatridevi Namastubhyam Sarvapapa-Kshayankari | Putran Dehi Mahaprajne Yasho Dehi Namo'stu Te || Om Jagaddhatryai Namaha ||",
            sacredMantraMeaning = "Salutations to Goddess Dhatri (the sacred Amla tree) and Mother Jagaddhatri, who remove all transgressions and bestow health, longevity, and imperishable merit.",
            shastraTithiRule = "Kartika Masa • Shukla Paksha • Navami (9th Tithi) • Purvahna Amla Parikrama & Trikala Jagaddhatri Puja",
            vratAndNaivedyaGuide = "Pour raw milk at the roots of an Amla tree, wind raw cotton thread 7 or 108 times around its trunk, perform camphor Aarti, donate a pumpkin (Kushmanda) with coins, and partake of a family meal beneath the Amla tree."
        ),
        "kalabhairava_jayanti" to FestivalShastraDetails(
            whyCelebrated = "Commemorates the appearance of Lord Kaal Bhairav—the fierce yet compassionate guardian of Kashi (Kashi Kotwal) and ruler of Time (Kala)—on Margashirsha/Kartika Krishna Ashtami.",
            puranicLegend = "In the Shiva Purana (Vidyeshwara Samhita), Lord Shiva manifested as Mahakala Bhairava on Krishna Ashtami to humble false pride and entrusted Him with eternal guardianship of Varanasi, where every pilgrim seeks Bhairava's permission and blessing.",
            presidingDeity = "Lord Sri Kaal Bhairav (Mahakala Bhairava)",
            sacredMantraSanskrit = "देवराजसेव्यमानपावनाङ्घ्रिपङ्कजं व्यालयज्ञसूत्रमिन्दुशेखरं कृपाकरम् । नारदादियोगिवृन्दवन्दितं दिगम्बरं काशिकापुराधिनाथ कालभैरवं भजे ॥",
            sacredMantraTransliteration = "Devaraja-Sevyamana-Pavananghripankajam Vyala-Yajnasutram Indusekharam Kripakaram | Naradadi-Yogivrinda-Vanditam Digambaram Kashikapuradhinatha Kalabhairavam Bhaje ||",
            sacredMantraMeaning = "I worship Lord Kalabhairava, supreme Lord of Kashi, whose lotus feet are served by Indra, compassionate protector adorned with the crescent moon.",
            shastraTithiRule = "Margashirsha (Purnimanta) / Kartika (Amanta) • Krishna Paksha • Ashtami (8th Tithi) • Pradosh & Nishita Kaal",
            vratAndNaivedyaGuide = "Light a four-faced mustard-oil lamp in the evening, recite Adi Shankaracharya's Kalabhairava Ashtakam, offer black sesame, Urad Vada, Jalebi, and coconut, and feed street dogs (Bhairava's vahana)."
        ),
        "mauni_amavasya" to FestivalShastraDetails(
            whyCelebrated = "The holiest Amavasya of the Magha / Thai month—the pinnacle Shahi Snan day at Prayagraj Magh Mela & Kumbh Mela and Tamil Nadu's Thai Amavasai—observed with sacred silence (Mauna Vrat), holy river bath, and Pitru Tarpan.",
            puranicLegend = "In the Padma Purana, on Magha Krishna Amavasya, the waters of the Ganga and Triveni Sangam turn into sacred nectar (Amrita), and Manu Maharshi was born on this day. Observing Mauna (inner and outer silence) stills the restless mind (Chandra) and awakens the soul (Surya).",
            presidingDeity = "Bhagavan Sri Hari-Vishnu, Triveni Ganga & Pitru Devatas",
            sacredMantraSanskrit = "अयोध्या मथुरा माया काशी काञ्ची अवन्तिका । पुरी द्वारावती चैव सप्तैता मोक्षदायिकाः ॥ ॐ नमो भगवते वासुदेवाय । ॐ पितृभ्यो नमः ॥",
            sacredMantraTransliteration = "Ayodhya Mathura Maya Kashi Kanchi Avantika | Puri Dwaravati Chaiva Saptaita Mokshadayikah || Om Namo Bhagavate Vasudevaya | Om Pitribhyo Namaha ||",
            sacredMantraMeaning = "Remembering the seven sacred liberating tirthas along with Lord Vasudeva and our revered ancestors during the silent Mauni Amavasya bath.",
            shastraTithiRule = "Magha (Purnimanta) / Pausha (Amanta) • Krishna Paksha • Amavasya (15th Tithi) • Brahma Muhurat Snan & Noon Tarpan",
            vratAndNaivedyaGuide = "Bathe at Brahma Muhurat in silence without speaking a word before Surya Arghya, perform black sesame (Til) Tarpan for ancestors, and donate Til-Laddus, warm blankets, and ghee."
        ),
        "amalaki_ekadashi_rangbhari" to FestivalShastraDetails(
            whyCelebrated = "Observed on Phalguna Shukla Ekadashi to worship Lord Vishnu residing within the sacred Amla tree (Amalaki Ekadashi) and celebrated in Varanasi as Rangbhari Ekadashi—when Lord Shiva and Goddess Parvati inaugurate the festival of Holi with Gulal.",
            puranicLegend = "In the Brahmanda Purana, the sacred Amalaki tree sprang from Lord Vishnu's primordial contemplation; in Kashi tradition, on Phalguna Shukla Ekadashi after Maha Shivaratri, Lord Vishwanath brings Mata Gaura home in a silver palanquin (Gauna) amidst clouds of rose-scented Abir and Gulal.",
            presidingDeity = "Lord Sri Lakshmi-Narayana (Amalaki Swarupa) & Lord Kashi Vishwanath-Gaura",
            sacredMantraSanskrit = "संसारसागरोत्तारकारिणे ज्ञानरूपिणे । आमलकीवृक्षरूपाय विष्णवे ते नमो नमः ॥ ॐ नमः पार्वती पतये हर हर महादेव ॥",
            sacredMantraTransliteration = "Samsara-Sagarottara-Karine Jnanarupine | Amalaki-Vriksharupaya Vishnave Te Namo Namaha || Om Namah Parvati Pataye Har Har Mahadev ||",
            sacredMantraMeaning = "Salutations to Lord Vishnu manifesting in the sacred Amalaki tree to carry devotees across the ocean of samsara, and to Lord Shiva, consort of Parvati.",
            shastraTithiRule = "Phalguna Masa • Shukla Paksha • Ekadashi (11th Tithi) • Morning Amla Puja & Midday Gulal Arpan",
            vratAndNaivedyaGuide = "Worship an Amla tree (or offer Amla fruits to Shaligram/Vishnu), offer pink/red herbal Gulal and Thandai/fruits to Shiva-Parvati, and observe Ekadashi Phalahar fast."
        ),
        "cheti_chand_jhulelal" to FestivalShastraDetails(
            whyCelebrated = "The auspicious Sindhi Lunar New Year on Chaitra Shukla Dwitiya celebrating the appearance day of Ishta Dev Sai Jhulelal (Uderolal), the compassionate avatar of Varuna Deva (Lord of the Waters), upon the sighting of the Chaitra crescent moon.",
            puranicLegend = "In 10th-century Sindh, when peaceful devotees prayed on the banks of the Sindhu River for forty days of fasting and devotion, Varuna Deva appeared from the river waves seated upon a Palla fish and lotus as infant Uderolal (Jhulelal) to restore religious harmony and fearlessness.",
            presidingDeity = "Ishta Dev Sai Jhulelal (Uderolal / Daryah Shah / Varuna Deva)",
            sacredMantraSanskrit = "ॐ जलपतये वरुणाय झूलेलालाय नमः । आयो लाल सभई चओ झूलेलाल । बेड़ा पार करणहार झूलेलाल ॥",
            sacredMantraTransliteration = "Om Jalapataye Varunaya Jhulelalaya Namaha | Aayo Lal Sabhai Chao Jhulelal | Beda Paar Karanhaar Jhulelal ||",
            sacredMantraMeaning = "Salutations to Lord Varuna manifesting as Sai Jhulelal, Lord of the Sacred Waters who carries every devotee's boat safely across all storms.",
            shastraTithiRule = "Chaitra Masa • Shukla Paksha • Dwitiya (2nd Tithi) • Pradosh Bahrana Sahib & Crescent Moon Darshan",
            vratAndNaivedyaGuide = "Prepare the sacred Bahrana Sahib (a bronze thali with a 5-wick wheat-flour oil lamp, vermilion, cardamom, fruits, and water kalash), offer Akha (sugar-coated rice with cardamom) to running water, and share sweet saffron Tahiri and Sai Bhaji."
        ),
        "putrada_ekadashi" to FestivalShastraDetails(
            whyCelebrated = "Observed on Shravana Shukla Ekadashi (and Pausha Shukla Ekadashi) for the health, wisdom, longevity, and prosperity of children, coinciding with Pavitropana Ekadashi and the start of Radha-Krishna's monsoon Jhulan Yatra.",
            puranicLegend = "In the Bhavishya Purana, Sri Krishna narrated to King Yudhishthira how King Mahijit of Mahishmati attained a virtuous, noble heir and spiritual liberation by observing Shravana Shukla Putrada Ekadashi with complete devotion to Lord Narayana.",
            presidingDeity = "Bhagavan Sri Santan Gopala & Lakshmi-Narayana (Jhulan Bihari)",
            sacredMantraSanskrit = "ॐ देवकीसुत गोविन्द वासुदेव जगत्पते । देहि मे तनयं कृष्ण त्वामहं शरणं गतः ॥",
            sacredMantraTransliteration = "Om Devakisuta Govinda Vasudeva Jagatpate | Dehi Me Tanayam Krishna Tvam Aham Sharanam Gatah ||",
            sacredMantraMeaning = "O Govinda, divine son of Devaki and Lord of the Universe, I take refuge in You; bless our family and children with health, noble character, and joy.",
            shastraTithiRule = "Shravana Masa • Shukla Paksha • Ekadashi (11th Tithi) • Morning Pavitra Arpan & Evening Jhulan Yatra",
            vratAndNaivedyaGuide = "Offer a knotted silk/cotton Pavitra thread and fresh Tulsi leaves to Lord Vishnu, gently rock Radha-Krishna on a flower-adorned Jhula (swing), chant the Santan Gopala Stotra, and observe Ekadashi Phalahar fast."
        ),
        "hariyali_amavasya" to FestivalShastraDetails(
            whyCelebrated = "A major pan-Indian monsoon Amavasya celebrated as Hariyali Amavasya in North India (planting sacred trees), Deep Puja / Gatari Amavasya in Maharashtra & Goa, Chukkala Amavasya in Andhra & Telangana, Bhimana Amavasya in Karnataka, Hareli in Chhattisgarh, and Aadi Amavasai / Karkidaka Vavu in Tamil Nadu & Kerala.",
            puranicLegend = "Falling on the new moon immediately preceding the holy month of Shravana, this day unites three sacred duties: gratitude to ancestors through noon Pitru Tarpan, ecological renewal by planting saplings in the monsoon rains, and worshipping all household lamps (Deepa-Lakshmi) to welcome Shravana's auspiciousness.",
            presidingDeity = "Deepa-Mahalakshmi, Lord Shiva-Parvati, Vriksha Devata & Pitru Devatas",
            sacredMantraSanskrit = "दीपो ज्योतिः परं ब्रह्म दीपो ज्योतिर्जनार्दनः । दीपो हरतु मे पापं सन्ध्यादीप नमोऽस्तु ते ॥ शुभं करोति कल्याणमारोग्यं धनसम्पदा ॥",
            sacredMantraTransliteration = "Deepo Jyotih Param Brahma Deepo Jyotir Janardanah | Deepo Haratu Me Papam Sandhyadeepa Namo'stu Te || Shubham Karoti Kalyanam Arogyam Dhana-Sampada ||",
            sacredMantraMeaning = "The lamp's flame represents Supreme Brahman and Lord Janardana; salutations to the sacred lamps that dispel darkness and bring health, peace, and prosperity.",
            shastraTithiRule = "Ashadha (Amanta) / Shravana (Purnimanta) • Krishna Paksha • Amavasya (15th Tithi) • Morning Vriksharopan, Noon Tarpan & Dusk Deep Puja",
            vratAndNaivedyaGuide = "Plant a sacred sapling (Tulsi, Peepal, Amla, Bilva, or Neem) in the morning, offer sesame-water Pitru Tarpan at noon, and at dusk clean all brass/silver household lamps, arrange them on a rangoli chowki, and offer sweet steamed Kanyakumari/Patoli or wheat-jaggery Malpua."
        )
    )
}


