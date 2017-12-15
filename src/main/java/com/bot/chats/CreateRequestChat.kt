package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.TextResolver
import com.bot.repo.CreditObtainRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.ChatBuilder
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.repo.CreditIncreaseRepo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger

class CreateRequestChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private lateinit var creditObtainRequest: CreditObtainRequest
	private lateinit var creditIncreaseRequest: CreditIncreaseRequest
	private var customer: Customer? = null
	private lateinit var customerList: MutableList<Customer>
	
	constructor(user: User, customer: Customer) : this(user) {
		this.customer = customer
	}
	
	fun getChat(): ChatBuilder {
		return ChatBuilder()
			.setNextChatFunction("Enter client name or ID", {
				customerList = customerRepo.findByFullNameLike(it)
				customerRepo.findById(it).ifPresent { customerList.add(it) }
				return@setNextChatFunction chooseClient()
			})
	}
	
	fun chooseClient(): ChatBuilder = ChatBuilder()
		.setNextChatFunction(Response {
			val num = AtomicInteger(0)
			
			return@Response "Choose client or enter new search query or /cancel or /create:\n" + customerList.stream()
				.map { "/${num.getAndIncrement()} ${it.fullName} " }
				.reduce { a, b -> "$a\n$b" }.orElse("Empty result. Try again.")
		}, {
			if (it.startsWith("/")) {
				try {
					customer = customerList[it.substring(1).toInt()]
				} catch (e: Exception) {
					return@setNextChatFunction when (it) {
						"/cancel" -> BaseChats.hello(user)
						"/create" -> CreateCustomerChat(user).getChat()
						else      -> chooseClient()
					}
				}
				return@setNextChatFunction getAction()
			} else {
				customerList = customerRepo.findByFullNameLike(it)
				customerRepo.findById(it).ifPresent { customerList.add(it) }
				return@setNextChatFunction chooseClient()
			}
		})
	
	
	fun getAction() = ChatBuilder()
		.setNextChatFunction(Response(user.id, "1 to create credit widthdraw, 2 for credit limit increase")
			.withInlineKeyboard(arrayOf("Credit widthdraw", "Limit increase", "Cancel")),
			{
				return@setNextChatFunction when {
					it.contains("1")         -> getCreditWidthDrawChat()
					it == "Credit widthdraw" -> getCreditWidthDrawChat()
					it == "Cancel"           -> BaseChats.hello(user)
					else                     -> getCreditLimitIncreaseChat()
				}
			})
	
	fun getCreditWidthDrawChat() = ChatBuilder()
		.then("Enter load amount", {
			creditObtainRequest.amount = it.toDouble()
		})
		.then("When is the pickup? Use 2015-10-30 10:30 as format", {
			if (!it.contains('T') && it.contains(' ')) {
				creditObtainRequest.pickupDate = LocalDateTime.of(
					LocalDate.parse(it.split(" ")[0]),
					LocalTime.parse(it.split(" ")[1])
				)
			} else {
				creditObtainRequest.pickupDate = LocalDateTime.parse(it)
			}
		})
		.then("BSO or other type?", {
			creditObtainRequest.bso = it.contains("bso", true)
		})
		.setOnCompleteAction { creditObtainsRepo.save(creditObtainRequest) }
	
	
	fun getCreditLimitIncreaseChat() = ChatBuilder()
		.then("Enter amount, $", { creditIncreaseRequest.amount = it.toDouble() })
		.setOnCompleteAction { creditIncreaseRepo.save(creditIncreaseRequest) }
}

/*

TODO Что делать дальше:
1. Придумать что то вроде checkError-функции, или возможности для передачи в качестве аргумента функции проверки, которая
будет возвращать бульку и прописать в чате onErrorFunction/onErrorText


 */
