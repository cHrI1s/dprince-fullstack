<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardCard header-title="Subscription plan" >
        <FormGenerator :ok-button="okButton"
                       :model="subscriptionPlan"
                       @click="saveSubscriptionPlan"
                       :form="formUI" />
    </DashboardCard>
</template>
<script>
import FormGenerator from "@/components/form/FormGenerator.vue";
import { SUBSCRIPTION_MODEL , ORGANIZATION_TYPES} from "@/dashboard/organization/Organization";
import {GENERAL_PLANS} from "@/dashboard/configuration/subscription-plan/utils/subscription-plan";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
export default {
    name: "CreateSubscriptionPlan",
    components: { DashboardCard,  FormGenerator, },
    data(){
        return{
            subscriptionPlanModel: {...this.defaultPlan},
            localSubscriptionPlan : [],
            features: [...GENERAL_PLANS],
            okButton: {
                label: "Save",
                icon : "pi pi-save"
            }
        }
    },
    methods:{
        saveSubscriptionPlan: function(){
            this.$api.post("/plan/save", this.subscriptionPlan).then(response=>{
                this.subscriptionPlan = {...SUBSCRIPTION_MODEL};
                this.$root['showAlert']("success", "Subscription Plan", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, "Subscription Plan");
            });
        },
    },
	computed: {
		institutionType: function(){
			if(typeof this.subscriptionPlan.institutionType!=='undefined') return this.subscriptionPlan.institutionType;
			return null;
		},
		subscriptionPlan: {
			get:function(){
				return this.subscriptionPlanModel;
			},set: function(newValue){
				this.subscriptionPlanModel = {...newValue};
			}
		},
		formUI: function(){
			return [
				{type: "TEXT", model: "name", label: 'Name', required: true, styleClass: 'col-md-6 mb-4'},
				{type: "SELECT", model: "institutionType", label: 'Institution Type'
					, required: true, styleClass: 'col-md-6 mb-4', options:[...ORGANIZATION_TYPES]},
				{type: "NUMBER", model: "emails", label: 'Emails', required: true, styleClass: 'col-md-6 mb-4'},
				{type: "NUMBER", model: "smses", label: 'Sms', required: true, styleClass: 'col-md-6 mb-4'},
				{type: "NUMBER", model: "whatsapp", label: 'Whatsapp', required: true, styleClass: 'col-md-6 mb-4'},
				{type: "NUMBER", model: "members", label: 'Members', required: false, styleClass: 'col-md-6 mb-4'},
				{type: "NUMBER", model: "price", label: 'Price', required: false, styleClass: 'col-md-6 mb-4'},
				{type: "NUMBER", model: "admins", label: 'Admins', required: false, styleClass: 'col-md-6 mb-4'},
				{
					type: "NUMBER", model: "families", label: 'Families',
					required: false, styleClass: (this.institutionType!=='CHURCH') ? 'col-12 mb-4' : 'col-6 mb-4'
				},
				{
					type: "NUMBER",
					model: "churchBranches",
					label: 'Church Branches',
					required: false,
					shownIf: {attribute: "institutionType", equals: 'CHURCH'},
					styleClass: 'col-md-6 mb-4'
				},
				{
					type: "MULTISELECT", model: "features", label: 'Features',
					required: false, styleClass: 'col-12 mb-4', options: [...GENERAL_PLANS]
				}
			]
		}
	},
    props:{
        defaultPlan : {
			type: Object,
			required: true,
	        default(){
				return {...SUBSCRIPTION_MODEL};
	        }
        },
        changeLabel : {
            type:String,
            default(){
                return "";
            }
        },
    },
    beforeMount(){
		this.subscriptionPlanModel = {...this.defaultPlan};
    }
}
</script>


<style scoped lang="scss">
</style>