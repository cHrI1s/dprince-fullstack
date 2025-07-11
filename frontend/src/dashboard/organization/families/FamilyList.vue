<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Families"
	               :refresh-button="true"
	               @refresh="initialize">
		<div class="d-none" :id="pageId">
			<FamilyCard :family="selectedFamily"/>
		</div>
		
		<PaginatedTable :table-data="tableData"
		                :search-model="searchModel"
		                :table-headers="tableHeader"
		                :extra-inputs="extraInputs"
		                :row-options="rowOptions"
		                :paginator-title="null"
		                @sort="(newContent)=>tableData={...newContent}"
		                @next="navigateTo(true)"
		                @previous="navigateTo(false)"
		                @search="makeSearch"
		                @options="actionPerformed"/>
		
		<ShowFamilyMembers :is-shown="showFamilyMembersDialog"
		                   :family="family"
		                   :isChurch="isInstitutionChurch"
							@close="closeFamilyMembersDialog"/>
		
	</DashboardCard>
</template>

<script>
import ShowFamilyMembers from "@/dashboard/organization/families/ShowFamilyMembers.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import FamilyCard from "@/dashboard/organization/families/FamilyCard.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {FAMILY_CREATION_MODEL, SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {FAMILY_SEARCH_MODEL} from "@/dashboard/organization/families/families";

export default {
	name: "FamilyList",
	components: {DashboardCard, PaginatedTable, ShowFamilyMembers, FamilyCard},
	emits: ["update"],
	computed: {
		userType: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			return loggedInUser.userType;
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		rowOptions: function(){
			return [
				{ label: "Edit", method: "editFamily", styleClass: "col-md-6 mb-4"},
				{ label: "Delete", method: "deleteFamily", styleClass: "col-md-6 mb-4"},
				{ label: "Show", method: "showFamilyMembers", styleClass: "col-md-6 mb-4" },
				{ label: "Print", method: "printFamily", styleClass: "col-md-6 mb-4" },
			];
		}
	},
	data(){
		return {
			showFamilyMembersDialog: false,
			family : {},
            signature: null,
            pageId  : generateRandomString(8),
			searchModel: {...FAMILY_SEARCH_MODEL},
			tableData: {...PAGE_MODEL},
			tableHeader: {
				"Name": {
					rows: [
						{ type: "String", attribute: "name" }
					]
				},
				"Date of Marriage": {
					rows: [
						{ type: "Date", attribute: "dob" }
					]
				},
				"No. Of Family Members": {
					rows: [
						{ type: "Counter", attribute: "members", label: "Members" }
					]
				}
			},
			extraInputs: [
				{ placeholder: "Member codes", type: "TEXTAREA", model: "memberCodesString", styleClass: "col-md-12 mb-4"},
			],
			selectedFamily: null,
		}
	},
	methods: {
		printFamily: function(theFamily){
			this.$store.commit("setLoading", true);
			this.selectedFamily = {...theFamily};
			setTimeout(()=>{
					this.print();
					this.$store.commit("setLoading", false);
				}, 2000);
		},
        print: function(){
	        this.$store.commit("setLoading", true);
			let disp_setting="toolbar=yes,location=no,";
			disp_setting+="directories=yes,menubar=yes,";
			disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
			let htmlElement = document.getElementById(this.pageId);
	        htmlElement.style.display = "block";
			let content_value = htmlElement.outerHTML;
	        htmlElement.style.display = "none";
			this.$store.commit("setLoading", false);
			let docprint= window.open("","",disp_setting);
			if(docprint!==null) {
				docprint.document.open();
				docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
				docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
				docprint.document.write('<html lang="en-US" xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
				docprint.document.write('<head><title>Family Card</title>');
				docprint.document.write('<link rel="stylesheet" href="/style/style.css"/>');
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;">');
				docprint.document.write('<style type="text/css" media="all">');
				let css = "" +
					"@media all{" +
						"@page{" +
							//"size: A4;" +
							//"orientation: landscape;" +
						"}" +
						"*{box-sizing:border-box;}" +
						// Body
						"body{" +
							"margin: 0;" +
							"padding: 0;" +
						"}" +
					"}";
				docprint.document.write(css);
				docprint.document.write('}</style>');
				docprint.document.write(content_value);
				docprint.document.write('</body></html>');
				docprint.document.close();
				docprint.focus();
			}
        },
		showFamilyMembers : function(row){
			this.showFamilyMembersDialog = true;
			this.family = row;
		},
		closeFamilyMembersDialog: function(){
			this.showFamilyMembersDialog = false;
		},
		deleteFamily: function(row){
			this.$api.delete(`/institution/delete-family/${row.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(singleFamily=>{
						return singleFamily.id!==row.id
					})
				}
				this.$root['showAlert']("success", "Family deletion.", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Family deletion.");
			});
		},
		editFamily: function(row){
			row = {
				...row,
				dob: (typeof row.dob!=='undefined' && row.dob!==null) ? new Date(row.dob) : null
			};
			this.$emit("update", row);
		},
		loadFamilies: function(){
			if(this.institution===null && SUPER_ADMINISTRATORS_ROLES.includes(this.userType)){
				this.$root['showAlert']('warn', 'Warning', "No institution Specified");
				return;
			}
			const TITLE = "Families Loading.";
			let data = this.$root['addInstitutionId'](this.searchModel);
			try{
                if(!isEmpty(data.memberCodesString)){
                    data = {
                        ...data,
                        memberCodes: data.memberCodesString.split(",")
                            .filter(code=>{ return !isEmpty(code); })
                            .map(code=>{
                                return code.trim();
                            })
                    }
                    delete data.memberCodesString;
                }
            }catch (ignored){
				// do nothing
			}
			this.$api.post("/institution/list-families", data).then(response=>{
				this.tableData = response;
				this.searchModel = {
					...this.searchModel,
					page: response.currentPage+1
				}
				this.$root['showAlert']("success", TITLE, "Families loaded.");
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
		actionPerformed: function(value){
			this[value.method](value.row);
		},
		makeSearch: function(searchModel){
			this.searchModel = {
				...this.searchModel,
				...searchModel
			};
			this.loadFamilies();
		},
		navigateTo: function(next=true){
			this.searchModel = {
				...this.searchModel,
				page: next ? this.searchModel.page+1 : this.searchModel.page-1
			}
			this.loadFamilies();
		},
		initialize: function(){
			this.searchModel = {...FAMILY_SEARCH_MODEL};
			this.loadFamilies();
		}
	},
	beforeMount(){
		this.initialize();
	},
	watch: {
		isChurch: function(newValue){
			this.searchModel = {...FAMILY_SEARCH_MODEL};
			this.tableData = {...PAGE_MODEL};
			this.tmpFamily = {...FAMILY_CREATION_MODEL};
			this.loadFamilies();
		},
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
        memberFamily : {
            type:Object,
            default(){
                return null;
            }
        }
	}
}
</script>

<style scoped lang="scss">

</style>