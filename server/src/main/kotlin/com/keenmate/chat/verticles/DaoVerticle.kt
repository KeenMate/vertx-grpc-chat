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
	private val consumerMap: ConcurrentHashMap<UUID, MutableCollection<MessageConsumer<*>>> = ConcurrentHashMap()

	// in-memory store of objects
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
					(messages.filter { msg ->
						msg.room.roomId == it.body().room.roomId
					}
						.takeLast(50))
				)
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetClients) {
				it.reply(clients)
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetRooms) {
				it.reply(rooms)
			}
		)

		list.add(eventBus
			.consumer<ChatChangeModel>(Constants.Dao.ChangeChat) {
				println("ChatChange received from event bus. message: ")
				println(it.body())
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
					it.roomId == chatChange.room.roomId
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
			.consumer<RoomModel>(Constants.Dao.AddRoom) {
				val room = it.body()

				// check if title exists
				val existingRoom = rooms.firstOrNull { it.title == room.title }
				if (existingRoom == null) {
					room.roomId = ++idCounter

					rooms.add(room)
					it.reply(room)
					eventBus.publish(Constants.Dao.RoomAdded, room)
				} else {
					it.fail(0, "Room exists")
				}
			}
		)

		// creates empty UUID and puts all DAO's consumers to it
		consumerMap[UUID(0, 0)] = list
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
			rooms.first { it.roomId == chatChange.room.roomId }
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
		// add client to target room (if he is not already there)
		rooms.find { it.roomId == chatChange.room.roomId }!!
			.clients.add(chatChange.clientConnected)

		// add this room to client's connected rooms
		clients.find { it.clientGuid == chatChange.clientConnected.clientGuid }!!
			.connectedRooms.add(chatChange.room)
	}

	private fun handleClientDisconnected(chatChange: ChatChangeModel) {
		// remove client from this room
		rooms.find { it.roomId == chatChange.room.roomId }!!
			.clients.removeIf { it.clientGuid == chatChange.clientDisconnected.clientGuid }

		// remove this room from client's connected rooms
		clients.find { it.clientGuid == chatChange.clientDisconnected.clientGuid }!!
			.connectedRooms.removeIf { it.roomId == chatChange.room.roomId }
	}

	override fun start(startFuture: Future<Void>) {
		startFuture.complete()
	}

	override fun stop(stopFuture: Future<Void>) {
		// unregister consumers for UUID(0, 0)
		consumerMap[UUID(0, 0)]?.removeAll { true }
	}
}