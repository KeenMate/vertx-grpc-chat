package com.keenmate.chat.services

import com.keenmate.chat.*
import com.keenmate.chat.models.*
import io.grpc.stub.StreamObserver
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.core.eventbus.sendAwait
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList
import java.lang.Exception

class ChatServiceImpl(private val vertx: Vertx) : ChatProviderGrpc.ChatProviderImplBase() {
	private val eventBus: EventBus = this.vertx.eventBus()

	// server-side streaming
	override fun getMessages(request: StringMessage?, responseObserver: StreamObserver<ChatChange>?) {
		eventBus.consumer<ChatChangeModel>(Constants.Dao.ClientChatChange(request!!.value)) {
			responseObserver!!.onNext(it.body().convert())
		}
	}

	override fun getClientsForRoom(request: Int32Message?, responseObserver: StreamObserver<Clients>?) {
		eventBus.send<ArrayList<ClientModel>>(Constants.Dao.GetClientsForRoom, request!!.value) {
			val response = Clients.newBuilder()
			response.addAllClients(
				it.result()
					.body()
					.map { it.convert() }
					.asIterable())

			responseObserver!!.onNext(response.build())
			println("Sent all clients for given room")
			responseObserver.onCompleted()
		}
	}

	// unary call
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
			println("Room created")
		}
	}

	// server-side streaming
	override fun joinRoom(request: JoinRoomRequest?, responseObserver: StreamObserver<Messages>?) {
		println("Joining room")
		val joinRoomRequestModel = JoinRoomRequestModel().parseFrom(request!!)

		// send chat chcange to system
		eventBus.send<ClientModel>(Constants.Dao.GetClient, joinRoomRequestModel.clientGuid) {
			val chatChangeNewClient = ChatChangeModel()
			chatChangeNewClient.theChange = ChatChange.TheChangeCase.CLIENTCONNECTED

			chatChangeNewClient.clientConnected = it.result().body()

			chatChangeNewClient.roomId = joinRoomRequestModel.roomId

			eventBus.send<Unit>(Constants.Dao.ChangeChat, chatChangeNewClient) {
				// fetch last 50 messages
				eventBus.send<ArrayList<MessageModel>>(
					Constants.Dao.GetMessages,
					joinRoomRequestModel) {
					responseObserver!!.onNext(
						Messages.newBuilder()
							.addAllMessages(
								it.result().body()
									.map { it.convert() }
									.asIterable())
							.build()
					)
					
					responseObserver.onCompleted()
				}
			}

			// listen for all upcoming changes
			// eventBus.consumer<ChatChangeModel>(Constants.Dao.ClientChatChange(joinRoomRequestModel.clientGuid)) {
			// 	println("Received notification from eventbus for client '${chatChangeNewClient.clientConnected.name}'")
			//
			// 	println("Sending message to response observer")
			// 	responseObserver!!.onNext(it.body().convert())
			// 	println("Message sent to client")
			// }
		}
	}

	override fun getRooms(request: Empty?, responseObserver: StreamObserver<Room>?) {
		println("Creating rooms hook")

		// stream existing rooms
		eventBus.send<ArrayList<RoomModel>>(Constants.Dao.GetRooms, "") {
			it.result().body()
				.forEach { room ->
					responseObserver!!.onNext(room.convert())
				}
		}

		// stream upcoming rooms
		eventBus.consumer<RoomModel>(Constants.Dao.RoomAdded) {
			responseObserver!!.onNext(it.body().convert())
			println("Sent new room to client")
		}
	}

	override fun sendMessage(request: Message?, responseObserver: StreamObserver<Empty>?) {
		println("Received message")

		val parsedMessage = MessageModel().parseFrom(request!!)
		println("Message parsed")
		val chatChangeModel = ChatChangeModel()
		chatChangeModel.theChange = ChatChange.TheChangeCase.MSG
		chatChangeModel.msg = parsedMessage

		chatChangeModel.roomId = parsedMessage.roomId

		eventBus.send<Unit>(Constants.Dao.ChangeChat, chatChangeModel) {
			responseObserver!!.onCompleted()
			println("Message sent")
		}
	}
}