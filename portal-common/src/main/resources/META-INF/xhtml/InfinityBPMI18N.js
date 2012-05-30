/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
var InfinityBPMI18N = function() {
	
	function createPlugin(initParam) {
		var propertiesMap;
		
		//Initialization of properties
		var initProp = {
			propFilePath : "",
			propFileBaseName : "",
			localeRetrievalURL : "",
			locale : ""
		};

		function initializeFromSingleEndPoint(initObj) {
			initializePropertiesMapFromSingleEndPoint(initObj.singleEndPoint)
			
		}

		function initializePropertiesMapFromSingleEndPoint(singleEndPoint) {
			propertiesMap = {};
			ajaxRequest("GET", singleEndPoint, false,  new function() {
				return {
					successCallback : function(val) {
						populatePropertiesMap(val, true);
					},
					failureCallback : function(val) {
						alert("InfinityBPMI18N loading properties failed.");
					}
				};
			});
		}

		function initialize(initObj) {
			for (var i in initObj) {
				if (initProp.hasOwnProperty(i))
				{
					initProp[i] = initObj[i];
				}
			}
			if (initProp.locale == "")
			{
				initializeLocale();
			}
			setPropertyFile();
			initializePropertiesMap();
		}
		
		/* 
		 * The server call should return the locale.
		 *  
		 */ 
		function initializeLocale() {
				ajaxRequest("GET", initProp.localeRetrievalURL, false,
						new function() {
							return {
								successCallback : function(val) {
									initProp.locale = val;
								},
								failureCallback : function(val) {
									initProp.locale = "en";
								}
							};
						});
		}
		
		function setPropertyFile() {
			initProp.propertyFile = initProp.propFilePath + initProp.propFileBaseName + ".properties";
			if (initProp.locale != "" && initProp.locale != "en")
			{
				initProp.propertyFile = initProp.propFilePath + initProp.propFileBaseName + "_" + initProp.locale + ".properties";
			}
		}
		
		function initializePropertiesMapDefault() {
			initProp.locale = "en";
			setPropertyFile();
			initializePropertiesMap(true);
		} 
		
		function populatePropertiesMap(val, isFinalAttemptToRead) {
			var propText;
			propText = val;
			if (propText && "" != propText)
			{
			    var lines = propText.split(/\r\n|\r|\n/);
			    for (var i = 0; i < lines.length; i++) {
			    	var keyVals = lines[i].split('=');
			    	if (keyVals && "" != keyVals
			    			&& keyVals.length == 2
			    			&& trim(keyVals[0]).indexOf('#') != 0) {
			    		propertiesMap[trim(keyVals[0])] = replaceUicodeChars(keyVals[1]);	
			    	}
			    }
			}
			else
			{
				if (!isFinalAttemptToRead) {
					initializePropertiesMapDefault();
				}
			}		
		}
		
		function initializePropertiesMap(isFinalAttemptToRead) {
			propertiesMap = {};
			ajaxRequest("GET", initProp.propertyFile, false,  new function() {
				return {
					successCallback : function(val) {
						populatePropertiesMap(val, isFinalAttemptToRead);
					},
					failureCallback : function(val) {
						if (!isFinalAttemptToRead) {
							initializePropertiesMapDefault();
						}
					}
				};
			});
		}
		
		function ajaxRequest(reqType, url, async, func) {
		    var xmlHttpReq = false;
		    if (window.XMLHttpRequest) {
		        xmlHttpReq = new XMLHttpRequest();
		    }
		    else if (window.ActiveXObject) {
		        xmlHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
		    }
		    
		    xmlHttpReq.open(reqType, url, async);
		    //xmlHttpReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		    
		    // In older browser versions (IE7 / FF3.6) the xmlHttpReq.onreadystatechange method is not called
		    // in case of sync request
		    // hence providing callbacks only in case of async request. 
		    if (true == async)
		    {
			    xmlHttpReq.onreadystatechange = function() {
					  if(xmlHttpReq.readyState == 4) {
						  if (xmlHttpReq.status == 200)
						  {
							  func.successCallback(xmlHttpReq.responseText);  
						  }
						  else
						  {
							  func.failureCallback();
						  }
					  }			  
					}		    	
		    }

		    xmlHttpReq.send(null);
		    
		    // In older browser versions (IE7 / FF3.6) the xmlHttpReq.onreadystatechange method is not called
		    // in case of sync requests
		    // hence reading directly from response text rather than providing a callback. 
		    if (true != async)
		    {
		    	try {
					  if (xmlHttpReq.status == 200)
					  {
						  func.successCallback(xmlHttpReq.responseText);  
					  }
					  else
					  {
						  func.failureCallback();
					  }
		    	} catch (e) {
		    		func.failureCallback();
		    	}
		    }
		}

		function replaceUicodeChars(str) {
			var index = -1
			if ((index = str.search(/\\u([a-fA-F0-9]){4}/)) >= 0) {
				return replaceUicodeChars(str.substring(0, index) + String.fromCharCode(parseInt(str.substring(index + 2, index + 6), 16)) + str.substring(index + 6));
			} else {
				return str;
			}
		}

		function trim(val)
		{
			return lTrim(tTrim(val));
		}
		
		function lTrim(val)
		{
			return val.replace(/\s*((\S+\s*)*)/, "$1");
		}
		
		function tTrim(val)
		{
			return val.replace(/((\s*\S+)*)\s*/, "$1");
		}

		if (initParam.singleEndPoint) {
			initializeFromSingleEndPoint(initParam);
		} else {
			initialize(initParam);
		}

		return {
			getProperty : function(key, defaultValue) {
				var val = propertiesMap[key];
				if (!val && defaultValue)
				{
					val = defaultValue;
				}
				
				return val;
			} // Interface function
		}; // Interface InfinityBPMI18N.<plugin-name>
	}

	return {
		// Following parameter are needed for successful initialization
		//	propFilePath (Mandatory) - path to the bundle's directory
		//	propFileBaseName (Mandatory) - base name for property files
		//	localeRetrievalURL (Optional if locale is provided) - a URL end-point that would return locale
		//	locale (Optional if localeRetrievalURL is provided) - locale
		//	pluginName (Mandatory) - a plugin name with which to access properties
		initPluginProps : function (initParam) {
			if (initParam.pluginName && ((initParam.propFilePath
					&& initParam.propFileBaseName
					&& (initParam.locale || initParam.localeRetrievalURL)) || initParam.singleEndPoint)) {
				this[initParam.pluginName] = createPlugin(initParam);
			} else {
				alert("InfinityBPMI18N initialialization failed. Mandatory parameter(s) missing.");
			}
		} // Interface function
	}; //Interface InfinityBPMI18N
}();

