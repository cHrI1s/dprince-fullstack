<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->
<template>
	<div :class="column">
		<div class="card">
			<div class="card-header"
			     v-if="headerTitle!==null">
				<div class="row align-items-center">
					<div class="mb-4" :class="(refreshButton || clearFormButton) ? 'col-md-8' : 'col-12'">
						<HeaderTitle :title-text="headerTitle"/>
					</div>
					
					<div class="col-md-4 mb-4"
					     v-if="refreshButton">
						<Button label="Refresh"
						        icon="pi pi-refresh fw-bolder text-white"
						        severity="warn"
						        class="w-100"
						        @click="refresh"/>
					</div>
					
					<div class="col-md-4 mb-4"
					     v-if="clearFormButton">
						<Button label="Clear Form"
						        icon="pi pi-times fw-bolder text-danger"
						        severity="danger"
						        outlined
						        class="w-100"
						        @click="clearForm"/>
					</div>
				</div>
				<hr v-if="isLineAvailable===true"/>
			</div>
			
			<div class="card-body pb-0 mb-0 pt-0"
			     :class="contentClass">
				<slot/>
			</div>
			
			<div class="card-footer pt-0"></div>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
import HeaderTitle from "@/dashboard/header-title/HeaderTitle.vue";
export default {
	name: "DashboardCard",
	components: {
        HeaderTitle,
		Button
    },
	emits: ["refresh", "clear"],
	methods: {
		refresh: function(){
			this.$emit("refresh");
		},
		clearForm: function(){
			this.$emit("clear");
		}
	},
	props: {
		refreshButton: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		},
		clearFormButton: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		},
		headerTitle : {
			type: [String, null],
			required: true,
			default(){
				return null;
			}
		},
		contentClass : {
			type: String,
			default(){
				return null;
			}
		},
        column:{
            type:String,
            default(){
                return null;
            }
        },
        marginBetweenCards:{
            type:String,
            default(){
                return null
            }
        },
        isLineAvailable:{
            type:Boolean,
            default(){
                return false;
            }
        }
        
	}
}
</script>
<style scoped lang="scss">

</style>