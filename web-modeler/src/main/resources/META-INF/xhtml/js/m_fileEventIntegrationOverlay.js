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
					var overlay = new FileEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function FileEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(FileEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);
 
				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					m_utils.jQuerySelect("configuration")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.configuration"));
					m_utils.jQuerySelect("parameters")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.parameters"));
					m_utils.jQuerySelect("label[for='directoryNameInput']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.directoryName"));
			        m_utils.jQuerySelect("label[for='fileNameInput']")
			        		.text(
			        				m_i18nUtils
			        						.getProperty("modeler.element.properties.fileEvent.fileName"));
					m_utils.jQuerySelect("label[for='recursiveInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.recursive"));
					m_utils.jQuerySelect("label[for='initialIntervalInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.initialInterval"));
					m_utils.jQuerySelect("label[for='postProcessingSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.postProcessing"));
					m_utils.jQuerySelect("label[for='alwaysConsumeInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.alwaysConsume"));
					m_utils.jQuerySelect("label[for='useSplitting']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.useSplitting"));
					m_utils.jQuerySelect("label[for='stopOnException']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.stopOnException"));
					m_utils.jQuerySelect("label[for='token']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.token"));
					m_utils.jQuerySelect("label[for='xml']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.fileEvent.xml"));
					m_utils.jQuerySelect("label[for='regex']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.fileEvent.regex"));			
					m_utils.jQuerySelect("label[for='inheritNamespaceTagName']")
							.text(
					           	m_i18nUtils
					           			.getProperty("modeler.element.properties.fileEvent.inheritNamespaceTagName"));
					
					m_utils.jQuerySelect("label[for='streaming']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.streaming"));
					m_utils.jQuerySelect("label[for='grouping']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.grouping"));
					m_utils.jQuerySelect("label[for='parallelProcessing']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.parallelProcessing"));
					
					
					this.useSplitting = this.mapInputId("useSplitting");
					this.stopOnException = this.mapInputId("stopOnException");
					this.token = this.mapInputId("token");
					this.xml = this.mapInputId("xml");
					this.regex = this.mapInputId("regex");
					this.inheritNamespaceTagName = this.mapInputId("inheritNamespaceTagName");
					this.streaming = this.mapInputId("streaming");
					this.grouping = this.mapInputId("grouping");
					this.parallelProcessing = this.mapInputId("parallelProcessing");
					
					
					this.directoryNameInput = this.mapInputId("directoryNameInput");
					this.fileNameInput = this.mapInputId("fileNameInput");
					this.recursiveInput = this.mapInputId("recursiveInput");
					this.initialIntervalInput = this
							.mapInputId("initialIntervalInput");
					this.initialIntervalUnitSelect = this
							.mapInputId("initialIntervalUnitSelect");
					this.repeatIntervalInput = this
							.mapInputId("repeatIntervalInput");
					this.repeatIntervalUnitSelect = this
							.mapInputId("repeatIntervalUnitSelect");

					this
							.initializeIntervalUnitSelect(this.initialIntervalUnitSelect);
					this
							.initializeIntervalUnitSelect(this.repeatIntervalUnitSelect);

					this.lockBehaviorSelect = this
							.mapInputId("lockBehaviorSelect");
					this.postProcessingSelect = this
							.mapInputId("postProcessingSelect");
					this.alwaysConsumeInput = this
							.mapInputId("alwaysConsumeInput");

					this.parameterDefinitionsPanel = this.mapInputId("parameterDefinitionsTable");
					this.outputBodyAccessPointInput = jQuery("#fileEvent #parametersTab #outputBodyAccessPointInput");
					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
								.create({
									scope : "fileEvent",
									submitHandler : this,
									supportsOrdering : true,
									supportsDataMappings : true,
									supportsDescriptors : false,
									supportsDataTypeSelection : true,
									supportsDocumentTypes : true,
									hideEnumerations:true
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
					this.registerForRouteChanges(this.directoryNameInput);
					this.registerForRouteChanges(this.fileNameInput);
					this.registerForRouteChanges(this.recursiveInput);
					this.registerForRouteChanges(this.initialIntervalInput);
					this.registerForRouteChanges(this.initialIntervalUnitSelect);
					this.registerForRouteChanges(this.repeatIntervalInput);
					this.registerForRouteChanges(this.repeatIntervalUnitSelect);
					this.registerForRouteChanges(this.lockBehaviorSelect);
					this.registerForRouteChanges(this.postProcessingSelect);
					this.registerForRouteChanges(this.alwaysConsumeInput);
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "file://";
					//if(this.fileOrDirectoryNameInput!=null && this.fileOrDirectoryNameInput.val()!="Please specify ..."){
						uri += this.directoryNameInput.val();
					//}
					
					var separator = "?";
					
					if(this.fileNameInput != null && this.fileNameInput.val().length != 0){
						uri += separator + "fileName="+this.fileNameInput.val();
						separator = "&amp;";
						
					}
					
					if (this.recursiveInput.is(":checked") == true) {
						uri += separator + "recursive="
								+ this.recursiveInput.is(":checked");
						separator = "&amp;";

					}

					if (this.getIntervalInMilliseconds(
							this.initialIntervalInput.val(),
							this.initialIntervalUnitSelect.val()) != null) {
						uri += separator
								+ "initialDelay="
								+ this.getIntervalInMilliseconds(
										this.initialIntervalInput.val(),
										this.initialIntervalUnitSelect.val());
						separator = "&amp;";
					}

					if (this.getIntervalInMilliseconds(this.repeatIntervalInput
							.val(), this.repeatIntervalUnitSelect.val()) != null) {
						uri += separator
								+ "delay="
								+ this.getIntervalInMilliseconds(
										this.repeatIntervalInput.val(),
										this.repeatIntervalUnitSelect.val());
						separator = "&amp;";
					}
					if (this.lockBehaviorSelect.val() == "none") {
						// nothing to do
					} else {
						if (this.lockBehaviorSelect.val() == "markerFile") {
							uri += separator + "readLock=markerFile";
							separator = "&amp;";
						} else {
							if (this.lockBehaviorSelect.val() == "changed") {
								uri += separator + "readLock=changed";
								separator = "&amp;";
							}
						}
					}

					/*
					 * uri += "&consumer.alwaysConsume=" +
					 * this.alwaysConsumeInput.prop("checked");
					 */

					if (this.postProcessingSelect.val() == "noop") {
						uri += "&amp;noop=true";
						uri += "&amp;delete=false";
					} else if (this.postProcessingSelect.val() == "delete") {
						uri += "&amp;noop=false";
						uri += "&amp;delete=true";
					}
					return uri;
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.activate = function() {
					this.directoryNameInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));
					this.initialIntervalInput.val(5000);
					this.repeatIntervalInput.val(5000);

				/*	var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));*/
					/*
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Name",
					 * "CamelFileName", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Name Only",
					 * "CamelFileNameOnly", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Absolute File Path",
					 * "CamelFileAbsolutePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Path",
					 * "CamelFileAbsolutePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Relative Path",
					 * "CamelFileRelativePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Parent",
					 * "CamelFileParent", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Last Modified Date",
					 * "CamelFileLastModified", "String"));
					 */
					var parameterMappings = [];
					this.submitOverlayChanges(parameterMappings);
					
				};
				FileEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};
				FileEventIntegrationOverlay.prototype.getSplitRouteDefinitions = function() {
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

				FileEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return "<from uri=\"" + this.getEndpointUri() + "\"/>"
							+ this.getSplitRouteDefinitions();
				};
				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.update = function() {
                     
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

					// route = route.replace(/&/g,"&amp;");

					var xmlDoc = jQuery
							.parseXML("<route>" + route + "</route>");
					var xmlObject = m_utils.jQuerySelect(xmlDoc);
					var from = m_utils.jQuerySelect(xmlObject).find("from");
					var uri = from.attr("uri");
					var uri = uri.split("//");
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
					
					if (uri[1] != null) {
						uri = uri[1].split("?");
						this.directoryNameInput.val(uri[0]);

						if (uri[1] != null) {
							var options = uri[1].split("&");

							if (options) {
								for ( var n = 0; n < options.length; ++n) {
									var option = options[n];

									option = option.split("=");

									var name = option[0];
									var value = option[1];

									if (name == "fileName") {
										this.fileNameInput.val(value);
									}else if (name == "recursive") {
										this.recursiveInput.prop("checked",
												value);
									} else if (name == "initialDelay") {
										var intervalWithUnit = this
												.getIntervalWithUnit(value);

										this.initialIntervalInput
												.val(intervalWithUnit.value);
										this.initialIntervalUnitSelect
												.val(intervalWithUnit.unit);
									} else if (name == "delay") {
										var intervalWithUnit = this
												.getIntervalWithUnit(value);

										this.repeatIntervalInput
												.val(intervalWithUnit.value);
										this.repeatIntervalUnitSelect
												.val(intervalWithUnit.unit);
										/*
										 * } else if (name ==
										 * "consumer.alwaysConsume") {
										 * this.alwaysConsumeInput.prop("checked",
										 * value == "true");
										 */
									} else if (name == "noop") {
										if (value == "true") {
											this.postProcessingSelect
													.val("noop");
										}
									} else if (name == "delete") {
										if (value == "true") {
											this.postProcessingSelect
													.val("delete");
										}
									} else if (name == "readLock") {
										this.lockBehaviorSelect.val(value)
									}
								}
							}
						}
					}

					this.outputBodyAccessPointInput
					.val(this.page.getEvent().attributes["carnot:engine:camel::outBodyAccessPoint"]);
					this.parameterDefinitionsPanel.setScopeModel(this.page
							.getModel());
					
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				/*	this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);*/
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.validate = function() {
					this.grouping.removeClass("error");
					this.directoryNameInput.removeClass("error");
					this.page.propertiesPanel.errorMessages=[];
					this.page.propertiesPanel.warningMessages=[];
					this.page.propertiesPanel.clearWarningMessages();
					var intRegex = /^\d+$/;
					
					if (((!intRegex.test(this.grouping.val()))|| (this.grouping.val()<1))&& (!(jQuery.trim(this.grouping.val())=="")) ) {
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
					
					if (m_utils.isEmptyString(this.directoryNameInput.val())) {
						this.page.propertiesPanel.errorMessages
								.push("Directory name must not be empty.");
						this.directoryNameInput.addClass("error");

						
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