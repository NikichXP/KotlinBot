package com.bot.future

import com.bot.entity.Response

class Chat2Builder {

	var actions = ArrayList<Action>()
	var eachStepAction: (() -> Unit)? = null
	
	fun message(text: Response) {
		actions.add(Action(Type.MESSAGE))
	}
	
}

class Action (type: Type) {



}

enum class Type {
	MESSAGE, QUESTION, BRANCH
}