/*******************************************************************************
 * @license
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

define(['require', 'dojo', 'orion/editor/regex', 'orion/commands'], function(require, dojo, mRegex, mCommands) {

var orion = orion || {};

/**
 * Utility methods
 * @namespace orion.searchUtils 
 */
 
orion.searchUtils = orion.searchUtils || {};

/**
 * Parse the search query string from the hash value of a search page.
 * @param {String} queryStr The hash string.
 * @returns {Object} An object having the properties:<ul>
 * <li>{@link Integer} <code>start</code> The start number of search result of current page.</li>
 * <li>{@link Integer} <code>rows</code> The max rows per page.</li>
 * <li>{@link String} <code>sort</code> The sort parameters."Path asc" or "Name asc".</li>
 * <li>{@link Object} <code>inFileQuery</code> The query object for in file search.</li>
 * </ul>
 * @name orion.searchUtils#parseQueryStr
 * @function
 */
orion.searchUtils.parseQueryStr = function(queryStr, fromStart) {
	var indexOfQMark = queryStr.indexOf("?"); //$NON-NLS-0$
	var indexOfQWqual = queryStr.indexOf("q="); //$NON-NLS-0$
	if(indexOfQMark < indexOfQWqual && indexOfQWqual > 0){
		queryStr = queryStr.substring(indexOfQMark+1);
	}
	var indexOfRegEx = queryStr.indexOf("regEx&");
	if(indexOfRegEx === 0){
		queryStr = queryStr.substring(indexOfRegEx + 6);
	}
	//var obj = dojo.queryToObject(queryStr);
	var splitQ = queryStr.split("&"); //$NON-NLS-0$
	var queryObj = {queryStr: queryStr, start:0, rows:10, sort:"Path asc", replace: null}; //$NON-NLS-0$
	queryObj.useRegEx = (indexOfRegEx === 0);
	for(var i=0; i < splitQ.length; i++){
		var qIndex = splitQ[i].indexOf("q="); //$NON-NLS-0$
		var rIndex = splitQ[i].indexOf("replace="); //$NON-NLS-0$
		if(qIndex >= 0){
			indexOfRegEx === 0 ? orion.searchUtils.parseLocationAndSearchStrRegEx(splitQ[i].substring(qIndex+2), queryObj, fromStart):
			                     orion.searchUtils.parseLocationAndSearchStr(splitQ[i].substring(qIndex+2), queryObj, fromStart);
		} else if(rIndex >= 0){
			queryObj.replace = splitQ[i].substring(rIndex+8);
		} else {
			var splitparameters = splitQ[i].split("="); //$NON-NLS-0$
			if(splitparameters.length === 2){
				if(splitparameters[0] === "rows"){ //$NON-NLS-0$
					queryObj.rows = parseInt(splitparameters[1]);
				} else if(splitparameters[0] === "start"){ //$NON-NLS-0$
					queryObj.start = parseInt(splitparameters[1]);
				} else if(splitparameters[0] === "sort"){ //$NON-NLS-0$
					queryObj.sort = splitparameters[1];
				} 
			}
		}
	}
	return queryObj;
};

orion.searchUtils.copyQueryParams = function(queryObj, copyReplace) {
	return {
		sort: queryObj.sort,
		useRegEx: queryObj.useRegEx,
		rows: queryObj.rows,
		start: queryObj.start,
		searchStr: queryObj.searchStr,
		location: queryObj.location,
		replace: copyReplace ? queryObj.replace: null
	};
};

orion.searchUtils.generateSearchHref = function(options) {
	var base =  require.toUrl("search/search.html"); //$NON-NLS-0$
	return base + "#" + orion.searchUtils.generateSearchQuery(options); //$NON-NLS-0$
};

orion.searchUtils.generateSearchQuery = function(options) {
	var sort = "Path asc", rows = 40, start = 0 , searchStr = "", loc = "", replace = "", regEx = ""; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
	if(options){
		if(options.useRegEx){
			regEx = "regEx&";
		}
		if(options.sort){
			sort = options.sort;
		}
		if(options.rows){
			rows = options.rows;
		}
		if(options.start){
			start = options.start;
		}
		if(options.searchStr){
			searchStr = options.searchStr;
			//searchStr = searchStr.split(" ").join(""); //$NON-NLS-0$
		}
		if(options.location){
			loc = options.location;
			if(loc.length > 0 && loc[loc.length -1] !== '*'){ //$NON-NLS-0$
				loc = loc + "*"; //$NON-NLS-0$
			}
			if(loc !== ""){
				loc = "+Location:" + loc; //$NON-NLS-0$
			}
		}
		if(options.replace !== null && options.replace !== undefined){
			replace = "&replace=" + options.replace; //$NON-NLS-0$
		}
	}
	return "?" + regEx + "sort=" + sort + "&rows=" + rows + "&start=" + start + "&q=" + searchStr + loc + replace; //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
};

orion.searchUtils.parseLocationAndSearchStr = function(locAndSearchStr, queryObj, fromStart) {
	var hasLocation = (locAndSearchStr.indexOf("+Location:") > -1); //$NON-NLS-0$
	queryObj.location = "";
	queryObj.searchStr = locAndSearchStr;
	if(hasLocation){
		var splitStr = locAndSearchStr.split("+Location:"); //$NON-NLS-0$
		if(splitStr.length === 2){
			var loc = splitStr[1];
			if(loc.length > 0 && loc[loc.length - 1] === '*'){ //$NON-NLS-0$
				loc = loc.substring(0, loc.length-1);
			}
			queryObj.location = loc;
			queryObj.searchStr = splitStr[0];
			//queryObj.searchStr = splitStr[0].split(" ").join(""); //$NON-NLS-0$
		}
	}
	queryObj.searchStrTitle = queryObj.searchStr.split("\\").join(""); //$NON-NLS-0$
	//If the search string contains white space, we should add this special property. 
	//The property's value is basically the original search string with the double quato at both end.
	//It is the caller's responsibility to decide wether this property has to be used or not.
	if(queryObj.searchStr.indexOf(" ") >= 0){
		queryObj.searchStrWithWhiteSpace = "\"" + queryObj.searchStr + "\"";
	}
	queryObj.inFileQuery= orion.searchUtils.generateInFileQuery(queryObj.searchStr, fromStart);
};

orion.searchUtils.parseLocationAndSearchStrRegEx = function(locAndSearchStr, queryObj, fromStart) {
	var hasLocation = (locAndSearchStr.indexOf("+Location:") > -1); //$NON-NLS-0$
	queryObj.location = "";
	queryObj.searchStr = locAndSearchStr;
	if(hasLocation){
		var splitStr = locAndSearchStr.split("+Location:"); //$NON-NLS-0$
		if(splitStr.length === 2){
			var loc = splitStr[1];
			if(loc.length > 0 && loc[loc.length - 1] === '*'){ //$NON-NLS-0$
				loc = loc.substring(0, loc.length-1);
			}
			queryObj.location = loc;
			queryObj.searchStr = splitStr[0];
		}
	}
	queryObj.searchStrTitle = queryObj.searchStr;
	queryObj.inFileQuery= orion.searchUtils.generateInFileQueryRegEx(queryObj.searchStr, fromStart);
};

orion.searchUtils.generateInFileQuery = function(searchStr, fromStart) {
	var inFileQuery = {};
	inFileQuery.originalSearchStr = searchStr;
	var hasStar = (searchStr.indexOf("*") > -1); //$NON-NLS-0$
	var hasQMark = (searchStr.indexOf("?") > -1); //$NON-NLS-0$
	if(hasStar){
		searchStr = searchStr.split("*").join(".*"); //$NON-NLS-1$ //$NON-NLS-0$
	}
	if(hasQMark){
		searchStr = searchStr.split("?").join("."); //$NON-NLS-1$ //$NON-NLS-0$
	}
	if(!hasStar && !hasQMark){
		inFileQuery.searchStr =searchStr.split("\\").join("").toLowerCase(); //$NON-NLS-0$
		inFileQuery.wildCard = false;
	} else {
		inFileQuery.searchStr =searchStr.toLowerCase();
		var prefix = "";
		if(fromStart){
			prefix = "^";
		}
		var regexp = mRegex.parse("/" + prefix + inFileQuery.searchStr + "/"); //$NON-NLS-1$ //$NON-NLS-0$
		if (regexp) {
			var pattern = regexp.pattern;
			var flags = regexp.flags;
			flags = flags + (flags.indexOf("i") === -1 ? "i" : ""); //$NON-NLS-1$ //$NON-NLS-0$
			inFileQuery.regExp = {pattern: pattern, flags: flags};
			inFileQuery.wildCard = true;
		}
	}
	inFileQuery.searchStrLength = inFileQuery.searchStr.length;
	return inFileQuery;
};
	
orion.searchUtils.generateInFileQueryRegEx = function(searchStr, fromStart) {
	var inFileQuery = {};
	inFileQuery.originalSearchStr = searchStr;
	inFileQuery.searchStr =searchStr;
	var prefix = "";
	if(fromStart){
		prefix = "^";
	}
	var regexp = mRegex.parse("/" + prefix + inFileQuery.searchStr + "/"); //$NON-NLS-1$ //$NON-NLS-0$
	if (regexp) {
		var pattern = regexp.pattern;
		var flags = regexp.flags;
		flags = flags + (flags.indexOf("i") === -1 ? "i" : ""); //$NON-NLS-1$ //$NON-NLS-0$
		inFileQuery.regExp = {pattern: pattern, flags: flags};
		inFileQuery.wildCard = true;
	}
	inFileQuery.searchStrLength = inFileQuery.searchStr.length;
	return inFileQuery;
};
	
orion.searchUtils.generateFindURLBinding = function(inFileQuery, lineNumber, replaceStr) {
	var binding = ",find="; //$NON-NLS-0$
	if (inFileQuery.wildCard) {
		binding = binding + "@@useRegEx@@true@@"; //$NON-NLS-0$
	}
	binding = binding + encodeURIComponent(inFileQuery.searchStr);
	if (typeof(replaceStr) === "string") { //$NON-NLS-0$
		binding = binding + "@@replaceWith@@" + encodeURIComponent(replaceStr); //$NON-NLS-0$
	}
	if (typeof(lineNumber) === "number") { //$NON-NLS-0$
		binding = binding + "@@atLine@@" + lineNumber; //$NON-NLS-0$
	}
	return binding;
};

orion.searchUtils.parseFindURLBinding = function(findParam) {
	var lineNumber = null;
	var splitParam = findParam.split("@@atLine@@");
	if(splitParam.length > 1){
		lineNumber = parseInt(splitParam[1]);
		if(lineNumber < 1){
			lineNumber = 1;
		}
	}
	var findAndReplaceQuery = splitParam[0];
	var replaceStr = null;
	var splitQuery = findAndReplaceQuery.split("@@replaceWith@@");
	var	findQuery = splitQuery[0];
	var useRegEx = false;
	if(findQuery.indexOf("@@useRegEx@@true@@") === 0){
		useRegEx = true;
		findQuery = findQuery.split("@@useRegEx@@true@@")[1];
	}
	if(splitQuery.length > 1){
		replaceStr = splitQuery[1];
	}
	return {searchStr: findQuery, replaceStr: replaceStr, lineNumber: lineNumber, useRegExp: useRegEx};
};

orion.searchUtils.replaceRegEx = function(text, regEx, replacingStr){
	var regexp = new RegExp(regEx.pattern, regEx.flags);
	return text.replace(regexp, replacingStr); 
	
};

orion.searchUtils.replaceStringLiteral = function(text, keyword, replacingStr){
	var regexp = mRegex.parse("/" + keyword + "/gim"); //$NON-NLS-1$ //$NON-NLS-0$
	return orion.searchUtils.replaceRegEx(text,regexp, replacingStr);
};

orion.searchUtils.searchOnelineLiteral =  function(inFileQuery, lineString, onlyOnce){
	var i,startIndex = 0;
	var found = false;
	var result = [];
	while(true){
		i = lineString.indexOf(inFileQuery.searchStr, startIndex);
		if (i < 0) {
			break;
		} else {
			result.push({startIndex: i});
			found = true;
			if(onlyOnce){
				break;
			}
			startIndex = i + inFileQuery.searchStrLength;
		}
	}
	if(found) {
		return result;
	}
	return null;
	
};

/**
 * Helper for finding regex matches in text contents.
 * 
 * @param {String}
 *            pattern A valid regexp pattern.
 * @param {String}
 *            flags Valid regexp flags: [is]
 * @param {Number}
 *            [startIndex] Default is false.
 * @return {Object} An object giving the match details, or
 *         <code>null</code> if no match found. The
 *         returned object will have the properties:<br />
 *         {Number} index<br />
 *         {Number} length
 */
orion.searchUtils.findRegExp =  function(text, pattern, flags, startIndex) {
	if (!pattern) {
		return null;
	}
	flags = flags || "";
	// 'g' makes exec() iterate all matches, 'm' makes ^$
	// work linewise
	flags += (flags.indexOf("g") === -1 ? "g" : "") //$NON-NLS-1$ //$NON-NLS-0$
			+ (flags.indexOf("m") === -1 ? "m" : ""); //$NON-NLS-1$ //$NON-NLS-0$
	var regexp = new RegExp(pattern, flags);
	var result = null, match = null;
	result = regexp.exec(text.substring(startIndex));
	return result && {
		startIndex : result.index + startIndex,
		length : result[0].length
	};
};

orion.searchUtils.searchOnelineRegEx =  function(inFileQuery, lineString, onlyOnce){
	var i,startIndex = 0;
	var found = false;
	var result = [];
	while(true){
		var regExResult = orion.searchUtils.findRegExp(lineString, inFileQuery.regExp.pattern, inFileQuery.regExp.flags, startIndex);
		if(regExResult){
			result.push(regExResult);
			found = true;
			if(onlyOnce){
				break;
			}
			startIndex = regExResult.startIndex + regExResult.length;
		} else {
			break;
		}
	}
	if(found) {
		return result;
	}
	return null;
};

orion.searchUtils.generateNewContents = function( oldContents, newContents, fileModelNode, replaceStr, searchStrLength){
	if(fileModelNode && oldContents){
		var updating;
		if(newContents.length > 0){
			updating = true;
		} else {
			updating = false;
		}
		for(var i = 0; i < oldContents.length ; i++){
			var lineStringOrigin = oldContents[i];
			var changingLine = false;
			var checked = false;
			var fullChecked = false;
			var checkedMatches = [];
			var originalMatches;
			var startNumber = 0;
			for(var j = 0; j < fileModelNode.children.length; j++){
				var lnumber = fileModelNode.children[j].lineNumber - 1;
				if(lnumber === i){
					startNumber = j;
					for(var k = 0; k < fileModelNode.children[j].matches.length; k++ ){
						if(fileModelNode.children[j+k].checked !== false){
							checkedMatches.push(k);
						}
					}
					checked = (checkedMatches.length > 0);
					fullChecked = (checkedMatches.length === fileModelNode.children[j].matches.length);
					originalMatches = fileModelNode.children[j].matches; 
					changingLine = true;
					break;
				}
			}
			if(changingLine){
				var newStr;
				if(!checked){
					newStr = lineStringOrigin;
					for(var k = 0; k < fileModelNode.children[startNumber].matches.length; k++ ){
						fileModelNode.children[startNumber+k].newMatches = fileModelNode.children[startNumber+k].matches;
					}
				} else{
					var result =  orion.searchUtils.replaceCheckedMatches(lineStringOrigin, replaceStr, originalMatches, checkedMatches, searchStrLength);
					newStr = result.replacedStr;
					for(var k = 0; k < fileModelNode.children[startNumber].matches.length; k++ ){
						fileModelNode.children[startNumber+k].newMatches = result.newMatches;
					}
				}
				if(updating){
					newContents[i] = newStr;
				} else {
					newContents.push(newStr);
				}
			} else if(!updating){
				newContents.push(lineStringOrigin);
			}
		}
	}
};

orion.searchUtils.generateMatchContext = function(contextAroundLength, fileContents, lineNumber/*zero based*/){
	var context = [];
	var totalContextLength = contextAroundLength*2 + 1;
	var startFrom, endTo;
	if(fileContents.length <= totalContextLength){
		startFrom = 0;
		endTo = fileContents.length -1;
	} else {
		startFrom = lineNumber - contextAroundLength;
		if(startFrom < 0){
			startFrom = 0;
			endTo = startFrom + totalContextLength - 1;
		} else {
			endTo = lineNumber + contextAroundLength;
			if(endTo > (fileContents.length -1)){
				endTo = fileContents.length -1;
				startFrom = endTo - totalContextLength + 1;
			}
			
		}
	}
	for(var i = startFrom; i <= endTo; i++){
		context.push({context: fileContents[i], current: (i === lineNumber)});
	}
	return context;
};

/**
 * Split file contents into lines. It also handles the mixed line endings with "\n", "\r" and "\r\n".
 *
 * @param {String} text The file contetns.
 * @returns {Array} Split file lines. 
 * @name orion.searchUtils#splitFile
 * @function
 */
orion.searchUtils.splitFile = function(text) {
	var cr = 0, lf = 0, index = 0, start = 0;
	var splitLines = [];
	while (true) {
		if (cr !== -1 && cr <= index) { 
			cr = text.indexOf("\r", index);  //$NON-NLS-0$
		}
		if (lf !== -1 && lf <= index) { 
			lf = text.indexOf("\n", index);  //$NON-NLS-0$
		}
		if (lf === -1 && cr === -1) {
			splitLines.push(text.substring(start));
			break; 
		}
		var offset = 1;
		if (cr !== -1 && lf !== -1) {
			if (cr + 1 === lf) {
				offset = 2;
				index = lf + 1;
			} else {
				index = (cr < lf ? cr : lf) + 1;
			}
		} else if (cr !== -1) {
			index = cr + 1;
		} else {
			index = lf + 1;
		}
		splitLines.push(text.substring(start, index - offset));
		start = index;
	}
	return splitLines;
};

orion.searchUtils.searchWithinFile = function( inFileQuery, fileModelNode, fileContentText, lineDelim, replacing, caseSensitive){
	var fileContents = orion.searchUtils.splitFile(fileContentText);
	if(replacing){
		fileModelNode.contents = fileContents;
	}
	if(fileModelNode){
		fileModelNode.children = [];
		var totalMatches = 0;
		for(var i = 0; i < fileContents.length ; i++){
			var lineStringOrigin = fileContents[i];
			if(lineStringOrigin && lineStringOrigin.length > 0){
				var lineString = caseSensitive ? lineStringOrigin : lineStringOrigin.toLowerCase();
				var result;
				if(inFileQuery.wildCard){
					result = orion.searchUtils.searchOnelineRegEx(inFileQuery, lineString);
				} else {
					result = orion.searchUtils.searchOnelineLiteral(inFileQuery, lineString);
				}
				if(result){
					var lineNumber = i+1;
					if(!replacing){
						var detailNode = {parent: fileModelNode, context: orion.searchUtils.generateMatchContext(2, fileContents, i), checked: fileModelNode.checked, 
										  type: "detail", matches: result, lineNumber: lineNumber, name: lineStringOrigin, 
										  location: fileModelNode.location + "-" + lineNumber}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
						fileModelNode.children.push(detailNode);
					} else {
						for(var j = 0; j < result.length; j++){
							var matchNumber = j+1;
							var detailNode = {parent: fileModelNode, checked: fileModelNode.checked, type: "detail", matches: result, lineNumber: lineNumber, matchNumber: matchNumber, name: lineStringOrigin, location: fileModelNode.location + "-" + lineNumber + "-" + matchNumber}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
							fileModelNode.children.push(detailNode);
						}
					}
					totalMatches += result.length;
				}
			}
		}
		fileModelNode.totalMatches = totalMatches;
	}
};

orion.searchUtils.replaceCheckedMatches = function(text, replacingStr, originalMatches, checkedMatches, defaultMatchLength){
	var gap = defaultMatchLength;
	var startIndex = 0;
	var replacedStr = "";
	var newMatches = [];
	for(var i = 0; i < originalMatches.length; i++){
		if(startIndex !== originalMatches[i].startIndex){
			replacedStr = replacedStr + text.substring(startIndex, originalMatches[i].startIndex);
		}
		if(originalMatches[i].length){
			gap = originalMatches[i].length;
		}
		var needReplace = false;
		for (var j = 0; j < checkedMatches.length; j++){
			if(checkedMatches[j] === i){
				needReplace = true;
				break;
			}
		}
		if(needReplace){
			newMatches.push({startIndex: replacedStr.length, length: replacingStr.length});
			replacedStr = replacedStr + replacingStr;
		} else {
			newMatches.push({startIndex: replacedStr.length, length: gap});
			replacedStr = replacedStr + text.substring(originalMatches[i].startIndex, originalMatches[i].startIndex + gap);
		}
		startIndex = originalMatches[i].startIndex + gap;
	}
	if(startIndex < text.length){
		replacedStr = replacedStr + text.substring(startIndex);
	}
	return {replacedStr: replacedStr, newMatches: newMatches};
};

orion.searchUtils.fullPathNameByMeta = function(parents){
	var parentIndex = parents.length;
	var fullPath = "";
	//add parents chain top down if needed
	if(parentIndex > 0){
		for(var j = parentIndex - 1; j > -1; j--){
			var separator = (fullPath === "") ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-0$
			fullPath = fullPath + separator + parents[j].Name;
		}
	}
	return fullPath;
};

orion.searchUtils.path2FolderName = function(filePath, fileName, keepTailSlash){
	var tail = keepTailSlash ? 0: 1;
	return filePath.substring(0, filePath.length-fileName.length-tail);
};

var MAX_RECENT_SEARCH_NUMBER = 20;

orion.searchUtils._storeRecentSearch = function(serviceRegistry, searches){
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		prefs.put("recentSearch", searches); //$NON-NLS-0$
	});
};

orion.searchUtils.addRecentSearch = function(serviceRegistry, searchName, useRegEx){
	if(typeof searchName !== "string" || !searchName ){
		return;
	}
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		var i;
		var searches = prefs.get("recentSearch"); //$NON-NLS-0$
		if (typeof searches === "string") { //$NON-NLS-0$
			searches = JSON.parse(searches);
		}
		if (searches) {
			var i;
			for (i in searches) {
				if (searches[i].name === searchName) {
					return;
				}
			}
			if(searches.length >= MAX_RECENT_SEARCH_NUMBER){
				var len = searches.length;
				searches.splice(MAX_RECENT_SEARCH_NUMBER-1, len-MAX_RECENT_SEARCH_NUMBER+1);
			}
		} else {
			searches = [];
		}
		searches.splice(0,0,{ "name": searchName, "regEx": useRegEx});//$NON-NLS-1$
		orion.searchUtils._storeRecentSearch(serviceRegistry, searches);
		//prefs.put("recentSearch", searches); //$NON-NLS-0$
	});
};

orion.searchUtils.populateSearchMenu = function(serviceRegistry, choicesMenu, type, makeLabelFunc) {
	// see http://bugs.dojotoolkit.org/ticket/10296
	choicesMenu.focusedChild = null;
	dojo.forEach(choicesMenu.getChildren(), function(child) {
		choicesMenu.removeChild(child);
		child.destroy();
	});
	orion.searchUtils.getSearches(serviceRegistry, type, function(searches){
		if (searches) {
			for (i in searches) {
				choicesMenu.addChild(new mCommands.CommandMenuItem({
					 label: makeLabelFunc(searches[i]),
					 hasLink: true
				}));
			}
		}
	});
};

orion.searchUtils.getSearches = function(serviceRegistry, type, callback){
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		var i;
		var searches = prefs.get(type); //$NON-NLS-0$
		if (typeof searches === "string") { //$NON-NLS-0$
			searches = JSON.parse(searches);
		}
		if (searches && callback) {
			callback(searches);
		}
	});
};

orion.searchUtils.getMixedSearches = function(serviceRegistry, mixed, callback){
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		var i;
		var searches = prefs.get("recentSearch"); //$NON-NLS-0$
		if (typeof searches === "string") { //$NON-NLS-0$
			searches = JSON.parse(searches);
		}
		if(mixed){
			var savedSearches = prefs.get("search"); //$NON-NLS-0$
			if (typeof savedSearches === "string") { //$NON-NLS-0$
				savedSearches = JSON.parse(savedSearches);
			}
			for (var i in savedSearches) {
				var qObj = orion.searchUtils.parseQueryStr(savedSearches[i].query)
				var duplicated = searches.some(function(search) {
						return qObj.searchStrTitle === search.name;
				});
				if(!duplicated){
					searches.push({"name": qObj.searchStrTitle, "label": savedSearches[i].name});
				}
			}
		}
		if (searches && callback) {
			callback(searches);
		}
	});
};

orion.searchUtils.getOpenSearchPref = function(serviceRegistry, callback){
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		var i;
		var openSearchPref = prefs.get("openSearchPref"); //$NON-NLS-0$
		if (typeof openSearchPref === "string") { //$NON-NLS-0$
			openSearchPref = JSON.parse(openSearchPref);
		}
		var openInNewTab = openSearchPref ? openSearchPref.openInNewTab : true;
		callback(openInNewTab);
	});
};

orion.searchUtils.setOpenSearchPref = function(serviceRegistry, openInNewTab){
	serviceRegistry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
		prefs.put("openSearchPref", {"openInNewTab": openInNewTab}); //$NON-NLS-0$
	});
};

return orion.searchUtils;
});
