/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

import {createRouter, createWebHistory} from "vue-router";
import Dashboard from "../views/Dashboard.vue";
import SignIn from "../auth/SignIn.vue";
import OrganizationDashboard from "@/dashboard/organization/OrganizationDashboard.vue";
import Category from "@/dashboard/configuration/categories/Category.vue";
import AddressPrinting from "@/dashboard/members/AddressPrinting.vue";
import SuperAdministrators from "@/dashboard/administrators/SuperAdministrators.vue";
import ChurchDashboard from "@/dashboard/church/ChurchDashboard.vue";
import ListOrganizations from "@/dashboard/organization/ListOrganizations.vue";
import ImportMembers from "@/dashboard/organization/ImportMembers.vue";
import Title from "@/dashboard/configuration/title/Title.vue";
import OrganizationCreator from "@/dashboard/organization/OrganizationCreator.vue";
import ChurchContributionBoard from "@/dashboard/church/contributions/ChurchContributionBoard.vue";
import ChurchCommunication from "@/dashboard/communication/ChurchCommunication.vue";
import CommunicationGroup from "@/dashboard/group/CommunicationGroup.vue";
import UsersList from "@/dashboard/users/UsersList.vue";
import AssistantAdminDashboard from "@/dashboard/users/AssistantAdminDashboard.vue";
import SuperAdministratorProfile from "@/dashboard/administrators/SuperAdministratorProfile.vue";
import OrganizationMemberDashboard from "@/dashboard/organization/OrganizationMemberDashboard.vue";
import InstitutionFamily from "@/dashboard/organization/families/InstitutionFamily.vue";
import ChurchEvent from "@/dashboard/church/ChurchEventBoard.vue";
import ChurchEventBoard from "@/dashboard/church/ChurchEventBoard.vue";
import ListEvent from "@/dashboard/church/ListEvent.vue";
import ReceiptGenerator from "@/dashboard/organization/ReceiptGenerator.vue";
import Baptism from "@/dashboard/church/church-activities/Baptism.vue";
import PrintBaptismCertificate from "@/dashboard/church/church-activities/PrintBaptismCertificate.vue";
import PrintMembershipCertificate from "@/dashboard/organization/PrintMembershipCertificate.vue";
import NotificationsViewer from "@/dashboard/notifications/NotificationsViewer.vue";
import SubscriptionBoard from "@/dashboard/configuration/subscription-plan/SubscriptionBoard.vue";
import CommunicationTemplate from "@/dashboard/communication/CommunicationTemplate.vue";
import Receipt from "@/dashboard/configuration/receipt/Receipt.vue";

import store from "@/store";
import GroupBoard from "@/dashboard/group/GroupBoard.vue";
import SessionCleaner from "@/app-core/SessionCleaner.vue";
import SchedulerBoard from "@/dashboard/scheduler/SchedulerBoard.vue";
import CertificateImporter from "@/dashboard/certificate/CertificateImporter.vue";
import MailerSettings from "@/dashboard/MailerSettings.vue";
import BdAnniversariesBoard from "@/dashboard/church/birthdays/BdAnniversariesBoard.vue";
import SignatureBoard from "@/dashboard/signatures/SignatureBoard.vue";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";
import AppHealth from "@/dashboard/health/AppHealth.vue";
import IpSetter from "@/dashboard/organization/IpSetter.vue";
import TermsConditions from "@/views/TermsConditions.vue";


const routes = [
    {
        path: "/",
        name: "/",
        redirect: "/dashboard",
    },
    {
        path: "/dashboard",
        name: "Dashboard",
        component: Dashboard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
        },
    },
    {
        path: "/health",
        name: "Application Health",
        component: AppHealth,
        meta: {
            dashboard: true,
            userTypes : [
                "SUPER_ADMINISTRATOR",
            ],
        },
    },
    {
        path: "/notifications",
        name: "Notifications",
        component: NotificationsViewer,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR"
            ],
        },
    },
    {
        path: "/organizations",
        name: "Organizations",
        component: OrganizationDashboard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR"
            ],
        },
    },
    {
        path: "/organizations/mailer-settings",
        name: "Organization Mailer",
        component: MailerSettings,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
            feature: "CUSTOM_MAILER"
        },
        props: {
            isChurch: false,
            headerTitle:"Mailer Settings"
        }
    },
    {
        path: "/organizations/create-new",
        name: "Create new organization",
        component: OrganizationCreator,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: {
            isChurch: false,
            headerTitle:"Create Organization"
        }
    },
    {
        path: "/organizations/families-board/:tab",
        name: "Org. Fam. Board",
        component: InstitutionFamily,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/organizations/assistant-admin-dashboard/:tab",
        name: "Organization Assistant Admins",
        component: AssistantAdminDashboard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR"
            ],
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/organizations/list",
        name: "List of Organizations",
        component: ListOrganizations,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: {
            isChurch: false
        }
    },
    {
        path: "/organizations/partners-dashboard/:tab",
        name: "New Partner",
        component: OrganizationMemberDashboard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/organizations/import-data",
        name: "Org Data Partner",
        component: ImportMembers,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
            feature : "IMPORT_EXISTING_PARTNER"
        },
        props: {
            isChurch: false
        }
    },
    {
        path: "/organizations/certificate-data",
        name: "Organization Certificates",
        component: CertificateImporter,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
            feature : "CERTIFICATE"
        },
        props: {
            isChurch: false
        }
    },
    {
        path: "/organizations/address-printing",
        name: "Organization Address Printing",
        component: AddressPrinting,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: {
            isChurch: false,
            pageType: "ADDRESS"
        }
    },
    {
        path: "/organizations/report-generation",
        name: "Organization Report",
        component: AddressPrinting,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            pageType: "REPORT",
            isChurch: false,
        }
    },
    {
        path: "/organizations/receipt-generation",
        name: "Org. Receipt Generator",
        component: ReceiptGenerator,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
        },
        props: {
            isChurch: false,
            pageType: "REPORT"
        }
    },
    {
        path: "/organizations/categories",
        name: "Categories",
        component: Category,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: false
        }
    },
    {
        path: "/organizations/configuration/receipt",
        name: "Org. Receipt",
        component: Receipt,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR", "ORGANIZATION_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"CUSTOM_ORGANIZATION_LOGO"
        },
        props: (route)=>({
            isChurch: false
        })
    },
    {
        path: "/organizations/birthdays/:tab",
        name: "Org. Birthdays",
        component: BdAnniversariesBoard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR", "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/organizations/scheduler",
        name: "Org. Backup",
        component: SchedulerBoard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
            // feature:"SCHEDULER"
        },
        props: (route)=>({
            isChurch: false
        })
    },






    {
        path: "/profile",
        name: "Profile",
        component: SuperAdministratorProfile,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes: [
                "ORGANIZATION_PARTNER",
                "ORGANIZATION_ADMINISTRATOR",
                "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_DATA_ENTRY_OPERATOR",

                "CHURCH_MEMBER",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",

                "SUPER_ASSISTANT_ADMINISTRATOR",
                "SUPER_ADMINISTRATOR",
            ]
        },
    },
    {
        path: "/ip-setter",
        name: "IP Address Setter",
        component: IpSetter,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes: [
                "ORGANIZATION_ADMINISTRATOR",

                "CHURCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",

                "SUPER_ASSISTANT_ADMINISTRATOR",
                "SUPER_ADMINISTRATOR",
            ]
        },
    },
    {
        path: "/title",
        name: "Title",
        component: Title,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR"
            ],
        },
    },
    {
        path: "/super-admins",
        name: "Super Administrators",
        component: SuperAdministrators,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR",
            ],
        },
    },

    {
        path: "/church/categories",
        name: "Church Types",
        component: Category,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church/dashboard",
        name: "Church Dashboard",
        component: ChurchDashboard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church/create",
        name: "Church Creation",
        component: OrganizationCreator,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            headerTitle: "Church Creation",
            isChurch: true,
        }
    },
    {
        path: "/church/list",
        name: "List of Churches",
        component: ListOrganizations,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church/mailer-settings",
        name: "Church Mailer",
        component: MailerSettings,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
            feature:"CUSTOM_MAILER"
        },
        props: {
            isChurch: true,
            headerTitle:"Mailer Settings"
        }
    },
    {
        path: "/church-member/baptism",
        name: "Member Baptism",
        component: Baptism,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church-member/baptism-certificate",
        name: "Baptism Certificate",
        component: PrintBaptismCertificate,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"CERTIFICATE"
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/institution/membership-certificate",
        name: "Membership Certificate",
        component: PrintMembershipCertificate,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"CERTIFICATE"
        },
        props: {
            isChurch: true
        }
    },


    {
        path: "/church/event-board/:tab",
        name: "Event Board",
        component: ChurchEventBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },


    {
        path: "/church/communication/template/:tab",
        name: "Template Board",
        component: CommunicationTemplate,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",
            ],
            feature:"COMMUNICATION"
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },




    {
        path: "/church/members-dashboard/:tab",
        name: "Add Member",
        component: OrganizationMemberDashboard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",

                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/group/:tab",
        name: "Groups",
        component: GroupBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",

                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },

    {
        path: "/church/families-board/:tab",
        name: "Church Families Board",
        component: InstitutionFamily,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",

                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/assistant-admin-dashboard/:tab",
        name: "Ass. Admins",
        component: AssistantAdminDashboard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/event-board",
        name: "Event",
        component: ChurchEventBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : ["SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",

                'CHURCH_DATA_ENTRY_OPERATOR'
            ],
        },
    },
    {
        path: "/church/create-event/:tab",
        name: "Create Event",
        component: ChurchEvent,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : ["SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",

                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/list-event/:tab",
        name: "List Event",
        component: ListEvent,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : ["SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR"],
        },
        props: (route)=>({
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/subscription/:tab",
        name: "Offerings",
        component: ChurchContributionBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: (route)=>({
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },



    {
        path: "/organizations/communication",
        name: "Org. Communication",
        component: ChurchCommunication,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR", "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_BRANCH_ADMINISTRATOR", "ORGANIZATION_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"COMMUNICATION"
        },
        props: (route)=>({
            isChurch: false
        })
    },
    {
        path: "/organizations/group/:tab",
        name: "Org. Groups",
        component: GroupBoard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR", "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_BRANCH_ADMINISTRATOR", "ORGANIZATION_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature: "MEMBER_GROUPS"
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/organizations/communication/template/:tab",
        name: "Org. Template Board",
        component: CommunicationTemplate,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_ADMINISTRATOR", "ORGANIZATION_ASSISTANT_ADMINISTRATOR",
                "ORGANIZATION_BRANCH_ADMINISTRATOR", "ORGANIZATION_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"COMMUNICATION"
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/communication",
        name: "Church Communication",
        component: ChurchCommunication,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"COMMUNICATION"
        },
        props: (route)=>({
            isChurch: true,
        })
    },
    {
        path: "/communication/group",
        name: "Group",
        component: CommunicationGroup,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
            feature:"COMMUNICATION"
        },
    },
    {
        path: "/church/communication/template/:tab",
        name: "Template Board",
        component: CommunicationTemplate,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",
            ],
            feature:"COMMUNICATION"
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },
    {
        path: "/church/signatures",
        name: "Church Signatures",
        component: SignatureBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR",
                "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR",
                "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR",
            ],
            feature:"CERTIFICATE"
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },


    {
        path: "/configuration/subscription-plan-board/:tab",
        name: "Subscription Plan",
        component: SubscriptionBoard,
        meta: {
            dashboard: true,
            institutionType : "GENERAL",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
        },
        props: (route)=>({
            isChurch: false,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })


    },


    {
        path: "/church/configuration/receipt",
        name: "Church Receipt",
        component: Receipt,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: (route)=>({
            isChurch: true
        })
    },



    {
        path: "/church/address-printing",
        name: "Church Address Printing",
        component: AddressPrinting,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: true,
            pageType: "ADDRESS"
        }
    },
    {
        path: "/users-list",
        name: "List",
        component: UsersList,
        meta: {
            dashboard: true,
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
    },
    {
        path: "/church/report-generation",
        name: "Church Report",
        component: AddressPrinting,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR"
            ],
        },
        props: {
            isChurch: true,
            pageType: "REPORT"
        }
    },
    {
        path: "/church/receipt-generation",
        name: "Church Receipt Generator",
        component: ReceiptGenerator,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR",
                "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: {
            isChurch: true,
        }
    },
    {
        path: "/church/birthdays/:tab",
        name: "Church Birthdays",
        component: BdAnniversariesBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
        },
        props: (route)=>({
            isChurch: true,
            tab: (typeof route.params.tab!=="undefined") ? parseInt(route.params.tab) : 0
        })
    },

    {
        path: "/church/import-data",
        name: "Church Data Import",
        component: ImportMembers,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
            feature : "IMPORT_EXISTING_PARTNER"
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church/certificate-data",
        name: "Church Certificates",
        component: CertificateImporter,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
                "CHURCH_ADMINISTRATOR", "CHURCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_BRANCH_ADMINISTRATOR", "CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR",
                "CHURCH_DATA_ENTRY_OPERATOR"
            ],
            feature : "CERTIFICATE"
        },
        props: {
            isChurch: true
        }
    },
    {
        path: "/church/scheduler",
        name: "Church. Backup",
        component: SchedulerBoard,
        meta: {
            dashboard: true,
            institutionType : "CHURCH",
            userTypes : [
                "SUPER_ADMINISTRATOR", "SUPER_ASSISTANT_ADMINISTRATOR",
            ],
            // feature:"SCHEDULER"
        },
        props: (route)=>({
            isChurch: true
        })
    },


    {
        path: "/sign-in",
        name: "SignIn",
        component: SignIn,
        meta: {
            dashboard: false,
            feature: "TWO_FACTOR_AUTHENTICATION"
        },
    },
    {
        path: "/terms",
        name: "Terms And Conditions",
        component: TermsConditions,
        meta: {
            dashboard: false,
        },
    },
    {
        path: "/clean",
        name: "Clean",
        component: SessionCleaner,
        meta: {
            dashboard: false,
        },
    },
    {
        path: "/:pathMath(.*)*",
        name: "Not Found",
        redirect: "/sign-in",
        meta: {
            dashboard: false,
        },
    }
];



const router  = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
    linkActiveClass: "active bg-gradient-success",
});


function isInstitutionAllowed(singleRoute){
    if(typeof singleRoute.meta.dashboard!=='undefined' && singleRoute.meta.dashboard) {
        let loggedInUser = store.getters.getLoggedInUser;
        if (typeof singleRoute.meta.feature !== 'undefined') {
            if (loggedInUser === null) return false;
            let institution = null;
            if (SUPER_ADMINISTRATORS_ROLES.includes(loggedInUser.userType)) {
                institution = store.getters.getInstitution;
                if (institution === null) return false;
            } else {
                institution = loggedInUser.institution;
            }
            let allowedFeatures = [...institution.allowedFeatures],
                institutionType = singleRoute.meta.institutionType,
                allowedUserTypes = singleRoute.meta.userTypes;

            return allowedFeatures.includes(singleRoute.meta.feature)
                && (institution.institutionType === institutionType)
                && allowedUserTypes.includes(loggedInUser.userType)
                && isUserAllowed(singleRoute);
        }
    }
    return true;
}

function isUserAllowed(singeRoute){
    if(singeRoute.userTypes){
        let loggedInUser = store.getters.getLoggedInUser;
        if(loggedInUser===null) return false;
        return singeRoute.userTypes.includes(loggedInUser.userType);
    }
    return true;
}

router.beforeEach((newRoute, old, next)=>{
    let clean = false;
    if(old.fullPath!=="/") {
        if (typeof old.meta.institutionType !== 'undefined') {
            if (typeof newRoute.meta.institutionType !== 'undefined') {
                clean = old.meta.institutionType !== newRoute.meta.institutionType;
            }
        } else {
            // new route institutionType is undefined
            if (typeof newRoute.meta.institutionType !== 'undefined') clean = true;
        }
        if (clean) store.commit("handleInstitutionChange");

        let loggedInUser = store.getters.getLoggedInUser;
        if(!["/", "/sign-in"].includes(old.fullPath)){
            if(loggedInUser!==null && newRoute.fullPath==="/sign-in") {
                store.commit("logout", true);
            }
        }

        let isAllowed = isInstitutionAllowed(newRoute);
        if(!isAllowed && old.fullPath!==newRoute.fullPath) {
            if(old.fullPath!=="/dashboard") next({path: "/dashboard"});
            else return;
        }
    }

    if(old.fullPath!==newRoute.fullPath) {
        let isAllowed = isInstitutionAllowed(newRoute);
        if(isAllowed) next();
        else next({path: "/dashboard"});
    }
});

export default router;
