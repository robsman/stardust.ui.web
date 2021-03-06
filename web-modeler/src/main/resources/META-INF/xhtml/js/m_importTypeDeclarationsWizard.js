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
      [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_command",
            "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_model",
            "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_structuredTypeBrowser","bpm-modeler/js/m_i18nUtils",
            "bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_urlUtils" ],
      function(m_utils, m_constants, m_communicationController, m_command,
            m_commandsController, m_model,
            m_dialog, m_typeDeclaration, m_structuredTypeBrowser, m_i18nUtils, m_messageDisplay, m_urlUtils) {
         return {
            initialize : function() {
               var wizard = new ImportTypeDeclarationsWizard();
               i18importtypeproperties();

               wizard.initialize(payloadObj.model);

            }
         };


         function i18importtypeproperties() {

            m_utils.jQuerySelect("#titleText")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.panel"));
            m_utils.jQuerySelect("#dialogCloseIcon").attr("title",
                  m_i18nUtils.getProperty("modeler.common.value.close"));
            m_utils.jQuerySelect("#import")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.text"));
            m_utils.jQuerySelect("#importMessage")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.message"));
            m_utils.jQuerySelect("#url")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.url"));
            m_utils.jQuerySelect("#loadFromUrlButton")
                  .attr(
                        "value",
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.load"));

            m_utils.jQuerySelect("#dataStructElement")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.heading.dataStructureElemnets"));
            m_utils.jQuerySelect("#structureDefinitionHintPanel")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.definitionPanel"));
            m_utils.jQuerySelect("#select")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.checkbox"));
            m_utils.jQuerySelect("#elementColumn")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.element.name"));
            m_utils.jQuerySelect("#typeColumn")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.element.properties.commonProperties.type"));
            m_utils.jQuerySelect("#cardinalityColumn")
                  .text(
                        m_i18nUtils
                              .getProperty("modeler.element.properties.commonProperties.cardinality"));
            m_utils.jQuerySelect("#importButton")
                  .attr(
                        "value",
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.import"));
            m_utils.jQuerySelect("#cancelButton")
                  .attr(
                        "value",
                        m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.cancel"));

         }
         /**
         *
         */
         function ImportTypeDeclarationsWizard() {
            this.tree = m_utils.jQuerySelect("#typeDeclarationsTable");
            this.tableBody = m_utils.jQuerySelect("tbody", this.tree);
            this.urlTextInput = m_utils.jQuerySelect("#urlTextInput");
            this.loadFromUrlButton = m_utils.jQuerySelect("#loadFromUrlButton");
            this.importButton = m_utils.jQuerySelect("#importButton");
            this.cancelButton = m_utils.jQuerySelect("#cancelButton");
            this.closeButton = m_utils.jQuerySelect("#dialogCloseIcon");
            this.selectAllCheckbox = m_utils.jQuerySelect("#selectAllCheckbox");
            this.selectAll = false;

            var view = this;
            this.loadFromUrlButton.click(function(event) {
               view.loadFromUrl();
            });

            this.importButton.click(function(event) {
               view.performImport();
            });

            this.cancelButton.click(function(event) {
               closePopup();
            });

            this.closeButton.click(function(event) {
               closePopup();
            });

            /**
            *
            */
            this.selectAllCheckbox.click(function(event) {
               view.selectAll = !view.selectAll;
               m_utils.jQuerySelect("table#typeDeclarationsTable tbody tr.top-level")
                  .each(function() {
                     m_utils.jQuerySelect(this).toggleClass("selected", view.selectAll);
               });
            });

            /**
            *
            */
            ImportTypeDeclarationsWizard.prototype.initialize = function(model) {
               this.model = model;

               this.tree.tableScroll({
                  height : 300
               });
               this.tree.treeTable();
            };

            /**
            * <code>structure</code> allows to pass a structure if no
            * structure cannot be retrieved from the server.
            */
            ImportTypeDeclarationsWizard.prototype.loadFromUrl = function(structure) {

               if (!this.urlTextInput.val()) {
                  this.urlTextInput.addClass("error");
                  m_messageDisplay
                        .showErrorMessage(m_i18nUtils
                              .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.emptyURL"));
                  return;
               }
               m_utils.jQuerySelect("body").css("cursor", "progress");
               // this.clearErrorMessages();
               this.urlTextInput.removeClass("error");

               var view = this;

               m_communicationController
                     .syncPostData(
                           {
                              url : m_communicationController
                                    .getEndpointUrl()
                                    + "/typeDeclarations/loadFromUrl"
                           },
                           JSON.stringify({
                              url : this.urlTextInput.val()
                           }),
                           {
                              "success" : function(serverData) {
                                 m_messageDisplay.clearAllMessages();
                                 view.urlTextInput.removeClass("error");
                                 jQuery.proxy(view.setSchema, view)(serverData);
                                 m_utils.jQuerySelect("body").css("cursor", "auto");
                              },
                              "error" : function() {
                                 m_utils.jQuerySelect("body").css("cursor", "auto");
                                 if (structure == null) {
                                    m_messageDisplay
                                          .showErrorMessage(m_i18nUtils
                                                .getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.xsdLoadFailed"));
                                    view.urlTextInput
                                          .addClass("error");
                                 } else {
                                    jQuery.proxy(view.setSchema, view)(structure);
                                 }
                              }
                           });
            };

            /**
            *
            */
            ImportTypeDeclarationsWizard.prototype.setSchema = function(schema) {
               console.time("@@@@@@@@@@@@@@@@@@@@@@@@@@@ XSD load time");
               m_utils.showWaitCursor();
               this.schema = schema;
               m_utils.debug("===> Type Declarations");
               m_utils.debug(schema);

               this.tableBody.empty();

               for ( var name in this.schema.elements) {
                  var element = this.schema.elements[name];

                  var path = "element-" + name.replace(/:/g, "-");

                  var elementType = element.type ? element.type : element.name;

                  var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(elementType, schema);
                  var row = m_structuredTypeBrowser.generateChildElementRow("element-", element, schemaType,
                        function(row, element, schemaType) {
                     m_utils.jQuerySelect("<td><span class='data-element'>" + element.name + "</span></td>").appendTo(row);
                     m_utils.jQuerySelect("<td>" + elementType + "</td>").appendTo(row);
                     m_utils.jQuerySelect("<td></td>").appendTo(row);
                  });

                  row.data("element", element);

                  row.addClass("top-level");
                  this.tableBody.append(row);

                  // drill into elements, too (requires element's schemaType, see above)
                  if (schemaType) {
                     if (m_structuredTypeBrowser.hasChildElements(row.get(0))) {
                        m_structuredTypeBrowser.insertDummyChildRow(row.get(0));
                     }
                  }
               }
               var view = this;
               //check if xsd contains any complex types
               if (this.schema.types) {
                  jQuery.each(this.schema.types, function(i, type) {
                     var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(type.name, view.schema);

                     var path = "type-" + type.name.replace(/:/g, "-");

                     var row = m_structuredTypeBrowser.generateChildElementRow("type-", type, schemaType,
                           function(row, element, schemaType) {

                        m_utils.jQuerySelect("<td><span class='data-element'>" + type.name + "</span></td>").appendTo(row);
                        m_utils.jQuerySelect("<td>" + type.name + "</td>").appendTo(row);
                        m_utils.jQuerySelect("<td></td>").appendTo(row);

                        row.data("typeDeclaration", type);
                     });
                     row.addClass("top-level");

                     view.tableBody.append(row);

                     if (m_structuredTypeBrowser.hasChildElements(row.get(0))) {
                        m_structuredTypeBrowser.insertDummyChildRow(row.get(0));
                     }
                  });
               }

               this.tree.tableScroll({
                  height : 150
               });
               // TODO - hack
               // The table scroll plugin sets height to auto if the
               // initial height is less than the provided height
               // settig max-height in the plugin should also work.
               m_utils.jQuerySelect("div.tablescroll_wrapper").css("max-height", "170px");
               this.tree.treeTable({
                  indent: 14,
                  onNodeShow: function() {
                     var thisRow = m_utils.jQuerySelect(this);
                     if (thisRow.get(0).id.indexOf("DUMMY_ROW") >= 0) {
                        var classList = thisRow.get(0).className.split(/\s+/);
                        var parentId;
                        for (var i in classList) {
                           if (classList[i].indexOf("child-of-") >= 0) {
                              parentId = classList[i].substring("child-of-".length);
                           }
                        }

                        if (parentId) {
                           m_utils.showWaitCursor();
                           var parentRow = m_utils.jQuerySelect("tr#" + parentId);
                           m_structuredTypeBrowser.insertChildElementRowsWithDummyOffsprings(parentRow);

                           // Remove the dummy row
                           jQuery("tr#" + parentId + "-DUMMY_ROW").remove();

                           // TODO - see if this there is a cleaner way to do this
                           // Only adding the child rows to the parent row and expanding the parent
                           // doesn't work well, as in this case the child rows don't appear to be initialized
                           // correctly cuusing the expand arrows to be missing for child rows which had children
                           // As a work around I am invoking a click on the parent row expand button after the
                           // child rows are added and this initialized the child rows correctly.
                           m_utils.jQuerySelect("tr#" + parentId + " td a").click();

                           m_utils.hideWaitCursor();
                        }
                     }
                  }
               });

               m_utils.jQuerySelect("table#typeDeclarationsTable tbody tr.top-level").mousedown(function() {
                  // allow multi-select, but restrict to top-level entries
                  m_utils.jQuerySelect(this).toggleClass("selected");
               });
               console.timeEnd("@@@@@@@@@@@@@@@@@@@@@@@@@@@ XSD load time");
               m_utils.hideWaitCursor();
            };

            /**
            *
            */
            ImportTypeDeclarationsWizard.prototype.performImport = function() {

               // collect selected types
               var typeDeclarations = [];
               m_utils.jQuerySelect("tr.selected", this.tableBody).each(function() {
                  var row = m_utils.jQuerySelect(this);
                  var typeDeclaration = row.data("typeDeclaration");
                  if (!typeDeclaration) {
                     typeDeclaration = row.data("element");
                  }
                  if (typeDeclaration) {
                     typeDeclarations.push(typeDeclaration);
                  }
               });

               if (typeDeclarations.length == 0) {
                this.urlTextInput.addClass("error");
                m_messageDisplay.showErrorMessage(
                        m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.typeDeclarationNotSelected"));
                return;
               }
               
               if (typeDeclarations.length > 0) {
                  var view = this;
                  var ns = view.schema.targetNamespace;
                  var location = view.urlTextInput.val();
                  var changes = [];
                  jQuery.each(typeDeclarations, function() {
                     var id = this.name;
                     var xref = ns ? "{" + ns + "}" + id : id;
                     var duplicate = false;
                     jQuery.each(changes, function() {
                    	 if (id == this.name) {
                    		 duplicate = true;
                    	 }
                     });
                     if (!duplicate) {
                         changes.push({
                             // must keep the original name as ID as otherwise the type can't be resolved eventually
                             "id": id,
                             "name": id,
                             "typeDeclaration" : {
                                type: {
                                   classifier: "ExternalReference",
                                   location: location,
                                   xref: xref
                                }
                             }
                          });
                     }
                  });
                  m_commandsController.submitCommand(
                        m_command.createCreateTypeDeclarationCommand(
                              view.model.id,
                              view.model.id,
                              changes));
               }
               closePopup();
            };
         };
      });