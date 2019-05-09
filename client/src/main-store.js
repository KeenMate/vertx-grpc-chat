import { Subject } from 'rxjs'
import { interval } from 'rxjs'
import m from 'moment'

import { UserProviderClient } from './grpc/User_grpc_web_pb'
import { ChatProviderClient } from './grpc/Chat_grpc_web_pb'

import { hostAddress } from './config/constants'

function insertOrUpdate (array, item, keySelector) {
	let existingIndex = array.findIndex(x => keySelector(x) === keySelector(item))

	if (existingIndex === -1)
		array.push(item)
	else
		array.splice(existingIndex, 1, item)
}

function insertOrSkip (array, item, keySelector) {
	const existing = array.find(x => keySelector(x) === keySelector(item))

	if (!existing)
		array.push(item)
}

export default {
	state: {
		user: null,
		userProvider: new UserProviderClient(hostAddress, undefined, undefined),
		chatProvider: new ChatProviderClient(hostAddress, undefined, undefined),
		updaterSubject: null,
		rooms: [],
		connectedRooms: [],
		currentRoomId: 0,
		clients: [],
		messages: []
	},
	mutations: {
		init (state) {
			state.updaterSubject = interval(60000).subscribe(x => {
				state.messages.forEach(msg => {
					msg.sentText = m(msg.sent).fromNow()
				})
			})
		},
		setUser (state, user) {
			state.user = user
		},
		addRoom (state, room) {
			if (!room.messages)
				room.messages = []

			if (!room.clients)
				room.clients = []

			insertOrSkip(state.rooms, room, x => x.roomid)
		},
		addConnectedRoom (state, room) {
			let foundRoom = state.rooms
				.find(x => x.roomid === room.roomid) || {}

			state.connectedRooms.push(foundRoom)
		},
		addClient (state, client) {
			state.clients.push(client)
		},
		addMessage (state, msg) {
			insertOrUpdate(state.messages, msg, x => x.messageid)

			insertOrUpdate(state.rooms.find(room => room.roomid === msg.roomid).messages,
				msg,
				x => x.messageid)
		},
		setCurrentRoom (state, roomId) {
			state.currentRoomId = roomId
		},
		addClientToRoom (state, payload) {
			const clients = state.rooms.find(x => x.roomid === payload[1])
				.clients

			let existingIndex = clients.findIndex(x => x.clientguid === payload[0].clientguid)

			if (existingIndex !== -1)
				return

			clients.push(payload[0])
		},
		removeClientFromRoom (state, payload) {
			const clients = state.rooms.find(x => x.roomid === payload[1])
				.clients

			let existingIndex = clients.findIndex(x => x.clientguid === payload[0].clientguid)

			if (existingIndex === -1)
				return

			clients.splice(existingIndex, 1)
		}
	}
}
