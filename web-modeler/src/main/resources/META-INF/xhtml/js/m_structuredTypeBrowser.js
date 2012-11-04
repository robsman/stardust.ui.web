/**
 * @author Robert.Sauer
 */
define(
		[ "jquery", "m_utils", "jquery.treeTable" ],
		function(jQuery, m_utils) {

			function generateChildElementRows(parentPath, schemaType, rowInitializer) {
				var childRows = [];

				if ((schemaType != null)
						&& (schemaType.isStructure())) {
					// append child rows
					jQuery.each(schemaType.getElements(), function(i, element) {
						var childPath = (parentPath || "") + "-" + element.name.replace(/:/g, "-");
						var childRow = jQuery("<tr id='" + childPath + "'></tr>");

						var childSchemaType = schemaType.resolveElementType(element.name);
						if (rowInitializer) {
							rowInitializer(childRow, element, childSchemaType);
						} else {

							jQuery("<td><span class='data-element'>" + this.name + "</span></td>").appendTo(childRow);
							jQuery("<td>" + this.type + "</td>").appendTo(childRow);
							jQuery("<td>" + this.cardinality + "</td>").appendTo(childRow);

							if (childSchemaType.isStructure()) {
								// add styles in preparation of lazily appending child rows
								childRow.addClass("parent");
								childRow.addClass("expanded");
							}
						}

						if (parentPath) {
							childRow.data("parentId", parentPath);
						}
						childRow.data("path", childPath);
						childRow.data("schemaType", childSchemaType);

						childRows.push(childRow);
					});
				}

				return childRows;
			};

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
			};

			return {
				generateChildElementRows: function(parentPath, schemaType, rowInitializer) {
					return generateChildElementRows(parentPath, schemaType, rowInitializer);
				},
				insertChildElementRowsLazily: function(parentRows, rowInitializer) {
					return insertChildElementRowsLazily(parentRows, rowInitializer);
				}

			};

		});