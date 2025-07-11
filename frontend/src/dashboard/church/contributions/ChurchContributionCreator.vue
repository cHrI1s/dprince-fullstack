<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<div class="card w-100">
			<div class="card-header">
				<h3 class="fw-bolder my-0">
					Create
				</h3>
			</div>
			
			<div class="card-body pt-0">
				<div class="d-flex w-100"
				     :class="index>0 ? 'mt-4' : ''"
				     v-for="(input, index) in inputs"
				     :key="index">
					<div class="flex-grow-1">
						<InputText class="w-100"
						           v-model="input.name"
						           placeholder="Offering"/>
					</div>
					<div class="ms-2">
						<Button icon="pi pi-times"
						        :disabled="inputs.length===1"
						        @click="removeInput(index)"
						        severity="danger"/>
					</div>
				</div>
			</div>
			
			<div class="card-footer pt-0">
				<div class="row">
					<div class="col-6">
						<Button label="Add"
						        @click="addInput"
						        :disabled="updating"
						        severity="info"
						        icon="pi pi-plus"
						        class="w-100"/>
					</div>
					
					<div class="col-6">
						<Button label="Save"
						        @click="save"
						        :disabled="saveDisabled"
						        severity="success"
						        icon="pi pi-plus"
						        class="w-100"/>
					</div>
				</div>
			</div>
   
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
import InputText from "primevue/inputtext";

import {isEmpty} from "@/utils/AppStringUtils";
export default {
	name: "ChurchContributionCreator",
	components: {
		Button,
		InputText,
	},
	emits: ["save", "update"],
	computed: {
		saveDisabled: function(){
			let empty = false,
				i = 0,
				inputsCount = this.inputs.length;
			for(; i<inputsCount; i++) if(isEmpty(this.inputs[i].name)) if(!empty) empty = true;
			return empty;
		}
	},
	data(){
		return {
			inputs: this.contributions,
			updating: this.isUpdate
		}
	},
	methods: {
		addInput: function(){
			this.inputs.push({name: null});
		},
		removeInput: function(index){
			this.inputs.splice(index, 1);
		},
		save: function(){
			if(this.$root['isInstitutionSet']()) {
				if (this.inputs.length === 1) if (isEmpty(this.inputs[0].name)) return;
				let data = this.$root['addInstitutionId']({contributions: [...this.inputs]});
				this.$api.post("/contribution/save", data).then(response => {
					this.$emit(this.updating ? "update" : "save", response);
				}).catch(error => {
					this.$root['handleApiError'](error, "Contribution Save");
				});
			}
		}
	},
	watch: {
		contributions: function(newValue){
			this.inputs = newValue;
		},
		isUpdate: function(newValue){
			this.updating = newValue;
		}
	},
	props: {
		contributions: {
			required: true,
			type: Array,
			default(){
				return [{name: null}];
			}
		},
		isUpdate: {
			required: true,
			type: Boolean,
			default(){
				return false;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>