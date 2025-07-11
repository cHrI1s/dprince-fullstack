<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog v-model:visible="shown"
	        :header="institutionLocal!==null
	            ? ('Change Subscription Plan : '+institutionLocal.name.toUpperCase())
	            : 'Change Subscription Plan'"
	        class="w-lg-50 w-md-75 w-100">
		<div class="d-block position-relative py-2">
			<Select class="w-100"
			        :options="plans"
			        placeholder="Select Plan."
			        optionLabel="label"
			        optionValue="value"
			        v-model="chosenPlan"/>
		</div>
		
		<template #footer>
			<div class="d-block position-relative">
				<Button label="Change Plan"
				        class="w-100"
				        severity="success"
				        @click="changePlan"
				        icon="pi pi-sort-alt change-icon"/>
			</div>
		</template>
	</Dialog>
</template>

<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import Select from "primevue/select";
export default {
	name: "SubscriptionPlanUpgrader",
	components: {Button, Dialog, Select},
	emits: ["close"],
	data(){
		return {
			visibleLocal: this.visible,
			plans: [],
			givenInstitution: this.institution
		}
	},
	computed: {
		institutionLocal: {
			get: function(){
				return this.givenInstitution;
			},
			set: function(newValue){
				this.givenInstitution = newValue;
			}
		},
		chosenPlan: {
			get: function(){
				return this.institutionLocal.subscriptionPlan;
			}, set: function(newValue){
				this.institutionLocal = {
					...this.institutionLocal,
					subscriptionPlan: newValue
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
	methods: {
		loadPlans: function(){
			if(this.institutionLocal!==null) {
				let institution = this.institutionLocal.institutionType === "CHURCH" ? 'CHURCH' : "GENERAL";
				this.$api.get(`/plan/get-all/${institution}`).then(response => {
					this.plans = response.map(singlePlan=>{
						return {
							label : singlePlan.name,
							value: singlePlan.id
						}
					});
				}).catch(error => {
					this.$root['handleApiError'](error, "Subscription Plans.");
				});
			}
		},
		changePlan: function(){
			if(this.chosenPlan===this.institution.subscriptionPlan){
				this.$root['showAlert']("warn", "Subscription Plan.", "Please select a different Plan.");
				return;
			}
			const DATA = {
				institutionId: this.institutionLocal.id,
				planId: this.chosenPlan
			};
			this.$api.post("/institution/upgrade-plan", DATA).then(response=>{
				this.$root['showAlert']("success", "Subscription Plan.", response.message);
				this.$emit("close", response.object);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Subscription Plan.");
			})
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
		institution: {
			type: [Object, null],
			required: true,
			default(){
				return null;
			}
		}
	},
	watch: {
		visible: function(newValue){
			this.shown = newValue;
		},
		institution: function(newValue){
			this.institutionLocal = newValue;
			this.loadPlans();
		}
	},
	beforeMount(){
		this.loadPlans();
	}
}
</script>

<style lang="scss">
	.change-icon{
		transform: rotate(90deg);
	}
</style>