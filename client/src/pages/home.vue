<template>
	<div class="ui divided grid">
		<div class="ui row">
			<!-- Actions -->
			<div class="column">
				<div class="ui buttons">
					<button class="ui button" @click="onShowNewRoomModal">
						Create room
					</button>
				</div>
			</div>
		</div>
		<div class="ui row">
			<div class="ui four wide column">
				<!-- All rooms in vertical list-->
				<h4 class="ui header" v-if="state.connectedRooms.length > 0">
					Connected rooms
				</h4>
				<List :items="state.connectedRooms" key-prop="roomid" v-slot="{ item }" @click="onRoomClick">
					<a class="header">
						{{item.title}}
					</a>
				</List>
				<h4 class="ui header">
					Available rooms
				</h4>
				<List :items="state.rooms" key-prop="roomid" v-slot="{ item }" @click="onRoomClick">
					<a class="header">
						{{item.title}}
					</a>
				</List>
			</div>
			<div class="ui twelve wide column">
				<!-- Connected rooms + chat window-->
				<div class="ui divided grid" v-if="state.currentRoomId !== 0">
					<div class="ui row">
						<!-- Chat -->
						<div class="column">
							<!--<div class="chat-spacer"></div>-->
							<!--<p class="chat-content" v-text=""></p>-->
							<Chat :room="state.rooms.find(x => x.roomid === state.currentRoomId)" @send="onSend"/>
						</div>
					</div>
				</div>
			</div>
		</div>
		<NewRoomModal ref="create-room-modal" @createRoom="onCreateRoom"/>
	</div>
</template>

<script>
	import List from '../components/list.vue'
	import NewRoomModal from '../components/new-room-modal.vue'
	import Chat from '../components/chat/chat.vue'

	import { Empty, Int32Message, StringMessage } from '../grpc/Common_pb'
	import { JoinRoomRequest, Message, ChatChange, NewRoomRequest } from '../grpc/Chat_pb'

	export default {
		name: 'HomePage',
		components: {
			List,
			NewRoomModal,
			Chat
		},
		data () {
			return {
				state: this.$store.state
			}
		},
		methods: {
			onSend (newMsgContent) {
				const msgPayload = new Message()
				msgPayload.setContent(newMsgContent)
				msgPayload.setCreatorguid(this.state.user.clientguid)
				msgPayload.setRoomid(
					this.state
					.rooms
					.find(x => x.roomid === this.state.currentRoomId).roomid
				)

				this.state.chatProvider.sendMessage(msgPayload, {}, (err, data) => {
					console.log('message successfully sent', data)
				})
			},
			onRoomClick (room) {
				let foundRoom = this.state.connectedRooms.find(x => x.roomid === room.roomid)

				if (foundRoom) {
					this.$store.commit('setCurrentRoom', foundRoom.roomid)
				} else {
					// todo: join to selected room
					this.joinRoomClicked(room)
				}
			},
			joinRoomClicked (room) {
				const joinRoomPayload = new JoinRoomRequest()

				joinRoomPayload.setClientguid(this.state.user.clientguid)
				joinRoomPayload.setRoomid(room.roomid)

				this.state.chatProvider.joinRoom(joinRoomPayload, {}, (err, data) => {
					if (err) {
						console.error(err)

						return
					}

					// save existing messages
					data.toObject()
					.messagesList
					.forEach(msg => this.$store.commit('addMessage', msg))
				})

				// load connected clients
				const roomIdMessage = new Int32Message()
				roomIdMessage.setValue(room.roomid)
				this.state.chatProvider.getClientsForRoom(roomIdMessage, {}, (err, data) => {
					const dataParsed = data.toObject()

					dataParsed.clientsList.forEach(client => this.$store.commit('addClientToRoom', [client, room.roomid]))
				})

				this.$store.commit('addConnectedRoom', room)
				this.$store.commit('setCurrentRoom', room.roomid)

				// roomMessagesServerStream.on('data', response => {
				// 	const parsedResponse = response.toObject(true)
				//
				// 	console.log('received message', parsedResponse)
				//
				// 	switch (response.getThechangeCase()) {
				// 		case ChatChange.ThechangeCase.MSG:
				// 			this.$store.commit('addMessage', parsedResponse.msg)
				// 			break
				// 		case ChatChange.ThechangeCase.CLIENTCONNECTED:
				// 			this.$store.commit('addClientToRoom', [parsedResponse.clientconnected, parsedResponse.roomid])
				// 			break
				// 		case ChatChange.ThechangeCase.CLIENTDISCONNECTED:
				// 			this.$store.commit('removeClientFromRoom', [parsedResponse.clientconnected, parsedResponse.roomid])
				// 			break
				// 		case ChatChange.ThechangeCase.THECHANGE_NOT_SET:
				// 			break
				// 	}
				//
				// })
			},
			onCreateRoom (roomsTitle) {
				const roomPayload = new NewRoomRequest()

				roomPayload.setTitle(roomsTitle)
				roomPayload.setCreatorguid(this.state.user.clientguid)
				roomPayload.setPrivate(false)

				this.state.chatProvider.createRoom(roomPayload, {}, (err, data) => {
					const room = data.toObject(true)
					this.$store.commit('addRoom', room)
					// join to returned room
					this.$nextTick(() => {
						this.joinRoomClicked(room)
					})
				})
			},
			onShowNewRoomModal () {
				// show modal then create & join
				this.$refs['create-room-modal'].showModal()

			},
			createRoomsHook () {
				const roomsServerStreaming = this.state.chatProvider.getRooms(new Empty())

				roomsServerStreaming.on('end', response => {
					console.log('end: ', response)
				})
				roomsServerStreaming.on('error', response => {
					console.log('error: ', response)
				})
				roomsServerStreaming.on('status', response => {
					console.log('status: ', response)
				})
				roomsServerStreaming.on('data', response => {
					this.$store.commit('addRoom', response.toObject(true))
				})
			},
			createClientsHook () {
				const clientsServerStreaming = this.state.userProvider.getUsers(new Empty(), {})

				clientsServerStreaming.on('data', response => {
					if (response.getClientguid() === (this.state.user && this.state.user.clientguid))
						return

					this.$store.commit('addClient', response.toObject(true))
				})
			},
			createMessagesHook () {
				const clientGuidPayload = new StringMessage()
				clientGuidPayload.setValue(this.state.user.clientguid)

				const messagesStream = this.state.chatProvider.getMessages(clientGuidPayload)

				messagesStream.on('data', response => {
					const parsedResponse = response.toObject(true)

					console.log('received message', parsedResponse)

					switch (response.getThechangeCase()) {
						case ChatChange.ThechangeCase.MSG:
							this.$store.commit('addMessage', parsedResponse.msg)
							break
						case ChatChange.ThechangeCase.CLIENTCONNECTED:
							this.$store.commit('addClientToRoom', [parsedResponse.clientconnected, parsedResponse.roomid])
							break
						case ChatChange.ThechangeCase.CLIENTDISCONNECTED:
							this.$store.commit('removeClientFromRoom', [parsedResponse.clientconnected, parsedResponse.roomid])
							break
						case ChatChange.ThechangeCase.THECHANGE_NOT_SET:
							break
					}
				})
			}
		},
		mounted () {
			this.createRoomsHook()
			this.createClientsHook()
			this.createMessagesHook()
		}
	}
</script>

<style scoped>
	.chat-spacer {
		width: 100%;
		height: 300px !important;
	}
</style>