<template>
	<div class="ui divided grid">
		<div class="ui row">
			<!-- Actions -->
			<div class="column">
				<div class="ui buttons">
					<button @click="onShowNewRoomModal" class="ui button">
						Create room
					</button>
				</div>
			</div>
		</div>
		<div class="ui row">
			<div class="ui four wide column">
				<!-- All rooms in vertical list-->
				<h4 class="ui header" v-if="state[MasterSlaveDataKey].sharedState.connectedRooms.length > 0">
					Connected rooms
				</h4>
				<List :items="state[MasterSlaveDataKey].sharedState.connectedRooms" @click="onRoomClick" key-prop="roomid" v-slot="{ item }">
					<a class="header">
						{{item.title}}
					</a>
				</List>
				<h4 class="ui header">
					Available rooms
				</h4>
				<List :items="state[MasterSlaveDataKey].sharedState.rooms" @click="onRoomClick" key-prop="roomid" v-slot="{ item }">
					<a class="header">
						{{item.title}}
					</a>
				</List>
			</div>
			<div class="ui twelve wide column">
				<!-- Connected rooms + chat window-->
				<div class="ui divided grid" v-if="state[MasterSlaveDataKey].sharedState.currentRoomId !== 0">
					<div class="ui row">
						<!-- Chat -->
						<div class="column">
							<!--<div class="chat-spacer"></div>-->
							<!--<p class="chat-content" v-text=""></p>-->
							<Chat :room="state[MasterSlaveDataKey].sharedState.rooms.find(x => x.roomid === state[MasterSlaveDataKey].sharedState.currentRoomId)"
							      @send="onSendClick"/>
						</div>
					</div>
				</div>
			</div>
		</div>
		<NewRoomModal @createRoom="onCreateRoom" ref="createRoomModal"/>
	</div>
</template>

<script>
	import List from '../components/list.vue'
	import NewRoomModal from '../components/new-room-modal.vue'
	import Chat from '../components/chat/chat.vue'
	import {MasterSlaveDataKey} from '../config/constants.js'
	import {toastError, toastSuccess} from '../helpers/toastr.js'
	import {toastGrpcNetError} from '../helpers/toastrPredefined.js'

	export default {
		name: 'HomePage',
		components: {
			List,
			NewRoomModal,
			Chat
		},
		data() {
			return {
				state: this.$store.state,
				MasterSlaveDataKey
			}
		},
		methods: {
			onCreateRoom(roomsTitle) {
				this.$store.dispatch('createRoom', {
					title: roomsTitle,
					private: false // todo: let user decide whether the room is private
				})
				.then(room => {
					switch (typeof room) {
						case 'string': // room's title
							// todo: create new room watcher
							this.$store.commit('addRoomAddedWatcher', {
								room,
								handler: () => {
									toastSuccess('Success!', `New room: ${room} has been successfully created`)
								}
							})
							break
						case 'object': // room already created
							toastSuccess('Success!', `New room: '${room.title}' has been successfully created`)
							break
					}
				})
				.catch(err => {
					switch (typeof err) {
						case 'string': // my error message
							toastError('Application error', err)
							break
						case 'object': // gRPC error
							if (err.code === 14)
								toastGrpcNetError('create room')
							break
					}
				})
			},
			onSendClick(messageContent) {
				this.$store.dispatch('sendMessage', {
					roomId: this.state[MasterSlaveDataKey].sharedState.currentRoomId,
					content: messageContent
				})
				.catch(err => {
					switch (err) {
						case 'string': // custom error message
							toastError('Application error', err)
							break
						case 'object': // gRPC error
							if (err.code === 14)
								toastGrpcNetError('send message')
							break
					}
				})
			},
			onRoomClick(room) {
				let foundRoom = this.state[MasterSlaveDataKey].sharedState.connectedRooms.find(x => x.roomid === room.roomid)

				if (foundRoom)
					this.$store.commit('setCurrentRoom', foundRoom.roomid)
				else
					this.$store.dispatch('joinRoom', room.roomid)
					.then(room => {
						switch (typeof room) {
							case 'number': // request sent to master tab to join to room
								break
							case 'object': // room joined
								toastSuccess('Success!', `You successfully joined '${room.title}'`)
								break
						}
					})
					.catch(err => {
						switch (typeof err) {
							case 'string':
								toastError('Error', err)
								break
							case 'object':
								if (err.code === 14)
									toastGrpcNetError('join to room')
								break
						}
					})
			},
			onShowNewRoomModal() {
				this.$refs['createRoomModal'].showModal()
			}
		}
	}
</script>

<style scoped>
	.chat-spacer {
		width: 100%;
		height: 300px !important;
	}
</style>