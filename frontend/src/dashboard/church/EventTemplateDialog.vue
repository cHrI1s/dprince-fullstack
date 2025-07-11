<template>
    <Dialog :visible="showTemplate"
            :modal="true"
            :closable="false"
            class="w-md-75 w-sm-50">
        <div class="row">
            <div class="col-md-12">
                <FormLabel label-text="Communication way"/>
                <Select class="w-100"
                        :options="communicationWays"
                        v-model="flori"
                        optionLabel="label"
                        @change="handleCommunication(flori)"
                        optionValue="value"
                        placeholder="Select a Template"/>
            </div>
            <div class="col-md-12">
                <FormLabel label-text="Select a template to use"/>
                <Select class="w-100"
                        :options="templates"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Select a Template"/>
            </div>
        </div>
        <template #footer>
            <Button class="w-100"
                    label="Close"
                    @click="$emit('close')"
                    icon="pi pi-times"/>
            <Button class="w-100"
                    severity="info"
                    label="Send Mail"
                    icon="pi pi-send"/>
        </template>
    </Dialog>
</template>
<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import Select from "primevue/select";
import FormLabel from "@/components/FormLabel.vue";
import {COMMUNICATION_WAY} from "@/dashboard/church/church";
export default {
    name: "EventTemplateDialog",
    emits:['close'],
    components :{
        FormLabel,
        Dialog,
        Select,
        Button
    },
    data(){
        return{
            templates:[],
            foundTemplates:[],
            communicationWays : [...COMMUNICATION_WAY].filter(way=>{
                return !["TELEGRAM", "WHATSAPP"].includes(way.value);
            }),
            flori : null,
        }
    },
    methods:{
        handleCommunication : function(way){
            this.templates = this.foundTemplates.filter(single=>{
                if(single.way===way) return single;
            }).map(singleFiltered=>{
                return {
                    label : singleFiltered.name,
                    value : singleFiltered.id
                }
            });
        },
        loadTemplates: function(){
            let data = this.$root['addInstitutionId']({});
            this.$api.post("/communication/get/template", data).then(response=>{
            this.foundTemplates = response.object;
            }).catch(error=>{
                this.$root['handleApiError'](error, "Template(s) Loading");
            });
        },
    },
    beforeMount() {
        this.loadTemplates();
    },
    props:{
        showTemplate:{
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