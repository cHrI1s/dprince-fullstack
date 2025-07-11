<!------------------------------------------------------------------------------
  - Copyright (c) 2024. AlienBase
  -
  - @project DPrince-Frontend
  - @author AlienBase
  - @copyright AlienBase All rights reserved
  ----------------------------------------------------------------------------->

<template>
    <div class="app-loader">
        <div class="d-flex w-100 h-100 align-items-center justify-content-center">
	        <div class="d-block position-relative text-center">
		        <ProgressSpinner strokeWidth="8"/>
		        <div class="text-white display-3 text-center">
			        Loading...
		        </div>
	        </div>
        </div>
    </div>
</template> 

<script>
    import ProgressSpinner from "primevue/progressspinner";
    export default{
        components: {
            ProgressSpinner
        },
		props: {
			opacity: {
				type: Number,
				default(){
					return 100;
				}
			}
		},
    }
</script>

<style lang="scss">
    .app-loader{
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100vh;
        background-color: rgba(7, 20, 38, .6);
        z-index: 1150;
        overflow: hidden;
    }
</style>