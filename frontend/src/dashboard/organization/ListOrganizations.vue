<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
	                    :showInstitutionName="true">
		<DashboardCard :header-title="(isInstitutionChurch) ? 'List of Churches' : 'List of General Organizations'"
		               :refresh-button="true"
		               @refresh="initialize">
	        <PaginatedTable :table-data="tableData"
	                        :search-model="searchModel"
	                        :table-headers="tableHeaders"
	                        :row-options="organizationOptions"
	                        @options="actionPerformed"
	                        childrenAttribute="branches"
	                        parentAttributeLinker="parentInstitutionId"
	                        :extra-inputs="extraInputs"
	                        @search="handleSearch"
	                        @sort="sortContent"
	                        @next="loadNextPage"
	                        @previous="loadPreviousPage"
	                        :paginator-title="(isInstitutionChurch) ? 'List of Churches' : 'List of General Organizations'"/>
			
			<SubscriptionPlanUpgrader :institution="tmpInstitution"
			                          :visible="upgraderShown && tmpInstitution!==null"
			                          @close="closeUpgrader"/>
			
			<InstitutionDetails :institution="tmpInstitution"
			                    :visible="showInstitutionDetails"
			                    @close="closeDetailor"/>
			
			<InstitutionRenewal @close="closeRenewalDialog"
			                    :institution="tmpInstitution"
			                    :is-shown="showRenewDialog"/>
			<TopUp @close="closeTopUp"
			       :institution="tmpInstitution"
			       :visible="showTopDialog"/>
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import SubscriptionPlanUpgrader from "@/dashboard/organization/dialogs/subscription/SubscriptionPlanUpgrader.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import TopUp from "@/dashboard/organization/dialogs/TopUp.vue";
import {
	BLOCKED_STATUSES,
	DEADLINE_TYPES,
	ORGANIZATION_SEARCH_MODEL,
} from "@/dashboard/organization/Organization";
import {CHURCH_MAIN_BRANCH_ADMINISTRATORS, SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import InstitutionDetails from "@/dashboard/organization/dialogs/details/InstitutionDetails.vue";
import InstitutionRenewal from "@/dashboard/organization/dialogs/InstitutionRenewal.vue";

export default {
	name: "ListOrganizations",
	components: {
		InstitutionRenewal,
		DashboardContainer,
		InstitutionDetails,
        PaginatedTable,
		DashboardCard,
		SubscriptionPlanUpgrader,
		TopUp
	},
	computed: {
		extraInputs: function(){
			return [
				{ type: "SELECT", model: "deadlineType", options:[...DEADLINE_TYPES],
					placeholder: "Deadline type", styleClass: "col-md-6 mb-4"},
                { type: "SELECT", model: "blockSelector", options:[...BLOCKED_STATUSES],
                    placeholder: "Block Status", styleClass: "col-md-6 mb-4"},
				
				{ type: "PHONE_NUMBER", model: ["phoneCode", "phone"],
					placeholder: "Phone",
					styleClass: "col-12 mb-4"},
				
			].filter(input=>{
				if(this.isInstitutionChurch && this.isUserChurchAdmin) {
					return !["deadlineType", "blockSelector"].includes(input.model);
				}
				return true;
			});
		},
		tableHeaders: function(){
			if(this.isInstitutionChurch) {
				return {
					"Name": {
						rows: [
							{ type: "String", attribute: "name" },
							{ type: "String", attribute: "email", label: "Email", lowercase: true },
							{ type: "String", attribute: "parentInstitutionName", label: "Main Branch", showIfNotNull: true, lowercase: true},
							{ type: "String", attribute: "baseCode", label: "Base Code" },
							{ type: "PlainNumber", attribute: "phone", label: "Phone Number" },
						]
					},
					"Type": {
						rows: [
							{ type: "NotNull", attribute: "parentInstitutionId", array: ["Main Church", "Church - Branch"] }
						]
					},
					"Status": {
						rows: [
							{ type: "Boolean", attribute: "blocked", array: ["Not Blocked", "Blocked"] }
						]
					},
					"People": {
						rows: [
							{ type: "PlainNumber", attribute: "members", label: "Members" },
							{ type: "PlainNumber", attribute: "admins", label: "Admins" }
						]
					},
					"Stats": {
						rows: [
							{ type: "PlainNumber", attribute: "emails", label: "Emails" },
							{ type: "PlainNumber", attribute: "smses", label: "SMS" }
						]
					},
					"Date": {
						rows: [
							{ type: "Date", attribute: "deadline", label: "Deadline" },
							{ type: "Date", attribute: "creationDate", label: "Creation" }
						]
					}
				}
			} else {
				return {
					"Name": {
						rows: [
							{ type: "String", attribute: "name"},
							{ type: "String", attribute: "email", label: "Email", showIfNotNull: true, lowercase: true },
							{ type: "String", attribute: "baseCode", label: "Base Code", showIfNotNull: true },
							{ type: "PlainNumber", attribute: "phone", label: "Phone Number", showIfNotNull: true },
						]
					},
					"Status": {
						rows: [
							{ type: "Boolean", attribute: "blocked", array: ["Not Blocked", "Blocked"]}
						]
					},
					"People": {
						rows: [
							{ type: "Number", attribute: "members", label: "Members"},
							{ type: "Number", attribute: "admins", label: "Admins"}
						]
					},
					"Stats": {
						rows: [
							{ type: "Number", attribute: "emails", label: "Emails"},
							{ type: "Number", attribute: "smses", label: "SMS"}
						]
					},
					"Date": {
						rows: [
							{ type: "Date", attribute: "deadline", label: "Deadline"},
							{ type: "Date", attribute: "creationDate", label: "Creation"}
						]
					}
				}
			}
		},
		organizationOptions : function(){
			return [
				{ label: "List Admins", method: "listAdministrators", styleClass: "col-md-4 mb-4" },
				{ label: "Create Admin", method: "addAdministrator", styleClass: "col-md-4 mb-4" },
				{ label: "List Families", method: "listFamilies", styleClass: "col-md-4 mb-4" },
				{ label: "Create Family", method: "createFamily", styleClass: "col-md-4 mb-4" },
				{ label: "List "+(this.isInstitutionChurch ? "Members" : "Partners"), method: "listMembers", styleClass: "col-md-4 mb-4" },
				{ label: "Create "+(this.isInstitutionChurch ? "Member" : "Partner"), method: "addMembers", styleClass: "col-md-4 mb-4" },
				{ label: "Import partners", method: "importPartners", styleClass: "col-md-4 mb-4" },
				{ label: "Address Printing", method: "engageAddressPrinting", styleClass: "col-md-4 mb-4" },
				{ label: "Report Generation", method: "engageReportGeneration", styleClass: "col-md-4 mb-4" },
				{ label: "Update", method: "updateOrganization", styleClass: "col-md-4 mb-4" },
				{ label: "Delete", method: "deleteOrganization", styleClass: "col-md-4 mb-4" },
				{ label: "Create Event", method: "createEventOrganization", styleClass: "col-md-4 mb-4" },
				{ label: "Set Institution", method: null, styleClass: "col-md-4 mb-4" },
				{ label: "Create Group", method: "createGroup", styleClass: "col-md-4 mb-4" },
				{ label: "Compose", method: "compose", styleClass: "col-md-4 mb-4" },
				{ label: "Upgrade Plan", method: "upgradePlan", styleClass: "col-md-4 mb-4" },
				{ label: "Renew Subscription", method: "renewSubscription", styleClass: "col-md-4 mb-4", hiddenIf:{attribute: 'subscription', equals: 'LIFETIME'} },
				{ label: "Toggle Block", method: "toggleBlockInstitution", styleClass: "col-md-4 mb-4" },
				{ label: "Show Details", method: "showDetails", styleClass: "col-md-4 mb-4" },
				{ label: "Top up", method: "topUp", styleClass: "col-md-4 mb-4" },
			].filter(option=>{
				let unallowedToOrganizations = ["compose", "createGroup", "createEventOrganization"],
					unallowedToChurch = ["importPartners"];
				
				if(this.isInstitutionChurch) {
					if(this.isUserChurchAdmin) {
						unallowedToChurch = [
							...unallowedToChurch,
							"renewSubscription", "upgradePlan", "topUp"
						];
					}
					return !unallowedToChurch.includes(option.method);
				}
				return !unallowedToOrganizations.includes(option.method);
			})
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		isUserChurchAdmin: function(){
			const loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return false;
			const theMainBranchAdmins = [
				...CHURCH_MAIN_BRANCH_ADMINISTRATORS,
				"CHURCH_DATA_ENTRY_OPERATOR"
			];
			return theMainBranchAdmins.includes(loggedInUser.userType);
		}
	},
	data() {
		return {
			showTopDialog:false,
			showRenewDialog:false,
            memberCertificatePage : false,
			tmpInstitution: null,
			upgraderShown : false,
			headers             : [
				"Name",
				"Category",
				"Subscription"
			],
            showSubscriptionPlanDialog: false,
            institutionToSuspend: null,
			organizationMultiOptions : [
				{ label: "Delete", method: "deleteMultiOrganization", styleClass: "col-md-6 mb-4" },
                { label: "Toggle Block", method: "toggleMultiBlockInstitution", styleClass: "col-md-6 mb-4" },
			],
			selectedIndex       : null,
			optionsShown        : true,
			searchModel: {...ORGANIZATION_SEARCH_MODEL},
			tableData: {...PAGE_MODEL},
			
			showInstitutionDetails: false,
		}
	},
	emits:['subscription'],
	methods: {
		closeTopUp: function(){
			this.showTopDialog = false;
			this.tmpInstitution = null;
		},
		renewSubscription: function(row){
			this.tmpInstitution = row;
			this.showRenewDialog = true;
		},
		closeRenewalDialog: function(institution=null){
			if(institution!==null){
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.map(item=>{
						if(item.id===institution.id) item = {...institution};
						return item;
					})
				}
			}
			this.showRenewDialog = false;
		},
		topUp: function(institution){
			this.tmpInstitution = {...institution};
			this.showTopDialog = true;
		},
		importPartners : function(){
			this.$router.push("/organizations/import-partners");
		},
		showDetails: function(institution){
			this.tmpInstitution = {...institution};
			this.showInstitutionDetails = true;
		},
		closeDetailor: function(){
			this.showInstitutionDetails = false;
			this.tmpInstitution = null;
		},
		listFamilies: function(ignored){
			let url = (this.isInstitutionChurch)
				? "/church/families-board/0"
				: "/organizations/families-board/0";
			this.$router.push(url);
		},
		deleteMultiOrganization: function(data){
			this.$api.post("/institution/multi-delete",
				{ itemsIds: [...data.itemsIds] }).then(response=>{
				this.$store.commit("setLoading", true);
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements - data.itemsIds.size,
					content: this.tableData.content.filter(item=>{ return !data.itemsIds.has(item.id); })
				};
				this.$store.commit("setLoading", false);
				this.$root['showAlert']("success", "Organizations Delete", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Organizations Deletion");
			});
		},
		toggleMultiBlockInstitution: function(data){
			this.$api.post("/institution/multiple-toggle-block", data).then(response=>{
				this.$root['showAlert']("success", "Organizations Delete", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Organizations Block");
			});
		},
        toggleBlockInstitution: function(row){
            let data = { institutionId: row.id };
            this.$api.post("/institution/toggle-block", data).then(response=>{
                this.$root['showAlert']('success', "Institution Status", response.message);
                this.tableData.content = this.tableData.content.map(institution=>{
                    if(institution.id===row.id) institution.blocked = !institution.blocked;
                    return institution;
                })
            }).catch(error=>{
                this.$root['handleApiError'](error, "Institution Status");
            })
        },
		createFamily: function(institution){
			let url = (this.isInstitutionChurch)
				? "/church/members-dashboard/0"
				: "/organizations/partners-dashboard/0";
			this.$router.push(url);
		},
		engageAddressPrinting: function(row){
			let url = (this.isInstitutionChurch)
				? "/church/address-printing"
				: "/organizations/address-printing";
			this.$router.push(url);
		},
		engageReportGeneration: function(row){
			let url = (this.isInstitutionChurch)
				? "/church/report-generation"
				: "/organizations/report-generation";
			this.$router.push(url);
		},
		listMembers: function(row){
            this.$store.commit("setInstitution", row);
			let url = this.isInstitutionChurch
				? "/church/members-dashboard"
				: "/organizations/partners-dashboard";
			this.$router.push(url+"/0");
		},
		addMembers: function(row){
			let url = this.isInstitutionChurch
				? "/church/members-dashboard"
				: "/organizations/partners-dashboard";
			this.$router.push(url+"/1");
		},
        loadInstitutions : function(){
			let data = {
				...this.searchModel,
				institutionType : (this.isInstitutionChurch) ? "CHURCH" : "GENERAL"
			};
	        delete data.phoneCode;
			let institutionsName = (this.isInstitutionChurch) ? "Churches" : "Organizations";
            this.$api.post("/institution/get", data).then(response=>{
                this.tableData = response;
				this.$root['showAlert']("success", institutionsName+" loading", "Loaded");
            }).catch(error=>{
                this.$root['handleApiError'](error, institutionsName+" Loading");
            });
        },
        
		handleSearch: function(searchModel){
			searchModel = {
				...searchModel,
				institutionType : (this.isInstitutionChurch) ? "CHURCH" : "GENERAL",
				phone: (typeof searchModel.phone!=='undefined' && searchModel.phone!==null)
					? parseInt(searchModel.phoneCode+""+searchModel.phone)
					: null
			};
			this.searchModel = {...searchModel};
			this.loadInstitutions();
		},
		sortContent: function(tableData){
			this.tableData = {...tableData}
		},
		loadNextPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page+1
			}
			this.loadInstitutions();
		},
		loadPreviousPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page-1
			}
			this.loadInstitutions();
		},
		multiActionPerformed: function(value){
			// TO recheck after this is over.
			if(value.method!==null) this[value.method]({itemsIds: value.items});
		},
        actionPerformed: function(value){
            this.saveInstitution(value.row);
            if(value.method!==null) this[value.method](value.row);
            else this.$root['showAlert']("success", "Institution", "Switched!");
        },
		saveInstitution: function(institution){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser!==null){
				let usersWhoCanEdits = [
					...SUPER_ADMINISTRATORS_ROLES,
					"CHURCH_ADMINISTRATOR",
					"CHURCH_BRANCH_ADMINISTRATOR"
				];
				if(usersWhoCanEdits.includes(loggedInUser.userType)){
					this.$store.commit("setInstitution", institution);
					this.$root['loadSignature']();
				}
			}
		},
		listAdministrators(ignored){
			let adminAddPage = (this.isInstitutionChurch)
				? "/church/assistant-admin-dashboard/0"
				: "/organizations/assistant-admin-dashboard/0";
			this.$router.push(adminAddPage);
		},
		addAdministrator : function(ignored){
			let adminAddPage = (this.isInstitutionChurch)
				? "/church/assistant-admin-dashboard/1"
				: "/organizations/assistant-admin-dashboard/1";
			this.$router.push(adminAddPage);
		},
		deleteOrganization : function(row){
			this.$api.delete(`/institution/delete/${row.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(item=>{
						return item.id!==row.id;
					})
				}
				this.$root['showAlert']("success",
					(this.isInstitutionChurch) ? "Church" : "Organization",
					response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Delete "+(this.isInstitutionChurch) ? "Church" : "Organization");
			});
		},
		updateOrganization: function(ignored){
			let url = (this.isInstitutionChurch)
				? "/church/create"
				: "/organizations/create-new";
			this.$router.push(url);
		},
        createEventOrganization: function(ignored){
            this.$router.push("/church/event-board/0")
        },
        createGroup: function(ignored){
            this.$router.push("/church/communication");
        },
        compose: function(ignored){
            this.$router.push("/church/communication");
        },
		upgradePlan: function(row){
			this.tmpInstitution = {...row};
			this.upgraderShown = true;
		},
		closeUpgrader: function(institution=null){
			this.upgraderShown = false;
			this.tmpInstitution = null;
			if(institution!==null){
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.map(listedInstitution=>{
						if(institution.id===listedInstitution.id){
							listedInstitution = {...institution};
						}
						return listedInstitution;
					})
				}
			}
		},
		initialize: function(){
			this.$store.commit("setInstitutionMembers", []);
			this.loadInstitutions();
		}
	},
    beforeMount() {
		this.initialize();
    },
	watch: {
		isChurch: function(newValue){
			this.tableData = {...PAGE_MODEL};
			this.searchModel = {...ORGANIZATION_SEARCH_MODEL};
			this.loadInstitutions();
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		
	},
}
</script>

<style scoped lang="scss">

</style>