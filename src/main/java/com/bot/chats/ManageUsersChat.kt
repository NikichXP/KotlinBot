package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.repo.UserRepo
import com.bot.util.*
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.toList

class ManageUsersChat(val user: User) {
	
	lateinit var userRepo: UserRepo
	lateinit var userList: List<User>
	
	init {
		launch {
			userRepo = Ctx.get(UserRepo::class.java)
		}
	}
	
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
		userList = userRepo.findAll()
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
		// fuck optimization. I'm fabulous
		val users = userRepo.findAll().stream()
			.filter { it.fullName?.contains(string, true) ?: false }
			.skip(skip.toLong()).limit(10).toList()
		return ChatBuilder(user).setNextChatFunction({ getTextByUsers(users, skip) }, {
			if (it.isSelection()) {
				return@setNextChatFunction userSelection(users[it.selection()!! - 1])
			}
			return@setNextChatFunction getChat()
		})
	}
	
	fun userSelection(user: User): ChatBuilder {
		return ChatBuilder(user).name("manageUser.selected")
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
					}
					"/broker" -> {
						user.type = User.Companion.Type.BROKER
					}
					"/admin"  -> {
						user.type = User.Companion.Type.ADMIN
					}
					"/noname" -> {
						user.type = User.Companion.Type.NONAME
					}
				}
				userRepo.save(user)
				return@setNextChatFunction if (it == "/back") {
					getChat()
				} else {
					BaseChats.hello(user)
				}
			})
	}
}
