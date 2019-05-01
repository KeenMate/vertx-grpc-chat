package com.keenmate.chat_01.services

import com.keenmate.chat_01.*
import com.keenmate.chat_01.models.ClientModel
import com.keenmate.chat_01.models.ConnectRequestModel
import io.grpc.stub.StreamObserver
import io.vertx.core.Future
import io.vertx.core.Vertx

class UserServiceImpl(private val vertx: Vertx) : UserProviderGrpc.UserProviderImplBase() {
	private val eventBus = vertx.eventBus()

	override fun connect(request: ConnectRequest?, responseObserver: StreamObserver<Client>?) {
		println("Requested to connect from new user: ${request!!.name}")
		eventBus.send<String>(
			Constants.Dao.AddClient,
			ConnectRequestModel().parseFrom(request).toString()
		) {
			if (it.succeeded()) {
				responseObserver!!.onNext(
					ClientModel()
						.parseFrom(it.result().body())
						.convert()
				)
				
				responseObserver.onCompleted()
			} else {
				println("Error occurred")
				responseObserver!!.onError(it.cause())
			}
		}
	}

	override fun getUser(request: RetrieveClientRequest?, responseObserver: StreamObserver<Client>?) {
		super.getUser(request, responseObserver)
	}

	override fun getUsers(request: Empty?, responseObserver: StreamObserver<Client>?) {
		vertx.executeBlocking({ _: Future<Unit> ->
			// stream existing clients
			eventBus.send<String>(Constants.Dao.GetClients, "") { referrerMsg ->
				val tmpConsumer = eventBus.consumer<String>(referrerMsg.result().body()) {
					responseObserver!!.onNext(ClientModel()
						.parseFrom(it.body())
						.convert())
				}
			}

			// stream upcoming clients
			eventBus.consumer<String>(Constants.Dao.ClientAdded) {
				responseObserver!!.onNext(ClientModel().parseFrom(it.body()).convert())
			}
		}, false) {			
			responseObserver!!.onCompleted()
		}
	}
}