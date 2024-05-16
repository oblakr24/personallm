package util

fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }