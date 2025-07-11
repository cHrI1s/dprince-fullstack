<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <div class="d-block position-relative">
        <div class="col-md-12">
            <InputText class="w-100 rounded-0"
                       v-model="searchQuery"
                       @input="searchCode"
                       placeholder="Search code"/>
        </div>
        <Button label="View/hide codes" class="rounded-0 mt-3" @click="viewCodes"/>
        <table class="table table-striped" v-if="codeVisibility">
            <thead>
            <tr>
                <th>Code</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(singleCode , index) in sheet" :key="index">
                <td class="fw-bolder">{{ singleCode.code }}</td>
                <td>{{ singleCode.description }}</td>
            </tr>
            
            <tr v-if="sheet.length===0">
                <td colspan="2"
                    class="fw-bolder text-center">No Code found</td>
            </tr>
            </tbody>
        </table>
    </div>
</template>
<script>
import InputText from "primevue/inputtext";
import Button from "primevue/button";
export default {
    name: "CheatCode",
    components:{
        InputText,
        Button
    },
    data(){
        return {
            foundCodes: [],
            searchQuery:null,
            searchedCode: false,
            codeVisibility: false,
            sheetLocal : [
                {
                    code        : "[[fullname]]",
                    description :"Full Name"
                },
	            {
                    code        : "[[firstname]]",
                    description :"Firstname"
                },
                {
                    code        : "[[lastname]]",
                    description : "Lastname"
                },
                {
                    code          : "[[date]]",
                    description   : "Today Date"
                },
                {
                    code        : "[[institution]]",
                    description : "Institution Name"
                },
                {
                    code        : "[[address]] or [[theaddress]]",
                    description : "institution Address"
                },
                {
                    code        : "[[amount]]",
                    description : "Amount"
                },
                {
                    code        : "[[subscriptions]]",
                    description : "Donations Names/Contributions Names"
                },
	            {
                    code        : "[[receiptpayment]]",
                    description : "Payment Mode / Mode of Payment"
                },
                {
                    code        : "[[receiptdate]]",
                    description : "Receipt Date"
                },
	            {
                    code        : "[[bibleverse]]",
                    description : "Institution Bible Verse"
                },
	            {
                    code        : "[[themail]]",
                    description : "Institution Email Address"
                },
	            {
                    code        : "[[thephone]]",
                    description : "Institution Phone number"
                },
	            {
                    code        : "[[thewebsite]]",
                    description : "Institution Website"
                },
	            {
                    code        : "[[receiptno]]",
                    description : "Receipt Number"
                },
	            {
                    code        : "[[receiptdate]]",
                    description : "Receipt Date"
                }
            ]
        }
    },
    methods:{
        viewCodes :function(){
            if(this.codeVisibility === true){
                this.codeVisibility = false
            }else{
                this.codeVisibility = true;
            }
        },
        searchCode:function (){
            this.searchedCode = (this.searchQuery.trim().length>0);
            this.sheet = this.sheetLocal.filter(singleCode=>{
                return (singleCode.description.toLowerCase().includes(this.searchQuery.toLowerCase()));
            });
        }
    },
    computed: {
        sheet: {
            get: function(){
                return (this.searchedCode) ? this.foundCodes : this.sheetLocal;
            }, set: function(newValue){
                this.foundCodes = newValue;
            }
        }
    }
}
</script>



<style scoped lang="scss">

</style>