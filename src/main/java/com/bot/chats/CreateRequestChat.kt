package com.bot.chats

import com.bot.Ctx
import com.bot.entity.*
import com.bot.repo.CreditObtainRepo
import com.bot.repo.CustomerRepo
import com.bot.entity.TextChatBuilder
import com.bot.entity.requests.CreditIncreaseRequest
import com.bot.entity.requests.CreditObtainRequest
import com.bot.entity.requests.CreditRequest
import com.bot.logic.Notifier
import com.bot.repo.CreditIncreaseRepo
import com.bot.tgapi.Method
import com.bot.util.GSheetsAPI
import com.bot.util.isNot
import kotlinx.coroutines.experimental.launch
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.atomic.AtomicBoolean

class CreateRequestChat(user: User) : ChatParent(user) {
	
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
	
	fun getChat(): TextChatBuilder {
		return TextChatBuilder(user).name("createRequest_home")
			.setNextChatFunction("createRequest.hello", {
				setCustomerList(it)
				return@setNextChatFunction chooseClient()
			})
	}
	
	fun chooseClient(): TextChatBuilder {
		return TextChatBuilder(user).name("createRequest_chooseClient")
			.setNextChatFunction {
				return@setNextChatFunction ListChat(user, customerList)
					.printFunction {
						"${it.fullName} (${it.accountId ?: "Pending"})"
					}
					.selectFunction {
						customer = it
						return@selectFunction getAction()
					}
					.also { it.headText = "Select client or type ID or fragment of other's client name for new search." }
					.addCustomChatButton("Create user", CreateCustomerChat(user).getChat())
					.elseFunction {
						setCustomerList(it)
						return@elseFunction chooseClient()
					}
					.getChat()
			}
	}
	
	private fun setCustomerList(query: String) {
		customerList = customerRepo.findByFullNameLowerCaseLike(query.toLowerCase())
		customerRepo.findById(query).ifPresent { customerList.add(it) }
		customerList = customerList.filter { it.accountId != null }.toMutableList()
	}
	
	
	fun getAction() = TextChatBuilder(user).name("createRequest_selectAction")
		.setNextChatFunction(Response(user, "createRequest.search.getAction")
			.withCustomKeyboard("\uD83D\uDCB8 Credit release", "\uD83D\uDCB5 Limit increase", "❌ Cancel"),
			{
				return@setNextChatFunction when {
					it.contains("Credit release") -> getCreditReleaseChat()
					it.contains("Cancel")         -> BaseChats.hello(user)
					it.contains("Limit increase") -> getCreditLimitIncreaseChat()
					else                          -> throw IllegalArgumentException("Update in chat 'CreateRequestChat' selector required.")
				}
			})
	
	private fun getCreditReleaseChat(): TextChatBuilder {
		val isBco = AtomicBoolean(false)
		val chat2 = TextChatBuilder(user)
			.then("createRequest.creditRelease.comment", { creditObtainRequest.comment = it })
			.setOnCompleteMessage(Response { "Your request #${creditObtainRequest.id} was written to DB. Thanks." })
			.setOnCompleteAction {
				creditObtainsRepo.save(creditObtainRequest)
				launch {
					val select = creditObtainRequest
					sheetsAPI.writeToTable("default", "Requests", -1,
						arrayOf(select.id,
							LocalDate.now().toString(),
							select.customer.id,
							user.fullName!!,
							select.customer.fullName,
							select.customer.accountId ?: "No account ID",
							select.fb,
							select.truckId ?: "Carrier",
							"$" + DecimalFormat("#,###.##").format(creditObtainRequest.amount),
							select.type,
							select.status,
							"", //Documents
							select.customer.info ?: "No Info",
							select.releaseId,
							"-",
							"-",
							"-",
							select.comment
						)
					)
				}
				Notifier.notifyOnCreate(creditObtainRequest)
			}
		val chat1 = TextChatBuilder(user).name("createRequest_release")
			.beforeExecution { creditObtainRequest = CreditObtainRequest(creator = user.id, customer = customer!!) }
			.then("createRequest.creditRelease.amount", {
				creditObtainRequest.amount = it.filter { it.isNot(',', '$') }.toDouble()
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
			.then(Response(user.id, "createRequest.creditRelease.bco").withCustomKeyboard("\uD83D\uDE8D BCO", "\uD83D\uDE9A Carrier"), {
				it.contains("bco", true).also {
					isBco.set(it)
					creditObtainRequest.bco = it
				}
			})
			.thenIf({ isBco.get() }, "Enter truck #", { creditObtainRequest.truckId = it })
			.then("createRequest.creditRelease.fb", { creditObtainRequest.fb = it })
			.also { it.nextChatDeterminer = { documentEditorChat(creditObtainRequest).also { it.nextChatDeterminer = { chat2 } } } }
		
		return chat1
	}
	
	fun documentEditorChat(request: CreditRequest) = MessageChatBuilder(user)
		.cycleAction({ it.text == null }, Response { "Send documents. White any text to end this. If you quit it by /home progress will be lost." },
			{
				request.documents.add(it.document ?: throw IllegalArgumentException("Unexpected type of data"))
				sendMessage("Document accepted: there are ${request.documents.size} documents related to request.")
			},
			{ if (request is CreditObtainRequest) creditObtainsRepo.save(request) else creditIncreaseRepo.save(request as CreditIncreaseRequest) })
	
	
	private fun getCreditLimitIncreaseChat() = TextChatBuilder(user).name("createRequest_increase")
		.beforeExecution { creditIncreaseRequest = CreditIncreaseRequest(creator = user.id, customer = customer!!) }
		.then("createRequest.limitIncrease.amount", { creditIncreaseRequest.amount = it.filter { it.isNot(',', '$') }.toDouble() })
		.then("createRequest.limitIncrease.comment", { creditIncreaseRequest.comment = it })
		.setOnCompleteAction {
			createLimitEntry(customer!!, creditIncreaseRequest)
		}
		.also { it.nextChatDeterminer = { documentEditorChat(creditIncreaseRequest) } }
	
	fun createLimitEntry(customer: Customer, type: String, comment: String, amount: Double): CreditIncreaseRequest {
		val creditIncreaseRequest = CreditIncreaseRequest(creator = user.id, customer = customer)
		creditIncreaseRequest.amount = amount
		creditIncreaseRequest.comment = comment
		creditIncreaseRequest.type = type
		return createLimitEntry(customer, creditIncreaseRequest)
	}
	
	private fun createLimitEntry(customer: Customer, creditIncreaseRequest: CreditIncreaseRequest): CreditIncreaseRequest {
		creditIncreaseRepo.save(creditIncreaseRequest)
		launch {
			sheetsAPI.writeToTable("default", "Requests", -1,
				arrayOf(creditIncreaseRequest.id,
					LocalDate.now().toString(),
					creditIncreaseRequest.customer.id,
					user.fullName!!,
					creditIncreaseRequest.customer.fullName,
					creditIncreaseRequest.customer.accountId ?: "No account ID",
					"-", //FB
					"-",
					"-",
					creditIncreaseRequest.type,
					creditIncreaseRequest.status,
					"", //Documents
					creditIncreaseRequest.customer.info ?: "No Info",
					"-", //release ID
					"$" + DecimalFormat("#,###.##").format(creditIncreaseRequest.amount),
					"-",
					"-",
					creditIncreaseRequest.comment
				)
			)
		}
		Notifier.notifyOnCreate(creditIncreaseRequest)
		return creditIncreaseRequest
	}
}
