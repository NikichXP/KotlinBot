package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CreditObtainRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.ChatBuilder
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.logic.TextResolver
import com.bot.repo.CreditIncreaseRepo
import com.bot.util.GSheetsAPI
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.atomic.AtomicInteger

class CreateRequestChat(val user: User) {
	
	private val customerRepo = Ctx.get(CustomerRepo::class.java)
	private val creditObtainsRepo = Ctx.get(CreditObtainRepo::class.java)
	private val creditIncreaseRepo = Ctx.get(CreditIncreaseRepo::class.java)
	private val sheetsAPI = Ctx.get(GSheetsAPI::class.java)
	private lateinit var creditObtainRequest: CreditObtainRequest
	private lateinit var creditIncreaseRequest: CreditIncreaseRequest
	private var customer: Customer? = null
	private lateinit var customerList: MutableList<Customer>
	
	constructor(user: User, customer: Customer) : this(user) {
		this.customer = customer
	}
	
	fun getChat(): ChatBuilder {
		return ChatBuilder(user).name("createRequest_home")
			.setNextChatFunction("createRequest.hello", {
				customerList = customerRepo.findByFullNameLowerCaseLike(it.toLowerCase())
				customerRepo.findById(it).ifPresent { customerList.add(it) }
				return@setNextChatFunction chooseClient()
			})
	}
	
	fun chooseClient(): ChatBuilder = ChatBuilder(user).name("createRequest_selectClient")
		.setNextChatFunction(Response {
			val num = AtomicInteger(0)
			
			return@Response TextResolver.getText("createRequest.search.choose") + "\n" + customerList.stream()
				.map { "/${num.getAndIncrement()} ${it.fullName} " }
				.reduce { a, b -> "$a\n$b" }.orElse("createRequest.search.empty")
		}, {
			if (it.startsWith("/")) {
				try {
					customer = customerList[it.replace("/", "").toInt()]
				} catch (e: Exception) {
					return@setNextChatFunction when (it) {
						"/cancel" -> BaseChats.hello(user)
						"/create" -> CreateCustomerChat(user).getChat()
						else      -> chooseClient()
					}
				}
				return@setNextChatFunction getAction()
			} else {
				customerList = customerRepo.findByFullNameLowerCaseLike(it.toLowerCase())
				customerRepo.findById(it).ifPresent { customerList.add(it) }
				return@setNextChatFunction chooseClient()
			}
		})
	
	
	fun getAction() = ChatBuilder(user).name("createRequest_selectAction")
		.setNextChatFunction(Response(user, "createRequest.search.getAction")
			.withCustomKeyboard(arrayOf("Credit release", "Limit increase", "Cancel")),
			{
				return@setNextChatFunction when {
					it == "Credit release" -> getCreditReleaseChat()
					it == "Cancel"         -> BaseChats.hello(user)
					else                   -> getCreditLimitIncreaseChat()
				}
			})
	
	private fun getCreditReleaseChat() = ChatBuilder(user).name("createRequest_release")
		.beforeExecution { creditObtainRequest = CreditObtainRequest(creator = user.id, customer = customer!!) }
		.then("createRequest.creditRelease.amount", {
			creditObtainRequest.amount = it.toDouble()
		})
		.then("createRequest.creditRelease.date", {
			creditObtainRequest.pickupDate = when (if (it.startsWith("/")) it.substring(1) else it) {
				"today"     -> LocalDate.now()
				"tomorrow"  -> LocalDate.now().plusDays(1)
				"monday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
				"tuesday"   -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
				"wednesday" -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
				"thursday"  -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY))
				"friday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
				"saturday"  -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
				"sunday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
				else        -> {
					if (it.matches(Regex("\\d{1,2}/\\d{1,2}"))) {
						LocalDate.now().withMonth(it.split("/")[0].toInt())
							.withDayOfMonth(it.split("/")[1].toInt())
					} else {
						LocalDate.parse(it)
					}
				}
			}
		})
		.then(Response(user.id, "createRequest.creditRelease.bco").withCustomKeyboard(arrayOf("BCO", "Carrier")), {
			creditObtainRequest.bco = it.contains("bco", true)
		})
		.then("createRequest.creditRelease.fb", { creditObtainRequest.fb = it })
		.then("createRequest.creditRelease.comment", { creditObtainRequest.comment = it })
		.setOnCompleteMessage(Response { "Your request #${creditObtainRequest.id} was written to DB. Thanks." })
		.setOnCompleteAction {
			creditObtainsRepo.save(creditObtainRequest)
			sheetsAPI.writeToTable("default", creditObtainRequest.type, -1,
				arrayOf(
					creditObtainRequest.id,
					LocalDate.now().toString(), //request.id	LocalDate.now()	user.name	customer.name
					user.fullName!!,
					customer!!.fullName,
					customer!!.id, //customer.id	новое поле fb	select.amount	String	select.status.value	пустое поле
					creditObtainRequest.fb,
					DecimalFormat("#,###.##").format(creditObtainRequest.amount),
					creditObtainRequest.type,
					creditObtainRequest.status,
					"",
					creditObtainRequest.comment, //новое поле comment	customer.contact	что?
					customer!!.info ?: "No info",
					"",
					customer!!.creditLimit.toString()
				)
			)
		}
	
	private fun getCreditLimitIncreaseChat() = ChatBuilder(user).name("createRequest_increase")
		.beforeExecution { creditIncreaseRequest = CreditIncreaseRequest(creator = user.id, customer = customer!!) }
		.then("createRequest.limitIncrease.amount", { creditIncreaseRequest.amount = it.toDouble() })
		.then("createRequest.limitIncrease.comment", { creditIncreaseRequest.comment = it })
		.setOnCompleteAction {
			createLimitEntry(customer!!, creditIncreaseRequest)
		}
	
	fun createLimitEntry(customer: Customer, type: String, comment: String, amount: Double): CreditIncreaseRequest {
		val creditIncreaseRequest = CreditIncreaseRequest(creator = user.id, customer = customer)
		creditIncreaseRequest.amount = amount
		creditIncreaseRequest.comment = comment
		creditIncreaseRequest.type = type
		return createLimitEntry(customer, creditIncreaseRequest)
	}
	
	private fun createLimitEntry(customer: Customer, creditIncreaseRequest: CreditIncreaseRequest): CreditIncreaseRequest {
		creditIncreaseRepo.save(creditIncreaseRequest)
		sheetsAPI.writeToTable("default", creditIncreaseRequest.type, -1,
			arrayOf(creditIncreaseRequest.id,
				LocalDate.now().toString(),
				user.fullName!!,
				customer.fullName,
				customer.id,
				"",
				creditIncreaseRequest.type,
				creditIncreaseRequest.status,
				"",
				"TODO documents",
				creditIncreaseRequest.comment,
				customer.info ?: "No info",
				"$0",
				DecimalFormat("#,###.##").format(creditIncreaseRequest.amount),
				customer.creditLimit.toString()
			)
		)
		return creditIncreaseRequest
	}
}

/*

TODO Что делать дальше:
1. Придумать что то вроде checkError-функции, или возможности для передачи в качестве аргумента функции проверки, которая
будет возвращать бульку и прописать в чате onErrorFunction/onErrorText


 */
