+<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="w-auto collapse navbar-collapse pt-2 pb-4"
	     :style="styleClass"
	     id="sidenav-collapse-main">
		<ul class="navbar-nav">
			<li class="nav-item"
			    v-for="(item, index) in navLinks"
			    :class="item.title ? 'mt-4' : ''"
			    :key="index">
				<h6 class="ps-4 text-uppercase font-weight-bolder text-white ms-2"
				    v-if="item.title">
					{{ item.label }}
				</h6>
				
				<div v-else>
					<sidenav-collapse v-if="typeof(item.command)==='undefined'"
					                  :aria-controls="''"
					                  v-bind:collapse="false"
					                  :collapseRef="item.url"
					                  :navText="item.label">
						<template v-slot:icon>
							<i class="material-icons-round opacity-10 fs-3">
								{{ item.icon }}
							</i>
						</template>
					</sidenav-collapse>
					
					<sidenav-collapse v-else
					                  :aria-controls="''"
					                  v-bind:collapse="false"
					                  :collapseRef="item.url"
					                  :navText="item.label"
					                  :command="(typeof item.command==='undefined') ? null : item.command"
					                  @command="handleCommand">
						<template v-slot:icon>
							<i class="material-icons-round opacity-10 fs-3">
								{{ item.icon }}
							</i>
						</template>
					</sidenav-collapse>
				</div>
			</li>
			
			<!--
			<li class="nav-item">
				<div>
					<sidenav-collapse :aria-controls="''"
					                  v-bind:collapse="false"
					                  collapseRef="/church/signatures"
					                  :navText="'Signatures'"
					                  :command="null">
						<template v-slot:icon>
							<i class="material-icons-round opacity-10 fs-3">cake</i>
						</template>
					</sidenav-collapse>
				</div>
			</li>-->
		</ul>
	</div>
</template>

<script>
import SidenavCollapse from "./SidenavCollapse.vue";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

export default {
	name: "SidenavList",
	props: {
		cardBg: String,
		height: {
			type: Number,
			required: true,
			default(){
				return 0;
			}
		}
	},
	computed: {
		listHeight: function(){
			return this.height;
		},
		styleClass: function(){
			return {
				maxHeight: this.listHeight+"px",
				height: this.listHeight+"px",
			}
		},
		navLinks: function(){
			let loggedInUser = this.$store.getters.getLoggedInUser;
			if(loggedInUser===null) return [];
			let routes = this.$router.getRoutes();
			return this.links.filter(singleNavLink=>{
				if(singleNavLink.title) return this.isUserAllowed(singleNavLink);
				
				if(singleNavLink.url==="/sign-in") return true;
				let institution = null;
				if(["CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"].includes(loggedInUser.userType)){
					if(typeof loggedInUser.institution!=="undefined"){
						institution = loggedInUser.institution;
						if(loggedInUser.institution.maxChurchBranches!==null && loggedInUser.institution.maxChurchBranches===0){
							if(["/church/create", "/church/list"].includes(singleNavLink.url)) return false;
						}
					}
					if(singleNavLink.command==="createNewOrganization") singleNavLink.label = "Create Branch";
				}
				let foundRoute = routes.find(singleRoute=>{
					let originalPath = singleRoute.path.split("/:")[0],
						baseUrl = singleNavLink.url.split("/:")[0];
					return originalPath===baseUrl;
				});
				// raba ko iyi institution ibifise
				if(typeof loggedInUser.institution!=="undefined" && loggedInUser.institution!==null){
					let institution = {...loggedInUser.institution};
					if(typeof foundRoute!=='undefined'){
						if(typeof foundRoute.meta.feature!=='undefined') {
							try {
								let allowedFeatures = [...institution.allowedFeatures],
									institutionType = foundRoute.meta.institutionType,
									allowedUserTypes = foundRoute.meta.userTypes;
								
								return allowedFeatures.includes(foundRoute.meta.feature)
									&& (institution.institutionType===institutionType)
									&& allowedUserTypes.includes(loggedInUser.userType)
									&& this.isUserAllowed(singleNavLink);
							} catch (ignored){
								//
							}
						}
					}
				}
				if(typeof(foundRoute)!=="undefined") {
					return foundRoute.meta.userTypes.includes(loggedInUser.userType)
						&& this.isUserAllowed(singleNavLink);
				}
				return false;
			});
		}
	},
	data() {
		return {
			title: APP_CONFIG.APP_NAME,
			controls: "dashboardsExamples",
			isActive: "active",
			links: [
				{ url: "/dashboard", label: "Home", collapse: false, icon: "dashboard", title: false},
				{ url: "/health", label: "Health", collapse: false, icon: "health_and_safety", title: false,
					userTypes: [
						 "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR"
					]},
				// { url: "/notifications", label: "Notifications", collapse: false, icon: "notifications", title: false},
				{ label: "General Organization", title: true,
					userTypes: [
						"SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR"
					]
				},
				
				/*
				
				 */
				{ url: "/organizations/list", label: "List", collapse: false, icon: "format_list_numbered", title: false},
				{ url: "/organizations/create-new", label: "Create", command: "createNewOrganization", collapse: false, icon: "add_business", title: false},
				{ url: "/organizations/partners-dashboard/:0", label: "Partner Board", collapse: false, icon: "person_add", title: false},
				{ url: "/organizations/receipt-generation", label: "Receipt Generator", collapse: false, icon: "receipt", title: false},
				{ url: "/organizations/families-board/:0", label: "Families Board", collapse: false, icon: "diversity_3", title: false},
				{ url: "/organizations/report-generation", label: "Report Generator", collapse: false, icon: "report", title: false},
				{ url: "/organizations/address-printing", label: "Address Printing", collapse: false, icon: "pin_drop", title: false},
				{ url: "/organizations/communication", label: "Communication", collapse: false, icon: "campaign", title: false},
				{ url: "/organizations/birthdays/:0", label: "Occasions", collapse: false, icon: "redeem", title: false},
				{ url: "/organizations/certificate-data", label: "Certificates", collapse: false, icon: "card_membership", title: false},
				
				
				{ label: "Configurations", title: true,
					userTypes: [
						"SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR"
					]
				},
				{ url: "/title", label: "Title", collapse: false, icon: "title", title: false},
				{ url: "/categories", label: "Categories", collapse: false, icon: "drag_indicator", title: false},
                { url: "/configuration/subscription-plan-board/:0", label: "Subscription Plan", collapse: false, icon: "subscriptions", title: false},
                { url: "/configuration/receipt", label: "Receipt", collapse: false, icon: "title", title: false},
                
                
				
				
				{ label: "Church", title: true,
					userTypes: [
						"EXTRA_SUPER_ADMIN",
                        "SUPER_ADMINISTRATOR",
                        "EXTRA_SUPER_ADMINISTRATOR",
                        "SUPER_ASSISTANT_ADMINISTRATOR"
					]
				},
				// /church/import-partners
				{ url: "/church/members-dashboard/:0", label: "Members Board", collapse: false, icon: "diversity_3", title: false},
				{ url: "/church/families-board/:0", label: "Families Board", collapse: false, icon: "diversity_3", title: false},
				{ url: "/church/subscription/:0", label: "Offering Board", collapse: false, icon: "subscriptions", title: false},
				{ url: "/church/receipt-generation", label: "Receipt Generator", collapse: false, icon: "receipt", title: false},
				{ url: "/church/report-generation", label: "Report Generator", collapse: false, icon: "report", title: false},
				{ url: "/church/birthdays/:0", label: "Occasions", collapse: false, icon: "redeem", title: false},
				{ url: "/church/group/:0", label: "Groups", collapse: false, icon: "group", title: false},
				{ url: "/church/address-printing", label: "Address Printing", collapse: false, icon: "pin_drop", title: false},
				{ url: "/church/communication", label: "Communication", collapse: false, icon: "campaign", title: false},
				{ url: "/church/certificate-data", label: "Certificates", collapse: false, icon: "card_membership", title: false},
				{ url: "/church/event-board/:0", label: "Event Board", collapse: false, icon: "event", title: false},
				{ url: "/church/create", label: "Create Church", command: "createNewOrganization", collapse: false, icon: "add_business", title: false, userTypes: [
						"EXTRA_SUPER_ADMIN",
						"SUPER_ADMINISTRATOR",
						"EXTRA_SUPER_ADMINISTRATOR",
					]},
				{ url: "/church/mailer-settings", label: "Mailer Settings", collapse: false, icon: "mail", title: false},
    
				
				{ label: "Account", title: true },
				{ url: "/super-admins", label: "Super Admins", collapse: false, icon: "person", title: false},
				
				
				// Organizations
				{ url: "/organizations/assistant-admin-dashboard/:0", label: "Ass. Admins", collapse: false, icon: "add_moderator", title: false},
				{ url: "/organizations/configuration/receipt", label: "Receipt Settings", collapse: false, icon: "settings_input_component", title: false},
				{ url: "/organizations/mailer-settings", label: "Mailer Settings", collapse: false, icon: "mail", title: false},
				{ url: "/organizations/categories", label: "Org. Categories", collapse: false, icon: "category", title: false},
				{ url: "/organizations/communication/template/:0", label: "Template", collapse: false, icon: "campaign", title: false},
				{ url: "/organizations/group/:0", label: "Groups", collapse: false, icon: "group", title: false},
				{ url: "/organizations/import-data", label: "Import Data", collapse: false, icon: "cloud_upload", title: false},
				{ url: "/organizations/scheduler", label: "Org. Backup", collapse: false, icon: "cloud_upload", title: false},
				{ url: "/church/event-template", label: "Event Template", collapse: false, icon: "event", title: false},
				{ url: "/church/categories", label: "Church Types", collapse: false, icon: "category", title: false},
				{ url: "/church/scheduler", label: "Org. Backup", collapse: false, icon: "cloud_upload", title: false},
				
				
				// Churches
				{ url: "/church/list", label: "List Branches", collapse: false, icon: "list", title: false, userTypes: [
						"EXTRA_SUPER_ADMIN",
						"SUPER_ADMINISTRATOR",
						"SUPER_ASSISTANT_ADMINISTRATOR",
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/configuration/receipt", label: "Receipt Settings", collapse: false, icon: "settings_input_component", title: false, userTypes: [
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/create", label: "Create Branch", command: "createNewOrganization", collapse: false, icon: "add_business", title: false, userTypes: [
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/import-data", label: "Import Data", collapse: false, icon: "cloud_upload", title: false, userTypes: [
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/assistant-admin-dashboard/:0", label: "Ass. Admins", collapse: false, icon: "add_moderator", title: false, userTypes: [
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/communication/template/:0", label: "Template", collapse: false, icon: "campaign", title: false, userTypes: [
						"SUPER_ADMINISTRATOR",
						"SUPER_ASSISTANT_ADMINISTRATOR",
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
						"CHURCH_BRANCH_ADMINISTRATOR",
						"CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
					]},
				{ url: "/church/signatures", label: "Signatures", collapse: false, icon: "campaign", title: false, userTypes: [
						"SUPER_ADMINISTRATOR",
						"SUPER_ASSISTANT_ADMINISTRATOR",
						"CHURCH_ADMINISTRATOR",
						"ORGANIZATION_ADMINISTRATOR",
					]},
				
				
				{ url: "/profile", label: "Profile", collapse: false, icon: "assignment_ind", title: false, userTypes: [
						"SUPER_ADMINISTRATOR",
						"SUPER_ASSISTANT_ADMINISTRATOR",
						"CHURCH_ADMINISTRATOR",
						"CHURCH_ASSISTANT_ADMINISTRATOR",
						"CHURCH_BRANCH_ADMINISTRATOR",
						"CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
					] },
				{ url: "/ip-setter", label: "IP Settings", collapse: false, icon: "assignment_ind", title: false },
				{ url: "/sign-in", label: "Log Out", collapse: false, icon: "login", title: false, command: "logout"},
			]
		};
	},
	methods: {
		isUserAllowed: function(singleNavLink){
			if(singleNavLink.userTypes){
				let loggedInUser = this.$store.getters.getLoggedInUser;
				if(loggedInUser===null) return false;
				return singleNavLink.userTypes.includes(loggedInUser.userType);
			}
			return true;
		},
		logout: function(ignored){
			this.$root['logout']();
		},
		createNewOrganization: function(ignored){
			this.$store.commit("setInstitution", null);
		},
		handleCommand: function(command){
			if(typeof this[command.method]==="function"){
				this[command.method](command.value);
			}
		}
	},
	components: {
		SidenavCollapse,
	},
};
</script>
