<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<DashboardCard :header-title="headerTitle">
			<div class="row justify-content-end mb-3">
				<div class="col-md-4 mb-4">
					<Button label="Clear Form"
					        class="w-100"
					        outlined
					        @click="clearForm"
					        severity="danger"
					        icon="pi pi-times"/>
				</div>
				<div class="col-md-4 mb-4">
					<Button label="Reinitialize Form"
					        class="w-100"
					        outlined
					        @click="initialize"
					        severity="warn"
					        icon="pi pi-refresh"/>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-4 mb-4" v-if="creatingFamily">
					<Button label="Fill With HOF Details"
					        severity="info"
					        class="w-100"
					        @click="sameAsFamilyHead"/>
				</div>
				<div class="col-md-4 mb-4" v-if="this.$store.getters.getFamily!==null">
					<Button label="Fill With Family Details"
					        severity="info"
					        class="w-100"
					        @click="fillWithFamilyDetails"/>
				</div>
			</div>
			
			<div class="row">
				<div class="d-block position-relative col-md-9 mb-4">
					<div class="row">
						<div class="col-md-4 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Salutation" :required="true"/>
								<Select v-model="member.titleId"
								        :options="titles"
								        optionValue="id"
								        optionLabel="shortName"
								        placeholder="Salutation"
								        class="w-100"/>
							</div>
						</div>
						<div class="col-md-4 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Firstname" :required="true"/>
								<InputText v-model="member.firstName"
								           placeholder="Firstname"
								           class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-4 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Lastname" :required="true"/>
								<InputText v-model="member.lastName"
								           placeholder="Lastname"
								           class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4"
						     v-if="isInstitutionChurch">
							<div class="d-block position-relative">
								<FormLabel label-text="Baptism Status" :required="true"/>
								<Select v-model="member.baptized"
								        :options="baptismStatus"
								        @change="handleBaptizedMemberStatus(member.baptized)"
								        placeholder="Baptism Status"
								        optionLabel="label"
								        optionValue="value"
								        class="w-100"/>
							</div>
						</div>
						<div class="col-md-6 mb-4"
						     v-if="isInstitutionChurch && isBaptized===true">
							<div class="d-block position-relative">
								<FormLabel label-text="Baptism Date"/>
								<DatePicker v-model="member.dateOfBaptism"
								            placeholder="Baptism Date"
								            class="w-100"/>
							</div>
						</div>
						
						<div class="mb-4 col-md-12"
						     :class="isInstitutionChurch ? 'col-md-6' : 'col-12'">
							<div class="d-block position-relative">
								<FormLabel label-text="Gender" :required="true"/>
								<Select v-model="member.gender"
								        :options="genders"
								        placeholder="Gender"
								        optionLabel="label"
								        optionValue="value"
								        class="w-100"/>
							</div>
						</div>
						
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Phone Number" :required="true"/>
								<div class="row">
									<div class="col-md-3">
										<Select :options="countriesPhoneCodes"
										        v-model="member.phoneCode"
										        placeholder="Country Code"
										        optionLabel="label"
										        optionValue="value"
										        class="w-100"/>
									</div>
									<div class="col-md-9">
										<InputNumber v-model="member.phone"
										             inputId="withoutgrouping"
										             :useGrouping="false"
										             placeholder="Phone Number"
										             class="w-100"/>
									</div>
								</div>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Alternate Phone Number" :required="false"/>
								<div class="row">
									<div class="col-md-3">
										<Select :options="countriesPhoneCodes"
										        v-model="member.alternatePhoneCode"
										        placeholder="Country Code"
										        optionLabel="label"
										        optionValue="value"
										        class="w-100"/>
									</div>
									<div class="col-md-9">
										<InputNumber v-model="member.alternatePhone"
										             inputId="withoutgrouping"
										             :useGrouping="false"
										             placeholder="Alternate Phone Number"
										             class="w-100"/>
									</div>
								</div>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Landline Phone Number" :required="false"/>
								<div class="row">
									<div class="col-md-3">
										<Select :options="countriesPhoneCodes"
										        v-model="member.landlinePhoneCode"
										        placeholder="Country Code"
										        optionLabel="label"
										        optionValue="value"
										        class="w-100"/>
									</div>
									<div class="col-md-9">
										<InputNumber v-model="member.landlinePhone"
										             inputId="number-no-grouping"
										             :useGrouping="false"
										             placeholder="Landline Phone Number"
										             class="w-100"/>
									</div>
								</div>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<input type="checkbox" name="whatsappNumber" style="transform: translate(0px, 1px);"
								       id="whatsappNumber"
								       v-model="member.whatsapp"
								       @click="copyPhoneNumberToWhatsappField"/>
								<label class="fw-bolder">Same as Phone Number</label>
								<div class="row">
									<div class="col-md-3">
										<Select :options="countriesPhoneCodes"
										        v-model="member.whatsappNumberCode"
										        placeholder="Country Code"
										        optionLabel="label"
										        optionValue="value"
										        class="w-100"/>
									</div>
									<div class="col-md-9">
										<InputNumber v-model="member.whatsappNumber"
										             inputId="whatsapp-no-grouping"
										             :useGrouping="false"
										             placeholder="Whatsapp Number"
										             class="mb-4 w-100"/>
									</div>
								</div>
							</div>
						</div>
						<div class="mb-4" :class="isInstitutionChurch ? 'col-md-6' : 'col-12'">
							<div class="d-block position-relative">
								<FormLabel label-text="Email" :required="false"/>
								<InputText v-model="member.email"
								           placeholder="Email"
								           class="w-100 normal-text"/>
							</div>
						</div>
						
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Address line 1" :required="true"/>
								<InputText v-model="member.addressLine1"
								           placeholder="Address line 1"
								           class="w-100"/>
							</div>
						</div>
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Address line 2" :required="true"/>
								<InputText v-model="member.addressLine2"
								           placeholder="Address line 2"
								           class="w-100"/>
							</div>
						</div>
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Address line 3" :required="true"/>
								<InputText v-model="member.addressLine3"
								           placeholder="Address line 3"
								           class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Address(District)" :required="true"/>
								<InputText v-model="member.district"
								           :disabled="indianContent"
								           placeholder="Address"
								           class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Country" :required="true"/>
								<Select v-model="member.country"
								        :options="countries"
								        @change="handleCountry(member.country)"
								        placeholder="Country"
								        optionLabel="label"
								        optionValue="value"
								        class="w-100"/>
							</div>
						</div>
						<div class="col-md-6 mb-4" v-if="member.country==='IN'"
						     :class="member.country!=='IN' ? 'col-md-12' : 'col-md-6'">
							<div class="d-block position-relative">
								<FormLabel label-text="State" :required="true"/>
								<Select v-model="member.state"
								        :options="states"
								        :disabled="indianContent && member.country==='IN'"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="State"
								        class="w-100"/>
							</div>
						</div>
						<div class="col-md-6 mb-4" v-else>
							<div class="d-block position-relative">
								<FormLabel label-text="State" :required="true"/>
								<InputText v-model="member.state"
								           placeholder="State"
								           class="w-100"/>
							</div>
						</div>
						<div class="mb-4" :class="isInstitutionChurch ? 'col-md-6' : 'col-12'">
							<div class="d-block position-relative">
								<FormLabel label-text="Pincode" :required="true"/>
								<InputGroup>
									<InputNumber v-model="member.pincode"
									             inputId="withoutgrouping"
									             :useGrouping="false"
									             placeholder="Pincode"
									             class="w-100"/>
									<Button class="" icon="pi pi-search"
									        @click="checkPinCodeExist"/>
								</InputGroup>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Date of Birth" :required="true"/>
								<DatePicker v-model="member.dob"
								            dateFormat="dd/mm/yy"
								            placeholder="Date of Birth"
								            :maxDate="maxDate"
								            iconDisplay="input"
								            :manualInput="false"
								            class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Date of Marriage" :required="false"/>
								<DatePicker v-model="member.dom"
								            dateFormat="dd/mm/yy"
								            placeholder="Date of Marriage"
								            iconDisplay="input"
								            :maxDate="maxDate"
								            :manualInput="false"
								            class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4"
						     v-if="isInstitutionChurch">
							<div class="d-block position-relative">
								<FormLabel label-text="Communion" :required="false"/>
								<Select v-model="member.communion"
								        :options="communionModel"
								        optionValue="value"
								        optionLabel="label"
								        placeholder="Communion"
								        class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4" v-if="isInstitutionChurch && member.communion===true">
							<div class="d-block position-relative">
								<FormLabel label-text="Date of Communion" :required="false"/>
								<DatePicker v-model="member.communionDate"
								            dateFormat="dd/mm/yy"
								            placeholder="Date of Communion"
								            iconDisplay="input"
								            :manualInput="false"
								            class="w-100"/>
							</div>
						</div>
						
						<div class="col-12 my-0 py-0"></div>
						
						<div class="mb-4"
						     :class="isInstitutionChurch ? '' : 'col-md-6'"
						     v-if="!isInstitutionChurch">
							<div class="d-block position-relative">
								<FormLabel label-text="Type of Subscription"
								           :required="true"/>
								<Select v-model="member.subscription"
								        :options="subscriptionTypes"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="Type of Subscription"
								        class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4"
						     v-if="!isInstitutionChurch">
							<div class="d-block position-relative">
								<FormLabel label-text="Partner Category" :required="true"/>
								<MultiSelect v-model="member.categoriesIds"
             :options="categories"
             optionLabel="label"
             optionValue="value"
             placeholder="Partner Category"
             class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-12 mb-4" :class="(isBaptized===true) ? 'col-md-12' : 'col-md-6'">
							<div class="d-block position-relative">
								<FormLabel label-text="Mode of Communication" :required="true"/>
								<Select v-model="member.language"
								        :options="languages"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="Mode of Communication"
								        class="w-100"/>
							</div>
						</div>
						
						<div class="mb-4"
						     :class="isInstitutionChurch ? 'col-md-6' : 'col-md-12'"
						     v-if="typeof member.id==='undefined' && member.id===null">
							<div class="d-block position-relative">
								<FormLabel label-text="Active" :required="true"/>
								<Select v-model="member.status"
								        :options="memberStatus"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="Active"
								        class="w-100"/>
							</div>
						</div>
						
						<div class="mb-4"
						     :class="(typeof member.id!=='undefined') ? 'col-md-12': 'col-md-6'"
						     v-if="typeof member.id!=='undefined' && member.id!==null">
							<div class="d-block position-relative">
								<FormLabel label-text="Creation Date">
									<div class="d-flex align-items-center">
										<span class="ms-2 fw-bolder cursor-pointer"
										      v-if="typeof member.id==='undefined'"
										      @click="nullifyCreationDate">
											{{ defaultCreationDate ? 'Set Date' : 'Today' }}
										</span>
									</div>
								</FormLabel>
								<DatePicker v-model="member.creationDate"
								            :maxDate="maxDate"
								            :disabled="typeof member.id!=='undefined' || defaultCreationDate"
								            dateFormat="dd/mm/yy"
								            iconDisplay="input"
								            placeholder="Creation Date"
								            :manualInput="false"
								            showButtonBar
								            class="w-100"/>
							</div>
						</div>
						
						<div class="col-md-6 mb-4"
						     v-if="isInstitutionChurch && canMakeHOF">
							<div class="d-block position-relative">
								<FormLabel label-text="Is Head of Family"
								           :required="false"/>
								<Select v-model="member.familyHead"
								        :options="familyHeadOptions"
								        optionLabel="label"
								        @change="createFamily"
								        :disabled="creatingFamily"
								        optionValue="value"
								        placeholder="Is Head of Family"
								        class="w-100"/>
							</div>
						</div>
						
						<div :class="canMakeHOF ? 'col-md-6' : 'col-12'"
						     class="mb-4"
						     v-if="isInstitutionChurch">
							<div class="d-block position-relative">
								<FormLabel label-text="Family Title"
								           :required="false"/>
								<Select v-model="member.familyRole"
								        :disabled="familyRoleDisabled"
								        :options="familyRoles"
								        optionLabel="label"
								        optionValue="value"
								        placeholder="Family Title"
								        class="w-100"/>
							</div>
						</div>
						
						
						<div class="col-md-12 mb-4">
							<div class="d-block position-relative">
								<FormLabel label-text="Modification Date" :required="false"/>
								<InputText v-model="member.updateDate"
								           :disabled="true"
								           placeholder="Modification Date"
								           class="w-100"/>
							</div>
						</div>
					</div>
				</div>
				
				<div class="d-block position-relative col-md-3 mb-4">
					<img :src="theImage" alt="Photo"
					     class="w-100 d-block position-relative img-thumbnail"/>
					<MyFileUploader accepted-file-types="image/jpeg"
					                @uploaded="setProfile"/>
				</div>
				
				<div class="mb-4"
				     :class="creatingFamily ? 'col-6' : 'col-12'">
					<div class="d-block position-relative">
						<Button label="Save"
						        @click="save"
						        icon="pi pi-save"
						        severity="success"
						        class="w-100"/>
					</div>
				</div>
				
				<div class="col-6 mb-4"
				     v-if="creatingFamily">
					<div class="d-block position-relative">
						<Button label="Finalize Creation"
						        @click="gotoFamilyBoard"
						        icon="pi pi-chevron-right"
						        severity="warn"
						        class="w-100"/>
					</div>
				</div>
			</div>
		</DashboardCard>
		
		<MemberDetails :member="savedMember"
		               :creatingFamily="creatingFamily"
		               :visible="memberDetailsShown"
		               :isChurch="isInstitutionChurch"
		               @close="(value)=>closeDetailor(value)"/>
	</div>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import InputNumber from "primevue/inputnumber";
import DatePicker from "primevue/datepicker";
import Select from "primevue/select";
import MultiSelect from "primevue/multiselect";
import InputGroup from 'primevue/inputgroup';
import {
	AVAILABLE_LANGUAGES,
	COMMUNION,
	INDIA_STATES,
	MEMBER_STATUS,
	PARTNER_MODEL,
	SUBSCRIPTION_PLANS,
} from "@/dashboard/organization/Organization";
import FormLabel from "@/components/FormLabel.vue";
import {
	FAMILY_CREATION_MODEL,
	FAMILY_ROLES,
	GENDERS,
	MEMBER_BAPTISM_STATUSES,
	SUPER_ADMINISTRATORS_ROLES
} from "@/dashboard/members/members";
import {getCountries} from "@/dashboard/utils/countries";
import axios from "axios";
import {convertUiToDate, getDateFromTimeStamp} from "@/utils/AppFx";
import MemberDetails from "@/dashboard/organization/dialogs/details/MemberDetails.vue";
import manImagePlaceholder from "@/dashboard/members/images/man-small.jpg";
import womanImagePlaceholder from "@/dashboard/members/images/woman-small.jpg";
import MyFileUploader from "@/dashboard/uploader/MyFileUploader.vue";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import {isEmpty} from "@/utils/AppStringUtils";

export default {
	name: "OrganizationMember",
	emits: ["setMember"],
	components: {
		MyFileUploader,
		MemberDetails,
		FormLabel,
		Button,
		DashboardCard,
		InputText,
		InputNumber,
		DatePicker,
		Select,
		MultiSelect,
		InputGroup
	},
	computed: {
		theImage: function(){
			if(this.profileImage!==null) return this.profileImage;
			if(this.member.profile!==null) return APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+this.member.profile;
			return this.member.gender==="MALE" ? manImagePlaceholder : womanImagePlaceholder;
		},
		countriesPhoneCodes: function(){
			let countryCodes = this.countries.map(country=>{
				return {
					label: country.dial_code,
					value: parseInt(country.dial_code.replace("+", ""))
				}
			});
			return [
				{ label: "No Code",  value: null},
				...countryCodes
			]
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		maxDate: function(){
			let now = new Date(this.today);
			now.setHours(23);
			now.setMinutes(59);
			now.setSeconds(59);
			return now;
		},
		isInstitutionChurch : function(){
			return this.isChurch;
		},
		canMakeHOF: function(){
			let family = this.$store.getters.getFamily;
			if(family===null) return true;
			if(family.members===null || typeof family.members==='undefined') return true;
			return family.members.length===0;
		},
		familyRoleDisabled : function(){
			if(this.canMakeHOF) return !this.member.familyHead && !this.creatingFamily;
			return this.canMakeHOF;
		},
		needSubscriptionType: function(){
			return this.hasSubscriptionType;
		},
		isFamilyCreationInit: function(){
			if(this.member.familyHead===null) return null;
			return this.member.familyHead;
		},
		family: function(){
			return this.$store.getters.getFamily;
		},
		member: {
			get: function(){
				return this.localMember;
			}, set: function(newValue){
				this.localMember = newValue;
			}
		}
	},
	data(){
		return {
			communionModel:[...COMMUNION],
			today: new Date(),
            copyPhoneNumber : false,
            indianContent : true,
            isStateAvailable : true,
			titles: [],
			defaultCreationDate : true,
			familyHeadOptions: [{label: "Yes", value: true}, {label: "No", value: false}],
			familyRoles: [...FAMILY_ROLES],
			localMember: {...this.memberModel},
			categories: [],
			subscriptionTypes: [...SUBSCRIPTION_PLANS],
			languages : [...AVAILABLE_LANGUAGES],
			memberStatus: [...MEMBER_STATUS],
			baptismStatus : [...MEMBER_BAPTISM_STATUSES],
			genders : [...GENDERS],
            countries: getCountries(),
			states: [...INDIA_STATES],
            district:[],
            isBaptized : false,
			
			savedMember: {...PARTNER_MODEL},
			memberDetailsShown: false,
			foundPincode  : [],
			
			creatingFamily: false,
			simpleLeave: true,
			newMemberToFamily: false,
			
			
			profileImage: null,
		}
	},
    methods:{
	    createFamily: function(){
			if(this.member.familyHead) {
				let family = {...FAMILY_CREATION_MODEL};
				this.$store.commit("setFamily", family);
			} else {
				this.$store.commit("setFamily", null);
			}
	    },
		setProfile: function(file){
			this.profileImage = APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+file[0].fileName;
			this.member = {
				...this.member,
				profile: file[0].fileName
			};
		},
		gotoFamilyBoard: function(){
			let url = (this.isInstitutionChurch)
				? "/church/families-board/1"
				: "/organizations/families-board/1";
			this.simpleLeave = false
			this.$router.push(url);
		},
		sameAsFamilyHead: function(){
			let family = this.$store.getters.getFamily;
			if(family!==null && typeof family.members!=='undefined'){
				if(family.members.length>0){
					let head = family.members[0];
					this.member = {
						...this.member,
						addressLine1: head.addressLine1,
						addressLine2: head.addressLine2,
						addressLine3: head.addressLine3,
						country: head.country,
						pincode: head.pincode,
						state: head.state,
						district: head.district
					}
				}
			}
		},
	    getDateFromTimeStamp,
	    closeDetailor: function(addFamilyMember = false){
		    this.$store.commit("setLoading", true);
			if(addFamilyMember){
				let family = this.$store.getters.getFamily===null
					? {...FAMILY_CREATION_MODEL}
					: {...this.$store.getters.getFamily};
				
				let familyMembers = (typeof family.members==='undefined') ? [] : family.members;
				familyMembers.push(this.savedMember);
				family = {
					...family,
					members: familyMembers
				};
				if(familyMembers.length===1){
					let hof = familyMembers[0];
					family = {
						...family,
						name: hof.fullName,
						familyHead: hof.id,
						addressLine1 : hof.addressLine1,
						addressLine2 : hof.addressLine2,
						addressLine3 : hof.addressLine3,
						pincode : hof.pincode,
						district : hof.district,
						area : hof.area,
						state : hof.state,
						country : hof.country,
						phone : hof.phone,
						phoneCode : hof.phoneCode,
						alternatePhone : hof.alternatePhone,
						alternatePhoneCode : hof.alternatePhoneCode,
						landlinePhone : hof.landlinePhone,
						landlinePhoneCode : hof.landlinePhoneCode,
					}
				}
				this.$store.commit("setFamily", family);
				this.creatingFamily = true;
				this.member = {
					...this.member,
					familyHead: false
				}
			} else {
				if(!this.newMemberToFamily) this.$store.commit("setFamily", null);
				this.creatingFamily = false;
			}
	        this.memberDetailsShown=false;
		    this.$store.commit("setLoading", false);
	    },
        handleBaptizedMemberStatus : function(baptized){
            this.isBaptized = baptized === "BAPTIZED";
        },
	    nullifyCreationDate: function(){
			if(typeof this.member.id==="undefined") {
				this.defaultCreationDate = !this.defaultCreationDate;
				if(this.defaultCreationDate) this.member.creationDate = new Date();
			}
	    },
	    copyPhoneNumberToWhatsappField: function(){
			if(!this.member.whatsapp){
				this.member.whatsappNumber = this.member.phone;
				this.member.whatsappNumberCode = this.member.phoneCode;
			} else {
				this.member.whatsappNumber = null;
				this.member.whatsappNumberCode = null;
			}
	    },
        handleCountry : function(country){
            this.indianContent = country === "IN";
            this.member.state = null;
            this.member.district = null;
            //this.member.pincode = null;
        },
	    clearForm: function(){
		    this.member = {
				...PARTNER_MODEL,
			    creationDate: new Date()
		    };
			this.$store.commit("setTmpInstitutionMember", null);
	    },

         save() {
            let data = this.$root['addInstitutionId'](this.member);
            const TITLE = ((this.isInstitutionChurch) ? "Member" : "Partner") + " creation";
            
            // *** THE MAIN CHANGE IS HERE: URL SELECTION ***
            const URL = this.isInstitutionChurch // <-- Changed this line
                    ? "/institution/add-member" // <-- If Church, use add-member
                    : "/institution/add-partner"; // <-- If General, use add-partner
            // The `this.needSubscriptionType ? "/institution/save-family-prospectus" : ...`
            // part has been removed entirely from this save logic.
            // If you have a separate "Save Family Prospectus" feature, it should
            // use its own dedicated save function/logic.

	        data = {
		        ...data,
		        dob : convertUiToDate(this.member.dob),
		        dom : convertUiToDate(this.member.dom),
		        creationDate : convertUiToDate(this.member.creationDate),
		        updateDate : convertUiToDate(this.member.updateDate),
	        };
			if(this.isInstitutionChurch) if (!data.communion) data.communionDate = null;
			
			console.log(URL, data);
	        this.$api.post(URL, data).then(response => {
		        this.savedMember = {
			        ...response.object,
			        titles : this.titles,
			        categories : this.categories
		        };
				this.profileImage = null;
		  
				
				// Nimba family iriko iraba edited raba wongeremwo uno muntu wangu.
		        if(this.$store.getters.getFamily!==null){
					let family = {...this.$store.getters.getFamily},
						familyMembersIds = [];
					if(!isEmpty(data.familyRole)){
						let familyMembers = [];
						if(typeof family.members!=='undefined' && family.members!==null) {
							let alreadyInsertedIds = [];
							familyMembers = [
								...family.members,
								{
									...response.object,
									familyRole: data.familyRole
								}
							].filter(singleMember => {
    // **CRITICAL FIX: Check if singleMember is null or undefined before accessing .id**
    if (singleMember === null || typeof singleMember === 'undefined') {
        return false; // Skip null/undefined entries
    }

    if (alreadyInsertedIds.includes(singleMember.id)) {
        return false;
    }
    alreadyInsertedIds = [
        ...alreadyInsertedIds,
        singleMember.id
    ];
    return true;
});
						}
						family = {
							...family,
							members: [
								...familyMembers,
							].filter(singleFamilyMember=>{
								if(!familyMembersIds.includes(singleFamilyMember.id)) {
									familyMembersIds = [
										...familyMembersIds,
										singleFamilyMember.id
									];
									return true;
								}
								return false;
							})
						};
						this.newMemberToFamily = true;
						this.$store.commit("setFamily", family);
					}
		        } else {
					this.$emit("setMember", null);
					console.log("yaciye ngaha wangu");
		        }
		        this.memberDetailsShown = true;
		        this.member = {
			        ...PARTNER_MODEL,
			        creationDate: new Date(),
			        newPinCode: false
		        };
				 this.$toast.add({
        severity: 'success', // "success" type directly
        summary: TITLE, // Use the TITLE variable (assuming it's defined in OM.vue or passed)
        detail: response.data.message, // Use the message from backend
        life: 5000
    });
		        //this.$root['showAlert']("success", TITLE, response.message);
		        if(!this.needSubscriptionType){
					this.$emit("setMember", this.member);
		        }
	        }).catch(error => {
				console.log(error);
		        this.$root['handleApiError'](error, TITLE);
	        });
        },
	    loadTitles: function(){
			this.$api.get("/title/get").then(response=>{
				this.titles = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, "Initialization of Form");
			})
	    },
        loadPinCodes : function(){
            if(this.indianContent || this.member.country==='IN') {
               return new Promise(resolve => {
	               axios.get("https://api.postalpincode.in/pincode/" + this.member.pincode).then(response => {
		               this.indianContent = true;
		               this.member.district = response.data[0].PostOffice[0].District;
		               this.member.state = response.data[0].PostOffice[0].State.toUpperCase();
					   resolve(true);
	               }).catch(ignored => {
					   resolve(false);
	               });
               })
            }
        },
	    checkPinCodeExist : function(){
			if(this.member.country==='IN') {
				this.loadPinCodes().then(result => {
					if (result === false) {
						this.$api.get("/institution/pincode/" + this.member.pincode).then(response => {
							if (response.successful === true) {
								this.member = {
									...this.member,
									district: response.object.district,
									country: response.object.country,
									state: response.object.state,
									newPinCode: false
								}
							} else {
								this.indianContent = false;
								this.member = {
									...this.member,
									district: null,
									country: "IN",
									state: null,
									newPinCode: true
								};
								
							}
						}).catch(ignored => {
							this.indianContent = false;
						});
					}
				})
			}
	    },
	    loadCategories: function(){
		    let data = this.$root['addInstitutionId']({});
		    this.$api.post(`/category/get`, data).then(response=>{
			    this.categories = response.map(singleCategory=>{
				    return {
					    label: singleCategory.name,
					    value: singleCategory.id,
				    }
			    });
		    }).catch(error=>{
			    this.$root['handleApiError'](error, "Category Loading");
		    });
	    },
	    initialize: function(){
			if(this.$root['isInstitutionSet']()) {
				this.loadTitles();
				if(!this.isInstitutionChurch) this.loadCategories();
			}
	    },
	    fillWithFamilyDetails: function(){
		    if(this.$store.getters.getFamily!==null){
				let family = {...this.$store.getters.getFamily};
			    this.member = {
				    ...this.member,
				    addressLine1: family.addressLine1,
				    addressLine2: family.addressLine2,
				    addressLine3: family.addressLine3,
				    pincode: family.pincode,
				    district: family.district,
				    area: family.area,
				    state: family.state,
				    country: family.country,
			    }
		    }
	    }
    },
	props : {
		headerTitle : {
			type : String,
			required: true,
			default(){
				return "Create Member";
			}
		},
		organization : {
			type : [Object, null],
			required: true,
			default(){
				return {};
			}
		},
  
		isChurch : {
			type : Boolean,
			required: true,
			default(){
				return false;
			}
		},
		memberModel: {
			type :Object,
			required: true,
			default(){
				return {...PARTNER_MODEL};
			}
		},
        hasSubscriptionType: {
            type: Boolean,
            required: false,
            default(){
                return false;
            }
        }
	},
	watch: {
		memberModel: function(newValue){
			this.member = {...newValue};
		},
	},
	beforeMount(){
		this.initialize();
		let baptismStatus = null;
		this.member = {
			...this.member,
			dob: (typeof this.member.dob!=='undefined' && this.member.dob!==null)
				? this.getDateFromTimeStamp(this.member.dob)
				: null,
			dom: (typeof this.member.dom!=='undefined' && this.member.dom!==null)
				? this.getDateFromTimeStamp(this.member.dom)
				: null,
			dateOfBaptism: (typeof this.member.dateOfBaptism!=='undefined' && this.member.dateOfBaptism!==null)
				? this.getDateFromTimeStamp(this.member.dateOfBaptism)
				: null,
			creationDate: (typeof this.member.creationDate!=='undefined' && this.member.creationDate!==null)
				? this.getDateFromTimeStamp(this.member.creationDate, true)
				: null,
			updateDate: (typeof this.member.updateDate!=='undefined' && this.member.updateDate!==null)
				? this.getDateFromTimeStamp(this.member.updateDate, true)
				: null
		};
		
		if(this.member.dateOfBaptism!==null) this.isBaptized = true;
	},
	beforeUnmount(){
		// if(this.simpleLeave) this.$store.commit("setFamily", null);
	}
}
</script>

<style lang="scss">

</style>