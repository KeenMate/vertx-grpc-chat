<template>
	<div>
		<div class="ui top attached menu">
			<a href="" class="item" @click="clearUser">
				{{$store.state.user && $store.state.user.name}}
			</a>
			<!-- Menu here -->
		</div>
		<div class="custom-container padded-top">
			<router-view></router-view>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'app',
		components: {

		},
		data () {
			return {
			}
		},
		methods: {
			clearUser () {
				window.localStorage.removeItem('user')
			}
		},
		mounted () {
		}
	}
</script>

<style scoped>
	.padded-top {
		margin-top: 15px;
	}
	
	.custom-container {
		width: 94%;
		margin-left: auto;
		margin-right: auto;
	}
</style>
