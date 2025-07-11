<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :show-institution-name="true">
		<DashboardCard header-title="Profile">
			<FormGenerator :ok-button="okButton"
			               :model="model"
			               :form="form"
			               @click="changePassword"/>
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormGenerator from "@/components/form/FormGenerator.vue";
import {ADMINISTRATOR_CREATION_MODEL} from "@/dashboard/administrators/administrators";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
export default {
	name: "SuperAdministratorProfile",
    components: {DashboardContainer, FormGenerator, DashboardCard},
	data(){
		return {
			model: {
				username: null,
				password: null,
				confirmPassword: null,
			},
			okButton: {
				icon: "pi pi-save",
				label: "Save"
			},
			form: [
				{ type: "TEXT", model: "username", label: "Username", inputClass:"normal-text", styleClass: "col-12 mb-4", required: false, disabled: true },
				{ type: "PASSWORD", model: "password", label: "Password", inputClass:"normal-text", styleClass: "col-md-6 mb-4" },
				{ type: "PASSWORD", model: "confirmPassword", label: "Confirm Password", inputClass:"normal-text", styleClass: "col-md-6 mb-4" }
			]
		}
	},
    methods:{
	    changePassword: function(model){
            this.$api.post("/user/update-my-password", model).then(response=>{
				this.model = {
					username: this.getLoggedInUserUsername(),
					password: null,
					confirmPassword: null,
				};
				this.$root['showAlert']('success', "Password Change", response.message);
            }).catch(error=>{
				this.$root['handleApiError'](error, "Password Change");
            });
        },
	    getLoggedInUserUsername: function(){
		    let loggedInUser = this.$store.getters.getLoggedInUser;
		    if(loggedInUser!==null) return loggedInUser.username;
			return null;
	    }
    },
	beforeMount(){
		this.model.username = this.getLoggedInUserUsername();
	}
}
</script>

<style scoped lang="scss">

</style>