package com.bot.logic.dialogprocessor

import com.bot.entity.Response
import com.bot.entity.User

interface DialogProcessor {
	
	fun input(text: String): Response
	fun getResponse(): Response
	
}