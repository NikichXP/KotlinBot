package com.bot.api

import com.bot.logic.TelegramInputParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@RestController
class RequestHandler {
	
	@Autowired
	lateinit var parser: TelegramInputParser
	
	@RequestMapping("/")
	fun listen(req: HttpServletRequest) {
		var x = BufferedReader(InputStreamReader(req.inputStream, StandardCharsets.UTF_8))
			.lines().reduce { s1, s2 -> s1 + s2 }.orElse("")
		x = String(x.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
		val sb = StringBuilder()
		var i = 0
		while (i < x.length) {
			if (x[i] != '\\') {
				sb.append(x[i])
			} else {
				if (x[i + 1] == 'u') {
					val charCode = Integer.parseInt(x.substring(i + 2, i + 6), 16)
					sb.append(Character.toChars(charCode))
				}
				i += 5
			}
			i++
		}
		try {
			parser.input(sb.toString())
		} catch (e: Exception) {
			println(sb.toString())
		}
		
	}
}
