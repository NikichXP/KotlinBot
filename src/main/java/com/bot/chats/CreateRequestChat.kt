package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.TextResolver
import com.bot.repo.CreditObtainsRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.ChatDialog
import java.time.LocalDateTime

class CreateRequestChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainsRepo::class.java)
	private lateinit var creditObtain: CreditObtains
	private lateinit var customer: Customer
	
	fun getChat(): ChatDialog {
		return ChatDialog()
			.setNextChatFunction("1 to create credit widthdraw, 2 for credit limit increase",
				{
					return@setNextChatFunction when {
						it.contains("1") -> getCreditWidthDrawChat()
						else             -> getCreditLimitIncreaseChat()
					}
				})
		
	}
	
	fun getCreditWidthDrawChat() = ChatDialog()
		.then(TextResolver.getText("requestCreate.enterCustomerName"), {
			creditObtain = CreditObtains()
			creditObtain.customer = customerRepo.findByFullNameLike(it).orElseGet {
				customerRepo.findById(it).orElseGet { null }
			}
		})
		.then("Enter load amount", {
			creditObtain.amount = it.toDouble()
		})
		.then("When is the pickup? Use 2015-10-30 10:30 as format", {
			creditObtain.pickupDate = LocalDateTime.parse(it)
		})
		.then("BSO or other type?", {
			creditObtain.bso = it.contains("bso", true)
		})
		.setOnCompleteAction { creditObtainsRepo.save(creditObtain) }
	
	
	fun getCreditLimitIncreaseChat() = ChatDialog()
		.then(TextResolver.getText("requestCreate.enterCustomerName"), {
			TODO("Increase credit limit request")
		})
}

/*

TODO Что делать дальше:
1. Придумать что то вроде checkError-функции, или возможности для передачи в качестве аргумента функции проверки, которая
будет возвращать бульку и прописать в чате onErrorFunction/onErrorText


 */
