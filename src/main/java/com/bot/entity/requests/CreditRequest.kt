package com.bot.entity.requests

import com.bot.entity.Customer

interface CreditRequest {
	
	val type: String
	var customer: Customer
	var amount: Double
	var creator: String
	var status: String
	
}