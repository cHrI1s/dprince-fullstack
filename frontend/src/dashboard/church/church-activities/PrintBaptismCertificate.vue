<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
     <div :style="pageStyle">
         <div style="position: relative;"
              v-if="institution!==null && member!==null">
             <img :src="bgImg"
                  alt="Background Image"
                  style="width: 297mm; height: 210mm; background-repeat: no-repeat;"/>
             <div style="position: absolute; top: 0; width: 297mm;
             text-align: center; font-size: 150%; margin-top: 87mm; color: #000; font-weight: bolder;">
                 {{ member?.gender==="MALE" ? "Mr." : "Ms."}}  {{ (member.firstName+" " + member.lastName).toUpperCase() }}
             </div>
             
             
             <div style="position: absolute; top: 0; margin-top: 124mm; font-size: 150%; font-weight: bolder; color: #000; width: 297mm; text-align: center;">
                  <strong>{{institution.name.toUpperCase()}}</strong>
             </div>
            
            <div style="position:absolute; top: 0; margin-top: 140mm; width: 45mm; height: 15mm; margin-left: 35mm;">
                <div style="display: flex; align-items: flex-end; height: 100%; width: 100%; justify-content: space-around;">
	                <img :src="priestSignatureImage"
	                     style="min-height: 1rem; min-width: 2rem; max-width: 100%; max-height:100%;"
	                     alt="Signature"/>
                </div>
            </div>
             
             <div style="position:absolute; top: 0;
		                margin-top: 148.5mm;
		                 width: 46mm;
		                margin-left: 219mm;
		                text-align: center; color: #000;
		                font-weight: bolder; font-size: 125%;">
	             {{ (member.dateOfBaptism===null) ? "---" : getDateFromTimeStamp(member.dateOfBaptism) }}
             </div>
	         
	         
	         <div style="position:absolute; top: 0;
		                margin-top: 191.7mm;
		                 width: 116mm;
		                margin-left: 135mm;
		                color: #000;
		                font-weight: bolder; font-size: 125%;">
	             {{ certificateNo }}
             </div>
         </div>
     </div>
</template>


<script>

import BaptismImg from "@/assets/img/Baptism-Sample.png"
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {getDateFromTimeStamp} from "@/utils/AppFx";

export default {
    name: "PrintBaptismCertificate",
    components:{},
    data() {
	    return {
		    pageStyle: {
			    height: "210mm",
			    maxHeight: '210mm',
			    width: "297mm",
			    maxWidth: '297mm',
			    overflow: 'hidden'
		    },
	    }
    },
    methods:{
	    getDateFromTimeStamp
    },
    emits:['closeBaptismCertificateDialog'],
    props:{
        showCertificate: {
            type:Boolean,
            default(){
                return false;
            }
        },
        priestImage: {
            type:Object,
            default(){
                return null;
            }
        },
	    person: {
		    type: [Object, null],
		    required: true,
		    default(){
			    return null;
		    }
	    }
    },
    computed:{
	    bgImg: function(){
			let certificatesBgs = this.$store.getters.getCertificatesBgs;
			if(certificatesBgs===null) return BaptismImg;
			if(typeof certificatesBgs['BAPTISM']!=='undefined'
				&& certificatesBgs['BAPTISM']!==null) return certificatesBgs['BAPTISM'];
			return BaptismImg;
	    },
	    priestSignatureImage: function(){
		    return this.$store.getters.getSignature;
	    },
        member: function(){
            return this.person;
        },
	    certificateNo: function(){
			if(this.member!==null) {
				if(typeof this.member.certificateNo!=='undefined'){
					return this.member.certificateNo;
				}
			}
			return "MC/BPTM/25DMC0110000/1"
	    },
	    institution: function(){
		    let loggedInUser = this.$store.getters.getLoggedInUser;
		    if(loggedInUser==null) return null;
		    if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
			    return this.$store.getters.getInstitution;
		    }
		    return loggedInUser.institution;
	    },
    }
}
</script>



<style scoped lang="scss">

    
</style>