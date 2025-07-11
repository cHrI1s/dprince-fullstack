<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="row rounded-0 bg-white text-center mt-4 align-items-center">
		<div class="col-4 cursor-pointer rounded-3">
			<Button label="Previous"
			        @click="$emit('previous')"
			        icon="pi pi-angle-left"
			        severity="warn"
			        class="w-100"/>
		</div>
		<div class="col-4">
			<div class="d-block position-relative">
				<div class="d-block position-relative text-xs">
					<span class="fw-bolder">
						{{ totalElements }}
					</span>
					{{ totalElements>1 ? 'elements' : 'element' }}
				</div>
				<div class="d-block position-relative text-xs">
					CurrentPage: <span class="fw-bolder">
						{{ currentPage }}
					</span>
				</div>
			</div>
		</div>
		<div class="col-4 cursor-pointer rounded-3">
			<Button label="Next"
			        @click="$emit('next')"
			        icon="pi pi-angle-right"
			        icon-pos="right"
			        severity="warn"
			        class="w-100"/>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
export default {
	name: "DataPaginator",
	emits:["next", "previous"],
	components: {
		Button
	},
	props: {
		totalElements: {
			type : Number,
			required : true,
			default(){
				return 0;
			}
		},
		currentPage: {
			type : Number,
			required : true,
			default(){
				return 1;
			}
		}
	}
}
</script>

<style lang="scss">

</style>