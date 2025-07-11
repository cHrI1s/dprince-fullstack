<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<SuperDashboardHome/>
	</div>
</template>
<script>
import SuperDashboardHome from "@/dashboard/stats/SuperDashboardHome.vue";
export default {
	name: "dashboard-default",
	data() {
		return {
		};
	},
	components: {
		SuperDashboardHome,
	},
};
</script>
