package com.bot.entity

import org.springframework.data.annotation.Id
import java.util.*
import kotlin.collections.ArrayList

class Customer(@Id var id: String = UUID.randomUUID().toString().replace("-", "").substring(0, 10),
               var accountId: String? = null,
               fullName: String,
               var agent: String,
               var created: Long = System.currentTimeMillis(),
               var updated: Long = System.currentTimeMillis(),
               var creditLimit: Double = 0.0,
               var documents: ArrayList<String> = ArrayList(),
               var address: String = "Nowhere",
               var info: String? = null,
               var contactData: String? = null
) {
	
	constructor(id: String, fullName: String, agent: String) : this(fullName = fullName, agent = agent) {
		this.id = id
	}
	
	var fullName: String = ""
		set(name) {
			field = name
			fullNameLowerCase = name.toLowerCase()
		}
	var fullNameLowerCase: String = fullName.toLowerCase()
		get() {
			return fullName.toLowerCase()
		}
	
	init {
		this.fullName = fullName
	}
}