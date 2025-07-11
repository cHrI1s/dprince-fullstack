<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Create Assistant Admin"
	               :clearFormButton="true"
	               @clear="clearForm">
		
		<FormGenerator :ok-button="okButton"
		               :model="model"
		               :form="form"
		               @click="(admin)=>$emit('save', admin)"/>
	</DashboardCard>
</template>


<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormGenerator from "@/components/form/FormGenerator.vue";
import {ASSISTANT_ADMIN_MODEL, USER_TYPES} from "@/dashboard/users/users";
import {GENDERS} from "@/dashboard/members/members";

export default {
	name: "AssistantAdminCreator",
	emits: ["save"],
	components: {FormGenerator, DashboardCard},
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		allowedUserTypes: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			let userTypes = [];
			if(loggedInUser!==null){
				userTypes = [...USER_TYPES]
					.filter(userType=>{
						let institution = this.$root['getInstitution']();
						if(institution!==null) {
							let userBasedExclusion = [];
							if (institution.institutionType === "GENERAL") {
								let superAdmins = ["ORGANIZATION_ADMINISTRATOR",
									"ORGANIZATION_ASSISTANT_ADMINISTRATOR",
									"ORGANIZATION_DATA_ENTRY_OPERATOR"];
								userBasedExclusion = {
									"EXTRA_SUPER_ADMIN": superAdmins,
									"SUPER_ADMINISTRATOR": superAdmins,
									"SUPER_ASSISTANT_ADMINISTRATOR": superAdmins,
									"ORGANIZATION_ADMINISTRATOR": [...superAdmins.filter(admin => admin !== "ORGANIZATION_ADMINISTRATOR")],
									"ORGANIZATION_ASSISTANT_ADMINISTRATOR": superAdmins.filter(admin => admin !== "ORGANIZATION_ADMINISTRATOR"),
								}
							} else {
								if(typeof institution.parentInstitutionId!=='undefined' && institution.parentInstitutionId!==null){
									let superAdmins = [
										"CHURCH_BRANCH_ADMINISTRATOR",
										"CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
										"CHURCH_DATA_ENTRY_OPERATOR"];
									userBasedExclusion = {
										"EXTRA_SUPER_ADMIN": superAdmins,
										"SUPER_ADMINISTRATOR": superAdmins,
										"SUPER_ASSISTANT_ADMINISTRATOR": superAdmins,
										"CHURCH_BRANCH_ADMINISTRATOR": ["CHURCH_ASSISTANT_ADMINISTRATOR", "CHURCH_DATA_ENTRY_OPERATOR"],
										"CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR": ["CHURCH_DATA_ENTRY_OPERATOR"],
									}
								} else {
									let superAdmins = [
										"CHURCH_ADMINISTRATOR",
										"CHURCH_ASSISTANT_ADMINISTRATOR",
										"CHURCH_DATA_ENTRY_OPERATOR"];
									userBasedExclusion = {
										"EXTRA_SUPER_ADMIN": superAdmins,
										"SUPER_ADMINISTRATOR": superAdmins,
										"SUPER_ASSISTANT_ADMINISTRATOR": superAdmins,
										"CHURCH_ADMINISTRATOR": superAdmins.filter(admin => admin !== "CHURCH_ADMINISTRATOR"),
										"CHURCH_ASSISTANT_ADMINISTRATOR": superAdmins.filter(admin => !["CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"].includes(admin)),
									}
								}
							}
							if(typeof userBasedExclusion[loggedInUser.userType]!=='undefined') {
								return userBasedExclusion[loggedInUser.userType].includes(userType.value);
							}
						}
						return false;
					});
			}
			return userTypes;
		},
		form: function(){
			let forbiddenInputs = [];
			let disabled = (typeof this.theModel.id!=='undefined' && this.theModel.id!==null);
			let inputs = [
				{ label: "Admin Type", model: "adminType", type: "SELECT", options: this.allowedUserTypes, styleClass : "col-md-6 mb-4"},
				{ label: "Admin Code/Staff Id", model: "adminCode", type: "TEXT", styleClass : "col-md-6 mb-4", disabled: disabled},
				{ label: "First Name", model: "firstName", type: "TEXT", styleClass : "col-md-6 mb-4"},
				{ label: "Last Name", model: "lastName", type: "TEXT", styleClass : "col-md-6 mb-4"},
				{ label: "Gender", model: "gender", type: "SELECT", options: [...GENDERS], styleClass : "col-md-6 mb-4"},
				{ label: "Phone", model: "phone", type: "NUMBER", styleClass : "col-md-6 mb-4"},
				{ label: "Email", model: "email", type: "TEXT", inputClass: "normal-text", styleClass : "col-md-6 mb-4"},
				{ label: "Username", model: "username", type: "TEXT", inputClass: "normal-text", styleClass : "col-md-6 mb-4"},
				{ label: "Password", model: "password", type: "PASSWORD", inputClass: "normal-text", styleClass : "col-md-6 mb-4"},
				{ label: "Confirm Password", model: "confirmPassword", type: "PASSWORD", inputClass: "normal-text", styleClass : "col-md-6 mb-4"},
			];
			if(this.isChurch) {
				forbiddenInputs = []; // ["firstName", "lastName", "gender", "email"];
			} else {
				forbiddenInputs = [];
			}
			return inputs.filter(singleInput=>{
				return !forbiddenInputs.includes(singleInput.model);
			});
		},
		theModel: function(){
			return this.model;
		}
	},
	data(){
		return {
			okButton: {
				label: "Save",
				icon:"pi pi-save"
			},
			model: {...this.admin}
		}
	},
	methods: {
		clearForm: function(){
			this.model = {...ASSISTANT_ADMIN_MODEL};
		}
	},
	watch: {
		admin: function(newValue){
			this.model = newValue;
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			default: false
		},
		admin: {
			type: Object,
			default(){
				return {...ASSISTANT_ADMIN_MODEL};
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>