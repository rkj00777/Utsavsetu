package com.example.platform.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.example.panchang.City
import com.example.panchang.RegionalPanchangConvention
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

sealed class LocationCityResult {
    data class Success(
        val city: City,
        val distanceKmToKnownMetro: Double,
        val sourceLabel: String,
        val accuracyMeters: Float? = null,
        val isApproximatePermission: Boolean = false,
        val fixAgeSeconds: Long? = null,
        val isStaleFix: Boolean = false
    ) : LocationCityResult()

    data class Error(
        val message: String,
        val isPermissionDenied: Boolean = false,
        val isGpsDisabled: Boolean = false
    ) : LocationCityResult()
}

sealed class WorldwideSearchResult {
    data class Success(val cities: List<City>) : WorldwideSearchResult()
    data object Empty : WorldwideSearchResult()
    data class Unavailable(val reason: String) : WorldwideSearchResult()
}

sealed class CoordinateValidationResult {
    data class Valid(val latitude: Double, val longitude: Double) : CoordinateValidationResult()
    data class Invalid(val errorMessage: String) : CoordinateValidationResult()
}

object LocationCityResolver {

    const val MAX_FRESH_FIX_AGE_SECONDS = 600L // 10 minutes

    val COMMON_TIME_ZONES: List<String> = listOf(
        "Asia/Kolkata",
        "Asia/Kathmandu",
        "Asia/Colombo",
        "Asia/Dubai",
        "Asia/Singapore",
        "Asia/Kuala_Lumpur",
        "Asia/Tokyo",
        "Europe/London",
        "Europe/Berlin",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "America/Toronto",
        "America/Vancouver",
        "Australia/Sydney",
        "Australia/Melbourne",
        "Australia/Perth",
        "Pacific/Auckland",
        "Africa/Johannesburg",
        "Indian/Mauritius",
        "UTC"
    )

    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        return hasFineLocationPermission(context) || hasCoarseLocationPermission(context)
    }

    fun isApproximateOnlyPermission(context: Context): Boolean {
        return hasCoarseLocationPermission(context) && !hasFineLocationPermission(context)
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching { locationManager.isLocationEnabled }.getOrDefault(false)
        } else {
            runCatching {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.getOrDefault(false)
        }
    }

    /**
     * Computes the age in seconds of a [Location] fix using elapsedRealtimeNanos when available,
     * falling back to wall-clock [Location.getTime].
     */
    fun computeFixAgeSeconds(
        location: Location,
        nowElapsedNanos: Long = SystemClock.elapsedRealtimeNanos(),
        nowWallMillis: Long = System.currentTimeMillis()
    ): Long {
        val elapsedNanos = location.elapsedRealtimeNanos
        if (elapsedNanos > 0L && nowElapsedNanos >= elapsedNanos) {
            return (nowElapsedNanos - elapsedNanos) / 1_000_000_000L
        }
        val fixWallMillis = location.time
        if (fixWallMillis > 0L && nowWallMillis >= fixWallMillis) {
            return (nowWallMillis - fixWallMillis) / 1_000L
        }
        return 0L
    }

    fun isFixStale(fixAgeSeconds: Long): Boolean = fixAgeSeconds > MAX_FRESH_FIX_AGE_SECONDS

    fun validateManualCoordinates(latInput: String, lonInput: String): CoordinateValidationResult {
        val trimmedLat = latInput.trim()
        val trimmedLon = lonInput.trim()

        if (trimmedLat.isEmpty()) {
            return CoordinateValidationResult.Invalid("Latitude cannot be empty. Enter a value between -90 and +90.")
        }
        if (trimmedLon.isEmpty()) {
            return CoordinateValidationResult.Invalid("Longitude cannot be empty. Enter a value between -180 and +180.")
        }

        val lat = trimmedLat.toDoubleOrNull()
        if (lat == null || lat.isNaN() || lat.isInfinite() || lat < -90.0 || lat > 90.0) {
            return CoordinateValidationResult.Invalid("Please enter a valid Latitude between -90.0 and +90.0.")
        }

        val lon = trimmedLon.toDoubleOrNull()
        if (lon == null || lon.isNaN() || lon.isInfinite() || lon < -180.0 || lon > 180.0) {
            return CoordinateValidationResult.Invalid("Please enter a valid Longitude between -180.0 and +180.0.")
        }

        return CoordinateValidationResult.Valid(lat, lon)
    }

    @SuppressLint("MissingPermission")
    suspend fun detectUserCity(context: Context): LocationCityResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext LocationCityResult.Error(
                message = "Location permission is required to detect your current location. You can still search cities or enter coordinates manually.",
                isPermissionDenied = true
            )
        }

        val approximateOnly = isApproximateOnlyPermission(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        // Check if location services are turned off completely
        if (locationManager != null && !isLocationServiceEnabled(context)) {
            return@withContext LocationCityResult.Error(
                message = "Device Location Services (GPS) appear to be disabled. Turn on Location in device settings and tap Retry, or search a city below.",
                isGpsDisabled = true
            )
        }

        var bestLocation: Location? = null
        var bestSource = "GPS"

        // 1. Try FusedLocationProviderClient for a fresh fix first
        val fusedFresh = fetchFusedLocation(context, approximateOnly = approximateOnly)
        if (fusedFresh != null) {
            bestLocation = fusedFresh
            bestSource = if (approximateOnly) "Fused Network (Approximate)" else "Fused GPS"
        }

        // 2. Fallback to Android LocationManager (fresh or cached) if Fused failed or returned stale fix
        val currentAge = bestLocation?.let { computeFixAgeSeconds(it) }
        if (bestLocation == null || (currentAge != null && isFixStale(currentAge))) {
            val lmLocation = fetchLocationManagerFix(context, locationManager, approximateOnly)
            if (lmLocation != null) {
                val lmAge = computeFixAgeSeconds(lmLocation)
                if (bestLocation == null || lmAge < (currentAge ?: Long.MAX_VALUE)) {
                    bestLocation = lmLocation
                    bestSource = if (approximateOnly) "Network Location (Approximate)" else "Device GPS"
                }
            }
        }

        if (bestLocation != null) {
            val fixAgeSec = computeFixAgeSeconds(bestLocation)
            val stale = isFixStale(fixAgeSec)
            val accuracy = if (bestLocation.hasAccuracy()) bestLocation.accuracy else null
            return@withContext resolveCoordinatesToCity(
                context = context,
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                accuracyMeters = accuracy,
                isApproximatePermission = approximateOnly,
                fixAgeSeconds = fixAgeSec,
                isStaleFix = stale,
                providerSource = bestSource
            )
        }

        return@withContext LocationCityResult.Error(
            message = "Could not obtain a location fix right now. Ensure Location Services are enabled and tap Retry, or select a city manually below."
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFusedLocation(
        context: Context,
        approximateOnly: Boolean
    ): Location? {
        return runCatching {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val priority = if (approximateOnly) {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            } else {
                Priority.PRIORITY_HIGH_ACCURACY
            }

            // Try fresh current location first with 5-second timeout
            val fresh = withTimeoutOrNull(5_000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val tokenSource = CancellationTokenSource()
                    cont.invokeOnCancellation { tokenSource.cancel() }
                    fusedClient.getCurrentLocation(priority, tokenSource.token)
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }
            if (fresh != null) return@runCatching fresh

            // Fallback to Fused lastLocation if fresh timed out
            withTimeoutOrNull(2_000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocationManagerFix(
        context: Context,
        locationManager: LocationManager?,
        approximateOnly: Boolean
    ): Location? {
        if (locationManager == null) return null

        val providers = if (approximateOnly) {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        } else {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        }

        // Check cached lastKnownLocation first
        var bestCached: Location? = null
        var bestCachedAge = Long.MAX_VALUE
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val lastKnown = locationManager.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        val age = computeFixAgeSeconds(lastKnown)
                        if (bestCached == null || age < bestCachedAge) {
                            bestCached = lastKnown
                            bestCachedAge = age
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // If we have a fresh cached fix (<= 10 minutes), return it immediately
        if (bestCached != null && !isFixStale(bestCachedAge)) {
            return bestCached
        }

        // Otherwise request a fresh single update
        val activeProviders = if (approximateOnly) {
            listOf(LocationManager.NETWORK_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        }
        for (provider in activeProviders) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val fresh = withTimeoutOrNull(4_000L) {
                        requestSingleUpdate(context, locationManager, provider)
                    }
                    if (fresh != null) {
                        return fresh
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Return stale cached fix if nothing fresher arrived so caller can warn the user & offer Retry
        return bestCached
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(
        context: Context,
        locationManager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        cont.invokeOnCancellation { cancellationSignal.cancel() }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    if (cont.isActive) {
                        cont.resume(location)
                    }
                }
            } else {
                cont.resume(locationManager.getLastKnownLocation(provider))
            }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    /**
     * Reverse-geocodes coordinates into city, state/region, country, and resolved IANA time zone.
     */
    suspend fun resolveCoordinatesToCity(
        context: Context?,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float? = null,
        isApproximatePermission: Boolean = false,
        fixAgeSeconds: Long? = null,
        isStaleFix: Boolean = false,
        providerSource: String = "GPS"
    ): LocationCityResult = withContext(Dispatchers.IO) {
        val nearestKnown = City.findNearestCity(latitude, longitude)
        val distKm = City.haversineDistanceKm(latitude, longitude, nearestKnown.latitude, nearestKnown.longitude)

        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        val coordsLabel = String.format(Locale.US, "%.4f°%s, %.4f°%s", abs(latitude), latDir, abs(longitude), lonDir)

        // Best-effort reverse geocoding for exact locality, state, and country
        val geocodedAddress = if (context != null) {
            reverseGeocodeBestEffort(context, latitude, longitude)
        } else null

        val geocodedCity = geocodedAddress?.locality
            ?: geocodedAddress?.subAdminArea
            ?: geocodedAddress?.featureName
        val geocodedState = geocodedAddress?.adminArea
        val geocodedCountry = geocodedAddress?.countryName

        // If within 35 km of a curated city in ALL_CITIES, preserve its rich regional metadata & timezone
        // while keeping the exact detected coordinates and geocoded locality if different
        if (distKm <= 35.0) {
            val localizedCity = nearestKnown.copy(
                latitude = latitude,
                longitude = longitude,
                isLocationDetected = true
            )
            return@withContext LocationCityResult.Success(
                city = localizedCity,
                distanceKmToKnownMetro = distKm,
                sourceLabel = "$providerSource ($coordsLabel)",
                accuracyMeters = accuracyMeters,
                isApproximatePermission = isApproximatePermission,
                fixAgeSeconds = fixAgeSeconds,
                isStaleFix = isStaleFix
            )
        }

        val resolvedCountry = geocodedCountry?.takeIf { it.isNotBlank() }
            ?: if (distKm <= 350.0) nearestKnown.country else "Worldwide"
        val resolvedState = geocodedState?.takeIf { it.isNotBlank() }
            ?: if (distKm <= 350.0) nearestKnown.state else resolvedCountry
        val resolvedName = geocodedCity?.takeIf { it.isNotBlank() }
            ?: if (distKm <= 250.0) "${nearestKnown.name} Region" else "Location ($coordsLabel)"
        val resolvedTz = City.resolveTimeZoneForCoordinates(latitude, longitude, resolvedCountry)
        val resolvedConvention = if (resolvedCountry.equals("India", ignoreCase = true) || distKm <= 350.0) {
            nearestKnown.panchangConvention
        } else {
            RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR
        }

        val customCity = City.createCustomGpsCity(
            latitude = latitude,
            longitude = longitude,
            name = resolvedName,
            state = resolvedState,
            district = resolvedState,
            country = resolvedCountry,
            timeZoneId = resolvedTz,
            convention = resolvedConvention
        )

        return@withContext LocationCityResult.Success(
            city = customCity,
            distanceKmToKnownMetro = distKm,
            sourceLabel = "$providerSource ($coordsLabel)",
            accuracyMeters = accuracyMeters,
            isApproximatePermission = isApproximatePermission,
            fixAgeSeconds = fixAgeSeconds,
            isStaleFix = isStaleFix
        )
    }

    /**
     * Best-effort online worldwide city search using Android Geocoder.
     * Handles unavailable Geocoder services, network failures, timeouts, and empty results gracefully.
     */
    suspend fun searchWorldwideCitiesOnline(
        context: Context,
        query: String,
        maxResults: Int = 8
    ): WorldwideSearchResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 3) return@withContext WorldwideSearchResult.Empty

        if (!runCatching { Geocoder.isPresent() }.getOrDefault(false)) {
            return@withContext WorldwideSearchResult.Unavailable("Online geocoder service is unavailable on this device.")
        }

        val addresses = try {
            withTimeoutOrNull(4_000L) {
                forwardGeocodeBestEffort(context, trimmed, maxResults)
            }
        } catch (e: Exception) {
            return@withContext WorldwideSearchResult.Unavailable(
                "Online worldwide search unavailable right now (offline or network error). Offline city database remains available."
            )
        }

        if (addresses == null) {
            return@withContext WorldwideSearchResult.Unavailable(
                "Online worldwide search timed out or is unavailable offline. Offline city database remains available."
            )
        }

        val mapped = addresses.mapNotNull { addr ->
            if (!addr.hasLatitude() || !addr.hasLongitude()) return@mapNotNull null
            val lat = addr.latitude
            val lon = addr.longitude
            val name = addr.locality
                ?: addr.subAdminArea
                ?: addr.adminArea
                ?: addr.featureName
                ?: return@mapNotNull null
            val country = addr.countryName?.takeIf { it.isNotBlank() } ?: "Worldwide"
            val state = addr.adminArea?.takeIf { it.isNotBlank() } ?: country
            val tz = City.resolveTimeZoneForCoordinates(lat, lon, country)
            val slug = "${name}_${state}_${country}"
                .lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
            City(
                id = "world_$slug",
                name = name,
                state = state,
                district = addr.subAdminArea ?: state,
                country = country,
                latitude = lat,
                longitude = lon,
                timeZoneId = tz,
                isLocationDetected = false,
                panchangConvention = if (country.equals("India", ignoreCase = true)) {
                    City.findNearestCity(lat, lon).panchangConvention
                } else {
                    RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR
                }
            )
        }.distinctBy { "${it.name.lowercase(Locale.US)}_${it.state.lowercase(Locale.US)}_${it.country.lowercase(Locale.US)}" }

        if (mapped.isEmpty()) {
            WorldwideSearchResult.Empty
        } else {
            WorldwideSearchResult.Success(mapped)
        }
    }

    /**
     * Ranks offline curated cities cleanly by prefix match, city name, aliases, state, district, and country.
     */
    fun searchOfflineCitiesRanked(query: String, limit: Int = 25): List<City> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return emptyList()
        return City.ALL_CITIES
            .mapNotNull { city ->
                val nameLow = city.name.lowercase(Locale.ROOT)
                val stateLow = city.state.lowercase(Locale.ROOT)
                val districtLow = city.district.lowercase(Locale.ROOT)
                val countryLow = city.country.lowercase(Locale.ROOT)
                val aliasMatch = city.aliases.any { it.lowercase(Locale.ROOT).contains(q) }
                val score = when {
                    nameLow == q -> 0
                    nameLow.startsWith(q) -> 1
                    city.aliases.any { it.lowercase(Locale.ROOT).startsWith(q) } -> 2
                    nameLow.contains(q) -> 3
                    aliasMatch -> 4
                    districtLow.startsWith(q) || stateLow.startsWith(q) -> 5
                    stateLow.contains(q) || districtLow.contains(q) -> 6
                    countryLow.contains(q) -> 7
                    else -> null
                }
                score?.let { it to city }
            }
            .sortedWith(compareBy<Pair<Int, City>> { it.first }.thenBy { it.second.name })
            .take(limit)
            .map { it.second }
    }

    fun formatTimeZoneSummary(timeZoneId: String): String {
        return runCatching {
            val zone = ZoneId.of(timeZoneId)
            val offset = zone.rules.getOffset(java.time.Instant.now())
            val offsetText = if (offset.totalSeconds == 0) "UTC" else "UTC$offset"
            "$timeZoneId ($offsetText)"
        }.getOrElse { timeZoneId }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocodeBestEffort(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Address? {
        if (!runCatching { Geocoder.isPresent() }.getOrDefault(false)) return null
        return runCatching {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                withTimeoutOrNull(3_500L) {
                    suspendCancellableCoroutine<Address?> { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) cont.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        })
                    }
                }
            } else {
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private suspend fun forwardGeocodeBestEffort(
        context: Context,
        query: String,
        maxResults: Int
    ): List<Address>? {
        val geocoder = Geocoder(context, Locale.ENGLISH)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(null)
                    }
                })
            }
        } else {
            geocoder.getFromLocationName(query, maxResults)
        }
    }
}
