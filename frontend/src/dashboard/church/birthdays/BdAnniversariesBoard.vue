<template>
	<DashboardContainer :show-institution-name="true">
		<DashboardTab :tabs="tabList"
		              :selected-tab="selectedTab"
		              @select="changeTab"/>
		
		<BirthdaysList :is-church="isInstitutionChurch"
		               :isBirthday="selectedTab===0"/>
	</DashboardContainer>
</template>

<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import BirthdaysList from "@/dashboard/church/birthdays/BirthdaysList.vue";

export default {
	name: "BdAnniversariesBoard",
	components: {BirthdaysList, DashboardTab, DashboardContainer},
	data(){
		return {
			localTab: this.tab,
			tabList: ['Birthday', 'Marriage Anniversaries']
		}
	},
	computed: {
		isInstitutionChurch: function(){
			return this.isChurch;
		},
		selectedTab: {
			set: function(newValue){
				this.localTab = newValue;
			}, get: function(){
				return this.localTab;
			}
		}
	},
	methods: {
		changeTab: function (tabIndex, reload = false) {
			this.selectedTab = tabIndex;
			if (tabIndex === 0) this.hasSubscriptionType = true;
			let url = (this.isInstitutionChurch)
				? "/church/birthdays/"
				: "/organizations/birthdays/"
			this.$router.push(url + this.selectedTab, {reload: true});
		},
	},
	props: {
		tab: {
			type: Number,
			required: true,
			default(){
				return 0
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>