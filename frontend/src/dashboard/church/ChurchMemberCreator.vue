<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer>
		<OrganizationMember :organization="church"
		                    :isChurch="true"
		                    :member-model="churchMember"
		                    header-title="Add church member"/>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import {CHURCH_MEMBER_MODEL} from "@/dashboard/church/church";
import OrganizationMember from "@/dashboard/organization/OrganizationMember.vue";

export default {
	name: "ChurchMemberCreator",
	components: {OrganizationMember, DashboardContainer},
	data(){
		return {
			church: {},
			churchMember: {...CHURCH_MEMBER_MODEL}
		}
	}
}
</script>

<style scoped lang="scss">

</style>