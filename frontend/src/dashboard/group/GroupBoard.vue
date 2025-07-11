
<template>
    <DashboardContainer :style="'pb-0'">
        <DashboardTab :tabs="['Group', 'List']"
                      :selected-tab="selectedTab"
                      @select="changeTab"/>
        <CommunicationGroup @list-member="handleGroupMember"
                            v-if="selectedTab===0"/>
        <ListPersonGroup :group="group"
                         v-if="selectedTab===1"
                         :list-person-disabled ="memberGroupList"
                         />
    </DashboardContainer>
</template>
<script>
import CommunicationGroup from "@/dashboard/group/CommunicationGroup.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import ListPersonGroup from "@/dashboard/group/ListPersonGroup.vue";
export default {
    name: "GroupBoard",
    components: {ListPersonGroup, DashboardTab, CommunicationGroup,DashboardContainer},
    data (){
        return{
            listTab : null,
            memberGroupList : false,
            selectedTab: this.tab,
            group : null,
        }
    },
    
    methods : {
        changeTab: function(tabIndex){
			if(tabIndex===1 && !this.memberGroupList){
				this.$root['showAlert']('warn',
					'Group',
					"No group Selected!");
				return ;
			}
            this.selectedTab = tabIndex;
            let url = this.isInstitutionChurch
	            ? "/church/group/"
	            : "/organizations/group/";
			url += this.memberGroupList===false
				? this.selectedTab = 0
				: this.selectedTab = 0;
            this.$router.push(url);
        },
        handleGroupMember: function (group){
            this.memberGroupList = true;
            this.selectedTab =  1;
            this.group = group;
        }
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