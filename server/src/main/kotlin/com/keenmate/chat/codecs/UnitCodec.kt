package com.keenmate.chat.codecs

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

class UnitCodec: MessageCodec<Unit, Unit> {
	override fun decodeFromWire(pos: Int, buffer: Buffer?) { }

	override fun systemCodecID(): Byte = -1

	override fun encodeToWire(buffer: Buffer?, s: Unit?) { }

	override fun transform(s: Unit?) { }

	override fun name(): String = "UnitCodec"
}