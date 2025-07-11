<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :style="'row'">
		<DashboardCard  :column="'col-md-6'" header-title="Title Creation">
			<div class="row mt-3" v-for="(singleInput, index) in inputs" :key="index">
				<div class="mb-3"
				     :class="(inputs.length>1)? 'col-md-3' : 'col-md-3'">
					<InputText v-model="singleInput.shortName"
					           placeholder="Abbr." class="w-100"/>
				</div>
				<div class="mb-3"
				     :class="(inputs.length>1)? 'col-md-7' : 'col-md-9'">
					<InputText v-model="singleInput.name"
					           placeholder="Title" class="w-100"/>
				</div>
				<div class="col-md-2" v-if="inputs.length>1">
					<Button class="w-100" @click="deleteTitle(index)"
					        severity="danger"
					        icon="pi pi-times"/>
				</div>
			</div>
			<div class="row mt-3">
				<div class="col-md-6">
					<Button class="w-100"
					        :disabled="isEditing"
					        @click="addTitle"
					        severity="info"
					        label="Add Title"
					        icon="pi pi-plus"/>
				</div>
				<div class="col-md-6">
					<Button class="w-100"
                            @click="save"
                            :label="(this.isEditing===true)
                            ? 'Update Title'
                            : 'Save Title' "
                            :icon="(this.isEditing===true)
                             ? 'pi pi-refresh'
                             : 'pi pi-save' "
                            :severity="(this.isEditing===true)
                            ? 'warn'
                            : 'success'"/>
				</div>
			</div>
		</DashboardCard>
		
		<DashboardCard :column="'col-md-6'" header-title="Titles">
			<div class="d-block position-relative p-0 m-0">
				<div class="row p-3"
				     :class="index%2===0 ? 'bg-light' : 'bg-transparent'"
				     v-for="(title, index) in titles"
				     :key="index">
					<div class="col-2 p-2">
						{{ title.shortName }}
					</div>
					<div class="col-10 p-2 fw-bolder">
						{{ title.name }}
					</div>
					Title
					<div class="col-md-6">
						<Button label="Update"
						        icon="pi pi-pencil"
						        severity="warn"
						        size="small"
						        @click="updateTitle(title)"
						        outlined
						        class="w-100"/>
					</div>
					<div class="col-md-6">
						<Button label="Delete"
						        icon="pi pi-trash"
						        size="small"
						        @click="removeTitle(title)"
						        outlined
						        severity="danger"
						        class="w-100"/>
					</div>
				</div>
			</div>
		</DashboardCard>
	</DashboardContainer>
</template>


<script>
    import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
    import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
    import InputText from "primevue/inputtext";
    import Button from "primevue/button";
    export default {
        name:"Title",
        components:{
            DashboardCard,
            DashboardContainer,
            InputText,
            Button
        },
        data(){
            return{
                inputs:[{name:null, shortName: null}],
	            titles: [],
	            isEditing: false
            }
        },
        methods:{
            addTitle : function(){
                this.inputs.push({name:null, shortName: null});
            },
            deleteTitle : function(index){
                this.inputs.splice(index, 1)
            },
	        removeTitle: function(title){
		        this.$api.delete(`/title/delete/${title.id}`).then(response=>{
			        this.titles = this.titles.filter(singleTitle=>{
						return singleTitle.id!==title.id;
			        });
					this.$root['showAlert']('success', "Title Deletion", response.message);
		        }).catch(error=>{
			        this.$root['handleApiError'](error, "Title Deletion.");
		        });
	        },
	        updateTitle: function(title){
				this.inputs = [{...title}];
				this.isEditing = true;
	        },
            save :function(){
                let url = (this.isEditing) ? "/title/update" : "/title/save",
                    data = (this.isEditing) ? this.inputs[0] : {titles: this.inputs};
                this.$api.post(url, data).then(response=>{
                    if(this.isEditing){
                        this.titles = this.titles.map(singleTitle=>{
                            if(singleTitle.id===data.id) return data;
                            return singleTitle;
                        })
                    } else {
                        this.titles = [
                            ...response.objects,
                            ...this.titles,
                        ]
                    }
                    this.$root['showAlert']('success', "Title save", response.message)
                    this.inputs = [{name:null, shortName: null}];
                    this.isEditing = false;
                }).catch(error=>{
                    this.$root['handleApiError'](error, "Title Save");
                });
            },
	        loadTitles: function(){
		        this.$api.get("/title/get").then(response=>{
			        this.titles = response;
		        }).catch(error=>{
			        this.$root['handleApiError'](error, "Title(s) Loading");
		        })
	        }
        },
	    beforeMount(){
			this.loadTitles();
	    }
    }
</script>

<style scoped lang="scss">

</style>