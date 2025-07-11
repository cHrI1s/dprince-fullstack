<template>
    <DashboardContainer :visible="listPersonDisabled">
        <DashboardCard :header-title="group.name">
            <PaginatedTable :table-data="tableData"
                            :search-model="searchModel"
                            @options="actionPerformed"
                            @search="handleSearch"
                            @sort="(newContent)=>tableData={...newContent}"
                            @next="loadNextPage"
                            :extra-inputs="extraInputs"
                            @previous="loadPreviousPage"
                            :row-options="GroupOptions"
                            :table-headers="tableHeaders"/>
        </DashboardCard>
    </DashboardContainer>
</template>
<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {DEFAULT_PAGE} from "@/dashboard/utils/default_values";
import {GENDERS, MEMBERSHIP_DURATIONS} from "@/dashboard/members/members";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import {getCountries} from "@/dashboard/utils/countries";
import {INDIA_STATES, MEMBER_SEARCH_MODEL} from "@/dashboard/organization/Organization";

export default {
    name: "ListPersonGroup",
    components: {PaginatedTable, DashboardCard, DashboardContainer},
    data(){
        return{
	        categories : [],
            searchModel: {...MEMBER_SEARCH_MODEL},
            tableData: {...DEFAULT_PAGE},
            tableHeaders: {
                "Full name":{
                    rows:[
                        { type: "StringArray", attribute: ["firstName", "lastName"]},
	                    { type: "Date", label:"DoB", attribute:"dob"},
	                    { type: "String", label:"Code", attribute:"code"},
                    ]
                },
                "Gender":{
                    rows:[
                        { type: "Select", attribute: "gender", options:[...GENDERS]},
                    ]
                },
                "Address":{
                    rows:[
                        { type: "String", attribute: "addressLine3"},
	                    { type: "String", attribute: "addressLine2"},
	                    { type: "String", attribute: "addressLine1"},
                    ]
                },
                
                
            },
        }
    },
    computed:{
        GroupOptions : function(){
            return [
                { label : "Remove from the group", method: "delete", styleClass: "col-md-12 mb-4"},
            ]
        },
	    extraInputs : function(){
		    return [
			    { type: "TEXTAREA", styleClass:"col-md-12 mb-4", model:"partnerCodes", placeholder: "Partner Codes" },
		    ]
	    },
    },
    methods: {
        delete : function(row){
           let data  = {
               groupId : this.group.id,
               memberId : row.id
           }
           this.$api.post("/group/delete-member", data).then(response=>{
               this.$root['showAlert']("success", "Group", response.message);
            this.tableData = {
                ...this.tableData,
                totalElements: this.tableData.totalElements-1,
                content: this.tableData.content.filter(item=>{
                    return item.id!==row.id;
                })
            };
           }).catch(error=>{
               this.$root['handleApiError'](error ,"Group ");
            })
        },
	    loadCategories: function(){
		    let data = this.$root['addInstitutionId']({});
		    this.$api.post("/category/get", data).then(response=>{
			    this.categories = response.map(category=>{
				    return {
					    label: category.name,
					    value: category.id
				    }
			    });
		    }).catch(error=>{
			    this.$root['handleApiError'](error, "Categories Loading");
		    });
	    },
        actionPerformed :function(value){
            this[value.method](value.row);
        },
        handleSearch: function(searchModel){
            this.searchModel = {
                ...searchModel,
                //institutionType : (this.isChurch) ? "CHURCH" : "GENERAL"
            }
			this.loadMembers();
        },
        loadNextPage: function(){
            this.searchModel = {
                ...this.searchModel,
                page : this.searchModel.page+1
            }
            this.loadMembers();
        },
        loadPreviousPage : function(){
            this.searchModel = {
                ...this.searchModel,
                page : this.searchModel.page-1
            }
            this.loadMembers();
        },
        loadMembers: function(){
			if(this.$root['isInstitutionSet']()){
				this.searchModel = this.$root['addInstitutionId']({
					...this.searchModel,
					groupId :this.group.id
				});
				this.$api.post("/group/list-members", this.searchModel).then(response=>{
					this.tableData = response;
					this.$root['showAlert']("success", "Group loading", "Loaded");
				}).catch(error=>{
					this.$root['handleApiError'](error, "Group Loading");
				});
			}
        },
    },
    beforeMount(){
        this.loadMembers();
		this.loadCategories();
    },
    props:{
        listPersonDisabled : {
            type:Boolean,
            default(){
                return false;
            }
        },
        group:{
            type:Object,
            default(){
                return null
            }
        }
    }
}
</script>



<style scoped lang="scss">

</style>