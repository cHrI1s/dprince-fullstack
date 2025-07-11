<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<h5 class="my-0">{{ titleText }}</h5>
	</div>
</template>


<script>
	export default {
		name : "HeaderTitle",
		props: {
			titleText: {
				required: true,
				default(){
					return null;
				}
			},
   
		}
	}
</script>

<style scoped lang="scss">

</style>