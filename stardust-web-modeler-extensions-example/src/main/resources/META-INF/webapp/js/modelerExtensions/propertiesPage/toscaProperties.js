/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
    [ 'example-extensions/js/ToscaProcessPropertiesPage', 'bpm-modeler/js/m_urlUtils' ],
    function(ToscaProcessPropertiesPage, m_urlUtils) {
      return {
        propertiesPage : [ {
          panelId : "processPropertiesPanel",
          id : "toscaProcessPropertiesPage",
          provider : ToscaProcessPropertiesPage,
          pageHtmlUrl : m_urlUtils.getContextName()
              + "/plugins/example-extensions/toscaProcessPropertiesPage.html",
          visibility : "always"
        } ]
      };
    });