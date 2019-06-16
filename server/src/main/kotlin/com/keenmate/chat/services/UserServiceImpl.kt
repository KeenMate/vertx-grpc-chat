package com.keenmate.chat.services

import com.keenmate.chat.*
import com.keenmate.chat.models.UserModel
import com.keenmate.chat.models.ConnectRequestModel
import io.grpc.stub.StreamObserver
import io.vertx.core.Vertx

class UserServiceImpl(vertx: Vertx) : UserProviderGrpc.UserProviderImplBase() {
	private val eventBus = vertx.eventBus()

	override fun connect(request: ConnectRequest?, responseObserver: StreamObserver<User>?) {
		println("Requested to connect from new user: ${request!!.name}")
		eventBus.send<UserModel>(
			Constants.Dao.AddUser,
			ConnectRequestModel().parseFrom(request)
		) {
			if (it.succeeded()) {
				responseObserver!!.onNext(
					it.result().body()
						.convert()
				)

				responseObserver.onCompleted()
			} else {
				println("Error occurred")
				responseObserver!!.onError(it.cause())
			}
		}
	}

	override fun disconnect(request: StringMessage?, responseObserver: StreamObserver<Status>?) {
		// todo: collect all consumers of one user to release them easily while disconnecting (or make better solution)

		super.disconnect(request, responseObserver)
	}
}