package com.keenmate.chat_01.models

import com.keenmate.chat_01.Message
import com.keenmate.chat_01.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class MessageModel: IModel<Message> {
	var messageId: Int = 0
	var sourceId: Int = 0
	var roomId: Int = 0
	var content: String = ""
	var sent: Long = 0
	
	override fun convert(): Message {
		return Message.newBuilder()
			.setMessageId(messageId)
			.setSourceId(sourceId)
			.setRoomId(roomId)
			.setContent(content)
			.setSent(sent)
			.build()
	}

	override fun parseFrom(src: String): MessageModel {
		val tmp = Json.parse(serializer(), src)
		
		messageId = tmp.messageId
		sourceId = tmp.sourceId
		roomId = tmp.roomId
		content = tmp.content
		sent = tmp.sent
		
		return this
	}

	override fun parseFrom(src: Message): MessageModel {
		messageId = src.messageId
		sourceId = src.sourceId
		roomId = src.roomId
		content = src.content
		sent = src.sent
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}