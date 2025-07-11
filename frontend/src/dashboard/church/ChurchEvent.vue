<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Create Event">
		<div class="row">
			<div class="col-md-6 mb-4">
				<FormLabel label-text="Title" :required="true"/>
				<InputText class="w-100"
				           placeholder="Title"
				           v-model="churchEvent.title"/>
			</div>
			<div class="col-md-6 mb-4">
				<FormLabel label-text="Place" :required="true"/>
				<InputText class="w-100"
				           placeholder="Place"
				           v-model="churchEvent.place"/>
			</div>
			<div class="col-md-6 mb-4">
				<FormLabel label-text="Expected of Attendees" :required="true"/>
				<InputNumber class="w-100"
				             placeholder="Expected of Attendees"
				             v-model="churchEvent.expectedAttendees"/>
			</div>
			<div class="col-md-6 mb-4">
				<FormLabel label-text="Date of Event" :required="true"/>
				<DatePicker class="w-100"
				            dateFormat="dd/mm/yy"
				            placeholder="Date of Event"
				            v-model="churchEvent.eventDate"/>
			</div>
			<div class="col-12 mb-4">
				<FormLabel label-text="Description" :required="true"/>
				<InputText class="w-100"
				           placeholder="Description"
				           v-model="churchEvent.description"/>
			</div>
			<div class="col-12 mb-4">
				<FormLabel label-text="Remarks" :required="true"/>
				<InputText class="w-100"
				           placeholder="Remarks"
				           v-model="churchEvent.remarks"/>
			</div>
			<div class="col-12 mt-4">
				<Button class="w-100" label="Save"
				        @click="save"
				        icon="pi pi-save"/>
			</div>
		</div>
	</DashboardCard>
</template>
<script>
import {CHURCH_EVENT, COMMUNICATION_WAY} from "@/dashboard/church/church";
import FormLabel from "@/components/FormLabel.vue";
import InputText from "primevue/inputtext";
import InputNumber from "primevue/inputnumber";
import DatePicker from "primevue/datepicker";
import Button from "primevue/button";
import {convertUiToDate, getDateFromTimeStamp} from "@/utils/AppFx";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";

export default {
	name: "ChurchEvent",
	emits: ["save"],
	components:{
		DashboardCard,
		FormLabel,
		InputText,
		InputNumber,
		DatePicker,
		Button,
	},
	data(){
		return{
			templates : [],
			foundTemplates:[],
			communicationWays : [...COMMUNICATION_WAY].filter(way=>{
				return !["TELEGRAM", "WHATSAPP"].includes(way.value);
			}),
			churchEvent : this.event,
			isEditing : false,
		}
	},
	watch: {
		event: function(newValue){
			this.churchEvent = this.prepareEvent(newValue);
		}
	},
	props:{
		event : {
			type:Object,
			default(){
				return {...CHURCH_EVENT};
			}
		}
	},
	methods:{
		prepareEvent: function(newEvent){
			return {
				...newEvent,
				eventDate: (newEvent.eventDate===null)
					? null
					: getDateFromTimeStamp(newEvent.eventDate),
				phones: (typeof newEvent.phones!=="undefined" && newEvent.phones!==null) ? newEvent.phones.join(",") : null,
				emails: (typeof newEvent.emails!=="undefined" && newEvent.emails!==null) ? newEvent.emails.join(",") : null,
			};
		},
		handleCommunication : function(way){
			if(way!==null){
				this.templates = this.foundTemplates.filter(singleTemplate=>{
					if(singleTemplate.way===way) return singleTemplate;
				}).map(singleFiltered=>{
					return {
						label : singleFiltered.name,
						value : singleFiltered.id
					}
				});
			}
		},
		handleEventMessage : function(templateId){
			this.foundTemplates.filter(singleTemplate=>{
				if(singleTemplate.id===templateId){
					this.localEventModel.eventMessage = singleTemplate.text
				}
			})
		},
		save: function(){
			let data =  (typeof this.churchEvent.id=='undefined')
				?  this.$root['addInstitutionId']({...this.churchEvent})
				: this.churchEvent;
			data = {
				...data,
				eventDate: (typeof data.id!=='undefined')
					? convertUiToDate(data.eventDate)
					: data.eventDate
			};
			this.$api.post("/event/save", data).then(response=>{
				this.churchEvent = {...CHURCH_EVENT};
				this.$root['showAlert']('success', "Church Event" ,response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Church Event");
			});
		},
		loadTemplates: function(){
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/communication/get/template", data).then(response=>{
				this.foundTemplates = response.objects.map(template=>{
					return {
						label: template.name,
						value: template.id
					}
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Template(s) Loading");
			});
		},
	},
	beforeMount() {
		this.loadTemplates();
		this.churchEvent = this.prepareEvent(this.churchEvent);
	}
}
</script>



<style scoped lang="scss">

</style>