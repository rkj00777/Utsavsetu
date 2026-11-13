package com.example.panchang

import kotlin.math.*

enum class RegionalPanchangConvention(
    val code: String,
    val displayName: String,
    val primaryRegions: String,
    val monthScheme: String
) {
    PURNIMANTA_NORTH(
        code = "purnimanta_north",
        displayName = "Purnimanta (Vikram Samvat)",
        primaryRegions = "UP, Bihar, MP, Rajasthan, Haryana, Punjab, Delhi, HP, Uttarakhand, J&K",
        monthScheme = "Month ends on Purnima (Full Moon). Krishna paksha starts the month."
    ),
    AMANTA_GUJARAT_MAHARASHTRA(
        code = "amanta_west",
        displayName = "Amanta (Shalivahana Shaka / Vikram)",
        primaryRegions = "Maharashtra, Gujarat, Goa",
        monthScheme = "Month ends on Amavasya (New Moon). Shukla paksha starts the month."
    ),
    AMANTA_SOUTH(
        code = "amanta_south",
        displayName = "Amanta (Chandramana)",
        primaryRegions = "Karnataka, Andhra Pradesh, Telangana",
        monthScheme = "Month ends on Amavasya (New Moon). Ugadi marks Chaitra Shukla Pratipada."
    ),
    TAMIL_SOLAR(
        code = "tamil_solar",
        displayName = "Tamil Solar (Sauramana)",
        primaryRegions = "Tamil Nadu, Puducherry",
        monthScheme = "Solar Sankranti months (Chithirai, Vaikasi... Margazhi, Thai)."
    ),
    MALAYALAM_SOLAR(
        code = "malayalam_solar",
        displayName = "Kollam Era (Malayalam Solar)",
        primaryRegions = "Kerala",
        monthScheme = "Solar Rashi transit months (Chingam, Kanni... Medam, Edavam)."
    ),
    BENGALI_SOLAR(
        code = "bengali_solar",
        displayName = "Bangabda (Bengali Solar)",
        primaryRegions = "West Bengal, Assam, Tripura",
        monthScheme = "Solar calendar beginning with Poila Boishakh."
    ),
    ODIA_SOLAR(
        code = "odia_solar",
        displayName = "Odia Panji",
        primaryRegions = "Odisha",
        monthScheme = "Solar Sankranti combined with Purnimanta lunar festivals."
    ),
    DIASPORA_LOCAL_SOLAR(
        code = "diaspora_local",
        displayName = "International Diaspora (Local Transit)",
        primaryRegions = "Worldwide Diaspora (US, UK, Canada, UAE, Singapore, Australia)",
        monthScheme = "Calculated at exact local longitude & latitude with local timezone."
    )
}

sealed class CityLookupResult {
    data class Found(val city: City) : CityLookupResult()
    data class Unknown(val requestedId: String) : CityLookupResult()
}

data class City(
    val id: String,
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String = "Asia/Kolkata",
    val isLocationDetected: Boolean = false,
    val district: String = state,
    val country: String = "India",
    val aliases: List<String> = emptyList(),
    val panchangConvention: RegionalPanchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH,
    val altitudeMeters: Double = 0.0
) {
    companion object {
        // ====================================================================
        // 1. TOP INDIAN METROPOLITAN HUBS
        // ====================================================================
        val BENGALURU = City("bengaluru", "Bengaluru", "Karnataka", 12.9716, 77.5946, district = "Bengaluru Urban", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Bangalore", "Bengaluru City"))
        val MUMBAI = City("mumbai", "Mumbai", "Maharashtra", 19.0760, 72.8777, district = "Mumbai City", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Bombay", "Navi Mumbai", "Thane"))
        val DELHI = City("delhi", "Delhi NCR", "Delhi", 28.6139, 77.2090, district = "New Delhi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("New Delhi", "Noida", "Gurugram", "Gurgaon", "Faridabad", "Ghaziabad"))
        val HYDERABAD = City("hyderabad", "Hyderabad", "Telangana", 17.3850, 78.4867, district = "Hyderabad", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Secunderabad", "Cyberabad"))
        val PUNE = City("pune", "Pune", "Maharashtra", 18.5204, 73.8567, district = "Pune", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Poona", "Pimpri Chinchwad"))
        val CHENNAI = City("chennai", "Chennai", "Tamil Nadu", 13.0827, 80.2707, district = "Chennai", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Madras"))
        val KOLKATA = City("kolkata", "Kolkata", "West Bengal", 22.5726, 88.3639, district = "Kolkata", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR, aliases = listOf("Calcutta", "Howrah"))
        val AHMEDABAD = City("ahmedabad", "Ahmedabad", "Gujarat", 23.0225, 72.5714, district = "Ahmedabad", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Amdavad", "Gandhinagar"))
        val JAIPUR = City("jaipur", "Jaipur", "Rajasthan", 26.9124, 75.7873, district = "Jaipur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Pink City"))
        val LUCKNOW = City("lucknow", "Lucknow", "Uttar Pradesh", 26.8467, 80.9462, district = "Lucknow", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Awadh"))
        val KOCHI = City("kochi", "Kochi", "Kerala", 9.9312, 76.2673, district = "Ernakulam", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Cochin", "Ernakulam"))
        val BHOPAL = City("bhopal", "Bhopal", "Madhya Pradesh", 23.2599, 77.4126, district = "Bhopal", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)

        // ====================================================================
        // 2. SACRED PILGRIMAGE KSHTRAS & CULTURAL HUBS
        // ====================================================================
        val VARANASI = City("varanasi", "Varanasi", "Uttar Pradesh", 25.3176, 82.9739, district = "Varanasi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Kashi", "Banaras", "Benares"))
        val AYODHYA = City("ayodhya", "Ayodhya", "Uttar Pradesh", 26.7922, 82.1998, district = "Ayodhya", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Faizabad", "Ram Janmabhoomi", "Saket"))
        val UJJAIN = City("ujjain", "Ujjain", "Madhya Pradesh", 23.1765, 75.7885, district = "Ujjain", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Avantika", "Mahakaleshwar"))
        val PRAYAGRAJ = City("prayagraj", "Prayagraj", "Uttar Pradesh", 25.4358, 81.8463, district = "Prayagraj", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Allahabad", "Triveni Sangam", "Prayag"))
        val HARIDWAR = City("haridwar", "Haridwar", "Uttarakhand", 29.9457, 78.1642, district = "Haridwar", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Hardwar", "Gangadwara", "Mayapuri"))
        val RISHIKESH = City("rishikesh", "Rishikesh", "Uttarakhand", 30.0869, 78.2676, district = "Dehradun", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Hrishikesh", "Yoga Capital"))
        val MATHURA = City("mathura", "Mathura", "Uttar Pradesh", 27.4924, 77.6737, district = "Mathura", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Braj", "Krishna Janmabhoomi"))
        val VRINDAVAN = City("vrindavan", "Vrindavan", "Uttar Pradesh", 27.5806, 77.7006, district = "Mathura", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Brindavan", "Braj Dham"))
        val TIRUPATI = City("tirupati", "Tirupati", "Andhra Pradesh", 13.6288, 79.4192, district = "Tirupati", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Tirumala", "Sri Venkateswara Kshetram"))
        val PURI = City("puri", "Puri", "Odisha", 19.8135, 85.8312, district = "Puri", panchangConvention = RegionalPanchangConvention.ODIA_SOLAR, aliases = listOf("Jagannath Puri", "Purusottama Kshetra", "Srikshetra"))
        val RAMESWARAM = City("rameswaram", "Rameswaram", "Tamil Nadu", 9.2876, 79.3129, district = "Ramanathapuram", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Rameshwaram", "Sethu"))
        val MADURAI = City("madurai", "Madurai", "Tamil Nadu", 9.9252, 78.1198, district = "Madurai", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Meenakshi Amman Kshetram", "Temple City"))
        val DWARKA = City("dwarka", "Dwarka", "Gujarat", 22.2442, 68.9685, district = "Devbhumi Dwarka", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Dwaraka", "Dvaravati", "Jagad Mandir"))
        val SOMNATH = City("somnath", "Somnath", "Gujarat", 20.8880, 70.4012, district = "Gir Somnath", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Prabhas Patan", "First Jyotirlinga"))
        val SHIRDI = City("shirdi", "Shirdi", "Maharashtra", 19.7645, 74.4762, district = "Ahmednagar", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Sai Baba Kshetram"))
        val BADRINATH = City("badrinath", "Badrinath", "Uttarakhand", 30.7433, 79.4938, district = "Chamoli", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Badrikashram", "Char Dham"))
        val KEDARNATH = City("kedarnath", "Kedarnath", "Uttarakhand", 30.7352, 79.0669, district = "Rudraprayag", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Kedar Khanda", "Jyotirlinga"))
        val GAYA = City("gaya", "Gaya", "Bihar", 24.7914, 85.0002, district = "Gaya", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Vishnupad", "Pitru Tarpan Kshetra"))
        val KAMAKHYA = City("kamakhya", "Kamakhya", "Assam", 26.1664, 91.7052, district = "Kamrup Metropolitan", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR, aliases = listOf("Guwahati Nilachal", "Shakti Peetha"))
        val HAMPI = City("hampi", "Hampi", "Karnataka", 15.3350, 76.4600, district = "Vijayanagara", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Kishkindha", "Virupaksha"))
        val THANJAVUR = City("thanjavur", "Thanjavur", "Tamil Nadu", 10.7870, 79.1378, district = "Thanjavur", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Tanjore", "Brihadeeswara"))
        val GURUVAYUR = City("guruvayur", "Guruvayur", "Kerala", 10.5946, 76.0416, district = "Thrissur", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Bhuloka Vaikuntham", "Guruvayoor"))
        val PANDHARPUR = City("pandharpur", "Pandharpur", "Maharashtra", 17.6775, 75.3240, district = "Solapur", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Vithoba Kshetram", "Pandhari"))
        val TRIMBAKESHWAR = City("trimbakeshwar", "Trimbakeshwar", "Maharashtra", 19.9322, 73.5308, district = "Nashik", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Trimbak", "Godavari Origin"))
        val OMKARESHWAR = City("omkareshwar", "Omkareshwar", "Madhya Pradesh", 22.2464, 76.1517, district = "Khandwa", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Mandhata", "Narmada Jyotirlinga"))
        val KURUKSHETRA = City("kurukshetra", "Kurukshetra", "Haryana", 29.9695, 76.8783, district = "Kurukshetra", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Dharmakshetra", "Brahma Sarovar"))
        val PUSHKAR = City("pushkar", "Pushkar", "Rajasthan", 26.4897, 74.5511, district = "Ajmer", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Brahma Mandir"))
        val CHITRAKOOT = City("chitrakoot", "Chitrakoot", "Uttar Pradesh", 25.1782, 80.8654, district = "Chitrakoot", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Ramghat", "Kamadgiri"))
        val PALITANA = City("palitana", "Palitana", "Gujarat", 21.5222, 71.8267, district = "Bhavnagar", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Shatrunjaya"))
        val GANGOTRI = City("gangotri", "Gangotri", "Uttarakhand", 30.9947, 78.9398, district = "Uttarkashi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Ganga Origin"))
        val YAMUNOTRI = City("yamunotri", "Yamunotri", "Uttarakhand", 31.0140, 78.4600, district = "Uttarkashi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Yamuna Origin"))

        // ====================================================================
        // 3. STATE CAPITALS, UNION TERRITORIES & REGIONAL DISTRICT HEADQUARTERS
        // ====================================================================
        val AMRITSAR = City("amritsar", "Amritsar", "Punjab", 31.6340, 74.8723, district = "Amritsar", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Golden Temple City"))
        val CHANDIGARH = City("chandigarh", "Chandigarh", "Chandigarh", 30.7333, 76.7794, district = "Chandigarh", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("City Beautiful"))
        val PATNA = City("patna", "Patna", "Bihar", 25.5941, 85.1376, district = "Patna", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Pataliputra"))
        val RANCHI = City("ranchi", "Ranchi", "Jharkhand", 23.3441, 85.3096, district = "Ranchi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val BHUBANESWAR = City("bhubaneswar", "Bhubaneswar", "Odisha", 20.2961, 85.8245, district = "Khordha", panchangConvention = RegionalPanchangConvention.ODIA_SOLAR, aliases = listOf("Ekamra Kshetra", "Temple City of the East"))
        val GUWAHATI = City("guwahati", "Guwahati", "Assam", 26.1445, 91.7362, district = "Kamrup Metropolitan", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR, aliases = listOf("Gauhati", "Pragjyotishpura"))
        val VISAKHAPATNAM = City("visakhapatnam", "Visakhapatnam", "Andhra Pradesh", 17.6868, 83.2185, district = "Visakhapatnam", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Vizag", "Waltair"))
        val VIJAYAWADA = City("vijayawada", "Vijayawada", "Andhra Pradesh", 16.5062, 80.6480, district = "NTR", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Bezawada", "Kanaka Durga Kshetram"))
        val GUNTUR = City("guntur", "Guntur", "Andhra Pradesh", 16.3067, 80.4365, district = "Guntur", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH)
        val WARANGAL = City("warangal", "Warangal", "Telangana", 17.9689, 79.5941, district = "Hanamkonda", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Orugallu", "Thousand Pillar Temple"))
        val MYSURU = City("mysuru", "Mysuru", "Karnataka", 12.2958, 76.6394, district = "Mysuru", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Mysore", "Chamundi Kshetram"))
        val MANGALURU = City("mangaluru", "Mangaluru", "Karnataka", 12.9141, 74.8560, district = "Dakshina Kannada", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Mangalore", "Mangaladevi"))
        val HUBBALLI = City("hubballi", "Hubballi-Dharwad", "Karnataka", 15.3647, 75.1240, district = "Dharwad", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Hubli"))
        val BELAGAVI = City("belagavi", "Belagavi", "Karnataka", 15.8497, 74.4977, district = "Belagavi", panchangConvention = RegionalPanchangConvention.AMANTA_SOUTH, aliases = listOf("Belgaum"))
        val COIMBATORE = City("coimbatore", "Coimbatore", "Tamil Nadu", 11.0168, 76.9558, district = "Coimbatore", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Kovai"))
        val TIRUCHIRAPPALLI = City("tiruchirappalli", "Tiruchirappalli", "Tamil Nadu", 10.7905, 78.7047, district = "Tiruchirappalli", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Trichy", "Srirangam"))
        val SALEM = City("salem", "Salem", "Tamil Nadu", 11.6643, 78.1460, district = "Salem", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR)
        val TIRUNELVELI = City("tirunelveli", "Tirunelveli", "Tamil Nadu", 8.7139, 77.7567, district = "Tirunelveli", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Nellai", "Nellaiappar"))
        val THIRUVANANTHAPURAM = City("thiruvananthapuram", "Thiruvananthapuram", "Kerala", 8.5241, 76.9366, district = "Thiruvananthapuram", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Trivandrum", "Padmanabhaswamy Temple"))
        val KOZHIKODE = City("kozhikode", "Kozhikode", "Kerala", 11.2588, 75.7804, district = "Kozhikode", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Calicut"))
        val THRISSUR = City("thrissur", "Thrissur", "Kerala", 10.5276, 76.2144, district = "Thrissur", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Trichur", "Vadakkumnathan", "Thrissur Pooram"))
        val KOLLAM = City("kollam", "Kollam", "Kerala", 8.8932, 76.6141, district = "Kollam", panchangConvention = RegionalPanchangConvention.MALAYALAM_SOLAR, aliases = listOf("Quilon"))
        val NAGPUR = City("nagpur", "Nagpur", "Maharashtra", 21.1458, 79.0882, district = "Nagpur", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Orange City", "Zero Mile"))
        val SURAT = City("surat", "Surat", "Gujarat", 21.1702, 72.8311, district = "Surat", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Diamond City"))
        val VADODARA = City("vadodara", "Vadodara", "Gujarat", 22.3072, 73.1812, district = "Vadodara", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Baroda", "Sanskari Nagari"))
        val RAJKOT = City("rajkot", "Rajkot", "Gujarat", 22.3039, 70.8022, district = "Rajkot", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA)
        val INDORE = City("indore", "Indore", "Madhya Pradesh", 22.7196, 75.8577, district = "Indore", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Ahilya Nagari"))
        val GWALIOR = City("gwalior", "Gwalior", "Madhya Pradesh", 26.2183, 78.1828, district = "Gwalior", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val JABALPUR = City("jabalpur", "Jabalpur", "Madhya Pradesh", 23.1815, 79.9864, district = "Jabalpur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Bhedaghat"))
        val AGRA = City("agra", "Agra", "Uttar Pradesh", 27.1767, 78.0081, district = "Agra", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val KANPUR = City("kanpur", "Kanpur", "Uttar Pradesh", 26.4499, 80.3319, district = "Kanpur Nagar", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Cawnpore"))
        val MEERUT = City("meerut", "Meerut", "Uttar Pradesh", 28.9845, 77.7064, district = "Meerut", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val GORAKHPUR = City("gorakhpur", "Gorakhpur", "Uttar Pradesh", 26.7606, 83.3732, district = "Gorakhpur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Gorakhnath Mandir"))
        val BAREILLY = City("bareilly", "Bareilly", "Uttar Pradesh", 28.3670, 79.4304, district = "Bareilly", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val JODHPUR = City("jodhpur", "Jodhpur", "Rajasthan", 26.2389, 73.0243, district = "Jodhpur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Sun City", "Blue City"))
        val UDAIPUR = City("udaipur", "Udaipur", "Rajasthan", 24.5854, 73.7125, district = "Udaipur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("City of Lakes", "Mewar"))
        val KOTA = City("kota", "Kota", "Rajasthan", 25.2138, 75.8648, district = "Kota", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val BIKANER = City("bikaner", "Bikaner", "Rajasthan", 28.0229, 73.3119, district = "Bikaner", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val AJMER = City("ajmer", "Ajmer", "Rajasthan", 26.4499, 74.6399, district = "Ajmer", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val LUDHIANA = City("ludhiana", "Ludhiana", "Punjab", 30.9010, 75.8573, district = "Ludhiana", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val JALANDHAR = City("jalandhar", "Jalandhar", "Punjab", 31.3260, 75.5762, district = "Jalandhar", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val DEHRADUN = City("dehradun", "Dehradun", "Uttarakhand", 30.3165, 78.0322, district = "Dehradun", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Doon"))
        val SHIMLA = City("shimla", "Shimla", "Himachal Pradesh", 31.1048, 77.1734, district = "Shimla", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Simla"))
        val DHARAMSHALA = City("dharamshala", "Dharamshala", "Himachal Pradesh", 32.2190, 76.3234, district = "Kangra", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Kangra Valley"))
        val SRINAGAR = City("srinagar", "Srinagar", "Jammu and Kashmir", 34.0837, 74.7973, district = "Srinagar", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Shankaracharya Temple"))
        val JAMMU = City("jammu", "Jammu", "Jammu and Kashmir", 32.7266, 74.8570, district = "Jammu", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("City of Temples", "Vaishno Devi Base"))
        val KATRA = City("katra", "Katra", "Jammu and Kashmir", 32.9918, 74.9317, district = "Reasi", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Maa Vaishno Devi Bhavan"))
        val LEH = City("leh", "Leh", "Ladakh", 34.1526, 77.5771, district = "Leh", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val RAIPUR = City("raipur", "Raipur", "Chhattisgarh", 21.2514, 81.6296, district = "Raipur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val BILASPUR = City("bilaspur", "Bilaspur", "Chhattisgarh", 22.0797, 82.1409, district = "Bilaspur", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val JAMSHEDPUR = City("jamshedpur", "Jamshedpur", "Jharkhand", 22.8046, 86.2029, district = "East Singhbhum", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Tatanagar"))
        val DHANBAD = City("dhanbad", "Dhanbad", "Jharkhand", 23.7957, 86.4304, district = "Dhanbad", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val CUTTACK = City("cuttack", "Cuttack", "Odisha", 20.4625, 85.8828, district = "Cuttack", panchangConvention = RegionalPanchangConvention.ODIA_SOLAR, aliases = listOf("Silver City", "Barabati"))
        val ROURKELA = City("rourkela", "Rourkela", "Odisha", 22.2604, 84.8536, district = "Sundargarh", panchangConvention = RegionalPanchangConvention.ODIA_SOLAR)
        val PANAJI = City("panaji", "Panaji", "Goa", 15.4909, 73.8278, district = "North Goa", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Panjim", "Shanta Durga"))
        val MARGAO = City("margao", "Margao", "Goa", 15.2832, 73.9862, district = "South Goa", panchangConvention = RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA, aliases = listOf("Madgaon"))
        val SHILLONG = City("shillong", "Shillong", "Meghalaya", 25.5788, 91.8933, district = "East Khasi Hills", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR)
        val AGARTALA = City("agartala", "Agartala", "Tripura", 23.8315, 91.2868, district = "West Tripura", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR, aliases = listOf("Tripura Sundari"))
        val IMPHAL = City("imphal", "Imphal", "Manipur", 24.8170, 93.9368, district = "Imphal West", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR, aliases = listOf("Govindajee Temple"))
        val AIZAWL = City("aizawl", "Aizawl", "Mizoram", 23.7271, 92.7176, district = "Aizawl", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR)
        val KOHIMA = City("kohima", "Kohima", "Nagaland", 25.6751, 94.1086, district = "Kohima", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR)
        val ITANAGAR = City("itanagar", "Itanagar", "Arunachal Pradesh", 27.0844, 93.6053, district = "Papum Pare", panchangConvention = RegionalPanchangConvention.BENGALI_SOLAR)
        val GANGTOK = City("gangtok", "Gangtok", "Sikkim", 27.3389, 88.6065, district = "East Sikkim", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH)
        val PORT_BLAIR = City("port_blair", "Port Blair", "Andaman and Nicobar Islands", 11.6234, 92.7265, district = "South Andaman", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR)
        val KANYAKUMARI = City("kanyakumari", "Kanyakumari", "Tamil Nadu", 8.0883, 77.5385, district = "Kanyakumari", panchangConvention = RegionalPanchangConvention.TAMIL_SOLAR, aliases = listOf("Cape Comorin", "Bhagavathi Amman"))

        // ====================================================================
        // 4. MAJOR INTERNATIONAL DIASPORA CENTERS
        // ====================================================================
        val LONDON = City("london", "London", "Greater London", 51.5074, -0.1278, timeZoneId = "Europe/London", country = "United Kingdom", district = "London", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Neasden Temple", "UK"))
        val NEW_YORK = City("new_york", "New York City", "New York", 40.7128, -74.0060, timeZoneId = "America/New_York", country = "United States", district = "New York", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("NYC", "Flushing Ganesh Temple", "New Jersey"))
        val SAN_JOSE = City("san_jose", "San Jose / Bay Area", "California", 37.3382, -121.8863, timeZoneId = "America/Los_Angeles", country = "United States", district = "Santa Clara", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Silicon Valley", "San Francisco", "Sunnyvale", "Fremont"))
        val CHICAGO = City("chicago", "Chicago", "Illinois", 41.8781, -87.6298, timeZoneId = "America/Chicago", country = "United States", district = "Cook", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Aurora Temple", "Midwest"))
        val DALLAS = City("dallas", "Dallas-Fort Worth", "Texas", 32.7767, -96.7970, timeZoneId = "America/Chicago", country = "United States", district = "Dallas", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("DFW", "Frisco", "Plano"))
        val TORONTO = City("toronto", "Toronto", "Ontario", 43.6532, -79.3832, timeZoneId = "America/Toronto", country = "Canada", district = "Toronto", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("GTA", "BAPS Toronto", "Richmond Hill"))
        val VANCOUVER = City("vancouver", "Vancouver", "British Columbia", 49.2827, -123.1207, timeZoneId = "America/Vancouver", country = "Canada", district = "Metro Vancouver", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Surrey", "Richmond"))
        val DUBAI = City("dubai", "Dubai", "Dubai", 25.2048, 55.2708, timeZoneId = "Asia/Dubai", country = "United Arab Emirates", district = "Dubai", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("UAE", "Bur Dubai", "Jebel Ali Hindu Temple"))
        val ABU_DHABI = City("abu_dhabi", "Abu Dhabi", "Abu Dhabi", 24.4539, 54.3773, timeZoneId = "Asia/Dubai", country = "United Arab Emirates", district = "Abu Dhabi", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("BAPS Hindu Mandir Abu Dhabi"))
        val SINGAPORE = City("singapore", "Singapore", "Singapore", 1.3521, 103.8198, timeZoneId = "Asia/Singapore", country = "Singapore", district = "Central", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Sri Mariamman Temple"))
        val KUALA_LUMPUR = City("kuala_lumpur", "Kuala Lumpur", "Federal Territory", 3.1390, 101.6869, timeZoneId = "Asia/Kuala_Lumpur", country = "Malaysia", district = "Kuala Lumpur", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Batu Caves", "KL"))
        val SYDNEY = City("sydney", "Sydney", "New South Wales", -33.8688, 151.2093, timeZoneId = "Australia/Sydney", country = "Australia", district = "Sydney", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Helensburgh Temple", "Parramatta"))
        val MELBOURNE = City("melbourne", "Melbourne", "Victoria", -37.8136, 144.9631, timeZoneId = "Australia/Melbourne", country = "Australia", district = "Melbourne", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Shri Shiva Vishnu Temple"))
        val PORT_LOUIS = City("port_louis", "Port Louis", "Port Louis", -20.1609, 57.5012, timeZoneId = "Indian/Mauritius", country = "Mauritius", district = "Port Louis", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Ganga Talao", "Grand Bassin"))
        val DURBAN = City("durban", "Durban", "KwaZulu-Natal", -29.8587, 31.0218, timeZoneId = "Africa/Johannesburg", country = "South Africa", district = "eThekwini", panchangConvention = RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR, aliases = listOf("Temple of Understanding"))
        val KATHMANDU = City("kathmandu", "Kathmandu", "Bagmati", 27.7172, 85.3240, timeZoneId = "Asia/Kathmandu", country = "Nepal", district = "Kathmandu", panchangConvention = RegionalPanchangConvention.PURNIMANTA_NORTH, aliases = listOf("Pashupatinath"))

        val TOP_CITIES = listOf(
            BENGALURU, MUMBAI, DELHI, HYDERABAD, PUNE, CHENNAI,
            KOLKATA, AHMEDABAD, JAIPUR, LUCKNOW, KOCHI, BHOPAL
        )

        val PILGRIMAGE_CITIES = listOf(
            VARANASI, AYODHYA, UJJAIN, PRAYAGRAJ, HARIDWAR, RISHIKESH,
            MATHURA, VRINDAVAN, TIRUPATI, PURI, RAMESWARAM, MADURAI,
            DWARKA, SOMNATH, SHIRDI, BADRINATH, KEDARNATH, GAYA,
            KAMAKHYA, HAMPI, THANJAVUR, GURUVAYUR, PANDHARPUR,
            TRIMBAKESHWAR, OMKARESHWAR, KURUKSHETRA, PUSHKAR,
            CHITRAKOOT, PALITANA, GANGOTRI, YAMUNOTRI
        )

        val INTERNATIONAL_CITIES = listOf(
            LONDON, NEW_YORK, SAN_JOSE, CHICAGO, DALLAS, TORONTO,
            VANCOUVER, DUBAI, ABU_DHABI, SINGAPORE, KUALA_LUMPUR,
            SYDNEY, MELBOURNE, PORT_LOUIS, DURBAN, KATHMANDU
        )

        val ALL_CITIES: List<City> = listOf(
            // Top Metros
            BENGALURU, MUMBAI, DELHI, HYDERABAD, PUNE, CHENNAI,
            KOLKATA, AHMEDABAD, JAIPUR, LUCKNOW, KOCHI, BHOPAL,
            // Pilgrimage Kshetras
            VARANASI, AYODHYA, UJJAIN, PRAYAGRAJ, HARIDWAR, RISHIKESH,
            MATHURA, VRINDAVAN, TIRUPATI, PURI, RAMESWARAM, MADURAI,
            DWARKA, SOMNATH, SHIRDI, BADRINATH, KEDARNATH, GAYA,
            KAMAKHYA, HAMPI, THANJAVUR, GURUVAYUR, PANDHARPUR,
            TRIMBAKESHWAR, OMKARESHWAR, KURUKSHETRA, PUSHKAR,
            CHITRAKOOT, PALITANA, GANGOTRI, YAMUNOTRI,
            // State Capitals & District Hubs
            AMRITSAR, CHANDIGARH, PATNA, RANCHI, BHUBANESWAR, GUWAHATI,
            VISAKHAPATNAM, VIJAYAWADA, GUNTUR, WARANGAL, MYSURU, MANGALURU,
            HUBBALLI, BELAGAVI, COIMBATORE, TIRUCHIRAPPALLI, SALEM, TIRUNELVELI,
            THIRUVANANTHAPURAM, KOZHIKODE, THRISSUR, KOLLAM, NAGPUR, SURAT,
            VADODARA, RAJKOT, INDORE, GWALIOR, JABALPUR, AGRA, KANPUR,
            MEERUT, GORAKHPUR, BAREILLY, JODHPUR, UDAIPUR, KOTA, BIKANER,
            AJMER, LUDHIANA, JALANDHAR, DEHRADUN, SHIMLA, DHARAMSHALA,
            SRINAGAR, JAMMU, KATRA, LEH, RAIPUR, BILASPUR, JAMSHEDPUR,
            DHANBAD, CUTTACK, ROURKELA, PANAJI, MARGAO, SHILLONG, AGARTALA,
            IMPHAL, AIZAWL, KOHIMA, ITANAGAR, GANGTOK, PORT_BLAIR, KANYAKUMARI,
            // International Diaspora
            LONDON, NEW_YORK, SAN_JOSE, CHICAGO, DALLAS, TORONTO,
            VANCOUVER, DUBAI, ABU_DHABI, SINGAPORE, KUALA_LUMPUR,
            SYDNEY, MELBOURNE, PORT_LOUIS, DURBAN, KATHMANDU
        )

        val DEFAULT_CITY: City = DELHI

        /**
         * Looks up a curated city by its canonical ID.
         * Returns `null` when [id] is unknown rather than silently substituting another city,
         * preventing inaccurate Panchang and Muhurat calculations.
         */
        fun findById(id: String): City? = ALL_CITIES.firstOrNull { it.id == id }

        /**
         * Explicit lookup returning [CityLookupResult.Found] or [CityLookupResult.Unknown]
         * so callers must handle missing city IDs intentionally.
         */
        fun lookupById(id: String): CityLookupResult {
            val match = findById(id)
            return if (match != null) {
                CityLookupResult.Found(match)
            } else {
                CityLookupResult.Unknown(id)
            }
        }

        fun searchCities(query: String): List<City> {
            val trimmed = query.trim().lowercase()
            if (trimmed.isEmpty()) return ALL_CITIES
            return ALL_CITIES.filter { city ->
                city.name.lowercase().contains(trimmed) ||
                city.state.lowercase().contains(trimmed) ||
                city.district.lowercase().contains(trimmed) ||
                city.country.lowercase().contains(trimmed) ||
                city.aliases.any { it.lowercase().contains(trimmed) }
            }
        }

        fun findNearestCity(latitude: Double, longitude: Double): City {
            return ALL_CITIES.minByOrNull { city ->
                haversineDistanceKm(latitude, longitude, city.latitude, city.longitude)
            } ?: DEFAULT_CITY
        }

        fun createCustomGpsCity(
            latitude: Double,
            longitude: Double,
            name: String? = null,
            state: String? = null,
            district: String? = null,
            country: String = "India",
            timeZoneId: String? = null,
            convention: RegionalPanchangConvention? = null
        ): City {
            val nearest = findNearestCity(latitude, longitude)
            val resolvedName = name?.takeIf { it.isNotBlank() } ?: "GPS (${String.format("%.2f°N, %.2f°E", latitude, longitude)})"
            val resolvedState = state?.takeIf { it.isNotBlank() } ?: nearest.state
            val resolvedDistrict = district?.takeIf { it.isNotBlank() } ?: nearest.district
            val resolvedTz = timeZoneId ?: nearest.timeZoneId
            val resolvedConvention = convention ?: nearest.panchangConvention
            val customId = "gps_${abs(latitude).toString().replace(".", "_")}_${abs(longitude).toString().replace(".", "_")}"

            return City(
                id = customId,
                name = resolvedName,
                state = resolvedState,
                district = resolvedDistrict,
                country = country,
                latitude = latitude,
                longitude = longitude,
                timeZoneId = resolvedTz,
                isLocationDetected = true,
                panchangConvention = resolvedConvention
            )
        }

        fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }
}
