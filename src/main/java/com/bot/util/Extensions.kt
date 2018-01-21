package com.bot.util

import java.time.*
import java.util.*

fun LocalDateTime.timeDiff(other: LocalDateTime) = Math.abs(this.toEpochSecond(ZoneOffset.UTC) - other.toEpochSecond(ZoneOffset.UTC))

fun toTimeWrap(time: Long, isMillis: Boolean = true): String {
	var t = if (isMillis) time / 1000 else time
	val ret = LinkedList<String>()
	ret.push(("" + (t % 60) + "s"))
	t /= 60
	ret.push("" + (t % 60) + "m")
	t /= 60
	ret.push("" + (t % 24) + "h")
	t /= 24
	if (t > 0) {
		ret.push("$t days")
	}
	return ret.stream().reduce { a, b -> "$a $b" }.orElse("SHIT")
}

fun LocalDateTime.getTimeDiff(other: LocalDateTime) = toTimeWrap(other.timeDiff(this), false)

fun String.matches(regex: String): Boolean = this.matches(kotlin.text.Regex(regex))
fun String.selection(prefix: String = "/"): Int? {
	if (this.substring(prefix.length).matches("\\d{1,}")) {
		return this.substring(prefix.length).toInt()
	}
	return null
}

fun String.isSelection(prefix: String = "/"): Boolean = this.substring(prefix.length).matches("\\d{1,}")