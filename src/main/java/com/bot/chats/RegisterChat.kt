package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.UserRepo

class RegisterChat(var user: User) {
	
	val userRepo = Ctx.get(UserRepo::class.java)
	
	fun getChat(): ChatBuilder = ChatBuilder()
		.setNextChatFunction("Enter your full name", {
			if (it.split(" ").size < 2) {
				return@setNextChatFunction getChat()
			} else {
				user.fullName = it
				userRepo.save(user)
				return@setNextChatFunction getMail()
			}
		})
	
	fun getMail(): ChatBuilder = ChatBuilder()
		.setNextChatFunction("Enter email", {
			if (!it.contains("@")) {
				return@setNextChatFunction getMail()
			} else {
				user.email = it
				userRepo.save(user)
				return@setNextChatFunction getApprove()
			}
		})
	
	fun getApprove(): ChatBuilder = ChatBuilder()
		.setNextChatFunction("Wait for admin to approve you", {
			user = userRepo.findById(user.id).get()
			
			if (user.type != User.Companion.Type.WAIT) {
				return@setNextChatFunction BaseChats.hello(user)
			}
			return@setNextChatFunction getApprove()
		})
}