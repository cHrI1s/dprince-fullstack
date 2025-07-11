/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

import { createStore } from "vuex";
import VuexPersistence from "vuex-persist";
import {SUPER_ADMINISTRATORS_ROLES} from "@/dashboard/members/members";


const vuexLocalStorage = new VuexPersistence({
    key: 'vuex', // The key to store the state on in the storage provider.
    storage: window.localStorage, // or window.sessionStorage or localForage
    // Function that passes the state and returns the state with only the objects you want to store.
    // reducer: state => state,
    // Function that passes a mutation and lets you decide if it should update the state in localStorage.
    // filter: mutation => (true)
});
const DEFAULT_STATE = {
    hideConfigButton: false,
    isPinned: true,
    showConfig: false,
    sidebarType: "bg-gradient-dark",
    isRTL: false,
    color: "success",
    isNavFixed: false,
    isAbsolute: false,
    showNavs: true,
    showSidenav: true,
    showNavbar: true,
    showFooter: true,
    showMain: true,
    isDarkMode: true,
    navbarFixed:
        "position-sticky blur shadow-blur left-auto top-1 z-index-sticky px-0 mx-4",
    absolute: "position-absolute px-4 mx-0 w-100 z-index-2",
    apiToken: null,
    loggedInUser: null,
    loading: false,
    processes: 0,
    institution: null,
    institutionMembers: [],
    family: null,
    familyCouple : [],
    template : [],
    priestSignature : null,
    churchEvent : null,

    peopleToCommunicate: null,
    tmpInstitutionMember: null,
    signature: null,
    certificatesBgs: null
};
export default createStore({
    plugins: [vuexLocalStorage.plugin],
    state: { ...DEFAULT_STATE },
    mutations: {
        setPeopleToCommunicate(state, payload){
            state.peopleToCommunicate = payload;
        },
        setChurchEvent(state, payload){
            state.churchEvent = payload;
        },
        setPriestSignature(state, payload){
            state.priestSignature = payload;
        },
        setTemplate (state ,payload){
            state.template = payload;
        },
        setFamilyCouple(state, payload){
            state.familyCouple = payload;
        },
        setTmpInstitutionMember(state, member){
            state.tmpInstitutionMember = member;
        },
        updateInstitutionDetails(state, institution){
            if(state.loggedInUser!==null) {
                if (SUPER_ADMINISTRATORS_ROLES.includes(state.loggedInUser.userType)) {
                    state.institution = institution;
                } else {
                    state.loggedInUser = {
                        ...state.loggedInUser,
                        institution: {...institution}
                    }
                }
            }
        },
        setInstitution(state, institution){
            state.institution = institution;
        },
        handleInstitutionChange(state){
            state.institution = null;
            state.tmpInstitutionMember = null;
            state.institutionMembers = [];
            state.family = null;
            state.peopleToCommunicate = null;
        },
        setFamily(state, family){
            state.family = family;
            if(family!==null && typeof family.members!=="undefined"){
                state.institutionMembers = family.members;
            } else {
                state.institutionMembers = [];
            }
        },
        setInstitutionMembers(state, familyMembers){
            if(familyMembers===null) familyMembers = [];
            let familyMemberSet = new Set(familyMembers);
            state.institutionMembers = [...familyMemberSet];
        },
        addInstitutionMember(state, newFamilyMember){
    // Null check for newFamilyMember as well, just in case it's null when passed
    if (newFamilyMember === null || typeof newFamilyMember === 'undefined' || typeof newFamilyMember.id === 'undefined') {
        console.warn("addInstitutionMember: newFamilyMember is null/undefined or missing ID, skipping.", newFamilyMember);
        return; // Exit if newFamilyMember is invalid
    }

    let memberAtIndex = state.institutionMembers.findIndex(member => {
        // **CRITICAL FIX: Check if 'member' is null or undefined before accessing its 'id'**
        if (member === null || typeof member === 'undefined') {
            return false; // Skip null/undefined entries during findIndex
        }
        return member.id === newFamilyMember.id;
    });

    if(memberAtIndex < 0) {
        state.institutionMembers = [
            ...state.institutionMembers,
            newFamilyMember
        ];
    }
},
        removeInstitutionMember(state, newFamilyMember){
            state.institutionMembers = state.institutionMembers.filter(singleMember=>{
                return singleMember.id!==newFamilyMember.id
            })
        },
        removeFamilyMember(state, newFamilyMember){
            if(state.family!==null) {
                state.family.members = state.family.members.filter(singleMember => {
                    return singleMember.id !== newFamilyMember.id
                });
            }
        },
        setLoggedInUser(state, loggedInUser){
            state.loggedInUser = (loggedInUser===null) ? null : {...loggedInUser};
        },
        login(state, session){
            state.loggedInUser = session.user;
            state.apiToken = session.token;
            state.apiToken = session.token;
            state.showNavbar = true;
            state.showSidenav = true;
            state.showFooter = true;
            state.hideConfigButton = false;
        },
        logout(state, definitive=true){
            state.loggedInUser = null;
            if(definitive) state.apiToken = null;
            state.apiToken = null;
            state.showNavbar = false;
            state.showSidenav = false;
            state.showFooter = false;
            state.hideConfigButton = true;

            state.family = null;
            state.institutionMembers = [];
            state.institution = null;
            state.tmpInstitutionMember = null;
            state.processes = 0;
            state.priestSignature = null;
            state.churchEvent = null;
        },
        resetProcess(state){
            state.processes = 0;
        },
        setSignature(state, value){
            state.signature = value;
        },
        setCertificatesBgs(state, value){
            state.certificatesBgs = value;
        },
        setLoading(state, loadingStatus){
            if(loadingStatus) state.processes += 1;
            else state.processes -= (state.processes===0) ? 0 : 1;
            if(loadingStatus || state.processes===0) state.loading = loadingStatus;
        },
        initLoading(state){
            state.processes = 0;
            state.loading = false;
        },
        toggleConfigurator(state) {
            state.showConfig = !state.showConfig;
        },
        navbarMinimize(state) {
            const sidenav_show = document.querySelector(".g-sidenav-show");

            if (sidenav_show.classList.contains("g-sidenav-pinned")) {
                sidenav_show.classList.remove("g-sidenav-pinned");
                state.isPinned = true;
            } else {
                sidenav_show.classList.add("g-sidenav-pinned");
                state.isPinned = false;
            }
        },
        navbarFixed(state) {
            state.isNavFixed = state.isNavFixed === false;
        },
        toggleEveryDisplay(state, hide) {
            state.showNavbar = !hide;
            state.showSidenav = !hide;
            state.showFooter = !hide;
        },
        toggleHideConfig(state, hideConfig) {
            state.hideConfigButton = hideConfig;
        },
        color(state, payload) {
            state.color = payload;
        },
    },
    actions: {
        setColor({ commit }, payload) {
            commit("color", payload);
        },



    },
    getters: {
        getPeopleToCommunicate(state){
            return state.peopleToCommunicate;
        },
        isPinned(state){
            return state.isPinned;
        },
        getChurchEvent(state){
            return state.churchEvent;
        },
        getPriestSignature (state){
            return state.priestSignature;
        },
        getTemplate(state){
            return state.template;
        },
        getMarriedCouple(state){
            return state.familyCouple;
        },
        getHideSubscription(state){
            return state.hideSubscription;
        },
        getBearerToken(state){
            return state.apiToken;
        },
        getLoggedInUser(state){
            return state.loggedInUser;
        },
        isLoading(state){
            return state.loading;
        },
        getInstitution(state){
            return state.institution;
        },
        getInstitutionMembers(state){
            return state.institutionMembers;
        },
        getFamily(state){
            return state.family;
        },



        getTmpInstitutionMember(state){
            return state.tmpInstitutionMember;
        },
        getSignature(state){
            return state.signature;
        },
        getCertificatesBgs(state){
            return state.certificatesBgs;
        },
    },
});
