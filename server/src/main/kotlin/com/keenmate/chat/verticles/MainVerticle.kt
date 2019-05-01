package com.keenmate.chat_01.verticles

import io.reactivex.Observable
import io.vertx.core.*

class MainVerticle : AbstractVerticle() {	
	override fun start(startFuture: Future<Void>) {
		val futures: MutableList<Future<out Any>> = ArrayList()
		
		val eventBus = vertx.eventBus()
		
		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle(DaoVerticle(eventBus), DeploymentOptions().apply { 
				isWorker = true
			}) {
				println("Verticle ${it.result()} successfully deployed")

				f.complete()
			}
		})
		
		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle("com.keenmate.chat_01.verticles.ChatVerticle") {
				println("Verticle ${it.result()} successfully deployed")
				
				f.complete()
			}
		})
		
		CompositeFuture.all(futures).setHandler { 
			startFuture.complete()
		}
	}
}
