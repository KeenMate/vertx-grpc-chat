package com.keenmate.chat.verticles

import com.keenmate.chat.filters.transport.CustomTransportFilter
import com.keenmate.chat.services.ChatServiceImpl
import com.keenmate.chat.services.UserServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.grpc.VertxServerBuilder


class ChatVerticle : AbstractVerticle() {	
	override fun start(startFuture: Future<Void>) {
		VertxServerBuilder.forPort(9990)
			.addService(ChatServiceImpl(vertx).bindService())
			.addService(UserServiceImpl(vertx).bindService())
			.addTransportFilter(CustomTransportFilter())
			.build().start()
		startFuture.complete()
	}
}