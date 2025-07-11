<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<footer class="py-3 footer">
		<div class="container-fluid d-flex justify-content-between position-relative">
			<div class="mb-4">
				<div class="text-sm text-center copyright text-muted">
					&copy; {{ new Date().getFullYear() }}
				</div>
			</div>
			
			<div class="text-sm mb-4">
				V1.0.0
			</div>
		</div>
	</footer>
</template>

<script>
export default {
	name: "app-footer",
};
</script>
