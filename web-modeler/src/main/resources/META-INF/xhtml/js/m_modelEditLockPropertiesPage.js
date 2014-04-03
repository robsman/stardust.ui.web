/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
    "bpm-modeler/js/m_session", "bpm-modeler/js/m_urlUtils",
    "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_commandsController",
    "bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
    "bpm-modeler/js/m_i18nUtils" ], function(m_utils, m_constants, m_session, m_urlUtils,
    m_communicationController, m_commandsController, m_command, m_propertiesPage,
    m_i18nUtils) {

  function retrieveLabel(key) {
    return m_i18nUtils.getProperty("modeler.propertyView.modelView.editLock." + key);
  }

  /**
   *
   */
  function EditLockPropertiesPage(propertiesPanel, id) {
    var propertiesPage = m_propertiesPage.createPropertiesPage(propertiesPanel, id,
        "Edit lock", "plugins/bpm-modeler/images/icons/table.png");

    m_utils.inheritFields(this, propertiesPage);
    m_utils.inheritMethods(this, propertiesPage);
    m_utils.inheritMethods(this, EditLockPropertiesPage.prototype);
  }

  /**
   *
   */
  EditLockPropertiesPage.prototype.initialize = function() {
    m_utils.jQuerySelect("#editLockHeading").text(retrieveLabel("title"));

    m_utils.jQuerySelect("label[for='editLockStatus']").text(
        retrieveLabel("editLockStatus"));
    m_utils.jQuerySelect("label[for='editLockOwner']").text(
        retrieveLabel("editLockOwner"));
    m_utils.jQuerySelect("input#refreshLockStatusButton").val(
        retrieveLabel("refreshLockStatusButtonText"));
    m_utils.jQuerySelect("input#breakEditLockButton").val(
        retrieveLabel("breakEditLockButtonText"));

    m_utils.jQuerySelect("label#deleteEditLockDialogMsg").text(
        retrieveLabel("deleteDialog.message"));
    m_utils.jQuerySelect("input#applyButton").val(
        retrieveLabel("deleteDialog.applyButtonText"));
    m_utils.jQuerySelect("input#closeButton").val(
        retrieveLabel("deleteDialog.closeButtonText"));

    this.editLockStatus = m_utils.jQuerySelect("label#editLockStatus");
    this.editLockOwner = m_utils.jQuerySelect("label#editLockOwner");

    this.refreshLockStatusButton = m_utils.jQuerySelect("#refreshLockStatusButton");
    this.refreshLockStatusButton.click({
      "page" : this
    }, function(event) {
      event.data.page.refreshLockStatus();
    });

    this.breakEditLockButton = m_utils.jQuerySelect("#breakEditLockButton");
    this.breakEditLockButton.click({
      "page" : this
    }, function(event) {
      event.data.page.breakEditLockDialog.dialog("open");
    });

    this.breakEditLockDialog = m_utils.jQuerySelect("#deleteEditLockDialog").dialog({
      autoOpen : false,
      draggable : true,
      title : retrieveLabel("deleteDialog.title"),
      width : "auto",
      height : "auto",
      open : function() {
        // TODO initialize UI state
      }
    });

    m_utils.jQuerySelect("#closeButton", this.breakEditLockDialog).click({
      page : this
    }, function(event) {
      event.data.page.breakEditLockDialog.dialog("close");
    });
    m_utils.jQuerySelect("#applyButton", this.breakEditLockDialog).click({
      page : this
    }, function(event) {
      event.data.page.breakEditLock();
      event.data.page.breakEditLockDialog.dialog("close");
    });
  };

  EditLockPropertiesPage.prototype.setElement = function() {
    this.refreshLockStatus();
  };

  EditLockPropertiesPage.prototype.refreshLockStatus = function() {
    // TODO retrieve status and update UI
    var page = this;

    m_communicationController.syncGetData({
      url : m_communicationController.getEndpointUrl() + "/sessions/editLock/"
          + encodeURIComponent(this.getModel().id)
    }, {
      "success" : function(json) {
        page.refreshFromLockStatus(json);
      },
      "error" : function(e) {
        page.propertiesPanel.errorMessages.push(retrieveLabel("refreshLockStatusError")
            + " " + (e.statusText || ""));
        page.propertiesPanel.showErrorMessages();
      }
    });
  };

  EditLockPropertiesPage.prototype.refreshFromLockStatus = function(lockInfoJson) {
    var lockInfo = this.getModel().editLock || {};

    m_utils.inheritFields(lockInfo, lockInfoJson);

    var status = retrieveLabel("editLockStatusNotLocked");
    var owner = "";
    var canBreakLock = !!lockInfo.canBreakEditLock;

    if ("lockedByMe" === lockInfo.lockStatus) {
      status = retrieveLabel("editLockStatusLockedByMe");
    }
    if ("lockedByOther" === lockInfo.lockStatus) {
      status = retrieveLabel("editLockStatusLockedByOther");
      owner = lockInfo.ownerName || lockInfo.ownerId || retrieveLabel("editLockUnknownOwner");
    }

    this.editLockStatus.text(status);
    this.editLockOwner.text(owner);
    m_utils.markControlReadonly(this.breakEditLockButton, !canBreakLock);
  };

  EditLockPropertiesPage.prototype.breakEditLock = function() {
    var page = this;

    m_communicationController.deleteData({
      url : m_urlUtils.getModelerEndpointUrl() + "/sessions/editLock/"
          + encodeURIComponent(this.getModel().id)
    }, undefined, {
      success : function(response) {
        page.breakEditLockDialog.dialog("close");
        page.refreshFromLockStatus(response);
      },
      error : function(e) {
        page.propertiesPanel.errorMessages.push(retrieveLabel("breakEditLockError") + " "
            + (e.statusText || ""));
        page.propertiesPanel.showErrorMessages();
      }
    });
    return;
  };

  return {
    create : function(propertiesPanel, id) {
      var page = new EditLockPropertiesPage(propertiesPanel, id);

      page.initialize();

      return page;
    }
  };

});