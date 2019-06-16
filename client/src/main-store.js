import {interval} from 'rxjs'
import m from 'moment'

import {ChatAddress, HostAddress, MasterSlaveDataKey} from './config/constants'
import {BroadcastMessage, initMasterSlaveState} from './mixins/master-slave-mixin.js'
import {ChatProviderClient} from './grpc/Chat_grpc_web_pb.js'
import {UserProviderClient} from './grpc/User_grpc_web_pb.js'
import {JoinRoomRequest, Message, NewRoomRequest} from './grpc/Chat_pb.js'
import {ConnectRequest} from './grpc/User_pb.js'

function insertOrUpdate(array, item, keySelector) {
	let existingIndex = array.findIndex(x => keySelector(x) === keySelector(item))

	if (existingIndex === -1)
		array.push(item)
	else
		array.splice(existingIndex, 1, item)
}

function insertOrSkip(array, item, keySelector) {
	const existing = array.find(x => keySelector(x) === keySelector(item))

	if (!existing)
		array.push(item)
}

export default {
	state: {
		[MasterSlaveDataKey]: initMasterSlaveState(),
		userProvider: null,
		chatProvider: null,
		updaterSubject: null,
		isWaiting: false,
		roomAddedWatchers: {} // key is room's title & value is handler
	},
	getters: {
		getRoom: s => id => s.rooms.find(x => x.roomid === id)
	},
	mutations: {
		setTabId(state, tabId) {
			state[MasterSlaveDataKey].tabId = tabId
		},
		initChannel(state, {onmessage, onerror}) {
			state[MasterSlaveDataKey].channel.onmessage = onmessage
			state[MasterSlaveDataKey].channel.onerror = onerror
		},
		addRoomAddedWatcher(state, {title, handler}) {
			if (Object.keys(state.roomAddedWatchers).indexOf(title) !== -1)
				return

			state.roomAddedWatchers[title] = handler
		},
		setIsWaitingForLogin(state, isWaiting) {
			state.isWaiting = isWaiting
		},
		setIsInitialized(state, isInitialized) {
			state[MasterSlaveDataKey].isInitialized = isInitialized
		},
		setIsMaster(state, isMaster) {
			state[MasterSlaveDataKey].isMaster = isMaster
		},
		setSharedState(state, shared) {
			state[MasterSlaveDataKey].sharedState = shared
		},
		setupMasterStuff(state, credentials) {
			if (state.chatProvider && state.userProvider)
				return

			// todo: implement authorization
			state.userProvider = new UserProviderClient(HostAddress, undefined, undefined)
			state.chatProvider = new ChatProviderClient(HostAddress, undefined, undefined)
		},
		init(state) {
			state.updaterSubject = interval(60000).subscribe(() => {
				state[MasterSlaveDataKey].sharedState.messages.forEach(msg => {
					msg.sentText = m(msg.sent).fromNow()
				})
			})
		},
		setUser(state, user) {
			state[MasterSlaveDataKey].sharedState.user = user
		},
		addRoom(state, room) {
			if (!room.messages)
				room.messages = []

			if (!room.users)
				room.users = []

			insertOrSkip(state[MasterSlaveDataKey].sharedState.rooms, room, x => x.roomid)
		},
		addConnectedRoom(state, roomId) {
			let foundRoom = state[MasterSlaveDataKey].sharedState.rooms
			.find(x => x.roomid === roomId) || {}

			insertOrUpdate(state[MasterSlaveDataKey].sharedState.connectedRooms, foundRoom, x => x.roomid)
		},
		addUser(state, user) {
			insertOrUpdate(state[MasterSlaveDataKey].sharedState.users, user, x => x.userguid)
		},
		addMessage(state, message) {
			insertOrUpdate(state[MasterSlaveDataKey].sharedState.messages, message, x => x.messageid)

			insertOrUpdate(
				state[MasterSlaveDataKey].sharedState.rooms.find(room => room.roomid === message.roomid).messages,
				message,
				x => x.messageid
			)
		},
		setCurrentRoom(state, roomId) {
			state[MasterSlaveDataKey].sharedState.currentRoomId = roomId
		},
		addUserToRoom(state, {roomId, user}) {
			const users = state[MasterSlaveDataKey].sharedState.rooms.find(x => x.roomid === roomId)
				.users

			let existingIndex = users.findIndex(x => x.userguid === user.userguid)

			if (existingIndex !== -1)
				return

			users.push(user)
		},
		removeUserFromRoom(state, {roomId, userGuid}) {
			const users = state[MasterSlaveDataKey].sharedState.rooms.find(x => x.roomid === roomId)
				.users

			let existingIndex = users.findIndex(x => x.userguid === userGuid)

			if (existingIndex === -1)
				return

			users.splice(existingIndex, 1)
		},
		removeUser(state, userguid) {
			const userPredicate = user => user.userguid === userguid

			state[MasterSlaveDataKey].sharedState
			.rooms.forEach(room =>
				room.users.removeIf(userPredicate))

			state[MasterSlaveDataKey].sharedState.users.removeIf(userPredicate)
		},
		removeMessage(state, messageId) {
			state[MasterSlaveDataKey].sharedState
			.messages.splice(
				state[MasterSlaveDataKey].sharedState
				.messages.findIndex(
					x => x.messageid === messageId
				),
				1
			)
		},
		removeRoom(state, roomId) {
			state[MasterSlaveDataKey].sharedState
			.rooms.splice(
				state[MasterSlaveDataKey].sharedState
				.rooms.findIndex(
					x => x.roomid === roomId
				),
				1
			)
		}
	},
	actions: {
		setMasterSlaveData({commit}, {isMaster, isInitialized, sharedState}) {
			return new Promise(resolve => {
				commit('setIsMaster', isMaster)
				commit('setIsInitialized', isInitialized)
				commit('setSharedState', sharedState)

				resolve()
			})
		},
		login({state, commit}, credentials) {
			if (state[MasterSlaveDataKey].isMaster) {
				const connectRequest = new ConnectRequest()
				connectRequest.setName(credentials.username)

				commit('setupMasterStuff', credentials)

				return new Promise((resolve, reject) => {
					state.userProvider.connect(connectRequest, {}, (err, data) => {
						if (err) {
							console.error(err)

							reject(err)
							return
						}

						const parsed = data.toObject(true)
						commit('setUser', parsed)
						resolve(parsed)
					})
				})
			} else {
				state[MasterSlaveDataKey].channel
				.postMessage(
					new BroadcastMessage(ChatAddress.LogIn, credentials)
				)

				return Promise.resolve()
			}
		},
		createRoom({state, commit}, room) {
			const communicationStore = state[MasterSlaveDataKey]
			if (!communicationStore.isMaster) {
				communicationStore.channel.postMessage(
					new BroadcastMessage(ChatAddress.CreateRoom, room)
				)
				return Promise.resolve(room.title)
			} else {
				if (!state.chatProvider || !(state[MasterSlaveDataKey].sharedState || {}).user) {
					console.error('Cannot create room because user is not in shared data')
					return Promise.reject('Client App error occured..')
				}

				const newRoomPayload = new NewRoomRequest()
				newRoomPayload.setCreatorguid(
					state[MasterSlaveDataKey].sharedState.user.userguid)
				newRoomPayload.setPrivate(room.private)
				newRoomPayload.setTitle(room.title)

				state.chatProvider.createRoom(newRoomPayload, {}, (err, response) => {
					if (err) {
						console.error(err)

						return Promise.reject(err)
					}

					const parsed = response.toObject(true)
					commit('addRoom', parsed)

					return Promise.resolve(parsed)
				})
			}
		},
		sendMessage({state, commit}, message) {
			const communicationStore = state[MasterSlaveDataKey]
			if (!communicationStore.isMaster) {
				communicationStore.channel.postMessage(
					new BroadcastMessage(ChatAddress.SendMessage, message)
				)
			} else {
				if (!state.chatProvider || !(state[MasterSlaveDataKey].sharedState || {}).user) {
					console.error('Cannot send message because user is not in shared data')
					return Promise.reject('Client App error occured..')
				}

				const newMessagePayload = new Message()
				newMessagePayload.setCreatorguid(
					state[MasterSlaveDataKey].sharedState.user.userguid)
				newMessagePayload.setRoomid(message.roomId)
				newMessagePayload.setContent(message.content)

				state.chatProvider.sendMessage(newMessagePayload, {}, (err, response) => {
					if (err) {
						console.error(err)

						return Promise.reject(err)
					}

					const parsed = response.toObject(false)
					if (err || !parsed.isok)
						console.error(err || parsed.errormessage)
				})
			}
		},
		joinRoom({state, commit, dispatch}, roomId) {
			const communicationStore = state[MasterSlaveDataKey]
			if (!communicationStore.isMaster) {
				communicationStore.channel.postMessage(
					new BroadcastMessage(ChatAddress.JoinRoom, roomId)
				)

				return Promise.resolve(roomId)
			} else {
				if (!state.chatProvider || !(state[MasterSlaveDataKey].sharedState || {}).user) {
					console.error('Cannot join room because user is not in shared data')
					return Promise.reject('Client App error occured..')
				}

				const joinRoomPayload = new JoinRoomRequest()
				joinRoomPayload.setUserguid(
					state[MasterSlaveDataKey].sharedState.user.userguid)
				joinRoomPayload.setRoomid(roomId)

				state.chatProvider.joinRoom(joinRoomPayload, {}, (err, response) => {
					const parsed = response.toObject(false)
					if (err || !parsed.isok) {
						console.error(err || parsed.errormessage)

						return Promise.reject(err)
					}

					dispatch('roomJoined', roomId)
					return Promise.resolve(
						state[MasterSlaveDataKey].sharedState
						.rooms.find(x => x.roomid === roomId)
					)
				})
			}
		},
		roomJoined({commit, state}, roomId) {
			commit('addConnectedRoom', roomId)
			commit('setCurrentRoom', roomId)

			if (state[MasterSlaveDataKey].isMaster)
				state[MasterSlaveDataKey]
				.channel
				.postMessage(
					new BroadcastMessage(
						ChatAddress.RoomJoined,
						roomId
					)
				)
		}
	}
}
