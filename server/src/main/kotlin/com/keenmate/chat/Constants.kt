package com.keenmate.chat

class Constants {
	class Dao {
		companion object {
			const val GetMessages = "chat-dao.get-messages"
			const val GetClients = "chat-dao.get-clients"
			const val GetRooms = "chat-dao.get-rooms"

			const val AddMessage = "chat-dao.add-message"
			const val AddClient = "chat-dao.add-client"
			const val AddRoom = "chat-dao.add-room"

			const val MessageAdded = "chat-dao.message-added"
			const val ClientAdded = "chat-dao.client-added"
			const val RoomAdded = "chat-dao.room-added"
		}
	}
}
