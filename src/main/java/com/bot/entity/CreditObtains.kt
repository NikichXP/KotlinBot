package com.bot.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDateTime
import java.util.*

data class CreditObtains(@Id val id: String = UUID.randomUUID().toString().substring(0..10),
                         @DBRef var customer: Customer? = null,
                         var amount: Double? = 0.0,
                         var pickupDate: LocalDateTime? = null,
                         var bso: Boolean? = null) {
	
	constructor(map: Map<String, Any>): this() {
		this.customer = map["customer"]!! as Customer
		this.pickupDate = map["pickupDate"]!! as LocalDateTime
		this.bso = map["bso"]!! as Boolean
		this.amount = map["amount"]!! as Double
	}
}