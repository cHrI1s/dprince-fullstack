/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

function getUIDate(timestamp, withTime=false){
    let date =  new Date(timestamp);
    let months = ["January",
        "February",
        "March",
        "April", "May", "June", "July","August", "September", "October", "November", "December" ];

    let month = months[date.getMonth()];
    let fullDate = month+" "+date.getDate()+","+date.getFullYear();

    if(withTime){
        let hour = date.getHours();
        if(hour<10) hour = "0"+hour;

        let minutes = date.getMinutes();
        if(minutes<10) minutes = "0"+minutes;

        if(date.getHours()!==0 && date.getMinutes()!==0) fullDate += " at "+hour+":"+minutes;
    }
    return fullDate;
}


export {
    getUIDate,

}