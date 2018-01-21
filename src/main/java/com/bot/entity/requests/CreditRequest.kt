package com.bot.entity.requests

import com.bot.Ctx
import com.bot.entity.Customer
import com.bot.entity.User
import com.bot.repo.UserRepo
import com.bot.util.getTimeDiff
import java.text.DecimalFormat
import java.time.LocalDateTime

interface CreditRequest {
	
	val id: String
	val type: String
	var customer: Customer
	var amount: Double
	var creator: String
	var approver: String?
	var status: String
	var comment: String
	var opened: LocalDateTime
	var closed: LocalDateTime?
	
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
		Opened: ${this.opened}
		${if (closed != null) ("Closed: " + closed + "\nTime opened: " + this.closed!!.getTimeDiff(this.opened)) else "Not closed"}
		Status: ${this.status}
		""".trimMargin()
	
	fun approve(user: User, amount: Double = this.amount) {
		this.status = Status.APPROVED.value
		this.amount = amount
		this.approver = user.id
		this.closed = LocalDateTime.now()
	}
	
	
}