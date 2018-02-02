package com.bot.chats

import com.bot.Ctx
import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditRequest
import com.bot.entity.requests.Status
import com.bot.logic.TextResolver.getText
import com.bot.repo.CreditIncreaseRepo
import com.bot.repo.CreditObtainRepo
import java.util.concurrent.atomic.AtomicInteger

class MyRequestsChat(user: User) : ChatParent(user) {
	
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private lateinit var requestList: MutableList<CreditRequest>
	private lateinit var select: CreditRequest
	
	fun getChat(): ChatBuilder {
		val int = AtomicInteger(0)
		
		requestList = mutableListOf()
		requestList.addAll(creditObtainsRepo.findByCreator(user.id))
		requestList.addAll(creditIncreaseRepo.findByCreator(user.id))
		requestList.sortWith(Comparator { e1, e2 -> e1.opened.compareTo(e2.opened) })
		
		return ListChat(user, requestList).also {
			it.headText = getText("myRequests.selectRequest") + "\n\n"
			it.printFunction {
				"${it.customer.fullName} :: ${it.type} :: ${it.amount} :: ${it.status}"
			}
			it.selectFunction {
				select = it
				return@selectFunction modifyRequest()
			}
			it.addCustomButton("On/off approved", {
				if (it.customFlags["hidden"] == null) {
					it.customFlags["hidden"] = true
					it.reset(requestList.filter { it.status == Status.PENDING.value })
				} else {
					it.customFlags.remove("hidden")
					it.reset(requestList)
				}
			})
		}.getChat()
	}
	
	fun modifyRequest(): ChatBuilder {
		return ChatBuilder(user).name("myRequests_modifyRequest")
			.setNextChatFunction(Response {
				select.getText() + "\n\n" + getText("myRequests.view.requestActions")
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