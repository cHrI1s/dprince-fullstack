<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<DateChooser :show="dateChooserVisible"
		             @close="closeDateChooser"/>
		
		<div class="position-relative"
		     style="display: none;"
		     :id="baptismPageId">
			<PrintBaptismCertificate v-if="tmpMember!==null && typeof tmpMember.id!=='undefined'"
			                         :person="tmpMember"/>
		</div>
		
		<div class="position-relative"
		     style="display: none;"
		     :id="childDedicationPageId">
			<ChildDedicationCertificate v-if="showChildCertificate"
			                            :institution="theInstitution"
			                            :person="childPerson"/>
		</div>
		
		
		<DashboardCard :header-title="dashboardCardTitle"
		               :refreshButton="true"
		               @refresh="initialize">
			<PaginatedTable :table-data="tableData"
			                searchQueryPlaceholder="Search By Name..."
			                :extraButtons="extraButtons"
			                :table-headers="tableHeaders"
			                :row-options="organizationOptions"
			                :extra-inputs="extraInputs"
			                :date-search="false"
			                :search-model="searchModel"
			                optionHeaderModel="fullName"
			                @options="actionPerformed"
			                @search="handleSearch"
			                @next="loadNextPage"
			                @sort="(newContent)=>tableData={...newContent}"
			                @previous="loadPreviousPage"
                            :show-non-institutional-button-on-family-creation="true"
			                @continueFamilyCreation="handleFamilyCreation"
                            @nonInstitutionFamilyMember="()=>$emit('insertNonInstitutionalFamilyMember')"
			                :canCreateFamily="canCreateFamily"
			                :paginator-title="isChurch? 'List of Members' : 'List of Partners'"/>
		</DashboardCard>
		
		<MemberDetails :visible="showingDetails"
		               :isChurch="false"
		               :member="tmpMember"
		               @close="hideDetails"/>
		
		<Dialog :visible="familyRoleGiverShown"
		        class="w-md-100 w-lg-50"
		        :draggable="false"
		        :modal="true"
		        :close-on-escape="false"
		        header="Family Role"
		        :closable="false">
			<div class="row mt-4">
				<div class="col-md-4 mb-4"
				     v-for="(role, index) in familyRoles"
				     :key="index">
					<div class="card bg-light"
					     @click="setFamilyRole(role.value)">
						<div class="card-body bg-transparent text-center fw-bolder cursor-pointer">
							{{ role.label }}
						</div>
					</div>
				</div>
			</div>
			<template #footer>
				<div class="w-100 d-block position-relative">
					<Button label="Close"
					        severity="danger"
					        @click="()=>familyRoleGiverShown=!familyRoleGiverShown"
					        class="w-100"
					        icon="pi pi-times"/>
				</div>
			</template>
		</Dialog>
		
		
		<MembershipExtender :visible="showExtender"
		                    :member="tmpMember"
		                    @close="closeMembershipExtender"/>
		
		
		<DonateDialog :member="tmpMember"
		              :visible="donatorShown"
		              :isChurch="isChurch"
		              @close="closeDonator"/>
		
		<div class="d-block">
			<div class="position-relative">
				<ContributionReceipt v-if="showReceipt"
				                     :isChurch="isInstitutionChurch"
				                     :receipt-data="receiptData"/>
			</div>
			<div class="position-relative"
			     style="display: none;"
			     :id="membershipPageId">
				<PrintMembershipCertificate :organization="tmpInstitution"
				                            v-if="tmpMember!==null && typeof tmpMember.id!=='undefined'"
                                            :priest-image="signature"
				                            :person="tmpMember"/>
			</div>
			<div class="position-relative"
			     style="display: none;"
			     :id="certificatePageId">
				<PrintBirthdayCertificate :organization="tmpInstitution"
				                          v-if="tmpMember!==null && typeof tmpMember.id!=='undefined'"
				                          :person="tmpMember"/>
			</div>
            <div class="position-relative">
                <BaptisimDialog :member-to-be-baptized="memberToBaptized"
                                   v-if="memberToBaptized!==null"
                                   @close-dialog="closeVisitMemberDialog"
                                   :is-visible="isVisitMemberDialogShown" />
            </div>
		</div>
		
		<GroupMemberAdder :shown="showGroupMemberIncluder"
		                  :member="tmpMember"
		                  @close="closeGroupMemberAdder"/>
	</div>
</template>

<script>
import BaptisimDialog from "@/dashboard/church/church-activities/dialogs/BaptismDialog.vue";
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {DEFAULT_PAGE} from "@/dashboard/utils/default_values";
import {FAMILY_ROLES, MEMBERSHIP_DURATIONS} from "@/dashboard/members/members";
import {
	AVAILABLE_LANGUAGES,
	INDIA_STATES,
	MEMBER_SEARCH_MODEL,
	PARTNER_MODEL,
	SUBSCRIPTION_PLANS
} from "@/dashboard/organization/Organization";
import DonateDialog from "@/dashboard/organization/dialogs/donation/DonateDialog.vue";
import PrintMembershipCertificate from "@/dashboard/organization/PrintMembershipCertificate.vue";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import PrintBaptismCertificate from "@/dashboard/church/church-activities/PrintBaptismCertificate.vue";
import PrintBirthdayCertificate from "@/dashboard/organization/families/PrintBirthdayCertificate.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import ContributionReceipt from "@/dashboard/church/contributions/receipts/ContributionReceipt.vue";
import GroupMemberAdder from "@/dashboard/group/GroupMemberAdder.vue";
import MemberDetails from "@/dashboard/organization/dialogs/details/MemberDetails.vue";
import {convertUiToDate, getYearDifference, renameKey} from "@/utils/AppFx";
import {getCountries} from "@/dashboard/utils/countries";
import MembershipExtender from "@/dashboard/organization/dialogs/members/MembershipExtender.vue";
import ChildDedicationCertificate from "@/dashboard/church/church-activities/ChildDedicationCertificate.vue";
import DateChooser from "@/dashboard/organization/dialogs/DateChooser.vue";

export default {
	name: "OrganizationMemberList",
	emits: ["update", "insertNonInstitutionalFamilyMember"],
	components: {
		DateChooser,
		ChildDedicationCertificate,
		MembershipExtender,
		MemberDetails,
		ContributionReceipt,
        PrintMembershipCertificate, DonateDialog,
		Button,
		PaginatedTable,
		DashboardCard,
		Dialog,
		PrintBaptismCertificate,
		PrintBirthdayCertificate,
		GroupMemberAdder,
		BaptisimDialog
	},
	computed: {
		theInstitution: function(){
			return this.institution;
		},
		canCreateFamily: function(){
			return this.institutionMembers.length>0;
		},
		institutionType: function(){
			return this.isChurch ? 'CHURCH' : 'GENERAL';
		},
		institutionMembers: function(){
			return (this.$store.getters.getInstitutionMembers!==null)
                ? this.$store.getters.getInstitutionMembers : [];
		},
		dashboardCardTitle: function(){
			return this.isChurch ? "Members" : "Partners";
		},
		organizationOptions: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return [];
			return [
				{ label : "Show Details", method: "showDetails", styleClass: "col-md-4 mb-4"},
				{ label : "Donate", method: "makeDonation", styleClass: "col-md-4 mb-4"},
				{ label : "List Donations", method: "listDonations", styleClass: "col-md-4 mb-4"},
				{ label : ["Remove From Family", "Add To Family"], toggle: "isFamilyMember", method: "engageAddToFamily", styleClass: "col-md-4 mb-4"},
				{ label : "Add to Group", method: "addToGroup", styleClass: "col-md-4 mb-4"},
				{ label : "Contribute", method: "contribute", styleClass: "col-md-4 mb-4"},
				{ label : "Edit", method: "editMember", styleClass: "col-md-4 mb-4"},
				{ label : "Delete", method: "deleteMember", styleClass: "col-md-4 mb-4"},
				{ label : "Set Pastor", method: "setPastor", styleClass: "col-md-4 mb-4"},
				{ label : "Christening", method: "baptizeMember",  styleClass: "col-md-4 mb-4"},
				{ label : "Baptism Certificate", method: "printBaptismCertificate", styleClass: "col-md-4 mb-4"},
				// { label : "Membership certificate", method: "handleMembershipCertificate", styleClass: "col-md-4 mb-4" },
				{ label : "Dedication certificate", method: "startDateChooser", styleClass: "col-md-4 mb-4" },
				// { label : "Birthday certificate", method: "printBirthdayCertificate", styleClass: "col-md-4 mb-4" },
				{ label : "Extend Membership", method: "extendMembership", styleClass: "col-md-4 mb-4" },
				{ label : ["Deactivate", "Activate"], toggle: "active", method: "toggleActive", styleClass: "col-md-4 mb-4" },
			].filter(option=>{
				let unAllowedToOrganization = ["sendBirthdayCertificate",
						"contribute",
						"printBirthdayCertificate",
						"setPastor", "baptizeMember",
						"handleMembershipCertificate",
						"handleMembershipCertificate"],
					unAllowedToChurch = ["makeDonation", 'extendMembership'];
			
				if(option.method==="engageAddToFamily"){
					let family = this.$store.getters.getFamily;
					if(family===null) return false;
				}
				
				let institution = this.institution,
					allowedFeatures = (typeof institution.allowedFeatures !== 'undefined' && institution.allowedFeatures !== null)
						? institution.allowedFeatures
						: [];
				let certificatesModules = [
					"printBaptismCertificate",
					"handleMembershipCertificate",
					"printBirthdayCertificate", "startDateChooser"
				];
				
				if(option.method==="addToGroup"){
					if(this.institution!==null){
						if(allowedFeatures!==null) return allowedFeatures.includes("MEMBER_GROUPS");
					}
					return false;
				}
				
				if(this.isInstitutionChurch) {
					if(loggedInUser.userType==='CHURCH_DATA_ENTRY_OPERATOR') {
						unAllowedToChurch = [
							...unAllowedToChurch,
							"handlePriestSignature",
							"setPastor"
						]
					}
					
					if (!allowedFeatures.includes("CERTIFICATE")) {
						unAllowedToChurch = [
							...unAllowedToChurch,
							...certificatesModules
						];
					}
					return !unAllowedToChurch.includes(option.method);
				} else {
					if (!unAllowedToOrganization.includes("CERTIFICATE")) {
						unAllowedToOrganization = [
							...unAllowedToOrganization,
							...certificatesModules
						];
					}
					return !unAllowedToOrganization.includes(option.method);
				}
			});
			
		},
		
		tableHeaders: function(){
			let isChurch = this.isChurch;
			let headers =  {
				"Full name":{
					rows:[
						{ type: "StringArray", attribute: ["firstName", "lastName"]},
						{ type: "Date", attribute: "dob", label: "DOB"},
						{ type: "String", attribute: "email", lowercase: true},
						{ type: "PlainNumber", attribute: "phone", label: "Phone: "}
					]
				},
				"Partner Code":{
					rows:[
						{ type: "String", attribute: "code"},
					]
				},
				"Info":{
					rows:[
						{ label:"State", type: "String", attribute: "state" },
						{ label:"District", type: "String", attribute: "district" },
						{ label:"Subscription", type: "Select", attribute: "subscription", options: [...SUBSCRIPTION_PLANS]},
						{ label:"Language", type: "Select", attribute: "language", options: [...AVAILABLE_LANGUAGES]},
					].filter(row=>{
						if(isChurch) return row.attribute!=="subscription";
						return true
					})
				},
				"Created on":{
					rows:[
						{ type: "Date", attribute: "creationDate"}
					]
				}
			};
			if(isChurch) headers = this.renameKey(headers, "Partner Code", "Member Code");
			return headers;
		},
		extraInputs: function(){
			return [
				{ type: "MULTISELECT", styleClass:"col-md-6 mb-4", model:"countries", placeholder: "Country", options: [...getCountries()] },
				{ type: "MULTISELECT", styleClass:"col-md-6 mb-4", model:"states", options: [...INDIA_STATES], placeholder: "States", shownIf: { model: "countries", values: ["IN"], exactMatch: true, onShowSet:{model: "state", value: null} } },
				{ type: "LINE", styleClass: "col-12" },
				{ type: "TEXT", styleClass:"col-md-6 mb-4", model:"state", placeholder: "State", hiddenIf: { model: "countries", values: ["IN"], onShowSet:{model: "states", value: null}, exactMatch: true } },
				{ type: "TEXT", styleClass:"col-md-6 mb-4", model:"district", placeholder: "District" },
				{ type: "NUMBER", styleClass:"col-lg-12 col-md-6 mb-4", model:"pincode", placeholder: "PinCode" },
				{ type: "SELECT", styleClass:"col-md-4 mb-4", model:"creationDateIn", placeholder: "Creation Date In", options: [...MEMBERSHIP_DURATIONS] },
				{ type: "DATE", format: "dd/mm/yy", styleClass:"col-md-4 mb-4", model:"from", placeholder: "Date From", shownIf: {model: "creationDateIn", values:["SINGLE_DATE", "CUSTOM_DATE_GAP"]} },
				{ type: "DATE", format: "dd/mm/yy", styleClass:"col-md-4 mb-4", model:"to", placeholder: "Date To", shownIf: {model: "creationDateIn", values:["CUSTOM_DATE_GAP"]} },
				{ type: "LINE", styleClass: "col-12" },
				{type: "PHONE_NUMBER", model: ["phoneCode", "phone"], styleClass: "col-md-6 mb-4", placeholder: "Phone Number"},
				{
					type: "PHONE_NUMBER",
					model: ["alternatePhoneCode", "alternatePhone"],
					styleClass: "col-md-6 mb-4",
					placeholder: "Alternate Phone Number"
				},
				{ type: "TEXTAREA", styleClass:"col-md-12 mb-4", model:"partnerCodes", placeholder: this.isInstitutionChurch ? "Members Codes" : "Partners Codes" },
				{ type: "MULTISELECT", styleClass:"col-md-12 mb-4", model:"categories", options:[...this.categories], placeholder: "Categories" },
				{ type: "SELECT", styleClass:"col-md-12 mb-4", model:"active", options:[...this.memberActiveOptions], placeholder: "Status" },
			].map(option=>{
				if(!this.isInstitutionChurch) {
					if(["categories", "active"].includes(option.model)) option.styleClass = "col-md-6 mb-4";
				}
				return option;
			}).filter(option=>{
				if(this.isChurch){
					return option.model!=="categories";
				}
				return true;
			})
		},
		extraButtons: function(){
			return [
				{ icon: "pi pi-eye fw-bolder", command: "showDetails" },
				{ icon: "pi pi-list fw-bolder", command: "listDonations" },
				{ icon: "pi pi-wallet fw-bolder", command: (this.institutionType==='CHURCH') ? "contribute" : "makeDonation" },
			];
		},
		tableData: {
			get: function(){
				let family = this.$store.getters.getFamily,
					output = {...this.tableDataLocal};
				if(family!==null){
					let members = (typeof family.members!=='undefined') ? family.members : [];
					let familyMembersIds = [];
					if(members.length>0) familyMembersIds = members.map(taggedMember=>taggedMember.id);
					output = {
						...output,
						content: [...output.content.map(member=>{
							let isFamilyMember = members
								.findIndex(foundMember => member.id === foundMember.id) >= 0;
							
							if(familyMembersIds.includes(member.id)) {
								member = {
									...member,
									tag: 'ADDED_TO_FAMILY'
								}
							}
							return {
								...member,
								isFamilyMember: isFamilyMember
							}
						})]
					};
				} else {
					output = {
						...output,
						content: [...output.content.map(member=>{
							if(typeof member.tag!=='undefined') delete member.tag;
							return {
								...member,
								isFamilyMember: false
							}
						})]
					};
				}
				return output;
			}, set: function(newValue){
				this.tableDataLocal = newValue;
			}
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		searchModel: {
			get: function(){
				return this.searchModelLocal;
			}, set: function(newValue) {
				this.searchModelLocal = {...newValue};
			}
		},
	},
	data(){
		return {
			subscription : [...SUBSCRIPTION_PLANS],
			searchModelLocal: {...MEMBER_SEARCH_MODEL},
			canUploadPriestImage : null,
			baptizedMember:[],
            signature : null,
            memberToBaptized : null,
            isVisitMemberDialogShown : false,
			receiptData: null,
            message:null,
			tableDataLocal: {...DEFAULT_PAGE},
			currentTag: null,
			unloadInstitution: true,
			familyRoles: [...FAMILY_ROLES],
			familyRoleGiverShown: false,
			tmpMember: {...PARTNER_MODEL},
			childPerson: null,
			showChildCertificate: false,
            tmpInstitution: null,
			donatorShown: false,
            membershipPageId  : generateRandomString(8),
            baptismPageId     : generateRandomString(8),
			childDedicationPageId     : generateRandomString(8),
            certificatePageId : generateRandomString(8),
            pageId : generateRandomString(8),
            institutionId : null,
			showReceipt : false,
			showExtender: false,
			
			showGroupMemberIncluder: false,
			showingDetails: false,
			categories: [],
			titles: [],
			memberActiveOptions: [
				{ label : "All", value: null },
				{ label : "Active", value: true },
				{ label : "Not Active", value: false },
			],
			dateChooserVisible: false,
		}
	},
	methods:{
		renameKey,
		closeDonator: function(isSave = false){
			this.donatorShown = false;
			if(isSave){
				let member = {...this.tmpMember};
				this.donatorShown = false;
				this.listDonations(member);
			}
		},
		hideDetails: function(ignored){
			this.showingDetails = false;
			this.tmpMember = {...PARTNER_MODEL};
		},
		showDetails: function(member){
			this.showingDetails = true;
			this.tmpMember = {
				...member,
				categories: [...this.categories],
				titles: [...this.titles]
			};
		},
		addToGroup: function(member){
			this.tmpMember = {...member};
			this.showGroupMemberIncluder = true;
		},
		closeGroupMemberAdder: function(ids=null){
            if(ids===null) {
	            this.showGroupMemberIncluder = false;
                this.tmpMember = {...PARTNER_MODEL};
            } else {
				if(this.tmpMember!==null) {
					this.tableData = {
						...this.tableData,
						content: this.tableData.content.map(member => {
							if (member.id === this.tmpMember.id) {
								member = {
									...member,
									groupsIds: [...ids]
								}
							}
							return member;
						})
					};
				}
	            this.showGroupMemberIncluder = false;
            }
		},
        print: function(pageId){
            let disp_setting="toolbar=yes,location=no,";
            disp_setting+="directories=yes,menubar=yes,";
            
            disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
            document.getElementById(pageId).style.display = "block";
            let content_value = document.getElementById(pageId).outerHTML;
            document.getElementById(pageId).style.display = "none";
            
            let docprint= window.open("","",disp_setting);
            
            if(docprint!==null) {
                docprint.document.open();
                docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
                docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
                docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
                docprint.document.write('<head><title>Certificate</title>');
                docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;">');
                docprint.document.write('<style type="text/css" media="all">');
                let css = null;
                if(pageId===this.membershipPageId
	                || pageId===this.baptismPageId
	                || pageId===this.childDedicationPageId){
                    css = "" +
                        "@media all {" +
	                        "@page{" +
		                        "size: A4 landscape;" +
		                        "orientation : landscape;" +
		                        "margin:0 !important;" +
		                        "padding: 0 !important;" +
	                        "}" +
	                        // Body
	                        "body{" +
		                        "margin: 0;" +
		                        "padding: 0;" +
		                        "width: 100% !important;" +
		                        "min-width: 100% !important;" +
		                        "max-width: 100% !important;" +
		                        "height: 210mm !important;" +
		                        "min-height: 210mm !important;" +
		                        "max-height: 210mm !important;" +
	                        "}" +
                        "}";
                }else{
                    css = "" +
                        "@media print {" +
	                        "@page{" +
		                        "size: A5 portrait;" +
		                        "orientation : portrait;" +
		                        "margin:0 !important;" +
		                        "padding: 0 !important;" +
	                        "}" +
	                        // Body
	                        "body{" +
		                        "margin: 0;" +
		                        "padding: 0;" +
		                        "width: 148.5mm !important;" +
		                        "min-width: 148.5mm  !important;" +
		                        "max-width: 148.5mm  !important;" +
		                        "height: 210mm !important;" +
		                        "min-height: 210mm !important;" +
		                        "max-height: 210mm !important;" +
		                        "margin-bottom : 0 !important" +
	                        "}" +
                        "}";
                }
                
                docprint.document.write(css);
                docprint.document.write('}</style>');
                docprint.document.write(content_value);
                docprint.document.write('</body></html>');
                docprint.document.close();
                docprint.focus();
            } else {
                // show error ko popup zugaye.
	            this.$root['showAlert']('success', "Popup", "Please enable Popups to operate!");
            }
        },
        handlePrint: function(row, pageId, certificateType){
			const DATA = {
				ownerId: row.id,
				certificateType: certificateType
			}, TITLE = "Certificate Generation.";
			this.$api.post("/certificates/get", DATA).then(response=>{
				if(response.successful){
					this.tmpInstitution = this.theInstitution;
					this.tmpMember = {
						...row,
						certificate: response.object,
						certificateNo : response.object.number
					};
					this.$store.commit("setLoading", true);
					setTimeout(()=>{
						this.$root['showAlert']('success', TITLE, response.message);
						this.$store.commit("setLoading", false);
						this.print(pageId);
					},1500);
				} else {
					this.$root['showAlert']('error', TITLE, response.message);
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
        },
		toggleActive: function(row){
			const TITLE = row.active ? "Deactivation" : "Activation";
			let data = this.$root['addInstitutionId']({ memberId: row.id });
			this.$api.post("/institution/member-toggle-active", data).then(response=>{
				this.$root['showAlert']('success', TITLE, response.message);
				let rowsCount = this.tableData.content.length;
				this.tableData = {
					...this.tableData,
					content: row.active===null
						? this.tableData.content.map(member=>{ if(member.id===row.id) member = response.object;return member;})
						: this.tableData.content.filter(member=>{ return member.id!==row.id}),
					totalElements: (row.active===null)
						? this.tableData.totalElements
						: this.tableData.totalElements-1,
					
					totalPages: (rowsCount===1) ? this.tableData.totalPages-1 : this.tableData.totalPages
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			})
		},
		extendMembership: function(row){
			this.tmpMember = {...row};
			this.showExtender = true;
		},
		closeMembershipExtender: function(updatedMember=null){
			if(updatedMember!==null){
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.map(member=>{
						if(member.id===updatedMember.id) member = updatedMember
						return member;
					})
				}
			}
			this.showExtender = false;
			this.tmpMember = {...PARTNER_MODEL};
		},
		closeDateChooser: function(selectedDate=null){
			this.dateChooserVisible = false;
			if(selectedDate!==null) {
				let person = {
					...this.tmpMember,
					dedicationDate: selectedDate
				};
				this.engageChildDedication(person, selectedDate);
			}
		},
		startDateChooser: function(row){
			this.tmpMember = {...row};
			this.dateChooserVisible = true;
		},
		engageChildDedication: function(row, selectedDate = null){
			let error = null;
			if(typeof row.dob==='undefined' || row.dob===null) {
				error = 'No date of birth for this person!';
			} else {
				let startDate = new Date(row.dob);
				let years = getYearDifference(startDate);
				if(years>18) error = "Person exceeds 18 Years old!";
			}
			if(error!==null){
				this.$root['showAlert']('warn', 'Child Dedication', error);
				return;
			}
			const DATA = this.$root['addInstitutionId']({ ownerId: row.id, certificateType: "KID_DEDICATION" }),
				TITLE = "Certificate Generation";
			this.$api.post("/certificates/get", DATA).then(response=>{
				this.$store.commit("setLoading", true);
				this.childPerson = {
					...row,
					certificateNumber: response.object.number,
					dedicationDate: selectedDate
				};
				this.showChildCertificate = true;
				setTimeout(()=>{
					this.$store.commit("setLoading", false);
					this.print(this.childDedicationPageId);
				}, 5000);
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
        printBirthdayCertificate : function(row){
            if(row.dob!==null){
	            this.tmpMember = row;
	            this.handlePrint(row, this.certificatePageId, "BIRTHDAY");
            }else{
	            this.$root['showAlert']('warn', "Birthday Certificate", "The Date of Birthday is empty.");
            }
        },
        handleMembershipCertificate: function(row){
           this.institutionId = row.institutionId;
	        this.$store.commit("setLoading", true);
	        this.$store.commit("setTmpInstitutionMember", row);
	        this.$store.commit("setLoading", false);
	        this.handlePrint(row, this.membershipPageId, "MEMBERSHIP");
        },
        setPastor: function(row){
            if(row.baptized==="BAPTIZED"){
                let data = {
	                memberId: row.id,
	                institutionId: row.institutionId,
	                churchFunction: "PRIEST"
                };
				data = this.$root['addInstitutionId'](data);
                this.$api.post("/institution/set-church-role", data).then(response=>{
                    this.$root['showAlert']('success', "Pastor Creation", response.message);
	                this.canUploadPriestImage = response.object;
					this.tableData.content.forEach(item=>{
						if(item.id===this.canUploadPriestImage.id){
							item.churchFunction = "PRIEST"
						}
					})
                }).catch(error=>{
                    this.$root['handleApiError'](error, "Pastor Creation");
                });
            }else{
                this.$root['showAlert']("warn", "Pastor creation", "The selected member is not baptized.")
            }
        },
        closeVisitMemberDialog :function(memberBaptized){
            this.isVisitMemberDialogShown = false;
			this.baptizedMember = null;
	        if(memberBaptized!==null){
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.map(member=>{
						if(member.id===memberBaptized.id) return memberBaptized;
						return member;
					})
				}
	        }
        },
        baptizeMember: function(row){
            if(row.baptized==="UNBAPTIZED"){
                this.isVisitMemberDialogShown = true;
                this.memberToBaptized =  row;
            }else{
                this.$root['showAlert']('warn', "Christening", "The selected member is already baptized")
            }
           
        },
        printBaptismCertificate: function(row){
            this.institutionId = row.institutionId;
            if(row.baptized==="BAPTIZED" && row.dateOfBaptism!==null){
	            let data = {
		            certificateType: "BAPTISM",
		            ownerId: row.id
	            };
	            this.$store.commit("setTmpInstitutionMember", row);
	            this.handlePrint(row, this.baptismPageId, "BAPTISM");
            }else{
                this.$root['showAlert']("warn", "Either the member is unbaptized or the date of baptism is null" );
            }
        },
		makeDonation: function(member){
			this.tmpMember = {...member};
			this.donatorShown = true;
		},
		listDonations: function(member){
			this.$store.commit("setLoading", true);
			this.$store.commit("setTmpInstitutionMember", {...member});
			setTimeout(()=>{
				this.$store.commit("setLoading", false);
				this.$router.push(this.isChurch
					? "/church/receipt-generation"
					: "/organizations/receipt-generation");
			}, 2000);
		},
		setFamilyRole: function(role){
			let theFamilyMember = {
				...this.tmpMember,
				familyRole: role
			}
			this.familyRoleGiverShown = false;
			let sameParent = this.$store.getters.getInstitutionMembers.find(member=>{
				let found = false;
				if(typeof member.familyRole!=="undefined"){
					return member.familyRole===role && ["FATHER", "MOTHER"].includes(member.familyRole);
				}
				return found;
			});
			if(typeof sameParent==="undefined") {
				this.addToFamily(theFamilyMember);
				this.tmpMember = {...PARTNER_MODEL};
			} else {
				this.$root['showAlert']("warn",
					"Family Creation.",
					"No two people of same role in the family can be created.");
			}
		},
		handleFamilyCreation: function(){
			if(this.institutionMembers.length<=1) {
				this.$root['showAlert']('warn', "Family", "A family must have more than one member.");
				return;
			}
			let family = this.$store.getters.getFamily;
			family.members = [...this.institutionMembers];
			this.$store.commit("setFamily", family);
			let url = (this.isChurch)
				? "/church/families-board/1"
				: "/organizations/families-board/1";
			this.unloadInstitution = false;
			this.$router.push(url);
		},
		removeTag: function(member){
			this.tableData = {
				...this.tableData,
				content: this.tableData.content.map(singleMember=>{
					if(singleMember.id===member.id) delete singleMember.tag;
					return singleMember;
				})
			};
		},
		applyTagged: function(){
			let family = this.$store.getters.getFamily;
			if(family!==null){
				let members = (typeof family.members!=='undefined') ? family.members : [];
				let familyMembersIds = [];
				if(members.length>0) familyMembersIds = members.map(taggedMember=>taggedMember.id);
				this.tableData = {
					...this.tableData,
					content: [...this.tableData.content.map(member=>{
						let isFamilyMember = members
							.findIndex(foundMember => member.id === foundMember.id) >= 0;
						
						if(familyMembersIds.includes(member.id)) {
							member = {
								...member,
								tag: 'ADDED_TO_FAMILY'
							}
						}
						return {
							...member,
							isFamilyMember: isFamilyMember
						}
					})]
				};
			}
		},
		engageAddressPrinting: function(member){
			let url = (this.isChurch)
				? `/church/address-printing/${member.code}`
				: `/organizations/address-printing/${member.code}`;
			this.$router.push(url);
		},
		engageAddToFamily: function(member){
			this.$store.commit("setLoading", true);
			let family = this.$store.getters.getFamily,
				members = (typeof family.members!=='undefined') ? family.members : [];
			let foundMember = members.find(familyMember=>{
				return familyMember.id===member.id
			});
			this.tmpMember = {...member};
			
			if(typeof foundMember==="undefined") {
				this.familyRoleGiverShown = true;
			} else {
				this.removeTag(member);
				family.members = members.filter(singleMember=>singleMember.id!==member.id);
				this.$store.commit("setFamily", family);
				this.familyRoleGiverShown = false;
				
				this.tableData = {
					...this.tableData,
					content: [...this.tableData.content.map(singleMember=>{
						if(singleMember.id===member.id){
							singleMember = {
								...singleMember,
								isFamilyMember: false
							}
						}
						return singleMember;
					})]
				}
			}
			
			this.$store.commit("setLoading", false);
		},
		addToFamily: function(memberToAdd){
			this.$store.commit("setLoading", true);
			let member = {...memberToAdd};
			this.tableData = {
				...this.tableData,
				content: this.tableData.content.map(singleMember=>{
					if(typeof singleMember.id!=='undefined') {
						if (singleMember.id === member.id) {
							singleMember = {
								...member,
								tag: "ADDED_TO_FAMILY",
								isFamilyMember: true
							}
							this.currentTag = "ADDED_TO_FAMILY";
						}
					}
					return singleMember;
				})
			};
			
			let family = this.$store.getters.getFamily,
				members = (typeof family.members!=='undefined') ? [...family.members] : [];
			let newMembers = new Set([...members, memberToAdd]);
			family = {
				...family,
				members: [...newMembers]
			}
			this.$store.commit("setFamily", family);
			this.$store.commit("setLoading", false);
		},
		contribute: function(member){
			this.$store.commit("setTmpInstitutionMember", member);
			this.$router.push("/church/subscription/1");
		},
		editMember: function(member){
			this.$emit("update", member);
		},
		deleteMember: function(member){
			let memberName = (this.isChurch) ? "Members" : "Partners";
			this.$api.delete(`/institution/delete-member/${member.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(item=>{
						return item.id!==member.id;
					})
				};
				this.$root['showAlert']('success', memberName+" Delete", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, memberName+" Delete");
			})
		},
		handleSearch: function(searchModel){
			let partnerCodes = null;
			if(!isEmpty(this.searchModel.partnerCodes)){
				partnerCodes = this.searchModel.partnerCodes.split(",");
				partnerCodes = partnerCodes.map(code=>{
					return code.trim();
				}).filter(code=>{
					return !isEmpty(code);
				});
			}
			searchModel = {
				...searchModel,
				institutionType : this.institutionType,
				birthdayDateFrom : convertUiToDate(this.searchModel.birthdayDateFrom),
				birthdayDateTo : convertUiToDate(this.searchModel.birthdayDateTo),
				
				partnerCodes: partnerCodes,
				phone: (searchModel.phone!==null)
					? parseInt(""+searchModel.phoneCode+searchModel.phone)
					: searchModel.phone,
				alternatePhone: (searchModel.alternatePhone!==null)
					? parseInt(""+searchModel.alternatePhoneCode+searchModel.alternatePhone)
					: searchModel.alternatePhone,
			};
			this.loadMembers(searchModel);
		},
		loadNextPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page+1
			}
			this.loadMembers(this.searchModel);
		},
		loadPreviousPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page-1
			}
			this.loadMembers(this.searchModel);
		},
		actionPerformed: function(value){
			if(typeof this[value.method]==='function'){
				this[value.method](value.row);
			}
		},
		loadMembers: function(searchModel){
			let data = this.$root['addInstitutionId']({...searchModel});
			let memberName = (this.isChurch) ? "Members" : "Partners";
			if(typeof data.partnerCodes==='string'){
				data = {
					...data,
					partnerCodes: data.partnerCodes.split(",")
				}
			}
			this.$api.post("/institution/list-members", data).then(response=>{
				this.tableData = response;
				this.$root['showAlert']("success", memberName+" loading", "Loaded");
			}).catch(error=>{
				this.$root['handleApiError'](error, memberName+" Loading");
			})
		},
		loadCategories: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/category/get" , data).then(response=>{
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
		loadTitles: function(){
			this.$api.get("/title/get").then(response=>{
				this.titles = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, "Titles Loading");
			});
		},
		initialize: function(){
			if(this.$root['isInstitutionSet']()) {
				this.searchModel = {...MEMBER_SEARCH_MODEL};
				this.loadMembers(this.searchModel);
				this.loadCategories();
				this.loadTitles();
			}
		}
	},
	beforeMount(){
		this.initialize();
	},
	watch: {
		isChurch: function(newValue){
			this.searchModel = {...MEMBER_SEARCH_MODEL};
			this.tableData = {...PAGE_MODEL};
			this.initialize();
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
		institution: {
			type: [Object, null],
			required: true,
			default(){
				return null;
			}
		},
  
	}
}
</script>



<style scoped lang="scss">

</style>