<template>
	<DashboardContainer :show-institution-name="true"
	                    :refresh-button="true"
	                    @refresh="initialize">
		<DashboardCard header-title="Scheduler">
			<div class="d-flex scheduler-buttons-container mb-5">
				<div class="scheduler-button px-3 py-5 border border-light rounded-3 text-center cursor-pointer"
				     v-for="(buttonValue, index) in timingStrings"
				     :class="chosenTiming.scheduleTime===buttonValue ? 'active' : ''"
				     :key="'timing_string'+index"
				     @click="setDay(buttonValue)">
					<div class="d-block position-relative mb-4">
						Every
					</div>
					<div class="fw-bolder button-label">
						{{ getButtonValue(buttonValue) }}
					</div>
				</div>
			</div>
			
			<div class="d-flex justify-content-center w-100 flex-wrap mb-5"
			     v-if="chosenTiming.scheduleTime!==null">
				<div class="w-100 mb-4 fw-bolder text-center">
					<h4 class="fw-bolder text-center">Time</h4>
				</div>
				<div class="card flex justify-center bg-transparent border-0 box-shadow-none">
					<InputOtp v-model="chosenTiming.hours"
					          :integerOnly="true" :length="2"
					          @change="setBoundaries('HOURS')">
						<template #default="{ attrs, events }">
							<input type="text" v-bind="attrs"
							       v-on="events"
							       class="custom-otp-input" />
						</template>
					</InputOtp>
				</div>
				<div class="fw-bolder px-3 display-3">:</div>
				<div class="card flex justify-center bg-transparent border-0 box-shadow-none">
					<InputOtp v-model="chosenTiming.minutes"
					          :integerOnly="true" :length="2"
					          @change="setBoundaries('MINUTES')">
						<template #default="{ attrs, events }">
							<input type="text" v-bind="attrs"
							       v-on="events"
							       class="custom-otp-input" />
						</template>
					</InputOtp>
				</div>
			</div>
			
			<Button label="Save"
			        v-if="chosenTiming.scheduleTime!==null"
			        @click="saveScheduler"
			        class="w-100"
			        severity="success"
			        icon="pi pi-save"/>
		</DashboardCard>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import Button from "primevue/button";
import InputOtp from "primevue/inputotp";
import {SCHEDULER_TIMINGS} from "@/dashboard/scheduler/scheduler";

export default {
	name: "SchedulerBoard",
	components: {DashboardCard, DashboardContainer, InputOtp, Button},
	computed: {
		timingStrings: function(){
			return this.timings.map(timing=>timing.value)
				.filter(timing=>{
					let included;
					switch(this.timingType){
						default:
						case "WEEKDAYS":
							included = ["EVERY_MONDAY",
								"EVERY_TUESDAY",
								"EVERY_WEDNESDAY",
								"EVERY_THURSDAY",
								"EVERY_FRIDAY"];
							break;
							
						case "WEEKENDS":
							included = ["EVERY_SATURDAY", "EVERY_SUNDAY"];
							break;
							
						case "CUSTOM":
							included = ["EVERY_DAY", "EVERY_MONTH"];
							break;
					}
					return included.includes(timing);
				});
		}
	},
	methods: {
		getButtonValue: function(value){
			let found = this.timings.find(timing=>timing.value===value);
			if(found) return found.label;
			return null;
		},
		setDay: function(value){
			this.chosenTiming = {
				...this.chosenTiming,
				scheduleTime: value
			}
		},
		setBoundaries: function(time){
			let value;
			if(time==="HOURS") {
				value = parseInt(this.chosenTiming.hours);
				if(value>23) this.chosenTiming.hours = "23";
				if(value<0) this.chosenTiming.hours = "00";
			} else {
				value = parseInt(this.chosenTiming.minutes);
				if(value>59) this.chosenTiming.minutes = "59";
			    if(value<0) this.chosenTiming.minutes = "00";
			}
		},
		saveScheduler: function(){
			let title = "Scheduler";
			let data = this.$root['addInstitutionId']({...this.chosenTiming});
			this.$api.post("/cron/create", data).then(response=>{
				this.$root['showAlert']('success', title, response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, title);
			});
		},
		getScheduler: function(){
			let title = "Scheduler";
			let data = this.$root['addInstitutionId']({});
			this.$api.post("/cron/get", data).then(response=>{
				if(response.successful) {
					this.chosenTiming = {
						...response.object,
						hours: response.object.hours===0 ? "00" : response.object,
						minutes: response.object.minutes===0 ? "00" : response.object,
					}
				}
				this.$root['showAlert']('success', title, response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, title);
			});
		},
		initialize: function(){
			this.getScheduler();
		}
	},
	data(){
		return {
			timings: [...SCHEDULER_TIMINGS],
			timingType: "WEEKDAYS",
			chosenTiming: {
				scheduleTime: null,
				hours: "00",
				minutes: "00",
			}
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">
.scheduler-buttons-container{
	gap: 10px;
}

.scheduler-button{
	flex: 1;
	background-color: rgba(100, 100, 100, .1);
}

.scheduler-button:hover,
.scheduler-button.active{
	background-color: rgba(52, 152, 219, 1);
	color: #fff;
}

.button-label{
	font-size: 1.2rem;
}


.custom-otp-input {
	width: 80px;
	font-size: 3rem;
	border: 0 none;
	color: var(--bs-body-color) !important;
	appearance: none;
	text-align: center;
	transition: all 0.2s;
	background: transparent;
	border-bottom: 5px solid var(--bs-body-color);
}

.custom-otp-input:focus {
	outline: 0 none;
	border-bottom-color: var(--p-primary-color);
}
</style>