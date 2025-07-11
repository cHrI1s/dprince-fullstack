
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
			<DashboardTab :tabs="['List Event', 'Create Event']"
			              :selected-tab="selectedTab"
			              @select="changeTab"/>
			
			<ChurchEvent :event="event"
			             @save="handleSave"
			             v-if="selectedTab===1"/>
			<ListEvent @sendEvent="handleData"
			           v-if="selectedTab===0"/>
		</DashboardContainer>
	</div>
</template>


<script>
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import ChurchEvent from "@/dashboard/church/ChurchEvent.vue";
import ListEvent from "@/dashboard/church/ListEvent.vue";
import {CHURCH_EVENT} from "@/dashboard/church/church";
export default {
    name: "ChurchEventBoard",
    components: {
        ChurchEvent,
        DashboardContainer,
        DashboardTab,
        ListEvent
    },
    data(){
        return{
            selectedTab: this.tab,
            event : {...CHURCH_EVENT},
        }
    },
    methods:{
        handleSave: function(){
            this.event = {...CHURCH_EVENT};
        },
        handleData : function(data){
            this.event = data;
        },
        changeTab: function(tabIndex){
            this.selectedTab = tabIndex;
            let url = "/church/event-board/";
            if(tabIndex===0) this.event = {...CHURCH_EVENT};
            this.$router.push(url+this.selectedTab);
        },
    },
    props:{
        tab: {
            type: Number,
            required: false,
            default(){
                return 0;
            }
        },
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