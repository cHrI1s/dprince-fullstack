/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

export const RECEIPT_DUMMY = {
    id: 485,
    entryDate: "2024-09-13T19:22:33.000+00:00",
    institutionId: 7,
    receiptNo: "24-08-10000",
    donations: [
        {
            id: 486,
            receiptNo: "24-08-10000",
            institution: null,
            member: null,
            contribution: {
                id: 226,
                institutionId: 7,
                name: "Construction",
                creationDate: "2024-08-30"
            },
            paymentMode: "CASH",
            category: null,
            amount: 10000,
            remarks: null,
            referenceNo: null,
            bankReference: null,
            referenceAccount: null,
            subscription: null
        },
        {
            id: 487,
            receiptNo: "24-08-10000",
            institution: null,
            member: null,
            contribution: {
                id: 227,
                institutionId: 7,
                name: "MiFem",
                creationDate: "2024-08-30"
            },
            paymentMode: "CASH",
            category: null,
            amount: 10000,
            remarks: null,
            referenceNo: null,
            bankReference: null,
            referenceAccount: null,
            subscription: null
        },
        {
            id: 488,
            receiptNo: "24-08-10000",
            institution: null,
            member: null,
            contribution: {
                id: 228,
                institutionId: 7,
                name: "NYABIRABA",
                creationDate: "2024-08-30"
            },
            paymentMode: "CASH",
            category: null,
            amount: 5000,
            remarks: null,
            referenceNo: null,
            bankReference: null,
            referenceAccount: null,
            subscription: null
        }
    ],
    member: {
        addressLine1: "9/61, Railway station road, Arasu Colony",
        addressLine2: "EdamalaiPattiPudur",
        addressLine3: "Arasu Colony",
        pincode: 620012,
        district: "Trichy",
        state: "TAMIL NADU",
        country: "BI",
        phone: 7418505116,
        alternatePhone: 7418505116,
        whatsappNumber: 7418505119,
        id: 102,
        institutionId: 7,
        titleId: 37,
        code: "24SJC08000002",
        firstName: "Julienne",
        lastName: "Hagabimana",
        gender: "FEMALE",
        baptized: "BAPTIZED",
        dob: "1968-04-11",
        dom: "1995-12-03",
        email: "julienne-hagabimana@gmail.com",
        subscription: "ANNUAL",
        status: "ACTIVE",
        language: "ENGLISH",
        categoryId: 31,
        creationDate: "2024-08-22",
        familyRole: null,
        baptist: null,
        dateOfBaptism: null,
        churchFunction: "PRIEST",
        deletable: false
},
    institution: {
        receiptTemplate: "TEMPLATE_1",
        id: 7,
        institutionType: "CHURCH",
        categoryId: 35,
        category: {
            id: 35,
                name: "Adventist",
                institutionType: "CHURCH"
            },
        subscription: "ANNUAL",
        subscriptionPlan: 1,
        churchType: null,
        parentInstitutionId: null,
        name: "Mont Sion",
        email: "monsionbujumbura@gmail.com",
        phone: 8122289482,
        address: "9/61 Railway Station Road Arasu Colony",
        logo: null,
        blocked: false,
        creationDate: "2024-08-25",
        deadline: "2025-08-25",
        members: 3,
        maxMembers: 500,
        admins: 1,
        maxAdmins: 0,
        families: 0,
        maxFamilies: null,
        emails: 10,
        maxEmails: 10,
        smses: 10,
        maxSmes: 10,
        whatsapp: 10,
        maxWhatsapp: 10,
        churchBranches: null,
        maxChurchBranches: null,
        plan: {
            id: 1,
            institutionType: "GENERAL",
            name: "Plan 1",
            smses: 10,
            emails: 10,
            whatsapp: 10,
            admins: 0,
            families: 0,
            members: 500,
            churchBranches: null,
            price: 0,
            features: null
        },
        toggleBlockMessage: "<div><div>Dear Church, </div><p>We are writing to inform you that your account on DNote  has been unblocked and is now fully accessible.  </p><p>Best regards,<div style='font-weight:bold;'>DNote Team</div></p></div>"
    }
}