package com.bot.util

import com.google.gson.Gson
import com.google.gson.JsonParser
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
import java.nio.charset.Charset


//TODO Cleanup response logging
@RestController
@RequestMapping("/api/google/sheets")
class GSheetsAPI
@Autowired constructor(val gAuthAPI: GAuthAPI) {
	
	val sheetId = "1H_LgnsVg_3WxD9nXE76KzGNEl40gA5OUlwXmOCmp-e8"
	
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
			writeToTable(sheetId, title, 1, arrayOf("ID заказа", "ФИО", "Телефон", "Тип", "Размещение", "Аванс",
				"Полная цена", "Коммент"))
		}
		
		return response.statusCodeValue
	}
	
	// TODO Update method for page - to make new dates appear
	// TODO Cache the first line
	
	@Synchronized
	fun getFirstFreeLine(sheetId: String?, page: String): Int {
		try {
			val x3 = restTemplate
				.getForEntity("https://sheets.googleapis.com/v4/spreadsheets/" + sheetId + "/values/" + page + "!" + "A1:A10000"
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
	fun writeToTable(sheetId: String, page: String, row: Int, data: Array<String>) {
		val rowX = if (row < 0) getFirstFreeLine(if (sheetId == "default") this.sheetId else sheetId, page) else row
		writeToTable(if (sheetId == "default") this.sheetId else sheetId,
			page + "!" + ROWSTART + rowX + ":" + (ROWSTART.toInt() + data.size - 1).toChar() + rowX,
			arrayOf(data))
	}
	
	@Synchronized
	fun writeToTable(id: String, range: String, data: Array<Array<String>>, retry: Boolean = false) {
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
		val entity = HttpEntity(gson.toJson(map).toByteArray(Charset.forName("UTF-8")), headers)
		
		val response = restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/$id/values:batchUpdate" +
				"?access_token=${gAuthAPI.accessToken}",
			entity,
			String::class.java)
		
		if (response.statusCodeValue != 200) {
			if (retry) {
				throw IllegalArgumentException()
			}
			println("WARNING! Error writing to sheet " + id + " data: " + Arrays.toString(data))
			println(response)
			try {
				createPage(sheetId, range.split(Regex("!"))[0])
				writeToTable(id, range, data, true)
			} catch (e: IllegalArgumentException) {
				println("And this won't be fixed by rename")
			}
		}
	}
	
	@Synchronized
	fun updateCellsWhere(sheetId: String = this.sheetId, page: String, criteria: (List<String>) -> Boolean, updateFx: (MutableList<String>) -> MutableList<String>) {
		val resp = restTemplate.getForEntity("https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values/$page" +
			"?access_token=${gAuthAPI.accessToken}", String::class.java)
		val arrays = JsonParser().parse(resp.body).asJsonObject.getAsJsonArray("values").map {
			it.asJsonArray.map { it.asString }
		}
		
		arrays.forEach { it.forEach { print(it + "\t") }; println("") }
		
		(0 until arrays.size).filter {
			criteria.invoke(arrays[it])
		}.stream().peek { println("NOT NULL") }
			.forEach {
				//				clearData(sheetId, "$page!A${it + 1}:Z${it + 1}")
				writeToTable(sheetId = sheetId, page = page, row = it + 1, data = updateFx.invoke(arrays[it].toMutableList()).toTypedArray())
			}
	}
	
	@Synchronized
	fun clearData(sheetId: String, range: String) {
		val response = restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values/$range:clear" +
				"?access_token=${gAuthAPI.accessToken}",
			"{}",
			String::class.java)
	}
	
	fun checkIfTableExist(sheetId: String, pageTextId: String): Boolean {
		val response = restTemplate.getForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/$sheetId?access_token=" + gAuthAPI.accessToken,
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