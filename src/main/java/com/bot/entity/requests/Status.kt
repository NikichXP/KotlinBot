package com.bot.entity.requests

enum class Status(val value: String) {
	
	PENDING("Pending"), DECLINED("Declined"), APPROVED("Approved"),
	CANCELLED("Cancelled")
	
}