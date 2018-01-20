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
import com.bot.util.GSheetsAPI
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestsChat(val user: User) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val gSheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private lateinit var requestList: MutableList<CreditRequest>
	//	private val workingLock = ConcurrentSkipListSet<String>() // TODO Fix concurrent issues
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		val int = AtomicInteger(0)
		return ChatBuilder(user).name("pendingRequests_home")
			.then(Response {
				requestList = mutableListOf()
				requestList.addAll(creditObtainsRepo.findByStatus(Status.PENDING.value))
				requestList.addAll(creditIncreaseRepo.findByStatus(Status.PENDING.value))
				requestList.stream().map {
					"/${int.incrementAndGet()} -- ${it.customer.fullName} ::" +
						" ${it.type} :: ${DecimalFormat("#,###.##").format(it.amount)}"
				}
					.reduce { a, b -> "$a\n$b" }.orElse("Nothing found")
			}, {
				select = requestList[it.replace("/", "").toInt() - 1]
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
				
				fun getCorrespondingChat(request: CreditRequest): ChatBuilder = if (request is CreditObtainRequest) approveRelease() else approveCreditLimit()
				
				when (it) { // TODO Change credit limit for customer
					"/cancel"  -> return@setNextChatFunction getChat()
					"/approve" -> {
						return@setNextChatFunction getCorrespondingChat(select)
					}
					"/decline" -> {
						select.status = Status.DECLINED.value
					}
					"/home"    -> {
					}
					else       -> {
						if (it.toDoubleOrNull() != null) {
							select.amount = it.toDouble()
							return@setNextChatFunction getCorrespondingChat(select)
						}
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
		return ChatBuilder(user).name("pendingRequests_approveCreditLimit")
			.setNextChatFunction("Enter amount or /cancel", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select.amount = it.toDouble()
					select.status = Status.APPROVED.value
					creditIncreaseRepo.save(select as CreditIncreaseRequest)
					gSheetsAPI.updateCellsWhere(page = select.type, criteria = { it[0] == select.id }, updateFx = {
						it[7] = select.status
						it[12] = "$" + DecimalFormat("#,###.##").format(select.amount - select.customer.creditLimit)
						it[13] = "$" + DecimalFormat("#,###.##").format(oldAmount)
						it[14] = "$" + DecimalFormat("#,###.##").format(select.amount)
						return@updateCellsWhere it
					})
					val customer = select.customer
					customer.creditLimit = select.amount // TODO Migrate from here
					customerRepo.save(customer)
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
	}
	
	fun approveRelease(): ChatBuilder {
		val select = this.select as CreditObtainRequest
		return ChatBuilder(user).name("pendingRequests_release")
			.setNextChatFunction("Enter Release ID or /cancel", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select.releaseId = it
					select.status = Status.APPROVED.value
					creditObtainsRepo.save(select)
					gSheetsAPI.updateCellsWhere(page = "Requests", criteria = { it[0] == select.id }, updateFx = {
						it[6] = DecimalFormat("#,###.##").format(select.amount)
						it[8] = select.status
						it[12] = select.releaseId
						return@updateCellsWhere it
					})
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
	}
}