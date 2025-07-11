/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

import { createApp } from "vue";
import App from "./App.vue";
import store from "./store";
import router from "./router";
import "./assets/css/nucleo-icons.css";
import "./assets/css/nucleo-svg.css";
import ChrisDashboard from "./chris-dashboard";

import PrimeVue from "primevue/config";
import Lara from '@primevue/themes/lara';
import ToastService from "primevue/toastservice";
import ConfirmationService from "primevue/confirmationservice";

// Axios
import api from "@/services/api";

// Font awesome
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons';
library.add(fas);

import { far } from '@fortawesome/free-regular-svg-icons';
library.add(far);
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { dom } from "@fortawesome/fontawesome-svg-core";
dom.watch();

const appInstance = createApp(App);
appInstance.use(store);
appInstance.use(router);
appInstance.use(ChrisDashboard);
appInstance.config.globalProperties.$api = api;
appInstance.use(PrimeVue, {
    theme: {
        preset: Lara,
        options: {
            prefix: 'p',
            darkModeSelector: '.my-app-dark',
            cssLayer: false
        }
    },
});
appInstance.use(ToastService);
appInstance.use(ConfirmationService);
appInstance.component('font-awesome-icon', FontAwesomeIcon)
appInstance.mount("#app");
