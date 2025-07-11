<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="page-header align-items-start min-vh-100"
	     style="
      background-image: url('https://images.unsplash.com/photo-1497294815431-9365093b7331?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1950&q=80');
    ">
		<span class="mask bg-gradient-dark opacity-6"></span>
		<div class="container my-auto">
			<div class="row">
				<div class="col-lg-4 col-md-8 col-12 mx-auto">
					<div class="card z-index-0 fadeIn3 fadeInBottom">
						<div class="card-header p-0 position-relative mt-n5 mx-3 z-index-2">
							<div class="bg-white border-radius-lg py-3">
								<div class="row align-content-lg-center position-relative mx-0 px-0">
									<div class="col-lg-4 col-md-4 col-6 mx-auto">
										<img src="/favicon.png"
										     alt="logo"
										     class="w-100"/>
									</div>
								</div>
								<h4 class="text-black font-weight-bolder text-center mt-2 mb-0">
									Sign in
								</h4>
							</div>
						</div>
						<div class="card-body">
							<DashboardTab :tabs="['Username', 'Email', 'SMS']"
							              :selected-tab="tab"
							              @select="changeTab"/>
							
							<div class="d-block position-relative alert-success mb-4 p-3 text-white"
							     v-if="tab===2 && otpForm.message!==null">
								<div class="d-block position-relative fw-bolder">Info: </div>
								{{ otpForm.message }}
							</div>
							
							<div class="position-relative text-start mt-3"
							     v-if="tab===0">
								<div class="mb-3">
									<InputText v-model="form.username"
									           type="email"
									           placeholder="Username"
									           name="email"
									           class="w-100 normal-text"/>
								</div>
								<div class="mb-3">
									<Password v-model="form.password"
									          toggle-mask
									          :inputProps="{ autocomplete: 'new-password' }"
									          type="password"
									          placeholder="Password"
									          name="password"
									          class="w-100 normal-text"
									          :feedback="false"
									          input-class="w-100 normal-text"/>
								</div>
								
								<TermsAcknowledgement/>
								
								<div class="text-center">
									<Button class="mt-4 w-100 fw-bolder"
									        severity="info"
									        icon="pi pi-sign-in fw-bolder"
									        @click="sign"
									        label="Sign In"/>
								</div>
							</div>
							
							<div class="position-relative mt-3"
							     v-if="tab===1">
								<div class="mb-3">
									<InputText v-model="emailForm.email"
									             placeholder="Email"
									             name="email-login-otp"
									             class="w-100"/>
								</div>
								
								<TermsAcknowledgement/>
								
								<div class="text-center">
									<Button class="mb-4 mt-2 w-100 fw-bolder"
									        severity="info"
									        icon="pi pi-send fw-bolder"
									        @click="emailSignIn"
									        label="Send OTP"/>
								</div>
							</div>
							
							<div class="position-relative mt-3"
							     v-if="tab===2">
								<div class="mb-3">
									<InputNumber v-model="phoneForm.phone"
									             :min="1000000000"
									             :max="9999999999"
									             placeholder="Phone Number"
									             inputId="withoutgrouping"
									             :useGrouping="false"
									             name="phone-number"
									             class="w-100"/>
								</div>
								
								<TermsAcknowledgement/>
								
								<div class="text-center">
									<Button class="mb-4 mt-2 w-100 fw-bolder"
									        severity="info"
									        icon="pi pi-send fw-bolder"
									        @click="phoneSignIn"
									        label="Send OTP"/>
								</div>
							</div>
							
							<div class="position-relative mt-3"
							     v-if="tab===3">
								<div class="mb-3">
									<InputNumber v-model="otpForm.otp"
									             :min="100000"
									             :max="999999"
									             placeholder="OTP"
									             inputId="withoutgrouping"
									             :useGrouping="false"
									             name="otp-number"
									             class="w-100"/>
								</div>
								
								<div class="mb-3 text-center fw-bolder alert-warning p-3">
									The OTP expires in {{ computedTimeLeft }}
								</div>
								
								<div class="d-block mb-4">
									OTP not Received?
									<span class="fw-bolder cursor-pointer" @click="resend">Click to resend</span>
								</div>
								
								
								<TermsAcknowledgement/>
								
								<div class="text-center">
									<Button class="mb-4 mt-2 w-100 fw-bolder"
									        severity="info"
									        icon="pi pi-unlock fw-bolder"
									        :disabled="verifyLoginButtonDisabled"
									        @click="verifyLogin"
									        label="Verify & Login"/>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<footer class="footer position-absolute bottom-2 py-2 w-100">
			<div class="container">
				<div class="copyright text-center text-sm text-white">
					&copy; {{ new Date().getFullYear() }}
				</div>
			</div>
		</footer>
	</div>
</template>

<script>
import Button from "primevue/button";
import InputNumber from "primevue/inputnumber";
import InputText from "primevue/inputtext";
import Password from "primevue/password";

import DashboardTab from "@/dashboard/tab/DashboardTab.vue";
import TermsAcknowledgement from "@/auth/TermsAcknowledgement.vue";

export default {
	name: "sign-in",
	data(){
		return {
			timeLeft: 300000,
			tab: 0,
			form : {
				username: null,
				password: null
			},
			emailForm: {
				email: null
			},
			phoneForm: {
				phone: null
			},
			otpForm: {
				message: null,
				otp: null,
				username: null
			},
			countDown: null,
			verifyLoginButtonDisabled: false
		}
	},
	computed: {
		computedTimeLeft: function(){
			let mins = parseInt(Math.floor(this.timeLeft/60000)),
				seconds = (this.timeLeft%60000)/1000;
			if(seconds<10) seconds = "0"+seconds;
			return "0"+mins+":"+seconds;
		}
	},
	components: {
		TermsAcknowledgement,
		DashboardTab,
		Button,
		InputNumber,
		InputText,
		Password
	},
	beforeMount() {
		this.$store.commit("toggleEveryDisplay", true);
		this.$store.commit("toggleHideConfig", true);
	},
	beforeUnmount() {
		this.$store.commit("toggleEveryDisplay", false);
		this.$store.commit("toggleHideConfig", false);
	},
	methods: {
		resend: function(){
			let data = {
				otpType: this.tab===1 ? "EMAIL" : "SMS",
				phone: this.phoneForm.phone,
				email: this.emailForm.email
			};
			this.$api.post('/user/resend-otp', data).catch(error=>{
				this.$root['handleApiError'](error, "OTP Resending.");
			});
		},
		changeTab: function(tabIndex){
			this.tab = tabIndex;
			if(tabIndex>0){
				if(tabIndex===1) {
					this.phoneForm = { phone: null };
				} else {
					this.emailForm = { email: null };
				}
			} else {
				this.phoneForm = { phone: null };
				this.emailForm = { email: null };
			}
		},
		sign: function(){
			this.$api.post("/user/login", this.form).then(response=>{
				this.$root['login'](response);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Login");
			});
		},
		emailSignIn: function(){
			this.$api.post("/user/email-login", this.emailForm).then(response=>{
				if(response.successful) {
					this.tab = 3;
					this.otpForm = {
						message: response.message,
						username: response.object.username
					};
					this.countDown = setInterval(()=>{
						if(this.timeLeft>=1000){
							this.timeLeft -= 1000;
						} else {
							this.verifyLoginButtonDisabled = true;
							this.countDown = null;
							clearInterval(this.countDown);
						}
					}, 1000);
				} else {
					this.$root['showAlert']("error", "OTP Login", "Failed to login.");
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, "OTP Login");
			});
		},
		phoneSignIn: function(){
			this.$api.post("/user/phone-login", this.phoneForm).then(response=>{
				if(response.successful) {
					this.tab = 3;
					this.otpForm = {
						message: response.message,
						username: response.object.username
					};
					this.countDown = setInterval(()=>{
						if(this.timeLeft>=1000){
							this.timeLeft -= 1000;
						} else {
							this.verifyLoginButtonDisabled = true;
							this.countDown = null;
							clearInterval(this.countDown);
						}
					}, 1000);
				} else {
					this.$root['showAlert']("error", "OTP Login", "Failed to login.");
				}
			}).catch(error=>{
				this.$root['handleApiError'](error, "OTP Login");
			});
		},
		verifyLogin: function(){
			this.$api.post("/user/login-verify-otp", this.otpForm).then(response=>{
				this.$root['login'](response);
			}).catch(error=>{
				this.$root['handleApiError'](error, "OTP Login");
			});
		}
	},
};
</script>
