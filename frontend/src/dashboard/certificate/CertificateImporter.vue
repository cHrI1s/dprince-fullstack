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
					        v-model="files.type"
					        placeholder="Select What you are uploading"
					        optionLabel="label"
					        option-value="value"/>
				</div>
				
				<div class="col-12 mt-4">
					<DashboardCard :header-title="'Import '+uploadType+' Certificate'">
						<div class="d-block position-relative mb-4">
							<h4 class="my-0">Step {{ files.fileName===null ? 1 : 2 }}</h4>
						</div>
						<MyFileUploader @uploaded="handleUploadedFiles"
						                accepted-file-types="image/*"
						                :multiple="false"
						                :disabled="files.fileName!==null || institutionId===null"/>
						
						<div class="d-block position-relative text-dark mt-4"
						     v-if="files.fileName!==null">
							File Name: <strong>{{ files.fileName }}</strong>
						</div>
						
						<div class="mt-4">
							<Button class="w-100"
							        :label="'Import '+this.uploadType+' Certificate'"
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
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
export default {
	name:'CertificateImporter',
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
				{ label: "Baptism Certificate", value: "BAPTISM" },
				{ label: "Child Dedication Certificate", value: "KID_DEDICATION" },
				{ label: "Marriage Certificate", value: "MARRIAGE" },
				// { label: "Birthday Certificate", value: "BIRTHDAY" },
			].filter(option=>{
				if(['KID_DEDICATION', 'MARRIAGE'].includes(option.value)) return this.isInstitutionChurch;
				return true;
			});
		},
		uploadType: function(){
			switch(this.files.type){
				default:
				case "BIRTHDAY": return "Birthday";
				case "MARRIAGE": return "Marriage";
				case "KID_DEDICATION": return "Child Dedication";
				case "BAPTISM": return "Baptism";
			}
		},
	},
	data(){
		return {
			canProcessUploaded: true,
			file : { name: null },
			donation : { name: null },
			files: {
				fileName: null,
				canProcessUploaded: true,
				type: "BAPTISM"
			}
		}
	},
	methods: {
		handleUploadedFiles: function(file){
			this.canProcessUploaded = false;
			this.files.fileName = file.fileName;
		},
		processImported: function(){
			let uploadType = this.uploadType+" Background Image";
			let data = this.$root['addInstitutionId']({...this.files});
			delete data.canProcessUploaded;
			this.$api.post("/institution/set-certificate", data).then(response=>{
				this.files = {
					fileName: null,
					canProcessUploaded: true,
					type: "BIRTHDAY"
				};
				if(SUPER_ADMINISTRATORS_ROLES.includes(this.$root['getUserType']())){
					let institution = this.$store.getters.getInstitution;
					if(institution!==null){
						institution = {
							...institution,
							certificates: { ...response.object.certificates }
						}
					}
					this.$store.commit("setInstitution", institution);
				} else {
					let loggedInUser = this.$store.getters.getLoggedInUser;
					loggedInUser = {
						...loggedInUser,
						institution: {
							...loggedInUser.institution,
							certificates: { ...response.object.certificates }
						}
					};
					this.$store.commit("setLoggedInUser", loggedInUser)
				}
				let certificates = response.object.certificates;
				if(typeof certificates[data.type]!=='undefined') {
					this.$root['applyCertificateBackground'](data.type, certificates[data.type]);
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