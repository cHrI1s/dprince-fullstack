/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

const BACKEND_SERVER = window.location.hostname;
export const APP_CONFIG = {
    BACKEND_SERVER: process.env.NODE_ENV==="PRODUCTION"
        ? process.env.VUE_APP_API_URL
        : "http://"+BACKEND_SERVER+":9090",
    APP_NAME: "DNote",
    DEVELOPER: "AlienBase",
    DEVELOPER_LINK: "https://alienbase.io",
    FILE_MAX_SIZE: 200*1024*1024,
    POPUP_TIMEOUT: 5000,
    MAX_ITEM_PER_PAGE: 2,
    CSV_DELIMITER: ","
}