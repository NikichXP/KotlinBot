package com.bot.util

import java.util.*
import java.util.concurrent.Semaphore


class BlockingQueue<T> {
	private val queue = LinkedList<T>()
	private val lock = Semaphore(0)
	
	fun peek(): T {
		lock.acquire()
		val ret = queue.peek()
		lock.release()
		return ret
	}
	
	fun poll(): T {
		lock.acquire()
		return queue.poll()
	}
	
	fun add(element: T) {
		queue.add(element)
		lock.release()
	}
}