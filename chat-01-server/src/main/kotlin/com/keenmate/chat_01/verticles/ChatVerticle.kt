package com.keenmate.chat_01.verticles

import com.keenmate.chat_01.services.ChatServiceImpl
import com.keenmate.chat_01.services.UserServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.grpc.VertxServerBuilder


class ChatVerticle : AbstractVerticle() {	
	override fun start(startFuture: Future<Void>) {
		VertxServerBuilder.forPort(9990)
			.addService(ChatServiceImpl(vertx).bindService())
			.addService(UserServiceImpl(vertx).bindService())
			.build().start()
		startFuture.complete()
	}
}