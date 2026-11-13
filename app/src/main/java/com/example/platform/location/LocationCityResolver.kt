package com.example.platform.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.example.panchang.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

sealed class LocationCityResult {
    data class Success(
        val city: City,
        val distanceKmToKnownMetro: Double,
        val sourceLabel: String
    ) : LocationCityResult()

    data class Error(val message: String) : LocationCityResult()
}

object LocationCityResolver {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun detectUserCity(context: Context): LocationCityResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext LocationCityResult.Error("Location permission is required to detect your city automatically.")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext LocationCityResult.Error("Location service is unavailable on this device.")

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val lastKnown = locationManager.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        if (bestLocation == null || lastKnown.time > bestLocation.time) {
                            bestLocation = lastKnown
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // If no cached location, request a fresh fix with a 4-second timeout
        if (bestLocation == null) {
            for (provider in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val fresh = withTimeoutOrNull(4000L) {
                            requestSingleUpdate(context, locationManager, provider)
                        }
                        if (fresh != null) {
                            bestLocation = fresh
                            break
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (bestLocation != null) {
            return@withContext resolveCoordinatesToCity(
                context = context,
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude
            )
        }

        // Fallback if emulator/device has no active GPS fix injected yet: resolve from timezone/locale or default metro
        return@withContext LocationCityResult.Error(
            "Could not obtain a GPS signal right now. Please ensure Location Services are enabled or pick from the city list below."
        )
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
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

    @Suppress("DEPRECATION")
    fun resolveCoordinatesToCity(
        context: Context?,
        latitude: Double,
        longitude: Double
    ): LocationCityResult {
        val nearestKnown = City.findNearestCity(latitude, longitude)
        val distKm = City.haversineDistanceKm(latitude, longitude, nearestKnown.latitude, nearestKnown.longitude)

        // If within 35 km of a curated city in ALL_CITIES, snap to that city's ID while using exact coordinates
        if (distKm <= 35.0) {
            val localizedCity = nearestKnown.copy(
                latitude = latitude,
                longitude = longitude,
                isLocationDetected = true
            )
            return LocationCityResult.Success(
                city = localizedCity,
                distanceKmToKnownMetro = distKm,
                sourceLabel = "GPS (${String.format(Locale.US, "%.2f°N, %.2f°E", latitude, longitude)})"
            )
        }

        // Try Android Geocoder to get exact city/locality name
        var geocodedCityName: String? = null
        var geocodedStateName: String? = null
        if (context != null && Geocoder.isPresent()) {
            try {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    geocodedCityName = addr.locality ?: addr.subAdminArea ?: addr.featureName
                    geocodedStateName = addr.adminArea ?: nearestKnown.state
                }
            } catch (_: Exception) {
            }
        }

        val resolvedName = geocodedCityName?.takeIf { it.isNotBlank() } ?: "${nearestKnown.name} Region"
        val resolvedState = geocodedStateName?.takeIf { it.isNotBlank() } ?: nearestKnown.state
        val customId = "gps_" + resolvedName.lowercase(Locale.US).replace("[^a-z0-9]".toRegex(), "_")

        val customCity = City(
            id = customId,
            name = resolvedName,
            state = resolvedState,
            latitude = latitude,
            longitude = longitude,
            isLocationDetected = true
        )

        return LocationCityResult.Success(
            city = customCity,
            distanceKmToKnownMetro = distKm,
            sourceLabel = "GPS (${String.format(Locale.US, "%.2f°N, %.2f°E", latitude, longitude)})"
        )
    }
}
