package com.bot.entity.requests

import com.bot.entity.Customer

interface CreditRequest {
	
	var customer: Customer
	var amount: Double
	
}