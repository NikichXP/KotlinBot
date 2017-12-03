package com.bot.repo

import com.bot.entity.Customer
import org.springframework.data.mongodb.repository.MongoRepository

interface CustomerRepo: MongoRepository<Customer, String> {
}