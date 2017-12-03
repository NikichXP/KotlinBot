package com.bot.logic.stateprocessors

import com.bot.entity.Response
import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User
import com.bot.logic.TextResolver

class HelloStateProcessor(override val user: User) : StateProcessor {
	
	override val state: State = State.HELLO
	
	override fun input(text: String): ResponseBlock {
		val ret = ResponseBlock(Response(user), TextResolver.getResultStateByText(text))
		ret.response = ret.response.withViewData(TextResolver.getStateText(ret.state))
		return ret
	}
}