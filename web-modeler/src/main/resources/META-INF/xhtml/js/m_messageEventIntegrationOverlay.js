/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils) {

			return {
				create : function(page, id) {
					var overlay = new MessageEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 *
			 */
			function MessageEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(
						MessageEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					m_utils.jQuerySelect("label[for='nameInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.name"));
					m_utils.jQuerySelect("label[for='typeSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.type"));


					m_utils.jQuerySelect("label[for='preserveQoSInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.preserveQoS"));
					m_utils.jQuerySelect("label[for='selector']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.selector"));
					m_utils.jQuerySelect("label[for='transacted']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.transacted"));
					m_utils.jQuerySelect("label[for='useSplitting']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.useSplitting"));
					m_utils.jQuerySelect("label[for='stopOnException']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.stopOnException"));
					m_utils.jQuerySelect("label[for='token']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.token"));
					m_utils.jQuerySelect("label[for='xml']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.messageEvent.xml"));
					m_utils.jQuerySelect("label[for='regex']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.messageEvent.regex"));
					m_utils.jQuerySelect("label[for='inheritNamespaceTagName']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.messageEvent.inheritNamespaceTagName"));
					
					m_utils.jQuerySelect("label[for='streaming']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.streaming"));
					m_utils.jQuerySelect("label[for='grouping']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.grouping"));
					m_utils.jQuerySelect("label[for='parallelProcessing']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.messageEvent.parallelProcessing"));

					
					this.useSplitting = this.mapInputId("useSplitting");
					this.stopOnException = this.mapInputId("stopOnException");
					this.token = this.mapInputId("token");
					this.xml = this.mapInputId("xml");
					this.regex = this.mapInputId("regex");
					this.inheritNamespaceTagName = this.mapInputId("inheritNamespaceTagName");
					this.streaming = this.mapInputId("streaming");
					this.grouping = this.mapInputId("grouping");
					this.parallelProcessing = this.mapInputId("parallelProcessing");			
					this.configurationSpan = this.mapInputId("configuration");
					this.configurationSpan.text(m_i18nUtils.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");
					this.parametersSpan.text(m_i18nUtils.getProperty("modeler.element.properties.event.parameters"));
					this.parameterDefinitionsPanel = this.mapInputId("parameterDefinitionsTable");
					this.outputBodyAccessPointInput = jQuery("#messageEvent #parametersTab #outputBodyAccessPointInput");
					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
								.create({
									scope : "messageEvent",
									submitHandler : this,
									supportsOrdering : true,
									supportsDataMappings : true,
									supportsDescriptors : false,
									supportsDataTypeSelection : true,
									supportsDocumentTypes : true,
									hideEnumerations:true,
									supportsDataPathes:true
								});

						if (this.propertiesTabs != null) {
							this.propertiesTabs.tabs();
						}
					
						this.parameterDefinitionNameInput = jQuery("#parametersTab #parameterDefinitionNameInput");
						
						this.outputBodyAccessPointInput.change(
										{
											panel : this
										},
										function(event) {
									if (!event.data.panel.validate()) {
										return;
									}

									if (event.data.panel.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
														event.data.panel.submitChanges({
									modelElement : {
										attributes : {
											"carnot:engine:camel::outBodyAccessPoint" : null
										}
									}
								});
									} else {
										/*event.data.panel
												.submitParameterDefinitionsChanges(
														"carnot:engine:camel::outBodyAccessPoint",
														event.data.panel.outputBodyAccessPointInput
																.val());*/
								event.data.panel.submitChanges({
									modelElement : {
										attributes : {
											"carnot:engine:camel::outBodyAccessPoint" : event.data.panel.outputBodyAccessPointInput
																.val()
										}
									}
								});
									}
								});
					this.typeSelect = this.mapInputId("typeSelect");
					this.nameInput = this.mapInputId("nameInput");

					this.clientIdInput = this.mapInputId("clientIdInput");
					this.selectorInput = this.mapInputId("selectorInput");
					this.transactedInput = this.mapInputId("transactedInput");
					this.preserveQoSInput = this.mapInputId("preserveQoSInput");
					this.jmsComponentIdInput= this.mapInputId("jmsComponentIdInput");	
					
					this.stopOnExceptionDisplay = jQuery("#splilTab #stopOnException");
					this.tokenDisplay = jQuery("#splilTab #token");
					this.xmlDisplay = jQuery("#splilTab #xml");
					this.regexDisplay = jQuery("#splilTab #regex");
					this.inheritNamespaceTagNameDisplay = jQuery("#splilTab #inheritNamespaceTagName");
					this.streamingDisplay = jQuery("#splilTab #streaming");
					this.groupingDisplay = jQuery("#splilTab #grouping");
					this.parallelProcessingDisplay = jQuery("#splilTab #parallelProcessing");
					this.stopOnExceptionLabel = jQuery("#splilTab #stopOnExceptionLabel");
					this.tokenLabel = jQuery("#splilTab #tokenLabel");
					this.xmlLabel = jQuery("#splilTab #xmlLabel");
					this.regexLabel = jQuery("#splilTab #regexLabel");
					this.inheritNamespaceTagNameLabel = jQuery("#splilTab #inheritNamespaceTagNameLabel");
					this.streamingLabel = jQuery("#splilTab #streamingLabel");
					this.groupingLabel = jQuery("#splilTab #groupingLabel");
					this.parallelProcessingLabel = jQuery("#splilTab #parallelProcessingLabel");
					
					this.stopOnExceptionDisplay.css("display", "none");
					this.tokenDisplay.css("display", "none");
					this.xmlDisplay.css("display", "none");
					this.regexDisplay.css("display", "none");
					this.inheritNamespaceTagNameDisplay.css("display", "none");
					this.streamingDisplay.css("display", "none");
					this.groupingDisplay.css("display", "none");
					this.parallelProcessingDisplay.css("display", "none");	
					this.stopOnExceptionLabel.css("display", "none");
					this.tokenLabel.css("display", "none");
					this.xmlLabel.css("display", "none");
					this.regexLabel.css("display", "none");
					this.inheritNamespaceTagNameLabel.css("display", "none");
					this.streamingLabel.css("display", "none");
					this.groupingLabel.css("display", "none");
					this.parallelProcessingLabel.css("display", "none");	
						
					this.registerForRouteChanges(this.useSplitting);	
					this.registerForRouteChanges(this.stopOnException);
					this.registerForRouteChanges(this.token);
					this.registerForRouteChanges(this.xml);
					this.registerForRouteChanges(this.regex);
					this.registerForRouteChanges(this.inheritNamespaceTagName);
					this.registerForRouteChanges(this.streaming);
					this.registerForRouteChanges(this.grouping);
					this.registerForRouteChanges(this.parallelProcessing);
					this.registerForRouteChanges(this.typeSelect);
					this.registerForRouteChanges(this.nameInput);
					this.registerForRouteChanges(this.clientIdInput);
					this.registerForRouteChanges(this.selectorInput);
					this.registerForRouteChanges(this.transactedInput);
					this.registerForRouteChanges(this.preserveQoSInput);
					this.registerForRouteChanges(this.jmsComponentIdInput);

				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var defaultJmsComponentId="jms";
					if(this.jmsComponentIdInput.val() != null && this.jmsComponentIdInput.val().length != 0)
					{
						defaultJmsComponentId=this.jmsComponentIdInput.val();
					}
					var uri = defaultJmsComponentId+":";

					uri += this.typeSelect.val();
					uri += ":";
					uri += this.nameInput.val();

					var separator = "?";

					if (this.clientIdInput.val() != null && this.clientIdInput.val().length != 0) {
						uri += separator + "clientId=" + encodeURIComponent(this.clientIdInput.val());
						separator = "&amp;";
					}

					if (this.selectorInput.val() != null && this.selectorInput.val().length != 0) {
						uri += separator + "selector=" + encodeURIComponent(this.selectorInput.val());
						separator = "&amp;";
					}

					if(this.transactedInput.prop("checked")== true){
						uri += separator + "transacted=";
						separator = "&amp;";
						uri += this.transactedInput.prop("checked");
					}
					if(this.preserveQoSInput.prop("checked")==true){
						uri += separator + "preserveMessageQos=";
						uri += this.preserveQoSInput.prop("checked");
					}
					//uri=uri.replace(/&/g, "&amp;");
					return uri;
				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.activate = function() {
					this.nameInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));

				/*	var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));

					this.submitOverlayChanges(parameterMappings);*/
					var parameterMappings = [];
					this.submitOverlayChanges(parameterMappings);
				};
				MessageEventIntegrationOverlay.prototype.getRouteContent = function()
				{
					var route = "<from uri=\"";

					route += this.getEndpointUri();
					route += "\"/>";
					route += this.getSplitRouteDefinitions();

					return route;
				};

				MessageEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};

				MessageEventIntegrationOverlay.prototype.getSplitRouteDefinitions = function() {
					var splitRoute ="";
					
					if (this.useSplitting.is(":checked") == true){
						splitRoute+="<split";
						if (this.streaming.is(":checked") == true){
							splitRoute+=" streaming=\"true\"";
						}

						if (this.parallelProcessing.is(":checked") == true){
							splitRoute+=" parallelProcessing=\"true\"";
						}

						if (this.stopOnException.is(":checked") == true){					
							splitRoute+=" stopOnException=\"true\"";
						}
						splitRoute+=" >";
						jQuery.trim(this.token.val());
						if (jQuery.trim(this.token.val()) == ""){
							splitRoute+="<simple>$simple{in.body}</simple>";
						}
						else {
							splitRoute+="<tokenize token=\""+this.token.val()+"\"";
							
							if (jQuery.trim(this.inheritNamespaceTagName.val()) != ""){
								splitRoute+=" inheritNamespaceTagName=\""+this.inheritNamespaceTagName.val()+"\"";
							}

							if (this.regex.is(":checked") == true){	
								splitRoute+="  regex=\"true\"";
							}

							if (this.xml.is(":checked") == true){
								splitRoute+="  xml=\"true\"";
							}
							if (jQuery.trim(this.grouping.val()) != ""){
								splitRoute+="  group=\""+this.grouping.val()+"\"";
							}
							splitRoute+=" />";
							
						}

						splitRoute+=this.getAdditionalRouteDefinitions();
						splitRoute+="</split>";
						this.stopOnExceptionDisplay.css("display", "block");
						this.tokenDisplay.css("display", "block");
						this.xmlDisplay.css("display", "block");
						this.regexDisplay.css("display", "block");
						this.inheritNamespaceTagNameDisplay.css("display", "block");
						this.streamingDisplay.css("display", "block");
						this.groupingDisplay.css("display", "block");
						this.parallelProcessingDisplay.css("display", "block");
						this.stopOnExceptionLabel.css("display", "block");
						this.tokenLabel.css("display", "block");
						this.xmlLabel.css("display", "block");
						this.regexLabel.css("display", "block");
						this.inheritNamespaceTagNameLabel.css("display", "block");
						this.streamingLabel.css("display", "block");
						this.groupingLabel.css("display", "block");
						this.parallelProcessingLabel.css("display", "block");		
						
						return splitRoute;
					} else {
						this.stopOnExceptionDisplay.css("display", "none");
						this.tokenDisplay.css("display", "none");
						this.xmlDisplay.css("display", "none");
						this.regexDisplay.css("display", "none");
						this.inheritNamespaceTagNameDisplay.css("display", "none");
						this.streamingDisplay.css("display", "none");
						this.groupingDisplay.css("display", "none");
						this.parallelProcessingDisplay.css("display", "none");
						this.stopOnExceptionLabel.css("display", "none");
						this.tokenLabel.css("display", "none");
						this.xmlLabel.css("display", "none");
						this.regexLabel.css("display", "none");
						this.inheritNamespaceTagNameLabel.css("display", "none");
						this.streamingLabel.css("display", "none");
						this.groupingLabel.css("display", "none");
						this.parallelProcessingLabel.css("display", "none");	
						return this.getAdditionalRouteDefinitions();
					}

				};

				MessageEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return "<from uri=\"" + this.getEndpointUri() + "\"/>"+ this.getSplitRouteDefinitions();
				};
				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.update = function() {
					this.outputBodyAccessPointInput.empty();
					this.outputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "' selected>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");

					
					
					for ( var n = 0; n < this.page.getEvent().parameterMappings.length; ++n) 
					{
						var accessPoint = this.page.getEvent().parameterMappings[n];
						//accessPoint.id=accessPoint.name;
						accessPoint.direction = m_constants.OUT_ACCESS_POINT;
						this.outputBodyAccessPointInput
								.append("<option value='" + accessPoint.id
										+ "'>" + accessPoint.name + "</option>");
					}
					
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					// TODO Need better URL encoding

					//route = route.replace(/&/g, "&amp;");

					var xmlDoc = jQuery.parseXML("<route>"+route+"</route>");
					var xmlObject = m_utils.jQuerySelect(xmlDoc);
					var from = m_utils.jQuerySelect(xmlObject).find("from");
					var uri = from.attr("uri");
					var uriSplitOptions = route.split("<split");
					if (uriSplitOptions.length == '1') {
						this.useSplitting.prop("checked",false);
					}
					else{
						this.useSplitting.prop("checked",true);
						
						var optionsSplit = uriSplitOptions[1].split(" ");

						if (optionsSplit) {
							for ( var n = 0; n < optionsSplit.length; ++n) {

								var optionSplit = optionsSplit[n];

								if (optionSplit.indexOf("=")>-1){
									optionSplit = optionSplit.split("=");
									var name = optionSplit[0];
									var value=optionSplit[1].substring(optionSplit[1].indexOf("\"")+1,optionSplit[1].indexOf("\"",optionSplit[1].indexOf("\"")+1));
									if (name == "stopOnException"){
										this.stopOnException.prop("checked",value);
									}
									if (name == "token"){
										this.token.val(value);
									}
									if (name == "regex"){
										this.regex.prop("checked",value);
									}
									if (name == "xml"){
										this.xml.prop("checked",value);
									}
									if (name == "inheritNamespaceTagName"){
										this.inheritNamespaceTagName.val(value);
									}
									if (name == "streaming"){
										this.streaming.prop("checked",value);
									}
									if (name == "group"){
										this.grouping.val(value);
									}
									if (name == "parallelProcessing"){
										this.parallelProcessing.prop("checked",value);
									}
								}
							}
						}	
						this.stopOnExceptionDisplay.css("display", "block");
						this.tokenDisplay.css("display", "block");
						this.xmlDisplay.css("display", "block");
						this.regexDisplay.css("display", "block");
						this.inheritNamespaceTagNameDisplay.css("display", "block");
						this.streamingDisplay.css("display", "block");
						this.groupingDisplay.css("display", "block");
						this.parallelProcessingDisplay.css("display", "block");								
						this.stopOnExceptionLabel.css("display", "block");
						this.tokenLabel.css("display", "block");
						this.xmlLabel.css("display", "block");
						this.regexLabel.css("display", "block");
						this.inheritNamespaceTagNameLabel.css("display", "block");
						this.streamingLabel.css("display", "block");
						this.groupingLabel.css("display", "block");
						this.parallelProcessingLabel.css("display", "block");		
					}
					
//					this.inheritNamespaceTagNameDisplay.val(uri[0]);
//					this.grouping.val("grouping inserted manually");
					
					if (uri) {
						var sourceAndProperties = uri.split("?");
						var source = sourceAndProperties[0];

						var sourceParts = source.split(":");
						this.jmsComponentIdInput.val(sourceParts[0]);
						this.typeSelect.val(sourceParts[1]);

						var clientName = "";


						for ( var i = 2; i < sourceParts.length; ++i) {
							if (i > 2) {
								clientName += ":";
							}
							clientName += sourceParts[i];
						}

						this.nameInput.val(clientName);

							/* parsing the last part of the URI*/


						if(sourceAndProperties[1] != null){
							var nameValues = sourceAndProperties[1].split('&');

						for ( var n = 0; n < nameValues.length; ++n) {
							var nameValue = nameValues[n].split("=");
							var name = nameValue[0];
							var value = nameValue[1];

							m_utils.debug("name: " + name);
							m_utils.debug("value: " + value);

							if (name == "clientId") {
								this.clientIdInput.val(decodeURIComponent(value));
							} else if (name == "selector") {
								this.selectorInput.val(decodeURIComponent(value));
							} else if (name == "transacted") {
								this.transactedInput.prop("checked",
										value == "true");
							} else if (name == "preserveMessageQos") {
								this.preserveQoSInput.prop("checked",
										value == "true");
							}
						}
						}/* end URI parsing*/
					}

				/*	this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
							*/
					this.outputBodyAccessPointInput
					.val(this.page.getEvent().attributes["carnot:engine:camel::outBodyAccessPoint"]);
					this.parameterDefinitionsPanel.setScopeModel(this.page
							.getModel());
					
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.validate = function() {
					
					this.grouping.removeClass("error");
					this.jmsComponentIdInput.removeClass("error");
					this.nameInput.removeClass("error");
					this.page.propertiesPanel.errorMessages=[];
					this.page.propertiesPanel.warningMessages=[];
					this.page.propertiesPanel.clearWarningMessages();
					var intRegex = /^\d+$/;
					
					if (((!intRegex.test(this.grouping.val())) || (this.grouping.val()<1))&& (!(jQuery.trim(this.grouping.val())=="")) ) {
							this.getPropertiesPanel().errorMessages.push("Field must be a positive number");
							this.grouping.addClass("error");
					}
					
					if (jQuery.trim(this.token.val())=="") {
						this.inheritNamespaceTagName.prop("disabled",true);
						this.inheritNamespaceTagName.val("");
						this.regex.prop("disabled",true);
						this.regex.prop("checked",false);
						this.xml.prop("disabled",true);
						this.xml.prop("checked",false);
						this.grouping.prop("disabled",true);
						this.grouping.val("");
						
					}else{
						this.inheritNamespaceTagName.prop("disabled",false);
						this.regex.prop("disabled",false);
						this.xml.prop("disabled",false);
						this.grouping.prop("disabled",false);
						
					}
					
					if (!(this.xml.is(":checked") == true)) {		
						this.inheritNamespaceTagName.prop("disabled",true);
						this.inheritNamespaceTagName.val("");
						
					}else{
						this.inheritNamespaceTagName.prop("disabled",false);				
					}
					
					if (m_utils.isEmptyString(this.jmsComponentIdInput.val()) ||
							this.jmsComponentIdInput.val() == m_i18nUtils
							.getProperty("modeler.general.toBeDefined")) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.jmsComponentIdInput.addClass("error");
						//this.jmsComponentIdInput.focus();
	
					}


					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.nameInput.addClass("error");
						//this.nameInput.focus();

					}
					
					if(this.page.overlay.parameterDefinitionsPanel.parameterDefinitions.length == 0) {
						this.page.propertiesPanel.warningMessages
						.push("No parameters defined for Start Event.");
						this.page.propertiesPanel.showWarningMessages();
					}
					
					if (this.page.propertiesPanel.errorMessages.length != 0){
						this.page.propertiesPanel.showErrorMessages();
						return false;
					}

					return true;
				};
			}
		});
