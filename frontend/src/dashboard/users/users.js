/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

export const ASSISTANT_ADMIN_MODEL = {
    adminCode       : null,
    firstName       : null,
    lastName        : null,
    email           : null,
    username        : null,
    password        : null,
    confirmPassword : null,
    organizationType: null,
}

export const ORGANIZATIONS_ADMINS = [
    "ORGANIZATION_PARTNER",
    "ORGANIZATION_ADMINISTRATOR",
    "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
    "ORGANIZATION_DATA_ENTRY_OPERATOR"
]

export const CHURCH_ADMINS = [
    "CHURCH_ADMINISTRATOR",
    "CHURCH_BRANCH_ADMINISTRATOR",
    "CHURCH_ASSISTANT_ADMINISTRATOR",
    "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
    "CHURCH_DATA_ENTRY_OPERATOR"
]

export const USER_TYPES =  [
    { label: "Organization Partner", value: "ORGANIZATION_PARTNER" },
    { label: "Organization Administrator", value: "ORGANIZATION_ADMINISTRATOR" },
    { label: "Organization Assistant Administrator", value: "ORGANIZATION_ASSISTANT_ADMINISTRATOR" },
    { label: "Organization Data Entry Operator", value: "ORGANIZATION_DATA_ENTRY_OPERATOR" },

    { label: "Church MEMBER", value: "CHURCH_MEMBER" },
    { label: "Church Administrator", value: "CHURCH_ADMINISTRATOR" },
    { label: "Church Branch Administrator", value: "CHURCH_BRANCH_ADMINISTRATOR" },
    { label: "Church Assistant Administrator", value: "CHURCH_ASSISTANT_ADMINISTRATOR" },
    { label: "Church Branch Assistant Administrator", value: "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR" },
    { label: "Church Data Entry Operator", value: "CHURCH_DATA_ENTRY_OPERATOR" },

    { label: "Super Assistant Administrator", value: "SUPER_ASSISTANT_ADMINISTRATOR" },
    { label: "Super Administrator", value: "SUPER_ADMINISTRATOR" },
    { label: "Extra Super ADMIN", value: "EXTRA_SUPER_ADMIN" }
];

export const DATA_ENTRY_OPERATORS = USER_TYPES.filter(userType=>{
    return [
        "CHURCH_DATA_ENTRY_OPERATOR",
        "ORGANIZATION_DATA_ENTRY_OPERATOR"
    ].includes(userType.value)
});