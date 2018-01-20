package com.bot.entity.requests

import com.bot.Ctx
import com.bot.entity.Customer
import com.bot.repo.UserRepo
import java.text.DecimalFormat

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
		
		Customer: ${this.customer.fullName}
		Created by agent (id): ${this.customer.agent} // ${Ctx.get(UserRepo::class.java)
			.findById(this.customer.agent).map { it.fullName }.orElse("Who is it?")}
		Current credit limit: ${this.customer.creditLimit}
		Address: ${this.customer.address}
		Info: ${this.customer.info}
		Amount: ${DecimalFormat("#,###.##").format(this.amount)}
		Creator: ${this.creator} // ${Ctx.get(UserRepo::class.java)
			.findById(this.creator).map { it.fullName }.orElse("Who is it?")}
		Status: ${this.status}
		""".trimMargin()
}