/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

define(["bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
    "bpm-modeler/js/m_user", "bpm-modeler/js/m_dialog",
    "bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils",
    "bpm-modeler/js/m_model", "bpm-modeler/js/m_ruleSetsHelper"], function(
        m_utils, m_constants, m_user, m_dialog, m_propertiesPage, m_i18nUtils,
        m_model, m_ruleSetsHelper) {
  return {
    create: function(propertiesPanel, extCfg) {
      var page = new PropertiesPageProvider(propertiesPanel, extCfg);
      return page;
    }
  };

  function PropertiesPageProvider(propertiesPanel, extCfg) {

    var propertiesPage = m_propertiesPage.createPage(propertiesPanel, extCfg);
    m_utils.inheritFields(this, propertiesPage);
    m_utils.inheritMethods(PropertiesPageProvider.prototype, propertiesPage);

    PropertiesPageProvider.prototype.setElement = function() {
      this.broadcastElementChangedEvent();
    }
  }
});