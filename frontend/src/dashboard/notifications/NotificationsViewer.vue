<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardContainer :refresh-button="true"
	                    @refresh="initiate">
		<DashboardCard header-title="Notifications">
			<div class="d-block position-relative mb-4 text-end text-sm">
				Page: <span class="fw-bolder">{{ notifications.currentPage+1 }}</span>
			</div>
			<div class="d-block position-relative mb-4 alert text-white"
			     v-for="(notification, index) in notifications.content"
			     :key="index"
			     :class="getAlertClass(notification)">
				<div class="fw-bolder mb-2">
					<h4 class="my-0">{{ notification.title }}</h4>
				</div>
				
				<div class="d-block position-relative px-2">
					{{ notification.content===null  ? '---' : notification.content }}
				</div>
				<div class="d-block position-relative px-2"
				     v-if="typeof notification.downloadLink!=='undefined' && notification.downloadLink!==null">
					<a :href="getNotificationLink(notification.downloadLink)"
					   target="_blank"
					   title="Click to Download"
					   class="text-white fw-bolder">
						{{ notification.downloadLink }}
					</a>
				</div>
				<div class="d-block position-relative text-end text-sm">
					{{ getDateFromTimeStamp(notification.notificationTime, true) }}
				</div>
			</div>
			
			<div class="d-block position-relative mb-4 text-center"
			     v-if="notifications.content.length===0">
				<h4 class="text-danger">No notification(s) Yet!</h4>
			</div>
			
			<div class="row" v-if="notifications.content.length>0">
				<div class="col-6">
					<Button label="Previous"
					        icon="pi pi-arrow-left"
					        icon-pos="left"
					        outlined
					        :disabled="(notifications.currentPage+1)<=1"
					        severity="warn"
					        @click="previous"
					        class="w-100"/>
				</div>
				<div class="col-6">
					<Button label="Next"
					        icon="pi pi-arrow-right"
					        icon-pos="right"
					        outlined
					        :disabled="(notifications.currentPage+1)>=notifications.totalPages"
					        severity="warn"
					        @click="next"
					        class="w-100"/>
				</div>
			</div>
		</DashboardCard>
	</DashboardContainer>
</template>


<script>
import Button from "primevue/button";
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {getDateFromTimeStamp} from "@/utils/AppFx";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

export default {
	name: "NotificationsViewer",
	components: {Button, DashboardCard, DashboardContainer},
	data(){
		return {
			notifications: {...PAGE_MODEL},
			searchModel : {
				page: 1,
				size: 10
			}
		}
	},
	methods: {
		getDateFromTimeStamp,
		getNotificationLink(link){
			return APP_CONFIG.BACKEND_SERVER+"/files/get-file/"+link;
		},
		getAlertClass: function(notification){
			let styleClass = null;
			switch(notification.actionType){
				case "READ":
					styleClass = "alert-info";
					break;
				case "CREATE":
					styleClass = "alert-success";
					break;
				case "UPDATE":
					styleClass = "alert-warning";
					break;
				case "DELETE":
				case "DEFAULT":
					styleClass = "alert-danger";
					break;
			}
			return styleClass;
		},
		loadNotification: function(){
			this.$api.post("/notification/get", this.searchModel).then(response=>{
				this.notifications = response;
				this.$root['showAlert']('success', "Notifications", "Loaded");
			}).catch(error=>{
				this.$root['handleApiError'](error, "Notifications");
			})
		},
		previous: function(){
			this.searchModel = {
				...this.searchModel,
				page: this.searchModel.page-1
			};
			this.loadNotification();
		},
		next: function(){
			this.searchModel = {
				...this.searchModel,
				page: this.searchModel.page+1
			};
			this.loadNotification();
		},
		initiate: function(){
			this.loadNotification();
		}
	},
	beforeMount(){
		this.initiate();
	}
}
</script>

<style scoped lang="scss">

</style>