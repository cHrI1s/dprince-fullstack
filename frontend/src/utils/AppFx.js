/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/


function getFormattedMoney(money, currency="Rs"){
	if(typeof(money)!=="undefined" && money!==null){
		return currency+". "+money.toLocaleString('Ru-ru');
	}
	return "--";
}

function allValuesAreUndefined(values){
	let areUndefined = [],
		i = 0,
		totalValues = values.length;
	for(;i<totalValues; i++){
		areUndefined.push((typeof (values[i])==="undefined"
			|| values[i]==="undefined"
			|| values[i].trim()===""));
	}
	return !areUndefined.includes(false);
	
}

function removeQuotes(text){
	text = text+"";
	text = text.replace(/"/g, '');
	if(text.trim()==="" || text.trim()==="undefined") text = null;
	return text;
}

function ucFirst(string) {
	return string[0].toUpperCase() + string.substring(1);
}

function lcFirst(string) {
	return string[0].toLowerCase() + string.substring(1);
}

function validateForm(form, validationObjects){
	const FORM_KEYS = Object.keys(form);
	let i = 0;
	for(; i<validationObjects.length; i++){
		let currentFieldValidator = validationObjects[i];
		switch (currentFieldValidator.type){
			case "NUMBER":
			case "STRING":
			case "DECIMAL":
			case "DATE":
				console.log("chris");
				break;
		}
	}
}

function generateChartColor() {
	return "#000000".replace(/0/g, function () {
		return (~~(Math.random() * 16)).toString(16);
	});
}

export function renameKey(obj, oldKey, newKey) {
	const newObj = {};
	for (const key in obj) {
		if (Object.hasOwnProperty.call(obj, key)) {
			if (key === oldKey) {
				newObj[newKey] = obj[oldKey];
			} else {
				newObj[key] = obj[key];
			}
		}
	}
	return newObj;
}

export function getDropdownLabel(value, OPTIONS = [], optionLabel="label", optionValue="value"){
	let output = OPTIONS.find(option=>{
		return option[optionValue]===value;
	});
	return typeof output!=="undefined" ? output[optionLabel] : null;
}

function dynamicSort(property) {
	let sortOrder = 1;
	if(property[0] === "-") {
		sortOrder = -1;
		property = property.substr(1);
	}
	return function (a,b) {
		/* next line works with strings and numbers,
		 * and you may want to customize it to your needs
		 */
		let result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
		return result * sortOrder;
	}
}

export function moneyInWords (num) {
	if(num===0) return "Zero";
	const UNITS = ['','one ','two ','three ','four ', 'five ','six ','seven ','eight ','nine ','ten ','eleven ','twelve ','thirteen ','fourteen ','fifteen ','sixteen ','seventeen ','eighteen ','nineteen '],
		TENS = ['', '', 'twenty','thirty','forty','fifty', 'sixty','seventy','eighty','ninety'];

	if ((num = num.toString()).length > 9) return 'overflow';
	let n = ('000000000' + num).substr(-9).match(/^(\d{2})(\d{2})(\d{2})(\d{1})(\d{2})$/);
	if (!n) return;
	let str = '';
	str += (parseInt(n[1]) !== 0) ? (UNITS[Number(n[1])] || TENS[n[1][0]] + ' ' + UNITS[n[1][1]]) + 'crore ' : '';
	str += (parseInt(n[2]) !== 0) ? (UNITS[Number(n[2])] || TENS[n[2][0]] + ' ' + UNITS[n[2][1]]) + 'lakh ' : '';
	str += (parseInt(n[3]) !== 0) ? (UNITS[Number(n[3])] || TENS[n[3][0]] + ' ' + UNITS[n[3][1]]) + 'thousand ' : '';
	str += (parseInt(n[4]) !== 0) ? (UNITS[Number(n[4])] || TENS[n[4][0]] + ' ' + UNITS[n[4][1]]) + 'hundred ' : '';
	str += (parseInt(n[5]) !== 0)
		? ((str !== '') ? 'and ' : '') + (UNITS[Number(n[5])] || TENS[n[5][0]] + ' ' + UNITS[n[5][1]]) : '';
	return str.toUpperCase().trim();
}

export function convertUiToDate(dateString){
	if(dateString===null) return null;
	if(typeof dateString==="undefined") return null;
	if(typeof dateString!=="string") return dateString;
	if(dateString.trim().length===0) return null;
	try{
		let parts = dateString.split("/"),
			day = parseInt(parts[0], 10),
			month = parseInt(parts[1], 10) - 1, // Months are zero-indexed (0 = January,
			year = parseInt(parts[2], 10);
		return new Date(year, month, day);
	} catch (e){
		return null;
	}
}


export function getYearDifference(startDate, endDate=null) {
	if(startDate===null) return 0;
	if(endDate===null) endDate = new Date();
	// Extract the years from both dates
	const startYear = startDate.getFullYear(),
		endYear = endDate.getFullYear();

	// Calculate the difference in years
	let yearDifference = endYear - startYear;
	// Check if the end date is before the anniversary of the start date in the current year
	if (endDate.getMonth() < startDate.getMonth() ||
		(endDate.getMonth() === startDate.getMonth() && endDate.getDate() < startDate.getDate())) {
		yearDifference--;
	}
	return yearDifference;
}

function getDateFromTimeStamp(timestamp, withTime=false){
	if(typeof timestamp==="undefined") return null;
	if(typeof timestamp==='string' && timestamp.indexOf('T')>0){
		let datePart = timestamp.split("-"),
			date = new Date();
		if(datePart.length>=2) {
			date.setFullYear(parseInt(datePart[0]));
			date.setMonth(parseInt(datePart[1])-1);
			date.setDate(parseInt(datePart[2]));
			return getDateFromTimeStamp(date.getTime());
		}
		return null;
	} else {
		if (timestamp === null) return null;
		let timeZoneOffset = new Date().getTimezoneOffset(),
			DATE = new Date(timestamp);
		if (timeZoneOffset !== DATE.getTimezoneOffset()) DATE.setMinutes(DATE.getMinutes() + timeZoneOffset);
		let day = DATE.getDate(),
			month = DATE.getMonth() + 1;
		let dayString = String(day),
			monthString = String(month);
		if (day < 10) dayString = "0" + day;
		if (month < 10) monthString = "0" + month;
		let fullDateString = dayString + "/" + monthString + "/" + DATE.getFullYear();
		if (withTime) {
			let hours = DATE.getHours(),
				hoursString = String(hours);
			if (hours < 10) hoursString = "0" + hours;
			let minutes = DATE.getMinutes(),
				minutesString = String(minutes);
			if (minutes < 10) minutesString = "0" + minutes;
			let seconds = DATE.getSeconds(),
				secondsString = String(seconds);
			if (seconds < 10) secondsString = "0" + seconds;
			fullDateString += " " + hoursString + ":" + minutesString + ":" + secondsString;
		}
		return fullDateString;
	}
}

function getObjectById(array, objectId){
	let returnedObject = null,
		i=0, length = array.length;
	for(; i<length; i++){
		const CURRENT_OBJECT = array[i];
		if(CURRENT_OBJECT.id===objectId){
			returnedObject = CURRENT_OBJECT;
			break;
		}
	}
	return returnedObject;
}


function getObjectByPropertyValue(array, property, value){
    let wantedObject = {},
		i=0,
		total = array.length;
    for(; i<total; i++){
        if(array[i][property]===value){
            wantedObject = array[i];
            break;
        }
    }
    return wantedObject;
}

function getSumOfProperty(arrayOfObjects, property){
	return arrayOfObjects.reduce((accumulator, object) => {
		return accumulator + object[property];
	}, 0);
}


function getPrice(classified){
	if(typeof(classified.price)==="undefined") return "--";
	return classified.price +" "+classified.currency;
}

const formatNumber = (number, shorten=false) => {
	if(shorten || typeof shorten==='undefined') {
		if (number >= 1000000000) {
			return (number / 1000000000).toFixed(1).replace(/\.0$/, '') + 'b';
		}
		if (number >= 1000000) {
			return (number / 1000000).toFixed(1).replace(/\.0$/, '') + 'm';
		}
		if (number >= 1000) {
			return (number / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
		}
	} else {
		number = number.toLocaleString("en-IN");
	}
	return number;
}

const getKeyByValue = (value, object)=>{
	let keys = Object.keys(object);
	let i = 0, keysCount = keys.length;
	for(; i<keysCount; i++) if(object[keys[i]]===value) return keys[i];
	return null;
};


const getDeepObjectValue = (objectElement, path)=>{
	if(typeof path==="undefined" || path===null) return null;
	if(path.trim()==="") return null;
	let dir = path.split("."), feedback = null, currentElement = objectElement;
	for(let i=0; i<dir.length; i++){
		if(typeof(currentElement[dir[i]])!=='undefined' && currentElement[dir[i]]!==null){
			currentElement = currentElement[dir[i]];
			feedback = currentElement;
		} else {
			feedback = null;
			break;
		}
	}
	return feedback;
};
 function getDateString(stringOne = "", date, stringTwo = ""){
	let userDate = new Date(date);
	let DAYS = [
		{number:0, dayString:"Sunday"},
		{number:1, dayString:"Monday"},
		{number:2, dayString:"Tuesday"},
		{number:3, dayString:"Wednesday"},
		{number:4, dayString:"Thursday"},
		{number:5, dayString:"Friday"},
		{number:6, dayString : "Saturday"}
	];
	let MONTHS = [
		{monthNumber :0 , monthString : " Jan "},
		{monthNumber :1 , monthString : " Feb "},
		{monthNumber :2, monthString : " Mar "},
		{monthNumber :3 , monthString : " Apr "},
		{monthNumber :4 , monthString : " May "},
		{monthNumber :5 , monthString : " Jun "},
		{monthNumber :6 , monthString : " Jul "},
		{monthNumber :7 , monthString : " Aug "},
		{monthNumber :8 , monthString : "  Sep "},
		{monthNumber :9 , monthString : " Oct  "},
		{monthNumber :10 , monthString : " Nov "},
		{monthNumber :11 , monthString : " Dec "}
	]
	let year  = userDate.getFullYear();
	let month = userDate.getMonth();
	let myDate = userDate.getDate();
	let dff  = userDate.getDay();
	let days = DAYS[dff]?.dayString;
	let monthInString = MONTHS[month]?.monthString
	return stringOne  + "   " + days + " , " + myDate + " " + stringTwo + " " + monthInString + " , " + year;
}

function getTodayDate(){
	let date = new Date();
	let todayDate = date.getDate();
	let month = date.getMonth()+1;
	let resultMonth = (month<10) ?  "0" +month : month;
	let resultDay   = (todayDate<10) ? "0" + todayDate : todayDate;
	let year  = date.getFullYear();
	return (resultDay+ "/" + resultMonth + "/" + year);
}

export function generateDistinctColors(count) {
	let colors = [];
	while (colors.length < count) {
		let color = '#' + (((1 << 24) * Math.random()) | 0).toString(16).padStart(6, '0');
		colors.push(color);
	}
	return colors;
}


export function areArraysEquals(array1=[], array2=[]){
	 if(array1===null || typeof array1==='undefined') return false;
	 if(array2===null || typeof array2==='undefined') return false;
	if(array1.length!==array2.length) return false;

	array1.sort();
	array2.sort();
	let i = 0,
		count = array1.length;
	for (; i < count; i++) {
		if (array1[i] !== array2[i]) return false;
	}

	return true;
}

export {
	getFormattedMoney,
	removeQuotes,
	generateChartColor,
	getObjectById,
	getDateFromTimeStamp,
	dynamicSort,
	getObjectByPropertyValue,
	getSumOfProperty,
	getPrice,
	formatNumber,
	getKeyByValue,
	getDeepObjectValue,
	getDateString,
	getTodayDate
}