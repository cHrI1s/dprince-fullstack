<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="card bg-light">
		<div class="card-body bg-transparent position-relative overflow-hidden">
			<div class="position-absolute top-0 right-0">
				<div class="p-3 bg-danger rounded-3 cursor-pointer"
				     @click="removeMember">
					<i class="pi pi-trash fw-bolder text-white"/>
				</div>
			</div>
			<div class="d-flex">
				<div class="avatar-container">
					<img :src="userAvatar"
					     class="w-100 user-profile"
					     alt="avatar-logo"/>
				</div>
				<div class="d-block position-relative flex-grow-1 ps-lg-3 ps-2">
					<div class="fw-bolder text-uppercase">
						{{ member.firstName +" "+ member.lastName }}
					</div>
					<div class="text-sm">
						{{ getFamilyRole(member.familyRole) }}
					</div>
					<div class="text-sm">
						Phone: <strong>{{ phoneNumber }}</strong>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import userAvatar from "@/assets/img/user-avatar.png";
import {FAMILY_ROLES} from "@/dashboard/members/members";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

export default {
	name: "FamilyMemberCard",
	methods: {
		removeMember: function(){
			this.$store.commit("setLoading", true);
			setTimeout(()=>{
				let family = this.$store.getters.getFamily;
				if(family!==null){
					let members = (typeof family.members!=="undefined") ? family.members : [];
					if(members.length>0){
						members = members.filter(singleMember=>singleMember.id!==this.member.id);
						family = {
							...family,
							members: [...members]
						}
						this.$store.commit("setFamily", family);
					}
				}
				this.$store.commit("setLoading", false);
			}, 1000);
		},
		getFamilyRole: function(roleValue){
			let value = [...FAMILY_ROLES].find(singleRole=>{
				return singleRole.value===roleValue;
			});
			if(typeof value==="undefined") return null;
			return value.label;
		}
	},
	computed: {
		userAvatar: function(){
			if(typeof this.member.profile!=='undefined' && this.member.profile!==null) return APP_CONFIG.BACKEND_SERVER+"/files/public-image/"+this.member.profile;
			return userAvatar;
		},
		phoneNumber: function(){
			let phone = (typeof this.member.phoneCode!=='undefined' && this.member.phoneCode!==null)
				? "+"+this.member.phoneCode+" "
				: "+91 ";
			if(typeof this.member.phone!=='undefined' && this.member.phone!==null){
				phone += this.member.phone;
				return phone;
			}
			return null;
		},
	},
	props: {
		member: {
			type: Object,
			required: true
		}
	}
}
</script>

<style lang="scss">
	.avatar-container{
		width: 60px;
	}
</style>