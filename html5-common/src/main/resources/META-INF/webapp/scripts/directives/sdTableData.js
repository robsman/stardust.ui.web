/**
 * Problems:
 * 
 * A timeout had to be introduced to wait for Angular to complete DOM operations.
 */
angular.module('bpm-common').directive('sdTableData', function() {
	return {
		restrict : "A",
		transclude : "element",
		compile : function(element, attrs, linker) {
			var aoColumnDefs = [ {
				sDefaultContent : "-",
				sClass : "",
				aTargets : [ "_all" ]
			} ];

			return {
				post : function(scope, element, attributes, controller) {
					// Parse expression
					var expression = attrs.sdTableData;
					var match = expression.match(/^\s*(.+)\s+in\s+(.*?)\s*(\s+track\s+by\s+(.+)\s*)?$/)
					var trackByExp, trackByExpGetter, trackByIdExpFn, trackByIdArrayFn, trackByIdObjFn, lhs, rhs;
					var valueIdentifier, keyIdentifier /*, hashFnLocals = { $id : hashKey } */;

					if (!match) {
						throw "Expected expression in form of '_item_ in _collection_[ track by _id_]' but got '{0}'.";
					}

					lhs = match[1];
					rhs = match[2];
					trackByExp = match[4];

					if (trackByExp) {
						trackByExpGetter = 0/* scope.$parse(trackByExp)*/;
						trackByIdExpFn = function(key, value, index) {
							// assign key, value, and $index to the locals so that they can be used in hash functions
							if (keyIdentifier) {
								hashFnLocals[keyIdentifier] = key;
							}
							hashFnLocals[valueIdentifier] = value;
							hashFnLocals.$index = index;
							return trackByExpGetter($scope, hashFnLocals);
						};
					} else {
						trackByIdArrayFn = function(key, value) {
							return hashKey(value);
						};
						trackByIdObjFn = function(key) {
							return key;
						};
					}

					match = lhs.match(/^(?:([\$\w]+)|\(([\$\w]+)\s*,\s*([\$\w]+)\))$/);
					if (!match) {
						throw ngRepeatMinErr(
								'iidexp',
								"'_item_' in '_item_ in _collection_' should be an identifier or '(_key_, _value_)' expression, but got '{0}'.",
								lhs);
					}
					valueIdentifier = match[3] || match[1];
					keyIdentifier = match[2];

					var elements = [];
					var parent = element.parent();
					var table = jQuery(parent.parent());

					scope.$watch(rhs, function(value) {
						if (value == null || value.length == 0) {
							return;
						}

						if (table.fnDestroy != null) {
							table.fnDestroy();
							console.log("Destroyed");
						}

						var i, block, childScope;

						// check if elements have already been rendered
						if (elements.length > 0) {
							// if so remove them from DOM, and destroy their scope
							for (i = 0; i < elements.length; i++) {
								elements[i].el.remove();
								elements[i].scope.$destroy();
							}

							elements = [];
						}

						for (var n = 0; n < value.length; ++n) {
							var rowScope = scope.$new();

							rowScope[lhs] = value[n];
							rowScope.$index = n;
							rowScope.$first = (n === 0);
							rowScope.$last = (n === (value.length - 1));
							rowScope.$middle = !(rowScope.$first || rowScope.$last);
							rowScope.$odd = !(rowScope.$even = (n & 1) === 0);

							linker(rowScope, function(clone) {
								parent.append(clone); // Add
								// to DOM
								jQuery(clone).prop("id", "sdTableRowIndex" + n);

								block = {};
								block.el = clone;
								block.scope = rowScope;
								elements.push(block);
							});
						}

						document.body.style.cursor = "wait";

						// There might be a way to synchronize against Angular JS operations; using timeout meanwhile
						window.setTimeout(function() {
							if (attributes.sdTableSelection) {
								// Clear selection
								scope.$eval(attributes.sdTableSelection).length = 0;
								table.find("tbody tr").removeClass("selectedRow");

								// Unbind events
								table.find("tbody tr").unbind("click");

								// Bind click events
								table.find("tbody tr").click(function(event) {
									var selection = scope.$eval(attributes.sdTableSelection);
									var indexString = jQuery(this).prop("id");

									indexString = indexString.substring(indexString.indexOf("sdTableRowIndex") 
													+ "sdTableRowIndex".length);

									var index = parseInt(indexString);

									if (event.ctrlKey) {
										var indexInSelection;

										if ((indexInSelection = jQuery.inArray(value[index], selection)) > -1) {
											selection.splice(indexInSelection, 1);
										} else {
											selection.push(value[index]);
										}

										jQuery(this).toggleClass("selectedRow");
									} else {
										table.find("tbody tr").removeClass("selectedRow");
										jQuery(this).addClass("selectedRow");

										selection.length = 0;
										selection.push(value[index]);
									}

									scope.$apply();

									// TODO Need to synchronize with apply?
									if (attributes.sdTableSelectionChanged) {
										scope.$apply(attributes.sdTableSelectionChanged);
									}
								});
							}

							// Mark first row
							table.find("tbody tr").last().addClass("lastRow");

							// Create Datatables
							// TODO Issues with nested repeats etc.
//							try {
//								table.dataTable({aoColumnDefs : aoColumnDefs});
//							} catch (x) {
//								console.log("Cannot create data table");
//								console.log(x);
//							}

							document.body.style.cursor = "default";
						}, 1000);
					});

					scope.$watch(attributes.sdTableSelection, function(value) {
						var rowObjects = scope.$eval(rhs);

						if (rowObjects) {
							table.find("tbody tr").removeClass("selectedRow");

							for (var n = 0; n < rowObjects.length; ++n) {
								for (var m = 0; m < value.length; ++m) {
									if (rowObjects[n] == value[m]) {
										table.find("#sdTableRowIndex" + n).addClass("selectedRow");
									}
								}
							}
						}
					});
				}
			};
		}
	};
});