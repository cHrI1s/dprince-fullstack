/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

export const  TEMPLATE_MODEL = {
    name: null,
    templateStyle: null,
    communicationWays: null,
    emailText  : null,
    smsText  : null,
    whatsappTemplate: null,
    bibleVerse : null,
    language: "ENGLISH"
}

export const  TEMPLATE_STYLE = [
    { label: "SMS: Receipt", value: "SMS_RECEIPT" },
    { label: "SMS: Promo", value: "SMS_PROMO" },
    { label: "SMS: Other", value: "SMS_OTHER" },
    { label: "Email: Receipt", value: "EMAIL_RECEIPT" },
    { label: "Email: Other", value: "EMAIL_OTHER" },
    { label: "Whatsapp: Receipt", value: "WHATSAPP_RECEIPT" },
    { label: "Whatsapp: Other", value: "WHATSAPP_OTHER" }
]

export const MODE_COMMUNICATIONS = [
    {label: "No Way Selected" , value: null},
    {label:"Sms" , value:"SMS"},
    {label:"Mail", value:"MAIL"},
    {label:"Whatsapp", value:"WHATSAPP"}
]

export const WHATSAPP_LANGUAGES = [
    "BENGALI",
    "ENGLISH",
    "GUJARATI",
    "HINDI",
    "KANNADA",
    "MALAYALAM",
    "MARATHI",
    "PUNJABI",
    "TAMIL",
    "TELUGU",
    "URDU"
]

