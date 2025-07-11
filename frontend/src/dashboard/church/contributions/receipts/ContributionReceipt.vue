<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<!--<div class="page-containers">-->
	<div :style="receiptSlip">
		<div :style="backgroundImageStyle"></div>
		
		<div :style="headerStyle"
		     v-if="!headerInBottom && !isCustomModel">
			<div :style="headerContentStyle.logoContainer"
			     v-if="leftSideLogoShown">
				<img :src="donation.institution.logo"
				     :alt="donation.institution.name+' logo'"
				     v-if="donation.institution.logo!==null"
				     style="max-width: 100%; max-height: 100px; text-align:center;"/>
			</div>
			<div :style="headerContentStyle.institutionHeaderInfoContainer">
				<div style="font-size: 1.2rem; font-weight: bolder;text-transform: uppercase;">{{ donation.institution.name }}</div>
				<div :style="{...fontSmall, ...textUpperCase}">
					Address: <span class="text-uppercase">{{ donation.institution.address }}</span>
				</div>
				<div :style="{...fontSmall}">
					Contact Number: +91 {{ donation.institution.receiptPhone }}
				</div>
			</div>
			
			
			<div :style="{...headerContentStyle.logoContainer, ...{textAlign: 'right'}}"
			     v-if="rightSideLogoShown">
				<img :src="donation.institution.logo"
				     :alt="donation.institution.name+' logo'"
				     v-if="donation.institution.logo!==null"
				     style="max-width: 100%; max-height: 100px; text-align:center;"/>
			</div>
			
			
			<!-- Show for template 2 only-->
			<div :style="headerContentStyle.qrCodeContainer"
			     v-if="qrCodeShown">
				<qrcode-vue :size="140"
				            :render-as="'svg'"
				            v-if="typeof donation.institution.upi!=='undefined' && donation.institution.upi!==null"
				            :value="donation.institution.upi">
				</qrcode-vue>
			</div>
		</div>
		
		<!-- Receipt content-->
		<div :style="receiptContentStyle">
			<div :style="{...contentContainerStyle.style, ...contentOnTop}">
				<div style="position: relative;">
					<div :style="{...contentContainerStyle.receiptHeader, ...fontBold}">
						Receipt
					</div>
					<div :style="contentContainerStyle.contentHeaderRight">
						<div>
							Receipt No:
							<span style="text-transform:uppercase !important;">
								<strong>{{ donation.receiptNo }}</strong>
							</span>
						</div>
						<div>
							Date:
							<span style="text-transform:uppercase !important;">
								<strong>{{getDateFromTimeStamp(donation.entryDate)}}</strong>
							</span>
						</div>
					</div>
				</div>
				
				<div style="">
					<div style="margin-top: 10px;">
						Dear
						<span style="text-transform:uppercase !important;">
							<strong v-if="typeof donation.member!=='undefined'" class="text-uppercase">
							{{donation.member.title + " " +donation.member.firstName+' '+donation.member.lastName}},
							{{ donation.member.code }},
							{{
									donation.member.addressLine1 +" "
									+ donation.member.addressLine2 + " "
									+ donation.member.addressLine3 + " "
									+ donation.member.district + " "
									+ donation.member.pincode + " "
									+ donation.member.state + " "
								
								}}
						</strong>.
						</span>
					</div>
					<div>
						With grateful thanks, we hereby acknowledge the receipt of Rs.
						<span style="text-transform:uppercase !important;">
							{{ totalAmount.toLocaleString("en-IN") }}
							<strong>({{ " Rupees "+ moneyInWords(totalAmount) + ' only '}})</strong>
						</span>.
					</div>
					
					<div style="margin-top: 10px;">
						<table :style="contentContainerStyle.tableStyle">
							<tr>
								<td :style="{...contentContainerStyle.cellStyle, ...contentContainerStyle.bold}">{{ isInstitutionChurch===true ? 'Member' : 'Partner' }} Code</td>
								<td :style="{...contentContainerStyle.cellStyle, ...contentContainerStyle.bold}">{{ isInstitutionChurch===true ? 'Offered To' : 'Donation To'}}</td>
								<td :style="{...contentContainerStyle.cellStyle, ...contentContainerStyle.bold}">{{ isInstitutionChurch===true ? 'Offered' : 'Donated'}} Through</td>
								<td :style="{...contentContainerStyle.cellStyle, ...contentContainerStyle.bold}">Amount</td>
							</tr>
							
							<tr v-for="(singleDonation, index) in donation.donations"
							    style="text-transform: uppercase !important;"
							    :key="index">
								<td :style="{...contentContainerStyle.cellStyle}" class="text-uppercase">{{ donation.member.code }}</td>
								<td :style="{...contentContainerStyle.cellStyle}" class="text-uppercase" v-if="singleDonation.contribution!==null">{{ singleDonation.contribution.name }}</td>
								<td :style="{...contentContainerStyle.cellStyle}" class="text-uppercase" v-else-if="singleDonation.category!==null">{{ singleDonation.category.name }}</td>
								<td :style="{...contentContainerStyle.cellStyle}" v-else>---</td>
								<td :style="{...contentContainerStyle.cellStyle}" class="text-uppercase">{{ getDropdownLabel(donation.paymentMode, paymentModes) }}</td>
								<td :style="{...contentContainerStyle.cellStyle}">{{ 'Rs. '+singleDonation.amount.toLocaleString('en-IN') }}</td>
							</tr>
						</table>
					</div>
				</div>
				
				<div style="margin-top: 25px;" v-if="isCustomModel">
					<div :style="{...footerColor, ...footerText, ...{marginBottom: '5px'}}">
						Your mobile registered with us is:
						<strong class="text-uppercase" v-if="donation.member.phone!==null">
							{{
								((typeof donation.member.phoneCode==='undefined' && donation.member.phoneCode!==null) ? donation.member.phoneCode : '+91')
								+donation.member.phone
							}}
						</strong>
						<strong v-else>---</strong>.
						Registered Email is: <strong>{{ donation.member.email }}</strong>
					</div>
					<div :style="{...footerColor, ...footerText, ...textUpperCase}" class="text-uppercase">
						{{donation.institution.bibleVerse}}
					</div>
					
					<div :style="{...footerColor, ...footerText, ...textUpperCase}" class="text-uppercase">
						{{donation.institution.receiptMessage}}
					</div>
					
					<div style="font-size: .7rem; margin-top: 5px; padding-left: 20px; padding-right: 20px; text-align: center !important;">This is a computer generated receipt, signature is not mandatory</div>
				</div>
			</div>
			
			<div :style="footerContent" v-if="!isCustomModel">
				<div :style="{ ...footerText, ...{marginBottom: '5px'}}">
					Your mobile registered with us is:
					<strong>{{ ((typeof donation.member.phoneCode==='undefined' || donation.member.phoneCode===null) ? '+91' : '+'+donation.member.phoneCode)+donation.member.phone }}</strong>.
					Registered Email is: <strong style="text-transform: lowercase !important;">{{ donation.member.email }}</strong>
				</div>
				<div :style="{...footerColor, ...footerText, ...textUpperCase}" class="text-uppercase">
					{{donation.institution.bibleVerse}}
				</div>
				
				<div :style="{...footerColor, ...footerText, ...textUpperCase}" class="text-uppercase">
                    {{donation.institution.receiptMessage}}
				</div>
				
				<div style="font-size: .7rem; margin-top: 5px; padding-left: 20px; padding-right: 20px; text-align: center !important;">This is a computer generated receipt, signature is not mandatory</div>
			</div>
		</div>
		
		
		<div :style="{...headerStyle, ...{position: 'absolute', width: '100%', bottom: '0'}}"
		     v-if="headerInBottom && !isCustomModel">
			<div :style="headerContentStyle.logoContainer"
			     v-if="leftSideLogoShown">
				<img :src="donation.institution.logo"
				     :alt="donation.institution.name+' logo'"
				     v-if="donation.institution.logo!==null"
				     style="max-width: 100%; max-height: 100px; text-align:center;"/>
			</div>
			<div :style="headerContentStyle.institutionHeaderInfoContainer">
				<div style="font-size: 1.2rem; font-weight: bolder;">{{ donation.institution.name }}</div>
				<div :style="{...fontSmall, ...textUpperCase}">
					Address: <span class="text-uppercase">{{ donation.institution.address }}</span>
				</div>
				<div :style="{...fontSmall}">
					Contact Number: +91 {{ donation.institution.receiptPhone }}
				</div>
				<div :style="{...fontSmall}">
					Website: {{donation.institution.website}}
				</div>
			</div>
			
			
			<div :style="{...headerContentStyle.logoContainer, ...{textAlign: 'right'}}"
			     v-if="rightSideLogoShown">
				<img :src="donation.institution.logo"
				     :alt="donation.institution.name+' logo'"
				     v-if="donation.institution.logo!==null"
				     style="max-width: 100%; max-height: 100px; text-align:center;"/>
			</div>
			
			
			<!-- Show for template 2 only-->
			<div :style="headerContentStyle.qrCodeContainer"
			     v-if="qrCodeShown">
				<qrcode-vue :size="140"
				            :render-as="'svg'"
				            v-if="typeof donation.institution.upi!=='undefined' && donation.institution.upi!==null"
				            :value="donation.institution.upi">
				</qrcode-vue>
			</div>
		</div>
	</div>
</template>

<script>
import image1 from "@/dashboard/church/contributions/receipts/bgs/Template 1 - Receipt.png"
import image2 from "@/dashboard/church/contributions/receipts/bgs/Template 2 - Receipt.png"
import image3 from "@/dashboard/church/contributions/receipts/bgs/Template 3 - Receipt.png"
import image4 from "@/dashboard/church/contributions/receipts/bgs/Template 4 - Receipt.png"
import image5 from "@/dashboard/church/contributions/receipts/bgs/Template 5 - Receipt.png"
import {getDateFromTimeStamp, getDropdownLabel, moneyInWords} from "@/utils/AppFx";
import QrcodeVue from "qrcode.vue";
import {PAYMENT_MODES, SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import {CONTRIBUTIONS_BACKGROUND} from "@/dashboard/church/contributions/receipts/bgs/BACKGROUND_64";
export default {
	name: "ContributionReceipt",
	components: {QrcodeVue},
	methods: {
		moneyInWords,
        getDateFromTimeStamp,
		getDropdownLabel,
		loadCustomReceiptTemplate: function(){
			if(typeof this.donation.institution.customReceiptTemplate!=='undefined'
				&& this.donation.institution.customReceiptTemplate!==null) {
				this.$api.get("/files/get/" + this.donation.institution.customReceiptTemplate,
					{responseType: 'blob'})
					.then(response => {
						this.customReceiptImage = URL.createObjectURL(response);
						this.$store.commit("setLoading", false);
					}).catch(ignored => {
					this.$root['showAlert']('warn', "Custom Receipt", "Failed to load custom receipt.");
					this.$store.commit("setLoading", false);
				});
			}
		}
    },
	data(){
		return {
            logo : null,
			receiptSlip : {
				width: "210mm",
				maxWidth: "210mm",
				minWidth: "210mm",
				height: "144.5mm",
				position: "relative",
				minHeight: "144.5mm",
				maxHeight: "144.5mm",
			},
			todayDate: new Date(),
			templates: [
				image1,
				image2,
				image3,
				image4,
				image5
			],
			templatedBackground: [...CONTRIBUTIONS_BACKGROUND],
			fontSmall: {
				fontSize: "0.8rem",
			},
			textUpperCase: {
				textTransform: "uppercase",
			},
			fontBold: {
				fontWeight: "bold"
			},
			contentContainerStyle: {
				style: {
					paddingLeft: "20px",
					paddingRight: "20px",
					color: "#000",
					boxSizing: "border-box",
					position: "relative"
				},
				receiptHeader: {
					fontSize: "1.3rem",
					textAlign: "center",
				},
				contentHeaderRight: {
					position: "absolute",
					fontSize: "0.8rem",
					textAlign: "right",
					right: 0,
					top: 0
				},
				tableStyle: {
					width: "100%",
					border: "1px solid #333",
					borderCollapse: "collapse"
				},
				bold : {
					fontWeight: "bold",
				},
				cellStyle: {
					padding: "0 10px",
					border: "1px solid #333",
					// fontSize: "0.9rem"
				}
			},
			footerText: {
				textAlign: "center",
				fontSize: "0.8rem",
				paddingLeft: '20px',
				paddingRight: '20px'
			},
			// AA3126854
			donation: this.receiptData,
			paymentModes: [...PAYMENT_MODES],
			customReceiptImage: null,
		}
	},
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		receiptContentStyle: function(){
			if(this.isCustomModel) {
				if (this.receiptTemplate === "TEMPLATE_2") {
					return { position: 'absolute', top: "13rem", width: '100%', paddingBottom: "50px"}
				}
				return {paddingTop: "1.3rem"}
			}
			return {}
		},
		receiptTemplate: function(){
			let template = this.donation.institution.receiptTemplate;
			if(typeof this.donation.id!=="undefined"){
				template = (typeof this.donation.institution.customReceiptTemplate!=='undefined'
					&& this.donation.institution.customReceiptTemplate!==null)
					? this.donation.institution.customModel
					: this.donation.institution.receiptTemplate
			}
			return template;
		},
		headerInBottom: function(){
			return this.receiptTemplate==="TEMPLATE_1"
		},
		footerContent: function(){
			if(this.headerInBottom) return {};
			return {
				position: "absolute",
				bottom: "15px",
				left: "0",
				width: "100%"
			}
		},
		leftSideLogoShown: function(){
			return !["TEMPLATE_3"].includes(this.donation.institution.receiptTemplate)
		},
		rightSideLogoShown: function(){
			return ["TEMPLATE_3"].includes(this.donation.institution.receiptTemplate)
		},
		qrCodeShown: function(){
			return ["TEMPLATE_2", "TEMPLATE_1"].includes(this.donation.institution.receiptTemplate)
		},
		headerContentStyle: function(){
			let institutionHeaderContainer = "center",
				width = "50%",
				color = "#fff",
				paddingRight = 0
			if(['TEMPLATE_3', "TEMPLATE_4", "TEMPLATE_5"].includes(this.donation.institution.receiptTemplate)){
				if(this.donation.institution.receiptTemplate==="TEMPLATE_3"){
					institutionHeaderContainer = "left";
				} else{
					institutionHeaderContainer = "right";
					if(this.donation.institution.receiptTemplate==="TEMPLATE_5"){
						paddingRight = "10%"
						color = "#000";
					}
				}
				width = "75%";
			}
			return {
				institutionHeaderInfoContainer: {
					width: width,
					textAlign: institutionHeaderContainer,
					color: color,
					paddingRight: paddingRight
				},
				logoContainer: {
					width: "25%"
				},
				qrCodeContainer: {
					width: "25%",
					textAlign: "right"
				},
			};
		},
		totalAmount: function(){
			let amount = 0;
			this.donation.donations.forEach(donation=>{amount += donation.amount;});
			return amount;
		},
		backgroundImageStyle: function(){
			return {
				width: "210mm",
				height: "144.5mm",
				position: "absolute",
				zIndex: -1,
				top: "0",
				left: "0",
				backgroundImage: `url(${this.backgroundImage})`,
				backgroundSize: 'cover',
				backgroundOrigin: 'border-box',
				backgroundPosition: 'center'
			}
		},
		isCustomModel: function(){
			return (this.donation.institution.customModel!=='undefined'
				&& this.donation.institution.customModel!==null);
		},
		backgroundImage: function(){
			if(this.donation.institution.customModel!=='undefined'
				&& this.donation.institution.customModel!==null) {
				return ["TEMPLATE_1", "TEMPLATE_2"].includes(this.donation.institution.customModel)
					? this.customReceiptImage
					: this.donation.institution.customModel;
			}
			switch (this.donation.institution.receiptTemplate) {
				case "TEMPLATE_1":
					return this.templatedBackground[0];
				
				case "TEMPLATE_2":
					return this.templatedBackground[1];
				
				case "TEMPLATE_3":
					return this.templatedBackground[2];
				
				case "TEMPLATE_4":
					return this.templatedBackground[3];
				
				case "TEMPLATE_5":
					return this.templatedBackground[4];
			}
			return this.templatedBackground[1];
		},
		headerStyle: function(){
			let height = "37%";
			switch(this.receiptTemplate){
				case "TEMPLATE_1":
					height = "35%";
					break;
				
				case "TEMPLATE_2":
					height = "37%";
					break;
				
				case "TEMPLATE_3":
					height = "31.7%";
					break;
				
				case "TEMPLATE_4":
					height = "32%";
					break;
				
				case "TEMPLATE_5":
					height = "37%";
					break;
			}
			return {
				padding: "10px",
				display: "flex",
				flexDirection: "row",
				justifyContent: 'space-between',
				height: height,
				paddingTop: '20px'
				// backgroundColor: "rgb(111,200,28)"
			}
		},
		footerColor: function(){
			return {
				color: ["TEMPLATE_4", "TEMPLATE_5"].includes(this.donation.institution.receiptTemplate)
					? "#fff"
					: "#000"
			}
		},
		contentOnTop: function(){
			return {
				paddingTop: (this.donation.institution.receiptTemplate==="TEMPLATE_1") ? "10px" : 0
			}
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
  
	},
	props: {
		receiptData: {
			type: [Object, null],
			required: true,
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
	},
	watch: {
		receiptData: function(newValue){
			this.donation = {...newValue};
			this.loadCustomReceiptTemplate();
		}
	},
}
</script>

<style scoped lang="scss">
	.page-container{
		width: 210mm;
		max-width: 210mm;
		min-width: 210mm;
		height: 144.5mm;
		min-height: 144.5mm;
		max-height: 144.5mm;
		
		position: fixed;
		top: 0;
		left: 0;
		background-color: yellow;
		z-index: 1099;
	}
</style>