/**
 * @author Robert.Sauer
 */
define(
		[ "jquery", "m_utils", "jquery.treeTable" ],
		function(jQuery, m_utils) {

			function generateChildElementRow(parentPath, element, schemaType, rowInitializer) {

				var childPath = (parentPath || "") + "-" + element.name.replace(/:/g, "-");
				var childRow = jQuery("<tr id='" + childPath + "'></tr>");

				if (rowInitializer) {
					rowInitializer(childRow, element, schemaType);
				} else {

					jQuery("<td><span class='data-element'>" + element.name + "</span></td>").appendTo(childRow);
					jQuery("<td>" + element.type + "</td>").appendTo(childRow);
					jQuery("<td>" + element.cardinality + "</td>").appendTo(childRow);

					if ((null != schemaType) && schemaType.isStructure()) {
						// add styles in preparation of lazily appending child rows
						childRow.addClass("parent");
						childRow.addClass("expanded");
					}
				}

				if (parentPath) {
					childRow.data("parentId", parentPath);
				}
				childRow.data("path", childPath);
				childRow.data("schemaType", schemaType);

				return childRow;
			}

			function generateChildElementRows(parentPath, schemaType, rowInitializer) {
				var childRows = [];

				if ((schemaType != null) && (schemaType.isStructure())) {
					// append child rows
					jQuery.each(schemaType.getElements(), function(i, element) {
						var childSchemaType = schemaType.resolveElementType(element.name);
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
							parentRow.after(childRow);
							childRow.appendBranchTo(parentRow[0]);
						});
						parentRow.collapse();

						parentRow.data("elements-initialized", true);
					}
				});
			}

			return {
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