<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<aside
		id="sidenav-main"
		class="sidenav navbar navbar-vertical navbar-expand-xs border-0 fixed-start ms-0 overflow-y-hidden"
		:class="`${sidebarType}`">
		
		<div class="position-relative h-100">
			<div class="sidenav-header text-center top-0 background-black d-flex w-100 justify-content-center align-items-center"
			     ref="sidenavHeader">
				<i class="top-0 p-3 cursor-pointer fas fa-times text-secondary opacity-5 position-absolute end-0 d-none d-xl-none"
				   aria-hidden="true"
				   id="iconSidenav"></i>
				<router-link to="/" class="m-0 d-block">
					<img :src="appLogo"
					     class="d-block position-relative navbar-brand-image"
					     alt="Application's Logo"/>
				</router-link>
			</div>
			
			<sidenav-list :height="sidenavHeight"/>
		</div>
	</aside>
</template>
<script>
import SidenavList from "./SidenavList.vue";
import { mapState } from "vuex";
import dnoteLogo from "@/assets/img/logos/favicon.png";
import {APP_CONFIG} from "@/utils/APP_CONFIG";


export default {
	name: "index",
	components: {
		SidenavList,
	},
	methods: {
		headerSidenavHeightChange: function(){
			const SIDENAV_HEADER = this.$refs.sidenavHeader.offsetHeight,
				WINDOW_HEIGHT = window.innerHeight;
			this.sidenavHeight = (WINDOW_HEIGHT-SIDENAV_HEADER);
		}
	},
	data() {
		return {
			sidenavHeight: 0,
			dnoteLogo
		};
	},
	computed: {
		...mapState(["sidebarType"]),
		appLogo: function(){
			let institution = this.$root['institution'];
			if(institution===null) return dnoteLogo;
			if(institution.logo!==null) return APP_CONFIG.BACKEND_SERVER+"/files/logo/"+institution.logo;
			return dnoteLogo;
		}
	},
	beforeUnmount() {
		window.removeEventListener("resize", this.headerSidenavHeightChange);
	},
	mounted(){
		this.headerSidenavHeightChange();
		window.addEventListener("resize", this.headerSidenavHeightChange);
	}
};
</script>

<style>
.background-black{
	background: linear-gradient(to bottom, black 85%, transparent 95%);
}

.navbar-brand-image{
	max-height: 50px;
	width: auto;
	margin: auto;
}
</style>
