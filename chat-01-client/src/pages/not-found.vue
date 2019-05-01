<template>
	<div class="ui container">
		<h3>
			File, you requested could not be found!
		</h3>
		
		<router-link :to="{ name: 'home' }"></router-link>
	</div>
</template>

<script>
	export default {
		name: 'NotFound'
	}
</script>

<style scoped>

</style>