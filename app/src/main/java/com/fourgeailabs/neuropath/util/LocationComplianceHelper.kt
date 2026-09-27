package com.fourgeailabs.neuropath.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.fourgeailabs.neuropath.data.model.EducationalLocale
import com.fourgeailabs.neuropath.data.model.GLOBAL_EDUCATIONAL_LOCALES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executor

data class LocationComplianceResult(
    val detectedCountry: String,
    val detectedCountryCode: String,
    val detectedStateOrProvince: String?,
    val detectedCity: String?,
    val matchedEducationalLocale: EducationalLocale?,
    val isVerified: Boolean,
    val complianceMessage: String,
    val postalCode: String = "",
    val isGoogleMapsVerified: Boolean = false,
    val resolutionSource: String = "Android Geocoder / postal resolver"
) {
    val detectedState: String get() = detectedStateOrProvince ?: ""
    val detectedDistrict: String get() = matchedEducationalLocale?.schoolDistrict ?: ""
    val educationalStandard: String get() = matchedEducationalLocale?.standardTitle ?: "Accredited National Framework"
    val verificationSource: String get() = if (isVerified) resolutionSource else "Locale Preset / Postal Override"
}

object LocationComplianceHelper {

    const val PRIVACY_DISCLAIMER_TITLE = "Privacy & Locale Detection Notice"
    const val PRIVACY_DISCLAIMER_TEXT = "Location services are used strictly to detect your regional educational jurisdiction (state/province, school district, and local curriculum standards). We NEVER track, record, store, or share your exact GPS coordinates, street address, or real-time location. 100% private, local-first, and child-safe."

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    // Exact-ZIP overrides checked before the range table and before the Android
    // Geocoder: small cities that geocoders (and broad ZIP ranges) collapse
    // into a nearby metro label.
    private val US_ZIP_EXACT: Map<String, Pair<String, String>> = mapOf(
        "85374" to ("Arizona" to "Surprise"),
        "85378" to ("Arizona" to "Surprise"),
        "85379" to ("Arizona" to "Surprise"),
        "85387" to ("Arizona" to "Surprise"),
        "85388" to ("Arizona" to "Surprise"),
        "90210" to ("California" to "Beverly Hills"),
        "90211" to ("California" to "Beverly Hills"),
        "90212" to ("California" to "Beverly Hills")
    )

    private data class UsZipRange(val start: Int, val end: Int, val state: String, val city: String)
    // Full 50-state (+ DC + Puerto Rico) ZIP coverage. Each range maps to a
    // city that has a registry entry. Order matters: more specific ranges
    // (DC, Maryland, Arizona city clusters, El Paso) come before the broader
    // ranges they overlap, because the lookup takes the first match.
    private val US_ZIP_RANGES: List<UsZipRange> = listOf(
        UsZipRange(600, 999, "Puerto Rico", "San Juan"),
        UsZipRange(1000, 2799, "Massachusetts", "Boston"),
        UsZipRange(2800, 2999, "Rhode Island", "Providence"),
        UsZipRange(3000, 3999, "New Hampshire", "Manchester"),
        UsZipRange(4000, 4999, "Maine", "Portland"),
        UsZipRange(5000, 5999, "Vermont", "Burlington"),
        UsZipRange(6000, 6999, "Connecticut", "Bridgeport"),
        UsZipRange(7000, 8999, "New Jersey", "Newark"),
        UsZipRange(10000, 14999, "New York", "New York City"),
        UsZipRange(15000, 19699, "Pennsylvania", "Philadelphia"),
        UsZipRange(19700, 19999, "Delaware", "Wilmington"),
        UsZipRange(20000, 20599, "Washington D.C.", "Washington"),
        UsZipRange(20600, 21999, "Maryland", "Baltimore"),
        UsZipRange(20100, 24658, "Virginia", "Fairfax"),
        UsZipRange(24700, 26999, "West Virginia", "Charleston"),
        UsZipRange(27000, 28999, "North Carolina", "Charlotte"),
        UsZipRange(29000, 29999, "South Carolina", "Columbia"),
        UsZipRange(30000, 31999, "Georgia", "Atlanta"),
        UsZipRange(32000, 34999, "Florida", "Miami"),
        UsZipRange(35000, 36999, "Alabama", "Birmingham"),
        UsZipRange(37000, 38599, "Tennessee", "Nashville"),
        UsZipRange(38600, 39999, "Mississippi", "Jackson"),
        UsZipRange(40000, 42999, "Kentucky", "Louisville"),
        UsZipRange(43000, 45999, "Ohio", "Columbus"),
        UsZipRange(46000, 47999, "Indiana", "Indianapolis"),
        UsZipRange(48000, 49999, "Michigan", "Detroit"),
        UsZipRange(50000, 52999, "Iowa", "Des Moines"),
        UsZipRange(53000, 54999, "Wisconsin", "Milwaukee"),
        UsZipRange(55000, 56799, "Minnesota", "Minneapolis"),
        UsZipRange(57000, 57999, "South Dakota", "Sioux Falls"),
        UsZipRange(58000, 58999, "North Dakota", "Fargo"),
        UsZipRange(59000, 59999, "Montana", "Billings"),
        UsZipRange(60000, 62999, "Illinois", "Chicago"),
        UsZipRange(63000, 65999, "Missouri", "Kansas City"),
        UsZipRange(66000, 67999, "Kansas", "Wichita"),
        UsZipRange(68000, 69999, "Nebraska", "Omaha"),
        UsZipRange(70000, 71599, "Louisiana", "New Orleans"),
        UsZipRange(71600, 72999, "Arkansas", "Little Rock"),
        UsZipRange(73000, 74999, "Oklahoma", "Oklahoma City"),
        UsZipRange(75000, 79999, "Texas", "Dallas"),
        UsZipRange(88500, 88599, "Texas", "El Paso"),
        UsZipRange(80000, 81658, "Colorado", "Denver"),
        UsZipRange(82000, 83199, "Wyoming", "Cheyenne"),
        UsZipRange(83200, 83999, "Idaho", "Boise"),
        UsZipRange(84000, 84799, "Utah", "Salt Lake City"),
        // Arizona city clusters (checked before the state-wide Phoenix range).
        UsZipRange(85251, 85260, "Arizona", "Scottsdale"),
        UsZipRange(85266, 85268, "Arizona", "Scottsdale"),
        UsZipRange(85271, 85271, "Arizona", "Scottsdale"),
        UsZipRange(85201, 85215, "Arizona", "Mesa"),
        UsZipRange(85224, 85226, "Arizona", "Chandler"),
        UsZipRange(85233, 85234, "Arizona", "Gilbert"),
        UsZipRange(85295, 85297, "Arizona", "Gilbert"),
        UsZipRange(85301, 85310, "Arizona", "Glendale"),
        UsZipRange(85345, 85345, "Arizona", "Peoria"),
        UsZipRange(85381, 85383, "Arizona", "Peoria"),
        UsZipRange(85281, 85284, "Arizona", "Tempe"),
        UsZipRange(85370, 85389, "Arizona", "Surprise"),
        UsZipRange(85701, 85756, "Arizona", "Tucson"),
        UsZipRange(86001, 86004, "Arizona", "Flagstaff"),
        UsZipRange(85000, 86556, "Arizona", "Phoenix"),
        UsZipRange(87000, 88499, "New Mexico", "Albuquerque"),
        UsZipRange(89000, 89899, "Nevada", "Las Vegas"),
        UsZipRange(90000, 96199, "California", "Los Angeles"),
        UsZipRange(96700, 96899, "Hawaii", "Honolulu"),
        UsZipRange(97000, 97999, "Oregon", "Portland"),
        UsZipRange(98000, 99499, "Washington", "Seattle"),
        UsZipRange(99500, 99999, "Alaska", "Anchorage")
    )

    // UK postcode areas (the letters before the first digit of the outward
    // code, e.g. "BD" in "BD1 1AA") mapped to registry state/city pairs.
    private val UK_POSTCODE_AREAS: Map<String, Pair<String, String>> = mapOf(
        "BD" to ("England - Yorkshire & the Humber" to "Bradford"),
        "HU" to ("England - Yorkshire & the Humber" to "Kingston upon Hull"),
        "ST" to ("England - West Midlands" to "Stoke-on-Trent"),
        "DE" to ("England - East Midlands" to "Derby"),
        "SR" to ("England - North East" to "Sunderland"),
        "PO" to ("England - South East" to "Portsmouth"),
        "PR" to ("England - North West" to "Preston"),
        "LU" to ("England - East of England" to "Luton"),
        "LS" to ("England - Yorkshire & the Humber" to "Leeds"),
        "NE" to ("England - North East" to "Newcastle upon Tyne"),
        "BS" to ("England - South West" to "Bristol"),
        "BN" to ("England - South East" to "Brighton"),
        "SO" to ("England - South East" to "Southampton"),
        "OX" to ("England - South East" to "Oxford"),
        "CB" to ("England - East of England" to "Cambridge"),
        "NR" to ("England - East of England" to "Norwich"),
        "NG" to ("England - East Midlands" to "Nottingham"),
        "LE" to ("England - East Midlands" to "Leicester"),
        "PL" to ("England - South West" to "Plymouth"),
        "BA" to ("England - South West" to "Bath"),
        "CR" to ("England - Greater London" to "Croydon"),
        "AB" to ("Scotland" to "Aberdeen"),
        "G" to ("Scotland" to "Glasgow"),
        "SA" to ("Wales" to "Swansea"),
        "EH" to ("Scotland" to "Edinburgh"),
        "CF" to ("Wales" to "Cardiff"),
        "BT" to ("Northern Ireland" to "Belfast"),
        "L" to ("England - North West" to "Liverpool"),
        "M" to ("England - North West" to "Manchester"),
        "B" to ("England - West Midlands" to "Birmingham"),
        "S" to ("England - Yorkshire & the Humber" to "Sheffield"),
        // London postal areas.
        "E" to ("England - Greater London" to "London"),
        "EC" to ("England - Greater London" to "London"),
        "N" to ("England - Greater London" to "London"),
        "NW" to ("England - Greater London" to "London"),
        "SE" to ("England - Greater London" to "London"),
        "SW" to ("England - Greater London" to "London"),
        "W" to ("England - Greater London" to "London"),
        "WC" to ("England - Greater London" to "London"),
        "BR" to ("England - Greater London" to "London"),
        "DA" to ("England - Greater London" to "London"),
        "EN" to ("England - Greater London" to "London"),
        "HA" to ("England - Greater London" to "London"),
        "IG" to ("England - Greater London" to "London"),
        "KT" to ("England - Greater London" to "London"),
        "RM" to ("England - Greater London" to "London"),
        "SM" to ("England - Greater London" to "London"),
        "TW" to ("England - Greater London" to "London"),
        "UB" to ("England - Greater London" to "London"),
        "WD" to ("England - Greater London" to "London")
    )

    // Canadian postal-code prefixes (first two characters of the ANA NAN
    // format, e.g. "M5" in "M5V 2T6") mapped to registry state/city pairs.
    private val CA_POSTAL_PREFIXES: Map<String, Pair<String, String>> = mapOf(
        "M5" to ("Ontario" to "Toronto"),
        "M4" to ("Ontario" to "Toronto"),
        "M6" to ("Ontario" to "Toronto"),
        "M3" to ("Ontario" to "Toronto"),
        "L4" to ("Ontario" to "Mississauga"),
        "L5" to ("Ontario" to "Mississauga"),
        "K1" to ("Ontario" to "Ottawa"),
        "K2" to ("Ontario" to "Ottawa"),
        "G1" to ("Quebec" to "Quebec City"),
        "H2" to ("Quebec" to "Montreal"),
        "H3" to ("Quebec" to "Montreal"),
        "H4" to ("Quebec" to "Montreal"),
        "V5" to ("British Columbia" to "Vancouver"),
        "V6" to ("British Columbia" to "Vancouver"),
        "V7" to ("British Columbia" to "Vancouver"),
        "V8" to ("British Columbia" to "Victoria"),
        "V9" to ("British Columbia" to "Victoria"),
        "T2" to ("Alberta" to "Calgary"),
        "T3" to ("Alberta" to "Calgary"),
        "T5" to ("Alberta" to "Edmonton"),
        "T6" to ("Alberta" to "Edmonton"),
        "R2" to ("Manitoba" to "Winnipeg"),
        "R3" to ("Manitoba" to "Winnipeg"),
        "B3" to ("Nova Scotia" to "Halifax")
    )

    private fun mapKnownUsZip(clean: String): Pair<String, String>? = US_ZIP_EXACT[clean.take(5)]

    /**
     * Requests one live location fix (network first for speed, GPS for
     * precision) and waits up to [timeoutMs]. Returns null on timeout or when
     * no provider is enabled. Callers must guard with [hasLocationPermission].
     */
    private suspend fun requestFreshLocation(
        locationManager: LocationManager?,
        timeoutMs: Long = 30_000L
    ): Location? {
        if (locationManager == null) return null
        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
        ).filter { provider ->
            try {
                locationManager.isProviderEnabled(provider)
            } catch (e: Exception) {
                false
            }
        }
        if (providers.isEmpty()) return null
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                val cancelSignal = CancellationSignal()
                cont.invokeOnCancellation {
                    try { cancelSignal.cancel() } catch (e: Exception) { /* already settled */ }
                }
                val directExecutor = Executor { command -> command.run() }
                var settled = false
                fun settle(location: Location?) {
                    // Only a real fix settles the race: a null callback from one
                    // provider must not cancel the other provider's attempt.
                    // The timeout handles giving up.
                    if (location != null && !settled) {
                        settled = true
                        try { cancelSignal.cancel() } catch (e: Exception) { /* already settled */ }
                        cont.resume(location) { _, _, _ -> }
                    }
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        for (provider in providers) {
                            try {
                                locationManager.getCurrentLocation(
                                    provider,
                                    cancelSignal,
                                    directExecutor,
                                    ::settle
                                )
                            } catch (e: Exception) {
                                // Provider refused; the other one may still answer.
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                if (!settled) {
                                    settled = true
                                    try { locationManager.removeUpdates(this) } catch (e: Exception) { /* ignore */ }
                                    cont.resume(location) { _, _, _ -> }
                                }
                            }
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        }
                        cont.invokeOnCancellation {
                            try { locationManager.removeUpdates(listener) } catch (e: Exception) { /* ignore */ }
                        }
                        var requested = false
                        for (provider in providers) {
                            try {
                                locationManager.requestLocationUpdates(
                                    provider, 0L, 0f, listener, Looper.getMainLooper()
                                )
                                requested = true
                            } catch (e: Exception) {
                                // Provider refused; try the next one.
                            }
                        }
                        if (!requested) settle(null)
                    }
                } catch (e: Exception) {
                    settle(null)
                }
            }
        }
    }

    suspend fun resolvePostalOrZipCode(context: Context, inputPostal: String): LocationComplianceResult = withContext(Dispatchers.IO) {
        val clean = inputPostal.trim().uppercase()
        if (clean.isBlank()) {
            return@withContext detectAndVerifyHomeCountry(context)
        }

        var foundCountry = "United States"
        var foundCountryCode = "US"
        var foundState: String? = null
        var foundCity: String? = null

        // Known postal mappings take precedence over Android Geocoder because
        // geocoders may collapse a ZIP into a nearby metro label.
        val zipStr = clean.take(5)
        val exactUsZip = mapKnownUsZip(clean)
        if (exactUsZip != null) {
            foundCountry = "United States"
            foundCountryCode = "US"
            foundState = exactUsZip.first
            foundCity = exactUsZip.second
        } else {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(clean, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    addr.countryName?.let { foundCountry = it }
                    addr.countryCode?.let { foundCountryCode = it.uppercase() }
                    addr.adminArea?.let { foundState = it }
                    addr.locality?.let { foundCity = it }
                }
            } catch (_: Exception) {
                // Continue to deterministic postal-prefix fallback below.
            }
        }

        // Comprehensive Postal / ZIP Prefix Resolution if Geocoder returned partial or blank.
        // Table-driven: US ZIP ranges (all 50 states + DC + Puerto Rico), UK
        // postcode areas, Canadian ANA NAN prefixes, Australian 4-digit
        // postcodes vs Indian 6-digit PINs (disambiguated by length so that
        // e.g. 400xxx can never again resolve to both Brisbane and Mumbai).
        if (foundState.isNullOrBlank()) {
            val usZipMatch = Regex("^\\d{5}(-\\d{4})?$").find(clean)
            if (usZipMatch != null) {
                val num = zipStr.toIntOrNull() ?: 0
                foundCountry = "United States"
                foundCountryCode = "US"
                val range = US_ZIP_RANGES.firstOrNull { num in it.start..it.end }
                if (range != null) {
                    foundState = range.state
                    foundCity = range.city
                }
                // Unknown ZIP: leave state/city unset rather than inventing a location.
            } else if (Regex("^[A-Z]\\d[A-Z]").containsMatchIn(clean)) {
                // Canadian postal codes (ANA NAN shape). Checked before the UK
                // branch: "M5V" would otherwise look like a Manchester postcode.
                val caEntry = CA_POSTAL_PREFIXES[clean.take(2)]
                if (caEntry != null) {
                    foundCountry = "Canada"
                    foundCountryCode = "CA"
                    foundState = caEntry.first
                    foundCity = caEntry.second
                }
            } else if (Regex("^[A-Z]{1,2}\\d").containsMatchIn(clean)) {
                // UK postcodes: match on the postal area (letters before the
                // first digit of the outward code, e.g. "BD" in "BD1 1AA").
                val area = clean.takeWhile { it.isLetter() }
                val ukEntry = UK_POSTCODE_AREAS[area]
                if (ukEntry != null) {
                    foundCountry = "United Kingdom"
                    foundCountryCode = "GB"
                    foundState = ukEntry.first
                    foundCity = ukEntry.second
                }
            } else if (Regex("^\\d{4}$").matches(clean)) {
                // Australian postcodes are exactly 4 digits.
                foundCountry = "Australia"
                foundCountryCode = "AU"
                when (clean.first()) {
                    '2' -> { foundState = "New South Wales"; foundCity = "Sydney" }
                    '3' -> { foundState = "Victoria"; foundCity = "Melbourne" }
                    '4' -> { foundState = "Queensland"; foundCity = "Brisbane" }
                    '5' -> { foundState = "South Australia"; foundCity = "Adelaide" }
                    '6' -> { foundState = "Western Australia"; foundCity = "Perth" }
                    '7' -> { foundState = "Tasmania"; foundCity = "Hobart" }
                }
            } else if (Regex("^\\d{6}$").matches(clean)) {
                // Indian PIN codes are exactly 6 digits.
                foundCountry = "India"
                foundCountryCode = "IN"
                when {
                    clean.startsWith("110") -> { foundState = "National Capital Region (Delhi)"; foundCity = "New Delhi" }
                    clean.startsWith("400") -> { foundState = "Maharashtra"; foundCity = "Mumbai" }
                    clean.startsWith("560") -> { foundState = "Karnataka"; foundCity = "Bengaluru" }
                }
            }
        }

        val normalizedCountry = mapToStandardCountryName(foundCountryCode, foundCountry)
        val matchedLocale = GLOBAL_EDUCATIONAL_LOCALES.find {
            (it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(foundCountryCode, ignoreCase = true)) &&
            (foundState == null || it.stateOrProvince.contains(foundState!!, ignoreCase = true)) &&
            (foundCity != null && it.city.equals(foundCity!!, ignoreCase = true))
        } ?: GLOBAL_EDUCATIONAL_LOCALES.find {
            (it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(foundCountryCode, ignoreCase = true)) &&
            (foundState == null || it.stateOrProvince.contains(foundState!!, ignoreCase = true))
        } ?: GLOBAL_EDUCATIONAL_LOCALES.find {
            it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(foundCountryCode, ignoreCase = true)
        } ?: GLOBAL_EDUCATIONAL_LOCALES.first()

        LocationComplianceResult(
            detectedCountry = normalizedCountry,
            detectedCountryCode = foundCountryCode,
            detectedStateOrProvince = foundState ?: matchedLocale.stateOrProvince,
            detectedCity = foundCity ?: matchedLocale.city,
            matchedEducationalLocale = matchedLocale,
            isVerified = true,
            complianceMessage = "🗺️ Resolved via Android Geocoder / postal resolver ($clean): Aligned to ${matchedLocale.standardTitle} for ${matchedLocale.schoolDistrict}.",
            postalCode = clean,
            isGoogleMapsVerified = false,
            resolutionSource = "Android Geocoder / ZIP-postal fallback"
        )
    }

    suspend fun detectAndVerifyHomeCountry(context: Context): LocationComplianceResult = withContext(Dispatchers.IO) {
        var detectedCountryName = "United States"
        var detectedCountryCode = "US"
        var detectedState: String? = null
        var detectedCity: String? = null
        var isFromGps = false
        // Human-readable reason the GPS path did not produce a fix. Surfaced
        // in resolutionSource so the result card in the UI says exactly what
        // happened instead of silently showing a fallback city.
        var gpsFailure: String? = null

        try {
            if (hasLocationPermission(context)) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val enabledProviders = listOf(
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.GPS_PROVIDER
                ).filter { provider ->
                    try {
                        locationManager?.isProviderEnabled(provider) == true
                    } catch (e: Exception) {
                        false
                    }
                }
                if (enabledProviders.isEmpty()) {
                    gpsFailure = "system location providers are all disabled"
                } else {
                    // Ask the radio for a live fix first: getLastKnownLocation() is
                    // null when nothing has recently woken the providers, which used
                    // to make the scan silently fall back to the SIM country with no
                    // city/state. A live fix makes the scan actually use GPS.
                    val liveLocation: Location? = try {
                        requestFreshLocation(locationManager)
                    } catch (e: SecurityException) {
                        null
                    }
                    val lastLocation: Location? = liveLocation ?: try {
                        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    } catch (e: SecurityException) {
                        null
                    }

                    if (lastLocation == null) {
                        gpsFailure = "no fix within 30s and no cached location (providers on: ${enabledProviders.joinToString()})"
                    } else {
                        // Never guess Phoenix/Surprise from a broad coordinate box.
                        // Reverse geocoding supplies the actual city/state.
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // For API 33+, geocoder has async callback, but standard synchronous list works via deprecated fallback in IO dispatcher
                            val addresses: List<Address>? = try {
                                @Suppress("DEPRECATION")
                                geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
                            } catch (e: Exception) {
                                null
                            }
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                addr.countryName?.let { detectedCountryName = it }
                                addr.countryCode?.let { detectedCountryCode = it.uppercase() }
                                addr.adminArea?.let { detectedState = it }
                                val reportedCity = addr.locality ?: addr.subAdminArea ?: addr.subLocality
                                if (!reportedCity.isNullOrBlank()) {
                                    detectedCity = reportedCity
                                    isFromGps = true
                                } else if (!detectedState.isNullOrBlank()) {
                                    isFromGps = true
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = try {
                                geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
                            } catch (e: Exception) {
                                null
                            }
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                addr.countryName?.let { detectedCountryName = it }
                                addr.countryCode?.let { detectedCountryCode = it.uppercase() }
                                addr.adminArea?.let { detectedState = it }
                                val reportedCity = addr.locality ?: addr.subAdminArea ?: addr.subLocality
                                if (!reportedCity.isNullOrBlank()) {
                                    detectedCity = reportedCity
                                    isFromGps = true
                                } else if (!detectedState.isNullOrBlank()) {
                                    isFromGps = true
                                }
                            }
                        }
                        if (!isFromGps) {
                            gpsFailure = "fix acquired but the geocoder returned no address"
                        }
                    }
                }
            } else {
                gpsFailure = "location permission not granted"
            }

            // Fallback to Telephony or System Locale if GPS not available or permission withheld
            if (!isFromGps) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val simCountryIso = telephonyManager?.simCountryIso?.uppercase()
                val networkCountryIso = telephonyManager?.networkCountryIso?.uppercase()

                val isoCode = if (!simCountryIso.isNullOrBlank()) simCountryIso
                else if (!networkCountryIso.isNullOrBlank()) networkCountryIso
                else Locale.getDefault().country.uppercase()

                if (isoCode.isNotBlank()) {
                    detectedCountryCode = isoCode
                    val locale = Locale.Builder().setRegion(isoCode).build()
                    val name = locale.displayCountry
                    if (!name.isNullOrBlank()) {
                        detectedCountryName = name
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback safe defaults
            detectedCountryName = "United States"
            detectedCountryCode = "US"
        }

        // Map to supported educational locales
        val normalizedCountry = mapToStandardCountryName(detectedCountryCode, detectedCountryName)
        val matchedLocale = GLOBAL_EDUCATIONAL_LOCALES.find {
            (it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(detectedCountryCode, ignoreCase = true)) &&
            (detectedState == null || it.stateOrProvince.contains(detectedState!!, ignoreCase = true)) &&
            (detectedCity != null && it.city.equals(detectedCity!!, ignoreCase = true))
        } ?: GLOBAL_EDUCATIONAL_LOCALES.find {
            (it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(detectedCountryCode, ignoreCase = true)) &&
            (detectedState == null || it.stateOrProvince.contains(detectedState!!, ignoreCase = true))
        } ?: GLOBAL_EDUCATIONAL_LOCALES.find {
            it.country.equals(normalizedCountry, ignoreCase = true) || it.countryCode.equals(detectedCountryCode, ignoreCase = true)
        } ?: GLOBAL_EDUCATIONAL_LOCALES.first()

        val complianceMsg = if (isFromGps) {
            "🗺️ Location Verified via Android Geocoder: Curriculum locked strictly to $normalizedCountry (${detectedState ?: matchedLocale.stateOrProvince}, ${detectedCity ?: matchedLocale.city}) educational standards."
        } else {
            "📍 System Locale Detected ($detectedCountryCode): Curriculum locked to $normalizedCountry standards. Rescan or enter ZIP code to refine."
        }

        LocationComplianceResult(
            detectedCountry = normalizedCountry,
            detectedCountryCode = detectedCountryCode,
            detectedStateOrProvince = detectedState ?: matchedLocale.stateOrProvince,
            detectedCity = detectedCity,
            matchedEducationalLocale = matchedLocale,
            isVerified = true,
            complianceMessage = complianceMsg,
            isGoogleMapsVerified = false,
            resolutionSource = if (isFromGps) "Android Geocoder GPS / Network Location"
                else "System Locale Preset" + (gpsFailure?.let { " (GPS: $it)" } ?: "")
        )
    }

    private fun mapToStandardCountryName(countryCode: String, rawName: String): String {
        return when (countryCode.uppercase()) {
            "US", "USA" -> "United States"
            "GB", "UK" -> "United Kingdom"
            "CA" -> "Canada"
            "AU" -> "Australia"
            "IN" -> "India"
            "DE" -> "Germany"
            "FR" -> "France"
            "JP" -> "Japan"
            "BR" -> "Brazil"
            "MX" -> "Mexico"
            else -> {
                GLOBAL_EDUCATIONAL_LOCALES.find { it.country.contains(rawName, ignoreCase = true) }?.country ?: rawName
            }
        }
    }
}
