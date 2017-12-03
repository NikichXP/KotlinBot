package com.bot.logic.stateprocessors

import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User

class MyRequestsStateProcessor(override val user: User) : StateProcessor {
	override val state: State
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	
	override fun input(text: String): ResponseBlock {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}