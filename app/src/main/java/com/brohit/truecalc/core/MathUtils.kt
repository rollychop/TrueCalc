package com.brohit.truecalc.core

import kotlin.math.abs
import kotlin.math.pow

object MathUtils {
    fun calculateInterestRate(principal: Double, termMonths: Int, installment: Double): Double? {
        if (principal <= 0.0 || termMonths <= 0 || installment <= 0.0) return null

        var rateGuess =
            (installment * termMonths / principal - 1) / termMonths  // Smart initial guess
        val tolerance = 1e-6
        val maxIterations = 20  // Limit iterations for real-time performance

        for (i in 0 until maxIterations) {
            val presentValue = calculatePresentValue(rateGuess, termMonths, installment)
            val diff = presentValue - principal

            if (abs(diff) < tolerance) return rateGuess * 12 * 100  // Convert monthly rate to annual %

            // Approximate derivative for Newton-Raphson
            val derivative = (calculatePresentValue(
                rateGuess + tolerance,
                termMonths,
                installment
            ) - presentValue) / tolerance
            if (derivative == 0.0) return null  // Avoid division by zero

            rateGuess -= diff / derivative
            if (rateGuess < 0) return null  // Negative rate is invalid
        }

        return null  // Failed to converge
    }

    private fun calculatePresentValue(rate: Double, termMonths: Int, installment: Double): Double {
        if (rate == 0.0) return installment * termMonths  // Special case: No interest
        return installment * ((1 - (1 + rate).pow(-termMonths)) / rate)  // Faster formula
    }


}