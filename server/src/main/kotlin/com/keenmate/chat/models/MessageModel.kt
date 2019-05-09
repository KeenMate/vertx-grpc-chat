package com.keenmate.chat.models

import com.keenmate.chat.Message
import com.keenmate.chat.models.base.IModel
import java.util.*

class MessageModel: IModel<Message> {
	var messageId: Int = 0
	var creatorGuid: String = UUID(0, 0)
		.toString()
	var roomId: Int = 0
	var content: String = ""
	var sent: Long = 0
	
	override fun convert(): Message {
		return Message.newBuilder()
			.setMessageId(messageId)
			.setCreatorGuid(creatorGuid)
			.setRoomId(roomId)
			.setContent(content)
			.setSent(sent)
			.build()
	}

	override fun parseFrom(src: Message): MessageModel {
		messageId = src.messageId
		creatorGuid = src.creatorGuid
		roomId = src.roomId
		content = src.content
		sent = src.sent
		
		return this
	}
	
	// override fun toString(): String {
	// 	return Json.stringify(serializer(), this)
	// }
}