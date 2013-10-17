define(
	[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
		"bpm-modeler/js/m_constants",
		"bpm-modeler/js/m_commandsController",
		"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
		"bpm-modeler/js/m_accessPoint",
		"bpm-modeler/js/m_typeDeclaration",
		"bpm-modeler/js/m_parameterDefinitionsPanel",
		"bpm-modeler/js/m_codeEditorAce" ],
	function(m_utils, m_i18nUtils, m_constants, m_commandsController,
		m_command, m_model, m_accessPoint, m_typeDeclaration,
		m_parameterDefinitionsPanel, m_codeEditorAce) {
	    return {
		create : function(view) {
		    var overlay = new SqlIntegrationOverlay();

		    overlay.initialize(view);

		    return overlay;
		}
	    };

	    /**
	     * 
	     */
	    function SqlIntegrationOverlay() {
		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.initialize = function(view) {
		    this.view = view;

		    this.view
			    .insertPropertiesTab(
				    "sqlIntegrationOverlay",
				    "parameters",
				    m_i18nUtils
					    .getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
				    "plugins/bpm-modeler/images/icons/database_link.png");
		    this.sqlQueryHeading = m_utils
			    .jQuerySelect("#sqlIntegrationOverlay #sqlQueryHeading");
		    this.inputBodyAccessPointInput = m_utils
			    .jQuerySelect("#parametersTab #inputBodyAccessPointInput");
		    this.outputBodyAccessPointInput = m_utils
			    .jQuerySelect("#parametersTab #outputBodyAccessPointInput");
		    this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv")
			    .get(0);
		    this.editorAnchor.id = "codeEditorDiv"
			    + Math.floor((Math.random() * 100000) + 1);

		    this.codeEditor = m_codeEditorAce
			    .getJSCodeEditor(this.editorAnchor.id);

		    m_utils.jQuerySelect("label[for='dataSourceNameInput']")
			    .text("DataSource");
		    this.dataSourceNameInput = m_utils
			    .jQuerySelect("#sqlIntegrationOverlay #dataSourceNameInput");

		    var self = this;

		    m_utils.jQuerySelect("a[href='#configurationTab']").click(
			    function() {
				self.setGlobalVariables();
			    });

		    this.dataSourceNameInput.change(function() {
			self.submitChanges();
		    });

		    this.codeEditor.getEditor().on('blur', function(e) {
			self.submitChanges();
		    });
		    this.inputBodyAccessPointInput
			    .change(function() {
				if (!self.view.validate()) {
				    return;
				}

				if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
				    self.view
					    .submitModelElementAttributeChange(
						    "carnot:engine:camel::inBodyAccessPoint",
						    null);
				} else {
				    self.view
					    .submitModelElementAttributeChange(
						    "carnot:engine:camel::inBodyAccessPoint",
						    self.inputBodyAccessPointInput
							    .val());
				}
			    });
		    this.outputBodyAccessPointInput
			    .change(function() {
				if (!self.view.validate()) {
				    return;
				}

				if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
				    self.view
					    .submitModelElementAttributeChange(
						    "carnot:engine:camel::outBodyAccessPoint",
						    null);
				} else {
				    self.view
					    .submitModelElementAttributeChange(
						    "carnot:engine:camel::outBodyAccessPoint",
						    self.outputBodyAccessPointInput
							    .val());
				}
			    });

		    this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
			    .create({
				scope : "parametersTab",
				submitHandler : this,
				supportsOrdering : false,
				supportsDataMappings : false,
				supportsDescriptors : false,
				supportsDataTypeSelection : true,
				supportsDocumentTypes : false,
				supportsOtherData : false

			    });
		    this.update();
		};

		/**
		 * 
		 */
		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.getModelElement = function() {
		    return this.view.getModelElement();
		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.getApplication = function() {
		    return this.view.application;
		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.getScopeModel = function() {
		    return this.view.getModelElement().model;
		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.activate = function() {
		    this.view
			    .submitChanges({
				attributes : {
				    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
				    "carnot:engine:camel::invocationPattern" : "sendReceive",
				    "carnot:engine:camel::invocationType" : "synchronous",
				    "carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay"
				}
			    });
		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.update = function() {
		    this.parameterDefinitionsPanel.setScopeModel(this
			    .getScopeModel());
		    this.parameterDefinitionsPanel
			    .setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

		    this.inputBodyAccessPointInput.empty();
		    this.inputBodyAccessPointInput.append("<option value='"
			    + m_constants.TO_BE_DEFINED + "'>"
			    + m_i18nUtils.getProperty("None") // TODO I18N
			    + "</option>");

		    for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
			var accessPoint = this.getApplication().contexts.application.accessPoints[n];

			if (accessPoint.direction != m_constants.IN_ACCESS_POINT) {
			    continue;
			}

			this.inputBodyAccessPointInput.append("<option value='"
				+ accessPoint.id + "'>" + accessPoint.name
				+ "</option>");
		    }
		    this.inputBodyAccessPointInput
			    .val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);

		    this.outputBodyAccessPointInput.empty();
		    this.outputBodyAccessPointInput.append("<option value='"
			    + m_constants.TO_BE_DEFINED + "' selected>"
			    + m_i18nUtils.getProperty("None") // TODO I18N
			    + "</option>");

		    for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
			var accessPoint = this.getApplication().contexts.application.accessPoints[n];

			if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
			    continue;
			}

			this.outputBodyAccessPointInput
				.append("<option value='" + accessPoint.id
					+ "'>" + accessPoint.name + "</option>");
		    }
		    this.outputBodyAccessPointInput
			    .val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);

		    this.codeEditor
			    .getEditor()
			    .getSession()
			    .setValue(
				    this.getApplication().attributes["stardust:sqlOverlay::sqlQuery"]);
		    this.dataSourceNameInput
			    .val(this.getApplication().attributes["stardust:sqlOverlay::dataSourceId"]);

		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.getRoute = function() {
		    var route = "";
		    var sqlQuery = this.codeEditor.getEditor().getSession()
			    .getValue();
		    var dataSourceName = this.dataSourceNameInput.val();

		    if (sqlQuery != null && sqlQuery != "") {

			sqlQuery = sqlQuery.replace(/&/g, "&amp;");
			sqlQuery = sqlQuery.replace(/</g, "&lt;");
			sqlQuery = sqlQuery.replace(/>/g, "&gt;");
		    }
		    if (!m_utils.isEmptyString(dataSourceName)) {
			route += "<to uri=\"sql:"
				+ sqlQuery
				+ "?dataSource=#"
				+ dataSourceName
				+ "&alwaysPopulateStatement=true&prepareStatementStrategy=#sqlPrepareStatementStrategy\" />";
			var outBodyAccessPoint = this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"];
			if (this.getApplication().contexts.application.accessPoints.length > 0) {
			    for (i = 0; i < this.getApplication().contexts.application.accessPoints.length; i++) {
				var accessPoint = this.getApplication().contexts.application.accessPoints[i];
				if (accessPoint.direction == "OUT"
					&& outBodyAccessPoint != null
					&& outBodyAccessPoint != ""
					&& outBodyAccessPoint == accessPoint.id) {
				    route += "<setHeader headerName=\""
					    + accessPoint.id + "\">";
				    route += "<simple>$simple{body}</simple>"
				    route += "</setHeader>";
				} else if (accessPoint.direction == "OUT") {
				    /*
				     * route += "<setHeader headerName=\"" +
				     * accessPoint.id + "\">"; route += "<simple>$simple{header."+accessPoint.id+"}</simple>"
				     * route += "</setHeader>";
				     */
				}
			    }
			}
			route += "<to uri=\"bean:bpmTypeConverter?method=fromList\"/>"
		    }

		    m_utils.debug(route);
		    route = route.replace(/&/g, "&amp;");
		    return route;
		};

		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.submitChanges = function(
			parameterDefinitionsChanges) {
		    this.view
			    .submitChanges({
				attributes : {
				    "carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay",
				    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
				    "carnot:engine:camel::invocationPattern" : "sendReceive",
				    "carnot:engine:camel::invocationType" : "synchronous",
				    "carnot:engine:camel::routeEntries" : this
					    .getRoute(),
				    "stardust:sqlOverlay::dataSourceId" : this.dataSourceNameInput
					    .val(),
				    // "stardust:scriptingOverlay::language" :
				    // this.languageSelect
				    // .val(),
				    "stardust:sqlOverlay::sqlQuery" : this.codeEditor
					    .getEditor().getSession()
					    .getValue()
				}
			    });
		};
		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
			parameterDefinitionsChanges) {
		    this.getApplication().contexts.application.accessPoints = parameterDefinitionsChanges;
		    this.view
			    .submitChanges({
				contexts : {
				    application : {
					accessPoints : parameterDefinitionsChanges
				    }
				},
				attributes : {
				    "carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay",
				    "carnot:engine:camel::camelContextId" : "defaultCamelContext",
				    "stardust:sqlOverlay::dataSourceId" : this.dataSourceNameInput
					    .val(),
				    "carnot:engine:camel::invocationPattern" : "sendReceive",
				    "carnot:engine:camel::invocationType" : "synchronous",
				    "carnot:engine:camel::routeEntries" : this
					    .getRoute(),
				    "stardust:sqlOverlay::sqlQuery" : this.codeEditor
					    .getEditor().getSession()
					    .getValue()
				}
			    });
		};
		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.setGlobalVariables = function() {
		    // Global variables for Code Editor auto-complete /
		    // validation
		    var globalVariables = {};
		    for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
			var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];
			var typeDeclaration = null;
			if (parameterDefinition.dataType == "struct") {
			    typeDeclaration = m_model
				    .findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
			}
			if (typeDeclaration != null) {
			    globalVariables[parameterDefinition.id] = typeDeclaration
				    .createInstance();
			} else {
			    globalVariables[parameterDefinition.id] = "";
			}
		    }
		    this.codeEditor.setGlobalVariables(globalVariables);
		};
		/**
		 * 
		 */
		SqlIntegrationOverlay.prototype.validate = function() {
		    var valid = true;
		    this.dataSourceNameInput.removeClass("error");
		    if (m_utils.isEmptyString(this.dataSourceNameInput.val())) {
			this.view.errorMessages
				.push("DataSource Name must be defined."); // TODO
			// I18N
			this.dataSourceNameInput.addClass("error");
			valid = false;
		    }
		    return valid;
		};
	    }
	});