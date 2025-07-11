<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="py-4 container-fluid position-relative">
		<InstitutionHeader/>
		
		<div class="d-block position-relative">
			<div class="row justify-content-end mb-5">
				<div class="col-md-4 col-6"
				     v-if="userType==='EXTRA_SUPER_ADMIN'">
					<Button label="Data AutoCorrect"
					        class="w-100"
					        severity="info"
					        @click="autoCorrect"/>
				</div>
				
				<div class="col-md-4 col-6">
					<Button label="Refresh"
					        class="w-100"
					        severity="warn"
					        @click="loadStats"/>
				</div>
			</div>
			
			
			
			<div class="d-block position-relative"
			     v-if="stats!==null">
				<div class="row mb-4">
					<div class="col-lg-4 col-md-6 mb-5"
					     v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Number of Clients', value: stats.institutions }"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'assured_workload', color: 'text-white', background: 'dark'}"
						                      :styleIcon="'yellow'" />
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5"
					     v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Churches', value: stats.churches+stats.churchBranches }"
						                      url="/church/list"
						                      :sub-details="churchesStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Go to Page</span>"
						                      :icon="{ name: 'church', color: 'text-white', background: 'dark'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5"
					     v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Organizations', value: stats.organizations }"
						                      url="/organizations/list"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Go to Page</span>"
						                      :icon="{ name: 'weekend', color: 'text-white', background: 'dark'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5"
					     v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Blocked Clients(s)', value: stats.blockedOrganizations+stats.blockedChurches }"
						                      :sub-details="blockedClientsStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'block', color: 'text-white', background: 'danger'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5">
						<mini-statistics-card :title="{ text: 'Males', value: (adminType==='SUPER') ? stats.churchMales+stats.organizationMales : stats.males}"
						                      :sub-details="malesStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'male', color: 'text-white', background: 'info'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5">
						<mini-statistics-card :title="{ text: 'Females', value: (adminType==='SUPER') ? stats.churchFemales+stats.organizationFemales : stats.females}"
						                      :sub-details="femalesStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'female', color: 'text-white', background: 'primary'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isOrganizationAdminsOnly || isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: isOrganizationAdminsOnly ? 'Partners' : 'Members', value: totalMembers}"
						                      :sub-details="activePartnersDetails"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'badge', color: 'text-white', background: 'info'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isOrganizationAdminsOnly">
						<mini-statistics-card :title="{ text: 'Partners', value: totalMembers}"
						                      :sub-details="countryWisePartnersDetails"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'badge', color: 'text-white', background: 'info'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Org Admins', value: stats.organizationAdmins+stats.organizationAssistantAdmins}"
						                      :sub-details="organizationAdmins"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'admin_panel_settings', color: 'text-white', background: 'info'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isSuperAdmin">
						<mini-statistics-card :title="{ text: 'Church Admins', value: stats.churchAdmins + stats.churchAssistantAdmins}"
						                      :sub-details="churchAdmins"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'admin_panel_settings', color: 'text-white', background: 'primary'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5">
						<mini-statistics-card :title="{ text: 'Families', value: (adminType==='SUPER') ? stats.churchFamilies+stats.organizationFamilies : stats.families}"
						                      :sub-details="familiesStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					
					<!--
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Members', value: stats.activeMembers+stats.inactiveMembers }"
						                      :sub-details="membersStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'loyalty', color: 'text-white', background: 'success'}"/>
					</div>-->
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Baptism', value: stats.baptizedMale+stats.baptizedFemale }"
						                      :sub-details="baptismStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Holy Communion', value: stats.communionMale+stats.communionFemale }"
						                      :sub-details="communionStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Babies', value: stats.babiesMale+stats.babiesFemale }"
						                      :sub-details="babiesStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'child_care', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Children', value: stats.childrenMale+stats.childrenFemale }"
						                      :sub-details="childrenStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Teenage', value: stats.teenageMale+stats.teenageFemale }"
						                      :sub-details="teenageStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Youth', value: stats.youthMale+stats.youthFemale }"
						                      :sub-details="youthStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Adult', value: stats.adultMale+stats.adultFemale }"
						                      :sub-details="adultStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'family_restroom', color: 'text-white', background: 'success'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Birthdays', value: stats.birthdays }"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'cake', color: 'text-white', background: 'warning'}"/>
					</div>
					
					<div class="col-lg-4 col-md-6 mb-5" v-if="isChurchAdminsOnly">
						<mini-statistics-card :title="{ text: 'Marriages', value: stats.marriages }"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'celebration', color: 'text-white', background: 'warning'}"/>
					</div>
				</div>
				
				<div class="row mb-4">
					<div class="col-12 mb-4"
					     v-if="isSuperAdmin">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Registrations</h5>
								</div>
								<div class="col-md-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="registrationTiming"
									        placeholder="Select Timing"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							<Chart type="bar"
							       :data="timedRegistrations"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4"
					     v-if="isOrganizationAdminsOnly">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Partners Registrations</h5>
								</div>
								<div class="col-md-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="orgRegistrationTiming"
									        placeholder="Select Timing"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							<Chart type="bar"
							       :data="orgTimedRegistrations"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					<div class="col-12 mb-4"
					     v-if="isChurchAdminsOnly">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Church Member Registrations</h5>
								</div>
								<div class="col-md-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="churchRegistrationTiming"
									        placeholder="Select Timing"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							<Chart type="bar"
							       :data="churchTimedRegistrations"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4" v-if="isSuperAdmin">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Plans</h5>
								</div>
								<div class="col-md-4 mb-4">
									<Select class="w-100"
									        :options="subscriptionInstitutions"
									        v-model="subscriptionInstitution"
									        placeholder="Select Type"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							<Chart type="bar"
							       :data="subscriptionPlansData"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4" v-if="isSuperAdmin">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Top 5 Activity</h5>
								</div>
								<div class="col-md-4 mb-4">
									<Select class="w-100"
									        :options="subscriptionInstitutions"
									        v-model="toppersSelection"
									        placeholder="Select Type"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							<Chart type="bar"
							       :data="toppersChart"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4"
					     v-if="isOrganizationHigherAdminsOnly || isChurchHigherAdminsOnly">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">
										{{ institutionType==="CHURCH" ? "Donations Based on Offerings" : "Donations Based on Categories" }}
									</h5>
								</div>
								<div class="col-md-4 mb-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="institutionDonationWiseDuration"
									        placeholder="Select Duration"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							
							<Chart type="bar"
							       :data="wiseDonations"
							       :options="institutionType==='GENERAL' ? horizontalChartOptions : pieChartOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4"
					     v-if="isOrganizationHigherAdminsOnly">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">{{ institutionType==='CHURCH' ? 'Members By Offerings' : 'Partners By Categories' }}</h5>
								</div>
								<div class="col-md-4 mb-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="institutionDonationDuration"
									        placeholder="Select Duration"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							
							<Chart type="bar"
							       :data="categorizedMembersData"
							       :options="weekRegistrationOptions"
							       class="chart-class"/>
						</div>
					</div>
					
					
					<div class="col-12 mb-4"
					     v-if="isOrganizationHigherAdminsOnly || isChurchHigherAdminsOnly">
						<div class="card p-md-4 p-2">
							<div class="card-header row align-items-center">
								<div class="col-md-8 mb-4">
									<h5 class="my-0 fw-bolder">Top Donations</h5>
								</div>
								<div class="col-md-4 mb-4">
									<Select class="w-100"
									        :options="dashboardDurations"
									        v-model="institutionTopperDuration"
									        placeholder="Select Duration"
									        optionLabel="label"
									        optionValue="value"/>
								</div>
							</div>
							
							<table class="table align-items-center mb-0 table-striped w-100">
								<thead class="table-header">
									<tr>
										<th class="fw-bolder text-white">Full Name</th>
										<th class="fw-bolder text-white">Code</th>
										<th class="fw-bolder text-white">Category</th>
										<th class="fw-bolder text-white">District</th>
										<th class="fw-bolder text-white">Amount</th>
									</tr>
								</thead>
								<tbody v-if="institutionWiseTopMembers.length>0">
									<tr v-for="(row, rowIndex) in institutionWiseTopMembers"
									    :key="rowIndex+'_index'">
										<td class="text-uppercase">{{ row.fullName }}</td>
										<td class="text-uppercase">{{ row.code }}</td>
										<td class="text-uppercase">{{ row.category }}</td>
										<td class="text-uppercase">{{ row.district }}</td>
										<td>{{ row.amount }}</td>
									</tr>
								</tbody>
								<tbody v-else>
									<tr><td colspan="5" class="fw-bolder text-center text-danger">No data ;(</td></tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
				
				
				<div class="row">
					<div class="col-12 mb-4">
						<h5 class="my-0 fw-bolder">Utility Services</h5>
					</div>
					<div class="col-lg-3 col-md-6 mb-4">
						<mini-statistics-card :title="{ text: 'Balanced WhatsApp', value: remainingWhatsapp }"
						                      :sub-details="balancedWhatsappStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'chat', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-3 col-md-6 mb-4">
						<mini-statistics-card :title="{ text: 'SMS', value: isSuperAdmin ? stats.churchUsedSms+stats.organizationUsedSms : stats.usedSms}"
						                      :sub-details="smsStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'sms', color: 'text-white', background: 'success'}"/>
					</div>
					<div class="col-lg-3 col-md-6 mb-4">
						<mini-statistics-card :title="{ text: 'Used Emails', value: isSuperAdmin ? stats.churchUsedEmails+stats.organizationUsedEmails : stats.usedEmails}"
						                      :sub-details="emailsStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'mail', color: 'text-white', background: 'success'}"/>
					</div>
					
					<div class="col-lg-3 col-md-6 mb-4">
						<mini-statistics-card :title="{ text: 'Used WhatsApps', value: stats.usedWhatsapp }"
						                      :sub-details="balancedWhatsappStats"
						                      detail="<span class='text-success text-sm font-weight-bolder'>Updated Now</span>"
						                      :icon="{ name: 'chat', color: 'text-white', background: 'success'}"/>
					</div>
				</div>
			</div>
			
			<div class="d-block position-relative py-5"
			     v-else>
				<div class="d-block position-relative">
					<h3 class="my-0 fw-bolder text-center">Nothing to show</h3>
				</div>
			</div>
		</div>
		
		
		<Dialog v-model:visible="expiring"
		        :modal="true" :closable="true"
		        :header="'Message from '+applicationName"
		        class="w-md-50 w-100">
			<div class="d-block position-relative text-center text-danger fw-bolder">
				{{ stats.expirationMessage }}
			</div>
		</Dialog>
	</div>
</template>

<script>
import MiniStatisticsCard from "@/views/components/MiniStatisticsCard.vue";
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import Select from "primevue/select";
import Chart from "primevue/chart";
import InstitutionHeader from "@/dashboard/containers/parts/InstitutionHeader.vue";
import {APP_CONFIG} from "@/utils/APP_CONFIG";
import {DASHBOARD_DURATIONS, STATS, TIME_BASED_SELECTION} from "@/dashboard/stats/stats";
import {generateDistinctColors} from "@/utils/AppFx";
import {CHURCH_ADMINS, ORGANIZATIONS_ADMINS} from "@/dashboard/users/users";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";

export default {
	name: "SuperDashboardHome",
	components: {
		InstitutionHeader,
		MiniStatisticsCard,
		Button,
		Chart,
		Dialog,
		Select
	},
	computed: {
		institutionType: function(){
			let institution = this.$root['institution'];
			return institution.institutionType;
		},
		wiseDonations: function(){
			if(this.stats!==null && typeof this.stats.categoryWiseDonations !=='undefined' && this.stats.categoryWiseDonations!==null){
				let stats = this.stats.categoryWiseDonations[this.institutionDonationWiseDuration];
				let data = [...Object.values(stats)],
					labels = Object.keys(stats);
				let colors = this.getColors(data.length);
				// let colors = "#0a2351";
				return {
					labels: [...labels],
					datasets: (this.institutionType==='CHURCH')
						? [{
							label: 'Toggle show Donations',
							borderColor: colors,
							backgroundColor: colors,
							data: data
						}]
						: [{
							label: 'Toggle show Donations Based on Categories',
							backgroundColor: colors,
							borderColor: colors,
							data: data
						}]
				}
			}
			return null;
		},
		horizontalChartOptions: function(){
			return {
				...this.weekRegistrationOptions,
				indexAxis: 'y'
			}
		},
		pieChartOptions: function(){
			return {
				maintainAspectRatio: false,
				aspectRatio: 0.8,
				plugins: {
					legend: {
						labels: {
							color: "#000"
						}
					}
				}
			}
		},
		remainingWhatsapp: function(){
			let remaining = this.stats.maxWhatsapp - this.stats.usedWhatsapp;
			if(remaining<0) remaining = 0;
			return remaining;
		},
		balancedWhatsappStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: 0, label: "Go Count"},
				{value: 0, label: "Church Count"}
			];
		},
		smsStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.smsTrans, label: "Trans SMS"},
				{value: this.stats.smsPromo, label: "Promo SMS"}
			];
		},
		emailsStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.churchUsedEmails, label: "GO Count"},
				{value: this.stats.organizationUsedEmails, label: "Church Count"}
			];
		},
		membersStats: function(){
			return [
				{value: this.stats.activeMembers, label: "Active"},
				{value: this.stats.inactiveMembers, label: "Inactive"}
			];
		},
		baptismStats: function(){
			return [
				{value: this.stats.baptizedMale, label: "Male"},
				{value: this.stats.baptizedFemale, label: "Female"}
			];
		},
		communionStats: function(){
			return [
				{value: this.stats.communionMale, label: "Male"},
				{value: this.stats.communionFemale, label: "Female"}
			];
		},
		babiesStats: function(){
			return [
				{value: this.stats.babiesMale, label: "Male"},
				{value: this.stats.babiesFemale, label: "Female"}
			];
		},
		childrenStats: function(){
			return [
				{value: this.stats.childrenMale, label: "Male"},
				{value: this.stats.childrenFemale, label: "Female"}
			];
		},
		teenageStats: function(){
			return [
				{value: this.stats.teenageMale, label: "Male"},
				{value: this.stats.teenageFemale, label: "Female"}
			];
		},
		youthStats: function(){
			return [
				{value: this.stats.youthMale, label: "Male"},
				{value: this.stats.youthFemale, label: "Female"}
			];
		},
		adultStats: function(){
			return [
				{value: this.stats.adultMale, label: "Male"},
				{value: this.stats.adultFemale, label: "Female"}
			];
		},
		
		familiesStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.churchFamilies, label: "Church"},
				{value: this.stats.organizationFamilies, label: "Orgs"}
			];
		},
		churchesStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.churches, label: "Main"},
				{value: this.stats.churchBranches, label: "Branches"}
			];
		},
		organizationAdmins: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.organizationAdmins, label: "Admins."},
				{value: this.stats.organizationAssistantAdmins, label: "Ass. Admins"}
			];
		},
		churchAdmins: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.churchAdmins, label: "Admins."},
				{value: this.stats.churchAssistantAdmins, label: "Ass. Admins"}
			];
		},
		blockedClientsStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.blockedOrganizations, label: "Orgs."},
				{value: this.stats.blockedChurches, label: "Churches"}
			];
		},
		malesStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.organizationMales, label: "Org."},
				{value: this.stats.churchMales, label: "Church"}
			];
		},
		femalesStats: function(){
			if(!this.isSuperAdmin) return [];
			return [
				{value: this.stats.organizationFemales, label: "Org."},
				{value: this.stats.churchFemales, label: "Church"}
			];
		},
		categorizedMembersData: function(){
			let data = {};
			let chosenDuration = this.institutionDonationDuration;
			if(typeof this.stats.membersByCategory!=='undefined' && this.stats.membersByCategory!==null) {
				let donationObject = this.stats.membersByCategory[chosenDuration];
				let labels = Object.keys(donationObject),
					values = labels.map(key=>{
						return donationObject[key] ?? 0;
				}), colors = this.getColors(labels.length);
				data = {
					labels: labels,
					datasets: [
						{
							label: "Toggle Show Stats",
							data: values,
							backgroundColor: colors,
							hoverBackgroundColor: colors
						}
					]
				}
			}
			return data;
		},
		institutionWiseTopMembers: function(){
			let data = [];
			let chosenDuration = this.institutionTopperDuration;
			if(typeof this.stats.institutionWiseTopMembers!=='undefined' && this.stats.institutionWiseTopMembers) {
				if(typeof this.stats.institutionWiseTopMembers[chosenDuration]!=='undefined') {
					data = this.stats.institutionWiseTopMembers[chosenDuration];
				}
			}
			return data;
		},
		subscriptionPlansData: function(){
			let subscriptionInstitution = this.subscriptionInstitution;
			if(typeof this.stats.churchesPlans!=='undefined' && typeof this.stats.organizationsPlans!=='undefined') {
				return {
					labels: (subscriptionInstitution === "CHURCH")
						? Object.keys(this.stats.churchesPlans)
						: Object.keys(this.stats.organizationsPlans),
					datasets: [{
						label: (subscriptionInstitution === "CHURCH") ? "Church" : "General Organizations",
						backgroundColor: "#56a124",
						borderColor: "#56a124",
						data: (subscriptionInstitution === "CHURCH")
							? Object.values(this.stats.churchesPlans)
							: Object.values(this.stats.organizationsPlans)
					}]
				};
			}
			return null;
		},
		toppersChart: function(){
			let toppersSelection = this.toppersSelection;
			if(typeof this.stats.churchToppers!=='undefined' || typeof this.stats.organizationToppers!=='undefined') {
				return {
					labels: (toppersSelection === "CHURCH")
						? this.stats.churchToppers.map(topper => topper.name)
						: this.stats.organizationToppers.map(topper => topper.name),
					datasets: [
						{
							label: "Members",
							backgroundColor: "#56a124",
							borderColor: "#56a124",
							data: (toppersSelection === "CHURCH")
								? this.stats.churchToppers.map(topper => {
									return topper.members;
								})
								: this.stats.organizationToppers.map(topper => {
									return topper.members;
								})
						},
						{
							label: "Receipts",
							backgroundColor: "#810831FF",
							borderColor: "#810831FF",
							data: (toppersSelection === "CHURCH")
								? this.stats.churchToppers.map(topper => {
									return topper.receipts;
								})
								: this.stats.organizationToppers.map(topper => {
									return topper.receipts;
								})
						},
						{
							label: "Addresses",
							backgroundColor: "#2471a1",
							borderColor: "#2471a1",
							data: (toppersSelection === "CHURCH")
								? this.stats.churchToppers.map(topper => {
									return topper.addresses;
								})
								: this.stats.organizationToppers.map(topper => {
									return topper.addresses;
								})
						}
					]
				};
			}
			return null;
		},
		isOrganizationAdmins: function(){
			let admins = [
				...ORGANIZATIONS_ADMINS,
				...SUPER_ADMINISTRATORS_ROLES
			];
			return admins.includes(this.$root['getUserType']());
		},
		isOrganizationAdminsOnly: function(){
			return ORGANIZATIONS_ADMINS.includes(this.$root['getUserType']());
		},
		isOrganizationHigherAdminsOnly: function(){
			let admins = ORGANIZATIONS_ADMINS.filter(admin=>admin!=="ORGANIZATION_DATA_ENTRY_OPERATOR");
			return admins.includes(this.$root['getUserType']());
		},
		isChurchAdmins: function(){
			let admins = [
				...CHURCH_ADMINS,
				...SUPER_ADMINISTRATORS_ROLES
			];
			return admins.includes(this.$root['getUserType']());
		},
		isChurchHigherAdminsOnly: function(){
			let admins = CHURCH_ADMINS.filter(admin=>admin!=="CHURCH_DATA_ENTRY_OPERATOR");
			return admins.includes(this.$root['getUserType']());
		},
		isChurchAdminsOnly: function(){
			return CHURCH_ADMINS.includes(this.$root['getUserType']());
		},
		timedRegistrations: function(){
			let data = null;
			if(typeof this.orgTimedRegistrations!=='undefined' && this.orgTimedRegistrations!==null){
				let timing = this.registrationTiming,
					dataObject = this.stats.orgRegistrations;
				if(typeof dataObject!=='undefined' && dataObject!==null) {
					if (typeof dataObject[timing] !== 'undefined' && dataObject[timing] !== null) {
						let labels = Object.keys(dataObject[timing]);
						if (labels.length > 0) {
							data = {
								labels: labels,
								datasets: [
									{
										label: "Organization Partners",
										backgroundColor: "#2435a1",
										borderColor: "#2435a1",
										data: Object.values(dataObject[timing])
									}
								]
							}
						}
					}
				}
			}
			
			if(this.churchTimedRegistrations!==null){
				let timing = this.churchRegistrationTiming,
					dataObject = this.stats.churchRegistrations;
				if(typeof dataObject!=='undefined' && dataObject!==null) {
					if (typeof dataObject[timing] !== 'undefined' && dataObject[timing] !== null) {
						let labels = Object.keys(dataObject[timing]);
						if (labels.length > 0) {
							if(data===null) {
								data = {
									labels: labels,
									datasets: [
										{
											label: "Church Members",
											backgroundColor: "#24a139",
											borderColor: "#24a139",
											data: Object.values(dataObject[timing])
										}
									]
								}
							} else {
								data = {
									...data,
									datasets: [
										...data.datasets,
										{
											label: "Church Members",
											backgroundColor: "#24a139",
											borderColor: "#24a139",
											data: Object.values(dataObject[timing])
										}
									]
								}
							}
						}
					}
				}
			} else {
				data = {...this.churchTimedRegistrations};
			}
			return data;
		},
		orgTimedRegistrations: function() {
    let timing = this.orgRegistrationTiming; // 'WEEK', 'MONTH', 'YEAR' போன்ற மதிப்புகள் இதில் இருக்கும் என்று கருதுகிறேன்.
    let data = null;
    let dataObject = this.stats.orgRegistrations;

    if (typeof dataObject !== 'undefined' && dataObject !== null) {
        if (typeof dataObject[timing] !== 'undefined' && dataObject[timing] !== null) {
            // API response-ல் dataObject[timing] ஒரு Array என்பதால், அதை நேரடியாக பயன்படுத்தலாம்.
            let chartDataArray = dataObject[timing];

            // Array-வில் டேட்டா உள்ளதா எனச் சரிபார்க்கவும்
            if (chartDataArray.length > 0) {
                let labels = chartDataArray.map(item => item.displayDate); // 'displayDate' ஐ labels ஆக எடுக்கவும்
                let values = chartDataArray.map(item => item.value);       // 'value' ஐ data ஆக எடுக்கவும்

                data = {
                    labels: labels,
                    datasets: [
                        {
                            label: "Partners",
                            backgroundColor: "#56a124",
                            borderColor: "#56a124",
                            data: values
                        }
                    ]
                };
            }
        }
    }
    return data;
},
		churchTimedRegistrations: function(){
			let timing = this.churchRegistrationTiming,
				data = null,
				dataObject = this.stats.churchRegistrations;
			if(typeof dataObject!=='undefined' && dataObject!==null) {
				if (typeof dataObject[timing] !== 'undefined' && dataObject[timing] !== null) {
					let labels = Object.keys(dataObject[timing]);
					if (labels.length > 0) {
						data = {
							labels: labels,
							datasets: [
								{
									label: "Members",
									backgroundColor: "#24a139",
									borderColor: "#24a139",
									data: Object.values(dataObject[timing])
								}
							]
						}
					}
				}
			}
			return data;
		},
		weekRegistrationOptions: function(){
			return {
				maintainAspectRatio: false,
				aspectRatio: 0.8,
				plugins: {
					legend: {
						labels: {
							color: "#000000"
						}
					}
				},
				scales: {
					x: {
						ticks: {
							color: '#000',
							font: {
								weight: 500
							}
						},
						grid: {
							display: false,
							drawBorder: false
						}
					},
					y: {
						ticks: {
							color: "#000"
						},
						grid: {
							color: "#ddd",
							drawBorder: false
						}
					}
				}
			}
		},
		isSuperAdmin: function(){
			if(this.$store.getters.getLoggedInUser!==null){
				return [
					"EXTRA_SUPER_ADMIN",
					"SUPER_ADMINISTRATOR",
					"SUPER_ASSISTANT_ADMINISTRATOR"
				].includes(this.$store.getters.getLoggedInUser.userType);
			}
			return false;
		},
		activePartnersDetails: function(){
			let active = (typeof this.stats.active!=='undefined' && this.stats.active!==null) ? this.stats.active : 0,
				inactive = (typeof this.stats.inactive!=='undefined' && this.stats.inactive!==null) ? this.stats.inactive : 0;
			return [
				{label: "Active", value: active},
				{label: "Inactive", value: inactive}
			]
		},
		countryWisePartnersDetails: function(){
			let active = (typeof this.stats.membersInIndia!=='undefined' && this.stats.membersInIndia!==null) ? this.stats.membersInIndia : 0,
				inactive = (typeof this.stats.membersAbroad!=='undefined' && this.stats.membersAbroad!==null) ? this.stats.membersAbroad : 0;
			return [
				{label: "India", value: active},
				{label: "Abroad", value: inactive}
			]
		},
		totalMembers: function(){
			let active = (typeof this.stats.active!=='undefined' && this.stats.active!==null) ? this.stats.active : 0,
				inactive = (typeof this.stats.inactive!=='undefined' && this.stats.inactive!==null) ? this.stats.inactive : 0;
				return active + inactive;
		},
		userType: function(){
			let type = null;
			if(this.$store.getters.getLoggedInUser!==null){
				type = this.$store.getters.getLoggedInUser.userType;
			}
			return type;
		},
		adminType: function(){
			let type = null;
			if(this.$store.getters.getLoggedInUser!==null){
				switch(this.$store.getters.getLoggedInUser.userType){
					case "EXTRA_SUPER_ADMIN":
					case "SUPER_ADMINISTRATOR":
					case "SUPER_ASSISTANT_ADMINISTRATOR":
						type = "SUPER"
						break;
						
					case "CHURCH_MEMBER":
					case "CHURCH_ADMINISTRATOR":
					case "CHURCH_ASSISTANT_ADMINISTRATOR":
					case "CHURCH_BRANCH_ADMINISTRATOR":
					case "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR":
						type = "CHURCH"
						break;
						
						
					case "ORGANIZATION_ADMINISTRATOR":
					case "ORGANIZATION_ASSISTANT_ADMINISTRATOR":
						type = "GENERAL";
						break;
				}
			}
			return type;
		}
	},
	data(){
		return {
			stats: null,
			expirationMessage: null,
			expiring: false,
			applicationName: APP_CONFIG.APP_NAME,
			registrationTiming: "WEEK",
			orgRegistrationTiming: "WEEK",
			churchRegistrationTiming: "WEEK",
			timeBasedSelection: [...TIME_BASED_SELECTION],
			
			subscriptionInstitution: "CHURCH",
			toppersSelection: "CHURCH",
			subscriptionInstitutions: [{label: "Church", value: "CHURCH"}, {label: "General Organiation", value: "GENERAL"}],
			
			dashboardDurations: [...DASHBOARD_DURATIONS],
			institutionDonationDuration: "WEEK",
			institutionDonationWiseDuration: "WEEK",
			institutionTopperDuration: "WEEK",
			
			colors: []
		}
	},
	methods: {
		generateDistinctColors,
		generateColors: function(){
			return new Promise(resolve=>{
				resolve(this.generateDistinctColors(100));
			});
		},
		getColors(count){
			return this.colors.filter((color, index)=> {
				return index < count;
			});
		},
		autoCorrect: function(){
			this.$api.post("/core/correct-data", {}).then(response=>{
				this.$root['showAlert']("success", "Data Auto Correction", response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Autocorrect Data.");
			});
		},
		loadStats: function(){
			this.$api.get("/dashboard/home").then(response=>{
				this.stats = response;
				if(typeof response.expirationMessage!=="undefined"){
					this.expiring = true;
				}
			}).catch(error=>{
				this.$store.commit("setLoading" ,false);
				this.$root['handleApiError'](error, "Initializing Dashboard.");
			});
		}
	},
	beforeMount(){
		this.$store.commit("setLoading", true);
		this.generateColors()
			.then(colors=>{
				this.colors = colors;
				this.$store.commit("setLoading", false);
				// this.stats = {...STATS};
				this.loadStats();
			});
	}
}
</script>

<style lang="scss">
	.chart-class{
		height: 50vh;
	}
</style>