<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<DashboardContainer :style="'pb-0 pe-md-4 ps-0'"
		                    :show-institution-name="true">
			
			<DashboardTab :tabs="['List Admins', 'New Admin']"
			              :selected-tab="selectedTab"
			              @select="changeTab"/>
			
			
			<AssistantAdminCreator v-if="selectedTab===1"
			                       :isChurch="isChurch"
			                       :admin="adminModel"
			                       @save="saveAdmin"/>
			
			<AssistantAdminsList v-if="selectedTab===0"
			                     @update="handleUpdate"
			                     :isChurch="isChurch"/>
		</DashboardContainer>
	</div>
</template>


<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import AssistantAdminCreator from "@/dashboard/users/AssistantAdminCreator.vue";
import AssistantAdminsList from "@/dashboard/users/AssistantAdminsList.vue";
import {ADMINISTRATOR_CREATION_MODEL} from "@/dashboard/administrators/administrators";
import {ASSISTANT_ADMIN_MODEL} from "@/dashboard/users/users";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "AssistantAdminDashboard",
	components: {AssistantAdminsList, AssistantAdminCreator, DashboardTab, DashboardContainer},
	data(){
		return {
			adminModel: {...ASSISTANT_ADMIN_MODEL},
			selectedTab: this.tab,
		}
	},
	methods: {
		changeTab: function(tabIndex){
			this.selectedTab = tabIndex;
			let url = (this.isChurch)
				? "/church/assistant-admin-dashboard/"
				: "/organizations/assistant-admin-dashboard/";
			this.$router.push(url+this.selectedTab);
		},
		saveAdmin: function(admin){
			admin = this.$root['addInstitutionId'](admin);
			let url = (typeof admin.id!=="undefined")
				? "/institution/update-admin"
				: "/institution/save-admin";
			this.$api.post(url, admin).then(response=>{
				this.adminModel = {...ASSISTANT_ADMIN_MODEL};
				this.$root['showAlert']("success", "Assistant Admin Creation", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Administrator Save");
			});
		},
		handleUpdate: function(admin){
			this.adminModel = {
				...admin,
				adminCode: admin.staffId
			};
			this.changeTab(1);
		}
	},
	watch: {
		tab: function(newValue){
			this.selectedTab = newValue;
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		tab: {
			type: Number,
			required: false,
			default(){
				return 0;
			}
		}
	},
	computed: {
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
	}
}
</script>

<style scoped lang="scss">

</style>