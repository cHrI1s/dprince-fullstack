<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer>
		<DashboardTab :tabs="['List', 'Update/Create']"
		              :selected-tab="selectedTab"
		              @select="changeTab"/>
		
		<SuperAdminsList :table-headers="['First name', 'Last name', 'flori']"
		                 v-if="selectedTab===0"
		                 :content="tableData"
		                 @update-password="handleEjectedRow($event, true)"
		                 @next="loadNextPage"
		                 @previous="loadPreviousPage"
		                 @search="search"
		                 @row="handleEjectedRow($event, false)"/>
		
		
		<SuperAdminCreator v-if="selectedTab===1"
                           :label="changeLabelOnUpdate"
		                   @workFinished="handleCreationWorkFinish"
		                   :passwordUpdate="userUpdate"
		                   :user="tmpUser"/>
	</DashboardContainer>
</template>

<script>
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import SuperAdminsList from "@/dashboard/administrators/SuperAdminsList.vue";
import SuperAdminCreator from "@/dashboard/administrators/SuperAdminCreator.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import {ADMINISTRATOR_CREATION_MODEL} from "@/dashboard/administrators/administrators";

export default {
	name: "SuperAdministrators",
	components: {
		SuperAdminCreator,
		SuperAdminsList,
		DashboardContainer,
		DashboardTab,
	},
    props:{
    
    },
	data(){
		return {
            changeLabelOnUpdate : false,
			selectedTab : 0,
			tmpUser: {...ADMINISTRATOR_CREATION_MODEL},
			userUpdate: false,
			searchModel: {...PAGINATOR_SEARCH_MODEL},
			tableData: {...PAGE_MODEL}
		}
	},
	methods: {
		handleCreationWorkFinish: function(row){
			// in case this was an update
			let missed = true;
			this.tableData.content = this.tableData.content.map(singleElement=>{
				if(singleElement.id===row.id){
					missed = false;
					return row;
				}
				return singleElement;
			});
			if(missed) {
				this.tableData = {
					...this.tableData,
					content: [
						{...row},
						...this.tableData.content
					],
					totalElements: this.tableData.totalElements + 1
				};
			}
			this.tmpUser = {...ADMINISTRATOR_CREATION_MODEL};
			this.userUpdate = false;
		},
		handleEjectedRow: function(row, isUpdate=false){
			this.tmpUser = row;
			this.userUpdate = isUpdate;
			this.changeTab(1);
		},
		changeTab: function(tabIndex){
			this.selectedTab = tabIndex;
		},
		deleteRow: function(row){
			let id = row.id;
			this.$api.delete(`/user/delete/${id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(row=>{
						return row.id!==id;
					})
				};
				this.$root['showAlert']('success', "User deletion", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "User deletion.");
			});
		},
		search: function(search){
			this.searchModel = search;
			this.$api.post("/user/list", search).then(response=>{
				this.tableData = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, "Loading Users.");
			});
		},
		loadNextPage: function(){
			this.searchModel = {
				...this.searchModel,
				page: this.searchModel.page+1
			};
			this.search(this.searchModel);
		},
		loadPreviousPage: function(){
			this.searchModel = {
				...this.searchModel,
				page: this.searchModel.page-1
			};
			this.search(this.searchModel);
		},
	},
	beforeMount(){
		this.search(this.searchModel);
	}
}
</script>

<style scoped lang="scss">

</style>