/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_urlUtils',
    'example-extensions/js/SqlApplicationIntegrationOverlay',
    'example-extensions/js/SocketEventIntegrationOverlay' ], function(m_urlUtils,
    SqlApplicationIntegrationOverlay, SocketEventIntegrationOverlay) {
  return {
    applicationIntegrationOverlay : [ {
      id : "sqlApplicationIntegrationOverlay",
      name : "SQL Database Connector",
      pageHtmlUrl : m_urlUtils.getContextName()
          + "/plugins/example-extensions/sqlApplicationIntegrationOverlay.html",
      provider : SqlApplicationIntegrationOverlay
    } ],
    eventIntegrationOverlay : [ {
      id : "socketEvent",
      name : "Process Event on Socket Connection",
      eventTypes : [ "startEvent" ],
      eventClass : "message",
      pageHtmlUrl : m_urlUtils.getContextName()
          + "/plugins/example-extensions/socketEventIntegrationOverlay.html",
      provider : SocketEventIntegrationOverlay
    } ]
  };
});