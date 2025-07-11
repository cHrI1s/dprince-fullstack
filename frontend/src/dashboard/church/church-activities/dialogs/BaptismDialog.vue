<template>
    <Dialog :visible="isVisible"
            :draggable="false"
            :modal="true"
            class="w-md-75 w-lg-50"
            :closable="false">
        <template #header>
	        <div class="row w-100">
		        <div class="col-6 text-center p-3"
		             :class="isInsider ? 'bg-dark text-white' : 'text-dark'"
		             @click="isInsider=!isInsider">
			        <h4 class="fw-bolder my-0 cursor-pointer"
			            :class="isInsider ? 'text-white' : 'text-dark'">Existing Priest</h4>
		        </div>
		        <div class="col-6 text-center p-3"
		             :class="!isInsider ? 'bg-dark text-white' : 'text-dark'"
		             @click="isInsider=!isInsider">
			        <h4 class="fw-bolder my-0 cursor-pointer"
			            :class="!isInsider ? 'text-white' : 'text-dark'">Add Guest Priest</h4>
		        </div>
	        </div>
        </template>
	    
        <div class="row mt-3" v-if="isInsider">
	        <div class="col-md-6 mb-4">
		        <FormLabel label-text="Type of Priest" :required="true"/>
		       <Select :options="booleans"
		               v-model="baptism.localBaptizer"
		               optionLabel="label"
		               optionValue="value"
		               placeholder="Type of Priest"
		               class="w-100"/>
	        </div>
	        
            <div class="col-md-6 mb-4"
                 v-if="baptism.localBaptizer">
	            <FormLabel label-text="Local Priest" :required="true"/>
                <Select class="w-100"
                        v-model="baptism.baptist"
                        inputClass="w-100"
                        :options="localPriests"
                        placeholder="Local Priest"
                        optionLabel="label"
                        optionValue="code"/>
            </div>
	        <div class="col-md-6 mb-4" v-else>
	            <FormLabel label-text="Guest Priest" :required="true"/>
                <Select class="w-100"
                        v-model="baptism.baptist"
                        inputClass="w-100"
                        :options="guestPriests"
                        placeholder="Local Priest"
                        optionLabel="label"
                        optionValue="code"/>
            </div>
	        
            <div class="col-12 mb-4">
	            <FormLabel label-text="Date of baptism" :required="true"/>
                <DatePicker  class="w-100"
                             v-model="baptism.dateOfBaptism"
                             :maxDate="maxDate"
                             placeholder="Date of Baptism"/>
            </div>
        </div>
	    
	    <div class="row" v-else>
            <div class="col-md-6 mb-4">
	            <FormLabel label-text="Local Priest"/>
                <InputText class="w-100"
                        v-model="guestBaptist.firstName"
                        placeholder="Priest First name"/>
            </div>
		    
		    <div class="col-md-6 mb-4">
	            <FormLabel label-text="Local Priest"/>
                <InputText class="w-100"
                        v-model="guestBaptist.lastName"
                        placeholder="Priest Last name"/>
            </div>
        </div>
	    
        <template #footer>
	        <div class="row mx-0 w-100">
		        <div class="col-6 ps-0">
			        <Button label="Cancel"
		                class="w-100"
		                @click="$emit('closeDialog', null)"
		                severity="danger"
		                icon="pi pi-times"/>
		        </div>
		        
		        
		        <div class="col-6 pe-0" v-if="isInsider">
			        <Button label="Baptize"
			                class="w-100"
			                severity="info"
			                @click="baptize"
			                icon="pi pi-save"/>
		        </div>
		        
		        <div class="col-6 pe-0" v-else>
			        <Button label="Create Baptist"
		                class="w-100"
		                severity="info"
		                @click="createBaptist"
		                icon="pi pi-save"/>
		        </div>
	        </div>
                
        </template>
    </Dialog>
</template>


<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Select from "primevue/select";
import DatePicker from "primevue/datepicker";
import {BAPTISM_MODEL, GUEST_BAPTIST} from "@/dashboard/church/church-activities/utils/visiting_model";
import FormLabel from "@/components/FormLabel.vue";
import {BOOLEANS} from "@/utils/structures/BOOLEAN";
export default {
    name: "BaptismDialog",
    components : {
	    FormLabel,
        Dialog,
        Button,
        Select,
        DatePicker,
        InputText
    },
    props:{
        isVisible : {
            type :Boolean,
            default(){
                return false;
            }
        },
        memberToBeBaptized : {
            type:Object,
            default(){
                return null;
            }
        }
    },
    emits : ['closeDialog', 'save'],
    data(){
        return{
	        localPriests : [],
	        guestPriests : [],
            isInsider  : true,
            baptism :{...BAPTISM_MODEL},
	        maxDate: new Date(),
	        guestBaptist: {...GUEST_BAPTIST}
        }
    },
    methods: {
        initialize : function(){
            let data = this.$root['addInstitutionId']({});
            this.$api.post("/baptism/get-baptists", data).then(response=>{
				this.localPriests = response.LOCAL.map(singleMember=>{
		            return {
			            label:singleMember.fullName.toUpperCase(),
			            code : singleMember.id,
		            };
	            });
                this.guestPriests = response.GUEST.map(singleMember=>{
	                return {
		                label:singleMember.fullName.toUpperCase(),
		                code : singleMember.id,
	                };
                });
            }).catch(error=>{
                this.$root['handleApiError'](error, "Member searching");
            });
        },
	    createBaptist: function(){
			const TITLE = "Guest Baptist";
		    let data = this.$root['addInstitutionId'](this.guestBaptist);
		    this.$api.post("/baptism/create/guest-baptist", data).then(response=>{
				this.guestPriests = [
					{
						label: response.object.fullName.toUpperCase(),
						code: response.object.id
					},
					...this.guestPriests
				];
			    this.guestBaptist = {...GUEST_BAPTIST};
			    this.$root['showAlert']('success', TITLE, response.message);
		    }).catch(error=>{
			    this.$root['handleApiError'](error, TITLE);
		    });
	    },
        baptize : function(){
	        let data = this.$root['addInstitutionId'](this.baptism);
			data = {
				...data,
				baptismalCandidateId: this.memberToBeBaptized.id
			};
			let url = this.isInsider ? "/baptism/local/baptize" : "/baptism/guest/baptize";
			this.$api.post(url, data).then(response=>{
				this.$root['showAlert']('success', "Baptism", response.message);
				// this.insider = {...INSIDER_MODEL};
				this.$emit("closeDialog", response.object);
			}).catch(error=>{
				this.$root['handleApiError'](error, "Christening");
			});
        }
    },
	computed: {
		booleans: function(){
			return [
				{ label : "Local Priest", value: true},
				{ label : "Guest Priest", value: false},
			]
		}
	},
	beforeMount(){
		this.initialize();
	}
}
</script>



<style scoped lang="scss">

</style>
