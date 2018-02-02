package com.bot.repo

import com.bot.Ctx
import com.bot.entity.Message
import com.bot.entity.User
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

object UserFactory {
	
	private lateinit var userRepo: UserRepo
	private val userMap = ConcurrentHashMap<String, User>()
	
	init {
		launch {
			userRepo = Ctx[UserRepo::class.java]
		}
	}
	
	operator fun get(message: Message): User = userMap.getOrPut(message.senderId, {
		userRepo.findById(message.senderId).orElseGet {
			userRepo.save(User(message))
		}
	})
	
	operator fun get(id: String): User = userMap.getOrPut(id, {
		userRepo.findById(id)
			.orElseThrow { IllegalArgumentException("Unknown user") }
	})
	
	fun save(id: String) {
		if (userMap[id] != null) userRepo.save(userMap[id]!!)
	}
	
	fun findAll(): MutableList<User> {
		userRepo.findAll().forEach {
			userMap.putIfAbsent(it.id, it)
		}
		return userMap.values.toMutableList()
	}
	
	fun stream(): Stream<User> = userMap.values.stream()
	fun forceCheck(id: String): User {
		this.userMap.put(id, userRepo.findById(id).get())
		return userMap[id]!!
	}
	
	fun findByType(startType: User.Companion.Type): List<User> = findAll().filter { it.type == startType }
	
}
