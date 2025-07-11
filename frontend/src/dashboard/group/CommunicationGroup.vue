<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="row mx-0">
		<DashboardCard  header-title="Group" :column="'col-md-6 mb-4 pe-md-2 ps-md-0 px-0'">
			<div class="row mb-4 mx-0" v-for="(singleGroup, index) in inputs.groups" :key="index">
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
			
			
			<div class="row mx-0">
				<div class="col-md-6 mb-4">
					<Button class="w-100"
					        @click="addGroup"
					        severity="info"  label="Add Group"
					        icon="pi pi-plus"/>
				</div>
				<div class="col-md-6">
					<Button class="w-100" @click="save" severity="primary"  label="Save"  icon="pi pi-save"/>
				</div>
			</div>
		</DashboardCard>
		
		<DashboardCard header-title="List"
		               :column="'col-md-6 mb-4 pe-md-0 ps-md-2 px-0'"
		               :refresh-button="true"
		               @refresh="loadGroups">
			<div class="d-block position-relative p-0 m-0">
				<div class="row p-3 border-top"
				     :class="{'bg-light': index%2===0, 'border-bottom' : foundGroups.length===index+1}"
				     v-for="(singleGroup, index) in foundGroups"
				     :key="index">
					<div class="col-12 p-2 fw-bolder text-uppercase">
						{{ singleGroup.name }}
					</div>
					
					<div class="col-md-4">
						<Button label="Update"
						        icon="pi pi-pencil"
						        severity="warn"
						        size="small"
						        @click="updateGroup(singleGroup, singleGroup.id)"
						        outlined
						        class="w-100"/>
					</div>
					<div class="col-md-4">
						<Button label="Delete"
						        icon="pi pi-trash"
						        size="small"
						        @click="removeGroup(singleGroup)"
						        outlined
						        severity="danger"
						        class="w-100"/>
					</div>
                    <div class="col-md-4">
                        <Button label="Member(s)"
                                icon="pi pi-users"
                                size="small"
                                @click="$emit('listMember',singleGroup)"
                                outlined
                                severity="info"
                                class="w-100"/>
                    </div>
				</div>
			</div>
		</DashboardCard>
	</div>
</template>

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
            foundGroups:[]
        }
    },
    emits:['listMember'],
    methods:{
        
        addGroup : function(){
            this.inputs.groups.push({name: null})
        },
        deleteGroup : function(index){
            this.inputs.groups.splice(index, 1)
        },
        save: function(){
			if(this.$root['isInstitutionSet']()) {
				let data = this.$root["addInstitutionId"]({...this.inputs});
				this.$api.post("/group/save", data).then(response => {
					let groupsIds = response.objects.map(singleGroup=>{
						return singleGroup.id;
					});
					this.foundGroups = [
						...response.objects,
						...this.foundGroups.filter(singleGroup=>{
							return !groupsIds.includes(singleGroup.id);
						})
					];
					this.inputs.groups = [{name:null}];
					let message = (this.isEditing) ? "Updated" : "Saved";
					this.$root['showAlert']('success', "Group", message);
					this.isEditing = false;
				}).catch(error => {
					this.$root['handleApiError'](error, "Group Save");
				});
			}
        },
	    loadGroups: function(){
			if(this.$root['isInstitutionSet']()) {
				let data = this.$root["addInstitutionId"]({});
				this.$api.post("/group/get", data).then(response => {
					this.foundGroups = response;
				}).catch(error => {
					this.$root['handleApiError'](error, "Group(s) Loading");
				});
			}
	    },
        updateGroup: function(group){
            this.isEditing = true;
            this.inputs.groups = [{...group}];
        },
        removeGroup: function(group){
            this.$api.delete(`/group/delete/${group.id}`).then(response=>{
                this.foundGroups = this.foundGroups.filter(singleGroup=>{
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

<style>

</style>