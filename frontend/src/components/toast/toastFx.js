/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

function getToastIcon(message){
    let icon = "pi-check-circle"
    switch(message.severity){
        case "info":
            icon = "pi-exclamation-circle";
            break;

        case "error":
        case "danger":
            icon = "pi-times-circle";
            break;

        case "warn":
            icon = "pi-exclamation-triangle";
            break;

        case "success":
            icon = "pi-check-circle";
            break;
    }
    return icon;
}
export {
    getToastIcon
}
