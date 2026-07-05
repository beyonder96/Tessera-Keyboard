import re

with open('app/src/main/java/com/example/PredictionEngine.kt', 'r') as f:
    content = f.read()

new_method = """    fun getSwipePrediction(swipePattern: String): String? {
        if (swipePattern.length < 2) return null
        val lower = swipePattern.lowercase()
        val firstChar = lower.first()
        val lastChar = lower.last()
        
        // Find best match in dictionary
        val matches = dictionary.filter { word ->
            if (word.first() != firstChar || word.last() != lastChar) return@filter false
            
            // Check if all characters in the word appear in the swipe pattern in order
            var wordIdx = 0
            for (char in lower) {
                if (wordIdx < word.length && char == word[wordIdx]) {
                    wordIdx++
                }
            }
            wordIdx == word.length
        }
        
        // Prefer shorter words that match the pattern, or most common words
        return matches.sortedBy { it.length }.firstOrNull()
    }
"""

idx = content.rfind('}')
content = content[:idx] + new_method + content[idx:]

with open('app/src/main/java/com/example/PredictionEngine.kt', 'w') as f:
    f.write(content)
