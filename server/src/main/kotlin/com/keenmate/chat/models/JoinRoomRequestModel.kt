package com.keenmate.chat.models

import com.keenmate.chat.JoinRoomRequest
import com.keenmate.chat.models.base.IModel
import java.util.*

class JoinRoomRequestModel: IModel<JoinRoomRequest> {
	var roomId: Int = 0
	var clientGuid: String = UUID(0, 0)
		.toString()
	
	override fun convert(): JoinRoomRequest {
		val joinRoomRequestBuilder = JoinRoomRequest.newBuilder()
			.setRoomId(roomId)
		
		if (clientGuid != UUID(0, 0).toString())
			joinRoomRequestBuilder.clientGuid = clientGuid
		
		return joinRoomRequestBuilder.build()
	}

	override fun parseFrom(src: JoinRoomRequest): JoinRoomRequestModel {
		roomId = src.roomId
		clientGuid = src.clientGuid
		
		return this
	}
}