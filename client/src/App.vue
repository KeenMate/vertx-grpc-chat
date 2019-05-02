<template>
	<div>
		<div class="ui top attached menu">
			<a href="" class="item">
				{{$store.state.user && $store.state.user.name}}
			</a>
			<!-- Menu here -->
		</div>
		<div class="custom-container padded-top">
			<router-view></router-view>
		</div>
		<!--<div class="ui container">
			<div class="ui grid">
				<div class="ui six wide column">
					<div class="ui input">
						<input
							type="text"
							placeholder="Username"
							v-model="username"
							@keypress.enter="connect"/>
					</div>
				</div>
				<div class="ui six wide column">
					<h2>Rooms</h2>
					<div class="ui list">
						<div
							class="item"
							v-for="room in rooms" :key="room.roomid"
							@click="() => joinRoom(room)">
							{{room.title}}
						</div>
					</div>
				</div>
				<div class="ui six wide column">
					<h2>Connected to Rooms:</h2>
					<div class="ui list">
						<div
							class="item"
							v-for="room in connectedRooms" :key="room.roomid">
							&lt;!&ndash; TODO: Show chat after selecting room &ndash;&gt;
							{{room.title}}
						</div>
					</div>
				</div>
				<div class="ui six wide column">
					<div class="ui input">
						<input
							type="text"
							placeholder="Room title"
							v-model="newRoomTitle"
							@keypress.enter="onCreateRoom"/>
					</div>
				</div>
				<div class="ui six wide column">
					<h2>Clients</h2>
					<div class="ui list">
						<div
							class="item"
							v-for="client in clients" :key="client.clientid"
							@click="() => connectToClient(client)">
							Name: {{client.name}}
						</div>
					</div>
				</div>
			</div>
		</div>-->
	</div>
</template>

<script>
	export default {
		name: 'app',
		components: {

		},
		data () {
			return {
				// chatProvider: null,
				// userProvider: null,
				// username: '',
				// newRoomTitle: '',
				// user: null,
				// rooms: [],
				// clients: [],
				// connectedRooms: [],
				// messagesSubject: new Subject(),
				// updaterSubject: null
			}
		},
		methods: {
			// joinRoom (room) {
			// 	const joinRoomPayload = new JoinRoomRequest()
			//
			// 	joinRoomPayload.setClient(this.user.$jspbMessageInstance)
			// 	joinRoomPayload.setRoomid(room.roomid)
			//
			// 	const roomMessagesServerStream = this.chatProvider.joinRoom(joinRoomPayload)
			//
			// 	this.connectedRooms.push(room)
			// 	this.messagesSubject
			// 		.pipe(
			// 			filter(x => x.roomid === room.roomid)
			// 		).subscribe(x => {
			// 			x.author = this.clients.find(c => c.clientid === x.sourceid)
			//
			// 			this.updaterSubject
			// 				.subscribe(() => x.sentText = (m(x.sent).fromNow()))
			//
			// 			(room.messges = room.messages || [])
			// 				.push(x)
			// 		})
			//
			// 	roomMessagesServerStream.on('data', response => {
			// 		this.messagesSubject.next(response.toObject(true))
			// 	})
			// },
			// connectToClient (targetClient) {
			// 	const roomPayload = new Room()
			// 	roomPayload.setTitle(this.newRoomTitle)
			// 	roomPayload.setClientsList([
			// 		this.user.$jspbMessageInstance,
			// 		targetClient.$jspbMessageInstance
			// 	])
			// 	roomPayload.setPrivate(true)
			//
			// 	this.createRoom(roomPayload)
			// 		.subscribe(r => { // createRoom returns created Room
			// 			// todo: show chat (probably make use of router?)
			// 		})
			// },
			// onCreateRoom () {
			// 	let payload = new Room()
			// 	payload.setTitle(this.newRoomTitle)
			// 	payload.addClients(this.user.$jspbMessageInstance)
			// 	payload.setPrivate(false)
			//
			// 	this.newRoomTitle = ''
			//
			// 	let x = this.createRoom(payload)
			// 		.subscribe(r => {
			// 			x.unsubscribe()
			// 			x.remove()
			//
			// 			// todo: join room
			//
			// 			// should be redirected?
			// 		})
			// },
			// createRoom (payload) {
			// 	const responsePromise = new Promise((resolve, reject) => {
			// 		this.chatProvider.createRoom(payload, {}, (err, data) => {
			// 			if (err) {
			// 				console.error('Could not create room: ', err)
			// 				reject(err)
			// 			}
			// 			resolve(data)
			// 		})
			// 	})
			//
			// 	return from(responsePromise)
			// }
		},
		mounted () {
			// this.updaterSubject = interval(60000)
		}
	}
</script>

<style scoped>
	.padded-top {
		margin-top: 15px;
	}
	
	.custom-container {
		width: 94%;
		margin-left: auto;
		margin-right: auto;
	}
</style>
