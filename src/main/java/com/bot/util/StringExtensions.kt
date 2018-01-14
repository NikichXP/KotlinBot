package com.bot.util


fun String.matches(regex: String): Boolean = this.matches(kotlin.text.Regex(regex))
fun String.selection(prefix: String = "/"): Int? {
	if (this.substring(prefix.length).matches("\\d{1,}")) {
		return this.substring(prefix.length).toInt()
	}
	return null
}

fun String.isSelection(prefix: String = "/"): Boolean = this.substring(prefix.length).matches("\\d{1,}")