package com.keenmate.chat.models

import com.keenmate.chat.Client
import com.keenmate.chat.models.base.IModel

class ClientModel: IModel<Client> {
	var clientGuid: String = "" 
	var name: String = ""
	var loggedOn: Long = 0
	
	// non-gRPC props
	var connectedRooms: ArrayList<RoomModel> = ArrayList()
	
	// override fun parseFrom(src: String): ClientModel {
	//	
	// 	// clientGuid = tmp.clientGuid
	// 	// name = tmp.name
	// 	// loggedOn = tmp.loggedOn
	//	
	// 	return this
	// }

	override fun parseFrom(src: Client): ClientModel {
		clientGuid = src.clientGuid
		name = src.name
		loggedOn = src.loggedOn
		
		return this
	}

	override fun convert(): Client {
		return Client.newBuilder()
			.setClientGuid(clientGuid)
			.setName(name)
			.setLoggedOn(loggedOn)
			.build()
	}

	// override fun toString(): String {
	// 	val sb = StringBuilder()
	//	
	// 	return sb.append("{")
	// 		.append("name='").append(name).append("'")
	// 		.append("clientGuid='").append(clientGuid).append("'")
	// 		.append("loggedOn=").append(loggedOn)
	// 		.append("}")
	// 		.toString()
	//	
	// 	// return Json.stringify(serializer(), this)
	// }
}