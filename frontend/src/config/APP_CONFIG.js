/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

const BACKEND_SERVER = window.location.hostname;

export const APP_CONFIG = {
    BACKEND_SERVER: "http://"+BACKEND_SERVER+":5000",
    APP_NAME: "DNote",
    DEVELOPER: "alienbase.io",
    DEVELOPER_LINK: "https://alienbase.io",
    FILE_MAX_SIZE: 20000000,
    POPUP_TIMEOUT: 5000,
    MAX_ITEM_PER_PAGE: 2
}