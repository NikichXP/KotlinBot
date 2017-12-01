package com.bot.logic.dialogprocessor

import com.bot.entity.Response

interface DialogProcessor {
	
	fun input(text: String): Response
	
}