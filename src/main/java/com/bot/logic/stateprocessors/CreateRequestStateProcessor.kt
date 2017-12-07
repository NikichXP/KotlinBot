package com.bot.logic.stateprocessors

import com.bot.Ctx
import com.bot.entity.*
import com.bot.logic.ChatProcessor
import com.bot.logic.TextResolver
import com.bot.repo.CreditObtainsRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.QuestionChat
import java.time.LocalDateTime

class CreateRequestStateProcessor(override val user: User, val chatProcessor: ChatProcessor) : StateProcessor {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainsRepo::class.java)
	private var context = HashMap<String, Any>()
	override val state = State.CREATE_REQUEST
	
	override fun input(text: String): ResponseBlock {
		when (text[0]) {
			'1'  -> {
				val questions = QuestionChat().then(TextResolver.getText("requestCreate.enterCustomerName"), {
					context["customer"] = customerRepo.findByFullNameLike(it).orElseGet {
						customerRepo.findById(it).orElseGet { null }
					}
				}).then("Enter load amount", {
					context["amount"] = it.toDouble()
				}).then("When is the pickup? Use 2015-10-30 10:30 as format", {
					context["pickupDate"] = LocalDateTime.parse(it)
				}).then("BSO or other type?", {
					context["bso"] = it.contains("bso", true)
				}).inTheEnd(State.HELLO, {
					creditObtainsRepo.save(CreditObtains(context))
				})
				
				chatProcessor.interceptHandle(QuestionableStateProcessor(user, chatProcessor, questions))
				return ResponseBlock(Response(user, ""), this.state)
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

/*

TODO Что делать дальше:
1. Решить как делать перехват сообщений с этого чатпроцессора на следующий.
2. Сделать этот переход. Здесь не слать ничего (или ""), изменить статус на иной.
3. Сделать в Questionable-СП возможность вернуться на предыдуший СП ИЛИ ЖЕ ЕЩЁ ЛУЧШЕ - перейти в любой из стейтов новых.
4. Может апнуть этот СП для Questionable?)
4.1. Может добавить в те чатики немного вариативности, ветвления а-ля JSON, подветки и так далее. Но это же долго....
5. Делать другие задачи.


 */
