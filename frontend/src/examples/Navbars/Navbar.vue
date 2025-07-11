<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<nav class="shadow-none navbar navbar-main navbar-expand-lg border-radius-xl"
	     v-bind="$attrs"
	     id="navbarBlur"
	     data-scroll="true"
	     :class="isAbsolute ? 'mt-4' : 'mt-0'">
		<div class="px-3 py-1 container-fluid">
			<breadcrumbs :currentPage="currentRouteName" :color="color" />
			<div class="mt-2 collapse navbar-collapse mt-sm-0 me-md-0 me-sm-4"
			     id="navbar">
				<div class="pe-md-3 d-flex align-items-center ms-md-auto">
					<!--<material-input id="search" label="Search here" />-->
				</div>
				<ul class="navbar-nav justify-content-end top-navbar gap-4">
					<li class="nav-item d-xl-none d-flex align-items-center">
						<a href="#"
						   @click="toggleSidebar"
						   class="p-0 nav-link text-body lh-1 p-2"
						   id="iconNavbarSidenav">
							<i class="material-icons cursor-pointer" v-if="$store.getters.isPinned">
								format_align_justify
							</i>
							<i class="material-icons cursor-pointer" v-else>
								format_align_right
							</i>
						</a>
					</li>
					<!--
					<li class="px-3 nav-item d-flex align-items-center">
						<a class="p-0 nav-link lh-1"
						   @click="toggleConfigurator"
						   :class="color ? color : 'text-body'">
							<i class="material-icons fixed-plugin-button-nav cursor-pointer">
								settings
							</i>
						</a>
					</li>-->
					
					
					<li class="px-3 nav-item d-flex align-items-center position-relative"
					    :class="subMenuClass">
						<a class="p-0 nav-link lh-1 p-2 position-relative"
						   :class="color ? color : 'text-body'">
							<i class="material-icons fixed-plugin-button-nav cursor-pointer"
							   @click="toggleSubMenu">
								settings
							</i>
							
							<div class="sub-menu text-dark">
								<ul class="d-block position-relative p-2 cursor-pointer text-normal list-unstyled text-dark">
									<li class="d-block position-relative p-0 m-0">
										<router-link to="/profile"
										             class="d-flex position-relative align-items-center p-2 sub-menu-hoverable"
										             aria-expanded="false"
										             @click="toggleSubMenu">
											<i class="material-icons cursor-pointer">person</i>
											<span>My Profile</span>
										</router-link>
									</li>
									<li class="d-block position-relative p-0 m-0">
										<router-link to="/notifications"
										             class="d-flex position-relative align-items-center p-2 sub-menu-hoverable"
										             aria-expanded="false"
										             @click="toggleSubMenu">
											<i class="material-icons cursor-pointer">notifications</i>
											<span>Notifications</span>
										</router-link>
									</li>
									<li class="d-block position-relative p-0 m-0">
										<div @click="$root['logout']()"
										      title="Logout"
										      class="d-flex position-relative align-items-center p-2 sub-menu-hoverable">
											<i class="material-icons cursor-pointer">logout</i>
											<span>Logout</span>
										</div>
									</li>
								</ul>
							</div>
						</a>
					</li>
				</ul>
			</div>
		</div>
	</nav>
</template>

<script>
//import MaterialInput from "@/components/MaterialInput.vue";
import Breadcrumbs from "../Breadcrumbs.vue";
import { mapMutations, mapState } from "vuex";

export default {
	name: "navbar",
	data() {
		return {
			showMenu: false,
			subMenuClass: null
		};
	},
	props: ["minNav", "color"],
	created() {
		this.minNav;
	},
	methods: {
		...mapMutations(["navbarMinimize", "toggleConfigurator"]),
		toggleSubMenu: function(){
			this.subMenuClass = this.subMenuClass===null ? "active" : null;
		},
		toggleSidebar() {
			this.navbarMinimize();
		},
		logout: function(){
			this.$root['logout']();
		}
	},
	components: {
		Breadcrumbs,
		// MaterialInput,
	},
	computed: {
		...mapState(["isRTL", "isAbsolute"]),
		
		currentRouteName() {
			return this.$route.name;
		},
	},
};
</script>

<style>
.text-size-icons{
	transform: translate(0, 2px);
	font-size: .8rem;
}
.sub-menu-hoverable:hover{
	background-color: #000;
	color: #fff !important;
}

.nav-item:not(.active) .nav-link .sub-menu{
	display: none;
}
.nav-item.active .nav-link .sub-menu{
	display: block;
}

.nav-link .sub-menu{
	position: absolute;
	top: 100%;
	right: 0;
	background-color: #fff;
	min-width: 180px;
	max-width: 200px;
	z-index: 1020;
	border: 1px solid #000;
}



.navbar .top-navbar .nav-link{
	color: #000 !important;
}


.navbar .top-navbar .nav-link.bg-gradient-success{
	background-color: transparent !important;
	background-image: linear-gradient(195deg, #276bd6 0%, #8135df 100%);
	color: #fff !important;
	font-weight: bolder !important;
}

.navbar .top-navbar .nav-link:not(.bg-gradient-success){
	background-color: #000 !important;
	color: #fff !important;
	font-weight: bolder !important;
}

.navbar-nav .material-icons{
	font-size: 2rem !important;
}

.navbar .sidenav-toggler-inner{
	width: 24px;
}

.navbar .sidenav-toggler-inner .sidenav-toggler-line{
	height: 3px !important;
	color: #000 !important;
	background-color: #fff;
}
</style>