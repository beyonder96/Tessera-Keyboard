package com.example.engine

object KeyProximityMap {

    private val neighborMap = mapOf(
        'q' to charArrayOf('w', 'a', 's'),
        'w' to charArrayOf('q', 'e', 'a', 's'),
        'e' to charArrayOf('w', 'r', 's', 'd'),
        'r' to charArrayOf('e', 't', 'd', 'f'),
        't' to charArrayOf('r', 'y', 'f', 'g'),
        'y' to charArrayOf('t', 'u', 'g', 'h'),
        'u' to charArrayOf('y', 'i', 'h', 'j'),
        'i' to charArrayOf('u', 'o', 'j', 'k'),
        'o' to charArrayOf('i', 'p', 'k', 'l'),
        'p' to charArrayOf('o', 'l'),
        'a' to charArrayOf('q', 'w', 's', 'z'),
        's' to charArrayOf('a', 'w', 'e', 'd', 'x', 'z'),
        'd' to charArrayOf('s', 'e', 'r', 'f', 'c', 'x'),
        'f' to charArrayOf('d', 'r', 't', 'g', 'v', 'c'),
        'g' to charArrayOf('f', 't', 'y', 'h', 'b', 'v'),
        'h' to charArrayOf('g', 'y', 'u', 'j', 'n', 'b'),
        'j' to charArrayOf('h', 'u', 'i', 'k', 'm', 'n'),
        'k' to charArrayOf('j', 'i', 'o', 'l', 'm'),
        'l' to charArrayOf('k', 'o', 'p', 'ç'),
        'ç' to charArrayOf('l', 'p'),
        'z' to charArrayOf('a', 's', 'x'),
        'x' to charArrayOf('z', 's', 'd', 'c'),
        'c' to charArrayOf('x', 'd', 'f', 'v'),
        'v' to charArrayOf('c', 'f', 'g', 'b'),
        'b' to charArrayOf('v', 'g', 'h', 'n'),
        'n' to charArrayOf('b', 'h', 'j', 'm'),
        'm' to charArrayOf('n', 'j', 'k')
    )

    fun getNeighbors(c: Char): CharArray {
        return neighborMap[c.lowercaseChar()] ?: CharArray(0)
    }

    fun isNeighbor(c1: Char, c2: Char): Boolean {
        if (c1.equals(c2, ignoreCase = true)) return true
        val neighbors = neighborMap[c1.lowercaseChar()] ?: return false
        val target = c2.lowercaseChar()
        for (n in neighbors) {
            if (n == target) return true
        }
        return false
    }
}
