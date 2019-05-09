package com.keenmate.chat.models

import com.keenmate.chat.ChatChange
import com.keenmate.chat.models.base.IModel

class ChatChangeModel: IModel<ChatChange> {
	var msg: MessageModel = MessageModel()
	var roomId: Int = 0
	var clientConnected: ClientModel = ClientModel()
	var clientDisconnected: ClientModel = ClientModel()
	var theChange: ChatChange.TheChangeCase = ChatChange.TheChangeCase.THECHANGE_NOT_SET
	
	override fun convert(): ChatChange {
		val result = ChatChange.newBuilder()
		
		result.roomId = roomId
		
		when (theChange) {
			ChatChange.TheChangeCase.MSG -> result.msg = msg.convert()
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> result.clientConnected = clientConnected.convert()
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> result.clientDisconnected = clientDisconnected.convert()
		}
		
		return result.build()
	}

	override fun parseFrom(src: ChatChange): ChatChangeModel {
		roomId = src.roomId
		
		when (src.theChangeCase) {
			ChatChange.TheChangeCase.MSG -> msg = MessageModel().parseFrom(src.msg)
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> clientConnected = ClientModel().parseFrom(src.clientConnected)
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> clientDisconnected = ClientModel().parseFrom(src.clientDisconnected)
		}
		
		return this
	}
}