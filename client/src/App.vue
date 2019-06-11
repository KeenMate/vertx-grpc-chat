<template>
	<div>
		<div class="ui top attached menu">
			<a href="" class="item" @click="clearUser">
				{{user && user.name}}
			</a>
			<!-- Menu here -->
		</div>
		<div class="custom-container padded-top">
			<router-view></router-view>
		</div>
	</div>
</template>

<script>
	import MasterSlaveMixin, {BroadcastMessage} from './mixins/master-slave-mixin.js'
	import {ChatAddress, MasterSlaveDataKey} from './config/constants.js'

	import {ChatEvent} from './grpc/Chat_pb.js'
	import {StringMessage, TheChange} from './grpc/Common_pb.js'

	export default {
		name: 'app',
		mixins: [
			MasterSlaveMixin
		],
		data() {
			return {
				state: this.$store.state,
				listenStream: null,
				canAllocate: false
			}
		},
		computed: {
			user() {
				return this.state[MasterSlaveDataKey].sharedState.user
			},
			canCreateHook() {
				let {state} = this

				return this.canAllocate &&
					state[MasterSlaveDataKey].isMaster &&
					state[MasterSlaveDataKey].isInitialized &&
					state[MasterSlaveDataKey].sharedState.user.userguid
			}
		},
		watch: {
			canCreateHook(val) {
				if (!val)
					return

				const userGuidPayload = new StringMessage()
				userGuidPayload.setValue(
					this.state[MasterSlaveDataKey].sharedState.user.userguid
				)
				this.listenStream = this.state.chatProvider
				.listen(userGuidPayload)

				this.listenStream.on('data', this.handleListenData)
			}
		},
		mounted() {
			this.joinTabsSession()
		},
		methods: {
			onFirstMaster() {
				this.$router.push({
					name: 'login'
				})
			},
			onMasterExists(masterSlaveData) {
				if (!masterSlaveData.sharedState.user.userguid)
					this.onFirstMaster()
				else
					this.$router.push({
						name: 'home'
					})
			},
			handleCustomMessage(msg) {
				// handles custom messages of Broadcasting system

				switch (msg.address) {
					case ChatAddress.LogIn:
						if (!this.state[MasterSlaveDataKey].isMaster)
							return 0
						
						this.$store.dispatch('login', msg.body)
						.then(data => {
							this.$store.state[MasterSlaveDataKey].channel
							.postMessage(
								new BroadcastMessage(ChatAddress.LoggedIn, data)
							)
						})
						break
					case ChatAddress.LoggedIn:
						this.$store.commit('setIsWaitingForLogin', false)
						break
					case ChatAddress.Chat:
						// note: I should not be master at this moment
						switch (msg.body.change) {
							case TheChange.NEW:
							case TheChange.MODIFIED:
							case TheChange.EXISTING:
								this.addItem(msg.body.object, msg.body.valueCase)
								break
							case TheChange.DELETED:
								this.removeItem(msg.body.object, msg.body.valueCase)
								break
							case TheChange.NOTSET:
								return 1
						}
						return 0
					case ChatAddress.SendMessage:
						if (!this.state[MasterSlaveDataKey].isMaster)
							return 0

						this.$store.dispatch('sendMessage', msg.body)
						break
					case ChatAddress.CreateRoom:
						if (!this.state[MasterSlaveDataKey].isMaster)
							return 0

						this.$store.dispatch('createRoom', msg.body)
						break
					case ChatAddress.JoinRoom:
						if (!this.state[MasterSlaveDataKey].isMaster)
							return 0

						this.$store.dispatch('joinRoom', msg.body)
						break
					case ChatAddress.RoomJoined:
						this.$store.dispatch('roomJoined', msg.body)
						break
					default:
						return 1
				}

				return 0
			},
			allocateResources() {
				this.$store.commit('setupMasterStuff')

				this.canAllocate = true
			},
			freeResources() {
				this.listenStream && this.listenStream.cancel() && (this.listenStream = null)

				this.canAllocate = false
			},
			clearUser() {
				window.localStorage.removeItem('user')
			},
			handleListenData(response) {
				const object = response.toObject()
				
				console.log('received message from listen stream: ', object)
				switch (response.getChange()) {
					case TheChange.NEW:
					// todo: Notify user (on active/focused window)
					case TheChange.MODIFIED:
					case TheChange.EXISTING:
						this.addItem(object, response.getValueCase())
						break
					case TheChange.DELETED:
						this.removeItem(object, response.getValueCase())
						break
					case TheChange.NOTSET:
						break
				}
			},
			addItem(object, valueCase) {
				let {$store, state} = this

				switch (valueCase) {
					case ChatEvent.ValueCase.ROOM:
						$store.commit('addRoom', object.room)
						break
					case ChatEvent.ValueCase.USER:
						$store.commit('addUser', object.user)

						// room with id 0 can exist
						//if (object.roomid !== 0)
						$store.commit('addUserToRoom', {
							roomId: object.roomid,
							user: object.user
						})
						break
					case ChatEvent.ValueCase.MESSAGE:
						$store.commit('addMessage', object.message)
						break
					case ChatEvent.ValueCase.VALUE_NOT_SET:
						break
				}

				if (!state[MasterSlaveDataKey].isMaster)
					return

				state[MasterSlaveDataKey].channel.postMessage(
					new BroadcastMessage(ChatAddress.Chat, {
						object,
						valueCase,
						change: TheChange.NEW
					})
				)
			},
			removeItem(object, valueCase) {
				let {$store, state} = this

				switch (valueCase) {
					case ChatEvent.ValueCase.ROOM:
						$store.commit('removeRoom', object.roomid)
						break
					case ChatEvent.ValueCase.USER:
						if (roomId !== 0)
							$store.commit('removeUserFromRoom', {
								roomId: object.roomid,
								userGuid: object.user.userguid
							})
						else
							$store.commit('removeUser', object.user.userguid)
						break
					case ChatEvent.ValueCase.MESSAGE:
						$store.commit('removeMessage', object.message.messageid)
						break
					case ChatEvent.ValueCase.VALUE_NOT_SET:
						break
				}

				if (!state[MasterSlaveDataKey].isMaster)
					return

				state[MasterSlaveDataKey].channel.postMessage(
					new BroadcastMessage(ChatAddress.Chat, {
						object,
						valueCase,
						change: TheChange.DELETED
					})
				)
			},
			initSharedState() {

			}
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
