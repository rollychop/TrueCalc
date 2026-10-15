package com.brohit.truecalc.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSearchTest {

    private data class Item(val name: String, val keywords: List<String>, val description: String)

    private val items = listOf(
        Item("EMI Calculator", listOf("loan", "home loan", "car loan", "mortgage"), "Monthly loan installment"),
        Item("Compound Interest", listOf("ci", "investment", "growth"), "Interest on interest"),
        Item("Simple Interest", listOf("si"), "Flat interest"),
        Item("Fixed Deposit", listOf("fd", "bank deposit"), "Bank FD maturity"),
        Item("Recurring Deposit", listOf("rd", "monthly deposit"), "RD maturity"),
        Item("BMI Calculator", listOf("body mass index", "weight"), "Body mass index"),
        Item("Ideal Weight Calculator", listOf("healthy weight"), "Healthy body weight"),
    )

    private fun search(q: String) = TextSearch.rank(items, q, { it.name }, { it.keywords }, { it.description }).map { it.name }

    @Test
    fun blankQuery_matchesNothing() {
        assertTrue(search("").isEmpty())
        assertTrue(search("   ").isEmpty())
    }

    @Test
    fun keywordFindsTool() {
        assertEquals("EMI Calculator", search("mortgage").first())
        assertEquals("EMI Calculator", search("Home Loan").first())
        assertEquals("Fixed Deposit", search("fd").first())
    }

    @Test
    fun caseInsensitiveAndNamePrefixRanksFirst() {
        assertEquals(listOf("Compound Interest", "Simple Interest"), search("interest").take(2).sorted())
        assertEquals("Compound Interest", search("COMP").first())
        assertEquals("Recurring Deposit", search("recurring").first())
    }

    @Test
    fun allTokensMustMatch() {
        assertEquals(listOf("EMI Calculator"), search("car loan"))
        assertTrue(search("car zebra").isEmpty())
    }

    @Test
    fun descriptionOnlyMatchesRankLast() {
        val result = search("deposit")
        assertEquals(setOf("Fixed Deposit", "Recurring Deposit"), result.take(2).toSet())
    }

    @Test
    fun scoreIsZeroWhenNothingMatches() {
        assertEquals(0, TextSearch.score("xyz", "EMI", listOf("loan"), "desc"))
    }

    @Test
    fun wholeWordInName_beatsExactKeyword() {
        assertEquals(listOf("Ideal Weight Calculator", "BMI Calculator"), search("weight"))
    }
}
