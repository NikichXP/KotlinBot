package com.bot.logic.stateprocessors

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.TextResolver
import com.bot.repo.CustomerRepo

class CreateRequestStateProcessor(override val user: User) : StateProcessor {
	
	private var phase = "start"
	private var customer: Customer? = null
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	override val state = State.CREATE_REQUEST
	
	override fun input(text: String): ResponseBlock {
	
	
		
//		return when (phase) {
//			"start" -> {
//				when (text[0]) {
//					'1'  -> {
//						phase = "increase limit"
//						ResponseBlock(Response(user, TextResolver.getText("requestCreate.enterCustomerName")), this.state)
//					}
//					'2' -> {
//						phase = "credit release"
//						ResponseBlock(Response(user, TextResolver.getText("requestCreate.enterCustomerName")), this.state)
//					}
//					'3'  -> ResponseBlock(Response(user, TextResolver.getText("cancelled")), State.HELLO)
//					else -> ResponseBlock(Response(user, TextResolver.getText("cancelled")), State.HELLO)
//				}
//			}
//			"increase limit" -> {
//				customer = customerRepo.findByFullNameLike(text).orElseGet {
//					customerRepo.findById(text).orElseGet(null)
//				}
//				if (customer == null) {
//					ResponseBlock(Response(user, TextResolver.getText("notFound")), State.HELLO)
//				}
//				ResponseBlock(Response(user, TextResolver.getText("cancelled")), State.HELLO)
//			}
//			else    -> {
//			}
//		}
		
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}