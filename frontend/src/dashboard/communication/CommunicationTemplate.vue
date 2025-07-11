<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
	    <DashboardContainer class="ps-lg-0 ps-md-3 pb-0 mb-0"
	                        :show-institution-name="true">
	        <DashboardTab :tabs="['List', 'Create']"
	                      :selected-tab="selectedTab"
	                      @select="changeTab"/>
		    
		    <ListCommunication @send-template="handleTemplate"
		                       :is-church="isInstitutionChurch"
		                       v-if="selectedTab===0"/>
		    <CreateTemplate :template-data="receivedTemplate"
		                    v-else/>
	    </DashboardContainer>
	</div>
</template>
<script>
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import ListCommunication from "@/dashboard/communication/ListCommunication.vue";
import CreateTemplate from "@/dashboard/communication/CreateTemplate.vue";
import {TEMPLATE_MODEL} from "@/dashboard/communication/utils/template";
export default {
    name: "CommunicationTemplate",
    components: {
        ListCommunication,
        DashboardTab,
        DashboardContainer,
        CreateTemplate
        },
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		}
	},
    data(){
        return{
            selectedTab: this.tab,
            receivedTemplate : {...TEMPLATE_MODEL},
            templates : [],
        }
    },
    
    methods:{
        handleTemplate : function(template){
            this.receivedTemplate = template;
        },
        changeTab: function(tabIndex){
            this.selectedTab = tabIndex;
	        if(tabIndex===0) this.receivedTemplate = {...TEMPLATE_MODEL};
			let url = this.isInstitutionChurch
				? "/church/communication/template/"
				: "/organizations/communication/template/"
            this.$router.push(url+this.selectedTab);
        },
    },
    props: {
        tab: {
            type: Number,
            required: false,
            default(){
                return 0;
            }
        },
        isChurch: {
			type: Boolean,
	        required: true,
	        default(){
				return true;
	        }
        }
    },
    watch:{
        tab:function(newValue){
            this.changeTab(newValue);
        }
    }
}
</script>



<style scoped lang="scss">

</style>