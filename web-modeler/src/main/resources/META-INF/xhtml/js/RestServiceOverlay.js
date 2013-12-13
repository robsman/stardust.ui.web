define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce"],
		function(m_utils, m_i18nUtils, m_constants, m_commandsController,
				m_command, m_model, m_accessPoint, m_typeDeclaration,
				m_parameterDefinitionsPanel, m_codeEditorAce) {
			return {
				create : function(view) {
					
					var overlay = new RestServiceOverlay();

					overlay.initialize(view);
					
					return overlay;
				}
			};

			/**
			 * 
			 */
			function RestServiceOverlay() {
				/**
				 * 
				 */
				RestServiceOverlay.prototype.initialize = function(view) {
					
					this.view = view;

					this.view.insertPropertiesTab("restServiceOverlay",
							"parameters", "Parameters",
							"plugins/bpm-modeler/images/icons/database_link.png");
					this.view.insertPropertiesTab("restServiceOverlay", "test",
							"Test", "plugins/bpm-modeler/images/icons/application-run.png");
					
					this.view.insertPropertiesTab("restServiceOverlay", "security",
							"Security", "plugins/bpm-modeler/images/icons/server-key.png"); 

					this.uriInput = m_utils.jQuerySelect("#restServiceOverlay #uriInput");
					this.queryStringLabel = m_utils.jQuerySelect("#restServiceOverlay #queryStringLabel");
					this.commandSelect = m_utils.jQuerySelect("#restServiceOverlay #commandSelect");
					this.requestTypeSelect = m_utils.jQuerySelect("#restServiceOverlay #requestTypeSelect");
					this.responseTypeSelect = m_utils.jQuerySelect("#restServiceOverlay #responseTypeSelect");
					this.crossDomainInput = m_utils.jQuerySelect("#restServiceOverlay #crossDomainInput");
					this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
					this.runButton = m_utils.jQuerySelect("#testTab #runButton");
					this.inputDataTextarea = m_utils.jQuerySelect("#testTab #inputDataTextarea");
					this.outputDataTextarea = m_utils.jQuerySelect("#testTab #outputDataTextarea");
					this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");

					this.securityModeSelect = m_utils.jQuerySelect("#securityTab #securityModeSelect");
					this.httpBasicAuthUserInput = m_utils.jQuerySelect("#securityTab #httpBasicAuthUserInput");
					this.httpBasicAuthPwdInput = m_utils.jQuerySelect("#securityTab #httpBasicAuthPwdInput");
					this.httpBasicAuthUsingCVInput = m_utils.jQuerySelect("#securityTab #httpBasicAuthUsingCVInput");
					
					this.customSecurityTokenKeyInput = m_utils.jQuerySelect("#securityTab #customSecurityTokenKeyInput");
					this.customSecurityTokenValueInput = m_utils.jQuerySelect("#securityTab #customSecurityTokenValueInput");
					this.customSecurityTokenUsingCVInput = m_utils.jQuerySelect("#securityTab #customSecurityTokenUsingCVInput");
					
					m_utils.jQuerySelect("label[for='securityModeSelect']").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.label"));
					
					m_utils.jQuerySelect("#securityModeSelect option[value='none']").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.none.label"));
					
					m_utils.jQuerySelect("#securityModeSelect option[value='httpBasicAuth']").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.httpBasicAuth.label"));
					
					m_utils.jQuerySelect("#securityModeSelect option[value='customSecTok']").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.securityModeSelect.customSecTok.label"));
			
					m_utils.jQuerySelect("#httpBasicAuthenticationHintLabel").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.httpBasicAuthenticationHint.label"));
					
					m_utils.jQuerySelect("#customSecurityTokenHintLabel").text(m_i18nUtils
							.getProperty("modeler.model.applicationOverlay.rest.security.customSecurityTokenHint.label"));
					
					
					this.resetButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.resetButton.title"));
					this.runButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.runButton.title"));
					m_utils.jQuerySelect("label[for='inputDataTextArea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.inputDataTextArea.label"));
					m_utils.jQuerySelect("label[for='outputDataTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.outputDataTextArea.label"));

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								supportsDocumentTypes : false
							});

					var self = this;
					
					var httpHeadersJson = this.getApplication().attributes["stardust:restServiceOverlay::httpHeaders"];
					
					if (!httpHeadersJson)
					{
						httpHeadersJson = "[]";
					}
					
					this.httpHeaders = JSON.parse(httpHeadersJson);
					
					this.initializeHeaderAttributesTable();
					
					this.uriInput.change(function() {
						self.submitChanges();
					});
					this.commandSelect.change(function() {
						self.submitChanges();
					});
					this.requestTypeSelect.change(function() {
						self.submitChanges();
					});
					this.responseTypeSelect.change(function() {
						self.submitChanges();
					});
					this.crossDomainInput.change(function() {
						self.submitChanges();
					});
					this.inputBodyAccessPointInput
							.change(function() {
								
								if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
									self.submitSingleAttributeChange(
											"carnot:engine:camel::inBodyAccessPoint",
												null);
								} else {
									self.submitSingleAttributeChange(
											"carnot:engine:camel::inBodyAccessPoint",
												self.inputBodyAccessPointInput
													.val());
								}
							});
					this.outputBodyAccessPointInput
							.change(function() {
								
								if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
									self.submitSingleAttributeChange(
											"carnot:engine:camel::outBodyAccessPoint",
												null);
								} else {
									self.submitSingleAttributeChange(
											"carnot:engine:camel::outBodyAccessPoint",
												self.outputBodyAccessPointInput
													.val());
								}
							});
					
					this.securityModeSelect.change(function() {
						self.setSecurityMode(self.securityModeSelect.val());
						self.submitChanges();
					});
					
					this.httpBasicAuthUserInput.change(function() {
						self.submitChanges();
					});
					
					this.httpBasicAuthPwdInput.change(function() {
						self.submitChanges();
					});
					
					this.httpBasicAuthUsingCVInput.change(function() {
						self.submitChanges();
					});
					
					this.customSecurityTokenKeyInput.change(function() {
						self.submitChanges();
					});
					
					this.customSecurityTokenValueInput.change(function() {
						self.submitChanges();
					}); 
					
					this.customSecurityTokenUsingCVInput.change(function() {
						self.submitChanges();
					});
					
					this.runButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										jQuery.support.cors = true;

										var dataType = "html";

										if (view.responseTypeSelect.val() === "application/xml") {
											dataType = "xml";
										}
										else if (view.responseTypeSelect.val() === "application/json") {
											dataType = "json";
										}

										jQuery
												.ajax(
														{
															type : view.commandSelect
																	.val(),
															url : view
																	.getInvocationUri(),
															contentType : view.requestTypeSelect
																	.val(),
															dataType : dataType,
															data : view
																	.getRequestData(),
															crossDomain : true
														})
												.done(
														function(data) {
															view.outputDataTextarea
																	.val(JSON
																			.stringify(data));
														})
												.fail(
														function() {
															view.outputDataTextarea
																	.val("Could not retrieve data from "
																			+ view
																					.getInvocationUri()
																			+ " via "
																			+ view.commandSelect
																					.val()
																			+ " command.");
														});
									});
					this.resetButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view.inputDataTextarea.empty();
										view.outputDataTextarea.empty();

										var inputData = "{";

										for ( var n = 0; n < view
												.getApplication().contexts.application.accessPoints.length; ++n) {
											var parameterDefinition = view
													.getApplication().contexts.application.accessPoints[n];

											if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
												continue;
											}

											if (n > 0) {
												inputData += ", ";
											}

											if (parameterDefinition.dataType == "primitive") {
												inputData += parameterDefinition.id;
												inputData += ": \"\"";
											} else if (parameterDefinition.dataType == "struct") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} else if (parameterDefinition.dataType == "dmsDocument") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} else {
												// Deal with primitives
											}
										}

										inputData += "}";

										view.inputDataTextarea
												.append(inputData);
									});
				};
				
				RestServiceOverlay.prototype.setSecurityMode = function(securityMode) {

					if (!securityMode) 
					{
						securityMode = "none";
					}

					this.securityModeSelect.val(securityMode);

					m_utils.jQuerySelect("#httpBasicAuthenticationDiv").hide();
					m_utils.jQuerySelect("#customSecurityTokenDiv").hide();
					
					if (securityMode === "httpBasicAuth") 
					{
						m_utils.jQuerySelect("#httpBasicAuthenticationDiv").show();
						this.customSecurityTokenKeyInput.val("");
						this.customSecurityTokenValueInput.val(""); 
						this.customSecurityTokenUsingCVInput.prop('checked', false);
						this.submitSingleAttributeChange("stardust:restServiceOverlay::httpHeaders", null);
					} 
					else if (securityMode === "customSecTok") 
					{
						m_utils.jQuerySelect("#customSecurityTokenDiv").show();
						this.httpBasicAuthUserInput.val("");
						this.httpBasicAuthPwdInput.val("");
						this.httpBasicAuthUsingCVInput.prop('checked', false);
					}
					else
					{
						this.httpBasicAuthUserInput.val("");
						this.httpBasicAuthPwdInput.val("");
						this.httpBasicAuthUsingCVInput.prop('checked', false);
						this.customSecurityTokenKeyInput.val("");
						this.customSecurityTokenValueInput.val("");
						this.customSecurityTokenUsingCVInput.prop('checked', false);
						
						this.submitSingleAttributeChange("stardust:restServiceOverlay::httpHeaders", null);
					}
				};
				
				RestServiceOverlay.prototype.initializeHeaderAttributesTable = function() {
					
					m_utils.jQuerySelect("#securityTab #addHeaderButton").click
					(
						{
							page : this,
						},
						function(event) 
						{
							event.data.page.httpHeaders.push
							(
								{
									"headerName" : "New" + (event.data.page.httpHeaders.length + 1), 
									"headerValue" : "New" + (event.data.page.httpHeaders.length + 1)
								}
							);
							
							// submit changes
							event.data.page.submitSingleAttributeChange(
									"stardust:restServiceOverlay::httpHeaders", JSON.stringify(event.data.page.httpHeaders));
							
							// update route
							event.data.page.submitSingleAttributeChange(
									"carnot:engine:camel::routeEntries", event.data.page.getRoute());
							
							event.data.page.refreshHeaderAttributesTable();
						
						}	
					);
					
					this.refreshHeaderAttributesTable();
				};
				
				RestServiceOverlay.prototype.refreshHeaderAttributesTable = function() {
					
					m_utils.jQuerySelect("table#headerAttributesTable tbody").empty();

					for ( var n = 0; n < this.httpHeaders.length; ++n) {
						
						var row = m_utils.jQuerySelect("<tr></tr>");
						var cell = m_utils.jQuerySelect("<td></td>");
						
						row.append(cell);

						var button = m_utils.jQuerySelect(
								"<input type='image' title='Delete' alt='Delete' class='toolbarButton' src='plugins/bpm-modeler/images/icons/delete.png'/>");

						button.click
						(
							{
								page : this,
								headerName : this.httpHeaders[n].headerName
							},
							function(event) 
							{
								newHttpHeaders = [];
								
								for (var h = 0; h < event.data.page.httpHeaders.length; ++h) 
								{
									if (event.data.headerName !== event.data.page.httpHeaders[h].headerName)
									{
										newHttpHeaders.push(event.data.page.httpHeaders[h]);
									}
								}
								
								event.data.page.httpHeaders = newHttpHeaders;
							
								// submit changes
								event.data.page.submitSingleAttributeChange(
										"stardust:restServiceOverlay::httpHeaders", JSON.stringify(newHttpHeaders));
								
								// update route
								event.data.page.submitSingleAttributeChange(
										"carnot:engine:camel::routeEntries", event.data.page.getRoute());
								
								event.data.page.refreshHeaderAttributesTable();
							}
						);

						cell.append(button);
						
						cell = m_utils.jQuerySelect("<td></td>");

						
						headerNameInput = m_utils.jQuerySelect("<input type='text' class='cellEditor' value='"+ this.httpHeaders[n].headerName +"'></input>");
						headerNameInput.change
						(
							{
								page : this,
								headerName : this.httpHeaders[n].headerName
							},
							function(event) 
							{
								var oldValue = event.data.headerName;
								var newValue = event.target.value;
								
								for ( var h = 0; h < event.data.page.httpHeaders.length; ++h) 
								{
									if (event.data.page.httpHeaders[h].headerName === oldValue)
									{
										event.data.page.httpHeaders[h].headerName = newValue;
									}
								}
								
								// submit changes
								event.data.page.submitSingleAttributeChange(
										"stardust:restServiceOverlay::httpHeaders", JSON.stringify(event.data.page.httpHeaders));
								
								// update route
								event.data.page.submitSingleAttributeChange(
										"carnot:engine:camel::routeEntries", event.data.page.getRoute());
								
								event.data.page.refreshHeaderAttributesTable();
							}
						);
//						headerNameInput.keydown
//						(
//							{
//								page : this
//							},	
//							function(event) 
//							{
//								if (event.which == 9) { //tab key pressed
//									if(!event.shiftKey){
////										event.data.page.preserveFocus(m_utils.jQuerySelect(this), true);
//									}
//								}
//							}
//						);
						
						cell.append(headerNameInput);
						row.append(cell);
						
						cell = m_utils.jQuerySelect("<td></td>");
						
						headerValueInput = m_utils.jQuerySelect("<input type='text' class='cellEditor' value='"+ this.httpHeaders[n].headerValue +"'></input>");
						headerValueInput.change
						(
							{
								page : this,
								headerValue : this.httpHeaders[n].headerValue
							},
							function(event) 
							{
								var oldValue = event.data.headerValue;
								var newValue = event.target.value;
								
								for ( var h = 0; h < event.data.page.httpHeaders.length; ++h) 
								{
									if (event.data.page.httpHeaders[h].headerValue === oldValue)
									{
										event.data.page.httpHeaders[h].headerValue = newValue;
									}
								}
								
								// submit changes
								event.data.page.submitSingleAttributeChange(
										"stardust:restServiceOverlay::httpHeaders", JSON.stringify(event.data.page.httpHeaders));
								
								// update route
								event.data.page.submitSingleAttributeChange(
										"carnot:engine:camel::routeEntries", event.data.page.getRoute());
								
								event.data.page.refreshHeaderAttributesTable();
							}
						);
						
						cell.append(headerValueInput);
						row.append(cell);
						
						m_utils.jQuerySelect(
								"table#headerAttributesTable tbody")
								.append(row);
					}
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getQueryString = function() {
					var queryString = "";

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
								|| this.uriInput.val().indexOf("{" + accessPoint.id + "}") >= 0 
								|| accessPoint.id == this.inputBodyAccessPointInput.val()) 
						{
							continue;
						}

						if (n > 0) {
							queryString += "&";
						}

						queryString += accessPoint.id;
						queryString += "=<i>";
						queryString += accessPoint.id;
						queryString += "</i>";
					}

					return queryString;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getInvocationUri = function() {
					var uri = this.uriInput.val();

					if (this.inputDataTextarea.val()) {
						var input = eval("(" + this.inputDataTextarea.val()
								+ ")");
						var start = true;

						for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
							var accessPoint = this.getApplication().contexts.application.accessPoints[n];

							if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
									|| accessPoint.id == this.inputBodyAccessPointInput
											.val()) {
								continue;
							}

							if (this.uriInput.val().indexOf(
									"{" + accessPoint.id + "}") >= 0) {
								uri = uri.replace("{" + accessPoint.id + "}",
										input[accessPoint.id]);
							} else {
								if (start) {
									uri += "?";
									start = false;
								} else {
									uri += "&";
								}

								uri += accessPoint.id;
								uri += "=";
								uri += input[accessPoint.id];
							}
						}
					}

					return uri;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getRequestData = function() {
					if (this.inputBodyAccessPointInput.val() != m_constants.TO_BE_DEFINED
							&& this.inputDataTextarea.val()) {
						var input = eval("(" + this.inputDataTextarea.val()
								+ ")");

						return input[this.inputBodyAccessPointInput.val()];
					}

					return null;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};
				

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getRoute = function() {

					var uri = this.uriInput.val();
					var start = true;
					var route = "";
					var httpUri = "";
					var httpQuery = "";
					
					if (uri.indexOf("?") >= 0)
					{
						// there is already a ? defined in URI
						start = false;
					}

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
								|| accessPoint.id == this.inputBodyAccessPointInput
										.val()) {
							continue;
						}

						if (this.uriInput.val().indexOf(
								"{" + accessPoint.id + "}") >= 0)
						{
							uri = uri.replace("{" + accessPoint.id + "}",
									"$simple{header." + accessPoint.id + "}");

							route += "<setHeader headerName='" + accessPoint.id + "'>";
							route += "<javaScript>encodeURIComponent(request.headers.get('" + accessPoint.id + "'))</javaScript>";
							route += "</setHeader>";
						}
						else
						{
							if (start)
							{
								uri += "?";
								start = false;
							}
							else
							{
								uri += "&";
							}

							uri += accessPoint.id;
							uri += "=";
							uri += "$simple{header." + accessPoint.id + "}";

							route += "<setHeader headerName='" + accessPoint.id + "'>";
							route += "<javaScript>encodeURIComponent(request.headers.get('" + accessPoint.id + "'))</javaScript>";
							route += "</setHeader>";
						}
					}

					uri = uri.replace(/&/g, "&amp;");
					
					// add addtional headers defined via custom security token
					if (this.securityModeSelect.val() === "customSecTok")
					{
						var cKey = this.customSecurityTokenKeyInput.val();
						route += "<setHeader headerName='" + m_utils.encodeXmlPredfinedCharacters(cKey) + "'>";
						
						route += "<constant>";
						
						if (this.customSecurityTokenUsingCVInput.prop("checked"))
						{
							route += "${"; 
						}
						
						var cValue = this.customSecurityTokenValueInput.val();
						route += m_utils.encodeXmlPredfinedCharacters(cValue);
						
						if (this.customSecurityTokenUsingCVInput.prop("checked"))
						{
							route += ":Password}"; 
						}
						
						route += "</constant>";
						route += "</setHeader>";
						
						for (var h=0; h<this.httpHeaders.length; h++)
						{
							var hName = this.httpHeaders[h].headerName;
							hName = m_utils.encodeXmlPredfinedCharacters(hName);
							
							var hValue = this.httpHeaders[h].headerValue;
							hValue = m_utils.encodeXmlPredfinedCharacters(hValue);
							
							route += "<setHeader headerName='" + hName + "'>";
							route += "<constant>" + hValue + "</constant>"; 
							route += "</setHeader>";
						}
					}
					

					if (uri.indexOf("?") > 0)
					{
						var index = uri.indexOf("?");
						httpUri = uri.substring(0, index);
                        httpQuery = uri.substring(index + 1); 						
						route += "<setHeader headerName='CamelHttpQuery'>";
						route += "<simple>" + httpQuery + "</simple>";
						route += "</setHeader>";
					}
					else
					{
						httpUri = uri;
					}

					route += "<setHeader headerName='CamelHttpUri'>";
					route += "<simple>" + httpUri + "</simple>";
					route += "</setHeader>";
					route += "<setHeader headerName='CamelHttpMethod'>";
					route += "<constant>" + this.commandSelect.val() + "</constant>";
					route += "</setHeader>";

					if (this.requestTypeSelect.val() === "application/json") {
						route += "<to uri='bean:bpmTypeConverter?method=toJSON' />";
					} else if (this.requestTypeSelect.val() === "application/xml") {
						route += "<to uri='bean:bpmTypeConverter?method=toXML' />";
					}

					route += "<to uri='http://isoverwritten";
					
					if (this.securityModeSelect.val() === "httpBasicAuth")
					{
						route += "?authMethod=Basic";
						route += "&amp;authUsername=" + this.httpBasicAuthUserInput.val();
						route += "&amp;authPassword=";
						
						if (this.httpBasicAuthUsingCVInput.prop("checked"))
						{
							route += "${";
							route += this.httpBasicAuthPwdInput.val(); // TODO: Verify if URL encoding is required.
							route += ":Password}"; // TODO: Add :password type information
						}
						else
						{
							route += this.getHttpBasicAuthRawPwd(); // TODO: Verify if URL encoding is required.
						}
					}
					
					route += "'/>";
					

					if (this.responseTypeSelect.val() === "application/json") {
						route += "<to uri='bean:bpmTypeConverter?method=fromJSON' />";
					} else if (this.responseTypeSelect.val() === "application/xml") {
						route += "<to uri='bean:bpmTypeConverter?method=fromXML' />";
					}

					return route;
				};
				
				RestServiceOverlay.prototype.getHttpBasicAuthRawPwd = function() {
					
					if(!m_utils.isEmptyString(this.httpBasicAuthPwdInput.val()))
					{
						var rawPwd = m_utils.encodeXmlPredfinedCharacters(this.httpBasicAuthPwdInput.val());
						rawPwd = this.convertPasswordToConfigVariable(rawPwd, true);
							rawPwd = "RAW(" + rawPwd + ")";	
						return rawPwd;
					}
					return this.httpBasicAuthPwdInput.val();
				};
				
				
				RestServiceOverlay.prototype.getHttpBasicAuthOriginePwd = function(rawPwd) {	
					
					if(!m_utils.isEmptyString(rawPwd))
					{
						var firstIdex = rawPwd.indexOf("(");
						var lastIdex = rawPwd.lastIndexOf(")");
						var originePwd = rawPwd.substring(firstIdex+1,lastIdex);
						originePwd = m_utils.decodeXmlPredfinedCharacters(originePwd);
						originePwd = this.convertConfigVariableToPassword(originePwd);
						return originePwd;
					}
					return this.httpBasicAuthPwdInput.val();
				};
				
				RestServiceOverlay.prototype.convertConfigVariableToPassword = function(originePwd) {	
					
					if(!m_utils.isEmptyString(originePwd))
					{
						if(originePwd.indexOf("${") > -1){
							var firstIdex = originePwd.indexOf("${");
							var lastIdex = originePwd.lastIndexOf(":");
							originePwd = originePwd.substring(firstIdex+2,lastIdex);
						}
						return originePwd;
					}
					return originePwd;
				};
				
				RestServiceOverlay.prototype.convertPasswordToConfigVariable = function(originePwd, basicAuthentication) {	
					if(basicAuthentication  == true && this.httpBasicAuthUsingCVInput.prop("checked")){
						originePwd = "${"+originePwd+ ":Password}";	
					}else if(basicAuthentication == false && this.customSecurityTokenUsingCVInput.prop("checked")){
						originePwd = "${"+originePwd+ ":Password}";
					}
					
					return originePwd;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "restServiceOverlay"
								}
							}, true);
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.update = function() {
					
					this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
					
					this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					this.inputBodyAccessPointInput.empty();
					this.inputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "'>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.IN_ACCESS_POINT) {
							continue;
						}

						this.inputBodyAccessPointInput.append("<option value='"
								+ accessPoint.id + "'>" + accessPoint.name
								+ "</option>");
					}

					this.outputBodyAccessPointInput.empty();
					this.outputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "' selected>"
							+ m_i18nUtils.getProperty("None") // I18N
							+ "</option>");

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						this.outputBodyAccessPointInput
								.append("<option value='" + accessPoint.id
										+ "'>" + accessPoint.name + "</option>");
					}

					this.inputBodyAccessPointInput.val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
					this.outputBodyAccessPointInput.val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
					this.uriInput.val(this.getApplication().attributes["stardust:restServiceOverlay::uri"]);
					this.queryStringLabel.empty();
					this.queryStringLabel.append(this.getQueryString());
					this.commandSelect.val(this.getApplication().attributes["stardust:restServiceOverlay::command"]);
					this.requestTypeSelect.val(this.getApplication().attributes["stardust:restServiceOverlay::requestType"]);
					this.responseTypeSelect.val(this.getApplication().attributes["stardust:restServiceOverlay::responseType"]);
					this.crossDomainInput.prop("checked", this.getApplication().attributes["stardust:restServiceOverlay::crossDomain"]);
					this.setSecurityMode(this.getApplication().attributes["stardust:restServiceOverlay::securityMode"]);
					this.httpBasicAuthUserInput.val(this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthUser"]);
					this.httpBasicAuthUsingCVInput.prop("checked", this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthCV"]);
					this.httpBasicAuthPwdInput.val(this.getHttpBasicAuthOriginePwd(this.getApplication().attributes["stardust:restServiceOverlay::httpBasicAuthPwd"]));
					this.customSecurityTokenKeyInput.val(this.getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenKey"]);
					this.customSecurityTokenUsingCVInput.prop("checked", this.getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenCV"]);
					this.customSecurityTokenValueInput.val(this.convertConfigVariableToPassword(this.getApplication().attributes["stardust:restServiceOverlay::customSecurityTokenValue"]));
					
					
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.submitChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "restServiceOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this.getRoute(),
									"stardust:restServiceOverlay::uri" : this.uriInput.val(),
									"stardust:restServiceOverlay::command" : this.commandSelect.val(),
									"stardust:restServiceOverlay::requestType" : this.requestTypeSelect.val(),
									"stardust:restServiceOverlay::responseType" : this.responseTypeSelect.val(),
									"stardust:restServiceOverlay::crossDomain" : this.crossDomainInput.prop("checked"),
									"stardust:restServiceOverlay::securityMode" : this.securityModeSelect.val(),
									"stardust:restServiceOverlay::httpBasicAuthUser" : this.httpBasicAuthUserInput.val(),
									"stardust:restServiceOverlay::httpBasicAuthPwd" : this.getHttpBasicAuthRawPwd(),
									"stardust:restServiceOverlay::httpBasicAuthCV" : this.httpBasicAuthUsingCVInput.prop("checked") 
										? this.httpBasicAuthUsingCVInput.prop("checked") : null,
									"stardust:restServiceOverlay::customSecurityTokenKey" : this.customSecurityTokenKeyInput.val(),
									"stardust:restServiceOverlay::customSecurityTokenValue" : this.convertPasswordToConfigVariable(this.customSecurityTokenValueInput.val(), false),
									"stardust:restServiceOverlay::customSecurityTokenCV" : this.customSecurityTokenUsingCVInput.prop("checked") 
										? this.customSecurityTokenUsingCVInput.prop("checked") : null
								}
							});
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								contexts : {
									application : {
										accessPoints : parameterDefinitionsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "restServiceOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this.getRoute(),
									"stardust:restServiceOverlay::uri" : this.uriInput.val(),
									"stardust:restServiceOverlay::command" : this.commandSelect.val(),
									"stardust:restServiceOverlay::requestType" : this.requestTypeSelect.val(),
									"stardust:restServiceOverlay::responseType" : this.responseTypeSelect.val(),
									"stardust:restServiceOverlay::crossDomain" : this.crossDomainInput.prop("checked"),
									"stardust:restServiceOverlay::securityMode" : this.securityModeSelect.val(),
									"stardust:restServiceOverlay::httpBasicAuthUser" : this.httpBasicAuthUserInput.val(),
									"stardust:restServiceOverlay::httpBasicAuthPwd" : this.getHttpBasicAuthRawPwd(),
									"stardust:restServiceOverlay::httpBasicAuthCV" : this.httpBasicAuthUsingCVInput.prop("checked") 
										? this.httpBasicAuthUsingCVInput.prop("checked") : null,
									"stardust:restServiceOverlay::customSecurityTokenKey" : this.customSecurityTokenKeyInput.val(),
									"stardust:restServiceOverlay::customSecurityTokenValue" : this.convertPasswordToConfigVariable(this.customSecurityTokenValueInput.val(), false),
									"stardust:restServiceOverlay::customSecurityTokenCV" : this.customSecurityTokenUsingCVInput.prop("checked") 
										? this.customSecurityTokenUsingCVInput.prop("checked") : null
								}
							}, true);
				};
				
				RestServiceOverlay.prototype.submitSingleAttributeChange = function(attribute, value) {
					
					if (this.getModelElement().attributes[attribute] != value) {
						var modelElement = {
							attributes : {}
						};
						modelElement.attributes[attribute] = value;
						this.view.submitChanges(modelElement, true);
					}
				};
				

				/**
				 * 
				 */
				RestServiceOverlay.prototype.validate = function() {

					this.uriInput.removeClass("error");
					this.httpBasicAuthUserInput.removeClass("error");
					this.httpBasicAuthPwdInput.removeClass("error");
					this.httpBasicAuthPwdInput.removeClass("warn");
					this.customSecurityTokenKeyInput.removeClass("error");
					this.customSecurityTokenValueInput.removeClass("error");
					this.customSecurityTokenValueInput.removeClass("warn");

					if (m_utils.isEmptyString(this.uriInput.val())) 
					{
						this.view.errorMessages.push("URI must not be empty."); // TODO I18N
						this.uriInput.addClass("error");
						return false;
					}
					
					if ("httpBasicAuth" === this.securityModeSelect.val())
					{
						if (m_utils.isEmptyString(this.httpBasicAuthUserInput.val())) 
						{
							this.view.errorMessages.push("Username for HTTP Basic Authentication must not be empty."); // TODO I18N
							this.httpBasicAuthUserInput.addClass("error");
							return false;
						}
						
						if (m_utils.isEmptyString(this.httpBasicAuthPwdInput.val())) 
						{
							this.view.errorMessages.push("Pasword for HTTP Basic Authentication must not be empty."); // TODO I18N
							this.httpBasicAuthPwdInput.addClass("error");
							return false;
						}
						else if (this.httpBasicAuthPwdInput.val().indexOf("${") == 0)
						{
							this.view.errorMessages.push("You should be using a configuration variable."); // TODO I18N
							this.httpBasicAuthPwdInput.addClass("warn");
						}
					}
					
					if ("customSecTok" === this.securityModeSelect.val())
					{
						if (m_utils.isEmptyString(this.customSecurityTokenKeyInput.val())) 
						{
							this.view.errorMessages.push("Token Key for Custom Security Token must not be empty.");
							this.customSecurityTokenKeyInput.addClass("error");
							return false;
						}
						
						if (m_utils.isEmptyString(this.customSecurityTokenValueInput.val())) 
						{
							this.view.errorMessages.push("Token Value for Custom Security Token must not be empty.");
							this.customSecurityTokenValueInput.addClass("error");
							return false;
						}
						else if (this.customSecurityTokenValueInput.val().indexOf("${") == 0)
						{
							this.view.errorMessages.push("You should be using a configuration variable.");
							this.customSecurityTokenValueInput.addClass("warn");
						}
					}

					return true;
				};
			}
		});