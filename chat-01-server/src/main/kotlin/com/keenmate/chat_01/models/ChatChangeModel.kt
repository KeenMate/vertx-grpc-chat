package com.keenmate.chat_01.models

import com.keenmate.chat_01.ChatChange
import com.keenmate.chat_01.Message
import com.keenmate.chat_01.MessageUpdate
import com.keenmate.chat_01.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class ChatChangeModel: IModel<ChatChange> {
	var msg: MessageModel? = null
	var messageUpdateModel: MessageUpdateModel? = null
	var theChange: ChatChange.TheChangeCase = ChatChange.TheChangeCase.THECHANGE_NOT_SET
	
	override fun convert(): ChatChange {
		val result = ChatChange.newBuilder()
		
		when (theChange) {
			ChatChange.TheChangeCase.MSG -> result.msg = msg!!.convert()
			ChatChange.TheChangeCase.UPDATED -> result.updated = messageUpdateModel!!.convert()
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
		}
		
		return result.build()
	}

	override fun parseFrom(src: String): IModel<ChatChange> {
		val tmp = Json.parse(serializer(), src)
		
		when (tmp.theChange) {
			ChatChange.TheChangeCase.MSG -> msg = tmp.msg
			ChatChange.TheChangeCase.UPDATED -> messageUpdateModel = tmp.messageUpdateModel
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
		}
		
		return this
	}

	override fun parseFrom(src: ChatChange): IModel<ChatChange> {		
		when (src.theChangeCase) {
			ChatChange.TheChangeCase.MSG -> msg = MessageModel().parseFrom(src.msg)
			ChatChange.TheChangeCase.UPDATED -> messageUpdateModel = MessageUpdateModel().parseFrom(src.updated)
			ChatChange.TheChangeCase.THECHANGE_NOT_SET -> { }
		}
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}