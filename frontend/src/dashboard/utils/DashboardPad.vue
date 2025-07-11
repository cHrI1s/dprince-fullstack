<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="row">
		<div v-for="(option, index) in options"
		     :key="index"
		     :class="option.styleClass">
			<router-link :to="option.url"
			             class="d-block position-relative py-5 text-center fw-bolder bg-white rounded-3">
				<h4 class="my-0">{{ option.label }}</h4>
			</router-link>
		</div>
	</div>
</template>


<script>

export default {
	name: "DashboardPad",
	props: {
		options: {
			type: Array,
			required: true,
			default(){
				return [];
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>