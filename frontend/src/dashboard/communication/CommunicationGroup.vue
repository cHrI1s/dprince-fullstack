<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<script>
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
export default {
    name: "CommunicationGroup",
    components: {
        DashboardCard,
        Button,
        InputText
    },
    data(){
        return{
            inputs:{
                groups: [{name: null}]
            },
            isEditing: false,
            groups:[]
        }
    },
    methods:{
        addGroup : function(){
            this.inputs.groups.push({name: null})
        },
        deleteGroup : function(index){
            this.inputs.groups.splice(index, 1)
        },
        save: function(){
	        this.inputs = this.$root['addInstitutionId'](this.inputs);
            this.$api.post("/group/save", this.inputs).then(response=>{
                this.loadGroups();
                this.$root['showAlert']('success', "Group", response.message);
                this.isEditing = false;
            }).catch(error=>{
                this.$root['handleApiError'](error, "Group Save");
            });
        },
        loadGroups: function(){
            this.$api.get("/group/get").then(response=>{
                this.groups = response;
            }).catch(error=>{
                this.$root['handleApiError'](error, "Group(s) Loading");
            })
        },
        updateGroup: function(group){
            this.isEditing = true;
            this.inputs.groups = [{...group}];
        },
        removeGroup: function(group){
            this.$api.delete(`/group/delete/${group.id}`).then(response=>{
                this.groups = this.groups.filter(singleGroup=>{
                    return singleGroup.id!==group.id;
                });
                this.$root['showAlert']('success', "Group Deletion", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, "Group Deletion.");
            });
        }
        
    },
    beforeMount() {
        this.loadGroups();
    }
}
</script>

<template>
    <div class="row">
        <DashboardCard  header-title="Group" :column="'col-md-6 mb-3'">
            <div class="row mt-3" v-for="(singleGroup, index) in inputs.groups" :key="index">
                <div class="col-md-10 mb-3"
                     :class="(inputs.groups.length>1)? 'col-md-10' : 'col-md-12'">
                    <InputText v-model="singleGroup.name"
                               placeholder="Group name" class="w-100"/>
                </div>
                <div class="col-md-2" v-if="inputs.groups.length>1">
                    <Button class="w-100" @click="deleteGroup(index)"
                            severity="danger"
                            icon="pi pi-times"/>
                </div>
            
            </div>
            <div class="row mt-3">
                <div class="col-md-6 mb-3">
                    <Button class="w-100"
                            @click="addGroup"
                            severity="info"
                            label="Add Group"
                            icon="pi pi-plus"/>
                </div>
                <div class="col-md-6">
                    <Button class="w-100" @click="save"
                            severity="primary"
                            label="Save"
                            icon="pi pi-save"/>
                </div>
            </div>
        </DashboardCard>
	    
        <DashboardCard header-title="List" :column="'col-md-6'">
            <div class="d-block position-relative p-0 m-0">
                <div class="row p-3"
                     :class="index%2!==0 ? 'bg-light' : 'bg-dark'"
                     v-for="(singleGroup, index) in groups"
                     :key="index">
                    <div class="col-12 p-2 fw-bolder" :class="index%2===0 ?'text-white' : ''">
                        {{ singleGroup.name }}
                    </div>
                    
                    <div class="col-md-6">
                        <Button label="Update"
                                icon="pi pi-pencil"
                                severity="warn"
                                size="small"
                                @click="updateGroup(singleGroup, singleGroup.id)"
                                outlined
                                class="w-100"/>
                    </div>
                    <div class="col-md-6">
                        <Button label="Delete"
                                icon="pi pi-trash"
                                size="small"
                                @click="removeGroup(singleGroup)"
                                outlined
                                severity="danger"
                                class="w-100"/>
                    </div>
                </div>
            </div>
        </DashboardCard>
    </div>
</template>

<style>

</style>