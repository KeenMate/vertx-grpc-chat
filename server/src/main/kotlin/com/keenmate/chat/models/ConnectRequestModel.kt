package com.keenmate.chat.models

import com.keenmate.chat.ConnectRequest
import com.keenmate.chat.models.base.IModel

class ConnectRequestModel: IModel<ConnectRequest> {

	var name: String = ""
	
	// override fun parseFrom(src: String): ConnectRequestModel {
	// 	val tmp = Json.parse(serializer(), src)
	//	
	// 	name = tmp.name
	//	
	// 	return this
	// }

	override fun parseFrom(src: ConnectRequest): ConnectRequestModel {
		name = src.name
		
		return this
	}
	
	override fun convert(): ConnectRequest {
		return ConnectRequest.newBuilder()
			.setName(name)
			.build()
	}

	// override fun toString(): String {
	// 	return Json.stringify(serializer(), this)
	// }
}