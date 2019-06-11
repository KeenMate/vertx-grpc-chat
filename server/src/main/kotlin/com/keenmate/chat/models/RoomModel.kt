package com.keenmate.chat.models

import com.keenmate.chat.Room
import com.keenmate.chat.models.base.IModel

class RoomModel: IModel<Room> {
	var roomId: Int = 0
	var title: String = ""
	var private: Boolean = false
	var creator: UserModel = UserModel()
	
	// non-gRPC props
	var messages: ArrayList<MessageModel> = ArrayList()
	var users: ArrayList<UserModel> = ArrayList()

	override fun parseFrom(src: Room): RoomModel {
		roomId = src.roomId
		title = src.title
		private = src.private
		creator.parseFrom(src.creator)
		
		return this
	}
	
	override fun convert(): Room {
		return Room.newBuilder()
			.setRoomId(roomId)
			.setTitle(title)
			.setPrivate(private)
			.setCreator(creator.convert())
			.build()
	}
}