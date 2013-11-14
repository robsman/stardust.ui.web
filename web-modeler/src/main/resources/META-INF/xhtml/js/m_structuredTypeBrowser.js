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
 * @author Robert.Sauer
 */
define(
		[ "jquery", "bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils", "jquery.treeTable" ],
		function(jQuery, m_typeDeclaration, m_utils, m_i18nUtils) {

			/**
			 * @param {string} qName the (potentially namespace prefixed) type name
			 * @param {string} namespace optional argument, explicitly stating the qName's namespace
			 * @returns {string} the type's label (either from a resource bundle, otherwise simple the type's name)
			 */
			function getSchemaTypeLabel(qName, namespace) {
				var label = "";
				var parsedName = m_typeDeclaration.parseQName(qName);
				if (("xsd" === parsedName.prefix) || ("http://www.w3.org/2001/XMLSchema" === (parsedName.namespace || namespace))) {
					label = m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType." + parsedName.name);
					if ( !label) {
						label = "xsd:" + parsedName.name;
					}
				}

				return label || parsedName.name;
			}

			/**
			 * @param {string} cardinality the cardinality key
			 * @returns {string} the cardinality's label (either from a resource bundle, otherwise simple the cardinality)
			 */
			function getCardinalityLabel(cardinality) {
				if (cardinality) {
					var label = m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.cardinality.option." + cardinality);

					return label || cardinality;
				}

				// In case of enum, there won't be an associated cardinality
				// so return empty string
				return "";
			}

			function generateChildElementRow(parentPath, element, schemaType, rowInitializer) {

				var childPath = (parentPath || "") + "-" + element.name.replace(/[:<>]/g, "-");
				var childRow = m_utils.jQuerySelect("<tr id='" + childPath + "'></tr>");

				if (rowInitializer) {
					rowInitializer(childRow, element, schemaType);
				} else {

					var nameColumn = m_utils.jQuerySelect("<td><span class='data-element'></span></td>");
					// set this way to ensure content is properly encoded
					nameColumn.children("td span").text(element.name);
					nameColumn.appendTo(childRow);

					var typeLabel;
					if (schemaType) {
						typeLabel = getSchemaTypeLabel(schemaType.name, schemaType.namespace);
					} else {
						typeLabel = getSchemaTypeLabel(element.type);
					}
					
					// Assumes element is of anonymous type and
					// displays the element name as its type too. 
					if (!typeLabel && element.classifier === "element") {
						typeLabel = element.name;
					}
					
					if (!typeLabel && element.classifier === "attribute" && element.primitiveType) {
						typeLabel = getSchemaTypeLabel(element.primitiveType);
					}

					if (element.classifier === "attribute") {
						var cardinalityLabel = element.cardinality;
					} else {
						var cardinalityLabel = getCardinalityLabel(element.cardinality);
					}

					m_utils.jQuerySelect("<td>" + (typeLabel || "") + "</td>").appendTo(childRow);
					m_utils.jQuerySelect("<td>" +  (cardinalityLabel || "") + "</td>").appendTo(childRow);

					if (schemaType && (schemaType.isStructure() || schemaType.isEnumeration())) {
						if ( !jQuery.isArray(schemaType.getElements()) || (0 < schemaType.getElements().length)) {
							// add styles in preparation of lazily appending child rows
							childRow.addClass("parent");
							childRow.addClass("expanded");
						}
					}
					if ((element.body && element.body.length > 0) || (element.attributes && element.attributes.length > 0)) {
						childRow.addClass("parent");
						childRow.addClass("expanded");
					}
				}

				if (parentPath) {
					childRow.data("parentId", parentPath);
				}
				childRow.data("path", childPath);
				if (schemaType) {
					childRow.data("schemaType", schemaType);
				} else if (element.body && element.body[0].classifier == "sequence") {
					childRow.data("schemaType", element.body[0].body);
				} else if (element.classifier === "element") {
					childRow.data("schemaType", element);
				}
				if (element.attributes) {
					childRow.data("attributes", element.attributes);
				}

				return childRow;
			}

			/**
			 * Returns a boolean indicating whether type represented by the passed row
			 * has any child elements.
			 * It's consequential in determining weather to insert a dummy row as a child of this row.
			 * (The function is used thus in the import XSD dialog)
			 */
			function hasChildElements(parent) {
				var parentRow = m_utils.jQuerySelect(parent);
				if ( !parentRow.data("elements-initialized")) {
					var parentPath = parent.id;
					var schemaTypeOrElements = parentRow.data("schemaType");
					var attributes = parentRow.data("attributes");
				}
					
				var isStruct = false;
				var isEnum = false;
				var elements = [];
				if (schemaTypeOrElements) {
					if ((typeof schemaTypeOrElements.getElements === "function")
							&& (typeof schemaTypeOrElements.isStructure === "function")
							&& (typeof schemaTypeOrElements.isEnumeration === "function")) {
						isStruct = schemaTypeOrElements.isStructure();
						isEnum = schemaTypeOrElements.isEnumeration();
						elements = (isStruct || isEnum) ? schemaTypeOrElements.getElements() : [];
					} else {
						isStruct = true;
						elements = schemaTypeOrElements;
					}
				}
				if ((elements && elements.length > 0) || (attributes && attributes.length > 0)) {
					return true;
				}

				return false;
			}
			
			function generateChildElementRows(parentPath, schemaTypeOrElements, rowInitializer) {
				var childRows = [];

				var isStruct = false;
				var isEnum = false;
				var elements = [];
				var attributes = [];
				if (schemaTypeOrElements) {
					if ((typeof schemaTypeOrElements.getElements === "function")
							&& (typeof schemaTypeOrElements.isStructure === "function")
							&& (typeof schemaTypeOrElements.isEnumeration === "function")) {
						isStruct = schemaTypeOrElements.isStructure();
						isEnum = schemaTypeOrElements.isEnumeration();
						elements = (isStruct || isEnum) ? schemaTypeOrElements.getElements() : [];
						attributes = (isStruct || isEnum) ? schemaTypeOrElements.getAttributes() : [];
					} else if (Array.isArray(schemaTypeOrElements)) {
						isStruct = true;
						elements = schemaTypeOrElements;
						if (schemaTypeOrElements.attributes) {
							attributes = schemaTypeOrElements.attributes;
						}
					}
				}
				if (attributes) {
					// append child rows
					jQuery.each(attributes, function(i, attribute) {
						var childRow = generateChildElementRow(parentPath, attribute, null, rowInitializer);
						childRow.css("fontStyle", "italic");
						childRows.push(childRow);
					});
				}
				if (elements) {
					// append child rows
					jQuery.each(elements, function(i, element) {
						var childSchemaType = undefined;
						if(isStruct && (typeof schemaTypeOrElements.resolveElementType === "function")){
							childSchemaType = schemaTypeOrElements.resolveElementType(element.name);
						}
						var childRow = generateChildElementRow(parentPath, element, childSchemaType, rowInitializer);
						if (element.attributes) {
							childRow.data("attributes", element.attributes);
						}
						if (element.classifier === "attribute") {
							childRow.css("fontStyle", "italic");
						}
						childRows.push(childRow);
					});
				}

				return childRows;
			}

			/**
			 * Inserts a dummy row (generated using generateDummyChildElementRow())
			 * as a child of the passed row.
			 */
			function insertDummyChildRow(parent) {
				var parentRow = m_utils.jQuerySelect(parent);
				var parentPath = parent.id;
				var schemaType = parentRow.data("schemaType");

				var childRow = generateDummyChildElementRow(parentPath);
				childRow.addClass("child-of-" + parentPath);
				if (parentRow.hasClass("locked")) {
					childRow.addClass("locked");
				}
				parentRow.after(childRow);
			}
			
			/**
			 * Generates a dummy row that could be inserted as a child row so that the 
			 * expand / collapse icon is generated for parent row.
			 * This row should be deleted after the actual child rows are loaded lazily.
			 * (The function is used thus in the import XSD dialog)
			 */
			function generateDummyChildElementRow(parentPath) {
				var childPath = (parentPath || "") + "-DUMMY_ROW";
				var childRow = jQuery("<tr id='" + childPath + "'></tr>");
				jQuery("<td></td>").appendTo(childRow);
				jQuery("<td></td>").appendTo(childRow);
				jQuery("<td></td>").appendTo(childRow);
				
				return childRow;
			}
			
			/**
			 * To be used before jquery.treeTable was initialized.
			 */
			function insertChildElementRowsEagerly(parentRows, rowInitializer) {
				jQuery.each(parentRows, function() {
					var parentRow = m_utils.jQuerySelect(this);

					var parentPath = this.id;
					var schemaType = parentRow.data("schemaType");

					var childRows = generateChildElementRows(parentPath, schemaType);
					
					var attributes = parentRow.data("attributes");
					if (attributes) {
						var attributeRows = generateChildElementRows(parentPath, attributes, rowInitializer);
						m_utils.insertArrayAt(childRows, attributeRows, 0);
					}
					
					jQuery.each(childRows, function(i, childRow) {
						// append child rows
						childRow.addClass("child-of-" + parentPath);
						if (parentRow.hasClass("locked")) {
							childRow.addClass("locked");
						}
						parentRow.after(childRow);
					});
				});
			}

			/**
			 * To be used after jquery.treeTable was initialized for on-demand child node insertion.
			 */
			function insertChildElementRowsLazily(parentRows, rowInitializer) {
				jQuery.each(parentRows, function() {
					var parentRow = m_utils.jQuerySelect(this);
					if ( !parentRow.data("elements-initialized")) {
						var parentPath = this.id;
						var schemaType = parentRow.data("schemaType");

						// trick to trigger initialization of child rows
						// first append at root ...
						var childRows = generateChildElementRows(parentPath, schemaType, rowInitializer);
						// reverse, to ensure child rows will end up in correct order in the table
						
						var attributes = parentRow.data("attributes");
						if (attributes) {
							var attributeRows = generateChildElementRows(parentPath, attributes, rowInitializer);
							m_utils.insertArrayAt(childRows, attributeRows, 0);
						}
						
						childRows.reverse();
						jQuery.each(childRows, function(i, childRow) {
							// ... then move to the proper location
							if (parentRow.hasClass("locked")) {
								childRow.addClass("locked");
							}
							parentRow.after(childRow);
							childRow.appendBranchTo(parentRow[0]);
						});
						parentRow.collapse();

						parentRow.data("elements-initialized", true);
					}
				});
			}
			
			/**
			 * Generates child element rows for the passed row, checks if the child element row has
			 * further children and if yes inserts a dummy row as their children
			 * (to get the expand icon on row of the tree table - used thus in import xsd dialog).
			 */
			function insertChildElementRowsWithDummyOffsprings(parentRows, rowInitializer) {
				jQuery.each(parentRows, function() {
					var parentRow = m_utils.jQuerySelect(this);
					if ( !parentRow.data("elements-initialized")) {
						var parentPath = this.id;
						var schemaType = parentRow.data("schemaType");

						// trick to trigger initialization of child rows
						// first append at root ...
						var childRows = generateChildElementRows(parentPath, schemaType, rowInitializer);
						
						// Add attributes if they were not added already by the call to
						// generateChildElementRows above.
						if (!schemaType || !(typeof schemaType.getAttributes === "function")) {
							var attributes = parentRow.data("attributes");
						}
						if (attributes) {
							var attributeRows = generateChildElementRows(parentPath, attributes, rowInitializer);
							m_utils.insertArrayAt(childRows, attributeRows, 0);
						}

						// reverse, to ensure child rows will end up in correct order in the table
						childRows.reverse();
						jQuery.each(childRows, function(i, childRow) {
							// ... then move to the proper location
							if (parentRow.hasClass("locked")) {
								childRow.addClass("locked");
							}
							parentRow.after(childRow);
							childRow.appendBranchTo(parentRow[0]);
							if (hasChildElements(childRow.get(0))) {
								insertDummyChildRow(childRow.get(0));
							}
						});
						parentRow.collapse();

						parentRow.data("elements-initialized", true);
					}
				});
			}

			return {
				/**
				 * @param {string} qName the (potentially namespace prefixed) type name
				 * @param {string} namespace optional argument, explicitly stating the qName's namespace
				 * @returns {string} the type's label (either from a resource bundle, otherwise simple the type's name)
				 */
				getSchemaTypeLabel: getSchemaTypeLabel,

				/**
				 * @param {string} cardinality the cardinality key
				 * @returns {string} the cardinality's label (either from a resource bundle, otherwise simple the cardinality)
				 */
				getCardinalityLabel: getCardinalityLabel,
				
				hasChildElements : hasChildElements,
				
				insertDummyChildRow : insertDummyChildRow,
				
				insertChildElementRowsWithDummyOffsprings : insertChildElementRowsWithDummyOffsprings,

				generateChildElementRow: function(parentPath, element, schemaType, rowInitializer) {
					return generateChildElementRow(parentPath, element, schemaType, rowInitializer);
				},
				generateChildElementRows: function(parentPath, schemaType, rowInitializer) {
					return generateChildElementRows(parentPath, schemaType, rowInitializer);
				},
				insertChildElementRowsEagerly: function(parentRows, rowInitializer) {
					return insertChildElementRowsEagerly(parentRows, rowInitializer);
				},
				insertChildElementRowsLazily: function(parentRows, rowInitializer) {
					return insertChildElementRowsLazily(parentRows, rowInitializer);
				}

			};

		});