<template>
	<div class="ui container">
		<div class="ui grid">
			<div class="ui sixteen wide column empty-header">
				<!--	Header-->
			</div>
			<div class="ui four wide column centered row">
				<template v-if="!state.isWaiting">
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
				</template>
				<template v-else>
					<div class="ui segment">
						<div class="ui active dimmer">
							<div class="ui text loader">Waiting to log in</div>
						</div>
						<p></p>
					</div>
				</template>
			</div>
		</div>
	</div>
</template>

<script>
	import {ChatAddress, MasterSlaveDataKey} from '../config/constants.js'
	import {BroadcastMessage} from '../mixins/master-slave-mixin.js'
	import {toastError, toastSuccess} from '../helpers/toastr.js'

	export default {
		name: 'login',
		data() {
			return {
				username: '',
				state: this.$store.state
			}
		},
		watch: {
			'state.isWaiting'(val, old) {
				if (!val && old) {
					this.$router.push({
						name: 'home'
					})
				}
			}
		},
		methods: {
			onUserNameSubmit() {
				this.$store.dispatch('login', {
					username: this.username
				}).then(data => {
					if (data) {
						// note: Im master so I sent connection request & received user
						// todo: notify client about successful login
						
						this.state[MasterSlaveDataKey].channel
						.postMessage(
							new BroadcastMessage(ChatAddress.LoggedIn, data)
						)
						
						toastSuccess('Logged in!', 'Login successful')
						
						this.$router.push({
							name: 'home'
						})
					} else {
						// note: Request has been sent over BroadcastChannel now I have to wait
						this.$store.commit('setIsWaitingForLogin', true)
					}
				})
				.catch(err => {
					if (err.code === 14)
						toastError('Network error', 'Could not contact remote server to authenticate')
				})
			}
		}
	}
</script>

<style scoped>
	.empty-header {
		margin-top: 10%;
	}
</style>