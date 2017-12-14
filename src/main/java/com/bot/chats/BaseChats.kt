package com.bot.chats

import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.State
import com.bot.entity.User
import com.bot.logic.TextResolver

object BaseChats {
	
	fun hello(user: User): ChatBuilder =
		ChatBuilder()
			.setNextChatFunction(Response(null)
				.withViewData(TextResolver.getStateText(State.HELLO))
				.withCustomKeyboard(TextResolver.mainMenu), {
				return@setNextChatFunction when (it) {
					TextResolver.getText("create_customer")  -> CreateCustomerChat(user).getChat()
					TextResolver.getText("create_request")   -> CreateRequestChat(user).getChat()
					TextResolver.getText("my_requests")      -> MyRequestsChat(user).getChat()
					TextResolver.getText("pending_requests") -> PendingRequestsChat(user).getChat()
					"2"                                      -> chat2(user)
					"3"                                      -> chat3(user)
					else                                     -> hello(user)
				}
			})
	
	fun chat2(user: User): ChatBuilder =
		ChatBuilder()
			.then(Response(null).withText("text2-1")
				.withCustomKeyboard(arrayOf<String>()), { println(it) })
			.then("text2-2", { println(it) })
			.then("text2-3", { println(it) })
			.then("text2-4", { println(it) })
			.setNextChatFunction("Num of chat next?", {
				return@setNextChatFunction when {
					it.contains("1") -> hello(user)
					it.contains("2") -> chat2(user)
					it.contains("3") -> chat3(user)
					else             -> hello(user)
				}
			})
	
	fun chat3(user: User): ChatBuilder =
		ChatBuilder()
			.then("text3-1", { println(it) })
			.then("text3-2", { println(it) })
			.then("text3-3", { println(it) })
			.then("text3-4", { println(it) })
			.setNextChatFunction("Num of chat next?", {
				return@setNextChatFunction when {
					it.contains("1") -> hello(user)
					it.contains("2") -> chat2(user)
					it.contains("3") -> chat3(user)
					else             -> hello(user)
				}
			})
	
	
}