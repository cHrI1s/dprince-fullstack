<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <div :style="pageStyle"
         v-if="institution!==null">
        <div style="position: relative;" >
            <img :src="birthdayImage"
                 alt="Background Image"
                 style="width:148.5mm; max-width: 148.5mm; min-width: 148.5mm;
                        height: 210mm;
                        min-height: 210mm;
                        max-height: 210mm;"/>
            <div :style="{...topHeaderStyle, ...trajan, ...textCenter}">
                HAPPY
            </div>
            <div :style="{...topBottomCenter, ...textCenter}">
                BIRTHDAY
            </div>
            
            <div :style="{...boxed, ...presentation, ...textCenter}">
                {{(person!==null) ? person.firstName + " " + person.lastName : "---" }}
            </div>
            <div :style="{...boxed, ...memberName, ...textCenter}">
            
            </div>
            <div :style="{...boxed, ...introduction, ...textCenter}">
               <div> May He give you the</div>
                desire of your heart and <div>make all your </div>plan succeed
                <div>
                    Psalm 20:4
                </div>
            </div>
            <div :style="{...boxed, ...churchName, ...textCenter}">
                 <strong>{{(institution!==null) ? institution.name : "--"}}</strong>
            </div>
            <div :style="{...boxed, ...footerContainer}">
                <div style="position:relative; display: flex; justify-content: space-between; width: 100%;">
                    <div :style="{...footerPart, textAlign: 'center'}">
                        <div>
                        
                        </div>
                        <div :style="footerFooter">
                        
                        </div>
                    </div>
                    
                    <div :style="{...footerPart, textAlign: 'center'}">
                        <div>
                        
                        </div>
                        <div :style="footerFooter">
                        
                        </div>
                    </div>
                </div>
                <div :style="{...boxed, ...bibleVerse, ...textCenter}">
                
                </div>
                <div :style="{...boxed, ...bibleVerseFooter, ...textCenter}">
                    <div>
                    
                    </div>
                    <div>
                    
                    </div>
                </div>
            </div>
        
        </div>
    </div>
</template>
<script>
import birthdayCertificate from "@/assets/img/Template -2 Birthday greetings.png"

export default {
    name: "PrintBirthdayCertificate",
    data(){
        return{
            birthdayImage : birthdayCertificate,
            pageStyle: {
                width: "148.5mm",
                minWidth: "148.5mm",
                maxWidth: "148.5mm",
                height: "210mm",
                minHeight: "210mm",
                maxHeight: "210mm",
                backgroundSize:"cover"
            },
            
            topHeaderStyle: {
                position: "absolute",
                top: "0%",
                marginTop: "6.5rem",
                fontFamily:'"Bickham Script One" !important',
                fontSize: "50px",
                letterSpacing: "0 !important",
                fontKerning: "normal",
                color: "#00f !important",
                "-webkit-transform": "scaleX(0.98)",
                transform: "scaleX(0.98)",
            },
            trajan: {
                fontFamily:'"Bickham Script One" !important',
                color: "#dfbe09",
            },
            textCenter: {
                width: '100%',
                textAlign: 'center'
            },
            topBottomCenter: {
                position: "absolute",
                top: 0,
                display: "block",
                justifyContent: "center",
                marginTop: "27.70%",
                fontSize: "50px",
                letterSpacing: "2px",
                color: "#dfbe09",
                fontFamily:'"Bickham Script One" !important',
                "-webkit-transform": "scaleX(0.98)",
                transform: "scaleX(0.98)",
            },
            boxed: {
                position: "absolute",
                top: 0,
            },
            logoContainer: {
                display: "flex",
                justifyContent: "center",
                marginTop: "21.75%",
                marginLeft: "50%",
                color: "greenyellow",
                backgroundColor: "yellow",
                width: "11.9%",
                borderRadius: "50%",
                overflow: "hidden",
                height: "17%",
                transform: "translateX(-50%)",
            },
            presentation: {
                position: "absolute",
                display: "flex",
                justifyContent: "center",
                marginTop: "55.2%",
                fontSize: "29px",
                fontFamily: "GOUDOS",
                color: "black",
                letterSpacing: "2.4px",
                "-webkit-transform": "scaleX(0.98)",
                transform: "scaleX(0.98)",
            },
            memberName: {
                position: "absolute",
                display: "flex",
                fontSize: "24px",
                fontFamily: "Helvetica-Oblique",
                fontWeight: 'bolder',
                justifyContent: "center",
                marginTop: "31.20%",
                color: "blueviolet",
                "-webkit-transform": "scaleX(0.85)",
                transform: "scaleX(0.85)",
            },
            confession: {
                position: "absolute",
                justifyContent: "center",
                marginTop: "46.75%",
                fontSize: "12px",
                color: '#444',
                fontFamily: "Poppins Medium",
                textAlign: "center !important",
                "-webkit-transform": "scaleX(0.95)",
                transform: "scaleX(0.95)",
                lineHeight: "1.2"
            },
            introduction: {
                fontFamily: "Helvetica-Oblique",
                fontSize: "50px",
                marginTop: "80%",
                lineHeight:"85%",
                color: "#000",
                "-webkit-transform": "scaleX(0.48)",
                transform: "scaleX(0.48)",
            },
            churchName: {
                fontFamily: "BOOKOSB",
                fontSize: "1.8rem",
                marginTop: "128%",
            },
            bibleVerse: {
                fontFamily: "BOOKOSB",
                fontSize: "20px",
                marginTop: "2%",
                color: "#000",
            },
            bibleVerseFooter: {
                fontFamily: "BOOKOS",
                fontSize: "15px",
                marginTop: "7%",
                color: "#000",
                "-webkit-transform": "scaleX(1.16)",
                transform: "scaleX(1.16)",
            },
            footerContainer: {
                fontFamily: "Almarose-Medium",
                fontSize: "20px",
                color: "#000",
                width: "80%",
                marginLeft: "10%",
                marginTop: "55.1%",
            },
            footerPart: {
                width: "20%",
            },
            footerFooter: {
                borderTop: '2px solid #000',
                paddingTop: "2%",
                fontsize: "22px !important",
                backgroundColor: "#fff"
            },
            institution : null,
            member : null,
        }
    },
    methods:{
    
    },
    watch: {
        organization: function(newValue){
            this.institution = newValue;
        },
        person: function(newValue){
            this.member = newValue;
        }
    },
    
    props:{
        organization: {
            type: [Object, null],
            required: true,
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
}
</script>



<style>

</style>