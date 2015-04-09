define(
		[ "bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_utils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils" ],
		function(m_angularContextUtils, m_utils, m_constants,
				m_commandsController, m_command, m_propertiesPage, m_model,
				m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					return new QualityControlActivityPropertiesPage(
							propertiesPanel);
				}
			};

			function QualityControlActivityPropertiesPage(newPropertiesPanel,
					newId, newTitle) {
				// Inheritance

				var propertiesPage = m_propertiesPage
						.createPropertiesPage(
								newPropertiesPanel,
								"qualityControlActivityPropertiesPage",
								getI18NProperty("modeler.activity.propertyPages.qualityControl.title"),
								"plugins/bpm-modeler/images/icons/quality-assurance.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						QualityControlActivityPropertiesPage.prototype,
						propertiesPage);

				// TODO Hack to bind propertiesPanel; introduces a circular
				// reference
				// which prohibits printing

				console.log("Properties Panel");
				console.log(this.propertiesPanel);
				console.log(this.propertiesPanel.element);

				this.propertiesPanel.propertiesPage = this;
				this.participants = [];

				this.isQualityControlActivity;
				this.qualityControlFormula;
				this.qualityControlParticipant;
				this.qualityControlProbability;
				this.noParticipantSetMessage;
				this.taskNotInteractiveMessage;

				/**
				 * 
				 */
				QualityControlActivityPropertiesPage.prototype.getActivity = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 * 
				 */
				QualityControlActivityPropertiesPage.prototype.setElement = function() {
					this.isQualityControlActivity = undefined;
					this.qualityControlParticipant = undefined;
					this.qualityControlProbability = undefined;
					this.qualityControlFormula = undefined
					this.noParticipantSetMessage = undefined;

					this.participants.lenght = 0;
					for ( var n in m_model.getModels()) {
						var model = m_model.getModels()[n];

						for ( var l in model.participants) {
							var participant = model.participants[l];

							this.participants.push({
								label : model.name + "/" + participant.name,
								fullId : participant.getFullId()
							});
						}
					}

					this.isQualityControlActivity = this.getModelElement().attributes["isQualityControlActivity"] ? this
							.getModelElement().attributes["isQualityControlActivity"]
							: false;
					this.qualityControlFormula = this.getModelElement().attributes["qualityControlFormula"] ? this
							.getModelElement().attributes["qualityControlFormula"]
							: "true";
					this.qualityControlProbability = this.getModelElement().attributes['qualityControlProbability'] ? parseInt(this
							.getModelElement().attributes['qualityControlProbability'])
							: 100;

					if (this.getModelElement().qualityControl
							&& this.getModelElement().qualityControl.participantFullId) {
						var participant = m_model
								.findParticipant(this.getModelElement().qualityControl.participantFullId);
						this.qualityControlParticipant = participant.externalReference ? participant.participantFullId
								: this.getModelElement().qualityControl.participantFullId;
					}

					this.safeApply();
				};

				/**
				 * 
				 */
				QualityControlActivityPropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				QualityControlActivityPropertiesPage.prototype.updateQAParams = function() {
					if (this.isQualityControlActivity) {
						this.getModelElement().attributes["isQualityControlActivity"] = true;
						if (this.qualityControlParticipant) {
							this.noParticipantSetMessage = undefined;
							this.getModelElement().attributes["qualityControlFormula"] = this.qualityControlFormula ? this.qualityControlFormula
									: true;
							this.getModelElement().attributes["qualityControlProbability"] = this.qualityControlProbability !== undefined ? this.qualityControlProbability
									: 100;
							this.getModelElement().qualityControl = {
								participantFullId : this.qualityControlParticipant,
								validCodes : []
							};
							this.submitChanges({
								modelElement : this.getModelElement()
							});
						} else {
							this.noParticipantSetMessage = this
									.getI18NProperty("modeler.activity.propertyPages.qualityControl.performerMissingMsg");
						}
					} else {
						this.getModelElement().attributes["isQualityControlActivity"] = null;
						this.getModelElement().attributes["qualityControlFormula"] = null;
						this.getModelElement().attributes["qualityControlProbability"] = null;
						this.getModelElement().qualityControl = null;
						this.submitChanges({
							modelElement : this.getModelElement()
						});
					}
				};

				/**
				 * 
				 */
				QualityControlActivityPropertiesPage.prototype.isModelElementInteractive = function() {
					var me = this.getModelElement();
					if (me && (me.taskType === 'manual' || me.taskType === 'user')) {
						return true;
					}
					
					this.taskNotInteractiveMessage = this.getI18NProperty("modeler.activity.propertyPages.qualityControl.taskNotInteractiveMsg");
					return false;
				};
				
				/**
				 * 
				 * @param key
				 * @returns
				 */
				QualityControlActivityPropertiesPage.prototype.getI18NProperty = function(
						key) {
					return getI18NProperty(key);
				};
			}

			function getI18NProperty(key) {
				return m_i18nUtils.getProperty(key);
			}
			;
		});