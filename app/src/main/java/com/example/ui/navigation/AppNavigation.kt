package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.AppViewModelFactory
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.UtsavAnimatedSplashScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.calendar.CalendarViewModel
import com.example.ui.screens.detail.DetailScreen
import com.example.ui.screens.detail.DetailViewModel
import com.example.ui.screens.festivals.FestivalsScreen
import com.example.ui.screens.festivals.FestivalsViewModel
import com.example.ui.screens.panchang.PanchangScreen
import com.example.ui.screens.panchang.PanchangViewModel
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.setup.SetupScreen
import com.example.ui.screens.setup.SetupViewModel
import com.example.ui.screens.today.TodayScreen
import com.example.ui.screens.today.TodayViewModel
import com.example.ui.theme.LocalLiquidGlassTokens

/**
 * Blueprint Section 2: Five Primary Destinations:
 * 1. Today — daily festival and Panchang summary.
 * 2. Calendar — monthly and upcoming festival calendar.
 * 3. Festivals — search and discover festivals.
 * 4. Panchang — detailed daily timings and astronomical calendar information.
 * 5. Profile — city, language, tradition, notifications, and preferences.
 */
enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TODAY("Today", Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "nav_tab_today"),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_tab_calendar"),
    FESTIVALS("Festivals", Icons.Filled.Celebration, Icons.Outlined.Celebration, "nav_tab_festivals"),
    PANCHANG("Panchang", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_tab_panchang"),
    PROFILE("Profile", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_profile")
}

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    hasCompletedSetup: Boolean,
    initialFestivalId: String? = null,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (hasCompletedSetup) "main" else "setup"
    var showSplash by rememberSaveable { mutableStateOf(initialFestivalId.isNullOrBlank()) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("setup") {
                val setupViewModel: SetupViewModel = viewModel(
                    factory = AppViewModelFactory(appContainer)
                )
                SetupScreen(
                    viewModel = setupViewModel,
                    onSetupComplete = {
                        navController.navigate("main") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                )
            }

            composable("main") {
                MainContainer(
                    appContainer = appContainer,
                    onNavigateToFestival = { id ->
                        navController.navigate("detail/$id")
                    },
                    onDataReset = {
                        navController.navigate("setup") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "detail/{festivalId}",
                arguments = listOf(navArgument("festivalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val festivalId = backStackEntry.arguments?.getString("festivalId") ?: ""
                val detailViewModel: DetailViewModel = viewModel(
                    factory = AppViewModelFactory(appContainer, festivalId = festivalId),
                    key = "detail_$festivalId"
                )
                DetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showSplash) {
            UtsavAnimatedSplashScreen(
                onSplashComplete = { showSplash = false }
            )
        }
    }

    LaunchedEffect(hasCompletedSetup) {
        if (hasCompletedSetup && navController.currentDestination?.route == "setup") {
            navController.navigate("main") {
                popUpTo("setup") { inclusive = true }
            }
        }
    }

    LaunchedEffect(initialFestivalId, hasCompletedSetup) {
        if (!initialFestivalId.isNullOrBlank() && hasCompletedSetup) {
            showSplash = false
            navController.navigate("detail/$initialFestivalId")
        }
    }
}

@Composable
fun MainContainer(
    appContainer: AppContainer,
    onNavigateToFestival: (String) -> Unit,
    onDataReset: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TODAY) }

    val todayViewModel: TodayViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val festivalsViewModel: FestivalsViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val panchangViewModel: PanchangViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelFactory(appContainer))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Responsive centered screen content container for mobile, foldable, and tablet sizes
        Box(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                label = "tab_crossfade"
            ) { tab ->
                when (tab) {
                    MainTab.TODAY -> TodayScreen(
                        viewModel = todayViewModel,
                        onNavigateToFestival = onNavigateToFestival,
                        onOpenSearch = { selectedTab = MainTab.FESTIVALS },
                        onOpenCalendar = { selectedTab = MainTab.CALENDAR },
                        onOpenPanchang = { selectedTab = MainTab.PANCHANG }
                    )
                    MainTab.CALENDAR -> CalendarScreen(
                        viewModel = calendarViewModel,
                        onNavigateToFestival = onNavigateToFestival,
                        onOpenSearch = { selectedTab = MainTab.FESTIVALS }
                    )
                    MainTab.FESTIVALS -> FestivalsScreen(
                        viewModel = festivalsViewModel,
                        onNavigateToFestival = onNavigateToFestival
                    )
                    MainTab.PANCHANG -> PanchangScreen(
                        viewModel = panchangViewModel
                    )
                    MainTab.PROFILE -> ProfileScreen(
                        viewModel = profileViewModel,
                        onDataDeleted = onDataReset
                    )
                }
            }
        }

        // Floating Bottom Navigation Bar (Liquid Glass Container)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .widthIn(max = 560.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingLiquidGlassBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_navigation_bar")
            )
        }
    }
}

@Composable
private fun FloatingLiquidGlassBottomBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = com.example.ui.theme.LocalUtsavStrings.current

    LiquidGlassSurface(
        cornerRadius = 32.dp,
        strongFill = true,
        elevation = 10.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompactWidth = maxWidth < 360.dp
            val labelFontSize = if (isCompactWidth) 9.5.sp else 11.sp
            val iconSize = if (isCompactWidth) 18.dp else 20.dp
            val horizontalPadding = if (isCompactWidth) 2.dp else 4.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val localizedTitle = when (tab) {
                        MainTab.TODAY -> strings.tabToday
                        MainTab.CALENDAR -> strings.tabCalendar
                        MainTab.FESTIVALS -> strings.tabFestivals
                        MainTab.PANCHANG -> strings.tabPanchang
                        MainTab.PROFILE -> strings.tabProfile
                    }
                    val pillColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_pill_color"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else glassTokens.onGlassSecondary,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_content_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectTab(tab) }
                            )
                            .padding(vertical = 5.dp, horizontal = horizontalPadding)
                            .testTag(tab.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = contentColor,
                                modifier = Modifier.size(iconSize)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = localizedTitle,
                                fontSize = labelFontSize,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
