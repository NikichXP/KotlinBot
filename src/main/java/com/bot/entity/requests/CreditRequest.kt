package com.bot.entity.requests

import com.bot.entity.Customer

interface CreditRequest {
	
	val id: String
	val type: String
	var customer: Customer
	var amount: Double
	var creator: String
	var approver: String?
	var status: String
	var comment: String
	
	fun getText() =
		"""Type: ${this.type}
		
		Customer:
		Full name: ${this.customer.fullName}
		Created by agent (id): ${this.customer.agent}
		Current credit limit: ${this.customer.creditLimit}
		Address: ${this.customer.address}
		Info: ${this.customer.info}
		Amount: ${this.amount}
		Creator: ${this.creator}
		Status: ${this.status}
		""".trimMargin()
}