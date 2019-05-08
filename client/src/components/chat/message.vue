<template>
	<div
		class="comment">
		<div class="content">
				<span class="author">
					{{ (msg.creator || {}).name }}
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
		methods: {
			onEdit () {
				// this.$emit('edit')
			}
		},
		mounted () {
			console.log("got message to display: ", this.msg)
		}
	}
</script>

<style scoped>

</style>