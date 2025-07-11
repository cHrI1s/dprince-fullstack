<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer>
		<DashboardCard header-title="Signature Importer">
			<MyFileUploader v-model="signatureModel.signatureImage"
			                @uploaded="handleUploadedFile"
			                styleClass="mb-4"/>
			<Button label="Import"
			        icon="pi pi-cloud-upload"
			        severity="success"
                    @click="save"
			        class="w-100"/>
		</DashboardCard>
	</DashboardContainer>
</template>


<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";
import Button from "primevue/button";

export default {
	name: "SignatureImporter",
	components: {
		MyFileUploader,
		DashboardCard,
		DashboardContainer,
		Button
	},
    data(){
        return {
            signatureModel:{
                signatureImage:null,
            }
        }
    },
	methods: {
		handleUploadedFile: function(uploadFile){
            this.signatureModel.signatureImage = uploadFile[0].fileName;
		},
        save: function(){
           let  data = {
                ...this.signatureModel,
                memberId : this.$store.getters.getTmpInstitutionMember.id,
            }
			data = this.$root['addInstitutionId'](data);
            this.$api.post("/signature/save", data).then(response=>{
                this.$root['showAlert']("success", "Priest Signature", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, "Priest Signature.")
            })
        }
	},
 
}
</script>

<style scoped lang="scss">

</style>