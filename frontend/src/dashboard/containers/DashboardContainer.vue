<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<div class="pt-4 container-fluid rounded-3 px-0">
			<div class="pt-4 mb-4 px-3"
			     v-if="showInstitutionName">
				<InstitutionHeader/>
			</div>
		</div>
		<div class="pt-0 pb-4 container-fluid rounded-0 ps-lg-0 pe-lg-4 px-md-4 px-0"
		     :class="style">
			<div class="row justify-content-end mb-4"
			     v-if="refreshButton">
				<div class="col-lg-3 col-md-4">
					<Button label="Refresh"
					        icon="pi pi-refresh fw-bolder"
					        severity="info"
					        class="w-100 fw-bolder"
					        @click="refresh"/>
				</div>
			</div>
			
			<slot/>
		</div>
	</div>
</template>


<script>
import Button from "primevue/button";
import InstitutionHeader from "@/dashboard/containers/parts/InstitutionHeader.vue";
export default {
	name: "DashboardContainer",
	emits: ["refresh"],
	components: {InstitutionHeader, Button},
	methods: {
		refresh: function(){
			this.$emit("refresh");
		}
	},
    props:{
        style: {
            type: String,
            default(){
                return null;
            }
        },
	    refreshButton: {
			type: Boolean,
		    required: false,
		    default(){
				return false;
		    }
	    },
	    showInstitutionName: {
		    type: Boolean,
		    required: false,
		    default(){
			    return false;
		    }
	    },
    }
}
</script>

<style scoped lang="scss">

</style>