package com.bot.logic

import com.bot.chats.CreateCustomerChat
import com.bot.entity.State
import com.bot.entity.User
import com.bot.logic.stateprocessors.*

object StateProcessorFactory {
	
//	fun getByState(state: State, user: User, chatProcessor2: ChatProcessor): StateProcessor {
//		return when (state) {
//			State.CREATE_CUSTOMER -> CreateCustomerChat(user)
//			State.CREATE_REQUEST  -> CreateRequestChat(user, chatProcessor2)
//			State.MY_REQUESTS     -> MyRequestsStateProcessor(user)
//			else                  -> HelloStateProcessor(user)
//		}
//	}
}