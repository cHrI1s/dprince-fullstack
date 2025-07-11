<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative mb-4 text-dark fw-bolder">
		<h4 class="text-dark fw-bolder mb-0">{{ institutionName }}</h4>
	</div>
</template>


<script>
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {CHURCH_MAIN_BRANCH_ADMINISTRATORS} from "../../members/members";

export default {
	name: "InstitutionHeader",
	computed: {
		institutionName: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				let userFullName = loggedInUser.firstName;
				if(this.$store.getters.getInstitution!==null){
					userFullName +=" -> "+this.$store.getters.getInstitution.name;
				}
				return "Hello, "+userFullName.toUpperCase();
			} else {
				let title = loggedInUser.institution.name;
				if(CHURCH_MAIN_BRANCH_ADMINISTRATORS.includes(loggedInUser.userType)){
					if(this.$store.getters.getInstitution!==null){
						title = this.$store.getters.getInstitution.name;
					}
				}
				if(title===null) title = "";
				return title.toUpperCase();
			}
		},
	}
}
</script>

<style scoped lang="scss">

</style>