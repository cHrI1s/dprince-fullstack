<template>
	<Dialog v-model:visible="shown"
	        :closable="false"
	        :modal="true"
	        class="w-lg-50 w-md-75 w-100">
		
		<template #header>
			<h4 class="d-block position-relative my-0 fw-bolder">
				{{ (typeof event.title!=='undefined' && event.title!==null) ? event.title : 'Title of the Event' }}
			</h4>
		</template>
		<div class="d-block position-relative pt-1">
			<MyFileUploader v-if="file===null"
			                :multiple="false"
			                accepted-file-types="text/csv"
			                @uploaded="uploadedAttendance"/>
			
			<div class="d-block position-relative message-info" v-else>
				<Message>
					Now the list has been, uploaded.
					Click on <strong>Mark As Attended</strong> to mark as present
					the list of the persons you just provided.
				</Message>
				
			</div>
		</div>
		
		
		<template #footer>
			<div class="row mx-0 position w-100">
				<div class="col-md-6 ms-0 ps-0 pe-2">
					<Button label="Close"
					        severity="danger"
					        icon="pi pi-times"
					        @click="$emit('close')"
					        class="w-100"/>
				</div>
				<div class="col-md-6 me-0 ps-2 pe-0">
					<Button label="Mark As Attended"
					        :disabled="file===null"
					        severity="warn"
					        @click="markAttendance"
					        class="w-100"/>
				</div>
			</div>
		</template>
	</Dialog>
</template>

<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import Message from 'primevue/message';
import {CHURCH_EVENT} from "@/dashboard/church/church";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";

export default {
	name: "AttendanceUploader",
	components: {MyFileUploader,Message, Dialog, Button},
	emits: ["close"],
	data(){
		return {
			file: null
		}
	},
	computed: {
		shown: function(){
			return this.visible;
		},
		event: function(){
			return this.eventData;
		}
	},
	methods: {
		uploadedAttendance: function(file){
			this.file = file.fileName;
		},
		markAttendance: function(){
			let data = {
				eventId: this.event.id,
				fileName: this.file
			}
			this.$api.post("/event/mark-attendance", data).then(response=>{
				this.$root['showAlert']("success", "Attendance", response.message);
				this.$emit('close');
			}).catch(error=>{
				this.$root['handleApiError'](error, "Attendance");
				this.$emit('close');
			});
		}
	},
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
}
</script>

<style scoped lang="scss">

</style>