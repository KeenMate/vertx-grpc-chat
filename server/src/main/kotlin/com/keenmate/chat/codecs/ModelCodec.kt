package com.keenmate.chat.codecs

import com.google.errorprone.annotations.Var
import com.keenmate.chat.models.ClientModel
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import java.io.*

class ModelCodec<TModel>(val modelClassName: String) : MessageCodec<TModel, TModel> {
	override fun decodeFromWire(pos: Int, buffer: Buffer?): TModel {
		if (buffer == null)
			throw Exception("Buffer of event bus is null")  
		
		val size = buffer.getInt(pos)
		
		val bytes = buffer.getBytes(pos + 4, pos + 4 + size)

		val inputStream = ByteArrayInputStream(bytes)
		val objectInputStream = ObjectInputStream(inputStream)
		
		return objectInputStream.readObject() as TModel
	}

	override fun systemCodecID(): Byte {
		return -1
	}

	override fun encodeToWire(buffer: Buffer?, s: TModel?) {
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

	override fun transform(s: TModel?): TModel {
		if (s == null)
			throw Exception("Error from codec - s is null")
		
		return s
	}

	override fun name(): String {
		return "${modelClassName}Codec"
	}

}