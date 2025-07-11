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
			<DashboardCard :header-title="headerTitle">
				<div class="row mb-2 justify-content-end"><div class="col-md-6 mb-4">
						<Button label="Clear Form"
						        @click="clearForm"
						        outlined
						        icon="pi pi-times"
						        severity="danger"
						        class="rounded-0 w-100 text-end"/>
					</div>
					<div class="col-md-6 mb-4">
						<Button label="Refresh Form"
						        @click="initialize"
						        icon="pi pi-refresh"
						        severity="warn"
						        class="rounded-0 w-100 text-end"/>
					</div>
				</div>
				
				<div class="row">
					<div class="col-md-6 mb-4"
					     v-if="isInstitutionChurch && isChurchBranch">
						<div class="d-block position-relative">
							<FormLabel :label-text="placeholder.category" :required="true"/>
							<Select v-model="organization.categoryId"
							        optionLabel="name"
							        optionValue="id"
							        :options="categories"
							        :placeholder="placeholder.category"
							        class="w-100"/>
						</div>
					</div>
					
					<div class="col-md-6 mb-4" v-if="isChurchBranch">
						<div class="d-block position-relative">
							<FormLabel label-text="Subscription Plan"
							           :required="true"/>
							<Select v-model="organization.subscriptionPlan"
							        optionLabel="name"
							        optionValue="id"
							        :options="subscriptionPlans"
							        placeholder="Subscription Plan"
							        class="w-100"/>
						</div>
					</div>
					
					<div class="col-md-6 mb-4"
					     v-if="isChurchBranch"
					     :class="isInstitutionChurch ? 'col-md-12' : 'col-md-6'">
						<div class="d-block position-relative">
							<FormLabel label-text="Subscription"
							           :required="true"/>
							<Select v-model="organization.subscription"
							        optionLabel="label"
							        optionValue="value"
							        :options="subscription"
							        placeholder="Subscription"
							        class="w-100"/>
						</div>
					</div>
					
					<div class="col-md- mb-4">
						<div class="d-block position-relative">
							<FormLabel :label-text="placeholder.name" :required="true"/>
							<InputText v-model="organization.name"
							           :placeholder="placeholder.name"
							           class="w-100"/>
						</div>
					</div>
					
					<div class="col-md-6 mb-4">
						<div class="d-block position-relative">
							<FormLabel label-text="Email" :required="true"/>
							<InputText v-model="organization.email"
							           :disabled="isUpdating"
							           name="institution_email"
							           :placeholder="placeholder.email"
							           class="w-100"/>
						</div>
					</div>
					
					<div class="col-md-6 mb-4">
						<div class="d-block position-relative">
							<FormLabel label-text="Reporting Email" :required="false"/>
							<InputText v-model="organization.reportingEmail"
							           :placeholder="'Reporting email'"
							           class="w-100 normal-text"/>
						</div>
					</div>
					
					
					<div class=" mb-4"
					     :class="(!isUpdating || isInstitutionChurch) ? 'col-md-12' : 'col-md-6'">
						<div class="d-block position-relative">
							<FormLabel :label-text="placeholder.baseCode" :required="true"/>
							<InputText v-model="organization.baseCode"
							           :placeholder="placeholder.baseCode"
							           name="institution-fresh-new-base-code"
							           :disabled="isUpdating"
							           maxlength="10"
							           class="w-100"/>
						</div>
					</div>
					
					<div class="col-md-12 mb-4" v-if="!isUpdating">
						<div class="d-block position-relative">
							<FormLabel label-text="Default Password" :required="true"/>
							<Password v-model="organization.defaultPassword"
							          toggle-mask
							          name="institution-fresh-new-password"
							          :inputProps="{ autocomplete: 'fresh-new-password' }"
							          placeholder="Default Password"
							          inputClass="w-100"
							          class="w-100"/>
						</div>
					</div>
					
					
					<div class="col-md-12 mb-4" :class="isInstitutionChurch ? 'col-md-6' : 'col-12'">
						<div class="row">
							<div class="col-md-3 col-4">
								<FormLabel label-text="Country Code" :required="true"/>
								<Select :options="countriesPhoneCodes"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="Code"
								        v-model="organization.phoneCode"
								        :disabled="isUpdating"
								        class="w-100"/>
							</div>
							<div class="col-md-9 col-8">
								<FormLabel label-text="Phone" :required="true"/>
								<InputText v-model="organization.phone"
								           :disabled="isUpdating"
								           placeholder="Phone"
								           class="w-100"/>
							</div>
						</div>
						
					</div>
					
					<div class="col-12 mb-4">
						<div class="d-block position-relative">
							<FormLabel :label-text="placeholder.address" :required="true"/>
							<Textarea v-model="organization.address"
							          :placeholder="placeholder.address"
							          class="w-100"/>
						</div>
					</div>
					<div class="col-12 mb-4">
						<div class="d-block position-relative">
							<FormLabel label-text="Website" :required="false"/>
							<InputText v-model="organization.website"
							           :placeholder="'Website'"
							           class="w-100 normal-text"/>
						</div>
					</div>
					
					<div class="col-12 mb-4"
					     v-if="selectedSubscriptionPlanFeatures.includes('CUSTOM_ORGANIZATION_LOGO')">
						<MyFileUploader @uploaded="handleUploadedFiles"/>
					</div>
					
					<div class="col-md-12 mb-4"
					     v-if="organization.logo!==null && selectedSubscriptionPlanFeatures.includes('CUSTOM_ORGANIZATION_LOGO')">
						<div class="row">
							<div class="col-md-4">
								<img :src="logo"
								     v-if="logo!==null"
								     class="mx-auto"
								     alt="Client Logo"
								     style="max-width:100%; max-height:200px"/>
							</div>
						</div>
					</div>
					
					<div class="mb-4 d-block position-relative"
					     v-if="termsVisible">
						<strong>Note:</strong> By Click Save, you acknowledge the Client agrees to the
						<router-link to="/terms" target="_blank" class="fw-bolder text-underline text-hoverable">Terms & Conditions</router-link> of this Application.
					</div>
					
					<div class="col-12 mb-4">
						<Button label="Save"
						        :disabled="!canSave"
						        @click="save"
						        icon="pi pi-save"
						        class="w-100"/>
					</div>
				</div>
			</DashboardCard>
		</DashboardContainer>
		
		
		<InstitutionDetails :institution="createdInstitution"
		                    :closeButton="{label: 'Next', severity: 'info', icon: 'pi pi-arrow-right'}"
		                    :visible="createdInstitution!==null"
		                    @close="closeDetailor"/>
	</div>
</template>


<script>
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import InputText from "primevue/inputtext";
import Password from "primevue/password";
import Select from "primevue/select";
import Button from "primevue/button";
import Textarea from "primevue/textarea";
import {ORGANIZATION_MODEL, ORGANIZATION_TYPES, SUBSCRIPTION_PLANS,} from "@/dashboard/organization/Organization";
import FormLabel from "@/components/FormLabel.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import InstitutionDetails from "@/dashboard/organization/dialogs/details/InstitutionDetails.vue";
import {getCountries} from "@/dashboard/utils/countries";

export default {
	name: "OrganizationCreator",
	components: {
		InstitutionDetails,
		DashboardContainer,
		FormLabel,
		DashboardCard,
		MyFileUploader,
		InputText,
		Select,
		Button,
		Textarea,
		Password
	},
	computed: {
		isInstitutionChurch : function(){
			return this.isChurch;
		},
		termsVisible: function(){
			let isUpdate = typeof(this.organization.id)!=='undefined' && this.organization.id!==null;
			return !isUpdate;
		},
		selectedSubscriptionPlanFeatures: function(){
			let features = [];
			if(this.organization.subscriptionPlan!==null){
				let selected = this.subscriptionPlans.find(plan=>plan.id===this.organization.subscriptionPlan);
				if(selected) features = selected.features;
			}
			return features;
		},
		isChurchBranch: function(){
			if(this.userType===null) return false;
			return !["CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"].includes(this.userType);
		},
		placeholder : function(){
			return (this.isInstitutionChurch)
				? this.orgPlaceholder.church
				: this.orgPlaceholder.organization;
		},
       userType: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			return loggedInUser.userType;
       },
		canSave: function(){
			if(this.isInstitutionChurch) {
				if(["CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"].includes(this.userType)) return true;
				return (this.categories.length>0 && this.subscriptionPlans.length>0);
			}
			return true;
		},
		isUpdating: function(){
			return typeof this.organization.id!=='undefined' && this.organization.id!==null;
		},
		countriesPhoneCodes: function(){
			return getCountries().map(country => {
				return {
					label: country.dial_code,
					value: parseInt(country.dial_code.replace("+", ""))
				}
			});
		},
	},
 
	data(){
		return {
            logo : null,
			organization        : {...ORGANIZATION_MODEL},
			organizationTypes   : [...ORGANIZATION_TYPES],
			subscription        : [...SUBSCRIPTION_PLANS],
			subscriptionPlans   : [],
            categories:[],
			
			// Do not touch this
			orgPlaceholder      : {
				church      : {
					email   : "Church Email",
					name    : "Church Name",
					address : "Church Address",
					baseCode: "Church Base Code",
					category: "Church Type"
				},
				organization: {
					email   : "Organization Email",
					name    : "Organization Name",
					address : "Organization Address",
					baseCode: "Organization Base Code",
					category: "Category"
				}
			},
			institutionSet: false,
			
			createdInstitution: null,
		}
	},
	methods: {
		closeDetailor: function(institution){
			this.$store.commit("setInstitution", this.createdInstitution);
			let url = (this.createdInstitution.institutionType==='GENERAL')
				? "/organizations/categories"
				: "/church/subscription/0";
			this.$router.push(url);
		},
        loadImage :async function(){
            if(this.organization.logo!==null){
                this.$api.get("files/get/"+this.organization.logo, {responseType:"blob"})
                    .then(response=>{
                        this.logo = URL.createObjectURL(response);
                        this.$root['showAlert']("success", "Logo Loading", response.message);
                    }).catch(error=>{
                    //this.$root['handleApiError'](error, "Logo Loading");
                });
            }
        },
		clearForm: function(){
			this.organization = {...ORGANIZATION_MODEL};
			this.$store.commit("setInstitution", null);
		},
		handleUploadedFiles: function(uploadedFiles){
			this.organization.logo = uploadedFiles[0].fileName;
            this.loadImage();
		},
		save: function(){
			let data = {...this.organization};
			data.institutionType = (this.isInstitutionChurch) ? "CHURCH" : "GENERAL";
            let url  = this.isUpdating
	            ? "/institution/update-institution"
	            : "/institution/save-organization";
			if(this.isInstitutionChurch){
				// Check if the user is not a church admin
				url = !(["CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"].includes(this.userType))
					? (this.isUpdating ? "/institution/update-institution" : "/institution/save-church")
					: (this.isUpdating ? "/institution/update-branch" : "/institution/save-church-branch");
			}
            this.$api.post(url, data).then(response=>{
				this.createdInstitution = response.object;
				this.organization = {...ORGANIZATION_MODEL};
				this.$store.commit("setInstitution", null);
                this.$root['showAlert']("success", "Institution", response.message)
            }).catch(error=>{
                this.$root['handleApiError'](error, "Institution");
            });
		},
        loadCategories : function(){
			let data = this.isInstitutionChurch
				? {}
				: this.$root['addInstitutionId']({});
            this.$api.post("/category/get", data)
                .then(response=>{
                    this.categories = response;
                    this.$root['showAlert']("success",
	                    this.isInstitutionChurch ? "Church types" :"Categories",
	                    this.isInstitutionChurch ? "Church types" : "Categories Loaded!");
                }).catch(error=>{
                this.$root['handleApiError'](error)
            })
        },
		loadSubscriptionPlans : function(){
			let institutionType = (this.isInstitutionChurch) ? "CHURCH" : "GENERAL";
            this.$api.get(`/plan/get-all/${institutionType}`)
                .then(response=>{
                    this.subscriptionPlans = response;
                    this.$root['showAlert']("success", "Subscription Plans", "Loaded!");
                }).catch(error=>{
                this.$root['handleApiError'](error)
            })
        },
		initialize: function(refresh=false){
			if(!refresh) {
				if (this.$store.getters.getInstitution !== null) {
					this.organization = {...this.$store.getters.getInstitution};
					this.institutionSet = true;
				}
			}
			if(this.isInstitutionChurch) this.loadCategories();
			this.loadSubscriptionPlans();
			
			if(this.organization!==null) this.loadImage()
		},
	},
    beforeMount() {
		this.initialize();
    },
    watch: {
        isChurch: function(newValue){
	        this.$store.commit("handleInstitutionChange");
			this.isInstitutionChurch = newValue;
	        this.institutionSet = false;
	        this.organization = {...ORGANIZATION_MODEL};
            this.initialize(true);
        }
    },
    props: {
		headerTitle : {
			type : String,
			required: true,
			default(){
				return "Create General Member";
			}
		},
		isChurch: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>