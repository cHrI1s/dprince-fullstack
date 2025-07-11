<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="row">
		<div class="col-md-6 mb-4">
			<ChurchContributionCreator :contributions="contributionModel"
			                           :isUpdate="isUpdate"
			                           @update="handleUpdate"
			                           @save="handleSave"/>
		</div>
		
		<div class="col-md-6 mb-4">
			<div class="card">
				<div class="card-header">
					<h3 class="fw-bolder my-0">
						Offerings
					</h3>
				</div>
				<div class="card-body px-0 pt-0">
					<div class="d-block position-relative">
						<div class="d-block position-relative p-lg-3 p-md-2"
						     :class="index%2===0 ? 'bg-light' : ''"
						     v-for="(contribution, index) in churchContributions"
						     :key="index">
							<div class="fw-bolder mb-2 text-dark text-uppercase">
								{{ contribution.name }}
							</div>
							<div class="row">
								<div class="col-6">
									<Button label="Update"
									        size="small"
									        outlined
									        @click="updateContribution(contribution)"
									        class="w-100"
									        severity="warn"/>
								</div>
								<div class="col-6">
									<Button label="Delete"
									        size="small"
									        @click="deleteContribution(contribution)"
									        outlined
									        class="w-100"
									        severity="danger"/>
								</div>
							</div>
						</div>
						
						<div class="text-danger fw-bolder text-center">
							<h4 class="text-danger fw-bolder text-center"
							    v-if="churchContributions.length===0">
								No Contribution(s) Created Yet
							</h4>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import ChurchContributionCreator from "@/dashboard/church/contributions/ChurchContributionCreator.vue";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
export default {
	name: "ContributionCreatorPad",
	components: {
		Button,
		ChurchContributionCreator,
	},
	computed: {
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
	},
	data(){
		return{
			contributionCreatorOpened: false,
			isUpdate : false,
			churchContributions: [],
			contributionModel: [{name: null}],
		}
	},
	methods:{
		updateContribution: function(contribution){
			this.isUpdate = true;
			this.contributionModel = [{...contribution}];
		},
		deleteContribution: function(contribution){
			this.$api.delete(`/contribution/delete/${contribution.id}`).then(response=>{
				this.$root['showAlert']("success", "Contributions Deletion", response.message);
				this.churchContributions = this.churchContributions.filter(singleContribution=>{
					return contribution.id!==singleContribution.id;
				})
			}).catch(error=>{
				this.$root['handleApiError'](error,  "Contributions Deletion");
			});
		},
		handleUpdate: function(rows){
			this.churchContributions = this.churchContributions.map(contribution=>{
				if(contribution.id===rows[0].id) return rows[0];
				return contribution;
			});
			this.contributionModel = [{name: null}];
			this.isUpdate = false;
		},
		handleSave: function(row){
			this.churchContributions = [
				...this.churchContributions,
				...row
			];
			this.contributionModel = [{name: null}];
			this.isUpdate = false;
		},
		loadContributions: function(){
			let url = "/contribution/get";
			if(this.institution!==null) url += "/"+this.institution.id;
			this.$api.get(url).then(response=>{
				this.churchContributions = response;
				this.$root['showAlert']("success", "Contributions Loading.", "Loaded");
			}).catch(error=>{
				this.$root['handleApiError'](error,  "Contributions Loading.");
			})
		},
		initialize: function(){
			if(this.$root['isInstitutionSet']()) {
				this.loadContributions();
			}
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">

</style>