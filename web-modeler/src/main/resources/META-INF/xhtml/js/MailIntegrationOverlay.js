define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce",
				"bpm-modeler/js/m_modelElementUtils" ],
		function(m_utils, m_i18nUtils, m_constants, m_commandsController,
				m_command, m_model, m_accessPoint, m_typeDeclaration,
				m_parameterDefinitionsPanel, m_codeEditorAce,
				m_modelElementUtils) {
			return {
				create : function(view) {
					var overlay = new MailIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			// Toolbar groups configuration.

			var editorToolbarGroups = [ {
				name : 'clipboard',
				groups : [ 'clipboard', 'undo' ]
			}, {
				name : 'editing',
				groups : [ 'find', 'selection', 'spellchecker' ]
			}, {
				name : 'links'
			}, {
				name : 'insert'
			}, {
				name : 'forms'
			}, {
				name : 'tools'
			}, {
				name : 'document',
				groups : [ 'mode', 'document', 'doctools' ]
			}, {
				name : 'others'
			}, '/', {
				name : 'basicstyles',
				groups : [ 'basicstyles', 'cleanup' ]
			}, {
				name : 'paragraph',
				groups : [ 'list', 'indent', 'blocks', 'align', 'bidi' ]
			}, {
				name : 'styles'
			}, {
				name : 'colors'
			} ];

			function MailIntegrationOverlay() {

				MailIntegrationOverlay.prototype.initialize = function(view) {

					this.view = view;

					this.view
							.insertPropertiesTab(
									"mailIntegrationOverlay",
									"parameters",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.parameters.title"),
									"plugins/bpm-modeler/images/icons/database_link.png");
					this.view
							.insertPropertiesTab(
									"mailIntegrationOverlay",
									"response",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.response.title"),
									"plugins/bpm-modeler/images/icons/email.png");
					this.view
							.insertPropertiesTab(
									"mailIntegrationOverlay",
									"test",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.test.title"),
									"plugins/bpm-modeler/images/icons/application-run.png");

					this.scriptCodeHeading = m_utils.jQuerySelect("#mailIntegrationOverlay #scriptCodeHeading");
					this.serverInput = m_utils.jQuerySelect("#mailIntegrationOverlay #serverInput");
					this.userInput = m_utils.jQuerySelect("#mailIntegrationOverlay #userInput");
					this.passwordInput = m_utils.jQuerySelect("#mailIntegrationOverlay #passwordInput");
					this.mailFormatSelect = m_utils.jQuerySelect("#mailIntegrationOverlay #mailFormatSelect");
					this.subjectInput = m_utils.jQuerySelect("#mailIntegrationOverlay #subjectInput");
					this.toInput = m_utils.jQuerySelect("#mailIntegrationOverlay #toInput");
					this.fromInput = m_utils.jQuerySelect("#mailIntegrationOverlay #fromInput");
					this.ccInput = m_utils.jQuerySelect("#mailIntegrationOverlay #ccInput");
					this.bccInput = m_utils.jQuerySelect("#mailIntegrationOverlay #bccInput");
					this.identifierInSubjectInput = m_utils.jQuerySelect("#mailIntegrationOverlay #identifierInSubjectInput");
					this.mailTemplateEditor = m_utils.jQuerySelect("#mailIntegrationOverlay #mailTemplateEditor");
					this.responseTypeSelect = m_utils.jQuerySelect("#responseTab #responseTypeSelect");
					this.responseOptionsTypeSelect = m_utils.jQuerySelect("#responseTab #responseOptionsTypeSelect");
					this.responseHttpUrlInput = m_utils.jQuerySelect("#responseTab #responseHttpUrlInput");

					CKEDITOR.replace("mailTemplateEditor", {
						toolbarGroups : editorToolbarGroups
					});

					this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
					this.runButton = m_utils.jQuerySelect("#testTab #runButton");
					this.inputDataTextarea = m_utils.jQuerySelect("#testTab #inputDataTextarea");
					this.outputDataTextarea = m_utils.jQuerySelect("#testTab #outputDataTextarea");
					this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");

					this.scriptCodeHeading.empty();
					this.scriptCodeHeading
							.append(m_i18nUtils
									.getProperty("modeler.model.applicationOverlay.email.template.heading"));
					this.resetButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.test.resetButton.title"));
					this.runButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.test.runButton.title"));

					m_utils.jQuerySelect("label[for='inputDataTextArea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.test.inputDataTextArea.label"));
					m_utils.jQuerySelect("label[for='outputDataTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.test.outputDataTextArea.label"));

					m_utils.jQuerySelect("#defaultValueHintLabel")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.defaultValueHint.label"));
					m_utils.jQuerySelect("label[for='serverInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.serverInput.label"));
					m_utils.jQuerySelect("label[for='userInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.userInput.label"));
					m_utils.jQuerySelect("label[for='passwordInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.passwordInput.label"));
					m_utils.jQuerySelect("label[for='mailFormatSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.label"));
					m_utils.jQuerySelect("#mailFormatSelect option[value='text/plain']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.text.label"));
					m_utils.jQuerySelect("#mailFormatSelect option[value='text/html']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.mailFormatSelect.html.label"));
					m_utils.jQuerySelect("label[for='subjectInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.subjectInput.label"));
					m_utils.jQuerySelect("label[for='toInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.toInput.label"));
					m_utils.jQuerySelect("label[for='fromInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.fromInput.label"));
					m_utils.jQuerySelect("label[for='ccInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.ccInput.label"));
					m_utils.jQuerySelect("label[for='bccInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.bccInput.label"));

					m_utils.jQuerySelect("label[for='identifierInSubjectInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.identifierInSubjectInput.label"));
					m_utils.jQuerySelect("#mailTemplateHeading")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.template.heading"));
					m_utils.jQuerySelect("label[for='responseTypeSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.label"));
					m_utils.jQuerySelect("#responseTypeSelect option[value='none']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.none.label"));
					m_utils.jQuerySelect("#responseTypeSelect option[value='http']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.label"));

					// response type select http

					m_utils.jQuerySelect("#responseOptionsTypeHintLabel")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.typeHint.label"));

					// m_utils.jQuerySelect("#label[for='responseOptionsTypeSelect']").text(m_i18nUtils
					// .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.type.label"));

					m_utils.jQuerySelect("#responseHttpUrlIHintLabel")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.urlHint.label"));

					// m_utils.jQuerySelect("#label[for='responseHttpUrlInput']").text(m_i18nUtils
					// .getProperty("modeler.model.applicationOverlay.email.responseTypeSelect.http.url.label"));

					var self = this;

					this.serverInput.change(function() {
						self.submitChanges();
					});

					this.userInput.change(function() {
						self.submitChanges();
					});

					this.passwordInput.change(function() {
						self.submitChanges();
					});

					this.mailFormatSelect.change(function() {
						self.submitChanges();
					});

					this.subjectInput.change(function() {
						self.submitChanges();
					});

					this.identifierInSubjectInput.change(function() {
						self.submitChanges();
					});

					this.fromInput.change(function() {
						self.submitChanges();
					});

					this.toInput.change(function() {
						self.submitChanges();
					});

					this.ccInput.change(function() {
						self.submitChanges();
					});

					this.bccInput.change(function() {
						self.submitChanges();
					});

					this.responseOptionsTypeSelect.change(function() {
						self.submitChanges();
					});

					this.responseTypeSelect.change(function() {
						self.setResponseType(self.responseTypeSelect.val());
						self.submitChanges();
					});

					this.responseHttpUrlInput.change(function() {
						self.submitChanges();
					});

					CKEDITOR.instances["mailTemplateEditor"].on('blur',
							function(e) {
								self.submitChanges();
							});

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								supportsDocumentTypes : false
							});

					this.populateResponseOptionsTypeSelect();

					this.runButton.click(function() {
						var output = "var input = ";

						output += self.inputDataTextarea.val();

						var markup = CKEDITOR.instances["mailTemplateEditor"]
								.getData();

						if (self.responseTypeSelect != "none") {
							markup += self.createResponseOptionString();
						}

						output += "; \""
								+ markup.replace(new RegExp("\"", 'g'), "'")
										.replace(new RegExp("\n", 'g'), " ")
										.replace(new RegExp("{{", 'g'),
												"\" + input.").replace(
												new RegExp("}}", 'g'), " + \"")
								+ "\"";

						self.outputDataTextarea.empty();
						self.outputDataTextarea.append(eval(output));
					});

					this.resetButton.click(function() {
						self.inputDataTextarea.empty();
						self.outputDataTextarea.empty();

						self.inputDataTextarea.append(self
								.createParameterObjectString(
										m_constants.IN_ACCESS_POINT, true));
					});

					if (this.getModelElement()
							&& this.getModelElement().isReadonly()) {
						CKEDITOR.instances["mailTemplateEditor"].config.readOnly = true;
					}
				};

				MailIntegrationOverlay.prototype.populateResponseOptionsTypeSelect = function() {

					this.responseOptionsTypeSelect.empty();
					this.responseOptionsTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					if (this.getScopeModel()) {

						this.responseOptionsTypeSelect
								.append("<optgroup label='"
										+ m_i18nUtils
												.getProperty("modeler.general.thisModel")
										+ "'>");

						for ( var i in this.getScopeModel().typeDeclarations) {

							if (!this.getScopeModel().typeDeclarations[i]
									.isSequence()) {
								this.responseOptionsTypeSelect
										.append("<option value='"
												+ this.getScopeModel().typeDeclarations[i]
														.getFullId()
												+ "'>"
												+ this.getScopeModel().typeDeclarations[i].name
												+ "</option>");
							}
						}

						this.responseOptionsTypeSelect
								.append("</optgroup><optgroup label='"
										+ m_i18nUtils
												.getProperty("modeler.general.otherModels")
										+ "'>");

						for ( var n in m_model.getModels()) {

							if (this.getScopeModel()
									&& m_model.getModels()[n] == this
											.getScopeModel()) {
								continue;
							}

							for ( var m in m_model.getModels()[n].typeDeclarations) {

								if (m_modelElementUtils
										.hasPublicVisibility(m_model
												.getModels()[n].typeDeclarations[m])) {

									if (!m_model.getModels()[n].typeDeclarations[m]
											.isSequence()) {

										this.responseOptionsTypeSelect
												.append("<option value='"
														+ m_model.getModels()[n].typeDeclarations[m]
																.getFullId()
														+ "'>"
														+ m_model.getModels()[n].name
														+ "/"
														+ m_model.getModels()[n].typeDeclarations[m].name
														+ "</option>");
									}
								}
							}
						}

						this.responseOptionsTypeSelect.append("</optgroup>");
					}
				};

				MailIntegrationOverlay.prototype.setResponseType = function(
						responseType) {

					if (!responseType) {
						responseType = "none";
					}

					this.responseTypeSelect.val(responseType);

					m_utils.jQuerySelect("#emailResponseDiv").hide();
					m_utils.jQuerySelect("#httpResponseDiv").hide();
					m_utils.jQuerySelect("#parameterDefinitionDirectionOutOption").hide();

					if (responseType === "http") {
						m_utils.jQuerySelect("#httpResponseDiv").show();
						// m_utils.jQuerySelect("#parameterDefinitionDirectionOutOption").show();
					} else if (responseType === "eMail") {
						m_utils.jQuerySelect("#emailResponseDiv").show();
						// m_utils.jQuerySelect("#parameterDefinitionDirectionOutOption").show();
					}
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.createParameterObjectString = function(
						direction, initializePrimitives) {

					var otherDirection;

					if (direction === m_constants.IN_ACCESS_POINT) {
						otherDirection = m_constants.OUT_ACCESS_POINT;
					} else {
						otherDirection = m_constants.IN_ACCESS_POINT;
					}

					var parameterObjectString = "{";

					var index = 0;

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

						if (parameterDefinition.direction == otherDirection) {
							continue;
						}

						if (index > 0) {
							parameterObjectString += ", ";
						}

						++index;

						if (parameterDefinition.dataType == "primitive") {
							if (initializePrimitives) {
								parameterObjectString += parameterDefinition.id;

								if (parameterDefinition.primitiveDataType === "String") {
									parameterObjectString += ": \"\"";
								} else if (parameterDefinition.primitiveDataType === "Boolean") {
									parameterObjectString += ": false";
								} else {
									parameterObjectString += ": 0";
								}
							}
						} else if (parameterDefinition.dataType == "struct") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

							parameterObjectString += parameterDefinition.id;
							parameterObjectString += ": ";
							parameterObjectString += JSON.stringify(
									typeDeclaration.createInstance(), null, 3);
						} else if (parameterDefinition.dataType == "dmsDocument") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

							parameterObjectString += parameterDefinition.id;
							parameterObjectString += ": ";
							parameterObjectString += JSON
									.stringify(
											typeDeclaration
													.createInstance({
														initializePrimitives : initializePrimitives
													}), null, 3);
						}
					}

					parameterObjectString += "}";

					return parameterObjectString;
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.createResponseOptionString = function() {

					if (this.responseOptionsTypeSelect.val() != null
							&& this.responseOptionsTypeSelect.val() != m_constants.TO_BE_DEFINED) {
						var typeDeclaration = m_model
								.findTypeDeclaration(this.responseOptionsTypeSelect
										.val());

						var optionMarkup = "<hr><p>Select one of the following options:</p><ul>";

						for ( var i = 0; i < typeDeclaration.getFacets().length; ++i) {

							var option = typeDeclaration.getFacets()[i];

							var hashCodeJS = "(";
							hashCodeJS += "processInstanceOid + '|' + ";
							hashCodeJS += "activityInstanceOid + '|' + ";
							hashCodeJS += "partition + '|false|";
							hashCodeJS += option.name;
							hashCodeJS += "').hashCode()";

							optionMarkup += "<li><a href=&quot;";
							optionMarkup += this.responseHttpUrlInput.val();
							optionMarkup += "/mail-confirmation";
							optionMarkup += "?activityInstanceOID=' + activityInstanceOid + '";
							optionMarkup += "&amp;processInstanceOID=' + processInstanceOid + '";
							optionMarkup += "&amp;partition=' + partition + '";
							optionMarkup += "&amp;investigate=false";
							optionMarkup += "&amp;outputValue=";
							optionMarkup += option.name;
							optionMarkup += "&amp;hashCode=' + ";
							optionMarkup += hashCodeJS;
							optionMarkup += "+ '";
							optionMarkup += "&quot;>";
							optionMarkup += option.name;
							optionMarkup += "</a></li>";
						}

						optionMarkup += "</ul>";

						return optionMarkup;
					}

					return "";
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.activate = function() {

					this
							.setResponseType(this.getApplication().attributes["stardust:emailOverlay::responseType"]);

					var accessPoints = this.createIntrinsicAccessPoints();
					this.submitParameterDefinitionsChanges(accessPoints);
				};

				MailIntegrationOverlay.prototype.createIntrinsicAccessPoints = function() {

					var accessPoints = {};
					var defaultAccessPoints = [];

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

						if (parameterDefinition.direction == m_constants.IN_ACCESS_POINT) {
							accessPoints[parameterDefinition.id] = parameterDefinition;
							defaultAccessPoints.push(parameterDefinition);
						}
					}

					if (this.responseTypeSelect.val() != "none") {
						defaultAccessPoints.push({
							id : "returnValue",
							name : "returnValue",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "OUT",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					if (!accessPoints["to"]) {
						defaultAccessPoints.push({
							id : "to",
							name : "to",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					if (!accessPoints["from"]) {
						defaultAccessPoints.push({
							id : "from",
							name : "from",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					if (!accessPoints["cc"]) {
						defaultAccessPoints.push({
							id : "cc",
							name : "cc",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					if (!accessPoints["bcc"]) {
						defaultAccessPoints.push({
							id : "bcc",
							name : "bcc",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					if (!accessPoints["subject"]) {
						defaultAccessPoints.push({
							id : "subject",
							name : "subject",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					return defaultAccessPoints;
				}

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.update = function() {

					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());

					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					this
							.setResponseType(this.getApplication().attributes["stardust:emailOverlay::responseType"]);

					this.serverInput
							.val(this.getApplication().attributes["stardust:emailOverlay::server"]);

					this.mailFormatSelect
							.val(this.getApplication().attributes["stardust:emailOverlay::mailFormat"]);

					this.subjectInput
							.val(this.getApplication().attributes["stardust:emailOverlay::subject"]);

					this.identifierInSubjectInput
							.prop(
									"checked",
									this.getApplication().attributes["stardust:emailOverlay::includeUniqueIdentifierInSubject"]);

					this.toInput
							.val(this.getApplication().attributes["stardust:emailOverlay::to"]);

					this.fromInput
							.val(this.getApplication().attributes["stardust:emailOverlay::from"]);

					this.ccInput
							.val(this.getApplication().attributes["stardust:emailOverlay::cc"]);

					this.bccInput
							.val(this.getApplication().attributes["stardust:emailOverlay::bcc"]);

					CKEDITOR.instances["mailTemplateEditor"]
							.setData(this.getApplication().attributes["stardust:emailOverlay::mailTemplate"]);

					this.responseOptionsTypeSelect
							.val(this.getApplication().attributes["stardust:emailOverlay::responseOptionType"]);

					this.responseHttpUrlInput
							.val(this.getApplication().attributes["stardust:emailOverlay::responseHttpUrl"]);

					this.userInput
							.val(this.getApplication().attributes["stardust:emailOverlay::user"]);

					this.passwordInput
							.val(this.getApplication().attributes["stardust:emailOverlay::pwd"]);
				};

				/**
				 *
				 */
				MailIntegrationOverlay.prototype.getRoute = function() {

					// convert possible SDT defined as IN mapping to Java native
					// object.
					var route = "<to uri=\"bean:bpmTypeConverter?method=toNativeObject\"/>\n";

					// if runtime doesn't provide a certain header, set the
					// default specified in UI
					if (this.subjectInput.val()) {

						route += "<choice>\n";
						route += "	<when>\n";
						route += "		<simple>$simple{in.header.subject} == null</simple>\n";
						route += "		<setHeader headerName=\"subject\">\n";
						route += "  		<constant>" + this.subjectInput.val()
								+ "</constant>\n";
						route += "		</setHeader>\n";
						route += "	</when>\n";
						route += "</choice>\n";

					}

					if (this.fromInput.val()) {
						route += "<choice>\n";
						route += "	<when>\n";
						route += "		<simple>$simple{in.header.from} == null</simple>\n";
						route += "		<setHeader headerName=\"from\">\n";
						route += "   		<constant>" + this.fromInput.val()
								+ "</constant>\n";
						route += "		</setHeader>\n";
						route += "	</when>\n";
						route += "</choice>\n";
					}

					if (this.toInput.val()) {
						route += "<choice>\n";
						route += "	<when>\n";
						route += "		<simple>$simple{in.header.to} == null</simple>\n";
						route += "		<setHeader headerName=\"to\">\n";
						route += "   		<constant>" + this.toInput.val()
								+ "</constant>\n";
						route += "		</setHeader>\n";
						route += "	</when>\n";
						route += "</choice>\n";
					}

					if (this.ccInput.val()) {
						route += "<choice>\n";
						route += "	<when>\n";
						route += "		<simple>$simple{in.header.cc} == null</simple>\n";
						route += "		<setHeader headerName=\"cc\">\n";
						route += "   		<constant>" + this.ccInput.val()
								+ "</constant>\n";
						route += "		</setHeader>\n";
						route += "	</when>\n";
						route += "</choice>\n";
					}

					if (this.bccInput.val()) {
						route += "<choice>\n";
						route += "	<when>\n";
						route += "		<simple>$simple{in.header.bcc} == null</simple>\n";
						route += "		<setHeader headerName=\"bcc\">\n";
						route += "   		<constant>" + this.bccInput.val()
								+ "</constant>\n";
						route += "		</setHeader>\n";
						route += "	</when>\n";
						route += "</choice>\n";
					}

					// java script endpoint that process e-mail template with
					// possible response links.
					route += "<setHeader headerName=\"CamelLanguageScript\">\n";
					route += "   <constant>\n";
					route += "      function setOutHeader(key, output){\n";
					route += "         exchange.out.headers.put(key,output);\n";
					route += "      }\n";

					route += "		String.prototype.hashCode = function() {";
					route += "			var hash = 0;\n";
					route += "			if (this == 0) return hash;\n";
					route += "			for (var i = 0; i &lt; this.length; i++) {\n";
					route += "				var character = this.charCodeAt(i);\n";
					route += "				hash = ((hash&lt;&lt;5)-hash)+character;\n";
					route += "				hash = hash &amp; hash;\n";
					route += "			}\n";
					route += "			return hash;\n";
					route += "		}\n";

					route += "var processInstanceOid = request.headers.get('ippProcessInstanceOid');\n";
					route += "var activityInstanceOid = request.headers.get('ippActivityInstanceOid');\n";
					route += "var partition = request.headers.get('ippPartition');\n";
					route += "var investigate = false;\n";

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {

						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						if (accessPoint.dataType == "primitive") {
							route += "var " + accessPoint.id + ";\n";
							route += "if(request.headers.get('"
									+ accessPoint.id + "') != null){\n";
							route += accessPoint.id
									+ " =  request.headers.get('"
									+ accessPoint.id + "');\n";
							route += "}\n";

						} else if (accessPoint.dataType == "struct") {
							route += "var " + accessPoint.id + ";\n";
							route += "if(request.headers.get('"
									+ accessPoint.id + "') != null){\n";
							route += accessPoint.id
									+ " =  eval('(' + request.headers.get('"
									+ accessPoint.id + "')+ ')');\n";
							route += "}\n";

						} else {
							route += "      var " + accessPoint.id
									+ " =  eval('(' + request.headers.get('"
									+ accessPoint.id + "')+ ')');\n";
						}
					}

					route += "\n";

					var markup = CKEDITOR.instances["mailTemplateEditor"]
							.getData();

					if (this.responseTypeSelect != "none") {
						markup += this.createResponseOptionString();
					}

					route += "      response = '"
							// + markup.replace(new RegExp("\"", 'g'), "'")
							+ markup.replace(new RegExp("\n", 'g'), " ")
									.replace(new RegExp("<", 'g'), "&lt;")
									.replace(new RegExp(">", 'g'), "&gt;")
									.replace(new RegExp("&nbsp;", 'g'),
											"&amp;nbsp;").replace(
											new RegExp("&copy;", 'g'),
											"&amp;copy;").replace(
											new RegExp("&acute;", 'g'),
											"&amp;acute;").replace(
											new RegExp("{{", 'g'), "' + ")
									.replace(new RegExp("}}", 'g'), " + '")
							+ "';\n";

					route += "      setOutHeader('response', response);\n";

					if (this.identifierInSubjectInput.val() != null
							&& this.identifierInSubjectInput.prop("checked")) {
						route += " 	setOutHeader('subject', '#ID:' + (partition + '|' + processInstanceOid + '|' + activityInstanceOid).hashCode() + '# - ' + subject);\n";
					} else {
						route += "      if (to){\n";
						route += "  		setOutHeader('subject', subject);\n";
						route += "      }\n";
					}

					route += "      if (to){\n";
					route += "      	setOutHeader('to', to);\n";
					route += "      }\n";

					route += "      if (from){\n";
					route += "      	setOutHeader('from', from);\n";
					route += "      }\n";

					route += "      if (cc){\n";
					route += "      	setOutHeader('cc', cc);\n";
					route += "      }\n";

					route += "      if (bcc){\n";
					route += "      	setOutHeader('bcc', bcc);\n";
					route += "      }\n";

					route += "   </constant>\n";
					route += "</setHeader>\n";

					// execute java sript
					route += "<to uri=\"language:javascript\"/>\n";

					// set content type
					route += "<setHeader headerName=\"contentType\">\n";
					route += "   <constant>" + this.mailFormatSelect.val()
							+ "</constant>\n";
					route += "</setHeader>";

					// set processed response to body
					route += "<setBody>\n";
					route += "   <simple>$simple{in.header.response}</simple>\n";
					route += "</setBody>\n";

					// execute smpt endpoint
					route += "<to uri=\"smtp://" + this.serverInput.val()
					if(!m_utils.isEmptyString(this.userInput.val()) && !m_utils.isEmptyString(this.passwordInput.val())){
						+ "?username=" + this.userInput.val()
						+ "&amp;password=" + this.passwordInput.val();
					}else if(!m_utils.isEmptyString(this.userInput.val()) ){
						+ "?username=" + this.userInput.val()
					}
					route += "\"/>";

					m_utils.debug(route);

					return route;
				};

				MailIntegrationOverlay.prototype.submitChanges = function() {

					var applicationTypeChanges = null;
					var invocationPatternChanges = null;
					var invocationTypeChanges = null;
					var responseTypeChanges = null;
					var responseHttpUrlChanges = null;
					var responseOptionsTypeChanges = null;

					if (this.responseTypeSelect.val() === "none") {
						applicationTypeChanges = "camelSpringProducerApplication";
						invocationPatternChanges = "send";
						invocationTypeChanges = "synchronous";
					} else {
						applicationTypeChanges = "camelConsumerApplication";
						invocationPatternChanges = "sendReceive";
						invocationTypeChanges = "asynchronous";
						responseTypeChanges = this.responseTypeSelect.val();
						responseHttpUrlChanges = this.responseHttpUrlInput
								.val();
						responseOptionsTypeChanges = this.responseOptionsTypeSelect
								.val();
					}

					var accessPointsChanges = this
							.createIntrinsicAccessPoints();

					this.view
							.submitChanges({
								type : applicationTypeChanges,
								contexts : {
									application : {
										accessPoints : accessPointsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "mailIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : invocationPatternChanges,
									"carnot:engine:camel::invocationType" : invocationTypeChanges,
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"carnot:engine:camel::consumerRoute" : "",
									"stardust:emailOverlay::responseType" : responseTypeChanges,
									"stardust:emailOverlay::responseOptionType" : responseOptionsTypeChanges,
									"stardust:emailOverlay::responseHttpUrl" : responseHttpUrlChanges,
									"stardust:emailOverlay::server" : this.serverInput
											.val(),
									"stardust:emailOverlay::user" : this.userInput
											.val(),
									"stardust:emailOverlay::pwd" : this.passwordInput
											.val(),
									"stardust:emailOverlay::mailFormat" : this.mailFormatSelect
											.val(),
									"stardust:emailOverlay::subject" : this.subjectInput
											.val(),
									"stardust:emailOverlay::includeUniqueIdentifierInSubject" : this.identifierInSubjectInput
											.prop("checked"),
									"stardust:emailOverlay::from" : this.fromInput
											.val(),
									"stardust:emailOverlay::to" : this.toInput
											.val(),
									"stardust:emailOverlay::cc" : this.ccInput
											.val(),
									"stardust:emailOverlay::bcc" : this.bccInput
											.val(),
									"stardust:emailOverlay::mailTemplate" : CKEDITOR.instances["mailTemplateEditor"]
											.getData()
								}
							});

				};

				MailIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {

					var applicationTypeChanges = null;

					if (this.responseTypeSelect.val() === "none") {
						applicationTypeChanges = "camelSpringProducerApplication";
					} else {
						applicationTypeChanges = "camelConsumerApplication";
					}

					this.view.submitChanges({
						type : applicationTypeChanges,
						contexts : {
							application : {
								accessPoints : parameterDefinitionsChanges
							}
						}
					}, true);
				};

				MailIntegrationOverlay.prototype.validate = function() {
					var valid = true;

					this.serverInput.removeClass("error");
					this.userInput.removeClass("error");
					this.passwordInput.removeClass("error");

					if (m_utils.isEmptyString(this.serverInput.val())) {
						this.view.errorMessages
								.push("Mail server must be defined."); // TODO
						// I18N
						this.serverInput.addClass("error");

						valid = false;
					}

					return valid;
				};
			}
		});