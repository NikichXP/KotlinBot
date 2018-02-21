package com.bot.entity.requests

import com.bot.entity.Customer
import com.bot.entity.User
import com.bot.logic.TextResolver
import com.bot.repo.UserFactory
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
	var optionalComment: String?
	var documents: MutableList<String>
	
	fun getText() =
		"""Type: ${this.type}
		
		Customer: ${this.customer.fullName}, ID: ${this.customer.id}
		Created by agent (id): ${this.customer.agent} // ${UserFactory[this.customer.agent].fullName}
		Current credit limit: ${this.customer.creditLimit}
		Address: ${this.customer.address}
		Info: ${this.customer.info}
		Amount: ${DecimalFormat("#,###.##").format(this.amount)}
		Creator: ${this.creator} // ${UserFactory[this.creator].fullName}
		Opened: ${this.opened}
		${if (closed != null) ("Closed: " + closed + "\nTime opened: " + this.closed!!.getTimeDiff(this.opened)) else "Not closed"}
		Status: ${TextResolver.getText(this.status.toLowerCase())}
		""".trimMargin()
	
	fun approve(user: User, amount: Double = this.amount) {
		this.status = Status.APPROVED.value
		this.amount = amount
		this.approver = user.id
		this.closed = LocalDateTime.now()
	}
	
	fun creatorName(): String = UserFactory[creator].fullName ?: "Noname"
	fun openedFor(truncated: Boolean = false): String =
		if (truncated) LocalDateTime.now().getTimeDiff(this.opened, showSeconds = false)
			.replace(" days", "d").replace("h ", ":").replace("m", "")
			.takeIf { it != "0:0" } ?: "<1m"
		else LocalDateTime.now().getTimeDiff(this.opened)
	
}