<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Super Administrators">
		<PaginatedTable :table-headers="tableHeaders"
		                :table-data="tableData"
		                :row-options="rowOptions"
		                @sort="(newContent)=>tableData={...newContent}"
		                @search="search"
		                @options="actionPerformed"
		                @next="$emit('next')"
		                @previous="$emit('previous')"
		                :date-search="false"
		                :paginator-title="'List'"/>
	</DashboardCard>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {USER_TYPES} from "@/dashboard/users/users";
import {GENDERS} from "@/dashboard/members/members";

export default {
	name: "SuperAdminsList",
	emits: ["emits", "row", "search", 'next', "previous", "updatePassword"],
	components: {
		PaginatedTable,
        DashboardCard,
    },
    data(){
        return {
            isEditing : false,
	        tableData: this.content,
	        rowOptions: [
		        {label : "Delete", method: "deleteRow" },
		        {label : "Update", method:"updateRow" },
		        {label : "Set Password", method:"setPassword" }
	        ],
	        tableHeaders: {
				"First name": {
					rows: [
						{type : "String", attribute: "firstName"},
						{type : "String", attribute: "username", lowercase: true}
					]
				},
		        "Gender": {
					rows: [
						{type : "Select", attribute: "gender", options: [...GENDERS]},
					]
				},
		        "Role": {
			        rows: [
				        {type : "Select", attribute: "userType", options: [...USER_TYPES]}
			        ]
				}
	        }
        }
    },
	watch: {
		content: function(newValue){
			if(newValue!==this.tableData) this.tableData = newValue;
		}
	},
    props:{
        title: {
            type: String,
            default(){
                return null;
            }
        },
        containerClass: {
            type: String,
            default(){
                return null;
            }
        },
        table: {
            type: Boolean,
            default(){
                return false;
            }
        },
        columnsLabels: {
            type: Array,
            default(){
                return [];
            }
        },
        content: {
            type: Object,
            default(){
                return {...PAGE_MODEL};
            }
        },
        extraOptions:{
            type: Array,
            default(){
                return [];
            }
        },
    },
    
    methods:{
	    search: function(search){
			this.$emit("search", search);
	    },
	    deleteRow: function(row){
			if(this.$store.getters.getLoggedInUser!==null
				&& this.$store.getters.getLoggedInUser.id===row.id){
				this.$root['showAlert']("warn",
					"Delete Administrator",
					"You can not delete yourself."
				);
				return;
			}
			this.$api.delete(`/user/delete/${row.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.filter(item=> {
						return item.id !== row.id;
					}),
					totalElements: this.tableData.totalElements-1,
				}
				this.$root['showAlert']("success", "Delete Administrator", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Delete Administrator");
			});
	    },
	    actionPerformed: function(value){
		    this[value.method](value.row);
	    },
	    updateRow: function(row){
			this.$emit("row", row);
            this.$store.commit("setLabel", "Update");
	    },
	    setPassword: function(row){
			this.$emit("updatePassword", row);
	    }
    },
}
</script>

<style scoped lang="scss">

</style>