<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<div class="d-block position-relative w-100">
		<div class="card d-block position-relative bg-transparent border-0 p-0 shadow-none">
			<div class="card-header bg-transparent p-0"
			     v-if="!tablePrintable && (hasQueryInput || dateSearch || paginatorExtraInputs.length>0)">
				<div class="d-block position-relative">
					<!-- Search by page-->
					<div class="row position-relative mb-4">
						<div class="col-12 mb-4"
						     v-if="hasQueryInput">
							<InputText class="w-100"
							           v-model="search.query"
							           :placeholder="searchQueryPlaceholder===null ? 'Search...': searchQueryPlaceholder"/>
						</div>
						<div v-for="(input, index) of paginatorExtraInputs"
						     :key="index"
						     :class="input.styleClass">
							<div class="row"
							     v-if="input.type==='PHONE_NUMBER'">
								<div class="col-md-4 col-4">
									<Select :options="countriesPhoneCodes"
									        optionLabel="label"
									        optionValue="value"
									        placeholder="Code"
									        v-model="search[input.model[0]]"
									        class="w-100"/>
								</div>
								<div class="col-md-8 col-8">
									<InputNumber inputId="withoutgrouping"
									             :useGrouping="false"
									             :placeholder="input.placeholder"
									             :min="(typeof input.min==='undefined') ? null : input.min"
									             :max="(typeof input.max==='undefined') ? null : input.max"
									             v-model="search[input.model[1]]"
									             class="w-100"/>
								</div>
							</div>
							
							<Select v-if="input.type==='SELECT'"
							        @change="onChangeValue($event)"
							        optionLabel="label"
							        optionValue="value"
							        :placeholder="input.placeholder"
							        :options="input.options"
							        v-model="search[input.model]"
							        class="w-100"/>
							
							<MultiSelect v-if="input.type==='MULTISELECT'"
							             filter
							             optionLabel="label"
							             optionValue="value"
							             :placeholder="input.placeholder"
							             :options="input.options"
							             v-model="search[input.model]"
							             class="w-100"/>
							
							<DatePicker v-if="input.type==='DATE' && typeof input.format!=='undefined'"
							            :selectionMpde="(typeof input.selectionMode!=='undefined') ? input.selectionMode: 'single'"
							            :placeholder="input.placeholder"
							            :options="input.options"
							            :dateFormat="input.format"
							            v-model="search[input.model]"
							            :maxDate="typeof input.maxDate ==='undefined' ? null : input.maxDate"
							            class="w-100"/>
							
							<DatePicker v-if="input.type==='DATE' && typeof input.format==='undefined'"
							            :selectionMpde="(typeof input.selectionMode!=='undefined') ? input.selectionMode: 'single'"
							            :placeholder="input.placeholder"
							            :options="input.options"
							            v-model="search[input.model]"
							            :maxDate="typeof(input.maxDate==='undefined') ? null : input.maxDate"
							            class="w-100"/>
							
							<InputText v-if="input.type==='TEXT'"
							           :placeholder="input.placeholder"
							           :options="input.options"
							           v-model="search[input.model]"
							           class="w-100"/>
							
							<InputNumber v-if="input.type==='NUMBER'"
							             inputId="withoutgrouping"
							             :useGrouping="false"
							             :placeholder="input.placeholder"
							             :options="input.options"
							             :min="(typeof input.min==='undefined') ? null : input.min"
							             :max="(typeof input.max==='undefined') ? null : input.max"
							             v-model="search[input.model]"
							             class="w-100"/>
							
							<Textarea v-if="input.type==='TEXTAREA'"
							          :placeholder="input.placeholder"
							          :options="input.options"
							          :auto-resize="true"
							          rows="2"
							          v-model="search[input.model]"
							          class="w-100"/>
						</div>
						
						<div class="col-6 mb-4"
						     v-if="paginated">
							<InputNumber class="w-100"
							             inputId="withoutgrouping"
							             :useGrouping="false"
							             input-class="w-100"
							             prefix="Page: "
							             :min="1"
							             v-model="search.page"
							             placeholder="Page Number"/>
						</div>
						<div class="col-6 mb-4"
						     v-if="paginated">
							<InputNumber class="w-100"
							             inputId="withoutgrouping"
							             :useGrouping="false"
							             input-class="w-100"
							             :min="1"
							             :max="100"
							             prefix="Elements per page: "
							             v-model="search.size"
							             placeholder="Element"/>
						</div>
						
						<div class="col-md-6 mb-4"
						     v-if="dateSearch">
							<DatePicker v-model="search.from"
							            :max-date="todayDate"
							            placeholder="From Date"
							            class="w-100"/>
						</div>
						<div class="col-md-6 mb-4"
						     v-if="dateSearch">
							<DatePicker v-model="search.to"
							            :max-date="todayDate"
							            placeholder="To Date"
							            class="w-100"/>
						</div>
						
						<div class="col-12">
							<Button label="Search"
							        icon="pi pi-search fw-bolder"
							        severity="primary"
							        @click="makeSearch"
							        class="w-100 text-black fw-bolder"/>
						</div>
					</div>
				</div>
				
				<div class="row py-1 justify-content-between">
					<h5 class="my-0 col-md-8" v-if="paginatorTitle!==null">
						{{ paginatorTitle }}
					</h5>
					<h5 class="my-0 col-md-8"
					    v-if="totalAmount!==null">
						Total: Rs.{{ totalAmount }}
					</h5>
					
					<div class="position-relative d-flex paginator-chip-container col-md-8 align-items-top">
						<div class="text-sm paginator-chip d-block my-0 lh-1"
						     v-for="(key, index) in searchedOptions"
						     :key="'chip_'+index">
							{{ key.label+": " }}
							<span class="fw-bolder">
								{{ key.value }}
							</span>
						</div>
					</div>
					
					<div class="fw-bolder text-sm col-md-4 text-end">
						<div>
							{{ formatNumber(tableData.totalElements)+' results found' }}
						</div>
						<div class="mt-3" v-if="paginated">
							{{ tableData.totalPages.toLocaleString('en-IN')+' pages' }}
						</div>
					</div>
				</div>
				
				<div class="row my-4">
					<div class="col-md-4 col-6"
					     v-if="allowedToCreateFamily">
						<Button label="Add Non-Institutional"
						        icon="pi pi-plus"
						        severity="primary"
						        @click="addNonInstitutionalMember"
						        class="w-100"/>
					</div>
                    
					<div class="col-md-4 col-6"
					     v-if="allowedToCreateFamily">
						<Button label="Continue"
						        icon="pi pi-check"
						        severity="info"
						        class="w-100"
						        @click="finishEditing"/>
					</div>
				</div>
			</div>
			
			<div class="card-body px-0 pt-0 pb-2 border-0">
				<div class="table-responsive p-0">
					<table :class="tablePrintable ? '' : 'table align-items-center mb-0 table-striped w-100'"
					       :style="tableStyle"
					       :id="tableId">
						<thead :class="tablePrintable ? '' : 'table-header'"
						       :style="tableHeadStyle">
							<tr>
								<th v-if="rowCounter">S. No.</th>
								<th v-if="makingSelection" class="export-ignored"></th>
								<th v-for="(singleHeader, index) in Object.keys(headers)"
								    :key="singleHeader+'_'+index"
								    :class="tablePrintable ? '' : 'text-uppercase text-white font-weight-bolder py-3'">
									<div class="d-flex justify-content-between">
										<span>
											{{ singleHeader }}
										</span>
										<span v-if="contentData.content.length>0">
											<i class="pi pi-sort-alt cursor-pointer"
											   @click="sortColumn(singleHeader)"/>
										</span>
									</div>
								</th>
								<th class="text-white export-ignored"
								    v-if="!tablePrintable && theRowOptions!==null"></th>
							</tr>
						</thead>
						
						<!-- Body of the table-->
						<tbody v-if="contentData.content.length>0">
							<tr v-for="(row, index) in contentData.content"
							    class="position-relative"
							    :class="getRowClass(row)"
							    :key="'row_'+index">
								<td v-if="rowCounter" style="font-size: .8rem;">{{ rowBaseCode+(index+1) }}</td>
								<td v-if="makingSelection"
								    class="export-ignored">
									<Checkbox v-model="row.isItemSelected"
									          :binary="true"
									          @change="toggleSelectedItem(index)"/>
								</td>
								
								<td v-for="(column, columnIndex) in Object.values(headers)"
								    :key="'column_'+columnIndex">
									<div class="d-flex px-2 py-1 align-items-center">
										<div class="px-2 me-2" v-if="typeof row[parentAttributeLinker]!=='undefined' && row[parentAttributeLinker]!==null && columnIndex===0">
											<i class="pi pi-arrow-right fw-bolder"/>
										</div>
										<div class="d-flex flex-column justify-content-center">
											<div class="d-block position-relative"
											     :class="tablePrintable ? '' : (columnRowIndex>0 ? 'text-sm' : 'fw-bolder')"
											     v-for="(columnRow, columnRowIndex) in getColumnRows(row, column.rows)"
											     :key="'column-row-'+columnRowIndex">
												<div class="d-block position-relative"
												     :class="[(columnRow.type==='StringCommand') ? 'cursor-pointer' : '']"
												     v-if="['Command', 'String', 'StringList', 'StringCommand', 'StringArray', 'Number', 'PhoneNumber', 'PlainNumber', 'Boolean', 'Select', 'Date', 'NotNull'].includes(columnRow.type)">
													<span class="position-relative"
													      v-if="typeof(columnRow.label)!=='undefined'"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														<u>{{ columnRow.label }}</u>
														{{ ((columnRow.labelNoColumn) ? "" : ": ") }}
													</span>
													<span v-if="['String', 'StringCommand', 'PlainNumber'].includes(columnRow.type)"
													      class="fw-bolder"
													      :class="columnRow.lowercase ? '' : 'text-uppercase'"
													      @click="executeCommand(row, columnRow)"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{
															(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
																? getDeepObjectValue(row, columnRow.attribute)
																: '---'
														}}
													</span>
                                                    <span v-if="columnRow.type==='Boolean'"
                                                          :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{
															(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
																? (columnRow.array[getDeepObjectValue(row, columnRow.attribute) ? 1 : 0])
																: '---'
														}}
													</span>
													<div v-if="columnRow.type==='Command'"
													      class="fw-bold cursor-pointer p-2"
													     @click="$emit('customEvent', row)"
                                                          :title="columnRow.text">
														{{ columnRow.text }}
													</div>
													<span v-if="columnRow.type==='StringArray'"
													      class="text-uppercase"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														<span v-for="(arrayProperty, propertyIndex) in columnRow.attribute"
														      :key="propertyIndex">
															{{
																(typeof(getDeepObjectValue(row, arrayProperty))!=='undefined' && getDeepObjectValue(row, arrayProperty)!==null)
																	? getDeepObjectValue(row, arrayProperty)
																	: '---'
															}}
															<span v-if="propertyIndex<(columnRow.attribute.length-1)">
																{{ typeof columnRow.separator!=="undefined" ? columnRow.separator : " " }}
															</span>
														</span>
													</span>
													<span v-if="columnRow.type==='PhoneNumber'"
													      class="text-uppercase"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														<span v-for="(arrayProperty, propertyIndex) in columnRow.attribute"
														      :key="propertyIndex">
															<span v-if="getDeepObjectValue(row, columnRow.attribute[1])!==null">
																{{
																	((propertyIndex===0) ? "+" : "") +
																	(typeof(getDeepObjectValue(row, arrayProperty))!=='undefined' && getDeepObjectValue(row, arrayProperty)!==null)
																		? getDeepObjectValue(row, arrayProperty)
																		: ''
																}}
															</span>
															<span v-else>---</span>
														</span>
													</span>
													<span v-if="columnRow.type==='StringList'"
													      class="text-uppercase"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														<span v-for="(arrayProperty, propertyIndex) in getDeepObjectValue(row, columnRow.attribute)"
														      :key="propertyIndex">
															{{ arrayProperty }}
															<span v-if="propertyIndex<(getDeepObjectValue(row, columnRow.attribute).length-1)">
																{{ typeof columnRow.separator!=="undefined" ? columnRow.separator : " " }}
															</span>
														</span>
													</span>
													<span v-if="['Date', 'DateTime'].includes(columnRow.type)"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{
															(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
																? getDateFromTimeStamp(getDeepObjectValue(row, columnRow.attribute), columnRow.type==='DateTime')
																: '---'
														}}
													</span>
													<span v-if="columnRow.type==='Number'"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{
															(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
																? formatNumber(getDeepObjectValue(row, columnRow.attribute), columnRow.shorten)
																: '---'
														}}
													</span>
													
													<span v-if="columnRow.type==='URL'"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{
															(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
																? getURI(getDeepObjectValue(row, columnRow.attribute))
																: '---'
														}}
													</span>
													
													<span v-if="columnRow.type==='Select'"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{ getDropdownLabel(getDeepObjectValue(row, columnRow.attribute), columnRow.options) }}
													</span>
													
													<span v-if="columnRow.type==='NotNull'"
													      :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
														{{ getDeepObjectValue(row, columnRow.attribute)!==null ? columnRow.array[1] : columnRow.array[0] }}
													</span>
												</div>
												
												<div class="d-block position-relative"
												     v-if="columnRow.type==='Counter'"
												     :title="typeof columnRow.title!=='undefined' ? getDeepObjectValue(row, columnRow.title) : ''">
													<span class="position-relative"
													      v-if="typeof(columnRow.label)!=='undefined'">
														{{ columnRow.label+': ' }}
													</span>
													{{
														(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)
															? getDeepObjectValue(row, columnRow.attribute).length
															: '---'
													}}
												</div>
												
												<div class="d-block position-relative"
												     v-if="columnRow.type==='DateTime'"
												     :title="typeof columnRow.title!=='undefined' ? columnRow.title : ''">
													<div class="d-block position-relative"
													     v-if="(typeof(getDeepObjectValue(row, columnRow.attribute))!=='undefined' && getDeepObjectValue(row, columnRow.attribute)!==null)">
														<div v-for="(timePart, timePartIndex) in getRequestTimeParts(getDeepObjectValue(row, columnRow.attribute))"
														     :key="'time-part_'+timePart+'_'+timePartIndex">
															{{ timePart }}
														</div>
													</div>
													<div class="d-block position-relative" v-else>
														---
													</div>
												</div>
											</div>
										</div>
									</div>
								</td>
								<td class="text-end export-ignored"
								    v-if="!tablePrintable && theRowOptions!==null">
									<span v-if="extraButtons!==null">
									<Button v-for="(button, btnIndex) in extraButtons"
									        :key="'extraButton_'+btnIndex"
									        :icon="button.icon"
									        @click="handleExtraButton(button, row)"
									        class="border-0 text-dark fw-bold me-3"
									        severity="info"
									        outlined/>
									</span>
									
									<Button icon="pi pi-cog"
									        @click="toggleOptioner(row)"
									        class="border-0 text-dark fw-bold"
									        outlined/>
								</td>
							</tr>
						
							<tr v-if="totalAmount!==null">
								<td v-if="rowCounter && theRowOptions!==null"></td>
								<td class="fw-bolder"
								    :colspan="totalColSpan">
									<div class="d-block position-relative px-2 py-1">
										Total amount
									</div>
								</td>
								<td>
									<div class="d-block position-relative px-2 py-1 fw-bolder">
										{{ totalAmount }}
									</div>
								</td>
								<td class="text-end export-ignored"
								    v-if="!tablePrintable && theRowOptions!==null">
								</td>
							</tr>
						</tbody>
						<tbody v-else>
							<tr>
								<td :colspan="Object.keys(headers).length+1"
								    class="text-center fw-bolder text-danger">
									No Records found.
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			
			<div class="card-footer px-0"
			     v-if="!tablePrintable">
				<div class="row align-items-center">
					<div class="col-4">
						<Button label="Previous"
						        @click="loadPage(false, search)"
						        icon="pi pi-arrow-left"
						        :disabled="contentData.currentPage===0"
						        class="no-outline w-100"
						        severity="warn"/>
					</div>
					<div class="col-4 text-center">
						<div class="d-block position-relative"
						     v-if="paginated">
							<div class="d-block position-relative text-xs"
							     v-if="contentData.content.length>0">
								{{ (contentData.currentPage+1) + ' out of ' + contentData.totalPages }}
							</div>
							<div class="d-block position-relative text-xxs"
							     v-if="contentData.content.length>0">
								Pages
							</div>
							<div class="d-block position-relative text-xs"
							     v-else>
								No pages found
							</div>
						</div>
						<div class="d-block position-relative"
						     v-else>
							<div class="row align-items-center">
								<div class="col-6">
									<div class="d-block position-relative text-xs"
									     v-if="contentData.content.length>0">
										{{ (contentData.currentPage+1) + ' out of ' + contentData.totalPages }}
									</div>
									<div class="d-block position-relative text-xxs"
									     v-if="contentData.content.length>0">
										Pages
									</div>
									<div class="d-block position-relative text-xs"
									     v-else>
										No pages found
									</div>
								</div>
								<div class="col-6">
									<InputNumber class="w-100"
									             inputId="withoutgrouping"
									             :useGrouping="false"
									             input-class="w-100"
									             :min="1"
									             :max="100"
									             v-model="search.size"
									             placeholder="Element"/>
								</div>
							</div>
						</div>
					</div>
					<div class="col-4">
						<Button label="Next"
						        @click="loadPage(true, search)"
						        icon="pi pi-arrow-right"
						        :disabled="contentData.totalPages<=theCurrentPage"
						        iconPos="right"
						        class="no-outline w-100"
						        severity="warn"/>
					</div>
				</div>
			</div>
		</div>
		
		<ListOptioner :options="theRowOptions"
		              :header-title="optionerHeader===null ? 'Options' : optionerHeader"
		              :visible="optionerShown"
		              :row="tmpRow"
		              @action-performed="actionPerformed"
		              @close="closeOptioner"/>
		
		
		<ListOptioner :options="multiRowOptions"
		              header-title="Options"
		              :visible="multiOptionerShown"
		              @action-performed="multiActionPerformed"
		              @close="closeMultipleOptioner"/>
	</div>
</template>


<script>

import {PAGE_MODEL} from "@/utils/DEFAULT_MODELS";
import {areArraysEquals, formatNumber, getDateFromTimeStamp, getDeepObjectValue} from "@/utils/AppFx";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

import Button from "primevue/button";
import Checkbox from "primevue/checkbox";
import DatePicker from 'primevue/datepicker';
import InputNumber from "primevue/inputnumber";
import InputText from "primevue/inputtext";
import Select from "primevue/select";
import MultiSelect from "primevue/multiselect";
import Textarea from "primevue/textarea";
import {generateRandomString, isEmpty} from "@/utils/AppStringUtils";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";
import ListOptioner from "@/dashboard/utils/ListOptioner.vue";
import {getCountries} from "@/dashboard/utils/countries";

export default {
	name: "PaginatedTable",
	emits: ["next", "previous", "navigate", "search", 'options',
		"continueFamilyCreation", "nonInstitutionFamilyMember",
		"multiOptions", "sort", "onChangeModel", "select", "customEvent"],
	components: {
		ListOptioner,
		Button,
		Checkbox,
		DatePicker,
		InputNumber,
		InputText,
		Select,
		MultiSelect,
		Textarea
	},
	data(){
		return {
            handleIsChurch:false,
            hideContent : false,
			tableId: generateRandomString(8),
			pageLocal: this.tableData.currentPage,
			searchLocal: this.searchModel,
			optionerShown: false,
			tmpRow : null,
			
			todayDate : new Date(),
			selectedItems: new Set(),
			multiOptionerShown: false,
			optionerHeader: null,
			chipsLocal: this.chips
		}
	},
	props: {
		rowBaseCode: {
			type: String,
			required: false,
			default(){
				return "";
			}
		},
		multiSelect: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		},
		extraButtons: {
			type: [Array, null],
			required: false,
			default(){
				return null;
			}
		},
		totalAmount: {
			type: [Number, null],
			required: false,
			default(){
				return null
			}
		},
		optionHeaderModel: {
			type: [String, null],
			required: false,
			default(){
				return null
			}
		},
		exportable: {
			type: Boolean,
			required: false,
			default(){
				return false
			}
		},
		paginated: {
			type: Boolean,
			required: false,
			default(){
				return true
			}
		},
		multiRowOptions: {
			type: Array,
			required: false,
			default(){
				return []
			}
		},
		dateSearch: {
			type: Boolean,
			default(){
				return true;
			}
		},
  
		paginatorTitle: {
			type: [String, null],
			required: false,
			default(){ return null; }
		},
		searchModel: {
			type: Object,
			required: false,
			default(){ return {...PAGINATOR_SEARCH_MODEL}; }
		},
		chips: {
			type: Array,
			required: false,
			default(){
				return [];
			}
		},
		tableHeaders:{
			type: Object,
			required: true,
			default(){ return {}; }
		},
		tableData:{
			type: Object,
			required: true,
			default(){ return {...PAGE_MODEL}; }
		},
		rowOptions: {
			type: [Array, null],
			required: false,
			default(){
				return null;
			}
		},
		rowTypeDefinition: {
			type: Object,
			required: false,
			default(){
				return {
					attributed: null,
					backgroundColor: []
				};
			}
		},
		canCreateFamily: {
			type: Boolean,
			default(){
				return false;
			}
		},
		tablePrintable: {
			type: Boolean,
			default(){
				return false;
			}
		},
        isChurch:{
            type:Boolean,
            default(){
                return false;
            }
        },
		hasQueryInput: {
			type: Boolean,
			default(){
				return true;
			}
		},
		extraInputs: {
			type: Array,
			default(){
				return [];
			}
		},
		rowCounter: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		},
		searchQueryPlaceholder: {
			type: [String, null],
			required: false,
			default(){
				return null;
			}
		},
		childrenAttribute:{
			type: [String, null],
			required: false,
			default(){
				return null
			}
		},
		parentAttributeLinker:{
			type: [String, null],
			required: false,
			default(){
				return null
			}
		}
	},
	watch:{
		searchModel: function(newValue){
			this.searchLocal = {...newValue};
		},
		exportTable: function(newValue){
			if(newValue) this.exportAsCSV();
		}
	},
	methods: {
		onChangeValue: function(event){
			this.$emit("onChangeModel", this.search);
		},
		getRowClass: function(row){
			let styleClass = (this.parentAttributeLinker!==null && typeof row[this.parentAttributeLinker]!=='undefined' && row[this.parentAttributeLinker]!==null)
				? "child-row"
				: "";
			styleClass += (typeof row.tag!=='undefined') ? ' row-tagged' : '';
			return styleClass;
		},
		getColumnRows: function(row, columnRows){
			return columnRows.filter(columnRow=>{
				let canInclude = true;
				if(typeof columnRow.showIfNotNull!=="undefined") {
					canInclude = this.getDeepObjectValue(row, columnRow.attribute)!==null;
				}
				return canInclude;
			});
		},
		handleExtraButton: function(button, row){
			if(typeof button.command!=='undefined') {
				this.$emit("options", {method: button.command, row: row});
			}
		},
		sortColumn: function(column){
			let nextSort = this.sortingColumns[column];
			if(typeof this.headers[column]!=="undefined") {
				let row = this.headers[column].rows[0];
				nextSort = (nextSort===-1) ? 1 : -1;
				let content = [...this.contentData.content];
				content.sort((x, y)=>{
					if(row.type==='StringArray'){
						let xString = row.attribute.reduce((accumulator, currentValue)=>{
							return accumulator + ((accumulator==="") ? "" : " ") + x [currentValue];
						}, ""),
							yString = row.attribute.reduce((accumulator, currentValue)=>{
								return accumulator + ((accumulator==="") ? "" : " ") + y [currentValue];
							}, "");
						return xString > yString ? nextSort : 0 - nextSort;
					}
					return x[row.attribute] > y[row.attribute] ? nextSort : 0 - nextSort;
				});
				this.sortingColumns[column] = nextSort;
				this.$emit('sort', {
					...this.contentData,
					content: content
				});
			}
		},
		multiActionPerformed: function(value){
			let objectValue = {method: value, items: this.selectedItems};
			this.closeMultipleOptioner();
			this.$emit("multiOptions", objectValue);
		},
		actOnMultiple: function(){
			this.multiOptionerShown = true;
		},
		closeMultipleOptioner: function(){
			this.multiOptionerShown = false;
		},
		clearSelection: function() {
			this.$store.commit("setLoading", true);
			new Promise((resolve) => {
				this.contentData = {
					...this.contentData,
					content: this.contentData.content.map(item=>{
						return {
							...item,
							isItemSelected : false
						}
					})
				}
				this.$store.commit("setLoading", false);
				resolve(true);
			});
			this.selectedItems = new Set();
		},
		toggleSelectedItem: function(index){
			this.$store.commit("setLoading", true);
			this.$emit("select", this.tableData.content[index])
			this.$store.commit("setLoading", false);
		},
		updateChips: function(){
			if(this.localChips===null) this.localChips = [];
			this.localChips = this.localChips.map(chip=>{
				let extraInput = this.paginatorExtraInputs.find(extraInput=>{
					return extraInput.model===chip.model;
				})
				if(extraInput!==undefined) if(typeof extraInput.options!=="undefined") chip.options = extraInput.options;
				switch(chip.type){
					case "DATE":
						chip.value = this.getDateFromTimeStamp(this.search[chip.model]);
						break;
					
					case "MULTISELECT":
						if(this.search[chip.model]!==null && typeof this.search[chip.model]!=="undefined") {
							chip.value = this.search[chip.model].map(singleValue => {
								return this.getDropdownLabel(singleValue, chip.options);
							});
							chip.value = (chip.value.length === 0) ? null : chip.value.join(", ");
						}
						break;
				}
				return chip;
			}).filter(chip=>{ return chip.value!==null; });
		},
		makeSearch: function(){
			this.updateChips();
			this.$emit('search', this.search);
		},
		canBeShown: function(input){
			let shown = true, changeSetter = null;
			if(input.shownIf) {
				let isPresent = true;
				if(input.shownIf.contains) {
					// input.shownIf.model irimwo zose muri
					let i =0;
					if(input.shownIf.model.length===0){
						shown = false;
					} else {
						for (; i < input.shownIf.values.length; i++) {
							if (!input.shownIf.model.includes(input.shownIf.values[i])) {
								shown = false;
								break;
							}
						}
					}
				} else {
					if(typeof this.search[input.shownIf.model]==='undefined'
						|| this.search[input.shownIf.model]===null){
						shown = false;
					} else {
						shown = false;
						let exactMatch = input.shownIf.exactMatch ?? false;
						if(exactMatch){
							if(typeof this.search[input.shownIf.model]=='object'
								&& typeof input.shownIf.values==='object'){
								if(this.search[input.shownIf.model].length===0) return false;
								shown = areArraysEquals(input.shownIf.values, this.search[input.shownIf.model]);
							}
						} else {
							shown = input.shownIf.values.includes(this.search[input.shownIf.model]);
						}
					}
				}
				
				if(shown && typeof input.shownIf.onShowSet!=="undefined") changeSetter = input.shownIf;
			}
			if(input.hiddenIf){
				if(this.search[input.hiddenIf.model]===null) {
					shown =  false;
				} else {
					if (typeof this.search[input.hiddenIf.model] === "undefined"
						|| this.search[input.hiddenIf.model]===null) {
						shown = false;
					} else {
						shown = false;
						let exactMatch = input.hiddenIf.exactMatch ?? false;
						if(exactMatch){
							if(typeof this.search[input.hiddenIf.model]=='object'
								&& typeof input.hiddenIf.values==='object'){
								if(this.search[input.hiddenIf.model].length===0) return false;
								shown = !areArraysEquals(input.hiddenIf.values, this.search[input.hiddenIf.model]);
							}
						} else {
							shown = !input.hiddenIf.values.includes(this.search[input.hiddenIf.model]);
						}
					}
				}
				
				if(shown && typeof input.hiddenIf.onShowSet!=="undefined") changeSetter = input.hiddenIf;
			}
			if(shown && changeSetter!==null){
				this.search[changeSetter.onShowSet.model] = changeSetter.onShowSet.value;
			}
			return shown;
		},
		getDateFromTimeStamp,
		actionPerformed: function(value){
			let objectValue = {method: value, row: this.tmpRow};
			this.optionerShown = false;
			let isDelete = (value!==null && value.toLocaleLowerCase().search('delete')>=0);
			if(isDelete){
				this.$confirm.require({
					group: 'templating',
					header: 'Confirmation',
					message: 'This action is irreversible. Please confirm to proceed moving forward.',
					icon: 'pi pi-exclamation-circle',
					rejectProps: {
						label: 'Cancel',
						icon: 'pi pi-times',
						severity: 'danger',
						outlined: true,
						size: 'small'
					},
					acceptProps: {
						label: 'Accept',
						icon: 'pi pi-check',
						severity: 'danger',
						size: 'small'
					},
					accept: () => {
						this.$emit("options", objectValue);
					},
					reject: () => {
						this.$toast.add({ severity: 'error', summary: 'Deletion', detail: 'You have rejected the deletion operation.', life: 3000 });
					}
				});
			} else {
				this.$emit("options", objectValue);
			}
		},
		closeOptioner: function(){
			this.optionerShown = false;
			this.tmpRow = null;
			this.optionerHeader = null;
		},
		toggleOptioner: function(row){
			if(this.theRowOptions!==null) {
				let headerTitle = null;
				if(this.optionerHeaderModel !== null){
					if(typeof row[this.optionerHeaderModel]!=='undefined') headerTitle = row[this.optionerHeaderModel].toUpperCase();
				}
				this.optionerHeader = headerTitle;
				this.tmpRow = row;
				this.optionerShown = true;
			}
		},
		getDropdownLabel: function(value, options){
			let option = options.find(singleOption=>{
				return singleOption.value===value;
			});
			return (typeof option==="undefined") ? "---" : option.label;
		},
		finishEditing: function(){
			this.$emit("continueFamilyCreation");
		},
        addNonInstitutionalMember: function(){
            this.$emit("nonInstitutionFamilyMember")
        },
		exportAsCSV: function(){
			let csvString = "";
			this.$store.commit("setLoading", true);
			const oldItemsCount = this.search.size;
			const totalElements = this.tableData.totalElements;
			if(!this.paginated) {
				this.search = {
					...this.search,
					size: totalElements
				};
			}
			setTimeout(()=>{
				const TABLE = document.getElementById(this.tableId);
				if(TABLE){
					let rowSelector = [
						"#"+this.tableId+" tbody tr",
						"#"+this.tableId+" thead tr"
					]
					let dataRows = document.querySelectorAll(rowSelector.join(","));
					dataRows.forEach(singleRow=>{
						// this means we have got all the tr
						let rowString = [];
						let trChildren = singleRow.children;
						let tableColumns = [],
							i = 0, count = trChildren.length;
						for(; i<count; i++){
							let classList = trChildren[i].classList;
							if(!classList.contains("export-ignored")){
								// Column must be put for excel
								let theColumnContent = trChildren[i].innerText;
								if(isEmpty(theColumnContent)) theColumnContent = "";
								theColumnContent = theColumnContent.trim();
								rowString.push(theColumnContent.replace(",", ";"));
							}
						}
						csvString += rowString.join(",")+"\n";
					});
				}
				this.downloadCSV(csvString, this.tableId);
				
				if(!this.paginated){
					this.search = {
						...this.search,
						size: oldItemsCount
					};
				}
				this.$store.commit("setLoading", false);
			}, 5000);
		},
		downloadCSV(csvContent, filename) {
			let csvFile;
			let downloadLink;
			
			// Create a CSV file
			csvFile = new Blob([csvContent], {type: "text/csv"});
			
			// Create a download link
			downloadLink = document.createElement("a");
			
			// Set the download link's file name
			downloadLink.download = filename+".csv";
			
			// Attach the CSV file to the download link
			downloadLink.href = window.URL.createObjectURL(csvFile);
			
			// Hide the download link and click it programmatically
			downloadLink.style.display = "none";
			downloadLink.id = "download_"+filename;
			document.body.appendChild(downloadLink);
			downloadLink.click();
			let newlyCreatedLinkTag = document.getElementById("download_"+filename);
			if(newlyCreatedLinkTag) newlyCreatedLinkTag.remove();
		},
		getDeepObjectValue,
		formatNumber,
		getCountries,
		getRequestTimeParts: function (timestamp){
			let datetimeParts = getDateFromTimeStamp(timestamp, true).split("@");
			return [datetimeParts[0].trim(), datetimeParts[1]]
		},
		getURI: function(url){
			return  url.replace(APP_CONFIG.BACKEND_SERVER, "");
		},
		loadPage: function(next, params = null){
			if(params!==null) this.$emit(next ? 'next' : 'previous', params);
			else this.$emit(next ? 'next' : 'previous');
		},
		itemSelected: function(id){ return this.selectedItems.has(id); },
		executeCommand: function(row, column){
			if(typeof column.command!=="undefined" && column.command!==null){
				this.$emit("options", {method: column.command, row: row});
				return;
			}
			if(typeof column.optionOpener!=="undefined" && column.optionOpener===true){
				this.tmpRow = {...row};
				this.optionerShown = true;
			}
		}
	},
	computed: {
		theCurrentPage: function(){
			return this.contentData.currentPage+1;
		},
		optionerHeaderModel: function(){
			return this.optionHeaderModel;
		},
		makingSelection: function(){
			return this.multiSelect;
		},
		search: {
			get: function(){
				return this.searchLocal;
			}, set: function(newValue){
				let changedSize = false;
				if(this.searchLocal.size===newValue.size){
					console.log("size yahindutse wangu");
				}
				if(newValue!==null) this.searchLocal = {...newValue};
				else this.searchLocal = newValue;
			}
		},
		localChips: {
			get: function(){
				return this.chipsLocal;
			}, set: function(newValue){
				this.chipsLocal = newValue;
			}
		},
		allowedToCreateFamily: function(){
			return this.canCreateFamily;
		},
		headers:function(){
			return this.tableHeaders;
		},
		theRowOptions: function(){
			return this.rowOptions;
		},
		contentData: function(){
			let tableData = {...this.tableData}
			if(this.paginated){
				return {
					...tableData,
					content: tableData.content.reduce((accumulator, currentRow)=>{
						if(typeof currentRow[this.childrenAttribute]!=='undefined' && currentRow[this.childrenAttribute]!==null){
							return [
								...accumulator,
								currentRow,
								...currentRow[this.childrenAttribute]
							];
						}
						return [
							...accumulator,
							currentRow
						];
					}, [])
				};
			} else {
				let size = this.search.size,
					page = this.search.page,
					start = (page-1) * size,
					end = start + size,
					totalPages = parseInt(Math.ceil(tableData.content.length/size));
				tableData = {
					...tableData,
					currentPage: page-1,
					content: tableData.content.filter((item, index)=>{
						return ((start<=index && index<end) || item===null);
					}),
					totalPages: totalPages.toLocaleString("en-IN")
				}
				return tableData;
			}
		},
		totalColSpan: function(){
			let headersCount = Object.keys(this.headers).length;
			if(this.theRowOptions!==null) return headersCount - 1 ;
			return headersCount;
		},
		countriesPhoneCodes: function(){
			return this.getCountries().map(country=>{
				return {
					label: country.dial_code,
					value: parseInt(country.dial_code.replace("+", ""))
				}
			});
		},
		paginatorExtraInputs: function(){
			return this.extraInputs.filter(singleInput=>{
				let output = this.canBeShown(singleInput);
				if(singleInput.thenModelClass){
					// change the class of the actionner
				}
				return output;
			});
		},
		sortingColumns: function(){
			return Object.keys(this.headers).reduce((previousValue, currentValue)=>{
				let array = [];
				array[currentValue] = -1
				return {
					...previousValue,
					...array
				}
			}, {});
		},
		searchedOptions: function(){
			return this.localChips.filter(chip=>{
				if(chip===null) return false;
				if(chip.value===undefined) return false;
				if(typeof chip.value==="undefined") return false;
				return chip.value != null;
			});
		},
		tableHeadStyle: function(){
			if(this.tablePrintable){
				return {
					borderBottom: "1px solid #999"
				}
			}
			return null;
		},
		tableStyle: function(){
			if(this.tablePrintable){
				return {
					color: "#000",
					border: "1px solid #999",
					borderCollapse: "collapse",
					width: '100%'
			}
			}
			return null;
		},
		page: {
			get: function(){
				return this.pageLocal;
			}, set: function(newValue){
				this.pageLocal = newValue;
			}
		}
	},
}
</script>


<style scoped>
	.row-tagged {
		background-color: rgba(253, 13, 13, 0.1);
	}
	
	.paginator-chip-container{
		gap: 10px;
	}
	.paginator-chip{
		height: fit-content !important;
		padding: 5px 10px;
		background-color: #ddd;
	}
</style>