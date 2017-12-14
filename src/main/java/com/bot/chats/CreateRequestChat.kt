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

class CreateRequestChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private lateinit var creditObtainRequest: CreditObtainRequest
	private lateinit var creditIncreaseRequest: CreditIncreaseRequest
	private lateinit var customer: Customer
	
	fun getChat(): ChatBuilder {
		return ChatBuilder()
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
		
	}
	
	fun getCreditWidthDrawChat() = ChatBuilder()
		.then(TextResolver.getText("requestCreate.enterCustomerName"), {
			customer = customerRepo.findByFullNameLike(it).orElseGet {
				customerRepo.findById(it).orElseGet { null }
			}
			creditObtainRequest = CreditObtainRequest(customer = customer, creator = user.id)
		})
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
		.then(TextResolver.getText("requestCreate.enterCustomerName"), {
			customer = customerRepo.findByFullNameLike(it).orElseGet {
				customerRepo.findById(it).orElseGet { null }
			}
			creditIncreaseRequest = CreditIncreaseRequest(customer = customer, creator = user.id)
		})
		.then("Enter amount, $", { creditIncreaseRequest.amount = it.toDouble() })
		.setOnCompleteAction { creditIncreaseRepo.save(creditIncreaseRequest) }
}

/*

TODO Что делать дальше:
1. Придумать что то вроде checkError-функции, или возможности для передачи в качестве аргумента функции проверки, которая
будет возвращать бульку и прописать в чате onErrorFunction/onErrorText


 */
