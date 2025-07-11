<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<router-link :data-bs-toggle="collapse ? 'collapse' : ''"
	             :to="collapse ? `#${collapseUrl}` : collapseUrl"
	             :aria-controls="collapseRef"
	             :aria-expanded="isExpanded"
	             class="nav-link"
	             :class="hasMatchedRoute() ? `active bg-gradient-${color}` : ''"
	             v-bind="$attrs"
	             @click="handleClick">
		<div class="text-center d-flex align-items-center justify-content-center me-2">
			<slot name="icon"></slot>
		</div>
		<span class="nav-link-text ms-1">
			{{
				navText
			}}
		</span>
	</router-link>
	<div :class="isExpanded ? 'collapse show' : 'collapse'">
		<slot name="list"></slot>
	</div>
</template>
<script>
import { mapState } from "vuex";

export default {
	name: "SidenavCollapse",
	emits: ["command"],
	props: {
		collapseRef: {
			type: String,
			required: true
		},
		navText: {
			type: String,
			required: true
		},
		collapse: {
			type: Boolean,
			default: true
		},
		command: {
			type: [String, null],
			required: false,
			default(){
				return null;
			}
		}
	},
	data() {
		return {
			isExpanded: false
		};
	},
	methods: {
		hasMatchedRoute(){
			let matchedPath = this.$route.matched[0];
			if(typeof matchedPath==="undefined") return this.getRoute()===this.collapseUrl;
			let matchedRoutePath = matchedPath.path.split("/:")[0],
				currentUrl = this.collapseRef.split("/:")[0];
			return matchedRoutePath===currentUrl;
		},
		getRoute() {
			const routeArr = this.$route.path.split("/");
			return routeArr[1];
		},
		handleClick: function(){
			this.isExpanded = !this.expanded;
			if(this.command!==null) this.$emit('command', {method: this.command, value: null});
		}
	},
	computed: {
		...mapState(["isRTL", "color"]),
		collapseUrl: function(){
			return this.collapseRef.replaceAll("/:", "/");
		}
	}
};
</script>
