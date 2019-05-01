<template>
	<div class="ui container">
		<div class="ui grid">
			<div class="ui sixteen wide column empty-header">
				<!--	Header-->
			</div>
			<div class="ui four wide column centered row">
				<div class="ui labeled input">
					<div class="ui label">
						Username:
					</div>
					<input
						id="username"
						type="text"
						placeholder="Username"
						v-model="username"
						@keypress.enter="onUserNameSubmit">
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import { ConnectRequest } from '../grpc/User_pb'

	export default {
		name: 'login',
		data () {
			return {
				username: '',
				state: this.$store.state
			}
		},
		methods: {
			onUserNameSubmit () {
				const connectRequest = new ConnectRequest()
				connectRequest.setName(this.username)
				
				this.$store.state.userProvider.connect(connectRequest, {}, (err, data) => {
					if (err) {
						console.error(err)
						
						return
					}
					
					this.$store.commit('setUser', data.toObject(true))
					window.localStorage.setItem('user', this.state.user)
					
					this.$router.push({
						name: 'home'
					})
				})
			}
		},
		mounted () {
		}
	}
</script>

<style scoped>
	.empty-header {
		margin-top: 10%;
	}
</style>