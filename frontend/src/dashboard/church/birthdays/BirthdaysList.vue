<template>
	<div class="d-block position-relative">
		<DashboardCard :header-title="title"
		               :refreshButton="false">
			<PaginatedTable :table-data="tableData"
			                :multi-select="multiSelect"
			                :has-query-input="false"
			                :table-headers="tableHeaders"
			                :paginated="true"
			                :extra-inputs="extraInputs"
			                :date-search="false"
			                :search-model="searchModel"
			                @select="selectItem"
			                @next="loadSidePage(true)"
			                @previous="loadSidePage(false)"
			                @search="makeSearch"/>
			
			
			<div class="row mt-4" v-if="tableData.content.length>0">
				<div class="col-md-4">
					<Button label="Download Excel"
					        class="w-100"
					        icon="pi pi-download"
					        severity="warn"
					        @click="mailMerge"/>
				</div>
				
				<div class="col-md-4">
					<Button :label="!multiSelect ? 'Multi Select' : 'Deselect'"
					        class="w-100"
					        :icon="multiSelect ? 'pi pi-times' : 'pi pi-check'"
					        severity="info"
					        @click="toggleSelect"/>
				</div>
				
				<div class="col-md-4">
					<Button :label="'Communicate'"
					        class="w-100"
					        icon="pi pi-share"
					        severity="info"
					        @click="communicate"/>
				</div>
			</div>
		</DashboardCard>
	</div>
</template>


<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import Button from "primevue/button";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {AVAILABLE_LANGUAGES} from "@/dashboard/organization/Organization";
import {getDateFromTimeStamp, renameKey} from "@/utils/AppFx";

export default {
	name: "BirthdaysList",
	components: {PaginatedTable, DashboardCard, Button},
	data(){
		return {
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
			multiSelect: false,
			selectedItems: new Set()
		}
	},
	computed: {
		title: function(){
			return this.isBirthday ? 'Birthdays' : 'Marriages Anniversaries';
		},
		extraInputs: function(){
			return [
				{ type: "SELECT", model: "multiple", placeholder: "Select", options: this.selectOptions, styleClass:'col-md-6 mb-4'},
				{ type: "DATE", model: "start", placeholder: "Date Start", format: "dd-MM", styleClass:'col-md-8 mb-4'},
				{ type: "DATE", model: "end", placeholder: "Date Start", format: "dd-MM", styleClass:'col-md-8 mb-4', shownIf: {model: "multiple", values: [true]}},
			].map(input=>{
				if(this.searchModel.multiple) input.styleClass = "col-md-4 mb-4";
				else input.styleClass = "col-md-6 mb-4";
				return input;
			});
		},
		isInstitutionChurch: function(){ return this.isChurch; },
		tableHeaders: function(){
			let headers = {
				"Full name":{
					rows:[
						{ type: "StringArray", attribute: ["firstName", "lastName"]},
						{ type: "Date", attribute: this.isBirthday ? "dob" : 'dom', label: this.isBirthday ? "DOB" : "DOM"},
						{ type: "String", attribute: "email"},
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
						{ label:"Language", type: "Select", attribute: "language", options: [...AVAILABLE_LANGUAGES]},
					].filter(row=>{
						if(this.isInstitutionChurch) return row.attribute!=="subscription";
						return true
					})
				},
				"Created on":{
					rows:[
						{ type: "Date", attribute: "creationDate"}
					]
				}
			};
			if(this.isInstitutionChurch) headers = renameKey(headers, "Partner Code", "Member Code");
			return headers;
		},
		isListBirthDay: function(){
			return this.isBirthday;
		}
	},
	methods: {
		communicate: function(){
			this.$store.commit("setPeopleToCommunicate", [...this.selectedItems]);
			let url = this.isInstitutionChurch
				? "/church/communication"
				: "/organizations/communication";
			this.$router.push(url);
		},
		toggleSelect: function(){
			if(this.multiSelect) {
				this.selectedItems = new Set();
				this.tableData = {
					...this.tableData,
					content: this.tableData.content.map(item=>{
						return {
							...item,
							isItemSelected: false
						}
					})
				}
				this.multiSelect = false;
			} else {
				this.multiSelect = true;
			}
		},
		loadSidePage: function(next = true){
			let page = next ? this.searchModel.page+1 : this.searchModel.page-1;
			if(page>this.tableData.totalPages || page<=0) return;
			this.searchModel = {
				...this.searchModel,
				page: page
			}
			this.makeSearch();
		},
		makeSearch: function(){
			let data = this.$root['addInstitutionId']({...this.searchModel});
			data = {
				...data,
				birthdayType: this.isBirthday ? "BIRTHDAY" : "MARRIAGE"
			}
			if(!data.multiple) data.end;
			this.$api.post("/institution/list-birthdays", data).then(response=>{
				this.tableData = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, this.title+" listing.");
			});
		},
		mailMerge: function(){
			this.$store.commit("setLoading", true);
			let contentRows = Object.values(
				this.tableData.content.map(row=>{
					return [
						row.firstName +" "+row.lastName,
						getDateFromTimeStamp(row.dob, false),
						row.phone,
						row.email,
						row.district
					];
				})
			);
			
			
			let csvRows = [
				["Full name",
					"DOB",
					"Phone",
					"Email",
					"District"],
				...contentRows
			];
			let csvRowString = csvRows.map(row=>{
				return row.join(",")
			}).join("\n");
			
			//this.$store.commit("setLoading", false);
			const blob = new Blob([csvRowString], { type: 'text/csv' }),
				link = document.createElement('a');
			// Set the download attribute with a filename
			let fileName = getDateFromTimeStamp(new Date(), true)+'-Birthdays.csv';
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
		selectItem: function(item){
			if(this.selectedItems.has(item)) this.selectedItems.delete(item);
			else this.selectedItems.add(item);
		},
		refresh: function(resetModel=true){
			if(resetModel) {
				this.searchModel = {
					multiple: false,
					start: null,
					end: null,
					size: 5,
					page: 1
				};
			}
			this.tableData = {...PAGE_MODEL};
		}
	},
	watch:{
		isChurch: function(ignored){ this.refresh() },
		isBirthday: function(ignored){ this.refresh(false) }
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		isBirthday: {
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