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
							<Chat :room="state.rooms.find(x => x.roomid === state.currentRoomId)" @send="onSend" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<NewRoomModal ref="create-room-modal" @createRoom="onCreateRoom" />
	</div>
</template>

<script>
	import { filter } from 'rxjs/operators'
	
	import List from '../components/list.vue'
	import NewRoomModal from '../components/new-room-modal.vue'
	import Chat from '../components/chat/chat.vue'

	import { Empty } from '../grpc/Common_pb'
	import { JoinRoomRequest, Room, Message, ChatChange } from '../grpc/Chat_pb'

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
				msgPayload.setCreator(this.state.user.$jspbMessageInstance)
				msgPayload.setRoom(
					this.state
					.rooms
					.find(x => x.roomid === this.state.currentRoomId).$jspbMessageInstance
				)
				
				this.state.chatProvider.sendMessage(msgPayload, {}, (err, data) => {
					console.log('message successfully sent', data)
				})
			},
			onRoomClick (room) {
				let foundRoom = this.state.connectedRooms.find(x => x.roomid === room.roomid)
				
				if (foundRoom) {
					// todo: switch to selected room
					this.$store.commit('setCurrentRoom', foundRoom)
				} else {
					// todo: join to selected room
					this.joinRoomClicked(room)
				}
			},
			joinRoomClicked (room) {
				const joinRoomPayload = new JoinRoomRequest()

				joinRoomPayload.setClient(this.state.user.$jspbMessageInstance)
				joinRoomPayload.setRoom(room.$jspbMessageInstance)

				const roomMessagesServerStream = this.state.chatProvider.joinRoom(joinRoomPayload)

				this.$store.commit('addConnectedRoom', room)
				this.$store.commit('setCurrentRoom', room)

				roomMessagesServerStream.on('data', response => {
					const parsedResponse = response.toObject(true)
					
					console.log(parsedResponse)
					
					switch (response.getThechangeCase()) {
						case ChatChange.ThechangeCase.MSG:
							this.$store.commit('addMessage', parsedResponse.msg)
							break;
						case ChatChange.ThechangeCase.CLIENTCONNECTED:
							this.$store.commit('addClientToRoom', [parsedResponse.clientconnected, parsedResponse.room])
							break;
						case ChatChange.ThechangeCase.CLIENTDISCONNECTED:
							this.$store.commit('removeClientFromRoom', [parsedResponse.clientconnected, parsedResponse.room])
							break;
						case ChatChange.ThechangeCase.THECHANGE_NOT_SET:
							break;
					}
					
				})
			},
			onCreateRoom (roomsTitle) {
				const roomPayload = new Room()

				roomPayload.setTitle(roomsTitle)
				roomPayload.setAuthor(this.state.user.$jspbMessageInstance)
				
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
			}
		},
		mounted () {
			this.createRoomsHook()
			this.createClientsHook()
		}
	}
</script>

<style scoped>
	.chat-spacer {
		width: 100%;
		height: 300px !important;
	}
</style>