<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard :header-title="donatingMember!==null ? donatingMember.fullName.toUpperCase()+' is contributing...' : 'No Member selected'"
	               :refresh-button="true"
	               @refresh="initialize">
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
						<div class="col-md-4 col-4 mb-4">
							<FormLabel label-text="Offering"
							           :required="true"/>
							<Select v-model="donation.contributionId"
							        placeholder="Offering"
							        optionLabel="name"
							        optionValue="id"
							        :options="churchContributions"
							        class="w-100"/>
						</div>
						<div class="col-md-4 col-4 mb-4">
							<FormLabel label-text="Amount"
							           :required="true"/>
							<InputNumber v-model="donation.amount"
							             :use-grouping="false"
							             inputClass="w-100"
							             placeholder="Amount"
							             class="w-100"/>
						</div>
						
						<div class="col-md-4 mb-4">
							<Button label="Remove"
							        severity="danger"
							        icon="pi pi-trash"
							        :disabled="index===0"
							        @click="toggleAddCategory(false, index)"
							        class="w-100"/>
						</div>
					</div>
					
					<div class="w-100 mb-4 px-2">
						<Button label="Add Contribution"
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
					           class="w-100 fw-bolder"/>
				</div>
				
				<div class="col-md-6 mb-4">
					<FormLabel label-text="Reference No"
					           :required="false"/>
					<InputText v-model="model.referenceNo"
					           placeholder="Reference No"
					           class="w-100 fw-bolder"/>
				</div>
				<div class="col-12 mb-4">
					<FormLabel label-text="Remarks"
					           :required="false"/>
					<Textarea v-model="model.remarks"
					          placeholder="Remarks"
					          class="w-100 fw-bolder"/>
				</div>
				
				<div class="col-12 mb-4">
					<FormLabel label-text="Credit Account"
					           :required="false"/>
					<Select v-model="model.creditAccount"
					        :options="creditAccounts"
					        placeholder="Credit Account"
					        optionLabel="label"
					        optionValue="value"
					        class="w-100 fw-bolder"/>
				</div>
				
				<div class="col-12 mb-4">
					<FormLabel label-text="Donation date"
					           :required="false"/>
					<DatePicker v-model="model.entryDate"
					            dateFormat="dd/mm/yy"
					            :max-date="maxDate"
					            placeholder="Donation date"
					            class="w-100"/>
				</div>
				
				<div class="col-12 mb-4">
					<Button label="Save"
					        icon="pi pi-save"
					        severity="success"
					        @click="save"
					        class="w-100"/>
				</div>
			</div>
		</div>
	</DashboardCard>
</template>

<script>
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Select from "primevue/select";
import DatePicker from "primevue/datepicker";
import InputNumber from "primevue/inputnumber";
import Textarea from "primevue/textarea";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {CATEGORY_IN_DONATION, DONATION_MODEL, PAYMENT_MODES} from "@/dashboard/organization/dialogs/donation/donation";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import FormLabel from "@/components/FormLabel.vue";
import {SUBSCRIPTION_PLANS} from "@/dashboard/organization/Organization";

export default {
	name: "ContributionMaker",
	components: {
		InputText,
		FormLabel,
		Button,
		DatePicker,
		Select,
		InputNumber,
		Textarea,
		DashboardCard,
	},
	computed: {
		donatingMember: function(){
			return this.$store.getters.getTmpInstitutionMember;
		},
		contributions: {
			get: function(){
				return this.contributionsLocal;
			}, set: function(newValue){
				this.contributionsLocal = newValue;
			}
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
	},
	data(){
		return {
            paymentMode : null,
			entryDate: null,
			accountId: null,
			creditAccounts: [],
			churchContributions: [],
			selectedContributionsIds : [null],
			model: {...DONATION_MODEL},
			paymentModes: [...PAYMENT_MODES],
			donation: null,
			maxDate: new Date(),
			subscriptions: [...SUBSCRIPTION_PLANS]
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
				};
			} else {
				if(indexToRemove!==null) {
					this.model = {
						...this.model,
						donations: this.model.donations.filter((category, index) => index !== indexToRemove)
					};
				}
			}
		},
		loadAccounts: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/institution/get-accounts", data).then(response => {
				this.creditAccounts = response.map(account=>{
					return {
						label: account.name.toUpperCase(),
						value: account.id
					}
				});
			}).catch(error => {
				this.$root['handleApiError'](error, "Credit Accounts Loadings");
			});
		},
		loadContributions: function(){
			let url = "/contribution/get";
			if(this.institution!==null) url += "/"+this.institution.id;
			this.$api.get(url).then(response=>{
				this.churchContributions = response.map(contribution=>{
					return {
						id: contribution.id,
						name: contribution.name.toUpperCase()
					}
				});
				this.$root['showAlert']("success", "Contributions Loading.", "Loaded");
			}).catch(error=>{
				this.$root['handleApiError'](error,  "Contributions Loading.");
			});
		},
        saveContributions : function(){
            return new Promise((resolve) => {
				if(this.$store.getters.getTmpInstitutionMember!==null) {
					let data = this.$root['addInstitutionId']({...this.model});
					data = {
						...data,
						memberId: this.$store.getters.getTmpInstitutionMember.id
					};
					this.$api.post("/contribution/contribute", data).then(response => {
						this.model = {...DONATION_MODEL};
						this.$root['showAlert'](typeof response.object!=='undefined'
							? "success" : 'error'
							, "Contributions Save.", response.message);
						resolve(typeof response.object!=='undefined');
					}).catch(error => {
						this.$root['handleApiError'](error, "Contributions Save.");
						resolve(false);
					});
				} else {
					this.$root['showAlert']("warn", "Contributions Save.", "No donator/giver set.");
				}
            })
            
        },
		save: function(){
            this.saveContributions().then(result=>{
                if(result) this.$router.push("/church/receipt-generation");
            });
		},
		initialize: function(){
			if(this.$root['isInstitutionSet']()) {
				this.loadContributions();
				this.loadAccounts();
			}
		},
	},
	beforeMount(){
		this.initialize();
	},
}
</script>

<style scoped lang="scss">

</style>