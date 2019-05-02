package com.keenmate.chat.models

import com.keenmate.chat.Client
import com.keenmate.chat.models.base.IModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

@Serializable
class ClientModel: IModel<Client> {
	var clientGuid: String = "" 
	var name: String = ""
	var loggedOn: Long = 0
	
	@UseExperimental(UnstableDefault::class)
	override fun parseFrom(src: String): ClientModel {
		val tmp = Json.parse(serializer(), src)
		
		clientGuid = tmp.clientGuid
		name = tmp.name
		loggedOn = tmp.loggedOn
		
		return this
	}

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

	override fun toString(): String {
		return Json.stringify(serializer(), this)
	}
}