<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <Dialog :modal="true" :closable="false"
            :visible="isSubscriptionListShown"
            class="w-75">
        <template #header >
        </template>
        <div class="row mt-3"
             v-for="(singleSub, index) in contributions"
             :key="index">
            <div class="col-md-4">
                <Select class="w-100"
                        v-model="singleSub.subscription"
                        optionLabel="label"
                        optionValue="value"
                        @change="handleSelectionRemove($event, singleSub.options, index)"
                        :options="singleSub.options"/>
            </div>
            <div class="col-md-6 mb-3"  >
                <InputNumber class="w-100"
                             v-model="singleSub.amount"/>
            </div>
            <div class="col-md-2" v-if="(contributions.length>=1)">
                <Button icon="pi pi-times" class="w-100"
                        @click="temporaryDeletion(singleSub,index)"/>
            </div>
        </div>
        
        
        <template #footer>
            <div class="col-m-4">
                <Button class="w-100"  label="Cancel" icon="pi pi-trash"
                        @click="$emit('closeSubscriptionDialog')"/>
            </div>
            <div class="col-md-4">
                <Button class="w-100" label="Add" @click="add"/>
            </div>
            <div class="col-md-4">
                <Button class="w-100" label="Confirm"  @click="confirm"/>
            </div>
        </template>
    
    </Dialog>
</template>

<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button"
import InputNumber from "primevue/inputnumber";
import Select from "primevue/select"
import {CHURCH_MEMBER_SUBSCRIPTION} from "@/dashboard/church/church";
export default {
    name: "SubscriptionList",
    components:{
        Dialog,
        Button,
        InputNumber,
        Select
    },
    data(){
        return{
            temp : [],
            deletedLabel: null,
            position:null,
            selectedContributionName: null,
            isBackBtnShown: false,
            positionOfTheSelected:null,
            newlyLabel:"",
            newAmount:0,
            churchContributions: [
                {label:"Mifem", value: 1},
                {label:"Ja", value: 2},
                {label:"construction", value: 3}
            ],
            localContributions: [],
            removedOptions: []
        }
    },
    computed: {
        contributions: {
            get: function(){
                return this.localContributions
            }, set: function(newValue){
                this.localContributions = newValue;
            }
        }
    },
    methods:{
        handleSelectionRemove: function(event, options, index){
            let removedOption = options.find(singleOption=>{
                return singleOption.value===event.value;
            });
            if(removedOption){
                this.removedOptions.push(event.value);
            }
        },
        add : function(){
            let allContributionsCount = this.contributions.length;
            if(this.contributions[allContributionsCount-1].subscription!==null) {
                let remainingOptions = this.churchContributions.filter(singleOption=>{
                    return !this.removedOptions.includes(singleOption.value);
                });
                let contributionModel = {
                    subscription: null,
                    amount: null,
                    options: [...remainingOptions]
                };
                this.contributions.push({...contributionModel});
            }
        },
        temporaryDeletion:function(sub,position){
            let selectedSubscription = this.contributions[position].subscription;
            if(this.contributions.length>position){
                // This means that there were contributions after the deleted one
                this.removedOptions = this.removedOptions.filter(singleValue=>{
                    return singleValue !== selectedSubscription;
                });
                let deletedOption = this.churchContributions.find(singleOption=>{
                    return singleOption.value === selectedSubscription;
                });
                if(deletedOption){
                    let i = position+1;
                    for(; i<this.contributions.length; i++){
                        let toRemainOptions = [
                            ...this.contributions[i].options,
                            {...deletedOption}
                        ],
                            toRemainValues = toRemainOptions.map(singleOption=>{
                                return singleOption.value;
                            });
                        this.contributions[i].options = this.churchContributions.filter(singleOption=>{
                            return toRemainValues.includes(singleOption.value);
                        })
                    }
                }
            }
            this.contributions.splice(position, 1);
        },
        confirm: function(){
            console.log(this.contributions);
        },
        initializeForm: function(){
            if(this.contributions.length===0){
                this.contributions = [{
                    ...CHURCH_MEMBER_SUBSCRIPTION,
                    options: [...this.churchContributions]
                }];
                /*this.contributions[0] = {
                    ...this.contributions[0],
                    options: {...this.churchContributions}
                }*/
            }
        }
    },
    beforeMount() {
        this.initializeForm();
    },
    props:{
        isSubscriptionListShown:{
            type:Boolean,
            default(){
                return false;
            }
        },
        
    },
    emits:['closeSubscriptionDialog'],
}
</script>

<style scoped lang="scss">

</style>