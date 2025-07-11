/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

import axios from 'axios';

import store from "@/store";
import {APP_CONFIG} from "@/utils/APP_CONFIG";

// Vue.use(Vuex);

// Remember to st the token for each'

function removeNullValues(object){
    let keys = Object.keys(object);
    keys.forEach(singleKey=>{
        if(object[singleKey]===null) delete object[singleKey];
    });
    return object;
}

// const BASE_URL = "http://151.106.39.202:5000";
const BASE_URL = APP_CONFIG.BACKEND_SERVER;

const api  = axios.create({
    baseURL: BASE_URL,
    timeout: 6000_000,
    // signal: AbortSignal.timeout(6000_000),
    headers: {
        'content-type': 'application/json',
        //'authorization': 'Bearer '+token
    }
});
api.defaults.timeout = 6000_000;
//api.defaults.headers.post['content-type'] = 'application/json; charset=utf-8';
api.defaults.headers.get['content-type'] = 'application/json; charset=utf-8';
if(store.getters.getBearerToken!==null) {
    api.defaults.headers.common['authorization'] = "Bearer " + store.getters.getBearerToken;
}

api.interceptors.request.use(config=>{
    store.commit("setLoading", true);
    if(config.method==="post"){
        if(config.url!=="/files/uploadMultipleFiles") {
            let data = (typeof config.data !== "object")
                ? JSON.parse(config.data)
                : config.data;
            data = removeNullValues(data);
            data = {
                ...data,
                clientTimezone: Intl.DateTimeFormat().resolvedOptions().timeZone
            }

            let datesAttribute = ["saveDate", "entryDate", "creationDate"];
            datesAttribute.forEach(singleDateAttribute=>{
                if(typeof data[singleDateAttribute]!=='undefined'){
                    let theDate = new Date(data[singleDateAttribute]);
                    if(theDate.getHours()===0 && theDate.getMinutes()===0 && theDate.getSeconds()===0 && theDate.getMilliseconds()===0){
                        theDate = new Date(theDate.getTime());
                        data[singleDateAttribute] = theDate;
                    }
                }
            });
            config = {
                ...config,
                data : (typeof config.data !== "object")
                    ? JSON.stringify(data)
                    : {...data}
            };
        }
    }
    return config;
}, (error)=>{
    store.commit("setLoading", false);
    // Do something with request error
    return Promise.reject(error);
});
api.interceptors.response.use((axiosResponse)=>{
    // console.log("out:Done");
    store.commit("setLoading", false);
    return axiosResponse.data;
}, (error)=>{
    store.commit("setLoading", false);
    // Do something with request error
    return Promise.reject(error);
});

api.defaults.onDownloadProgress = function(uploadProgress){
    // console.log(uploadProgress);
};

/*
api.defaults.timeoutErrorMessage = function(){
    return "ehy hey";
}*/
api.defaults.timeoutErrorMessage = 'timeout';
export default api;
