package com.bot.chats

import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.Notifier
import com.bot.repo.UserFactory
import com.bot.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.toList

class ManageUsersChat(user: User): ChatParent(user) {
	
	lateinit var userList: List<User>
	
	fun getChat(): ChatBuilder {
		return ChatBuilder(user).name("manageUsers.select")
			.setNextChatFunction("Choose action:\n/all users\nOr enter name (or part) to find user", {
				if (it == "/all") {
					return@setNextChatFunction allUsersList(0)
				} else {
					return@setNextChatFunction findUsersBy(it)
				}
			})
	}
	
	fun allUsersList(skip: Int): ChatBuilder {
		userList = UserFactory.findAll()
		val userSubList = userList.drop(skip).take(10)
		return ChatBuilder(user)
			.setNextChatFunction(Response {
				getTextByUsers(userSubList, skip)
			}, {
				return@setNextChatFunction when {
					it.startsWith("/search") -> findUsersBy(it.substring(7).trim())
					it == "/prev"            -> allUsersList(Math.max(skip - 10, 0))
					it == "/next"            -> allUsersList(skip + 10)
					it.isSelection("/")      -> userSelection(userList[it.selection()!! - 1])
					else                     -> getChat()
				}
			})
	}
	
	private fun getTextByUsers(users: List<User>, skip: Int = 0): String {
		val i = AtomicInteger(0)
		return "Choose user or use /prev <=> /next page or \"/search Vladislav\" to search within full name. Skipping users now: $skip\n" +
			"Users: ${users.stream().map { "/${i.incrementAndGet()} ${it.fullName}" }.reduce { a, b -> "$a\n$b" }.orElse("End of list.")}"
	}
	
	private fun findUsersBy(string: String, skip: Int = 0): ChatBuilder {
		val users = UserFactory.stream()
			.filter { it.fullName?.contains(string, true) ?: false }
			.skip(skip.toLong()).limit(10).toList()
		return ChatBuilder(user).setNextChatFunction({ getTextByUsers(users, skip) }, {
			if (it.isSelection()) {
				return@setNextChatFunction userSelection(UserFactory[users[it.selection()!! - 1].id])
			}
			return@setNextChatFunction getChat()
		})
	}
	
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
			}, {
				when (it) {
					"/credit" -> {
						user.type = User.Companion.Type.CREDIT
						user.accessLevel = 2
					}
					"/broker" -> {
						user.type = User.Companion.Type.BROKER
						user.accessLevel = 1
					}
					"/admin"  -> {
						user.type = User.Companion.Type.ADMIN
						user.accessLevel = 3
					}
					"/noname" -> {
						user.type = User.Companion.Type.NONAME
						user.accessLevel = 0
					}
				}
				UserFactory.save(user.id)
				Notifier.updateUserNamesMapCache()
				Notifier.notifyOnPermissions(user)
				return@setNextChatFunction if (it == "/back") {
					getChat()
				} else {
					BaseChats.hello(this.user)
				}
			})
	}
}
