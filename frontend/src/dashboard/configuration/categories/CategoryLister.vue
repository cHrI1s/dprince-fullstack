<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative">
		<div class="row position-relative"
		     v-if="categories.length>0">
			<div class="col-12">
				<h3 class="fw-bolder">
					{{ isInstitutionChurch ? 'Types of Churches' : "Partners' Categories" }}
				</h3>
			</div>
			<div class="col-md-6 position-relative mt-4"
			     v-for="(singleCategory, index) in categories"
			     :key="singleCategory.id+'_'+index">
				<div class="text-dark bg-light p-md-4 p-3 border rounded-3">
					<div class="d-block position-relative fw-bolder mb-2 text-dark">
						{{ singleCategory.name }}
					</div>
					
					<div class="row">
						<div class="col-6">
							<Button label="Update"
							        icon="pi pi-pencil"
							        size="small"
							        severity="warn"
							        outlined
							        @click="$emit('update', singleCategory)"
							        :title="'Update '+singleCategory.name"
							        class="w-100"/>
						</div>
						
						<div class="col-6">
							<Button label="Delete"
							        icon="pi pi-trash"
							        severity="danger"
							        size="small"
							        @click="$emit('delete', singleCategory)"
							        outlined
							        :title="'Delete '+singleCategory.name"
							        class="w-100"/>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="d-block position-relative px-3 py-5" v-else>
			<h3 class="my-0 text-danger fw-bolder text-center">
				{{ isInstitutionChurch ? 'No Church Type(s) found here' : 'No Category found here.' }}
			</h3>
		</div>
	</div>
</template>

<script>
import Button from "primevue/button";

export default {
	name: "CategoryLister",
	components: {Button},
	emits: ["update", "delete"],
	computed : {
		isInstitutionChurch: function(){
			return this.isChurch;
		},
	},
	props: {
		categories: {
			type: Array,
			required : true,
			default(){
				return [];
			}
		},
		title: {
			type: String,
			required : true,
			default(){
				return null;
			}
		},
		isChurch: {
			type: Boolean,
			required: true,
			default(){
				return false;
			}
		}
	}
}
</script>

<style scoped lang="scss">

</style>