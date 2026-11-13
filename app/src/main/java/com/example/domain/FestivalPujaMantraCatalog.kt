package com.example.domain

import com.example.domain.model.Festival
import com.example.panchang.Paksha

/**
 * Represents a sacred mantra recited during a specific stage/step of a festival's Pooja Vidhi.
 */
data class FestivalPujaMantra(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val ritualStage: String,
    val sanskrit: String,
    val transliteration: String,
    val meaning: String,
    val chantCountRecommendation: String
)

/**
 * Comprehensive Shastra-verified Pooja Mantra Catalog mapping every festival in UtsavSetu
 * to the 5 progressive ritual mantras recited during its 5-step Pooja Vidhi:
 * 1. Step 1: Purification (Achamana) & Vrat/Puja Sankalpa Mantra
 * 2. Step 2: Deity Invocation (Avahana), Kalash Sthapana & Dhyana Mantra
 * 3. Step 3: Core Moola Japa, Abhishekam & Ashtottara Archana Mantra
 * 4. Step 4: Special Festival Ritual, Arghya, Sutra-Bandhan, Katha or Naivedya Mantra
 * 5. Step 5: Maha Aarti, Mantra Pushpanjali & Kshama Prarthana Mantra
 */
object FestivalPujaMantraCatalog {

    private data class SpecialStepMantra(
        val step2Title: String,
        val step2Sanskrit: String,
        val step2Transliteration: String,
        val step2Meaning: String,
        val step4Title: String,
        val step4Sanskrit: String,
        val step4Transliteration: String,
        val step4Meaning: String,
        val step4ChantCount: String = "Recite 3 or 11 times during offering"
    )

    private val SPECIAL_FESTIVAL_MANTRAS: Map<String, SpecialStepMantra> = mapOf(
        "ganesh_chaturthi" to SpecialStepMantra(
            step2Title = "Prana Pratishtha & Ganapati Avahana Mantra",
            step2Sanskrit = "गजाननं भूतगणादिसेवितं कपित्थजम्बूफलचारुभक्षणम् । उमासुतं शोकविनाशकारकं नमामि विघ्नेश्वरपादपङ्कजम् ॥",
            step2Transliteration = "Gajananam Bhutaganadisevitam Kapittha-Jambuphala-Charu-Bhakshanam | Umasutam Shoka-Vinashakarakam Namami Vighneshwara-Padapankajam ||",
            step2Meaning = "I bow to the lotus feet of Lord Vighneshwara, son of Uma, served by celestial hosts, who removes all sorrow.",
            step4Title = "21 Durva-Yugma & Modak Samarpan Mantra",
            step4Sanskrit = "काण्डात्प्ररोहन्ती परुषः परुषस्परि एवा नो दूर्वे प्रतनु सहस्रेण शतेन च । ॐ गं गणपतये नमः दूर्वायुग्मं मोदकं च समर्पयामि ॥",
            step4Transliteration = "Kandat Prarohanti Parushah Parushaspari Eva No Durve Pratanu Sahasrena Shatena Cha | Om Gam Ganapataye Namah Durvayugmam Modakam Cha Samarpayami ||",
            step4Meaning = "O sacred Durva grass that sprouts anew from every knot, bless us with hundredfold longevity; I offer pairs of Durva and Modaks to Lord Ganapati.",
            step4ChantCount = "Chant 21 times while offering 21 Durva pairs & Modaks"
        ),
        "diwali_lakshmi_puja" to SpecialStepMantra(
            step2Title = "Lakshmi-Ganesha & Kalash Avahana Mantra",
            step2Sanskrit = "सरसिजनिलये सरोजहस्ते धवलतमांशुकगन्धमाल्यशोभे । भगवति हरिवल्लभे मनोज्ञे त्रिभुवनभूतिकरि प्रसीद मह्यम् ॥",
            step2Transliteration = "Sarasijanilaye Sarojahaste Dhavalatamamshuka-Gandhamalya-Shobhe | Bhagavati Harivallabhe Manojne Tribhuvana-Bhutikari Prasida Mahyam ||",
            step2Meaning = "O Goddess seated on the lotus, holding a lotus, dressed in luminous white and fragrant garlands, beloved of Lord Hari, bless the three worlds and grace our home.",
            step4Title = "Kubera Kosha & Deepavali Deepa-Jyoti Mantra",
            step4Sanskrit = "ॐ यक्षाय कुबेराय वैश्रवणाय धनधान्याधिपतये धनधान्यसमृद्धिं मे देहि दापय स्वाहा । शुभं करोति कल्याणमारोग्यं धनसम्पदा ॥",
            step4Transliteration = "Om Yakshaya Kuberaya Vaishravanaya Dhana-Dhanyadhipataye Dhana-Dhanya-Samriddhim Me Dehi Dapaya Svaha | Shubham Karoti Kalyanam Arogyam Dhana-Sampada ||",
            step4Meaning = "Salutations to Lord Kubera, treasurer of the gods; bestow righteous wealth, food grains, health, and auspicious light upon our household.",
            step4ChantCount = "Chant 11 times while offering Akshat, Coriander & Kheel-Batasha"
        ),
        "bhai_dooj" to SpecialStepMantra(
            step2Title = "Yamaraja & Yamuna Devi Avahana Mantra",
            step2Sanskrit = "धर्मराज नमस्तुभ्यं नमस्ते यमुनाग्रज । पाहि मां किङ्करैः सार्धं सूर्यपुत्र नमोऽस्तु ते ॥",
            step2Transliteration = "Dharmaraja Namastubhyam Namaste Yamunagraja | Pahi Mam Kinkaraih Sardham Suryaputra Namo'stu Te ||",
            step2Meaning = "Salutations to Dharmaraja Yama, elder brother of Goddess Yamuna and son of Surya; protect our family with grace.",
            step4Title = "Bhratri Forehead Tilak & Raksha Mantra",
            step4Sanskrit = "गङ्गा पूजे यमुना को यमी पूजे यमराज को, सुभद्रा पूजे कृष्ण को, गंगा यमुना नीर बहे मेरे भाई आप बढ़ें फूले फलें ॥",
            step4Transliteration = "Ganga Puje Yamuna Ko, Yami Puje Yamaraj Ko, Subhadra Puje Krishna Ko, Ganga Yamuna Neer Bahe Mere Bhai Aap Badhein Phoolein Phalein ||",
            step4Meaning = "As Ganga worships Yamuna, as Yamuna worships Yamaraja, and as Subhadra worships Sri Krishna, may my brother flourish in longevity and joy as long as Ganga and Yamuna flow.",
            step4ChantCount = "Recite while applying Roli-Chandan-Akshat Tilak on brother's forehead"
        ),
        "dhanteras" to SpecialStepMantra(
            step2Title = "Lord Dhanvantari Amrita-Kalash Dhyana Mantra",
            step2Sanskrit = "अच्युतानन्त गोविन्द विष्णो नारायणामृत । रोगान्मे नाशयाशेषानातूराणां चिकित्सक ॥",
            step2Transliteration = "Achyutananta Govinda Vishno Narayanamrita | Rogan Me Nashayashesan Aturanam Chikitsaka ||",
            step2Meaning = "O immortal Lord Narayana manifesting as the divine physician Dhanvantari, heal and remove all illnesses from our household.",
            step4Title = "Trayodashi South-Facing Yama Deepdaan Mantra",
            step4Sanskrit = "मृत्युना पाशहस्तेन कालेन भार्यया सह । त्रयोदश्यां दीपदानात्सूर्यजः प्रीयतां मम ॥",
            step4Transliteration = "Mrityuna Pashahastena Kalena Bharyaya Saha | Trayodashyam Deepadanat Suryajah Priyatam Mama ||",
            step4Meaning = "By offering this sacred lamp on Trayodashi evening, may the son of Surya (Yamaraja) be pleased and grant our family long life and protection from untimely fear.",
            step4ChantCount = "Recite 3 times while lighting the South-facing Yama Deepam at dusk"
        ),
        "govardhan_puja" to SpecialStepMantra(
            step2Title = "Giriraj Govardhan & Gopala Avahana Mantra",
            step2Sanskrit = "नमस्ते गिरिराजेन्द्र सर्वभूतसुखावह । गोवर्धन महाभाग गोकुलानन्दकारक ॥",
            step2Transliteration = "Namaste Girirajendra Sarvabhuta-Sukhavaha | Govardhana Mahabhaga Gokulanandakaraka ||",
            step2Meaning = "Salutations to the King of Mountains, Sri Govardhan Giriraj, bringer of joy to Gokul and shelter to all beings.",
            step4Title = "Annakut 56-Bhog & 7-Parikrama Mantra",
            step4Sanskrit = "पदे पदे महापुण्यं पदे पदेऽश्वमेधकम् । प्रदक्षिणां करिष्यामि गिरिराज नमोऽस्तु ते ॥",
            step4Transliteration = "Pade Pade Mahapunyam Pade Pade'shvamedhakam | Pradakshinam Karishyami Giriraja Namo'stu Te ||",
            step4Meaning = "Every step brings sacred merit; I perform clockwise Parikrama around You, O Govardhan Giriraj, offering Annakut with devotion.",
            step4ChantCount = "Chant during the 7 clockwise Parikramas around Govardhan"
        ),
        "karwa_chauth" to SpecialStepMantra(
            step2Title = "Karwa Mata (Gauri-Shiva) & Kalash Sthapana Mantra",
            step2Sanskrit = "नमः शिवायै शर्वाण्यै सौभाग्यं संततिं शुभाम् । प्रयच्छ भक्तियुक्तानां नारीणां हरवल्लभे ॥",
            step2Transliteration = "Namah Shivayai Sharvanyai Saubhagyam Santatim Shubham | Prayaccha Bhaktiyuktanam Narinam Haravallabhe ||",
            step2Meaning = "Salutations to Goddess Parvati, beloved of Lord Shiva; bestow unbroken marital harmony, health, and auspiciousness upon devoted women.",
            step4Title = "Chandrodaya Chalni (Moonrise) Arghya Mantra",
            step4Sanskrit = "ज्योत्स्नापते नमस्तुभ्यं नमस्ते ज्योतिषां पते । नमस्ते रोहिणीकान्त अर्घ्यं मे प्रतिगृह्यताम् ॥",
            step4Transliteration = "Jyotsnapate Namastubhyam Namaste Jyotisham Pate | Namaste Rohinikanta Arghyam Me Pratigrihyatam ||",
            step4Meaning = "Salutations to the Lord of Moonlight, ruler of the stars and beloved of Rohini; graciously accept my sacred Karwa water Arghya.",
            step4ChantCount = "Recite 3 or 7 times while offering Arghya to the rising Moon"
        ),
        "makar_sankranti" to SpecialStepMantra(
            step2Title = "Uttarayan Surya Mandala Dhyana Mantra",
            step2Sanskrit = "जपाकुसुमसंकाशं काश्यपेयं महाद्युतिम् । तमोऽरिं सर्वपापघ्नं प्रणतोऽस्मि दिवाकरम् ॥",
            step2Transliteration = "Japakusuma-Sankasham Kashyapeyam Mahadyutim | Tamo'rim Sarvapapaghnam Pranato'smi Divakaram ||",
            step2Meaning = "I bow to Surya Divakara, radiant like the red hibiscus flower, son of Sage Kashyapa, dispeller of darkness and negativity.",
            step4Title = "Til-Gud (Sesame-Jaggery) & Pongal Samarpan Mantra",
            step4Sanskrit = "तिलान् ददामि विप्राय सगुडान् सूर्यप्रीतये । उत्तरायणपुण्येन मम सौख्यं विवर्धताम् ॥",
            step4Transliteration = "Tilan Dadami Vipraya Sagudan Suryapritaye | Uttarayana-Punyena Mama Saukhyam Vivardhatam || (Til-Gul Ghya, God-God Bola)",
            step4Meaning = "For the delight of Surya Deva during holy Uttarayan, I offer and share sesame and jaggery; may sweetness, warmth, and harmony grow in all lives.",
            step4ChantCount = "Recite while offering Til-Gud, Khichdi, or overflowing Pongal"
        ),
        "gudi_padwa_ugadi" to SpecialStepMantra(
            step2Title = "Samvatsara Kalash & Lord Brahma Srishti Mantra",
            step2Sanskrit = "ॐ भूर्भुवः स्वः प्रजापतये नमः । नूतनसंवत्सरे सुखशान्तिसमृद्धिप्रदात्रे नमः ॥",
            step2Transliteration = "Om Bhur Bhuvah Svah Prajapataye Namaha | Nutana-Samvatsare Sukha-Shanti-Samriddhi-Pradatre Namaha ||",
            step2Meaning = "Salutations to Lord Brahma (Prajapati), creator of time and the cosmos, at the dawn of this new Samvatsara year.",
            step4Title = "Nimba-Sharkara (Ugadi Pachadi / Neem-Jaggery) Sevana Mantra",
            step4Sanskrit = "शतायुर्वज्रदेहाय सर्वसम्पत्कराय च । सर्वारिष्टविनाशाय निम्बकन्दलभक्षणम् ॥",
            step4Transliteration = "Shatayur Vajradehaya Sarva-Sampat-Karaya Cha | Sarvarishta-Vinashaya Nimbakandala-Bhakshanam ||",
            step4Meaning = "For a hundred years of resilient health, prosperity, and protection from all seasonal ailments, I partake of this sacred Neem-blossom and jaggery prasad.",
            step4ChantCount = "Recite before partaking in Ugadi Pachadi / Neem-Jaggery prasad"
        ),
        "durga_ashtami" to SpecialStepMantra(
            step2Title = "Maha Ashtami Mahishasuramardini Dhyana Mantra",
            step2Sanskrit = "या देवी सर्वभूतेषु मातृरूपेण संस्थिता । नमस्तस्यै नमस्तस्यै नमस्तस्यै नमो नमः ॥",
            step2Transliteration = "Ya Devi Sarvabhuteshu Matri-Rupena Samsthita | Namastasyai Namastasyai Namastasyai Namo Namaha ||",
            step2Meaning = "To the Divine Mother who abides in all beings as the compassionate Mother, salutations again and again.",
            step4Title = "Ashtami-Navami Sandhi Puja & Kanya Pujan Mantra",
            step4Sanskrit = "ॐ ऐं ह्रीं क्लीं चामुण्डायै विच्चे । मन्त्राक्षरमयीं लक्ष्मीं मातृणां रूपधारिणीम् । नवदुर्गात्मिकां साक्षात् कन्यामावाहयाम्यहम् ॥",
            step4Transliteration = "Om Aim Hreem Kleem Chamundaye Viche | Mantraksharamayim Lakshmim Matrinam Rupadharinim | Navadurgatmikam Sakshat Kanyam Avahayamyaham ||",
            step4Meaning = "Salutations to Devi Chamunda at the sacred Sandhi Kaal; I revere the young Kumarikas as living manifestations of the nine forms of Goddess Durga.",
            step4ChantCount = "Chant 108 times during Sandhi Puja lotus offering & Kanya Pujan"
        ),
        "navratri_ghatasthapana" to SpecialStepMantra(
            step2Title = "Mangala Kalash & Sapta-Dhanya (Barley) Ghatasthapana Mantra",
            step2Sanskrit = "ॐ आ जिघ्र कलशं मह्या त्वा विशन्त्विन्दवः । पुनरूर्जा नि वर्तस्व सा नः सहस्रं धुक्ष्वोरुधारा पयस्वती ॥",
            step2Transliteration = "Om A Jighra Kalasham Mahya Tva Vishantvindavah | Punar Urja Ni Vartasva Sa Nah Sahasram Dhukshvorudhara Payasvati ||",
            step2Meaning = "O sacred Kalash, may auspicious waters, vital energy, and abundant nourishment flow into our household throughout these nine sacred days.",
            step4Title = "Akhanda Jyoti & Nava-Durga Raksha Mantra",
            step4Sanskrit = "प्रथमं शैलपुत्री च द्वितीयं ब्रह्मचारिणी । तृतीयं चन्द्रघण्टेति कूष्माण्डेति चतुर्थकम् । पञ्चमं स्कन्दमातेति षष्ठं कात्यायनीति च । सप्तमं कालरात्रीति महागौरीति चाष्टमम् । नवमं सिद्धिदात्री च नवदुर्गाः प्रकीर्तिताः ॥",
            step4Transliteration = "Prathamam Shailaputri Cha Dwitiyam Brahmacharini | Tritiyam Chandraghanteti Kushmandeti Chaturthakam | Panchamam Skandamateti Shashtham Katyayaniti Cha | Saptamam Kalaratriti Mahagauriti Chashtamam | Navamam Siddhidatri Cha Navadurgah Prakirtitah ||",
            step4Meaning = "I bow to the nine divine manifestations of Mother Durga: Shailaputri, Brahmacharini, Chandraghanta, Kushmanda, Skandamata, Katyayani, Kalaratri, Mahagauri, and Siddhidatri.",
            step4ChantCount = "Recite after lighting the Akhand Jyot lamp"
        ),
        "maha_shivaratri" to SpecialStepMantra(
            step2Title = "Parthiva / Jyotirlinga Avahana & Panchamrit Snan Mantra",
            step2Sanskrit = "नमः शम्भवाय च मयोभवाय च नमः शङ्कराय च मयस्कराय च नमः शिवाय च शिवतराय च ॥",
            step2Transliteration = "Namah Shambhavaya Cha Mayobhavaya Cha Namah Shankaraya Cha Mayaskaraya Cha Namah Shivaya Cha Shivataraya Cha || (Sri Rudram)",
            step2Meaning = "Salutations to Lord Shambhu, bestower of bliss and auspiciousness, and to Shiva, the supreme source of grace.",
            step4Title = "Bilva Patra (Bel Leaf) Samarpan Mantra",
            step4Sanskrit = "त्रिदलं त्रिगुणाकारं त्रिनेत्रं च त्रयायुधम् । त्रिजन्मपापसंहारं एकबिल्वं शिवार्पणम् ॥",
            step4Transliteration = "Tridalam Trigunakaram Trinetram Cha Trayayudham | Trijanma-Papa-Samharam Ekabilvam Shivarpanam ||",
            step4Meaning = "Embodying the three Gunas, the three eyes of Shiva, and the three sacred weapons, I offer this three-leafed Bilva Patra to Lord Shiva.",
            step4ChantCount = "Chant 11, 21, or 108 times while offering 3-leaf Bilva Patras"
        ),
        "krishna_janmashtami" to SpecialStepMantra(
            step2Title = "Balakrishna (Laddu Gopal) Avahana & Cradle Dhyana Mantra",
            step2Sanskrit = "कस्तूरीतिलकं ललाटपटले वक्षःस्थले कौस्तुभं नासाग्रे वरमौक्तिकं करतले वेणुं करे कङ्कणम् । सर्वाङ्गे हरिचन्दनं सुललितं कण्ठे च मुक्तावलिं गोपस्त्रीपरिवेष्टितो विजयते गोपालचूडामणिः ॥",
            step2Transliteration = "Kasturi-Tilakam Lalata-Patale Vakshahsthale Kaustubham Nasagre Vara-Mauktikam Karatale Venum Kare Kankanam | Sarvange Harichandanam Sulalitam Kanthe Cha Muktavalim Gopastri-Pariveshtito Vijayate Gopala-Chudamanih ||",
            step2Meaning = "All glories to the crown jewel of Gopala, adorned with musk tilak, the Kaustubha gem, a flute in His hands, and cooling sandalwood.",
            step4Title = "Nishita Midnight Shankha-Arghya & Makhan-Mishri Mantra",
            step4Sanskrit = "जातः कंसवधार्थाय भूभारोत्तारणाय च । पाण्डवानां हितार्थाय धर्मसंस्थापनाय च । गृहाणार्घ्यं मया दत्तं देवक्या सहितो हरे ॥",
            step4Transliteration = "Jatah Kamsavadharthaya Bhubharottaranaya Cha | Pandavanam Hitarthaya Dharmasamsthapanaya Cha | Grihanarghyam Maya Dattam Devakya Sahito Hare ||",
            step4Meaning = "O Lord Hari, who incarnated at midnight to establish Dharma and uplift the world, accept this sacred Arghya along with Mother Devaki.",
            step4ChantCount = "Recite during Nishita Kaal Panchamrit Abhishek & Arghya"
        ),
        "varalakshmi_vratam" to SpecialStepMantra(
            step2Title = "Kalasha Ammavaru Avahana & Ashta-Lakshmi Mantra",
            step2Sanskrit = "लक्ष्मीं क्षीरसमुद्रराजतनयां श्रीरङ्गधामेश्वरीं दासीभूतसमस्तदेववनितां लोकैकदीपाङ्कुराम् । श्रीमन्मन्दकटाक्षलब्धविभवब्रह्मेन्द्रगङ्गाधरां त्वां त्रैलोक्यकुटुम्बिनीं सरसिजां वन्दे मुकुन्दप्रियाम् ॥",
            step2Transliteration = "Lakshmim Kshirasamudraraja-Tanayam Srirangadhameshvarim Dasibhuta-Samasta-Devavanitam Lokaika-Dipankuram | Sriman-Mandakataksha-Labdha-Vibhava-Brahmendra-Gangadharam Tvam Trailokya-Kutumbinim Sarasijam Vande Mukundapriyam ||",
            step2Meaning = "I bow to Goddess Mahalakshmi, daughter of the Milk Ocean and beloved of Mukunda, whose gracious glance blesses the three worlds.",
            step4Title = "Navagranthi (9-Knot) Toram / Charadu Wrist-Tying Mantra",
            step4Sanskrit = "बध्नामि दक्षिणे हस्ते नवसूत्रं शुभप्रदम् । पुत्रपौत्राभिवृद्धिश्च मम सौभाग्यं देहि मे रमे ॥",
            step4Transliteration = "Badhnami Dakshine Haste Navasutram Shubhapradam | Putrapautrabhivriddhishcha Mama Saubhagyam Dehi Me Rame ||",
            step4Meaning = "I tie this auspicious nine-knotted sacred thread on my right wrist; O Goddess Rama (Varalakshmi), bless our family with health, progeny, and lasting prosperity.",
            step4ChantCount = "Recite while tying the 9-knot turmeric Toram thread"
        ),
        "holi_dhulandi" to SpecialStepMantra(
            step2Title = "Bhakt Prahlad & Lord Lakshmi-Narasimha Smarana Mantra",
            step2Sanskrit = "प्रह्लादहृदयाह्लादं भक्ताविद्याविदारणम् । शरदिन्दुल्लसद्वक्त्रं लक्ष्मीकान्तं नमाम्यहम् ॥",
            step2Transliteration = "Prahlada-Hridayahladam Bhaktavidya-Vidaranam | Sharadindullasad-Vaktram Lakshmikantam Namamyaham ||",
            step2Meaning = "I bow to the Lord of Lakshmi, who delights the heart of devotee Prahlada and dispels all fear and ignorance.",
            step4Title = "Holika Dahan Pradakshina & Dol-Govinda Gulal Mantra",
            step4Sanskrit = "असृक्पाभयसंत्रस्तैः कृता त्वं होलि बालिशैः । अतस्त्वां पूजयिष्यामि भूते भूतिप्रदा भव ॥ ॐ नमो भगवते वासुदेवाय ॥",
            step4Transliteration = "Asrikpabhaya-Samtrastaih Krita Tvam Holi Balishaih | Atastvam Pujayishyami Bhute Bhutiprada Bhava || Om Namo Bhagavate Vasudevaya ||",
            step4Meaning = "We worship this sacred Holika fire during Pradosh; dispel all fear and seasonal ailments and bestow prosperity and joy upon our home.",
            step4ChantCount = "Chant during 3 or 7 clockwise Parikramas of Holika Agni"
        ),
        "raksha_bandhan" to SpecialStepMantra(
            step2Title = "Shravani Upakarma & Varuna-Narayana Vandana Mantra",
            step2Sanskrit = "शं नो वरुणः शं न इन्द्रो बृहस्पतिः । शं नो विष्णुरुरुक्रमः ॥",
            step2Transliteration = "Sham No Varunah Sham Na Indro Brihaspatih | Sham No Vishnur Urukramah ||",
            step2Meaning = "May Varuna, Indra, Brihaspati, and all-pervading Vishnu bestow peace and auspicious protection upon us.",
            step4Title = "Sacred Raksha Sutra (Rakhi) Tying Mantra",
            step4Sanskrit = "येन बद्धो बली राजा दानवेन्द्रो महाबलः । तेन त्वामनुबध्नामि रक्षे मा चल मा चल ॥",
            step4Transliteration = "Yena Baddho Bali Raja Danavendro Mahabalah | Tena Tvam Anubadhnami Rakshe Ma Chala Ma Chala ||",
            step4Meaning = "With that same bond of dharma by which the mighty King Bali was bound, I tie this sacred Raksha Sutra on your wrist; O protective cord, remain steadfast forever!",
            step4ChantCount = "Recite clearly while tying the Rakhi on the right wrist"
        ),
        "rama_navami" to SpecialStepMantra(
            step2Title = "Madhyahna Karkata-Lagna Ram Lalla Avahana Mantra",
            step2Sanskrit = "नीलाम्बुजश्यामलकोमलाङ्गं सीतासमारोपितवामभागम् । पाणौ महासायकचारुचापं नमामि रामं रघुवंशनाथम् ॥",
            step2Transliteration = "Nilambuja-Shyamala-Komalangam Sita-Samaropita-Vamabhagam | Panau Mahasayaka-Charuchapam Namami Ramam Raghuvamshanatham ||",
            step2Meaning = "I bow to Sri Rama, Lord of the Raghu dynasty, gentle as a blue lotus, with Mother Sita at His left side and the noble bow in hand.",
            step4Title = "Ram रक्षा & Panakam-Kosambari Naivedya Mantra",
            step4Sanskrit = "आपदामपहर्तारं दातारं सर्वसम्पदाम् । लोकाभिरामं श्रीरामं भूयो भूयो नमाम्यहम् ॥",
            step4Transliteration = "Apadam Apahartaram Dataram Sarva-Sampadam | Lokabhiramam Sriramam Bhuyo Bhuyo Namamyaham ||",
            step4Meaning = "Again and again I bow to Sri Rama, delight of the world, remover of all distress and bestower of all noble virtues.",
            step4ChantCount = "Chant 11 or 108 times during Midday Janmotsav Puja"
        ),
        "vijayadashami_dussehra" to SpecialStepMantra(
            step2Title = "Goddess Aparajita & Shastra/Ayudha Avahana Mantra",
            step2Sanskrit = "इमां पूजाँ मया देवि यथाशक्ति निवेदिताम् । रक्षार्थं तु समादाय व्रज स्वस्थानमुत्तमम् ॥ ॐ अपराजितायै नमः ॥",
            step2Transliteration = "Imam Pujam Maya Devi Yathashakti Niveditam | Rakshartham Tu Samadaya Vraja Svasthanam Uttamam || Om Aparajitayai Namaha ||",
            step2Meaning = "Salutations to the unconquered Goddess Aparajita; accept our heartfelt worship and grant victorious dharmic resolve in all endeavors.",
            step4Title = "Shami / Apta Leaf ('Sona') & Vidyarambham Mantra",
            step4Sanskrit = "शमी शमयते पापं शमी लोहितकण्टका । धारिण्यर्जुनबाणानां रामस्य प्रियवादिनी ॥ करिष्यमाणयात्रायां यथाकालं सुखं मया । तत्र निर्विघ्नकर्त्री त्वं भव श्रीरामपूजिते ॥",
            step4Transliteration = "Shami Shamayate Papam Shami Lohitakantaka | Dharinyarjunabananam Ramasya Priyavadini || Karishyamanayatrayam Yathakalam Sukham Maya | Tatra Nirvighnakartri Tvam Bhava Sriramapujite ||",
            step4Meaning = "O sacred Shami tree worshipped by Lord Rama and shelter of Arjuna's bow, pacify all obstacles and bless our path with victory and peace.",
            step4ChantCount = "Recite during Vijaya Muhurat Shami Puja & Sona leaf exchange"
        ),
        "chhath_puja" to SpecialStepMantra(
            step2Title = "Chhathi Maiya (Shashthi Devi) & Koshi/Soop Sthapana Mantra",
            step2Sanskrit = "षष्ठांशां प्रकृतेः शुद्धां सुप्रतिष्ठां च सुव्रताम् । सुपुत्रदां च शुभदां दयारूपां जगत्प्रसूम् । श्वेतचम्पकवर्णाभां रत्नभूषणभूषिताम् । ध्याननिष्ठां महादेवीं षष्ठीं देवीं नमाम्यहम् ॥",
            step2Transliteration = "Shashthansham Prakriteh Shuddham Supratishtham Cha Suvratam | Suputradam Cha Shubhadam Dayarupam Jagatprasum | Shvetachampaka-Varnabham Ratnabhushana-Bhushitam | Dhyananishtham Mahadevim Shashthim Devim Namamyaham ||",
            step2Meaning = "I bow to Goddess Shashthi (Chhathi Maiya), the sixth divine aspect of Prakriti, compassionate protector of children, health, and households.",
            step4Title = "Sandhya (Sunset) & Usha (Sunrise) Surya Arghya Mantra",
            step4Sanskrit = "ॐ एहि सूर्य सहस्रांशो तेजोराशे जगत्पते । अनुकम्पय मां भक्त्या गृहाणार्घ्यं दिवाकर ॥ ॐ छठी मैयायै नमः ॥",
            step4Transliteration = "Om Ehi Surya Sahasramsho Tejorashe Jagatpate | Anukampaya Mam Bhaktya Grihanarghyam Divakara || Om Chhathi Maiyayai Namaha ||",
            step4Meaning = "O thousand-rayed Surya Deva, ocean of light and life, and revered Chhathi Maiya, graciously accept this bamboo-soop Arghya offering.",
            step4ChantCount = "Chant while offering milk-and-water Arghya at Sunset & Sunrise"
        ),
        "akshaya_tritiya" to SpecialStepMantra(
            step2Title = "Lakshmi-Narayana & Lord Parashurama Dhyana Mantra",
            step2Sanskrit = "शान्ताकारं भुजगशयनं पद्मनाभं सुरेशं विश्वाधारं गगनसदृशं मेघवर्णं शुभाङ्गम् । लक्ष्मीकान्तं कमलनयनं योगिभिर्ध्यानगम्यं वन्दे विष्णुं भवभयहरं सर्वलोकैकनाथम् ॥",
            step2Transliteration = "Shantakaram Bhujagashayanam Padmanabham Suresham Vishvadharam Gaganasadrisham Meghavarnam Shubhangam | Lakshmikantam Kamalanayanam Yogibhir Dhyanagamyam Vande Vishnum Bhavabhayaharam Sarvalokaikanatham ||",
            step2Meaning = "I bow to Lord Vishnu, serene consort of Goddess Lakshmi and support of the universe, whose grace makes every noble deed imperishable (Akshaya).",
            step4Title = "Udaka-Kumbha (Water Pitcher), Sattu & Suvarna Daan Mantra",
            step4Sanskrit = "एष धर्मघटो दत्तो ब्रह्मविष्णुशिवात्मकः । अस्य प्रदानात्सफला मम सन्तु मनोरथाः ॥ अक्षय्यं कर्म मेऽस्तु सदा ॥",
            step4Transliteration = "Esha Dharmaghato Datto Brahmavishnushivatmakah | Asya Pradanat Saphala Mama Santu Manorathah || Akshayyam Karma Me'stu Sada ||",
            step4Meaning = "I offer this cooling water pitcher (Dharma-Ghata) embodying Brahma, Vishnu, and Shiva; may this charitable offering bear imperishable merit.",
            step4ChantCount = "Recite while consecrating the Jala-Kumbha, Sattu, and Gold/Barley"
        ),
        "vasant_panchami" to SpecialStepMantra(
            step2Title = "Vagdevi Saraswati Veena-Pustaka Avahana Mantra",
            step2Sanskrit = "सरस्वति महाभागे विद्ये कमललोचने । विद्यारूपे विशालाक्षि विद्यां देहि नमोऽस्तु ते ॥",
            step2Transliteration = "Sarasvati Mahabhage Vidye Kamalalochane | Vidyarupe Vishalakshi Vidyam Dehi Namo'stu Te ||",
            step2Meaning = "O auspicious Goddess Saraswati of lotus eyes, embodiment of supreme knowledge, bless us with wisdom, clarity, and learning.",
            step4Title = "Aksharabhyasam / Hatey Khori & Book/Instrument Worship Mantra",
            step4Sanskrit = "ॐ ऐं सरस्वत्यै नमः । नमस्ते शारदे देवि काश्मीरपुरवासिनि । त्वामहं प्रार्थये नित्यं विद्यादानं च देहि मे ॥",
            step4Transliteration = "Om Aim Sarasvatyai Namaha | Namaste Sharade Devi Kashmirapuravasini | Tvam Aham Prarthaye Nityam Vidyadanam Cha Dehi Me ||",
            step4Meaning = "Salutations to Goddess Sharada; I pray to You to bless our books, instruments, and studies with luminous discernment.",
            step4ChantCount = "Chant 11 or 21 times while offering yellow flowers to books & Veena"
        ),
        "hanuman_jayanti" to SpecialStepMantra(
            step2Title = "Anjaneya Prana-Shakti & Ram-Doota Dhyana Mantra",
            step2Sanskrit = "अतुलितबलधामं हेमशैलाभदेहं दनुजवनकृशानुं ज्ञानिनामग्रगण्यम् । सकलगुणनिधानं वानराणामधीशं रघुपतिप्रियभक्तं वातजातं नमामि ॥",
            step2Transliteration = "Atulitabaladhamam Hemashailabhadeham Danujavanakrishanum Jnaninam Agraganyam | Sakalagunanidhanam Vanaranam Adhisham Raghupatipriyabhaktam Vatajatam Namami ||",
            step2Meaning = "I bow to the son of the Wind God, abode of immeasurable strength, radiant like a golden mountain, foremost among the wise, and dearest devotee of Sri Rama.",
            step4Title = "Sindoor Chola, Tulsi-Mala & Sankat Mochan Mantra",
            step4Sanskrit = "अञ्जनानन्दनं वीरं जानकीशोकनाशनम् । कपीशमक्षहन्तारं वन्दे लङ्काभयङ्करम् ॥ ॐ हं हनुमते रुद्रात्मकाय हुं फट् ॥",
            step4Transliteration = "Anjananandanam Viram Janaki-Shoka-Nashanam | Kapisham Akshahantaram Vande Lankabhayankaram || Om Ham Hanumate Rudratmakaya Hum Phat ||",
            step4Meaning = "I bow to heroic Hanuman, joy of Mother Anjana and remover of Mother Sita's sorrow; may His Rudra-shakti protect us from all fear.",
            step4ChantCount = "Chant 11 times alongside Hanuman Chalisa recitation"
        ),
        "guru_purnima" to SpecialStepMantra(
            step2Title = "Maharshi Veda Vyasa & Adi-Guru Dakshinamurti Dhyana Mantra",
            step2Sanskrit = "व्यासं वसिष्ठनप्तारं शक्तेः पौत्रमकल्मषम् । पराशरात्मजं वन्दे शुकतातं तपोनिधिम् ॥",
            step2Transliteration = "Vyasam Vasishthanaptaram Shakteh Pautram Akalmasham | Parasharatmajam Vande Shukatatam Taponidhim ||",
            step2Meaning = "I bow to Maharshi Veda Vyasa, treasury of tapasya, son of Parashara, father of Shukadeva, and compiler of the Vedas.",
            step4Title = "Guru Paduka Archana & Jnana-Deepa Mantra",
            step4Sanskrit = "अज्ञानतिमिरान्धस्य ज्ञानाञ्जनशलाकया । चक्षुरुन्मीलितं येन तस्मै श्रीगुरवे नमः ॥",
            step4Transliteration = "Ajnana-Timirandhasya Jnananjana-Shalakaya | Chakshur Unmilitam Yena Tasmai Sri Gurave Namaha ||",
            step4Meaning = "Salutations to that revered Guru who opened our eyes with the collyrium of wisdom, dispelling the darkness of ignorance.",
            step4ChantCount = "Chant 11 times while offering yellow flowers at the Guru Paduka"
        ),
        "nag_panchami" to SpecialStepMantra(
            step2Title = "Ananta-Vasuki Navanaga Avahana Mantra",
            step2Sanskrit = "सर्वे नागाः प्रीयन्ताम् मे ये केचित् पृथिवीतले । ये च हेलिमरीचिस्था येऽन्तरे दिवि संस्थिताः । ये नदीषु महानागा ये सरस्वतिगामिनः । ये च वापीतडागेषु तेषु सर्वेषु वै नमः ॥",
            step2Transliteration = "Sarve Nagah Priyantam Me Ye Kechit Prithivitale | Ye Cha Helimarichistha Ye'ntare Divi Samsthitah | Ye Nadishu Mahanaga Ye Sarasvatigaminah | Ye Cha Vapitadageshu Teshu Sarveshu Vai Namah ||",
            step2Meaning = "May all Naga deities residing upon the earth, in the sky, rivers, and lakes be pleased and accept our respectful salutations.",
            step4Title = "Kshira (Raw Milk), Durva & Lahi Samarpan Mantra",
            step4Sanskrit = "नर्मदायै नमः प्रातर्नर्मदायै नमो निशि । नमोऽस्तु नर्मदे तुभ्यं त्राहि मां विषसर्पतः ॥ ॐ नवकुलनागराजाय नमः ॥",
            step4Transliteration = "Narmadayai Namah Pratar Narmadayai Namo Nishi | Namo'stu Narmade Tubhyam Trahi Mam Vishasarpatah || Om Navakula-Nagarajaya Namaha ||",
            step4Meaning = "Salutations morning and night to sacred Narmada and the nine Naga kings; bless our household with ecological harmony and fearlessness.",
            step4ChantCount = "Recite 9 times while offering milk, Durva, and puffed paddy (Lahi)"
        ),
        "hartalika_teej" to SpecialStepMantra(
            step2Title = "Parthiva Uma-Maheshwara Sthapana Mantra",
            step2Sanskrit = "उमां कान्तां रमां वाणीं सर्वाभरणभूषिताम् । हरितालिकाव्रते देवीं पार्वतीं स्थापयाम्यहम् ॥",
            step2Transliteration = "Umam Kantam Ramam Vanim Sarvabharana-Bhushitam | Haritalika-Vrate Devim Parvatim Sthapayamyaham ||",
            step2Meaning = "On this sacred Hartalika Teej Vrat, I invoke and establish Goddess Parvati alongside Lord Maheshwara on the decorated altar.",
            step4Title = "Shodasha-Patri (16 Sacred Leaves) & Suhag Pitari Mantra",
            step4Sanskrit = "शिवायै नमः । उमायै नमः । पार्वत्यै नमः । जगद्धात्र्यै नमः । जगत्प्रतिष्ठायै नमः । शान्तिरूपिण्यै नमः ॥",
            step4Transliteration = "Shivayai Namah | Umayai Namah | Parvatyai Namah | Jagaddhatryai Namah | Jagatpratishthayai Namah | Shantirupinyai Namah ||",
            step4Meaning = "Salutations to Goddess Shivaa, Uma, Parvati, Sustainer of the Universe, and Embodiment of Peace as I offer the 16 sacred leaves and Suhag Pitari.",
            step4ChantCount = "Chant while offering 16 varieties of sacred leaves (Patri) & Suhag items"
        ),
        "kajari_teej" to SpecialStepMantra(
            step2Title = "Neemri Mata & Talab (Pond) Altar Avahana Mantra",
            step2Sanskrit = "नमो देव्यै महादेव्यै शिवायै सततं नमः । नमः प्रकृत्यै भद्रायै नियताः प्रणताः स्म ताम् ॥",
            step2Transliteration = "Namo Devyai Mahadevyai Shivayai Satatam Namaha | Namah Prakrityai Bhadrayai Niyatah Pranatah Sma Tam ||",
            step2Meaning = "Constant salutations to the Supreme Goddess, auspicious Prakriti and Neemri Mata, before whom we bow with devotion.",
            step4Title = "Kajari Teej Chandrodaya (7-Rotation) Arghya Mantra",
            step4Sanskrit = "चन्द्रमा मनसो जातश्चक्षोः सूर्यो अजायत । ॐ सोमाय नमः कजरीतीजव्रतार्घ्यं समर्पयामि ॥",
            step4Transliteration = "Chandrama Manaso Jatash Chakshoh Suryo Ajayata | Om Somaya Namah Kajari-Teej-Vratarghyam Samarpayami ||",
            step4Meaning = "Salutations to Chandra Deva; holding wheat, Sattu, and fruit, I offer this sacred Kajari Teej Arghya for family peace and prosperity.",
            step4ChantCount = "Recite during evening Neemri Puja & Moonrise Arghya"
        ),
        "maha_navami_ayudha_puja" to SpecialStepMantra(
            step2Title = "Maa Siddhidatri & Saraswati-Lakshmi-Durga Avahana Mantra",
            step2Sanskrit = "सिद्धगन्धर्वयक्षाद्यैरसुरैरमरैरपि । सेव्यमाना सदा भूयात् सिद्धिदा सिद्धिदायिनी ॥",
            step2Transliteration = "Siddha-Gandharva-Yakshadair Asurair Amarair Api | Sevyamana Sada Bhuyat Siddhida Siddhidayini ||",
            step2Meaning = "May Goddess Siddhidatri, revered by celestial beings and sages alike, bestow spiritual and righteous accomplishment upon us.",
            step4Title = "Ayudha, Shastra & Pustak (Tools, Instruments & Books) Puja Mantra",
            step4Sanskrit = "या कुन्देन्दुतुषारहारधवला... ॐ महासरस्वत्यै नमः । ॐ महालक्ष्म्यै नमः । ॐ महाकाल्यै नमः । आयुधपूजां समर्पयामि ॥",
            step4Transliteration = "Om Mahasarasvatyai Namah | Om Mahalakshmyai Namah | Om Mahakalyai Namah | Ayudha-Pujam Samarpayami ||",
            step4Meaning = "Honoring Mahasaraswati, Mahalakshmi, and Mahakali, I sanctify our instruments of knowledge, craft, and livelihood.",
            step4ChantCount = "Recite while applying Haldi-Kumkum Tilak & flowers to books, tools, and vehicles"
        ),
        "labh_panchami" to SpecialStepMantra(
            step2Title = "Shubh-Labh Ganapati & Sharada Bahi-Khata Avahana Mantra",
            step2Sanskrit = "ॐ श्रीं ह्रीं क्लीं ग्लौं गं गणपतये वर वरद सर्वजनं मे वशमानय स्वाहा । एकदन्ताय विद्महे वक्रतुण्डाय धीमहि तन्नो दन्ती प्रचोदयात् ॥",
            step2Transliteration = "Om Shreem Hreem Kleem Glaum Gam Ganapataye Vara Varada Sarvajanam Me Vashamanaya Svaha | Ekadantaya Vidmahe Vakratundaya Dhimahi Tanno Danti Prachodayat ||",
            step2Meaning = "Salutations to Lord Ganapati and Goddess Mahalakshmi on Labh Panchami; illuminate our intellect and bless our new endeavors.",
            step4Title = "New Account Book (Khatu) & Jnan-Panchami Mantra",
            step4Sanskrit = "श्री गणेशाय नमः । श्री महालक्ष्म्यै नमः । श्री सरस्वत्यै नमः । लाभपञ्चमीदिने शुभलाभं भवतु ॥ नमो नाणस्स ॥",
            step4Transliteration = "Sri Ganeshaya Namah | Sri Mahalakshmyai Namah | Sri Sarasvatyai Namah | Labha-Panchami-Dine Shubha-Labham Bhavatu || (Jain: Namo Nanassa)",
            step4Meaning = "Salutations to Ganesha, Mahalakshmi, Saraswati, and Supreme Knowledge (Jnana); may this Labh Panchami inaugurate ethical prosperity and wisdom.",
            step4ChantCount = "Inscribe and recite while opening new account books or sacred texts"
        ),
        "gopashtami" to SpecialStepMantra(
            step2Title = "Bhagavan Sri Gopala & Surabhi Kamadhenu Avahana Mantra",
            step2Sanskrit = "नमो गोभ्यः श्रीमतीभ्यः सौरभेयीभ्य एव च । नमो ब्रह्मसुताभ्यश्च पवित्राभ्यो नमो नमः ॥",
            step2Transliteration = "Namo Gobhyah Srimatibhyah Saurabheyibhya Eva Cha | Namo Brahmasutabhyash Cha Pavitrabhyo Namo Namaha ||",
            step2Meaning = "Salutations to the sacred, auspicious cows descended from Kamadhenu Surabhi, embodiments of purity and nourishment.",
            step4Title = "Gau-Mata Pradakshina & Gogras (Jaggery-Fodder) Offering Mantra",
            step4Sanskrit = "गवां दृष्ट्वा नमस्कृत्य कुर्याच्चैव प्रदक्षिणम् । गावो ममाग्रतो नित्यं गावः पृष्ठत एव च । गावो मे सर्वतश्चैव गवां मध्ये वसाम्यहम् ॥",
            step4Transliteration = "Gavam Drishtva Namaskritya Kuryach Chaiva Pradakshinam | Gavo Mamagrato Nityam Gavah Prishthata Eva Cha | Gavo Me Sarvatash Chaiva Gavam Madhye Vasamyaham ||",
            step4Meaning = "Bowing to Gau-Mata and performing clockwise Parikrama, may the grace and peace of the sacred cows surround us on all sides.",
            step4ChantCount = "Recite during Gau-Parikrama and offering jaggery, green fodder, and Puas"
        ),
        "akshaya_navami_amla_navami" to SpecialStepMantra(
            step2Title = "Dhatri (Amla) Vriksha & Lakshmi-Narayana Avahana Mantra",
            step2Sanskrit = "धात्रीदेवि नमस्तुभ्यं सर्वपापक्षयङ्करि । पुत्रान्देहि महाप्राज्ञे यशो देहि नमोऽस्तु ते ॥",
            step2Transliteration = "Dhatridevi Namastubhyam Sarvapapa-Kshayankari | Putran Dehi Mahaprajne Yasho Dehi Namo'stu Te ||",
            step2Meaning = "Salutations to Goddess Dhatri (the sacred Amla tree), abode of Lord Hari and Mahalakshmi; bestow health, longevity, and imperishable virtue.",
            step4Title = "108/7 Amla Parikrama & Devi Jagaddhatri Prarthana Mantra",
            step4Sanskrit = "या सा पद्मासनस्था विपुलकटितटी पद्मपत्रायताक्षी । ॐ आधारभूते चाधारे जगद्धात्री नमोऽस्तु ते ॥",
            step4Transliteration = "Om Adharabhute Chadhare Jagaddhatri Namo'stu Te | Om Namo Bhagavate Vasudevaya ||",
            step4Meaning = "Salutations to Mother Jagaddhatri, upholder of the universe, and to Lord Vasudeva dwelling within the sacred Amla tree.",
            step4ChantCount = "Chant while winding raw cotton thread 7 or 108 times around the Amla tree"
        ),
        "kalabhairava_jayanti" to SpecialStepMantra(
            step2Title = "Kashi Kotwal Lord Kaal Bhairav Dhyana Mantra",
            step2Sanskrit = "देवराजसेव्यमानपावनाङ्घ्रिपङ्कजं व्यालयज्ञसूत्रमिन्दुशेखरं कृपाकरम् । नारदादियोगिवृन्दवन्दितं दिगम्बरं काशिकापुराधिनाथ कालभैरवं भजे ॥",
            step2Transliteration = "Devaraja-Sevyamana-Pavananghripankajam Vyala-Yajnasutram Indusekharam Kripakaram | Naradadi-Yogivrinda-Vanditam Digambaram Kashikapuradhinatha Kalabhairavam Bhaje ||",
            step2Meaning = "I worship Lord Kalabhairava, supreme guardian of Kashi, whose lotus feet are revered by Indra and Narada, merciful protector of devotees.",
            step4Title = "Kalabhairava Raksha & Mustard-Oil Deepam Mantra",
            step4Sanskrit = "अतिक्रूर महाकाय कल्पान्तदहनोपम । भैरव नमस्तुभ्यमनुज्ञां दातुमर्हसि ॥ ॐ ह्रीं बटुकाय आपदुद्धारणाय कुरु कुरु बटुकाय ह्रीं ॥",
            step4Transliteration = "Atikrura Mahakaya Kalpantadahanopama | Bhairava Namastubhyam Anujnam Datum Arhasi || Om Hreem Batukaya Apaduddharanaya Kuru Kuru Batukaya Hreem ||",
            step4Meaning = "Salutations to Lord Bhairava, remover of all perils, negativity, and obstacles; bless us with fearlessness and spiritual discipline.",
            step4ChantCount = "Chant 8 or 11 times alongside Kalabhairava Ashtakam"
        ),
        "mauni_amavasya" to SpecialStepMantra(
            step2Title = "Triveni Tirtha Avahana & Mauna Snan Mantra",
            step2Sanskrit = "गङ्गे च यमुने चैव गोदावरि सरस्वति । नर्मदे सिन्धु कावेरि जलेऽस्मिन् सन्निधिं कुरु ॥ पुष्कराद्यानि तीर्थानि गङ्गाद्याः सरितस्तथा । आगच्छन्तु पवित्राणि स्नानकाले सदा मम ॥",
            step2Transliteration = "Gange Cha Yamune Chaiva Godavari Sarasvati | Narmade Sindhu Kaveri Jale'smin Sannidhim Kuru || Pushkaradyani Tirthani Gangadyah Saritastatha | Agacchantu Pavitrani Snanakale Sada Mama ||",
            step2Meaning = "May the sacred waters of Ganga, Yamuna, Saraswati, Godavari, Narmada, Sindhu, Kaveri, and Pushkara be present in this holy Magha bath.",
            step4Title = "Krishna-Til (Black Sesame) Pitru Tarpan & Surya Arghya Mantra",
            step4Sanskrit = "ॐ पितृभ्यः स्वधायिभ्यः स्वधा नमः । गोत्रे अस्मत्पितरः सोमसदः तान् तिलोदकेन तर्पयामि ॥",
            step4Transliteration = "Om Pitribhyah Svadhayibhyah Svadha Namaha | Gotre Asmat-Pitarah Somasadah Tan Tilodakena Tarpayami ||",
            step4Meaning = "With reverence and silence (Mauna), I offer black sesame and sacred water (Tilodaka Tarpan) for the peace and blessings of our ancestors.",
            step4ChantCount = "Recite during morning Snan, Surya Arghya, and sesame Daan"
        ),
        "amalaki_ekadashi_rangbhari" to SpecialStepMantra(
            step2Title = "Amalaki Vriksha Sri Hari & Kashi Vishwanath Avahana Mantra",
            step2Sanskrit = "संसारसागरोत्तारकारिणे ज्ञानरूपिणे । आमलकीवृक्षरूपाय विष्णवे ते नमो नमः ॥",
            step2Transliteration = "Samsara-Sagarottara-Karine Jnanarupine | Amalaki-Vriksharupaya Vishnave Te Namo Namaha ||",
            step2Meaning = "Salutations to Lord Vishnu, embodiment of divine wisdom who manifests within the sacred Amalaki (Amla) tree.",
            step4Title = "Rangbhari Ekadashi Shiva-Gauri Gulal & Amla Puja Mantra",
            step4Sanskrit = "गौरीवल्लभ कामारे कालकूटविषशन । मामुद्धरापदाम्भोधेः काशीविश्वेश्वर प्रभो ॥ ॐ नमः शिवाय । ॐ नमो भगवते वासुदेवाय ॥",
            step4Transliteration = "Gaurivallabha Kamare Kalakutavishashana | Mam Uddharapadambhodheh Kashivishveshvara Prabho || Om Namah Shivaya | Om Namo Bhagavate Vasudevaya ||",
            step4Meaning = "O Lord Kashi Vishwanath, beloved of Gauri, and Lord Vasudeva, accept this offering of Amla and auspicious Abir-Gulal.",
            step4ChantCount = "Recite while offering Amla fruits and herbal Gulal to Hari-Hara"
        ),
        "cheti_chand_jhulelal" to SpecialStepMantra(
            step2Title = "Bahrana Sahib Jyot & Varuna-Jhulelal Avahana Mantra",
            step2Sanskrit = "ॐ तत्त्वायामि ब्रह्मणा वन्दमानस्तदा शास्ते यजमानो हविर्भिः । अहेडमानो वरुणेह बोध्युरुशंस मा न आयुः प्र मोषीः ॥",
            step2Transliteration = "Om Tattvayami Brahmana Vandamanas Tada Shaste Yajamano Havirbhih | Ahedamano Varuneha Bodhy Urushamsa Ma Na Ayuh Pra Moshih ||",
            step2Meaning = "With sacred Vedic hymns and offerings, we invoke Lord Varuna (Ishta Dev Jhulelal), Lord of the life-giving waters, to bless us with long life and peace.",
            step4Title = "Chaitra Dwitiya Chandra Darshan & Palav Prayer",
            step4Sanskrit = "ॐ जलपतये वरुणाय झूलेलालाय नमः । आयो लाल सभई चओ झूलेलाल । बेड़ा पार करणहार झूलेलाल ॥",
            step4Transliteration = "Om Jalapataye Varunaya Jhulelalaya Namaha | Aayo Lal Sabhai Chao Jhulelal | Beda Paar Karanhaar Jhulelal ||",
            step4Meaning = "Salutations to Sai Jhulelal, Lord of the Waters, who guides every devotee's vessel safely across the ocean of life.",
            step4ChantCount = "Chant while lighting the 5-wick flour diya (Akhand Jyot) and offering Akha"
        ),
        "putrada_ekadashi" to SpecialStepMantra(
            step2Title = "Santan Gopala & Lakshmi-Narayana Avahana Mantra",
            step2Sanskrit = "ॐ श्रीं ह्रीं क्लीं ग्लौं देवकीसुत गोविन्द वासुदेव जगत्पते । देहि मे तनयं कृष्ण त्वामहं शरणं गतः ॥",
            step2Transliteration = "Om Shreem Hreem Kleem Glaum Devakisuta Govinda Vasudeva Jagatpate | Dehi Me Tanayam Krishna Tvam Aham Sharanam Gatah ||",
            step2Meaning = "O Govinda, son of Devaki and Lord of the Universe, I take refuge in You; bless our family and children with wisdom, health, and virtue.",
            step4Title = "Shravana Pavitropana Silk-Thread & Jhulan Yatra Mantra",
            step4Sanskrit = "पवित्रं कुरु मे देव सर्वपापप्रणाशन । श्रावणेकादशीपुण्यं सफलं कुरु केशव ॥",
            step4Transliteration = "Pavitram Kuru Me Deva Sarvapapa-Pranashana | Shravanekadashi-Punyam Saphalam Kuru Keshava ||",
            step4Meaning = "O Lord Keshava, sanctify our hearts and accept this sacred Pavitropana thread and Jhulan swing offering on Shravana Putrada Ekadashi.",
            step4ChantCount = "Chant 11 or 108 times while offering Tulsi and the knotted Pavitra thread"
        ),
        "hariyali_amavasya" to SpecialStepMantra(
            step2Title = "Deepa-Lakshmi & Sapta-Vriksha Prakriti Avahana Mantra",
            step2Sanskrit = "अश्वत्थमेकं पिचुमन्दमेकं न्यग्रोधमेकं दश चिञ्चिणीकाः । कपित्थबिल्वामलकत्रयं च पञ्चाग्रवापी नरकं न पश्येत् ॥",
            step2Transliteration = "Ashvattham Ekam Pichumandam Ekam Nyagrodham Ekam Dasha Chinchinikah | Kapittha-Bilvamalaka-Trayam Cha Panchamravapi Narakam Na Pashyet ||",
            step2Meaning = "One who nurtures and plants the sacred Peepal, Neem, Banyan, Tamarind, Wood-apple, Bilva, Amla, and Mango trees brings enduring blessing to the earth and ancestors.",
            step4Title = "Divyanchi / Deep Puja (Sacred Lamp Worship) Mantra",
            step4Sanskrit = "दीपो ज्योतिः परं ब्रह्म दीपो ज्योतिर्जनार्दनः । दीपो हरतु मे पापं सन्ध्यादीप नमोऽस्तु ते ॥ शुभं करोति कल्याणमारोग्यं धनसम्पदा ॥",
            step4Transliteration = "Deepo Jyotih Param Brahma Deepo Jyotir Janardanah | Deepo Haratu Me Papam Sandhyadeepa Namo'stu Te || Shubham Karoti Kalyanam Arogyam Dhana-Sampada ||",
            step4Meaning = "The lamp's flame represents Supreme Brahman and Lord Janardana; salutations to the sacred household lamps that bring health, wisdom, and auspiciousness.",
            step4ChantCount = "Recite while worshipping all cleansed brass/silver household lamps on Deep Amavasya"
        )
    )

    private fun lunarMonthToSanskrit(monthName: String): String {
        return when (monthName.uppercase()) {
            "CHAITRA" -> "चैत्र"
            "VAISHAKHA" -> "वैशाख"
            "JYESHTHA" -> "ज्येष्ठ"
            "ASHADHA" -> "आषाढ"
            "SHRAVANA" -> "श्रावण"
            "BHADRAPADA" -> "भाद्रपद"
            "ASHVINA" -> "आश्विन"
            "KARTIKA" -> "कार्तिक"
            "MARGASHIRSHA" -> "मार्गशीर्ष"
            "PAUSHA" -> "पौष"
            "MAGHA" -> "माघ"
            "PHALGUNA" -> "फाल्गुन"
            else -> "शुभ"
        }
    }

    private fun deityToSanskrit(deityEnglish: String): String {
        val lower = deityEnglish.lowercase()
        return when {
            lower.contains("lakshmi") && lower.contains("ganesha") -> "महालक्ष्मी गणेश कुबेर देवता"
            lower.contains("ganesha") || lower.contains("ganapati") || lower.contains("vinayaka") -> "सिद्धिविनायक गणपति देवता"
            lower.contains("lakshmi") -> "महालक्ष्मी देवता"
            lower.contains("saraswati") || lower.contains("sharada") -> "महासरस्वती देवता"
            lower.contains("durga") || lower.contains("chandi") || lower.contains("shakti") || lower.contains("parvati") || lower.contains("gauri") -> "जगदम्बा दुर्गा पार्वती देवता"
            lower.contains("shiva") || lower.contains("mahadeva") || lower.contains("rudra") || lower.contains("bhairava") -> "उमा महेश्वर महादेव"
            lower.contains("krishna") || lower.contains("govardhan") || lower.contains("gopala") || lower.contains("radha") -> "भगवान श्री कृष्ण गोविन्द"
            lower.contains("rama") || lower.contains("raghu") || lower.contains("sita") -> "भगवान श्री सीतारामचन्द्र"
            lower.contains("hanuman") || lower.contains("anjaneya") -> "पवनपुत्र हनुमान देवता"
            lower.contains("vishnu") || lower.contains("narayana") || lower.contains("hari") || lower.contains("venkateswara") || lower.contains("jagannath") || lower.contains("vitthala") -> "भगवान श्री लक्ष्मी नारायण"
            lower.contains("surya") || lower.contains("chhathi") || lower.contains("aditya") -> "भगवान श्री सूर्यनारायण"
            lower.contains("dhanvantari") -> "भगवान श्री धन्वन्तरि"
            lower.contains("yama") || lower.contains("chitragupta") -> "धर्मराज यम देवता"
            lower.contains("murugan") || lower.contains("skanda") || lower.contains("kartikeya") -> "भगवान श्री कार्तिकेय स्कन्द"
            lower.contains("guru") || lower.contains("vyasa") || lower.contains("dattatreya") -> "श्री सद्गुरु दत्तात्रेय देवता"
            else -> "इष्ट देवता"
        }
    }

    private fun festivalNameToSanskrit(festival: Festival): String {
        val native = festival.nameNative.trim()
        val hasDevanagari = native.any { it in '\u0900'..'\u097F' }
        return if (hasDevanagari) {
            native.replace(Regex("[A-Za-z0-9()/\\-]"), " ").replace(Regex("\\s+"), " ").trim()
        } else {
            "महापर्व"
        }
    }

    /**
     * Returns the 5 progressive, step-mapped Pooja Mantras for [festival] using its [details].
     * Every step (1..5) of the festival's Puja Vidhi receives an authentic, dedicated mantra.
     */
    fun getMantrasForFestival(
        festival: Festival,
        details: FestivalShastraDetails
    ): List<FestivalPujaMantra> {
        val rule = festival.dateRule
        val pakshaSanskrit = if (rule.paksha == Paksha.SHUKLA) "शुक्लपक्षे" else "कृष्णपक्षे"
        val pakshaEnglish = if (rule.paksha == Paksha.SHUKLA) "Shukla Paksha" else "Krishna Paksha"
        val masaSanskrit = lunarMonthToSanskrit(rule.lunarMonth)
        val deitySanskrit = deityToSanskrit(details.presidingDeity)
        val parvaSanskrit = festivalNameToSanskrit(festival)
        val special = SPECIAL_FESTIVAL_MANTRAS[festival.id]

        // Step 1: Achamana Purification & Festival Vrat/Puja Sankalpa Mantra
        val step1Mantra = FestivalPujaMantra(
            id = "${festival.id}_mantra_step_1",
            stepNumber = 1,
            title = "1. Pavitrakaran & ${festival.nameEnglish.substringBefore(" (").substringBefore(" /")} Sankalpa Mantra",
            ritualStage = "Achamana, Purification & Puja Sankalpa",
            sanskrit = "ॐ अपवित्रः पवित्रो वा सर्वावस्थां गतोऽपि वा । यः स्मरेत् पुण्डरीकाक्षं स बाह्याभ्यन्तरः शुचिः ॥ ॐ तत्सदद्य $masaSanskrit मासे $pakshaSanskrit शुभतिथौ श्री $deitySanskrit प्रीत्यर्थं $parvaSanskrit पूजनं अहं करिष्ये ॥",
            transliteration = "Om Apavitrah Pavitro Va Sarvavastham Gato'pi Va | Yah Smaret Pundarikaksham Sa Bahyabhyantarah Shuchih || Om Tat Sat Adya ${rule.lunarMonth} Mase $pakshaEnglish Shubha-Tithau Sri ${details.presidingDeity} Prityartham ${festival.nameEnglish} Pujanam Aham Karishye ||",
            meaning = "May we become pure within and without by remembering the Lotus-Eyed Lord; on this auspicious ${rule.lunarMonth} $pakshaEnglish tithi, I resolve to perform the sacred ${festival.nameEnglish} Puja for the grace of ${details.presidingDeity}.",
            chantCountRecommendation = "Recite 3 times"
        )

        // Step 2: Avahana, Kalash Sthapana & Presiding Deity Dhyana Mantra
        val step2Mantra = if (special != null) {
            FestivalPujaMantra(
                id = "${festival.id}_mantra_step_2",
                stepNumber = 2,
                title = "2. ${special.step2Title}",
                ritualStage = "Kalash Sthapana, Avahana & Dhyana",
                sanskrit = special.step2Sanskrit,
                transliteration = special.step2Transliteration,
                meaning = special.step2Meaning,
                chantCountRecommendation = "Recite 3 times"
            )
        } else {
            buildDeityAvahanaMantra(festival, details)
        }

        // Step 3: Core Festival Moola Japa & Shodashopachara Archana Mantra
        val step3Mantra = FestivalPujaMantra(
            id = "${festival.id}_mantra_step_3",
            stepNumber = 3,
            title = "3. Primary Moola & Archana Mantra (${details.presidingDeity.substringBefore(" (").substringBefore(",")})",
            ritualStage = "Primary Moola Mantra Japa & Archana",
            sanskrit = details.sacredMantraSanskrit,
            transliteration = details.sacredMantraTransliteration,
            meaning = details.sacredMantraMeaning,
            chantCountRecommendation = "Chant 11, 21, or 108 times"
        )

        // Step 4: Special Festival Offering, Arghya, Sutra-Bandhan or Naivedya Mantra
        val step4Mantra = if (special != null) {
            FestivalPujaMantra(
                id = "${festival.id}_mantra_step_4",
                stepNumber = 4,
                title = "4. ${special.step4Title}",
                ritualStage = "Special Arghya, Vrat Katha & Naivedya",
                sanskrit = special.step4Sanskrit,
                transliteration = special.step4Transliteration,
                meaning = special.step4Meaning,
                chantCountRecommendation = "Recite 3 or 11 times"
            )
        } else {
            buildNaivedyaAndVratMantra(festival, details)
        }

        // Step 5: Maha Aarti, Mantra Pushpanjali & Kshama Prarthana Mantra
        val step5Mantra = FestivalPujaMantra(
            id = "${festival.id}_mantra_step_5",
            stepNumber = 5,
            title = "5. Karpoora Maha Aarti, Pushpanjali & Kshama Prarthana",
            ritualStage = "Karpoora Maha Aarti & Mantra Pushpanjali",
            sanskrit = "कर्पूरगौरं करुणावतारं संसारसारं भुजगेन्द्रहारम् । सदा वसन्तं हृदयारविन्दे भवं भवानीसहितं नमामि ॥ मन्त्रहीनं क्रियाहीनं भक्तिहीनं सुरेश्वर । यत्पूजितं मया देव परिपूर्णं तदस्तु मे ॥",
            transliteration = "Karpura-Gauram Karunavataram Samsara-Saram Bhujagendra-Haram | Sada Vasantam Hridayaravinde Bhavam Bhavanisahitam Namami || Mantrahinam Kriyahinam Bhaktihinam Sureshvara | Yat Pujitam Maya Deva Paripurnam Tad Astu Me ||",
            meaning = "Salutations to the compassionate Divine abiding in the lotus of the heart; O Lord, whatever has been offered by me even if lacking in mantra or ritual perfection, may it be accepted as complete through devotion.",
            chantCountRecommendation = "Recite during Maha Aarti"
        )

        return listOf(step1Mantra, step2Mantra, step3Mantra, step4Mantra, step5Mantra)
    }

    private fun buildDeityAvahanaMantra(
        festival: Festival,
        details: FestivalShastraDetails
    ): FestivalPujaMantra {
        val deityLower = details.presidingDeity.lowercase()
        val deitySanskrit = deityToSanskrit(details.presidingDeity)
        val (sanskrit, translit, meaning) = when {
            deityLower.contains("vishnu") || deityLower.contains("narayana") || deityLower.contains("vitthala") ||
                deityLower.contains("jagannath") || deityLower.contains("venkateswara") || deityLower.contains("shaligram") -> Triple(
                "शान्ताकारं भुजगशयनं पद्मनाभं सुरेशं विश्वाधारं गगनसदृशं मेघवर्णं शुभाङ्गम् । लक्ष्मीकान्तं कमलनयनं योगिभिर्ध्यानगम्यं वन्दे विष्णुं भवभयहरं सर्वलोकैकनाथम् ॥",
                "Shantakaram Bhujagashayanam Padmanabham Suresham Vishvadharam Gaganasadrisham Meghavarnam Shubhangam | Lakshmikantam Kamalanayanam Yogibhir Dhyanagamyam Vande Vishnum Bhavabhayaharam Sarvalokaikanatham ||",
                "I meditate upon Lord Maha-Vishnu, serene supporter of the universe and consort of Goddess Lakshmi, who removes all worldly fears."
            )
            deityLower.contains("krishna") || deityLower.contains("radha") || deityLower.contains("gopala") -> Triple(
                "वंशीविभूषितकरान्नवनीरदाभात्पीताम्बरादरुणबिम्बफलाधरोष्ठात् । पूर्णेन्दुसुन्दरमुखादरविन्दनेत्रात्कृष्णात्परं किमपि तत्त्वमहं न जाने ॥",
                "Vamshi-Vibhushita-Karan Navaniradabhat Pitambarad Aruna-Bimbaphaladharoshthat | Purnendu-Sundara-Mukhad Aravinda-Netrat Krishnat Param Kim Api Tattvam Aham Na Jane ||",
                "I meditate upon Bhagavan Sri Krishna, holding the sacred flute, dressed in golden-yellow silk, with lotus eyes and face radiant as the full moon."
            )
            deityLower.contains("rama") || deityLower.contains("sita") || deityLower.contains("janaki") -> Triple(
                "ध्यायेदाजानुबाहुं धृतशरधनुषं बद्धपद्मासनस्थं पीतं वासो वसानं नवकमलदलस्पर्धिनेत्रं प्रसन्नम् । वामाङ्कारूढसीतामुखकमलमिलल्लोचनं नीरदाभं नानालङ्कारदीप्तं दधतमुरुजटामण्डनं रामचन्द्रम् ॥",
                "Dhyayed Ajanubahum Dhritasharadhanusham Baddhapadmasanastham Pitam Vaso Vasanam Navakamaladalaspardhinetram Prasannam | Vamankarudha-Sita-Mukhakamala-Milallochanam Niradabham Ramachandram ||",
                "I meditate upon serene Bhagavan Sri Ramachandra along with Mother Sita, lotus-eyed, dressed in yellow silk, and radiating dharma."
            )
            deityLower.contains("shiva") || deityLower.contains("mahadeva") || deityLower.contains("arunachala") ||
                deityLower.contains("gangadhara") -> Triple(
                "ध्यायेन्नित्यं महेशं रजतगिरिनिभं चारुचन्द्रावतंसं रत्नाकल्पोज्ज्वलाङ्गं परशुमृगवराभीतिहस्तं प्रसन्नम् । पद्मासीनं समन्तात्स्तुतममरगणैर्व्याघ्रकृत्तिं वसानं विश्वाद्यं विश्वबीजं निखिलभयहरं पञ्चवक्त्रं त्रिनेत्रम् ॥",
                "Dhyayen Nityam Mahesham Rajatagirinibham Charuchandravatamsam Ratnakalpojjvalangam Parashumrigavarabhitihastam Prasannam | Vishvadyam Vishvabijam Nikhilabhayaharam Panchavaktram Trinetram ||",
                "I meditate upon Lord Maheshwara, bright as a silver mountain, wearing the crescent moon, bestowing fearlessness and boons upon all creation."
            )
            deityLower.contains("durga") || deityLower.contains("gauri") || deityLower.contains("parvati") ||
                deityLower.contains("kali") || deityLower.contains("kamakhya") || deityLower.contains("sheetala") ||
                deityLower.contains("devi") || deityLower.contains("mahalakshmi") -> Triple(
                "या देवी सर्वभूतेषु दयारूपेण संस्थिता । या देवी सर्वभूतेषु मातृरूपेण संस्थिता । नमस्तस्यै नमस्तस्यै नमस्तस्यै नमो नमः ॥",
                "Ya Devi Sarvabhuteshu Dayarupena Samsthita | Ya Devi Sarvabhuteshu Matri-Rupena Samsthita | Namastasyai Namastasyai Namastasyai Namo Namaha ||",
                "Salutations again and again to the Divine Mother who abides in all beings as compassion, strength, and maternal grace."
            )
            deityLower.contains("surya") || deityLower.contains("aditya") -> Triple(
                "आकृष्णेन रजसा वर्तमानो निवेशयन्नमृतं मर्त्यं च । हिरण्ययेन सविता रथेना देवो याति भुवनानि पश्यन् ॥",
                "A Krishnena Rajasa Vartamano Niveshayann Amritam Martyam Cha | Hiranyayena Savita Rathena Devo Yati Bhuvanani Pashyan || (Rigveda 1.35.2)",
                "Moving across the heavens in His golden chariot, Lord Savitur (Surya) illuminates all worlds and sustains all living beings."
            )
            deityLower.contains("murugan") || deityLower.contains("skanda") || deityLower.contains("subrahmanya") ||
                deityLower.contains("kartikeya") -> Triple(
                "षडाननं कुङ्कुमरक्तवर्णं महामंतिं दिव्यमयूरवाहनम् । रुद्रस्य सूनुं सुरसैन्यनाथं गुहं सदाहं शरणं प्रपद्ये ॥",
                "Shadananam Kunkuma-Raktavarnam Mahamatim Divya-Mayura-Vahanam | Rudrasya Sunum Surasainyanatham Guham Sadaham Sharanam Prapadye ||",
                "I take refuge in six-faced Lord Guha (Murugan), radiant as vermilion, mounted on the divine peacock, son of Rudra and leader of the celestial forces."
            )
            else -> Triple(
                "आगच्छ देवदेवेश तेजोराशे जगत्पते । क्रियमाणां मया पूजां गृहाण सुरसत्तम ॥ श्री $deitySanskrit आवाहयामि स्थापयामि पूजयामि ॥",
                "Agaccha Devadevesha Tejorashe Jagatpate | Kriyamanam Maya Pujam Grihana Surasattama || Sri ${details.presidingDeity} Avahayami Sthapayami Pujayami ||",
                "Come, O radiant Presiding Deity (${details.presidingDeity}), and graciously accept the sacred worship offered at our altar today."
            )
        }

        return FestivalPujaMantra(
            id = "${festival.id}_mantra_step_2",
            stepNumber = 2,
            title = "2. ${details.presidingDeity.substringBefore(" (").substringBefore(",")} Dhyana & Avahana Mantra",
            ritualStage = "Kalash Sthapana, Avahana & Dhyana",
            sanskrit = sanskrit,
            transliteration = translit,
            meaning = meaning,
            chantCountRecommendation = "Recite 3 times"
        )
    }

    private fun buildNaivedyaAndVratMantra(
        festival: Festival,
        details: FestivalShastraDetails
    ): FestivalPujaMantra {
        return FestivalPujaMantra(
            id = "${festival.id}_mantra_step_4",
            stepNumber = 4,
            title = "4. ${festival.nameEnglish.substringBefore(" (").substringBefore(" /")} Naivedya & Prarthana Mantra",
            ritualStage = "Special Arghya, Vrat Katha & Naivedya",
            sanskrit = "ॐ प्राणाय स्वाहा । ॐ अपानाय स्वाहा । ॐ व्यानाय स्वाहा । ॐ उदानाय स्वाहा । ॐ समानाय स्वाहा । नैवेद्यं गृह्यतां देव भक्तिं मे ह्यचलां कुरु । ईप्सितं मे वरं देहि परत्र च परां गतिम् ॥",
            transliteration = "Om Pranaya Svaha | Om Apanaya Svaha | Om Vyanaya Svaha | Om Udanaya Svaha | Om Samanaya Svaha | Naivedyam Grihyatam Deva Bhaktim Me Hyachalam Kuru | Ipsitam Me Varam Dehi Paratra Cha Param Gatim ||",
            meaning = "O Divine Lord (${details.presidingDeity}), graciously accept this sattvik Naivedya offering; grant us unwavering devotion, health, and auspicious blessings.",
            chantCountRecommendation = "Recite 3 times"
        )
    }
}
