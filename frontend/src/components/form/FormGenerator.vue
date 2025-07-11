<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="row">
		<div v-for="(form, index) in shownForm"
		     :key="index"
		     :class="form.styleClass">
			<div class="d-block position-relative">
				<FormLabel :label-text="form.label"
				           :required="(typeof form.required!=='undefined') ? form.required : true"/>
				
				
				<InputText v-if="form.type==='TEXT'"
				           v-model="generator[form.model]"
				           :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				           :placeholder="form.label"
				           class="w-100"
				           :class="typeof form.inputClass!=='undefined' ? form.inputClass : ''"/>
				
				<InputNumber v-if="form.type==='NUMBER'"
				             inputId="withoutgrouping" :useGrouping="false"
				             v-model="generator[form.model]"
				             :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				             :placeholder="form.label"
				             class="w-100"/>
				
				<Password v-if="form.type==='PASSWORD'"
				          v-model="generator[form.model]"
				          toggleMask
				          :inputProps="{ autocomplete: 'new-password' }"
				          :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				          :placeholder="form.label"
				          :feedback="form.feedback"
				          :input-class="(typeof form.inputClass!=='undefined' ? form.inputClass : '')+ ' w-100'"
				          class="w-100"
				          :class="typeof form.inputClass!=='undefined' ? form.inputClass : ''"/>
				
				<DatePicker v-if="form.type==='CALENDAR' && (typeof form.max!=='undefined')"
				            v-model="generator[form.model]"
				            dateFormat="dd/mm/yy"
				            :maxDate="form.max"
				            :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				            :placeholder="form.label"
				            class="w-100"/>
				
				<DatePicker v-if="form.type==='CALENDAR' && (typeof form.max==='undefined')"
				            v-model="generator[form.model]"
				            dateFormat="dd/mm/yy"
				            :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				            :placeholder="form.label"
				            class="w-100"/>
				
				<Select v-if="form.type==='SELECT'"
				        v-model="generator[form.model]"
				        :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				        :options="form.options"
				        optionLabel="label"
				        optionValue="value"
				        :placeholder="form.label"
				        class="w-100"/>
				
				<Select v-if="form.type==='COUNTRY'"
				        v-model="generator[form.model]"
				        :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				        :options="countries"
				        optionLabel="label"
				        optionValue="value"
				        :placeholder="form.label"
				        class="w-100"/>
				
				<MultiSelect v-if="form.type==='MULTISELECT'"
				             v-model="generator[form.model]"
				             :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				             :options="form.options"
				             optionLabel="label"
				             optionValue="value"
				             :placeholder="form.label"
				             class="w-100"/>
				
				
				<Button v-if="form.type==='BUTTON'"
				        :label="form.label"
				        @click="$emit('customEvent', form.click)"
				        :icon="form.icon"
				        severity="info"
				        class="w-100"/>
				
				<Textarea v-if="form.type==='TEXTAREA'"
				          v-model="generator[form.model]"
				          :disabled="(typeof form.disabled)!=='undefined' ? form.disabled : false"
				          autoResize
				          :placeholder="form.label"
				          class="w-100"
				          :class="typeof form.inputClass!=='undefined' ? form.inputClass : ''"/>
				
				
				<div class="row"
				     v-if="form.type==='PHONE_NUMBER'">
					<div class="col-md-4 col-4">
						<Select :options="countriesPhoneCodes"
						        optionLabel="label"
						        optionValue="value"
						        placeholder="Code"
						        v-model="generator[form.model[0]]"
						        class="w-100"/>
					</div>
					<div class="col-md-8 col-8">
						<InputNumber inputId="withoutgrouping"
						             :useGrouping="false"
						             :placeholder="form.placeholder"
						             :min="(typeof form.min==='undefined') ? null : form.min"
						             :max="(typeof form.max==='undefined') ? null : form.max"
						             v-model="generator[form.model[1]]"
						             class="w-100"/>
					</div>
				</div>
			</div>
		</div>
		
		<div class="col-6 mb-4"
		     v-if="cancelButton!==null">
			<Button :label="cancelButton.label"
			        @click="$emit(cancelButton.event, generator)"
			        :icon="cancelButton.icon"
			        :disabled="btnDisabled"
			        severity="danger"
			        class="w-100"/>
		</div>
		
		<div class="mb-4"
		     :class="cancelButton!==null ? 'col-6' : 'col-12'">
			<Button :label="okButton.label"
			        @click="$emit('click', generator)"
			        :icon="okButton.icon"
			        severity="success"
			        class="w-100"/>
		</div>
	</div>
</template>

<script>
import FormLabel from "@/components/FormLabel.vue";
import Button from "primevue/button";
import DatePicker from "primevue/datepicker";
import InputText from "primevue/inputtext";
import InputNumber from "primevue/inputnumber";
import MultiSelect from "primevue/multiselect";
import Select from "primevue/select";
import Textarea from "primevue/textarea";
import Password from "primevue/password";
import {getCountries} from "@/dashboard/utils/countries";

export default {
	name: "FormGenerator",
	components: {
		FormLabel,
		Button,
		DatePicker,
		InputText,
		InputNumber,
		MultiSelect,
		Select,
		Textarea,
		Password
	},
	emits: ["click", "close", "customClick", "customEvent"],
	data(){
		return {
			uiForm: this.form,
			formModel: this.model
		}
	},
	methods: {
		getCountries
	},
	watch: {
		model: function(newValue){
			this.formModel = newValue;
		}
	},
	computed: {
		shownForm : function(){
			return this.form.filter(singleForm=>{
				if(typeof singleForm.shownIf==="undefined") return true;
				let model = singleForm.shownIf.attribute,
					value = singleForm.shownIf.equals;
				let shown = this.generator[model]===value;
				if(!shown) this.generator[singleForm.model] = null;
				return shown;
			});
		},
		generator : {
			get : function(){
				return this.formModel;
			},
			set: function(newValue){
				this.formModel = newValue;
			}
		},
		countriesPhoneCodes: function(){
			return this.getCountries().map(country=>{
				return {
					label: country.dial_code,
					value: parseInt(country.dial_code.replace("+", ""))
				}
			});
		},
		countries: function(){
			return this.getCountries().map(country=>{
				return {
					label: country.label,
					value: country.value
				}
			});
		}
	},
	props : {
		form : {
			type: Object,
			required : true,
			default(){
				return {};
			}
		},
		btnDisabled:{
			type:Boolean,
			default(){
				return false;
			}
		},
		model : {
			type: Object,
			required : true,
			default(){
				return {};
			}
		},
		okButton : {
			type: Object,
			required : true,
			default(){
				return {
					label: 'Save',
					icon: "pi pi-save"
				};
			}
		},
		cancelButton : {
			type: [Object, null],
			required : false,
			default(){
				return null;
			}
		},
		
	}
}
</script>

<style scoped lang="scss">

</style>