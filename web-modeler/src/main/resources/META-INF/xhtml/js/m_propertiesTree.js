/**
 * @author Marc.Gille
 */
define(
		[ "m_utils" ],
		function(m_utils) {
			var categories = {
				"General" : {
					"label" : "General",
					"categories" : [],
					"properties" : [ {
						"label" : "Key",
						"type" : "boolean"
					}, {
						"label" : "Persistent",
						"type" : "boolean"
					} ]
				},
				"UI" : {
					"label" : "UI",
					"categories" : [],
					"properties" : [ {
						"label" : "Label",
						"type" : "String"
					},{
						"name" : "InputPreferences_showDescription",
						"label" : "Show Description",
						"type" : "boolean"
					}, 
					{
						"name" : "InputPreferences_readonly",
						"label" : "Readonly",
						"type" : "boolean"
					}, {
						"name" : "InputPreferences_mandatory",
						"label" : "Mandatory",
						"type" : "boolean"
					}, {
						"label" : "Format",
						"type" : "string"
					}, {
						"name" : "InputPreferences_style",
						"label" : "CSS Style",
						"type" : "string"
					}, {
						"name" : "InputPreferences_styleClass",
						"label" : "CSS Style Class",
						"type" : "string"
					}, {
						"name" : "InputPreferences_prefixKey",
						"label" : "Prefix I18N Key",
						"type" : "string"
					}, {
						"name" : "InputPreferences_suffixKey",
						"label" : "Suffix I18N Key",
						"type" : "string"
					}, {
						"name" : "StringInputPreferences_stringInputType",
						"label" : "String Input Type",
						"type" : "string",
						"enumeration" : [ {
							"label" : "TextInput",
							"value" : "TEXTINPUT"
						}, {
							"label" : "Text Area",
							"value" : "TEXTAREA"
						} ]
					}, {
						"label" : "Text Area Rows",
						"type" : "long"
					}, {
						"label" : "Text Area Columns",
						"type" : "long"
					}, {
						"name" : "BooleanInputPreferences_readonlyOutputType",
						"label" : "Boolean Readonly Input Type",
						"type" : "string",
						"enumeration" : [ {
							"label" : "Checkbox",
							"value" : "CHECKBOX"
						}, {
							"label" : "Text Output",
							"value" : "TEXTOUTPUT"
						} ]
					} ]
				}
			};

			return {
				create : function(tableId) {
					var propertiesTree = new PropertiesTree();

					propertiesTree.initialize(tableId, categories);

					return propertiesTree;
				}
			};

			/**
			 * 
			 */
			function PropertiesTree() {
				this.tableId = null;
				this.categories = null;

				/**
				 * 
				 */
				PropertiesTree.prototype.toString = function() {
					return "Lightdust.PropertiesTree";
				};

				/**
				 * 
				 */
				PropertiesTree.prototype.initialize = function(tableId,
						categories) {
					this.tableId = tableId;
					this.categories = categories;
					this.table = jQuery("#" + tableId);
					this.tableBody = jQuery("table#" + tableId + " tbody");

					this.tableBody.empty();

					var n = 0;

					for (categoryName in this.categories) {
						var category = this.categories[categoryName];
						var content = "<tr id=\"categoryRow-" + n + "\">";

						content += "<td>";
						content += category.label;
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);

						var m = 0;

						for (propertyName in category.properties) {
							var property = category.properties[propertyName];

							content = "<tr id=\"propertyRow-" + m
									+ "\" class=\"child-of-categoryRow-" + n
									+ "\">";
							content += "<td><span>";
							content += property.label;
							content += "</span></td>";
							content += "<td class=\"editable\">";

							if (property.type == "string") {
								if (property.enumeration == null) {
									content += "<input type=\"text\"\>";
								} else {
									content += "<select>";
									for (enumeratorName in property.enumeration) {
										var enumerator = property.enumeration[enumeratorName];

										content += "<option value\""
												+ enumerator.value + "\">"
												+ enumerator.label
												+ "</option>";
									}

									content += "</select>";
								}
							} else if (property.type == "long") {
								content += "<input type=\"text\"\ style=\"text-align: right;\">";
							} else if (property.type == "boolean") {
								content += "<input type=\"checkbox\"\>";
							}

							content += "</td>";
							content += "</tr>";

							this.tableBody.append(content);

							++m;
						}

						++n;
					}

					this.table.tableScroll({
						height : 200
					});
					this.table.treeTable();

					jQuery("table#fieldPropertiesTable tbody tr").mousedown(
							function() {
								jQuery("tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
							});

				};
			}
			;
		});