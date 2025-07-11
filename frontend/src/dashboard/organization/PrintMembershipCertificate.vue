<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <DashboardContainer>
        <div :style="pageStyle"
             v-if="member!==null && institution!==null">
            <div style="position: relative;">
                <img :src="membershipCertificate"
                     alt="Background Image"
                     style="width: 100%; background-repeat: no-repeat;"/>
                <div :style="{...topHeaderStyle, ...baskerVille, ...textCenter}">
                    MEMBERSHIP
                </div>
                <div :style="{...topBottomCenter, ...textCenter}">
                    OF CHURCH
                </div>
                <div :style="{...boxed, ...logoContainer}">
                    <img :src="logo"
                         alt="Logo of the Church"
                         style="width: 100%"/>
                </div>
                <div :style="{...boxed, ...presentation, ...textCenter}">
                    THIS  CERTIFICATE  IS  PROUDLY PRESENTED TO
                </div>
                <div :style="{...boxed, ...memberName, ...textCenter}">
                    {{member.gender==="MALE" ? "Mr" : "Ms"}}  {{member.firstName+" " + member.lastName}}
                </div>
                <div :style="{...boxed, ...confession, ...textCenter}">
                    <div>
                        FOR PUBLICLY CONFESSED JESUS CHRIST AS LORD AND SAVIOUR AND HAS BEEN RECEIVED
                    </div>
                    <div>
                        INTO FULL MEMBERSHIP
                    </div>
                </div>
	            
	            <div :style="{...boxed, ...churchName, ...textCenter}">
		            {{ organizationName }}
	            </div>
             
                <div :style="{...boxed, ...footerContainer}">
                    <div style="position:relative; display: flex; justify-content: space-between; width: 100%;">
                        <div :style="{...footerPart, textAlign: 'center'}">
                            <div style="margin-top: 0.9rem">
                                {{getTodayDate()}}
                            </div>
                        </div>
                        
                        <div :style="{...footerPart, textAlign: 'center'}">
                                <img :src="priestSignatureImage"
                                     style="width: 4.3rem; height:1.1rem; margin-top: 0.9rem"
                                     alt=""/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </DashboardContainer>
</template>
<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import membershipCertificate from "@/assets/img/Membership.png";
import {getTodayDate} from "../../utils/AppFx";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
export default {
    name: "PrintMembershipCertificate",
    components:{
        DashboardContainer
    },
    data(){
        return{
            logo: null,
            priest : null,
            priestSignatureImage : null,
            membershipCertificate : membershipCertificate,
            singleInstitutionRequest:{},
            foundInstitution:null,
	        
	        row: {
				display:"flex",
		        flexDirection:"row",
	        },
	        column1:{
				flexDirection:"column",
		        marginLeft:"7%"
		        
	        },
	        column2:{
		        flexDirection:"column",
		        marginLeft:"69%"
	        },
            pageStyle: {
                width: "297mm",
                minWidth: "297mm",
                maxWidth: "297mm",
                height: "210mm",
                minHeight: "210mm",
                maxHeight: "210mm",
            },
            topHeaderStyle: {
                position: "absolute",
                top: "3rem",
	            marginTop: "3.2rem",
	            fontSize: "1.5rem",
                letterSpacing: "0 !important",
                fontKerning: "normal",
                color: "#fff !important",
	            "-webkit-transform": "scaleX(0.968)",
	            transform: "scaleX(0.968)",
            },
            baskerVille: {
                fontFamily:'"Baskerville Old Face" !important'
            },
            textCenter: {
                width: '100%',
                textAlign: 'center'
            },
            topBottomCenter : {
                position: "absolute",
	            top: "8.9rem",
	            marginTop: "0.2rem",
	            fontSize: "1.5rem",
                display: "block",
                justifyContent: "center",
                color: "#fff !important",
                fontFamily:"Poppins Regular",
                "-webkit-transform": "scaleX(0.968)",
                transform: "scaleX(0.968)",
            },
            boxed: {
                position: "absolute",
                top: 0,
	            marginTop:"2rem"
            },
            logoContainer : {
                display: "flex",
                justifyContent: "center",
                marginTop: "21.75%",
                marginLeft: "50%",
                color: "greenyellow",
                backgroundColor: "yellow",
                width: "11.9%",
                borderRadius:"50%",
                overflow: "hidden",
                height: "17%",
                transform: "translateX(-50%)",
            },
            presentation: {
                position: "absolute",
                display: "flex",
                justifyContent: "center",
                marginTop: "26.4rem",
                fontSize: "1rem",
                fontFamily: "Poppins Medium",
                color: "black",
                //"-webkit-transform": "scaleX(1.11)",
                //transform: "scaleX(1.11)",
            },
            memberName:{
                position: "absolute",
                display: "flex",
                fontSize: "1rem",
                fontWeight: 'bolder',
                justifyContent: "center",
	            marginTop: "30.4rem",
                color:"blueviolet",
            },
            confession : {
                position: "absolute",
                justifyContent: "center",
                marginTop: "33.9rem",
                fontSize: "1rem",
                color: '#444',
                fontFamily: "Poppins Medium",
                textAlign: "center !important",
                "-webkit-transform": "scaleX(0.955)",
                transform: "scaleX(0.955)",
                //lineHeight:"1.2"
            },
            churchName : {
                fontFamily : "Poppins Medium",
                fontSize  : "0.9rem",
	            marginTop: "37.1rem",
                color : "#000",
                fontWeight: 'bolder',
            },
            footerContainer : {
                fontFamily : "Poppins Medium",
                fontSize : "20px",
                color: "#000",
                width : "84%",
                marginLeft : "8%",
                marginTop : "60.1%",
            },
            footerPart: {
                width: "20%",
                
            },
            footerFooter : {
                borderTop: '2px solid #000',
                paddingTop: "2%",
                fontsize: "22px !important",
                backgroundColor: "#fff"
            },
            member: this.person,
	        
        }
    },
	computed: {
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		organizationName:function(){
			return (this.organization!==null) ? this.organization.name : "";
		}
	},
    methods: {
        getTodayDate,
        
    },
    beforeMount() {
        if(this.organization!==null) return this.loadImage;
    },
    props:{
        person: {
            type: [Object, null],
            required: true,
            default(){
                return null;
            }
        },
        priestImage: {
            type:Object,
            default(){
                return null;
            }
        },
	    organization:{
			type:[Object, null],
		    default(){
				return null;
		    }
	    }
    },
    watch: {
        person: function(newValue){
            this.member = newValue;
        }
    }
}
</script>

<style >
</style>