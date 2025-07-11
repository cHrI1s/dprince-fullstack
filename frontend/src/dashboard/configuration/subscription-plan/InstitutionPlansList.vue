<template>
	<div class="d-block position-relative">
		<div class="d-block position-relative mb-4"
		     v-for="(singleSubscription , index) in subscriptionPlans"
		     :key="index">
			<div class="card rounded-0 bg-gray-200">
				<div class="card-header mb-0 pb-0 bg-gray-200">
					<h4 class="mb-4">
						{{ singleSubscription.institutionType }}
					</h4>
					<table>
						<tr>
							<th>Name:</th>
							<td class="text-muted">{{singleSubscription.name}}</td>
						</tr>
						<tr>
							<th>Emails:</th>
							<td class="text-muted">{{singleSubscription.emails}}</td>
						</tr>
						<tr>
							<th>Smses:</th>
							<td class="text-muted">{{singleSubscription.smses}}</td>
						</tr>
					</table>
				</div>
				<div class="card-body">
					<div class="row">
						<div class="col-md-6">
							<Button class="w-100 rounded-0"
							        label="Delete"
							        severity="danger"
							        @click="$emit('edit', singleSubscription.id, index, singleSubscription.institutionType)"
							        icon="pi pi-trash"/>
						</div>
						<div class="col-md-6">
							<Button class="w-100 rounded-0"
							        label="Update"
							        @click="$emit('update', singleSubscription)"
							        icon="pi pi-refresh"/>
						</div>
					</div>
				
				
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";
export default {
	name: "InstitutionPlansList",
	components: {Button},
	emits: ["edit", "update"],
	computed: {
		subscriptionPlans: function(){
			return this.plans;
		}
	},
	props: {
		plans: {
			type : Array,
			required: true,
			default(){
				return [];
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>