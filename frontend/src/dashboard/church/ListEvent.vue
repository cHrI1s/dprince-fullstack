<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<div :id="eventReportId" style="display: none;">
			<EventReport :single-event="tmpEvent"/>
		</div>
		
		<DashboardCard :header-title="'Event(s)'"
		               :refresh-button="true"
		               @refresh="initialize">
			<PaginatedTable :table-data="tableData"
			                :searchQueryPlaceholder="'Search By Event Name'"
			                :searchModel="searchModel"
			                @options="actionPerformed"
			                @next="loadNextPage"
			                @search="loadEvents"
			                @previous="loadPreviousPage"
			                :row-options="eventsOptions"
			                :table-headers="tableHeaders"
			                paginator-title="Event"/>
			<EventTemplateDialog @close="closeEventTemplateDialog"
			                     :show-template="showEventTemplateDialog"/>
		</DashboardCard>
		
		<AttendanceUploader :event-data="tmpEvent"
		                    :visible="attendanceUploader"
		                    @close="closeAttendance"/>
		
		<EventDetails :event-data="tmpEvent"
		              :visible="showingEventDetails"
		              @close="closeEventDetailor"/>
	</div>
</template>
<script>
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {DEFAULT_PAGE} from "@/dashboard/utils/default_values";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import EventTemplateDialog from "@/dashboard/church/EventTemplateDialog.vue";
import EventDetails from "@/dashboard/church/modals/EventDetails.vue";
import EventReport from "@/dashboard/church/modals/EventReport.vue";
import {CHURCH_EVENT} from "@/dashboard/church/church";
import AttendanceUploader from "@/dashboard/church/modals/AttendanceUploader.vue";
import {generateRandomString} from "@/utils/AppStringUtils";

export default {
	name: "ListEvent",
	components: {
		AttendanceUploader,
		EventDetails,
		EventTemplateDialog,
		DashboardCard,
		PaginatedTable,
		EventReport
	},
	emits :['sendEvent'],
	data(){
		return{
			showEventTemplateDialog : false,
			searchModel: {...PAGINATOR_SEARCH_MODEL},
			tableData: {...DEFAULT_PAGE},
			tableHeaders: {
				"Title":{
					rows:[
						{ type: "String", attribute: "title", title: "description" },
						{ type: "String", attribute: "owner.name", showIfNotNull: true},
					]
				},
				"Event Date":{
					rows:[
						{ type: "Date", attribute: "eventDate"}
					]
				},
				"Published":{
					rows:[
						{ type: "Boolean", attribute: "shared", array: ["No", "Yes"]}
					]
				},
				"Infos":{
					rows:[
						{ type: "PlainNumber", attribute: "expectedAttendees", label: " Ex. Attend."},
						{ type: "PlainNumber", attribute: "numberOfAttendees", label: " Attended"}
					]
				}
			},
			eventsOptions : [
				{ label: "Update", method: "updateEvent", styleClass: "col-md-6 mb-4" },
				{ label: "Delete", method: "deleteEvent", styleClass: "col-md-6 mb-4" },
				{ label: "Finalize", method: "concludeEvent", styleClass: "col-md-6 mb-4" },
				{ label: "Show Details", method: "showDetails", styleClass: "col-md-6 mb-4" },
				{ label: "Upload Attendance", method: "uploadAttendance", styleClass: "col-md-6 mb-4" },
				{ label: "Print Event", method: "printEvent", styleClass: "col-md-6 mb-4" },
			],
			tmpEvent : {...CHURCH_EVENT},
			showingEventDetails : false,
			attendanceUploader: false,
			eventReportId: generateRandomString(8)
		}
	},
	methods: {
		printEvent: function(theEvent){
			const TITLE = "Event Loading...";
			let data = this.$root['addInstitutionId']({ eventId : theEvent.id });
			this.$api.post("/event/get-event", data).then(response=>{
				if(response.successful) {
					this.tmpEvent = {...response.object};
					this.$store.commit("setLoading", true);
					setTimeout(()=>{
						this.$store.commit("setLoading", false);
						this.downloadAsPDF();
					}, 5000);
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		},
		downloadAsPDF : function() {
			let disp_setting="toolbar=yes,location=no,";
			disp_setting+="directories=yes,menubar=yes,";
			disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
			let htmlElement = document.getElementById(this.eventReportId);
			htmlElement.style.display = "block";
			htmlElement.style.width = "100%";
			let content_value = htmlElement.outerHTML;
			if(this.pageType!=='ADDRESS')  content_value += '<footer></footer>';
			htmlElement.style.display = "none";
			this.$store.commit("setLoading", false);
			let docprint = window.open("","",disp_setting);
			if(docprint!=null) {
				docprint.document.open();
				docprint.document.write('<!DOCTYPE html>');
				docprint.document.write('<html lang="en">');
				docprint.document.write('<head><title>Event</title>');
				docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;width: 21cm;">');
				docprint.document.write('<style type="text/css" media="all">');
				let css = "@media all {";
				css += "@page{" +
						"size: A4;" +
						"margin:0 !important;" +
						"padding: 0 !important;" +
					"}";
				css += "body{" +
					"margin: 0 !important;" +
					"padding: 0 !important;" +
					"counter-reset: page 1;" +
					"}" +
					"*{box-sizing:border-box;}" +
					".text-uppercase{text-transform: uppercase !important;}";
				
				css = "@media print {" +
					"@page{" +
						"size: A4;" +
						"margin:0mm 8mm !important;" +
						"padding: 0mm 8mm !important;" +
						"counter-increment: page;" +
					"}" +
					// Body
					"body{" +
						"margin: 0mm 8mm;" +
						"padding: 0mm 8mm;" +
						"counter-reset: page;" +
					"}" +
					"footer {" +
						"position: fixed;" +
						"bottom: 10px; right: 10px !important;" +
						"font-size: 12px;" +
						"color: #000;" +
					"}" +
					".text-uppercase{text-transform: uppercase !important;}"+
					"footer::after {" +
					// 'content: "Page " counter(page);' +
					"}" +
					"}" + css;
				css += " table {" +
					"width: 100% !important;" +
					"border-collapse: collapse;" +
					"margin: 0;" +
					"font-family: Arial, sans-serif;" +
					"}" +
					"th {" +
					"border: 1px solid #444;" +
					"padding: 5px;" +
					"text-align: left;" +
					"font-weight: bold;" +
					"color: #000;" +
					"text-transform: uppercase !important;" +
					"}" +
					"tbody tr {" +
					"border: 1px solid #444;" +
					"}" +
					"td {" +
					"border: 1px solid #444;" +
					"padding: 5px;" +
					"text-align: left;" +
					"color: #000;" +
					"}" +
					"tfoot td {" +
					"border-top: 1px solid #444; " +
					"padding: 5px;" +
					"text-align: right;" +
					"color: #000;" +
					"}"
				css += "font-family:verdana,Arial;color:#000;" +
					"font-family:arial Verdana, Geneva, sans-serif;" +
					"font-size:12px;" +
					"}";
				docprint.document.write(css);
				docprint.document.write('}</style>');
				docprint.document.write(content_value);
				docprint.document.write('</body></html>');
				docprint.document.close();
				docprint.focus();
			} else {
				this.$root['showAlert']('warn', "Print", "Check you have not Popup are not disabled.")
			}
		},
		closeAttendance: function(){
			this.tmpEvent = {...CHURCH_EVENT};
			this.attendanceUploader = false;
		},
		uploadAttendance: function(theEvent){
			this.attendanceUploader = true;
			this.tmpEvent = {...theEvent};
		},
		closeEventDetailor: function(){
			this.tmpEvent = {...CHURCH_EVENT};
			this.showingEventDetails = false;
		},
		showDetails: function(theEvent){
			this.tmpEvent = {...theEvent};
			this.showingEventDetails = true;
		},
		concludeEvent : function(row){
			let data  = { eventId : row.id };
			this.$api.post("/event/conclude-event", data).then(response=>{
				this.$root['showAlert']("success", "Finalizing Event", response.message);
				this.tableData.content = this.tableData.content.map(event=>{
					if(event.id===row.id)  event.settled = ! event.settled
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Finalizing Event");
			});
		},
		closeEventTemplateDialog : function(){
			this.showEventTemplateDialog = false;
		},
		shareEvent: function(data){
			this.$api.post("/event/share-event", data).then(response=>{
				this.$root['showAlert']("success", "Event(s)", response.message);
				this.searchModel = {...PAGINATOR_SEARCH_MODEL};
				if(typeof data.id!=="undefined") {
					this.tableData.content = this.tableData.content.map(singleEvent => {
						if (singleEvent.id === data.id) singleEvent.shared = !singleEvent.shared;
						return singleEvent;
					})
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, "Event(s)")
			});
		},
		sendSms: function(row){
			if(row.settled===true){
				let data = {
					communicationWay : "SMS",
					id : row.id
				};
				data = this.$root['addInstitutionId']({...data});
				this.shareEvent(data);
			} else {
				this.$root['showAlert']("warn", "Warning" , "The event to be settled first.");
			}
		},
		sendMail: function(row){
			if(row.settled===true){
				let data = {
					communicationWay : "MAIL",
					id : row.id,
				};
				data = this.$root['addInstitutionId']({...data});
				this.shareEvent(data);
			} else {
				this.$root['showAlert']("warn", "Warning" , "The event to be settled first.");
			}
			
		},
		updateEvent : function(row){
			if(row.shared===false && row.settled===false){
				this.$router.push("/church/event-board/1");
				this.$emit("sendEvent", row);
			}else{
				this.$root['showAlert']('warn', "Warning", "The current event has been" +
					" settled," +
					" You cannot edit this event.");
			}
		},
		deleteEvent : function(row){
			this.$api.delete("/event/delete/"+row.id).then(response=>{
				this.tableData = {
					...this.tableData,
					totalElements: this.tableData.totalElements-1,
					content: this.tableData.content.filter(item=>{
						return item.id!==row.id;
					})
				}
				this.$root['showAlert']("success", "Event Deletion",response.message);
			}) .catch(error=>{
				this.$root['handleApiError'](error, "Event Deletion");
			})
		},
		actionPerformed: function(value){
			this[value.method](value.row);
		},
		loadNextPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page+1
			}
			this.loadEvents();
		},
		loadPreviousPage: function(){
			this.searchModel = {
				...this.searchModel,
				page : this.searchModel.page-1,
			}
			this.loadEvents();
		},
		loadEvents: function(){
			let data = this.$root['addInstitutionId']({...this.searchModel}, false);
			this.$api.post("/event/search",data).then(response=>{
				this.tableData = response;
				this.$root['showAlert']("success", "Event(s) loading", "Loaded");
			}).catch(error=>{
				this.$root['handleApiError'](error, "Event(s) Loading");
			});
		},
		initialize: function(){
			if(this.$root['isInstitutionSet']()){
				this.loadEvents();
			}
		}
	},
	beforeMount(){
		this.initialize();
	},
}
</script>



<style scoped lang="scss">

</style>