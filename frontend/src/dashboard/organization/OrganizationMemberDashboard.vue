<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
		                    :showInstitutionName="true">
			<DashboardTab :tabs="tabList"
			              :selected-tab="selectedTab"
			              @select="changeTab"/>
			
			<OrganizationMember v-if="selectedTab===1"
			                    :is-church="isInstitutionChurch"
			                    :header-title="isInstitutionChurch ? 'Create Member' : 'Create Partner'"
			                    :organization="institution"
			                    :member-model="memberModel"
			                    :hasSubscriptionType="hasSubscriptionType"
			                    @set-member="setMember"/>
			
			<OrganizationMemberList v-if="selectedTab===0"
			                        :institution="institution"
			                        :is-church="isInstitutionChurch"
			                        @update="handleMemberUpdate"
			                        @insertNonInstitutionalFamilyMember="handleNonInstitutionFamilyMember"/>
		</DashboardContainer>
	</div>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import OrganizationMember from "@/dashboard/organization/OrganizationMember.vue";
import OrganizationMemberList from "@/dashboard/organization/OrganizationMemberList.vue";
import {PARTNER_MODEL} from "@/dashboard/organization/Organization";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "OrganizationMemberDashboard",
	components: {DashboardTab, DashboardContainer, OrganizationMember,
		OrganizationMemberList},
	data(){
		return {
			selectedTab: this.tab,
			memberModel: {...PARTNER_MODEL},
            hasSubscriptionType: false,
			isInstitutionChurch: this.isChurch
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
		tabList: function(){
			return this.isInstitutionChurch
				? ['List Members', 'New Member']
				: ['List Partner', 'New Partner'];
		}
	},
	methods: {
		setMember: function(newMember){
            this.hasSubscriptionType = newMember!==null;
			this.$store.commit("addInstitutionMember", newMember);
			this.changeTab(0);
		},
        handleNonInstitutionFamilyMember: function(){
            this.hasSubscriptionType = true;
            this.changeTab(1);
        },
		handleMemberUpdate: function(member){
			this.memberModel = {...member};
			this.changeTab(1);
		},
		changeTab: function(tabIndex, reload = false){
			this.selectedTab = tabIndex;
            if(tabIndex===0) this.hasSubscriptionType = true;
            let url = (this.isInstitutionChurch)
                ? "/church/members-dashboard/"
                : "/organizations/partners-dashboard/"
			this.$router.push(url+this.selectedTab, {reload : true});
		},
	},
	watch: {
		isChurch: function(newValue){
			this.isInstitutionChurch = newValue;
			this.changeTab(0);
		},
		tab: function(newValue){ this.selectedTab = newValue; }
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
	}
}
</script>

<style scoped lang="scss">

</style>