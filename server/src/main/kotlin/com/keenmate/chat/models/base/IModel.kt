package com.keenmate.chat.models.base

interface IModel<TTarget> {
	fun convert(): TTarget

	// fun parseFrom(src: String): IModel<TTarget>
	fun parseFrom(src: TTarget): IModel<TTarget>
}