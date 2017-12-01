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
                    var created: Long,
                    var updated: Long,
                    var creditLimit: Double,
                    var documents: ArrayList<String>,
                    var comment: String,
                    var contactData: String,
                    var growth: Long,
                    var releaseId: String
                    )  {
}