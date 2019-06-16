export const HostAddress = 'http://localhost:8090'
//'http://api.chat.km-ubu-001.serv.keenmate.com'

export const VueWindowsStorageKey = 'vertx-chat-tabs'
export const BroadcastChannelName = 'VertxChatBroadcast'
export const MasterSlaveDataKey = 'masterSlaveData'
export const BroadcastAddress = {
	Echo: 0,
	EchoEcho: 1,
	NewMaster: 2,
	SlaveDied: 3
}

export const ChatAddress = {
	//NewRoom: 4, // Slaves receive Room (new or modified)
	//DeletedRoom: 8, // Slaves receive room id

	CreateRoom: 4, // Master reveives Room: { title: String, isPrivate: Boolean }
	JoinRoom: 5, // Master receives room id
	RoomJoined: 6, // Slaves receives room id

	SendMessage: 7, // Master receives Message: { content: String, roomId: Number }
	
	Chat: 8, // this address covers Chat events (new, modified, deleted) (room, user, message)
	
	LogIn: 9,
	LoggedIn: 10

	//NewMessage: 9, // Slaves receive Message (new or modified)
	//DeletedMessage: 10, // Slaves receive message id
	//
	//NewUser: 11, // Slaves receive User (new or modified)
	//DeletedUser: 12, // Slaves receive user guid
}
