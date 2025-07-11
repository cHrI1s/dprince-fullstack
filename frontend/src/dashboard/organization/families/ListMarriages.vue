<template>
	<div class="d-block position-relative">
		<DashboardCard header-title="List of Marriages"
		               :refresh-button="true"
		               @refresh="initialize(false)">
			<PaginatedTable :table-data="tableData"
			                :has-query-input="false"
			                :search-model="searchModel"
			                :table-headers="tableHeaders"
			                :extra-inputs="extraInputs"
			                :date-search="false"
			                :row-options="rowOptions"
			                @next="navigateTo(true)"
			                @previous="navigateTo(false)"
			                @search="makeSearch"
			                @options="actionPerformed"/>
			
			
			<div :id="pageId" style="display: none;">
				<MarriageCertificate v-if="familyDetails!==null"
				                     :isChurch="isInstitutionChurch"
				                     :familyDetails="familyDetails"/>
			</div>
			<ShowFamilyMembers :is-shown="showFamilyMembersDialog"
			                   :family="family"
			                   :isChurch="isInstitutionChurch"
			                   @close="closeFamilyMembersDialog"/>
		
		</DashboardCard>
	</div>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import MarriageCertificate from "@/dashboard/organization/families/parts/MarriageCertificate.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import ShowFamilyMembers from "@/dashboard/organization/families/ShowFamilyMembers.vue";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {FAMILY_SEARCH_MODEL} from "@/dashboard/organization/families/families";
import {generateRandomString} from "@/utils/AppStringUtils";

export default {
	name: "BirthdaysList",
	components: {ShowFamilyMembers, PaginatedTable, DashboardCard, MarriageCertificate},
	data(){
		return {
			showFamilyMembersDialog: false,
			family : {},
			image : null,
			signature: null,
			familyDetails : null,
			pageId  : generateRandomString(8),
			tableData: {...PAGE_MODEL},
			searchModel: {
				multiple: false,
				start: null,
				end: null,
				size: 5,
				page: 1
			},
			selectOptions: [
				{ label: "Single Date", value: false},
				{ label: "Date Gap", value: true},
			],
		}
	},
	computed: {
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		tableHeaders: function(){
			return {
				"Name":{
					rows:[
						{ type: "String", attribute: "name"},
						{ type: "String", attribute: "hofCode", label: "HOF Code"},
						{ type: "String", attribute: "familyCode", label: "Family Code"},
						{ type: "PlainNumber", attribute: "phone", label: "Phone: "}
					]
				},
				"Info":{
					rows:[
						{ label:"State", type: "String", attribute: "state" },
						{ label:"District", type: "String", attribute: "district" },
					]
				},
				"Date of Marriage":{
					rows:[
						{ type: "Date", attribute: "dob"}
					]
				}
			};
		},
		extraInputs: function(){
			return [
				{ type: "SELECT", model: "multiple", placeholder: "Select", options: this.selectOptions, styleClass:'col-md-4 mb-4'},
				{ type: "DATE", model: "start", placeholder: "Date Start", format: "dd-MM", styleClass:'col-md-8 mb-4'},
				{ type: "DATE", model: "end", placeholder: "Date Start", format: "dd-MM", styleClass:'col-md-8 mb-4', shownIf: {model: "multiple", values: [true]}},
			].map(input=>{
				if(this.searchModel.multiple) input.styleClass = "col-md-4 mb-4";
				else input.styleClass = "col-md-6 mb-4";
				return input;
			});
		},
		rowOptions: function(){
			return [
				{ label: "Marriage Certificate", method: "handleMarriageCertificate", styleClass: "col-md-6 mb-4" },
				{ label: "Show", method: "showFamilyMembers", styleClass: "col-md-6 mb-4" },
			].filter(option=>{
				let planableOptions = ["handleMarriageCertificate"];
				// raba ko yemerwe kuyiraba
				if(this.institution===null) return false;
				let allowedFeatures = this.institution.allowedFeatures;
				if(planableOptions.includes(option.method)){
					return allowedFeatures.includes("CERTIFICATE");
				}
				return true;
			});
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		}
	},
	methods: {
		makeSearch: function(){
			let data = this.$root['addInstitutionId']({...this.searchModel});
			if(!data.multiple) delete data.end;
			data = {
				...data,
				birthdayType: 'MARRIAGE'
			};
			this.$api.post("/institution/list-marriage-birthdays", data).then(response=>{
				this.tableData = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, "Marriage Anniversaries listing.");
			});
		},
		navigateTo: function(next=true){
			this.searchModel = {
				...this.searchModel,
				page: next ? this.searchModel.page+1 : this.searchModel.page-1
			}
			this.loadFamilies();
		},
		initialize: function(andSearch=false){
			this.searchModel = {...FAMILY_SEARCH_MODEL};
			if(andSearch) this.makeSearch();
		},
		actionPerformed: function(value){
			this[value.method](value.row);
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
				docprint.document.write('<head><title>Membership Certificate</title>');
				docprint.document.write('<link rel="stylesheet" href="/style/style.css"/>');
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;">');
				docprint.document.write('<style type="text/css" media="all">');
				let css = "" +
					"@media all{" +
					"@page{" +
					//"size: A4;" +
					//"orientation: landscape;" +
					"margin:0 0 !important;" +
					"padding: 0 0 !important;" +
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
		handleMarriageCertificate: function(family){
			let data = {
				ownerId: family.id,
				certificateType: "MARRIAGE"
			};
			const TITLE = "Marriage Certificate.";
			this.$api.post("/certificates/generate", data).then(response=>{
				if(response.successful) {
					family = {
						...family,
						certificateNo: response.object.number
					};
					this.generateMarriageCertificate(family);
				} else {
					this.$root['showAlert']('error', TITLE, response.message);
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
		generateMarriageCertificate: function(row){
			this.$store.commit("setLoading", true);
			if(row.dob!==null){
				if(row.members.length<2){
					this.$root['showAlert']("warn", "Marriage",
						"No husband/father and mother/wife found in this family.");
					this.$store.commit("setLoading", false);
					return;
				}
				this.familyDetails = row;
				setTimeout(()=>{
					this.$store.commit("setLoading", false);
					this.print();
				},2000);
			} else{
				this.$root['showAlert']("warn", "Marriage Certificate.", "The Date of Marriage is undefined!");
				this.$store.commit("setLoading", false);
			}
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
	}
}
</script>

<style scoped lang="scss">

</style>