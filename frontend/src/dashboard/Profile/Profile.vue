<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :show-institution-name="true">
		<SuperAdministratorProfile/>
	</DashboardContainer>
</template>

<script>
import SuperAdministratorProfile from "@/dashboard/administrators/SuperAdministratorProfile.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";


export default {
	name: "profile-overview",
	data() {
		return {
			showMenu: false,};
	},
	components: {
		DashboardContainer,
		SuperAdministratorProfile,
	},
};
</script>
