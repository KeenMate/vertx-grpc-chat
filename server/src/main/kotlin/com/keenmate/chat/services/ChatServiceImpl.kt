package com.keenmate.chat.services

import com.keenmate.chat.*
import com.keenmate.chat.models.*
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList

class ChatServiceImpl(private val vertx: Vertx) : ChatProviderGrpc.ChatProviderImplBase() {
	private val eventBus: EventBus = this.vertx.eventBus()

	// server-side streaming
	override fun listen(request: StringMessage?, responseObserver: StreamObserver<ChatEvent>?) {
		// users
		listenUsers(responseObserver)

		// rooms
		listenRooms(responseObserver)

		// messages
		listenChatChanges(request!!.value, responseObserver)
	}

	// override fun getMessages(request: StringMessage?, responseObserver: StreamObserver<ChatChange>?) {
	// 	val consumer = eventBus.consumer<ChatChangeModel>(Constants.Dao.ClientChatChange(request!!.value))
	//
	// 	consumer.handler {
	// 		try {
	// 			responseObserver!!.onNext(it.body().convert())
	// 		} catch (ex: StatusRuntimeException) {
	// 			consumer.unregister()
	// 		}
	// 	}
	// }
	//
	// override fun getRooms(request: Empty?, responseObserver: StreamObserver<Room>?) {
	// 	println("Creating rooms hook")
	//
	// 	// stream existing rooms
	// 	eventBus.send<ArrayList<RoomModel>>(Constants.Dao.GetRooms, "") {
	// 		it.result().body()
	// 			.forEach { room ->
	// 				responseObserver!!.onNext(room.convert())
	// 			}
	// 	}
	//
	// 	// stream upcoming rooms
	// 	val consumer = eventBus.consumer<RoomModel>(Constants.Dao.RoomAdded)
	// 	consumer.handler {
	// 		try {
	// 			responseObserver!!.onNext(it.body().convert())
	// 			println("Sent new room to user")
	// 		} catch (ex: StatusRuntimeException) {
	// 			consumer.unregister()
	// 		}
	// 	}
	// }

	override fun createRoom(request: NewRoomRequest?, responseObserver: StreamObserver<Room>?) {
		println("Creating room")
		eventBus.send<RoomModel>(Constants.Dao.AddRoom,
			NewRoomRequestModel().parseFrom(request!!)
		) {
			responseObserver!!
				.onNext(
					it.result().body().convert()
				)
			responseObserver.onCompleted()
		}
	}

	override fun joinRoom(request: JoinRoomRequest?, responseObserver: StreamObserver<Status>?) {
		println("Joining room")
		val joinRoomRequestModel = JoinRoomRequestModel().parseFrom(request!!)

		// send new-user chat change to system
		eventBus.send<UserModel>(Constants.Dao.GetClient, joinRoomRequestModel.userGuid) {
			val newUserChatEvent = ChatEventModel()
			newUserChatEvent.change = TheChange.NEW

			newUserChatEvent.valueCase = ChatEvent.ValueCase.USER
			newUserChatEvent.user = it.result().body()

			newUserChatEvent.roomId = joinRoomRequestModel.roomId

			eventBus.send<Unit>(Constants.Dao.ChangeChat, newUserChatEvent) {
				// fetch last 50 messages
				eventBus.send<ArrayList<MessageModel>>(
					Constants.Dao.GetMessages,
					joinRoomRequestModel.roomId) {
					// todo: send message to corresponding consumers
					it.result()
						.body()
						.map {
							val messageChatEvent = ChatEventModel()
							messageChatEvent.change = TheChange.EXISTING
							messageChatEvent.message = it
							messageChatEvent.valueCase = ChatEvent.ValueCase.MESSAGE
							messageChatEvent.roomId = it.roomId

							return@map messageChatEvent
						}
						.forEach {
							eventBus.publish(Constants.Dao.ClientChatChange(newUserChatEvent.user!!.userGuid), it)
						}

					responseObserver!!.onNext(Status.newBuilder()
						.setIsOk(true)
						.build())
					responseObserver.onCompleted()
				}
			}
		}
	}

	override fun sendMessage(request: Message?, responseObserver: StreamObserver<Status>?) {
		println("Received message")

		val parsedMessage = MessageModel().parseFrom(request!!)
		parsedMessage.sent = Date().time
		
		println("Message parsed")
		val chatChangeModel = ChatEventModel()
		chatChangeModel.change = TheChange.NEW

		chatChangeModel.message = parsedMessage
		chatChangeModel.valueCase = ChatEvent.ValueCase.MESSAGE

		chatChangeModel.roomId = parsedMessage.roomId

		eventBus.send<Unit>(Constants.Dao.ChangeChat, chatChangeModel) {
			responseObserver!!.onNext(Status.newBuilder()
				.setIsOk(true)
				.build()
			)
			responseObserver.onCompleted()
		}
	}

	private fun listenUsers(responseObserver: StreamObserver<ChatEvent>?) {
		println("Users streaming is not implemented yet")
	}

	private fun listenRooms(responseObserver: StreamObserver<ChatEvent>?) {
		println("Creating rooms hook")

		// stream existing rooms
		println("Streaming existing rooms")
		eventBus.send<ArrayList<RoomModel>>(Constants.Dao.GetRooms, Unit) {
			val response = ChatEvent.newBuilder()
				.setChange(TheChange.EXISTING)
			it.result().body()
				.forEach { room ->
					response.room = room.convert()

					responseObserver!!.onNext(response.build())
				}
		}

		// stream upcoming rooms
		val newRoomResponse = ChatEvent.newBuilder()
			.setChange(TheChange.NEW)
		val consumer = eventBus.consumer<RoomModel>(Constants.Dao.RoomAdded)
		consumer.handler {
			try {
				responseObserver!!.onNext(
					newRoomResponse.setRoom(it.body().convert())
						.build()
				)
				println("Sent new room to user")
			} catch (ex: StatusRuntimeException) {
				println("Could not send new room to user - unregistering consumer")
				consumer.unregister()
			}
		}
	}

	private fun listenChatChanges(userGuid: String, responseObserver: StreamObserver<ChatEvent>?) {
		// todo: send existing messages & users
		
		val consumer = eventBus.consumer<ChatEventModel>(
			Constants.Dao.ClientChatChange(userGuid)
		)

		consumer.handler {
			println("Room Id of ChatEvent before send: ${it.body().roomId}")
			
			try {
				responseObserver!!.onNext(it.body().convert())
			} catch (ex: StatusRuntimeException) {
				consumer.unregister()
			}
		}
	}


}