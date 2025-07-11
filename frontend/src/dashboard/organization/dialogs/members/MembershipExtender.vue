<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import Select from "primevue/select";
import FormLabel from "@/components/FormLabel.vue";
export default {
	name: "MembershipExtender",
	components: {FormLabel, Button, Dialog, Select},
	emits: ["close"],
	updated(){
		this.selectedMembership = null;
	},
	props: {
		visible: {
			type: Boolean,
			required: true,
			default(){ return false; }
		},
		member: {
			type: [Object, null],
			required: true,
			default(){ return null; }
		},
	},
	computed: {
		shown: function(){
			return this.visible;
		},
		orgMember: function(){
			return this.member;
		},
		options: function(){
			return [
				{label: "One Week", value: "WEEKLY"},
				{label: "One Month", value: "MONTHLY"},
				{label: "3 Months", value: "QUARTER"},
				{label: "6 Months", value: "SEMESTRAL"},
				{label: "1 Year", value: "ANNUAL"},
				{label: "Lifetime", value: "LIFETIME"},
			]
		}
	},
	data(){
		return {
			selectedMembership: null,
		}
	},
	methods: {
		extendMembership: function(){
			let data = this.$root['addInstitutionId']({
				memberId: this.orgMember.id,
				duration: this.selectedMembership
			}, false, true);
			const TITLE = "Membership Extension";
			this.$api.post("/institution/extend-membership", data).then(response=>{
				this.$root['showAlert']("success", TITLE, response.message);
				this.$emit("close", response.object);
			}).catch(error=>{
				this.$root['handleApiError'](error, TITLE);
			});
		}
	}
}
</script>

<template>
	<Dialog :visible="shown"
	        class="w-md-50 w-100"
	        :draggable="false"
	        :modal="true"
	        :close-on-escape="false"
	        :header="orgMember.firstName+' '+orgMember.lastName"
	        :closable="false">
		<div class="row mt-4">
			<div class="d-block position-relative">
				<FormLabel label-text="Extend To"/>
				<Select :options="options"
				        v-model="selectedMembership"
				        optionLabel="label"
				        optionValue="value"
				        placeholder="Extend Membership to"
				        class="w-100"/>
			</div>
		</div>
		<template #footer>
			<div class="row position-relative w-100 px-0 mx-0">
				<div class="col-6 ps-0">
					<Button label="Close"
					        severity="danger"
					        @click="()=>$emit('close')"
					        class="w-100"
					        icon="pi pi-times"/>
				</div>
				<div class="col-6 pe-0">
					<Button label="Extend"
					        severity="success"
					        @click="extendMembership"
					        class="w-100"
					        icon="pi pi-arrow-right-arrow-left"/>
				</div>
			</div>
		</template>
	</Dialog>
</template>

<style scoped lang="scss">

</style>