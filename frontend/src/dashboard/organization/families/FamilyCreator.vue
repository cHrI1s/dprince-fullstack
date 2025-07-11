<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<DashboardCard header-title="Family Creation">
		<div class="d-block position-relative">
			<div class="row justify-content-around">
				<div class="col-md-6 mb-4">
					<img :src="familyPhoto"
					     alt="Family Picture"
					     class="w-100 d-block position-relative img-thumbnail mb-4 family-image"/>
					
					<MyFileUploader accepted-file-types="image/jpeg"
					                :multiple="false"
					                :maximum-file-size="51200"
					                @uploaded="setProfile"/>
				</div>
			</div>
		</div>
		
		
		
		<div class="row mb-4 justify-content-end">
			<div class="col-md-4">
				<Button label="Add Member"
				        icon="pi pi-plus"
				        class="w-100"
				        @click="addMember"
				        severity="warn"/>
			</div>
		</div>
		
		<div class="row position-relative mb-4">
			<div class="mb-4 col-md-6"
			     v-for="(member, index) in institutionMembers"
			     :key="index+'_family_member'">
				<FamilyMemberCard :member="member"
				                  title="Click to Remove"/>
			</div>
		</div>
		
		
		<div class="d-block position-relative" v-if="canFillFamilyDetails">
			<div class="mb-4">
				<div class="d-block position-relative mt-2">
					<FormLabel label-text="Family Head" :required="true"/>
					<Select class="w-100"
					        placeholder="Choose Family Head"
					        optionLabel="fullName"
					        optionValue="id"
					        v-model="family.familyHead"
					        @change="setFamilyHead"
					        :options="institutionMembers"/>
				</div>
			</div>
			<FormGenerator :ok-button="okButton"
			               :model="family"
			               :form="form"
			               @click="saveFamily"/>
		</div>
	</DashboardCard>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import FormGenerator from "@/components/form/FormGenerator.vue";
import Select from 'primevue/select';
import Button from 'primevue/button';
import {FAMILY_CREATION_MODEL} from "@/dashboard/members/members";
import {INDIA_STATES} from "@/dashboard/organization/Organization";
import FamilyMemberCard from "@/dashboard/organization/families/FamilyMemberCard.vue";
import FormLabel from "@/components/FormLabel.vue";
import {convertUiToDate, getDateFromTimeStamp} from "@/utils/AppFx";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";

import dummyFamilyPhoto from "@/dashboard/organization/families/images/family-small.jpg";

export default {
	name: "FamilyCreator",
	components: {MyFileUploader, Button, FormLabel, Select, FamilyMemberCard, FormGenerator, DashboardCard},
	emits: ["save"],
	computed: {
		familyPhoto: function(){
			if(this.family!==null){
				if(typeof this.family.photo!=='undefined' && this.family.photo!==null) {
					return APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+this.family.photo;
				}
				return dummyFamilyPhoto;
			}
			return this.theFamilyPhoto;
		},
		family: {
			get: function(){
				let family = this.$store.getters.getFamily;
				if(family==null) return {...FAMILY_CREATION_MODEL};
				return family;
			},
			set: function(newFamily){
				this.$store.commit("setFamily", newFamily);
			}
		},
		canFillFamilyDetails: function(){
			let family = this.$store.getters.getFamily;
			if(family===null) return false;
			return (typeof family.members!=='undefined' && family.members.length>=2);
		},
		isInstitutionChurch:function(){
			return this.isChurch;
		},
		institutionMembers: function(){
			let familyMembers = [],
				family = this.$store.getters.getFamily;
			if(family!==null){
				if(typeof family.members!=='undefined') familyMembers = family.members;
			}
			return familyMembers.map(member=>{
				return {
					...member,
					fullName: member.fullName.toUpperCase(),
				}
			});
		},
		userType: function(){
			if(this.$store.getters.getLoggedInUser===null) return null;
			return this.$store.getters.getLoggedInUser.userType;
		},
		form: function(){
			return [
				{ type: "SELECT", model: "state", label: "State", styleClass: "col-md-6 mb-4", options: [...INDIA_STATES] },
				{ type: "TEXT", model: "district", label: "District", styleClass: "col-md-6 mb-4" },
				{ type: "NUMBER", model: "pincode", label: "Address Pincode", styleClass: "col-md-6 mb-4"},
				{ type: "TEXT", model: "addressLine1", label: "Address Line 1", styleClass: "col-md-6 mb-4" },
				{ type: "TEXT", model: "addressLine2", label: "Address Line 2", styleClass: "col-md-6 mb-4" },
				{ type: "TEXT", model: "addressLine3", label: "Address Line 3", styleClass: "col-md-6 mb-4" },
				{ type: "COUNTRY", model: "country", label: "Country", styleClass: "col-12 mb-4" },
				{ type: "PHONE_NUMBER", model: ["phoneCode", "phone"], label: "Phone", styleClass: "col-md-6 mb-4" },
				{ type: "CALENDAR", model: "dob", label: "Date Of Marriage", styleClass: "col-md-6 mb-4", required: false, max: new Date() },
			];
		}
	},
	data(){
		return {
			okButton: { label: "Save Family", icon: "pi pi-save" },
			localFamily: {...FAMILY_CREATION_MODEL},
			theFamilyPhoto: null,
		}
	},
	methods: {
		setProfile: function(file){
			this.theFamilyPhoto = APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+file.fileName;
			this.family = {
				...this.family,
				photo: file.fileName
			};
		},
		setFamilyHead: function(event){
			let member = this.institutionMembers.find(member=>{
				return member.id===event.value;
			});
			if(typeof member!=='undefined') {
				this.family = {
					...this.family,
					state: member.state,
					district: member.district,
					pincode: member.pincode,
					addressLine1: member.addressLine1,
					addressLine2: member.addressLine2,
					addressLine3: member.addressLine3,
					phone: member.phone,
					familyHead: event.value,
					dob : getDateFromTimeStamp(member.dom)
				};
				this.$store.commit('setFamily', this.family);
			}
		},
		addMember: function(){
			let theFamily = {...this.family};
			this.$store.commit("setLoading", true);
			this.$store.commit("setFamily", theFamily);
			this.$store.commit("setLoading", false);
			
			let url = (this.isInstitutionChurch)
				? "/church/members-dashboard/0"
				: "/organizations/partners-dashboard/0";
			this.$router.push(url);
		},
  
		saveFamily:function(data){
			data = {
				...data,
				members: this.institutionMembers.map(singleMember=>{
					return {
						memberId: singleMember.id,
						title: singleMember.familyRole
					}
				})
			}
			if(typeof this.family.members!=="undefined"){
				data = {
					...data,
					id: this.family.id
				};
			}
			data = this.$root['addInstitutionId'](data);
			data = {
				...data,
				dob: convertUiToDate(data.dob)
			};
			let members = (typeof this.family.members!=='undefined') ? this.family.members : [];
			if(members.length===0) {
				this.$root['showAlert']('warn',
					"Family Save.",
					"Family Must have at least two members to be saved.");
				return;
			}
			this.$api.post("/institution/save-family", data).then(response=>{
				this.theFamilyPhoto = null;
				this.family =  {...FAMILY_CREATION_MODEL};
				this.$emit("save");
				this.$store.commit("setFamily", null);
				this.$root['showAlert']('success', "Family Save.", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Family Save.");
			});
		},
	},
	props: {
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		},
		institution: {
			type: [Object, null],
			required: true,
			default(){
				return null;
			}
		}
	}
}
</script>

<style scoped lang="scss">
.family-image{
	aspect-ratio: 1.6;
}
</style>