package com.bot.entity.requests

import com.bot.entity.Customer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDateTime
import java.util.*

class CreditIncreaseRequest(@Id override var id: String = UUID.randomUUID().toString().substring(0..10),
                            @DBRef override var customer: Customer,
                            override var creator: String,
                            override var approver: String? = null,
                            override var amount: Double = 0.0) : CreditRequest {
	
	override var opened: LocalDateTime = LocalDateTime.now()
	override var closed: LocalDateTime? = null
	
	override var type: String = "Credit increase"
	override var status: String = "Pending"
	override var comment: String = ""
	
}