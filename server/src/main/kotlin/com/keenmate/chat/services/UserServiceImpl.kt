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

	// override fun getUsers(request: Empty?, responseObserver: StreamObserver<UserChange>?) {
	// 	// stream existing users
	// 	println("Sending existing users")
	// 	eventBus.send<ArrayList<UserModel>>(Constants.Dao.GetClients, Unit) {
	// 		println("Received users from EB")
	// 		val result = UserChange.newBuilder()
	// 			.setChange(TheChange.EXISTING)
	//
	// 		it.result().body()
	// 			.forEach {
	// 				println("Sending existing user ${it.name} to response stream")
	//
	// 				responseObserver!!.onNext(
	// 					result
	// 						.setUser(it.convert())
	// 						.build()
	// 				)
	//
	// 				println("Existing user to response stream sent")
	// 			}
	// 	}
	//
	// 	// listen for upcoming changes
	// 	val changeConsumer = eventBus.consumer<UserChangeModel>(Constants.Dao.ClientChanged)
	//
	// 	changeConsumer.handler {
	// 		println("About to send user change")
	// 		try {
	// 			println("Sending change in users")
	// 			responseObserver!!.onNext(it.body().convert())
	// 		} catch (ex: StatusRuntimeException) {
	// 			println("There is no one on the other side of response stream")
	// 			changeConsumer.unregister()
	// 		}
	// 	}
	// }
}