package com.keenmate.chat.models

import com.keenmate.chat.ChatChange
import com.keenmate.chat.models.base.IModel

class ChatChangeModel: IModel<ChatChange> {
	var msg: MessageModel = MessageModel()
	var room: RoomModel = RoomModel()
	var clientConnected: ClientModel = ClientModel()
	var clientDisconnected: ClientModel = ClientModel()
	var theChange: ChatChange.TheChangeCase = ChatChange.TheChangeCase.THECHANGE_NOT_SET
	
	override fun convert(): ChatChange {
		val result = ChatChange.newBuilder()
		
		result.room = room.convert()
		
		when (theChange) {
			ChatChange.TheChangeCase.MSG -> result.msg = msg.convert()
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> result.clientConnected = clientConnected.convert()
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> result.clientDisconnected = clientDisconnected.convert()
		}
		
		return result.build()
	}

	// @UnstableDefault
	// override fun parseFrom(src: String): ChatChangeModel {
	// 	val tmp = Json.parse(serializer(), src)
	//	
	// 	room = tmp.room
	// 	when (tmp.theChange.toString()) {
	// 		ChatChange.TheChangeCase.MSG.toString() -> msg = tmp.msg
	// 		ChatChange.TheChangeCase.THECHANGE_NOT_SET.toString() -> { }
	// 		ChatChange.TheChangeCase.CLIENTCONNECTED.toString() -> clientConnected = tmp.clientConnected
	// 		ChatChange.TheChangeCase.CLIENTDISCONNECTED.toString() -> clientDisconnected = tmp.clientDisconnected
	// 	}
	//	
	// 	return this
	// }

	override fun parseFrom(src: ChatChange): ChatChangeModel {
		room = RoomModel().parseFrom(src.room)
		
		when (src.theChangeCase) {
			ChatChange.TheChangeCase.MSG -> msg = MessageModel().parseFrom(src.msg)
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> clientConnected = ClientModel().parseFrom(src.clientConnected)
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> clientDisconnected = ClientModel().parseFrom(src.clientDisconnected)
		}
		
		return this
	}
}