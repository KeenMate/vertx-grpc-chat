<template>
	<div class="ui relaxed divided list">
		<div class="item" v-for="item in items" :key="item[keyProp]" @click="onClick(item)">
			<div class="content">
				<slot :item="item"></slot>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'list',
		props: [
			'items',
			'keyProp'
		],
		methods: {
			onClick (item) {
				this.$emit('click', item)
			}
		}
	}
</script>

<style scoped>

</style>