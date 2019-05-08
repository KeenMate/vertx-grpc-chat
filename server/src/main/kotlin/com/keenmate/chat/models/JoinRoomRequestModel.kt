package com.keenmate.chat.models

import com.keenmate.chat.JoinRoomRequest
import com.keenmate.chat.models.base.IModel

class JoinRoomRequestModel: IModel<JoinRoomRequest> {
	var room: RoomModel = RoomModel()
	var client: ClientModel = ClientModel()
	
	override fun convert(): JoinRoomRequest {
		val joinRoomRequestBuilder = JoinRoomRequest.newBuilder()
			.setRoom(room!!.convert())
		
		if (client.clientGuid != "")
			joinRoomRequestBuilder.client = client.convert()
		
		return joinRoomRequestBuilder.build()
	}

	// override fun parseFrom(src: String): JoinRoomRequestModel {
	// 	val tmp = Json.parse(serializer(), src)
	//	
	// 	room = tmp.room
	// 	client = tmp.client
	//	
	// 	return this
	// }

	override fun parseFrom(src: JoinRoomRequest): JoinRoomRequestModel {
		room = RoomModel().parseFrom(src.room)
		client = ClientModel().parseFrom(src.client)
		
		return this
	}

	// override fun toString(): String {
	// 	return Json.stringify(serializer(), this)
	// }
}