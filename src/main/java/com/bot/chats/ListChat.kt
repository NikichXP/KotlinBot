package com.bot.chats

import com.bot.entity.ChatBuilder
import com.bot.entity.Response
import com.bot.entity.User
import java.util.concurrent.atomic.AtomicInteger

class ListChat<T>(user: User) : ChatParent(user) {
	
	var list: MutableList<T> = mutableListOf()
	var pageSize: Int = 10
	var fixedPageSize: Int = 10
	var printFx: (T) -> String = { it.toString() }
	var customButtonsMap = HashMap<String, ((ListChat<T>) -> Unit)>()
	var customChatButtons = HashMap<String, ChatBuilder>()
	var backChat: ChatBuilder = BaseChats.hello(user)
	var selectFunction: (T) -> ChatBuilder = { BaseChats.hello(user) }
	var headText = ""
	var tailText = ""
	var customFlags = HashMap<String, Any>()
	var elseFunction: (String) -> ChatBuilder = { getChat() }
	
	constructor(user: User, list: List<T>) : this(user) {
		this.list = list.toMutableList()
	}
	
	fun add(miniList: List<T>) {
		this.list.addAll(miniList)
	}
	
	fun addCustomButton(name: String, action: (ListChat<T>) -> Unit) = also { this.customButtonsMap[name] = action }
	fun addCustomChatButton(name: String, chat: ChatBuilder) = also { this.customChatButtons[name] = chat }
	fun pageSize(pageSize: Int) = also {
		this.pageSize = pageSize
		this.fixedPageSize = pageSize
	}
	
	fun printFunction(printFunction: (T) -> String) = also { this.printFx = printFunction }
	fun selectFunction(chat: (T) -> ChatBuilder) = also { this.selectFunction = chat }
	fun backChat(chat: ChatBuilder) = also { this.backChat = chat }
	fun elseFunction(inputHandler: (String) -> ChatBuilder) = also { this.elseFunction = inputHandler }
	fun reset(list: List<T>) = also {
		this.list = list.toMutableList()
		this.pageSize = fixedPageSize
	}
	
	fun getChat(skip: Int = 0): ChatBuilder {
		val i = AtomicInteger(1)
		pageSize = Math.min(pageSize, list.size)
		return ChatBuilder(user)
			.setNextChatFunction(
				Response {
					"[${skip + 1} - ${skip + pageSize}] / ${list.size}\n" + headText + "\n" +
						list.stream().skip(skip.toLong()).limit(pageSize.toLong())
							.map { i.getAndIncrement().toString() + " - " + printFx.invoke(it) }
							.reduce { a, b -> a + "\n" + b }
							.orElse("Empty list") + "\n$tailText"
				}
					.withCustomKeyboard(arraySelection(list.drop(skip).take(pageSize).count()))
				, {
				if (customButtonsMap.containsKey(it)) {
					customButtonsMap[it]!!.invoke(this)
					return@setNextChatFunction getChat(0)
				}
				return@setNextChatFunction when (it) {
					"<<", "«" -> getChat(Math.max(0, skip - pageSize))
					">>", "»" -> getChat(Math.min(skip + pageSize, list.size - pageSize))
					"Home"    -> BaseChats.hello(user)
					"Back"    -> backChat
					else      -> when {
						it.toIntOrNull() != null      -> {
							if (it.toInt() + skip - 1 < list.size) {
								selectFunction.invoke(list[it.toInt() + skip - 1])
							} else {
								elseFunction.invoke(it)
							}
						}
						customChatButtons[it] != null -> customChatButtons[it]!!
						else                          -> elseFunction.invoke(it)
					}
				}
			})
	}
	
	private fun arraySelection(count: Int): Array<Array<String>> {
		val line1 = (1..count).map { it.toString() }.toTypedArray()
		if ((customButtonsMap.size + customChatButtons.size) < 5) {
			val line2 = arrayOf("<<", "Home", "Back") + customButtonsMap.keys.toTypedArray() +
				customChatButtons.keys.toTypedArray() + arrayOf(">>")
			return arrayOf(line1, line2)
		} else {
			val line2 = arrayOf("<<", "Home", "Back", ">>")
			val line3 = customButtonsMap.keys.toTypedArray() + customChatButtons.keys.toTypedArray()
			return arrayOf(line1, line2, line3)
		}
	}
	
}