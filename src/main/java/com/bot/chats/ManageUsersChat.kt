package com.bot.chats

import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.Notifier
import com.bot.repo.UserFactory
import com.bot.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.toList

class ManageUsersChat(user: User) : ChatParent(user) {
		
	fun getChat(startType: User.Companion.Type? = null) = ListChat(user, if (startType != null) UserFactory.findByType(startType) else UserFactory.findAll())
		.also { it.headText = "Select user to perform actions" }
		.printFunction { "${it.fullName} -- ${it.type.name}" }
		.addCustomButton("All", { it.reset(UserFactory.findAll()) })
		.addCustomButton("Pending", { it.reset(UserFactory.findByType(User.Companion.Type.NONAME)) })
		.addCustomButton("Broker", { it.reset(UserFactory.findByType(User.Companion.Type.BROKER)) })
		.addCustomButton("Credit", { it.reset(UserFactory.findByType(User.Companion.Type.CREDIT)) })
		.addCustomButton("Admin", { it.reset(UserFactory.findByType(User.Companion.Type.ADMIN)) })
		.selectFunction { userSelection(it) }
		.getChat()
	
	fun userSelection(user: User): ChatBuilder {
		return ChatBuilder(this.user).name("manageUser.selected")
			.setNextChatFunction(Response {
				"""
				User ${user.fullName}
				Status: ${user.type}
				You can make this user:
				/credit /broker /admin or /noname
				You can go /back or /home
			""".trimIndent()
			}.withCustomKeyboard(arrayOf(arrayOf("Noname", "Credit", "Broker", "Admin"), arrayOf("Home", "Back"))), {
				when (it.filter { it != '/' }.toLowerCase()) {
					"credit" -> {
						user.type = User.Companion.Type.CREDIT
						user.accessLevel = 2
					}
					"broker" -> {
						user.type = User.Companion.Type.BROKER
						user.accessLevel = 1
					}
					"admin"  -> {
						user.type = User.Companion.Type.ADMIN
						user.accessLevel = 3
					}
					"noname" -> {
						user.type = User.Companion.Type.NONAME
						user.accessLevel = 0
					}
				}
				UserFactory.save(user.id)
				Notifier.updateUserNamesMapCache()
				Notifier.notifyOnPermissions(user)
				return@setNextChatFunction if (it == "/back" || it == "Back") {
					getChat()
				} else {
					BaseChats.hello(this.user)
				}
			})
	}
}
