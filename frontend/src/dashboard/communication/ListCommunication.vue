<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer :style="'px-0 mx-0'"
                         @refresh="refreshPage"
                        :refresh-button="true">
        <DashboardCard header-title="List"
                       column="w-100">
            <div class="d-block position-relative p-0 m-0">
                <div class="row p-3"
                     :class="index%2!==0 ? 'bg-white' : 'bg-light'"
                     v-for="(singleTemplate, index) in templates"
                     :key="index">
                    <div class="col-12 p-2 fw-bolder">
                        <h4 class="fw-bolder">{{ singleTemplate.name }}</h4>
                    </div>
                    <div class="col-12 p-2 fw-bolder template-text-container"
                         v-if="!isEmpty(singleTemplate.emailText)">
	                    <div v-html="singleTemplate.emailText"/>
                    </div>
	                <div class="col-12 p-2"
	                     v-if="!isEmpty(singleTemplate.smsText)">
		                {{ singleTemplate.smsText }}
                    </div>
	                <div class="col-12 p-2"
	                     v-if="!isEmpty(singleTemplate.whatsappTemplate)">
		                {{ singleTemplate.whatsappTemplate }}
                    </div>
                    
                    <div class="col-md-6">
                        <Button label="Update"
                                icon="pi pi-pencil"
                                severity="warn"
                                size="small"
                                @click="updateTemplate(singleTemplate)"
                                outlined
                                class="w-100"/>
                    </div>
                    <div class="col-md-6">
                        <Button label="Delete"
                                icon="pi pi-trash"
                                size="small"
                                @click="removeTemplate(singleTemplate, index)"
                                outlined
                                severity="danger"
                                class="w-100"/>
                    </div>
                </div>
                
                <div class="d-block position-relative"
                     v-if="templates.length===0">
                    <h3 class="my-0 text-danger text-center">No Templates(found)</h3>
                </div>
            </div>
        </DashboardCard>
    </DashboardContainer>
</template>
<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import Button from "primevue/button";
import {isEmpty} from "@/utils/AppStringUtils";
export default {
    name: "ListCommunication",
    components: {
        DashboardCard,
        Button,
        DashboardContainer
    },
    emits:["sendTemplate"],
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		}
	},
    data(){
        return {
            templates : [],
            count  : 0,
        }
    },
    methods:{
	    isEmpty,
        loadTemplates: function(){
            let data = this.$root['addInstitutionId']({});
            this.$api.post("/communication/get/template", data).then(response=>{
                this.templates = response.objects.map(singleTemplate=>{
					return {
						...singleTemplate,
						name: singleTemplate.name.toUpperCase()
					}
                });
            }).catch(error=>{
                this.$root['handleApiError'](error, "Template(s) Loading");
            });
        },
        refreshPage : function(){
			if(this.$root["isInstitutionSet"]()) {
				this.loadTemplates();
			}
        },
        updateTemplate : function(template){
            this.templateData  = {
                ...this.templateData,
                id : template.id,
                institutionId : template.institutionId,
                type :  template.type,
                text  : template.text,
                style : template.style,
            }
			template = {
				...template,
				communicationWays: (template.communicationWays!==null && template.communicationWays.length>0)
					? template.communicationWays[0]
					: null
			};
            this.isEditing = true;
	        this.$emit("sendTemplate", template);
	        let url = this.isInstitutionChurch
		        ? "/church/communication/template/1"
		        : "/organizations/communication/template/1";
	        this.$router.push(url);
        },
        removeTemplate : function(template,  index){
            this.$api.delete("/communication/delete-template/"+template.id).then(response=>{
                this.templates.splice(index ,1);
                this.$root['showAlert']("success", "Deletion", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, 'Deletion');
            })
        },
    },
    beforeMount() {
        this.loadTemplates();
    },
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		}
	},
}
</script>



<style>
	.template-text-container p{
		line-height: normal !important;
		margin-bottom: 0 !important;
	}
</style>