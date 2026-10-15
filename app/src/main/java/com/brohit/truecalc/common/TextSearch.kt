package com.brohit.truecalc.common

/**
 * Small, dependency-free relevance scorer for tool search (name / keywords / description).
 *
 * The query is split into whitespace tokens; every token must match at least one field or the
 * item scores 0. Per token the best match counts: exact name > name prefix > whole word of name >
 * exact keyword > word prefix in name > keyword prefix > substring of name > substring of keyword > substring of
 * description. For multi-word queries a whole-phrase match on the name or a keyword gets a bonus, so "home loan" ranks
 * an item with the keyword "home loan" above one that merely contains both words.
 */
object TextSearch {

    fun score(query: String, name: String, keywords: List<String>, description: String): Int {
        val q = normalize(query)
        if (q.isEmpty()) return 0
        val n = normalize(name)
        val k = keywords.map(::normalize)
        val d = normalize(description)

        var total = 0
        for (token in q.split(' ')) {
            val s = tokenScore(token, n, k, d)
            if (s == 0) return 0
            total += s
        }
        val tokens = q.split(' ')
        if (tokens.size > 1) {
            // Phrase bonus for multi-word queries ("home loan"); single words are already ranked
            // by tokenScore.
            if (n == q || k.any { it == q }) total += 100
            else if (n.startsWith(q) || k.any { it.startsWith(q) }) total += 40
        }
        return total
    }

    /** Returns [items] that match [query], best first; ties keep the original order. */
    fun <T> rank(
        items: List<T>,
        query: String,
        name: (T) -> String,
        keywords: (T) -> List<String>,
        description: (T) -> String,
    ): List<T> = items
        .mapIndexed { index, item -> Triple(item, index, score(query, name(item), keywords(item), description(item))) }
        .filter { it.third > 0 }
        .sortedWith(compareByDescending<Triple<T, Int, Int>> { it.third }.thenBy { it.second })
        .map { it.first }

    private fun tokenScore(token: String, name: String, keywords: List<String>, description: String): Int {
        val nameWords = name.split(' ')
        return when {
            name == token -> 100
            name.startsWith(token) -> 80
            nameWords.any { it == token } -> 75
            keywords.any { it == token } -> 70
            nameWords.any { it.startsWith(token) } -> 60
            keywords.any { kw -> kw.startsWith(token) || kw.split(' ').any { it.startsWith(token) } } -> 50
            name.contains(token) -> 40
            keywords.any { it.contains(token) } -> 30
            description.contains(token) -> 10
            else -> 0
        }
    }

    private fun normalize(s: String): String =
        s.lowercase().trim().replace(Regex("\\s+"), " ")
}
