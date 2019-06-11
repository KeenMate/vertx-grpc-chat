package com.keenmate.chat.helpers

import com.keenmate.chat.Constants
import com.keenmate.chat.codecs.ByteCodec
import com.keenmate.chat.codecs.UnitCodec
import com.keenmate.chat.models.*
import io.vertx.core.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList

fun registerDefaultCodecs(eventBus: EventBus) {
	eventBus
		.registerDefaultCodec(ArrayList::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec + "1"))
		.registerDefaultCodec(Collections.singletonList(Any::class.java)::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec + "2"))
		.registerDefaultCodec(emptyList<Any>()::class.java, ByteCodec(Constants.CodecNames.ArrayListByteCodec))
		.registerDefaultCodec(NewRoomRequestModel::class.java, ByteCodec(Constants.CodecNames.newRoomRequestModelName()))
		.registerDefaultCodec(UserModel::class.java, ByteCodec(Constants.CodecNames.clientModelName()))
		.registerDefaultCodec(ChatEventModel::class.java, ByteCodec(Constants.CodecNames.chatEventModelName()))
		.registerDefaultCodec(ConnectRequestModel::class.java, ByteCodec(Constants.CodecNames.connectRequestModelName()))
		.registerDefaultCodec(JoinRoomRequestModel::class.java, ByteCodec(Constants.CodecNames.joinRoomRequestModelName()))
		.registerDefaultCodec(MessageModel::class.java, ByteCodec(Constants.CodecNames.messageModelName()))
		.registerDefaultCodec(RoomModel::class.java, ByteCodec(Constants.CodecNames.roomModelName()))
		.registerDefaultCodec(UserChangeModel::class.java, ByteCodec(Constants.CodecNames.clientChangeModelName()))
		.registerDefaultCodec(Unit::class.java, UnitCodec())
}
