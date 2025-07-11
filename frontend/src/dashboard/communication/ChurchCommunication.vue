<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :show-institution-name="true"
	                    :style="'ps-0 mx-0'">
		<CommunicationCompose :isChurch="isInstitutionChurch"/>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import CommunicationCompose from "@/dashboard/communication/CommunicationCompose.vue";

export default {
    name: "ChurchCommunication",
    components: {
        CommunicationCompose,
        DashboardContainer,
    },
    data(){
        return{
            showCompose: false,
            showTemplate : false,
            options:[
                {label:"Compose"},
                {label:"Template"},
            ]
        }
    },
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch
		}
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>