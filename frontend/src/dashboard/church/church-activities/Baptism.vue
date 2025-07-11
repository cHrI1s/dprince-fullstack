<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer >
        <DashboardCard header-title="Baptism">
            <div class="row">
                <div class="col-12 mb-3">
                    <DatePicker  class="w-100" v-model="baptizedModel.date" placeholder="Date of Baptism"/>
                </div>
                <div class="col-12 mb-3">
                    <AutoComplete class="w-100"
                                  inputClass="w-100"
                                  placeholder="To: Type to search a Priest"
                                  :suggestions="foundMembers"
                                  optionLabel="label"
                                  @complete="search"
                                  dataKey="code"
                                  v-model="baptizedModel.baptist"/>
                </div>
                <div class="col-12">
                    <Button label="Save" class="w-100" icon="pi pi-save" @click="save"/>
                </div>
            </div>
        </DashboardCard>
    </DashboardContainer>
</template>
<script>

import Button from "primevue/button";
import DatePicker from "primevue/datepicker";
import AutoComplete from "primevue/autocomplete";
import {BAPTIZED_MODEL} from "@/dashboard/church/church";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";

export default {
    name: "Baptism",
    components: {DashboardContainer, DatePicker, DashboardCard, AutoComplete,Button},
    data(){
        return{
            baptizedModel:{...BAPTIZED_MODEL},
            foundMembers:[],
            tempArray:[]
        }
    },
    methods:{
        save: function(){
            let baptist = this.baptizedModel.baptist.code;
            
            let data = {
                ...this.baptizedModel,
                baptist,
                baptismalCandidateId : this.$store.getters.getTmpInstitutionMember.id
            };
            this.$api.post("/institution/member-baptism", data).then(response=>{
                this.$root['showAlert']('success', "Baptism", response.message);
                this.baptizedModel = {...BAPTIZED_MODEL};
            }).catch(error=>{
                this.$root['handleApiError'](error, "Christening")
            })
            
        },
        search: function(event){
            let data = { query: event.query }
            data = this.$root['addInstitutionId'](data);
            this.$api.post("/institution/search-member", data).then(response=>{
                this.tempArray = response.map(singleMember=>{
                    if(singleMember.churchFunction==="PRIEST"){
                        return {
                            label:singleMember.firstName+ " "+singleMember.lastName,
                            code : singleMember.id,
                        }
                    }
                });
                
                this.foundMembers = this.tempArray.filter((singleFiltered=>{
                    return singleFiltered!==undefined;
                }))
                
            }).catch(error=>{
                this.$root['handleApiError'](error, "Member searching");
            })
        },
    }
}
</script>



<style scoped lang="scss">

</style>