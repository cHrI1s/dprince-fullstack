/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/
import {PERSON_CONTACT} from "@/dashboard/members/members";
import {PAGINATOR_SEARCH_MODEL} from "@/dashboard/utils/paginator/paginatedTable";

export const ORGANIZATION_SEARCH_MODEL = {
	...PAGINATOR_SEARCH_MODEL,
	blockSelector : "ALL",
	phoneCode: 91,
	phone: null,
}

export const ORGANIZATION_MODEL = {
	institutionType			: null,
    categoryId				: null,
    name        			: null,
    subscription			: null,
    subscriptionPlan		: null,
   	logo					: null,
   	address					: null,
	baseCode				: null,
	phoneCode				: 91,
	phone					: null,
	defaultPassword			: null,
	website					: null,
	reportingEmail			: null
}

export const CATEGORY_MODEL = {
	name            :  null,
}

export const ORGANIZATION_TYPES = [
	{label : "General Member", value: "GENERAL"},
	{label : "Church", value: "CHURCH"}
];


export const SUBSCRIPTION_PLANS = [
	{label : "Weekly", value: "WEEKLY"},
	{label : "Monthly", value: "MONTHLY"},
	{label : "Quarter", value: "QUARTER"},
	{label : "Semestral", value: "SEMESTRAL"},
	{label : "Annual", value: "ANNUAL"},
	{label : "Lifetime", value: "LIFETIME"}
];

export const AVAILABLE_LANGUAGES = [
	{label: "Assamese", value: "ASSAMESE" },
	{label: "Bengali", value: "BENGALI" },
	{label: "Bodo", value: "BODO" },
	{label: "Dogri", value: "DOGRI" },
	{label: "English", value: "ENGLISH" },
	{label: "Gujarati", value: "GUJARATI" },
	{label: "Hindi", value: "HINDI" },
	{label: "Kannada", value: "KANNADA" },
	{label: "Kashmiri", value: "KASHMIRI" },
	{label: "Konkani", value: "KONKANI" },
	{label: "Malayalam", value: "MALAYALAM" },
	{label: "Maithili", value: "MAITHILI" },
	{label: "Manipuri", value: "MANIPURI" },
	{label: "Marathi", value: "MARATHI" },
	{label: "Nepali", value: "NEPALI" },
	{label: "Oriya", value: "ORIYA" },
	{label: "Punjabi", value: "PUNJABI" },
	{label: "Sanskrit",  value: "SANSKRIT" },
	{label: "Santali",  value: "SANTALI" },
	{label: "Sindhi",  value: "SINDHI" },
	{label: "Tamil",  value: "TAMIL" },
	{label: "Telugu",  value: "TELUGU" },
	{label: "Urdu",  value: "URDU" }


];

export const MEMBER_STATUS = [
	{label : "Active", value: "ACTIVE"},
	{label : "Not Active", value: "INACTIVE"},
];

export const MEMBER_SEARCH_MODEL = {
	...PAGINATOR_SEARCH_MODEL,
	countries: null,
	creationDateIn: "TODAY",
	birthdayDateFrom: null,
	birthdayDateTo: null,
	phone : null,
	phoneCode : 91,
	alternatePhone : null,
	alternatePhoneCode : 91,
	district : null,
	pincode : null,
	partnerCodes: null,
	active: null
}

export const INDIA_STATES = [
	{ label: "Andhra Pradesh", value:"ANDHRA PRADESH" },
	{ label: "Arunachal Pradesh", value:"ARUNACHAL PRADESH" },
	{ label: "Assam", value: "ASSAM"},
	{ label: "Bihar", value:"BIHAR" },
	{ label: "Chhattisgarh", value: "CHHATTISGARH"},
	{ label: "Goa", value:"GOA" },
	{ label: "Gujarat", value:"GUJARAT" },
	{ label: "Haryana", value:"HARYANA" },
	{ label: "Himachal Pradesh", value: "HIMACHAL PRADESH"},
	{ label: "Jharkhand", value:"JHARKHAND"  },
	{ label: "Karnataka", value: "KARNATAKA" },
	{ label: "Kerala", value: "KERALA" },
	{ label: "Madhya Pradesh", value:"MADHYA PRADESH"  },
	{ label: "Maharashtra", value:"MAHARASHTRA"  },
	{ label: "Manipur", value: "MANIPUR" },
	{ label: "Meghalaya", value: "MEGHALAYA" },
	{ label: "Mizoram", value: "MIZORAM" },
	{ label: "Nagaland", value:  "NAGALAND"},
	{ label: "Odisha", value: "ODISHA" },
	{ label: "Punjab", value: "PUNJAB" },
	{ label: "Rajasthan", value: "RAJASTHAN" },
	{ label: "Sikkim", value: "SIKKIM" },
	{ label: "Tamil Nadu", value: "TAMIL NADU" },
	{ label: "Telangana", value: "TELANGANA" },
	{ label: "Tripura", value: "TRIPURA"},
	{ label: "Uttar Pradesh", value: "UTTAR PRADESH" },
	{ label: "Uttarakhand", value: "UTTARAKHAND" },
	{ label: "West Bengal", value: "WEST BENGAL" }
];


export const COMMUNION = [
	{label:"Yes", value:true},
	{label:"No", value:false},
]
export const PARTNER_SUBSCRIPTIONS = [
	{ label : "Annual", value : "ANNUAL" },
	{ label : "Lifetime", value : "LIFETIME" }
]

export const PARTNER_MODEL = {
	  id                  : null,
	titleId				: null,
	firstName			: null,
	lastName			: null,
	gender				: null,
	baptized			: null,
	profile				: null,
	dateOfBaptism       : null,
	dob					: null,
	dom					: null,
	domain				: null,

	...PERSON_CONTACT,

	whatsappNumber		: null,
	whatsappNumberCode	: 91,
	status				: "ACTIVE",
	email				: null,
	subscription		: null,
	categoriesIds		: [],
	language			: "TAMIL",
	statusbar			: null,
	creationDate		: null,
	updateDate			: null,
	newPinCode			: false,
	communion           : null,
	communionDate       : null,
	familyHead			: false,
	familyRole			: null
}

export const DEADLINE_TYPES = [
	{label: "All", value: "ALL"},
	{label: "Not Expiring", value: "NOT_EXPIRING"},
	{label: "Expiring Soon", value: "EXPIRING_SOON"},
	{label: "Expired", value: "EXPIRED"},
]



export const BLOCKED_STATUSES = [
	{ label : "All", value  : "ALL" },
	{ label : "Blocked", value  : "BLOCKED" },
	{ label : "Not blocked", value  : "NOT_BLOCKED" },
]
export const SUBSCRIPTION_MODEL = {
	emails    			 	: 0,
	members    			 	: 0,
	name   				 	: null,
	institutionType         :null,
	smses    			    : 0,
    whatsapp    	 		: 0,
	price    		 		: 0,
	admins           		: 0,
	families 		 		:0,
	churchBranches   		: 0,
	features    	 		: []
}
