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
					getStepOptions(data, path);
				},
				getDataAsJavaScriptObject: function(data) {
					return getDataAsJavaScriptObject(data);
				},
				getAllDataAsJavaScriptObjects: function(model) {
					return getAllDataAsJavaScriptObjects(model); 
				}
			};
			
			/**
			 * 
			 */
			function getAllDataAsJavaScriptObjects(model) {
				var allData = {}; // data.id used as object key
				
				jQuery.each(model.dataItems,
					function(index, data) {
						allData[data.id] = getDataAsJavaScriptObject(data);
					});
				
				return allData;
			}
			
			/**
			 * 
			 */
			function getDataAsJavaScriptObject(data) {
				var obj;
				
				switch (data.dataType) {
				case m_constants.PRIMITIVE_DATA_TYPE:
					obj = getPrimitiveValue(data.primitiveDataType);
					break;
					
				case m_constants.STRUCTURED_DATA_TYPE:
					var typeDeclaration = m_model.findTypeDeclaration(data.structuredDataTypeFullId);
					obj = createObject(typeDeclaration);
					break;
					
				// Treat unrecognized / unsupported data types as empty Objects
				case m_constants.DOCUMENT_DATA_TYPE:
				case "dmsDocumentList":
				case "dmsFolder":
				case "dmsFolderList":
				default: // e.g. entity
					obj = {};
					break;
				}
				
				return obj;
			}
			
			/**
			 * 
			 */
			function createObject(typeDeclaration) {
				var obj = {};
				
				var id = typeDeclaration.id;
				
				var body = typeDeclaration.schema.types[id].body;
				var elements, facets;
				
				var structureType = (body != null) ? "sequence" : "enumeration";
				
				switch (structureType) {
				case "sequence":
					elements = body.elements;
					for (var element in elements) {
						var name = elements[element].name;
						var type = elements[element].type;
						
						// Evaluate type
						var value;
						if (isBuiltInXsdDataType(type)) {
							var mappedPrimitiveType = getMappedPrimitiveType(type)
							value = getPrimitiveValue(mappedPrimitiveType);
						}
						else {
							// TODO: Support for choice, anonymous, sequence
							
							// Possibly a nested type
							var nestedTypeDeclaration = findTypeDeclaration(type, typeDeclaration.model);
							if (nestedTypeDeclaration != null) {
								value = createObject(nestedTypeDeclaration);
							}
							else {
								value = {};
							}
						}

						// Evaluate cardinality
						switch (elements[element].cardinality)
						{
						case "required":
							obj[name] = value;
							break;
							
						case "many":
							// TODO: Returning an array for multiple cardinality fields breaks code auto-completion in CodeMirror 
							// obj[name] = new Array(value);

							obj[name] = value;
							break;
							
						default:
							obj[name] = value;
							break;
						}
					}
					break;
					
				case "enumeration":
					/*obj = [];
					
					facets = typeDeclaration.schema.types[id].facets;
					for (var element in facets) {
						obj.push(facets[element].name);
					}*/
					
					obj = ""; // Enumerations are treated as Strings
					break;
				}
				
				return obj;
			}
			
			/**
			 * 
			 */
			function findTypeDeclaration(type, model) {
				for (var n in model.typeDeclarations) {
					var typeDeclaration = model.typeDeclarations[n];

					if (typeDeclaration.schema.elements[typeDeclaration.id] == null) {
						continue;
					}

					var currentType = typeDeclaration.schema.elements[typeDeclaration.id].type;
					if (type == currentType) {
						return typeDeclaration;
					}
				}
				
				return null;
			}
			
			/**
			 * 
			 */
			function isBuiltInXsdDataType(xsdDataType) {
				return (getMappedPrimitiveType(xsdDataType) != "") ? true : false; 
			}
			
			/**
			 * 
			 */
			function getMappedPrimitiveType(xsdDataType) {
				var primitiveType = "";
				
				// XSD Data types
				var xsdStringDataTypes = ["ENTITIES", "ENTITY", "ID","IDREF","IDREFS","language","Name","NCName",
				                          "NMTOKEN","NMTOKENS","normalizedString","QName","string","token"];
				var xsdNumericDataTypes = ["byte","decimal","int","integer","long","negativeInteger","nonNegativeInteger",
				                           "nonPositiveInteger","positiveInteger","short","unsignedLong","unsignedInt","unsignedShort","unsignedByte"];
				var xsdDateTimeDataTypes = ["date","dateTime","duration","gDay","gMonth","gMonthDay","gYear","gYearMonth","time"];
				var xsdMiscDataTypes = ["anyURI","base64Binary","boolean","double","float","hexBinary","NOTATION","QName"];

				var dataType = "";
				
				// Split xsdDataType (e.g. "xsd:integer") to get "integer"
				var index = m_utils.getLastIndexOf(xsdDataType, ":");
				if (index != -1) {
					dataType = xsdDataType.substr(index);
				}
				
				// This "switch" block is required since boolean, float and double are contained in xsdMiscDataTypes
				switch (dataType) {
				case "boolean":
					primitiveType = "boolean";
					break;
					
				case "double":
				case "float":
					primitiveType = dataType;
					break;
				}
				
				if (primitiveType == "") {
					if (m_utils.isItemInArray(xsdStringDataTypes, dataType)) {
						primitiveType = "String";
					}
					else if (m_utils.isItemInArray(xsdNumericDataTypes, dataType)) {
						primitiveType = "int";
					}
					else if (m_utils.isItemInArray(xsdDateTimeDataTypes, dataType)) {
						primitiveType = "Calendar";
					}
					else if (m_utils.isItemInArray(xsdMiscDataTypes, dataType)) {
						// Treat misc types as Strings
						primitiveType = "String";
					}
				}
				
				return primitiveType;
			}

			/**
			 * 
			 */
			function getPrimitiveValue(primitiveType)
			{
				var val;
				
				switch (primitiveType)
				{
				case "String":
				case "byte":
				case "char":
					val = "";
					break;
					
				case m_constants.BOOLEAN_PRIMITIVE_DATA_TYPE:
					val = true;
					break;
				
				case m_constants.DOUBLE_PRIMITIVE_DATA_TYPE:
				case "float":
				case "int":
				case "long":
				case "short":
					val = 0;
					break;
				
				case "Calendar":
				case "Timestamp":
					val = new Date();
					break;
					
				default:
					// Treat unsupported types as Strings  
					val = "";
					break;
				}
				
				return val;
			}

			/**
			 * 
			 */
			function getStepOptions(data, path)
			{
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