package com.keenmate.chat.codecs

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ByteCodec<T>(val name: String): MessageCodec<T, T> {
	override fun decodeFromWire(pos: Int, buffer: Buffer?): T {
		if (buffer == null)
			throw Exception("Buffer of event bus is null")

		val size = buffer.getInt(pos)

		val bytes = buffer.getBytes(pos + 4, pos + 4 + size)

		val inputStream = ByteArrayInputStream(bytes)
		val objectInputStream = ObjectInputStream(inputStream)

		return objectInputStream.readObject() as T
	}

	override fun systemCodecID(): Byte {
		return -1
	}

	override fun encodeToWire(buffer: Buffer?, s: T?) {
		if (s == null || buffer == null)
			return

		val byteArrayStream = ByteArrayOutputStream()
		val objectOutputStream = ObjectOutputStream(byteArrayStream)

		objectOutputStream.writeObject(s)
		objectOutputStream.close()

		val byteArray = byteArrayStream.toByteArray()
		buffer.appendInt(byteArray.size)
		buffer.appendBytes(byteArray)
	}

	override fun transform(s: T?): T {
		if (s == null)
			throw Exception("Error from codec - s is null")

		return s
	}

	override fun name(): String {
		return "${name}ByteCodec"
	}
}