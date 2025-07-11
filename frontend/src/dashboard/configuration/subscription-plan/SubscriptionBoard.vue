<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer>
        <DashboardTab :tabs="tabs" :selected-tab="selectedTab" @select="changeTab"/>
        <CreateSubscriptionPlan v-if="selectedTab===1"
                                :default-plan="plan"/>
        <ListSubscriptionPlans v-if="selectedTab===0"
                               @change-title ="changeTitle"
                               @update="handleUpdate"/>
    </DashboardContainer>
</template>
<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import CreateSubscriptionPlan from "@/dashboard/configuration/subscription-plan/CreateSubscriptionPlan.vue";
import ListSubscriptionPlans from "@/dashboard/configuration/subscription-plan/ListSubscriptionPlans.vue";
import {SUBSCRIPTION_MODEL} from "@/dashboard/organization/Organization";

export default {
    name: "SubscriptionBoard",
    components: {ListSubscriptionPlans, CreateSubscriptionPlan, DashboardTab, DashboardContainer},
    emits : ['switchLabel'],
    data(){
        return{
            buttonLabel : "Update",
            selectedTab : this.tab,
	        plan: {...SUBSCRIPTION_MODEL},
            tabs : ['List', 'Create']
        }
    },
    methods:{
        changeTitle : function(title){
            this.tabs[1] = title;
        },
	    handleUpdate: function(plan){
			this.plan = {...plan};
			this.changeTab(1);
	    },
        
        changeTab: function(tabIndex){
	        if(tabIndex===0) this.plan = {...SUBSCRIPTION_MODEL};
            this.selectedTab = tabIndex;
            let url = "/configuration/subscription-plan-board/";
            this.$router.push(url+this.selectedTab);
            this.tabs[1] = "Create";
        },
    },
    props:{
        tab: {
            type: Number,
            required: false,
            default(){
                return 0;
            }
        }
    },
    watch:{
        tab: function(newValue){
            this.selectedTab = newValue;
        }
    }
}
</script>



<style scoped lang="scss">

</style>