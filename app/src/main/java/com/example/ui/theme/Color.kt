package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// UtsavSetu Official Brand Palette (Approved Master Identity)
// ============================================================================
val UtsavDeepBurgundy = Color(0xFF520019)       // #520019 - Master Brand Backdrop & Splash
val UtsavDarkMaroon = Color(0xFF350014)         // #350014 - Deep Shadow & Dark Mode Surfaces
val UtsavRoyalPlum = Color(0xFF4B123F)          // #4B123F - Selected Accent & Elevation
val UtsavMetallicGold = Color(0xFFD9A441)       // #D9A441 - Sacred Arches, Borders, Diya & Emblems
val UtsavLightGold = Color(0xFFF8D982)          // #F8D982 - Luminous Shimmer, Flame Core & Highlights
val UtsavSaffron = Color(0xFFF28C28)            // #F28C28 - Sacred Temple Flags & Primary Actions
val UtsavIvory = Color(0xFFFFF5E6)              // #FFF5E6 - Crisp Clean Warm Surfaces & Typography
val UtsavVermilion = Color(0xFFC62828)          // #C62828 - Auspicious Tilak & Festival Alerts

// ============================================================================
// Semantic & Theme Palette (Light Mode)
// Warm traditional Indian sacred cream backdrop with crisp pure white cards
// ============================================================================
val SacredIvory = Color(0xFFF7F2E7)             // Authentic warm soothing sacred cream canvas (not harsh white)
val WarmSand = Color(0xFFEFE6D6)                // Warm Sand secondary surface
val SaffronAccent = UtsavSaffron                // #F28C28 - Sacred saffron primary actions & navigation
val VermilionAccent = UtsavVermilion            // #C62828 - Festival accents and important highlights
val AntiqueGold = UtsavMetallicGold             // #D9A441 - Borders, icons, decorative brand details
val DeepPlum = UtsavRoyalPlum                   // #4B123F - Selected accent contrast
val ForestGreen = Color(0xFF2E6930)             // Auspicious or completed states
val CharcoalPrimary = Color(0xFF171512)         // Deep dark charcoal for supreme text contrast (16:1 WCAG AAA)
val MutedBrownSecondary = Color(0xFF4C4338)     // Rich warm dark brown secondary text (WCAG AAA)
val PureWhite = Color(0xFFFFFFFF)               // Solid clean white cards for standout contrast on cream
val BorderOutline = Color(0xFFD6C3AA)           // Distinct, visible warm sandalwood card border

// Compatibility aliases used across existing components
val KumkumVermilion = VermilionAccent
val DeepVermilion = VermilionAccent
val MarigoldSaffron = SaffronAccent
val TulsiGreen = ForestGreen
val WarmIvoryBackground = SacredIvory
val SandalwoodSurface = PureWhite               // Solid high-contrast white cards on Sacred Ivory
val AlmondCardVariant = WarmSand                // Secondary warm sand surface
val CharcoalText = CharcoalPrimary
val SubtitleText = MutedBrownSecondary
val MutedGoldAccent = AntiqueGold

// Light Liquid Glass Tokens (High-opacity frosted surfaces for crisp text over artwork)
val LightGlassSurfaceTop = Color(0xF8FFFFFF)      // 97% opaque crisp white top
val LightGlassSurfaceBottom = Color(0xF0FAF7F2)   // 94% sacred ivory glass bottom
val LightGlassBorderHighlight = Color(0xFFFFFFFF) // Thin white top-left specular border
val LightGlassBorderSubtle = Color(0x66D9A441)    // Thin Metallic Gold border (#D9A441)

// ============================================================================
// Dark Mode: Deep Meditative Incense Charcoal Night with Warm Gold & Saffron Accents
// (Restores clean, meditative, modern dark UI — not flooded with dark maroon/burgundy)
// ============================================================================
val DiyaAmber = UtsavLightGold                  // Luminous Light Gold primary accent (#F8D982)
val SindoorCoral = Color(0xFFE57662)            // Restrained Vermilion accent
val EmeraldLeaf = Color(0xFF68B680)             // High-contrast Auspicious Green for dark mode
val SubtleAmberAccent = UtsavMetallicGold       // Luminous Metallic Gold for dark mode

val IncenseNightBackground = Color(0xFF141216)  // Deep mystical incense charcoal night
val WarmCharcoalSurface = Color(0xFF211D24)     // Clean dark charcoal card surface
val ElevatedDarkVariant = Color(0xFF2B2530)     // Elevated charcoal surface
val BrightEveningText = Color(0xFFFAF6F0)        // Crisp ivory primary text
val MutedEveningText = Color(0xFFC8BFC9)         // High-contrast readable secondary text
val DarkBorderOutline = Color(0x33D9A441)       // Subtle metallic gold border in dark mode

// Dark Liquid Glass Tokens
val DarkGlassSurfaceTop = Color(0xF22A2432)       // 95% opaque deep incense charcoal
val DarkGlassSurfaceBottom = Color(0xEB191520)    // 92% opaque incense night
val DarkGlassBorderHighlight = Color(0x66D9A441)  // Thin metallic gold border
val DarkGlassBorderSubtle = Color(0x26FFFFFF)     // Subtle inner rim

// Shared Accents
val GoldAccent = UtsavLightGold
val DeepGold = UtsavMetallicGold
