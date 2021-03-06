package com.bot.repo

import com.bot.entity.Customer
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface CustomerRepo : MongoRepository<Customer, String> {
	
	fun findByFullNameLike(fullName: String): MutableList<Customer>
	fun findByFullNameLowerCaseLike(fullName: String): MutableList<Customer>
	
}