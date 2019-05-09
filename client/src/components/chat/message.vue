<template>
	<div
		class="comment">
		<div class="content">
				<span class="author">
					{{ creatorName }}
				</span>
			<div class="metadata">
					<span class="date">
						{{ msg.sentText || 'date not available' }}
					</span>
			</div>
			<div class="text">
				{{ msg.content }}
			</div>
			<div class="actions">
				<a v-if="editVisible" class="edit" @click="onEdit">Edit</a>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'message',
		props: [
			'msg',
			'editVisible'
		],
		data () {
			return {
				state: this.$store.state,
				creatorName: ''
			}
		},
		methods: {
			onEdit () {
				// this.$emit('edit')
			}
		},
		created () {
			this.creatorName = (this.state.rooms.find(x => x.roomid === this.msg.roomid)
			.clients.find(x => x.clientguid === this.msg.creatorguid) || {})
				.name || 'Unknown'
		}
	}
</script>
