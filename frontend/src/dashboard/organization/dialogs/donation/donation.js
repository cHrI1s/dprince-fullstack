/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/
import {SUBSCRIPTION_PLANS} from "@/dashboard/organization/Organization";


export const CATEGORY_IN_DONATION = {
    categoryId      : null,
    contributionId  : null,
    amount          : null,
    subscription    : null,
}

export const DONATION_MODEL = {
    paymentMode     : null,
    category        : null,
    amount          : null,
    remarks         : null,
    referenceNo     : null,
    bankReference   : null,
    referenceAccount: null,
    entryDate       : null,
    creditAccount   : null,
    donations       : [{...CATEGORY_IN_DONATION}],
};

export const PAYMENT_MODES = [
    { label: "Cash", value: "CASH"},
    { label: "UPI", value: "UPI"},
    { label: "MO", value: "MO"},
    { label: "Cheque", value: "CHEQUE"},
    { label: "DD", value: "DD"},
    { label: "Online Donation", value: "ONLINE_DONATION"},
]