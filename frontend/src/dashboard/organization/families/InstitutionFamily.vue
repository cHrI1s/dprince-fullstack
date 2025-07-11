<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
	                    :show-institution-name="true">
		<DashboardTab :tabs="['List Of Families', 'Create' ,'List Marriages']"
		              :selected-tab="selectedTab"
		              @select="changeTab"/>
		
		<FamilyList v-if="selectedTab===0"
		            :isChurch="isInstitutionChurch"
		            @update="startFamilyUpdate"/>
		
		<FamilyCreator v-if="selectedTab===1"
		               :institution="institution"
		               :is-church="isInstitutionChurch"/>
		
		<ListMarriages :is-church="isInstitutionChurch"
		               v-if="selectedTab===2"/>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import FamilyCreator from "@/dashboard/organization/families/FamilyCreator.vue";
import FamilyList from "@/dashboard/organization/families/FamilyList.vue";
import {FAMILY_CREATION_MODEL, SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import ListMarriages from "@/dashboard/organization/families/ListMarriages.vue";

export default {
	name: "InstitutionFamily",
	components: {FamilyList, FamilyCreator, DashboardTab, DashboardContainer, ListMarriages},
	computed: {
		selectedTab: {
			get: function(){
				return this.tabLocal;
			}, set:function(newValue){ this.tabLocal = newValue; }
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		family: {
			get: function(){
				return (this.$store.getters.getFamily!==null)
					? this.$store.getters.getFamily
					: {...FAMILY_CREATION_MODEL};
			}, set:function(newValue){
				this.$store.commit("setFamily", newValue);
			}
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		}
	},
	data(){
		return {
			tmpFamily: {...FAMILY_CREATION_MODEL},
			tabLocal: this.tab
		}
	},
	methods: {
		startFamilyUpdate: function(family){
			this.$store.commit("setFamily", family);
			this.changeTab(1);
		},
		changeTab: function(tabIndex){
			this.selectedTab = tabIndex;
			let url = (this.isInstitutionChurch)
				? "/church/families-board/"
				: "/organizations/families-board/";
			this.$router.push(url+tabIndex);
		},
	},
	watch: {
		isChurch: function(){
			this.$root['handleInstitutionTypeChange']();
		},
		tab: function(newValue){
			this.tabLocal = newValue;
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
	}
}
</script>

<style scoped lang="scss">

</style>