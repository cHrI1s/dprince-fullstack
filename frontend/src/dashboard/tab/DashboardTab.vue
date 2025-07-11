<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<script>
export default {
	name: "DashboardTab",
	emits: ['select'],
	props : {
		selectedTab: {
			required: true,
			type: Number,
			default(){
				return 0;
			}
		},
		tabs: {
			required: true,
			type : Array,
			default(){
				return [];
			}
		},
  
	}
}
</script>

<template>
	<div class="d-flex mb-4 bg-white rounded-3" >
		<div class="d-flex position-relative align-items-center flex-grow-1 cursor-pointer rounded-3"
		     v-for="(singleTab, index) of tabs"
		     :class="{'selected text-white' : selectedTab===index}"
		     :key="index"
		     @click="$emit('select',index)">
			<div class="w-100 p-3 text-center cursor-pointer">
				<div class="fw-bolder cursor-pointer">
					{{ singleTab }}
				</div>
			</div>
		</div>
	</div>
</template>

<style lang="scss">
	.selected{
		background-image: linear-gradient(195deg, #66bb6a 0%, #43a047 100%);
	}
</style>