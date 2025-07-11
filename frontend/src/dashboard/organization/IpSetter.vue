<template>
	<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
	                    :showInstitutionName="true">
		<DashboardCard header-title="Ip Address Setter"
		               :refresh-button="true"
		               @refresh="initialize">
			<div class="w-100 row position-relative mx-0 px-0">
				<div class="col-12 mb-4">
					<InputText class="d-block position-relative w-100"
					           v-model="ipAddress"
					           placeholder="IP Address"/>
				</div>
				<div class="col-12 mb-4">
					<Button @click="save"
					        label="Save"
					        class="w-100"
					        icon="pi pi-save"
					        severity="success"/>
				</div>
			</div>
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import InputText from "primevue/inputtext";
import Button from "primevue/button";

export default {
	name: "IpSetter",
	components: {Button, DashboardCard, DashboardContainer, InputText},
	data(){
		return {
			ipAddress : null
		}
	},
	methods: {
		initialize: function(){
			let data = this.$root['addInstitutionId']({});
			const TITLE = "IP Settings loading.";
			this.$api.post('/institution/get-ip', data).then(response=>{
				console.log(response);
				this.ipAddress = typeof(response.object)==='undefined'
					? null
					: response.object;
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
		save: function(){
			let data = this.$root['addInstitutionId']({ip: this.ipAddress});
			const TITLE = "IP Settings loading.";
			this.$api.post('/institution/set-ip', data).then(response=>{
				this.$root['logout'](false);
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">

</style>