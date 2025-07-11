<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog :visible="donatorShown"
	        class="w-md-75 w-lg-50 w-100"
	        :close-on-escape="false"
	        :header="donatorFullName+' is donating.'"
	        :modal="true"
	        :closable="false">
		<div class="d-block mt-4">
			<div class="row">
				<div class="col-12 mb-4">
					<FormLabel label-text="Payment"
					           :required="true"/>
					<Select v-model="model.paymentMode"
					        placeholder="Payment"
					        optionLabel="label"
					        optionValue="value"
					        :options="paymentModes"
					        class="w-100"/>
				</div>
				<div class="col-12 mx-0 px-0">
					<div class="row mx-0 px-0 align-items-end"
					     v-for="(donation, index) in model.donations"
					     :key="'category_'+index">
						<div class="col-md-6 col-4 mb-4">
							<FormLabel label-text="Category"
							           :required="true"/>
							<Select v-model="donation.categoryId"
							        placeholder="Category"
							        optionLabel="label"
							        optionValue="value"
							        :options="categories"
							        class="w-100"/>
						</div>
						<div class="col-md-6 col-4 mb-4">
							<FormLabel label-text="Amount"
							           :required="true"/>
							<InputNumber v-model="donation.amount"
							             :use-grouping="false"
							             inputClass="w-100"
							             placeholder="Amount"
							             class="w-100"/>
						</div>
						<div class="col-md-6 col-4 mb-4">
							<FormLabel label-text="Subscriptions"
							           :required="true"/>
							<Select v-model="donation.subscription"
							        :options="subscriptions"
							            optionLabel="label"
							            optionValue="value"
							             inputClass="w-100"
							             placeholder="Subscriptions"
							             class="w-100"/>
						</div>
						
						<div class="col-md-6 col-4 mb-4">
							<Button label="Remove"
							        severity="danger"
							        icon="pi pi-trash"
							        :disabled="index===0"
							        @click="toggleAddCategory(false, index)"
							        class="w-100"/>
						</div>
					</div>
					
					<div class="w-100 mb-4">
						<Button label="Add New Category"
						        severity="info"
						        icon="pi pi-plus"
						        @click="toggleAddCategory(true)"
						        class="w-100"/>
					</div>
				</div>
				
				<div class="col-md-12 mb-4">
					<FormLabel label-text="Reference Bank"
					           :required="true"/>
					<InputText v-model="model.bankReference"
					           placeholder="Reference Bank"
					           inputClass="w-100"
					           icon="pi pi-plus fw-bolder"
					           class="w-100 fw-bolder"/>
				</div>
				
				
				<div class="col-md-6 mb-4">
					<FormLabel label-text="Reference Acct. No"
					           :required="false"/>
					<InputText v-model="model.referenceAccount"
					        placeholder="Reference Acct. No"
					           class="w-100 fw-bolder text-uppercase"/>
				</div>
				
				<div class="col-md-6 mb-4">
					<FormLabel label-text="Reference No"
					           :required="false"/>
					<InputText v-model="model.referenceNo"
					           placeholder="Reference No"
					           class="w-100 fw-bolder text-uppercase"/>
				</div>
				<div class="col-12 mb-4">
					<FormLabel label-text="Remarks"
					           :required="false"/>
					<Textarea v-model="model.remarks"
					          placeholder="Remarks"
					          class="w-100 fw-bolder text-uppercase"/>
				</div>
				
				<div class="col-12 mb-4">
					<FormLabel label-text="Credit Account"
					           :required="false"/>
					<Select v-model="model.creditAccount"
					        :options="accounts"
					        placeholder="Credit Account"
					        optionLabel="label"
					        optionValue="value"
					        class="w-100 fw-bolder"/>
				</div>
				
				<div class="col-12 mb-4">
					<FormLabel label-text="Donation date"
					           :required="false"/>
					<DatePicker v-model="model.entryDate"
					            :maxDate="today"
					            dateFormat="dd/mm/yy"
					            placeholder="Donation date"
					            class="w-100"/>
				</div>
				
				<div class="col-6 mb-4">
					<Button label="Close"
					        icon="pi pi-times"
					        severity="danger"
					        @click="$emit('close')"
					        class="w-100"/>
				</div>
				
				<div class="col-6 mb-4">
					<Button label="Save"
					        icon="pi pi-save"
					        severity="success"
					        @click="saveDonation"
					        class="w-100"/>
				</div>
			</div>
		</div>
	</Dialog>
</template>

<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import DatePicker from "primevue/datepicker";
import Select from "primevue/select";
import InputNumber from "primevue/inputnumber";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import {CATEGORY_IN_DONATION, DONATION_MODEL, PAYMENT_MODES} from "@/dashboard/organization/dialogs/donation/donation";
import FormLabel from "@/components/FormLabel.vue";
import {SUBSCRIPTION_PLANS} from "@/dashboard/organization/Organization";

export default {
	name: "DonateDialog",
	components: {Button, DatePicker, FormLabel, Dialog, InputText, InputNumber, Textarea, Select},
	emits: ['close'],
	computed: {
		donatorFullName: function(){
			if(typeof this.donator.fullName==='undefined') return "";
			return this.donator.fullName.toUpperCase();
		},
		form : function(){
			let output = [
				{
					label: "Payment Mode",
					model: "paymentMode",
					styleClass: "col-12 mb-4",
					type: "SELECT",
					options: [...PAYMENT_MODES]
				},
				{
					label: "Category",
					model: "category",
					styleClass: "col-md-5 col-4 mb-4",
					type: "SELECT",
					options: this.categories
				},
				{label: "Amount", model: "amount", styleClass: "col-md-5 col-4 mb-4", type: "NUMBER", options: this.categories},
				{label: "Add", model: null, styleClass: "col-md-2 col-4 mb-4", type: "BUTTON", icon:"pi pi-plus fw-bolder",
					click: "removeCategories"},
				{label: "Reference Bank", model: "bankReference", required: false, styleClass: "col-md-12 mb-4", type: "TEXT"},
				{label: "Reference Acct. No", model: "referenceAccount", required: false, styleClass: "col-md-6 mb-4", type: "TEXT"},
				{label: "Reference No", model: "referenceNo", required: false, styleClass: "col-md-6 mb-4", type: "TEXT"},
				{label: "Remarks", model: "remarks", required: false, styleClass: "col-md-12 mb-4", type: "TEXTAREA"},
			];
			if(this.accounts.length>0){
				output.push({
					label: "Credit Account",
					model: "creditAccount",
					styleClass: "col-12 mb-4",
					required: false,
					type: "SELECT",
					options: [...this.accounts]
				});
			}
			output = [
				...output,
				{label: "Donation Date",
					model: "entryDate",
					required: false,
					styleClass: "col-md-12 mb-4",
					type: "CALENDAR", max:this.today},
			]
			return output;
		},
		donator: function(){
			return this.member
		}
	},
	data(){
		return {
			today : new Date(),
			donatorShown: this.visible,
			okButton: {
				icon: "pi pi-save",
				label: "Save"
			},
			cancelButton: {
				icon: "pi pi-times",
				label: "Cancel",
				event: 'close'
			},
			model: {...DONATION_MODEL},
			paymentModes: [...PAYMENT_MODES],
			subscriptions : [...SUBSCRIPTION_PLANS],
			categories: [],
			accounts: []
		}
	},
	methods: {
		toggleAddCategory: function(isAdd=true, indexToRemove=null){
			if(isAdd) {
				this.model = {
					...this.model,
					donations: [
						...this.model.donations,
						{...CATEGORY_IN_DONATION}
					]
				}
			} else {
				if(indexToRemove!==null) {
					this.model = {
						...this.model,
						donations: this.model.donations.filter((category, index) => index !== indexToRemove)
					}
				}
			}
		},
		loadAccounts: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/institution/get-accounts", data).then(response => {
				this.accounts = response.map(account=>{
					return {
						label: account.name.toUpperCase(),
						value: account.id
					}
				});
			}).catch(error => {
				this.$root['handleApiError'](error, "Credit Accounts Loadings");
			});
		},
		loadCategories: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post(`/category/get`, data).then(response=>{
				this.categories = response.map(singleCategory=>{
					return {
						label: singleCategory.name,
						value: singleCategory.id
					}
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donation");
			});
		},
		saveDonation: function(){
			let form = this.$root['addInstitutionId']({
				...this.model,
				memberId: this.member.id
			});
			this.$api.post("/institution/make-donation", form).then(response=>{
				this.model = {
					...DONATION_MODEL,
					donations : [{
						categoryId      : null,
						amount          : null,
						subscription    : null,
					}],
				};
				this.$root['showAlert'](typeof response.object!=='undefined' ? "success" : 'error',
					"Donation",
					response.message);
				
				if(typeof response.object!=='undefined') this.$emit("close", true);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Donation");
			});
		},
		initialize: function(){
			this.model = {...DONATION_MODEL};
			this.loadAccounts();
			this.loadCategories();
		}
	},
	watch: {
		visible: function(newValue){
			this.donatorShown = newValue;
			if(newValue) {
				this.model = {
					...DONATION_MODEL,
					donations: [{
						categoryId: null,
						amount: null,
						subscription: null,
					}],
				};
			}
		}
	},
	props: {
		visible: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		member: {
			type: [Object, null],
			required: true,
			default(){
				return {};
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">

</style>