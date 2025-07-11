/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";

export const SUPER_ADMINISTRATORS_ROLES = [
    "EXTRA_SUPER_ADMIN",
    "SUPER_ADMINISTRATOR",
    "SUPER_ASSISTANT_ADMINISTRATOR"
];

export const CHURCH_MAIN_BRANCH_ADMINISTRATORS = [
    "CHURCH_ADMINISTRATOR",
    "CHURCH_ASSISTANT_ADMINISTRATOR"
]

export const ADDRESS_PRINT_GENERATOR = {
    state       : null,
    pincode     : null,
    categories  : null,
    languages   : null,
    duration    : null,
    subscription: null,
    dateFrom    : null,
    dateTo      : null,
    amountFrom  : null,
    amountTo    : null,
    paymentMode : null,
    memberCodes : null
}

export const MEMBERSHIP_DURATIONS = [
    {label: "Year", value: "YEAR"},
    {label: "Last 6 Months", value: "LAST_SIX_MONTH"},
    {label: "Last 3 Months", value: "LAST_THREE_MONTH"},
    {label: "Last 1 Month", value: "LAST_MONTH"},
    {label: "Last 7 Days", value: "LAST_SEVEN_DAYS"},
    {label: "Today", value: "TODAY"},
    {label: "Single Date", value: "SINGLE_DATE"},
    {label: "Custom Date Gap", value: "CUSTOM_DATE_GAP"},
    {label: "All Time", value: "LIFE_TIME"}
];

export const PAYMENT_MODES = [
    {label: "Cash", value: "CASH"},
    {label: "UPI", value: "UPI"},
    {label: "Cheque", value: "CHEQUE"},
    {label: "Online Donation", value: "ONLINE_DONATION"},
    {label: "MO", value: "MO"},
    {label: "DD", value: "DD"},
];

export const MEMBER_BAPTISM_STATUSES = [
    { label: "Not Baptized", value: "UNBAPTIZED"},
    { label: "Baptized", value: "BAPTIZED"},
]

export const PERSON_CONTACT = {
    addressLine1		: null,
    addressLine2		: null,
    addressLine3		: null,
    pincode				: null,
    district			: null,
    area			    : null,
    state				: null,
    country				: "IN",
    phone				: null,
    phoneCode			: 91,
    alternatePhone      : null,
    alternatePhoneCode  : 91,
    landlinePhone       : null,
    landlinePhoneCode   : 91
}

export const FAMILY_ROLES = [
    {label : "Grand Father", value: "GRAND_FATHER"},
    {label : "Father", value: "FATHER"},
    {label : "Husband", value: "HUSBAND"},
    {label : "Father In Law", value: "FATHER_IN_LAW"},
    {label : "Uncle", value: "UNCLE"},
    {label : "Nephew", value: "NEPHEW"},
    {label : "Grand Mother", value: "GRAND_MOTHER"},
    {label : "Mother", value: "MOTHER"},
    {label : "Wife", value: "WIFE"},
    {label : "Mother In Law", value: "MOTHER_IN_LAW"},
    {label : "Aunt", value: "AUNT"},
    {label : "Niece", value: "NIECE"},
    {label : "Daughter", value: "DAUGHTER"},
    {label : "Sister", value: "SISTER"},
    {label : "Daughter In Law", value: "DAUGHTER_IN_LAW"},
    {label : "Son", value: "SON"},
    {label : "Brother", value: "BROTHER"},
    {label : "Son In Law", value: "SON_IN_LAW"},
    {label : "Cousin", value: "COUSIN"},
]

export const GENDERS = [
    {label: "Male", value: "MALE"},
    {label: "Female", value: "FEMALE"},
]
export const FAMILY_CREATION_MODEL = {
    name: null,
    dob : null,
    photo: null,
    transferCertificateEnclosed: null, //states whether the transfer certificate has been enclosed or not,
    familyHead: null,
    ...PERSON_CONTACT
}

export const BIRTHDAY_GAPS = [
    { label: "All", value: "ALL"},
    { label: "Today", value: "TODAY"},
    { label: "Within A Week", value: "WEEK" },
    { label: "Within A Month", value: "MONTH" },
    { label: "Custom Date", value: "CUSTOM" },
    { label: "Custom Date Dap", value: "CUSTOM_GAP" },
]


export const  ADDRESS_PRINTING_SEARCH_MODEL = {
    ...PAGINATOR_SEARCH_MODEL,
    size: 10,
    country: [],
    pincode : null,
    states: [],
    state : null,
    subscription : null,
    duration : "TODAY",
    minAmount : null,
    maxAmount : null,
    paymentMode : null,
    memberCodes : null,
    categories: [],
    phone: null,
    phoneCode : 91,
    alternatePhoneCode : 91,
    alternatePhone: null,
    creditAccountId: null,
    languages: null,
    withReceipt: true,
    active: true
}