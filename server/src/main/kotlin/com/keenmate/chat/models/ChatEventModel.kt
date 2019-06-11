package com.keenmate.chat.models

import com.keenmate.chat.*
import com.keenmate.chat.models.base.IModel

class ChatEventModel : IModel<ChatEvent> {
	var change: TheChange = TheChange.NOTSET
	var valueCase: ChatEvent.ValueCase = ChatEvent.ValueCase.VALUE_NOT_SET
	var room: RoomModel? = null
	var user: UserModel? = null
	var message: MessageModel? = null
	var roomId: Int = -1

	override fun convert(): ChatEvent {
		val response = ChatEvent.newBuilder()

		response.change = change

		when (valueCase) {
			ChatEvent.ValueCase.ROOM -> {
				if (change != TheChange.DELETED)
					response.room = room?.convert() ?: Room.newBuilder().build()
				else
					response.roomId = roomId
			}
			ChatEvent.ValueCase.USER -> {
				response.user = user?.convert() ?: User.newBuilder().build()
				response.roomId = roomId
			}
			ChatEvent.ValueCase.MESSAGE -> response.message = message?.convert() ?: Message.newBuilder().build()
			ChatEvent.ValueCase.VALUE_NOT_SET -> { }
		}

		return response.build()
	}

	override fun parseFrom(src: ChatEvent): ChatEventModel {
		change = src.change
		valueCase = src.valueCase
		
		when (src.valueCase) {
			ChatEvent.ValueCase.ROOM -> {
				if (change != TheChange.DELETED)
					room = RoomModel().parseFrom(src.room)
				else
					roomId = src.roomId
			}
			ChatEvent.ValueCase.USER -> {
				user = UserModel().parseFrom(src.user)
				roomId = src.roomId
			}
			ChatEvent.ValueCase.MESSAGE -> message = MessageModel().parseFrom(src.message)
			ChatEvent.ValueCase.VALUE_NOT_SET -> TODO()
		}
		
		return this
	}
}