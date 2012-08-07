/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_dialog", "m_basicPropertiesPage", "m_dataTraversal" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_dialog, m_basicPropertiesPage, m_dataTraversal) {
			return {
				create : function(propertiesPanel) {
					var page = new EventBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function EventBasicPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(EventBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.startEventPanel = this.mapInputId("startEventPanel");
					this.scanInput = this.mapInputId("scanInput");
					this.documentDataList = this.mapInputId("documentDataList");
					this.participantOutput = this
							.mapInputId("participantOutput");
					this.eventTypeSelectInput = this
							.mapInputId("eventTypeSelectInput");
					this.manualTriggerPanel = this
							.mapInputId("manualTriggerPanel");
					this.scanTriggerPanel = this.mapInputId("scanTriggerPanel");
					this.camelTriggerPanel = this
							.mapInputId("camelTriggerPanel");
					this.endpointTypeSelectInput = this
							.mapInputId("endpointTypeSelectInput");
					this.endpointUriPrefix = this
							.mapInputId("endpointUriPrefix");
					this.endpointUriTextarea = this
							.mapInputId("endpointUriTextarea");
					this.dataPathTextInput = this
							.mapInputId("dataPathTextInput");
					this.endpointAccessPointsSelectInput = this
							.mapInputId("endpointAccessPointsSelectInput");
					this.camelEndpointDataMappingDataSelectInput = this
							.mapInputId("camelEndpointDataMappingDataSelectInput");

					this.eventTypeSelectInput
							.append("<option value=\"manualTrigger\">Manual Process Start</option>");
					this.eventTypeSelectInput
							.append("<option value=\"scanTrigger\">Process Start via Scan</option>");
					this.eventTypeSelectInput
							.append("<option value=\"camelTrigger\">Process Start via Camel Endpoint</option>");

					m_dialog.makeVisible(this.manualTriggerPanel);
					m_dialog.makeInvisible(this.scanTriggerPanel);
					m_dialog.makeInvisible(this.camelTriggerPanel);

					// Initialize callbacks

					this.eventTypeSelectInput
							.change(
									{
										"callbackScope" : this
									},
									function(event) {
										if (jQuery(this).val() == "manualTrigger") {
											m_dialog
													.makeVisible(event.data.callbackScope.manualTriggerPanel);
											m_dialog
													.makeInvisible(event.data.callbackScope.scanTriggerPanel);
											m_dialog
													.makeInvisible(event.data.callbackScope.camelTriggerPanel);
										} else if (jQuery(this).val() == "scanTrigger") {
											m_dialog
													.makeInvisible(event.data.callbackScope.manualTriggerPanel);
											m_dialog
													.makeVisible(event.data.callbackScope.scanTriggerPanel);
											m_dialog
													.makeInvisible(event.data.callbackScope.camelTriggerPanel);
										} else if (jQuery(this).val() == "camelTrigger") {
											m_dialog
													.makeInvisible(event.data.callbackScope.manualTriggerPanel);
											m_dialog
													.makeInvisible(event.data.callbackScope.scanTriggerPanel);
											m_dialog
													.makeVisible(event.data.callbackScope.camelTriggerPanel);
										}
									});

					this.scanInput.click({
						"callbackScope" : this
					}, function(event) {
						if (event.data.callbackScope.scanInput.is(":checked")) {
							event.data.callbackScope.documentDataList
									.removeAttr("disabled");
						} else {
							event.data.callbackScope.documentDataList.attr(
									"disabled", true);
						}
					});

					this.endpointTypeSelectInput
							.change(
									{
										"callbackScope" : this
									},
									function(event) {
										event.data.callbackScope.endpointUriPrefix
												.empty();
										event.data.callbackScope.endpointUriTextarea
												.empty();
										event.data.callbackScope.endpointAccessPointsSelectInput
												.empty();

										if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "fileEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"file:");
											event.data.callbackScope.endpointUriTextarea
													.append("directoryName[?options]");

											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileName\">CamelFileName</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileNameOnly\">CamelFileNameOnly</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileAbsolute\">CamelFileAbsolute</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileAbsolutePath\">CamelFileAbsolutePath</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFilePath\">CamelFilePath</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileRelativePath\">CamelFileRelativePath</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileParent\">CamelFileParent</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileLength\">CamelFileLength</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelFileLastModified\">CamelFileLastModified</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("</optgroup>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Output Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("</optgroup>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "ftpEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"ftp:");
											event.data.callbackScope.endpointUriTextarea
													.append("directoryName[?options]");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "pop3Endpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"pop3:");
											event.data.callbackScope.endpointUriTextarea
													.append("[]host:port?options");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"body\">Mail Body</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"mailAttachments\">Mail Attachments</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "smtpEndpoint") {

											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"smtp:");
											event.data.callbackScope.endpointUriTextarea
													.append("[]host:port?options");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"body\">Mail Body</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"mailAttachments\">Mail Attachments</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "imapEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"imap:");
											event.data.callbackScope.endpointUriTextarea
													.append("[]host:port?options");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"body\">Mail Body</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"mailAttachments\">Mail Attachments</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "jmsEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"jms:");
											event.data.callbackScope.endpointUriTextarea
													.append("MyQueue?exchangePattern=InOut");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "webServiceEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"cxfrs:");
											event.data.callbackScope.endpointUriTextarea
													.append("//bean://rsServer/");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "restletEndpoint") {
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"Content-Type\">Content-Type</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelHttpMethod\">CamelHttpMethod</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelHttpQuery\">CamelHttpQuery</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelHttpResponseCode\">CamelHttpResponseCode</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelHttpUri\">CamelHttpUri</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelRestletLogin\">CamelRestletLogin</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelRestletPassword\">CamelRestletPassword</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelRestletRequest\">CamelRestletRequest</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"org.restlet.*\">org.restlet.*</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Output Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelRestletResponse\">CamelRestletResponse</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "hazelcastEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"hazelcast:");
											event.data.callbackScope.endpointUriTextarea
													.append("%sfoo\", HazelcastConstants.MAP_PREFIX");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "quickfixEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"quickfix:");
											event.data.callbackScope.endpointUriTextarea
													.append("myconfig.cfg?sessionID=FIX.4.2:MARKET->TRADER");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"Message\">(Map)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"EventCategory\">EventCategory (Enumeration)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"SessionID\">SessionID (String)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"MessageType\">MessageType (String)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"DataDictionary\">DataDictionary (String)</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "quartzEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"quartz:");
											event.data.callbackScope.endpointUriTextarea
													.append("//timerName?options");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"calendar\">calendar</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"fireTime\">fireTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"jobDetail\">jobDetail</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"jobInstance\">jobInstance</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"jobRuntTime\">jobRuntTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"mergedJobDataMap\">mergedJobDataMap</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"nextFireTime\">nextFireTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"previousFireTime\">previousFireTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"refireCount\">refireCount</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"previousFireTime\">previousFireTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"result\">result</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"scheduledFireTime\">scheduledFireTime</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"scheduler\">scheduler</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"trigger\">trigger</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"triggerName\">triggerName</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"triggerGroup\">triggerGroup</option>");
										} else if (event.data.callbackScope.endpointTypeSelectInput
												.val() == "springWSEndpoint") {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"spring-ws:");
											event.data.callbackScope.endpointUriTextarea
													.append("rootqname: http://example.com/GetFoo?endpointMapping=#endpointMapping");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"xmlBody\">xmlBody (Map)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelSpringWebserviceEndpointUri\">CamelSpringWebserviceEndpointUri (String)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelSpringWebserviceSoapAction\">CamelSpringWebserviceSoapAction (String)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"CamelSpringWebserviceAddressingAction\">CamelSpringWebserviceAddressingAction (URI)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Output Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"xmlBody\">xmlBody (Map)</option>");
										} else {
											event.data.callbackScope.endpointUriPrefix
													.append("&lt;from uri=\"");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Input Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"message\">message (Structure)</option>");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<optgroup label=\"Output Data\">");
											event.data.callbackScope.endpointAccessPointsSelectInput
													.append("<option value=\"message\">message (Structure)</option>");
										}
									});
					this.dataPathTextInput./*
											 * bind( "keydown", function( event ) {
											 * if ( event.keyCode ===
											 * jQuery.ui.keyCode.TAB &&
											 * jQuery(this).data( "autocomplete"
											 * ).menu.active ) {
											 * event.preventDefault(); } }) .
											 */autocomplete({
						minLength : 0,
						source : function(request, response) {
							m_utils.debug("source function:");
							m_utils.debug(request);
							// delegate back to autocomplete, but extract the
							// last
							// term
							// response(jQuery.ui.autocomplete.filter(availableTags,
							// extractLast(request.term)));
							response(m_dataTraversal.getStepOptions(null,
									request.term));
						},
						focus : function() {
							m_utils.debug("focus function");

							// prevent value inserted on focus

							return false;
						},
						select : function(event, ui) {
							m_utils.debug("select function");
							m_utils.debug("this.value = " + this.value);
							m_utils.debug("ui.item.value = " + ui.item.value);

							var steps = m_dataTraversal.split(this.value);

							steps.pop();
							steps.push(ui.item.value);

							if (steps.length > 1) {
								this.value = steps.join(".");
							} else {
								this.value = steps[0];
							}

							return false;
						}
					});
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.populateDataSelectInputs = function() {
					this.documentDataList.empty();

					this.documentDataList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					this.camelEndpointDataMappingDataSelectInput.empty();

					var models = this.propertiesPanel.models;

					for ( var n in models) {
						var model = models[n];

						for ( var m in model.dataItems) {
							var content = "<option value='"
									+ model.dataItems[m].getFullId() + "'>"
									+ model.name + "/"
									+ model.dataItems[m].name + "</option>";

							this.documentDataList.append(content);
							this.camelEndpointDataMappingDataSelectInput
									.append(content);
						}
					}
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();
					this.populateDataSelectInputs();

					if (this.propertiesPanel.element.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						this.startEventPanel.removeAttr("class");
						this.participantOutput.empty();

						if (this.propertiesPanel.participant != null) {
							this.participantOutput.append("Started by <b>"
									+ this.propertiesPanel.participant.name
									+ ".</b>");
						} else {
							this.participantOutput
									.append("Starting participant to be defined.</b>");

						}

						if (this.propertiesPanel.element.modelElement.documentDataId != null) {
							this.documentDataList
									.val(this.propertiesPanel.element.modelElement.documentDataId);
							this.scanInput.val(true);
						} else {
							this.scanInput.val(false);
							this.documentDataList
									.val(m_constants.TO_BE_DEFINED);
						}
					} else {
						this.startEventPanel.attr("class", "invisible");
					}
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Event name must not be empty.");
						this.nameInput.addClass("error");

						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};

				// <carnot:Trigger Oid="10049" Id="FileTrigger" Name="File
				// Trigger " Type="camel">
				// <carnot:AccessPoints>
				// <carnot:AccessPoint Oid="10063" Id="message" Name="message"
				// Direction="OUT" Type="primitive">
				// <carnot:Attributes>
				// <carnot:Attribute Name="carnot:engine:browsable"
				// Value="false" Type="boolean"/>
				// <carnot:Attribute Name="carnot:engine:visibility"
				// Value="Public"/>
				// <carnot:Attribute Name="authorization:data.readDataValues"
				// Value="__carnot_internal_all_permissions__"/>
				// <carnot:Attribute Name="carnot:engine:type" Value="String"
				// Type="org.eclipse.stardust.engine.core.pojo.data.Type"/>
				// </carnot:Attributes>
				// </carnot:AccessPoint>
				// <carnot:AccessPoint Oid="10213" Id="headers" Name="headers"
				// Direction="OUT" Type="serializable">
				// <carnot:Attributes>
				// <carnot:Attribute Name="carnot:engine:className"
				// Value="org.eclipse.stardust.engine.extensions.camel.runtime.GenericMessage"/>
				// <carnot:Attribute Name="carnot:engine:browsable" Value="true"
				// Type="boolean"/>
				// </carnot:Attributes>
				// </carnot:AccessPoint>
				// </carnot:AccessPoints>
				// <carnot:DataFlows>
				// <carnot:DataFlow Oid="10061">
				// <carnot:DataRef Id="BodyContent"/>
				// <carnot:AccessPointRef Id="message"/>
				// </carnot:DataFlow>
				// </carnot:DataFlows>
				// <carnot:Attributes>
				// <carnot:Attribute Name="carnot:engine:camel::camelContextId"
				// Value="camelContext"/>
				// <carnot:Attribute Name="carnot:engine:camel::endpointURI"
				// Value="file://C:/tmp/camel/sdt?delay=5000"/>
				// <carnot:Attribute Name="carnot:engine:camel::camelRouteExt"
				// Value="&lt;from
				// uri=&quot;file://C:/tmp/camel/sdt?delay=5000&quot;
				// /&gt;&#13;&#10;&lt;convertBodyTo
				// type=&quot;java.lang.String&quot;/&gt;&#13;&#10;&lt;to
				// uri=&quot;ipp:direct&quot; /&gt;"/>
				// <carnot:Attribute
				// Name="carnot:engine:camel::endpointTypeClass"
				// Value="org.eclipse.stardust.engine.extensions.camel.runtime.GenericEndpoint"/>
				// </carnot:Attributes>
			}
		});