<template>
	<div class="container-fluid py-4">
		<div class="text-center"><h2 class="my-0">Terms And Conditions</h2></div>
		
		<div class="d-block position-relative">
		
		</div>
	</div>
</template>

<script>
export default {
	name: "TermsConditions",
	beforeMount(){
		this.$store.commit("toggleEveryDisplay", true);
		this.$store.commit("toggleHideConfig", true);
	},
	beforeUnmount() {
		this.$store.commit("toggleEveryDisplay", false);
		this.$store.commit("toggleHideConfig", false);
	},
}
</script>

<style scoped lang="scss">

</style>