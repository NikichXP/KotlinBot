package com.bot.entity.requests

import com.bot.entity.Customer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class CreditObtainRequest(@Id override val id: String = UUID.randomUUID().toString().substring(0..10),
                               @DBRef override var customer: Customer,
                               override var creator: String,
                               override var amount: Double = 0.0,
                               override var approver: String? = null,
                               var pickupDate: LocalDate? = null,
                               var bco: Boolean? = null) : CreditRequest {
	
	override var opened: LocalDateTime = LocalDateTime.now()
	override var closed: LocalDateTime? = null
	
	var fb = ""
	var releaseId: String = ""
	override val type: String = "Credit obtain"
	override var status: String = "Pending"
	override var comment: String = ""
	
}