package com.keenmate.chat.models

import com.keenmate.chat.User
import com.keenmate.chat.models.base.IModel

class UserModel: IModel<User> {
	var userGuid: String = "" 
	var name: String = ""
	var loggedOn: Long = 0
	
	// non-gRPC props
	var connectedRooms: ArrayList<RoomModel> = ArrayList()
	
	// override fun parseFrom(src: String): UserModel {
	//	
	// 	// userGuid = tmp.userGuid
	// 	// name = tmp.name
	// 	// loggedOn = tmp.loggedOn
	//	
	// 	return this
	// }

	override fun parseFrom(src: User): UserModel {
		userGuid = src.userGuid
		name = src.name
		loggedOn = src.loggedOn
		
		return this
	}

	override fun convert(): User {
		return User.newBuilder()
			.setUserGuid(userGuid)
			.setName(name)
			.setLoggedOn(loggedOn)
			.build()
	}

	// override fun toString(): String {
	// 	val sb = StringBuilder()
	//	
	// 	return sb.append("{")
	// 		.append("name='").append(name).append("'")
	// 		.append("userGuid='").append(userGuid).append("'")
	// 		.append("loggedOn=").append(loggedOn)
	// 		.append("}")
	// 		.toString()
	//	
	// 	// return Json.stringify(serializer(), this)
	// }
}