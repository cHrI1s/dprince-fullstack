<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :show-institution-name="true">
		<DashboardCard :header-title="headerTitle">
			<div class="row position-relative mb-4 justify-content-end">
				<Button label="Reset Form"
				        class="col-md-4"
				        severity="warn"
				        icon="pi pi-refresh"
				        @click="refresh"/>
			</div>
			<FormGenerator :ok-button="okButton"
			               :model="creationModel"
			               :form="formUI"
			               @click="save"/>
	        
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import Button from "primevue/button";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormGenerator from "@/components/form/FormGenerator.vue";
import {ADMINISTRATOR_CREATION_MODEL} from "@/dashboard/administrators/administrators";
import {GENDERS} from "@/dashboard/members/members";
import {USER_TYPES} from "@/dashboard/users/users";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";

export default {
	name: "SuperAdminCreator",
	components: {
		DashboardContainer,
		Button,
		FormGenerator,
		DashboardCard
	},
	emits: ["workFinished"],
	computed: {
		headerTitle: function(){
			return (typeof this.creationModel.id!=='undefined')
				? 'Update Administrator'
				: "Create Administrator";
		},
		formUI: function(){
			let userTypes = USER_TYPES.filter(singleUserType=>{
				let allowedTypes = [];
				let loggedInUser = this.$store.getters.getLoggedInUser;
				if(loggedInUser!==null){
					switch(loggedInUser.userType){
						case "EXTRA_SUPER_ADMIN":
							allowedTypes = [
								...allowedTypes,
								"EXTRA_SUPER_ADMIN",
								"SUPER_ADMINISTRATOR",
								"SUPER_ASSISTANT_ADMINISTRATOR"
							];
							break;
							
						case "SUPER_ADMINISTRATOR":
							allowedTypes = [
								...allowedTypes,
								"SUPER_ADMINISTRATOR",
								"SUPER_ASSISTANT_ADMINISTRATOR"
							];
							break;
						
						case "CHURCH_ADMINISTRATOR":
							allowedTypes = [
								...allowedTypes,
								"CHURCH_ASSISTANT_ADMINISTRATOR"
							];
							break;
						
						case "ORGANIZATION_ADMINISTRATOR":
							allowedTypes = [
								...allowedTypes,
								"ORGANIZATION_ASSISTANT_ADMINISTRATOR"
							];
							break;
					}
				}
				return allowedTypes.includes(singleUserType.value);
			});
			return [
				{ type: "TEXT", model: "staffId", label: 'Staff Id', required: true, styleClass: 'col-12 mb-4'},
				{ type: "TEXT", model: "firstName", label: 'First name', required: true, styleClass: 'col-md-6 mb-4'},
				{ type: "TEXT", model: "lastName", label: 'Last name', required: true, styleClass: 'col-md-6 mb-4'},
				{ type: "SELECT", model: "gender", label: 'Gender', required: true, styleClass: 'col-md-6 mb-4', options: [...GENDERS]},
				{ type: "NUMBER", model: "phone", label: 'Phone Number', required: true, styleClass: 'col-md-6 mb-4'},
				{ type: "TEXT", model: "username", label: 'Email', inputClass:'normal-text', required: true, styleClass: 'col-md-6 mb-4'},
				{ type: "SELECT", model: "userType", label: 'User Role', required: true, styleClass: 'col-md-6 mb-4', options: userTypes},
				{ type: "PASSWORD", model: "password", label: 'Password', inputClass:'normal-text', required: true, styleClass: 'col-md-6 mb-4', feedback: true},
				{ type: "PASSWORD", model: "confirmPassword", label: 'Confirm Password', inputClass:'normal-text', required: true, styleClass: 'col-md-6 mb-4', feedback: true},
			].filter(singleForm=>{
				if(typeof this.localCreationModel.id!=='undefined' && !this.passwordUpdateLocal) return singleForm.type!=="PASSWORD";
				if(this.passwordUpdateLocal) return singleForm.type==="PASSWORD";
				return true;
			});
		},
		creationModel: {
			get: function(){
				return (this.localCreationModel===null)
					? {...ADMINISTRATOR_CREATION_MODEL}
					: this.localCreationModel;
			}, set: function(newValue){
				this.localCreationModel = newValue;
			}
		},
	},
	data(){
        return {
			okButton: {
				label:(this.$store.getters.getLabel==null) ? "Save" : "Update",
				icon : (this.$store.getters.getLabel==null) ? "pi pi-save" : "pi pi-refresh"
			},
			localCreationModel: this.user,
			passwordUpdateLocal: this.passwordUpdateform
		}
	},
	methods: {
		refresh: function(){
			this.creationModel = {...ADMINISTRATOR_CREATION_MODEL};
			this.localCreationModel =  {...ADMINISTRATOR_CREATION_MODEL};
		},
		save: function(form){
			let update = (typeof this.localCreationModel.id!=='undefined'),
				url =  update
					? (this.passwordUpdateLocal
						? "/user/update-user-password"
						: "/user/update-user" )
					: "/user/create";
     
			this.$api.post(url, form).then(response=>{
				this.refresh();
				this.$emit("workFinished", response);
				this.passwordUpdateLocal = false;
				this.$root['showAlert']('success', "Administrator Creation",
					update
						? "Updated"
						: "Created");
			}).catch(error=>{
				this.$root['handleApiError'](error, "Super Admin Creation");
			})
		},
	},
	watch: {
		user: function(newValue){
			this.creationModel = newValue;
		},
		passwordUpdate: function(newValue){
			this.passwordUpdateLocal = newValue;
		}
	},
    beforeMount() {
    },
    props: {
		user: {
			type: Object,
			default(){
				return null;
			}
		},
  
		passwordUpdate: {
			type: Boolean,
			default(){
				return false;
			}
		}
	},
 
}
</script>

<style scoped lang="scss">

</style>