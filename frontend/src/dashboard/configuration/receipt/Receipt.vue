<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer :style="'row mx-0 px-0'"
                        :refresh-button="true"
                        :show-institution-name="true"
                        @refresh="initialize">
	    <DashboardCard :column="'col-md-6 mb-4'" :header-title="'Credit Accounts Details'" >
		    <div class="d-block position- mb-4 relative p-0 m-0">
			    <div class="d-flex gap-3 mb-4"
			         v-for="(singleAccount, index) in accountsForm"
			         :key="index+'_index'">
				    <InputText class="flex-grow-1"
				               placeholder="Account Name"
				               v-model="singleAccount.name"/>
				    <Button icon="pi pi-times"
				            @click="removeAccount(index)"
				            :disabled="index===0 || accountsForm.length===1"
				            severity="danger"/>
			    </div>
			    
			    <div class="row">
				    <div class="col-6">
					    <Button label="Add Account"
					            icon="pi pi-plus"
					            class="w-100"
					            severity="info"
					            :disabled="isEditing"
					            @click="addAccount"/>
				    </div>
				    <div class="col-6">
					    <Button label="Save"
					            icon="pi pi-save"
					            class="w-100"
					            @click="saveAccounts"/>
				    </div>
			    </div>
		    </div>
	    </DashboardCard>
	    
	    <DashboardCard :column="'col-md-6 mb-4'"
	                   :header-title="'Credit Accounts List'"
	                   contentClass="px-0">
		    <div class="d-block position-relative text-dark relative p-3 m-0"
		         :class="index%2===0 ? 'bg-light': 'bg-white'"
		         v-for="(singleAccount, index) in accounts"
		         :key="index+'_index'">
			    <div class="d-flex gap-3 mb-3 fw-bolder text-uppercase">
				    {{ singleAccount.name }}
			    </div>
			    
			    <div class="row">
				    <div class="col-6">
					    <Button label="Update"
					            icon="pi pi-save"
					            class="w-100"
					            severity="info"
					            outlined
					            @click="engageUpdate(singleAccount)"/>
				    </div>
				    <div class="col-6">
					    <Button label="Delete"
					            severity="danger"
					            icon="pi pi-trash"
					            class="w-100"
					            outlined
					            @click="deleteAccount(singleAccount.id)"/>
				    </div>
			    </div>
		    </div>
		    
		    <div class="d-block position-relative fw-bolder py-4"
		         v-if="accounts.length===0">
			   <h4 class="text-center px-3 my-0 text-danger">
				   No credit Account(s) found. ;(
			   </h4>
		    </div>
	    </DashboardCard>
	    
        <DashboardCard header-title="" :column="'col-md-6 mb-4'">
	        <div class="row">
		        <div class="col-12 mb-4">
			        <FormLabel label-text="Bible Verse" :required="true"/>
			        <Textarea class="w-100" v-model="receiptModel.bibleVerse" :auto-resize="true" :rows="2" placeholder="Bible Verse"/>
		        </div>
		        <div class="col-12 mb-4">
			        <FormLabel label-text="Message" :required="true"/>
			        <Textarea class="w-100" v-model="receiptModel.receiptMessage" :auto-resize="true" :rows="2" placeholder="Message"/>
		        </div>
		        
		        <div class="mb-4"
		             v-if="canUploadCustomTemplate"
		             :class="receiptModel.receiptType==='USER_DEFINED' ? 'col-md-6' : 'col-12'">
			        <FormLabel label-text="Receipt Type" :required="true"/>
			        <Select class="w-100" v-model="receiptModel.receiptType"
			                :options="receiptTypeOptions"
			                optionLabel="label"
			                optionValue="value"
			                placeholder="Type of receipts"/>
		        </div>
		        
		        <div class="col-md-6 mb-4"
		             v-if="receiptModel.receiptType==='USER_DEFINED' && canUploadCustomTemplate">
			        <FormLabel label-text="Receipt Type" :required="true"/>
			        <Select class="w-100" v-model="receiptModel.customModel" :options="customModelOptions" optionLabel="label" optionValue="value" placeholder="Type of receipts"/>
		        </div>
		        
		        
		        <div class="col-12 mb-4"
		             v-if="receiptModel.receiptType==='PREDEFINED'">
			        <FormLabel label-text="Receipt Template" :required="true"/>
			        <Select class="w-100" v-model="receiptModel.receiptTemplate" :options="receiptTemplates" optionLabel="label" optionValue="value" placeholder="Type of receipts"/>
		        </div>
		        
		        <div class="col-12 mb-4"
		             v-if="receiptModel.receiptType==='USER_DEFINED' && canUploadCustomTemplate">
			        <FormLabel label-text="Receipt Template" :required="true"/>
			        <MyFileUploader :multiple="false"
			                        class="w-100"
			                        @uploaded="handleUploadedFiles"/>
		        </div>
		        
		        <div class="col-12 mb-4"
		             v-if="receiptModel.receiptType!=='USER_DEFINED'">
			        <FormLabel label-text="Receipt Phone" :required="true"/>
			        <InputNumber class="w-100" v-model="receiptModel.receiptPhone"
			                     :useGrouping="false"
			                     inputId="withoutgrouping"
			                     placeholder="Receipt Phone"/>
		        </div>
		        
		        <div class="col-12 mb-4"
		             v-if="receiptModel.receiptType!=='USER_DEFINED'">
			        <FormLabel label-text="Payment UPI Id" :required="true"/>
			        <Textarea class="w-100" v-model="receiptModel.upi" placeholder="Payment UPI Id"/>
		        </div>
		        
		        <div class="col-6 mb-4">
			        <Button label="Preview"
			                icon="pi pi-eye"
			                severity="warn"
			                @click="handlePreview"
			                class="w-100"/>
		        </div>
		        <div class="col-6 mb-4">
			        <Button label="Save"
			                icon="pi pi-save"
			                severity="success"
			                @click="save"
			                class="w-100"/>
		        </div>
	        </div>
        </DashboardCard>
	    
        <DashboardCard :column="'col-md-6'"
                       :header-title="'View'"
                       v-if="foundInstitution!==null">
            <div class="d-block position- mb-4 relative p-0 m-0">
                <div class="row p-3 text-dark">
                    <div class="col-12 p-2"
                         v-if="receiptTemplateImage!==null">
	                    <div class="fw-bolder">Template  </div>
	                    <div class="d-block position-relative">
		                    <img :src="receiptTemplateImage"
		                         alt="Receipt template Background"
		                         class="w-100"/>
	                    </div>
	                    
	                   
                    </div>
                    <hr/>
                    <div class="col-12 p-2">
                        <span class="fw-bolder">Message  </span>
                        <p class="text-uppercase">{{ foundInstitution.receiptMessage }}</p>
                    </div>
                    <hr/>
                    <div class="col-12 p-2">
                        <span class="fw-bolder">Bible verse  </span>
                        <p class="text-uppercase"> {{ foundInstitution.bibleVerse }}</p>
                       
                    </div>
	                <div class="col-12 p-2">
                        <span class="fw-bolder">Contact Phone</span>
                        <p class="text-uppercase">{{ foundInstitution.receiptPhone===null ? '---' :  foundInstitution.receiptPhone }}</p>
                    </div>
	                
	                <div class="col-12 p-2">
                        <span class="fw-bolder">UPI</span>
                        <p>{{ foundInstitution.upi===null ? '---' :  foundInstitution.upi }}</p>
                    </div>
                    <div class="col-10 p-2 fw-bolder">
                    </div>
                    
                    <div class="col-md-12">
                        <Button label="Update"
                                icon="pi pi-pencil"
                                severity="warn"
                                size="small"
                                @click="updateReceipt"
                                outlined
                                class="w-100"/>
                    </div>
                </div>
            </div>
	
        </DashboardCard>
	    
	    
	    <Dialog v-model:visible="previewVisible"
	            :modal="true"
	            :draggable="false"
	            class="w-lg-75 w-md-100"
	            header="Receipt Preview"
	            :closable="true">
		    <div class="position-relative">
			    <ContributionReceipt :receiptData="receipt"
			                         class="mx-auto"
			                         v-if="receipt!==null"/>
		    </div>
	    </Dialog>
    </DashboardContainer>
</template>


<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import {RECEIPT_MODEL, RECEIPT_TEMPLATE} from "@/dashboard/configuration/receipt/utils/receiptModel";
import Button from "primevue/button";
import Textarea from "primevue/textarea";
import InputText from "primevue/inputtext";
import InputNumber from "primevue/inputnumber";
import Dialog from "primevue/dialog";
import Select from "primevue/select";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {CHURCH_MAIN_BRANCH_ADMINISTRATORS, SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

import image1 from "@/dashboard/church/contributions/receipts/bgs/Template 1 - Receipt.png"
import image2 from "@/dashboard/church/contributions/receipts/bgs/Template 2 - Receipt.png"
import image3 from "@/dashboard/church/contributions/receipts/bgs/Template 3 - Receipt.png"
import image4 from "@/dashboard/church/contributions/receipts/bgs/Template 4 - Receipt.png"
import image5 from "@/dashboard/church/contributions/receipts/bgs/Template 5 - Receipt.png"
import ContributionReceipt from "@/dashboard/church/contributions/receipts/ContributionReceipt.vue";
import FormLabel from "@/components/FormLabel.vue";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";

export default {
    name: "Receipt",
    components: {
	    MyFileUploader,
	    FormLabel,
	    Textarea,
	    Select,
	    InputNumber,
	    Dialog,ContributionReceipt, DashboardCard, DashboardContainer, Button, InputText},
	computed: {
		
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			if(CHURCH_MAIN_BRANCH_ADMINISTRATORS.includes(loggedInUser.userType)){
				let branchInstitution = this.$store.getters.getInstitution;
				return branchInstitution===null ? loggedInUser.institution : branchInstitution;
			}
			return loggedInUser.institution;
		},
		canUploadCustomTemplate: function(){
			let output = false;
			if(this.institution!==null){
				let allowedFeatures = this.institution.allowedFeatures;
				if(typeof allowedFeatures!=='undefined' && allowedFeatures!==null){
					output = allowedFeatures.includes("CUSTOM_RECEIPT");
				}
			}
			return output;
		},
		receiptTemplateImage: function(){
			if(this.receiptModel.receiptType==='PREDEFINED' || this.customReceiptImage===null) {
				let template = this.receiptModel.receiptTemplate;
				if (template === null) return null;
				let image = ""
				switch (template) {
					case "TEMPLATE_1":
						image = image1;
						break;
					
					default:
					case "TEMPLATE_2":
						image = image2;
						break;
					
					case "TEMPLATE_3":
						image = image3;
						break;
					
					case "TEMPLATE_4":
						image = image4;
						break;
					
					case "TEMPLATE_5":
						image = image5;
						break;
				}
				return image;
			}
			return this.customReceiptImage
		},
		receipt: function(){
			return {
				institution: {
					...this.institution,
					receiptTemplate: this.customReceiptImage!==null
						? this.receiptModel.customModel
						: this.receiptModel.receiptTemplate,
					bibleVerse: this.receiptModel.bibleVerse,
					receiptMessage: this.receiptModel.receiptMessage,
					logo: this.institutionLogo,
					customModel: this.customReceiptImage
				},
				receiptNo: 'Receipt No',
				entryDate: new Date(),
				donations: [],
				member: {
					firstName: 'First Name',
					lastName: 'Last name',
					phone: 'XXXXXXXXXX',
					phoneCode: 91,
					code: 'code',
					addressLine1: 'addressLine1',
					addressLine2: 'addressLine2',
					addressLine3: 'addressLine3',
					district: 'district',
					pincode: 'pincode',
					state: 'state',
					email: "dummy-email@email.com"
				}
			};
		},
		customModelOptions: function(){
			return this.receiptTemplates.filter(option=>{
				return ["TEMPLATE_1", "TEMPLATE_2"].includes(option.value);
			}).map(option=>{
				option.label = (option.value==="TEMPLATE_1") ? "Model 1" : "Model 2";
				return option;
			});
		}
	},
    data(){
        return{
			receiptTypeOptions: [{label: "Predefined", value:"PREDEFINED"},{label: "Custom Upload", value:"USER_DEFINED"}],
	        receiptTemplates: [...RECEIPT_TEMPLATE],
            okButton: {
                label: "Save",
                icon: "pi pi-save"
            },
	        previewButton: {
                label: "Preview",
                icon: "pi pi-eye",
		        event: "customClick",
		        disabled : "allo"
            },
            templates : [...RECEIPT_TEMPLATE],
            receiptModel: {...RECEIPT_MODEL},
            foundInstitution : null,
            singleTemplate : null,
            template : [],
	        updatedReceipt : {},
	        accounts: [],
	        accountsForm: [{name: null}],
	        isEditing: false,
	        image:null,
	        previewVisible: false,
	        institutionLogo: null,
	        customReceiptImage: null,
        }
    },
    methods : {
		getCustomTemplateImage: function(fileName ,initialize){
			this.$api.get("/files/get/" + fileName,
				{responseType: 'blob'})
				.then(response => {
					this.customReceiptImage = URL.createObjectURL(response);
					
					
					if(initialize){
						this.receiptModel = {
							...this.receiptModel,
							receiptTemplate: null,
							receiptType: "USER_DEFINED",
							customModel: this.institution.customModel
						};
						this.foundInstitution = {
							...this.institution,
							customReceiptTemplate: this.customReceiptImage
						}
					}
					this.$store.commit("setLoading", false);
				}).catch(ignored => {
				this.$root['showAlert']('warn', "Custom Receipt", "Failed to load custom receipt.");
				this.$store.commit("setLoading", false);
			});
		},
	    handleUploadedFiles: function(file){
			this.$store.commit("setLoading", true);
		    this.canProcessUploaded = false;
		    this.receiptModel.customReceiptTemplate = file.fileName;
		    
		    this.getCustomTemplateImage(file.fileName);
	    },
		addAccount: function(){
			this.accountsForm.push({name: null});
		},
	    removeAccount: function(index){
			this.accountsForm.splice(index, 1);
	    },
	    engageUpdate: function(creditAccount){
		    this.isEditing = true;
		    this.accountsForm = [{...creditAccount}];
	    },
	    deleteAccount: function(id){
		    this.$api.delete(`/institution/delete-account/${id}`).then(response => {
			    this.accountsForm = [{name: null}];
				this.accounts = this.accounts.filter(account=>{
					return account.id!==id;
				});
			    this.isEditing = false;
			    this.$root['showAlert']("success", "Credit Account Deletion", response.message);
		    }).catch(error => {
			    this.$root['handleApiError'](error, "Credit Account Deletion");
		    });
	    },
	    saveAccounts: function(){
			let data = {  },
				url = "/institution/save-accounts";
			data = this.$root['addInstitutionId'](data);
			if(typeof this.accountsForm[0].id!=="undefined"){
				url = "/institution/update-account";
				data = {
					...data,
					name: this.accountsForm[0].name,
					id:this.accountsForm[0].id
				};
			} else {
				data = {
					...data,
					creditAccounts: this.accountsForm
				};
			}
		    this.$api.post(url, data).then(response => {
			    this.accountsForm = [{name: null}];
				if(this.isEditing){
					this.accounts = this.accounts.map(account=>{
						if(account.id===data.id){
							return response.object;
						}
						return account;
					})
				} else {
					this.accounts = [
						...response.objects,
						...this.accounts,
					];
				}
			    this.isEditing = false;
			    this.$root['showAlert']("success", "Receipt Template", response.message);
		    }).catch(error => {
			    this.$root['handleApiError'](error, "Receipt Template")
		    });
	    },
	    loadAccounts: function(){
			let data = this.$root['addInstitutionId']({});
		    this.$api.post("/institution/get-accounts", data).then(response => {
			    this.accounts = response;
			    this.isEditing = false;
			    this.$root['showAlert']("success", "Credit Accounts Loadings", "Loaded.");
		    }).catch(error => {
			    this.$root['handleApiError'](error, "Credit Accounts Loadings")
		    });
	    },
	    handlePreview: function(content){
		    this.previewVisible = true;
	    },
        save : function(){
			if(this.$root['isInstitutionSet']()) {
				let data = this.$root['addInstitutionId'](this.receiptModel);
				
				if(data.receiptType==="PREDEFINED"){
					data ={
						...data,
						customReceiptTemplate: null,
						customModel: null,
					}
				} else {
					data = {
						...data,
						receiptTemplate: null,
					}
				}
				
				
				this.$api.post("/institution/save-receipt-configuration", data).then(response => {
					this.foundInstitution = response.object;
					this.$store.commit("updateInstitutionDetails", response.object);
					if(data.receiptType==="PREDEFINED") {
						this.receiptModel = {
							...RECEIPT_MODEL,
							receiptTemplate: this.foundInstitution.receiptTemplate
						};
					} else {
						this.receiptModel = {
							...this.receiptModel,
							receiptPhone: null,
							upi: null,
							bibleVerse: null,
							receiptMessage: null,
						}
					}
					this.okButton = {
						label: "Save",
						icon: "pi pi-save"
					};
					this.$root['showAlert']("success", "Receipt Template", response.message);
				}).catch(error => {
					this.$root['handleApiError'](error, "Receipt Template")
				});
			}
        },
        loadReceipt : function(){
			if(this.$root['isInstitutionSet']()) {
				if(typeof this.institution.customReceiptTemplate!=='undefined'
					&& this.institution.customReceiptTemplate!==null){
					this.getCustomTemplateImage(this.institution.customReceiptTemplate, true);
				} else {
					let actualTemplate = this.templates.find(singleTemplate => {
						return singleTemplate.value === this.institution.receiptTemplate;
					});
					if (typeof actualTemplate !== "undefined") {
						this.receiptModel = {
							...this.receiptModel,
							receiptTemplate: actualTemplate.value
						};
					}
					this.foundInstitution = {
						...this.institution,
						receiptTemplate: actualTemplate.value
					}
				}
			}
        },
        updateReceipt :function(){
            this.okButton.label = "Update";
            this.okButton.icon  = "pi pi-refresh";
            this.receiptModel = {
                ...this.receiptModel,
                receiptTemplate : this.institution.receiptTemplate,
	            receiptMessage : this.institution.receiptMessage,
                bibleVerse: this.institution.bibleVerse,
	            receiptPhone: this.institution.receiptPhone,
	            upi: this.institution.upi
            }
        },
	    initialize: function(){
			if(this.$root['isInstitutionSet']()){
				this.loadReceipt();
				this.loadAccounts();
				this.institutionLogo = this.$root['loadInstitutionLogo']()
			}
	    }
     
    },
	watch: {
		isChurch: function(newValue){
			this.templates = [...RECEIPT_TEMPLATE];
			this.receiptModel= {...RECEIPT_MODEL};
			this.foundInstitution = null;
			this.accounts = [];
			this.singleTemplate = null;
			this.initialize();
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
    mounted() {
		this.initialize();
    }
}
</script>



<style scoped lang="scss">

</style>