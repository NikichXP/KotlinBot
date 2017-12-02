package com.bot.logic.stateprocessors

import com.bot.entity.Response
import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User
import com.bot.logic.StateProcessor

class CreateCustomerStateProcessor(override val user: User) : StateProcessor {
	
	override val state: State = State.CREATE_CUSTOMER
	var stage = 0
	
	override fun input(text: String): ResponseBlock {
		return ResponseBlock(Response(user, "TEXT HERE"), State.HELLO)
	}
	
}