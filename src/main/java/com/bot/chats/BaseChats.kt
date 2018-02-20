package com.bot.chats

import com.bot.Ctx
import com.bot.entity.TextChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.TextResolver
import com.bot.logic.TextResolver.getText
import com.bot.tgapi.Method
import com.bot.util.GSheetsAPI
import com.nikichxp.util.Async.async

object BaseChats {
	
	lateinit var gSheetsAPI: GSheetsAPI
	
	init {
		async {
			gSheetsAPI = Ctx.get(GSheetsAPI::class.java)
		}
	}
	
	fun hello(user: User): TextChatBuilder =
		if (user.accessLevel == 0 || user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat()
		else TextChatBuilder(user).name("hello")
			.setNextChatFunction(Response(user)
				.withViewData(TextResolver.getText("home"))
				.withCustomKeyboard(TextResolver.mainMenu.slice(0 until user.accessLevel).toTypedArray()), {
				return@setNextChatFunction when {
					it == getText("createCustomer")                          -> CreateCustomerChat(user).getChat()
					it == getText("createRequest")                           -> CreateRequestChat(user).getChat()
					it == getText("myRequests")                              -> MyRequestsChat(user).getChat()
					it == getText("pendingRequests") && user.accessLevel > 1 -> PendingRequestsChat(user).getChat()
					it == getText("manageUsers") && user.accessLevel > 2     -> ManageUsersChat(user).getChat()
					it == getText("pendingUsers") && user.accessLevel > 2    -> ManageUsersChat(user).getChat(User.Companion.Type.NONAME)
					it.contains("test")                                      -> test(user)
					it.contains("help")                                      -> getHelp(user)
					else                                                     -> hello(user)
				}
			})
	
	fun getHelp(user: User) = TextChatBuilder(user)
		.setNextChatFunction {
			Method.sendMessage(user.id, "Complete guide is located here: http://telegra.ph/Tekst-02-19-2")
			hello(user)
		}
	
	fun test(user: User): TextChatBuilder {
		val list = (-1..5).toList()
		return ListChat<Int>(user, list).selectFunction {
			println("Selected: $it")
			return@selectFunction BaseChats.hello(user)
		}.printFunction { "Num ${it}" }.getChat()
	}
	
	
}