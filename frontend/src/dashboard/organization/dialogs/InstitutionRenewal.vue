<template>
	<Dialog :visible="isShown"
	        :header="institutionLocalName!==null
	            ? (institutionLocalName.toUpperCase() + ' plan renewal') : 'Plan renewal'"
	        class="w-lg-50 w-md-75 w-100" :closable="false">
		
		<div class="row position-relative mb-4">
			<div class="col-6">
				<div class="d-block position-relative fw-bolder">Current Expiration Date</div>
				<div class="d-block position-relative">{{ getDateFromTimeStamp(institutionLocal.deadline) }}</div>
			</div>
			
			<div class="col-6 text-end">
				<div class="d-block position-relative fw-bolder">Next Expiration Date</div>
				<div class="d-block position-relative" v-if="chosenPlan!=='LIFETIME'">{{ newDeadline }}</div>
				<div class="d-block position-relative" v-else>No Expiration</div>
			</div>
		</div>
		
		<div class="d-block position-relative py-2">
			<Select class="w-100"
			        :options="subscriptions"
			        placeholder="Select Plan."
			        optionLabel="label"
			        optionValue="value"
			        v-model="chosenPlan"/>
		</div>
		
		<template #footer>
			<Button label="Cancel"
			        class="w-100"
			        severity="danger"
			        @click="$emit('close')"
			        icon="pi pi-times"/>
			
			<Button label="Renew"
			        class="w-100"
			        severity="success"
			        @click="renewInstitutionSubscription"
			        icon="pi pi-sort-alt change-icon"/>
		</template>
	</Dialog>
</template>
<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import Select from "primevue/select";
import {SUBSCRIPTION_PLANS} from "@/dashboard/organization/Organization";
import {getDateFromTimeStamp} from "@/utils/AppFx";
export default{
	name : 'InstitutionRenewal',
	components:{
		Dialog,
		Button,
		Select
	},
	props:{
		isShown:{
			type:Boolean,
			default(){
				return false;
			}
		},
		institution: {
			type: [Object, null],
			default(){
				return null
			}
		}
	},
	emits: ["close"],
	data(){
		return {
			subscriptions:[...SUBSCRIPTION_PLANS],
			plans: [],
			givenRow: this.institution
		}
	},
	computed: {
		newDeadline: function(){
			let institution = this.institutionLocal;
			let newDeadline = null;
			if(institution!==null){
				newDeadline = new Date(institution.deadline);
				let chosenPlan = this.chosenPlan;
				switch (chosenPlan){
					case "WEEKLY":
						newDeadline.setDate(newDeadline.getDate()+7);
						break;
					
					case "MONTHLY":
						newDeadline.setMonth(newDeadline.getMonth()+1);
						break;
					
					case "QUARTER":
						newDeadline.setMonth(newDeadline.getMonth()+3);
						break;
					
					case "SEMESTRAL":
						newDeadline.setMonth(newDeadline.getMonth()+6);
						break;
						
					case "ANNUAL" :
						newDeadline.setFullYear(newDeadline.getFullYear()+1);
						break;
				}
				return getDateFromTimeStamp(newDeadline);
			}
			return newDeadline;
		},
		institutionLocal: {
			get: function(){
				return this.givenRow;
			},
			set: function(newValue){
				this.givenRow = newValue;
			}
		},
		institutionLocalName: function(){
			if(this.institutionLocal===null) return null;
			return this.institutionLocal.name;
		},
		chosenPlan: {
			get: function(){
				return this.institutionLocal.subscription;
			}, set: function(newValue){
				this.institutionLocal = {
					...this.institutionLocal,
					subscription: newValue
				};
			}
		},
		
		
		shown : {
			get: function(){
				return this.visibleLocal;
			},
			set: function(newValue){
				this.visibleLocal = newValue;
				if(!newValue) this.$emit("close", null);
			}
		}
	},
	watch: {
		
		institution: function(newValue){
			this.institutionLocal = newValue;
		}
	},
	methods: {
		getDateFromTimeStamp,
		renewInstitutionSubscription: function(){
			const DATA = {
				institutionId: this.institutionLocal.id,
				subscription: this.chosenPlan,
				planId :this.institutionLocal.subscriptionPlan
			};
			this.$api.post("/institution/renew-subscription", DATA).then(response=>{
				this.$root['showAlert']("success", "Institution Renewal.", response.message);
				this.$emit("close", response.object);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Institution Renewal.");
			})
		},
	},
}

</script>


<style scoped lang="scss">

</style>