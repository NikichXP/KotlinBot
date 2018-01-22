package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.UserRepo
import com.bot.tgapi.Method

class RegisterChat(var user: User) {
	
	val userRepo = Ctx.get(UserRepo::class.java)
	
	fun getChat(): ChatBuilder = ChatBuilder(user).name("register_hello")
		.setNextChatFunction(Response(user, "register.hello").withCustomKeyboard(arrayOf("Start")), {
			return@setNextChatFunction if (it != "Start") {
				getChat()
			} else getName()
		})
	
	fun getName(): ChatBuilder = ChatBuilder(user).name("register_name")
		.setNextChatFunction("register.fullname", {
			if (it.split(" ").size < 2) {
				Method.sendMessage(Response(user, "register.fullname.error.twoWordsMinRequirement"))
				return@setNextChatFunction getChat()
			} else {
				user.fullName = it
				userRepo.save(user)
				return@setNextChatFunction getMail()
			}
		})
	
	fun getMail(): ChatBuilder = ChatBuilder(user).name("register_mail")
		.setNextChatFunction("register.email", {
			if (!it.contains("@")) {
				Method.sendMessage(Response(user, "register.email.error.check"))
				return@setNextChatFunction getMail()
			} else {
				user.email = it
				userRepo.save(user)
				return@setNextChatFunction getApprove()
			}
		})
	
	fun getApprove(): ChatBuilder = ChatBuilder(user).name("register_check")
		.setNextChatFunction(Response(user, "Wait for admin to approve you").withCustomKeyboard(arrayOf("Check")), {
			user = userRepo.findById(user.id).get()
			
			if (user.type != User.Companion.Type.NONAME) {
				return@setNextChatFunction BaseChats.hello(user)
			}
			return@setNextChatFunction getApprove()
		})
}