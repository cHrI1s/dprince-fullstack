<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<Dialog :visible="shown" modal class="w-md-100 w-lg-75 w-100"
	        @hide="$emit('close')"
	        :draggable="false"
	        :close-on-escape="false"
	        :closable="false">
		<template #header>
			<div class="d-block position-relative">
				<h4 class="my-0">{{ headerTitle }}</h4>
			</div>
		</template>
		
		<div class="row"
		     v-if="visibleOptions!==null">
			<div class="col-md-4"
			     v-for="(option, index) in visibleOptions"
			     :key="index"
			     :class="option.styleClass">
				<div class="text-center py-4 bg-light mt-3 rounded-3 w-100
				            fw-bolder cursor-pointer" 
                     :class="(options.length>1) ? 'col-md-4' : 'col-md-12'"
				     @click="$emit('actionPerformed', option.method)"
				     v-if="typeof option.toggle==='undefined'">
					{{ option.label }}
				</div>
				<div class="text-center py-4 bg-light mt-3 rounded-3 w-100
				            fw-bolder cursor-pointer"
                     :class="(options.length>1) ? 'col-md-4' : 'col-md-12'"
				     @click="$emit('actionPerformed', option.method)"
				     v-else>
					{{ selectedRow[option.toggle] ? option.label[0] : option.label[1] }}
				</div>
			</div>
		</div>
		
		<template #footer>
			<div class="row justify-content-end w-100 pt-4">
				<div class="col-md-6">
					<Button label="Close"
					        class="w-100"
					        @click="$emit('close')"
					        icon="pi pi-times"
					        severity="danger"
					        outlined/>
				</div>
			</div>
		</template>
	</Dialog>
</template>


<script>
import Button from "primevue/button";
import Dialog from "primevue/dialog";

export default {
	name: "ListOptioner",
	components: {
		Dialog,
		Button
	},
	emits: ['close', 'actionPerformed'],
	data(){
		return {
			shown : this.visible
		}
	},
	watch: {
		visible: function(newValue){
			return this.shown = newValue;
		}
	},
	computed: {
		selectedRow: function(){
			return this.row;
		},
		visibleOptions: function(){
			return this.options.filter(option=>{
				let shown = true;
				if(typeof option.hiddenIf!=='undefined'){
					let attribute = option.hiddenIf.attribute,
						value = option.hiddenIf.equals;
					
					if(typeof this.selectedRow[attribute]!=='undefined'){
						shown = this.selectedRow[attribute]!==value;
					}
				}
				return shown;
			})
		}
	},
	props: {
		visible: {
			type: Boolean,
			required: true,
			default(){
				return true;
			}
		},
		headerTitle: {
			type: String,
			required: true,
			default(){
				return "Title";
			}
		},
		options: {
			type: [Array, null],
			required: true,
			default(){
				return [];
			}
		},
		row: {
			type: [Object, null],
			required: false,
			default(){
				return null
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>