<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<script>
export default {
	name: "SessionCleaner",
	beforeMount(){
		this.$store.commit("setLoading", true);
		this.$root['cleanSession']();
		this.$store.commit("setLoading", false);
	}
}
</script>

<template>
	<div class=""></div>
</template>

<style scoped lang="scss">

</style>