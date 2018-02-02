package com.bot.chats

import com.bot.entity.*
import com.bot.repo.UserFactory
import com.bot.tgapi.Method

class RegisterChat(user: User): ChatParent(user) {
	
	fun getChat(): ChatBuilder = ChatBuilder(user).name("register_hello")
		.setNextChatFunction(Response(user, "register.hello").withCustomKeyboard(arrayOf("Start")), {
			return@setNextChatFunction if (it != "Start") {
				getChat()
			} else {
				when {
					user.fullName == null -> getName()
					user.email == null    -> getMail()
					else                  -> getApprove()
				}
			}
		})
	
	fun getName(): ChatBuilder = ChatBuilder(user).name("register_name")
		.setNextChatFunction("register.fullname", {
			if (it.split(" ").size < 2) {
				sendMessage("register.fullname.error.twoWordsMinRequirement")
				return@setNextChatFunction getChat()
			} else {
				user.fullName = it
				UserFactory.save(user.id)
				return@setNextChatFunction getMail()
			}
		})
	
	fun getMail(): ChatBuilder = ChatBuilder(user).name("register_mail")
		.setNextChatFunction("register.email", {
			if (!it.contains("@")) {
				sendMessage("register.email.error.check")
				return@setNextChatFunction getMail()
			} else {
				user.email = it
				UserFactory.save(user.id)
				return@setNextChatFunction getApprove()
			}
		})
	
	fun getApprove(): ChatBuilder = ChatBuilder(user).name("register_check")
		.setNextChatFunction(Response(user, "Wait for admin to approve you. Your name and email:\n" +
			"${user.fullName}\n${user.email}\nYou can edit this data")
			.withCustomKeyboard(
				arrayOf("Edit name", "Edit mail", "Check")
			), {
			when (it) {
				"Edit name" -> return@setNextChatFunction getName()
				"Edit mail" -> return@setNextChatFunction getMail()
			}
			user = UserFactory.forceCheck(user.id)
			
			if (user.type != User.Companion.Type.NONAME) {
				return@setNextChatFunction BaseChats.hello(user)
			}
			return@setNextChatFunction getApprove()
		})
}