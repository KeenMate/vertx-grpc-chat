package com.keenmate.chat

import com.keenmate.chat.models.*

class Constants {	
	class CodecNames {
		companion object {
			const val ByteCodec = "ByteCodec"
			const val ArrayListByteCodec = "ArrayListByteCodec"
			
			fun clientChangeModelName(isArray: Boolean = false): String = modelName(UserChangeModel::class.simpleName!!, isArray)
			
			fun newRoomRequestModelName(isArray: Boolean = false): String = modelName(NewRoomRequestModel::class.simpleName!!, isArray)
			
			fun roomModelName(isArray: Boolean = false): String = modelName(RoomModel::class.simpleName!!, isArray)

			fun clientModelName(isArray: Boolean = false): String = modelName(UserModel::class.simpleName!!, isArray)

			fun chatEventModelName(isArray: Boolean = false): String = modelName(ChatEventModel::class.simpleName!!, isArray)

			fun connectRequestModelName(isArray: Boolean = false): String = modelName(ConnectRequestModel::class.simpleName!!, isArray)

			fun joinRoomRequestModelName(isArray: Boolean = false): String = modelName(JoinRoomRequestModel::class.simpleName!!, isArray)

			fun messageModelName(isArray: Boolean = false): String = modelName(MessageModel::class.simpleName!!, isArray)

			private fun modelName (name: String, isArray: Boolean): String {
				return "$name${if (isArray) "Array" else ""}Codec"
			}
		}
	}
	
	class Dao {
		companion object {
			val ClientChatChange = { id: String ->
				"user.$id.chat-change"
			}
			
			const val GetMessages = "chat-dao.get-messages"
			const val GetClients = "chat-dao.get-users"
			const val GetClientsForRoom = "chat-dao.get-users-for-room"
			const val GetClient = "chat-dao.get-user"
			const val GetRooms = "chat-dao.get-rooms"
			const val GetRoom = "chat-dao.get-room"

			const val ChangeChat = "chat-dao.change-chat"
			const val AddUser = "chat-dao.add-user"
			const val AddRoom = "chat-dao.add-room"
			
			const val RoomAdded = "chat-dao.room-added"
			const val ClientChanged = "chat-dao.user-changed"
		}
	}
}
