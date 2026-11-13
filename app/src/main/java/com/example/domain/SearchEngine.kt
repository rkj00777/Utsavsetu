package com.example.domain

import com.example.domain.model.Festival
import java.text.Normalizer
import kotlin.math.min

object SearchEngine {

    private val QUERY_CONNECTORS = setOf(
        "and", "or", "with", "for", "in", "on", "of", "the", "a", "an", "india", "indian", "all", "festival", "festivals"
    )

    private val GENERIC_FESTIVAL_WORDS = setOf(
        "puja", "pooja", "pujan", "pujo", "vrat", "vrata", "vratam", "utsav", "utsavam", "parva", "jayanti", "janmotsav", "mahotsav"
    )

    /**
     * Normalizes Hinglish/Tanglish transliterated text:
     * - Strips diacritical accents
     * - Converts to lowercase
     * - Collapses common phonetic equivalents (e.g., w -> v, ee -> i, oo -> u, sh -> s, th -> t)
     */
    fun normalize(input: String): String {
        val decomposed = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFD)
        val stripped = decomposed.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return stripped
            .replace("w", "v")
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("aa", "a")
            .replace("ph", "f")
            .replace("sh", "s")
            .replace("th", "t")
            .replace("kh", "k")
            .replace("gh", "g")
            .replace("ch", "c")
            .replace("dh", "d")
            .replace("bh", "b")
            .replace("zh", "z")
            .replace("[^a-z0-9\\s]".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /**
     * Deeper Indic phonetic skeleton that unifies labial/dental/vowel transliteration variations:
     * - Collapses b / v / w -> v (so "gobardhan", "goberdhan", "goverdhan", "govardhan" -> identical skeleton!)
     * - Collapses dhooj / dooj / dhoj / duj -> "duj"
     * - Collapses bhaiya / bhaiyya / bhai -> consistent root
     * - Collapses duplicate consonants (dd->d, jj->j, tt->t, nn->n, ll->l, ss->s, mm->m, kk->k)
     */
    fun phoneticSkeleton(input: String): String {
        val norm = normalize(input)
        if (norm.isEmpty()) return ""
        return norm
            .replace("aiya", "ai")
            .replace("aiy", "ai")
            .replace("ayya", "ai")
            .replace("b", "v")
            .replace("ou", "u")
            .replace("o", "u")
            .replace("e", "a")
            .replace("y", "i")
            .replace("z", "j")
            .replace("q", "k")
            .replace("x", "ks")
            .replace(Regex("([a-z])\\1+"), "$1")
            .trim()
    }

    /**
     * Searches a list of festivals with multi-name regional alias ranking, Indic phonetic skeleton
     * matching (e.g. "Bhai dhooj", "gobardhan"), multi-festival compound query support, and typo tolerance.
     */
    fun search(query: String, festivals: List<Festival>): List<Festival> {
        val rawQuery = query.trim().lowercase()
        if (rawQuery.isBlank()) return festivals

        val normQuery = normalize(rawQuery)
        val skelQuery = phoneticSkeleton(rawQuery)

        // Support compound queries like "Bhai dhooj and gobardhan" or "diwali, holi"
        val subQueries = splitCompoundQuery(rawQuery)

        val scored = festivals.mapNotNull { festival ->
            var bestScore = calculateMatchScore(rawQuery, normQuery, skelQuery, festival)
            if (subQueries.size > 1) {
                for (sub in subQueries) {
                    val subNorm = normalize(sub)
                    val subSkel = phoneticSkeleton(sub)
                    val subScore = calculateMatchScore(sub, subNorm, subSkel, festival)
                    if (subScore > bestScore) {
                        bestScore = subScore
                    }
                }
            }
            if (bestScore > 0) Pair(festival, bestScore) else null
        }

        return scored.sortedByDescending { it.second }.map { it.first }
    }

    private fun splitCompoundQuery(rawQuery: String): List<String> {
        val parts = rawQuery
            .split(Regex("\\s+(?:and|or|&)\\s+|[,;/+&]+"))
            .map { it.trim() }
            .filter { it.length >= 3 && it !in QUERY_CONNECTORS }
        return if (parts.size > 1) parts else listOf(rawQuery)
    }

    /**
     * Returns the specific regional name or alias that matched the user's search query,
     * helping users understand why a festival with multiple names matched their search.
     */
    fun findMatchedRegionalName(query: String, festival: Festival): String? {
        val rawQuery = query.trim().lowercase()
        if (rawQuery.length < 2) return null
        val normQuery = normalize(rawQuery)
        val skelQuery = phoneticSkeleton(rawQuery)

        val regionalBadges = FestivalRegionalNames.getRegionalNamesForFestival(festival)
        for (badge in regionalBadges) {
            val normBadge = normalize(badge.festivalName)
            val skelBadge = phoneticSkeleton(badge.festivalName)
            if (badge.festivalName.lowercase().contains(rawQuery) ||
                (normQuery.isNotEmpty() && normBadge.contains(normQuery)) ||
                (skelQuery.length >= 4 && skelBadge.contains(skelQuery)) ||
                (badge.nativeScript != null && badge.nativeScript.contains(rawQuery))
            ) {
                return "${badge.festivalName} (${badge.regionOrLanguage})"
            }
        }

        for (alias in festival.aliases) {
            val clean = alias.trim()
            val normAlias = normalize(clean)
            val skelAlias = phoneticSkeleton(clean)
            if (clean.lowercase().contains(rawQuery) ||
                (normQuery.isNotEmpty() && normAlias.contains(normQuery)) ||
                (skelQuery.length >= 4 && skelAlias.contains(skelQuery))
            ) {
                return clean.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            }
        }
        return null
    }

    private fun calculateMatchScore(
        rawQuery: String,
        normQuery: String,
        skelQuery: String,
        festival: Festival
    ): Int {
        val normNameEng = normalize(festival.nameEnglish)
        val skelNameEng = phoneticSkeleton(festival.nameEnglish)
        val regionalBadges = FestivalRegionalNames.getRegionalNamesForFestival(festival)
        val allAliases = (festival.aliases + regionalBadges.map { it.festivalName }).map { it.trim().lowercase() }
        val normAliases = allAliases.map { normalize(it) }
        val skelAliases = allAliases.map { phoneticSkeleton(it) }
        val normQueryNoSpaces = normQuery.replace(" ", "")
        val skelQueryNoSpaces = skelQuery.replace(" ", "")

        // 1. Exact alias or ID match (Highest rank)
        if (festival.id.lowercase() == rawQuery || allAliases.contains(rawQuery)) {
            return 100
        }
        if (normQuery.isNotEmpty() && (normAliases.contains(normQuery) || normAliases.any { it.replace(" ", "") == normQueryNoSpaces })) {
            return 95
        }
        if (skelQuery.length >= 3 && (skelAliases.contains(skelQuery) || skelAliases.any { it.replace(" ", "") == skelQueryNoSpaces })) {
            return 92
        }

        // 2. Exact substring in English name, Native script name, or Regional script
        if (festival.nameEnglish.lowercase().contains(rawQuery) ||
            festival.nameNative.lowercase().contains(rawQuery) ||
            regionalBadges.any { it.nativeScript?.contains(rawQuery) == true }
        ) {
            return 88
        }
        if (normQuery.isNotEmpty() && (normNameEng.contains(normQuery) || normNameEng.replace(" ", "").contains(normQueryNoSpaces))) {
            return 84
        }
        if (skelQuery.length >= 3 && (skelNameEng.contains(skelQuery) || skelNameEng.replace(" ", "").contains(skelQueryNoSpaces))) {
            return 82
        }

        // 3. Prefix match in aliases (norm or phonetic skeleton)
        if (normQuery.isNotEmpty() && normAliases.any { it.startsWith(normQuery) || it.replace(" ", "").startsWith(normQueryNoSpaces) }) {
            return 75
        }
        if (skelQuery.length >= 3 && skelAliases.any { it.startsWith(skelQuery) || it.replace(" ", "").startsWith(skelQueryNoSpaces) }) {
            return 72
        }

        // 4. Substring match in aliases (norm or phonetic skeleton)
        if (normQuery.isNotEmpty() && normAliases.any { it.contains(normQuery) || it.replace(" ", "").contains(normQueryNoSpaces) }) {
            return 65
        }
        if (skelQuery.length >= 3 && skelAliases.any { it.contains(skelQuery) || it.replace(" ", "").contains(skelQueryNoSpaces) }) {
            return 62
        }

        // 5. Token-level & fuzzy edit-distance matching for multi-word or slightly misspelled queries
        val queryTokens = normQuery.split(" ")
            .map { it.trim() }
            .filter { it.length >= 3 && it !in QUERY_CONNECTORS }
        val distinctiveTokens = queryTokens.filter { it !in GENERIC_FESTIVAL_WORDS }
        val tokensToCheck = distinctiveTokens.ifEmpty { queryTokens }

        if (tokensToCheck.isNotEmpty()) {
            val candidateTokens = (normNameEng.split(" ") + normAliases.flatMap { it.split(" ") })
                .map { it.trim() }
                .filter { it.length >= 3 }
                .distinct()
            val candidateSkelTokens = candidateTokens.map { phoneticSkeleton(it) }

            var matchedTokenCount = 0
            for (qToken in tokensToCheck) {
                val qSkel = phoneticSkeleton(qToken)
                val tokenMatched = candidateTokens.any { cTok ->
                    cTok.startsWith(qToken) || qToken.startsWith(cTok) ||
                        (qToken.length >= 4 && cTok.length >= 4 && levenshtein(qToken, cTok) <= 1)
                } || candidateSkelTokens.any { cSkel ->
                    cSkel.isNotEmpty() && qSkel.isNotEmpty() && (
                        cSkel.startsWith(qSkel) || qSkel.startsWith(cSkel) ||
                            (qSkel.length >= 4 && cSkel.length >= 4 && levenshtein(qSkel, cSkel) <= 1) ||
                            (qSkel.length >= 6 && cSkel.length >= 6 && levenshtein(qSkel, cSkel) <= 2)
                        )
                }
                if (tokenMatched) matchedTokenCount++
            }

            if (matchedTokenCount == tokensToCheck.size) {
                return 58
            } else if (matchedTokenCount > 0 && tokensToCheck.size > 1) {
                return 45
            }
        }

        // 6. Match in significance or traditions
        if (festival.significance.lowercase().contains(rawQuery) ||
            festival.traditions.any { it.displayName.lowercase().contains(rawQuery) }
        ) {
            return 30
        }

        return 0
    }

    private fun levenshtein(lhs: String, rhs: String): Int {
        if (lhs == rhs) return 0
        if (lhs.isEmpty()) return rhs.length
        if (rhs.isEmpty()) return lhs.length

        var prev = IntArray(rhs.length + 1) { it }
        var curr = IntArray(rhs.length + 1)

        for (i in 1..lhs.length) {
            curr[0] = i
            for (j in 1..rhs.length) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[rhs.length]
    }
}

