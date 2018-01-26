package com.bot.logic

import com.bot.entity.User
import com.bot.entity.requests.CreditObtainRequest
import com.bot.entity.requests.CreditRequest
import com.bot.repo.UserFactory
import com.bot.tgapi.Method
import kotlinx.coroutines.experimental.launch
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
	
	}
	
	fun notifyOnUpdate(request: CreditRequest) {
		when (request) {
			is CreditObtainRequest -> {
				Method.sendMessage(request.creator, "Your release request (ID #${request.id}) is now: \"${request.status}\"\n" +
					"Release ID: ${request.releaseId}.\nEditor: ${userName(request.approver)}")
				if (request.approver != null) {
					Method.sendMessage(request.approver!!, "You updated release request (ID #${request.id}), is now: \"${request.status}\"\n" +
						"Release ID: ${request.releaseId}.\nCreator: ${userName(request.creator)}")
				}
			}
			else                   -> {
				Method.sendMessage(request.creator, "Your request: ${request.type} (ID #${request.id}) is now: \"${request.status}\"\n" +
					"Amount: ${request.amount}.\nEditor: ${userName(request.approver)}")
				if (request.approver != null) {
					Method.sendMessage(request.approver!!, "You updated request: ${request.type} (ID #${request.id}), is now: \"${request.status}\"\n" +
						"Amount: ${request.amount}.\nCreator: ${userName(request.creator)}")
				}
			}
		}
	}
	
	fun notifyOnPermissions(user: User) {
		Method.sendMessage(user.id, "Your permission level is now ${user.accessLevel} and it is ${user.type.name}")
	}
	
}
