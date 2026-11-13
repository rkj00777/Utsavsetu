package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalLiquidGlassTokens

/**
 * Central Photographic Drawable Registry for UtsavSetu "Living India" UI.
 * Maps all 71 canonical festival & welcome IDs to locally bundled WebP photographs in res/drawable-nodpi/.
 */
object FestivalPhotographicRegistry {

    private data class PhotoMapping(
        @DrawableRes val resId: Int,
        val resName: String,
        val description: String
    )

    private val MAPPINGS: Map<String, PhotoMapping> = mapOf(
        "welcome_utsavsetu" to PhotoMapping(R.drawable.img_welcome_hero, "img_welcome_hero", "Evening Ganga Aarti illumination at Dashashwamedh Ghat, Varanasi"),
        "ganesh_chaturthi" to PhotoMapping(R.drawable.img_ganesh_chaturthi, "img_ganesh_chaturthi", "Ganesh Chaturthi pandal and murti celebration"),
        "diwali_lakshmi_puja" to PhotoMapping(R.drawable.img_diwali, "img_diwali", "Diwali clay diyas and festive rangoli of lights"),
        "karwa_chauth" to PhotoMapping(R.drawable.img_karwa_chauth, "img_karwa_chauth", "Karwa Chauth moonrise ritual and traditional sieve"),
        "makar_sankranti" to PhotoMapping(R.drawable.img_makar_sankranti, "img_makar_sankranti", "Makar Sankranti solar harvest rituals and devotees"),
        "gudi_padwa_ugadi" to PhotoMapping(R.drawable.img_gudi_padwa, "img_gudi_padwa", "Traditional Gudi Padwa and Ugadi New Year procession"),
        "durga_ashtami" to PhotoMapping(R.drawable.img_durga_ashtami, "img_durga_ashtami", "Durga Puja Maha Ashtami pandal and Goddess Durga idol"),
        "navratri_ghatasthapana" to PhotoMapping(R.drawable.img_navratri_durga, "img_navratri_durga", "Sharad Navratri Goddess Durga worship and Ghatasthapana"),
        "maha_shivaratri" to PhotoMapping(R.drawable.img_maha_shivratri, "img_maha_shivratri", "Maha Shivaratri night vigil and Shiva temple worship"),
        "krishna_janmashtami" to PhotoMapping(R.drawable.img_janmashtami, "img_janmashtami", "Krishna Janmashtami celebration and sacred shrine"),
        "varalakshmi_vratam" to PhotoMapping(R.drawable.img_varalakshmi, "img_varalakshmi", "Varalakshmi Vratam decorated kalash and sacred offerings"),
        "holi_dhulandi" to PhotoMapping(R.drawable.img_holi, "img_holi", "Holi festival of colors celebration in Braj"),
        "raksha_bandhan" to PhotoMapping(R.drawable.img_raksha_bandhan, "img_raksha_bandhan", "Raksha Bandhan ceremonial rakhi threads and lamps"),
        "rama_navami" to PhotoMapping(R.drawable.img_rama_navami, "img_rama_navami", "Sri Rama Navami shrine and festive celebration"),
        "vijayadashami_dussehra" to PhotoMapping(R.drawable.img_dussehra, "img_dussehra", "Vijayadashami and Dussehra cultural celebrations"),
        "chhath_puja" to PhotoMapping(R.drawable.img_chhath_puja, "img_chhath_puja", "Chhath Puja Surya Arghya offerings at river ghat"),
        "akshaya_tritiya" to PhotoMapping(R.drawable.img_akshaya_tritiya, "img_akshaya_tritiya", "Sacred Badrinath shrine open on Akshaya Tritiya"),
        "bhai_dooj" to PhotoMapping(R.drawable.img_bhai_dooj, "img_bhai_dooj", "Bhai Dooj and Bhai Phonta ceremonial tilak ritual"),
        "dhanteras" to PhotoMapping(R.drawable.img_dhanteras, "img_dhanteras", "Dhanteras and Lord Dhanvantari auspicious worship"),
        "govardhan_puja" to PhotoMapping(R.drawable.img_govardhan, "img_govardhan", "Govardhan Puja and Annakut Shrinathji devotion"),
        "vasant_panchami" to PhotoMapping(R.drawable.img_vasant_panchami, "img_vasant_panchami", "Vasant Panchami Goddess Saraswati idol and worship"),
        "hanuman_jayanti" to PhotoMapping(R.drawable.img_hanuman_jayanti, "img_hanuman_jayanti", "Hanuman Jayanti sacred temple murti"),
        "guru_purnima" to PhotoMapping(R.drawable.img_guru_purnima, "img_guru_purnima", "Guru Purnima and Maharshi Vyasa homage"),
        "nag_panchami" to PhotoMapping(R.drawable.img_nag_panchami, "img_nag_panchami", "Nag Panchami sacred serpent shrine worship"),
        "onam_thiruvonam" to PhotoMapping(R.drawable.img_onam, "img_onam", "Onam Thiruvonam intricate floral Pookkalam carpet"),
        "mesha_sankranti_new_year" to PhotoMapping(R.drawable.img_mesha_sankranti, "img_mesha_sankranti", "Vishu Kani and Solar New Year auspicious arrangement"),
        "ratha_yatra" to PhotoMapping(R.drawable.img_ratha_yatra, "img_ratha_yatra", "Jagannath Ratha Yatra grand chariot procession in Puri"),
        "kartik_purnima" to PhotoMapping(R.drawable.img_kartik_purnima, "img_kartik_purnima", "Kartik Purnima and Dev Deepawali illuminated ghats of Varanasi"),
        "vat_savitri_vrat" to PhotoMapping(R.drawable.img_vat_savitri, "img_vat_savitri", "Vat Savitri Vrat sacred banyan tree circumambulation"),
        "tulsi_vivah_dev_uthani_ekadashi" to PhotoMapping(R.drawable.img_tulsi_vivah, "img_tulsi_vivah", "Tulsi Vivah and Dev Uthani Ekadashi sacred vrindavan worship"),
        "sharad_purnima_kojagiri" to PhotoMapping(R.drawable.img_sharad_purnima, "img_sharad_purnima", "Sharad Purnima and Kojagiri Lakshmi Puja"),
        "naraka_chaturdashi_choti_diwali" to PhotoMapping(R.drawable.img_naraka_chaturdashi, "img_naraka_chaturdashi", "Naraka Chaturdashi and Choti Diwali observance"),
        "ahoi_ashtami" to PhotoMapping(R.drawable.img_ahoi_ashtami, "img_ahoi_ashtami", "Ahoi Ashtami Vrat traditional ritual worship"),
        "skanda_sashti_soorasamharam" to PhotoMapping(R.drawable.img_skanda_sashti, "img_skanda_sashti", "Skanda Sashti Lord Murugan sacred statue and temple"),
        "hariyali_teej" to PhotoMapping(R.drawable.img_hariyali_teej, "img_hariyali_teej", "Hariyali Teej monsoon festival celebration"),
        "anant_chaturdashi" to PhotoMapping(R.drawable.img_anant_chaturdashi, "img_anant_chaturdashi", "Anant Chaturdashi and Ganesh Visarjan procession"),
        "nirjala_ekadashi" to PhotoMapping(R.drawable.img_nirjala_ekadashi, "img_nirjala_ekadashi", "Nirjala Ekadashi sacred Vishnu temple Complex at Tirumala"),
        "ashadhi_ekadashi_devshayani" to PhotoMapping(R.drawable.img_ashadhi_ekadashi, "img_ashadhi_ekadashi", "Ashadhi Ekadashi Pandharpur Wari Dindi pilgrimage"),
        "ganga_dussehra" to PhotoMapping(R.drawable.img_ganga_dussehra, "img_ganga_dussehra", "Ganga Dussehra evening gathering at Har Ki Pauri, Haridwar"),
        "sheetala_ashtami_basoda" to PhotoMapping(R.drawable.img_sheetala_ashtami, "img_sheetala_ashtami", "Sheetala Ashtami sacred stepped temple tank"),
        "ratha_saptami" to PhotoMapping(R.drawable.img_ratha_saptami, "img_ratha_saptami", "Ratha Saptami Surya Jayanti solar chariot sculpture"),
        "vaikuntha_ekadashi" to PhotoMapping(R.drawable.img_vaikuntha_ekadashi, "img_vaikuntha_ekadashi", "Vaikuntha Ekadashi Vaikuntha Dwara temple celebration"),
        "lohri_bhogi" to PhotoMapping(R.drawable.img_lohri_bhogi, "img_lohri_bhogi", "Lohri and Bhogi sacred winter harvest bonfire"),
        "vishwakarma_puja" to PhotoMapping(R.drawable.img_vishwakarma, "img_vishwakarma", "Vishwakarma Puja divine architect shrine and offerings"),
        "buddha_purnima" to PhotoMapping(R.drawable.img_buddha_purnima, "img_buddha_purnima", "Buddha Purnima Vesak illuminated lotus lanterns"),
        "mahavir_jayanti" to PhotoMapping(R.drawable.img_mahavir_jayanti, "img_mahavir_jayanti", "Mahavir Jayanti ancient Jain Tirthankara sculpture"),
        "guru_nanak_jayanti" to PhotoMapping(R.drawable.img_guru_nanak, "img_guru_nanak", "Guru Nanak Gurpurab illuminated Harmandir Sahib complex"),
        "thaipusam" to PhotoMapping(R.drawable.img_thaipusam, "img_thaipusam", "Thaipusam ceremonial Kavadi procession"),
        "radha_ashtami" to PhotoMapping(R.drawable.img_radha_ashtami, "img_radha_ashtami", "Radha Ashtami Vrindavan deity adornment"),
        "sita_navami" to PhotoMapping(R.drawable.img_sita_navami, "img_sita_navami", "Sita Navami Janaki Mandir shrine at Sitamarhi"),
        "narasimha_jayanti" to PhotoMapping(R.drawable.img_narasimha_jayanti, "img_narasimha_jayanti", "Narasimha Jayanti monolithic Ugra Narasimha sculpture at Hampi"),
        "shani_jayanti" to PhotoMapping(R.drawable.img_shani_jayanti, "img_shani_jayanti", "Shani Jayanti Lord Shanishwara statue"),
        "dattatreya_jayanti" to PhotoMapping(R.drawable.img_dattatreya_jayanti, "img_dattatreya_jayanti", "Dattatreya Jayanti historic Dattatreya temple"),
        "vivah_panchami" to PhotoMapping(R.drawable.img_vivah_panchami, "img_vivah_panchami", "Vivah Panchami illuminated Janaki Mandir in Janakpur"),
        "mahalaya_amavasya" to PhotoMapping(R.drawable.img_mahalaya, "img_mahalaya", "Mahalaya Amavasya Pitru Paksha tarpan ritual on the Ganges"),
        "nuakhai_rishi_panchami" to PhotoMapping(R.drawable.img_nuakhai, "img_nuakhai", "Nuakhai harvest festival cultural dance performance in Odisha"),
        "bathukamma" to PhotoMapping(R.drawable.img_bathukamma, "img_bathukamma", "Bathukamma stacked floral arrangement in Telangana"),
        "bonalu" to PhotoMapping(R.drawable.img_bonalu, "img_bonalu", "Bonalu decorated sacred Bonam pots in Telangana"),
        "raja_parba" to PhotoMapping(R.drawable.img_raja_parba, "img_raja_parba", "Raja Parba traditional swing celebration in Odisha"),
        "gangaur" to PhotoMapping(R.drawable.img_gangaur, "img_gangaur", "Gangaur traditional folk dance and procession"),
        "jitiya_jivitputrika" to PhotoMapping(R.drawable.img_jitiya, "img_jitiya", "Jitiya Jivitputrika Vrat river ghat ritual"),
        "sankashti_chaturthi_sakat" to PhotoMapping(R.drawable.img_sankashti_chaturthi, "img_sankashti_chaturthi", "Sankashti Chaturthi sacred Ganesha shrine"),
        "vaikunta_ekadashi" to PhotoMapping(R.drawable.img_vaikuntha_ekadashi, "img_vaikuntha_ekadashi", "Vaikunta Ekadashi Mukkoti Ekadashi temple celebration"),
        "karthigai_deepam" to PhotoMapping(R.drawable.img_karthigai_deepam, "img_karthigai_deepam", "Karthigai Deepam traditional agal vilakku oil lamps on kolam"),
        "ambubachi_mela" to PhotoMapping(R.drawable.img_ambubachi_mela, "img_ambubachi_mela", "Ambubachi Mela at historic Kamakhya Temple in Guwahati"),
        "masi_magham_attukal_pongala" to PhotoMapping(R.drawable.img_masi_magham, "img_masi_magham", "Attukal Pongala and Masi Magham sacred hearth offerings"),
        "sarhul_karam_parab" to PhotoMapping(R.drawable.img_sarhul_karam, "img_sarhul_karam", "Sarhul and Karam Parab nature worship festival"),
        "paryushana_samvatsari" to PhotoMapping(R.drawable.img_paryushana, "img_paryushana", "Paryushana Parva sacred Palitana Jain temple complex"),
        "hola_mohalla" to PhotoMapping(R.drawable.img_hola_mohalla, "img_hola_mohalla", "Hola Mohalla Nihang martial procession at Anandpur Sahib"),
        "losar_hemis" to PhotoMapping(R.drawable.img_losar_hemis, "img_losar_hemis", "Losar and Hemis Festival masked dance at Hemis Monastery"),
        "hornbill_northeast_utsav" to PhotoMapping(R.drawable.img_hornbill_northeast, "img_hornbill_northeast", "Hornbill Festival cultural celebration in Nagaland"),
        "hartalika_teej" to PhotoMapping(R.drawable.img_hartalika_teej, "img_hartalika_teej", "Hartalika Teej and Swarna Gowri Vrat Shiva-Parvati worship"),
        "kajari_teej" to PhotoMapping(R.drawable.img_kajari_teej, "img_kajari_teej", "Kajari Teej and Badi Teej traditional celebration"),
        "maha_navami_ayudha_puja" to PhotoMapping(R.drawable.img_maha_navami_ayudha, "img_maha_navami_ayudha", "Maha Navami and Ayudha Puja Goddess Durga and Saraswati worship"),
        "labh_panchami" to PhotoMapping(R.drawable.img_labh_panchami, "img_labh_panchami", "Labh Panchami and Gnan Panchami auspicious Shubh-Labh worship"),
        "gopashtami" to PhotoMapping(R.drawable.img_gopashtami, "img_gopashtami", "Gopashtami sacred Gau-Mata and Govinda worship in Braj"),
        "akshaya_navami_amla_navami" to PhotoMapping(R.drawable.img_akshaya_navami, "img_akshaya_navami", "Akshaya Navami Amla Vriksha and Jagaddhatri Puja"),
        "kalabhairava_jayanti" to PhotoMapping(R.drawable.img_kalabhairava_jayanti, "img_kalabhairava_jayanti", "Kaal Bhairav Jayanti Kashi Kotwal sacred shrine worship"),
        "mauni_amavasya" to PhotoMapping(R.drawable.img_mauni_amavasya, "img_mauni_amavasya", "Mauni Amavasya sacred river snan and Pitru Tarpan"),
        "amalaki_ekadashi_rangbhari" to PhotoMapping(R.drawable.img_amalaki_ekadashi, "img_amalaki_ekadashi", "Amalaki Ekadashi and Kashi Rangbhari Ekadashi celebration"),
        "cheti_chand_jhulelal" to PhotoMapping(R.drawable.img_cheti_chand, "img_cheti_chand", "Cheti Chand Jhulelal Jayanti Bahrana Sahib Jal-Jyot worship"),
        "putrada_ekadashi" to PhotoMapping(R.drawable.img_putrada_ekadashi, "img_putrada_ekadashi", "Shravana Putrada Ekadashi and Pavitropana Vishnu worship"),
        "hariyali_amavasya" to PhotoMapping(R.drawable.img_hariyali_amavasya, "img_hariyali_amavasya", "Hariyali Amavasya and Deep Puja sacred lamp illumination")
    )

    @DrawableRes
    fun getDrawableForFestival(festivalId: String?): Int {
        if (festivalId == null) return R.drawable.img_cultural_fallback
        return MAPPINGS[festivalId]?.resId ?: R.drawable.img_cultural_fallback
    }

    fun getDrawableResName(festivalId: String?): String {
        if (festivalId == null) return "img_cultural_fallback"
        return MAPPINGS[festivalId]?.resName ?: "img_cultural_fallback"
    }

    fun isFallback(festivalId: String?): Boolean {
        return festivalId == null || !MAPPINGS.containsKey(festivalId)
    }

    fun getAllMappedFestivalIds(): Set<String> = MAPPINGS.keys

    fun getAccessibilityDescription(festivalId: String?): String {
        if (festivalId == null) return "Traditional Indian Aarti lamp and floral offerings"
        return MAPPINGS[festivalId]?.description ?: "Traditional Indian Aarti lamp and floral offerings"
    }
}

/**
 * Selective Liquid Glass surface used exclusively for:
 * - Today screen's Tithi Hero card
 * - Floating bottom navigation
 * - Festival countdown overlays
 * - Quick-action panels
 * - Small floating controls over festival artwork
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    strongFill: Boolean = false,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_press_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (glassTokens.isDark) 0.45f else 0.12f),
                spotColor = glassTokens.decorativeGold.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(brush = if (strongFill) glassTokens.glassStrongFillBrush else glassTokens.glassFillBrush)
            .border(width = 1.dp, brush = glassTokens.glassBorderBrush, shape = shape)
            .then(clickableModifier),
        content = content
    )
}

/**
 * Photographic Indian Luxury Festival Artwork Banner.
 * Displays locally bundled WebP photographs mapped to canonical festival IDs with cinematic scrim overlays.
 */
@Composable
fun FestivalArtworkBanner(
    festivalId: String?,
    modifier: Modifier = Modifier,
    isHeroBackground: Boolean = false,
    contentDescription: String? = FestivalPhotographicRegistry.getAccessibilityDescription(festivalId)
) {
    val drawableRes = remember(festivalId) {
        FestivalPhotographicRegistry.getDrawableForFestival(festivalId)
    }

    Box(
        modifier = modifier.testTag("festival_artwork_${festivalId ?: "fallback"}")
    ) {
        // 1. Locally bundled WebP photographic asset background
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Cinematic luxury gradient scrim overlays for high-contrast text legibility
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dark vignette top-to-bottom
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = if (isHeroBackground) 0.65f else 0.50f)
                    )
                )
            )

            // Warm golden bottom border highlight
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFFFFD700).copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, size.height - 1.dp.toPx()),
                end = Offset(size.width, size.height - 1.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
