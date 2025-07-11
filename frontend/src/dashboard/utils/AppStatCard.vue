<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="card mb-2" :class="directionReverse ? reverseDirection : ''">
		<div class="card-header p-3 pt-2">
			<div class="icon icon-lg icon-shape shadow text-center border-radius-xl mt-n4 position-absolute"
			     :class="`bg-gradient-${icon.background} shadow-${icon.background}`">
				<i class="material-icons opacity-10"
				   :class="icon.color"
				   aria-hidden="true">
					{{ icon.name }}
				</i>
			</div>
			<div class="pt-1 text-end">
				<p class="text-sm mb-0 text-capitalize">{{ title.text }}</p>
				<h4 class="mb-0">{{ title.value }}</h4>
			</div>
		</div>
		<div class="card-body pt-0 pb-0">
			{{ detail }}
		</div>
		<div class="card-footer pt-0"></div>
	</div>
</template>

<script>
import { mapState } from "vuex";
export default {
	name: "AppStatCard",
	data() {
		return {
			reverseDirection: "flex-row-reverse justify-content-between",
		};
	},
	props: {
		title: {
			type: Object,
			required: true,
			text: String,
			value: [Number, String],
		},
		detail: {
			type: String,
			default: "",
		},
		icon: {
			type: Object,
			required: true,
			name: String,
			color: String,
			background: String,
			default: () => ({
				color: "text-white",
				background: "success",
			}),
		},
		directionReverse: {
			type: Boolean,
			default: false,
		},
	},
	computed: {
		...mapState(["isRTL"]),
	},
};
</script>
