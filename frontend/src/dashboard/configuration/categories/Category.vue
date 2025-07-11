<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :style="'px-0'">
		<div class="row justify-content-end">
			<div class="col-md-3 mb-4">
				<Button class="w-100"
				        label="Refresh"
				        icon="pi pi-refresh"
				        @click="initializeComponent"
				        severity="warn"/>
			</div>
			<div class="col-md-3 mb-4">
				<Button class="w-100"
				        label="Next"
				        icon="pi pi-arrow-right"
				        @click="goToReceipts"
				        severity="info"/>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12 mb-4">
				<DashboardCard :header-title="(isChurch ? 'Types Creation' : 'Categories Creation')">
                    <div class="mb-4"
					     v-for="(category, index) in categoriesToCreate.categories"
                         :key="index">
          
						<div class="d-flex">
							<InputText placeholder="Category name"
							           v-model="category.name"
							           class="w-100"/>
							
							<Button icon="pi pi-times"
							        severity="danger"
							        class="ms-4"
							        :disabled="index===0"
							        @click="removeCategoryToInCreation(index)"/>
						</div>
					</div>
					
					<div class="row position-relative">
						<div class="col-6">
							<Button :label="(isChurch ? 'Add Church Type' : 'Add Category')"
                                    :disabled="isEditing"
							        @click="addCategoryToInCreation"
							        icon="pi pi-plus"
							        severity="info"
							        class="w-100"/>
						</div>
						
						
						<div class="col-6">
							<Button :label="(this.isEditing===true) ? 'Update' : 'Save'"
                                    :icon="(this.isEditing===true) ? 'pi pi-refresh' : 'pi pi-save'"
                                    :severity="(this.isEditing===true) ? 'warn' : 'success'"
                                    @click="saveCategory"
							        class="w-100"/>
						</div>
					</div>
				</DashboardCard>
			</div>
			
			<div class="col-md-12 mb-4">
				<DashboardContainer class="bg-white">
					<CategoryLister :categories="categories"
					                :isChurch="isInstitutionChurch"
					                @update="updateCategory"
					                @delete="deleteCategory"
					                :title="institutionName"/>
				</DashboardContainer>
			</div>
		</div>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import InputText from "primevue/inputtext";
import Button from "primevue/button";
import {CATEGORY_MODEL} from "@/dashboard/organization/Organization";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {ORGANIZATION_TYPES} from "@/dashboard/organization/Organization";
import CategoryLister from "@/dashboard/configuration/categories/CategoryLister.vue";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
export default {
	name: "Category",
	components: {
		CategoryLister,
		DashboardCard,
		DashboardContainer,
		InputText,
		Button
	},
	data(){
		return {
			categoriesToCreate : {
                institutionType: null,
                categories: [{...CATEGORY_MODEL}]
            },
			categories: [],
            isEditing : false,
            organizationTypes : [...ORGANIZATION_TYPES],
			updating: false,
		}
	},
	methods: {
		addCategoryToInCreation : function(){
			this.categoriesToCreate.categories.push({...CATEGORY_MODEL});
		},
		removeCategoryToInCreation: function(index){
			this.categoriesToCreate.categories.splice(index, 1);
		},
        deleteCategory : function(category){
	        let title = (this.isInstitutionChurch) ? "Church Type" : "Category";
            this.$api.delete(`/category/delete/${category.id}`).then(response=>{
	            this.categories = this.categories.filter(singleCategory=>{
		            return singleCategory.id!==category.id;
	            });
                this.$root['showAlert']('success', title+" Deletion", response.message);
            }).catch(error=>{
                this.$root['handleApiError'](error, title+" Deletion.");
            });
        },
		createCategory: function(data){
			let title = (this.isInstitutionChurch) ? "Church Type(s)" : "Category(ies)";
			let url = (this.isEditing) ? "/category/update" : "/category/save";
				this.$api.post(url, data).then(response => {
				this.updating = false;
				if (this.isEditing) {
					this.categories = this.categories.map(singleCategory => {
						if (singleCategory.id === data.id) return data;
						return singleCategory;
					})
				} else {
					this.categories = [
						...response.objects,
						...this.categories,
					]
				}
				this.categoriesToCreate = {
					institutionType: null,
					categories: [{name: null}]
				};
				this.isEditing = false;
				this.$root['showAlert']('success', title, response.message);
			}).catch(error => {
				this.$root['handleApiError'](error, title);
			});
		},
        saveCategory : function(){
            let data = (this.isEditing) ? this.categoriesToCreate.categories[0]
                                        : this.categoriesToCreate;
			if(this.isInstitutionChurch){
				this.createCategory(data);
			} else {
		        if(this.$root['isInstitutionSet']()) {
			        data = this.$root['addInstitutionId'](data);
			        this.createCategory(data);
		        }
	        }
        },
        loadCategories : function(){
	        let title = (this.isInstitutionChurch) ? "Church Type(s)" : "Category(ies)";
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/category/get", data).then(response => {
				this.categories = response;
			}).catch(error => {
				this.$root['handleApiError'](error, title+" Loading");
			});
        },
        updateCategory : function(category){
			this.updating = true;
            this.categoriesToCreate = {
                institutionType: category.institutionType,
                categories: [{...category}]
            };
            this.isEditing = true;
        },
		initializeComponent: function(){
			if(this.isInstitutionChurch){
				this.loadCategories();
			} else {
				if(this.$root['isInstitutionSet']()) this.loadCategories();
			}
		},
		goToReceipts: function(){
			this.$router.push("/organizations/configuration/receipt");
		}
	},
    beforeMount() {
        this.initializeComponent();
    },
    computed:{
		isInstitutionChurch: function(){
			return this.isChurch;
		},
	    institution: function(){
		    let loggedInUser = this.$store.getters.getLoggedInUser;
		    if(loggedInUser==null) return null;
		    if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
			    return this.$store.getters.getInstitution;
		    }
		    return loggedInUser.institution;
	    },
	    institutionName: function(){
		    if(!this.isInstitutionChurch){
			    if(this.institution===null) return "No Institution Set";
				return this.institution.name;
		    }
		    return "Churches";
	    },
    },
	watch: {
		isChurch: function(newValue){
			this.categories = [];
			if(newValue) {
				this.$store.commit("setInstitution", null);
				this.loadCategories();
			} else {
				if(this.$root['isInstitutionSet']()) this.loadCategories();
			}
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
	}
}
</script>

<style scoped lang="scss">

</style>