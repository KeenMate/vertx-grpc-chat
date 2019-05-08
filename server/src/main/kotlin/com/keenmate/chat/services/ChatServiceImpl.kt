package com.keenmate.chat.services

import com.keenmate.chat.*
import com.keenmate.chat.models.*
import io.grpc.stub.StreamObserver
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList

class ChatServiceImpl(private val vertx: Vertx) : ChatProviderGrpc.ChatProviderImplBase() {
	private val eventBus: EventBus = this.vertx.eventBus()

	// unary call
	override fun createRoom(request: Room?, responseObserver: StreamObserver<Room>?) {
		println("Creating room")
		eventBus.send<RoomModel>(Constants.Dao.AddRoom,
			RoomModel().parseFrom(request!!)
		) {
			responseObserver!!
				.onNext(
					it.result().body().convert()
				)
			responseObserver.onCompleted()
		}
	}
	
	// server-side streaming
	override fun joinRoom(request: JoinRoomRequest?, responseObserver: StreamObserver<ChatChange>?) {
		println("Joining room")
		val joinRoomRequestModel = JoinRoomRequestModel().parseFrom(request!!)
		
		// notify already connected clients
		val chatChange = ChatChangeModel()
		chatChange.theChange = ChatChange.TheChangeCase.CLIENTCONNECTED
		chatChange.clientConnected = joinRoomRequestModel.client
		chatChange.room = joinRoomRequestModel.room
		eventBus.send(Constants.Dao.ChangeChat, chatChange)
		
		// fetch last 50 messages of room
		eventBus.send<ArrayList<MessageModel>>(
			Constants.Dao.GetMessages,
			joinRoomRequestModel) {
			it.result().body()
				.forEach { msg ->
					val chatChange = ChatChangeModel()
					chatChange.msg = msg
					chatChange.room = msg.room
					chatChange.theChange = ChatChange.TheChangeCase.MSG
					
					responseObserver!!.onNext(chatChange.convert())
				}
		}
		
		// listen for all upcoming changes
		eventBus.consumer<ChatChangeModel>(Constants.Dao.ClientChatChange(joinRoomRequestModel.client.clientGuid)) {
			println("Received notification from eventbus for client ${joinRoomRequestModel.client.name}")
			
			println("Sending message to response observer")
			responseObserver!!.onNext(it.body().convert())
		}
	}

	// unary call
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
			println("Room has been added")
			responseObserver!!.onNext(it.body().convert())
		}
	}

	override fun sendMessage(request: Message?, responseObserver: StreamObserver<Empty>?) {
		println("Received message")
		
		val parsedMessage = MessageModel().parseFrom(request!!)
		println("Message parsed")
		val chatChangeModel = ChatChangeModel()
		chatChangeModel.theChange = ChatChange.TheChangeCase.MSG
		chatChangeModel.msg = parsedMessage
		chatChangeModel.room = parsedMessage.room
		
		println("ChatChangeModel created")

		eventBus.send<Unit>(Constants.Dao.ChangeChat, chatChangeModel) {
			responseObserver!!.onCompleted()
		}
	}
}