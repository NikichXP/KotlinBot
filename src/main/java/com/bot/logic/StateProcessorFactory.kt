package com.bot.logic

import com.bot.entity.State
import com.bot.entity.User
import com.bot.logic.stateprocessors.CreateCustomerStateProcessor
import com.bot.logic.stateprocessors.HelloStateProcessor

object StateProcessorFactory {
	
	fun getByState(state: State, user: User): StateProcessor {
		return when (state) {
			State.CREATE_CUSTOMER -> CreateCustomerStateProcessor(user)
			else                  -> HelloStateProcessor(user)
		}
	}
}