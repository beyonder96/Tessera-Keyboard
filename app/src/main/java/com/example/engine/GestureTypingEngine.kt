package com.example.engine

import android.graphics.Rect
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

data class TrajectoryPoint(
    val x: Float,
    val y: Float,
    val time: Long
)

data class KeyRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val cx: Float get() = (left + right) / 2f
    val cy: Float get() = (top + bottom) / 2f
    val width: Float get() = (right - left).toFloat()
    val height: Float get() = (bottom - top).toFloat()
}

class GestureTypingEngine(
    private val trie: TrieDictionary,
    private val staticDictionary: List<String>,
    private val accentRestorationMap: Map<String, String>,
    private val abbreviationsMap: Map<String, String>,
    private val bigramNextWordMap: Map<String, List<String>>
) {

    data class KeyCenter(val char: Char, val cx: Float, val cy: Float)

    private data class Corner(val char: Char, val trajectoryIdx: Int, val angle: Float)

    fun recognize(
        rawTrajectory: List<TrajectoryPoint>,
        keyBounds: Map<Char, KeyRect>,
        previousWord: String? = null
    ): List<String> {
        if (rawTrajectory.size < 3 || keyBounds.isEmpty()) return emptyList()

        // 1. Resample and smooth trajectory points
        val trajectory = resampleTrajectory(rawTrajectory, minSpacing = 8f)
        if (trajectory.size < 3) return emptyList()

        // 2. Compute key geometry
        val keyCenters = HashMap<Char, KeyCenter>(keyBounds.size)
        var totalWidth = 0f
        var count = 0
        for ((char, rect) in keyBounds) {
            keyCenters[char] = KeyCenter(char, rect.cx, rect.cy)
            totalWidth += rect.width
            count++
        }
        val avgKeyWidth = if (count > 0) totalWidth / count else 40f
        val keyTolerance = avgKeyWidth * 1.35f
        val keyToleranceSq = keyTolerance * keyTolerance

        val startPt = trajectory.first()
        val endPt = trajectory.last()

        // Total path length verification
        var totalPathLength = 0f
        for (i in 0 until trajectory.size - 1) {
            totalPathLength += hypot(trajectory[i + 1].x - trajectory[i].x, trajectory[i + 1].y - trajectory[i].y)
        }
        if (totalPathLength < avgKeyWidth * 0.75f) {
            // Path too short to be a swipe gesture
            return emptyList()
        }

        // 3. Candidate Start Keys (closest within tolerance)
        val startCandidates = keyCenters.values
            .map { it to distSq(startPt.x, startPt.y, it.cx, it.cy) }
            .filter { it.second <= keyToleranceSq * 1.3f }
            .sortedBy { it.second }
            .map { it.first.char }
            .take(3)

        // 4. Candidate End Keys (closest within tolerance)
        val endCandidates = keyCenters.values
            .map { it to distSq(endPt.x, endPt.y, it.cx, it.cy) }
            .filter { it.second <= keyToleranceSq * 1.6f }
            .sortedBy { it.second }
            .map { it.first.char }
            .take(4)

        if (startCandidates.isEmpty() || endCandidates.isEmpty()) return emptyList()

        // 5. Detect Corners / Directional Inflections
        val corners = detectCorners(trajectory, keyCenters, avgKeyWidth)

        // 6. Traverse Trie with Spatial Pruning
        val scoredWords = HashMap<String, Float>()

        for (startChar in startCandidates) {
            val rootNode = trie.root
            val childIdx = startChar - 'a'
            if (childIdx !in 0..25) continue
            val startNode = rootNode.children[childIdx] ?: continue

            val startKey = keyCenters[startChar] ?: continue
            val initialDist = hypot(startPt.x - startKey.cx, startPt.y - startKey.cy)

            traverseTrieSpatial(
                node = startNode,
                currentChar = startChar,
                depth = 1,
                trajectory = trajectory,
                trajIdx = 0,
                accumDist = initialDist,
                keyCenters = keyCenters,
                endCandidates = endCandidates,
                corners = corners,
                avgKeyWidth = avgKeyWidth,
                previousWord = previousWord,
                scoredWords = scoredWords
            )
        }

        // 7. Evaluate high-frequency static dictionary and accent restoration words
        evaluateStaticCandidates(
            trajectory = trajectory,
            startCandidates = startCandidates,
            endCandidates = endCandidates,
            keyCenters = keyCenters,
            corners = corners,
            avgKeyWidth = avgKeyWidth,
            previousWord = previousWord,
            scoredWords = scoredWords
        )

        // 8. Order by score descending and return top candidates
        return scoredWords.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
            .take(4)
    }

    private fun traverseTrieSpatial(
        node: TrieDictionary.TrieNode,
        currentChar: Char,
        depth: Int,
        trajectory: List<TrajectoryPoint>,
        trajIdx: Int,
        accumDist: Float,
        keyCenters: Map<Char, KeyCenter>,
        endCandidates: List<Char>,
        corners: List<Corner>,
        avgKeyWidth: Float,
        previousWord: String?,
        scoredWords: MutableMap<String, Float>
    ) {
        if (depth > 16) return

        // If this node represents valid words in dictionary:
        val nodeWords = node.words
        if (!nodeWords.isNullOrEmpty() && depth >= 2) {
            if (currentChar in endCandidates) {
                val trajProgress = trajIdx.toFloat() / max(1, trajectory.size - 1)
                if (trajProgress >= 0.50f) {
                    val endPt = trajectory.last()
                    val endKey = keyCenters[currentChar]
                    val finalEndDist = if (endKey != null) hypot(endPt.x - endKey.cx, endPt.y - endKey.cy) else 0f
                    val totalDistance = accumDist + finalEndDist

                    for (entry in nodeWords) {
                        val word = entry.word
                        val score = computeCandidateScore(
                            word = word,
                            normLength = depth,
                            totalDist = totalDistance,
                            freq = entry.frequency,
                            corners = corners,
                            keyCenters = keyCenters,
                            avgKeyWidth = avgKeyWidth,
                            previousWord = previousWord
                        )
                        val prevBest = scoredWords[word] ?: -Float.MAX_VALUE
                        if (score > prevBest) {
                            scoredWords[word] = score
                        }
                    }
                }
            }
        }

        // Now explore children of this node
        val maxReachDist = avgKeyWidth * 1.50f
        val maxReachDistSq = maxReachDist * maxReachDist

        for (cIndex in 0..25) {
            val child = node.children[cIndex] ?: continue
            val nextChar = ('a' + cIndex)
            val nextKey = keyCenters[nextChar] ?: continue

            // Allow consecutive repeated letters (e.g. "carro", "isso")
            if (nextChar == currentChar) {
                traverseTrieSpatial(
                    node = child,
                    currentChar = nextChar,
                    depth = depth + 1,
                    trajectory = trajectory,
                    trajIdx = trajIdx,
                    accumDist = accumDist,
                    keyCenters = keyCenters,
                    endCandidates = endCandidates,
                    corners = corners,
                    avgKeyWidth = avgKeyWidth,
                    previousWord = previousWord,
                    scoredWords = scoredWords
                )
                continue
            }

            // Search trajectory from trajIdx onwards to find the closest point to nextKey
            var bestIdx = -1
            var minDistSq = Float.MAX_VALUE
            for (t in trajIdx until trajectory.size) {
                val pt = trajectory[t]
                val dSq = distSq(pt.x, pt.y, nextKey.cx, nextKey.cy)
                if (dSq < minDistSq) {
                    minDistSq = dSq
                    bestIdx = t
                }
            }

            if (minDistSq <= maxReachDistSq && bestIdx >= trajIdx) {
                traverseTrieSpatial(
                    node = child,
                    currentChar = nextChar,
                    depth = depth + 1,
                    trajectory = trajectory,
                    trajIdx = bestIdx,
                    accumDist = accumDist + sqrt(minDistSq),
                    keyCenters = keyCenters,
                    endCandidates = endCandidates,
                    corners = corners,
                    avgKeyWidth = avgKeyWidth,
                    previousWord = previousWord,
                    scoredWords = scoredWords
                )
            }
        }
    }

    private fun evaluateStaticCandidates(
        trajectory: List<TrajectoryPoint>,
        startCandidates: List<Char>,
        endCandidates: List<Char>,
        keyCenters: Map<Char, KeyCenter>,
        corners: List<Corner>,
        avgKeyWidth: Float,
        previousWord: String?,
        scoredWords: MutableMap<String, Float>
    ) {
        val staticPool = staticDictionary + accentRestorationMap.values + abbreviationsMap.values
        val maxReachDist = avgKeyWidth * 1.55f
        val maxReachDistSq = maxReachDist * maxReachDist

        for (word in staticPool) {
            val norm = TrieDictionary.normalizeFast(word)
            if (norm.length < 2) continue
            val first = norm.first()
            val last = norm.last()
            if (first !in startCandidates || last !in endCandidates) continue

            // Compute alignment distance along trajectory
            var currentTrajIdx = 0
            var accumDist = 0f
            var valid = true

            for (i in 0 until norm.length) {
                val c = norm[i]
                val key = keyCenters[c]
                if (key == null) {
                    valid = false
                    break
                }

                if (i > 0 && c == norm[i - 1]) {
                    // Repeated letter
                    continue
                }

                var bestIdx = -1
                var minDistSq = Float.MAX_VALUE
                for (t in currentTrajIdx until trajectory.size) {
                    val pt = trajectory[t]
                    val dSq = distSq(pt.x, pt.y, key.cx, key.cy)
                    if (dSq < minDistSq) {
                        minDistSq = dSq
                        bestIdx = t
                    }
                }

                if (minDistSq > maxReachDistSq || bestIdx < currentTrajIdx) {
                    valid = false
                    break
                }

                currentTrajIdx = bestIdx
                accumDist += sqrt(minDistSq)
            }

            if (valid && currentTrajIdx >= trajectory.size * 0.45f) {
                val score = computeCandidateScore(
                    word = word,
                    normLength = norm.length,
                    totalDist = accumDist,
                    freq = 245,
                    corners = corners,
                    keyCenters = keyCenters,
                    avgKeyWidth = avgKeyWidth,
                    previousWord = previousWord
                )
                val prevBest = scoredWords[word] ?: -Float.MAX_VALUE
                if (score > prevBest) {
                    scoredWords[word] = score
                }
            }
        }
    }

    private fun computeCandidateScore(
        word: String,
        normLength: Int,
        totalDist: Float,
        freq: Int,
        corners: List<Corner>,
        keyCenters: Map<Char, KeyCenter>,
        avgKeyWidth: Float,
        previousWord: String?
    ): Float {
        // Average spatial distance per letter normalized by key width
        val avgDist = totalDist / (normLength * avgKeyWidth)
        var score = -avgDist * 2.2f

        // Word frequency bonus (frequencies from 1 to 255)
        val freqBonus = ln((freq + 2).toFloat()) * 0.45f
        score += freqBonus

        // Corner matching bonus: reward words whose letters match detected inflection points
        val normWord = TrieDictionary.normalizeFast(word)
        for (corner in corners) {
            if (normWord.contains(corner.char)) {
                score += 1.3f
            } else {
                score -= 0.6f
            }
        }

        // Bigram context bonus
        if (!previousWord.isNullOrEmpty()) {
            val expectedNext = bigramNextWordMap[previousWord.lowercase()]
            if (expectedNext != null && expectedNext.contains(word.lowercase())) {
                score += 1.6f
            }
        }

        // Short words (2-3 chars) length penalty if trajectory was very long
        if (normLength <= 3 && totalDist > avgKeyWidth * 4.5f) {
            score -= 1.2f
        }

        return score
    }

    private fun detectCorners(
        trajectory: List<TrajectoryPoint>,
        keyCenters: Map<Char, KeyCenter>,
        avgKeyWidth: Float
    ): List<Corner> {
        val corners = mutableListOf<Corner>()
        if (trajectory.size < 6) return corners

        val step = max(1, trajectory.size / 18)
        val toleranceSq = (avgKeyWidth * 1.0f) * (avgKeyWidth * 1.0f)

        for (i in step until trajectory.size - step) {
            val pPrev = trajectory[i - step]
            val pCurr = trajectory[i]
            val pNext = trajectory[i + step]

            val v1x = pCurr.x - pPrev.x
            val v1y = pCurr.y - pPrev.y
            val v2x = pNext.x - pCurr.x
            val v2y = pNext.y - pCurr.y

            val len1 = hypot(v1x, v1y)
            val len2 = hypot(v2x, v2y)
            if (len1 < 5f || len2 < 5f) continue

            val dot = (v1x * v2x + v1y * v2y) / (len1 * len2)
            // dot = cos(theta). If theta > 40 degrees, dot < 0.76
            if (dot < 0.72f) {
                var closestChar: Char? = null
                var minDistSq = Float.MAX_VALUE
                for ((char, center) in keyCenters) {
                    val dSq = distSq(pCurr.x, pCurr.y, center.cx, center.cy)
                    if (dSq < minDistSq && dSq <= toleranceSq) {
                        minDistSq = dSq
                        closestChar = char
                    }
                }
                if (closestChar != null) {
                    if (corners.isEmpty() || corners.last().char != closestChar) {
                        val angle = acos(dot.coerceIn(-1f, 1f))
                        corners.add(Corner(closestChar, i, angle))
                    }
                }
            }
        }
        return corners
    }

    private fun resampleTrajectory(raw: List<TrajectoryPoint>, minSpacing: Float): List<TrajectoryPoint> {
        if (raw.isEmpty()) return emptyList()
        val result = ArrayList<TrajectoryPoint>(raw.size)
        result.add(raw.first())
        val minSpacingSq = minSpacing * minSpacing
        var lastPt = raw.first()

        for (i in 1 until raw.size - 1) {
            val pt = raw[i]
            val dSq = distSq(pt.x, pt.y, lastPt.x, lastPt.y)
            if (dSq >= minSpacingSq) {
                result.add(pt)
                lastPt = pt
            }
        }
        result.add(raw.last())
        return result
    }

    private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }
}
