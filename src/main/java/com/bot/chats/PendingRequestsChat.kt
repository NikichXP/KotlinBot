package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.entity.requests.CreditRequest
import com.bot.entity.requests.Status
import com.bot.logic.Notifier
import com.bot.logic.TextResolver
import com.bot.repo.CreditIncreaseRepo
import com.bot.repo.CreditObtainRepo
import com.bot.repo.CustomerRepo
import com.bot.tgapi.Method
import com.bot.util.GSheetsAPI
import kotlinx.coroutines.experimental.launch
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicInteger

class PendingRequestsChat(user: User) : ChatParent(user) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val gSheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private var requestList = mutableListOf<CreditRequest>()
	//	private val workingLock = ConcurrentSkipListSet<String>() // TODO Fix concurrent issues
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		requestList.also {
			it.addAll(creditObtainsRepo.findByStatus(Status.PENDING.value))
			it.addAll(creditIncreaseRepo.findByStatus(Status.PENDING.value))
		}
		return ListChat(user, requestList)
			.printFunction {
				"${it.customer.fullName} (${it.customer.accountId
					?: "Pending"}) :: ${it.type} :: ${DecimalFormat("#,###.##").format(it.amount)}," +
					" ${it.openedFor(true)}"
			}
			.also { it.headText = "Select request" }
			.selectFunction { viewRequest(it) }
			.getChat()
	}
	
	private fun viewRequest(request: CreditRequest): ChatBuilder {
		select = request
		return ChatBuilder(user)
			.setNextChatFunction(
				Response { select.getText() + TextResolver.getText("pendingRequest.actionSelect") }
					.withCustomKeyboard("❌ Cancel", "\uD83C\uDFBE Approve", "\uD83D\uDD34 Decline", "\uD83C\uDFE0 Home"),
				{
					when (it.split(" ").last().toLowerCase()) {
						"cancel"  -> return@setNextChatFunction getChat()
						"approve" -> {
							return@setNextChatFunction if (request is CreditObtainRequest) approveRelease() else approveCreditLimit()
						}
						"decline" -> {
							select.status = Status.DECLINED.value
							select.approver = user.id
							Notifier.notifyOnUpdate(select)
							if (request is CreditObtainRequest) creditObtainsRepo.save(request) else creditIncreaseRepo.save(request as CreditIncreaseRequest)
							launch {
								gSheetsAPI.updateCellsWhere(page = "Requests", criteria = { it[0] == request.id }, updateFx = {
									it[10] = request.status
									return@updateCellsWhere it
								})
							}
						}
						"home"    -> {
						}
						else      -> {
							sendMessage("pendingRequest.error.unknownAction")
							return@setNextChatFunction getChat()
						}
					}
					return@setNextChatFunction BaseChats.hello(user)
				}
			)
	}
	
	private fun approveCreditLimit(): ChatBuilder {
		val oldAmount = select.amount
		val ret = ChatBuilder(user).name("pendingRequests_approveCreditLimit")
			.setNextChatFunction("pendingRequest.enterLimitAmount", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select.approve(user, it.toDouble())
					creditIncreaseRepo.save(select as CreditIncreaseRequest)
					launch {
						gSheetsAPI.updateCellsWhere(page = "Requests", criteria = { it[0] == select.id }, updateFx = {
							it[5] = if (select.type == "New customer") "Set:${select.customer.accountId
								?: "Failed to set ID?"}"
							else select.customer.accountId ?: "Failed to get ID"
							it[10] = select.status
							it[16] = "$" + DecimalFormat("#,###.##").format(select.amount - select.customer.creditLimit)
							it[14] = "$" + DecimalFormat("#,###.##").format(oldAmount)
							it[15] = "$" + DecimalFormat("#,###.##").format(select.amount)
							return@updateCellsWhere it
						})
					}
					val customer = select.customer
					customer.creditLimit = select.amount // TODO Migrate from here
					customerRepo.save(customer)
					Notifier.notifyOnUpdate(select)
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
		if (select.type == "New customer") {
			return ChatBuilder(user).name("pendingRequests_approveClient")
				.setNextChatFunction("Enter customer account ID", {
					select.customer.accountId = it
					customerRepo.save(select.customer)
					return@setNextChatFunction ret
				})
		}
		return ret
	}
	
	private fun approveRelease(): ChatBuilder {
		val select = this.select as CreditObtainRequest
		return ChatBuilder(user).name("pendingRequests_release")
			.setNextChatFunction("pendingRequest.enterReleaseID", {
				if (it.contains("cancel")) {
					return@setNextChatFunction getChat()
				} else {
					select.releaseId = it
					select.approve(user)
					creditObtainsRepo.save(select)
					launch {
						gSheetsAPI.updateCellsWhere(page = "Requests", criteria = { it[0] == select.id }, updateFx = {
							it[7] = "$" + DecimalFormat("#,###.##").format(select.amount)
							it[10] = select.status
							it[13] = select.releaseId
							return@updateCellsWhere it
						})
					}
					Notifier.notifyOnUpdate(select)
					return@setNextChatFunction BaseChats.hello(user)
				}
			})
	}
}