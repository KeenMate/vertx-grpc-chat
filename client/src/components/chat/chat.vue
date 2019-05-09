<template>
	<div class="chat">
		<div class="ui comments">
			<div class="ui dividing header">
				{{room.title}}
			</div>
			<Message v-for="msg in room.messages" :key="msg.messageid"
			         :msg="msg"
			         :edit-visible="msg.creatorguid === state.user.clientguid"/>
		</div>
		<div class="message-box ui grid">
			<div class="ui row">
				<div class="twelve wide column">
					<textarea v-model="newMsgContent"></textarea>
				</div>
				<div class="four wide column">
					<button class="ui button" @click="onSendClick">
						Send
					</button>
				</div>
			</div>
		</div>
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
			'room',
		],
		data () {
			return {
				state: this.$store.state,
				newMsgContent: ''
			}
		},
		methods: {
			onMessageEdit(message) {
			
			},
			onSendClick () {
				this.$emit('send', this.newMsgContent)
				
				this.newMsgContent = ''
			}
		}
	}
</script>

<style scoped>
	.chat {
		position: absolute;
		width: 100%;
	}
	
	.chat .ui.comments {
		position: relative;
		top: 0;
		bottom: 100px;
		width: 100%;
	}
	
	.chat .message-box {
		position: relative;
		bottom: 0;
		height: 100px;
		width: 100%;
	}
	
	.chat .message-box textarea {
		height: 100%;
		width: 100%;
	}
</style>