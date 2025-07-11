<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog :visible="visible"
	        :closable="false"
	        :modal="true"
	        :header="'Top Up'"
	        class="w-lg-50 w-md-75 w-100">
		
		<div class="row pt-1">
			<div class="col-md-6 mb-3">
				<FormLabel :required="false" label-text="Email"/>
				<InputNumber v-model="topUpModel.email"  class="w-100" placeholder="Email"/>
			</div>
			<div class="col-md-6 mb-3">
				<FormLabel :required="false"  label-text="Sms"/>
				<InputNumber v-model="topUpModel.sms"  class="w-100" placeholder="Sms"/>
			</div>
			<div class="col-md-6">
				<FormLabel :required="false" label-text="Whatsapp"/>
				<InputNumber  v-model="topUpModel.whatsapp" class="w-100" placeholder="Whatsapp"/>
			</div>
			<div class="col-md-6 mb-3">
				<FormLabel :required="false" label-text="Additional User"/>
				<InputNumber v-model="topUpModel.additionalUser"  class="w-100" placeholder="Additional User"/>
			</div>
			<div class="col-md-6">
				<FormLabel :required="false" label-text="Backup"/>
				<InputNumber v-model="topUpModel.backup"  class="w-100" placeholder="Backup"/>
			</div>
		</div>
		<template #footer>
			
				<Button
				        label="Close"
				        severity="danger"
				        icon="pi pi-times"
				        @click="$emit('close')"
				        class="w-100"/>
				<Button
				        label="save"
				        icon="pi pi-plus"
				        @click="saveTop"
				        class="w-100"/>
			
		</template>
	</Dialog>
</template>

<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import InputNumber from "primevue/inputnumber";
import FormLabel from "@/components/FormLabel.vue";
export default {
	name: "InstitutionDetails",
	methods: {
		saveTop: function(){
			if(this.topUpModel.sms!==null
				|| this.topUpModel.email!==null
				|| this.topUpModel.backup!==null
				|| this.topUpModel.whatsapp!==null
				|| this.topUpModel.additionalUser){
				let data = this.$root['addInstitutionId']({...this.topUpModel});
				this.$api.post("/institution/save-topup" ,data).then(response=>{
					this.$root['showAlert']("success", "Top Up", response.message);
					this.topUpModel = {
							sms            :  null,
							email          :  null,
							whatsapp       :  null,
							additionalUser :  null,
							backup         :  null
					};
					this.$emit('close', response.object);
				}).catch(error=>{
					this.$root['handleApiError'](error, "Top Up")
				})
			}else{
				this.$root['showAlert']('warn', 'Top Up' , "At least one must be given.");
			}
			
			
		}
	},
	components: {
		FormLabel,
		Button,
		Dialog,
		InputNumber
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
			institutionLocal: this.institution,
			topUpModel : {
				sms            :  null,
				email          :  null,
				whatsapp       :  null,
				additionalUser :  null,
		        backup         :  null
			}
		}
	},
	computed: {
	
		
	},
}
</script>

<style scoped lang="scss">

</style>