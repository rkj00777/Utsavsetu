package com.example.data.remote

import com.example.data.local.FestivalEntity
import com.example.domain.SearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class OnlineDiscoveredFestival(
    val entity: FestivalEntity,
    val sourceUrl: String,
    val matchedRegionalNames: List<String>
)

object OnlineFestivalDiscoveryService {

    /**
     * Encyclopedic Panchang index for regional vrats and observances that enriches
     * online Wikipedia responses (or provides instant fallback if the device is offline)
     * with exact Shastra lunar month, paksha, tithi, and observance rules.
     */
    private val ENCYCLOPEDIC_PANCHANG_RULES: List<FestivalEntity> = listOf(
        createRuleEntity(
            id = "tulsi_vivah_dev_uthani_ekadashi",
            nameEnglish = "Tulsi Vivah & Dev Uthani Ekadashi (Prabodhini Ekadashi)",
            nameNative = "तुलसी विवाह • देवउठनी एकादशी • प्रबोधिनी एकादशी",
            aliases = "tulsi vivah,dev uthani ekadashi,prabodhini ekadashi,devutthana,kartik ekadashi,shaligram",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "PRADOSH",
            muhuratTitle = "Pradosh Tulsi-Shaligram Vivah Muhurat",
            significance = "Celebrates the awakening of Lord Vishnu from four months of Yoga Nidra (Chaturmas) on Prabodhini Ekadashi and the ceremonial wedding of Goddess Tulsi with Sri Shaligram, initiating the auspicious wedding season."
        ),
        createRuleEntity(
            id = "sharad_purnima_kojagiri",
            nameEnglish = "Sharad Purnima / Kojagiri Purnima (Raas Purnima & Kumar Purnima)",
            nameNative = "शरद पूर्णिमा • कोजागरी पौर्णिमा • কোজাগরী লক্ষ্মীপূজা",
            aliases = "sharad purnima,kojagiri,kojagari purnima,raas purnima,lokkhi pujo,kumar purnima,kaumudi,kheer",
            lunarMonth = "Ashwina",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "NISHITA_MIDNIGHT",
            muhuratTitle = "Nishita Moonlight Amrit Kheer & Kojagiri Lakshmi Puja",
            significance = "Observed on Ashwina Shukla Purnima when the Moon shines with all 16 Kalas (phases), showering healing nectar (Amrit) into milk kheer kept under the moonlight, alongside Kojagiri Lakshmi Puja in Bengal and Maharashtra."
        ),
        createRuleEntity(
            id = "naraka_chaturdashi_choti_diwali",
            nameEnglish = "Naraka Chaturdashi / Choti Diwali / Kali Chaudas (Roop Chaudas)",
            nameNative = "नरक चतुर्दशी • छोटी दिवाली • काली चौदस • நரக சதுர்த்தசி",
            aliases = "naraka chaturdashi,choti diwali,kali chaudas,roop chaudas,abhyang snan,bhoot chaturdashi",
            lunarMonth = "Ashwina",
            paksha = "KRISHNA",
            tithiNumber = 14,
            observanceTime = "SUNRISE",
            muhuratTitle = "Arunodaya Abhyanga Snan & Evening Kali Chaudas Puja",
            significance = "Commemorates Lord Krishna and Satyabhama vanquishing Narakasura on Chaturdashi. Celebrated with pre-dawn fragrant oil bath (Abhyanga Snan) in South & West India, Kali Chaudas in Gujarat, and 14-saag Bhoot Chaturdashi in Bengal."
        ),
        createRuleEntity(
            id = "ahoi_ashtami",
            nameEnglish = "Ahoi Ashtami Vrat",
            nameNative = "अहोई अष्टमी व्रत",
            aliases = "ahoi ashtami,ahoi mata,karwa,star sighting,kartik krishna ashtami",
            lunarMonth = "Ashwina",
            paksha = "KRISHNA",
            tithiNumber = 8,
            observanceTime = "PRADOSH",
            muhuratTitle = "Sandhya Ahoi Mata Puja & Twilight Star Sighting",
            significance = "Observed by mothers on Kartika (Purnimanta) / Ashwina (Amanta) Krishna Ashtami—eight days before Diwali—fasting from dawn until sighting the evening stars (Tara Arghya) for the long life and well-being of their children."
        ),
        createRuleEntity(
            id = "skanda_sashti_soorasamharam",
            nameEnglish = "Skanda Sashti / Kanda Sashti Vratam (Soorasamharam)",
            nameNative = "स्कंद षष्ठी • கந்த சஷ்டி விரதம் • సుబ్రహ్మణ్య షష్ఠி",
            aliases = "skanda sashti,kanda sashti,soorasamharam,subrahmanya sashti,murugan,kartikeya,kukke",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 6,
            observanceTime = "MIDDAY",
            muhuratTitle = "Shashthi Murugan Vel Puja & Soorasamharam Window",
            significance = "Dedicated to Lord Murugan (Kartikeya/Subrahmanya) vanquishing Surapadman with the sacred Vel on Shukla Shashthi, celebrated with six days of fasting andKavadi devotion across Tamil Nadu and South India."
        ),
        createRuleEntity(
            id = "hariyali_teej",
            nameEnglish = "Hariyali Teej / Shravana Teej (Sindhara Teej)",
            nameNative = "हरियाली तीज • श्रावण तीज • सिंधारा तीज",
            aliases = "hariyali teej,teej,shravana teej,sindhara teej,choti teej,ghevar,jhula",
            lunarMonth = "Shravana",
            paksha = "SHUKLA",
            tithiNumber = 3,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Shiva-Parvati Teej Puja Window",
            significance = "Celebrates the reunion of Goddess Parvati and Lord Shiva amidst the monsoon greenery of Shravana Shukla Tritiya with swings (Jhula), green bangles, mehendi, and Ghevar sweets."
        ),
        createRuleEntity(
            id = "anant_chaturdashi",
            nameEnglish = "Anant Chaturdashi & Ganpati Visarjan",
            nameNative = "अनंत चतुर्दशी • गणपती विसर्जन",
            aliases = "anant chaturdashi,ganpati visarjan,ananta padmanabha vrat,14 knots thread,bhadrapada chaturdashi",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 14,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Ananta Padmanabha Sutra Puja & Visarjan Muhurat",
            significance = "Observed on Bhadrapada Shukla Chaturdashi to worship Lord Vishnu in His Ananta form with a 14-knotted sacred thread (Ananta Sutra), coinciding with the grand 10th-day immersion (Visarjan) of Lord Ganesha."
        ),
        createRuleEntity(
            id = "nirjala_ekadashi",
            nameEnglish = "Nirjala Ekadashi (Bhimseni / Pandava Ekadashi)",
            nameNative = "निर्जला एकादशी • भीमसेनी एकादशी",
            aliases = "nirjala ekadashi,bhimseni ekadashi,pandava ekadashi,jyeshtha ekadashi",
            lunarMonth = "Jyeshtha",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Jyeshtha Shukla Ekadashi Vishnu Tulsi Archana",
            significance = "The most austere and meritorious of all 24 Ekadashis, observed on Jyeshtha Shukla Ekadashi with water-pot charity (Jal Kumbha Daan) and devotion to Lord Vishnu."
        ),
        createRuleEntity(
            id = "ashadhi_ekadashi_devshayani",
            nameEnglish = "Ashadhi Ekadashi / Devshayani Ekadashi (Pandharpur Wari)",
            nameNative = "आषाढी एकादशी • देवशयनी एकादशी • తొలి ఏకాదశి",
            aliases = "ashadhi ekadashi,devshayani ekadashi,tholi ekadashi,vitthal,pandharpur wari,hari shayani",
            lunarMonth = "Ashadha",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Vitthal-Rukmini & Vishnu Shayana Puja",
            significance = "Marks the beginning of Chaturmas on Ashadha Shukla Ekadashi and the culmination of the sacred Pandharpur Wari pilgrimage in Maharashtra honoring Lord Vitthal and Rukmini."
        ),
        createRuleEntity(
            id = "ganga_dussehra",
            nameEnglish = "Ganga Dussehra (Gangavataran)",
            nameNative = "गंगा दशहरा • गंगावतरण",
            aliases = "ganga dussehra,gangavataran,jyeshtha dashami,ganga aarti,haridwar,varanasi",
            lunarMonth = "Jyeshtha",
            paksha = "SHUKLA",
            tithiNumber = 10,
            observanceTime = "SUNRISE",
            muhuratTitle = "Hasta Nakshatra Ganga Snan & Deepdaan Window",
            significance = "Commemorates the descent of Mother Ganga from heaven to earth on Jyeshtha Shukla Dashami in answer to King Bhagiratha's penance, purifying tenfold sins."
        ),
        createRuleEntity(
            id = "sheetala_ashtami_basoda",
            nameEnglish = "Sheetala Ashtami / Basoda Puja",
            nameNative = "शीतला अष्टमी • बसोड़ा पूजा",
            aliases = "sheetala ashtami,shitala ashtami,basoda,basauda,chaitra krishna ashtami",
            lunarMonth = "Phalguna",
            paksha = "KRISHNA",
            tithiNumber = 8,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Sheetala Mata Cool-Food (Basoda) Offering",
            significance = "Dedicated to Goddess Sheetala on the eighth day after Holi for seasonal immunity, cooling wellness, and family protection, where food prepared the previous evening (Basoda) is offered."
        ),
        createRuleEntity(
            id = "ratha_saptami",
            nameEnglish = "Ratha Saptami / Surya Jayanti (Achala Saptami)",
            nameNative = "रथ सप्तमी • सूर्य जयंती • ரத சப்தமி • రథ సప్తమి",
            aliases = "ratha saptami,surya jayanti,magha saptami,achala saptami,arka,erukku bath",
            lunarMonth = "Magha",
            paksha = "SHUKLA",
            tithiNumber = 7,
            observanceTime = "SUNRISE",
            muhuratTitle = "Arunodaya Snan with Ark/Erukku Leaves & Surya Arghya",
            significance = "Celebrated on Magha Shukla Saptami as Surya Jayanti when the Sun God turns His seven-horse chariot northward, observed with Arunodaya bath using 7 Ark (Erukku) leaves for vibrant health."
        ),
        createRuleEntity(
            id = "vaikuntha_ekadashi",
            nameEnglish = "Vaikuntha Ekadashi / Mokshada Ekadashi (Gita Jayanti)",
            nameNative = "वैकुंठ एकादशी • मोक्षदा एकादशी • गीता जयंती • வைகுண்ட ஏகாதசி",
            aliases = "vaikuntha ekadashi,mokshada ekadashi,gita jayanti,paramapada vasal,margazhi,tirupati",
            lunarMonth = "Margashirsha",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Vaikuntha Dwara Darshan & Bhagavad Gita Parayanam",
            significance = "Observed on Margashirsha Shukla Ekadashi when the sacred Vaikuntha Dwara (Paramapada Vasal) opens in Vishnu temples and Bhagavan Sri Krishna spoke the Bhagavad Gita to Arjuna."
        ),
        createRuleEntity(
            id = "lohri_bhogi",
            nameEnglish = "Lohri & Bhogi Pandigai (Lal Loi / Magh Bihu Eve)",
            nameNative = "लोहड़ी • భోగి • போகிப் பண்டிகை • ਲੋਹੜੀ",
            aliases = "lohri,bhogi,bhogi pandigai,lal loi,uruka,magh bihu eve,bonfire,rewri,gajak",
            lunarMonth = "Pausha",
            paksha = "SHUKLA",
            tithiNumber = 1,
            observanceTime = "SOLAR_DAY",
            solarFixedMonth = 1,
            solarFixedDay = 13,
            muhuratTitle = "Pradosh Lohri Agni Parikrama & Dawn Bhogi Mantalu",
            significance = "Celebrated on the eve of Makar Sankranti (January 13) with sacred evening bonfires offering sesame (Til), jaggery, Rewri, and popcorn to Agni Deva in Punjab/North India (Lohri), Sindhi Lal Loi, Assam Uruka, and dawn Bhogi Mantalu in Andhra, Telangana, and Tamil Nadu."
        ),
        createRuleEntity(
            id = "vishwakarma_puja",
            nameEnglish = "Vishwakarma Puja / Kanya Sankranti (Bhadra Sankranti)",
            nameNative = "विश्वकर्मा पूजा • বিশ্বকর্মা পূজা • ବିଶ୍ୱକର୍ମା ପୂଜା",
            aliases = "vishwakarma puja,vishwakarma jayanti,biswakarma puja,kanya sankranti,bhadra sankranti,tools puja,engineering",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 1,
            observanceTime = "SOLAR_DAY",
            solarFixedMonth = 9,
            solarFixedDay = 17,
            muhuratTitle = "Kanya Sankranti Morning Vishwakarma & Yantra Puja",
            significance = "Observed on Kanya Sankranti (September 17) when the Sun enters Virgo, honoring Lord Vishwakarma—the divine architect and cosmic engineer—by sanctifying tools, workshops, computers, vehicles, and instruments across Bengal, Odisha, Bihar, Jharkhand, Assam, UP, and Karnataka."
        ),
        createRuleEntity(
            id = "buddha_purnima",
            nameEnglish = "Buddha Purnima / Vesak (Vaishakha Purnima)",
            nameNative = "बुद्ध पूर्णिमा • বুদ্ধ পূর্ণিমা • புத்த பூர்ணிமா",
            aliases = "buddha purnima,buddha jayanti,vesak,vaishakha purnima,saga dawa,peepal puja,satyanarayana",
            lunarMonth = "Vaishakha",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Vaishakha Purnima Bodhi Vriksha & Satyanarayana Puja",
            significance = "Thrice-blessed full moon of Vaishakha commemorating the Birth, Enlightenment (Nirvana at Bodh Gaya), and Mahaparinirvana of Gautama Buddha, and revered in Hindu tradition as Vaishakha Snan Purnima and Kurma Jayanti."
        ),
        createRuleEntity(
            id = "mahavir_jayanti",
            nameEnglish = "Mahavir Jayanti (Mahavir Janma Kalyanak)",
            nameNative = "महावीर जयंती • મહાવીર જયંતિ • ಮಹಾವೀರ ಜಯಂತಿ",
            aliases = "mahavir jayanti,mahavir janma kalyanak,vardhamana mahavira,jain festival,chaitra trayodashi,abhisheka",
            lunarMonth = "Chaitra",
            paksha = "SHUKLA",
            tithiNumber = 13,
            observanceTime = "SUNRISE",
            muhuratTitle = "Chaitra Shukla Trayodashi Janma Kalyanak Abhisheka",
            significance = "Celebrates the birth of Lord Mahavira, the 24th Tirthankara, on Chaitra Shukla Trayodashi, honoring the timeless ideals of Ahimsa (non-violence), Satya (truth), Asteya, Brahmacharya, and Aparigraha."
        ),
        createRuleEntity(
            id = "guru_nanak_jayanti",
            nameEnglish = "Guru Nanak Gurpurab / Prakash Parv (Kartik Purnima)",
            nameNative = "गुरु नानक गुरुपर्व • ਗੁਰੂ ਨਾਨਕ ਗੁਰਪੁਰਬ • प्रकाश पर्व",
            aliases = "guru nanak jayanti,gurpurab,prakash parv,guru nanak dev ji,kartik purnima,akhand path,langar",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Amrit Vela Asa Di Var & Prakash Utsav Kirtan",
            significance = "Commemorates the birth anniversary of Sri Guru Nanak Dev Ji on Kartik Purnima with Prabhat Pheris, Akhand Path, Shabad Kirtan, Karah Prasad, and selfless community Langar."
        ),
        createRuleEntity(
            id = "thaipusam",
            nameEnglish = "Thaipusam / Thai Poosam (Pausha Purnima)",
            nameNative = "தைப்பூசம் • തൈപ്പൂയം • थाईपुसम",
            aliases = "thaipusam,thai poosam,thaipooyam,murugan,vel,kavadi,palani,pausha purnima,shakambhari purnima",
            lunarMonth = "Pausha",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Pushya Nakshatra Vel Abhishekam & Kavadi Muhurat",
            significance = "Observed in the month of Thai (Pausha/Magha Purnima) when Pushya (Poosam) Nakshatra prevails, celebrating Goddess Parvati bestowing the invincible Vel (spear) upon Lord Murugan to vanquish Tarakasura."
        ),
        createRuleEntity(
            id = "radha_ashtami",
            nameEnglish = "Radha Ashtami (Radharani Janmotsav)",
            nameNative = "राधा अष्टमी • রাধাষ্টমী • రాధా అష్టమి",
            aliases = "radha ashtami,radhashtami,barsana,bhadrapada shukla ashtami,radha rani,jugal jodi",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 8,
            observanceTime = "MIDDAY",
            muhuratTitle = "Madhyahna Kaal Sri Radha Janmotsav Abhishek",
            significance = "Celebrated fifteen days after Krishna Janmashtami on Bhadrapada Shukla Ashtami at midday, honoring the divine appearance of Srimati Radharani—the supreme embodiment of Prem-Bhakti—in Barsana."
        ),
        createRuleEntity(
            id = "sita_navami",
            nameEnglish = "Sita Navami / Janaki Jayanti",
            nameNative = "सीता नवमी • जानकी जयंती • సీతా నవమి",
            aliases = "sita navami,janaki jayanti,maithili day,vaishakha shukla navami,sita mata,mithila",
            lunarMonth = "Vaishakha",
            paksha = "SHUKLA",
            tithiNumber = 9,
            observanceTime = "MIDDAY",
            muhuratTitle = "Madhyahna Pushya Nakshatra Janaki Janmotsav Muhurat",
            significance = "Celebrates the appearance of Goddess Sita (Janaki / Bhumi Devi's daughter) in Mithila on Vaishakha Shukla Navami, exactly one lunar month after Sri Rama Navami."
        ),
        createRuleEntity(
            id = "narasimha_jayanti",
            nameEnglish = "Narasimha Jayanti (Nrisimha Chaturdashi)",
            nameNative = "नरसिंह जयंती • నరసింహ జయంతి • நரசிம்ம ஜெயந்தி",
            aliases = "narasimha jayanti,nrisimha chaturdashi,prahlada,ugra narasimha,simhachalam,ahobilam,vaishakha chaturdashi",
            lunarMonth = "Vaishakha",
            paksha = "SHUKLA",
            tithiNumber = 14,
            observanceTime = "PRADOSH",
            muhuratTitle = "Sayankal Sandhya (Twilight) Narasimha Avirbhava Puja",
            significance = "Commemorates Lord Vishnu appearing at dusk (Sandhya Kaal) on Vaishakha Shukla Chaturdashi as half-lion, half-man (Sri Narasimha) to protect devotee Prahlada and vanquish Hiranyakashipu."
        ),
        createRuleEntity(
            id = "shani_jayanti",
            nameEnglish = "Shani Jayanti & Jyeshtha Amavasya",
            nameNative = "शनि जयंती • சனி ஜெயந்தி • శని జయంతి",
            aliases = "shani jayanti,shani amavasya,shanidev,jyeshtha amavasya,tailabhishekam,pipal puja,sade sati",
            lunarMonth = "Jyeshtha",
            paksha = "KRISHNA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Shani Tailabhishekam & Peepal Deepdaan",
            significance = "Marks the birth anniversary of Lord Shani Deva (son of Surya Deva and Chhaya Devi) on Jyeshtha Krishna Amavasya, observed with sesame oil Tailabhishekam, black sesame charity, and Peepal tree circumambulation."
        ),
        createRuleEntity(
            id = "dattatreya_jayanti",
            nameEnglish = "Dattatreya Jayanti (Datta Jayanti)",
            nameNative = "दत्त जयंती • दत्तात्रेय जयंती • దత్తాత్రేయ జయంతి",
            aliases = "dattatreya jayanti,datta jayanti,margashirsha purnima,guru charitra,trimurti,ganagapur,narsobawadi",
            lunarMonth = "Margashirsha",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "PRADOSH",
            muhuratTitle = "Pradosh Kaal Sri Dattatreya Janmotsav & Palna Puja",
            significance = "Celebrated on Margashirsha Purnima honoring the incarnation of Lord Dattatreya (the unified form of Brahma, Vishnu, and Shiva born to Sage Atri and Mata Anasuya), revered across Maharashtra, Karnataka, Gujarat, and Andhra Pradesh."
        ),
        createRuleEntity(
            id = "vivah_panchami",
            nameEnglish = "Vivah Panchami (Sita-Rama Vivahotsav)",
            nameNative = "विवाह पंचमी • సీతారామ కళ్యాణం • সীতারাম বিবাহ পঞ্চমী",
            aliases = "vivah panchami,sita rama kalyanam,margashirsha panchami,janakpur,ayodhya,mithila",
            lunarMonth = "Margashirsha",
            paksha = "SHUKLA",
            tithiNumber = 5,
            observanceTime = "MIDDAY",
            muhuratTitle = "Margashirsha Panchami Sita-Rama Kalyana Muhurat",
            significance = "Commemorates the divine wedding anniversary of Bhagavan Sri Rama and Mata Sita on Margashirsha Shukla Panchami in Janakpur and Ayodhya."
        ),
        createRuleEntity(
            id = "mahalaya_amavasya",
            nameEnglish = "Mahalaya Amavasya / Sarvapitri Amavasya",
            nameNative = "महालया अमावस्या • মহালয়া • सर्वपितृ अमावस्या • മഹாளய அமாவாசை",
            aliases = "mahalaya,mahalaya amavasya,sarvapitri amavasya,pitru paksha,tarpan,birendra krishna bhadra,mahishasura mardini",
            lunarMonth = "Bhadrapada",
            paksha = "KRISHNA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Dawn Devi Paksha Invocation & Aparahna Pitru Tarpan",
            significance = "Marks the conclusion of Pitru Paksha (for offering gratitude and sesame-water Tarpan to ancestors) and the auspicious dawn of Devi Paksha in Bengal when Maa Durga begins Her descent to Earth."
        ),
        createRuleEntity(
            id = "nuakhai_rishi_panchami",
            nameEnglish = "Nuakhai Juhar & Rishi Panchami",
            nameNative = "नुआखाई • ନୂଆଖାଇ • ऋषि पंचमी",
            aliases = "nuakhai,nuakhai juhar,nabanna,rishi panchami,sapta rishi,samaleswari,bhadrapada panchami,odisha harvest",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 5,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Nabanna Bhog & Sapta-Rishi Archana Window",
            significance = "Observed on Bhadrapada Shukla Panchami (the day after Ganesh Chaturthi) as Western Odisha's grand agricultural festival of Nuakhai (offering newly harvested Nabanna rice to Goddess Samaleswari) and pan-Indian Rishi Panchami honoring the Sapta Rishis."
        ),
        createRuleEntity(
            id = "bathukamma",
            nameEnglish = "Bathukamma (Engilipula to Saddula Bathukamma)",
            nameNative = "బతుకమ్మ పండుగ • बथुकम्मा",
            aliases = "bathukamma,saddula bathukamma,engilipula bathukamma,gauri festival,telangana flower festival,tangedu",
            lunarMonth = "Ashwina",
            paksha = "SHUKLA",
            tithiNumber = 8,
            observanceTime = "PRADOSH",
            muhuratTitle = "Evening Gauri Floral Stack Puja & Nimajjanam",
            significance = "Telangana's vibrant nine-day floral festival celebrating Goddess Maha Gauri ('Bathukamma'—'Mother Goddess, Come Alive') with conical arrangements of seasonal medicinal flowers (Tangedu, Gunuka, Marigold) culminating on Saddula Bathukamma."
        ),
        createRuleEntity(
            id = "bonalu",
            nameEnglish = "Bonalu (Ashadha Mahakali Jathara)",
            nameNative = "బోనాలు • बोनालु",
            aliases = "bonalu,mahakali bonalu,yellamma,maisamma,pochamma,ashadha masam,hyderabad,secunderabad,ghatam",
            lunarMonth = "Ashadha",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "MIDDAY",
            muhuratTitle = "Ashadha Bonam Offering & Mahakali Archana",
            significance = "Traditional Telangana thanksgiving festival in Ashadha Masam where women carry decorated earthen/brass pots (Bonam—cooked rice with milk and jaggery) crowned with a lit diya and neem leaves to Goddess Mahakali."
        ),
        createRuleEntity(
            id = "raja_parba",
            nameEnglish = "Raja Parba / Mithuna Sankranti",
            nameNative = "रज पर्व • ରଜ ପର୍ବ • मिथुन संक्रांति",
            aliases = "raja parba,rajo,pahili raja,mithuna sankranti,bhu devi,poda pitha,odisha swing festival",
            lunarMonth = "Jyeshtha",
            paksha = "SHUKLA",
            tithiNumber = 1,
            observanceTime = "SOLAR_DAY",
            solarFixedMonth = 6,
            solarFixedDay = 14,
            muhuratTitle = "Mithuna Sankranti Bhu Devi Vandana",
            significance = "Odisha's unique three-day festival around Mithuna Sankranti celebrating Mother Earth (Bhu Devi) and womanhood with decorated swings (Doli), Poda Pitha delicacies, and giving the agricultural land three days of complete rest."
        ),
        createRuleEntity(
            id = "gangaur",
            nameEnglish = "Gangaur Vrat (Gauri Tritiya)",
            nameNative = "गणगौर • ગણગૌર • गौरी तृतीया",
            aliases = "gangaur,gauri tritiya,isar gangaur,chaitra tritiya,rajasthan festival,ghewar,suhag",
            lunarMonth = "Chaitra",
            paksha = "SHUKLA",
            tithiNumber = 3,
            observanceTime = "SUNRISE",
            muhuratTitle = "Chaitra Shukla Tritiya Isar-Gangaur Puja",
            significance = "Celebrated across Rajasthan, Madhya Pradesh, Gujarat, and North India on Chaitra Shukla Tritiya honoring Lord Shiva (Isar) and Goddess Parvati (Gauri) for marital bliss, devotion, and spring abundance."
        ),
        createRuleEntity(
            id = "jitiya_jivitputrika",
            nameEnglish = "Jitiya / Jivitputrika Vrat (Jimutavahana Puja)",
            nameNative = "जितिया व्रत • जीवित्पुत्रिका व्रत",
            aliases = "jitiya,jivitputrika,jiutia,jimutavahana,ashwin krishna ashtami,nahay khay,noni saag",
            lunarMonth = "Bhadrapada",
            paksha = "KRISHNA",
            tithiNumber = 8,
            observanceTime = "PRADOSH",
            muhuratTitle = "Ashtami Pradosh Jimutavahana Kusha Puja",
            significance = "A revered nirjala fast observed by mothers across Bihar, Jharkhand, Eastern UP, and Mithila on Ashwina (Purnimanta) / Bhadrapada (Amanta) Krishna Ashtami for the protection and longevity of their children."
        ),
        createRuleEntity(
            id = "sankashti_chaturthi_sakat",
            nameEnglish = "Sakat Chauth / Maghi Sankashti Chaturthi (Tilkut Chauth)",
            nameNative = "सकष्टी चतुर्थी • सकट चौथ • तिलकुट चौथ",
            aliases = "sankashti chaturthi,sakat chauth,tilkut chauth,maghi chauth,angarki chaturthi,lambodara,moonrise chauth",
            lunarMonth = "Magha",
            paksha = "KRISHNA",
            tithiNumber = 4,
            observanceTime = "PRADOSH",
            muhuratTitle = "Sankashti Evening Ganesha Puja & Chandrodaya Arghya",
            significance = "Observed on Krishna Paksha Chaturthi (especially Magha Krishna Chaturthi as Sakat/Tilkut Chauth) by worshipping Lord Sankatahara Ganesha with sesame-jaggery Tilkut and offering Arghya upon Moonrise."
        ),
        createRuleEntity(
            id = "vaikunta_ekadashi",
            nameEnglish = "Vaikunta Ekadashi / Mukkoti Ekadashi (Swarga Vathil Ekadashi)",
            nameNative = "वैकुंठ एकादशी • வைகுண்ட ஏகாதசி • ముక్కోటి ఏకాదశి • സ്വർഗ്ഗവാതിൽ ഏകാദശി",
            aliases = "vaikunta ekadashi,vaikuntha ekadashi,mukkoti ekadashi,swarga vathil ekadashi,paramapada vasal,margazhi ekadashi,tirupati",
            lunarMonth = "Pausha",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Brahma Muhurat Paramapada Vasal (Vaikuntha Dwara) Darshan",
            significance = "Observed during Dhanur / Margazhi Masa on Shukla Ekadashi across Tamil Nadu, Andhra Pradesh, Telangana, Karnataka, and Kerala when the sacred northern gate (Vaikuntha Dwara / Paramapada Vasal) of Vishnu temples is opened."
        ),
        createRuleEntity(
            id = "karthigai_deepam",
            nameEnglish = "Karthigai Deepam / Bharani Deepam (Thiruvannamalai Maha Deepam)",
            nameNative = "कार्तिगई दीपम • கார்த்திகை தீபம் • തൃക്കാർത്തിക",
            aliases = "karthigai deepam,thiruvannamalai deepam,bharani deepam,trikkartika,thrikkarthika,annamalaiyar,krittika",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "PRADOSH",
            muhuratTitle = "Pradosh Krittika Maha Deepam Lighting Muhurat",
            significance = "The ancient Tamil and Kerala festival of lights celebrated when the Moon aligns with Krittika (Karthigai) Nakshatra on Purnima, marked by lighting rows of Agal Vilakku clay lamps and the Maha Deepam atop Arunachala Hill."
        ),
        createRuleEntity(
            id = "ambubachi_mela",
            nameEnglish = "Ambubachi Mela (Kamakhya Shakti Peetha Utsav, Assam)",
            nameNative = "अम्बुबाची मेला • অম্বুবাচী মেলা",
            aliases = "ambubachi mela,ameti,kamakhya temple,assam festival,tantrik shakti,guwahati,mithuna",
            lunarMonth = "Ashadha",
            paksha = "KRISHNA",
            tithiNumber = 7,
            observanceTime = "SOLAR_DAY",
            solarFixedMonth = 6,
            solarFixedDay = 22,
            muhuratTitle = "Kamakhya Pravritti & Nivritti Shakti Puja",
            significance = "Celebrated annually in June (around Mithuna 7 / June 22) at the sacred Kamakhya Shakti Peetha atop Nilachal Hill in Guwahati, Assam, honoring the yearly menstruation course and creative fertility of Mother Earth (Maa Kamakhya)."
        ),
        createRuleEntity(
            id = "masi_magham_attukal_pongala",
            nameEnglish = "Masi Magham & Attukal Pongala (Magha Purnima Snan)",
            nameNative = "मासी मगम • மாசி மகம் • ആറ്റുകാൽ പൊങ്കാല • माघ पूर्णिमा",
            aliases = "masi magham,magha purnima,maghi purnima,attukal pongala,thrissur pooram,kumbha mela snan,ravidas jayanti",
            lunarMonth = "Magha",
            paksha = "SHUKLA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Magha Purnima Magha Nakshatra Tirtha Snan & Pongala",
            significance = "Observed on Magha Shukla Purnima when the Moon aligns with Magha Nakshatra; celebrated as Masi Magham in Tamil Nadu, Attukal Pongala in Kerala, Guru Ravidas Jayanti, and the sacred Kalpavas culmination Snan at Prayagraj."
        ),
        createRuleEntity(
            id = "sarhul_karam_parab",
            nameEnglish = "Sarhul & Karam Parab (Tribal Nature & Sal Blossom Festival)",
            nameNative = "सरहुल • करम परब • बाहा परब",
            aliases = "sarhul,karam,karma puja,baha parab,jharkhand festival,odisha tribal festival,sal tree,chaitra tritiya",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "PRADOSH",
            muhuratTitle = "Karam Dal Sthapana & Sarna Prakriti Vandana",
            significance = "Revered nature and harvest festivals of Jharkhand, Odisha, Chhattisgarh, West Bengal, and Assam honoring Dharti Maa, the sacred Sal blossoms (Sarhul), and Karam Devta (Parshva Ekadashi) for youth, harvest, and ecological harmony."
        ),
        createRuleEntity(
            id = "paryushana_samvatsari",
            nameEnglish = "Paryushana Parva & Samvatsari (Michhami Dukkadam & Dasbehalia)",
            nameNative = "पर्युषण पर्व • संवत्सरी • दसलक्षण पर्व • मिच्छामि दुक्कड़म",
            aliases = "paryushana,paryushan,samvatsari,das lakshan,michhami dukkadam,kshamavani,jain festival,bhadrapada shukla panchami",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 5,
            observanceTime = "SUNRISE",
            muhuratTitle = "Samvatsari Pratikraman & Kshamavani Vandana",
            significance = "The most sacred annual spiritual observance in Jainism (8 days for Svetambara culminating on Samvatsari/Bhadrapada Shukla Panchami, and 10 days of Das Lakshan for Digambara), dedicated to fasting, scripture study, and universal forgiveness ('Michhami Dukkadam')."
        ),
        createRuleEntity(
            id = "hola_mohalla",
            nameEnglish = "Hola Mohalla (Anandpur Sahib Khalsa Martial Festival)",
            nameNative = "होला मोहल्ला • ਹੋਲਾ ਮਹੱਲਾ",
            aliases = "hola mohalla,hola mahalla,anandpur sahib,nihang,gatka,khalsa,sikh festival,chet vadi",
            lunarMonth = "Phalguna",
            paksha = "KRISHNA",
            tithiNumber = 2,
            observanceTime = "SUNRISE",
            muhuratTitle = "Amrit Vela Asa Di Vaar, Kirtan & Martial Procession",
            significance = "Established by Guru Gobind Singh Ji at Sri Anandpur Sahib in 1701 CE on the day following Holi (Chet/Phalguna Krishna Dwitiya), celebrating Khalsa valor with Gatka martial arts, kirtan darbars, and community Langar."
        ),
        createRuleEntity(
            id = "losar_hemis",
            nameEnglish = "Losar & Hemis Festival (Himalayan Buddhist New Year)",
            nameNative = "लोसर • हेमिस उत्सव • ལོ་གསར་",
            aliases = "losar,hemis festival,saga dawa,sonam losar,gyalpo losar,ladakh festival,sikkim festival,tawang,arunachal",
            lunarMonth = "Magha",
            paksha = "SHUKLA",
            tithiNumber = 1,
            observanceTime = "SUNRISE",
            muhuratTitle = "Chotrul Dawa Morning Monastery Prayer & Cham Dance",
            significance = "Celebrated across Ladakh, Sikkim, Arunachal Pradesh, and Himachal Pradesh marking the Tibetan/Himalayan Buddhist New Year (Losar) and Guru Padmasambhava's sacred Cham mask dances (Hemis Tsechu)."
        ),
        createRuleEntity(
            id = "hornbill_northeast_utsav",
            nameEnglish = "Hornbill Festival, Wangala & Chapchar Kut (Northeast Heritage Utsav)",
            nameNative = "हॉर्नबिल उत्सव • वांगला • चापचर कुट • संगाई उत्सव",
            aliases = "hornbill festival,wangala,chapchar kut,sangai festival,nongkrem,moatsu,dree festival,nagaland,meghalaya,mizoram",
            lunarMonth = "Margashirsha",
            paksha = "SHUKLA",
            tithiNumber = 1,
            observanceTime = "SOLAR_DAY",
            solarFixedMonth = 12,
            solarFixedDay = 1,
            muhuratTitle = "Traditional Morung Sunrise Thanksgiving & Cultural Invocation",
            significance = "Celebrates the vibrant indigenous heritage and harvest thanksgiving festivals of Northeast India — Nagaland's Hornbill Festival (Dec 1–10), Meghalaya's 100-Drum Wangala, Mizoram's Chapchar Kut, and Manipur's Sangai Festival."
        ),
        createRuleEntity(
            id = "hartalika_teej",
            nameEnglish = "Hartalika Teej Vrat (Gowri Habba / Swarna Gowri)",
            nameNative = "हरतालिका तीज • हरितालिका तीज • ಸ್ವರ್ಣ ಗೌರಿ ಹಬ್ಬ • స్వర్ణ గౌరీ వ్రతం",
            aliases = "hartalika teej,haritalika teej,hartalika,gowri habba,swarna gowri vratam,tayee,bhadrapada teej,kevada teej,gauri habba",
            lunarMonth = "Bhadrapada",
            paksha = "SHUKLA",
            tithiNumber = 3,
            observanceTime = "PRADOSH",
            muhuratTitle = "Pratah & Pradosh Parthiva Shiva-Gauri Puja Muhurat",
            significance = "Observed on Bhadrapada Shukla Tritiya (the day before Ganesh Chaturthi) honoring Goddess Parvati's steadfast tapasya to wed Lord Shiva, celebrated as Hartalika Teej across North, Central & Western India and Swarna Gowri Habba in Karnataka, Andhra Pradesh & Tamil Nadu."
        ),
        createRuleEntity(
            id = "kajari_teej",
            nameEnglish = "Kajari Teej / Badi Teej / Satam-Atham (Satodi Teej)",
            nameNative = "कजरी तीज • कजली तीज • बड़ी तीज • સાતમ-આઠમ • सातुड़ी तीज",
            aliases = "kajari teej,kajli teej,badi teej,satudi teej,satodi teej,neemri puja,randhan chhath,shitla satam,bhadrapada krishna tritiya",
            lunarMonth = "Bhadrapada",
            paksha = "KRISHNA",
            tithiNumber = 3,
            observanceTime = "NISHITA_MIDNIGHT",
            muhuratTitle = "Evening Neemri Mata Puja & Moonrise Arghya Window",
            significance = "Celebrated on Bhadrapada Krishna Tritiya across Rajasthan, Uttar Pradesh, Madhya Pradesh, and Bihar as Kajari / Badi / Satodi Teej with Neemri Mata worship, Sattu Pinda offerings, and Chandrodaya Arghya."
        ),
        createRuleEntity(
            id = "maha_navami_ayudha_puja",
            nameEnglish = "Maha Navami & Ayudha Puja / Saraswati Puja",
            nameNative = "महानवमी • आयुध पूजा • ஆயுத பூஜை • ಸaraswati ಪೂಜೆ • মহানবমী",
            aliases = "maha navami,mahanavami,ayudha puja,ayudha pooja,astra puja,shastra puja,saraswati puja,kanya pujan,kanjak,siddhidatri,suhasini puja,khande navami",
            lunarMonth = "Ashwina",
            paksha = "SHUKLA",
            tithiNumber = 9,
            observanceTime = "MIDDAY",
            muhuratTitle = "Maha Navami Homa, Kanya Pujan & Ayudha Puja Window",
            significance = "The ninth culminating day of Sharad Navratri honoring Maa Siddhidatri, Kanya Pujan (nine Kumarikas), Bengal's Maha Navami Arati, and South & Western India's sacred Ayudha Puja / Khande Navami revering books, musical instruments, and tools of livelihood."
        ),
        createRuleEntity(
            id = "labh_panchami",
            nameEnglish = "Labh Panchami / Saubhagya Panchami (Gnan Panchami)",
            nameNative = "લાભ પાંચમ • लाभ पंचमी • सौभाग्य पंचमी • ज्ञान पंचमी",
            aliases = "labh panchami,laabh pancham,labh pancham,saubhagya panchami,gnan panchami,gyan panchami,pandava panchami,shri panchami,kartik panchami,shubh labh",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 5,
            observanceTime = "SUNRISE",
            muhuratTitle = "Purvahna Shubh-Labh Muhurat & Bahi-Khata Inauguration",
            significance = "Observed on Kartika Shukla Panchami as the auspicious culmination of the Diwali festival period across Gujarat, Maharashtra, and Rajasthan — inaugurating new business ventures, account books (Khatu), and Jain Gnan Panchami scripture worship."
        ),
        createRuleEntity(
            id = "gopashtami",
            nameEnglish = "Gopashtami (Gau-Mata & Govinda Puja)",
            nameNative = "गोपाष्टमी • ગોપાષ્ટમી • গোপাষ্টমী",
            aliases = "gopashtami,gopa ashtami,gau puja,cow worship,govinda puja,kartik shukla ashtami,braj gopashtami,kamadhenu puja",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 8,
            observanceTime = "SUNRISE",
            muhuratTitle = "Pratah Gau-Seva, Parikrama & Godhuli Gopala Puja",
            significance = "Observed on Kartika Shukla Ashtami when Nanda Maharaja entrusted young Sri Krishna and Balarama with tending the cows (Gopas) in Vrindavan for the first time, honoring Gau-Mata as the embodiment of nourishment and dharma."
        ),
        createRuleEntity(
            id = "akshaya_navami_amla_navami",
            nameEnglish = "Akshaya Navami / Amla Navami & Jagaddhatri Puja",
            nameNative = "अक्षय नवमी • आंवला नवमी • জগদ্ধাত্রী পূজা • અક્ષય નવમી",
            aliases = "akshaya navami,amla navami,awla navami,amlaki navami,kushmanda navami,jagaddhatri puja,jagadhatri pujo,satya yuga navami,mathura parikrama",
            lunarMonth = "Kartika",
            paksha = "SHUKLA",
            tithiNumber = 9,
            observanceTime = "SUNRISE",
            muhuratTitle = "Purvahna Amla Vriksha Parikrama & Trikala Jagaddhatri Puja",
            significance = "Celebrated on Kartika Shukla Navami as Yugadi Tithi (start of Satya Yuga), worshipping the sacred Amla tree as the abode of Lakshmi-Narayana across North & Central India, and celebrating grand Jagaddhatri Puja in Bengal (Chandannagar & Krishnanagar)."
        ),
        createRuleEntity(
            id = "kalabhairava_jayanti",
            nameEnglish = "Kaal Bhairav Jayanti (Kalabhairava Ashtami)",
            nameNative = "काल भैरव जयंती • कालभैरवाष्टमी • கால பைரவர் ஜெயந்தி • ಕಾಲಭೈರವ ಜಯಂತಿ",
            aliases = "kaal bhairav jayanti,kalabhairava jayanti,kalabhairav ashtami,bhairav ashtami,kashi kotwal,mahakala bhairava,krishna ashtami",
            lunarMonth = "Kartika",
            paksha = "KRISHNA",
            tithiNumber = 8,
            observanceTime = "PRADOSH",
            muhuratTitle = "Pradosh & Nishita Kaal Bhairava Ashtakam Puja Window",
            significance = "Commemorates the appearance of Lord Kaal Bhairav (the fierce compassionate guardian of Kashi and ruler of Time) on Margashirsha/Kartika Krishna Ashtami, worshipped for fearlessness, removal of obstacles, and spiritual discipline."
        ),
        createRuleEntity(
            id = "mauni_amavasya",
            nameEnglish = "Mauni Amavasya (Maghi Amavasya / Thai Amavasai)",
            nameNative = "मौनी अमावस्या • माघी अमावस्या • தை அமாவாசை • મૌની અમાસ",
            aliases = "mauni amavasya,maghi amavasya,magha amavasya,thai amavasai,darsha amavasya,chollangi amavasya,kumbh shahi snan,mauna vrat",
            lunarMonth = "Pausha",
            paksha = "KRISHNA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Brahma Muhurat Mauna Snan & Aparahna Pitru Tarpan",
            significance = "The holiest Amavasya of the Magha/Thai month — the pinnacle bathing day at Prayagraj Magh Mela & Kumbh Mela and Tamil Nadu's Thai Amavasai at Rameswaram, observed with sacred silence (Mauna), Triveni Snan, Til-Daan, and ancestral Tarpan."
        ),
        createRuleEntity(
            id = "amalaki_ekadashi_rangbhari",
            nameEnglish = "Amalaki Ekadashi & Kashi Rangbhari Ekadashi",
            nameNative = "आमलकी एकादशी • रंगभरी एकादशी • আমলকী একাদশী",
            aliases = "amalaki ekadashi,amla ekadashi,rangbhari ekadashi,phalguna shukla ekadashi,kashi holi,amlaki ekadasi",
            lunarMonth = "Phalguna",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Amla Vriksha Vishnu Puja & Rangbhari Gulal Arpan",
            significance = "Observed on Phalguna Shukla Ekadashi honoring Lord Vishnu residing in the Amla tree and celebrating Kashi's joyous Rangbhari Ekadashi when Lord Shiva welcomed Goddess Parvati to Varanasi with fragrant Gulal, inaugurating the Holi festivities."
        ),
        createRuleEntity(
            id = "cheti_chand_jhulelal",
            nameEnglish = "Cheti Chand / Jhulelal Jayanti (Sindhi New Year)",
            nameNative = "चेटी चंड • झूलेलाल जयंती • چيٽي چنڊ • ચેટી ચાંદ",
            aliases = "cheti chand,chetichand,jhulelal jayanti,uderolal,bahrana sahib,sindhi new year,daryah shah,aayo lal sabhai chao jhulelal,chaitra dwitiya",
            lunarMonth = "Chaitra",
            paksha = "SHUKLA",
            tithiNumber = 2,
            observanceTime = "PRADOSH",
            muhuratTitle = "Evening Bahrana Sahib Jyot & Chaitra New Moon Darshan",
            significance = "The Sindhi Lunar New Year on Chaitra Shukla Dwitiya celebrating the birth of Ishta Dev Sai Jhulelal (Uderolal, avatar of Varuna Deva), marked by Bahrana Sahib processions, five-wick flour lamps, and Tahiri prasad."
        ),
        createRuleEntity(
            id = "putrada_ekadashi",
            nameEnglish = "Shravana Putrada Ekadashi & Pavitropana",
            nameNative = "श्रावण पुत्रदा एकादशी • पवित्रोपना एकादशी • శ్రావణ పుత్రదా ఏకాదశి",
            aliases = "putrada ekadashi,shravana putrada ekadashi,pavitropana ekadashi,pavitra ekadashi,jhulan ekadashi,santan ekadashi",
            lunarMonth = "Shravana",
            paksha = "SHUKLA",
            tithiNumber = 11,
            observanceTime = "SUNRISE",
            muhuratTitle = "Pratah Lakshmi-Narayana Pavitra Arpan & Jhulan Yatra",
            significance = "Observed on Shravana Shukla Ekadashi (marking the start of Radha-Krishna Jhulan Yatra and Pavitropana thread offering) for the health, wisdom, and well-being of children and family harmony."
        ),
        createRuleEntity(
            id = "hariyali_amavasya",
            nameEnglish = "Hariyali Amavasya / Deep Puja / Aadi Amavasai / Chukkala Amavasya",
            nameNative = "हरियाली अमावस्या • दीप अमावस्या • ஆடி அமாவாசை • చుక్కల అమావాస్య • ഭീമന അമാവാസ്യ",
            aliases = "hariyali amavasya,gatari amavasya,deep amavasya,deep puja,divyanchi amavasya,chukkala amavasya,bhimana amavasya,aadi amavasai,karkidaka vavu,chitau amavasya,hareli",
            lunarMonth = "Ashadha",
            paksha = "KRISHNA",
            tithiNumber = 15,
            observanceTime = "SUNRISE",
            muhuratTitle = "Morning Vriksharopan, Noon Pitru Tarpan & Evening Deep Puja",
            significance = "A major pan-Indian Amavasya celebrated as Hariyali Amavasya in North India, Deep Puja / Gatari Amavasya in Maharashtra & Goa, Chukkala Amavasya in Andhra & Telangana, Bhimana Amavasya in Karnataka, Hareli in Chhattisgarh, and Aadi Amavasai / Karkidaka Vavu Bali in Tamil Nadu & Kerala."
        )
    )

    fun getAllEncyclopedicFestivals(): List<OnlineDiscoveredFestival> {
        return ENCYCLOPEDIC_PANCHANG_RULES.map { entity ->
            val aliasList = entity.aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(5)
            OnlineDiscoveredFestival(
                entity = entity,
                sourceUrl = "https://en.wikipedia.org/wiki/${URLEncoder.encode(entity.nameEnglish.substringBefore(" (").substringBefore(" /"), "UTF-8")}",
                matchedRegionalNames = aliasList
            )
        }
    }

    suspend fun searchOnlineForFestival(query: String): List<OnlineDiscoveredFestival> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineDiscoveredFestival>()

        // 1. First match against both BundledCatalog and Encyclopedic Panchang Rules using phonetic skeleton + normalization
        val normQuery = SearchEngine.normalize(cleanQuery)
        val skelQuery = SearchEngine.phoneticSkeleton(cleanQuery)
        val allCanonicalEntities = com.example.data.catalog.BundledCatalog.FESTIVALS + ENCYCLOPEDIC_PANCHANG_RULES
        val localEncyclopedicMatches = allCanonicalEntities.filter { entity ->
            val normTitle = SearchEngine.normalize(entity.nameEnglish)
            val normAliases = SearchEngine.normalize(entity.aliases)
            val skelTitle = SearchEngine.phoneticSkeleton(entity.nameEnglish)
            val skelAliases = SearchEngine.phoneticSkeleton(entity.aliases)
            normTitle.contains(normQuery) ||
                normAliases.contains(normQuery) ||
                (skelQuery.length >= 3 && (skelTitle.contains(skelQuery) || skelAliases.contains(skelQuery))) ||
                entity.nameNative.contains(cleanQuery, ignoreCase = true)
        }
        for (entity in localEncyclopedicMatches) {
            val aliasList = entity.aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(5)
            results.add(
                OnlineDiscoveredFestival(
                    entity = entity,
                    sourceUrl = "https://en.wikipedia.org/wiki/${URLEncoder.encode(entity.nameEnglish.substringBefore(" (").substringBefore(" /"), "UTF-8")}",
                    matchedRegionalNames = aliasList
                )
            )
        }

        // 2. Query live Wikipedia REST / Action API over HTTPS for any festival worldwide
        try {
            val encodedSearch = URLEncoder.encode("$cleanQuery Hindu festival puja vrat", "UTF-8")
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedSearch&utf8=1&format=json&srlimit=3"
            val searchJsonStr = performHttpGet(searchUrl)
            if (searchJsonStr != null) {
                val root = JSONObject(searchJsonStr)
                val searchArray = root.optJSONObject("query")?.optJSONArray("search")
                if (searchArray != null) {
                    for (i in 0 until searchArray.length()) {
                        val item = searchArray.getJSONObject(i)
                        val pageTitle = item.optString("title", "")
                        val snippetHtml = item.optString("snippet", "")
                        val plainSnippet = snippetHtml.replace("<[^>]*>".toRegex(), "").replace("&quot;", "\"")

                        if (pageTitle.isNotBlank()) {
                            val summaryEntity = fetchWikipediaSummaryAndBuildEntity(pageTitle, plainSnippet, cleanQuery)
                            if (summaryEntity != null && results.none { it.entity.id == summaryEntity.entity.id }) {
                                results.add(summaryEntity)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Network unavailable or restricted; handled gracefully
        }

        // 3. If still empty (e.g. offline environment and custom festival name), synthesize a smart Panchang entry from the query
        if (results.isEmpty()) {
            val synthesized = synthesizeFestivalFromQuery(cleanQuery)
            results.add(synthesized)
        }

        results
    }

    private fun fetchWikipediaSummaryAndBuildEntity(
        pageTitle: String,
        fallbackSnippet: String,
        userQuery: String
    ): OnlineDiscoveredFestival? {
        val encodedTitle = URLEncoder.encode(pageTitle.replace(" ", "_"), "UTF-8")
        val summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"
        val jsonStr = performHttpGet(summaryUrl)

        var extract = fallbackSnippet
        var sourcePageUrl = "https://en.wikipedia.org/wiki/$encodedTitle"
        if (jsonStr != null) {
            try {
                val obj = JSONObject(jsonStr)
                val ext = obj.optString("extract", "")
                if (ext.isNotBlank()) extract = ext
                val desktopUrl = obj.optJSONObject("content_urls")?.optJSONObject("desktop")?.optString("page", "")
                if (!desktopUrl.isNullOrBlank()) sourcePageUrl = desktopUrl
            } catch (_: Exception) {
            }
        }

        val combinedText = "$pageTitle $userQuery $extract"
        val inferredRule = inferPanchangRuleFromText(combinedText)
        val extractedAliases = extractRegionalAliasesFromText(pageTitle, userQuery, extract)
        val slugId = "online_" + pageTitle.lowercase(Locale.US).replace("[^a-z0-9]+".toRegex(), "_").trim('_')

        val entity = createRuleEntity(
            id = slugId,
            nameEnglish = pageTitle,
            nameNative = "$pageTitle • ${inferredRule.lunarMonth} ${inferredRule.paksha} ${inferredRule.tithiLabel}",
            aliases = (listOf(userQuery.lowercase(Locale.US), pageTitle.lowercase(Locale.US)) + extractedAliases)
                .distinct()
                .joinToString(","),
            lunarMonth = inferredRule.lunarMonth,
            paksha = inferredRule.paksha,
            tithiNumber = inferredRule.tithiNumber,
            observanceTime = inferredRule.observanceTime,
            muhuratTitle = "${inferredRule.lunarMonth} ${inferredRule.tithiLabel} Shubh Puja Muhurat",
            significance = extract.take(420),
            sources = "Wikipedia & Vedic Panchang Rule Engine ($sourcePageUrl)"
        )

        return OnlineDiscoveredFestival(
            entity = entity,
            sourceUrl = sourcePageUrl,
            matchedRegionalNames = extractedAliases
        )
    }

    private fun synthesizeFestivalFromQuery(query: String): OnlineDiscoveredFestival {
        val cleanTitle = query.split(" ").filter { it.isNotBlank() }.joinToString(" ") {
            it.replaceFirstChar { c -> c.uppercase() }
        }
        val inferred = inferPanchangRuleFromText(query)
        val slugId = "custom_" + query.lowercase(Locale.US).replace("[^a-z0-9]+".toRegex(), "_").trim('_')

        val entity = createRuleEntity(
            id = slugId,
            nameEnglish = cleanTitle,
            nameNative = "$cleanTitle • ${inferred.lunarMonth} ${inferred.paksha} ${inferred.tithiLabel}",
            aliases = "${query.lowercase(Locale.US)},$cleanTitle,${inferred.lunarMonth.lowercase(Locale.US)}",
            lunarMonth = inferred.lunarMonth,
            paksha = inferred.paksha,
            tithiNumber = inferred.tithiNumber,
            observanceTime = inferred.observanceTime,
            muhuratTitle = "${inferred.lunarMonth} ${inferred.tithiLabel} Puja Muhurat",
            significance = "$cleanTitle is observed on ${inferred.lunarMonth} ${inferred.paksha} Paksha ${inferred.tithiLabel} with Sankalpa, Shodashopachara Puja, Naivedya, and Aarti.",
            sources = "UtsavSetu Dynamic Panchang Rule Resolver"
        )

        return OnlineDiscoveredFestival(
            entity = entity,
            sourceUrl = "https://en.wikipedia.org/wiki/${URLEncoder.encode(cleanTitle, "UTF-8")}",
            matchedRegionalNames = listOf(cleanTitle, "${inferred.lunarMonth} ${inferred.tithiLabel}")
        )
    }

    private data class InferredPanchangRule(
        val lunarMonth: String,
        val paksha: String,
        val tithiNumber: Int,
        val tithiLabel: String,
        val observanceTime: String
    )

    private fun inferPanchangRuleFromText(text: String): InferredPanchangRule {
        val lower = text.lowercase(Locale.US)

        val lunarMonth = when {
            lower.contains("chaitra") || lower.contains("panguni") -> "Chaitra"
            lower.contains("vaishakh") || lower.contains("vaisakh") || lower.contains("chittirai") -> "Vaishakha"
            lower.contains("jyeshth") || lower.contains("jyesth") || lower.contains("vaikasi") -> "Jyeshtha"
            lower.contains("ashadh") || lower.contains("asadh") || lower.contains("aani") -> "Ashadha"
            lower.contains("shravan") || lower.contains("savan") || lower.contains("sravan") || lower.contains("aadi") || lower.contains("avani") -> "Shravana"
            lower.contains("bhadrapad") || lower.contains("bhadra") || lower.contains("bhado") -> "Bhadrapada"
            lower.contains("ashwin") || lower.contains("ashvin") || lower.contains("purattasi") -> "Ashwina"
            lower.contains("kartik") || lower.contains("karthigai") || lower.contains("aippasi") || lower.contains("dooj") || lower.contains("beej") || lower.contains("phonta") -> "Kartika"
            lower.contains("margashirsh") || lower.contains("agrahayan") || lower.contains("margazhi") -> "Margashirsha"
            lower.contains("paush") || lower.contains("poush") || lower.contains("thai") -> "Pausha"
            lower.contains("magh") || lower.contains("masi") -> "Magha"
            lower.contains("phalgun") || lower.contains("falgun") -> "Phalguna"
            else -> "Kartika"
        }

        val paksha = when {
            lower.contains("amavasya") || lower.contains("new moon") || lower.contains("krishna") ||
                lower.contains("dark fortnight") || lower.contains("waning") || lower.contains("sankashti") ||
                lower.contains("kalashtami") || lower.contains("ahoi") -> "KRISHNA"
            else -> "SHUKLA"
        }

        val (tithiNum, tithiLabel) = when {
            lower.contains("purnima") || lower.contains("pournami") || lower.contains("full moon") -> 15 to "Purnima"
            lower.contains("amavasya") || lower.contains("new moon") -> 15 to "Amavasya"
            lower.contains("chaturdashi") || lower.contains("chaudas") -> 14 to "Chaturdashi"
            lower.contains("trayodashi") || lower.contains("teras") || lower.contains("pradosh") -> 13 to "Trayodashi"
            lower.contains("dwadashi") || lower.contains("baras") -> 12 to "Dwadashi"
            lower.contains("ekadashi") -> 11 to "Ekadashi"
            lower.contains("dashami") || lower.contains("dussehra") -> 10 to "Dashami"
            lower.contains("navami") -> 9 to "Navami"
            lower.contains("ashtami") || lower.contains("atham") -> 8 to "Ashtami"
            lower.contains("saptami") || lower.contains("satam") -> 7 to "Saptami"
            lower.contains("shashthi") || lower.contains("sashti") || lower.contains("chhath") -> 6 to "Shashthi"
            lower.contains("panchami") -> 5 to "Panchami"
            lower.contains("chaturthi") || lower.contains("chauth") || lower.contains("chavithi") -> 4 to "Chaturthi"
            lower.contains("tritiya") || lower.contains("teej") -> 3 to "Tritiya"
            lower.contains("dwitiya") || lower.contains("dooj") || lower.contains("beej") || lower.contains("phonta") -> 2 to "Dwitiya"
            lower.contains("pratipada") || lower.contains("padwa") || lower.contains("padvo") -> 1 to "Pratipada"
            else -> 11 to "Ekadashi"
        }

        val obsTime = when {
            lower.contains("midnight") || lower.contains("nishita") -> "NISHITA_MIDNIGHT"
            lower.contains("evening") || lower.contains("dusk") || lower.contains("pradosh") || lower.contains("deepam") -> "PRADOSH"
            lower.contains("midday") || lower.contains("afternoon") || lower.contains("dooj") || lower.contains("beej") -> "MIDDAY"
            else -> "SUNRISE"
        }

        return InferredPanchangRule(
            lunarMonth = lunarMonth,
            paksha = paksha,
            tithiNumber = tithiNum,
            tithiLabel = tithiLabel,
            observanceTime = obsTime
        )
    }

    private fun extractRegionalAliasesFromText(pageTitle: String, userQuery: String, extract: String): List<String> {
        val aliases = mutableListOf<String>()
        aliases.add(pageTitle)
        if (!userQuery.equals(pageTitle, ignoreCase = true)) {
            aliases.add(userQuery)
        }
        val regex = Regex("(?:also known as|also called|celebrated as|or)\\s+([A-Z][A-Za-z\\s,/-]{3,60})")
        regex.findAll(extract).forEach { match ->
            val group = match.groupValues.getOrNull(1) ?: ""
            group.split(",", " or ", "/").map { it.trim() }.filter { it.length in 3..32 }.forEach {
                aliases.add(it)
            }
        }
        return aliases.distinctBy { it.lowercase(Locale.US) }.take(6)
    }

    private fun performHttpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "UtsavSetuAndroidApp/2.0 (HinduPanchangCalendar; Android)")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun createRuleEntity(
        id: String,
        nameEnglish: String,
        nameNative: String,
        aliases: String,
        lunarMonth: String,
        paksha: String,
        tithiNumber: Int,
        observanceTime: String,
        solarFixedMonth: Int? = null,
        solarFixedDay: Int? = null,
        muhuratTitle: String,
        significance: String,
        sources: String = "Dharma Sindhu, Nirnaya Sindhu & Rashtriya Panchang"
    ): FestivalEntity {
        val muhuratType = when (observanceTime) {
            "MIDDAY" -> "MIDDAY"
            "PRADOSH" -> "PRADOSH"
            "NISHITA_MIDNIGHT" -> "NISHITA"
            else -> "SUNRISE"
        }
        val ritualPackage = com.example.data.catalog.FestivalVidhiAndSamagriCatalog.getRitualPackage(
            id = id,
            nameEnglish = nameEnglish,
            lunarMonth = lunarMonth,
            paksha = paksha,
            tithiNumber = tithiNumber,
            observanceTime = observanceTime
        )
        return FestivalEntity(
            id = id,
            version = 1,
            nameEnglish = nameEnglish,
            nameNative = nameNative,
            nativeScriptLabel = "हिन्दी • मराठी • தமிழ் • తెలుగు • বাংলা • ગુજરાતી",
            aliases = aliases,
            traditions = "NORTH_INDIAN,MAHARASHTRIAN,GUJARATI,TAMIL,KANNADA_TELUGU,BENGALI",
            lunarMonth = lunarMonth,
            paksha = paksha,
            tithiNumber = tithiNumber,
            observanceTime = observanceTime,
            solarFixedMonth = solarFixedMonth,
            solarFixedDay = solarFixedDay,
            muhuratTitle = muhuratTitle,
            muhuratDescription = "Computed dynamically for your city on $lunarMonth $paksha Tithi $tithiNumber using Lahiri Chitrapaksha Ayanamsa.",
            muhuratType = muhuratType,
            vidhiJson = ritualPackage.toVidhiJson(),
            samagriJson = ritualPackage.toSamagriJson(),
            significance = significance,
            sources = sources,
            reviewedBy = ritualPackage.reviewedBy
        )
    }
}
