package com.keenmate.chat.models

import com.keenmate.chat.JoinRoomRequest
import com.keenmate.chat.models.base.IModel
import java.util.*

class JoinRoomRequestModel: IModel<JoinRoomRequest> {
	var roomId: Int = 0
	var userGuid: String = UUID(0, 0)
		.toString()
	
	override fun convert(): JoinRoomRequest {
		val joinRoomRequestBuilder = JoinRoomRequest.newBuilder()
			.setRoomId(roomId)
		
		if (userGuid != UUID(0, 0).toString())
			joinRoomRequestBuilder.userGuid = userGuid
		
		return joinRoomRequestBuilder.build()
	}

	override fun parseFrom(src: JoinRoomRequest): JoinRoomRequestModel {
		roomId = src.roomId
		userGuid = src.userGuid
		
		return this
	}
}