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
	        :header="memberFullName"
	        class="w-md-75 w-100">
		
		<div class="row">
			<div class="col-md-3 col-4">
				<img :src="memberProfile" alt="Profile"
				     class="w-100 d-block position-relative user-profile img-thumbnail"/>
			</div>
			<div class="col-md-9 col-8">
				<div class="d-block position-relative pt-1">
					<table class="w-100">
						<tr v-if="typeof themember.titles!=='undefined'">
							<td class="py-1 px-2">Full Name</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
								{{ getDbLabel(themember.titleId, themember.titles, "shortName") }} {{ themember.firstName+" "+themember.lastName }}
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">DOB</td>
							<td class="py-1 px-2 fw-bolder">{{ themember.dob!==null ? getDateFromTimeStamp(themember.dob) : "--"}}</td>
							<td class="py-1 px-2">DOM</td>
							<td class="py-1 px-2 fw-bolder">{{ themember.dom!==null ? getDateFromTimeStamp(themember.dom) : "--"}}</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Gender</td>
							<td class="py-1 px-2 fw-bolder text-uppercase">{{ getDropdownLabel(themember.gender, genders) }}</td>
						</tr>
						<tr v-if="themember.baptized!==null">
							<td class="py-1 px-2">Baptized</td>
							<td class="py-1 px-2 fw-bolder text-uppercase">{{ themember.baptized==='BAPTIZED' ? "Yes" : "No" }}</td>
							<td class="py-1 px-2">Date Of Baptism</td>
							<td class="py-1 px-2 fw-bolder">{{ themember.dateOfBaptism!==null ? getDateFromTimeStamp(themember.dateOfBaptism) : "--"}}</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Code</td>
							<td class="py-1 px-2 fw-bolder">{{ themember.code }}</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Phone</td>
							<td class="py-1 px-2 fw-bolder" colspan="3">
						<span v-if="themember.phone!==null">
							{{ (themember.phoneCode!==null ? "+"+themember.phoneCode : "") }}
						</span>
								<span v-else>No Phone Number</span>
								{{ themember.phone!==null ? themember.phone : "" }}
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Alternate Phone</td>
							<td class="py-1 px-2 fw-bolder" colspan="3">
								{{
									(themember.alternatePhone===null)
										? "---"
										:  (themember.whatsappNumberCode!==null
											? "+"+themember.alternatePhoneCode
											: "")
										+themember.alternatePhone
								}}
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">WhatsApp</td>
							<td class="py-1 px-2 fw-bolder" colspan="3">
								{{
									(themember.whatsappNumber===null)
										? "---" :
										(themember.whatsappNumberCode!==null ? "+"+themember.whatsappNumberCode :"")
										+" "+themember.whatsappNumber
								}}
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Landline Phone</td>
							<td class="py-1 px-2 fw-bolder" colspan="3">
								<div v-if="themember.landlinePhone!==null">
									<span v-if="themember.landlinePhoneCode!==null">
										{{ (themember.landlinePhoneCode!==null ? "+"+themember.landlinePhoneCode : "") }}
									</span>
										<span v-else>+91</span>
										<span>
										{{ themember.landlinePhone!==null ? " "+themember.landlinePhone : "" }}
									</span>
								</div>
								<div v-else>---</div>
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Email</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">{{ themember.email===null ? '---' : themember.email }}</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Address</td>
							<td class="py-1 px-2 fw-bolder" colspan="3">
								<div class="d-block position-relative text-uppercase">{{ themember.addressLine1 }}</div>
								<div class="d-block position-relative text-uppercase">{{ themember.addressLine2 }}</div>
								<div class="d-block position-relative text-uppercase">{{ themember.addressLine3 }}</div>
								<div class="d-block position-relative text-uppercase">{{ themember.district+" - " +
								+ themember.pincode +
								", "+themember.state +
								", "+ getCountryNameByCode(themember.country) }}</div>
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Language</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">{{ themember.language }}</td>
						</tr>
						<tr>
    <td class="py-1 px-2" style="text-align: left; padding-left: 0;">Status</td> <td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">{{ themember.active ? "Active" : "Inactive" }}</td>
</tr>
						
						<tr v-if="themember.subscription!==null">
							<td class="py-1 px-2">Subscription</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">{{ themember.subscription }}</td>
						</tr>
						
						<tr v-if="typeof themember.subscriptionsMap!=='undefined'&& Object.keys(themember.subscriptionsMap).length>0">
							<td class="py-1 px-2">Subscriptions</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
								<table class="w-100">
									<thead>
										<tr class="table-header text-white fw-bolder">
											<td class="px-2">Name</td>
											<td class="px-2">Subscription</td>
											<td class="px-2">Deadline</td>
										</tr>
									</thead>
									<tbody>
										<tr v-for="(subscriptionKey, index) in Object.keys(themember.subscriptionsMap)"
										    :key="'subscription_'+index">
											<td class="px-2">{{ subscriptionKey }}</td>
											<td class="px-2">
												{{
													(themember.subscriptionsMap[subscriptionKey].subscription.toUpperCase()==='LIFETIME')
														? "LIFETIME"
														: themember.subscriptionsMap[subscriptionKey].subscription }}
											</td>
											<td class="px-2">
												{{
													(themember.subscriptionsMap[subscriptionKey].subscription.toUpperCase()==='LIFETIME')
														? "No deadline"
														: getDateFromTimeStamp(themember.subscriptionsMap[subscriptionKey].deadline) }}
											</td>
										</tr>
									</tbody>
								</table>
							</td>
						</tr>
						<tr v-if="(typeof themember.categories!=='undefined' && themember.categories!==null && institutionIsChurch) && !institutionIsChurch">
							<td class="py-1 px-2">Categories</td>
							<td class="py-1 px-2 fw-bolder text-uppercase" colspan="3">
								<ul>
									<li v-for="(categoryId, index) in themember.categoriesIds"
									    :key="index">
										{{ getDbLabel(categoryId, themember.categories, "name") }}
									</li>
								</ul>
							</td>
						</tr>
						<tr>
							<td class="py-1 px-2">Creation</td>
							<td class="py-1 px-2 fw-bolder">{{ getDateFromTimeStamp(themember.creationDate, true) }}</td>
							
							<td class="py-1 px-2">Modification</td>
							<td class="py-1 px-2 fw-bolder">{{ themember.updateDate ? getDateFromTimeStamp(themember.updateDate, true) : "--"}}</td>
						</tr>
					</table>
					
					
					<div class="row mt-4"
					     v-if="typeof themember.familyMembers!=='undefined' && themember.familyMembers!==null && themember.familyMembers.length>0">
						<div class="col-12 mb-4 fw-bolder">
							Family Members
						</div>
						<div class="col-md-6 mb-4"
						     v-for="(familyMember, fINdex) in themember.familyMembers"
						     :key="'f_index_'+fINdex">
							<div class="card bg-light text-uppercase">
								<div class="card-header bg-light fw-bolder py-3 text-uppercase">
									<div class="d-block position-relative">
										{{ familyMember.firstName+' '+familyMember.lastName }}
									</div>
									<div class="text-sm">
										{{ getFamilyRole(familyMember.familyRole) }}
									</div>
								</div>
								<div class="card-footer pt-0 pb-2 text-uppercase">
									<div>Code: <span class="fw-bolder">{{ familyMember.code }}</span></div>
									<div>Phone:
										<span class="fw-bolder">
									<span v-if="familyMember.phone!==null">
										{{ (familyMember.phoneCode!==null ? "+"+familyMember.phoneCode : "") }}
									</span>
									<span v-else>No Phone Number</span>
										{{ familyMember.phone!==null ? familyMember.phone : "" }}
									</span>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<template #footer>
			<div class="w-100 d-block position-relative mx-0 px-0">
				<div class="w-100 row px-0 mx-0">
					<div :class="institutionIsChurch ? 'col-6 pe-2 ps-0' : 'col-12 px-0'">
						<Button label="Close"
						        severity="danger"
						        icon="pi pi-times fw-bolder"
						        @click="()=>close(false)"
						        class="w-100 fw-bolder"/>
					</div>
					
					<div class="col-6 ps-2 pe-0"
					     v-if="institutionIsChurch">
						<Button label="Family Member"
						        severity="info"
						        icon="pi pi-plus fw-bolder"
						        @click="()=>close(true)"
						        class="w-100 fw-bolder"/>
					</div>
				</div>
			</div>
		</template>
	</Dialog>
</template>


<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import {PARTNER_MODEL} from "@/dashboard/organization/Organization";
import {FAMILY_ROLES, GENDERS} from "../../../members/members";
import {getDateFromTimeStamp, getDeepObjectValue} from "@/utils/AppFx";
import {getCountryNameByCode} from "../../../utils/countries";
import userAvatar from "@/assets/img/user-avatar.png";

export default {
	name: "MemberDetails",
	components: {Button, Dialog},
	emits: ["close"],
	props: {
		visible: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
		member: {
			type: [Object, null],
			required: true,
			default(){
				return {...PARTNER_MODEL};
			}
		}
	},
	watch: {
		visible: function(newValue){
			this.shown = newValue;
		},
		member: function(newValue){
			this.themember = newValue;
		}
	},
	data(){
		return {
			visibleLocal: this.visible,
			themember: this.member,
			genders: GENDERS
		}
	},
	computed: {
		theMember: function(){
			return this.member;
		},
		memberProfile: function(){
			if(typeof this.theMember.profile!=='undefined' && this.theMember.profile!==null) return this.$root['getImageUrl'](this.theMember.profile)
			return userAvatar;
		},
		memberFullName: function(){
			let fullName = this.themember.firstName+' '+this.themember.lastName;
			return fullName.toUpperCase();
		},
		institutionIsChurch: function(){
			return this.isChurch;
		},
		shown : {
			get: function(){
				return this.visibleLocal;
			},
			set: function(newValue){
				this.visibleLocal = newValue;
			}
		}
	},
	methods: {
		getFamilyRole: function(familyRole){
			if(familyRole!==null) {
				let familyRoleObject = FAMILY_ROLES.find(role=>role.value===familyRole);
				if(typeof familyRoleObject!=='undefined') return familyRoleObject.label;
			}
			return "---";
		},
		close: function(value=false){
			this.$emit('close', value);
		},
		getDeepObjectValue,
		showCategories: function(categoriesIds, categories){
			if(categoriesIds===null) return null;
			return categories.filter(singleCategory => {
				return categoriesIds.includes(singleCategory.value)
			}).map(category => {
				return category.label;
			});
		},
		getCountryNameByCode,
		getDateFromTimeStamp,
		getDropdownLabel: function(value, options){
			let option = options.find(singleOption=>{
				return singleOption.value===value;
			});
			return (typeof option==="undefined") ? "---" : option.label;
		},
		getDbLabel: function(value, options, label){
			let option = options.find(singleOption=>{
				return singleOption.id===value;
			});
			return (typeof option==="undefined") ? "" : option[label];
		},
	}
}
</script>

<style scoped lang="scss">

</style>