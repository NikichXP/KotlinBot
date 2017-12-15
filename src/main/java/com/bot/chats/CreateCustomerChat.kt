package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CustomerRepo
import com.nikichxp.util.Async.async

class CreateCustomerChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private lateinit var customer: Customer
	
	fun getChat() = ChatBuilder(user.id)
		.then(Response { "Enter client name" }, {
			customer = Customer(fullName = it, agent = user.id)
		})
		.then("Enter client address", {
			customer.address = it
		})
		.then("Enter client info", {
			customer.info = it
		})
		.setEachStepFunction { async { customerRepo.save(customer) } }
		.setNextChatFunction(Response("Creation complete. Continue home or create request",
			arrayOf("Home", "Request")),
			{
				return@setNextChatFunction when (it) {
					"Request" -> CreateRequestChat(user, customer).getAction()
					else -> BaseChats.hello(user)
				}
			})
	
}
