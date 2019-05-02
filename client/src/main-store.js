import { Subject } from 'rxjs'
import { interval } from 'rxjs'
import m from 'moment'

import { UserProviderClient } from './grpc/User_grpc_web_pb'
import { ChatProviderClient } from './grpc/Chat_grpc_web_pb'

import { hostAddress } from './config/constants'

export default {
	state: {
		user: null,
		userProvider: new UserProviderClient(hostAddress, undefined, undefined),
		chatProvider: new ChatProviderClient(hostAddress, undefined, undefined),
		updaterSubject: interval(60000).subscribe(x => {
			this.messages.forEach(msg => {
				msg.sentText = m(msg.sent).fromNow()
			})
		}),
		rooms: [],
		connectedRooms: [],
		clients: [],
		messages: new Subject(),
		messagesObservers: {},
	},
	mutations: {
		setUser (state, user) {
			state.user = user
		},
		addRoom (state, room) {
			state.rooms.push(room)
		},
		addConnectedRoom (state, room) {
			state.connectedRooms.push(room)
		},
		addClient (state, client) {
			state.clients.push(client)
		},
		addMessageObserver (state, roomid, observer) {
			(state.messagesObservers[roomid] = state.messagesObservers[roomid] || [])
				.push(observer)
		}
	}
}
