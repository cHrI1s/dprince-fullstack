/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

async function captureScreen() {
	let output = {
		mediaStream: null,
		hasError: false,
		error: null
	};
	try {
		output.mediaStream = await navigator.mediaDevices.getDisplayMedia({
			video: true,
			audio: true
		});
	} catch (error){
		output.hasError = true;
		output.error = error;
	}
	return output;
}

export {
	captureScreen
}