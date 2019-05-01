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
				<h4 class="ui header">
					Connected rooms
				</h4>
				<List :items="state.connectedRooms" key-prop="roomid" v-slot="{ item }">
					<a class="header">
						{{item.title}}
					</a>
				</List>
				<h4 class="ui header">
					Available rooms
				</h4>
				<List :items="state.rooms" key-prop="roomid" v-slot="{ item }" @click="joinRoomClicked">
					<a class="header">
						{{item.title}}
					</a>
				</List>
			</div>
			<div class="ui twelve wide column">
				<!-- Connected rooms + chat window-->
				<div class="ui divided grid">
					<!--<div class="ui row">
						&lt;!&ndash; Rooms &ndash;&gt;
						<div class="column">
							<div class="ui secondary pointing menu">
								<a class="item" v-for="room in state.connectedRooms">
									{{ room.title }}
								</a>
							</div>
						</div>
					</div>-->
					<div class="ui row">
						<!-- Chat -->
						<div class="column">
							<div class="chat-spacer"></div>
							<p class="chat-content" v-text=""></p>
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

	import { Empty } from '../grpc/Common_pb'
	import { JoinRoomRequest, Room } from '../grpc/Chat_pb'

	export default {
		name: 'HomePage',
		components: {
			List,
			NewRoomModal
		},
		data () {
			return {
				state: this.$store.state
			}
		},
		methods: {
			joinRoomClicked (room) {
				const joinRoomPayload = new JoinRoomRequest()

				joinRoomPayload.setClient(this.state.user.$jspbMessageInstance)
				joinRoomPayload.setRoomid(room.roomid)

				const roomMessagesServerStream = this.state.chatProvider.joinRoom(joinRoomPayload)

				this.$store.commit('addConnectedRoom', room)
				
				this.$store.commit('addMessageObserver', room.roomid, this.state.messages
					.pipe(
						filter(x => x.roomid === room.roomid)
					).subscribe(x => {
						x.author = this.state.clients.find(c => c.clientid === x.sourceid)
	
						this.$store.commit()

						this.$store.commit('addUpdaterObserver', this.state
							.updaterSubject
							.subscribe(() => x.sentText = (m(x.sent).fromNow()))
						)
	
						(room.messges = room.messages || [])
							.push(x)
					})
				)

				roomMessagesServerStream.on('data', response => {
					this.state.messages.next(response.toObject(true))
				})
			},
			onCreateRoom (roomsTitle) {
				const roomPayload = new Room()

				roomPayload.setTitle(roomsTitle)
				roomPayload.addClients(this.state.user.$jspbMessageInstance)
				
				this.state.chatProvider.createRoom(roomPayload, {}, (err, data) => {
					// join to returned room
					this.joinRoomClicked(data.toObject(true))
				})
			},
			onShowNewRoomModal () {
				// show modal then create & join
				this.$refs['create-room-modal'].showModal()
				
			},
			createRoomsHook () {
				const roomsServerStreaming = this.state.chatProvider.roomsHook(new Empty())

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