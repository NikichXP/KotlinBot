package com.bot.logic.stateprocessors

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.DialogProcessor
import com.bot.logic.TextResolver
import com.bot.repo.CreditObtainsRepo
import com.bot.repo.CustomerRepo
import com.bot.util.QuestionChat
import java.time.LocalDateTime

class CreateRequestStateProcessor(override val user: User, dialogProcessor: DialogProcessor) : StateProcessor {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainsRepo::class.java)
	private var context = HashMap<String, Any>()
	override val state = State.CREATE_REQUEST
	
	override fun input(text: String): ResponseBlock {
		when (text[0]) {
			'1'  -> {
				val questions = QuestionChat().then(TextResolver.getText("requestCreate.enterCustomerName"), {
					context["customer"] = customerRepo.findByFullNameLike(text).orElseGet {
						customerRepo.findById(text).orElseGet(null)
					}
				}).then("Enter load amount", {
					context["amount"] = it.toDouble()
				}).then("When is the pickup? Use 2015-10-30 10:30 as format", {
					context["pickupDate"] = LocalDateTime.parse(it)
				}).then("BSO or other type?", {
					context["bso"] = it.contains("bso", true)
				}).afterAll {
					creditObtainsRepo.save(CreditObtains(context))
				}
				
				QuestionableStateProcessor(user = user, parentStateProcessor = this, questions = questions)
				
				//				ResponseBlock(Response(user, TextResolver.getText("requestCreate.enterCustomerName")), this.state)
			}
			'2'  -> {
				//					phase = "credit release"
				ResponseBlock(Response(user, TextResolver.getText("requestCreate.enterCustomerName")), this.state)
			}
			'3'  -> return ResponseBlock(Response(user, TextResolver.getText("cancelled")), State.HELLO)
			else -> return ResponseBlock(Response(user, TextResolver.getText("cancelled")), State.HELLO)
		}
		TODO("not implemented")
	}
}
	
