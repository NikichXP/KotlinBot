package com.bot.chats

import com.bot.entity.*
import com.bot.logic.Notifier
import com.bot.repo.UserFactory
import com.bot.tgapi.Method

class RegisterChat(override var user: User) : ChatParent(user) {
	
	fun getChat(): ChatBuilder = ChatBuilder(this.user).name("register_hello")
		.setNextChatFunction(Response(this.user, "register.hello").withCustomKeyboard("Start"), {
			return@setNextChatFunction if (it != "Start") {
				getChat()
			} else {
				when {
					this.user.fullName == null -> getName()
					this.user.email == null    -> getMail()
					else                       -> getApprove()
				}
			}
		})
	
	fun getName(): ChatBuilder = ChatBuilder(this.user).name("register_name")
		.setNextChatFunction("register.fullname", {
			if (it.split(" ").size < 2) {
				sendMessage("register.fullname.error.twoWordsMinRequirement")
				return@setNextChatFunction getChat()
			} else {
				this.user.fullName = it
				UserFactory.save(this.user.id)
				return@setNextChatFunction getMail()
			}
		})
	
	fun getMail(): ChatBuilder = ChatBuilder(this.user).name("register_mail")
		.setNextChatFunction("register.email", {
			if (!it.contains("@")) {
				sendMessage("register.email.error.check")
				return@setNextChatFunction getMail()
			} else {
				this.user.email = it
				UserFactory.save(this.user.id)
				return@setNextChatFunction getApprove()
			}
		})
	
	fun getApprove(): ChatBuilder = ChatBuilder(this.user).name("register_check")
		.setNextChatFunction(Response(this.user, "Wait for admin to approve you. Your name and email:\n" +
			"${this.user.fullName}\n${this.user.email}\nYou can edit this data or submit your appliance")
			.withCustomKeyboard("Edit name", "Edit mail", "Submit/Check"), {
			when (it) {
				"Edit name" -> return@setNextChatFunction getName()
				"Edit mail" -> return@setNextChatFunction getMail()
			}
			this.user = UserFactory.forceCheck(this.user.id)
			this.user.isSubmitted = true
			UserFactory.save(this.user)
			
			sendMessage("Submitted")
			Notifier.notifyOnRegister(this.user)
			
			if (this.user.type != User.Companion.Type.NONAME) {
				return@setNextChatFunction BaseChats.hello(this.user)
			}
			return@setNextChatFunction getApprove()
		})
}