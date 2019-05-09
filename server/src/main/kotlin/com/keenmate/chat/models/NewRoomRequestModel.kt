package com.keenmate.chat.models

import com.keenmate.chat.NewRoomRequest
import com.keenmate.chat.models.base.IModel
import java.util.*

class NewRoomRequestModel: IModel<NewRoomRequest> {
	var creatorGuid: String = UUID(0, 0)
		.toString()
	var title: String = ""
	var private: Boolean = false
	
	override fun convert(): NewRoomRequest {
		return NewRoomRequest.newBuilder()
			.setCreatorGuid(creatorGuid)
			.setTitle(title)
			.setPrivate(private)
			.build()
	}

	override fun parseFrom(src: NewRoomRequest): NewRoomRequestModel {
		creatorGuid = src.creatorGuid
		title = src.title
		private = src.private
		
		return this
	}
}