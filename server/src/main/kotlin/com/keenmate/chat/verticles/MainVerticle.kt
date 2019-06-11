package com.keenmate.chat.verticles

import com.keenmate.chat.helpers.registerDefaultCodecs
import io.vertx.core.*
import kotlin.collections.ArrayList

class MainVerticle : AbstractVerticle() {
	val verticles: ArrayList<String> = ArrayList()

	override fun start(startFuture: Future<Void>) {
		val futures: MutableList<Future<out Any>> = ArrayList()

		val eventBus = vertx.eventBus()
		registerDefaultCodecs(eventBus)

		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle(DaoVerticle(eventBus), DeploymentOptions().apply {
				// isWorker = true
			}) {
				if (it.result() != null) {
					println("Verticle ${it.result()} successfully deployed")
					verticles.add(it.result())
				} else {
					println("Error in deploying Verticle")

					println(it.cause())
					f.fail(it.cause())
				}

				f.complete()
			}
		})

		futures.add(Future.future<Unit> { f ->
			vertx.deployVerticle("com.keenmate.chat.verticles.ChatVerticle") {
				if (it.result() != null) {
					println("Verticle ${it.result()} successfully deployed")
					verticles.add(it.result())
				}
				else {
					println("Error in deploying Verticle")

					println(it.cause())
					f.fail(it.cause())
				}

				f.complete()
			}
		})

		CompositeFuture.all(futures).setHandler {
			if (it.failed()) {
				startFuture.fail(it.cause())

				return@setHandler
			}

			startFuture.complete()
		}
	}

	override fun stop(stopFuture: Future<Void>?) {
		verticles.forEach {
			vertx.undeploy(it)
		}
		
		stopFuture!!.complete()
	}
}
