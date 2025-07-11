<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'">
		<div class="row mb-4 justify-content-end mx-0">
			<Button label="Next" icon="pi pi-arrow-right"
			        severity="info"
			        @click="goToNext"
			        class="col-md-4"/>
		</div>
		<DashboardTab :tabs="['Creator', 'Maker']"
		              :selected-tab="selectedTab"
		              @select="changeTab"/>
		
		<ContributionCreatorPad v-if="selectedTab===0"/>
		<ContributionMaker v-else/>
	</DashboardContainer>
</template>

<script>
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import ContributionCreatorPad from "@/dashboard/church/contributions/ContributionCreatorPad.vue";
import ContributionMaker from "@/dashboard/church/contributions/ContributionMaker.vue";

export default {
    name: "ChurchContributionBoard",
    components: {
	    Button,
	    ContributionMaker,
	    ContributionCreatorPad,
	    DashboardTab,
        DashboardContainer,
    },
    data(){
        return{
	        selectedTab: this.tab,
        }
    },
    methods:{
	    changeTab: function(tabIndex){
		    this.selectedTab = tabIndex;
		    this.$router.push("/church/subscription/"+this.selectedTab);
	    },
	    goToNext: function(){
			this.$router.push('/church/configuration/receipt');
	    }
    },
	watch: {
		tab: function(newValue){
			this.changeTab(newValue);
		}
	},
	props: {
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