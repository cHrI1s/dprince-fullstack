<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard :header-title="cardTitle">
		<PaginatedTable :table-data="tableData"
		                :table-headers="tableHeaders"
		                :date-search="false"
		                :row-options="rowOptions"
		                :paginated="false"
		                @sort="(newContent)=>tableData={...newContent}"
		                @options="actionPerform"
		                @search="performSearch"
		                @next="loadNextPage"
		                @previous="loadPreviousPage"
		                :paginator-title="null"/>
	</DashboardCard>
</template>


<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {USER_TYPES} from "@/dashboard/users/users";

export default {
	name: "AssistantAdminsList",
	emits: ["update"],
	components: {PaginatedTable, DashboardCard},
	computed: {
		userType: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			return loggedInUser.userType;
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		cardTitle: function(){
			return this.isChurch ? "Church Assistant Admins" : "Organization Assistant Admins";
		}
	},
	data(){
		return {
			tableData: {...PAGE_MODEL},
			rowOptions: [
				{label: "Update", method: "updateAdmin"},
				{label: "Delete", method: "deleteAdmin"},
			],
			searchModel : {
				...PAGINATOR_SEARCH_MODEL
			},
			//Change table name according to whether the institution is CHURCH OR GENERAL
			tableHeaders: {
				'Full name': {
					rows: [
						{type: "String", attribute: "firstName"},
						{type: "String", attribute: "lastName"}
					]
				},
				'Username': {
					rows: [
						{type: "String", attribute: "username", lowercase:true}
					]
				},
				'Staff Id': {
					rows: [
						{type: "String", attribute: "staffId"}
					]
				},
				'Role': {
					rows: [
						{type: "Select", attribute: "userType", options: [...USER_TYPES]},
					]
				}
			},
		}
	},
	methods: {
		updateAdmin: function(row){
			this.$emit("update", row);
		},
		deleteAdmin: function(row){
			this.$api.delete(`/institution/delete-admin/${row.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(item=>{
						return item.id!==row.id;
					})
				};
				this.$root['showAlert']("success", "Admin deletion", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Admin deletion");
			})
		},
		loadAdmins: function(){
			if(this.$root['isInstitutionSet']()) {
				let data = this.$root['addInstitutionId'](this.searchModel);
				this.$api.post("/institution/list-admins", data).then(response => {
					this.tableData = response;
					this.$root['showAlert']("success", "Admin", "Loaded!.");
				}).catch(error => {
					this.$root['handleApiError'](error, "Admins Loading.");
				});
			}
		},
		performSearch: function(searchModel){
			this.searchModel = {
				...this.searchModel,
				...searchModel
			}
			this.loadAdmins();
		},
		loadNextPage : function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page+1
			};
			this.loadAdmins();
		},
		loadPreviousPage : function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page-1
			};
			this.loadAdmins();
		},
		actionPerform: function(value){
			this[value.method](value.row);
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		}
	},
	beforeMount(){
		this.loadAdmins();
	},
}
</script>

<style scoped lang="scss">

</style>