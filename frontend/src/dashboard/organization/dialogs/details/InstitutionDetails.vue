<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog v-model:visible="shown"
	        :closable="false"
	        :modal="true"
	        :header="'Institution Details'"
	        class="w-lg-50 w-md-75 w-100">
		<div class="d-block position-relative">
			<table>
				<tbody>
					<tr>
						<td class="py-1 px-2">Name</td>
						<td class="py-1 px-2 fw-bolder text-uppercase">{{ institution.name }}</td>
					</tr>
					<tr v-if="institution.parentInstitutionName!==null">
						<td class="py-1 px-2">Main Branch Name</td>
						<td class="py-1 px-2 fw-bolder text-uppercase">{{ institution.parentInstitutionName }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Organization Base Code</td>
						<td class="py-1 px-2 fw-bolder">{{ institution.baseCode }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Email</td>
						<td class="py-1 px-2 fw-bolder">{{ institution.email }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Phone Number</td>
						<td class="py-1 px-2 fw-bolder">{{ institution.phone }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Subscription Plan</td>
						<td class="py-1 px-2 fw-bolder text-uppercase">{{ subscriptionPlan.name }}</td>
					</tr>
					<tr v-if="subscriptionPlan.emails!==0">
						<td class="py-1 px-2">No. of. Emails</td>
						<td class="py-1 px-2 fw-bolder">
							{{ institution.emails===null ? 0 : institution.emails }} out of
							{{ subscriptionPlan.emails }}
						</td>
					</tr>
					<tr v-if="subscriptionPlan.smses!==0">
						<td class="py-1 px-2">No. of. Sms</td>
						<td class="py-1 px-2 fw-bolder">
							{{ institution.smses===null ? 0 : institution.smses }} out of
							{{ subscriptionPlan.smses }}
						</td>
					</tr>
					<tr v-if="subscriptionPlan.whatsapp!==0">
						<td class="py-1 px-2">No. of. WhatsApp</td>
						<td class="py-1 px-2 fw-bolder">{{ subscriptionPlan.whatsapp }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">No. of. Members</td>
						<td class="py-1 px-2 fw-bolder">{{ subscriptionPlan.members }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">No. of. Admins</td>
						<td class="py-1 px-2 fw-bolder">{{ subscriptionPlan.admins }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">No. of. Families</td>
						<td class="py-1 px-2 fw-bolder">{{ subscriptionPlan.families }}</td>
					</tr>
					<tr v-if="subscriptionPlan.churchBranches!==0">
						<td class="py-1 px-2">No. of. Branches</td>
						<td class="py-1 px-2 fw-bolder">{{ subscriptionPlan.churchBranches }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Subscriptions</td>
						<td class="py-1 px-2 fw-bolder text-uppercase">{{ institution.subscription }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Address</td>
						<td class="py-1 px-2 fw-bolder text-uppercase">{{ institution.address }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Creation</td>
						<td class="py-1 px-2 fw-bolder">{{ getDateFromTimeStamp(institution.creationDate) }}</td>
					</tr>
					<tr>
						<td class="py-1 px-2">Deadline</td>
						<td class="py-1 px-2 fw-bolder" v-if="institution.subscription!=='LIFETIME'">{{ getDateFromTimeStamp(institution.deadline) }}</td>
						<td class="py-1 px-2 fw-bolder" v-else>No deadline</td>
					</tr>
				</tbody>
			</table>
		</div>
		<template #footer>
			<div class="pt-1 d-block position w-100">
				<Button v-if="closeButton===null"
				        label="Close"
				        severity="danger"
				        icon="pi pi-times"
				        @click="$emit('close')"
				        class="w-100"/>
				<Button v-else
				        :label="closeButton.label"
				        :severity="closeButton.severity"
				        :icon="closeButton.icon"
				        @click="$emit('close')"
				        class="w-100"/>
			</div>
		</template>
	</Dialog>
</template>

<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import {getDateFromTimeStamp} from "../../../../utils/AppFx";
export default {
	name: "InstitutionDetails",
	methods: {getDateFromTimeStamp},
	components: {
		Button,
		Dialog
	},
	emits: ["close"],
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
		},
		closeButton: {
			type: [Object, null],
			required: false,
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
			// this.loadPlans();
		}
	},
	data(){
		return {
			visibleLocal: this.visible,
			institutionLocal: this.institution
		}
	},
	computed: {
		subscriptionPlan: function(){
			if(this.institution.institutionPlan!==null) return this.institution.institutionPlan;
			return this.institution.plan
		},
		shown : {
			get: function(){
				return this.visibleLocal;
			},
			set: function(newValue){
				this.visibleLocal = newValue;
				if(!newValue) this.$emit("close");
			}
		}
	},
}
</script>

<style scoped lang="scss">

</style>