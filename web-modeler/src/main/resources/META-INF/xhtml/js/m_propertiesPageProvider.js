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

define(["bpm-modeler/js/m_utils", "bpm-modeler/js/m_propertiesPage"], function(
        m_utils, m_propertiesPage) {
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
      if (this.propertiesPanel && this.propertiesPanel.element) {
        if (!this.propertiesPanel.refreshElement) {
          this.propertiesPanel.refreshElement = true;
        } else {
          this.propertiesPanel.refreshElement = false;
        }
      }
      //TODO: remove following
      this.broadcastElementChangedEvent();
    }
  }
});