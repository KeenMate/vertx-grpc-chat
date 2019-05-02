<template>
	<div class="ui comments">
		<div class="ui dividing header">
			{{title}}
		</div>
		<Message v-for="msg in room.messages" :key="msg.messageid"
		         :msg="msg"
		         :edit-visible="msg.author.clientid === state.user.clientid"/>
		<!--<div
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
		</div>-->
	</div>
</template>

<script>
	import Message from './message.vue'
	
	export default {
		name: 'chat',
		components: {
			Message
		},
		props: [
			'title',
			'room',
			'updater'
		],
		data () {
			return {
				state: this.$store.state
			}
		}
	}
</script>

<style scoped>

</style>