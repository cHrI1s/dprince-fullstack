<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog :modal="true"
	        :close-on-escape="true"
	        :closable="false"
	        class="w-md-50 w-100"
	        :visible="visible">
		<template #header>
			<div class="d-block position-relative">
				<h3 v-if="memberLocal!==null">Add {{ memberLocal.fullName.toUpperCase() }} to group(s)</h3>
				<h3 v-else>No member Selected</h3>
			</div>
		</template>
		
		<div class="row align-items-center pt-1">
			<div class="col-md-8">
				<MultiSelect optionLabel="name"
				             optionValue="id"
				             :options="groups"
				             v-model="groupsIds"
				             class="w-100"
				             placeholder="Select Group(s)"/>
			</div>
			<div class="col-md-4">
				<Button label="Save"
				        class="w-100"
				        severity="info"
				        @click="save"
				        icon="pi pi-plus"/>
			</div>
		</div>
		
		<template #footer>
			<div class="row position-relative w-100 mx-0 px-0">
				<div class="col-6 ps-0">
					<Button label="Refresh"
					        class="w-100"
					        severity="warn"
					        @click="loadGroups"
					        icon="pi pi-refresh"/>
				</div>
				<div class="col-6 pe-0">
					<Button label="Close"
					        class="w-100"
					        severity="danger"
					        @click="$emit('close', null)"
					        icon="pi pi-times"/>
				</div>
			</div>
		</template>
	</Dialog>
</template>

<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import MultiSelect from "primevue/multiselect";
export default {
	name: "GroupMemberAdder",
	components: {
		Button,
		Dialog,
		MultiSelect
	},
	emits: ["close"],
	data(){
		return {
			visible: this.shown,
			memberLocal: this.member,
			groups: [],
            selectedGroup : null,
		}
	},
	computed: {
		groupsIds: {
			get: function(){
				return this.memberLocal.groupsIds
			},
			set: function(newValue){
				this.memberLocal = {
					...this.memberLocal,
					groupsIds : [...newValue]
				}
			}
		}
	},
	methods: {
		loadGroups: function(){
			let data = this.$root["addInstitutionId"]({});
			this.$api.post("/group/get", data).then(response=>{
				this.groups = response.map(group=>{
					return {
						...group,
						name: group.name.toUpperCase()
					}
				});
			}).catch(error=>{
				this.$root['handleApiError'](error, "Group(s) Loading");
			});
		},
		save: function(){
			let data = {
				memberId : this.memberLocal.id,
				groupsIds: this.groupsIds
			};
			this.$api.post("/group/add-member", data).then(response=>{
				this.$emit("close", this.groupsIds);
				this.$root['showAlert']("success",
					"Saving Membership to Group",
					response.message);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Saving Membership to Group");
			})
		}
	},
	beforeMount() {
		this.loadGroups();
	},
	props: {
		shown: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		},
		member: {
			type: [Object, null],
			required: true,
			default(){
				return null
			}
		}
	},
	watch: {
		shown: function(newValue){
			this.visible = newValue;
		},
		member: function(newValue){
			this.memberLocal = {...newValue};
		}
	}
}
</script>

<style scoped lang="scss">

</style>