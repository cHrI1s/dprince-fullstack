<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <div class="table-responsive">
        <table class="table align-items-center mb-0">
            <thead class="">
                <tr>
                    <th v-for="(singleTr, index) in tableHeaders"
                        :key="index"
                        class="text-uppercase text-secondary
                                    text-xs font-weight-bolder opacity-7">
                        {{ singleTr }}
                    </th>
                </tr>
            </thead>
	        
            <tbody v-if="typeof(tableData.content)!=='undefined'">
                <tr v-for="(row, index) in tableData.content"
                    :key="index">
                    <td v-for="(column, columnIndex) in tableHeaders"
                        :key="columnIndex"
                        class="px-md-4 px-3">
                        {{ getColumnValue(row , column)}}
                    </td>
                    <td class="px-md-4 px-3 text-end">
                        <!-- i need to include t extra options components-->
                    </td>
                </tr>
            </tbody>
	        <tbody v-else>
                <tr>
                    <td :colspan="tableHeaders.length" class="text-center fw-bolder text-danger py-4">
                        No record(s) found
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
<script>
    import {getUIDate} from "@/dashboard/utils/appFx";
    export default {
        name:"AppTable",
        components:{
        },
        methods:{
            getColumnValue: function(row, column){
                if(typeof(column.date)!=="undefined") return getUIDate(row[column.label]);
                /*if(typeof(column.getModelLabelFx)!=="undefined"){
                    return column.getModelLabelFx(row[column.label], ...column.fxParams);
                }*/
                return row[column.label];
            }
        },
        props:{
			tableHeaders: {
				type: Array,
				required: true,
				default(){
					return [];
				}
			},
            tableData: {
                type: Object,
	            required: true,
                default(){
                    return {};
                }
            }
        }
    }
</script>



<style scoped lang="scss">

</style>