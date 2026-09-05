package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.data.model.EducationalLocale
import com.example.data.model.GLOBAL_EDUCATIONAL_LOCALES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationComplianceResult(
    val detectedCountry: String,
    val detectedCountryCode: String,
    val detectedStateOrProvince: String?,
    val detectedCity: String?,
    val matchedEducationalLocale: EducationalLocale?,
    val isVerified: Boolean,
    val complianceMessage: String,
    val postalCode: String = ""
) {
    val detectedState: String get() = detectedStateOrProvince ?: ""
    val detectedDistrict: String get() = matchedEducationalLocale?.schoolDistrict ?: ""
    val educationalStandard: String get() = matchedEducationalLocale?.standardTitle ?: "Accredited National Framework"
    val verificationSource: String get() = if (isVerified) "Device GPS / Regional Locale" else "Locale Preset / Postal Override"
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

    suspend fun resolvePostalOrZipCode(context: Context, inputPostal: String): LocationComplianceResult = withContext(Dispatchers.IO) {
        val clean = inputPostal.trim().uppercase()
        if (clean.isBlank()) {
            return@withContext detectAndVerifyHomeCountry(context)
        }

        var foundCountry = "United States"
        var foundCountryCode = "US"
        var foundState: String? = null
        var foundCity: String? = null

        // Try Geocoder reverse lookup first for precise postal resolution
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
            // Fallback to pattern matching
        }

        // Comprehensive Postal / ZIP Prefix Resolution if Geocoder returned partial or blank
        if (foundState.isNullOrBlank()) {
            val usZipMatch = Regex("^\\d{5}(-\\d{4})?$").find(clean)
            if (usZipMatch != null) {
                val zipStr = clean.take(5)
                val num = zipStr.toIntOrNull() ?: 0
                foundCountry = "United States"
                foundCountryCode = "US"
                when (zipStr) {
                    "85379" -> { foundState = "Arizona"; foundCity = "Surprise" }
                    "90210", "90211", "90212" -> { foundState = "California"; foundCity = "Beverly Hills" }
                    else -> {
                        when {
                            num in 85251..85260 || num in 85266..85268 || num == 85271 -> { foundState = "Arizona"; foundCity = "Scottsdale" }
                            num in 85201..85215 -> { foundState = "Arizona"; foundCity = "Mesa" }
                            num in 85224..85226 -> { foundState = "Arizona"; foundCity = "Chandler" }
                            num in 85233..85234 || num in 85295..85297 -> { foundState = "Arizona"; foundCity = "Gilbert" }
                            num in 85301..85310 -> { foundState = "Arizona"; foundCity = "Glendale" }
                            num == 85345 || num in 85381..85383 -> { foundState = "Arizona"; foundCity = "Peoria" }
                            num in 85281..85284 -> { foundState = "Arizona"; foundCity = "Tempe" }
                            num in 85701..85756 -> { foundState = "Arizona"; foundCity = "Tucson" }
                            num in 86001..86004 -> { foundState = "Arizona"; foundCity = "Flagstaff" }
                            num in 90000..96199 -> { foundState = "California"; foundCity = "Los Angeles" }
                            num in 75000..79999 -> { foundState = "Texas"; foundCity = "Dallas" }
                            num in 10000..14999 -> { foundState = "New York"; foundCity = "New York City" }
                            num in 32000..34999 -> { foundState = "Florida"; foundCity = "Miami" }
                            num in 60000..62999 -> { foundState = "Illinois"; foundCity = "Chicago" }
                            num in 98000..99499 -> { foundState = "Washington"; foundCity = "Seattle" }
                            num in 1000..2799 -> { foundState = "Massachusetts"; foundCity = "Boston" }
                            num in 15000..19699 -> { foundState = "Pennsylvania"; foundCity = "Philadelphia" }
                            num in 30000..31999 -> { foundState = "Georgia"; foundCity = "Atlanta" }
                            num in 43000..45999 -> { foundState = "Ohio"; foundCity = "Columbus" }
                            num in 48000..49999 -> { foundState = "Michigan"; foundCity = "Detroit" }
                            num in 27000..28999 -> { foundState = "North Carolina"; foundCity = "Charlotte" }
                            num in 20100..24658 -> { foundState = "Virginia"; foundCity = "Fairfax" }
                            num in 80000..81658 -> { foundState = "Colorado"; foundCity = "Denver" }
                            num in 85000..86556 -> { foundState = "Arizona"; foundCity = "Phoenix" }
                            num in 20000..20599 -> { foundState = "Washington D.C."; foundCity = "Washington" }
                            else -> { foundState = "California"; foundCity = "Los Angeles" }
                        }
                    }
                }
            } else if (clean.startsWith("SW") || clean.startsWith("EC") || clean.startsWith("W1") || clean.startsWith("E1") || clean.startsWith("N1") || clean.startsWith("SE") || clean.startsWith("WC")) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "England - Greater London"
                foundCity = "London"
            } else if (clean.startsWith("M") && clean.length <= 4 && clean.any { it.isDigit() }) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "England - North West"
                foundCity = "Manchester"
            } else if (clean.startsWith("B") && clean.length <= 4 && clean.any { it.isDigit() }) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "England - West Midlands"
                foundCity = "Birmingham"
            } else if (clean.startsWith("EH")) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "Scotland"
                foundCity = "Edinburgh"
            } else if (clean.startsWith("CF")) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "Wales"
                foundCity = "Cardiff"
            } else if (clean.startsWith("BT")) {
                foundCountry = "United Kingdom"
                foundCountryCode = "GB"
                foundState = "Northern Ireland"
                foundCity = "Belfast"
            } else if (clean.startsWith("M5") || clean.startsWith("M4") || clean.startsWith("M6") || clean.startsWith("M3")) {
                foundCountry = "Canada"
                foundCountryCode = "CA"
                foundState = "Ontario"
                foundCity = "Toronto"
            } else if (clean.startsWith("K1") || clean.startsWith("K2")) {
                foundCountry = "Canada"
                foundCountryCode = "CA"
                foundState = "Ontario"
                foundCity = "Ottawa"
            } else if (clean.startsWith("H2") || clean.startsWith("H3") || clean.startsWith("H4")) {
                foundCountry = "Canada"
                foundCountryCode = "CA"
                foundState = "Quebec"
                foundCity = "Montreal"
            } else if (clean.startsWith("V5") || clean.startsWith("V6") || clean.startsWith("V7")) {
                foundCountry = "Canada"
                foundCountryCode = "CA"
                foundState = "British Columbia"
                foundCity = "Vancouver"
            } else if (clean.startsWith("T2") || clean.startsWith("T3")) {
                foundCountry = "Canada"
                foundCountryCode = "CA"
                foundState = "Alberta"
                foundCity = "Calgary"
            } else if (clean.startsWith("200") || clean.startsWith("201") || clean.startsWith("202")) {
                foundCountry = "Australia"
                foundCountryCode = "AU"
                foundState = "New South Wales"
                foundCity = "Sydney"
            } else if (clean.startsWith("300") || clean.startsWith("301") || clean.startsWith("302")) {
                foundCountry = "Australia"
                foundCountryCode = "AU"
                foundState = "Victoria"
                foundCity = "Melbourne"
            } else if (clean.startsWith("400") || clean.startsWith("401")) {
                foundCountry = "Australia"
                foundCountryCode = "AU"
                foundState = "Queensland"
                foundCity = "Brisbane"
            } else if (clean.startsWith("110")) {
                foundCountry = "India"
                foundCountryCode = "IN"
                foundState = "National Capital Region (Delhi)"
                foundCity = "New Delhi"
            } else if (clean.startsWith("400")) {
                foundCountry = "India"
                foundCountryCode = "IN"
                foundState = "Maharashtra"
                foundCity = "Mumbai"
            } else if (clean.startsWith("560")) {
                foundCountry = "India"
                foundCountryCode = "IN"
                foundState = "Karnataka"
                foundCity = "Bengaluru"
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
            complianceMessage = "Resolved from postal/zip override ($clean): Aligned to ${matchedLocale.standardTitle} for ${matchedLocale.schoolDistrict}.",
            postalCode = clean
        )
    }

    suspend fun detectAndVerifyHomeCountry(context: Context): LocationComplianceResult = withContext(Dispatchers.IO) {
        var detectedCountryName = "United States"
        var detectedCountryCode = "US"
        var detectedState: String? = "California"
        var detectedCity: String? = "Los Angeles"
        var isFromGps = false

        try {
            if (hasLocationPermission(context)) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastLocation: Location? = try {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                } catch (e: SecurityException) {
                    null
                }

                if (lastLocation != null) {
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
                            }
                            isFromGps = true
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            addr.countryName?.let { detectedCountryName = it }
                            addr.countryCode?.let { detectedCountryCode = it.uppercase() }
                            addr.adminArea?.let { detectedState = it }
                            val reportedCity = addr.locality ?: addr.subAdminArea ?: addr.subLocality
                            if (!reportedCity.isNullOrBlank()) {
                                detectedCity = reportedCity
                            }
                            isFromGps = true
                        }
                    }
                }
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
                    val locale = Locale("", isoCode)
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
            "Verified via device location: Curriculum locked strictly to $normalizedCountry educational standards."
        } else {
            "Detected via system regional locale ($detectedCountryCode): Curriculum locked to $normalizedCountry guidelines."
        }

        LocationComplianceResult(
            detectedCountry = normalizedCountry,
            detectedCountryCode = detectedCountryCode,
            detectedStateOrProvince = detectedState ?: matchedLocale.stateOrProvince,
            detectedCity = detectedCity ?: matchedLocale.city,
            matchedEducationalLocale = matchedLocale,
            isVerified = true,
            complianceMessage = complianceMsg
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
