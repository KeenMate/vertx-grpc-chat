package com.keenmate.chat.verticles

import com.keenmate.chat.Constants
import com.keenmate.chat.Room
import com.keenmate.chat.models.ClientModel
import com.keenmate.chat.models.JoinRoomRequestModel
import com.keenmate.chat.models.MessageModel
import com.keenmate.chat.models.RoomModel
import io.reactivex.subjects.ReplaySubject
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.random.Random
import java.util.concurrent.ConcurrentHashMap

class DaoVerticle(private val eventBus: EventBus) : AbstractVerticle() {
	private val consumerMap: ConcurrentHashMap<UUID, MutableCollection<MessageConsumer<*>>> = ConcurrentHashMap()
	
	private val registerConsumer = eventBus.consumer<String>("asd") {
		// message will be of specific type ->
		//    containing client's UUID, and the "point of interest"
		
		// add new consumer to map to corresponding UUID
	}
	
	init {		
		// add all consumers to consumers map 
		val list = mutableListOf<MessageConsumer<*>>()
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.GetMessages) { mainMessage ->
				val joinRoomRequestModel = JoinRoomRequestModel().parseFrom(mainMessage.body())

				val tmpAddress = generateTmpPublisherAddress()

				val tmpPublisher = eventBus.sender<String>(tmpAddress)
				vertx.executeBlocking({ fut: Future<Unit> ->
					messagesSubject.values
						.forEach {
							val casted = (it as MessageModel)

							if (casted.room != null && joinRoomRequestModel.room != null &&
								casted.room!!.roomId != joinRoomRequestModel.room!!.roomId)
								return@forEach

							tmpPublisher.send(casted.toString())
						}

					fut.complete()
				}, false, {
					tmpPublisher.end()
				})

				mainMessage.reply(tmpAddress)
			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.GetClients) { mainMessage ->
				val tmpAddress = generateTmpPublisherAddress()

				val tmpPublisher = eventBus.sender<String>(tmpAddress)
				vertx.executeBlocking({ fut: Future<Unit> ->
					clientsSubject.values
						.forEach {
							tmpPublisher.send((it as ClientModel).toString())
						}
					
					fut.complete()
				}, false) {
					tmpPublisher.end()
				}

				mainMessage.reply(tmpAddress)
			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.GetRooms) { mainMessage ->
				val tmpAddress = generateTmpPublisherAddress()

				val tmpPublisher = eventBus.sender<String>(tmpAddress)
				vertx.executeBlocking({ fut: Future<Unit> ->
					roomsSubject.values
						.forEach {
							tmpPublisher.send(
								(it as RoomModel).toString()
							)
						}

					fut.complete()
				}, false) {
					tmpPublisher.end()
				}

				mainMessage.reply(tmpAddress)
			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.AddMessage) {
				val messageModel = MessageModel().parseFrom(it.body())


			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.AddClient) {
				val clientModel = ClientModel().parseFrom(it.body())

				vertx.executeBlocking({ fut: Future<ClientModel?> ->
					fut.complete(clientsSubject
						.values
						.firstOrNull { x ->
							(x as ClientModel).name == clientModel.name
						} as ClientModel?
					)
				}, false) { response ->
					var toReturn: ClientModel? = response.result()
					if (toReturn == null) {
						println("Client was not found - creating one")
						toReturn = clientModel
						toReturn.clientGuid
						toReturn.loggedOn = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

						clientsSubject.onNext(toReturn)
					}

					it.reply(toReturn.toString())
				}
			}
		)
		
		list.add(eventBus
			.consumer<String>(Constants.Dao.AddRoom) {
				val room = RoomModel().parseFrom(it.body())
				val tmpBuild = Room.newBuilder()
					.setRoomId(++idCounter)
					.setTitle(room.title)
					.setPrivate(false)
					.build()

				it.reply(room.parseFrom(tmpBuild).toString())
				roomsSubject.onNext(room)
			}
		)
		
		// creates empty UUID and puts all DAO's consumers to it
		consumerMap.put(UUID(0, 0), list)
	}
	
	// --------------------------------
	
	private var idCounter = 0
	private val randomInstance = Random(Date().time)

	private val messagesSubject: ReplaySubject<MessageModel> = ReplaySubject.create()
	private val clientsSubject: ReplaySubject<ClientModel> = ReplaySubject.create()
	private val roomsSubject: ReplaySubject<RoomModel> = ReplaySubject.create()

	override fun start(startFuture: Future<Void>) {
		roomsSubject.subscribe {
			println("New Room Added - sending to Everyone!")
			eventBus.publish(Constants.Dao.RoomAdded, it.toString())
		}

		clientsSubject.subscribe {
			println("New User Added - sending to Everyone!")
			eventBus.publish(Constants.Dao.ClientAdded, it.toString())
		}

		startFuture.complete()
	}
	
	override fun stop(stopFuture: Future<Void>) {
		// todo: Unregister consumers for UUID(0, 0)
	}
	
	private fun generateTmpPublisherAddress(): String {
		return "dao.tmp-publisher:${randomInstance.nextLong()}"
	}
}