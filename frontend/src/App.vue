<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->
<template>
	<sidenav :custom_class="color"
	         :class="[isRTL ? 'fixed-end' : 'fixed-start']"
	         v-if="showSidenav"/>
	<main class="main-content position-relative max-height-vh-100 h-100 overflow-x-hidden">
		<!-- nav -->
		<navbar :class="[isNavFixed ? navbarFixed : '', isAbsolute ? absolute : '']"
		        :color="isAbsolute ? 'text-white opacity-8' : ''"
		        :minNav="navbarMinimize"
		        v-if="showNavbar"/>
		
		<router-view v-slot="{ Component }">
			<component :is="Component" />
		</router-view>
		
		<Toast>
			<template #message="slotProps">
				<span class="p-toast-message-icon pi" :class="getToastIcon(slotProps.message)"></span>
				<div class="p-toast-message-text">
					<span class="p-toast-summary">{{slotProps.message.summary}}</span>
					<div class="p-toast-detail"
					     v-if="typeof(slotProps.message.detail)==='string'">
						{{slotProps.message.detail}}
					</div>
					<div class="p-toast-detail" v-if="typeof(slotProps.message.detail)==='object'">
						<ul class="mx-0">
							<li v-for="(singleDetail, index) in slotProps.message.detail" :key="index">
								{{ singleDetail }}
							</li>
						</ul>
					</div>
				</div>
			</template>
		</Toast>
		
		<ConfirmDialog group="templating">
			<template #message="slotProps">
				<div class="flex flex-col items-center w-full gap-4 border-b border-surface-200 dark:border-surface-700">
					<p>{{ slotProps.message.message }}</p>
				</div>
			</template>
		</ConfirmDialog>
		
		<AppLoader :class="$store.getters.isLoading ? 'loader-shown' : 'loader-hidden'"/>
		
		<app-footer v-show="showFooter" />
		
		<div class="bottom-buttons"
		     v-if="$store.getters.getFamily!==null || $store.getters.getInstitution!==null">
			<div class="position-relative d-flex align-items-center buttons-container">
				<div class="institution-reset-button fw-bolder"
				     v-if="$store.getters.getFamily!==null"
				     title="Unset Family"
				     @click="unsetFamily">
					Unset Family
				</div>
				
				<div class="institution-reset-button fw-bolder"
				     v-if="$store.getters.getInstitution!==null"
				     :title="resetInstitutionButton"
				     @click="resetInstitution">
					{{ resetInstitutionButton }}
				</div>
			</div>
		</div>
	</main>
</template>

<script>
import Toast from 'primevue/toast';
import Sidenav from "./examples/Sidenav";
import Navbar from "@/examples/Navbars/Navbar.vue";
import AppFooter from "@/examples/Footer.vue";
import { mapMutations, mapState } from "vuex";
import {getToastIcon} from "@/components/toast/toastFx";
import ConfirmDialog from "primevue/confirmdialog";
import AppLoader from "@/components/loader/AppLoader.vue";
import {
	SUPER_ADMINISTRATORS_ROLES,
	CHURCH_MAIN_BRANCH_ADMINISTRATORS, FAMILY_ROLES
} from "@/dashboard/members/members";
import {getCountries} from "@/dashboard/utils/countries";
import {INDIA_STATES} from "@/dashboard/organization/Organization";
import dnoteLogo from "@/assets/img/logos/favicon.png";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import LocalDatabase from "@/database/IndexedDB";
import {isEmpty} from "@/utils/AppStringUtils";

export default {
	name: "App",
	components: {
		AppLoader,
		Sidenav,
		Navbar,
		Toast,
		ConfirmDialog,
		AppFooter,
	},
	data(){
		return{
			countries: getCountries(),
			indianState : [...INDIA_STATES],
			familyRoles : [...FAMILY_ROLES],
			institutionLogos: {},
			loggedOutMessageShown: false,
			indexedDb: new LocalDatabase()
		}
	},
	methods: {
		...mapMutations(["toggleConfigurator", "navbarMinimize"]),
		getToastIcon,
		preloadBgData: function(){
			return new Promise(resolve=>{
				const DATA = this.$root['addInstitutionId']({});
				this.$api.post("/certificates/get-backgrounds", DATA).then(response=>{
					resolve(response);
				}).catch(ignored=>{
					resolve(null);
				});
			});
		},
		loadBgs: function(){
			this.preloadBgData().then(response=>{
				if(response!==null){
					// We have something there
					const certificatesBgsMap = response.object,
						keys = Object.keys(certificatesBgsMap);
					keys.forEach(key=>{
						let value = certificatesBgsMap[key];
						if(typeof value!=='undefined' && value!==null) this.applyCertificateBackground(key, value.fileName);
					});
				}
			})
		},
		applyCertificateBackground: function(key, certificateBackgroundFileName){
			if(!isEmpty(certificateBackgroundFileName)){
				this.$api.get("/files/get/"+certificateBackgroundFileName,
					{responseType : 'blob'}).then(blobResponse=>{
					this.$store.commit("setLoading", true);
					this.indexedDb.update("blobs", key, blobResponse).then(saveResult=>{
						if(saveResult) {
							let certificateURL = URL.createObjectURL(blobResponse);
							
							let certificatesBackgrounds = this.$store.getters.getCertificatesBgs;
							if (certificatesBackgrounds === null) certificatesBackgrounds = {};
							certificatesBackgrounds[key] = certificateURL;
							this.$store.commit("setCertificatesBgs", certificatesBackgrounds);
						}
						this.$store.commit("setLoading", false);
					});
				}).catch(ignored=>{});
			}
		},
		loadSignature: function(){
			let institution = this.institution;
			if(this.institution!==null) {
				if (typeof institution.priestSignature !== 'undefined'
					&& institution.priestSignature !== null) {
					this.$api.get("/files/get/" + institution.priestSignature,
						{responseType: "blob"})
						.then(response => {
							this.$store.commit("setLoading", true);
							this.indexedDb.update("blobs", institution.id, response).then(saveResult=>{
								let signatureUrl = URL.createObjectURL(response);
								this.$store.commit("setSignature", signatureUrl);
								this.$store.commit("setLoading", false);
							});
						}).catch(ignored => {});
				}
			}
		},
		displayFamilyRole: function(familyRole){
			let role = null;
			 switch (familyRole){
				 case 'FATHER' :
					 role =  'Father';
					break;
				 case 'MOTHER' :
					 role =  'Mother';
				     break;
					 
				 case 'HUSBAND' :
					 role =  'Husband';
					break;
				 case 'WIFE' :
					 role =  'Wife';
				     break;
				 case "GRAND_FATHER":
					 role = "Grand Father";
				     break;
				 case "FATHER_IN_LAW":
					 role = "Father In Law";
				     break;
				 case "UNCLE":
					 role = "Uncle";
				     break;
				 
				 
				 case "NEPHEW":
					 role = "Nephew";
					 break;
				 case "GRAND_MOTHER":
					 role = "Grand Mother";
					 break;
				 case "MOTHER_IN_LAW":
					 role = "Mother In Law";
					 break;
				 case "AUNT":
					 role = "Aunt";
				     break;
				 case "NIECE":
					 role = "Niece";
				     break;
					
				 case "DAUGHTER":
					 role = "Daughter";
					 break;
				 case "SISTER":
					 role = "Sister"
					 break;
				 case "DAUGHTER_IN_LAW":
					 role = "Daughter In Law";
					 break;
				 case "SON":
					 role = "Son";
					 break;
			     case "SON_IN_LAW":
					 role = "Son In Law";
					 break;
				 case "COUSIN":
					 role = "Cousin";
					 break;
				 default :
					  case "BROTHER": // <--- ADD THIS CASE
                      role = "Brother"; // <--- ADD THIS LINE
                      break; // <--- ADD THIS LINE
			 }
			 return role;
		},
		resetInstitution: function(){
			if(this.$store.getters.getInstitution!=null){
				this.$store.commit("handleInstitutionChange");
			}
		},
		getIndianState: function(state){
			let stateLabel = null;
			 this.indianState.filter(singleState=>{
				if(singleState.value===state){
					stateLabel =  singleState.label;
				}
			})
			return stateLabel;
		},
		verifyIsLoggedIn: function(routePath){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(typeof routePath.meta!=="undefined"){
				let meta = routePath.meta;
				if(meta.dashboard){
					if(loggedInUser===null){
						this.cleanSession();
					} else {
						let userTypes = meta.userTypes;
						if(!userTypes.includes(loggedInUser.userType)) this.$router.push("/dashboard");
					}
				} else {
					if(routePath.fullPath==="/sign-in" && loggedInUser!==null) this.$router.push("/dashboard");
				}
			}
		},
		handleInstitutionTypeChange: function(){
			this.$store.commit("setInstitution", null);
			this.$store.commit("setFamily", null);
			this.$store.commit("setTmpInstitutionMember", null);
			this.$store.commit("setInstitutionMembers", []);
		},
		getInstitution(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser!==null) {
				if (SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)) {
					return this.$store.getters.getInstitution;
				}  else {
					return loggedInUser.institution;
				}
			}
			return null;
		},
		addInstitutionId: function(data, isChurchId=false, force=false){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser!==null) {
				if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)) {
					if (this.$store.getters.getInstitution !== null) {
						data = (isChurchId)
							? {...data, churchId: this.$store.getters.getInstitution.id}
							: {...data, institutionId: this.$store.getters.getInstitution.id};
					}
				} else {
					let institutionId = loggedInUser.institution.id;
					if(CHURCH_MAIN_BRANCH_ADMINISTRATORS.includes(loggedInUser.userType)){
						if (this.$store.getters.getInstitution !== null) {
							institutionId = this.$store.getters.getInstitution.id;
						}
					}
					if(institutionId!==loggedInUser.institution.id || force) {
						data = (isChurchId)
							? {...data, churchId: institutionId}
							: {...data, institutionId: institutionId};
					}
				}
			}
			return data;
		},
		login: function(userSession=null){
			if(userSession!==null) {
				this.$store.commit("login", userSession);
				this.$api.defaults.headers.common['authorization'] = "Bearer "+this.$store.getters.getBearerToken;
				this.$router.push('/dashboard'); // navigate to home
				this.loadSignature();
			}
		},
		cleanSession(){
			this.$store.commit("logout");
			localStorage.clear();
			this.indexedDb.clean();
			this.$router.push({path: "/sign-in", replace: true});
		},
		logout: function(acknowledgeServer = true){
			if(acknowledgeServer) {
				if(this.$store.getters.getLoggedInUser!==null){
					this.$api.get("/user/logout").then(response => {
						const messageType = (response.successful) ? "success" : "error";
						this.$toast.add({
							severity: messageType, summary: "Logout", detail: response.message,
							life: 5000
						});
						this.cleanSession();
					}).catch(error => {
						this.handleApiError(error, "Logout");
						this.cleanSession();
					});
				} else {
					this.cleanSession();
				}
			} else {
				this.cleanSession();
			}
		},
		showAlert: function(messageType, title, msg){
			this.$toast.add({
				severity: messageType,
				summary: title,
				detail:msg,
				life: 5000});
		},
		getUserType: function(){
			if(this.$store.getters.getLoggedInUser===null) return null;
			return this.$store.getters.getLoggedInUser.userType;
		},
		handleApiError: function (error, title, returnValue= false, isLogin=false){
			let msg = "Oups, Something Went Wrong";
			if(error.message==="timeout"){
				msg = "Server timeout.";
			} else {
				const ERROR = (typeof error!=="undefined" && typeof error.response !== "undefined")
					? error.response
					: error.request;
				
				if(typeof(ERROR)!=="undefined" && typeof(ERROR.status)!=="undefined") {
					switch (ERROR.status) {
						case 500:
						case 417:
						case 406:
							msg = error.response.data.message;
							break;
						
						case 503:
							msg = "The application is undergoing a maintenance. Please wait while we work on it.";
							this.logout(false);
							break;
						
						case 401:
							if(!this.loggedOutMessageShown) {
								msg = null;
								if (isLogin) msg = "Not logged in.";
								this.logout(true);
								this.loggedOutMessageShown = true;
								setTimeout(()=>{
									this.loggedOutMessageShown = false;
								}, 2000);
							}
							break;
						
						case 404:
							msg = "Resource not found!.";
							break;
						
						case 400:
							if(typeof error.response.data.errors!=="undefined") {
								msg = error.response.data.errors;
							}
							break;
						
						case 403:
							msg = error.response.data.message;
							this.logout(true);
							break;
						
						case 405:
							msg = "Unsupported method";
							break;
						
						case 0:
							msg = "The connection the server failed. Please try again later.";
							this.holderShown = true;
							break;
					}
				}
			}
			if(msg!=null) this.showAlert("error", title, msg);
			this.showingLoader = false;
			if(returnValue) return msg;
		},
		isInstitutionSet: function(){
			if(this.institution===null && SUPER_ADMINISTRATORS_ROLES.includes(this.userType)){
				this.$root['showAlert']('warn', 'Warning', "No institution Specified");
				return false;
			}
			return true;
		},
		handleStorageChange: function(event){
			if(event.key===null && localStorage.length===0){
				this.logout(true);
			}
		},
		getImageUrl(imageName){
			return APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+imageName;
		},
		loadInstitutionLogo : function(){
			let institution = this.institution;
			if(institution===null) return dnoteLogo;
			if(institution.logo!==null) return APP_CONFIG.BACKEND_SERVER+"/files/logo/"+institution.logo;
			return dnoteLogo;
		},
		downloadReceipt: function(containerId) {
			let disp_setting="toolbar=yes,location=no,";
			disp_setting+="directories=yes,menubar=yes,";
			disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
			document.getElementById(containerId).style.display = "block";
			let content_value = document.getElementById(containerId).outerHTML;
			document.getElementById(containerId).style.display = "none";
			this.$store.commit("setLoading", false);
			let docprint= window.open("","",disp_setting);
			docprint.document.open();
			docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
			docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
			docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
			docprint.document.write('<head><title>My Title</title>');
			docprint.document.write('</head><body onLoad="self.print()" style="margin:0;padding:0;">');
			docprint.document.write('<style type="text/css" media="all">');
			let css = "" +
				"@media all {" +
					"@page{" +
						"size: A5;" +
						"margin:0 0 !important;" +
						"padding: 0 0 !important;" +
					"}" +
					// Body
					"body{" +
						"margin: 0 0;" +
						"padding: 0 0;" +
					"}" +
					"*{box-sizing:border-box;}" +
					"font-family:verdana,Arial;color:#000;" +
					"font-family:arial Verdana, Geneva, sans-serif;" +
				"}"
			docprint.document.write(css);
			docprint.document.write('}</style>');
			docprint.document.write(content_value);
			docprint.document.write('</body></html>');
			docprint.document.close();
			docprint.focus();
		},
		unsetFamily: function(){
			this.$store.commit("setFamily", null);
		},
	},
	computed: {
		resetInstitutionButton: function(){
			if(CHURCH_MAIN_BRANCH_ADMINISTRATORS.includes(this.userType)){
				return "Unset Church";
			} else {
				// only for super Administrators
				if(this.institution!==null){
					return this.institution.institutionType==="GENERAL" ? "Unset General Member" : " Unset Church";
				}
				return null;
			}
		},
		userType: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return null;
			return loggedInUser.userType;
		},
		institution: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser==null) return null;
			if(SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)){
				return this.$store.getters.getInstitution;
			}
			return loggedInUser.institution;
		},
		...mapState([
			"isRTL",
			"color",
			"isAbsolute",
			"isNavFixed",
			"navbarFixed",
			"absolute",
			"showSidenav",
			"showNavbar",
			"showFooter",
			"showConfig",
			"hideConfigButton",
		]),
	},
	beforeMount() {
		this.$store.state.isTransparent = "bg-transparent";
		const sidenav = document.getElementsByClassName("g-sidenav-show")[0];
		if (window.innerWidth > 1200) {
			sidenav.classList.add("g-sidenav-pinned");
		}
		window.addEventListener("storage", this.handleStorageChange);
		this.$store.commit("initLoading");
		this.loadSignature();
		this.loadBgs();
	},
	beforeUnmount(){
		window.removeEventListener("storage", this.handleStorageChange);
	},
	watch: {
		'$route': function(newValue){
			this.verifyIsLoggedIn(newValue);
		}
	}
};
</script>

<style>
.bottom-buttons{
	position: fixed;
	bottom: 20px;
	right: 20px;
	background-color: #000;
	border: 1px solid white;
	border-radius: 50px;
	min-width: 100px;
	padding: 10px 30px;
}
.buttons-container{
	gap: 15px;
}
.institution-reset-button{
	background-color: #fff;
	padding: 5px 10px;
	right: 0;
	border-radius: 5px;
	border: 0 !important;
	cursor: pointer !important;
	color: #000;
}


@keyframes bounce-in {
	0% {
		transform: scale(0.5);
		opacity: 0;
	}
	60% {
		transform: scale(1.25);
		opacity: 1;
	}
	80% {
		transform: scale(1);
		opacity: 1;
	}
	90% {
		transform: scale(1.1);
		opacity: 1;
	}
	100% {
		transform: scale(1);
	}
}


/* Custom Scrollbar for WebKit browsers (Chrome, Safari, Edge) */
::-webkit-scrollbar {
	width: 14px;               /* Width of the scrollbar */
}

::-webkit-scrollbar-track {
	background: #212123;       /* Background of the track */
	border-radius: 0;       /* Rounded edges */
}

::-webkit-scrollbar-thumb {
	background-color: #212123;    /* Scrollbar color */
	border-radius: 0;       /* Rounded edges */
	border: 3px solid #fff; /* Adds space around the scrollbar */
}

::-webkit-scrollbar-thumb:hover {
	background-color: #212123;   /* Scrollbar color on hover */
}

body {
	-ms-overflow-style: -ms-autohiding-scrollbar;
}
</style>
