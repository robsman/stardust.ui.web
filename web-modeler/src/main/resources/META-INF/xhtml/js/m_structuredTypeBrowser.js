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
				var childRow = jQuery("<tr id='" + childPath + "'></tr>");

				if (rowInitializer) {
					rowInitializer(childRow, element, schemaType);
				} else {

					var nameColumn = jQuery("<td><span class='data-element'></span></td>");
					// set this way to ensure content is properly encoded
					nameColumn.children("td span").text(element.name);
					nameColumn.appendTo(childRow);

					var typeLabel;
					if (schemaType) {
						typeLabel = getSchemaTypeLabel(schemaType.name, schemaType.namespace);
					} else {
						typeLabel = getSchemaTypeLabel(element.type);
					}
					var cardinalityLabel = getCardinalityLabel(element.cardinality);

					jQuery("<td>" + (typeLabel || "") + "</td>").appendTo(childRow);
					jQuery("<td>" +  (cardinalityLabel || "") + "</td>").appendTo(childRow);

					if (schemaType && (schemaType.isStructure() || schemaType.isEnumeration())) {
						if ( !jQuery.isArray(schemaType.getElements()) || (0 < schemaType.getElements().length)) {
							// add styles in preparation of lazily appending child rows
							childRow.addClass("parent");
							childRow.addClass("expanded");
						}
					}
				}

				if (parentPath) {
					childRow.data("parentId", parentPath);
				}
				childRow.data("path", childPath);
				childRow.data("schemaType", schemaType);

				return childRow;
			}

			function generateChildElementRows(parentPath, schemaTypeOrElements, rowInitializer) {
				var childRows = [];

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
				if (elements) {
					// append child rows
					jQuery.each(elements, function(i, element) {
						var childSchemaType = (isStruct && (typeof schemaTypeOrElements.resolveElementType === "function"))
							? schemaTypeOrElements.resolveElementType(element.name)
							: undefined;
						var childRow = generateChildElementRow(parentPath, element, childSchemaType, rowInitializer);

						childRows.push(childRow);
					});
				}

				return childRows;
			}

			/**
			 * To be used before jquery.treeTable was initialized.
			 */
			function insertChildElementRowsEagerly(parentRows, rowInitializer) {
				jQuery.each(parentRows, function() {
					var parentRow = jQuery(this);

					var parentPath = this.id;
					var schemaType = parentRow.data("schemaType");

					var childRows = generateChildElementRows(parentPath, schemaType);
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
					var parentRow = jQuery(this);
					if ( !parentRow.data("elements-initialized")) {
						var parentPath = this.id;
						var schemaType = parentRow.data("schemaType");

						// trick to trigger initialization of child rows
						// first append at root ...
						var childRows = generateChildElementRows(parentPath, schemaType, rowInitializer);
						// reverse, to ensure child rows will end up in correct order in the table
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