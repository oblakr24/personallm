package util

fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun String.ellipsized(maxLen: Int) = if (length <= maxLen) this else this.take(maxLen - 3) + "..."