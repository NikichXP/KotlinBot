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
import com.bot.tgapi.Method
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private lateinit var requestList: MutableList<CreditRequest>
	//	private val workingLock = ConcurrentSkipListSet<String>() // TODO Fix concurrent issues
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		val int = AtomicInteger(0)
		return ChatBuilder()
			.then(Response {
				requestList = mutableListOf()
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
						return@setNextChatFunction if (select is CreditObtainRequest) approveRelease() else approveCreditLimit()
					}
					"/decline" -> {
						select.status = Status.DECLINED.value
					}
					"/home"    -> {
					}
					else       -> {
						Method.sendMessage(user.id, "Unknown action")
						return@setNextChatFunction getChat()
					}
				}
				return@setNextChatFunction BaseChats.hello(user)
			})
			.setOnCompleteMessage(Response {
				"Request ${select.id} (${select.customer.fullName} for $${select.amount}) " +
					"is now ${select.status}"
			})
	}
	
	fun approveCreditLimit(): ChatBuilder {
		val oldAmount = select.amount
		return ChatBuilder()
			.then("Enter amount", { select.amount = it.toDouble() })
			.setNextChatFunction("Enter Release ID or /cancel", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select.status = Status.APPROVED.value
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
			.setOnCompleteAction {
				// TODO Update client, and write new info to table
			}
	}
	
	fun approveRelease(): ChatBuilder {
		val select_ = select as CreditObtainRequest
		return ChatBuilder()
			.setNextChatFunction("Enter Release ID or /cancel", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select_.releaseID = it
					select_.status = Status.APPROVED.value
					creditObtainsRepo.save(select_)
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
	}
}