<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Compose"
	               :refresh-button="true"
	               @refresh="initialize(true)">
		<div class="row">
            <div class="col-12 mb-4">
	            <Select v-model="hideRecipient"
	                    class="w-100"
	                    placeholder="Select template"
	                    option-label="label"
	                    optionValue="value"
	                    @change="changeTemplateType"
	                    :options="booleans"/>
            </div>
			
			<div class="col-md-6 mb-4">
				<Select v-model="communication.way"
				        class="w-100"
				        placeholder="Select Communication way"
				        option-label="label"
				        option-value="value"
				        @change="changeCommunication"
				        :options="modeOfCommunication"/>
			</div>
			
			<div class="col-md-6 mb-4">
	            <Select v-model="communication.templateId"
	                    class="w-100"
	                    placeholder="Select template"
	                    option-label="type"
	                    :empty-message="'No Template(s) available'"
	                    @change="handleTextMessage(communication.templateId, templates)"
	                    optionValue="templateId"
	                    :options="theTemplates"/>
            </div>
			
			<div class="col-12 mb-4"
			     v-if="!hideRecipient">
				<AutoComplete class="w-100"
				              inputClass="w-100"
				              placeholder="To: Type to search Members"
				              multiple
				              forceSelection
				              :suggestions="foundMembers"
                              option-label="label"
				              @complete="search"
                              dataKey="code"
				              v-model="communication.destination"/>
			</div>
   
			<div class="mb-4"
			     :class="canUseGroups ? 'col-md-6' : 'col-12'"
			     v-if="communication.way==='MAIL'">
				<InputText class="w-100 normal-text"
				           v-model="communication.subject"
				           placeholder="Subject"/>
			</div>
			
			<div class="mb-4"
			     :class="communication.way==='MAIL' ? 'col-md-6' : 'col-12'"
			     v-if="canUseGroups">
				<Select v-model="communication.destinationGroup"
				        class="w-100"
				        placeholder="Select Group"
				        option-label="name"
				        option-value="id"
				        :empty-message="'No Group(s) available'"
				        :options="groups"/>
			</div>
			
			<div class="col-12 mb-4" v-if="hideRecipient">
                <Textarea v-model="communicablePersons"
                          placeholder="Recipient"
                          class="w-100 normal-text"/>
			</div>
			
			<div class="col-12 mb-4 text-end">
				Recipients: <strong>{{ countRecipients }}</strong>
			</div>
			
			<div class="col-12">
				<Button class="w-100" @click="send" label="Send"/>
			</div>
		</div>
	</DashboardCard>
</template>

<script>
import AutoComplete from "primevue/autocomplete";
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Select from "primevue/select";
import Textarea from "primevue/textarea";
import {MODE_COMMUNICATIONS} from "@/dashboard/communication/utils/template";
import {BOOLEANS} from "@/utils/structures/BOOLEAN";
import {isEmpty} from "@/utils/AppStringUtils";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
    name: "CommunicationCompose",
    components: {
		AutoComplete,
	    DashboardCard,
	    Button,
	    Select,
	    Textarea,
	    InputText
    },
    data(){
        return {
	        hideRecipient: true,
	        sendTo: null,
	        emailDestination: null,
	        modeOfCommunication: [...MODE_COMMUNICATIONS],
	        groups: [],
	        tmpArray: [],
	        communicationLocal: {
		        destination: [],
		        destinationGroup: null,
		        recipient: null,
		        way: null,
		        message: null,
		        subject: null,
		        templateId: null
	        },
	        communicablePeople: {
				"EMAIL": [],
		        "SMS": [],
		        "WHATSAPP": []
	        },
	        foundMembers: [],
	        templates: [],
	        communicablePersons: [],
	        selectedWay: null
        }
    },
	methods: {
		changeTemplateType: function(ignoredEvent){
			this.selectedWay = null;
			this.communicationLocal = {
				...this.communicationLocal,
				way: null,
				templateId: null
			};
		},
		changeCommunication: function(ignoredEvent){
			let output = [];
			this.selectedWay = this.communication.way;
			switch (this.communication.way) {
				case "SMS":
					output = [...this.communicablePeople["SMS"]];
					break;
				
				case "WHATSAPP":
					output = [...this.communicablePeople["WHATSAPP"]];
					break;
				
				case "EMAIL":
					output = [...this.communicablePeople["EMAIL"]];
					break;
				
				default:
					output = [];
					break;
			}
			this.communicationLocal = {
				...this.communicationLocal,
				templateId: null
			};
			this.communicablePersons = output;
		},
		handleTextMessage : function(type, templates){
          if(type!==null)this.communication.message = templates[0].text;
        },
        send : function(){
	        let data = {...this.communication};
	        data = this.$root['addInstitutionId'](data);
	        data = {
		        ...data,
		        recipient: this.hideRecipient ? this.communicablePersons : [],
		        destination: this.hideRecipient ? [] : (data.destination===null
			        ? null
			        : data.destination.map(destination=>{
				        return destination.code;
			        }))
	        };
			if(typeof data.message==="undefined") data = {... data, message: null};
			if(typeof data.recipient==='string'){
				data = {
					...data,
					recipient: data.recipient.split(",").map(single=>{
						return single.trim();
					})
				}
			}
			const TITLE = "Communication";
			this.$api.post("/communication/send", data).then(response=>{
				this.communication = {
			        destination: [],
			        destinationGroup: null,
			        recipient: null,
			        way: null,
			        message: null,
			        subject: null,
			        templateId: null
		        };
		        this.selectedWay = null;
				this.communicablePersons = [];
				this.$store.commit("setPeopleToCommunicate", null);
				this.$root['showAlert']("success", TITLE, response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
        },
	    search: function(event){
			let data = { query: event.query, communicationWay: this.communication.way };
			data = this.$root['addInstitutionId']({...data});
		    this.$api.post("/institution/search-member", data).then(response=>{
                this.foundMembers = response.map(singleMember=>{
                    return {
                        label: singleMember.fullName.toUpperCase(),
                        code: singleMember.id
                    };
                });
		    }).catch(error=>{
			    this.$root['handleApiError'](error, "Member searching");
		    });
	    },
        loadGroups: function(){
            let data = this.$root['addInstitutionId']({});
            this.$api.post("/group/get",data).then(response=>{
                this.groups = response.map(group=>{
					return {
						...group,
						name: group.name.toUpperCase()
					}
                });
            }).catch(error=>{
                this.$root['handleApiError'](error, "Group(s) Loading");
            });
        },
        loadTemplates: function(){
            let data = this.$root['addInstitutionId']({});
            this.$api.post("/communication/get/template", data).then(response=>{
                this.templates = response.objects.map(singleTemplate=>{
                    return {
                        type : singleTemplate.name.toUpperCase(),
                        emailText : singleTemplate.emailText,
                        smsText : singleTemplate.smsText,
                        templateId : singleTemplate.id,
	                    whatsappTemplate : singleTemplate.whatsappTemplate
                    }
                });
            }).catch(error=>{
                this.$root['handleApiError'](error, "Template(s) Loading!");
            });
        },
	    initialize: function(clean=false){
			if(this.$root['isInstitutionSet']()) {
				this.loadGroups();
				this.loadTemplates();
				
				if(this.$store.getters.getPeopleToCommunicate!==null){
					if(clean){
						// this.$store.commit("setPeopleToCommunicate", null);
					} else {
						let people = this.$store.getters.getPeopleToCommunicate;
						let phones = people
							.filter(person => {
								return (typeof person.phoneCode !== 'undefined' && person.phoneCode !== null)
									&& (typeof person.phone !== 'undefined' && person.phone !== null);
							}).map(person => "+"+person.phoneCode+""+person.phone);
						this.communicablePeople = {
							"SMS": [...phones],
							"WHATSAPP": [...phones],
							"EMAIL":  people
								.filter(person => {
									return person.email !== null && person.email !== "";
								}).map(person => person.email)
						};
					}
				}
			}
	    }
    },
    beforeMount() {
        this.initialize();
    },
	computed: {
		countRecipients: function(){
			let people = 0
			if(this.hideRecipient){
				if(typeof this.communicablePersons==='string'){
					if(isEmpty(this.communicablePersons)) {
						people = this.communicablePersons
							.split(",").filter(person => !isEmpty(person)).length;
					}
				} else {
					people = this.communicablePersons.length;
				}
			} else {
				people = this.communication.destination!==null
					? this.communication.destination.length
					: 0;
			}
			return people;
		},
		theTemplates: function(){
			let output = [];
			switch(this.selectedWay){
				case "SMS":
					output = this.templates.filter(template=>!isEmpty(template.smsText));
					break;
					
				case "MAIL":
					output = this.templates.filter(template=>!isEmpty(template.emailText));
					break;
					
				case "WHATSAPP":
					output = this.templates.filter(template=>!isEmpty(template.whatsappTemplate));
					break;
			}
			return output;
		},
		loggedInUser: function(){
			return this.$store.getters.getLoggedInUser;
		},
		canUseGroups: function(){
			if(this.loggedInUser===null) return false;
			if(SUPER_ADMINISTRATORS_ROLES.includes(this.loggedInUser.userType)) return true;
			let institution = this.loggedInUser.institution;
			if(typeof institution==='undefined' || institution===null) return null;
			return this.hideRecipient && institution.allowedFeatures.includes("MEMBER_GROUPS");
		},
		booleans: function(){
			return [...BOOLEANS].map(singleBool=>{
				return {
					...singleBool,
					label: (singleBool.value) ? "Static Template" : "Dynamic Template"
				}
			});
		},
		communication: {
			get: function(){
				if(this.peopleToCommunicate!==null){
					this.communicationLocal = {
						...this.communicationLocal,
						destination: [...this.peopleToCommunicate],
					}
				}
				return this.communicationLocal;
			}, set: function(newValue){
				this.communicationLocal = {...newValue};
			}
		},
		peopleToCommunicate: function(){
			let people = this.$store.getters.getPeopleToCommunicate;
			let way = this.selectedWay!==null ? this.selectedWay : undefined;
			if(people===null) return null;
			return people.filter(singleMember=>{
				let visible = true;
				switch(way){
					default:
					case undefined:
						visible = false;
						break;
						
					case "MAIL":
						visible = !isEmpty(singleMember.email);
						break;
					
					case "SMS":
						visible = singleMember.phone!=null;
						break;
					
					case "WHATSAPP":
						visible = singleMember.whatsappNumber!=null;
						break;
				}
				return visible;
			}).map(singleMember=>{
				return {
					label: singleMember.fullName.toUpperCase(),
					code: singleMember.id
				}
			})
		},
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