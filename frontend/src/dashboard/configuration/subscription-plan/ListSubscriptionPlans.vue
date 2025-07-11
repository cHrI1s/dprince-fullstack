<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :refresh-button="true"
	                    class="bg-white"
	                    @refresh="loadSubscriptions">
		<div class="row">
			<div class="col-md-6 mb-4">
				<InstitutionPlansList :plans="organizationPlans"
				                      @edit="deleteSubscription"
				                      @update="updateSubscription"/>
			</div>
			<div class="col-md-6 mb-4">
				<InstitutionPlansList :plans="churchPlans"
				                      @edit="deleteSubscription"
				                      @update="updateSubscription"/>
			</div>
		</div>
	</DashboardContainer>
</template>
<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {DEFAULT_PAGE} from "@/dashboard/utils/default_values";
import Button from "primevue/button";
import InstitutionPlansList from "@/dashboard/configuration/subscription-plan/InstitutionPlansList.vue";
export default {
    name: "ListSubscriptionPlans",
    components: {
	    DashboardContainer,
	    InstitutionPlansList
    },
	emits: ["update", "changeTitle"],
    data() {
        return {
            tab : 0,
            subscriptionPlans: [],
	        churchPlans: [],
	        organizationPlans: []
        }
    },
    methods: {
        updateSubscription: function (subscription) {
			this.$emit("update", {...subscription});
            this.$emit("changeTitle", "Update");
        },
        deleteSubscription: function (id, index, institutionType) {
            let data  = {
                id : id,
                subscriptionPlan : id
            }
            
            this.$api.post("/plan/delete", data).then(response => {
                this.$root['showAlert']("success", "Deletion", response.message);
				if(institutionType==='CHURCH') this.churchPlans.splice(index, 1);
				else this.organizationPlans.splice(index, 1);
            }).catch(error => {
                this.$root['handleApiError'](error, "Deletion");
            });
        },
        loadSubscriptions: function () {
			this.$store.commit("setLoading", true);
            this.$api.get("/plan/get-all").then(response => {
				if(response!==null) {
					response.forEach(plan => {
						if (plan.institutionType === 'CHURCH') {
							this.churchPlans = [
								...this.churchPlans,
								plan
							].map(plan=>{
								return {
									...plan,
									name: plan.name.toUpperCase()
								}
							});
						} else {
							this.organizationPlans = [
								...this.organizationPlans,
								plan
							].map(plan=>{
								return {
									...plan,
									name: plan.name.toUpperCase()
								}
							});
						}
					});
				}
                this.subscriptionPlans = response;
                this.$root['showAlert']("success", "Subscription(s) loading", "Loaded");
	            this.$store.commit("setLoading", false);
            }).catch(error => {
	            this.$store.commit("setLoading", false);
                this.$root['handleApiError'](error, "Subscription(s) Loading");
            })
        }
    },
    
    beforeMount() {
        this.loadSubscriptions();
    }
}


</script>



<style scoped lang="scss">

</style>