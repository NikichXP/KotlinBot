package com.bot.entity

import org.springframework.data.annotation.Id
import java.util.*
import kotlin.collections.ArrayList

/*
TODO What is growth, releaseId
 */

data class Customer(@Id var id: String = UUID.randomUUID().toString().replace("-", "").substring(0, 10),
                    var fullName: String,
                    var agent: String,
                    var created: Long = System.currentTimeMillis(),
                    var updated: Long = System.currentTimeMillis(),
                    var creditLimit: Double = 0.0,
                    var documents: ArrayList<String> = ArrayList(),
                    var address: String = "Nowhere",
                    var info: String? = null,
                    var contactData: String? = null,
                    var growth: Long = 0,
                    var releaseId: String? = null
                    )  {
	
	
}