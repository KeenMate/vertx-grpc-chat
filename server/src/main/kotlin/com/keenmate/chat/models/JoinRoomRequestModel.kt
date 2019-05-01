package com.keenmate.chat_01.models

import com.keenmate.chat_01.JoinRoomRequest
import com.keenmate.chat_01.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class JoinRoomRequestModel: IModel<JoinRoomRequest> {
	var roomId: Int = 0
	var client: ClientModel? = null
	
	override fun convert(): JoinRoomRequest {
		val joinRoomRequestBuilder = JoinRoomRequest.newBuilder()
			.setRoomId(roomId)
		
		if (client != null)
			joinRoomRequestBuilder.client = client!!.convert()
		
		return joinRoomRequestBuilder.build()
	}

	override fun parseFrom(src: String): JoinRoomRequestModel {
		val tmp = Json.parse(serializer(), src)
		
		roomId = tmp.roomId
		client = tmp.client
		
		return this
	}

	override fun parseFrom(src: JoinRoomRequest): JoinRoomRequestModel {
		roomId = src.roomId
		client = ClientModel().parseFrom(src.client)
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}