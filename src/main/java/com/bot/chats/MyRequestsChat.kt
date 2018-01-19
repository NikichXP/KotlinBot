package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditRequest
import com.bot.entity.requests.Status
import com.bot.repo.CreditIncreaseRepo
import com.bot.repo.CreditObtainRepo
import java.util.concurrent.atomic.AtomicInteger

class MyRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private lateinit var requestList: MutableList<CreditRequest>
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		val int = AtomicInteger(0)
		return ChatBuilder(user).name("myRequests_home")
			.setNextChatFunction(Response {
				requestList = mutableListOf()
				requestList.addAll(creditObtainsRepo.findByCreator(user.id))
				requestList.addAll(creditIncreaseRepo.findByCreator(user.id))
				
				"Select your request or back /home to menu\n\n" +
					requestList.stream().map {
						"/${int.incrementAndGet()} -- ${it.customer.fullName} ::" +
							" ${it.type} :: ${it.amount} :: ${it.status}"
					}
						.reduce { a, b -> "$a\n$b" }.orElse("None of your requests found")
			}, {
				return@setNextChatFunction when {
					it == "/home"            -> BaseChats.hello(user)
					it.startsWith("/")       -> {
						select = requestList[it.substring(1).toInt() - 1]
						modifyRequest()
					}
					it.matches(Regex("\\d")) -> {
						select = requestList[it.toInt() - 1]
						modifyRequest()
					}
					else                     -> BaseChats.hello(user)
				}
			}) // -1 cause starts counting with 1
		
	}
	
	fun modifyRequest(): ChatBuilder {
		return ChatBuilder(user).name("myRequests_modifyRequest")
			.setNextChatFunction(Response {
				select.getText() + "\n\n /back to all orders\n/home to main menu\n" +
					"/decline request (you can decline approved)"
			}, {
				return@setNextChatFunction when (it) {
					"/back"    -> this.getChat()
					"/decline" -> {
						if (select.status != Status.APPROVED.value) {
							creditIncreaseRepo.deleteById(select.id)
							creditObtainsRepo.deleteById(select.id)
						}
						this.getChat()
					}
					else       -> BaseChats.hello(user)
				}
			})
	}
}