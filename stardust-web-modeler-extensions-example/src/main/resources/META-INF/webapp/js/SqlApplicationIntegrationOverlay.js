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
    [ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
        "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
        "bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint",
        "bpm-modeler/js/m_parameterDefinitionsPanel" ],
    function(m_utils, m_constants, m_commandsController, m_command, m_model,
        m_accessPoint, m_parameterDefinitionsPanel) {
      return {
        create : function(view) {
          var overlay = new SqlApplicationIntegrationOverlay();

          overlay.initialize(view);

          return overlay;
        }
      };

      /**
       *
       */
      function SqlApplicationIntegrationOverlay() {

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.initialize = function(view) {
          this.view = view;
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.getModelElement = function() {
          return this.view.getModelElement();
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.getApplication = function() {
          return this.view.application;
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.getScopeModel = function() {
          return this.view.getModelElement().model;
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.activate = function() {
          this.view
              .submitChanges({
                attributes : {
                  "carnot:engine:camel::applicationIntegrationOverlay" : "SqlApplicationIntegrationOverlay"
                }
              });
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.update = function() {
        };

        /**
         *
         */
        SqlApplicationIntegrationOverlay.prototype.initializeTableRowsRecursively = function(
            accessPoint, tableRows, elementName, schemaType, parentPath) {
          var path = !parentPath ? ("." + accessPoint.id)
              : (parentPath + "." + elementName);
          var tableRow = {};

          tableRows.push(tableRow);

          tableRow.path = path;
          tableRow.parentPath = parentPath;
          tableRow.name = !parentPath ? accessPoint.name : elementName;

          if (this.fieldMappings[accessPoint.id] != null) {
            tableRow.mapping = this.fieldMappings[accessPoint.id][tableRow.path]
          }

          // Recursive resolution

          var view = this;
          if (schemaType.isStructure()) {
            jQuery.each(schemaType.getElements(), function(i, element) {
              view.initializeTableRowsRecursively(accessPoint, tableRows, element.name,
                  schemaType.resolveElementType(element.name), path);
            });
          }
        };
      }
    });