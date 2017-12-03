package com.bot.logic.stateprocessors

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.StateProcessor
import com.bot.logic.TextResolver
import com.bot.repo.CustomerRepo
import com.nikichxp.util.Async.async

class CreateCustomerStateProcessor(override val user: User) : StateProcessor {
	
	override val state: State = State.CREATE_CUSTOMER
	var stage = 0
	
	val customerRepo = Ctx.get(CustomerRepo::class.java)
	
	lateinit var customer: Customer
	
	override fun input(text: String): ResponseBlock {
		return when (stage) {
			0    -> {
				customer = Customer(fullName = text, agent = user.id)
				stage++
				async {
					customerRepo.save(customer)
				}
				ResponseBlock(Response(user, TextResolver.getText("customerCreate.address")), State.CREATE_CUSTOMER)
			}
			1    -> {
				customer.address = text
				stage++
				async{
					customerRepo.save(customer)
				}
				ResponseBlock(Response(user, TextResolver.getText("customerCreate.info")), State.CREATE_CUSTOMER)
			}
			2    -> {
				customer.info = text
				async{
					customerRepo.save(customer)
				}
				ResponseBlock(Response(user, TextResolver.getText("customerCreate.complete")), State.HELLO)
			}
			else -> throw IllegalArgumentException("")
		}
	}
	
}