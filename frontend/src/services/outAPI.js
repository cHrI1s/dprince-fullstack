/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

import axios from 'axios';
import Vue from 'vue';

// Remember to st the token for each
const outAPI  = axios.create({
    timeout: 10000,
    /*headers: {
        //'content-type': 'application/json',
        'authorization': 'Bearer '+token
    }*/
});
//outAPI.defaults.headers.post['content-type'] = 'application/json; charset=utf-8';
export default outAPI;
