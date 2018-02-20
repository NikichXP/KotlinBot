package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.TextResolver
import com.bot.repo.CustomerRepo
import com.bot.util.GSheetsAPI
import kotlinx.coroutines.experimental.launch
import java.time.LocalDate

class CreateCustomerChat(user: User) : ChatParent(user) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val sheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private var customer: Customer = Customer(fullName = "", agent = user.id)
	
	fun getChat(): TextChatBuilder = TextChatBuilder(user).name("createCustomer_intro")
		.setNextChatFunction(
			Response(user.id, "customerCreate.hello")
				.withCustomKeyboard("Create New Customer", "Add Existing Customer"),
			{
				return@setNextChatFunction when (it) {
					"Create New Customer"   -> createUser()
					"Add Existing Customer" -> importUser()
					else                    -> {
						sendMessage("Unexpected chat type response")
						getChat()
					}
				}
			}
		)
	
	private fun importUser(): TextChatBuilder {
		var fullname: String = ""
		return TextChatBuilder(user).name("createCustomer_import")
			.then("customerCreate.import.name", { fullname = it })
			.then("customerCreate.import.id", {
				customer = Customer(id = it, fullName = fullname, agent = user.id)
				customer.accountId = it
			})
			.then("customerCreate.import.address", { if (it != "/skip") customer.address = it })
			.then("customerCreate.import.info", { customer.info = it })
			.setOnCompleteAction { customerRepo.save(customer) }
			.setOnCompleteMessage("Customer ${customer.fullName} with ID ${customer.id} created")
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
	
	private fun createUser(): TextChatBuilder {
		var limit = 0.0
		return TextChatBuilder(user).name("createCustomer_new")
			.then(Response { "customerCreate.create.name" }, { customer = Customer(fullName = it, agent = user.id) })
			.then("customerCreate.create.address", { customer.address = it })
			.then("customerCreate.create.info", { customer.info = it })
			.then("customerCreate.create.creditLimit.request", {
				try {
					limit = it.toDouble()
				} catch (e: Exception) {
					sendMessage("customerCreate.create.creditLimit.request.error.parse")
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
						val result = CreateRequestChat(user).createLimitEntry(customer, "New customer", "Created user", limit)
						sendMessage("Limit increase request created successfully, " +
							"customer id: ${customer.id}, request id: ${result.id}")
					}
				}
			}
			.setNextChatFunction(Response(TextResolver.getText("customerCreate.create.complete.1", false) + customer.id + "\n" +
				TextResolver.getText("customerCreate.create.complete.2", false),
				arrayOf("Home", "Request")),
				{
					return@setNextChatFunction when (it) {
						"Request" -> CreateRequestChat(user, customer).getAction()
						else      -> BaseChats.hello(user)
					}
				})
	}
	
}
