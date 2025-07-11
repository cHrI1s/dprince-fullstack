<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<table class="table align-items-center mb-0">
		<thead>
			<tr>
				<th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7"
					v-for="(header, index) in headers"
					:key="index">
					{{ header }}
				</th>
				<th class="text-secondary opacity-7"></th>
			</tr>
		</thead>
		<tbody>
			<slot/>
		</tbody>
	</table>
</template>

<script>
import {DEFAULT_PAGE} from "@/dashboard/utils/default_values";

export default {
	name: "ListingTable",
	props: {
		headers: {
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