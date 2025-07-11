<template>
	<div class="w-100" v-if="institution!==null">
		<div style="position: relative;">
			<div style="display: flex; justify-content: center; margin-bottom: 2rem; font-weight: bolder;">
				<div style="width: 50%; text-align: center;">
					<div style="font-size: 1.1rem;">{{ institution.name.toUpperCase() }}</div>
					<div style="font-size: .8rem;">{{ institution.address }}</div>
				</div>
			</div>
			
			<div style="width: 20%; padding-right: 2rem; top: 0;position: absolute;">
				<img :src="institutionLogo"
				     style="max-width: 100%; max-height: 150px;"
				     :alt="institution.name.toUpperCase()+'\'s logo'"/>
			</div>
		</div>
		
		<div style="display: flex; justify-content: space-around; margin-bottom: 2rem;">
			<div style="display: block; position:relative; width: 50%;">
				<img :src="familyPhoto"
				     alt="Family Picture"
				     style="aspect-ratio: 1.6; width: 100%; display: block; position:relative;"/>
			</div>
		</div>
		<div style="margin-bottom: 1rem;font-weight: bolder;">Family Code: {{ familyCode===null ? '---' : familyCode }}</div>
		<div style="margin-bottom: 1rem;font-weight: bolder;">Head of Family: <span style="text-transform: uppercase !important;">{{ familyHead!==null ? familyHead.fullName : '---' }}</span></div>
		<div>
			<table style="width: 100%; border: 1px solid #999; margin-bottom: 2rem; border-collapse: collapse;">
				<thead>
					<tr style="padding: .5rem; font-weight: bolder; border: 1px solid #999;">
						<td style="padding: .5rem; border: 1px solid #999;">Full name</td>
						<td style="padding: .5rem; border: 1px solid #999;">Birthday</td>
						<td style="padding: .5rem; border: 1px solid #999;">{{ institution.institutionType==='CHURCH' ? 'Member Code' : 'Partner Code' }}</td>
						<td style="padding: .5rem; border: 1px solid #999;">Relation</td>
						<td style="padding: .5rem; border: 1px solid #999;">Member Since</td>
					</tr>
				</thead>
				<tbody>
					<tr style="padding: .5rem; border: 1px solid #999; text-transform: uppercase !important;"
					    v-for="(member, index) in members"
					    :key="'member_index'+index">
						<td style="padding: .5rem; border: 1px solid #999;">{{ member.fullName }}</td>
						<td style="padding: .5rem; border: 1px solid #999;">{{ getDateFromTimeStamp(member.dob) }}</td>
						<td style="padding: .5rem; border: 1px solid #999;">{{ member.code }}</td>
						<td style="padding: .5rem; border: 1px solid #999;">
							{{ $root['displayFamilyRole'](member.familyRole) }}
						</td>
						<td style="padding: .5rem; border: 1px solid #999;">
							{{ member?.memberSince===null ? '---' : getDateFromTimeStamp(member.memberSince) }}
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		
		<div style="display: flex; flex-wrap: wrap; margin-bottom: 2rem;">
			<div style="width: 50%;">Phone: {{ familyPhoneNumber }}</div>
			<div style="width: 50%;">Email: {{ familyEmail }}</div>
			<div style="width: 100%; margin-bottom: 2rem;">
				<div style="width: 100%; border-bottom: 1px solid #ccc; padding-top: .5rem;">Address:</div>
				<div>{{ familyAddress }}</div>
				<div>{{ familyCountry }}</div>
			</div>
			<div style="width: 50%;">
				<div style="font-weight: bold;">Marriage Anniversary:</div>
				<div v-if="theFamily!==null">{{ (typeof theFamily.dob !=='undefined' && theFamily.dob!==null) ? getDateFromTimeStamp(theFamily.dob) : '---' }}</div>
			</div>
		</div>
		
		<div style="text-align: right !important;">
			Last Update: {{ lastUpdate===null ? '---' : getDateFromTimeStamp(lastUpdate) }}
		</div>
		
		<div v-if="loggedInUser!==null" style="text-align: right; margin-top: 1rem;">
			<div>Printed by:</div>
			<div>
				{{ loggedInUser.firstName+' '+loggedInUser.lastName }}
			</div>
		</div>
	</div>
</template>

<script>
import {FAMILY_CREATION_MODEL} from "@/dashboard/members/members";
import {getCountries, getCountryObjectByCode} from "@/dashboard/utils/countries";
import {getDateFromTimeStamp} from "@/utils/AppFx";
import dnoteLogo from "@/assets/img/logos/favicon.png";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import dummyFamilyPhoto from "@/dashboard/organization/families/images/family-small.jpg";
import {isEmpty} from "@/utils/AppStringUtils";

export default {
	name: "FamilyCard",
	data(){
		return {
		}
	},
	computed : {
		loggedInUser: function(){
			return this.$store.getters.getLoggedInUser;
		},
		familyPhoto: function(){
			if(this.theFamily!==null){
				if(typeof this.theFamily.photo!=='undefined' && this.theFamily.photo!==null) {
					return APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+this.theFamily.photo;
				}
			}
			return dummyFamilyPhoto;
		},
		institutionLogo: function() {
			let institution = this.institution;
			if(institution===null) return dnoteLogo;
			if(institution.logo!==null) return APP_CONFIG.BACKEND_SERVER+"/files/logo/"+institution.logo;
			return dnoteLogo;
		},
		theFamily: function(){
			return this.family;
		},
		partner: function(){
			let partnerMap = {
				"HUSBAND" : "WIFE",
				"WIFE" : "HUSBAND",
				"FATHER": "MOTHER",
				"MOTHER": "FATHER"
			};
			if(this.familyHead!==null){
				let partner = this.members.find(member=>{
					let opposite = partnerMap[this.familyHead.familyRole];
					return member.familyRole===opposite;
				});
				if(typeof partner!=='undefined') return partner;
			}
			return {};
		},
		members: function(){
			if(this.theFamily===null) return [];
			let members = (typeof this.theFamily.members!=='undefined' && this.theFamily.members!==null)
				? this.theFamily.members
				: [];
			if(Object.keys(this.familyHead).length>0) {
				let available = []
				members = [
					{...this.familyHead},
					...members
				].filter(member=>{
					if(available.includes(member.code)) return false;
					available = [
						...available,
						member.code
					];
					return true;
				});
			}
			return members;
		},
		institution: function(){ return this.$root['institution'];},
		familyCode: function(){
			let code = null;
			if(this.family!==null) {
				if (this.family.familyCode !== null) {
					code = this.family.familyCode;
				} else {
					if (this.familyHead !== null) code = this.familyHead.code;
				}
			}
			return code;
		},
		familyHead: function(){
			if(this.theFamily===null) return null;
			let familyHead = this.theFamily.members.find(member=>member.id===this.theFamily.familyHead);
			if(typeof familyHead==='undefined') return null;
			return familyHead;
		},
		familyPhoneNumber: function(){
			if(this.theFamily===null) return "---";
			return '+'+this.theFamily.phoneCode+' '+this.theFamily.phone;
		},
		familyEmail: function(){
			if(this.theFamily===null) return "---";
			return (typeof this.theFamily.email==='undefined' || this.theFamily.email===null) ? '---' : this.theFamily.email;
		},
		familyAddress: function(){
			if(this.theFamily===null) return "---";
			let address = [];
			if(!isEmpty(this.theFamily.addressLine1)) address.push(this.theFamily.addressLine1);
			if(!isEmpty(this.theFamily.addressLine2)) address.push(this.theFamily.addressLine2);
			if(!isEmpty(this.theFamily.addressLine3)) address.push(this.theFamily.addressLine3);
			if(!isEmpty(this.theFamily.district)) address.push(this.theFamily.district);
			if(typeof this.theFamily.pincode!=='undefined' && this.theFamily.pincode!==null) address.push(this.theFamily.pincode);
			if(!isEmpty(this.theFamily.country)) {
				let country = getCountryObjectByCode(this.theFamily.country);
				if(country!==null) address.push(country.label);
			}
			address = address.join(", ");
			return address.toUpperCase();
		},
		familyCountry: function(){
			if(this.theFamily===null) return "---";
			let countryAddress = [],
				country = this.getCountries().find(country=> country.value===this.theFamily.country);
			countryAddress.push(this.theFamily.state);
			if(typeof country!=='undefined') countryAddress.push(country.label);
			return countryAddress.join(', ');
		},
		birthdays: function(){
			if(this.theFamily===null) return [];
			if(typeof this.theFamily.members==='undefined') return [];
			return this.theFamily.members.map(member=>{
				return member.dob;
			}).filter(dob=>dob!==null);
		},
		lastUpdate: function(){
			if(this.theFamily===null) return null;
			if(typeof this.theFamily.lastUpdate!=='undefined' && this.theFamily.lastUpdate!==null) return this.theFamily.lastUpdate
			return null;
		}
	},
	methods: {
		getDateFromTimeStamp,
		getCountries,
	},
	props: {
		family: {
			type: [Object, null],
			required: true,
			default(){
				return {...FAMILY_CREATION_MODEL};
			}
		}
	},
}
</script>

<style scoped lang="scss">

</style>