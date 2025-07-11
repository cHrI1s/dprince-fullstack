<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
	<FileUpload name="file"
                @uploader="uploader"
	            :disabled="isDisabled"
	            :custom-upload="true"
	            :multiple="multipleUpload"
	            :accept="acceptedFileTypes"
	            :maxFileSize="maxFileSize">
		<template #header="{ chooseCallback, uploadCallback, clearCallback, files }">
			<div class="w-100 d-block position-relative">
				<div class="flex">
					<Button @click="chooseCallback()"
					        class="fw-bolder p-3 me-3"
					        icon="pi pi-images p-3 fw-bolder"
					        title="Choose Image(s)"
					        rounded
					        outlined
					        severity="warning"/>
					<Button @click="uploadEvent(uploadCallback)"
					        class="fw-bolder p-3 me-3"
					        icon="pi pi-cloud-upload p-3 fw-bolder"
					        title="Upload Image(s)"
					        rounded
					        outlined
					        severity="success"
					        :disabled="!files || files.length === 0"/>
					<Button @click="clearCallback()"
					        class="fw-bolder p-3"
					        icon="pi pi-times p-3 fw-bolder"
					        rounded
					        outlined
					        severity="danger"
					        :disabled="!files || files.length === 0"/>
				</div>
			</div>
		</template>
		
		<template #content="{messages, progress, files, removeFileCallback, uploadedFiles}">
			<div class="d-block position-relative mb-4"
			     v-if="progress!==null">
				<ProgressBar :value="progress" style="height: 8px"></ProgressBar>
			</div>
			
			<div class="d-block position-relative mb-4"
			     v-if="files.length>0">
				<div class="fw-bolder mb-4">Files to Upload</div>
				<div class="d-block position-relative">
					<div class="d-block"
					     v-for="(singleFile, idx) in files"
					     :key="'file_index'+idx">
						<div class="row py-3 px-2 border border-light bg-light rounded-3"
						     :class="idx>0 ? 'mt-4': ''">
							<div class="col-md-3">
								<div class="d-block position-relative w-100"
								     v-if="isImage(singleFile)">
									<img :src="getImageBlob(singleFile)"
									     :alt="singleFile.name"
									     class="w-100 d-block position-relative"/>
								</div>
								<div class="d-block position-relative w-100"
								     v-else>
									<img src="/folder.png"
									     alt="File placeholder"
									     class="w-100 d-block position-relative"/>
								</div>
							</div>
							<div class="col-md-6 px-0">
								<div class="d-block fw-bold">Name: {{ singleFile.name }}</div>
								<div class="d-block text-sm">File Size: {{ getFileSize(singleFile.size) }}</div>
								<div class="d-block text-sm">File Type: {{ getHumanReadableExtensionFromMime(singleFile.type) }}</div>
							</div>
							<div class="col-md-3">
								<div class="w-100 p-3 bg-danger text-white rounded-3 text-center cursor-pointer"
								     title="Remove this file"
								     @click="removeFileCallback(idx)">
									<i class="pi pi-trash fw-bolder text-white"/>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div class="d-block position-relative mb-4"
			     v-if="uploadedFiles.length>0">
				<div class="fw-bolder mb-4">Uploaded Files</div>
				<div class="d-block position-relative">
					<div class="d-block"
					     v-for="(singleFile, idx) in uploadedFiles"
					     :key="'file_index'+idx">
						<div class="row py-3 px-2 border border-light bg-light rounded-3"
						     :class="idx>0 ? 'mt-4': ''">
							<div class="col-md-3">
								<div class="d-block position-relative w-100"
								     v-if="isImage(singleFile)">
									<img :src="getImageBlob(singleFile)"
									     :alt="singleFile.name"
									     class="w-100 d-block position-relative"/>
								</div>
								<div class="d-block position-relative w-100"
								     v-else>
									<img src="/folder.png"
									     alt="File placeholder"
									     class="w-100 d-block position-relative"/>
								</div>
							</div>
							<div class="col-md-9 px-0">
								<div class="d-block fw-bold">Name: {{ singleFile.name }}</div>
								<div class="d-block text-sm">File Size: {{ getFileSize(singleFile.size) }}</div>
								<div class="d-block text-sm">File Type: {{ getHumanReadableExtensionFromMime(singleFile.type) }}</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div class="d-block position-relative"
			     v-for="(message, index) in messages"
			     :key="'message_'+index">
				{{ message }}
			</div>
		</template>
		
		<template #empty>
			<div>
				<h4 class="mt-3">
					Drag and drop files to here to upload.
				</h4>
			</div>
		</template>
	</FileUpload>
</template>

<script>
import FileUpload from "primevue/fileupload";
import Button from "primevue/button";
import ProgressBar from "primevue/progressbar";
import {getFileSize, getHumanReadableExtensionFromMime} from "@/utils/APP_FILE_UTILS";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

export default {
	name: "MyFileUploader",
	emits: ["uploaded"],
	components : {
		FileUpload,
		Button,
		ProgressBar
	},
	computed: {
		maxFileSize : function(){
			if(this.maximumFileSize===null) return APP_CONFIG.FILE_MAX_SIZE;
			return this.maximumFileSize;
		},
		isDisabled: function(){
			return this.disabled;
		}
	},
	data(){
		return {
			totalSize: 0,
			totalSizePercent: 0,
			
			multipleUpload : this.multiple,
		}
	},
	methods: {
		isImage: function(singleFile){
			return ['jpg','jpg','png','gif', "jpeg"].includes(this.getHumanReadableExtensionFromMime(singleFile.type));
		},
		getImageBlob: function(singleFile){
			const blob = new Blob([singleFile], {type: singleFile.type});
			return URL.createObjectURL(blob);
		},
		getFileSize,
		getHumanReadableExtensionFromMime,
		onRemoveTemplatingFile(file, removeFileCallback, index) {
			removeFileCallback(index);
			this.totalSize -= parseInt(this.formatSize(file.size));
			this.totalSizePercent = this.totalSize / 10;
		},
		onClearTemplatingUpload(clear) {
			clear();
			this.totalSize = 0;
			this.totalSizePercent = 0;
		},
		onSelectedFiles(event) {
			this.files = event.files;
			this.files.forEach((file) => {
				this.totalSize += parseInt(this.formatSize(file.size));
			});
		},
        uploader: function(e){
	        let formData = new FormData();
            e.files.forEach(singleFile=>{ formData.set("files", singleFile); });
	        
	        this.$api.post("/files/uploadMultipleFiles", formData,{
				headers: {
					"Content-Type" : "multipart/form-data"
				}
	        }).then(response=>{
		        this.$emit("uploaded", this.multipleUpload ? response : response[0]);
				this.$root['showAlert']('success', "File(s) Upload", "Uploaded!");
		        // Display a message to acknowledge the upload was successful
	        }).catch(error=>{
		        this.$root['handleApiError'](error, "File Upload");
	        });
        },
		uploadEvent(callback) {
			this.totalSizePercent = this.totalSize / 10;
			callback();
		},
		onTemplatedUpload() {
			this.$root['showAlert']('info', "File(s) Upload", "Uploaded!");
		},
		formatSize(bytes) {
			const k = 1024,
				dm = 3,
				sizes = this.$primevue.config.locale.fileSizeTypes;
			
			if (bytes === 0) return `0 ${sizes[0]}`;
			
			const i = Math.floor(Math.log(bytes) / Math.log(k)),
				formattedSize = parseFloat((bytes / Math.pow(k, i)).toFixed(dm));
			
			return `${formattedSize} ${sizes[i]}`;
		}
	},
	watch: {
		multiple: function(newValue){
			this.multipleUpload = newValue;
		}
	},
	props: {
		multiple: {
			type: Boolean,
			required: false,
			default(){
				return true;
			}
		},
		disabled: {
			type: Boolean,
			required: false,
			default(){
				return false;
			}
		},
		maximumFileSize: {
			type: Number,
			required: false,
			default(){
				return null;
			}
		},
		acceptedFileTypes: {
			type: String,
			required: false,
			default(){
				return "image/*";
			}
		}
	},
}
</script>

<style lang="scss">
</style>