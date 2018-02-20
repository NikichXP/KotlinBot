package com.bot.logic

import com.bot.entity.Response
import com.bot.entity.User
import com.bot.entity.requests.CreditObtainRequest
import com.bot.entity.requests.CreditRequest
import com.bot.repo.UserFactory
import com.bot.tgapi.Method
import kotlinx.coroutines.experimental.launch
import java.text.DecimalFormat
import java.util.*

object Notifier {
	
	private val userNamesMap = HashMap<String, String>()
	
	init {
		launch {
			updateUserNamesMapCache()
		}
	}
	
	fun updateUserNamesMapCache() {
		userNamesMap.clear()
		UserFactory.findAll().forEach { userNamesMap.put(it.id, it.fullName ?: it.id) }
	}
	
	fun userName(id: String?): String {
		id ?: return "No one"
		if (userNamesMap[id] != null) return userNamesMap[id]!!
		updateUserNamesMapCache()
		return userNamesMap[id] ?: id
	}
	
	fun notifyOnCreate(request: CreditRequest) {
		UserFactory.findAll().filter { it.accessLevel >= 2 && it.id != request.creator }.forEach {
			Method.sendMessage(it.id, "New request: ${request.type};\nCreator: ${request.creatorName()}\n" +
				"Customer: ${request.customer.fullName} (${request.customer.id})\n" +
				"Amount: $${DecimalFormat("#,###.##").format(request.amount)}\n" +
				"Comment: ${request.comment}")
		}
	}
	
	fun notifyOnUpdate(request: CreditRequest) {
		when (request) {
			is CreditObtainRequest -> {
				Method.sendMessage(request.creator, "Your release request (ID #${request.id}) is now: \"${request.status}\"\n" +
					"Company: ${request.customer.fullName} (${request.customer.id})\n" +
					"Amount: ${request.amount}, Editor: ${userName(request.approver)}\nComment: ${request.comment}\n" +
					"Release ID: ${request.releaseId}.\nEditor: ${userName(request.approver)}"
					+ if (request.optionalComment != null) "\nReason:${request.optionalComment}" else "")
				if (request.approver != null) {
					Method.sendMessage(request.approver!!, "You updated release request (ID #${request.id}), is now: \"${request.status}\"\n" +
						"Company: ${request.customer.fullName} (${request.customer.id})\n" +
						"Amount: ${request.amount}, Editor: ${userName(request.approver)}\nComment: ${request.comment}\n" +
						"Release ID: ${request.releaseId}.\nCreator: ${userName(request.creator)}")
				}
			}
			else                   -> {
				Method.sendMessage(request.creator,
					"Your request: ${request.type} (ID #${request.id}) is now: \"${request.status}\"\n" +
						"Company: ${request.customer.fullName} (${request.customer.id})\n" +
						"Amount: ${request.amount}, Editor: ${userName(request.approver)}\nComment: ${request.comment}"
						+ if (request.optionalComment != null) "\nReason:${request.optionalComment}" else "")
				if (request.approver != null) {
					Method.sendMessage(request.approver!!, "You updated request: ${request.type} (ID #${request.id}), is now: \"${request.status}\"\n" +
						"Company: ${request.customer.fullName} (${request.customer.id})\n" +
						"Amount: ${request.amount}, Editor: ${userName(request.approver)}\nComment: ${request.comment}")
				}
			}
		}
	}
	
	fun notifyOnPermissions(user: User) {
		Method.sendMessage(user.id, "Your permission level is now ${user.accessLevel} and it is ${user.type.name}")
	}
	
	fun notifyOnRegister(user: User) {
		if (user.type != User.Companion.Type.NONAME || !user.isSubmitted) {
			return
		}
		UserFactory.findByType(User.Companion.Type.ADMIN).forEach {
			Method.sendMessage(Response { "Registered user: ${user.fullName} - Pending" }.ensureUser(it.id).withNoKeyboardChange())
		}
	}
	
}
