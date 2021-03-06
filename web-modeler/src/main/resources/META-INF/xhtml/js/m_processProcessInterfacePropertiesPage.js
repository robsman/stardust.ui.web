/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_model" ],
		function(m_utils, m_constants, m_dialog, m_propertiesPage,
				m_dataTypeSelector, m_parameterDefinitionsPanel, m_i18nUtils, m_model) {
			return {
				create : function(propertiesPanel) {
					var page = new ProcessProcessInterfacePropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function ProcessProcessInterfacePropertiesPage(newPropertiesPanel,
					newId, newTitle) {
				var processInterfacetext = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.processInterface");
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "processInterfacePropertiesPage",
						processInterfacetext,
						"plugins/bpm-modeler/images/icons/process-interface.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ProcessProcessInterfacePropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.initialize = function() {
					this.processInterfaceTypeSelectInput = this
							.mapInputId("processInterfaceTypeSelectInput");
					this.noInterfacePanel = this.mapInputId("noInterfacePanel");
					this.providesProcessInterfacePanel = this
							.mapInputId("providesProcessInterfacePanel");
					this.implementsProcessInterfacePanel = this
          .mapInputId("implementsProcessInterfacePanel");
					this.webServiceInterfaceCheckboxInput = this
							.mapInputId("webServiceInterfaceCheckboxInput");
					this.restInterfaceCheckboxInput = this
							.mapInputId("restInterfaceCheckboxInput");
					this.processInterfaceFromDataCreationWizardLink = this
							.mapInputId("processInterfaceFromDataCreationWizardLink");
					this.processDataTableBody = m_utils.jQuerySelect("#processDataTable tbody"); // TODO
					
					this.implementsProcess = this.mapInputId("implementsProcess");
					
					this.parameterDefinitionsPanelForProvider = m_parameterDefinitionsPanel
							.create({
								scope : "providesProcessInterfacePanel",
								submitHandler : this,
								// listType : "object",
								supportsOrdering : false,
								supportsDataMappings : true,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								restrictToCurrentModel : false,
								supportsInOutDirection : true,
								showExternalDataReferences : true
							});
					

					this.parameterDefinitionsPanelForImplementer = m_parameterDefinitionsPanel
              .create({
                scope: "implementsProcessInterfacePanel",
                submitHandler: this,
                // listType : "object",
                readOnlyParameterList: true,
                supportsOrdering: false,
                supportsDataMappings: true,
                supportsDescriptors: false,
                supportsDataTypeSelection: true,
                restrictToCurrentModel: false,
                supportsInOutDirection: true,
                disableParameterDefinitionNameInput: true,
                disableParameterDefinitionDirectionSelect: true,
                showExternalDataReferences : true
              });
					
					this.internationalizeLabels();
					this.processInterfaceTypeSelectInput
							.change(
									{
										"callbackScope" : this
									},
									function(event) {
										if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "noInterface") {
											event.data.callbackScope
													.setNoInterface();
										} else if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "providesProcessInterface") {
											event.data.callbackScope
													.setProvidesProcessInterface();
										} else if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "implementsProcessInterface") {
											event.data.callbackScope
													.setImplementsProcessInterface();
										}

										event.data.callbackScope.submitProcessInterfaceType();
									});
					

					this.implementsProcess.change({
            "callbackScope": this
          }, function(event) {
            
            event.data.callbackScope.submitImplementsProcessChange();

            //TODO: remove post server side implementation
            /*var process = m_model.findProcess(event.data.callbackScope.implementsProcess.val());
            event.data.callbackScope.getModelElement().formalParameters = process.formalParameters;
            event.data.callbackScope.parameterDefinitionsPanelForImplementer
                    .setScopeModel(event.data.callbackScope.getModelElement().model);
            event.data.callbackScope.parameterDefinitionsPanelForImplementer
                    .setParameterDefinitions(event.data.callbackScope.getModelElement().formalParameters); */
          });
					
					this.processInterfaceFromDataCreationWizardLink.click({
						"callbackScope" : this
					}, function(event) {
						m_utils.jQuerySelect("#processInterfaceFromDataCreationWizard")
								.dialog("open");
					});
					m_utils.jQuerySelect("#processInterfaceFromDataCreationWizard").dialog({
						autoOpen : false,
						draggable : true
					});
					m_utils.jQuerySelect(
							"#processInterfaceFromDataCreationWizard #cancelButton")
							.click(
									function() {
										m_utils.jQuerySelect(
												"#processInterfaceFromDataCreationWizard")
												.dialog("close");
									});
					m_utils.jQuerySelect(
							"#processInterfaceFromDataCreationWizard #generateButton")
							.click(
									{
										"page" : this
									},
									function(event) {
										event.data.page.parameterDefinitionsTableBody
												.empty();

										var rows = m_utils.jQuerySelect("#processDataTable tbody tr");

										for ( var n = 0; n < rows.length; ++n) {
											var row = rows[n];
											var dataSymbol = jQuery.data(row,
													"dataSymbol");

											if (m_utils.jQuerySelect(
													"#processDataTable tbody tr input:eq("
															+ n + ")").is(
													":checked")) {
												var content = "<tr id=\"parameterRow-"
														+ n + "\">";

												content += "<td>";
												content += "<input type=\"text\" value=\""
														+ dataSymbol.dataName
														+ "\" class=\"nameInput\"></input>";
												content += "</td>";

												content += "<td>";
												content += event.data.page
														.getTypeSelectList("");
												content += "</td>";

												content += "<td align=\"right\">";
												content += ("<select size=\"1\" class=\"directionSelect\"><option value=\"IN\">IN</option>"
														+ "<option value=\"OUT\">OUT</option>"
														+ "<option value=\"INOUT\">INOUT</option>"
														+ "</select>");
												content += "</td>";

												content += "<td>";
												content += "<input type=\"text\" value=\""
														+ dataSymbol.dataName
														+ "\" class=\"nameInput\"></input>";
												content += "</td>";
												content += "</tr>";

												event.data.page.parameterDefinitionsTableBody
														.append(content);

												m_utils.jQuerySelect(
														"#parameterDefinitionsTable tbody tr #parameterRow-"
																+ n + " select")
														.val(
																m_utils.jQuerySelect(
																		"#processDataTable tbody tr select:eq("
																				+ n
																				+ ")")
																		.val());
											}
										}

										event.data.page.parameterDefinitionsTableBody
												.append("<tr id=\"newRow\"><td><a id=\"newLink\"><img src=\"plugins/bpm-modeler/images/icons/add.png\"/></a></td><td></td><td></td><td></td>");

										m_utils.jQuerySelect(
												"#processInterfaceFromDataCreationWizard")
												.dialog("close");
									});
					this.webServiceInterfaceCheckboxInput.change({
						page : this
					}, function(event) {
						event.data.page.submitProtocol();
					});
					this.restInterfaceCheckboxInput.change({
						page : this
					}, function(event) {
						event.data.page.submitProtocol();
					});
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.internationalizeLabels = function() {
					this.processInterfaceTypeSelectInput.empty();
					this.processInterfaceTypeSelectInput.append("<option value='" + m_constants.NO_PROCESS_INTERFACE_KEY + "'>" + m_i18nUtils.getProperty("modeler.processdefinition.propertyPages.processInterface.type.noProcessInterface") + "</option>" );
					this.processInterfaceTypeSelectInput.append("<option value='" + m_constants.PROVIDES_PROCESS_INTERFACE_KEY + "'>"
									+ m_i18nUtils
									.getProperty("modeler.processdefinition.propertyPages.processInterface.type.providesProcessInterface") + "</option>");
					this.processInterfaceTypeSelectInput
							.append("<option value='" + m_constants.IMPLEMENTS_PROCESS_INTERFACE_KEY + "'>"
									+ m_i18nUtils
									.getProperty("modeler.processdefinition.propertyPages.processInterface.type.implementsProcessInterface") + "</option>");

					// TODO Ugly

					m_utils.jQuerySelect("#processdefinitionselect").find("label[for='processInterfaceTypeSelectInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.type"));
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.populateProcessDataTable = function() {
					this.processDataTableBody.empty();

					var dataSymbols = this.propertiesPanel.diagram.dataSymbols;

					for ( var n in dataSymbols) {
						var dataSymbol = dataSymbols[n];
						var row = "<tr id=\"data-" + dataSymbol.oid + "\">";

						row += "<td>";
						row += "<input type=\"checkbox\">";
						row += "</td>";
						row += "<td>";
						row += dataSymbol.dataName;
						row += "</td>";
						row += "<td>";
						row += "<select>";
						row += "<option value=\"in\">In</option>";
						row += "<option value=\"out\">Out</option>";
						row += "<option value=\"inOut\">In/Out</option>";
						row += "</select>";
						row += "</td>";
						row += "</tr>";

						this.processDataTableBody.append(row);

						m_utils.jQuerySelect("#processDataTable #data-" + dataSymbol.oid)
								.data("dataSymbol", dataSymbol);
					}
				};

				/**
				 * 
				 */
        ProcessProcessInterfacePropertiesPage.prototype.populateImplementsProcess = function() {
          this.implementsProcess.empty();
          
          this.implementsProcess.append("<option value='"
                  + m_constants.TO_BE_DEFINED
                  + "'>"
                  + m_i18nUtils
                      .getProperty("modeler.general.toBeDefined")
                  + "</option>");
          
          var modelsSorted = m_utils.convertToSortedArray(
                  m_model.getModels(), "name", true);

          for ( var n in modelsSorted) {
            if (modelsSorted[n] == this.getModelElement().model) {
              continue;
            }

            processesSorted = m_utils.convertToSortedArray(
                    modelsSorted[n].processes, "name", true);
            for ( var m in processesSorted) {
              if ((processesSorted[m].processInterfaceType === m_constants.PROVIDES_PROCESS_INTERFACE_KEY)) {
                this.implementsProcess
                .append("<option value='"
                    + processesSorted[m].getFullId()
                    + "'>"
                    + modelsSorted[n].name
                    + "/"
                    + processesSorted[m].name
                    + "</option>");
              }
            }
          }
        }
				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setNoInterface = function() {
					this.processInterfaceTypeSelectInput.val(m_constants.NO_PROCESS_INTERFACE_KEY);
					m_dialog.makeVisible(this.noInterfacePanel);
					m_dialog.makeInvisible(this.providesProcessInterfacePanel);
					m_dialog.makeInvisible(this.implementsProcessInterfacePanel);
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setProvidesProcessInterface = function() {
					this.processInterfaceTypeSelectInput
							.val(m_constants.PROVIDES_PROCESS_INTERFACE_KEY);
					m_dialog.makeInvisible(this.noInterfacePanel);
					m_dialog.makeInvisible(this.implementsProcessInterfacePanel);
					m_dialog.makeVisible(this.providesProcessInterfacePanel);

					if (this.getModelElement().formalParameters == null) {
						this.getModelElement().formalParameters = [];
					}

					this.parameterDefinitionsPanelForProvider.setScopeModel(this
							.getModelElement().model);
					this.parameterDefinitionsPanelForProvider.setParameterDefinitions(this
							.getModelElement().formalParameters);
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setImplementsProcessInterface = function() {
					this.processInterfaceTypeSelectInput.val(m_constants.IMPLEMENTS_PROCESS_INTERFACE_KEY);
					
					m_dialog.makeInvisible(this.noInterfacePanel);
          m_dialog.makeInvisible(this.providesProcessInterfacePanel);

          m_dialog.makeVisible(this.implementsProcessInterfacePanel);          
					
          this.populateImplementsProcess();

          if(this.getModelElement().implementsProcessId){
            this.implementsProcess.val(this.getModelElement().implementsProcessId);
          }else{
            this.implementsProcess.val(m_constants.TO_BE_DEFINED);  
          }
          
          if (this.getModelElement().formalParameters == null) {
            this.getModelElement().formalParameters = [];            
          }

          this.parameterDefinitionsPanelForImplementer.setScopeModel(this
              .getModelElement().model);
          this.parameterDefinitionsPanelForImplementer.setParameterDefinitions(this
              .getModelElement().formalParameters);
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.populateParameterDefinitionFields = function() {
					this.parameterDefinitionNameInput
							.val(this.currentParameterDefinition.name);
					this.parameterDefinitionDirectionSelect
							.val(this.currentParameterDefinition.direction);
					this.parameterDefinitionDataSelect
							.val(this.currentParameterDefinition.dataFullId);
					this.parameterDefinitionPathInput
							.val(this.currentParameterDefinition.path);
					this.dataTypeSelector
							.setDataType(this.currentParameterDefinition);
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setElement = function() {
					this.parameterDefinitionsPanelForProvider.setScopeModel(this
							.getModelElement().model);

					if (this.getModelElement().processInterfaceType == m_constants.NO_PROCESS_INTERFACE_KEY) {
						this.setNoInterface();
					} else if (this.getModelElement().processInterfaceType == m_constants.PROVIDES_PROCESS_INTERFACE_KEY) {
						this.setProvidesProcessInterface();

						// Set protocol

						if (this.getModelElement().attributes["carnot:engine:externalInvocationType"] == "SOAP") {
							this.restInterfaceCheckboxInput.prop("checked", false);
							this.webServiceInterfaceCheckboxInput.prop("checked",
									true);
						} else if (this.getModelElement().attributes["carnot:engine:externalInvocationType"] == "REST") {
							this.restInterfaceCheckboxInput.prop("checked", true);
							this.webServiceInterfaceCheckboxInput.prop("checked",
									false);
						} else if (this.getModelElement().attributes["carnot:engine:externalInvocationType"] == "BOTH") {
							this.restInterfaceCheckboxInput.prop("checked", true);
							this.webServiceInterfaceCheckboxInput.prop("checked",
									true);
						} else {
							this.restInterfaceCheckboxInput.prop("checked", false);
							this.webServiceInterfaceCheckboxInput.prop("checked",
									false);
						}
					} else if (this.getModelElement().processInterfaceType == m_constants.IMPLEMENTS_PROCESS_INTERFACE_KEY) {
						this.setImplementsProcessInterface();
					}
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.validate = function() {
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.submitProtocol = function() {
					var attributes = {};

					if (this.webServiceInterfaceCheckboxInput.is(":checked")) {
						if (this.restInterfaceCheckboxInput.is(":checked")) {
							attributes["carnot:engine:externalInvocationType"] = "BOTH";
						} else {
							attributes["carnot:engine:externalInvocationType"] = "SOAP";
						}
					} else {
						if (this.restInterfaceCheckboxInput.is(":checked")) {
							attributes["carnot:engine:externalInvocationType"] = "REST";
						} else {
							attributes["carnot:engine:externalInvocationType"] = "none";
						}
					}

					this.propertiesPanel.submitChanges({
						attributes : attributes
					});
				};

				/**
				 * Callback for parameterDefinitionsPanelForProvider and parameterDefinitionsPanelForImplementer.
				 */
				ProcessProcessInterfacePropertiesPage.prototype.submitParameterDefinitionsChanges = function(
						formalParameters) {
					this.propertiesPanel.submitChanges({
						"formalParameters" : formalParameters
					});
				};

				/**
				 *
				 */
				ProcessProcessInterfacePropertiesPage.prototype.submitProcessInterfaceType = function(
						formalParameters) {
					this.propertiesPanel.submitChanges({
						"processInterfaceType" : this.processInterfaceTypeSelectInput.val()
					});
				};
				
				/**
				 * 
				 */
        ProcessProcessInterfacePropertiesPage.prototype.submitImplementsProcessChange = function() {
          this.propertiesPanel.submitChanges({
            "implementsProcessId": this.implementsProcess.val()
          });
        };
        
			}
		});