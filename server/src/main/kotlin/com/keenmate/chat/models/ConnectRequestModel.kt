package com.keenmate.chat_01.models

import com.keenmate.chat_01.ConnectRequest
import com.keenmate.chat_01.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class ConnectRequestModel: IModel<ConnectRequest> {

	var name: String = ""
	
	override fun parseFrom(src: String): ConnectRequestModel {
		val tmp = Json.parse(serializer(), src)
		
		name = tmp.name
		
		return this
	}

	override fun parseFrom(src: ConnectRequest): ConnectRequestModel {
		name = src.name
		
		return this
	}
	
	override fun convert(): ConnectRequest {
		return ConnectRequest.newBuilder()
			.setName(name)
			.build()
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}