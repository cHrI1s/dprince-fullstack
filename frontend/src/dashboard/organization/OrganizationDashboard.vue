<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->
<template>
	<DashboardContainer>
		<div class="pt-4"
		     v-if="showInstitutionName">
			<InstitutionHeader/>
		</div>
		
		<div class="row position-relative mb-4">
			<div class="col-lg-3 col-md-6"
			     v-for="(singleKey, index) in Object.keys(stats)"
			     :key="index">
				<AppStatCard
					:title="stats[singleKey].title"
					:detail="stats[singleKey].detail"
					:icon="stats[singleKey].icon"/>
			</div>
		</div>
		
		<DashboardPad :options="options"/>
	</DashboardContainer>
</template>

<script>
	import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
	import DashboardPad from "@/dashboard/utils/DashboardPad.vue";
	import AppStatCard from "@/dashboard/utils/AppStatCard.vue";
	import InstitutionHeader from "@/dashboard/containers/parts/InstitutionHeader.vue";
	
	export default {
		name : "OrganizationDashboard",
		components: {
			InstitutionHeader,
			AppStatCard,
			DashboardPad,
			DashboardContainer
		},
		data(){
			return {
				stats: {
					money: {
						title : { text: "Today's Money", value: "Rs. 0" },
						detail : "Funds received today.",
						icon: {
							name: 'weekend',
							color: 'text-white',
							background: 'dark',
						}
					},
					partners: {
						title : { text: "Partners", value: 135 },
						detail : "Overall partners",
						icon: {
							name: 'handshake',
							color: 'text-white',
							background: 'success',
						}
					},
					newPartners: {
						title : { text: "Today's Partners", value: 5 },
						detail : "New partners today",
						icon: {
							name: 'handshake',
							color: 'text-white',
							background: 'warning',
						}
					},
					users: {
						title : { text: "Admins", value: 5 },
						detail : "All the Administrators",
						icon: {
							name: 'groups',
							color: 'text-white',
							background: 'primary',
						}
					}
				},
				options : [
					{ label: "Create Organization", styleClass: "col-md-4 mb-4", url: "/organizations/create-new" },
					{ label: "List Organizations", styleClass: "col-md-4 mb-4", url: "/organizations/list" },
					{ label: "Add Partner", styleClass: "col-md-4 mb-4", url: "/organizations" },
					{ label: "Import Partners", styleClass: "col-md-4 mb-4", url: "/organizations/import-partners" },
					{ label: "Address Printing", styleClass: "col-md-4 mb-4", url: "/organizations/address-printing" },
					{ label: "Report Generator", styleClass: "col-md-4 mb-4", url: "/organizations/report-generation" },
				]
			}
		},
		methods: {
		},
		beforeMount(){
			this.$store.commit("setInstitution", null);
			this.$store.commit("setFamily", null);
		},
		props: {
			showInstitutionName: {
				type: Boolean,
				required: false,
				default(){
					return false;
				}
			},
		}
	}
</script>

<style lang="scss">

</style>