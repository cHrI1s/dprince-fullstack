<template>
	<DashboardCard header-title="System Health"
	               :refresh-button="true"
	               @refresh="initialize()">
		<div class="d-block position-relative"
		     v-if="server!==null">
			<div class="d-block position-relative alert text-white mb-4"
			     :class="server.status.status==='UP' ? 'alert-success' : 'alert-danger'">
				The Server is <strong>{{ server.status.status }}</strong>
			</div>
			
			<div class="text-black fw-bolder mb-4">System Metrics</div>
			
			<div class="d-block position-relative">
				<table class="table table-responsive table-striped table-bordered">
					<thead class="thead-dark">
						<tr>
							<th class="col py-1 px-2 fw-bolder">Name</th>
							<th class="col py-1 px-2 fw-bolder">Value</th>
						</tr>
					</thead>
					
					<tbody>
						<tr v-for="singleMetric in Object.keys(metrics)"
						    :key="singleMetric">
							<td class="py-1 px-2"
							    :title="metrics[singleMetric].description">
								{{ singleMetric }}
							</td>
							<td class="py-1 px-2">
								{{ metrics[singleMetric].measurements[0].value }}
								{{ metrics[singleMetric].baseUnit===null ? "" : metrics[singleMetric].baseUnit }}
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</DashboardCard>
</template>

<script>
import DashboardCard from "@/dashboard/containers/DashboardCard.vue";

export default {
	name: "AppHealth",
	components: {
		DashboardCard
	},
	data(){
		return {
			server : null
		}
	},
	computed: {
		metrics: function() {
			if(this.server===null) return {};
			return this.server.metrics;
		}
	},
	methods: {
		initialize: function(){
			this.$api.get("/api/actuator/health").then(response=>{
				this.server = response;
				console.log(response);
			}).catch(error=>{
				console.log(error);
			});
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>

<style scoped lang="scss">

</style>