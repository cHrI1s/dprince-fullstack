<template>
    <Dialog :visible="isShown" :modal="true"
            :closable="false"
            class="w-lg-75 w-md-50">
        <template>
        
        </template>
        <div class="row">
            <div class="col-md-6"
                 @click="$emit('print')">
                <div class="card rounded-0" style="background-color: #ecf0f1">
                    <div class="card-body">
                        <h4 class="fw-bolder text-center">Print</h4>
                        <h4 class="fw-bolder cursor-pointer text-center">Print</h4>
                    </div>
                </div>
            </div>
            <div class="col-md-6 mb-4" @click="$emit('sendSMS')">
                <div class="card rounded-0" style="background-color: #ecf0f1">
                    <div class="card-body">
                        <h4 class="fw-bolder  cursor-pointer text-center">SMS</h4>
                        <h4 class="fw-bolder text-center">SMS</h4>
                    </div>
                </div>
            </div>
            <div class="col-md-6" @click="$emit('sendEmail')">
                <div class="card rounded-0" style="background-color: #ecf0f1">
                    <div class="card-body">
                        <h4 class="fw-bolder cursor-pointer text-center">EMAIL</h4>
                        <h4 class="fw-bolder text-center">EMAIL</h4>
                    </div>
                </div>
            </div>
            <div class="col-md-6" @click="$emit('mailMerge')">
                <div class="card rounded-0" style="background-color: #ecf0f1">
                    <div class="card-body">
                        <h4 class="fw-bolder cursor-pointer text-center">MERGE MAIL</h4>
                        <h4 class="fw-bolder text-center">MERGE MAIL</h4>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <div class="col-12">
                <Button class="w-100" label="Cancel" icon="pi pi-times" @click="$emit('cancel')"/>
            </div>
        </template>
    </Dialog>
</template>
<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
export default {
    name: "ReceiptPrePrinter",
    components:{Button,Dialog},
    emits:['print', 'sendSMS', 'sendEmail', 'mailMerge','cancel'],
    data(){
        return{
        
        }
    },
    props:{
        isShown :{
            type:Boolean,
            default(){
                return false;
            }
        }
    }
}
</script>



<style scoped lang="scss">

</style>