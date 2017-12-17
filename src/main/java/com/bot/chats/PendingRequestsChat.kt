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
import com.bot.repo.CustomerRepo
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
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
				/home for main menu
				/approve this
				/decline this
				Or set your own credit limit (12345.67)
			"""
			}, {
				when (it) { // TODO Change credit limit for customer
					"/cancel"  -> return@setNextChatFunction getChat()
					"/approve" -> {
						select.status = Status.APPROVED.value
						select.customer.creditLimit = select.amount
					}
					"/decline" -> {
						select.status = Status.DECLINED.value
					}
					"/home"    -> {
					}
					else       -> {
						select.status = Status.MODIFIED.value + "@$it"
						select.customer.creditLimit = it.toDouble()
					}
				}
				return@setNextChatFunction BaseChats.hello(user)
			})
			.setOnCompleteAction {
				customerRepo.save(select.customer)
				if (select is CreditObtainRequest) {
					creditObtainsRepo.save(select as CreditObtainRequest)
				} else {
					creditIncreaseRepo.save(select as CreditIncreaseRequest)
				}
			}
			.setOnCompleteMessage(Response {
				"Request ${select.id} (${select.customer.fullName} for $${select.amount}) " +
					"is now ${select.status}"
			})
		
	}
}