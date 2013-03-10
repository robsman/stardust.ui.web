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
					var overlay = new GenericEndpointOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function GenericEndpointOverlay() {
				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.camelContextInput = jQuery("#genericEndpointOverlay #camelContextInput");
					this.routeTextarea = jQuery("#genericEndpointOverlay #routeTextarea");
					this.additionalBeanSpecificationTextarea = jQuery("#genericEndpointOverlay #additionalBeanSpecificationTextarea");
					this.requestDataInput = jQuery("#genericEndpointOverlay #requestDataInput");
					this.responseDataInput = jQuery("#genericEndpointOverlay #responseDataInput");
					this.directionInput = jQuery("#genericEndpointOverlay #directionInput");

					// this.registerInputForModelElementAttributeChangeSubmission(
					// this.camelContextInput,
					// "carnot:engine:camel::camelContextId");
					// this.registerInputForModelElementAttributeChangeSubmission(
					// this.routeTextarea,
					// "carnot:engine:camel::routeEntries");
					// this
					// .registerInputForModelElementAttributeChangeSubmission(
					// this.additionalBeanSpecificationTextarea,
					// "carnot:engine:camel::additionalSpringBeanDefinitions");

					this.directionInput
							.change(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										if (view.directionInput.val() == "requestResponse") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:camel::producerMethodName" : "sendBodyInOut(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)"
														},
														contexts : {
															application : {
																accessPoints : [
																		{
																			id : "oParam1",
																			name : "Input",
																			direction : "IN",
																			dataType : "primitive",
																			primitiveDataType : "string"
																		},
																		{
																			id : "returnValue",
																			name : "Output",
																			direction : "OUT",
																			dataType : "primitive",
																			primitiveDataType : "string",
																			attributes : {
																				"carnot:engine:flavor" : "RETURN_VALUE"
																			}
																		} ]
															}
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:camel::producerMethodName" : ""
														},
														contexts : {
															application : {
																accessPoints : [ {
																	id : "oParam1",
																	name : "Input",
																	direction : "IN",
																	dataType : "primitive",
																	primitiveDataType : "string"
																} ]
															}
														}
													});
										}
									});
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.activate = function() {
					this
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpoint",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::producerMethodName" : "sendBodyInOut(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)"
								},
								contexts : {
									application : {
										accessPoints : [
												{
													id : "oParam1",
													name : "Input",
													direction : "IN",
													dataType : "primitive",
													primitiveDataType : "string"
												},
												{
													id : "returnValue",
													name : "Output",
													direction : "OUT",
													dataType : "primitive",
													primitiveDataType : "string",
													attributes : {
														"carnot:engine:flavor" : "RETURN_VALUE"
													}
												} ]
									}
								}
							});
				};

				/**
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.update = function() {
					this.camelContextInput
							.val(this.application.attributes["carnot:engine:camel::camelContextId"]);
					this.routeTextarea
							.val(this.application.attributes["carnot:engine:camel::routeEntries"]);
					this.additionalBeanSpecificationTextarea
							.val(this.application.attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
					

					if (this.application.attributes["carnot:engine:camel::producerMethodName"]
							&& this.application.attributes["carnot:engine:camel::producerMethodName"]
									.indexOf("sendBodyInOut") == 0) {
						this.directionInput.val("requestResponse");
					} else {
						this.directionInput.val("requestOnly");
					}
				};
			}
		});