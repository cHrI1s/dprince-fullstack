<template>
	<div style="width: 210mm;
		padding: 1rem;
		box-sizing: border-box;
		color: #000;
		max-width: 210mm;">
		<div v-if="theEvent!==null">
			<div style="text-align: right;">
				{{ getDateFromTimeStamp(theEvent.eventDate) }}
			</div>
			<div style="text-transform: uppercase !important;">
				<div style="font-size: 1.5rem; text-align: center; font-weight: bolder;">{{ theEvent.title }}</div>
				<div style="text-align: center; font-weight: bolder;">{{ theEvent.place }}</div>
			</div>
			
			<div style="display: flex; justify-content: space-around; margin-top: 1rem;">
				<div style="width: 50%; text-align: left;">Expected Attendees: {{ theEvent.expectedAttendees }}</div>
				<div style="width: 50%; text-align: right;" v-if="theEvent.attendees!==null">Attended: {{ theEvent.attendees.length }}</div>
			</div>
			
			<div style="margin-top: 1rem;">
				<div>List of People Who attended</div>
				
				<table style="width: 100%;
			        background-color: #fff;
			        border: 1px solid #000;
			        margin-top: 0.5rem;
			        border-collapse: collapse;">
					<thead>
					<tr>
						<td :style="cellStyle">No</td>
						<td :style="cellStyle">Full Name</td>
						<td :style="cellStyle">Member Code</td>
					</tr>
					</thead>
					
					<tbody style="text-transform: uppercase !important;"
					       v-if="theEvent.attendees!==null && theEvent.attendees.length>0">
						<tr v-for="(person, index) of theEvent.attendees"
						    :key="index">
							<td :style="cellStyle">{{ index+1 }}</td>
							<td :style="cellStyle">{{ person.firstName + ' '+person.lastName }}</td>
							<td :style="cellStyle">{{ person.code }}</td>
						</tr>
					</tbody>
					
					<tbody style="text-transform: uppercase !important;"
					       v-else>
						<tr>
							<td :style="{...cellStyle, textAlign: 'center'}" colspan="3">No one attended the event</td>
						</tr>
					</tbody>
					
					
				</table>
			</div>
		</div>
	</div>
</template>

<script>
import {getDateFromTimeStamp} from "@/utils/AppFx";

export default {
	name: "EventReport",
	methods: {getDateFromTimeStamp},
	data(){
		return {
			cellStyle: {
				padding: '.5rem 1rem',
				border: '1px solid #000'
			},
		}
	},
	computed: {
		theEvent: function(){
			return this.singleEvent;
		}
	},
	props: {
		singleEvent: {
			type: Object,
			required: true,
			default(){
				return null;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>