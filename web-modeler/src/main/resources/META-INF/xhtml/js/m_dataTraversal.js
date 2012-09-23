/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_model" ],
		function(m_utils, m_constants, m_model) {
			return {
				split: split,
				extractLast: extractLast,
				getStepOptions : function(data, path) {
					var steps = path.split(".");
					var n = 0;
					var typeDeclaration = null;

					while (n < steps.length) {
						if (n == 0) {
							if (data == null) {
								var data = findData(steps[n]);

								if (data == null) {
									if (steps.length == 1) {
										return getAllMatchingData(steps[n]);
									} else {
										m_utils.debug("Illegal argument: data "
												+ steps[0]
												+ " cannot be found.");
									}

								} else if (data.type != m_constants.STRUCTURED_DATA_TYPE) {
									m_utils
											.debug("Illegal argument: data is no structured data.");
								}

								typeDeclaration = m_model
										.findDataStructure(data.structuredDataTypeFullId).typeDeclaration;
							} else {
								dataStructure = m_model
										.findDataStructure(data.structuredDataTypeFullId);
								typeDeclaration = dataStructure.typeDeclaration;

								// Assuming type hierarchy has been resolved

								var schemaElement = typeDeclaration.children[steps[n]];

								if (schemaElement == null) {
									return getAllMatchingChildren(typeDeclaration, steps[n]);
								} else {

									// Traverse to next step

									typeDeclaration = schemaElement.type;
								}
							}
						} else {
							var schemaElement = typeDeclaration.children[steps[n]];

							if (schemaElement == null) {
								return getAllMatchingChildren(typeDeclaration, steps[n]);
							} else {
								typeDeclaration = schemaElement.type;
							}
						}

						++n;
					}

					m_utils
							.debug("Illegal state: Loop should have been exited already.");
				}
			};

			/**
			 * 
			 */
			function split(val) {
				return val.split(".");
			}

			/**
			 * 
			 */
			function extractLast(term) {
				return split(term).pop();
			}

			/**
			 * 
			 */
			function findData(name) {
				var models = m_model.getModels();

				for ( var n in models) {
					for ( var m in models[n].dataItems) {
						if (models[n].dataItems[m].name == name) {
							return models[n].dataItems[m];
						}
					}
				}

				return null;
			}

			/**
			 * 
			 */
			function getAllMatchingData(fragment) {
				var stepOptions = [];
				var models = m_model.getModels();

				for ( var n in models) {
					for ( var m in models[n].dataItems) {
						if (fragment.length == 0
								|| models[n].dataItems[m].name.toLowerCase()
										.indexOf(fragment.toLowerCase()) == 0) {
							stepOptions.push(models[n].dataItems[m].name);
						}
					}
				}

				return stepOptions;

			}

			/**
			 * 
			 */
			function getAllMatchingChildren(typeDeclaration, fragment) {
				var stepOptions = [];

				for ( var n in typeDeclaration.children) {
					if (fragment.length == 0
							|| typeDeclaration.children[n].name.toLowerCase()
									.indexOf(fragment.toLowerCase()) == 0) {
						stepOptions.push(typeDeclaration.children[n].name);
					}
				}

				return stepOptions;

			}
		});