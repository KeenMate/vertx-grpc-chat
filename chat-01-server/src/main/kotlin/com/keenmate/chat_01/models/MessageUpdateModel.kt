package com.keenmate.chat_01.models

import com.keenmate.chat_01.MessageUpdate
import com.keenmate.chat_01.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.mvel2.util.ReflectionUtil

@Serializable
class MessageUpdateModel: IModel<MessageUpdate> {
	
	var messageId: Int = 0
	var seen: Boolean = false
	var received: Boolean = false
	var content: String = ""
	var updatedCase: MessageUpdate.UpdatedCase = MessageUpdate.UpdatedCase.UPDATED_NOT_SET
	
	override fun convert(): MessageUpdate {
		val result = MessageUpdate.newBuilder()
			.setMessageId(messageId)

		when (updatedCase) {
			MessageUpdate.UpdatedCase.SEEN -> result.seen = seen
			MessageUpdate.UpdatedCase.RECEIVED -> result.received = received
			MessageUpdate.UpdatedCase.CONTENT -> result.content = content
			MessageUpdate.UpdatedCase.UPDATED_NOT_SET -> { }
		}
		
		return result.build()
	}

	override fun parseFrom(src: String): MessageUpdateModel {
		val tmp = Json.parse(serializer(), src)
		
		messageId = tmp.messageId
		updatedCase = tmp.updatedCase

		when (updatedCase) {
			MessageUpdate.UpdatedCase.SEEN -> seen = tmp.seen
			MessageUpdate.UpdatedCase.RECEIVED -> received = tmp.received
			MessageUpdate.UpdatedCase.CONTENT -> content = tmp.content
			MessageUpdate.UpdatedCase.UPDATED_NOT_SET -> { }
		}
		
		return this
	}

	override fun parseFrom(src: MessageUpdate): MessageUpdateModel {
		messageId = src.messageId

		when (updatedCase) {
			MessageUpdate.UpdatedCase.SEEN -> seen = src.seen
			MessageUpdate.UpdatedCase.RECEIVED -> received = src.received
			MessageUpdate.UpdatedCase.CONTENT -> content = src.content
			MessageUpdate.UpdatedCase.UPDATED_NOT_SET -> { }
		}
		
		return this
	}

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}