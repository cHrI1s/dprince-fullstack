<template>
	<DashboardContainer :refresh-button="true"
	                    @refresh="initialize">
		<DashboardCard header-title="Signatures">
			<div class="row mx-0">
				<div class="col-md-12 mb-4">
					<FormLabel labelText="Priest" :required="true"/>
					<Select class="w-100"
					        v-model="defaultSignature.priestId"
					        :options="priests"
					        @change="loadSignature"
					        placeholder="PRIEST"
					        optionLabel="fullName"
					        optionValue="id"/>
				</div>
				
				<div class="col-md-6 mb-4">
					<div class="card bg-light">
						<div class="card-content py-3 bg-light">
							<div class="d-block position-relative" v-if="signature===null">
								<h4 class="my-0 fw-bolder text-center mb-4">No signature</h4>
								<div class="d-block position-relative text-center">
									<img :src="theSignature" class="signature" alt="Signature"/>
								</div>
							</div>
							
							<div class="d-block position-relative" v-else>
								<div class="d-block position-relative text-center">
									<img :src="signature" class="signature"
									     alt="Signature"/>
								</div>
							</div>
						</div>
					</div>
				</div>
				
				<div class="col-md-6 mb-4" v-if="defaultSignature.priestId!==null">
					<FormLabel label-text="Upload Signature"/>
					<MyFileUploader v-model="signatureModel.signatureImage"
					                @uploaded="handleUploadedFile"
					                styleClass="mb-4"/>
					<Button label="Import"
					        icon="pi pi-cloud-upload"
					        severity="success"
					        @click="saveSignature"
					        class="w-100 mt-4"/>
				</div>
				
				<div class="col-md-12 mb-4" v-if="signature!==null">
					<Button label="Set Default"
					        icon="pi pi-save"
					        severity="warn"
					        @click="saveDefaultSignature"
					        class="w-100"/>
				</div>
			</div>
		</DashboardCard>
	</DashboardContainer>
</template>


<script>
import Select from "primevue/select";
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormLabel from "@/components/FormLabel.vue";
import signaturePlaceholder from "./images/img.png";
import {isEmpty} from "@/utils/AppStringUtils";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "SignatureBoard",
	components: {
		MyFileUploader,
		Button,
		DashboardCard,
		DashboardContainer,
		FormLabel,
		Select
	},
	data(){
		return {
			priests: [],
			defaultSignature: {
				priestId: null
			},
			theSignature: signaturePlaceholder,
			loadedSignature: null,
			
			
			signatureModel:{
				signatureImage:null,
			}
		}
	},
	computed: {
		loggedInUser : function(){
			return this.$store.getters.getLoggedInUser;
		},
		selectedPriest: function(){
			let priest = this.priests.find(singlePriest=>singlePriest.id===this.defaultSignature.priestId);
			if(typeof priest!=='undefined' && priest!==null) return priest;
			return null;
		},
		signature: function(){
			if(this.defaultSignature.priestId===null) return null;
			let priest = this.selectedPriest;
			if(typeof priest!=='undefined' && priest!==null) {
				if(!isEmpty(priest.signature) && priest.signature!==null){
					return this.loadedSignature;
				}
			}
			return null;
		}
	},
	methods: {
		loadSignature: function(event){
			let priest = this.selectedPriest;
			if(priest!==null) {
				this.$api.get("/files/get/"+priest.signature, {
					responseType: 'blob'
				}).then(response=>{
					this.$store.commit("setLoading", true);
					this.loadedSignature = URL.createObjectURL(response);
					this.$store.commit("setLoading", false);
				}).catch(ignored=>{});
			}
		},
		handleUploadedFile: function(uploadFile){
			this.signatureModel.signatureImage = uploadFile[0].fileName;
		},
		saveSignature: function(){
			let  data = {
				...this.signatureModel,
				memberId : this.defaultSignature.priestId,
			}
			data = this.$root['addInstitutionId']({...data});
			this.$api.post("/signature/save", data).then(response=>{
				this.$root['showAlert']("success", "Priest Signature", response.message);
				this.priests = this.priests.map(singlePriest=>{
					if(singlePriest.id===this.selectedPriest.id) {
						singlePriest = {
							...singlePriest,
							signature: response.object.signatureImage
						}
					}
					return singlePriest;
				});
				this.defaultSignature = {priestId: null};
			}).catch(error=>{
				this.$root['handleApiError'](error, "Priest Signature.")
			});
		},
		saveDefaultSignature: function(){
			const TITLE = "Setting Church Signature";
			let data = this.$root['addInstitutionId']({...this.defaultSignature});
			this.$api.post("/signature/set-default", data).then(response=>{
				this.$store.commit("setLoading", true);
				if(response.successful) {
					let institution = response.object;
					if(SUPER_ADMINISTRATORS_ROLES.includes(this.loggedInUser.userType)){
						let storedInstitution = this.$store.getters.getInstitution;
						if(storedInstitution!==null){
							storedInstitution = {
								...storedInstitution,
								priestSignature: institution.priestSignature
							};
							this.$store.commit("setInstitution", storedInstitution);
						}
					} else {
						let newUser = {
							...this.loggedInUser,
							institution: {
								...this.loggedInUser.institution,
								priestSignature: institution.priestSignature
							}
						};
						this.$store.commit("setLoggedInUser", newUser);
					}
					this.$root['loadSignature']();
				}
				this.$store.commit("setLoading", false);
				this.$root['showAlert'](response.successful ? 'success' : 'danger',
					TITLE,
					response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
		initialize: function(){
			if(this.loggedInUser!=null) {
				const TITLE = "Signatures Loading...";
				let data = this.$root['addInstitutionId']({});
				this.$api.post("/signature/list", data).then(response => {
					this.priests = response.objects;
					this.$root['showAlert']('success', TITLE, response.message);
				}).catch(error => {
					this.$root['handleApiError'](error, TITLE);
				});
			}
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">
	.signature{
		max-width: 75%;
		min-width: 50%;
		max-height: 50dvh;
		min-height: 25dvh;
	}
</style>