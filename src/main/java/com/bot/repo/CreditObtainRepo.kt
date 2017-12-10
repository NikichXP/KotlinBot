package com.bot.repo

import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import org.springframework.data.mongodb.repository.MongoRepository

interface CreditObtainRepo : MongoRepository<CreditObtainRequest, String> {
	fun findByCreator(creator: String): List<CreditObtainRequest>
}

interface CreditIncreaseRepo : MongoRepository<CreditIncreaseRequest, String> {
	fun findByCreator(creator: String): List<CreditIncreaseRequest>
}