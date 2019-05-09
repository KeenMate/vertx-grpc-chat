package com.keenmate.chat.verticles

import com.keenmate.chat.ChatChange
import com.keenmate.chat.Constants
import com.keenmate.chat.models.*
import io.reactivex.subjects.PublishSubject
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.kotlin.core.json.array
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class DaoVerticle(private val eventBus: EventBus) : AbstractVerticle() {
	private var idCounter = 0

	// for storing observables and eventbus's consumers/publishers..
	// might not be neccessary at all
	private val consumerMap: ConcurrentHashMap<String, MutableCollection<MessageConsumer<*>>> = ConcurrentHashMap()

	// in-memory store of objects
	// this is just temporary until other data-store comes as replace..
	// this can be either Database or some messaging system like Apache's kafka
	private val clients: ArrayList<ClientModel> = ArrayList()
	private val messages: ArrayList<MessageModel> = ArrayList()
	private val rooms: ArrayList<RoomModel> = ArrayList()

	init {
		// add all consumers to consumers map 
		val list = mutableListOf<MessageConsumer<*>>()

		list.add(eventBus
			.consumer<JoinRoomRequestModel>(Constants.Dao.GetMessages) {
				// return existing messages
				it.reply(
					messages.filter { msg ->
						msg.roomId == it.body().roomId
					}.takeLast(50)
				)
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetClients) {
				it.reply(clients)
			}
		)
		
		list.add(eventBus
			.consumer<Int>(Constants.Dao.GetClientsForRoom) { msg ->
				msg.reply(
					rooms.find { it.roomId == msg.body()}?.clients
						?.distinctBy { it.clientGuid }
						?: ArrayList<ClientModel>()
				)
			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.GetClient) { msg ->
				println("fetching client: ${msg.body()}")
				msg.reply(clients.first { it.clientGuid == msg.body() })
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetRooms) {
				it.reply(rooms)
			}
		)

		list.add(eventBus
			.consumer<Int>(Constants.Dao.GetRoom) { msg ->
				msg.reply(rooms.first { it.roomId == msg.body() })
			}
		)

		list.add(eventBus
			.consumer<ChatChangeModel>(Constants.Dao.ChangeChat) {
				println("ChatChange received from event bus. message: ")
				val chatChange = it.body()

				when (chatChange.theChange) {
					ChatChange.TheChangeCase.MSG -> {
						println("New message handling")
						handleMessageChange(chatChange)
						println("New message handled")
					}
					ChatChange.TheChangeCase.CLIENTCONNECTED -> {
						println("Client connected handling")
						handleClientConnected(chatChange)
						println("Client connected handled")
					}
					ChatChange.TheChangeCase.CLIENTDISCONNECTED -> {
						println("Client disconnected handling")
						handleClientDisconnected(chatChange)
						println("Client disconnected handled")
					}
					ChatChange.TheChangeCase.THECHANGE_NOT_SET -> {
						println("The Change not set")
						it.reply("")
					}
				}


				println("About to handle notifications")
				// notify related clients
				rooms.find {
					it.roomId == chatChange.roomId
				}?.clients?.forEach {
					println("notifying client ${it.name} about ChatChange")
					eventBus.send(Constants.Dao.ClientChatChange(
						it.clientGuid
					), chatChange)
				}
			}
		)

		list.add(eventBus
			.consumer<ConnectRequestModel>(Constants.Dao.AddClient) {
				val client = it.body()

				var existingClient = clients.firstOrNull {
					return@firstOrNull it.name == client.name
				}

				if (existingClient == null) {
					existingClient = ClientModel()
					existingClient.clientGuid = UUID.randomUUID().toString()
					existingClient.loggedOn = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
					existingClient.name = client.name

					clients.add(existingClient)
				}

				it.reply(existingClient)
			}
		)

		list.add(eventBus
			.consumer<NewRoomRequestModel>(Constants.Dao.AddRoom) {
				val newRoomRequest = it.body()
				
				val newRoom = RoomModel()
				newRoom.title = newRoomRequest.title

				// check if title exists
				val existingRoom = rooms.firstOrNull { it.title == newRoomRequest.title }
				if (existingRoom == null) {
					newRoom.roomId = ++idCounter

					rooms.add(newRoom)
					it.reply(newRoom)
					eventBus.publish(Constants.Dao.RoomAdded, newRoom)
				} else {
					it.fail(0, "Room exists")
				}
			}
		)

		// puts all DAO's consumers to map
		consumerMap[""] = list
	}

	private fun handleMessageChange(chatChange: ChatChangeModel) {
		// new message
		println("Id of received message: ${chatChange.msg.messageId}")
		if (chatChange.msg.messageId == 0) {
			chatChange.msg.messageId = idCounter++

			// todo: validate message props (target room etc.)

			// add to messages
			messages.add(chatChange.msg)

			// add to room
			rooms.first { it.roomId == chatChange.roomId }
				.messages.add(chatChange.msg)
		}

		// message exists
		val existingMsg = messages.find { it.messageId == chatChange.msg.messageId }

		if (existingMsg != null) {
			// update message's parts
			existingMsg.content = chatChange.msg.content
		}
	}

	private fun handleClientConnected(chatChange: ChatChangeModel) {
		val room = rooms.find { it.roomId == chatChange.roomId }
		
		if (room == null) {
			println("Room doesnt exist anymore")
			return
		}
		
		// add client to target room (even if client is already there..)
		room.clients.add(chatChange.clientConnected)

		// add this room to client's connected rooms
		val client = clients.find { it.clientGuid == chatChange.clientConnected.clientGuid }!!
		
		if (client.connectedRooms.find { it.roomId == chatChange.roomId } == null)
			client.connectedRooms.add(room)
	}

	private fun handleClientDisconnected(chatChange: ChatChangeModel) {
		val room = rooms.find { it.roomId == chatChange.roomId }!!

		// remove client from this room
		room.clients.removeIf { it.clientGuid == chatChange.clientDisconnected.clientGuid }

		// remove this room from client's connected rooms
		clients.find { it.clientGuid == chatChange.clientDisconnected.clientGuid }!!
			.connectedRooms.removeIf { it.roomId == chatChange.roomId }
	}

	override fun start(startFuture: Future<Void>) {
		startFuture.complete()
	}

	override fun stop(stopFuture: Future<Void>) {
		// unregister consumers for empty UUID
		consumerMap[""]?.removeAll { true }
	}
}