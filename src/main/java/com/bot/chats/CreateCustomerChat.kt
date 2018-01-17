package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CustomerRepo
import com.bot.tgapi.Method
import com.bot.util.GSheetsAPI
import com.nikichxp.util.Async.async
import kotlinx.coroutines.experimental.launch
import java.time.LocalDate

class CreateCustomerChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val sheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private lateinit var customer: Customer
	
	fun getChat() = ChatBuilder(user.id).name("createCustomer_intro")
		.setNextChatFunction(Response("Create user or import existing?")
			.withCustomKeyboard(arrayOf("Create user", "Import user")),
			{
				return@setNextChatFunction if (it == "Create user") {
					createUser()
				} else {
					ChatBuilder()
				}
			}
		)
	
	private fun createUser(): ChatBuilder {
		var limit = 0.0
		return ChatBuilder(user.id).name("createCustomerChat")
			.then(Response { "Enter client name" }, {
				customer = Customer(fullName = it, agent = user.id)
			})
			.then("Enter client address",
				{
					customer.address = it
				})
			.then("Enter client contact info",
				{
					customer.info = it
				})
			.then("Enter requesting credit limit (or 0 to skip this)", {
				try {
					limit = it.toDouble()
				} catch (e: Exception) {
					Method.sendMessage(user.id, "Parsing failed, limit set to 0.0, request making status: drop")
				}
			})
			.setOnCompleteAction {
				customerRepo.save(customer)
				launch {
					sheetsAPI.writeToTable("default", "CustomerRequests", -1, arrayOf(
						LocalDate.now().toString(), customer.id, customer.fullName, customer.address, customer.agent,
						customer.contactData ?: "", customer.info ?: ""))
				}
				if (limit > 0) {
					launch {
						val result = CreateRequestChat(user).createLimitEntry(customer, "Created user", limit)
						Method.sendMessage(user.id, "Limit increase request created successfully, " +
							"customer id: ${customer.id}, request id: ${result.id}")
					}
				}
			}
			.setNextChatFunction(Response("Creation complete. Your user id is: ${customer.id}.\n" +
				"Continue home or create another request",
				arrayOf("Home", "Request")),
				{
					return@setNextChatFunction when (it) {
						"Request" -> CreateRequestChat(user, customer).getAction()
						else      -> BaseChats.hello(user)
					}
				})
	}
	
}
