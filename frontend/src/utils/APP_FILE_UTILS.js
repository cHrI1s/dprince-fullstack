/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

function getFileIcon(fileType){
	let icon = "pi pi-image";
	switch (fileType){
		case "PDF_FILE":
			icon = "pi pi-file-pdf";
			break;
			
		case "IMAGE":
			icon = "pi pi-image";
			break;
	}
	return icon;
}

function getFileType(fileTypeGroup){
	let type = "pi pi-image";
	switch (fileTypeGroup){
		case "PDF_FILE":
			type = "Pdf";
			break;
		
		case "IMAGE":
			type = "image";
			break;
	}
	return type;
}

function getFileSize(bytes, dp=1) {
	const thresh = 1024;
	
	if (Math.abs(bytes) < thresh) {
		return bytes + ' B';
	}
	
	const units = ['KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
	let u = -1;
	const r = 10**dp;
	
	do {
		bytes /= thresh;
		++u;
	} while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);
	
	return bytes.toFixed(dp) + ' ' + units[u];
}

function getHumanReadableExtensionFromMime(mime) {
	const mimeToExtensionMap = {
		'application/pdf': 'pdf',
		'application/msword': 'doc',
		'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
		'application/vnd.ms-excel': 'xls',
		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
		'application/vnd.ms-powerpoint': 'ppt',
		'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
		'image/jpeg': 'jpg',
		'image/jpg': 'jpg',
		'image/png': 'png',
		'image/gif': 'gif',
		'text/plain': 'txt',
		'text/csv': 'csv',
		'text/html': 'html'
	};

	const extension = mimeToExtensionMap[mime];
	return extension || 'unknown';
}

export { getFileIcon, getFileSize, getFileType, getHumanReadableExtensionFromMime };