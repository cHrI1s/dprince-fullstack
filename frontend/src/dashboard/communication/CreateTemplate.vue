<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer :style="'px-0 mx-0'">
        <CheatCode/>
	    
	    <div class="mt-4">
		    <DashboardCard header-title="Template">
			    <div class="row">
				    <div class="col-md-6 mb-4">
					    <InputText placeholder="Template name"
					               v-model="template.name" class="w-100"/>
				    </div>
				    
				    <div class="col-md-6 mb-4">
					    <Select placeholder="Template Style"
					            :options="templateStyle"
					            optionLabel="label"
					            optionValue="value"
					            v-model="template.templateStyle" class="w-100"/>
				    </div>
				    
				    <div class="col-md-12 mb-4">
					    <FormLabel label-text="Communication Way"
					               :required="true"/>
					    <Select placeholder="Template Use case"
					                 :options="communicationWays"
					                 optionLabel="label"
					                 optionValue="value"
					                 v-model="template.communicationWays"
					                 class="w-100"/>
				    </div>
				    
				    <div class="col-md-12"
				         v-if="template.communicationWays!==null && template.communicationWays.includes('MAIL')">
					    <FormLabel label-text="Email Message"
					               :required="false"/>
					    <Editor editorStyle="height: 220px"
					            v-model="template.emailText"
					            class="w-100"/>
				    </div>
				    
				    <div class="col-md-6 mb-4"
				         v-if="template.communicationWays!==null && template.communicationWays.includes('WHATSAPP')">
					    <FormLabel label-text="WhatsApp Template Language"
					               :required="true"/>
					    <Select v-model="template.language"
					            :options="whatsappLanguages"
					            placeholder="Whatsapp Template Language"
					            optionLabel="label"
					            optionValue="value"
					            class="w-100"/>
				    </div>
				    
				    <div class="col-md-6 mb-4"
				         v-if="template.communicationWays!==null && template.communicationWays.includes('WHATSAPP')">
					    <FormLabel label-text="WhatsApp Template"
					               :required="true"/>
					    <InputText v-model="template.whatsappTemplate"
					               placeholder="Name of the template"
					              class="w-100 normal-text"/>
				    </div>
				    
				    <div class="col-md-12"
				         v-if="template.communicationWays!==null && template.communicationWays.includes('SMS')">
					    <FormLabel label-text="Sms Message"
					               :required="true"/>
					    <Textarea class="w-100 normal-text"
					              placeholder="Message Text"
					              rows="7"
					              :auto-resize="true"
					              v-model="template.smsText"/>
				    </div>
			    </div>
			    
			    <div class="col-md-12 mt-3">
				    <Button class="w-100"
				            :label="(typeof this.template.id!=='undefined') ? 'Update' : 'Save'"
				            @click="save"
				            :icon="(typeof this.template.id!=='undefined') ? 'pi pi-refresh' : 'pi pi-save'"
				            severity="primary"/>
			    </div>
		    </DashboardCard>
	    </div>
    </DashboardContainer>
</template>


<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import CheatCode from "@/dashboard/communication/components/CheatCode.vue";
import Button from "primevue/button";
import Select from "primevue/select";
import Editor from "primevue/editor";
import Textarea from "primevue/textarea";
import InputText from "primevue/inputtext";
import FormLabel from "@/components/FormLabel.vue";
import {TEMPLATE_MODEL, TEMPLATE_STYLE, WHATSAPP_LANGUAGES} from "@/dashboard/communication/utils/template";
import {COMMUNICATION_WAY} from "@/dashboard/church/church";
import {isEmpty} from "@/utils/AppStringUtils";
import {AVAILABLE_LANGUAGES} from "@/dashboard/organization/Organization";

export default {
    name: "CreateTemplate",
    components: {
		CheatCode,
	    Select,
	    DashboardContainer,
	    DashboardCard,
	    Button,
	    Editor,
	    Textarea,
	    FormLabel,
	    InputText},
    data(){
        return{
            template : this.templateData,
            isEditing : false,
	        templateStyle: [...TEMPLATE_STYLE],
            data : null,
            updateTemplate : null,
	        selectedCommunicationWays: [],
        }
    },
    methods:{
        save: function(){
             let data = this.$root['addInstitutionId'](this.template);
			 data = {
				 ...data,
				 communicationWays: data.communicationWays!==null ? [data.communicationWays] : null
			 };
             if(isEmpty(data.emailText) && isEmpty(data.smsText) && isEmpty(data.whatsappTemplate)){
				 this.$root['showAlert']('warn', "Template", "At least one message should be set.");
				 return;
             }
            this.$api.post("/communication/template/save", data).then(response=>{
                this.template = {...TEMPLATE_MODEL};
	            this.$root['showAlert']("success", "Template", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, "Template");
            });
        },
    },
    
    props:{
	    templateData:{
            type: Object,
		    required: false,
            default(){
                return {...TEMPLATE_MODEL};
            }
        }
    },
    computed:{
	    whatsappLanguages: function(){
			return [...AVAILABLE_LANGUAGES].filter(language=>WHATSAPP_LANGUAGES.includes(language.value));
	    },
	    communicationWays: function(){
		    return [...COMMUNICATION_WAY].filter(way=>{
				let ACCEPTED = ["SMS", "MAIL", "WHATSAPP"];
				return ACCEPTED.includes(way.value);
		    });
	    },
    },
   
}
</script>


<style scoped lang="scss">
</style>