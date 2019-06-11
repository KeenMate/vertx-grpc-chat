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
		</div>
	</div>
</template>

<script>
	import {MasterSlaveDataKey} from '../../config/constants.js'

	export default {
		name: 'message',
		props: [
			'msg'
		],
		data () {
			return {
				state: this.$store.state,
				creatorName: ''
			}
		},
		created () {
			this.creatorName = (this.state[MasterSlaveDataKey].sharedState.rooms.find(x => x.roomid === this.msg.roomid)
			.users.find(x => x.userguid === this.msg.creatorguid) || {})
				.name || 'Unknown'
		}
	}
</script>
