package com.keenmate.chat_01.models.base

interface IModel<TTarget> {
	fun convert(): TTarget

	fun parseFrom(src: String): IModel<TTarget>
	fun parseFrom(src: TTarget): IModel<TTarget>

	override fun toString(): String
}