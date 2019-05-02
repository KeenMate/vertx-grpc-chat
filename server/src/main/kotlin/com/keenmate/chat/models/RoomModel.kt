package com.keenmate.chat.models

import com.keenmate.chat.Room
import com.keenmate.chat.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class RoomModel: IModel<Room> {
	var roomId: Int = 0
	var title: String = ""
	var clients: List<ClientModel> = listOf()
	var private: Boolean = false

	override fun parseFrom(src: String): RoomModel {
		val tmp = Json.parse(serializer(), src)
		
		roomId = tmp.roomId
		title = tmp.title
		clients = tmp.clients
		private = tmp.private
		
		return this
	}

	override fun parseFrom(src: Room): RoomModel {
		roomId = src.roomId
		title = src.title
		private = src.private
		
		return this
	}
	
	override fun convert(): Room {
		return Room.newBuilder()
			.setRoomId(roomId)
			.setTitle(title)
			.setPrivate(private)
			.build()
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}