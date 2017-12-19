package com.bot.util

import com.google.gson.Gson
import com.nikichxp.util.JsonUtil
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.util.Arrays
import com.nikichxp.util.Json
import org.springframework.beans.factory.annotation.Autowired
import org.apache.http.impl.client.HttpClients
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory


//TODO Cleanup response logging
@RestController
@RequestMapping("/api/google/sheets")
class GSheetsAPI
@Autowired constructor(val gAuthAPI: GAuthAPI) {
	
	val sheetId = "1H_LgnsVg_3WxD9nXE76KzGNEl40gA5OUlwXmOCmp-e8"
	
	//	@Synchronized protected fun writeOrderToTable(order: Order): Boolean {
	//		val sheetsId: String?
	//		if (eventIdToSheet[order.getLinkedEvent().getId()] == null) {
	//			sheetsId = createEventTable(order.getLinkedEvent())
	//		} else {
	//			sheetsId = eventIdToSheet[order.getLinkedEvent().getId()]
	//		}
	//		if (!checkIfTableExist(sheetsId, order.getOfferClone().getBeginDate().toString())) {
	//			return false
	//		}
	//		val counter = AtomicInteger(getFirstFreeLine(sheetsId, order.getOfferClone().getBeginDate().toString()))
	//		order.getPersons().forEach { person ->
	//			writeToTable(sheetsId, order.getOfferClone().getBeginDate().toString(),
	//					counter.getAndIncrement(), order.getId(), person.getFullName(), person.getPhone(), person.getType(),
	//					person.getSort(), order.getDeposit() + "", order.getFullPrice() + "", order.getComment()
	//			)
	//		}
	//		return true
	//	}
	
	//	@Synchronized private fun createEventTable(event: Event): String? {
	//		val headers = HttpHeaders()
	//		headers.set("charset", "UTF-8")
	//		headers.contentType = MediaType.APPLICATION_JSON
	//
	//		val arr = event.getOffers().stream()
	//			.map({ offer -> Json.of("properties", Json.of("title", offer.getBeginDate().toString())).json() })
	//			.collect(Collectors.toList<T>())
	//
	//		val entity = HttpEntity<T>(
	//				Json.of(
	//						"properties", Json
	//					.of("title", event.getName()))
	//					.and("sheets", arr)
	//					.toString().getBytes(), headers)
	//
	//		val response = restTemplate.postForEntity(
	//				"https://sheets.googleapis.com/v4/spreadsheets?access_token=" + gAuthAPI.accessToken, entity, String::class.java)
	//
	//		if (debug) {
	//			println(response)
	//			TelegramBotMethods.sendMessage(response.toString())
	//		}
	//
	//		if (response.statusCodeValue != 200) {
	//			println("Warning, response is not 200")
	//			return null
	//		}
	//
	//		val spreadsheetId = JsonUtil.of(response.body!!).getX("spreadsheetId")
	//
	//		configRepo.save(ElseAPI.ConfigPair("sheet-" + event.getId(), spreadsheetId))
	//		eventIdToSheet.put(event.getId(), spreadsheetId)
	//
	//		return spreadsheetId
	//	}
	
	@Synchronized
	fun createPage(sheetId: String, title: String): Int {
		val requestJson = "{\"requests\":[{\"addSheet\":{\"properties\":{\"title\":\"$title\"}}}]}"
		
		val headers = HttpHeaders()
		headers.set("charset", "UTF-8")
		headers.contentType = MediaType.APPLICATION_JSON
		
		val entity = HttpEntity(requestJson.toByteArray(), headers)
		
		println(gAuthAPI.accessToken)
		
		val response = restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/" + sheetId + ":batchUpdate" +
				"?access_token=" + gAuthAPI.accessToken,
			entity,
			String::class.java)
		
		println(gAuthAPI.accessToken)
		
		if (debug) {
			println(response)
		}
		
		if (response.statusCodeValue == 200) {
			writeToTable(sheetId, title, 1, "ID заказа", "ФИО", "Телефон", "Тип", "Размещение", "Аванс",
				"Полная цена", "Коммент")
		}
		
		return response.statusCodeValue
	}
	
	// TODO Update method for page - to make new dates appear
	// TODO Cache the first line
	
	@Synchronized
	fun getFirstFreeLine(sheetId: String?, page: String): Int {
		try {
			val x3 = restTemplate
				.getForEntity("https://sheets.googleapis.com/v4/spreadsheets/" + sheetId + "/values/" + page + "!" + "A1:A1000"
					+ "?majorDimension=COLUMNS&access_token="
					+ gAuthAPI.accessToken, String::class.java).body!!
			
			return JsonUtil.of(x3).getJson().getAsJsonArray("values").get(0).asJsonArray.size() + 1
		} catch (ingored: Exception) {
			return 1
		}
		
	}
	
	/**
	 * Simple write a line to table
	 *
	 * @param sheetId sheet ID, it's long value in URL
	 * @param page    page title, like 'Page1' or '2017-05-12T11:30'
	 * @param row     a row of writing - 1,2,3... to write on A1..E1, A2..E2 etc. Use -1 to get first free line
	 * @param data    all the rows
	 */
	fun writeToTable(sheetId: String, page: String, row: Int, vararg data: String) {
		val row_ = if (row < 0) getFirstFreeLine(if (sheetId == "default") this.sheetId else sheetId, page) else row
		writeToTable(if (sheetId == "default") this.sheetId else sheetId,
			page + "!" + ROWSTART + row_ + ":" + (ROWSTART.toInt() + data.size - 1).toChar() + row_,
			arrayOf(data as Array<String>))
	}
	
	@Synchronized
	fun writeToTable(id: String, range: String, data: Array<Array<String>>) {
		val arrays = arrayOfNulls<Json.JsonArr>(data.size)
		for (i in arrays.indices) {
			arrays[i] = Json.JsonArr(data[i])
		}
		
		val headers = HttpHeaders()
		headers.set("charset", "UTF-8")
		headers.contentType = MediaType.APPLICATION_JSON
		
		val map = HashMap<String, Any>()
		map.put("valueInputOption", "RAW")
		
		val dataMap = HashMap<String, Any>()
		dataMap.put("range", range)
		dataMap.put("values", data)
		
		map.put("data", dataMap)
		val entity = HttpEntity(gson.toJson(map), headers)
		
		val response = restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/" + id + "/values:batchUpdate" +
				"?access_token=" + gAuthAPI.accessToken,
			entity,
			String::class.java)
		
		if (response.statusCodeValue != 200) {
			println("WARNING! Error writing to sheet " + id + " data: " + Arrays.toString(data))
			println(response)
		}
	}
	
	fun checkIfTableExist(sheetId: String, pageTextId: String): Boolean {
		val response = restTemplate.getForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/" + sheetId + "?access_token=" + gAuthAPI.accessToken,
			String::class.java)
		
		if (debug) {
			println(response)
		}
		
		if (response.statusCodeValue != 200) {
			return false
		}
		if (response.statusCodeValue == 404) {
			return false
		}
		try {
			for (json in JsonUtil.of(response.body!!).getJson().getAsJsonArray("sheets")) {
				if (JsonUtil.of(json.toString()).getX("properties.title") == pageTextId) {
					return true
				}
			}
		} catch (ex: Exception) {
			return false
		}
		
		return createPage(sheetId, pageTextId) == 200
	}
	
	companion object {
		
		var debug = true
		var gson = Gson()
		private var restTemplate: RestTemplate
		/**
		 * every row will start from this one. if A - A12..E12, C - C12..G12 etc
		 */
		private val ROWSTART = 'A'
		
		init {
			val requestFactory = HttpComponentsClientHttpRequestFactory(HttpClients.createDefault())
			
			restTemplate = RestTemplate(requestFactory)
			restTemplate.errorHandler = object : ResponseErrorHandler {
				override fun hasError(clientHttpResponse: ClientHttpResponse): Boolean {
					return false
				}
				
				override fun handleError(clientHttpResponse: ClientHttpResponse) {
					if (clientHttpResponse.rawStatusCode == 403) {
						println("Warning! Illegal permissions!")
					}
				}
			}
		} // configure rest template
	}
}