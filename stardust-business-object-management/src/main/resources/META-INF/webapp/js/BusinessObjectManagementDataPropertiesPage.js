define([
  "bpm-modeler/js/m_utils",
  "bpm-modeler/js/m_constants",
  "bpm-modeler/js/m_commandsController",
  "bpm-modeler/js/m_command",
  "bpm-modeler/js/m_propertiesPage"
], function(m_utils, m_constants, m_commandsController, m_command, m_propertiesPage) {
  return {
    create: function(propertiesPanel) {
      return new BusinessObjectManagementDataPropertiesPage(propertiesPanel);
    }
  };

  function BusinessObjectManagementDataPropertiesPage(newPropertiesPanel, newId, newTitle) {

    // Inheritance

    var propertiesPage = m_propertiesPage.createPropertiesPage(newPropertiesPanel,
        "BusinessObjectManagementDataPropertiesPage", "Checklist Management");

    m_utils.inheritFields(this, propertiesPage);
    m_utils.inheritMethods(BusinessObjectManagementDataPropertiesPage.prototype, propertiesPage);

    // Field initialization

    this.primaryKeyFieldSelect = this.mapInputId("primaryKeyFieldSelect");
    this.singleObjectRetrievalRestUriInput = this.mapInputId("singleObjectRetrievalRestUriInput");
    this.objectSetRetrievalRestUriInput = this.mapInputId("objectSetRetrievalRestUriInput");

    this.registerInputForModelElementAttributeChangeSubmission(this.primaryKeyFieldSelect,
        "checklist-management:primaryKeyField");
    this.registerInputForModelElementAttributeChangeSubmission(this.singleObjectRetrievalRestUriInput,
        "checklist-management:singleObjectRetrievalRestUriInput");
    this.registerInputForModelElementAttributeChangeSubmission(this.objectSetRetrievalRestUriInput,
        "checklist-management:objectSetRetrievalRestUriInput");

    /**
     * 
     */
    BusinessObjectManagementDataPropertiesPage.prototype.setElement = function() {
      this.primaryKeyFieldSelect.val(this.getModelElement().attributes["checklist-management:primaryKeyField"]);
      this.singleObjectRetrievalRestUriInput
          .val(this.getModelElement().attributes["checklist-management:singleObjectRetrievalRestUriInput"]);
      this.objectSetRetrievalRestUriInput
          .val(this.getModelElement().attributes["checklist-management:objectSetRetrievalRestUriInput"]);
    };

    /**
     * 
     */
    BusinessObjectManagementDataPropertiesPage.prototype.validate = function() {
      return true;
    };
  }
});