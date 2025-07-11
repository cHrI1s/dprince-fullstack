<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<DashboardContainer :style="'pb-0 pe-md-4 pe-0 ps-0'"
		                    :show-institution-name="true">
			<div class="row position-relative mb-4 mx-0">
				<div class="col-12">
					<Select class="w-100"
					        :options="uploadOptions"
					        v-model="files.uploader"
					        placeholder="Select What you are uploading"
					        optionLabel="label"
					        option-value="value"/>
				</div>
				
				<div class="col-12 mt-4">
					<DashboardCard :header-title="'Import '+uploadType+' via CSV file'">
						<div class="d-block position-relative mb-4">
							<h4 class="my-0">Step {{ files.file.name===null ? 1 : 2 }}</h4>
						</div>
						<MyFileUploader @uploaded="handleUploadedFiles"
						                accepted-file-types="text/csv"
						                :multiple="false"
						                :disabled="files.file.name!==null || institutionId===null"/>
						
						<div class="d-block position-relative text-dark mt-4"
						     v-if="files.file.name!==null">
							File Name: <strong>{{ files.file.name }}</strong>
						</div>
						
						<div class="mt-4">
							<Button class="w-100"
							        :label="this.uploadType+' Import'"
							        :disabled="canProcessUploaded"
							        @click="processImported"
							        icon="pi pi-cloud-upload"
							        severity="success"/>
						</div>
					</DashboardCard>
				</div>
			</div>
		</DashboardContainer>
	</div>
</template>
<script >
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";
import Button from "primevue/button";
import Select from "primevue/select";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
export default {
	name:'ImportMembers',
	components:{
		DashboardCard,
		DashboardContainer,
		MyFileUploader,
		Button,
		Select
	},
	computed: {
		institutionId: function(){
			let institution = this.$root['getInstitution']();
			if(institution===null) return null;
			return institution.id;
		},
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		uploadOptions: function(){
			return [
				{ label: this.isInstitutionChurch ? "Members" : "Partners", value: "MEMBERS" },
				{ label: "Donations", value: "DONATIONS" },
				{ label: "Families", value: "FAMILIES" },
			]
		},
		uploadType: function(){
			switch(this.files.uploader){
				default:
				case "MEMBERS": return this.isInstitutionChurch ? "Members" : "Partners";
				case "DONATIONS": return "Donations";
				case "FAMILIES": return "Families";
			}
		},
		uploadUrl: function(){
			switch(this.files.uploader){
				default:
				case "MEMBERS": return "/institution/import-members";
				case "DONATIONS": return "/institution/import-donations";
				case "FAMILIES": return "/institution/import-families";
			}
		},
	},
	data(){
		return {
			canProcessUploaded: true,
			file : { name: null },
			donation : { name: null },
			files: {
				file: { name: null},
				canProcessUploaded: true,
				uploader: "MEMBERS"
			}
		}
	},
	methods: {
		handleUploadedFiles: function(file){
			this.canProcessUploaded = false;
			this.files.file.name = file.fileName;
		},
		processImported: function(){
			let uploadType = this.uploadType+" Import";
			let data = this.$root['addInstitutionId']({...this.files.file});
			this.$api.post(this.uploadUrl, data).then(response=>{
				this.files = {
					file: { name: null},
					canProcessUploaded: true,
					uploader: "MEMBERS"
				}
				this.$root['showAlert']("success",
					uploadType,
					response.message)
			}).catch(error=>{
				this.$root['handleApiError'](error, uploadType);
			});
		},
	},
	watch: {
		isChurch: function(newValue){
			this.isInstitutionChurch = newValue;
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
	}
}
</script>

<style scoped lang="scss">

</style>