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
		[ "bpm-modeler/js/GenericEndpointOverlay",
				"bpm-modeler/js/RestServiceOverlay",
				"bpm-modeler/js/RulesIntegrationOverlay",
				"bpm-modeler/js/m_manualTriggerIntegrationOverlay",
				"bpm-modeler/js/m_timerEventIntegrationOverlay",
				"bpm-modeler/js/m_fileEventIntegrationOverlay",
				"bpm-modeler/js/m_emailEventIntegrationOverlay",
				"bpm-modeler/js/m_messageEventIntegrationOverlay",
				"bpm-modeler/js/m_scanEventIntegrationOverlay",
				"bpm-modeler/js/m_genericCamelRouteEventIntegrationOverlay" ],
		function(GenericEndpointOverlay, RestServiceOverlay, RulesIntegrationOverlay, m_manualTriggerIntegrationOverlay,
				m_timerEventIntegrationOverlay, m_fileEventIntegrationOverlay,
				m_emailEventIntegrationOverlay,
				m_messageEventIntegrationOverlay,
				m_scanEventIntegrationOverlay,
				m_genericCamelRouteEventIntegrationOverlay) {
			return {
				applicationIntegrationOverlay : [ {
					id : "genericEndpointOverlay",
					name : "Generic Camel Endpoint",
					pageHtmlUrl : "genericEndpointOverlay.html",
					provider : GenericEndpointOverlay
				}, {
					id : "restServiceOverlay",
					name : "REST Service",
					pageHtmlUrl : "restServiceOverlay.html",
					provider : RestServiceOverlay,
					visibility : "preview"
				},
				{
					id : "rulesIntegrationOverlay",
					name : "Rules Set Invocation",
					pageHtmlUrl: "rulesIntegrationOverlay.html",
					provider : RulesIntegrationOverlay,
					visibility : "preview"
				}],
				eventIntegrationOverlay : [
						{
							id : "manualTrigger",
							name : "Manual Start by User",
							eventTypes : [ "startEvent" ],
							eventClass : "none",
							pageHtmlUrl : "manualTriggerIntegrationOverlay.html",
							provider : m_manualTriggerIntegrationOverlay
						},
						{
							id : "timerEvent",
							name : "Timer Event",
							eventTypes : [ "startEvent", "intermediateEvent" ],
							eventClass : "timer",
							pageHtmlUrl : "timerEventIntegrationOverlay.html",
							provider : m_timerEventIntegrationOverlay
						},
						{
							id : "fileEvent",
							name : "File Event",
							eventTypes : [ "startEvent", "intermediateEvent",
									"endEvent" ],
							eventClass : "message",
							pageHtmlUrl : "fileEventIntegrationOverlay.html",
							provider : m_fileEventIntegrationOverlay
						},
						{
							id : "emailEvent",
							name : "EMail Event",
							eventTypes : [ "startEvent", "intermediateEvent",
									"endEvent" ],
							eventClass : "message",
							pageHtmlUrl : "emailEventIntegrationOverlay.html",
							provider : m_emailEventIntegrationOverlay
						},
						{
							id : "messageEvent",
							name : "Message Event",
							eventTypes : [ "startEvent", "intermediateEvent",
									"endEvent" ],
							eventClass : "message",
							pageHtmlUrl : "messageEventIntegrationOverlay.html",
							provider : m_messageEventIntegrationOverlay
						},
						{
							id : "scanEvent",
							name : "Scan Event",
							eventTypes : [ "startEvent" ],
							eventClass : "message",
							pageHtmlUrl : "scanEventIntegrationOverlay.html",
							provider : m_scanEventIntegrationOverlay
						},
						{
							id : "genericCamelRouteEvent",
							name : "Generic Camel Route Event",
							eventTypes : [ "startEvent" ],
							eventClass : "message",
							pageHtmlUrl : "genericCamelRouteEventIntegrationOverlay.html",
							provider : m_genericCamelRouteEventIntegrationOverlay
						} ]
			};
		});