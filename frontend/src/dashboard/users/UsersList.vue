<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative" v-if="contained">
		<DashboardContainer>
			<PaginatedTable :table-headers="tableHeaders"
			                :table-data="tableData"
			                :options="optionsCalled"
			                @search="search"
			                :paginator-title="'List'"/>
		</DashboardContainer>
	</div>
	<div class="d-block position-relative" v-else>
		<PaginatedTable :table-headers="tableHeaders"
		                :table-data="tableData"
		                :options="optionsCalled"
		                @search="search"
		                :paginator-title="'List'"/>
	</div>
</template>


<script>
import DashboardContainer from "@/dashboard/containers/DashboardContainer.vue";
import PaginatedTable from "@/dashboard/utils/paginator/PaginatedTable.vue";
import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
export default {
    name: "UsersList",
    components: {DashboardContainer, PaginatedTable},
    data(){
        return{
            tableData: {...PAGE_MODEL},
            tableHeaders: {
                'Name': {
                    rows: [
                        {type: "String", attribute: "name"}
                    ]
                },
                'Gender': {
                    rows: [
                        {type: "Number", attribute: "followers"}
                    ]
                }
            },
        }
    },
    methods:{
        optionsCalled: function(row){
            this.tmpRow = row;
            this.optionerShown = true;
        },
	    search: function(search){
			this.$api.post("/user/list", search).then(response=>{
				this.tableData = response;
			}).catch(error=>{
				this.$root['handleApiError'](error, "Loading Users.");
			})
	    }
    },
    props: {
        contained: {
            type: Boolean,
            required: false,
            default(){
                return true;
            }
        }
    }
}
</script>

<style scoped lang="scss">

</style>