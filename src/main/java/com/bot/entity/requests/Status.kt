package com.bot.entity.requests

enum class Status(val value: String) {
	
	PENDING("Pending"), DECLINED("Declined"), APPROVED("Approved"),
	MODIFIED("Modified"), CANCELLED("Cancelled")
	
}