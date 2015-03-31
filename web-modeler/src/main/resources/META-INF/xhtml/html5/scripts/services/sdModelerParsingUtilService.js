/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/*
 * @author Yogesh.Manware 
 * 
 * (copied from m_parsingUtils.js)
 * 
 */

(function() {
  'use strict';

  angular.module('modeler-ui').provider('sdModelerParsingUtilService',
          function() {
            this.$get = ['sdLoggerService', function(sdLoggerService) {
              var service = new UtilService(sdLoggerService);
              return service;
            }];
          });

  /*
   * 
   */
  function UtilService(sdLoggerService) {
    var trace = sdLoggerService
            .getLogger('modeler-ui.sdModelerParsingUtilService');

    /**
     * Parse a javascript object to an array of its dot-delimited elements where
     * each string is a unique path in the object hierarchy, and all possible
     * paths are accounted for
     */
    UtilService.prototype.parseJSObjToStringFrags = function(modelElement) {
      var results = [], /* accumulate our strings */
      key, /* key of object we are testing */
      temp; /*
             * temp var for entries we will push to our results, and pass along
             * in our recursion
             */

      for (key in obj) {
        if (obj.hasOwnProperty(key)) {
          temp = name + "." + key;
          results.push(temp);
          if (typeof (obj[key]) === "object") {
            results = results.concat(this.parseJSObjToStringFrags(obj[key],
                    temp));
          }
        }
      }
      return results;
    }

    /**
     * Given a typeDeclaration,parse it to an array of its dot-delimited
     * elements where each string is a unique path in the object hierarchy, and
     * all possible paths are accounted for.
     */
    UtilService.prototype.parseTypeToStringFrags = function(typeDecl, name) {
      var elements = typeDecl.getElements();
      var elementCount = elements.length;
      var results = [];
      var temp;
      while (elementCount--) {
        temp = elements[elementCount];
        results.push(name + "." + temp.name);
        if (typeof typeDecl.asSchemaType === "function") {
          var childSchemaType = typeDecl.asSchemaType().resolveElementType(
                  temp.name);
        } else if (typeof typeDecl.resolveElementType === "function") {
          var childSchemaType = typeDecl.resolveElementType(temp.name);
        }

        if (childSchemaType && childSchemaType.type) {
          results = results.concat(this.parseTypeToStringFrags(childSchemaType,
                  name + "." + temp.name));
        }
      }
      return results;
    }

    /**
     * 
     */
    UtilService.prototype.parseParamDefToStringFrags = function(paramDef,
            modelService) {
      if (!modelService) {
        trace.error("Model service not available!");
        return;
      }
      var typeDecl, data, lookupData;

      if (paramDef.dataType === "primitive") {
        data = [paramDef.id];
      } else {
        if ((paramDef.structuredDataTypeFullId && paramDef.structuredDataTypeFullId != "TO_BE_DEFINED")
                || paramDef.dataFullId) {
          typeDecl = modelService
                  .findTypeDeclaration(paramDef.structuredDataTypeFullId
                          || paramDef.dataFullId);
          if (!typeDecl) {
            /* Couldn't find typeDecl in our default model, lets try harder! */
            lookupData = modelService
                    .findData(paramDef.structuredDataTypeFullId
                            || paramDef.dataFullId);
            if (lookupData.structuredDataTypeFullId) { // structuredDataTypeFullId
              // does not exist for
              // primitive types
              typeDecl = modelService
                      .findTypeDeclaration(lookupData.structuredDataTypeFullId);
            }
          }
          if (typeDecl) {
            data = this.parseTypeToStringFrags(typeDecl, paramDef.id);
          }
        }
      }
      return data;

    }

  }

})();
