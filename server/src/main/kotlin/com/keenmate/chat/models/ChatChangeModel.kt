package com.keenmate.chat.models

import com.keenmate.chat.ChatChange
import com.keenmate.chat.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class ChatChangeModel: IModel<ChatChange> {
	var msg: MessageModel? = null
	var clientConnected: ClientModel? = null
	var clientDisconnected: ClientModel? = null
	var theChange: ChatChange.TheChangeCase = ChatChange.TheChangeCase.THECHANGE_NOT_SET
	
	override fun convert(): ChatChange {
		val result = ChatChange.newBuilder()
		
		when (theChange) {
			ChatChange.TheChangeCase.MSG -> result.msg = msg!!.convert()
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> result.clientConnected = clientConnected!!.convert()
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> result.clientDisconnected = clientDisconnected!!.convert()
		}
		
		return result.build()
	}

	override fun parseFrom(src: String): IModel<ChatChange> {
		val tmp = Json.parse(serializer(), src)
		
		when (tmp.theChange) {
			ChatChange.TheChangeCase.MSG -> msg = tmp.msg
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> clientConnected = tmp.clientConnected
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> clientDisconnected = tmp.clientDisconnected
		}
		
		return this
	}

	override fun parseFrom(src: ChatChange): IModel<ChatChange> {		
		when (src.theChangeCase) {
			ChatChange.TheChangeCase.MSG -> msg = MessageModel().parseFrom(src.msg)
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
			ChatChange.TheChangeCase.CLIENTCONNECTED -> clientConnected = ClientModel().parseFrom(src.clientConnected)
			ChatChange.TheChangeCase.CLIENTDISCONNECTED -> clientDisconnected = ClientModel().parseFrom(src.clientDisconnected)
		}
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}