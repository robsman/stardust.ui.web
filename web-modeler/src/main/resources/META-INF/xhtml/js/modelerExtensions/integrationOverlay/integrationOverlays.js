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
         "bpm-modeler/js/MailIntegrationOverlay",
            "bpm-modeler/js/RestServiceOverlay",
            "bpm-modeler/js/RulesIntegrationOverlay",
            "bpm-modeler/js/ScriptingIntegrationOverlay",
            "bpm-modeler/js/SqlIntegrationOverlay",
            "bpm-modeler/js/StoredProcedureIntegrationOverlay",
            "bpm-modeler/js/m_manualTriggerIntegrationOverlay",
            "bpm-modeler/js/m_timerEventIntegrationOverlay",
            "bpm-modeler/js/m_intermediateTimerEventIntegrationOverlay",
            "bpm-modeler/js/m_intermediateErrorEventIntegrationOverlay",
            "bpm-modeler/js/m_fileEventIntegrationOverlay",
            "bpm-modeler/js/m_emailEventIntegrationOverlay",
            "bpm-modeler/js/m_messageEventIntegrationOverlay",
            "bpm-modeler/js/m_scanEventIntegrationOverlay",
            "bpm-modeler/js/m_genericCamelRouteEventIntegrationOverlay" ],
      function(GenericEndpointOverlay, MailIntegrationOverlay, RestServiceOverlay, RulesIntegrationOverlay, ScriptingIntegrationOverlay,SqlIntegrationOverlay,StoredProcedureIntegrationOverlay, m_manualTriggerIntegrationOverlay,
            m_timerEventIntegrationOverlay,  m_intermediateTimerEventIntegrationOverlay, m_intermediateErrorEventIntegrationOverlay,
            m_fileEventIntegrationOverlay, m_emailEventIntegrationOverlay,
            m_messageEventIntegrationOverlay,
            m_scanEventIntegrationOverlay,
            m_genericCamelRouteEventIntegrationOverlay) {
         return {
            applicationIntegrationOverlay : [ {
               id : "genericEndpointOverlay",
               name : "Generic Camel Endpoint",
               pageHtmlUrl : "plugins/bpm-modeler/views/modeler/genericEndpointOverlay.html",
               provider : GenericEndpointOverlay
            }, {
               id : "mailIntegrationOverlay",
               name : "E-Mail Send/Receive",
               pageHtmlUrl : "plugins/bpm-modeler/views/modeler/mailIntegrationOverlay.html",
               provider : MailIntegrationOverlay
            }, {
               id : "restServiceOverlay",
               name : "REST Service",
               pageHtmlUrl : "plugins/bpm-modeler/views/modeler/restServiceOverlay.html",
               provider : RestServiceOverlay
            },
            {
               id : "rulesIntegrationOverlay",
               name : "Rules Set Invocation",
               pageHtmlUrl: "plugins/bpm-modeler/views/modeler/rulesIntegrationOverlay.html",
               provider : RulesIntegrationOverlay
            },
            {
               id : "scriptingIntegrationOverlay",
               name : "Script Invocation",
               pageHtmlUrl: "plugins/bpm-modeler/views/modeler/scriptingIntegrationOverlay.html",
               provider : ScriptingIntegrationOverlay
            },
            {
               id : "sqlIntegrationOverlay",
               name : "SQL Invocation",
               pageHtmlUrl: "plugins/bpm-modeler/views/modeler/sqlIntegrationOverlay.html",
               provider : SqlIntegrationOverlay
            },
            {
               id : "storedProcedureIntegrationOverlay",
               name : "Stored Procedure Invocation",
               pageHtmlUrl: "plugins/bpm-modeler/views/modeler/storedProcedureIntegrationOverlay.html",
               provider : StoredProcedureIntegrationOverlay,
               visibility:"preview"
            }
            ],
            eventIntegrationOverlay : [
                  {
                     id : "manualTrigger",
                     name : "Manual Start by User",
                     eventTypes : [ "startEvent" ],
                     eventClass : "none",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/manualTriggerIntegrationOverlay.html",
                     provider : m_manualTriggerIntegrationOverlay
                  },
                  {
                     id : "timerEvent",
                     name : "Timer Event",
                     eventTypes : [ "startEvent"],
                     eventClass : "timer",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/timerEventIntegrationOverlay.html",
                     provider : m_timerEventIntegrationOverlay
                  },
                  {
                     id : "timerEvent_intermediate",
                     name : "Timer Event",
                     eventTypes : [ "intermediateEvent" ],
                     eventClass : "timer",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/intermediateTimerEventIntegrationOverlay.html",
                     provider : m_intermediateTimerEventIntegrationOverlay
                  },
                  {
                     id : "errorEvent_intermediate",
                     name : "error Event",
                     eventTypes : [ "intermediateEvent" ],
                     eventClass : "exception",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/intermediateErrorEventIntegrationOverlay.html",
                     provider : m_intermediateErrorEventIntegrationOverlay
                  },
                  {
                     id : "fileEvent",
                     name : "File Event",
                     eventTypes : [ "startEvent", "endEvent" ],
                     eventClass : "message",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/fileEventIntegrationOverlay.html",
                     provider : m_fileEventIntegrationOverlay
                  },
                  {
                     id : "emailEvent",
                     name : "EMail Event",
                     eventTypes : [ "startEvent", "endEvent" ],
                     eventClass : "message",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/emailEventIntegrationOverlay.html",
                     provider : m_emailEventIntegrationOverlay
                  },
                  {
                     id : "messageEvent",
                     name : "Message Event",
                     eventTypes : [ "startEvent", "endEvent" ],
                     eventClass : "message",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/messageEventIntegrationOverlay.html",
                     provider : m_messageEventIntegrationOverlay
                  },
                  {
                     id : "scanEvent",
                     name : "Scan Event",
                     eventTypes : [ "startEvent" ],
                     eventClass : "message",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/scanEventIntegrationOverlay.html",
                     provider : m_scanEventIntegrationOverlay
                  },
                  {
                     id : "genericCamelRouteEvent",
                     name : "Generic Camel Route Event",
                     eventTypes : [ "startEvent" ],
                     eventClass : "message",
                     pageHtmlUrl : "plugins/bpm-modeler/views/modeler/genericCamelRouteEventIntegrationOverlay.html",
                     provider : m_genericCamelRouteEventIntegrationOverlay
                  } ]
         };
      });