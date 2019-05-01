package com.keenmate.chat_01.services

import com.keenmate.chat_01.*
import com.keenmate.chat_01.models.*
import io.grpc.stub.StreamObserver
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import scala.collection.immutable.Stream

class ChatServiceImpl(private val vertx: Vertx) : ChatProviderGrpc.ChatProviderImplBase() {
	private val eventBus: EventBus = this.vertx.eventBus()

	// unary call
	override fun createRoom(request: Room?, responseObserver: StreamObserver<Room>?) {
		eventBus.send<String>(Constants.Dao.AddRoom, RoomModel().parseFrom(request!!).toString()) {
			responseObserver!!
				.onNext(
					RoomModel()
						.parseFrom(it.result().body())
						.convert()
				)
			responseObserver.onCompleted()
		}
	}
	
	// server-side streaming
	override fun joinRoom(request: JoinRoomRequest?, responseObserver: StreamObserver<ChatChange>?) {
		vertx.executeBlocking({ fut: Future<Unit> ->
			eventBus.send<String>(
				Constants.Dao.GetMessages,
				JoinRoomRequestModel().parseFrom(request!!).toString()) {

				val tmpConsumer = eventBus.consumer<String>(it.result().body())
					.handler {
						responseObserver!!.onNext(
							ChatChangeModel().parseFrom(it.body()).convert()
						)
					}
			}
			
			eventBus.consumer<String>(Constants.Dao.MessageAdded) {
				val parsed = MessageModel().parseFrom(it.body())
				
				if (parsed.roomId == request.roomId) {
					val chatChangeModel = ChatChangeModel()
					
					chatChangeModel.msg = parsed
					chatChangeModel.theChange = ChatChange.TheChangeCase.MSG

					responseObserver!!.onNext(chatChangeModel.convert())
				}
			}
		}, false, {
			responseObserver!!.onCompleted()
		})
		
		eventBus.consumer<String>(Constants.Dao.MessageAdded)
	}

	// server-side streaming
	override fun roomsHook(request: Empty?, responseObserver: StreamObserver<Room>?) {
		println("Requested rooms hook")
		
		// stream existing rooms
		vertx.executeBlocking({ _: Future<Unit> ->
			eventBus.send<String>(Constants.Dao.GetRooms, "") { tmpAddress ->
				eventBus.consumer<String>(tmpAddress.result().body()) {
					responseObserver!!.onNext(RoomModel()
						.parseFrom(it.body())
						.convert())
				}
			}

			// stream upcoming rooms
			eventBus.consumer<String>(Constants.Dao.RoomAdded) {
				println("Room has been added")
				responseObserver!!.onNext(RoomModel().parseFrom(it.body()).convert())
			}
		}, false) {
			responseObserver!!.onCompleted()
		}
	}

	override fun sendMessage(request: Message?, responseObserver: StreamObserver<Message>?) {
		eventBus.send(Constants.Dao.AddMessage, MessageModel().parseFrom(request!!).toString())
	}
}