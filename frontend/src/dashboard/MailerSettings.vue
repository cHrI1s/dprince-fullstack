<template>
	<div class="d-block position-relative">
		<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
		                    :show-institution-name="true">
			<DashboardCard header-title="Mailer Settings"
			               :refresh-button="true"
			               @refresh="initialize">
				
				<FormGenerator :model="mailer"
				               :form="form"
				               :ok-button="okButton"
				               @click="save"/>
			</DashboardCard>
		</DashboardContainer>
	</div>
</template>


<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormGenerator from "@/components/form/FormGenerator.vue";
import {MAILER_MODEL} from "@/dashboard/utils/mailer";

export default {
	name: "MailerSettings",
	components: {DashboardCard, DashboardContainer, FormGenerator},
	data(){
		return {
			mailer: {...MAILER_MODEL},
			form: [
				{ model: "host", label: "Host", type: "TEXT", styleClass: "mb-4", inputClass: "normal-text" },
				{ model: "port", label: "Port", type: "NUMBER", styleClass: "mb-4", inputClass: "normal-text" },
				{ model: "username", label: "Username", type: "TEXT", styleClass: "mb-4", inputClass: "normal-text" },
				{ model: "password", label: "Password", type: "PASSWORD", styleClass: "mb-4", inputClass: "normal-text" },
			],
			okButton: {
				label: "Save Settings",
				icon: "pi pi-save"
			}
		}
	},
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		institution: function(){
			return this.$store.getters.getInstitution;
		}
	},
	methods: {
		initialize: function(){
			if(this.institution===null){
				let institutionName = this.isInstitutionChurch
					? "Church"
					: "Organization";
				this.$root['showAlert']('warn', "Mailer Settings", "No "+institutionName+" defined!");
				return;
			}
			this.$api.get("/mailer/get/"+this.institution.id).then(response=>{
				this.mailer = { ...response.object };
			}).catch(error=>{
				this.$root['handleApiError'](error, "Mailer Loading!");
			});
		},
		save: function(formData){
			if(this.institution===null){
				let institutionName = this.isInstitutionChurch
					? "Church"
					: "Organization";
				this.$root['showAlert']('warn', "Mailer Settings", "No "+institutionName+" defined!");
				return;
			}
			let data = this.$root['addInstitutionId']({...formData});
			this.$api.post("/mailer/save-settings", data).then(response=>{
				this.$root['showAlert']('success', "Mailer Settings", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Mailer Settings");
			});
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">

</style>