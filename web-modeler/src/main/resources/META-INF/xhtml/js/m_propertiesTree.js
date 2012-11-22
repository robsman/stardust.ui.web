/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils,m_i18nUtils) {
			
			var categories = {
				"General" : {
					"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.general"), 
					"categories" : [],
					"properties" : [ {
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.general.persistent"), 
						"type" : "boolean"
					}, {
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.general.key"), 
						"type" : "boolean"
					} ]
				},
				"UI" : {
					"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui"),
					"categories" : [],
					"properties" : [ {
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.label"), 
						"type" : "String"
					},{
						"name" : "InputPreferences_showDescription",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.showDescription"), 
						"type" : "boolean"
					}, 
					{
						"name" : "InputPreferences_readonly",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.readOnly"), 
						"type" : "boolean"
					}, {
						"name" : "InputPreferences_mandatory",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.mandatory"), 
						"type" : "boolean"
					}, {
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.format"),
						"type" : "string"
					}, {
						"name" : "InputPreferences_style",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.cssStyle"),
						"type" : "string"
					}, {
						"name" : "InputPreferences_styleClass",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.cssStyleClass"),
						"type" : "string"
					}, {
						"name" : "InputPreferences_prefixKey",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.prefixI18NKey"),
						"type" : "string"
					}, {
						"name" : "InputPreferences_suffixKey",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.suffixI18NKey"),
						"type" : "string"
					}, {
						"name" : "StringInputPreferences_stringInputType",
						"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.stringInputType"),
						"type" : "string",
						"enumeration" : [ {
							"label" :  m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textInput"),
							"value" : "TEXTINPUT"
						}, {
							"label" :  m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textArea"),
							"value" : "TEXTAREA"
						} ]
					}, {
						"label" :  m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textAreaRows"),
						"type" : "long"
					}, {
						"label" :  m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textAreaColumns"),
						"type" : "long"
					}, {
						"name" : "BooleanInputPreferences_readonlyOutputType",
						"label" :  m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.booleanReadonlyInputType"),
						"type" : "string",
						"enumeration" : [ {
							"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.checkBox"),
							"value" : "CHECKBOX"
						}, {
							"label" : m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textOutput"),
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
						height : 150
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