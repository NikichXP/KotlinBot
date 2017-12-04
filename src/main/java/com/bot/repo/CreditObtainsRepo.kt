package com.bot.repo

import com.bot.entity.CreditObtains
import org.springframework.data.mongodb.repository.MongoRepository

interface CreditObtainsRepo : MongoRepository<CreditObtains, String> {
}