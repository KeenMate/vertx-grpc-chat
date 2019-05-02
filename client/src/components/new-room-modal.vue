<template>
	<div class="ui mini modal" ref="create-room-modal">
		<div class="header">New Room:</div>
		<div class="content">
			<div class="ui centered grid">
				<div class="column">
					<div class="ui input">
						<input type="text" placeholder="Room's title" v-model="newRoomTitle">
					</div>
				</div>
			</div>
		</div>
		<div class="actions">
			<div class="ui approve button" @click="onCreateRoom">
				Create
			</div>
			<div class="ui cancel button">
				Cancel
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'NewRoomModal',
		data () {
			return {
				newRoomTitle: ''
			}
		},
		methods: {
			showModal () {
				$(this.$refs['create-room-modal']).modal('show')
			},
			onCreateRoom () {
				this.$emit('createRoom', this.newRoomTitle)
				
				this.newRoomTitle = ''
			}
		}
	}
</script>

<style scoped>

</style>