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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel) {

			return {
				create : function(page, id) {
					var overlay = new GenericCamelRouteIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function GenericCamelRouteIntegrationOverlay() {
				/**
				 * 
				 */
				GenericCamelRouteIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.page = page;
					this.id = id;
					this.endpointUriPrefix = this
							.mapInputId("endpointUriPrefix");
					this.endpointUriTextarea = this
							.mapInputId("endpointUriTextarea");
					this.additionalRouteTextarea = this
							.mapInputId("routeTextarea");
					this.dataPathTextInput = this
							.mapInputId("dataPathTextInput");
					this.endpointAccessPointsSelectInput = this
							.mapInputId("endpointAccessPointsSelectInput");
					this.camelEndpointDataMappingDataSelectInput = this
							.mapInputId("camelEndpointDataMappingDataSelectInput");

					// this.endpointTypeSelectInput
					// .change(
					// {
					// "callbackScope" : this
					// },
					// function(event) {
					// event.data.callbackScope.endpointUriPrefix
					// .empty();
					// event.data.callbackScope.endpointUriTextarea
					// .empty();
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .empty();
					//
					// if (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "fileEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"file:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("directoryName[?options]");
					//
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileName\">CamelFileName</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileNameOnly\">CamelFileNameOnly</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileAbsolute\">CamelFileAbsolute</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileAbsolutePath\">CamelFileAbsolutePath</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFilePath\">CamelFilePath</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileRelativePath\">CamelFileRelativePath</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileParent\">CamelFileParent</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileLength\">CamelFileLength</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelFileLastModified\">CamelFileLastModified</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("</optgroup>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Output Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("</optgroup>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "ftpEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"ftp:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("directoryName[?options]");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "pop3Endpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"pop3:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("[]host:port?options");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"body\">Mail Body</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"mailAttachments\">Mail
					// Attachments</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "smtpEndpoint") {
					//
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"smtp:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("[]host:port?options");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"body\">Mail Body</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"mailAttachments\">Mail
					// Attachments</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "imapEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"imap:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("[]host:port?options");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"body\">Mail Body</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"mailAttachments\">Mail
					// Attachments</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "jmsEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"jms:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("MyQueue?exchangePattern=InOut");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "webServiceEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"cxfrs:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("//bean://rsServer/");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "restletEndpoint") {
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"Content-Type\">Content-Type</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelHttpMethod\">CamelHttpMethod</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelHttpQuery\">CamelHttpQuery</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelHttpResponseCode\">CamelHttpResponseCode</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelHttpUri\">CamelHttpUri</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelRestletLogin\">CamelRestletLogin</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelRestletPassword\">CamelRestletPassword</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelRestletRequest\">CamelRestletRequest</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"org.restlet.*\">org.restlet.*</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Output Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelRestletResponse\">CamelRestletResponse</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "hazelcastEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"hazelcast:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("%sfoo\", HazelcastConstants.MAP_PREFIX");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "quickfixEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"quickfix:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("myconfig.cfg?sessionID=FIX.4.2:MARKET->TRADER");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"Message\">(Map)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"EventCategory\">EventCategory
					// (Enumeration)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"SessionID\">SessionID
					// (String)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"MessageType\">MessageType
					// (String)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"DataDictionary\">DataDictionary
					// (String)</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "quartzEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"quartz:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("//timerName?options");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"calendar\">calendar</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"fireTime\">fireTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"jobDetail\">jobDetail</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"jobInstance\">jobInstance</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"jobRuntTime\">jobRuntTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"mergedJobDataMap\">mergedJobDataMap</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"nextFireTime\">nextFireTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"previousFireTime\">previousFireTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"refireCount\">refireCount</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"previousFireTime\">previousFireTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"result\">result</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"scheduledFireTime\">scheduledFireTime</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"scheduler\">scheduler</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"trigger\">trigger</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"triggerName\">triggerName</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"triggerGroup\">triggerGroup</option>");
					// } else if
					// (event.data.callbackScope.endpointTypeSelectInput
					// .val() == "springWSEndpoint") {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"spring-ws:");
					// event.data.callbackScope.endpointUriTextarea
					// .append("rootqname:
					// http://example.com/GetFoo?endpointMapping=#endpointMapping");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"xmlBody\">xmlBody
					// (Map)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelSpringWebserviceEndpointUri\">CamelSpringWebserviceEndpointUri
					// (String)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelSpringWebserviceSoapAction\">CamelSpringWebserviceSoapAction
					// (String)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option
					// value=\"CamelSpringWebserviceAddressingAction\">CamelSpringWebserviceAddressingAction
					// (URI)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Output Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"xmlBody\">xmlBody
					// (Map)</option>");
					// } else {
					// event.data.callbackScope.endpointUriPrefix
					// .append("&lt;from uri=\"");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Input Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"message\">message
					// (Structure)</option>");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<optgroup label=\"Output Data\">");
					// event.data.callbackScope.endpointAccessPointsSelectInput
					// .append("<option value=\"message\">message
					// (Structure)</option>");
					// }
					// });
					// this.dataPathTextInput
					// ./*
					// * bind( "keydown", function( event ) { if (
					// * event.keyCode === jQuery.ui.keyCode.TAB &&
					// * jQuery(this).data( "autocomplete" ).menu.active ) {
					// * event.preventDefault(); } }) .
					// */autocomplete({
					// minLength : 0,
					// source : function(request, response) {
					// m_utils.debug("source function:");
					// m_utils.debug(request);
					// // delegate back to autocomplete, but extract the
					// // last
					// // term
					// // response(jQuery.ui.autocomplete.filter(availableTags,
					// // extractLast(request.term)));
					// response(m_dataTraversal.getStepOptions(null,
					// request.term));
					// },
					// focus : function() {
					// m_utils.debug("focus function");
					//
					// // prevent value inserted on focus
					//
					// return false;
					// },
					// select : function(event, ui) {
					// m_utils.debug("select function");
					// m_utils.debug("this.value = " + this.value);
					// m_utils.debug("ui.item.value = " + ui.item.value);
					//
					// var steps = m_dataTraversal.split(this.value);
					//
					// steps.pop();
					// steps.push(ui.item.value);
					//
					// if (steps.length > 1) {
					// this.value = steps.join(".");
					// } else {
					// this.value = steps[0];
					// }
					//
					// return false;
					// }
					// });
				};

				/**
				 * 
				 */
				GenericCamelRouteIntegrationOverlay.prototype.mapInputId = function(
						inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				GenericCamelRouteIntegrationOverlay.prototype.activate = function() {
				};

				/**
				 * 
				 */
				GenericCamelRouteIntegrationOverlay.prototype.update = function() {
					this.populateDataSelectInputs();
					var xmlDoc = jQuery
							.parseXML("<route>"
									+ this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"]
									+ "</route>");
					var xmlObject = jQuery(xmlDoc);

					var fromUri = "";
					var additionalRoutes = "";

					jQuery(xmlObject).find("from").each(function() {
						fromUri = jQuery(this).attr("uri");
					});

					jQuery(xmlObject).find("route").each(function() {
					jQuery(this).children().each(function() {
//						var toUri = jQuery(this).attr("uri");
//
//						if (toUri.indexOf("ipp") < 0) {
//							additionalRoutes += jQuery(this).content();
//						}

						m_utils.debug(jQuery(this));
						additionalRoutes += jQuery(this).toString();
					});
					});					

					this.endpointUriTextarea.val(fromUri);
					//this.additionalRouteTextarea.val(additionalRoutes);
				};

				/**
				 * 
				 */
				GenericCamelRouteIntegrationOverlay.prototype.populateDataSelectInputs = function() {
					// this.camelEndpointDataMappingDataSelectInput.empty();
					//
					// var models = this.propertiesPanel.models;
					//
					// for ( var n in models) {
					// var model = models[n];
					//
					// for ( var m in model.dataItems) {
					// var content = "<option value='"
					// + model.dataItems[m].getFullId() + "'>"
					// + model.name + "/"
					// + model.dataItems[m].name + "</option>";
					//
					// this.documentDataList.append(content);
					// this.camelEndpointDataMappingDataSelectInput
					// .append(content);
					// }
					// }
				};
			}
		});