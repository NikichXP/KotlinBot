package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.entity.requests.CreditRequest
import com.bot.entity.requests.Status
import com.bot.repo.CreditIncreaseRepo
import com.bot.repo.CreditObtainRepo
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private var requestList = mutableListOf<CreditRequest>()
	//	private val workingLock = ConcurrentSkipListSet<String>() // TODO Fix concurrent issues
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		val int = AtomicInteger(0)
		return ChatBuilder()
			.then(Response {
				requestList.addAll(creditObtainsRepo.findByStatus(Status.PENDING.value))
				requestList.addAll(creditIncreaseRepo.findByStatus(Status.PENDING.value))
				requestList.stream().map {
					"/${int.incrementAndGet()} -- ${it.customer.fullName} ::" +
						" ${it.type} :: ${it.amount}"
				}
					.reduce { a, b -> "$a\n$b" }.orElse("Nothing found")
			}, {
				select = requestList[it.substring(1).toInt() - 1]
			})
			.setNextChatFunction(Response {
				select.getText() + """
				/cancel editing
				/approve this
				/decline this
			"""
			}, {
				when (it) {
					"/cancel"  -> getChat()
					"/approve" -> {
						select.status = Status.APPROVED.value
						return@setNextChatFunction BaseChats.hello(user)
					}
					"/decline" -> {
						select.status = Status.DECLINED.value
						return@setNextChatFunction BaseChats.hello(user)
					}
					else       -> BaseChats.hello(user)
				}
			})
			.setOnCompleteAction {
				if (select is CreditObtainRequest) {
					creditObtainsRepo.save(select as CreditObtainRequest)
				} else {
					creditIncreaseRepo.save(select as CreditIncreaseRequest)
				}
			}
			.setOnCompleteMessage(Response{"Request ${select.id} (${select.customer.fullName} for $${select.amount}) " +
				"is now ${select.status}"})
		
	}
}