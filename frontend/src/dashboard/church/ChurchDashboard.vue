<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer>
		<DashboardCard header-title="Options">
			<div class="row">
				<div class="col-md-4 mb-4"
				     v-for="(option, index) in options"
				     :key="index">
                    <router-link :to="option.url">
                        <div class="d-block position-relative border rounded-3
                            py-5 bg-light cursor-pointer text-center">
                            <h4>{{option.label}}</h4>
                        </div>
                    </router-link>
				</div>
			</div>
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";

export default {
	name: "ChurchDashboard",
	components: {DashboardCard, DashboardContainer},
	data(){
		return {
			options : [
				{ label: "Create Church", url:"", userType: ["SUPER_ADMIN"]},
				{ label: "Add Member", url:""},
				{ label: "Show Members", url:"/church/list-members"},
			]
		}
	}
}
</script>

<style scoped lang="scss">

</style>