package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.TextResolver
import com.bot.util.GSheetsAPI
import com.nikichxp.util.Async.async

object BaseChats {
	
	lateinit var gSheetsAPI: GSheetsAPI
	
	init {
		async {
			gSheetsAPI = Ctx.get(GSheetsAPI::class.java)
		}
	}
	
	fun hello(user: User): ChatBuilder =
		if (user.accessLevel == 0 || user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat()
		else ChatBuilder(user).name("hello")
			.setNextChatFunction(Response(user)
				.withViewData(TextResolver.getText("home"))
				.withCustomKeyboard(TextResolver.mainMenu), {
				return@setNextChatFunction when (it) {
					TextResolver.getText("createCustomer")  -> CreateCustomerChat(user).getChat()
					TextResolver.getText("createRequest")   -> CreateRequestChat(user).getChat()
					TextResolver.getText("myRequests")      -> MyRequestsChat(user).getChat()
					TextResolver.getText("pendingRequests") -> PendingRequestsChat(user).getChat()
					TextResolver.getText("manageUsers")     -> ManageUsersChat(user).getChat()
					TextResolver.getText("pendingUsers")    -> ManageUsersChat(user).getChat()
					"test", "/test"                         -> test(user)
					"2"                                     -> chat2(user)
					"3"                                     -> chat3(user)
					else                                    -> hello(user)
				}
			})
	
	fun test(user: User): ChatBuilder =
		ChatBuilder(user)
			.setOnCompleteAction {
				gSheetsAPI.writeToTable(gSheetsAPI.sheetId,
					"Test", gSheetsAPI.getFirstFreeLine(gSheetsAPI.sheetId, "Test"),
					arrayOf("A", "B", "C", "D"))
			}
	
	fun chat2(user: User): ChatBuilder =
		ChatBuilder(user)
			.then(Response(user).withText("text2-1")
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
		ChatBuilder(user)
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