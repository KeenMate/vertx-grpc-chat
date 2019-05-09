package com.keenmate.chat.filters.transport

import io.grpc.Attributes
import io.grpc.ServerTransportFilter

class CustomTransportFilter: ServerTransportFilter() {
	override fun transportTerminated(transportAttrs: Attributes?) {
		println("Transport terminated... available attrs:")
		println(transportAttrs)
	}
}