package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CustomerRepo
import com.bot.util.GSheetsAPI
import com.nikichxp.util.Async.async
import java.time.LocalDate

class CreateCustomerChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val sheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private lateinit var customer: Customer
	
	fun getChat() = ChatBuilder(user.id).name("createCustomerChat")
		.then(Response { "Enter client name" }, {
			customer = Customer(fullName = it, agent = user.id)
		})
		.then("Enter client address", {
			customer.address = it
		})
		.then("Enter client contact info", {
			customer.info = it
		})
		.setEachStepFunction { async { customerRepo.save(customer) } }
		.setOnCompleteAction {
			sheetsAPI.writeToTable("default", "CustomerRequests", -1,
				LocalDate.now().toString(), customer.id, customer.fullName, customer.address, customer.agent,
				customer.contactData ?: "", customer.info ?: "")
		}
		.setNextChatFunction(Response("Creation complete. Continue home or create request",
			arrayOf("Home", "Request")),
			{
				return@setNextChatFunction when (it) {
					"Request" -> CreateRequestChat(user, customer).getAction()
					else      -> BaseChats.hello(user)
				}
			})
	
}
