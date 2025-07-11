<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div style="width: 100%">
		<div class="position-relative page bg-white"
		     style="display: flex;
					justify-content: space-between;
					position: relative;
					flex-wrap: wrap;
					padding: 0 8mm;
					box-sizing: border-box;
					width: 100%;"
		     :id="pageId">
			<div v-for="(card, index) in cards"
			     :key="index"
			     :style="index%22===0 ? headerStyle : cardStyle">
				<div v-if="index%22===0"
				     :style="getCardStyle(index)">
					<div :style="headerContainerStyle">
						<div style="width: 30%;">
							<div>{{username}}</div>
							<div v-for="(singleLine, index) in givenHeaders"
							     :key="'header_'+index">
								{{ singleLine.label }} :
								<span style="font-weight: bolder;">
									{{ singleLine.value }}
								</span>
							</div>
						</div>
						
						<div style="flex-grow: 1; text-align: center; text-transform: uppercase; font-weight: bolder;">
							<div>
								{{ institution===null ? '---' : institution.name }}
							</div>
							<div v-if="amountHeader!==null">
								{{ amountHeader.label + ' : '+ amountHeader.value }}
							</div>
						</div>
						
						<div style="width: 30%; text-transform: uppercase; font-weight: bolder; text-align:right;">
							<div>
								Page {{  parseInt(Math.ceil(index/22)+1 ) }} out of
								{{ parseInt(Math.ceil(cards.length/22)) }}
							</div>
							<div>{{ getDateFromTimeStamp(nowDateTime) }}</div>
						</div>
					</div>
				</div>
				
				
				
				<div v-else
				     style="height: 100%; font-size: 0.8rem; text-transform: uppercase; line-height: 1.2; overflow: hidden !important;">
					<div :style="cardHeader">{{ card.code }}</div>
					<div style="line-height: 1.1; color: #000; font-weight: bold;">
						{{ card.title + " "+card.firstName +" "+card.lastName }}
					</div>
					
					<div style="font-size: 0.7rem;">
						<div>
							{{ card.addressLine1}}
						</div>
						
						<div :style="addressContainer">
							{{ card.addressLine2+', '+card.addressLine3+', '+card.district+', '+card.state }}
						</div>
						
						<div :style="addressContainer">
							PIN: {{ card.pincode }}
						</div>
						
						<div>
							Phone: <strong>{{ card.phone }}</strong>
						</div>
					</div>
				</div>
				
				<div :style="qrCodeContainer" v-if="index%22!==0">
					<qrcode-vue :size="60"
					            :render-as="'svg'"
					            :value="card.addressLine1+', '+card.addressLine2+', '+card.addressLine3+', '+card.district+', '+card.pincode+', '+card.state">
					</qrcode-vue>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import QrcodeVue from "qrcode.vue";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import {getDateFromTimeStamp} from "@/utils/AppFx";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "AddressPrintingPage",
	components: {QrcodeVue},
	data(){
		return {
			pageId: generateRandomString(8),
			headerContainerStyle: {
				padding: "5px",
				display: "flex",
				fontSize: "0.7rem",
				width: "100%",
				flexWrap: "nowrap",
			},
			headerStyle: {
				width: "100%",
				height: 15.5+"mm",
				display: "flex",
				alignItems: "center",
				position: "relative",
				boxSizing: "border-box",
				justifyContent: "space-between",
				fontSize: 10+"px",
				backgroundColor: 'white',
				borderBottom: "1px solid grey"
			},
			cardStyle :{
				position: "relative",
				width: 6.35+"cm",
				backgroundColor: "white",
				height: 3.8+"cm",
				padding: 10+"px",
				boxSizing: "border-box",
				marginBottom: 0,
				overflow:"hidden",
			},
			cardHeader :{
				fontWeight: "bold",
				textTransform:"uppercase",
				fontSize: "0.7rem"
			},
			qrCodeContainer: {
				position: "absolute",
				bottom: "2mm",
				right: "2mm",
				width: "18mm",
				height: "18mm"
			},
			addressContainer: {
				width: "40mm",
			},
		}
	},
	methods: {
		getCardStyle: function(index){
			if(index%22!==0) return null;
			return {
				width: "100%",
			}
		},
		getDateFromTimeStamp,
	},
	props: {
		content: {
			required: true,
			type: Array,
			default(){
				return [];
			}
		},
		headers: {
			required: true,
			type: Array,
			default(){
				return []
			}
		}
	},
	computed: {
		nowDateTime: function(){
			return new Date()
		},
		cards: function(){
			let theCards = this.content.filter(card=>{
				return card!==null;
			}), i = 0, theCardsLength = theCards.length , newCards = [];
			for(; i<theCardsLength; i++){
				if(i%21===0) newCards.push({});
				newCards.push(theCards[i]);
			}
			return newCards;
		},
		institution: function(){
			let loggedInUSer = this.$store.getters.getLoggedInUser;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUSer.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUSer.institution;
		},
		givenHeaders: function(){
			return this.headers.filter(header=>header.label.toLowerCase()!=="amount");
		},
		amountHeader: function(){
			let amountHeader = this.headers.find(header=>header.label.toLowerCase()==="amount");
			if(typeof amountHeader==='undefined') return null;
			return amountHeader;
		},
		username: function(){
			if(this.$store.getters.getLoggedInUser!==null){
				return this.$store.getters.getLoggedInUser.username;
			}
			return null;
		}
	}
}
</script>

<style lang="scss">
</style>