package com.keenmate.chat.verticles

import com.keenmate.chat.Constants
import com.keenmate.chat.codecs.ByteCodec
import com.keenmate.chat.codecs.ModelCodec
import com.keenmate.chat.models.*
import io.vertx.core.*
import java.util.*
import kotlin.collections.ArrayList

class MainVerticle : AbstractVerticle() {	
	override fun start(startFuture: Future<Void>) {		
		val futures: MutableList<Future<out Any>> = ArrayList()
		
		val eventBus = vertx.eventBus()
		eventBus
			.registerDefaultCodec(ArrayList::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec + "1"))
			.registerDefaultCodec(Collections.singletonList(Any::class.java)::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec + "2"))
			.registerDefaultCodec(emptyList<Any>()::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec))
			.registerDefaultCodec(NewRoomRequestModel::class.java, ByteCodec(Constants.CodecNames.newRoomRequestModelName()))
			.registerDefaultCodec(ClientModel::class.java, ByteCodec(Constants.CodecNames.clientModelName()))
			.registerDefaultCodec(ChatChangeModel::class.java, ByteCodec(Constants.CodecNames.chatChangeModelName()))
			.registerDefaultCodec(ConnectRequestModel::class.java, ByteCodec(Constants.CodecNames.connectRequestModelName()))
			.registerDefaultCodec(JoinRoomRequestModel::class.java, ByteCodec(Constants.CodecNames.joinRoomRequestModelName()))
			.registerDefaultCodec(MessageModel::class.java, ByteCodec(Constants.CodecNames.messageModelName()))
			.registerDefaultCodec(RoomModel::class.java, ByteCodec(Constants.CodecNames.roomModelName()))
		
		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle(DaoVerticle(eventBus), DeploymentOptions().apply { 
				isWorker = true
			}) {
				if (it.result() != null)
					println("Verticle ${it.result()} successfully deployed")
				else {
					println("Error in deploying Verticle")

					println(it.cause())
				}

				f.complete()
			}
		})
		
		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle("com.keenmate.chat.verticles.ChatVerticle") {
				if (it.result() != null)
					println("Verticle ${it.result()} successfully deployed")
				else {
					println("Error in deploying Verticle")
					
					println(it.cause())
				}
				
				f.complete()
			}
		})
		
		CompositeFuture.all(futures).setHandler { 
			startFuture.complete()
		}
	}
}
