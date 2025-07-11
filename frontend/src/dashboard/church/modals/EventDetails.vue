<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog v-model:visible="shown"
	        :closable="false"
	        :modal="true"
	        :header="typeof event.title!=='undefined' ? event.title : 'Title of the Event'"
	        class="w-lg-50 w-md-75 w-100">
		<div class="d-block position-relative pt-1">
			<table class="w-100">
				<tr>
					<td class="py-1 px-2">Description</td>
					<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
						{{ event.description }}
					</td>
				</tr>
				<tr>
					<td class="py-1 px-2">Place</td>
					<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
						{{ event.place }}
					</td>
				</tr>
				<tr>
					<td class="py-1 px-2">Date</td>
					<td class="py-1 px-2 fw-bolder" colspan="3">
						{{ getDateFromTimeStamp(event.eventDate) }}
					</td>
				</tr>
				<tr>
					<td class="py-1 px-2">Expected Attendees</td>
					<td class="py-1 px-2 fw-bolder">
						{{ event.expectedAttendees }}
					</td>
					<td class="py-1 px-2">Attendees</td>
					<td class="py-1 px-2 fw-bolder">
						{{ event.numberOfAttendees }}
					</td>
				</tr>
				<tr>
					<td class="py-1 px-2">Remarks</td>
					<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
						{{ event.remarks }}
					</td>
				</tr>
			</table>
		</div>
		
		<template #footer>
			<div class="pt-1 d-block position w-100">
				<Button label="Close"
				        severity="danger"
				        icon="pi pi-times"
				        @click="$emit('close')"
				        class="w-100"/>
			</div>
		</template>
	</Dialog>
</template>

<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import {CHURCH_EVENT} from "@/dashboard/church/church";
import {getDateFromTimeStamp} from "@/utils/AppFx";

export default {
	name: "EventDetails",
	methods: {getDateFromTimeStamp},
	components: {Dialog, Button},
	props: {
		visible: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		eventData: {
			type: Object,
			required: true,
			default(){
				return {...CHURCH_EVENT};
			}
		}
	},
	computed: {
		shown: function(){
			return this.visible;
		},
		event: function(){
			return this.eventData;
		}
	}
}
</script>

<style scoped lang="scss">

</style>