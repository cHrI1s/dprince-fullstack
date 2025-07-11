<template>
	<Dialog :visible="visible"
	        :closable="false"
	        :modal="true"
	        :header="'Select a Date'"
	        class="w-lg-50 w-md-75 w-100">
		
		<div class="d-block position-relative">
			<DatePicker date-format="dd/mm/yy"
			            v-model="selectedDate"
			            :max-date="maxDate"
			            class="w-100"
			            placeholder="Select a Date"/>
		</div>
		
		
		<template #footer>
			<Button
				label="Close"
				severity="danger"
				icon="pi pi-times"
				@click="close(true)"
				class="w-100"/>
			<Button
				label="Go On"
				icon="pi pi-check"
				@click="close(false)"
				class="w-100"/>
		
		</template>
	</Dialog>
</template>


<script>
import Dialog from "primevue/dialog";
import DatePicker from "primevue/datepicker";
import Button from "primevue/button";
export default {
	name: "DateChooser",
	components: {
		Button,
		Dialog,
		DatePicker
	},
	emits: ["close"],
	data(){
		return {
			selectedDate: null,
			maxDate: new Date()
		}
	},
	computed: {
		visible: function(){
			return this.show;
		}
	},
	props: {
		show: {
			required: true,
			default(){
				return true;
			}
		}
	},
	methods: {
		close: function(setNull=true){
			if(setNull) this.selectedDate = null;
			this.$emit('close', this.selectedDate);
		},
	}
}
</script>

<style scoped lang="scss">

</style>