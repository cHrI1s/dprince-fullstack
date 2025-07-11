<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div>
		<DashboardContainer :style="'pb-0 pe-md-4'"
		                    :show-institution-name="true">
			<div class="d-none"
			     :id="reportPageId">
				<div :style="page">
					<div :style="pageHeader">
						<div style="width: 25%; padding: 10px;">
							<div style="font-weight: bold;">{{ loggedInUser!==null ? loggedInUser.username : '---' }}</div>
							<div style="font-size: 0.9rem; text-transform: uppercase" v-if="selectedCountry!==null">Countries: {{ selectedCountry }}</div>
							<div style="font-size: 0.9rem;" v-if="selectedStates!==null">State(s): {{ selectedStates }}</div>
							<div style="font-size: 0.9rem;" v-if="searchModel.pincode!==null">PinCode: {{ searchModel.pincode }}</div>
							<div style="font-size: 0.9rem;" v-if="!isEmpty(searchModel.district)">District: <span class="text-uppercase">{{ searchModel.district}}</span></div>
							<div style="font-size: 0.9rem;" v-if="searchModel.phone!==null">Phone: {{ "+"+searchModel.phoneCode+searchModel.phone }}</div>
							<div style="font-size: 0.9rem;"  v-if="selectedSubscriptions!==null">Subscriptions: {{ selectedSubscriptions }}</div>
							<div style="font-size: 0.9rem;" v-if="searchModel.alternatePhone!==null">PinCode: {{ "+"+searchModel.alternatePhoneCode+searchModel.alternatePhone }}</div>
							<div style="font-size: 0.9rem;" v-if="selectedCategories!==null">{{ (isInstitutionChurch ? 'Contributions: ' : 'Categories: ') + selectedCategories }}</div>
							<div style="font-size: 0.9rem;" v-if="selectedPaymentModes!==null">Payment Modes: {{ selectedPaymentModes }}</div>
							<div style="font-size: 0.9rem;" v-if="selectedLanguages!==null">Languages: {{ selectedLanguages }}</div>
							<div style="font-size: 0.9rem;" v-if="searchModel.creditAccountId!==null">
								Credit Account: {{ getDropdownLabel(searchModel.creditAccountId, creditAccounts) }}
							</div>
							<div style="font-size: 0.9rem;" v-if="uniqueDates.length>0">
								{{ uniqueDates.length>1 ? "Dates" : "Date" }}:
								{{ uniqueDates.join(" - ") }}
							</div>
						</div>
						
						<div style="width: 50%; text-align: center; padding: 10px;">
							<div style="font-weight: bold;">
								<h4 style="margin-bottom: 0;">{{ institution===null ? "---" : institution.name }}</h4>
							</div>
							<div v-if="amountGap!==null">
								{{ amountGap }}
							</div>
						</div>
						
						<div style="width: 25%; text-align: right; padding: 10px; font-size: 0.9rem;">
							<div>{{ getDateFromTimeStamp(nowDatime, true) }}</div>
						</div>
					</div>
					
					<div class="w-100 text-uppercase" style="text-transform: uppercase !important;">
						<PaginatedTable :table-data="tableData"
						                :rowBaseCode="reportCode"
						                :table-headers="tableHeaders"
						                :extra-inputs="extraInputs"
						                :rowCounter="true"
						                :total-amount="totalAmountToShow"
						                :searchModel="searchModel"
						                :tablePrintable="true"/>
					</div>
				</div>
			</div>
			
			<AddressPrintingPage class="mb-4 d-none"
			                     style="width: 100%;"
			                     v-if="addresses.length>0"
			                     :id="addressPageId"
			                     :headers="pageHeaderContent"
			                     :content="addresses"/>
			
			
			
			
			<DashboardCard :header-title="headerTitle"
			               :refreshButton="true"
			               @refresh="resetForm(true)">
				<PaginatedTable :table-data="tableData"
				                :exportable="exportable"
				                :searchQueryPlaceholder="searchPlaceholder"
				                :table-headers="tableHeaders"
				                :extra-inputs="extraInputs"
				                @onChangeModel="triggerModelChange"
				                :searchModel="searchModel"
				                :total-amount="totalAmountToShow"
				                :row-options="rowOptions"
				                :chips="searchChips"
				                :paginator-title="null"
				                :paginated="false"
				                :date-search="false"
				                @next="(params)=>loadSidePages(true, params)"
				                @previous="(params)=>loadSidePages(false, params)"
				                @sort="(newContent)=>tableData={...newContent}"
				                @search="makeSearch"
				                ref="myTable"
				                @options="actionPerformed"/>
			</DashboardCard>
			
			
			<!--  -->
			<div class="mt-4 row"
			     v-if="tableData.content.length>0">
				<div class="mb-4"
				     :class="exportable ? 'col-md-4' : 'col-md-6'">
					<Button :label="printButtonLabel"
					        class="w-100"
					        icon="pi pi-print"
					        @click="makePdf(false)"/>
				</div>
				
				<div class="mb-4"
				     v-if="pageType==='ADDRESS' && false"
				     :class="exportable ? 'col-md-4' : 'col-md-6'">
					<Button :label="'Roll Print'"
					        class="w-100"
					        severity="warn"
					        icon="pi pi-print"
					        @click="makePdf(true)"/>
				</div>
				
				<div class="mb-4"
				     v-if="pageType!=='ADDRESS'"
				     :class="exportable ? 'col-md-4' : 'col-md-6'">
					<Button label="Mail Merge"
					        class="w-100"
					        icon="pi pi-print"
					        severity="warn"
					        @click="mailMerge"/>
				</div>
				
				<div class="col-md-4 mb-4" v-if="exportable">
					<Button label="Export Excel"
					        icon="pi pi-file-export"
					        severity="danger"
					        class="w-100"
					        @click="engageExcelExport"/>
				</div>
			</div>
		</DashboardContainer>
		
		<div :id="receiptPageId" class="d-none">
			<ContributionReceipt :receiptData="receipt"
			                     :is-church="isInstitutionChurch"
			                     v-if="receipt!==null"/>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {
	ADDRESS_PRINT_GENERATOR,
	ADDRESS_PRINTING_SEARCH_MODEL,
	MEMBERSHIP_DURATIONS,
	PAYMENT_MODES,
	SUPER_ADMINISTRATORS_ROLES
} from "@/dashboard/members/members";
import {AVAILABLE_LANGUAGES, INDIA_STATES, SUBSCRIPTION_PLANS} from "@/dashboard/organization/Organization";
import AddressPrintingPage from "@/dashboard/members/AddressPrintingPage.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {areArraysEquals, convertUiToDate, getDateFromTimeStamp, getDropdownLabel, renameKey} from "@/utils/AppFx";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import {getCountries} from "@/dashboard/utils/countries";
import ContributionReceipt from "@/dashboard/church/contributions/receipts/ContributionReceipt.vue";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

export default {
	name: "AddressPrinting",
	components: {
		ContributionReceipt,
		Button,
		PaginatedTable,
		AddressPrintingPage,
		DashboardCard,
		DashboardContainer
	},
	data(){
		return {
			generator: {...ADDRESS_PRINT_GENERATOR},
			addresses: [],
			creditAccounts: [],
			tableData: {...PAGE_MODEL },
			allTableData: {...PAGE_MODEL },
			elementsPerPage: 3,
			totalAmount: null,
			uniqueDates: [],
			amountGap: null,
			pageHeaderContent: [],
			searchModel: {...ADDRESS_PRINTING_SEARCH_MODEL},
			categories: [],
			searchChips: [
				{type: "STRING", model: "query", label: "Query"},
				{type: "MULTISELECT", model: "categories", label: "Categories"},
				{type: "MULTISELECT", model: "paymentMode", label: "Payment Modes"},
			],
			page: {
				zoom: "100%",
				width: "100%",
				//minWidth: "210mm",
				//maxWidth: "210mm",
			},
			pageHeader: {
				display: "flex",
				width: "100%",
				backgroundColor: "white",
				color: "#000"
			},
			nowDatime: new Date(),
			
			addressPageId: generateRandomString(8),
			reportPageId: generateRandomString(8),
			
			receiptPageId: generateRandomString(8),
			receipt: null,
		}
	},
	computed: {
		reportCode: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser!==null) {
				let institution;
				if (SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)) institution = this.$store.getters.getInstitution;
				else institution = loggedInUser.institution;
				
				if(institution==null) return null;
				return institution.baseCode.toUpperCase();
			}
			return "";
		},
		totalAmountToShow: function(){
			let amount = this.totalAmount;
			if(this.searchModel.withReceipt!==true) amount = null;
			return amount;
		},
		exportable: function(){
			return this.pageType!=='ADDRESS' && this.tableData.content.length>0;
		},
		loggedInUser: function(){
			return this.$store.getters.getLoggedInUser;
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		printButtonLabel: function(){
			return this.pageType==="ADDRESS" ? "Print Addresses" : "Generate Report";
		},
		headerTitle : function(){
			return this.pageType==="ADDRESS" ? "Print Generator" : "Report Generation";
		},
		tableHeaders: function(){
			let headers;
			if(this.pageType==="ADDRESS") {
				headers = {
					"Full name":{
						rows: [
							{type: "StringArray", attribute: ["firstName", "lastName"]}
						]
					},
					"Partner Code":{ rows: [ {type: "String", attribute: "code" } ] },
					"Address":{ rows: [ {type: "StringArray", attribute: ["addressLine1", "addressLine2", "addressLine3", "district", "pincode"], separator: ", " } ] },
				}
				if(this.isInstitutionChurch) headers = this.renameKey(headers, "Partner Code", "Member Code");
			} else {
				headers = {
					"Receipt No." : { rows: [ { type: "StringCommand", attribute: "receiptNo", optionOpener: true } ]},
					"Date" : { rows: [ { type: "DateTime", attribute: "donationDate" } ]},
					"Partner Code" : { rows: [ { type: "String", attribute: "memberCode" } ]},
					"Partner Name" : { rows: [ { type: "String", attribute: "donator" } ]},
					"Category" : { rows: [ { type: "String", attribute: "category" } ]},
					"Payment Mode" : { rows: [ { type: "Select", attribute: "paymentMode", options: [...PAYMENT_MODES] } ]},
					"Amount(Rs.)" : { rows: [ { type: "PlainNumber", attribute: "amount" } ]},
				}
				if(this.isInstitutionChurch) {
					headers = this.renameKey(headers, "Category", "Offering");
					headers = this.renameKey(headers, "Partner Code", "Member Code");
					headers = this.renameKey(headers, "Partner Name", "Member Name");
				}
			}
			if(this.searchModel.withReceipt===null || !this.searchModel.withReceipt){
				["Receipt No.", "Payment Mode", "Amount(Rs.)", "Category", "Date"].forEach(removeableKey=>{
					delete headers[removeableKey];
				});
				headers = {
					...headers,
					"Phone Number" : { rows: [ { type: "PhoneNumber", attribute: ['phoneCode', 'phone'] } ]},
				}
			}
			return headers;
		},
		searchPlaceholder: function(){
			return (this.isInstitutionChurch) ? "Search by Member Name...." : "Search by Partner name...";
		},
		extraInputs: function(){
			let isChurch = this.isInstitutionChurch;
			let output = [
				{
					type: "MULTISELECT",
					model: "country",
					options: [
						...getCountries()
					],
					styleClass: "col-md-4 mb-4",
					placeholder: "Country"
				},
				{
					type: "MULTISELECT",
					model: "states",
					styleClass: "col-md-4 mb-4",
					placeholder: "State",
					options: [...INDIA_STATES],
					shownIf: {model: "country", values: ["IN"],
						exactMatch: true,
						thenModelClass: "col-md-4 mb-4",
						onShowSet:{model: "state", value: null}
					}
				},
				{
					type: "TEXT",
					model: "state",
					styleClass: "col-md-4 mb-4",
					placeholder: "State",
					hiddenIf: {model: "country", values: ["IN"],
						exactMatch: true,
						thenModelClass: "col-md-4 mb-4",
						onShowSet:{model: "states", value: null}
					}
				},
				{type: "TEXT", model: "pincode", styleClass: "col-md-4 mb-4", placeholder: "Pincode"},
				{
					type: "TEXT",
					model: "district",
					styleClass: "col-12 mb-4",
					placeholder: "District"
				}
			];
			if(isChurch) {
				output = [
					...output,
					{
						type: "MULTISELECT",
						model: "categories",
						placeholder: "Offering",
						options: this.categories,
						styleClass: "col-md-12 mb-4"
					}
				]
			} else {
				output = [
					...output,
					{
						type: "MULTISELECT",
						model: "subscription",
						styleClass: "col-md-6 mb-4",
						placeholder: "Subscription",
						options: [...SUBSCRIPTION_PLANS]
					},
					{
						type: "MULTISELECT",
						model: "categories",
						placeholder: "Category",
						options: this.categories,
						styleClass: "col-md-6 mb-4"
					}
				];
			}
			
			return [
				...output,
				{
					type: "SELECT",
					model: "duration",
					styleClass: "col-md-4 mb-4",
					placeholder: "Duration",
					options: [...MEMBERSHIP_DURATIONS]
				},
				{
					type: "DATE",
					model: "from",
					styleClass: "col-md-4 mb-4",
					placeholder: "Starting date",
					format: "dd/mm/yy",
					maxDate: this.nowDatime,
					shownIf: {model: "duration", values: ["CUSTOM_DATE_GAP", "SINGLE_DATE"]}
				},
				{
					type: "DATE",
					model: "to",
					styleClass: "col-md-4 mb-4",
					placeholder: "Ending date",
					format: "dd/mm/yy",
					maxDate: this.nowDatime,
					shownIf: {model: "duration", values: ["CUSTOM_DATE_GAP"]}
				},
				{ type: "LINE", styleClass: "col-12" },
				{type: "NUMBER", model: "minAmount", placeholder: "Amount Minimum", styleClass: "col-md-6 mb-4", shownIf: { model: "withReceipt",  values:[true] }},
				{type: "NUMBER", model: "maxAmount", placeholder: "Amount Maximum", styleClass: "col-md-6 mb-4", shownIf: { model: "withReceipt",  values:[true] }},
				{
					type: "MULTISELECT",
					model: "paymentMode",
					placeholder: "Payment Mode",
					options: [...PAYMENT_MODES],
					styleClass: "col-md-6 mb-4",
					shownIf: { model: "withReceipt",  values:[true] }
				},
				{
					type: "MULTISELECT",
					model: "languages",
					placeholder: "Languages",
					options: [...AVAILABLE_LANGUAGES],
					styleClass: "col-md-6 mb-4"
				},
				{
					type: "TEXTAREA",
					model: "memberCodes",
					styleClass: "col-12 mb-4",
					placeholder: (this.isInstitutionChurch) ? "Member Codes..." : "Partner Codes..."
				},
				{type: "PHONE_NUMBER", model: ["phoneCode", "phone"], styleClass: "col-md-6 mb-4", placeholder: "Phone Number"},
				{
					type: "PHONE_NUMBER",
					model: ["alternatePhoneCode", "alternatePhone"],
					styleClass: "col-md-6 mb-4",
					placeholder: "Alternate Phone Number"
				},
				{
					type: "SELECT",
					model: "creditAccountId",
					options: [...this.creditAccounts],
					styleClass: "col-md-4 mb-4",
					placeholder: "Credit Account",
					shownIf: { model: "withReceipt",  values:[true] }
				},
				{
					type: "SELECT",
					model: "withReceipt",
					options: [{label: "All", value: null},{label: "With Receipt", value: true},{label: "No Receipt", value: false}],
					styleClass: "col-md-4 mb-4",
					placeholder: "Have Receipt - All",
					triggerChange: true
				},
				{
					type: "SELECT",
					model: "active",
					options: [{label: "All", value: null}, {label: "Active", value: true},{label: "Not Active", value: false}],
					styleClass: "col-md-4 mb-4",
					placeholder: "All"
				},
				{ type: "LINE", styleClass: "col-12" },
			];
		},
		rowOptions: function() {
			if(this.pageType==="ADDRESS") return null;
			let options = [
				{label: "Download/Print", method: "previewReceipt", styleClass: "col-md-6 mb-md-0 mb-4"},
				{label: "Send SMS", method: "sendSms", styleClass: "col-md-6 mb-md-0 mb-4"},
				{label: "Send Whatsapp", method: "snedWhatsapp", styleClass: "col-md-6 mb-md-0 mb-4"},
				{label: "Send Email", method: "sendEmail", styleClass: "col-md-6 mb-md-0 mb-4"},
				{label: "Mail Merge", method: "mailMergeSingleReceipt", styleClass: "col-md-6 mb-md-0 mb-4"},
				{label: "Delete", method: "deleteDonation", styleClass: "col-md-6 mb-md-0 mb-4"},
			];
			return options.filter(singleOption=>{
				if(["sendSms", "sendEmail", "sendWhatsapp"].includes(singleOption.method)){
					if(this.institution===null) return false;
					return this.institution.allowedFeatures.includes("COMMUNICATION");
				}
				return true;
			});
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		selectedPaymentModes: function(){
			let output = null;
			let paymentModes = this.searchModel.paymentMode;
			if(paymentModes!==null){
				if(paymentModes.length>0) {
					output = paymentModes.map(item => {
						return getDropdownLabel(item, [...PAYMENT_MODES]);
					}).filter(item => {
						return item !== null;
					}).join(", ");
				}
			}
			return output;
		},
		selectedCategories: function(){
			let output = null;
			let categories = this.searchModel.categories;
			if(categories!==null){
				if(categories.length>0) {
					output = categories.map(item => {
						return getDropdownLabel(item, this.categories);
					}).filter(item => {
						return item !== null;
					}).join(", ");
				}
			}
			return output;
		},
		selectedLanguages: function(){
			let output = null;
			let languages = this.searchModel.languages;
			if(languages!==null){
				if(languages.length>0) {
					output = languages.map(item => {
						return getDropdownLabel(item, [...AVAILABLE_LANGUAGES]);
					}).filter(item => {
						return item !== null;
					}).join(", ");
				}
			}
			return output;
		},
		selectedSubscriptions: function(){
			let output = null;
			if(this.isInstitutionChurch) return output;
			let subscriptions = this.searchModel.subscription;
			if(subscriptions!==null){
				if(subscriptions.length>0) {
					output = subscriptions.map(item => {
						return getDropdownLabel(item, [...SUBSCRIPTION_PLANS]);
					}).filter(item => {
						return item !== null;
					}).join(", ");
				}
			}
			return output;
		},
		selectedCountry: function(){
			let output = null;
			let countries = this.searchModel.country;
			if(countries!==null && countries.length>0){
				output = countries.map(country=>{
					return getDropdownLabel(country, [...getCountries()]);
				}).join(", ");
			}
			return output;
		},
		selectedStates: function(){
			let output = null;
			if(areArraysEquals(this.searchModel.country, ['IN'])){
				if (this.searchModel.states !== null && this.searchModel.states.length>0){
					output = this.searchModel.states.map(state=>{
						return getDropdownLabel(state, [...INDIA_STATES]);
					}).join(", ");
				}
			} else {
				if (this.searchModel.state !== null) output = this.searchModel.state;
			}
			return output;
		}
	},
	methods: {
		triggerModelChange: function(newSearchModel){
			if(newSearchModel!==null){
				if(newSearchModel.withReceipt!==this.searchModel.withReceipt
				 || newSearchModel.active!==this.searchModel.active) {
					this.tableData = {...PAGE_MODEL};
					this.searchModel = {
						...this.searchModel,
						withReceipt: newSearchModel.withReceipt,
						active: newSearchModel.active,
						page: 1
					}
				}
			}
		},
		getDropdownLabel,
		renameKey,
		isEmpty,
		engageExcelExport: function(){
			this.$refs.myTable.exportAsCSV();
		},
		mailMerge: function(){
			this.$store.commit("setLoading", true);
			let contentRows = Object.values(
				this.allTableData.content.map(row=>{
					return [
						row.receiptNo,
						this.getDateFromTimeStamp(row.donationDate, false),
						row.memberCode,
						row.donator,
						row.category,
						this.getDropdownLabel(row.paymentMode, [...PAYMENT_MODES]),
						row.amount
					];
				}).reduce((accumulator, [col1, col2, col3, col4, col5, col6, col7])=>{
					if(!accumulator[col3]) {
						col5 = col5.replaceAll(",", ";");
						accumulator[col3] = [col1, col2, col3, col4, col5, col6, col7];
					} else {
						accumulator[col3][6] += col7;
						let categories = accumulator[col3][4]+"; "+col5;
						categories = categories.replaceAll(",", ";");
						accumulator[col3][4] = categories.trim();
					}
					return accumulator;
				}, {})
			);
			let csvRows = [
				["Receipt No",
					"Donation Date",
					(this.isInstitutionChurch) ? "Member Code" : "Partner Code" ,
					(this.isInstitutionChurch) ? "Member" : "Partner",
					"Category", "Payment Mode", "Amount"],
				...contentRows
			];
			let csvRowString = csvRows.map(row=>{
				return row.join(",")
			}).join("\n").toUpperCase();
			
			
			const blob = new Blob([csvRowString], { type: 'text/csv' }),
				link = document.createElement('a');
			// Set the download attribute with a filename
			let fileName = this.getDateFromTimeStamp(new Date(), true)+'-Report.csv';
			fileName = fileName.replaceAll(":", "_")
				.replaceAll("/", "_");
			link.download = fileName;
			// Create a URL for the Blob and set it as the href attribute
			link.href = window.URL.createObjectURL(blob);
			// Append the link to the body (it's invisible)
			document.body.appendChild(link);
			// Programmatically click the link to trigger the download
			link.click();
			// Remove the link after triggering the download
			document.body.removeChild(link);
			this.$store.commit("setLoading", false);
		},
		actionPerformed: function(value){
			if(value.method!==null) this[value.method](value.row);
		},
		loadContributions: function(){
			let data = this.$root['addInstitutionId']({}, false, true);
			this.$api.get(`/contribution/get/${data.institutionId}`).then(response=>{
				this.categories = response.map(singleCategory=>{
					return {
						label: singleCategory.name.toUpperCase(),
						value: singleCategory.id
					}
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Church Contributions.");
			});
		},
		loadCategories: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post(`/category/get`, data).then(response=>{
				this.categories = response.map(singleCategory=>{
					return {
						label: singleCategory.name.toUpperCase(),
						value: singleCategory.id
					}
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Categories.");
			});
		},
		resetForm : function(reload = true){
			if (reload) {
				this.searchModel = {...ADDRESS_PRINTING_SEARCH_MODEL};
				this.addresses = [];
				this.totalAmount = null;
				this.tableData = {...PAGE_MODEL};
				this.allTableData = {...PAGE_MODEL};
				this.searchChips = [
					{type: "STRING", model: "query", label: "Query"},
					{type: "MULTISELECT", model: "categories", label: "Categories"},
					{type: "MULTISELECT", model: "paymentMode", label: "Payment Modes"},
				];
				this.pageHeaderContent = [];
			}
			if(this.$root['isInstitutionSet']()) {
				if(this.isInstitutionChurch) this.loadContributions();
				else this.loadCategories();
				this.loadAccounts();
			}
		},
		getDateFromTimeStamp,
		loadSidePages: function(isNext, params){
			let data = {
				...this.searchModel,
				page: isNext ? this.searchModel.page+1 : this.searchModel.page-1,
				currentPage: isNext ? this.searchModel.page+2 : this.searchModel.page,
				size: params.size
			};
			this.searchModel = {...data};
		},
		makeSearch : function(searchModel){
			this.searchModel = {...PAGE_MODEL};
			this.pageHeaderContent = [];
			if(this.$root['isInstitutionSet']()) {
				this.$store.commit("setLoading", true);
				this.searchModel = {...searchModel};
				let fromDate = new Date(this.nowDatime),
					toDate = new Date(this.nowDatime);
				switch (this.searchModel.duration) {
					case "YEAR":
						fromDate.setFullYear(fromDate.getFullYear() - 1);
						break;
					
					case "LAST_SIX_MONTH":
						fromDate = fromDate.setMonth(toDate.getMonth() - 6);
						break;
					
					case "LAST_THREE_MONTH":
						fromDate = fromDate.setMonth(toDate.getMonth() - 3);
						break;
					
					case "LAST_MONTH":
						fromDate = fromDate.setMonth(toDate.getMonth() - 1);
						break;
					
					case "LAST_SEVEN_DAYS":
						fromDate = fromDate.setDate(toDate.getDate() - 7);
						break;
					
					case "TODAY":
						toDate = null;
						break;
					
					case "SINGLE_DATE":
						fromDate = this.searchModel.from;
						toDate = null;
						break;
					
					case "CUSTOM_DATE_GAP":
						fromDate = this.searchModel.from;
						toDate = this.searchModel.to;
						break;
						
					case "LIFE_TIME":
						fromDate = null;
						toDate = null;
						break;
				}
				
				if(!this.isEmpty(this.searchModel.district)){
					this.pageHeaderContent = [
						{label: "District", value: this.searchModel.district}
					];
				}
				let dateGaps = [];
				if(fromDate!==null) dateGaps.push(getDateFromTimeStamp(fromDate));
				
				if(toDate===null && this.searchModel.duration!=='LIFE_TIME') toDate = new Date();
				if(toDate!==fromDate && toDate!==null) dateGaps.push(getDateFromTimeStamp(toDate));
				if(dateGaps.length>0){
					this.uniqueDates = [...new Set(dateGaps)];
					this.pageHeaderContent = [
						...this.pageHeaderContent,
						{
							label: dateGaps.length>1 ? "Dates" : "Date",
							value: this.uniqueDates.join(" - ")
						},
					];
				}
				
				let minAmount = this.searchModel.minAmount,
					maxAmount = this.searchModel.maxAmount;
				if(minAmount!==null || maxAmount!==null) {
					this.amountGap = [minAmount, maxAmount]
						.filter(a=>{return a!==null;})
						.join(" - ") + 'Rs.';
					this.pageHeaderContent = [
						...this.pageHeaderContent,
						{
							label: "Amount",
							value: this.amountGap
						},
					];
				}
				let data = this.$root['addInstitutionId']({...this.searchModel});
				if (data.memberCodes !== null && typeof data.memberCodes !== "undefined") {
					data.memberCodes = data.memberCodes.split(",").filter(memberCode => {
						return memberCode !== null && memberCode.trim() !== "";
					}).map(memberCode => {
						return memberCode.trim();
					});
				}
				let url = this.pageType === "ADDRESS"
					? "/institution/get-addresses"
					: "/institution/get-report";
				data = {
					...data,
					phone: (data.phone!==null)
						? parseInt(""+data.phoneCode+data.phone)
						: data.phone,
					alternatePhone: (data.alternatePhone!==null)
						? parseInt(""+data.alternatePhoneCode+data.alternatePhone)
						: data.alternatePhone,
				}
				delete data["page"];
				delete data["size"];
				
				if(this.pageType!=='ADDRESS'){
					this.reportCode = generateRandomString(2)+"-";
				}
				
				
				let unEmptyFields = [
					{model: "district", type: "STRING"},
					{model: "categories", type: "ARRAY"}
				];
				if(data.duration==="LIFE_TIME"){
					let selected = [];
					let i = 0, fieldsCount = unEmptyFields.length;
					for(; i<fieldsCount; i++){
						let fieldModel = unEmptyFields[i];
						if(fieldModel.type==="STRING"){
							if(isEmpty(data[fieldModel.model])) {
								selected.push(false);
							}
						}
						
						if(fieldModel.type==="ARRAY"){
							if(data[fieldModel.model]===null || data[fieldModel.model].length===0
							|| (this.categories.length===data[fieldModel.model].length)) {
								selected.push(false);
							}
						}
					}
					
					let falses = 0;
					selected.forEach(single=>{
						if(!single) falses += 1;
					});
					
					if(falses===fieldsCount){
						let categoryName = this.isInstitutionChurch
							? "Church Contribution"
							: "Category";
						this.$store.commit("setLoading", false);
						this.$root['showAlert']("error", "Filters",
							"Please select at least a "+categoryName+"(not all) or a District.");
						return;
					}
				}
				
				this.$api.post(url, data).then(response => {
					this.$store.commit("setLoading", true);
					this.tableData = this.pageType === "ADDRESS" ? response : response.receipts;
					this.allTableData = this.pageType === "ADDRESS" ? {...response} : {...response.receipts};
					if(this.pageType !== "ADDRESS") this.totalAmount = response.total;
					let content = [...this.tableData.content],
						currentReturnedPages = parseInt(Math.ceil(content.length / 21));
					if (currentReturnedPages > 0 && this.pageType === "ADDRESS") {
						for (let i = 0; i < currentReturnedPages; i++) {
							this.addresses = [
								...content.slice(0, i * 21),
								null,
								...content.slice(i * 21)
							];
						}
					};
					this.$root['showAlert']("success", "Data Loading", "Loaded.");
					this.$store.commit("setLoading", false);
					this.$store.commit("setLoading", false);
				}).catch(error => {
					this.$root['handleApiError'](error, "Data Fetching");
					this.$store.commit("setLoading", false);
					this.$store.commit("setLoading", false);
				});
			}
		},
		makePdf : function(rollPrint = false){
			this.$store.commit("setLoading", true);
			setTimeout(()=>{
				this.downloadAsPDF(rollPrint);
			}, 2000);
		},
		downloadAsPDF : function(rollPrint = false) {
			let disp_setting="toolbar=yes,location=no,";
			disp_setting+="directories=yes,menubar=yes,";
			disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
			let htmlElement = document.getElementById((this.pageType==='ADDRESS') ? this.addressPageId : this.reportPageId);
			htmlElement.style.display = "block";
			htmlElement.style.width = "100%";
			let content_value = htmlElement.outerHTML;
			if(this.pageType!=='ADDRESS')  content_value += '<footer></footer>';
			htmlElement.style.display = "none";
			this.$store.commit("setLoading", false);
			let docprint = window.open("","",disp_setting);
			if(docprint!=null) {
				docprint.document.open();
				docprint.document.write('<!DOCTYPE html>');
				docprint.document.write('<html lang="en">');
				docprint.document.write('<head><title>To Print</title>');
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;width: 21cm;">');
				docprint.document.write('<style type="text/css" media="all">');
				let css = "@media all {";
				if(!rollPrint) {
					if(this.pageType==='ADDRESS') {
						css += "@page{" +
								"size: A4;" +
								"margin:0 !important;" +
								"padding: 0 !important;" +
							"}";
					} else {
						css += "@page{" +
								"size: A4;" +
								"margin:0 !important;" +
								"padding: 0 !important;" +
							"}";
					}
				}
				css += "body{" +
						"margin: 0 !important;" +
						"padding: 0 !important;" +
						"counter-reset: page 1;" +
					"}" +
					"*{box-sizing:border-box;}";
					".text-uppercase{text-transform: uppercase !important;}";
				
					if (this.pageType === "ADDRESS") {
						css += "#" + ((this.pageType === 'ADDRESS') ? this.addressPageId : this.reportPageId) + " div:nth-child(22n+22){" +
								"page-break-after: always !important;" +
								"break-after: always !important;" +
							"}" +
							"#" + ((this.pageType === 'ADDRESS') ? this.addressPageId : this.reportPageId) + " div{" +
								"page-break-inside:avoid !important;" +
								"break-inside: avoid !important;" +
								"position:relative;" +
								"display: block;" +
							"}";
						css += "#" + this.addressPageId + " div:nth-child(22n+20)," +
							"#" + this.addressPageId + " div:nth-child(22n+21)," +
							"#" + this.addressPageId + " div:nth-child(22n+22){" +
								"padding-bottom: 0 !important;" +
							"}";
					}
					else {
						css = "@media print {" +
							"@page{" +
								"size: A4;" +
								"margin:0mm 8mm !important;" +
								"padding: 0mm 8mm !important;" +
								"counter-increment: page;" +
							"}" +
							// Body
							"body{" +
								"margin: 0mm 8mm;" +
								"padding: 0mm 8mm;" +
								"counter-reset: page;" +
							"}" +
							"footer {" +
								"position: fixed;" +
								"bottom: 10px; right: 10px !important;" +
								"font-size: 12px;" +
								"color: #000;" +
							"}" +
							".text-uppercase{text-transform: uppercase !important;}"+
							"footer::after {" +
								// 'content: "Page " counter(page);' +
							"}" +
						"}" + css;
						css += " table {" +
								"width: 100% !important;" +
								"border-collapse: collapse;" +
								"margin: 0;" +
								"font-family: Arial, sans-serif;" +
							"}" +
							"th {" +
								"border: 1px solid #444;" +
								"padding: 5px;" +
								"text-align: left;" +
								"font-weight: bold;" +
								"color: #000;" +
								"text-transform: uppercase !important;" +
							"}" +
							"tbody tr {" +
								"border: 1px solid #444;" +
							"}" +
							"td {" +
								"border: 1px solid #444;" +
								"padding: 5px;" +
								"text-align: left;" +
								"color: #000;" +
							"}" +
							"tfoot td {" +
								"border-top: 1px solid #444; " +
								"padding: 5px;" +
								"text-align: right;" +
								"color: #000;" +
							"}"
					}
					css += "font-family:verdana,Arial;color:#000;" +
						"font-family:arial Verdana, Geneva, sans-serif;" +
						"font-size:12px;" +
					"}";
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
		loadAccounts: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/institution/get-accounts", data).then(response => {
				this.creditAccounts = [
					{ label: "Empty", value: null},
					...response.map(account=>{
						return {
							label: account.name.toUpperCase(),
							value: account.id
						}
					})
				];
			}).catch(error => {
				this.$root['handleApiError'](error, "Credit Accounts Loadings");
			});
		},
		
		
		fetchReceipt: function(data){
			return new Promise((resolve, reject)=>{
				data = this.$root['addInstitutionId'](data);
				this.$api.post("/institution/get-single-donation", data).then(response=>{
					resolve(response.object);
				}).catch(error=>{
					reject(error);
				});
			});
		},
		deleteDonation: function(donation){
			this.$api.delete(`/institution/delete-donation/${donation.id}`).then(response=>{
				this.$store.commit("setLoading", true);
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(singleDonation=>{
						return singleDonation.receiptNo!==donation.receiptNo;
					})
				};
				this.allTableData = {
					...this.allTableData,
					totalElements: this.allTableData.totalElements-1,
					content: this.allTableData.content.filter(singleDonation=>{
						return singleDonation.receiptNo!==donation.receiptNo;
					})
				};
				this.$store.commit("setLoading", false);
				this.$root['showAlert']("success", "Donation deletion.", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donation deletion.");
			})
		},
		sendSms: function(receipt, way="SMS"){
			let data = {
				receiptId: receipt.id,
				communicationWays : [way]
			}, title;
			switch(way){
				case "SMS":
				default:
					title = "SMS Sending...";
					break;
					
				case "EMAIL":
					title = "Email Sending...";
					break;
					
				case "WHATSAPP":
					title = "Whatsapp Message Sending...";
					break;
			}
			this.$api.post("/institution/acknowledge-fund-receipts", data).then(response=>{
				this.$root['showAlert']("success",
					title,
					response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, title);
			})
		},
		sendEmail: function(receipt){
			this.sendSms(receipt, "EMAIL")
		},
		snedWhatsapp: function(receipt){
			this.sendSms(receipt, "WHATSAPP")
		},
		mailMergeSingleReceipt: function(singleReport){
			this.fetchReceipt({receiptCode: singleReport.receiptNo}).then(receipt=>{
				console.log(receipt);
				let donationsString = receipt.donationTargets.map(donation=>donation.trim()).join("; ");
				let csvRows = [
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
				];
				/*
				let csvRows = [
					[ "Payment Mode",
						"Receipt No",
						"DateTime",
						"Total",
						"Institution Name",
						"Institution Email",
						"Institution Address",
						"Member Full Name" ],
					[
						receipt.paymentMode.toUpperCase().trim(),
						receipt.receiptNo.toUpperCase().trim(),
						getDateFromTimeStamp(receipt.entryDate),
						receipt.total,
						receipt.institution.name.toUpperCase().trim(),
						receipt.institution.email.toLowerCase().trim(),
						receipt.institution.address.toUpperCase().replaceAll(",", ";").trim(),
						receipt.member.fullName.toUpperCase().trim(),
					]
				];
				 */
				let csvRowString = csvRows.map(row=>{
					return row.join(APP_CONFIG.CSV_DELIMITER);
				}).join("\n").toUpperCase();
				
				const blob = new Blob([csvRowString], { type: 'text/csv;charset=utf-8' }),
					link = document.createElement('a');
				// Set the download attribute with a filename
				link.download = singleReport.receiptNo+'.csv';
				// Create a URL for the Blob and set it as the href attribute
				link.href = window.URL.createObjectURL(blob);
				// Append the link to the body (it's invisible)
				document.body.appendChild(link);
				// Programmatically click the link to trigger the download
				link.click();
				// Remove the link after triggering the download
				document.body.removeChild(link);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Receipt fetching");
			});
		},
		loadBackgroundImage(){
			return new Promise(resolve=>{
				if(typeof this.receipt.institution.customReceiptTemplate!=='undefined'
					&& this.receipt.institution.customReceiptTemplate!==null){
					this.$api.get("/files/get/" + this.receipt.institution.customReceiptTemplate,
						{responseType: 'blob'})
						.then(response => {
							let theImage = URL.createObjectURL(response);
							let receiptTemplate = this.receipt.institution.customModel;
							this.receipt = {
								...this.receipt,
								institution: {
									...this.institution,
									customModel: theImage,
									customReceiptTemplate: theImage,
									receiptTemplate: receiptTemplate
								}
							}
							resolve(true);
						}).catch(ignored => {
						resolve(false);
						this.$root['showAlert']('warn', "Custom Receipt", "Failed to load custom receipt.");
					});
				}
				resolve(false);
			});
		},
		previewReceipt: function(receipt){
			let data = this.$root['addInstitutionId']({receiptCode : receipt.receiptNo});
			this.$api.post("/institution/get-single-donation", data).then(response=>{
				this.receipt = response.object;
				this.loadBackgroundImage().then(()=>{
					this.receipt = {
						...this.receipt,
						institution : {
							...this.receipt.institution,
							logo: this.$root['loadInstitutionLogo']()
						}
					};
					
					this.$store.commit("setLoading", true);
					setTimeout(()=>{
						this.downloadReceipt(this.receiptPageId);
					}, 2000);
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Receipt loading");
			});
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
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;width: 21cm;">');
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
	},
	watch: {
		pageType: function(newValue){
			this.resetForm(true);
		},
		isChurch: function(newValue){
			this.resetForm(true);
		}
	},
	props: {
		pageType: {
			type: String,
			required: true,
			default(){
				return "ADDRESS";
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		
	},
	beforeMount(){
		this.resetForm(true);
	}
}
</script>

<style scoped lang="scss">

</style>