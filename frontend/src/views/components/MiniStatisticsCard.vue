<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="card mb-2 h-100" :class="directionReverse ? reverseDirection : ''">
		<div class="card-header p-3 pt-2">
			<div class="icon icon-lg icon-shape shadow text-center border-radius-xl mt-n4 position-absolute"
			     :class="`bg-gradient-${icon.background}`">
				<!-- -->
				<i class="material-icons opacity-10"
				   :style="icon.backgroundColor"
				   :class="icon.color"
				   aria-hidden="true">{{ icon.name }}</i>
			</div>
			<div class="pt-1" :class="isRTL ? 'text-start' : 'text-end'">
				<p class="text-sm mb-0 text-capitalize">{{ title.text }}</p>
				<h4 class="mb-0">{{ title.value }}</h4>
			</div>
		</div>
		<div class="d-block position-relative px-3">
			<div class="d-block position-relative"
			     v-for="(subDetail, index) of subDetails"
			     :key="'sub_detail_'+index">
				<div v-if="subDetail!==null">{{ subDetail.label+": "+subDetail.value }}</div>
				<div class="text-white" v-else>---</div>
			</div>
		</div>
		<hr class="dark horizontal my-0" />
		<div class="card-footer p-md-3 p-2" :class="isRTL ? 'text-start' : 'text-end'">
			<!--  eslint-disable-next-line vue/no-v-html -->
			<router-link :to="url"
			             v-if="url!==null">
				<p class="mb-0" v-html="detail"></p>
			</router-link>
			<p class="mb-0" v-html="detail" v-else></p>
		</div>
	</div>
</template>

<script>
import { mapState } from "vuex";
export default {
	name: "MiniStatisticsCard",
	data() {
		return {
			reverseDirection: "flex-row-reverse justify-content-between",
		};
	},
	props: {
		url: {
			type: String,
			required: false,
			default(){
				return null;
			}
		},
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
		styleIcon :{
			type: String,
			default(){
				return "";
			}
		},
		subDetails: {
			type: Array,
			default(){
				return [null, null];
			}
		},
		icon: {
			type: Object,
			required: true,
			name: String,
			color: String,
			background: String,
			default: () => ({
				color: "text-white",
				background: "dark",
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
