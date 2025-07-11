<template>
	<div :style="pageStyle">
		<div style="position: relative;"
		     v-if="member!==null && theInstitution!==null">
			<img :src="bgImg"
			     alt="Background Image"
			     style="width: 100%; background-repeat: no-repeat;"/>
			
			<div style="position: absolute; top: 0; margin-top: 28mm;
						font-size: 180%;
						font-weight: bolder;
						color: #000; width: 297mm; text-align: center;">
				<strong>{{theInstitution.name.toUpperCase()}}</strong>
			</div>
			
			
			<div style="position: absolute; top: 0; width: 297mm;
			text-align: center; font-size: 180%;
			margin-top: 101.5mm; color: #000; font-weight: bolder;">
				{{ (member.firstName+" " + member.lastName).toUpperCase() }}
			</div>
			
			<div style="position:absolute; top: 0; margin-top: 137mm;
						width: 45mm; height: 15mm;
						margin-left: 196.5mm;">
				<div style="display: flex; align-items: flex-end; height: 100%; width: 100%; justify-content: space-around;">
					<img :src="priestSignatureImage"
					     style="min-height: 1rem; min-width: 2rem; max-width: 100%; max-height:100%;"
					     alt="Signature"/>
				</div>
			</div>
			
			<div style="position:absolute; top: 0;
		                margin-top: 144.5mm; width: 46mm;
		                margin-left: 55mm;
		                text-align: center; color: #000;
		                font-weight: bolder; font-size: 125%;">
				{{ dedicationDate  }}
			</div>
			
			
			<div style="position:absolute; top: 0;
		                margin-top: 191mm; width: 116mm;
		                margin-left: 140mm;
		                text-align: left; color: #000;
		                font-weight: bolder; font-size: 125%;">
				{{ certificateNumber }}
			</div>
		
		</div>
	</div>
</template>


<script>
import DedicationImage from "@/assets/img/Child-Dedication.png";
import {getDateFromTimeStamp} from "@/utils/AppFx";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "ChildDedicationCertificate",
	data(){
		return {
			pageStyle: {
				height: "210mm",
				width: "297mm",
			},
			member: this.person
		}
	},
	methods:{
		getDateFromTimeStamp
	},
	computed:{
		bgImg: function(){
			let certificatesBgs = this.$store.getters.getCertificatesBgs;
			if(certificatesBgs===null) return DedicationImage;
			if(typeof certificatesBgs['KID_DEDICATION']!=='undefined'
				&& certificatesBgs['KID_DEDICATION']!==null) return certificatesBgs['KID_DEDICATION'];
			return DedicationImage;
		},
		priestSignatureImage: function(){
			return this.$store.getters.getSignature;
		},
		dedicationDate: function(){
			if(this.member!==null) {
				if (this.member.dedicationDate !== 'undefined' && this.member.dedicationDate !== null) {
					return getDateFromTimeStamp(this.member.dedicationDate);
				}
			}
			return "---";
		},
		certificateNumber: function(){
			if(this.member===null) return null;
			return this.member.certificateNumber;
		},
		theInstitution: function(){
			return this.institution;
		},
	},
	props: {
		person: {
			type: [Object, null],
			required: true,
			default(){
				return null;
			}
		},
		institution: {
			type: [Object, null],
			required: true,
			default(){
				return null;
			}
		},
	},
	watch: {
		person: function(newValue){
			this.member = {
				...newValue,
				dedicationDate: (typeof newValue.dedicationDate!=='undefined')
					? newValue.dedicationDate
					: null
				
			};
		}
	}
}
</script>

<style scoped lang="scss">

</style>