package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CreditObtainRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.ChatBuilder
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.repo.CreditIncreaseRepo
import com.bot.util.GSheetsAPI
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
		return ChatBuilder().name("createRequest_home")
			.setNextChatFunction("Enter client name or ID", {
				customerList = customerRepo.findByFullNameLowerCaseLike(it.toLowerCase())
				customerRepo.findById(it).ifPresent { customerList.add(it) }
				return@setNextChatFunction chooseClient()
			})
	}
	
	fun chooseClient(): ChatBuilder = ChatBuilder().name("createRequest_selectClient")
		.setNextChatFunction(Response {
			val num = AtomicInteger(0)
			
			return@Response "Choose client or enter new search query or /cancel or /create:\n" + customerList.stream()
				.map { "/${num.getAndIncrement()} ${it.fullName} " }
				.reduce { a, b -> "$a\n$b" }.orElse("Empty result. Try again.")
		}, {
			if (it.startsWith("/")) {
				try {
					customer = customerList[it.substring(1).toInt()]
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
	
	
	fun getAction() = ChatBuilder().name("createRequest_selectAction")
		.setNextChatFunction(Response(user.id, "1 to create credit widthdraw, 2 for credit limit increase")
			.withInlineKeyboard(arrayOf("Credit widthdraw", "Limit increase", "Cancel")),
			{
				return@setNextChatFunction when {
					it.contains("1")         -> getCreditWidthDrawChat()
					it == "Credit widthdraw" -> getCreditWidthDrawChat()
					it == "Cancel"           -> BaseChats.hello(user)
					else                     -> getCreditLimitIncreaseChat()
				}
			})
	
	private fun getCreditWidthDrawChat() = ChatBuilder().name("createRequest_widthdraw")
		.beforeExecution { creditObtainRequest = CreditObtainRequest(creator = user.id, customer = customer!!) }
		.then("Enter load amount", {
			creditObtainRequest.amount = it.toDouble()
		})
		.then("When is the pickup? Formats: 5/24, 2015-4-25\nAlso available: /today /tomorrow /monday /tuesday " +
			"/wednesday /thursday /friday /saturday /sunday", {
			creditObtainRequest.pickupDate = when (it) {
				"/today"     -> LocalDate.now()
				"/tomorrow"  -> LocalDate.now().plusDays(1)
				"/monday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
				"/tuesday"   -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
				"/wednesday" -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
				"/thursday"  -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY))
				"/friday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
				"/saturday"  -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
				"/sunday"    -> LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
				else         -> {
					if (it.matches(Regex("\\d{1,2}/\\d{1,2}"))) {
						LocalDate.now().withMonth(it.split("/")[0].toInt())
							.withDayOfMonth(it.split("/")[1].toInt())
					} else {
						LocalDate.parse(it)
					}
				}
			}
		})
		.then("BSO or other type?", {
			creditObtainRequest.bso = it.contains("bso", true)
		})
		.then("Add a comment", { creditObtainRequest.comment = it })
		.setOnCompleteAction {
			creditObtainsRepo.save(creditObtainRequest)
			sheetsAPI.writeToTable("default", "Requests", -1,
				creditObtainRequest.id,
				LocalDate.now().toString(), //request.id	LocalDate.now()	user.name	customer.name
				user.username + "/" + user.id,
				customer!!.fullName,
				customer!!.id, //customer.id	новое поле fb	select.amount	String	select.status.value	пустое поле
				"TODO FB", // TODO FB
				creditObtainRequest.amount.toString(),
				creditObtainRequest.type,
				creditObtainRequest.status,
				"",
				creditObtainRequest.comment, //новое поле comment	customer.contact	что?
				customer!!.info ?: "No info",
				"",
				customer!!.creditLimit.toString())
		}
	
	
	private fun getCreditLimitIncreaseChat() = ChatBuilder().name("createRequest_increase")
		.beforeExecution { creditIncreaseRequest = CreditIncreaseRequest(creator = user.id, customer = customer!!) }
		.then("Enter amount, $", { creditIncreaseRequest.amount = it.toDouble() })
		.then("Add a comment", { creditIncreaseRequest.comment = it })
		.setOnCompleteAction {
			creditIncreaseRepo.save(creditIncreaseRequest)
			sheetsAPI.writeToTable("default", "Credit limit", -1,
				creditIncreaseRequest.id,
				LocalDate.now().toString(),
				user.username + "/" + user.id,
				customer!!.fullName,
				customer!!.id,
				"",
				creditIncreaseRequest.type,
				creditIncreaseRequest.status,
				"",
				"TODO documents",
				creditIncreaseRequest.comment,
				customer!!.info ?: "No info",
				"$0",
				creditIncreaseRequest.amount.toString(),
				customer!!.creditLimit.toString())
		}
}

/*

TODO Что делать дальше:
1. Придумать что то вроде checkError-функции, или возможности для передачи в качестве аргумента функции проверки, которая
будет возвращать бульку и прописать в чате onErrorFunction/onErrorText


 */
