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
		                    :show-institution-name="true"
		                    :refresh-button="true"
		                    @refresh="initialize">
			<DashboardCard :header-title="cardTitle">
				<PaginatedTable :table-data="tableData"
				                :table-headers="tableHeaders"
				                :row-options="rowOptions"
				                :totalAmount="totalAmount"
				                :hasQueryInput="false"
				                :paginator-title="null"
				                @sort="(newContent)=>tableData={...newContent}"
				                @search="makeSearch"
				                @options="actionPerformed"
				                @next="loadPage(true)"
				                @previous="loadPage(false)"
				                @customEvent="commandClick"
				                :paginated="isPaginated"
				                :date-search="isPaginated"
				                :extraInputs="extraInputs"/>
	            
			</DashboardCard>
			
			<div :id="receiptPageId" class="d-none">
				<ContributionReceipt :receiptData="receipt"
				                     :is-church="isInstitutionChurch"
				                     v-if="receipt!==null"/>
			</div>
		</DashboardContainer>
		
		<Dialog :modal="true"
		        :draggable="false"
		        :closable="false"
		        class="w-xl-25 w-md-50 w-100"
		        @close="closeAcknowledgmentDisplayer"
		        :visible="showAcknowledgmentDisplayer">
			<template #header>
				<h4 class="my-0">{{ selectedReceipt.receiptNo }}</h4>
			</template>
			<div class="d-block position-relative mt-0 py-0">
				<table class="table table-stripped"
				       v-if="selectedReceipt.lastAcknowledgements.length>0">
					<tbody>
						<tr v-for="(message, index) in selectedReceipt.lastAcknowledgements"
						    class="p-3"
						    :key="'msg_'+index">
							<td class="fw-bolder">{{ message.communicationWay }}</td>
							<td>{{ getDateFromTimeStamp(message.messageSendingTime, true) }}</td>
						</tr>
					</tbody>
				</table>
				
				<div class="p-3 text-danger text-center fw-bolder"
				     v-else>
					No message sent so far
				</div>
			</div>
			
			<template #footer>
				<div class="row mt-0 py-0 w-100 mx-0 px-0">
					<div class="col-12 mx-0 px-0">
						<Button label="Close"
						        class="text-white w-100"
						        severity="danger"
						        @click="closeAcknowledgmentDisplayer"
						        icon="pi pi-times text-white"/>
					</div>
				</div>
			</template>
		</Dialog>
	</div>
</template>

<script>
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import Dialog from "primevue/dialog";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {PAYMENT_MODES} from "@/dashboard/organization/dialogs/donation/donation";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import {convertUiToDate, getDateFromTimeStamp, renameKey} from "@/utils/AppFx";
import ContributionReceipt from "@/dashboard/church/contributions/receipts/ContributionReceipt.vue";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import {DATA_ENTRY_OPERATORS} from "@/dashboard/users/users";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";


export default {
	name: "ReceiptGenerator",
	components: {Button, ContributionReceipt, Dialog, PaginatedTable, DashboardCard, DashboardContainer},
	computed: {
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		cardTitle: function(){
			let member = this.$store.getters.getTmpInstitutionMember;
			if(member===null) return "No user Selected";
			let fullName = member.fullName+" - "+member.code;
			return fullName.toUpperCase();
		},
		isPaginated: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			let output = false;
			if(loggedInUser!==null){
				output = !["ORGANIZATION_DATA_ENTRY_OPERATOR", "CHURCH_DATA_ENTRY_OPERATOR"]
					.includes(loggedInUser.userType);
			}
			return output;
		},
		extraInputs: function(){
			if(this.isPaginated) {
				return [
					{
						type: "TEXT",
						model: "receiptNo",
						placeholder: "Receipt Number",
						styleClass: "col-md-12 mb-4"
					},
					{
						type: "SELECT",
						model: "paymentMode",
						placeholder: "Payment Mode",
						options: [...PAYMENT_MODES],
						styleClass: "col-md-6 mb-4"
					},
					{
						type: "SELECT",
						model: "category",
						placeholder: (this.isInstitutionChurch) ? "Offering" : "Category",
						options: this.categories,
						styleClass: "col-md-6 mb-4"
					}
				];
			}
			return [];
		},
		tableHeaders: function(){
			let categoryAttribute = (this.isInstitutionChurch) ? "contribution.name" : "category.name";
			let headers = {
				"Payment Mode": {
					rows: [
						{ type: "Select", label: "Mode", attribute: "paymentMode", options: [...PAYMENT_MODES]},
						{ type: "Command", text: "Show Acknowledgments", click: "customEvent"},
					]
				},
				"Receipt No": {
					rows: [ { type: "String", attribute: "receiptNo" } ]
				},
				"Infos.": {
					rows: [
						{ type: "String", label: "Ref. No.", attribute: "referenceNo" } ,
						{ type: "String", label: "Bank Ref.", attribute: "bankReference" },
						{ type: "String", label: "A/C Ref.", attribute: "referenceAccount" },
						{ type: "String", label: "Remarks", attribute: "remarks" }
					]
				},
				"Categories" :{
					rows:[
						{ type: "StringList", attribute: "donationTargets",separator: ", " }
					]
				},/*
				"Communication" :{
					rows:[
						{ type: "DateTime", label: "Last SMS", attribute: 'lastSentSms.messageSendingTime' }
					]
				},*/
				"Entry Date": {
					rows: [
						{ type: "Date", attribute: "entryDate" }
					]
				},
				"Amount": {
					rows: [
						{ type: "PlainNumber", attribute: "total", label: "Rs.", labelNoColumn: true }
					]
				}
			};
			if(this.$root['institution']!==null) {
				let allowedFeatures = this.$root['institution'].allowedFeatures;
				if (allowedFeatures) {
					if (!allowedFeatures.includes("COMMUNICATION")) {
						delete headers['Communication'];
					}
				}
			}
			if(this.isInstitutionChurch) headers = renameKey(headers, "Categories", "Offerings");
			return headers;
		},
		rowOptions: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return [];
			return [
				{ label: "Download/Print", method: "previewReceipt", styleClass:"col-md-6 mb-md-0 mb-4"},
				{ label: "Send SMS", method: "sendSms", styleClass:"col-md-6 mb-md-0 mb-4"},
				{ label: "Send WhatsApp", method: "sendWhatsapp", styleClass:"col-md-6 mb-md-0 mb-4"},
				{ label: "Send Email", method: "sendEmail", styleClass:"col-md-6 mb-md-0 mb-4"},
				{ label: "Mail Merge", method: "mailMerge", styleClass:"col-md-6 mb-md-0 mb-4"},
				{ label: "Delete", method: "deleteDonation", styleClass:"col-md-6 mb-md-0 mb-4" },
			].filter(option=>{
				if(option.method==="deleteDonation"){
					return !["CHURCH_DATA_ENTRY_OPERATOR", "ORGANIZATION_DATA_ENTRY_OPERATOR"]
						.includes(loggedInUser.userType);
				}
				
				if(["sendSms", "sendEmail", "sendWhatsapp"].includes(option.method)){
					return this.institution.allowedFeatures.includes("COMMUNICATION");
				}
				return true;
			});
		},
	},
	data(){
		return {
			cleanContent: true,
			searchModel: {
				...PAGINATOR_SEARCH_MODEL,
				memberId: null,
				paymentMode: null,
				category: null,
				receiptNo: null
			},
			categories: [],
			tableData : {...PAGE_MODEL},
			receipt: null,
			receiptId: null,
			receiptPageId: generateRandomString(8),
            institutionLogo : null,
			loadedLogo : null,
			totalAmount: 0,
			selectedReceipt : null,
			showAcknowledgmentDisplayer : false,
		}
	},
	methods: {
		getDateFromTimeStamp,
		commandClick: function(row){
			this.selectedReceipt = {...row};
			this.showAcknowledgmentDisplayer = true;
		},
		closeAcknowledgmentDisplayer: function(){
			this.selectedReceipt = null;
			this.showAcknowledgmentDisplayer = false;
		},
		previewReceipt: function(receipt){
			this.sendSms(receipt, "RECEIPT").then(result=>{
				if(result!==null){
					this.$store.commit("setLoading", true);
					let lastAcknowledgements = (typeof receipt.lastAcknowledgements!=='undefined' && receipt.lastAcknowledgements!==null)
						? receipt.lastAcknowledgements
						: [];
					lastAcknowledgements = lastAcknowledgements.filter(ack=> ack.communicationWay!=='RECEIPT');
					lastAcknowledgements.push(result);
					let theReceipt = {
						...receipt,
						lastAcknowledgements: [...lastAcknowledgements]
					};
					this.receipt = {...theReceipt};
					this.tableData = {
						...this.tableData,
						content: this.tableData.content.map(singleReceipt=>{
							if(singleReceipt.id===receipt.id) return theReceipt;
							return singleReceipt;
						})
					}
					this.institutionLogo = receipt.institution.logo;
					this.loadInstitutionLogo();
					setTimeout(()=>{
						this.downloadReceipt(this.receiptPageId);
					}, 2000);
				}
			})
		},
        loadInstitutionLogo : function(){
			if(this.institutionLogo!==null) {
				if(this.loadedLogo===null) {
					this.$api.get("files/get/" + this.institutionLogo,
						{responseType: 'blob'})
						.then(response => {
							this.loadedLogo = URL.createObjectURL(response);
							this.receipt.institution.logo = this.loadedLogo;
						}).catch(error => {
						this.$root['handleApiError'](error, "Logo Loading");
					});
				} else {
					this.receipt.institution.logo = this.loadedLogo;
				}
			}
        },
		downloadReceipt: function(containerId) {
			let disp_setting="toolbar=yes,location=no,";
			disp_setting+="directories=yes,menubar=yes,";
			disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
			document.getElementById(containerId).style.display = "block";
			let content_value = document.getElementById(containerId).outerHTML;
			document.getElementById(containerId).style.display = "none";
			this.$store.commit("setLoading", false);
			let docprint = window.open("","",disp_setting);
			if(docprint!=null) {
				docprint.document.open();
				docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
				docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
				docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
				docprint.document.write('<head><title>'+this.receipt.receiptNo+'</title>');
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;">');
				docprint.document.write('<style type="text/css" media="all">');
				let css = "" +
					"@media all {" +
					"@page{" +
					"size: A5;" +
					"margin:0 0 !important;" +
					"padding: 0 0 !important;" +
					"}" +
					// Body
					"body{" +
					"margin: 0 0;" +
					"padding: 0 0;" +
					"}" +
					"*{box-sizing:border-box;}" +
					"font-family:verdana,Arial;color:#000;" +
					"font-family:arial Verdana, Geneva, sans-serif;" +
					"}"
				docprint.document.write(css);
				docprint.document.write('}</style>');
				docprint.document.write(content_value);
				docprint.document.write('</body></html>');
				docprint.document.close();
				docprint.focus();
			} else {
				this.$root['showAlert']('warn', "Print", "Check you have not Popup are not disabled.")
			}
		},
		mailMerge: function(receipt){
			let donationsString = receipt.donationTargets.map(donation=>donation.trim()).join("; "),
				csvRows = [
				[ "Receipt No",
					"Donation Date",
					"Payment Mode",
					this.isInstitutionChurch ? "Member Code" : "Partner Code",
					this.isInstitutionChurch ? "Member" : "Partner",
					this.isInstitutionChurch ? "Offering" : "Category",
					"Total"],
				[
					receipt.receiptNo,
					getDateFromTimeStamp(receipt.entryDate),
					receipt.paymentMode,
					receipt.member.code,
					receipt.member.fullName,
					donationsString,
					receipt.total,
				]
			],
				csvRowString = csvRows.map(row=>{
				return row.join(APP_CONFIG.CSV_DELIMITER);
			}).join("\n");
			
			const blob = new Blob([csvRowString], { type: 'text/csv;charset=utf-8' }),
				link = document.createElement('a');
			// Set the download attribute with a filename
			link.download = 'data.csv';
			// Create a URL for the Blob and set it as the href attribute
			link.href = window.URL.createObjectURL(blob);
			// Append the link to the body (it's invisible)
			document.body.appendChild(link);
			// Programmatically click the link to trigger the download
			link.click();
			// Remove the link after triggering the download
			document.body.removeChild(link);
		},
		sendWhatsapp: function(receipt){
			this.sendSms(receipt, "WHATSAPP");
		},
		sendSms: function(receipt, communicationWay="SMS"){
			return new Promise((resolve)=>{
				let data = {
					receiptId: receipt.id,
					communicationWays : [communicationWay]
				};
				this.$api.post("/institution/acknowledge-fund-receipts", data).then(response=>{
					this.$root['showAlert']("success",
						communicationWay+" sending",
						response.message);
					resolve(typeof response.object!=='undefined' ? response.object: null);
				}).catch(error=>{
					resolve(null);
					this.$root['handleApiError'](error, communicationWay+" sending");
				});
			});
		},
		sendEmail: function(receipt){
			this.sendSms(receipt, "MAIL");
		},
		deleteDonation: function(donation){
			this.$api.delete(`/institution/delete-donation/${donation.id}`).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(singleDonation=>{
						return singleDonation.id!==donation.id;
					})
				}
				this.$root['showAlert']("success", "Donation deletion.", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donation deletion.");
			})
		},
		loadCategories: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/category/get", data).then(response=>{
				this.categories = [
					{ label: "Empty", value: null },
					...response.map(category=>{
						return {
							label: category.name.toUpperCase(),
							value: category.id
						}
					})
				];
				this.$root['showAlert']("success", "Category.", "Loaded.");
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donations.");
			});
		},
		loadContributions: function(){
			let data = this.$root['addInstitutionId']({}, false, true);
			this.$api.get(`/contribution/get/${data.institutionId}`).then(response=>{
				this.categories = [
					{ label: "Empty", value: null },
					...response.map(singleCategory=>{
						return {
							label: singleCategory.name.toUpperCase(),
							value: singleCategory.id
					}})
				];
				this.$root['showAlert']("success", "Church Contributions.", "Contributions Loaded.");
			}).catch(error=>{
				this.$root['handleApiError'](error, "Church Contributions.");
			});
		},
		actionPerformed: function(optionData){
			this[optionData.method](optionData.row);
		},
		makeSearch: function(searchModel){
			this.searchModel = {
				...searchModel,
				entryDate: convertUiToDate(this.searchModel.entryDate)
			};
			this.loadDonations();
		},
		loadPage: function(next=false){
			this.searchModel = {
				...this.searchModel,
				page: (next)
					? this.searchModel.page+1
					: this.searchModel.page-1
			};
			this.loadDonations();
		},
		loadDonations: function(){
			let data = this.$root['addInstitutionId']({...this.searchModel});
			let member = this.$store.getters.getTmpInstitutionMember;
			
			
			if(isEmpty(data.receiptNo)){
				if(member===null) {
					// The import
					this.$root['showAlert']("warn",
						"Warning",
						"No donator/contributor, receipt number selected.");
					return null;
				} else {
					data = {
						...this.searchModel,
						memberId: member.id
					}
				}
			} else {
				this.$store.commit("setTmpInstitutionMember", null);
				data = {
					...data,
					memberId: null,
				}
			}
			
			this.$api.post("/institution/get-partner-donations", data).then(response=>{
				/**
				 * Response is of type
				 * {
				 *     total: which is the total amount given,
				 *     donations: which is of type PAGE_MODEL
				 * }
				 */
				this.$store.commit("setLoading", true);
				this.tableData = response.receipts;
				this.totalAmount = response.total;
				this.$store.commit("setLoading", false);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donations loading.");
			})
		},
		initialize: function(){
			this.cleanContent = false;
			if(this.$store.getters.getTmpInstitutionMember !== null) {
				if(this.isInstitutionChurch) this.loadContributions();
				else this.loadCategories();
				this.loadDonations();
			}
		}
	},
	watch: {
		isChurch: function(newValue){
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
	},
	beforeMount(){
		this.initialize();
	},
	beforeUnmount(){
		this.$store.commit("setTmpInstitutionMember", null);
	}
}
</script>

<style scoped lang="scss">

</style>