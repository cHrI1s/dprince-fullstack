<template>
	<div style="display: block !important; position: relative !important;">
		<img :src="marriageImage"
		     style="width: 297mm;
					max-width: 297mm;
					height: 210mm;
					max-height: 210mm;
					aspect-ratio: 297/210;
					display: block;"
		     alt="Marriage Certificate"/>
		
		<div style="position:absolute; font-weight: bolder; z-index: 1;
		top: 84mm; width: 261mm;
		margin-left: 18mm;
		display: flex;
		font-size: 27px;">
			<div style="color: #000;
			width: 120mm;
			text-align: right !important;">
				{{ marriedPeople.length===0 ? "" : marriedPeople[0].fullName }}
			</div>
			<div style="color: #000;
			width: 120mm;
			margin-left: 20mm;">
				{{ marriedPeople.length===0 ? "" : marriedPeople[1].fullName }}
			</div>
		</div>
		
		<div style="display: flex;
					left: 105mm;
					font-size: 22px;
					font-weight: bolder;
					font-style: italic;
					position: absolute !important;
					color: #000;
					top: 141.5mm;">
			<div v-html="marriageDate.length===0 ? '' : marriageDate[0]"></div>
			<div style="margin-left: 50mm; margin-top: 1.5mm;" v-html="marriageDate.length===0 ? '' : marriageDate[1]"></div>
		</div>
		
		
		<div style="display: flex;
					left: 105mm;
					font-size: 22px;
					font-style: italic;
					position: absolute !important;
					color: #000;
					top: 157.5mm;">
			<div v-if="institution!==null" style="font-weight: bolder;">{{ institution.name.toUpperCase() }}</div>
		</div>
		
		<div style="display: flex;
					align-items: flex-end !important;
					left: 150mm;
					width: 40mm;
					height: 12mm;
					font-size: 22px;
					font-style: italic;
					position: absolute !important;
					color: #000;
					top: 168.5mm">
			<div v-if="signature!==null" style="width: 100%; height: 100%;">
				<img :src="signature"
				     style="max-width: 100%; max-height: 100%"
				     alt="Priest Signature"/>
			</div>
		</div>
		
		<div style="position: absolute; top: 188mm; margin-left: 125mm; font-weight: bolder; color: #000;">
			{{ certificateNo }}
		</div>
	</div>
</template>


<script>
import defaultImage from "@/assets/img/Marriage-Sample.png";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {isEmpty} from "@/utils/AppStringUtils";
export default {
	name: "MarriageCertificate",
	data(){
		return {
			loadedSignature: null
		}
	},
	computed: {
		marriageImage: function(){
			let output = defaultImage;
			let certificatesBgs = this.$store.getters.getCertificatesBgs;
			if(certificatesBgs!==null){
				if(typeof certificatesBgs['MARRIAGE']!=='undefined') output = certificatesBgs['MARRIAGE'];
			}
			return output;
		},
		loggedInUser: function(){
			return this.$store.getters.getLoggedInUser;
		},
		signature: function(){
			if(this.institution===null) return null;
			return this.loadedSignature;
		},
		institution: function(){
			let loggedInUser = this.loggedInUser;
			if(loggedInUser===null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)) return this.$store.getters.getInstitution;
			return loggedInUser.institution;
		},
		theMarriageDetails: function(){
			return this.familyDetails;
		},
		marriedPeople: function(){
			if(this.theMarriageDetails===null) return [];
			if(this.theMarriageDetails.members.length!==2) return [];
			return this.theMarriageDetails.members;
		},
		marriageDate: function(){
			if(this.theMarriageDetails===null) return ["hey", "hey hey"];
			let date = new Date(this.theMarriageDetails.dob);
			const months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
			let firstPart = date.getDate()+"";
			if(firstPart.endsWith("11")) firstPart += "<sup>th</sup>";
			else if(firstPart.endsWith("1")) firstPart += "<sup>st</sup>";
			else if(firstPart.endsWith("2")) firstPart += "<sup>nd</sup>";
			else if(firstPart.endsWith("3")) firstPart += "<sup>rd</sup>";
			else firstPart += "<sup>th</sup>";
			return [firstPart, months[date.getMonth()]+", "+date.getFullYear()];
		},
		certificateNo: function(){
			if(this.theMarriageDetails!==null) {
				if (typeof this.theMarriageDetails.certificateNo !== 'undefined') {
					return this.theMarriageDetails.certificateNo;
				}
			}
			return "Certificate No";
		}
	},
	methods: {
		loadSignature: function(){
			if(this.institution!==null && !isEmpty(this.institution.priestSignature)) {
				this.$api.get("/files/get/"+this.institution.priestSignature, {
					responseType: 'blob'
				}).then(response=>{
					this.$store.commit("setLoading", true);
					this.loadedSignature = URL.createObjectURL(response);
					this.$store.commit("setLoading", false);
				}).catch(ignored=>{});
			}
		},
	},
	beforeMount(){
		this.loadSignature();
	},
	props: {
		familyDetails:{
			type:Object,
			default(){
				return null;
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		},
	}
}
</script>

<style scoped lang="scss">
</style>