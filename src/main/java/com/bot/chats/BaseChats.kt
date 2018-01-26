package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.TextResolver
import com.bot.logic.TextResolver.getText
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
				.withCustomKeyboard(TextResolver.mainMenu.slice(0 until user.accessLevel).toTypedArray()), {
				return@setNextChatFunction when {
					it == getText("createCustomer")                          -> CreateCustomerChat(user).getChat()
					it == getText("createRequest")                           -> CreateRequestChat(user).getChat()
					it == getText("myRequests")                              -> MyRequestsChat(user).getChat()
					it == getText("pendingRequests") && user.accessLevel > 1 -> PendingRequestsChat(user).getChat()
					it == getText("manageUsers") && user.accessLevel > 2     -> ManageUsersChat(user).getChat()
					it == getText("pendingUsers") && user.accessLevel > 2    -> ManageUsersChat(user).getChat()
					it.contains("test")                                      -> test(user)
					else                                                     -> hello(user)
				}
			})
	
	fun test(user: User): ChatBuilder =
		ChatBuilder(user)
			.setOnCompleteAction {
				gSheetsAPI.writeToTable(gSheetsAPI.sheetId,
					"Test", gSheetsAPI.getFirstFreeLine(gSheetsAPI.sheetId, "Test"),
					arrayOf("A", "B", "C", "D"))
			}
	
}