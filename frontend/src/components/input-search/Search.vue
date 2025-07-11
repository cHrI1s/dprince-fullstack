<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<script>
import InputText from "primevue/inputtext";
import Button from "primevue/button";
export default {
    name: "Search",
    components:{
        InputText,
        Button
    },
    data(){
        return{
            data:null
        }
    },
    props:{
        placeholder:{
            type:String,
            default(){
                return null
            }
        },
        url:{
            type:String,
            default(){
                return null
            }
        },
    },
    methods:{
        search :function(){
            console.log(this.url+""+ this.data)
        }
    }
}
</script>

<template>
    <div class="card p-4 rounded-0">
        <div class="row">
            <div class="col-md-8">
                <InputText v-model="data" :placeholder="placeholder" class="w-100 rounded-0"/>
            </div>
            <div class="col-md-4">
                <Button class="w-100" label="Search"  :search="url" @click="search"/>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">

</style>