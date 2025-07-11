<template>
    <Dialog :visible="isShown"
            :modal="true"
            :draggable="false"
            :closable="false"
            class="w-md-75 w-lg-50 w-100">
        <template #header>
            <h6 class="text-center">Family Members</h6>
        </template>
        <div class="row" >
            <div class="col-12 mb-4">
                <span class="fw-bolder">Family Head: </span>
                <span class="fw-bolder" v-if="familyHead !== null">
                    {{ isInstitutionChurch ? familyHead.code : familyHead.title + " " + familyHead.fullName }}
                </span>
                <span class="fw-bolder" v-else>
                    ---
                </span>
                <div class="d-block mt-3">
                    <span class="fw-bolder">Family Head Role : </span>
                    {{ familyHeadRole ? $root['displayFamilyRole'](familyHeadRole) : '---' }}
                </div>
            </div>
            <div class="col-md-6" v-for="(singleMember , index) in members" :key="index">
                <div class="card mt-3 p-3 bg-light text-uppercase">
                    <div class="d-block mt-3">
                        <span class="fw-bolder">Family role : </span>
                        {{ $root['displayFamilyRole'](singleMember.familyRole) }}
                        <hr/>
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">Name :</span>
                        {{ singleMember.title+" "+singleMember.fullName }}
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">DOB :</span>
                        {{(singleMember.dob!==null) ? getDateFromTimeStamp(singleMember.dob) : "--" }}
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">Address :</span>
                        {{singleMember.addressLine1 + " " + singleMember.addressLine2 }}
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">District :</span>
                        {{singleMember.district +"-" + singleMember.pincode}}
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">State :</span>
                        {{$root['getIndianState'](singleMember.state)}}
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">Country :</span>
                        {{ getCountryNameByCode(singleMember.country) }}
                    </div>
                    <div class="d-block mt-3">
                        <span class="fw-bolder">Email :</span>
                        <span class="text-lowercase">
                            {{( singleMember.email!==null) ? singleMember.email : "---" }}
                        </span>
                    </div>
                    <div class="d-block mt-3 ">
                        <span class="fw-bolder">Phone :</span>
                        {{singleMember.phone}}
                    </div>
                    <div class="d-block mt-3">
                        <span class="fw-bolder">Whatsapp :</span>
                        {{singleMember.whatsappNumber!==null ? singleMember.whatsappNumber : "---"}}
                    </div>
                    <div class="d-block mt-3">
                        <span class="fw-bolder">Code :</span>
                        {{ singleMember.code }}
                    </div>
                </div>
            </div>
        </div>

        <Button class="w-100 mt-3" label="Cancel"
                severity="danger"
                icon="pi pi-times"
                @click="$emit('close')"/>
    </Dialog>
</template>

<script>
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import {getCountryNameByCode} from "../../utils/countries";
import {FAMILY_ROLES} from "@/dashboard/members/members";
import {getDateFromTimeStamp} from "@/utils/AppFx";

export default{
    name: "ShowFamilyMembers",
    components:{Dialog,Button},
    data(){
        return{
            familyRole:[...FAMILY_ROLES], // Idhu ippo sariya irukku
        }
    },
    emits:['close'],
    props:{
        isShown:{
            type:Boolean,
            default(){
                return false;
            }
        },
        family:{
            type:Object,
            default(){
                return {};
            }
        },
        isChurch: {
            type: Boolean,
            required: true,
            default(){
                return false;
            }
        },
    },
    methods:{
        getDateFromTimeStamp,
        getCountryNameByCode,
    },

    computed:{
        theFamily: function(){
            return this.family;
        },
        members: function(){
            if (typeof this.theFamily.members === "undefined" || !Array.isArray(this.theFamily.members)) {
                return [];
            }
            let theMembers = [...this.theFamily.members];
            let availableCodes = [];

            return theMembers.filter(member => {
                if (member && member.code) {
                    if (availableCodes.includes(member.code)) {
                        return false;
                    }
                    availableCodes.push(member.code);
                    return true;
                }
                return false;
            });
        },
        familyHead: function(){
            if (!this.family || !this.family.familyHead) {
                return null;
            }
            if (!this.members || this.members.length === 0) {
                return null;
            }

            let familyHead = this.members.find(member => member.id === this.family.familyHead);

            return familyHead || null;
        },
        // NEW COMPUTED PROPERTY ADDED HERE: Family Head-oda Role-a kandupidikka
        familyHeadRole: function() {
            // familyHead object irukka illana members list irukka nu check pannu
            if (!this.familyHead || !this.members) return null;

            // members list-kulla, familyHead-oda ID-a vachu andha member entry-a thedu
            // Andha entry-kku thaan familyRole property irukkum
            const headMemberEntry = this.members.find(member => member.id === this.familyHead.id);

            // Thedina entry irundha, adhoda familyRole-a return pannu. Illana null.
            return headMemberEntry ? headMemberEntry.familyRole : null;
        },
        isInstitutionChurch: function(){
            return this.isChurch;
        },
    }
}
</script>

<style scoped lang="scss">

</style>