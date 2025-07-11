<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<script>
export default {
	name: "FormLabel",
	props : {
		labelText: {
			type : String,
			required: true,
			default(){
				return null;
			}
		},
		required: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		}
	}
}
</script>

<template>
	<div class="d-flex justify-content-between position-relative mb-1 fw-bold">
		<div class="d-block position-relative">
			{{ labelText }}
			<span class="text-danger fw-bolder"
			      v-if="required">*</span>
		</div>
		
		<slot/>
	</div>
</template>

<style scoped lang="scss">

</style>