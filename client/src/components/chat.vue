<template>
	<div class="ui comments">
		<div class="ui dividing header">
			{{title}}
		</div>
		<div
			class="comment"
			v-for="msg in room.messages" :key="msg.messageid">
			<div class="content">
				<span class="author">
					{{ (msg.author || {}).name }}
				</span>
				<div class="metadata">
					<span class="date">
						{{ msg.sentText }}
					</span>
				</div>
				<div class="text">
					{{ msg.content }}
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'chat',
		props: [
			'title',
			'room',
			'updater'
		]
	}
</script>

<style scoped>

</style>