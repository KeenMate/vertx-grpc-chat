package com.keenmate.chat.models

import com.keenmate.chat.Message
import com.keenmate.chat.models.base.IModel

class MessageModel: IModel<Message> {
	var messageId: Int = 0
	var creator: ClientModel = ClientModel()
	var room: RoomModel = RoomModel()
	var content: String = ""
	var sent: Long = 0
	
	override fun convert(): Message {
		return Message.newBuilder()
			.setMessageId(messageId)
			.setCreator(creator.convert())
			.setRoom(room.convert())
			.setContent(content)
			.setSent(sent)
			.build()
	}

	// override fun parseFrom(src: String): MessageModel {
	// 	val tmp = Json.parse(serializer(), src)
	//	
	// 	messageId = tmp.messageId
	// 	creator = tmp.creator
	// 	room = tmp.room
	// 	content = tmp.content
	// 	sent = tmp.sent
	//	
	// 	return this
	// }

	override fun parseFrom(src: Message): MessageModel {
		messageId = src.messageId
		creator = ClientModel().parseFrom(src.creator)
		println("Client parsed")
		room = RoomModel().parseFrom(src.room)
		println("Room parsed")
		content = src.content
		sent = src.sent
		
		return this
	}
	
	// override fun toString(): String {
	// 	return Json.stringify(serializer(), this)
	// }
}