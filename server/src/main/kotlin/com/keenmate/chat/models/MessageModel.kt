package com.keenmate.chat.models

import com.keenmate.chat.Message
import com.keenmate.chat.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class MessageModel: IModel<Message> {
	var messageId: Int = 0
	var creator: ClientModel? = null
	var room: RoomModel? = null
	var content: String = ""
	var sent: Long = 0
	
	override fun convert(): Message {
		return Message.newBuilder()
			.setMessageId(messageId)
			.setCreator(creator!!.convert())
			.setRoom(room!!.convert())
			.setContent(content)
			.setSent(sent)
			.build()
	}

	override fun parseFrom(src: String): MessageModel {
		val tmp = Json.parse(serializer(), src)
		
		messageId = tmp.messageId
		creator = tmp.creator
		room = tmp.room
		content = tmp.content
		sent = tmp.sent
		
		return this
	}

	override fun parseFrom(src: Message): MessageModel {
		messageId = src.messageId
		creator = ClientModel().parseFrom(src.creator)
		room = RoomModel().parseFrom(src.room)
		content = src.content
		sent = src.sent
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}