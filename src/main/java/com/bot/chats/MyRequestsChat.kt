package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatDialog
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditRequest
import com.bot.repo.CreditIncreaseRepo
import com.bot.repo.CreditObtainRepo
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicInteger

class MyRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private var requestList = mutableListOf<CreditRequest>()
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatDialog {
		val int = AtomicInteger(0)
		return ChatDialog()
			.then(Response {
				requestList.addAll(creditObtainsRepo.findByCreator(user.id))
				requestList.addAll(creditIncreaseRepo.findByCreator(user.id))
				requestList.stream().map {
					"/${int.incrementAndGet()} -- ${it.customer.fullName} ::" +
						" ${it.type} :: ${it.amount}"
				}
					.reduce { a, b -> "$a\n$b" }.orElse("Nothing found")
			}, { select = requestList[it.substring(1).toInt() - 1] }) // -1 cause starts counting with 1
			.setOnCompleteMessage(Response {
				"Type: ${select.type}\n\n" +
					"Customer:\n" +
					"Full name: ${select.customer.fullName}\n" +
					"Created by agent (id): ${select.customer.agent}\n" +
					"Current credit limit: ${select.customer.creditLimit}\n" +
					"Address: ${select.customer.address}\n" +
					"Info: ${select.customer.info}\n\n" +
					"Amount: ${select.amount}\n" +
					"Creator: ${select.creator}\n" +
					"Status: ${select.status}\n"
			})
	}
}