package com.keenmate.chat.verticles

import com.keenmate.chat.ChatEvent
import com.keenmate.chat.Constants
import com.keenmate.chat.TheChange
import com.keenmate.chat.models.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.impl.ConcurrentHashSet
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class DaoVerticle(private val eventBus: EventBus) : AbstractVerticle() {
	private var idCounter = 0

	// for storing observables and eventbus's consumers/publishers..
	// might not be neccessary at all
	private val consumerMap: ConcurrentHashMap<String, MutableList<MessageConsumer<*>>> = ConcurrentHashMap()

	// in-memory store of objects
	// this is just temporary until other data-store comes as a replace..
	// this can be either Database or some messaging system like Apache's kafka
	// private val users: ArrayList<UserModel> = ArrayList()
	// private val messages: ArrayList<MessageModel> = ArrayList()
	// private val rooms: ArrayList<RoomModel> = ArrayList()

	private val users: ConcurrentHashSet<UserModel> = ConcurrentHashSet()
	private val messages: ConcurrentHashSet<MessageModel> = ConcurrentHashSet()
	private val rooms: ConcurrentHashSet<RoomModel> = ConcurrentHashSet()

	init {
		// add all consumers to consumers map 
		val list = mutableListOf<MessageConsumer<*>>()

		list.add(eventBus
			.consumer<Int>(Constants.Dao.GetMessages) {
				// return existing messages
				it.reply(
					messages
						.filter { msg ->
							msg.roomId == it.body()
						}
						.takeLast(50)
				)
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetClients) {
				it.reply(ArrayList(users))
			}
		)

		list.add(eventBus
			.consumer<Int>(Constants.Dao.GetClientsForRoom) { msg ->
				msg.reply(
					rooms.find { it.roomId == msg.body() }?.users
						?.distinctBy { it.userGuid }
						?: ArrayList<UserModel>()
				)
			}
		)

		list.add(eventBus
			.consumer<String>(Constants.Dao.GetClient) { msg ->
				println("fetching user: ${msg.body()}")
				msg.reply(users.first { it.userGuid == msg.body() })
			}
		)

		list.add(eventBus
			.consumer<Unit>(Constants.Dao.GetRooms) {
				it.reply(ArrayList(rooms))
			}
		)

		list.add(eventBus
			.consumer<Int>(Constants.Dao.GetRoom) { msg ->
				msg.reply(rooms.first { it.roomId == msg.body() })
			}
		)

		list.add(eventBus
			.consumer<ChatEventModel>(Constants.Dao.ChangeChat) {
				println("ChatChange received from event bus. message: ")

				val chatEvent = it.body()

				when (chatEvent.valueCase) {
					ChatEvent.ValueCase.MESSAGE -> {
						when (chatEvent.change) {
							TheChange.NOTSET -> {
								println("Error: change for message was not set")
							}
							TheChange.MODIFIED, TheChange.NEW -> handleMessageChange(chatEvent)
							TheChange.DELETED -> TODO()
							TheChange.EXISTING -> {
								println("Error: Client should not send 'Existing' message")
							}
							TheChange.UNRECOGNIZED -> {
								println("Error: TheChange for message was not recognized")
							}
						}
					}
					ChatEvent.ValueCase.USER -> {
						when (chatEvent.change) {
							TheChange.NOTSET -> {
								println("Error: change for user was not set")
							}
							TheChange.NEW -> {
								handleUserJoined(chatEvent)
							}
							TheChange.MODIFIED -> {
								TODO()
							}
							TheChange.DELETED -> {
								handleUserDisconnected(chatEvent)
							}
							TheChange.EXISTING -> {
								println("Error: Client should not send 'Existing' user")
							}
							TheChange.UNRECOGNIZED -> {
								println("Error: TheChange for user was not recognized")
							}
						}
					}
					ChatEvent.ValueCase.VALUE_NOT_SET -> {
						println("Error: ValueCase was not set..")
					}
					ChatEvent.ValueCase.ROOM -> {
						when (chatEvent.change) {
							TheChange.NOTSET -> println("Error: change for room was not set")
							TheChange.NEW -> {
							}
							TheChange.MODIFIED -> {
								handleModifyRoom(chatEvent)
							}
							TheChange.DELETED -> TODO()
							TheChange.EXISTING -> {
								println("Error: Client should not send 'Existing' room")
							}
							TheChange.UNRECOGNIZED -> {
								println("Error: TheChange for room was not recognized")
							}
						}
					}
				}


				println("About to handle notifications")
				// notify related users
				rooms.find {
					it.roomId == chatEvent.roomId
				}?.users?.forEach {
					println("notifying user ${it.name} about ChatChange")
					eventBus.publish(Constants.Dao.ClientChatChange(
						it.userGuid
					), chatEvent)
				}

				it.reply(Unit)
			}
		)

		list.add(eventBus
			.consumer<ConnectRequestModel>(Constants.Dao.AddUser) {
				val connectRequestModel = it.body()

				var user = users.firstOrNull {
					it.name == connectRequestModel.name
				}

				if (user == null) {
					user = UserModel()
					user.userGuid = UUID.randomUUID().toString()
					user.loggedOn = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
					user.name = connectRequestModel.name

					handleNewUser(user)
				}

				it.reply(user)
			}
		)

		list.add(eventBus
			.consumer<NewRoomRequestModel>(Constants.Dao.AddRoom) {
				val newRoomRequest = it.body()

				val newRoom = RoomModel()
				newRoom.title = newRoomRequest.title

				val creatorFound = users.find { it.userGuid == newRoomRequest.creatorGuid }
				if (creatorFound != null)
					newRoom.creator = creatorFound

				// check if title exists
				val existingRoom = rooms.firstOrNull { it.title == newRoomRequest.title }
				if (existingRoom == null) {
					newRoom.roomId = ++idCounter

					rooms.add(newRoom)
					eventBus.publish(Constants.Dao.RoomAdded, newRoom)
					it.reply(newRoom)
				} else {
					it.fail(0, "Room exists")
				}
			}
		)

		// puts all DAO's consumers to map
		consumerMap[""] = list
	}

	private fun handleModifyRoom(chatEvent: ChatEventModel) {
		if (chatEvent.room == null)
			return
		
		val found = rooms.find { it.roomId == chatEvent.roomId } ?: return

		// update existing room
		found.title = chatEvent.room!!.title
		found.private = chatEvent.room!!.private
		
		found.users.forEach { user ->
			val room = user.connectedRooms.first { it.roomId == found.roomId }
			
			room.title = found.title
			room.private = found.private
		}
	}

	private fun handleNewUser(user: UserModel) {
		val found = users.find { it.name == user.name || it.userGuid == user.userGuid }

		if (found == null)
			users.add(user)

		val userChange = UserChangeModel()
		userChange.change = TheChange.NEW
		userChange.user = user

		// todo: propagate new user
		// eventBus.publish(Constants.Dao.ClientChanged, userChange)
	}

	/**
	 * Handles new messages as well as existing (by updating them)
	 */
	private fun handleMessageChange(chatEvent: ChatEventModel) {
		if (chatEvent.message == null)
			return

		println("Id of received message: ${chatEvent.message!!.messageId}")

		// new message
		if (chatEvent.message!!.messageId == 0) {
			chatEvent.message!!.messageId = idCounter++

			// todo: validate message props (target room etc.)

			// add to messages
			messages.add(chatEvent.message)

			// add to room
			rooms.first { it.roomId == chatEvent.roomId }
				.messages.add(chatEvent.message!!)
		}

		// message exists
		val existingMsg = messages.find { it.messageId == chatEvent.message!!.messageId }

		if (existingMsg != null) {
			// update message's parts
			existingMsg.content = chatEvent.message!!.content
		}
	}

	private fun handleUserJoined(chatEvent: ChatEventModel) {
		val room = rooms.find { it.roomId == chatEvent.roomId }

		if (room == null) {
			println("Room doesnt exist anymore")
			return
		}

		// add user to target room (even if user is already there..)
		if (chatEvent.user == null)
			return

		if (room.users.find { it.userGuid == chatEvent.user!!.userGuid } == null)
		room.users.add(chatEvent.user!!)

		// add this room to user's connected rooms
		val user = users.find { it.userGuid == chatEvent.user!!.userGuid }!!

		if (user.connectedRooms.find { it.roomId == chatEvent.roomId } == null)
			user.connectedRooms.add(room)
	}

	private fun handleUserDisconnected(chatEvent: ChatEventModel) {
		if (chatEvent.user == null)
			return

		val room = rooms.find { it.roomId == chatEvent.roomId }!!

		// remove user from this room
		room.users.removeIf { it.userGuid == chatEvent.user!!.userGuid }

		// remove this room from user's connected rooms
		users.find { it.userGuid == chatEvent.user!!.userGuid }!!
			.connectedRooms.removeIf { it.roomId == chatEvent.roomId }
	}

	// override fun start(startFuture: Future<Void>) {
	// 	startFuture.complete()
	// }

	override fun stop(stopFuture: Future<Void>) {
		// unregister consumers for empty UUID
		
		for (i: Int in consumerMap[""]?.count()?.rangeTo(0) ?: IntRange(0, 0)) {
			consumerMap[""]?.elementAt(i)?.unregister()
			consumerMap[""]?.removeAt(i)
		}
	}
}