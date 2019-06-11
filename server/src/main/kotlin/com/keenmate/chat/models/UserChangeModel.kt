package com.keenmate.chat.models

import com.keenmate.chat.TheChange
import com.keenmate.chat.UserChange
import com.keenmate.chat.models.base.IModel

class UserChangeModel: IModel<UserChange> {
	var user: UserModel = UserModel()
	var change: TheChange = TheChange.NOTSET
	
	override fun convert(): UserChange {
		return UserChange.newBuilder()
			.setUser(user.convert())
			.setChange(change)
			.build()
	}

	override fun parseFrom(src: UserChange): UserChangeModel {
		user = UserModel().parseFrom(src.user)
		change = src.change
		
		return this
	}
}