package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CustomerRepo
import com.nikichxp.util.Async.async

class CreateCustomerChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private lateinit var customer: Customer
	
	fun getChat() = QuestionChat(user.id)
		.then("Enter full name", {
			customer = Customer(fullName = it, agent = user.id)
		})
		.then("Enter client address", {
			customer.address = it
		})
		.then("Enter client info", {
			customer.info = it
		})
		.setEachStepFunction { async { customerRepo.save(customer) } }
		.setNextChatFunction(Response(null).withText("Creation complete. Any action to continue.")
			.withCustomKeyboard(arrayOf("Home")), { BaseChats.hello(user) })
}
