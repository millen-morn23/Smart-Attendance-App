package com.example.utils

import kotlin.math.*

object LocationUtils {

    /**
     * Calculates the physical distance in meters between two coordinates using the Haversine formula.
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth's mean radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Determines whether the student's location lies within the allowed radius of the class session.
     */
    fun isWithinRadius(
        studentLat: Double, studentLon: Double,
        classLat: Double, classLon: Double,
        radiusMeters: Double
    ): Boolean {
        val distance = calculateDistance(studentLat, studentLon, classLat, classLon)
        return distance <= radiusMeters
    }
}
