package com.bot.future

import com.bot.entity.*
import com.bot.repo.UserRepo
import com.bot.util.BlockingQueue
import com.nikichxp.util.Async.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.HashMap

@Service
class AltChatProcessor {
	
	@Autowired
	lateinit var userRepo: UserRepo
	
	final val chatList = HashMap<String, ChatState>()
	final val inbox = BlockingQueue<Message>()
	
	init {
		async {
			while (true) {
				val message = inbox.poll()
				async {
					chatList.getOrPut(message.senderId, {
						val user = userRepo.findById(message.senderId).orElseGet { userRepo.save(User(message.senderId)) }
						ChatState(user)
					}).inbox(message)
				}
			}
		}
	}
	
	fun inbox(message: Message) {
		inbox.add(message)
	}
	
	
}