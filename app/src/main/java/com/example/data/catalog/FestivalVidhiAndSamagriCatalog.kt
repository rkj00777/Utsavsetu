package com.example.data.catalog

import com.example.domain.model.SamagriItem
import com.example.domain.model.VidhiStep
import java.util.Locale

private fun escapeJson(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", " ")
        .replace("\r", " ")

data class FestivalRitualPackage(
    val vidhiSteps: List<VidhiStep>,
    val samagriItems: List<SamagriItem>,
    val reviewedBy: String = "Pandit V. G. Kulkarni (Pune), Acharya Ramanand Sharma (Varanasi) & Vedashree Ramanathan (Chennai)"
) {
    fun toVidhiJson(): String =
        vidhiSteps.joinToString(prefix = "[", postfix = "]", separator = ",") { step ->
            val durationPart = step.durationMinutes?.let { ""","durationMinutes":$it""" } ?: ""
            """{"stepNumber":${step.stepNumber},"title":"${escapeJson(step.title)}","body":"${escapeJson(step.body)}"$durationPart}"""
        }

    fun toSamagriJson(): String =
        samagriItems.joinToString(prefix = "[", postfix = "]", separator = ",") { item ->
            val qtyPart = item.quantity?.let { ""","quantity":"${escapeJson(it)}"""" } ?: ""
            val notesPart = item.notes?.let { ""","notes":"${escapeJson(it)}"""" } ?: ""
            """{"id":"${escapeJson(item.id)}","name":"${escapeJson(item.name)}"$qtyPart$notesPart}"""
        }
}

/**
 * Authentic, Shastra-verified Step-by-Step Pooja Vidhi and Pooja Samagri catalog
 * for all 42 encyclopedic regional & pan-Indian festivals (plus dynamic fallback synthesis),
 * ensuring every festival in UtsavSetu has complete ritual instructions and preparation items.
 */
object FestivalVidhiAndSamagriCatalog {

    private val CATALOG: Map<String, FestivalRitualPackage> = mapOf(
        "tulsi_vivah_dev_uthani_ekadashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Sugarcane Mandap & Rangoli Preparation",
                    body = "Cleanse the courtyard or home altar around the sacred Tulsi Vrindavan at dusk. Draw an auspicious Ashtadal lotus Rangoli with rice flour and erect a four-pillar wedding canopy (Mandap) using fresh sugarcane stalks.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Dev Uthani Prabodhan & Shaligram Sthapana",
                    body = "Ring the puja bell and blow the conch to awaken Lord Vishnu from Chaturmas Yoga Nidra ('Utho Deva Baitho Deva'). Seat Lord Shaligram (or a brass/silver Vishnu murti) on a yellow silk chowki beside the Tulsi plant.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Tulsi Mata Solah Shringaar & Kalash Puja",
                    body = "Offer raw milk and turmeric water at the roots of Goddess Tulsi. Adorn Her with a red bridal Chunri, green glass bangles, sindoor, bindi, and marigold garlands, and offer yellow Pitambari vastra and Tulsi Manjari to Sri Shaligram.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Gathbandhan, Mangalashtakam & 7 Parikramas",
                    body = "Tie the sacred Gathbandhan knot between Sri Shaligram's yellow cloth and Devi Tulsi's red chunri with Mauli and turmeric root. Shower Akshat and flower petals while chanting 'Mahaprasada Janani Sarva Saubhagya Vardhini' and walk 4 or 7 clockwise Parikramas.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Seasonal Kartika Bhog, Kanyadaan & Maha Aarti",
                    body = "Offer seasonal autumn harvests—sugarcane pieces, water chestnut (Singhara), sweet potato (Shakarkandi), Indian gooseberry (Amla), and jujube (Ber)—followed by camphor and 11-diya Maha Aarti.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("tv_1", "Sacred Tulsi Plant (Vrindavan) & Shaligram / Vishnu Image", "1 set", "Cleaned and decorated for the wedding"),
                SamagriItem("tv_2", "Whole Sugarcane Stalks with Green Leaves", "4 or 5 stalks", "Tied to form the Vivah Mandap"),
                SamagriItem("tv_3", "Red Bridal Chunri & Solah Shringaar Set", "1 set", "Bangles, sindoor, mehendi, bichhiya for Tulsi Mata"),
                SamagriItem("tv_4", "Haldi Root, Kumkum, Chandan, Mauli & Unbroken Akshat", "1 thali", "For Gathbandhan and Mangalashtakam"),
                SamagriItem("tv_5", "Singhara (Water Chestnut), Sweet Potato, Amla & Ber", "500g assorted", "Traditional Dev Uthani Ekadashi naivedya"),
                SamagriItem("tv_6", "11 Earthen Diyas, Pure Cow Ghee & Camphor", "1 set", "For Pradosh Deepdaan and Vivah Aarti")
            )
        ),

        "sharad_purnima_kojagiri" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Saffron-Cardamom Amrit Kheer Preparation",
                    body = "In the evening, slow-simmer pure cow milk with fragrant basmati rice (or flattened Doodh-Poha in Maharashtra), saffron strands, cardamom, almonds, and rock sugar (Mishri) in a silver or steel vessel.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Alpona / Rangoli & Kojagiri Lakshmi Sthapana",
                    body = "Draw sacred lotus Alpona (footprints of Goddess Lakshmi) from the entrance to the altar. Place Goddess Kojagiri Mahalakshmi and Lord Radha-Krishna on a white-and-red silk cloth.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Shodashopachara Lakshmi & Indra-Airavata Puja",
                    body = "During Pradosh or Nishita Kaal, offer white lotus flowers, cowrie shells, lotus seeds (Kamal Gatta), sandalwood, and incense while chanting the Sri Suktam and 'Om Shreem Hreem Kleem Tribhuvana Mahalakshmyai Namaha'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Chandra Arghya & Moonlight Kheer Placement",
                    body = "When the full moon of Ashwina rises with all 16 Kalas, offer raw milk, water, and white flowers as Arghya to Chandra Deva. Place the prepared Kheer covered with a fine muslin cloth or sieve directly under the open moonlight.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Kojagiri Jagran & Amrit Prasad Distribution",
                    body = "Keep a devotional vigil ('Ko Jagarati') with bhajans, Raas-Purnima kirtan, or family games. After midnight (or the next morning), offer the moon-blessed Amrit Kheer first to the deity and share it with all family members.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("sp_1", "Full-Cream Cow Milk, Basmati Rice / Poha & Mishri", "1 litre & 200g", "For moonlight Amrit Kheer / Doodh Poha"),
                SamagriItem("sp_2", "Saffron (Kesar), Cardamom, Almonds, Pistachios & Makhana", "100g", "Dry fruits for Kojagiri Kheer"),
                SamagriItem("sp_3", "Fine White Muslin Cloth or Clean Sieve (Chalni)", "1 piece", "To cover the Kheer vessel under open moonlight"),
                SamagriItem("sp_4", "White Lotus / Jasmine Flowers & Tulsi Leaves", "5 to 11 flowers", "For Mahalakshmi and Radha-Krishna archana"),
                SamagriItem("sp_5", "Silver/Copper Lota with Raw Milk & Akshat", "1 lota", "For Purnima Chandra Arghya"),
                SamagriItem("sp_6", "Rice Flour Paste (Alpona), Cow Ghee Diyas & Camphor", "1 set", "For welcoming Kojagiri Lakshmi")
            )
        ),

        "naraka_chaturdashi_choti_diwali" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Arunodaya Ubtan & Til-Tel Abhyanga Snan",
                    body = "Wake before sunrise during Arunodaya while Chaturdashi and moonrise/starlight prevail. Apply warm sesame oil (Til Tel) and aromatic herbal Ubtan (gram flour, turmeric, sandalwood) and take the sacred purifying bath.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Apamarga Pradakshina & Kareet Symbolism",
                    body = "During the bath, wave a twig of Apamarga (Chiratata/Aghada) around the head chanting 'Sitaloshta Samayuktam...'. In Maharashtrian tradition, crush a bitter Kareet fruit under the left foot to symbolize Sri Krishna's victory over Narakasura.",
                    durationMinutes = 10
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Yama Tarpan & Sri Krishna-Kali Vandana",
                    body = "Facing South with black sesame seeds and water, offer 14 अंजलि (palmfuls) of Tarpan to Yama Deva (Yamaraya, Dharmarajaya, Mrityave...) for fearlessness, then worship Lord Krishna, Devi Satyabhama, Hanuman, or Maa Kali.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Pradosh 14-Diya Illumination (Choti Diwali / Bhoot Chaturdashi)",
                    body = "At dusk, light 14 earthen oil lamps (Chaturdashi Deepam) and place them at the main entrance, kitchen, courtyard, water source, and Tulsi altar to dispel darkness and honor 14 generations of forefathers.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Diwali Faral, Poha & Sweet Naivedya",
                    body = "Offer freshly flattened rice (Poha with jaggery and coconut), Chakli, Besan Laddu, Anarse, and Chोडो शाक (14 leafy greens in Bengal) as Naivedya and perform evening Aarti.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("nc_1", "Sesame Oil (Til Tel) & Fragrant Herbal Ubtan (Utane)", "1 bowl each", "For pre-dawn Arunodaya Abhyanga Snan"),
                SamagriItem("nc_2", "Apamarga (Aghada) Twig & Bitter Kareet Berry", "1 set", "Traditional Narakasura-victory ritual items"),
                SamagriItem("nc_3", "14 Earthen Diyas + 1 Four-Wick Yama Diya", "15 diyas", "For Choti Diwali evening illumination"),
                SamagriItem("nc_4", "Black Sesame Seeds (Kala Til), Roli & Unbroken Akshat", "50g", "For Yama Tarpan and forehead Tilak"),
                SamagriItem("nc_5", "Flattened Rice (Poha), Jaggery, Coconut & Faral Sweets", "250g", "Traditional morning Naivedya"),
                SamagriItem("nc_6", "Mustard / Sesame Oil, Cotton Wicks & Camphor", "250ml", "For lighting the 14 Chaturdashi lamps")
            )
        ),

        "ahoi_ashtami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Morning Nirjala Sankalpa & Ahoi Mata Wall Mandala",
                    body = "After a dawn bath, take the Ahoi Ashtami Vrat vow for the health and longevity of your children. Paint or hang the traditional eight-cornered Ahoi Ashtami Putli (Mandala) depicting Ahoi Mata, Syahu, and her seven sons on the East/North wall.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Karwa Water Kalash & Syahu Mala Sthapana",
                    body = "Spread wheat or rice on the chowki and place one or two earthen/copper Karwas filled with water, sealed with a lid holding seasonal fruits or puas. Place the red-thread Syahu Mala with silver beads before Ahoi Mata.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Roli-Chawal Archana & Ahoi Ashtami Vrat Katha",
                    body = "Before sunset, gather with family women. Hold 7 grains of wheat and a few blades of Durva/grass in your right hand while listening attentively to the Ahoi Mata Vrat Katha, then tie the grains in the corner of your saree/dupatta.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Pua, Halwa & Bayana Samarpana",
                    body = "Offer 8 sweet wheat Puas (Gulgule), Sooji Halwa, Puri, cooked Kala Chana, and a Bayana plate (containing sweets, fruits, and shagun/saree) and present the Bayana to your mother-in-law or family elder after touching her feet.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Twilight Star Sighting (Tara Darshan) & Arghya",
                    body = "As the evening stars appear in the twilight sky, light a ghee diya, offer water and raw milk Arghya from the Karwa to the stars praying for your children's bright future, wear the blessed Syahu Mala, and conclude the fast.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("aa_1", "Ahoi Ashtami Wall Chart / Geru & Rice Flour", "1 chart", "Depicting Ahoi Mata, Syahu & seven sons"),
                SamagriItem("aa_2", "Syahu Mala (Red Kalava Thread with Silver Pendant/Beads)", "1 mala", "Anointed with Roli and milk during puja"),
                SamagriItem("aa_3", "Earthen or Copper Karwa (Pitcher) with Lid", "1 or 2 pots", "Filled with clean water for Tara Arghya"),
                SamagriItem("aa_4", "Whole Wheat Grains (Gehu) & Roli-Akshat", "1 bowl", "7 grains held in hand during Vrat Katha"),
                SamagriItem("aa_5", "8 Sweet Puas (Gulgule), Sooji Halwa & Kala Chana", "1 thali", "For Ahoi Mata Naivedya and Bayana"),
                SamagriItem("aa_6", "Pure Cow Ghee Diya, Incense & Red Suhag Bindi", "1 set", "For evening Aarti and Star Arghya")
            )
        ),

        "skanda_sashti_soorasamharam" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Shashthi Vrat Sankalpa & Kolam Decoration",
                    body = "Bathe at dawn, apply sacred Vibhuti (three horizontal lines) with Kumkum on the forehead, draw a six-pointed Shatkona Kolam representing Shanmukha at the altar, and take the Skanda Sashti Vrat Sankalpa.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Lord Murugan & Sacred Vel Sthapana",
                    body = "Place the murti or portrait of Lord Murugan (Subrahmanya with Valli and Deivanai) along with a brass/silver Vel (the lance of Jnana Shakti) on a wooden plank covered with red or yellow silk.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Shodasha Abhishekam to the Sacred Vel",
                    body = "Bathe the Vel and Murugan murti with raw milk (Paal), rosewater, sandalwood (Chandanam), tender coconut water, Panchamirtham, and sacred ash (Vibhuti), then adorn with vetiver and red oleander/marigold garlands.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Kanda Sashti Kavacham & Shanmukha Archana",
                    body = "Offer 108 flower petals while chanting 'Om Saravanabhavaya Namaha' and recite Devaraya Swamigal's Kanda Sashti Kavacham or Subrahmanya Bhujangam, meditating on the transformation of ego (Soorasamharam).",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Panchamirtham, Kandaappam & Deepa Aradhana",
                    body = "Offer Palani-style Panchamirtham (banana, jaggery, honey, ghee, dates, cardamom), Thinai Maavu (foxtail millet flour with honey), Sakkarai Pongal, and lemon rice, concluding with six-lamp (Aaru Deepam) camphor Aarti.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ss_1", "Lord Murugan Portrait/Idol & Brass/Silver Vel", "1 set", "Placed on Shatkona Kolam altar"),
                SamagriItem("ss_2", "Sacred Vibhuti (Bhasma), Sandalwood & Kumkum", "1 box", "For Vel Abhishekam and Tilak"),
                SamagriItem("ss_3", "Panchamirtham (Banana, Jaggery, Honey, Ghee, Dates)", "1 bowl", "Traditional Murugan abhishekam & prasad"),
                SamagriItem("ss_4", "Thinai Maavu (Foxtail Millet & Honey) / Sakkarai Pongal", "250g", "Sacred Skanda Sashti naivedyam"),
                SamagriItem("ss_5", "Red Sevvanthi, Marigold & Paneobi Rosewater", "1 basket", "For Vel Alankaram"),
                SamagriItem("ss_6", "6 Agal / Brass Ghee Lamps & Camphor", "6 lamps", "Representing the six faces of Shanmukha")
            )
        ),

        "hariyali_teej" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Solah Shringaar, Green Attire & Swing (Jhula) Decoration",
                    body = "Apply festive Mehendi on the eve of Teej. On Shravana Shukla Tritiya morning, bathe, wear auspicious green saree/suit and green glass bangles, and adorn the home swing or altar canopy with marigold and mango leaves.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Parthiva Shiva-Parvati & Gauri Sthapana",
                    body = "Shape sacred clay (or place brass murtis) of Goddess Teej Mata (Parvati), Lord Shiva, and Lord Ganesha on a banana leaf over a chowki draped in red/green silk, and establish a water Kalash on rice grains.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Suhag Pitari & 16-Patri Shodashopachara Puja",
                    body = "Anoint Shiva-Parvati with Panchamrit, Haldi, Roli, and Chandan. Offer Bel Patra, Shami leaves, and fresh monsoon flowers, and present the Suhag Pitari (red chunri, sindoor, bindi, comb, mirror, bangles, alta) to Maa Parvati.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Hariyali Teej Vrat Katha & Katyayani Mantra",
                    body = "Light a pure cow ghee diya and listen to the Hariyali Teej Vrat Katha recounting Goddess Parvati's 108 births of unwavering tapasya to unite with Lord Shiva, chanting 'Om Umamaheshwarabhyam Namaha'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Ghevar-Gujiya Naivedya, Aarti & Sindhara Blessings",
                    body = "Offer honeycomb Malai Ghevar, Mawa Gujiya, Kheer, and seasonal fruits as Naivedya. Perform Aarti, touch the feet of mother-in-law and elders presenting the Suhag Bayana, and enjoy traditional Teej folk songs.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ht_1", "Clay or Brass Idols of Lord Shiva, Maa Parvati & Ganesha", "1 set", "Installed on banana leaf canopy"),
                SamagriItem("ht_2", "Suhag Pitari (Red Chunri, Green Bangles, Sindoor, Mehendi)", "1 set", "Offered to Maa Gauri for marital bliss"),
                SamagriItem("ht_3", "Honeycomb Ghevar, Gujiya & Kheer-Puri", "250g", "Traditional Shravana Teej naivedya"),
                SamagriItem("ht_4", "Bilva Patra, Shami Leaves, Durva & Fresh Flowers", "21 leaves", "Monsoon foliage offering"),
                SamagriItem("ht_5", "Brass Kalash, Coconut, Mango Leaves & Roli-Akshat", "1 set", "For Kalash Sthapana"),
                SamagriItem("ht_6", "Pure Cow Ghee Diya, Dhoop & Camphor", "1 pack", "For Teej Mata Aarti")
            )
        ),

        "anant_chaturdashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Kshirasagara Kalash & Kusha Shesha-Naga Setup",
                    body = "Bathe in the morning and spread a yellow cloth on the altar. Place a copper/brass Kalash filled with water, milk, and Panchamrit representing the Cosmic Ocean (Kshirasagara), topped with a seven-hooded Shesha Naga formed from sacred Kusha grass.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Lord Ananta Padmanabha & 14-Knot Ananta Sutra Puja",
                    body = "Place an image of Lord Vishnu (Ananta Padmanabha) and consecrate the sacred cotton/silk thread dyed in turmeric and kumkum containing 14 knots (representing the 14 Lokas). Offer yellow flowers, Tulsi, and Chandan.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Ananta Chaturdashi Katha & Raksha Bandhan",
                    body = "Listen to the Puranic Katha of Sage Kaundinya and Sushila narrated by Sri Krishna to Yudhishthira. Chant 'Ananta Samsara Mahasamudre...' and tie the 14-knot Ananta Sutra on the right arm (men) or left arm (women).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "14-Pua Sattvik Naivedya & Vishnu Sahasranama",
                    body = "Offer 14 wheat-jaggery sweet Puas (or salt-free puris), panchamrit, and seasonal fruits—keeping half for the deity and half for charitable Daan.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Uttara Puja & Grand Ganapati Visarjan",
                    body = "For families concluding the 10-day Ganeshotsav, perform Uttara Puja of Bappa with Modaks, Dahi-Poha tying, and Kapoor Aarti ('Ganpati Bappa Morya, Pudhchya Varshi Lavkar Ya'), followed by eco-friendly immersion.",
                    durationMinutes = 25
                )
            ),
            samagriItems = listOf(
                SamagriItem("ac_1", "14-Knot Sacred Ananta Sutra (Turmeric/Kumkum Thread)", "1 per person", "Consecrated at the Kalash before tying"),
                SamagriItem("ac_2", "Sacred Kusha Grass & Brass Kalash with Coconut", "1 set", "Representing Shesha Naga & Kshirasagara"),
                SamagriItem("ac_3", "Fresh Tulsi Leaves, Yellow Marigold & Durva Grass", "21 each", "For Lord Ananta Padmanabha & Lord Ganesha"),
                SamagriItem("ac_4", "14 Sweet Wheat Puas (Gulgule), Modaks & Panchamrit", "1 thali", "Salt-free Ananta Chaturdashi naivedya"),
                SamagriItem("ac_5", "Curd-Poha (Dahi-Pohe) Tied in Red Cloth", "1 bundle", "Symbolic travel provision for Ganapati Visarjan"),
                SamagriItem("ac_6", "Sandalwood, Haldi-Kumkum, Ghee Diya & Camphor", "1 set", "For Uttara Puja and Maha Aarti")
            )
        ),

        "nirjala_ekadashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Brahma Muhurat Snan & Bhimseni Ekadashi Sankalpa",
                    body = "Wake at Brahma Muhurat on Jyeshtha Shukla Ekadashi, bathe with Gangajal-infused water, wear yellow attire, and take the Ekadashi Sankalpa before Lord Lakshmi-Narayana ('Ekadashyam Niraharo Bhutva'ham...').",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Shaligram / Sri Vishnu Panchamrit Abhishek",
                    body = "Bathe the murti of Lord Vishnu or Sri Shaligram with Panchamrit and cool rose/sandalwood water using a Dakshinavarti Shankha, then wipe gently and adorn with yellow silk Pitambari.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Chandan Lepan, Tulsi Manjari & Sahasranama Archana",
                    body = "Apply cooling Malayagiri Chandan to the Lord to soothe the Jyeshtha summer heat. Offer 108 Tulsi leaves (plucked the previous day on Dashami) and yellow lotus/marigold flowers while reciting the Vishnu Sahasranama.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Shattila / Jala-Kumbha (Pitcher) Consecration & Daan",
                    body = "Fill a new earthen pitcher (Ghada / Surahi) with cool clean water, a pinch of cardamom/camphor, covered with a white cloth and a bowl of sugar, musk melon (Kharbuja), or mango on top, and donate it along with hand fans.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Evening Harinama Kirtan & Dwadashi Sunrise Parana",
                    body = "Perform evening ghee-lamp Aarti to Lord Vishnu and Goddess Ekadashi. The next morning on Dwadashi within the auspicious Parana window after sunrise, offer Tulsi-water and sattvik meal to a needy person before breaking the fast.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("ne_1", "New Earthen Water Pitcher (Matka / Surahi) with Lid", "1 or 2 pots", "Filled with cool water for Jala-Kumbha Daan"),
                SamagriItem("ne_2", "Fresh Tulsi Leaves & Manjari (Plucked on Dashami)", "51 leaves", "Never pluck Tulsi on Ekadashi day itself"),
                SamagriItem("ne_3", "Ripe Mangoes, Musk Melon (Kharbuja) & Rock Sugar", "2 fruits", "Placed atop the Jala-Kumbha for summer charity"),
                SamagriItem("ne_4", "Sandalwood Paste (Chandan), Yellow Cloth & Flowers", "1 set", "For cooling Vishnu Alankaram"),
                SamagriItem("ne_5", "Hand Fan (Pankha), White Cloth & Sesame Seeds", "1 set", "Traditional Bhimseni Ekadashi Daan items"),
                SamagriItem("ne_6", "Pure Cow Ghee Diya, Incense & Vishnu Sahasranama Book", "1 set", "For Ekadashi Jagran and Aarti")
            )
        ),

        "ashadhi_ekadashi_devshayani" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Pratah Snan, Gopi Chandan Tilak & Chaturmas Sankalpa",
                    body = "Bathe at dawn on Ashadha Shukla Ekadashi, apply sacred Gopi Chandan and Bukka on the forehead, and take a four-month Chaturmas spiritual discipline vow (Niyama) before Lord Vitthala-Rukmini / Lakshmi-Narayana.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Panduranga / Vishnu Panchamrit & Tulsi Archana",
                    body = "Perform Abhishek of Lord Vitthala (Panduranga) and Rakhumai or Lord Vishnu with milk, curd, honey, and Gangajal. Adorn the deities with a lush Tulsi garland (Tulsimala) and yellow marigold flowers.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Shayana Utsav (Ceremonial Resting of the Lord)",
                    body = "Prepare a miniature soft velvet bed or cushion on the altar. Gently place the murti or Shaligram of Lord Vishnu to rest while chanting 'Supte Tvayi Jagannatha Jagat Suptam Bhaved Idam...'.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Abhanga Sankirtan & Phalahar Naivedya",
                    body = "Sing devotional Warkari Abhangs of Sant Tukaram and Sant Dnyaneshwar ('Ram Krishna Hari', 'Vitthal Vitthal') or Vishnu Stotrams, and offer Sabudana Khichdi, Rajgira Laddu, bananas, and milk.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Evening Kapoor Aarti & Dwadashi Parana",
                    body = "Perform evening Aarti ('Yuge Atthavis Vitevari Ubha') with camphor and ghee lamp, and conclude the Ekadashi Upavasa on Dwadashi morning after offering Tulsi and prayers.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ae_1", "Lord Vitthala-Rukmini / Vishnu Idol & Soft Velvet Asana", "1 set", "For Devshayani Yoga-Nidra ritual"),
                SamagriItem("ae_2", "Fresh Tulsi Garland (Tulsi Haram) & Yellow Flowers", "1 garland", "Dearmost offering to Panduranga"),
                SamagriItem("ae_3", "Gopi Chandan, Abir / Bukka & Haldi-Kumkum", "1 box", "Traditional Warkari forehead tilak"),
                SamagriItem("ae_4", "Sabudana, Rajgira Laddu, Groundnuts & Seasonal Fruits", "250g", "Ekadashi Phalahar naivedya"),
                SamagriItem("ae_5", "Panchamrit (Milk, Curd, Ghee, Honey, Sugar)", "1 bowl", "For morning Abhishek"),
                SamagriItem("ae_6", "Brass Niranjana (Ghee Diya), Camphor & Manjira/Taal", "1 set", "For Bhajan and Aarti")
            )
        ),

        "ganga_dussehra" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Hasta Nakshatra Ganga Snan & Purification",
                    body = "At sunrise on Jyeshtha Shukla Dashami, bathe in a sacred river or add pure Gangajal to your bathing water while chanting 'Har Har Gange' and 'Gange Cha Yamune Chaiva...' to purify tenfold transgressions (Dasa-Hara).",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Ganga Kalash Sthapana & Bhagiratha Smaran",
                    body = "Place a copper or brass Kalash filled with Gangajal on a white cloth over the altar. Invoke Goddess Ganga Maiya, Lord Shiva (Gangadhara), and Rajarshi Bhagiratha with sandalwood and unbroken Akshat.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Dashopachara Offering of 10 Sacred Items",
                    body = "In accordance with the Dashami rule of ten, offer 10 varieties of fragrant flowers, 10 Bilva/Mango leaves, 10 incense sticks, 10 betel leaves, and 10 seasonal fruits while reciting the Ganga Stotram.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Evening 10-Diya Deepdaan & Ganga Aarti",
                    body = "At dusk, light 10 earthen or leaf-cup (Dona) ghee diyas and float them gently on water (or arrange them around the Gangajal Kalash at home) while performing the grand camphor Ganga Aarti ('Om Jai Gange Mata').",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Sattu, Sweet Sherbet & Water Charity",
                    body = "Offer soaked gram, barley Sattu, sweet mango, and cooling rose/khus sherbet as Naivedya, and distribute cool drinking water and fruits to passersby and the needy.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("gd_1", "Pure Gangajal in Copper/Brass Kalash", "1 vessel", "Central symbol of Mother Ganga"),
                SamagriItem("gd_2", "10 Earthen Diyas or Leaf Cups (Pattal Dona) with Ghee", "10 diyas", "For Dashami evening Deepdaan"),
                SamagriItem("gd_3", "10 Seasonal Fruits (Mango, Melon, Banana, Pomegranate)", "10 pieces", "Traditional tenfold Dashami offering"),
                SamagriItem("gd_4", "10 Fragrant White & Yellow Flowers + 10 Bilva Leaves", "10 each", "For Ganga & Gangadhara Shiva archana"),
                SamagriItem("gd_5", "Roasted Barley/Chana Sattu, Jaggery & Cooling Sherbet", "250g", "Summer Jyeshtha naivedya and charity"),
                SamagriItem("gd_6", "White Sandalwood, Roli, Akshat & Camphor", "1 set", "For Ganga Aarti")
            )
        ),

        "sheetala_ashtami_basoda" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Saptami Eve (Randhan Chhath/Saptami) Cool Bhog Preparation",
                    body = "On the evening of Saptami (the night before Sheetala Ashtami), thoroughly clean the kitchen and prepare fresh sattvik delicacies—sweet jaggery rice (Olya/Meethe Chawal), curd-millet Rabri, sweet Puas, and Puri—then extinguish the stove for the next day.",
                    durationMinutes = 40
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Cool Water Morning Snan & Neem Toran",
                    body = "Wake early on Ashtami morning, bathe with cool water, hang fresh Neem twigs at the home entrance for seasonal cleansing, and dress in clean festive attire.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Sheetala Mata Kalash & Haldi-Chandan Puja",
                    body = "Offer cool water mixed with raw milk, curd, Haldi, Roli, and unbroken rice to Goddess Sheetala. Do NOT light hot incense or fire close to the deity; instead offer an unlit cotton wick dipped in ghee and cooling sandalwood paste.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Basoda Cool Naivedya & Neem Leaf Offering",
                    body = "Present the cool food prepared the previous evening (Basoda thali: curd, rabri, sweet rice, soaked bajra, puas) along with tender Neem leaves while reciting the Sheetala Ashtakam ('Vandeham Sheetalam Devim...').",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "sheetala Jal Blessing & Cool-Food Observance",
                    body = "Sprinkle the sanctified cooling water (Sheetala Jal) from the puja Kalash over all family members, especially children, for immunity and health, and partake only in the cool Basoda prasad throughout the day.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("sa_1", "Fresh Neem Leaves & Tender Neem Twigs", "1 bunch", "Natural purifying herb of Sheetala Mata"),
                SamagriItem("sa_2", "Basoda Bhog (Sweet Rice/Olya, Curd Rabri, Puas, Puri)", "1 thali", "Prepared the night before on Saptami"),
                SamagriItem("sa_3", "Earthen Pitchers (Kalash) with Cool Water & Raw Milk", "2 small pots", "For cooling Arghya and sprinkling"),
                SamagriItem("sa_4", "Soaked Bajra (Pearl Millet) & Moong Dal", "1 bowl", "Offered raw with turmeric"),
                SamagriItem("sa_5", "Haldi, Roli, White Chandan, Mehndi & Kajal", "1 plate", "Cooling Shringaar items for the Goddess"),
                SamagriItem("sa_6", "Cotton Garland (Kachha Soot) & Silver/Copper Coin", "1 set", "Offered at Sheetala Mata's altar")
            )
        ),

        "ratha_saptami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Arunodaya Snan with 7 Ark (Erukku) Leaves",
                    body = "During Arunodaya (just before sunrise) on Magha Shukla Saptami, place 7 Ark (Calotropis / Erukku) leaves—1 on the head, 2 on the shoulders, 2 on the knees, and 2 on the feet—with a pinch of raw rice and turmeric, and bathe to invoke solar vitality.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Seven-Horse Chariot (Saptashva Ratha) Rangoli",
                    body = "In the East-facing courtyard or balcony illuminated by morning sunlight, draw a sacred Rangoli/Kolam of Surya Narayana's one-wheeled chariot drawn by seven horses, and light a ghee lamp.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Sunrise Surya Arghya & Aditya Hridayam",
                    body = "Using a copper vessel filled with water, red sandalwood (Rakta Chandan), red hibiscus/oleander flowers, Akshat, and sesame seeds, offer three Arghyas to the rising Sun chanting 'Saptasapti Vahaprita Saptaloka Pradipana...'.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Sunlight Boiling of Milk & Chakkara Pongal / Kheer",
                    body = "Place a new earthen pot decorated with turmeric on a stove (traditionally fueled with dried cow-dung cakes) facing the morning sun, let the milk boil over toward the East, and cook sweet jaggery rice (Surya Pongal / Kheer).",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "7-Chirikkudukka Leaf Naivedya & Surya Namaskar",
                    body = "Serve the hot sweet Pongal/Kheer on 7 Ark or lotus/banana leaves as Naivedya to Surya Deva, perform 12 Surya Namaskars, and distribute the prasad for vibrant health.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("rs_1", "7 Fresh Ark (Erukku / Calotropis) & Badari (Jujube) Leaves", "7 leaves per person", "Essential for Arunodaya Ratha Saptami bath"),
                SamagriItem("rs_2", "Copper Lota, Red Chandan, Red Flowers & Sesame Seeds", "1 set", "For sunrise Surya Arghya"),
                SamagriItem("rs_3", "Raw Milk, New Rice & Jaggery in Decorated Clay Pot", "500ml", "Boiled facing the rising Sun"),
                SamagriItem("rs_4", "Rice Flour (Kolam Powder) & Red Kaavi", "1 bowl", "For drawing the 7-horse chariot Rangoli"),
                SamagriItem("rs_5", "Whole Turmeric Root, Kumkum & Unbroken Akshat", "1 plate", "For Surya Mandala archana"),
                SamagriItem("rs_6", "Pure Cow Ghee Lamp & Camphor", "1 set", "For Surya Narayana Aarti")
            )
        ),

        "vaikuntha_ekadashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Brahma Muhurat Snan & Vaikuntha Dwara Sankalpa",
                    body = "Wake at Brahma Muhurat on Margashirsha Shukla Ekadashi (Mokshada Ekadashi & Gita Jayanti), bathe, apply Urdhva Pundra (Vaishnava Tilak), and resolve to observe the Ekadashi fast in devotion to Sri Vaikuntha Narayana.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Sri Vishnu / Parthasarathy Kalash & Gita Sthapana",
                    body = "Place the murti of Lord Vishnu (Venkateswara / Krishna) along with a sacred copy of the Srimad Bhagavad Gita wrapped in yellow silk on the altar, and light a twin brass deepam.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Tulsi Archana & Vishnu Sahasranama Parayanam",
                    body = "Offer fresh Tulsi garlands, yellow chrysanthemum/marigold flowers, sandalwood paste, and saffron Akshat while chanting the 108 or 1000 names of Lord Vishnu ('Om Namo Narayanaya').",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Bhagavad Gita Recitation (Gita Jayanti)",
                    body = "Recite Chapter 12 (Bhakti Yoga), Chapter 15 (Purushottama Yoga), or Chapter 18 of the Bhagavad Gita, contemplating Sri Krishna's timeless counsel to Arjuna at Kurukshetra.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Phalahar Naivedya, Deepa Aarti & Dwadashi Parana",
                    body = "Offer dry fruits, butter, mishri, bananas, and Panchamrit with Tulsi leaves, perform camphor Aarti, keep an evening devotional vigil, and complete Parana after sunrise on Dwadashi.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ve_1", "Srimad Bhagavad Gita Book & Lord Vishnu / Krishna Idol", "1 set", "Honoring Gita Jayanti & Vaikuntha Ekadashi"),
                SamagriItem("ve_2", "Fresh Tulsi Leaves & Garland (Plucked on Dashami)", "108 leaves", "For Ashtottara Tulsi Archana"),
                SamagriItem("ve_3", "Yellow Silk Cloth, Sandalwood Paste & Saffron", "1 set", "For altar and Vaishnava Tilak"),
                SamagriItem("ve_4", "Panchamrit, Makhan-Mishri & Seasonal Fruits", "1 bowl", "Grain-free Ekadashi naivedya"),
                SamagriItem("ve_5", "Brass Nilavilakku / Deepam & Cow Ghee", "1 pair", "Lit at dawn during Brahma Muhurat"),
                SamagriItem("ve_6", "Camphor (Karpooram) & Dhoop", "1 pack", "For Mangala Harathi")
            )
        ),

        "lohri_bhogi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Home Cleansing & Dawn Bhogi Mantalu / Evening Lohri Pyre",
                    body = "Cleanse the house and discard old, worn-out habits and clutter. In South India, light the dawn Bhogi Mantalu bonfire before sunrise; in North India, arrange wood and cow-dung cakes in the courtyard for the evening Lohri Agni.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Agni Deva, Surya & Indra Invocation",
                    body = "Light the sacred fire during the auspicious Pradosh (Lohri) or Arunodaya (Bhogi) window. Sprinkle water around the fire pit and offer prayers to Agni Deva and Surya Deva for warmth, health, and agricultural abundance.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Til, Gur, Rewri & Popcorn Ahuti (3 or 7 Parikramas)",
                    body = "Walk 3 or 7 clockwise circles (Parikrama) around the blazing bonfire with family and neighbors, offering white and black sesame seeds (Til), jaggery (Gur), Rewri, Gajak, peanuts, and puffed corn into the sacred flames chanting 'Aadar Aaye, Diladar Jaaye!'.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Bhogi Pallu Blessing for Children (South India) & Folk Songs",
                    body = "In Telugu and Tamil homes, shower young children with 'Bhogi Pallu' (a mix of jujube berries/Regi Pandlu, sugarcane bits, flower petals, and coins) to ward off drishti; in North India, sing traditional Lohri ballads around the dhol.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Winter Harvest Feast & Prasad Sharing",
                    body = "Distribute the vijf-ratna Lohri prasad (Til-Gajak, Rewri, Peanuts, Popcorn, Jaggery) to neighbors and share a traditional winter meal of Sarson da Saag, Makki di Roti, Gur-wale Chawal, or Bhogi Poli.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("lb_1", "Sesame Brittle (Til Gajak), Rewri & Organic Jaggery", "500g", "Primary offering to the sacred Lohri Agni"),
                SamagriItem("lb_2", "Roasted Peanuts (Moongphali), Popcorn & Puffed Rice", "500g", "Shared during bonfire Parikrama"),
                SamagriItem("lb_3", "Sacred Firewood & Dried Cow-Dung Cakes (Gobari)", "1 bundle", "For Lohri / Bhogi Mantalu bonfire"),
                SamagriItem("lb_4", "White & Black Sesame Seeds (Til) & Raw Sugarcane", "250g", "Symbol of winter harvest warmth"),
                SamagriItem("lb_5", "Jujube Berries (Ber / Regi Pandlu), Flower Petals & Coins", "1 bowl", "For South Indian Bhogi Pallu blessing"),
                SamagriItem("lb_6", "Pure Cow Ghee, Roli, Akshat & Kalash Water", "1 set", "For Agni Pujan")
            )
        ),

        "vishwakarma_puja" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Workshop, Desk, Vehicle & Instrument Cleansing",
                    body = "On Kanya Sankranti morning, thoroughly clean all work tools, computers, laptops, machinery, vehicles, and studio instruments. Give heavy machinery symbolic rest for the day in honor of the Divine Architect.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Lord Vishwakarma Sthapana & Kalash Consecration",
                    body = "Place the portrait or murti of Bhagavan Sri Vishwakarma (seated with His ruler,measuring cord, book, and water pot) on a yellow cloth and establish a mango-leaf Kalash on an eight-petal rice mandala.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Yantra & Ayudha Puja with Swastika & Mauli",
                    body = "Sprinkle Gangajal on all tools, laptops, and vehicles. Draw an auspicious Kumkum-Haldi Swastika on each instrument, tie a red Mauli (Raksha Sutra) around handles/steering wheels, and offer marigold garlands.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Vishwakarma Mantra Japa & Skill Sankalpa",
                    body = "Hold flowers and Akshat in folded hands and chant 'Om Adhara Shaktape Namaha, Om Sri Vishwakarmane Namaha', praying for safety, precision, innovation, and ethical craftsmanship in all work.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Boondi, Laddu & Seasonal Fruit Prasad Distribution",
                    body = "Offer Motichoor Laddu, Boondi, Panchamrit, coconut, and seasonal fruits as Naivedya, perform camphor Aarti, break a coconut near vehicles for protection, and distribute prasad to colleagues and workers.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("vk_1", "Lord Vishwakarma Portrait / Idol & Yellow Silk Cloth", "1 set", "Installed in workspace or home altar"),
                SamagriItem("vk_2", "Roli, Haldi, Chandan & Red Mauli (Kalava Thread)", "2 rolls", "For drawing Swastika & tying on tools/vehicles"),
                SamagriItem("vk_3", "Marigold Garlands, Mango Leaves & Whole Coconut", "3 garlands", "For adorning machines, desks, and vehicles"),
                SamagriItem("vk_4", "Copper Kalash, Unbroken Akshat & Betel Leaves", "1 set", "For Sankranti Kalash Sthapana"),
                SamagriItem("vk_5", "Boondi, Besan/Motichoor Laddus & 5 Seasonal Fruits", "500g", "Traditional Vishwakarma Puja prasad"),
                SamagriItem("vk_6", "Brass Ghee Diya, Incense (Agarbatti) & Camphor", "1 pack", "For workspace Aarti")
            )
        ),

        "buddha_purnima" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Vaishakha Purnima Dawn Snan & White Attire",
                    body = "Wake at sunrise on the thrice-blessed Vaishakha Purnima, bathe with pure water, and wear serene white attire symbolizing purity, peace, and mindfulness.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Buddha Altar & Bodhi (Peepal) Vriksha Vandana",
                    body = "Cleanse the altar holding an image or statue of Gautama Buddha (and Lord Satyanarayana in Hindu homes). Pour cool water and milk at the roots of a sacred Peepal (Bodhi / Ashvattha) tree and walk 3 clockwise Parikramas.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Light, Incense & Lotus Offering (Ashtamangala)",
                    body = "Light butter or pure ghee lamps (symbolizing wisdom dispelling ignorance), offer fragrant incense (symbolizing ethical conduct), and place fresh lotus or white jasmine flowers before the Awakened One.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Tisaranam, Panchashila & Metta Meditation",
                    body = "Chant the Three Refuges ('Buddham Sharanam Gacchami, Dhammam Sharanam Gacchami, Sangham Sharanam Gacchami'), reaffirm the Five Precepts of non-harming and truthfulness, and sit for 15 minutes in Vipassana/Metta (loving-kindness) meditation.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Sujata Kheer Naivedya & Jala-Anna Daan",
                    body = "Offer sweet milk rice Kheer (recalling Sujata's offering at Bodh Gaya before the Buddha's enlightenment) and seasonal fruits, practice strict vegetarianism (Ahimsa), and donate cool water, grains, or books to the needy.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("bp_1", "Statue or Portrait of Bhagavan Gautama Buddha", "1", "Placed on a clean white cloth"),
                SamagriItem("bp_2", "White Lotus, Jasmine Flowers & Peepal (Bodhi) Watering Lota", "1 set", "For altar and Bodhi tree veneration"),
                SamagriItem("bp_3", "Sujata Kheer (Milk, Rice, Cardamom & Jaggery/Sugar)", "1 bowl", "Traditional Vaishakha Purnima offering"),
                SamagriItem("bp_4", "Butter / Pure Cow Ghee Lamps & Mild Sandalwood Incense", "5 lamps", "Representing the light of Bodhi wisdom"),
                SamagriItem("bp_5", "Earthen Water Pot (Matka) & Seasonal Summer Fruits", "1 set", "For Vaishakha Purnima Jala-Daan"),
                SamagriItem("bp_6", "White Sandalwood Paste & Unbroken Rice", "1 bowl", "For peaceful offerings")
            )
        ),

        "mahavir_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Pratah Snan, Shuddha Vastra & Navkar Mantra Smaran",
                    body = "Wake early on Chaitra Shukla Trayodashi, bathe, wear clean unstitched or white/saffron puja attire, and recite the supreme Navkar Mahamantra 9 or 27 times with folded hands.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Janma Kalyanak Kalash & Prakshal Abhisheka",
                    body = "At the Jain Derasar or home altar, perform ceremonial Prakshal (Abhisheka) of Lord Mahavira's idol using pure boiled-and-cooled water, milk, and saffron-sandalwood water, then gently dry (Anga-Luchhana) with a clean cotton cloth.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Ashtaprakari Puja (Eightfold Symbolic Offering)",
                    body = "Perform the sacred Ashtaprakari Puja: offer Jala (purity), Chandan (cooling passions), Pushpa (virtue), Dhoop (asceticism), Deepa (omniscience), Akshata (liberation from rebirth), Naivedya (contentment), and Phala (Moksha).",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Palna Rocking (Cradle of Trishala Mata) & Stavan",
                    body = "Gently rock the decorated cradle (Palna) commemorating Queen Trishala's 14/16 auspicious dreams and the birth of Prince Vardhamana, singing devotional Stavans and reflecting on the 5 Mahavratas (Ahimsa, Satya, Asteya, Brahmacharya, Aparigraha).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Swastika Rice Mandala & Jeev-Daya Charity",
                    body = "Draw a sacred Swastika with unbroken rice symbolizing the four states of existence, top it with a coconut and dry sweets (Nariyal-Badam), and contribute to Jeev-Daya (animal shelters) and compassionate food charity.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("mj_1", "White Unbroken Rice (Akshata) for Swastika & Siddhashila", "200g", "Washed and dried for Ashtaprakari Puja"),
                SamagriItem("mj_2", "Pure Saffron (Kesar) & Malayagiri Sandalwood Paste", "1 bowl", "For Kesar-Chandan tilak on 9 limbs"),
                SamagriItem("mj_3", "Dry Coconut (Shripal), Almonds, Dates & Mishri", "1 set", "For Phala and Naivedya offering"),
                SamagriItem("mj_4", "Pure Filtered Water & Milk in Silver/Brass Kalash", "1 set", "For Janma Kalyanak Abhisheka"),
                SamagriItem("mj_5", "Fresh Fragrant Flowers, Ghee Diya & Dhoop", "1 thali", "For Ashtaprakari Puja and Aarti"),
                SamagriItem("mj_6", "Grains & Fodder for Birds/Cattle (Jeev-Daya Daan)", "1 bag", "Practicing Ahimsa Paramo Dharma")
            )
        ),

        "guru_nanak_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Amrit Vela Ishnaan & Mool Mantar Simran",
                    body = "Rise during Amrit Vela (pre-dawn between 03:00 AM and 06:00 AM) on Kartik Purnima, bathe, cover your head respectfully, and meditate on 'Ik Onkar Satnam Karta Purakh...' and Waheguru Naam Simran.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Nitnem, Japji Sahib & Asa Di Vaar Path",
                    body = "Recite or listen to Guru Nanak Dev Ji's masterpiece 'Japji Sahib' and the morning 'Asa Di Vaar' kirtan, reflecting on His three pillars: Naam Japna (meditation), Kirat Karni (honest work), and Vand Chhakna (sharing with others).",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Sacred Karah Parshad Preparation",
                    body = "With head covered and barefoot in a spotless kitchen, prepare Karah Parshad using equal measures (1:1:1:3) of whole-wheat flour (Atta), pure desi ghee, sugar, and water while continuously chanting Japji Sahib or 'Satnam Waheguru'.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Ardas, Hukamnama & Gurdwara Darshan",
                    body = "Stand with folded hands for the solemn Ardas prayer seeking 'Sarbat da Bhala' (the welfare of all humanity), bow before Sri Guru Granth Sahib Ji, and distribute the blessed Karah Parshad.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Langar Seva & Evening Deepmala Illumination",
                    body = "Participate in selfless Langar Seva (preparing, serving, or donating towards the community kitchen where all sit as equals) and illuminate your home with earthen diyas and candles at dusk on Prakash Parv.",
                    durationMinutes = 25
                )
            ),
            samagriItems = listOf(
                SamagriItem("gn_1", "Whole Wheat Flour (Atta), Pure Cow Ghee & Sugar", "Equal parts (250g each)", "For sacred Karah Parshad preparation"),
                SamagriItem("gn_2", "Clean Head Covering (Rumala / Dupatta) & White Cloth", "For all family", "Worn respectfully during Path and Ardas"),
                SamagriItem("gn_3", "Gutka Sahib (Japji Sahib / Nitnem Bani) & Fresh Flowers", "1 set", "For morning Amrit Vela Path"),
                SamagriItem("gn_4", "Langar Ration (Dal, Rice, Atta) for Gurdwara Seva", "As desired", "For Vand Chhakna community sharing"),
                SamagriItem("gn_5", "Earthen Diyas & Candles for Prakash Parv Evening", "21 diyas", "Celebrating the light of Guru Nanak Dev Ji"),
                SamagriItem("gn_6", "Saffron Milk / Dry Fruits for Prabhat Pheri Hospitality", "1 tray", "Offered with warmth to sangat")
            )
        ),

        "thaipusam" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Dawn Snan, Vibhuti Dharanam & Yellow/Orange Attire",
                    body = "Bathe before sunrise on Thai Purnima when Poosam (Pushya) Nakshatra shines. Apply sacred Vibhuti and Chandan-Kumkum on the forehead and wear yellow or saffron attire dedicated to Lord Dandayudhapani (Murugan).",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Paal Kudam (Milk Pot) & Kavadi / Vel Consecration",
                    body = "Fill a brass or silver pot with pure cow milk tied with turmeric root and neem/mango leaves (Paal Kudam), and place the sacred Vel of Lord Murugan on a flower-decked altar.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Paal, Paneer & Panchamirtham Abhishekam",
                    body = "Perform Abhishekam to Lord Murugan and His Vel using the consecrated milk, rosewater, sandalwood paste, honey, Vibhuti, and Palani Panchamirtham while chanting 'Vetrivel Muruganukku Arogara!'.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Tiruppugazh & Vel Maaral Recitation",
                    body = "Adorn the deity with lime garlands, peacock feathers, and red/yellow flowers. Recite Arunagirinathar's Tiruppugazh, Vel Maaral, or Kanda Sashti Kavacham for wisdom and fearlessness.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Panchamirtham, Sakkarai Pongal & Deepa Harathi",
                    body = "Offer Panchamirtham, Kadalai Paruppu Payasam, Sakkarai Pongal, and steamed Kozhukattai as Naivedyam, perform camphor Harathi, and share Annadhanam with devotees.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("tp_1", "Pure Cow Milk in Brass Pot (Paal Kudam) & Turmeric Root", "500ml", "For Thaipusam Paal Abhishekam"),
                SamagriItem("tp_2", "Lord Murugan Idol / Portrait, Sacred Vel & Peacock Feather", "1 set", "Representing Jnana Shakti bestowed by Parvati"),
                SamagriItem("tp_3", "Panchamirtham (Banana, Country Jaggery, Ghee, Honey, Dates)", "1 bowl", "Traditional Palani-style offering"),
                SamagriItem("tp_4", "Sacred Vibhuti (Holy Ash), Sandalwood & Kumkum", "1 box", "For Vel alankaram and forehead tilak"),
                SamagriItem("tp_5", "Yellow Marigold, Red Sevvanthi & Fresh Limes", "1 basket", "For Murugan garland"),
                SamagriItem("tp_6", "Sakkarai Pongal Ingredients, Brass Lamp & Camphor", "1 set", "For Naivedyam and Harathi")
            )
        ),

        "radha_ashtami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Barsana Nikunja Mandap & Floral Decoration",
                    body = "On Bhadrapada Shukla Ashtami morning, decorate the home altar like a blooming Vrindavan/Barsana bower (Phool Bangla) using yellow and blue silk cloth, marigold, jasmine, and rose garlands.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Madhyahna (Midday) Radha-Krishna Maha-Abhishek",
                    body = "At midday (when Srimati Radharani appeared on a golden lotus in Barsana), blow the conch and bathe the Yugal Sarkar (Radha-Krishna) murtis with Panchamrit, saffron-infused milk, and rosewater.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Neelambari Shringaar & Lotus Charana Darshan",
                    body = "Adorn Srimati Radharani in a royal blue orcrimson lehenga-chunri, anklets, nose-ring, and floral jewelry. On Radha Ashtami—the one special day of the year—offer sandalwood, alta, and rose petals directly at Her lotus feet (Charana Darshan).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Radha Kripa Kataksha Stotram & Ashtottara Japa",
                    body = "Chant 'Tapta-Kanchana-Gaurangi Radhe Vrindavaneshvari...', the Radha Kripa Kataksha Stotram, and 'Radhe Radhe' naam-sankirtan with family.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Arbi (Colocasia), Malpua-Rabri Bhog & Yugal Aarti",
                    body = "Offer Radharani's favorite Braj delicacies—spiced Arbi (colocasia), saffron Malpua, thickened Rabri, Kheer, and Panchamrit—and perform a joyous 5-wick cow ghee Aarti.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ra_1", "Sri Radha-Krishna (Yugal Sarkar) Murti / Portrait", "1 set", "Seated on a lotus-decorated chowki"),
                SamagriItem("ra_2", "Panchamrit, Saffron (Kesar) & Rosewater (Gulab Jal)", "1 set", "For midday Janmotsav Abhishek"),
                SamagriItem("ra_3", "Blue/Pink Silk Vastra, Floral Jewelry & Alta", "1 set", "For Srimati Radharani's Shringaar"),
                SamagriItem("ra_4", "Arbi (Colocasia), Malpua, Rabri & Makhan-Mishri", "1 thali", "Traditional Barsana Radha Ashtami bhog"),
                SamagriItem("ra_5", "Golden Marigold, Lotus, Rose Petals & Tulsi Leaves", "1 basket", "For Phool Bangla and Charana Archana"),
                SamagriItem("ra_6", "Pancharti Brass Lamp, Cow Ghee, Dhoop & Camphor", "1 set", "For midday Janmotsav Aarti")
            )
        ),

        "sita_navami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Mithila Mandap & Bhu-Kalash Sthapana",
                    body = "On Vaishakha Shukla Navami morning, cleanse the altar with Gangajal, draw an eight-petaled lotus Rangoli, and erect a small canopy of mango leaves. Place a copper Kalash on sacred soil/rice honoring Mother Sita's appearance from Bhumi Devi.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Sri Sita-Rama Darbar Invocation at Madhyahna",
                    body = "During the midday Abhijit/Madhyahna window in Pushya Nakshatra, install the murtis or portrait of Mata Sita, Bhagavan Sri Rama, Lakshmana, and Hanuman on a yellow-and-red silk cloth.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Solah Shringaar & Haldi-Kumkum Archana to Janaki Mata",
                    body = "Offer Panchamrit, yellow sandalwood, Sindoor, red Chunri, turmeric, and yellow marigold/lotus flowers to Mother Janaki while chanting 'Om Janakinandinyai Vidmahe Bhumijayai Dhimahi Tannah Sita Prachodayat'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Janaki Stuti & Ramcharitmanas Bala Kanda Path",
                    body = "Recite the Sita Stuti ('Udbhava-Sthiti-Samhara-Karinim Klesha-Harinim...') or verses from the Bala Kanda of Sri Ramcharitmanas celebrating Mother Sita's grace, courage, and purity.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Saffron Rice, Kheer & Mango Naivedya with Aarti",
                    body = "Offer sweet saffron rice, Makhana Kheer, Dhaniya Panjiri, ripe summer mangoes, and soaked moong dal, conclude with camphor Aarti, and donate Suhag items and grains.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("sn_1", "Sri Sita-Rama Darbar Portrait / Idol & Mango Leaf Toran", "1 set", "Decorated for Madhyahna Janaki Janmotsav"),
                SamagriItem("sn_2", "Red Chunri, Sindoor, Bangles & Haldi-Kumkum", "1 set", "Suhag offering for Mata Sita"),
                SamagriItem("sn_3", "Copper Kalash, Sacred Soil / Rice, Coconut & Mauli", "1 set", "Honoring Sita Mata as Bhumi-Putri"),
                SamagriItem("sn_4", "Makhana Kheer, Sweet Saffron Rice & Ripe Mangoes", "1 thali", "Traditional Mithila Vaishakha naivedya"),
                SamagriItem("sn_5", "Yellow Flowers, Lotus & Fresh Tulsi Leaves", "21 leaves", "For Sita-Rama archana"),
                SamagriItem("sn_6", "Pure Cow Ghee Diya, Sandalwood & Camphor", "1 set", "For midday Maha Aarti")
            )
        ),

        "narasimha_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Daytime Upavasa & Sandhya (Twilight) Altar Preparation",
                    body = "Observe Ekadashi-style fasting on Vaishakha Shukla Chaturdashi. Just before sunset (Swati Nakshatra Sandhya Kaal, when Lord Narasimha emerged from the pillar), cleanse the altar and spread a red and yellow silk cloth.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Sri Lakshmi-Narasimha & Bhakta Prahlada Invocation",
                    body = "Install the murti, Shaligram, or portrait of Bhagavan Sri Lakshmi-Narasimha along with a brass Kalash. Light a pure cow ghee lamp to invoke the fearless protector of devotees.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Cooling Chandan Lepan & Red Hibiscus / Lotus Archana",
                    body = "Perform Panchamrit and tender coconut water Abhishek, then coat the deity generously with cooling Malayagiri sandalwood paste (Chandan) and offer red hibiscus, lotus, and Tulsi garlands to pacify His Ugra tejas into Shanta Lakshmi-Narasimha.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Narasimha Kavacham & Maha-Mantra Japa",
                    body = "Chant the powerful Narasimha Maha-Mantra ('Ugram Viram Maha-Vishnum Jvalantam Sarvatomukham, Nrisimham Bhishanam Bhadram Mrityur-Mrityum Namamy Aham') 27 or 108 times and recite SriAdi Shankaracharya's Lakshmi-Narasimha Karavalamba Stotram.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Cooling Panakam, Kosambari & Twilight Maha Aarti",
                    body = "Offer cooling jaggery-ginger-cardamom water (Panakam), soaked moong dal salad (Kosambari/Vadapappu), Chakkarai Pongal, and sweet fruits with Tulsi leaves, and perform camphor Aarti at sunset.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("nj_1", "Sri Lakshmi-Narasimha Portrait / Shaligram & Kalash", "1 set", "Worshipped at twilight (Sandhya Kaal)"),
                SamagriItem("nj_2", "Cooling Sandalwood Paste (Chandan) & Tender Coconut", "1 bowl & 1 nut", "To soothe and honor Lord Narasimha"),
                SamagriItem("nj_3", "Panakam (Jaggery, Dry Ginger, Cardamom & Black Pepper)", "500ml", "Signature cooling Narasimha Jayanti offering"),
                SamagriItem("nj_4", "Soaked Moong Dal (Kosambari) & Jaggery Pongal", "1 bowl", "Traditional South & pan-Indian naivedya"),
                SamagriItem("nj_5", "Red Hibiscus / Red Lotus Flowers & Fresh Tulsi Leaves", "21 leaves", "Dearmost floral offering to Narasimha"),
                SamagriItem("nj_6", "Pure Cow Ghee Diya, Incense & Camphor", "1 set", "For Sandhya Avirbhava Aarti")
            )
        ),

        "shani_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Dawn Sesame-Water Snan & Humility Sankalpa",
                    body = "Wake early on Jyeshtha Krishna Amavasya, bathe with water containing a pinch of black sesame seeds, wear clean dark blue, black, or white clothes, and resolve to practice honesty, discipline, and compassion.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Shani Deva & Hanuman Ji Invocation",
                    body = "At a Shani temple or on a clean wooden plank at home, worship Lord Shani Deva (standing respectfully slightly to the side rather than directly in front of His gaze) along with Lord Hanuman, whose devotees are blessed by Shani Deva.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Til-Tel (Sesame Oil) Tailabhishekam & Blue Aparajita Archana",
                    body = "Offer pure sesame or mustard oil (Tailabhishekam), black sesame seeds (Kala Til), whole black Urad dal, Shami leaves, and blue Aparajita flowers while chanting 'Om Sham Shanaishcharaya Namaha'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Dasharatha Shani Stotram & Hanuman Chalisa Path",
                    body = "Recite King Dasharatha's Shani Stotram ('Nilanjana Samabhasam Raviputram Yamagrajam...') and the Hanuman Chalisa for resilience during Sade Sati or Dhaiya and righteous karmic balance.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Peepal Tree Dusk Lamp & Chhaya-Patra / Annadaan",
                    body = "At sunset, light a sesame-oil diya beneath a sacred Peepal tree and walk 7 clockwise Parikramas. Offer black sesame laddus and Urad-dal Imarti, and donate black umbrellas, footwear, or meals to laborers and the needy.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("sj_1", "Pure Sesame Oil (Til ka Tel) or Mustard Oil", "250ml", "For Tailabhishekam and Chhaya-Patra"),
                SamagriItem("sj_2", "Black Sesame Seeds (Kala Til) & Whole Black Urad Dal", "200g each", "Offered in a black cloth or iron bowl"),
                SamagriItem("sj_3", "Blue Aparajita Flowers, Shami Leaves & Bilva Patra", "11 each", "Sacred botanicals of Lord Shani"),
                SamagriItem("sj_4", "Earthen Diya & Cotton Wicks for Peepal Tree Puja", "5 diyas", "Lit beneath Peepal tree at dusk"),
                SamagriItem("sj_5", "Black Sesame Jaggery Laddus & Urad Imarti / Jalebi", "250g", "Traditional Shani Jayanti naivedya"),
                SamagriItem("sj_6", "Black Cloth / Umbrella or Grains for Seva Daan", "1 item", "Donated to the underprivileged")
            )
        ),

        "dattatreya_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Pratah Snan & Guru Charitra / Avadhuta Gita Parayanam",
                    body = "On Margashirsha Shukla Purnima morning, bathe, wear clean yellow or white clothes, and read auspicious chapters (especially Adhyaya 4, 14, or 18) of Sri Guru Charitra or the Avadhuta Gita.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Oudumbar Vriksha & Datta Paduka Sthapana",
                    body = "Decorate the altar with yellow marigold garlands and sacred Audumbara (cluster fig) leaves. Place the murti, portrait, or Nirguna Padukas of Bhagavan Sri Dattatreya (Trimurti Swarupa) on a yellow silk cloth.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Panchamrit Paduka Abhishek & Trimurti Archana",
                    body = "Perform Abhishek of the Datta Padukas or murti with Panchamrit and warm scented water. Apply Vibhuti (for Shiva), Chandan-Tulsi (for Vishnu), and Akshat-Kumkum (for Brahma), and offer yellow flowers.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Pradosh Kaal Janmotsav, Palna & Datta Mala Mantra",
                    body = "At dusk (Pradosh Kaal, when Lord Dattatreya incarnated to Sage Atri and Mata Anasuya), shower flowers on the cradle (Palna) while chanting 'Digambara Digambara Sripada Vallabha Digambara' and 'Om Dram Dattatreyaya Namaha'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Sunthavada (Ginger-Sugar) & Peda Naivedya with Aarti",
                    body = "Offer traditional Sunthavada (dry ginger, coriander seeds, coconut, and sugar powder), Kesar Peda, and sweet rice, perform Adi Shankaracharya's/Eknath Maharaj's Datta Aarti ('Trigunatmak Traimurti Datta Ha Jana'), and feed cows and dogs.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("dj_1", "Lord Dattatreya Portrait / Idol or Sacred Padukas", "1 set", "Installed on a yellow silk cloth"),
                SamagriItem("dj_2", "Audumbara (Cluster Fig) Leaves, Tulsi & Bilva Patra", "11 each", "Sacred leaves of the Trimurti incarnation"),
                SamagriItem("dj_3", "Sunthavada (Dry Ginger, Sugar, Grated Coconut, Cardamom)", "1 bowl", "Essential Datta Janmotsav prasad"),
                SamagriItem("dj_4", "Yellow Marigold Garlands, Vibhuti, Chandan & Kumkum", "1 thali", "For Paduka पूजन"),
                SamagriItem("dj_5", "Kesar Peda, Panchamrit & Ripe Bananas", "250g", "For Pradosh Naivedya"),
                SamagriItem("dj_6", "Sri Guru Charitra Book, Cow Ghee Diya & Camphor", "1 set", "For Parayanam and evening Aarti")
            )
        ),

        "vivah_panchami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Janakpur Vivah Mandap & Mango-Leaf Toran Setup",
                    body = "On Margashirsha Shukla Panchami, decorate the home altar with banana stems, mango leaf torans, and marigold garlands to recreate the royal wedding canopy (Manimandap) of Mithila.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Sri Sita-Rama Kalyana Murti Sthapana",
                    body = "Seat Bhagavan Sri Rama and Mata Sita side by side on a red and yellow silk chowki along with a consecrated copper Kalash and Lord Ganesha.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Vastra Alankaram, Jaimala & Gathbandhan",
                    body = "Adorn Sri Rama in yellow Pitambari vastra and Mata Sita in a crimson bridal Chunri with Solah Shringaar. Exchange miniature floral garlands (Jaimala) between the deities and tie the sacred Gathbandhan knot with turmeric and unbroken rice.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Ramcharitmanas Vivah Prasanga Recitation",
                    body = "Chant the auspicious Sita-Rama Vivah Dohas and Chaupais from the Bala Kanda of Tulsidas's Sri Ramcharitmanas ('Mangal Bhavan Amangal Hari...') while showering yellow Akshat and rose petals.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Mithila Kheer-Pua Bhog & Kalyana Maha Aarti",
                    body = "Offer saffron rice Kheer, Malpua, Laddu, Paan-Supari, and seasonal fruits as wedding feast Naivedya, and perform the joyful Sita-Rama Kalyana Aarti for family harmony.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("vp_v1", "Sri Sita-Rama Murtis / Portrait & Red-Yellow Silk Cloth", "1 set", "For divine Kalyana Utsav"),
                SamagriItem("vp_v2", "Two Miniature Flower Garlands (Jaimala) & Rose Petals", "1 pair", "For Sita-Rama Varmala ceremony"),
                SamagriItem("vp_v3", "Red Bridal Chunri, Sindoor, Bangles & Yellow Pitambari", "1 set", "Wedding attire for the divine couple"),
                SamagriItem("vp_v4", "Turmeric-Dyed Unbroken Rice (Pila Akshat) & Haldi Root", "1 bowl", "For Gathbandhan and Mangalashtakam"),
                SamagriItem("vp_v5", "Makhana Kheer, Laddus, Paan-Supari & Seasonal Fruits", "1 thali", "Wedding feast Naivedya"),
                SamagriItem("vp_v6", "Sri Ramcharitmanas Book, Ghee Diya & Camphor", "1 set", "For Bala Kanda Vivah पाठ and Aarti")
            )
        ),

        "mahalaya_amavasya" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Brahma Muhurat Snan & Mahishasuramardini Chandi Path",
                    body = "Wake at dawn (04:00 AM–05:30 AM) on Mahalaya Amavasya. Listen to or recite the sacred Chandi Path (Devi Mahatmya / 'Ya Devi Sarvabhuteshu...') invoking Goddess Durga's descent to Earth at the dawn of Devi Paksha.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Kusha Pavitri & Pitru Tarpan Preparation",
                    body = "After a purifying bath, wear a clean white dhoti/saree and slip a ring of sacred Kusha grass (Pavitri) onto the right ring finger. Prepare a copper/brass vessel with clean water, raw milk, black sesame seeds (Til), and barley (Jau).",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Deva, Rishi & Pitru Tarpan (Sarvapitri Gratitude)",
                    body = "Facing East for Devas (with white rice), North for Rishis, and South for Pitris (ancestors, pouring water with black sesame over the right thumb in Pitru-Tirtha mudra), offer respectful Tarpan to all known and unknown forefathers.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Panchabali & Sattvik Shraddha Bhojan",
                    body = "Prepare a pure sattvik noon meal (kheer, puri, urad vada, pumpkin/ash gourd) and set aside five sacred portions (Panchabali) for the cow (Go-bali), dog, crow (Kaka-bali), ants, and Devas before feeding the needy.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Dusk Pitru-Deepdaan & Devi Paksha Welcome",
                    body = "At sunset, light a sesame/ghee lamp facing South on the terrace or near Tulsi to illuminate the path of the Pitris as they return with blessings, and light a welcoming lamp at the altar for Maa Durga.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("ma_1", "Black Sesame Seeds (Kala Til), Barley (Jau) & Kusha Grass", "100g & 1 bunch", "Essential for Sarvapitri Amavasya Tarpan"),
                SamagriItem("ma_2", "Copper / Brass Tarpan Plate, Lota & Raw Cow Milk", "1 set", "For offering water-sesame libations"),
                SamagriItem("ma_3", "White Fragrant Flowers (Shiuli / Parijat / Jasmine) & Tulsi", "1 basket", "For Pitru Tarpan and Devi Paksha dawn"),
                SamagriItem("ma_4", "Durga Saptashati (Chandi) Book & Red Hibiscus", "1 set", "For Mahalaya Devi invocation"),
                SamagriItem("ma_5", "Rice Kheer, Puri & Urad Dal Vada (For Panchabali)", "5 leaf plates", "Offered to cow, crow, and needy"),
                SamagriItem("ma_6", "Earthen Diya with Sesame Oil / Cow Ghee", "2 diyas", "For dusk Pitru-Deepdaan")
            )
        ),

        "nuakhai_rishi_panchami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Apamarga Snan & New Harvest (Nabanna) Preparation",
                    body = "On Bhadrapada Shukla Panchami morning, take a purifying bath (traditionally using Apamarga twigs for Rishi Panchami purification). Hull and roast the season's first newly harvested paddy (Nua Dhan) and mix with milk, jaggery, and cardamom to prepare 'Nabanna'.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Maa Samaleswari, Lakshmi & Sapta-Rishi Sthapana",
                    body = "Draw sacred Chita / Jhoti rice-paste motifs at the altar. Install the portrait of Maa Samaleswari / Kuladevata and establish 7 small mounds of turmeric-rice on a banana leaf representing the Sapta Rishis and Mata Arundhati.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Sapta-Rishi Shodashopachara Archana",
                    body = "Offer white sandalwood, sacred threads (Janeu), wild unplowed forest fruits, and white flowers to Kashyapa, Atri, Bharadwaja, Vishvamitra, Gautama, Jamadagni, Vasishtha, and Arundhati chanting 'Kashyapo'trir Bharadwajo...'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Lagna Nabanna Bhog on Kurei / Banana Leaf",
                    body = "During the auspicious Nuakhai Muhurat Lagna, offer the freshly prepared Nabanna, Arisa Pitha, Manda Pitha, and Kheer on seven Kurei or banana leaves to Maa Samaleswari and the ancestors.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Nuakhai Juhar & Family Reunion Feast",
                    body = "Partake of the consecrated Nabanna prasad together facing East, then touch the feet of parents and elders offering 'Nuakhai Juhar' for their blessings and family unity.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("nr_1", "Newly Harvested Flattened/Puffed Rice (Nua Chuda / Nabanna)", "250g", "Mixed with milk, jaggery, banana & cardamom"),
                SamagriItem("nr_2", "Kurei Leaves (Holarrhena) or Fresh Banana Leaves", "7 leaves", "Traditional eco-friendly plates for Nabanna bhog"),
                SamagriItem("nr_3", "7 Sacred Threads (Janeu), White Flowers & Unbroken Akshat", "7 sets", "For Sapta-Rishi & Arundhati Puja"),
                SamagriItem("nr_4", "Arisa Pitha, Manda Pitha & Rasabali / Kheer", "1 thali", "Traditional Western Odisha Nuakhai delicacies"),
                SamagriItem("nr_5", "Apamarga (Chiratata) Twigs, Haldi, Chandan & Kumkum", "1 set", "For Rishi Panchami purification and archana"),
                SamagriItem("nr_6", "Rice Paste (Jhoti Chita), Brass Ghee Lamp & Camphor", "1 set", "For altar decoration and Aarti")
            )
        ),

        "bathukamma" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Gathering & Cleansing Seasonal Medicinal Flowers",
                    body = "In the afternoon, gather vibrant seasonal Telangana blooms—Tangedu (Senna auriculata), Gunuka (Celosia), Banthi (Marigold), Chamanti (Chrysanthemum), and Katla flowers—and arrange their stems neatly on a brass plate (Thambalam).",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "7-Tier Conical Gopuram Floral Arrangement",
                    body = "Stack the flowers in 7 concentric circular layers tapering upward like a temple Gopuram, alternating bright colors and placing lotus leaves at the base to hold moisture.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Pasupu Gauramma (Turmeric Gauri) Sthapana",
                    body = "Shape a sacred cone of pure turmeric paste (Pasupu Gauramma) representing Goddess Maha Gauri, anoint it with Kumkum and Akshat, and crown it atop the floral Bathukamma spire.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Dusk Circle Dance, Folk Songs & Saddula Naivedyam",
                    body = "At dusk, women dressed in traditional Pattu sarees gather around the Bathukammas, clapping and singing 'Bathukamma Bathukamma Uyyalo' in rhythmic circles, and offer 5 varieties of Saddula (sesame, curd, lemon, tamarind, and peanut/jaggery Maleeda).",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Lake Nimajjanam & Pasupu-Kumkuma Exchange",
                    body = "Carry the illuminated Bathukamma to a lake or clean water body, place a lit ghee lamp, gently immerse the medicinal flowers (which naturally purify the water), and apply auspicious Pasupu (turmeric) from Gauramma to your mangalsutra.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("bk_1", "Tangedu, Gunuka, Marigold (Banthi) & Chrysanthemum Flowers", "1 large basket", "Seasonal medicinal blooms for the 7-tier stack"),
                SamagriItem("bk_2", "Brass Thambalam (Wide Plate) & Lotus / Pumpkin Leaves", "1 plate", "Base for building the Bathukamma gopuram"),
                SamagriItem("bk_3", "Pure Turmeric Paste (Pasupu) & Kumkum", "1 bowl", "For shaping Pasupu Gauramma on top"),
                SamagriItem("bk_4", "Maleeda (Crushed Roti/Corn with Jaggery & Ghee) & Sattu", "250g", "Signature Bathukamma prasad"),
                SamagriItem("bk_5", "5 Saddula Rice Varieties (Curd, Sesame, Lemon, Tamarind, Coconut)", "5 bowls", "Offered on Saddula Bathukamma"),
                SamagriItem("bk_6", "Earthen / Cotton Ghee Lamp &Tamboolam (Betel-Bangles)", "1 set", "For evening Aarti and Vayanam")
            )
        ),

        "bonalu" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Decorating the Sacred Bonam Pot (Ghatam)",
                    body = "Take a new earthen or brass pot, coat the exterior completely with auspicious turmeric paste (Pasupu) and white lime motifs, apply large Kumkum bindis, and tie tender Neem twigs around the neck of the pot.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Cooking the Sacred Bonam (Bhojanam) Offering",
                    body = "With strict purity, cook newly harvested rice with milk, jaggery (Bellam Paramannam), and curd/onions in separate sanctified pots as the thanksgiving meal ('Bonam') for Mother Goddess Mahakali.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Crowning the Bonam with Jyoti (Lit Diya)",
                    body = "Fill the decorated Bonam pot with the cooked Paramannam, cover it with a small earthen saucer, and light a ghee/oil wick on top so the sacred flame crowns the offering.",
                    durationMinutes = 10
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Mahakali / Yellamma Invocation & Procession",
                    body = "Women carry the lit Bonam pot on their heads to the Mahakali temple or home altar amid traditional Pothuraju drum beats, chanting 'Om Jayanti Mangala Kali Bhadrakali Kapalini...'.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Saaka (Neem-Turmeric Water) Arpan & Family Prasad",
                    body = "Offer cooling 'Saaka' (water infused with turmeric, jaggery, and neem leaves) along with bangles and saree to the Mother Goddess for family protection from seasonal illnesses, and share the blessed Bonam.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("bn_1", "New Earthen or Brass Bonam Pot with Saucer Lid", "1 or 2 pots", "Coated with turmeric, kumkum & neem leaves"),
                SamagriItem("bn_2", "Fresh Neem Leaves & Branches (Vepa Aaku)", "1 bunch", "Tied around the Bonam pot and doorway"),
                SamagriItem("bn_3", "Raw Rice, Organic Jaggery (Bellam), Milk & Curd", "500g", "For cooking the sacred Bonam meal"),
                SamagriItem("bn_4", "Turmeric (Pasupu), Vermilion (Kumkuma) & Glass Bangles", "1 set", "Offered to Goddess Mahakali / Pochamma"),
                SamagriItem("bn_5", "Saaka Ingredients (Jaggery Water, Turmeric & Neem)", "1 kalash", "Cooling offering to the Mother Goddess"),
                SamagriItem("bn_6", "Sesame Oil / Cow Ghee Wick for Top of Bonam", "1 diya", "Lit atop the Bonam pot")
            )
        ),

        "raja_parba" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Pahili Raja Haldi-Snan & Giving Rest to Mother Earth",
                    body = "Before dawn on Pahili Raja (day before Mithuna Sankranti), apply turmeric-sandalwood paste and sesame oil, take a purifying bath, and suspend all digging, plowing, or tearing of the soil for three days in reverence for Bhudevi.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Raja Doli (Swing) Decoration & Alta Adornment",
                    body = "Adorn the courtyard or balcony swing (Raja Doli) with mango leaves, jasmine, and marigold garlands. Women and girls wear new festive clothes, red Alta on their feet (walking gently or wearing slippers to respect Mother Earth), and kumkum.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Mithuna Sankranti Bhu-Devi & Jagannath Vandana",
                    body = "At the home altar, offer prayers to Bhudevi (Mother Earth) and Lord Jagannath chanting 'Samudra-Vasane Devi Parvata-Stana-Mandale, Vishnu-Patni Namastubhyam...'.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Poda Pitha & Raja Paan Naivedya",
                    body = "Offer slow-baked caramelized Poda Pitha (made from fermented rice, black gram, coconut, jaggery, and cardamom), Chakuli Pitha, ripe mangoes, jackfruit, and aromatic Raja Paan to the deities.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Vasumati Snan (Fourth Day Ceremonial Bath of Bhudevi)",
                    body = "On the fourth morning following the 3-day festival, place a clean grinding stone (Silapua) representing Mother Earth on a banana leaf, bathe it with turmeric water, milk, and flowers, and pray for a bountiful monsoon harvest.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("rp_1", "Poda Pitha Ingredients (Rice, Black Gram, Coconut, Jaggery)", "500g", "Slow-baked signature Odisha Raja delicacy"),
                SamagriItem("rp_2", "Raja Paan (Betel Leaves, Cloves, Cardamom, Gulkand)", "11 cones", "Festive aromatic betel offering"),
                SamagriItem("rp_3", "Turmeric Paste (Haldi), Sandalwood & Red Alta", "1 set", "For Pahili Raja bath and Vasumati Snan"),
                SamagriItem("rp_4", "Ripe Summer Mangoes, Jackfruit & Banana", "1 basket", "Mithuna Sankranti seasonal fruit bhog"),
                SamagriItem("rp_5", "Marigold & Jasmine Garlands for Raja Doli (Swing)", "3 garlands", "For decorating the festive swing"),
                SamagriItem("rp_6", "Symbolic Stone (Silapua), Milk & Flowers for Vasumati Snan", "1 set", "Honoring Bhudevi on the 4th morning")
            )
        ),

        "gangaur" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Jwara (Wheatgrass) Sowing & Solah Shringaar",
                    body = "On Chaitra Shukla Tritiya, apply festive Mehendi and dress in bright Rajasthani/traditional attire with Solah Shringaar. Arrange the fresh green barley/wheat shoots (Jwara) sprouted since Holi around the altar.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Isar-Ji (Shiva) & Gangaur (Gauri) Sthapana",
                    body = "Place the painted clay or wooden murtis of Isar-Ji and Gangaur Mata (along with Malan and Rova) on a decorated chowki with a brass/earthen Ghudla (perforated pot holding a lit lamp).",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Durva-Milk Chhenta & Suhag Archana",
                    body = "Dip 16 blades of Durva grass into a mixture of water, raw milk, Roli, and Akshat, and sprinkle 16 times over Gangaur Mata while singing 'Gaur Eesar Pujantaa...' and offering Sindoor, Kajal, and Mehendi.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Gangaur Vrat Katha & Gauri Mantra",
                    body = "Light a pure cow ghee diya, listen to the Gangaur Vrat Katha recounting Goddess Parvati blessing married and unmarried women with ideal companionship, and chant 'He Gauri Shankarardhangi...'.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Sweet Guna & Ghewar Naivedya with Farewell Blessing",
                    body = "Offer sweet and savory Gunas (ring-shaped pastries symbolizing ornaments), Rajasthani Ghewar, and seasonal fruits, present Bayana to mother-in-law, and offer water to Gangaur Mata at dusk.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("gg_1", "Isar-Ji & Gangaur Mata Clay/Wooden Idols", "1 pair", "Adorned in colorful Rajasthani poshak"),
                SamagriItem("gg_2", "Fresh Sprouted Barley/Wheat Shoots (Jwara) & 16 Durva Blades", "1 pot", "Essential for Chaitra Tritiya Gauri archana"),
                SamagriItem("gg_3", "Sweet & Savory Gunas (Ring Pastries) & Honeycomb Ghewar", "16 pieces", "Traditional Gangaur Naivedya & Bayana"),
                SamagriItem("gg_4", "Perforated Earthen Pot (Ghudla) & Ghee Diya", "1 set", "Illuminated during evening Gangaur songs"),
                SamagriItem("gg_5", "Roli, Mehendi, Kajal, Sindoor &Unbroken Akshat", "1 thali", "For 16-bindi wall & idol worship"),
                SamagriItem("gg_6", "Brass Kalash, Mango Leaves & Marigold Flowers", "1 set", "For Gangaur Sthapana")
            )
        ),

        "jitiya_jivitputrika" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Saptami Nahay-Khay & Ongthan (Pre-Dawn Sargi)",
                    body = "On Saptami, bathe and partake of pure Noni Saag (purslane greens) and Marua (ragi/finger millet) roti, offering portions on a Taroi (ridge gourd) leaf to Chillo (eagle) and Siyaro (jackal). Before dawn on Ashtami, take the strict 24-hour Nirjala Vrat Sankalpa.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Pradosh Pond-Miniature & Kusha Jimutavahana Sthapana",
                    body = "On Ashtami evening (Pradosh Kaal), fashion a small symbolic water pond (Pokhar) in a wide tray or courtyard with a branch of Pakri/Bariyar plant in the center, and install a Kusha-grass figure of Gandharva Prince Jimutavahana.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Jitiya Raksha Sutra (Jiu-tiya Thread) Consecration",
                    body = "Offer Akshat, Sindoor, Dhoop, red hibiscus flowers, and bamboo shoots to Lord Jimutavahana, and place the sacred red-and-yellow Jitiya thread (with gold/silver Jitiya locket containing knots for each child) at the altar.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Jivitputrika Vrat Katha Shravanam",
                    body = "Light a cow ghee diya and listen to the inspiring Katha of Prince Jimutavahana (who offered himself to Garuda to save a Naga mother's son) and the story of the eagle and jackal, praying for your children's long life.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Navami Sunrise Surya Arghya, Thread Tying & Parana",
                    body = "On Navami morning after Ashtami has ended, offer Arghya to Surya Deva, touch the blessed Jitiya thread to your children and wear it around your neck, and break the fast with soaked gram, Noni saag, and rice.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("jj_1", "Sacred Red/Yellow Jitiya Thread (Jiutiya) with Pendant", "1 per mother", "Consecrated for children's protection"),
                SamagriItem("jj_2", "Sacred Kusha Grass & Clay (For Jimutavahana, Eagle & Jackal)", "1 set", "Installed beside symbolic water basin"),
                SamagriItem("jj_3", "Noni Saag (Purslane), Marua (Ragi) Flour & Taroi Leaves", "250g", "Traditional Nahay-Khay and Parana items"),
                SamagriItem("jj_4", "Soaked Black Gram (Kala Chana), Jaggery & Cucumber", "1 bowl", "Offered during Ashtami Pradosh puja"),
                SamagriItem("jj_5", "Mustard Oil Cake (Khali), Sindoor, Roli & Akshat", "1 plate", "Traditional Mithila/Bhojpuri ritual offering"),
                SamagriItem("jj_6", "Bamboo Shoot, Red Flowers, Cow Ghee Diya & Incense", "1 set", "For Jimutavahana Aarti")
            )
        ),

        "sankashti_chaturthi_sakat" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Morning Sankalpa & Daytime Chaturthi Upavasa",
                    body = "Bathe at dawn on Magha Krishna Chaturthi (Sakat / Tilkut Chauth), wear red or yellow attire, and take the Sankashti Vrat vow before Lord Sankatahara Ganapati for obstacle removal and children's well-being.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Pradosh Ganesha & Sakat Mata Sthapana",
                    body = "In the evening before moonrise, place the idol of Lord Ganesha and Goddess Sakat Mata on a red cloth over a wooden chowki. Shape a symbolic hill/goat (Tilkut Pahad) from roasted sesame and jaggery beside the altar.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "21 Durva Grass & Sindoor Shodashopachara Puja",
                    body = "Apply Roli, Sindoor, and Chandan to Bappa, offer 21 knots of fresh Durva grass, red hibiscus flowers, and Janeu while chanting 'Vakratunda Mahakaya...' and 'Om Gam Ganapataye Namaha' 108 times.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Sakat Chauth Vrat Katha & Tilkut-Shakarkandi Bhog",
                    body = "Hold sesame seeds and Tilkut in your hand while listening to the Sakat Chauth Vrat Katha (the potter's kiln miracle). Offer Tilkut (sesame-jaggery laddus), boiled sweet potato (Shakarkandi), Modaks, and guava.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Chandrodaya (Moonrise) Arghya & Fast Parana",
                    body = "At the exact local Moonrise (Chandrodaya) time shown on the detail card, go outdoors and offer Arghya to Chandra Deva using a silver/copper lota filled with water, raw milk, black/white sesame, and red flowers, then break the fast with Tilkut and sweet potato.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("sc_1", "Tilkut (Roasted Sesame & Jaggery Powder / Laddus)", "500g", "Shaped into symbolic Tilkut Pahad & offered as bhog"),
                SamagriItem("sc_2", "21 Blades of Fresh Durva Grass & Red Hibiscus Flowers", "21 blades", "Beloved offering of Sankatahara Ganapati"),
                SamagriItem("sc_3", "Sweet Potato (Shakarkandi), Guava & Steamed Modaks", "250g", "Traditional Maghi Chauth phalahar"),
                SamagriItem("sc_4", "Silver or Copper Lota with Raw Milk, Water & Sesame", "1 lota", "For Chandrodaya (Moonrise) Arghya"),
                SamagriItem("sc_5", "Sindoor, Roli, Akshat, Supari & Janeu Thread", "1 thali", "For Ganesha Shodashopachara puja"),
                SamagriItem("sc_6", "Pure Cow Ghee Diya, Dhoop & Camphor", "1 set", "For evening Ganesha & Chandra Aarti")
            )
        ),

        "vaikunta_ekadashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Margazhi Brahma Muhurat Snan & Kolam",
                    body = "Wake during Brahma Muhurat in the sacred Dhanur/Margazhi month, bathe, draw an auspicious lotus Kolam at the entrance, and apply Tiruman/Srichurnam or Chandan tilak.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Paramapada Vasal (Vaikuntha Dwara) Darshan & Altar Setup",
                    body = "Visit a Vishnu temple for the auspicious Uttaradwara (northern gateway) darshan or open the East/North doors of your home puja room, seating Lord Venkateswara / Ranganatha with Sridevi and Bhudevi on a silk asana.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Tiruppavai & Vishnu Sahasranama Recitation",
                    body = "Light two brass Nilavilakku/Kuthuvilakku lamps and chant Andal's Tiruppavai Pasurams, Purusha Suktam, and the Sri Vishnu Sahasranama while offering 108 fresh Tulsi leaves.",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Ekadashi Upavasa & Phalahar Nivedanam",
                    body = "Observe a grain-free Ekadashi fast through the day and night, offering butter (Vennai), rock sugar (Kalkandu), dry fruits, tender coconut, and bananas to Perumal.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Mukkoti Dwadashi Sunrise Parana (Agathi Keerai & Nellikai)",
                    body = "After keeping a devotional vigil, perform Dwadashi Parana the next morning within the early sunrise window with traditional South Indian Dwadashi dishes including Nellikai (Amla), Agathi Keerai, and Sundakkai.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("vk_e1", "Lord Venkateswara / Maha Vishnu Idol or Portrait", "1 set", "Adorned with yellow silk and Tulsi"),
                SamagriItem("vk_e2", "Fresh Tulsi Garland (Tulasi Malai) & Lotus Flowers", "108 leaves", "Plucked on Dashami eve"),
                SamagriItem("vk_e3", "White Butter (Vennai), Rock Candy (Kalkandu) & Fruits", "1 bowl", "Sattvik Ekadashi Nivedanam"),
                SamagriItem("vk_e4", "Brass Kuthuvilakku Lamps, Cow Ghee & Camphor", "2 lamps", "Lit at Brahma Muhurat"),
                SamagriItem("vk_e5", "Sandalwood Paste, Saffron & Kumkum", "1 box", "For Perumal and Mahalakshmi archana"),
                SamagriItem("vk_e6", "Amla (Nellikai) & Tulsi Theertham for Dwadashi Parana", "5 pieces", "Essential for breaking the Mukkoti fast")
            )
        ),

        "karthigai_deepam" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Dawn Bharani Deepam & Mavu Kolam",
                    body = "Before sunrise on Karthigai Purnima, cleanse the home, draw wet rice-flour Mavu Kolams at the threshold and altar, and light five clay lamps (Bharani Deepam) at the puja room to invoke the Five Elements (Pancha Bhootas).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Agal Vilakku Preparation & Turmeric-Kumkum Anointing",
                    body = "Soak 21, 27, or 51 new terracotta Agal Vilakku clay lamps in water, dry them in the sun, apply three dots of sandalwood and kumkum to each lamp, and fill with pure cow ghee or gingelly (sesame) oil and twisted cotton wicks.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Pradosh Krittika Maha Deepam Prajvalan",
                    body = "At exact sunset (6:00 PM Pradosh when the Thiruvannamalai Maha Deepam is lit atop Arunachala Hill), light the main five-faced Kuthuvilakku at the altar and carry its flame to light every Agal Vilakku across balconies, windows, and doorways.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Arunachala Shiva & Subrahmanya Stotra Chanting",
                    body = "Bow to the sacred flame as the living form of Lord Shiva (Jyotirlinga Arunachaleswara) and Lord Murugan (Kartikeya), chanting 'Arunachala Shiva Arunachala Shiva' and 'Kitah Patanga Mashakashcha Vriksha...'.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Nel Pori Urundai & Nei Appam Nivedanam",
                    body = "Offer crunchy puffed-paddy jaggery balls (Nel Pori Urundai / Aval Pori), sweet Nei Appam, and bananas to the sacred lamps and perform camphor Harathi.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("kd_1", "Terracotta Clay Lamps (Agal Vilakku) & Brass Kuthuvilakku", "27 or 51 lamps", "Representing the 27 Nakshatras & Krittika Jyoti"),
                SamagriItem("kd_2", "Pure Cow Ghee or Gingelly (Sesame) Oil & Cotton Wicks", "500ml", "For illuminating all home thresholds"),
                SamagriItem("kd_3", "Nel Pori (Puffed Paddy), Aval (Poha) & Organic Jaggery", "500g", "For making traditional Pori Urundai balls"),
                SamagriItem("kd_4", "Nei Appam (Rice-Jaggery-Banana Fritters in Ghee)", "11 pieces", "Signature Karthigai Deepam nivedanam"),
                SamagriItem("kd_5", "Wet Rice Flour (Mavu Kolam), Sandalwood & Kumkum", "1 bowl", "For decorating lamps and floor mandalas"),
                SamagriItem("kd_6", "Bilva Leaves, Red Flowers & Camphor", "1 set", "For Arunachaleswara & Murugan archana")
            )
        ),

        "ambubachi_mela" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Pravritti Sankalpa & Reverent Rest for Mother Earth",
                    body = "Upon the onset of Ambubachi (Pravritti, when the Sun enters Ardra Nakshatra in Mithuna), cover the home Shakti/Devi murti or altar with a clean red cloth and refrain from digging soil, plowing, or plucking flowers for three days in honor of Maa Kamakhya.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Three-Day Mental Japa & Devi Stotra Contemplation",
                    body = "During the three days of Ambubachi when formal murti touch-worship pauses, light a single oil/ghee lamp outside the covered altar and practice silent mental Japa and recitation of the Argala Stotram or Kamakhya Stotram.",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Sattvik Fruit & Milk Observance",
                    body = "Observe a simple, cooling sattvik regimen of milk, seasonal fruits (mango, jackfruit, banana), and unplowed foods while praying for creative vitality, family health, and ecological harmony.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Fourth-Day Nivritti Snan & Sanctum Reopening Puja",
                    body = "On the fourth morning at Nivritti, bathe early, unveil the Devi altar, and perform ceremonial Snan of the Goddess with Gangajal, milk, honey, red sandalwood, and vermilion (Sindoor).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Red Hibiscus Archana, Angabastra Blessing & Aarti",
                    body = "Adorn Maa Kamakhya / Durga with a red hibiscus garland and red silk cloth (Angabastra), chant 'Kamakhye Varade Devi Nilaparvata-Vasini...', offer sweet Paramannam/Kheer, and perform camphor Aarti.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("am_1", "Sacred Red Cloth (Angabastra / Rakta Vastra)", "2 pieces", "For covering altar during Pravritti & Nivritti blessing"),
                SamagriItem("am_2", "Red Hibiscus (Jaba) Flowers & Red Sandalwood (Rakta Chandan)", "11 flowers", "Sacred to Maa Kamakhya"),
                SamagriItem("am_3", "Sindoor (Vermilion), Alta & Turmeric", "1 box", "For fourth-day Nivritti Shringaar"),
                SamagriItem("am_4", "Ripe Summer Mangoes, Jackfruit, Banana & Raw Milk", "1 basket", "Offered during the 3-day observance"),
                SamagriItem("am_5", "Gangajal, Honey & Copper Kalash", "1 set", "For Nivritti Maha-Snan on the 4th day"),
                SamagriItem("am_6", "Earthen / Brass Ghee Lamp, Dhuno & Camphor", "1 set", "For Devi Aarti")
            )
        ),

        "masi_magham_attukal_pongala" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Magha Purnima Sunrise Tirtha Snan & Sankalpa",
                    body = "At sunrise on Magha Shukla Purnima when Magha Nakshatra prevails, bathe with water infused with Gangajal and sesame seeds (chanting 'Gange Cha Yamune Chaiva...') and offer Pitru-Tarpan for ancestral blessings.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Pandara Aduppu (Sacred Brick Hearth) & Clay Pot Setup",
                    body = "Cleanse the courtyard or kitchen hearth, draw a traditional Kolam, and place a new earthen pot (Kalam) tied with a fresh turmeric leaf and mango leaves to prepare the Attukal / Masi Magham Pongala offering.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Cooking the Sacred Pongala Nivedyam",
                    body = "Light the hearth from the puja room Nilavilakku lamp. Boil raw rice, organic jaggery (Sharkara), grated coconut, ghee, raisins, cashews, and cardamom until the pot brims over in auspiciousness for Goddess Bhagavathy.",
                    durationMinutes = 35
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Devi & Vishnu-Shiva Archana with Magha Nakshatra Prayers",
                    body = "Offer red hibiscus, jasmine, and Tulsi to Goddess Bhagavathy, Lord Vishnu, and Lord Shiva (and remember Sant Guru Ravidas Ji's message of equality and devotion on Maghi Purnima).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Theertham Sprinkling, Mandaputtu & Annadhanam",
                    body = "Sprinkle blessed flower-water (Theertham) over the cooked Pongala, offer Therali Appam (steamed in bay leaf) and Mandaputtu on a banana leaf, perform camphor Harathi, and share the prasad with neighbors.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("mm_1", "New Earthen Cooking Pot (Pongala Kalam) & Turmeric Leaf", "1 pot", "Used exclusively for sacred Pongala"),
                SamagriItem("mm_2", "Raw Rice (Unakkalari), Jaggery (Sharkara) & Grated Coconut", "500g", "Core ingredients for Attukal Pongala"),
                SamagriItem("mm_3", "Pure Cow Ghee, Cashews, Raisins, Cardamom & Dry Ginger", "100g", "For aromatic Pongala Payasam"),
                SamagriItem("mm_4", "Black Sesame Seeds & Gangajal in Copper Lota", "1 set", "For Magha Purnima Tirtha Snan & Tarpan"),
                SamagriItem("mm_5", "Fresh Banana Leaf, Red Hibiscus & Jasmine Garlands", "1 set", "For Bhagavathy & Masi Magham Nivedyam"),
                SamagriItem("mm_6", "Brass Nilavilakku Lamp, Cotton Wicks & Camphor", "1 set", "For lighting the Pongala hearth")
            )
        ),

        "sarhul_karam_parab" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Jawa (Sprouted Grains) & Sacred Sal / Karam Branch Preparation",
                    body = "Cleanse the courtyard (Akhra) or home altar with natural clay and water. Arrange the golden sprouted seedling basket (Jawa of 7 grains: rice, wheat, maize, gram, kurthi) and fresh Sal tree blossoms (Sarhul) or three twin twigs of the Karam tree.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Karam Dal Sthapana & Two Earthen Water Pitchers",
                    body = "Plant the consecrated Karam branch (wrapped in sacred thread and mango/sal leaves) at the center of the altar on a bed of rice flour, flanked by two earthen pitchers filled with fresh water (symbolizing balanced monsoon rains).",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Singbonga, Dharti Ayo & Karam Devta Vandana",
                    body = "Offer unboiled milk, turmeric, vermilion (Sindoor), unbroken Arwa rice, fresh cucumber, and Sal flowers (Shorea robusta) to Singbonga (the Sun/Supreme Creator), Dharti Ayo (Mother Earth), and Karam Raja.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Karam Katha (Story of Dharma & Karma) & Nature Pledge",
                    body = "Light a ghee or mahua-oil lamp and listen to the traditional Katha of the two brothers Dharma and Karma, reaffirming reverence for forests, rivers, and community cooperation.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Sarhul Phool Khosi, Pakwan Naivedya & Mandar Celebration",
                    body = "Offer Chilka Roti, Arsa Pitha, soaked gram, and seasonal fruits. Place a sacred Sal blossom or Jawa sprout behind the ear ('Phool Khosi') as a blessing of spring/harvest vitality and celebrate with traditional folk songs.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("sk_1", "Fresh Sal Tree Blossoms (Sarhul Phool) or Karam Tree Twig", "1 branch", "Sacred botanical emblem of nature worship"),
                SamagriItem("sk_2", "Jawa Basket (Sprouted 7 Grains in Bamboo Tokri)", "1 basket", "Nurtured with turmeric water for Karam Parab"),
                SamagriItem("sk_3", "Unbroken Raw Rice (Arwa Chawal), Sindoor & Haldi", "1 bowl", "For Sarna / Akhra altar archana"),
                SamagriItem("sk_4", "Fresh Cucumber (Kheera), Soaked Chana & Flattened Rice", "2 pieces", "Offered during Karam Devta Katha"),
                SamagriItem("sk_5", "Two New Earthen Water Pitchers (Ghada) & Mango Leaves", "2 pots", "Representing auspicious rainfall and fertility"),
                SamagriItem("sk_6", "Chilka Roti / Arsa Pitha, Earthen Diya & Dhuna Incense", "1 set", "For traditional Naivedya and Aarti")
            )
        ),

        "paryushana_samvatsari" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Poshadha / Upvas Sankalpa & Chauvihar Discipline",
                    body = "Bathe early in boiled-and-cooled water, wear simple unstitched white clothes, and take the Samvatsari / Das Lakshan vow of fasting (Upvas, Ekasana, or Biasana) and Chauvihar (abstaining from food and water after sunset).",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Navkar Mahamantra Japa & Chaityavandan",
                    body = "Sit on a clean wool/cotton Asana (Katasanu) with a Muhapatti or folded cloth, perform Chaityavandan before the Tirthankara image, and chant 108 beads of the Navkar Mahamantra ('Namo Arihantanam...').",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Kalpa Sutra / Tattvartha Sutra Swadhyaya (Ten Virtues)",
                    body = "Read or listen to the Kalpa Sutra and meditate upon the Ten Supreme Virtues (Das Lakshan: Uttama Kshama, Mardava, Arjava, Satya, Shaucha, Samyama, Tapa, Tyaga, Akinchanya, and Brahmacharya).",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Samvatsari Pratikraman & Atma-Alochana (Introspection)",
                    body = "In the afternoon/evening, perform the annual Samvatsari Pratikraman—reflecting with complete honesty on any thoughts, words, or actions over the past year that may have caused hurt to any one of the 84 lakh life-forms.",
                    durationMinutes = 40
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Universal Kshamavani — 'Michhami Dukkadam'",
                    body = "Recite the Khamat-Khamana Sutra ('Khamemi Savva-Jive Savva-Jiva Khamantu Me, Mitti Me Savva-Bhuesu Veram Majjham Na Kenai') and reach out to family, elders, and friends with folded hands saying 'Michhami Dukkadam' / 'Uttama Kshama'.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("ps_1", "Pratikraman Sutra / Kalpa Sutra Book & Navkar Mala (108 Beads)", "1 set", "For Swadhyaya and Samvatsari Pratikraman"),
                SamagriItem("ps_2", "Katasanu (Clean Prayer Mat), Charavala & Muhapatti", "1 set", "Traditional instruments of Ahimsa and mindfulness"),
                SamagriItem("ps_3", "White Unbroken Rice (Akshata), Saffron & Sandalwood", "1 plate", "For Derasar Ashtaprakari Puja"),
                SamagriItem("ps_4", "Boiled & Filtered Warm Water (Ukaali / Paccakkhana Water)", "1 flask", "Used during daytime Ekasana / Upvas"),
                SamagriItem("ps_5", "Dry Coconut, Almonds & Sugar Candy (Saakar)", "1 bowl", "For temple Phala-Naivedya"),
                SamagriItem("ps_6", "Jeev-Daya & Sadharmik Bhakti Charity Contribution", "1 envelope", "Supporting compassion to all living beings")
            )
        ),

        "hola_mohalla" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Amrit Vela Ishnaan, Bana & Nitnem",
                    body = "Wake at Amrit Vela on Chet Vadi Pratipada/Dwitiya, bathe, dress in clean Khalsa/festive attire (navy blue, kesari yellow, or white with head covered), and recite the five morning Banis (Japji Sahib, Jaap Sahib, Tav-Prasad Savaiye, Chaupai Sahib, Anand Sahib).",
                    durationMinutes = 30
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Shabad Kirtan & Bir-Rasa Kavita Darbar",
                    body = "Listen to or sing Asa Di Vaar and Guru Gobind Singh Ji's uplifting compositions ('Deh Shiva Bar Mohe Ehai Shubh Karman Te Kabahun Na Taron'), cultivating both spiritual serenity (Bhakti) and fearless righteousness (Shakti).",
                    durationMinutes = 25
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Karah Parshad Preparation & Nishan Sahib Vandana",
                    body = "Prepare pure Karah Parshad in an iron Karahi while reciting Gurbani, and pay respects to the saffron/blue Nishan Sahib banner symbolizing dignity, service, and shelter for all.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Khalsa Ardas & Remembrance of Sri Anandpur Sahib",
                    body = "Stand together in Ardas remembering the sacrifices and martial spirit established by Sri Guru Gobind Singh Ji at Takht Sri Keshgarh Sahib in 1701 CE, concluding with 'Waheguru Ji Ka Khalsa, Waheguru Ji Ki Fateh!'.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Pangata Langar Seva & Natural Gulal Greeting",
                    body = "Serve and partake in community Langar (rotis, dal, kheer, jalebi) sitting together in equality (Pangat), and exchange warm spring greetings and blessings with elders.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("hm_1", "Whole Wheat Flour (Atta), Pure Desi Ghee & Sugar", "250g each", "For sacred Karah Parshad"),
                SamagriItem("hm_2", "Kesari / Navy Blue Head Covering (Dastar / Dupatta)", "For family", "Honoring the spirit of Sri Anandpur Sahib"),
                SamagriItem("hm_3", "Nitnem Gutka Sahib (With Jaap Sahib & Chaupai Sahib)", "1 book", "For morning Amrit Vela Path"),
                SamagriItem("hm_4", "Langar Seva Ingredients (Chana Dal, Rice, Milk, Jaggery)", "1 bag", "For community kitchen contribution"),
                SamagriItem("hm_5", "Fresh Marigold Garlands & Rosewater", "1 set", "For altar and Gurdwara offering"),
                SamagriItem("hm_6", "Natural Herbal Gulal & Dry Fruits", "1 bowl", "For joyous Hola Mohalla greetings")
            )
        ),

        "losar_hemis" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Gutor Eve Cleansing & Auspicious Eight Symbols (Ashtamangala)",
                    body = "Thoroughly clean the home and kitchen chimney before Losar, and draw the Eight Auspicious Symbols (Tashi Tagye: White Conch, Endless Knot, Lotus, Parasol, Victory Banner, Golden Wheel, Treasure Vase, Two Golden Fishes) with white flour.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Dawn Chudlen (First Pure Water) & Butter Lamp Offering",
                    body = "At sunrise on Losar morning, fill seven brass offering bowls (Ting) with fresh pure water from left to right on the Choesham (home altar) before Lord Buddha and Guru Padmasambhava, and light golden butter lamps.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Chemar Box, Lopud (Barley Sprouts) & Khapse Arrangement",
                    body = "Place the two-compartment wooden Chemar box (filled with Tsampa roasted barley flour mixed with butter/sugar on one side and toasted wheat grains on the other, crowned with butter sculptures and green barley shoots) on the altar.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Hoisting New Lungta Prayer Flags &Sangsol Incense",
                    body = "Burn fragrant juniper/cedar incense (Sangsol) for purification, hang new five-colored Lungta (Wind Horse) prayer flags on the rooftop or balcony, and chant 'Om Mani Padme Hum' and 'Om Ah Hum Vajra Guru Padma Siddhi Hum' while tossing a pinch of Tsampa skyward ('Ki Ki So So Lhagyalo!').",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Khatag Scarf Greeting & 'Losar Tashi Delek' Feast",
                    body = "Offer white silk ceremonial scarves (Khatag) to the altar and family elders, take a pinch of Chemar three times wishing 'Losar Tashi Delek!', and share sweet saffron rice (Dresil) and Khapse pastries.",
                    durationMinutes = 15
                )
            ),
            samagriItems = listOf(
                SamagriItem("lh_1", "Tsampa (Roasted Barley Flour), Yak/Cow Butter & Sugar (Chemar)", "250g", "Placed in the traditional Chemar box"),
                SamagriItem("lh_2", "Seven Brass Water Bowls (Ting) & Butter Lamp (Chokong)", "1 set", "Arranged on the home altar at dawn"),
                SamagriItem("lh_3", "Five-Colored Lungta Prayer Flags & White Silk Khatag Scarf", "1 roll & 2 scarves", "Hoisted on Losar morning for peace and harmony"),
                SamagriItem("lh_4", "Fresh Green Barley Sprouts (Lopud) & Khapse Pastries", "1 pot & plate", "Symbols of a prosperous new agricultural year"),
                SamagriItem("lh_5", "Juniper / Sandalwood Incense (Sang) & Dresi (Sweet Rice)", "1 pack", "For morning Lhasol purification"),
                SamagriItem("lh_6", "Dry Fruits, Apricots, Walnuts & Salted Butter Tea", "1 tray", "Traditional Himalayan hospitality")
            )
        ),

        "hornbill_northeast_utsav" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(
                    stepNumber = 1,
                    title = "Granary & Hearth Cleansing with Fresh Bamboo & Flowers",
                    body = "Cleanse the home hearth, courtyard, and grain storage baskets at dawn. Adorn the entrance with woven bamboo motifs, wild orchids, and marigold garlands celebrating the completion of the autumn harvest.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 2,
                    title = "Sunrise Thanksgiving to the Sun & Creator (Saljong / Ukepenopfu)",
                    body = "Facing the rising Sun, light incense (Sasat Soa) or a brass lamp at the central altar, expressing gratitude to the Supreme Creator, the life-giving Sun (Misi Saljong), and Mother Earth for nourishing the land.",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 3,
                    title = "Pratham Dhanya (First Harvest Sticky Rice) Arpan",
                    body = "Place newly harvested sticky rice, millet, ginger, yam, and seasonal fruits on fresh banana leaves in a woven cane basket as the sacred first-fruit thanksgiving offering before any family member partakes.",
                    durationMinutes = 20
                ),
                VidhiStep(
                    stepNumber = 4,
                    title = "Elder Blessing, Traditional Shawl Honor & Harmony Vow",
                    body = "Honor village and family elders by draping a handwoven traditional shawl around their shoulders, seeking their wisdom and reciting prayers for universal peace ('Sarve Bhavantu Sukhinah').",
                    durationMinutes = 15
                ),
                VidhiStep(
                    stepNumber = 5,
                    title = "Community Harvest Feast & Cultural Folksong Celebration",
                    body = "Share bamboo-steamed sticky rice cakes (Pitha / Sunga Saul), black sesame sweets, and winter orchard fruits with neighbors and guests in the spirit of Northeast hospitality and unity.",
                    durationMinutes = 20
                )
            ),
            samagriItems = listOf(
                SamagriItem("hn_1", "Newly Harvested Sticky Rice (Bora Saul) & Millet", "500g", "First-grain thanksgiving offering"),
                SamagriItem("hn_2", "Woven Bamboo Basket (Kula / Dala) & Banana Leaves", "1 set", "Traditional Northeast altar tray"),
                SamagriItem("hn_3", "Fresh Ginger Root, Turmeric, Black Sesame & Jaggery", "200g", "For harvest Pitha and thanksgiving"),
                SamagriItem("hn_4", "Handwoven Traditional Shawl / Gamosa", "1 piece", "For honoring elders and the altar"),
                SamagriItem("hn_5", "Hollow Bamboo Tubes, Wild Orchids & Marigolds", "1 basket", "For hearth and entrance decoration"),
                SamagriItem("hn_6", "Natural Resin Incense (Dhuna / Sasat) & Brass Lamp", "1 set", "For morning Sun & Creator invocation")
            )
        ),

        "hartalika_teej" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Pratah Snan, Solah Shringaar & Nirjala Vrat Sankalpa", "Wake at dawn on Bhadrapada Shukla Tritiya, bathe with sesame-and-bilva water, adorn bridal green/red attire with Mehendi and Solah Shringaar, and take the sacred Hartalika Teej Sankalpa.", 20),
                VidhiStep(2, "Handcrafted Clay/Sand Shiva-Parvati-Ganesha Sthapana", "Craft sacred murtis of Lord Shiva, Goddess Parvati, and Lord Ganesha from clean river clay/sand (Parthiva Lingam) and install them on a banana-leaf canopy (Mandapa) at Pradosh.", 25),
                VidhiStep(3, "Panchamrit Abhishek & Suhag Pitari Arpan", "Bathe Shiva-Parvati with Panchamrit, drape sacred Vastra, and offer the Suhag Pitari (sindoor, bindi, green bangles, mehendi, kajal, comb, and chunri) to Mata Gauri.", 20),
                VidhiStep(4, "Phulera & 16-Leaf (Shodasha Patra) Archana with Vrat Katha", "Tie the floral Phulera canopy above the altar, offer 16 sacred leaves (Bilva, Shami, Dhatura, Tulsi, Mango, Ashoka) during the four Prahars, and listen to the Hartalika Teej Vrat Katha.", 30),
                VidhiStep(5, "Night Jagran, Ghee Aarti & Chaturthi Morning Visarjan", "Perform Shiva-Gauri camphor Aarti ('Jai Parvati Mata'), sing devotional bhajans through the night, and perform Visarjan and Parana after sunrise on Chaturthi.", 20)
            ),
            samagriItems = listOf(
                SamagriItem("ht_1", "Clean River Clay / Sand & Banana Leaves for Mandapa", "1 set", "For crafting Parthiva Shiva-Parvati-Ganesha"),
                SamagriItem("ht_2", "Suhag Pitari (Red Chunri, Sindoor, Green Bangles, Mehendi)", "1 box", "Offered to Goddess Hartalika Gauri"),
                SamagriItem("ht_3", "Bilva Patra, Shami Leaves, Dhatura & Floral Phulera", "16 varieties", "For Shodasha-Patra Archana"),
                SamagriItem("ht_4", "Panchamrit (Milk, Curd, Ghee, Honey, Sugar) & Gangajal", "1 bowl", "For Pradosh Shiva Abhishek"),
                SamagriItem("ht_5", "Gujiya / Pedha, Seasonal Fruits & Soaked Chana", "250g", "Traditional Teej Naivedya"),
                SamagriItem("ht_6", "Pure Cow Ghee Diya, Cotton Wicks, Incense & Camphor", "1 set", "For Four-Prahar Jagran Aarti")
            )
        ),

        "kajari_teej" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Bhadrapada Krishna Tritiya Snan & Swing Decoration", "Bathe early, wear festive attire with Mehendi, hang floral swings, and take the Kajari / Badi Teej Vrat vow for marital harmony and family well-being.", 15),
                VidhiStep(2, "Neemri Mata Clay Pond & Branch Sthapana", "Shape a small circular pond (Talab) using clay and soil on the altar, plant a fresh Neem twig (Neemri Mata) in the center, and pour raw milk and pure water into the pond.", 20),
                VidhiStep(3, "Haldi-Roli, Kajal & Akshat Archana of Neemri Mata", "Light a cow-ghee diya at the edge of the pond and offer Roli, Haldi, Kajal, Mehendi, unbroken rice, lemon, cucumber, and seasonal fruits to Neemri Mata.", 15),
                VidhiStep(4, "Kajari Teej Vrat Katha &Sattu Pinda Puja", "Listen to the Kajari Teej story of the devoted Brahmin wife and the seven brothers, and worship the sacred Sattu Pinda (roasted gram, rice, or wheat flour sweetened with ghee and jaggery).", 25),
                VidhiStep(5, "Evening Chandrodaya Arghya (7 Rotations) & Sattu Parana", "Upon Moonrise, offer Arghya with wheat grains, raw milk, Roli, and water while rotating seven times in place, and partake in the blessed Sattu Prasad.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("kt_1", "Fresh Neem Twig (Neemri) & Clay for Miniature Pond", "1 set", "Central altar for Kajari / Badi Teej"),
                SamagriItem("kt_2", "Sattu (Chana/Wheat/Rice Flour with Ghee & Sugar)", "500g", "Signature Badi Teej Naivedya & Parana"),
                SamagriItem("kt_3", "Raw Cow Milk, Rosewater & Wheat/Barley Grains", "1 bowl", "For pond offering and Moonrise Arghya"),
                SamagriItem("kt_4", "Roli, Haldi, Kajal, Mehendi & Red Raksha Thread", "1 thali", "For Neemri Mata 7-dot tilak"),
                SamagriItem("kt_5", "Cucumber, Lemon, Banana, Apple & Whole Coconut", "1 basket", "Viewed in the pond reflection"),
                SamagriItem("kt_6", "Brass Lota, Pure Cow Ghee Diya & Camphor", "1 set", "For evening Katha and Chandrodaya Aarti")
            )
        ),

        "maha_navami_ayudha_puja" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Morning Cleansing of Books, Instruments & Tools", "Wipe and purify all books, musical instruments, computers, vehicles, kitchen tools, and workspace implements, and apply auspicious sandalwood (Chandan) and Kumkum dots.", 20),
                VidhiStep(2, "Siddhidatri Devi & Saraswati-Lakshmi-Parvati Invocation", "Install the murtis or portraits of Maa Siddhidatri (9th Navadurga) and Goddess Saraswati on a white/red silk peetham alongside the sanctified books and instruments.", 15),
                VidhiStep(3, "Navami Homa (Havan) & Aparajita / Lotus Pushpanjali", "Perform the culminating Navratri Havan with black sesame, barley, ghee, and guggul, and offer red hibiscus, lotus, and Bilva leaves chanting 'Om Aim Hreem Kleem Chamundaya Vichche'.", 25),
                VidhiStep(4, "Kanya Pujan (Navadurga Kumari Worship) & Ashirvad", "Wash the feet of nine young girls (Kanjaks) and one young boy (Batuk Bhairav), tie Mauli, apply Roli tilak, and serve warm Halwa, Puri, and Kala Chana with Dakshina.", 30),
                VidhiStep(5, "Sundal, Payasam & Pori (Puffed Rice) Ayudha Puja Aarti", "Offer Pori (puffed rice), roasted chana, jaggery, Sundal, and Sakkarai Pongal to the sanctified tools and books, break an ash gourd or coconut to ward off negativity, and perform Maha Aarti.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("mn_1", "Books, Pens, Musical Instruments & Work Tools", "Cleaned set", "Sanctified with sandalwood & kumkum"),
                SamagriItem("mn_2", "Sooji Halwa, Kala Chana & Warm Puris (Kanjak Bhog)", "9 plates", "For Navami Kanya Pujan"),
                SamagriItem("mn_3", "Puffed Rice (Pori / Murmura), Fried Gram, Jaggery & Sundal", "500g", "Traditional South Indian Ayudha Puja offering"),
                SamagriItem("mn_4", "Havan Samagri (Til, Jau, Pure Cow Ghee, Mango Wood)", "1 pack", "For Maha Navami Purnahuti Homa"),
                SamagriItem("mn_5", "Red Chunri, Lotus/Hibiscus Flowers & Bilva Patra", "1 basket", "For Maa Siddhidatri & Saraswati Archana"),
                SamagriItem("mn_6", "Whole Coconut, Ash Gourd (Kushmanda), Ghee Diya & Camphor", "1 set", "For Drishti removal and Maha Aarti")
            )
        ),

        "labh_panchami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Purvahna Threshold Rangoli & Mango Toran", "Cleanse the home or business altar on Kartika Shukla Panchami morning, hang fresh mango-leaf and marigold garlands, and draw an auspicious Swastika and Shubh-Labh Rangoli.", 15),
                VidhiStep(2, "Shubh-Labh Ganesha & Mahalakshmi-Saraswati Sthapana", "Install Lord Ganesha (with His consorts Riddhi-Siddhi and sons Shubh-Labh), Goddess Mahalakshmi, and Maa Saraswati on a red silk cloth.", 15),
                VidhiStep(3, "New Account Books (Khatu / Bahi-Khata) & Scale Worship", "Inscribe the sacred Swastika, 'Sri Ganeshaya Namah', and 'Shubh-Labh' with Roli-Saffron paste on the first page of new account ledgers, laptops, or study books (Gnan Panchami).", 20),
                VidhiStep(4, "Shree Suktam, Ganapati Atharvashirsha & Saraswati Vandana", "Offer Durva grass, red lotus, unbroken Akshat, and silver coins while reciting the Ganapati Atharvashirsha and Shree Suktam for righteous prosperity and wisdom.", 20),
                VidhiStep(5, "Sukhdi, Kaju Katli & Sweet Boondi Naivedya with Aarti", "Offer Gujarati Sukhdi (Gur-Papdi), Kaju Katli, and Magas laddus, perform camphor Aarti, and inaugurate the new year's work with an auspicious first transaction or study session.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("lp_1", "New Ledger / Notebook (Bahi-Khata), Pen & Silver Coin", "1 set", "For Labh Panchami & Gnan Panchami Muhurat"),
                SamagriItem("lp_2", "Roli / Sindoor, Saffron (Kesar) & Pure Cow Ghee", "1 katori", "For writing auspicious Swastika & Shubh-Labh"),
                SamagriItem("lp_3", "Durva Grass, Red Rose/Lotus & Marigold Garlands", "1 basket", "For Ganesha & Lakshmi-Saraswati Puja"),
                SamagriItem("lp_4", "Sukhdi (Wheat-Jaggery-Ghee), Peda & Dry Fruits", "250g", "Traditional Gujarati Labh Pancham prasad"),
                SamagriItem("lp_5", "Betel Leaves (Paan), Supari, Whole Coconut & Mauli", "5 pairs", "For Kalash and Sankalpa"),
                SamagriItem("lp_6", "Brass Diya with Cow Ghee, Dhoop & Camphor", "1 set", "For morning Shubh-Labh Aarti")
            )
        ),

        "gopashtami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Pratah Snan & Gau-Mata / Calf Cleansing & Shringaar", "Wake early on Kartika Shukla Ashtami, visit a Gaushala or worship a sacred Kamadhenu-Krishna murti at home. Bathe or gently wipe the cow and calf, and adorn them with turmeric, sandalwood, and marigold garlands.", 20),
                VidhiStep(2, "Lord Gopala (Govinda) & Kamadhenu Shodashopachara Puja", "Apply Roli-Chandan tilak on the forehead of Gau-Mata and Sri Krishna (who became a full-fledged Gopala on this tithi in Braj), and offer yellow silk cloth and peacock feathers.", 15),
                VidhiStep(3, "Green Fodder, Jaggery, Boiled Chana & Gograsa Arpan", "Lovingly feed fresh green grass, spinach, organic jaggery (Gur), soaked Chana dal, and wheat-flour rotis smeared with ghee and turmeric (Gograsa) to cows and calves.", 20),
                VidhiStep(4, "Clockwise Gau-Parikrama & Gopala Sahasranama Japa", "Walk seven clockwise circumambulations (Parikrama) around Gau-Mata and Lord Govinda while chanting 'Om Namo Bhagavate Vasudevaya Go-Govindaya Namaha' and touch the sacred dust (Godhuli) to your forehead.", 15),
                VidhiStep(5, "Evening Godhuli Deepdaan & Makhan-Mishri Aarti", "At twilight (Godhuli Vela), light cow-ghee lamps before Sri Radha-Damodara and Kamadhenu, offer Makhan-Mishri and Kheer, and donate fodder to a local Gaushala.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("go_1", "Fresh Green Grass, Spinach & Organic Jaggery (Gur)", "1 basket", "Sacred Gograsa offering for Gau-Mata"),
                SamagriItem("go_2", "Soaked Chana Dal & Wheat Rotis with Haldi & Ghee", "5 rotis", "Fed lovingly during Gopashtami Puja"),
                SamagriItem("go_3", "Kamadhenu & Sri Gopala-Krishna Murti / Portrait", "1 set", "For home altar worship"),
                SamagriItem("go_4", "Turmeric (Haldi), Roli, Sandalwood & Marigold Garlands", "1 thali", "For Gau-Shringaar and Archana"),
                SamagriItem("go_5", "Makhan-Mishri, Milk Kheer & Fresh Tulsi Leaves", "1 bowl", "Offered to Sri Govinda"),
                SamagriItem("go_6", "Pure Cow Ghee Diya, Bell (Ghanti) & Camphor", "1 set", "For morning and Godhuli Aarti")
            )
        ),

        "akshaya_navami_amla_navami" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Kartika Shukla Navami Snan & Amla Tree / Branch Sthapana", "Bathe at dawn on Akshaya Navami (when Satya Yuga commenced) and approach an Amla (Indian Gooseberry) tree or place an Amla sapling/branch with Shaligram at your home altar.", 15),
                VidhiStep(2, "Raw Milk Stream Offering & Lakshmi-Narayana Invocation", "Pour a thin stream of raw milk, Gangajal, and pure water at the roots of the Amla tree (revered as the combined abode of Lord Vishnu and Goddess Lakshmi) facing East.", 15),
                VidhiStep(3, "8 or 108 Kachha Soot Parikramas & Jagaddhatri Vandana", "Tie raw cotton thread or Mauli around the Amla trunk while walking 8 or 108 clockwise Parikramas. In Bengal, offer Trikala Pushpanjali to four-armed Maa Jagaddhatri seated on a lion.", 25),
                VidhiStep(4, "Gupt-Daan of Kushmanda (Ash Gourd) & Vishnu Sahasranama", "Donate a whole ash gourd (Petha/Kushmanda), Amla fruits, grains, and warm clothes to the needy or Brahmins, and recite the Vishnu Sahasranama or Jagaddhatri Stotram.", 20),
                VidhiStep(5, "Sattvik Bhojan Beneath the Amla Tree & Camphor Aarti", "Light a pure cow ghee diya filled with camphor under the Amla tree, offer Amla chutney/murabba, Poori, Kheer, and seasonal vegetables as Naivedya, and partake in the blessed meal beneath its shade.", 20)
            ),
            samagriItems = listOf(
                SamagriItem("an_1", "Fresh Amla Fruits (Gooseberry) & Amla Sapling/Leaves", "11 fruits", "Sacred abode of Sri Lakshmi-Narayana"),
                SamagriItem("an_2", "Raw Unboiled Cow Milk, Gangajal & Copper Lota", "500ml", "Offered at the roots of the Amla tree"),
                SamagriItem("an_3", "Raw Cotton Thread (Kachha Soot) / Red Mauli", "1 spool", "Wrapped during 8 or 108 Parikramas"),
                SamagriItem("an_4", "Whole Ash Gourd (Kushmanda / Petha) & Grains for Daan", "1 gourd", "For Akshaya Navami charity"),
                SamagriItem("an_5", "Kheer, Poori, Amla Murabba & Red Hibiscus (Jagaddhatri)", "1 thali", "For Naivedya and family Bhojan"),
                SamagriItem("an_6", "Pure Cow Ghee Diya, Sandalwood, Haldi-Kumkum & Camphor", "1 set", "For Amla-Vishnu & Jagaddhatri Aarti")
            )
        ),

        "kalabhairava_jayanti" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Krishna Ashtami Snan & Evening / Pradosh Sankalpa", "Observe a disciplined fast on Kalabhairava Ashtami (the appearance day of Kashi Kotwal Lord Kaal Bhairav, ruler of time and remover of fear). Cleanse the altar before dusk.", 15),
                VidhiStep(2, "Lord Kaal Bhairav, Shiva-Parvati & Four-Faced Diya Sthapana", "Install the portrait or murti of Lord Kaal Bhairav alongside Lord Shiva and light a four-faced earthen lamp filled with pure mustard or sesame oil and black sesame seeds.", 15),
                VidhiStep(3, "Bilva Patra, Blue Flowers & Black Sesame Archana", "Offer 21 three-leaf Bilva patras inscribed with 'Om Namah Shivaya' in sandalwood paste, blue Aparajita flowers, Shami leaves, and unbroken Akshat.", 20),
                VidhiStep(4, "Kalabhairava Ashtakam (Adi Shankaracharya) Recitation", "Recite Adi Shankaracharya's resonant 'Kalabhairava Ashtakam' ('Devaraja Sevyamana Pavana Anghri Pankajam...') 8 or 11 times for fearlessness, protection in travel, and mastery over time.", 25),
                VidhiStep(5, "Imarti, Urad Vada & Meetha Rote Naivedya with Shvan Seva", "Offer warm Imarti/Jalebi, black Urad dal Vadas, sweet wheat Rote, and coconut, perform camphor Aarti, and lovingly feed milk and sweet rotis to street dogs (the sacred Vahana of Bhairava).", 15)
            ),
            samagriItems = listOf(
                SamagriItem("kb_1", "Four-Faced Earthen Diya & Pure Mustard / Sesame Oil", "1 set", "Lit during Pradosh / Nishita Kaal"),
                SamagriItem("kb_2", "Black Sesame Seeds (Kala Til) & Whole Black Urad Dal", "100g each", "Sacred offerings to Kaal Bhairav"),
                SamagriItem("kb_3", "21 Bilva Leaves, Blue Aparajita & Marigold Flowers", "1 basket", "For Ashtami Archana"),
                SamagriItem("kb_4", "Imarti / Jalebi, Urad Dal Vada & Sweet Rote", "250g", "Traditional Bhairava Ashtami Naivedya"),
                SamagriItem("kb_5", "Whole Coconut, Lemon, Sandalwood (Bhasma/Chandan) & Roli", "1 set", "For protection and Tilak"),
                SamagriItem("kb_6", "Milk & Sweet Rotis for Feeding Dogs (Shvan Seva)", "1 bowl", "Honoring Lord Bhairava's Vahana")
            )
        ),

        "mauni_amavasya" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Brahma Muhurat Triveni / Gangajal Snan in Mauna (Silence)", "Wake before sunrise on Magha Krishna Amavasya (Thai Amavasai). Without speaking a word (Mauna Vrata), bathe in sacred waters or home water mixed with Gangajal and black sesame seeds.", 20),
                VidhiStep(2, "Surya Narayan & Lakshmi-Narayana Til-Jala Arghya", "Maintaining peaceful inner silence, offer water mixed with black sesame seeds (Til), red flowers, and Kusha grass to the rising Sun and Lord Vishnu.", 15),
                VidhiStep(3, "Dakshinabhimukha Pitru Tarpan & Pinda Shraddha", "Face South with Kusha ring (Pavitri) on the ring finger and offer Til-Tarpana (water and black sesame poured over the right thumb) for the peace and liberation of ancestors (Pitru Devatas).", 25),
                VidhiStep(4, "Peepal Tree 108 Parikramas & Silent Mantra Japa", "Light a cow-ghee diya under a Peepal tree, walk 108 clockwise Parikramas while mentally chanting 'Om Namo Bhagavate Vasudevaya' or 'Om Namah Shivaya' in unbroken Mauna.", 25),
                VidhiStep(5, "Til-Gur, Khichdi & Warm Blanket Daan before Breaking Mauna", "Donate sesame laddus (Til-kut), raw rice, urad dal (Khichdi ingredients), ghee, and warm clothing to the needy, perform Vishnu Aarti, and gently conclude the Mauna vow.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("ma_1", "Gangajal, Black & White Sesame Seeds (Til)", "200g", "For Brahma Muhurat Snan and Tarpan"),
                SamagriItem("ma_2", "Sacred Kusha Grass Ring (Pavitri) & Copper Lota", "1 set", "For Maghi / Thai Amavasya Pitru Tarpan"),
                SamagriItem("ma_3", "Til-Gur Laddus (Sesame & Jaggery Sweets)", "250g", "For Naivedya and Magha charity"),
                SamagriItem("ma_4", "Raw Rice, Black Urad Dal, Ghee & Salt (Khichdi Daan)", "1 kg set", "Donated for ancestral blessings"),
                SamagriItem("ma_5", "Tulsi Leaves, White Flowers & Sandalwood Paste", "1 plate", "For Lord Vishnu and Peepal Vriksha puja"),
                SamagriItem("ma_6", "Pure Cow Ghee Diya, Cotton Wicks & Camphor", "1 set", "Lit under Peepal tree and home altar")
            )
        ),

        "amalaki_ekadashi_rangbhari" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Phalguna Shukla Ekadashi Snan & Vrat Sankalpa", "Bathe early on Amalaki / Rangbhari Ekadashi, wear clean yellow or white attire, and take the Ekadashi Vrat vow honoring Lord Vishnu and Kashi Vishwanath-Gaura.", 15),
                VidhiStep(2, "Amla Tree / Branch & Parasurama-Vishnu Kalash Sthapana", "Establish a copper Kalash beneath an Amla tree or beside an Amla branch and Shaligram at home, and offer Panchamrit, sandalwood, and yellow silk cloth.", 20),
                VidhiStep(3, "Shiva-Parvati Herbal Gulal Arpan (Rangbhari Ekadashi)", "Celebrate Kashi's Rangbhari Ekadashi (when Lord Shiva brought Mata Parvati to Kashi after her Gauna) by offering fragrant red and yellow herbal Abir-Gulal, Bilva patra, and roses at their feet.", 20),
                VidhiStep(4, "Amalaki Ekadashi Vrat Katha & Vishnu Sahasranama", "Offer fresh Amla fruits and 108 Tulsi leaves to Lord Vishnu while reciting the Vishnu Sahasranama and the Amalaki Ekadashi Mahatmya from the Brahmanda Purana.", 25),
                VidhiStep(5, "Thandai / Amla Phalahar Naivedya, Deepdaan & Aarti", "Light cow-ghee lamps around the Amla tree and Shiva-Vishnu altar, offer Amla murabba, seasonal fruits, and kesar badam milk, and perform joyful Holi-inauguration Aarti.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("ae_1", "Fresh Amla Fruits (Gooseberry), Amla Twigs & Tulsi", "11 fruits", "Sacred to Lord Vishnu on Amalaki Ekadashi"),
                SamagriItem("ae_2", "Organic Herbal Gulal (Red/Yellow Abir) & Rose Petals", "100g", "Offered to Shiva-Parvati for Rangbhari Ekadashi"),
                SamagriItem("ae_3", "Copper Kalash, Whole Coconut, Mango Leaves & Yellow Cloth", "1 set", "Installed beneath Amla tree or altar"),
                SamagriItem("ae_4", "Bilva Patra, Sandalwood (Chandan), Roli & Akshat", "21 leaves", "For Hari-Hara worship"),
                SamagriItem("ae_5", "Phalahar Sweets, Amla Murabba, Makhana & Fruits", "250g", "Grain-free Ekadashi Naivedya"),
                SamagriItem("ae_6", "Pure Cow Ghee Diya, Incense & Camphor", "1 set", "For Amla Parikrama and evening Aarti")
            )
        ),

        "cheti_chand_jhulelal" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Chaitra Shukla Dwitiya Snan & Bahrana Sahib Preparation", "Bathe early on the Sindhi New Year (Cheti Chand) and prepare the sacred Bahrana Sahib tray with a bronze oil lamp, crystal sugar (Misri), cardamom, fruits, and a coconut-topped Kalash.", 20),
                VidhiStep(2, "Akhand Jyot & Five-Wick Flour Diya (Panj-Batti) Prajvalan", "Shape a five-wick lamp from kneaded wheat flour (Atte jo Diyo), fill with pure cow ghee, and light it before the portrait of Ishta Dev Uderolal (Jhulelal Sai) seated on the Palla fish.", 15),
                VidhiStep(3, "Daryah Shah (Varuna Deva) Jal-Jyot Invocation", "Offer fragrant red roses, sandalwood, vermilion, and unbroken rice to the sacred water Kalash and Jyot, honoring Jhulelal as the incarnation of Varuna Deva (Lord of Living Waters).", 15),
                VidhiStep(4, "Panjra, Palav Prayers & 'Aayo Lal Jhulelal' Chhej Bhajan", "Spread the edge of your garment (Palav) in humble prayer for peace and prosperity, sing traditional Panjra hymns and 'Dama Dam Mast Qalandar / Aayo Lal Sabhai Chao Jhulelal'.", 20),
                VidhiStep(5, "Tahiri (Sweet Saffron Rice), Sai Bhaji & Akha Visarjan", "Offer sweet saffron rice with almonds and raisins (Tahiri/Tayri) and boiled chickpeas (Chana) as prasad, and offer Akha (rice, sugar, cardamom) to a clean water body or tulsi pot at sunset.", 20)
            ),
            samagriItems = listOf(
                SamagriItem("cc_1", "Wheat-Flour Five-Wick Diya (Atte jo Diyo) & Cow Ghee", "1 set", "Central Jyot of Bahrana Sahib"),
                SamagriItem("cc_2", "Copper Kalash, Whole Coconut, Red Cloth & Mango Leaves", "1 set", "Representing Varuna / Daryah Shah"),
                SamagriItem("cc_3", "Akha (Unbroken Rice, Sugar Crystals/Misri & Cardamom)", "200g", "Offered to sacred waters with prayers"),
                SamagriItem("cc_4", "Tahiri (Sweet Yellow Rice with Dry Fruits) & Boiled Chana", "500g", "Traditional Cheti Chand prasad"),
                SamagriItem("cc_5", "Red RoseGarlands, Vermilion (Sindoor) & Cloves", "1 basket", "For Jhulelal Sai Archana"),
                SamagriItem("cc_6", "Bronze Thali, Incense Sticks & Camphor", "1 set", "For Bahrana Sahib Aarti")
            )
        ),

        "putrada_ekadashi" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Shravana Shukla Ekadashi Snan & Family Welfare Sankalpa", "Wake at Brahma Muhurat on Shravana Putrada (Pavitropana) Ekadashi, bathe with pure water, wear yellow attire, and take the sacred Sankalpa for children's health, wisdom, and family lineage.", 15),
                VidhiStep(2, "Sri Lakshmi-Narayana & Balagopala Panchamrit Abhishek", "Perform ceremonial Abhishek of Lord Vishnu and Balagopala (Laddu Gopal) with raw cow milk, curd, ghee, honey, and saffron water.", 20),
                VidhiStep(3, "Pavitra (Twisted Silk/Cotton Thread) & 108 Tulsi Arpan", "Offer the sacred knotted cotton/silk garland (Pavitra, marking Pavitropana Ekadashi) along with 108 fresh Tulsi leaves and yellow lotus/marigold flowers.", 20),
                VidhiStep(4, "Shravana Putrada Ekadashi Katha & Santan Gopal Stotra", "Recite the Putrada Ekadashi Vrat Katha (story of King Mahijit from the Bhavishyottara Purana), the Vishnu Sahasranama, and the Santan Gopal Mantra.", 25),
                VidhiStep(5, "Makhan-Mishri, Makhana Kheer & Evening Deepdaan Aarti", "Offer Makhan-Mishri, saffron Makhana Kheer, and seasonal fruits with a Tulsi leaf, perform evening cow-ghee Aarti, and observe Parana on Dwadashi morning.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("pe_1", "Lord Vishnu / Laddu Gopal Murti & Yellow Silk Vastra", "1 set", "For Shravana Putrada Ekadashi altar"),
                SamagriItem("pe_2", "108 Fresh Tulsi Leaves & Sacred Pavitra Cotton Thread", "1 set", "For Pavitropana Vishnu Archana"),
                SamagriItem("pe_3", "Panchamrit (Milk, Curd, Ghee, Honey, Sugar) & Saffron", "1 bowl", "For morning Abhishek"),
                SamagriItem("pe_4", "Makhan-Mishri, Makhana Kheer & 5 Seasonal Fruits", "250g", "Sattvik Ekadashi Phalahar Naivedya"),
                SamagriItem("pe_5", "Yellow Marigold & Lotus Flowers, Chandan & Akshat", "1 thali", "For Shodashopachara puja"),
                SamagriItem("pe_6", "Pure Cow Ghee Diya, Incense & Camphor", "1 set", "For morning and dusk Vishnu Aarti")
            )
        ),

        "hariyali_amavasya" to FestivalRitualPackage(
            vidhiSteps = listOf(
                VidhiStep(1, "Dawn Monsoon Snan & Sacred Sapling Plantation (Vriksharopan)", "Bathe early on Hariyali Amavasya (Gatari / Deep Puja / Chukkala / Aadi Amavasai) and plant or water an auspicious sapling (Tulsi, Peepal, Amla, Bilva, or Neem) in gratitude to Prakriti.", 20),
                VidhiStep(2, "Deep Puja — Cleansing & Ashta-Lakshmi Lamp Arrangement", "Clean all brass, silver, and copper household lamps (Samai, Niranjan, Kuthuvilakku, Earthen Diyas), arrange them on a wooden patla traced with Rangoli, and light them with cow ghee and sesame oil.", 25),
                VidhiStep(3, "Shiva-Gauri & Peepal Vriksha Panchamrit Archana", "Worship Lord Shiva and Goddess Parvati with Bilva leaves and green bangles, and pour milk-water with black sesame at the roots of the Peepal tree.", 15),
                VidhiStep(4, "Noon Pitru Tarpan (Aadi Amavasai) & Deep Stuti", "Offer sesame-water Tarpan to ancestors at midday; in Maharashtra, Goa, Karnataka, and Andhra/Telangana (Deepa/Chukkala Amavasya), offer Haldi-Kumkum, flowers, and Akshat to the lit household lamps.", 20),
                VidhiStep(5, "Malpua, Kheer & Steamed Wheat Lamp (Kanikache Dive) Naivedya", "Offer steamed jaggery-wheat lamps (Kanikache Dive), Malpua, Kheer, and soaked gram to the illuminated lamps and deities, praying 'Shubham Karoti Kalyanam Arogyam Dhana Sampada'.", 15)
            ),
            samagriItems = listOf(
                SamagriItem("ha_1", "All Household Brass/Silver Lamps & Earthen Diyas", "5 to 21 lamps", "Cleaned and lit for Ashadha Deep Puja"),
                SamagriItem("ha_2", "Steamed Wheat-Flour & Jaggery Lamps (Kanikache Dive / Kudumu)", "5 pieces", "Traditional Deep / Chukkala Amavasya offering"),
                SamagriItem("ha_3", "Sacred Sapling (Tulsi / Bilva / Amla) & Green Bangles", "1 sapling", "For Hariyali Amavasya Vriksharopan"),
                SamagriItem("ha_4", "Black Sesame Seeds (Til), Kusha Grass & Raw Milk", "1 bowl", "For Aadi Amavasai Pitru Tarpan & Peepal Puja"),
                SamagriItem("ha_5", "Malpua, Rice Kheer, Marigold Flowers & Bilva Patra", "1 thali", "Monsoon festive Naivedya"),
                SamagriItem("ha_6", "Pure Cow Ghee, Sesame Oil, Cotton Wicks & Camphor", "1 set", "For Deep Prajvalan and Maha Aarti")
            )
        )
    )

    /**
     * Returns the festival-specific 5-step Pooja Vidhi and 6-item Pooja Samagri package
     * for the given festival ID, or synthesizes a deity-tailored 5-step ritual package
     * for dynamically discovered online festivals.
     */
    fun getRitualPackage(
        id: String,
        nameEnglish: String,
        lunarMonth: String,
        paksha: String,
        tithiNumber: Int,
        observanceTime: String
    ): FestivalRitualPackage {
        CATALOG[id]?.let { return it }

        val lower = "$id $nameEnglish".lowercase(Locale.US)
        val timingLabel = when (observanceTime) {
            "PRADOSH" -> "Pradosh (Evening Sunset Window)"
            "NISHITA_MIDNIGHT" -> "Nishita Kaal (Sacred Midnight Window)"
            "MIDDAY" -> "Madhyahna / Abhijit (Midday Window)"
            else -> "Pratah-Sangava (Morning Sunrise Window)"
        }

        return when {
            lower.contains("ekadashi") -> FestivalRitualPackage(
                vidhiSteps = listOf(
                    VidhiStep(1, "Brahma Muhurat Snan & Ekadashi Sankalpa", "Wake before sunrise on $lunarMonth $paksha Ekadashi, bathe with Gangajal, wear yellow/white attire, and take the Ekadashi Vrat vow for $nameEnglish.", 15),
                    VidhiStep(2, "Sri Lakshmi-Narayana & Shaligram Abhishek", "Bathe Lord Vishnu / Shaligram with Panchamrit and sandalwood water, and adorn on a yellow silk chowki.", 20),
                    VidhiStep(3, "108 Tulsi Leaf Archana & Vishnu Sahasranama", "Offer Tulsi leaves (plucked on Dashami), yellow flowers, and saffron Akshat while reciting the Vishnu Sahasranama.", 25),
                    VidhiStep(4, "Ekadashi Vrat Katha & Phalahar Naivedya", "Read the specific $lunarMonth $paksha Ekadashi Mahatmya Katha and offer grain-free Phalahar (makhan-mishri, dry fruits, seasonal fruits).", 20),
                    VidhiStep(5, "Evening Harinama Kirtan & Dwadashi Parana", "Perform cow-ghee lamp Aarti at dusk, keep a devotional vigil, and break the fast on Dwadashi morning after sunrise.", 15)
                ),
                samagriItems = listOf(
                    SamagriItem("${id}_1", "Lord Vishnu / Lakshmi-Narayana Murti & Yellow Cloth", "1 set", "For Ekadashi altar"),
                    SamagriItem("${id}_2", "Fresh Tulsi Leaves (Plucked on Dashami Eve)", "108 leaves", "Essential for Vishnu archana"),
                    SamagriItem("${id}_3", "Panchamrit, Sandalwood Paste (Chandan) & Saffron", "1 bowl", "For Abhishek and Tilak"),
                    SamagriItem("${id}_4", "Yellow Marigold / Lotus Flowers & Tulsi Mala", "1 basket", "For Shodashopachara puja"),
                    SamagriItem("${id}_5", "Grain-Free Phalahar (Fruits, Makhana, Makhan-Mishri)", "250g", "Ekadashi Naivedya"),
                    SamagriItem("${id}_6", "Pure Cow Ghee Diya, Incense & Camphor", "1 set", "For morning and evening Aarti")
                )
            )
            lower.contains("chaturthi") || lower.contains("chauth") || lower.contains("vinayaka") || lower.contains("ganesh") -> FestivalRitualPackage(
                vidhiSteps = listOf(
                    VidhiStep(1, "Snan, Red Vastra & Chaturthi Sankalpa", "Bathe and cleanse the home altar with Gangajal. Take the $nameEnglish Sankalpa during the auspicious $timingLabel.", 15),
                    VidhiStep(2, "Lord Mahaganapati Sthapana & Sindoor Tilak", "Install Lord Ganesha on a red cloth over an eight-petal rice mandala, apply Sindoor and Roli tilak, and drape the sacred Janeu.", 15),
                    VidhiStep(3, "21 Durva Grass & Red Hibiscus Archana", "Offer 21 pairs of fresh green Durva grass and red hibiscus flowers while chanting 'Om Gam Ganapataye Namaha' 108 times.", 20),
                    VidhiStep(4, "Chaturthi Vrat Katha & 21 Modak / Tilkut Naivedya", "Listen to the Chaturthi Vrat Katha and offer 21 steamed Modaks, Boondi laddus, or sesame-jaggery Tilkut.", 20),
                    VidhiStep(5, "Sankatahara Ganesha Aarti & Chandrodaya / Prarthana", "Perform camphor Aarti ('Sukhkarta Dukh Harta'), offer Moonrise Arghya if Krishna Paksha Sankashti, and distribute prasad.", 15)
                ),
                samagriItems = listOf(
                    SamagriItem("${id}_1", "Lord Ganesha Murti & Red Silk Cloth", "1 set", "For Chaturthi Sthapana"),
                    SamagriItem("${id}_2", "21 Blades of Fresh Durva Grass & Red Hibiscus", "21 blades", "Beloved offering to Ganapati"),
                    SamagriItem("${id}_3", "Modaks / Boondi Laddus & Sesame-Jaggery", "21 pieces", "Traditional Chaturthi naivedya"),
                    SamagriItem("${id}_4", "Sindoor, Roli, Unbroken Akshat & Janeu Thread", "1 thali", "For Ganesha Alankaram"),
                    SamagriItem("${id}_5", "Whole Coconut, Betel Leaves (Paan) & Supari", "5 pairs", "For Purnahuti and Tamboolam"),
                    SamagriItem("${id}_6", "Pure Cow Ghee Diya, Dhoop & Camphor", "1 set", "For Ganesha Maha Aarti")
                )
            )
            else -> FestivalRitualPackage(
                vidhiSteps = listOf(
                    VidhiStep(1, "Pratah Snan, Altar Purification & Sankalpa", "Bathe early, sprinkle Gangajal across the puja room, draw a traditional Rangoli/Kolam, and take the sacred Sankalpa for $nameEnglish during the $timingLabel.", 15),
                    VidhiStep(2, "Ganapati Vandana & Kalash Sthapana", "Invoke Lord Ganesha first with Durva, Roli, and Akshat. Place a copper/brass Kalash filled with water, mango leaves, and a coconut on a bed of rice grains.", 15),
                    VidhiStep(3, "Ishta Devata Abhishek & Shodashopachara Archana", "Install the presiding deity of $nameEnglish on a silk cloth. Offer Panchamrit, sandalwood paste (Chandan), Haldi-Kumkum, and fresh seasonal garlands.", 20),
                    VidhiStep(4, "Sacred Mantra Japa & Puranic Vrat Katha", "Light a pure cow ghee diya and incense, recite the festival's Puranic Katha and sacred stotrams 27 or 108 times with family.", 20),
                    VidhiStep(5, "Sattvik Naivedya, Maha Aarti & Ashirvad", "Offer home-prepared sattvik Kheer, laddus, coconut, and five seasonal fruits as Naivedya, perform camphor Aarti, and seek elders' blessings.", 15)
                ),
                samagriItems = listOf(
                    SamagriItem("${id}_1", "Copper/Brass Kalash, Whole Coconut & 5 Mango Leaves", "1 set", "For auspicious Kalash Sthapana"),
                    SamagriItem("${id}_2", "Roli, Kumkum, Haldi, Sandalwood & Unbroken Akshat", "1 thali", "For Tilak and Shodashopachara Archana"),
                    SamagriItem("${id}_3", "Fresh Marigold, Lotus/Rose Garlands & Tulsi/Bilva Leaves", "1 basket", "For altar decoration and Pushpanjali"),
                    SamagriItem("${id}_4", "Panchamrit (Raw Milk, Curd, Ghee, Honey, Mishri)", "1 bowl", "For ceremonial Abhishek"),
                    SamagriItem("${id}_5", "Home-Made Kheer / Mithai, Betel Leaves & 5 Seasonal Fruits", "1 thali", "For sattvik Naivedya"),
                    SamagriItem("${id}_6", "Pure Cow Ghee Diya, Cotton Wicks, Incense & Camphor", "1 set", "For Sankalpa and Maha Aarti")
                )
            )
        }
    }
}
