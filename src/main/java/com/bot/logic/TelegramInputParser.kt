package com.bot.logic

import com.bot.entity.Message
import com.bot.entity.User
import com.bot.logic.dialogprocessor.AdminDialogProcessor
import com.bot.logic.dialogprocessor.BrokerDialogProcessor
import com.bot.logic.dialogprocessor.ClientDialogProcessor
import com.bot.logic.dialogprocessor.DialogProcessor
import com.bot.tgapi.Method
import com.bot.repo.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TelegramInputParser {
	
	@Autowired
	lateinit var userRepo: UserRepo
	
	val chatProcessors = ConcurrentHashMap<String, DialogProcessor>()
	
	fun input(inputJson: String) {
		try {
			val message = Message(inputJson)
			val chatProcessor = chatProcessors.getOrPut(message.senderId, {
				val user = userRepo.findById(message.senderId).orElseGet { userRepo.save(User(message.senderId)) }
				@Suppress("REDUNDANT_ELSE_IN_WHEN")
				return@getOrPut when (user.type) {
					User.Companion.Type.CLIENT -> ClientDialogProcessor(user)
					User.Companion.Type.BROKER -> BrokerDialogProcessor(user)
					User.Companion.Type.ADMIN  -> AdminDialogProcessor(user)
					else                       -> throw IllegalArgumentException("User ${user.id} type unexpected.")
				}
			})
			Method.sendMessage(chatProcessor.input(message.text))
		} catch (e: NullPointerException) {
			Method.sendMessage("34080460", "error on parse: $inputJson")
		}
		
	}
	
}
