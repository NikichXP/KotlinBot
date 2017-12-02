package com.bot.logic

import com.bot.entity.Response
import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User

interface StateProcessor {
	
	val state: State
	val user: User
	
	fun input(text: String): ResponseBlock
	
}