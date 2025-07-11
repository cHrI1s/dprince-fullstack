<script>
export default {
	name: "TermsAcknowledgement"
}
</script>

<template>
	<div class="mb-4 d-block position-relative text-dark">
		<strong>Note:</strong> By Signing In, you acknowledge you agree
		<router-link to="/terms" target="_blank"
		             alt="Terms And Conditions"
		             title="Terms & Conditions"
		             class="fw-bolder text-underline text-hoverable">Terms & Conditions</router-link> of this Application.
	</div>
</template>

<style scoped lang="scss">

</style>