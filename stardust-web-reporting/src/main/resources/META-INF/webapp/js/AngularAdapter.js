/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Marc.Gille
 * @author Yogesh.Manware
 * 
 */

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.AngularAdapter) {
	bpm.portal.AngularAdapter = function AngularAdapter(options) {
		/**
		 * 
		 */
	   var angularCompile;
		
		//load directives here, directives file should have init method which initializes 
	    //directive module and return module name so that dependency can be added to main module.
	    AngularAdapter.prototype.initializeModule = function(angular) {
	      this.angular = angular;

	      //load all directives
	      var dirModuleNames = [];
	      if(options && options.directives){
	    	  options.directives.forEach(function(directiveModule) {
	  	        if (directiveModule) {
	  	          var moduleName = directiveModule.init();
	  	          dirModuleNames = dirModuleNames.concat(moduleName);
	  	        }
	  	      });  
	      }

	      this.angularModule = angular.module('angularApp', dirModuleNames);

	      // Taken From - http://jsfiddle.net/cn8VF/
		  // This is to delay model updates till element is in focus

		  this.angularModule.directive('ngModelOnblur', function() {
			return {
					restrict : 'A',
					require : 'ngModel',
					link : function(scope, elm, attr, ngModelCtrl) {
						if (attr.type === 'radio' || attr.type === 'checkbox') {
							return;
						}
						elm.unbind('input').unbind('keydown').unbind('change');
						elm.bind('blur', function() {
							scope.$apply(function() {
								ngModelCtrl.$setViewValue(elm.val());
							});
						});
					}
				};
			});

			this.initalizeFormatConstraints();
	      
	      return this.angularModule;
	    };
		
		/**
		 * bootstrap module
		 */
		AngularAdapter.prototype.initialize = function(angular) {
			this.angular.bootstrap(document, [ 'angularApp' ]);

			// Is this correct?

			this.__defineGetter__('angularApp', function() {
				return this.angularModule;
			});
		};

		/**
		 * 
		 */
		AngularAdapter.prototype.mergeControllerWithScope = function(controller) {
			var scope = this.angular.element(document.body).scope();

			jQuery.extend(scope, controller);
			this.inheritMethods(scope, controller);

			scope.updateView = function() {
				this.$apply();
			};
			
			scope.runInAngularContext = function(func) {
	         scope.$apply(func);
	      };

			return scope;
		};
		
		/**
		 * Auxiliary method to copy all methods from the parentObject to the
		 * childObject.
		 */
		AngularAdapter.prototype.inheritMethods = function(childObject,
				parentObject) {
			for ( var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		};
		
      /*
       * 
       */
      AngularAdapter.prototype.getCompiler = function() {
         return angularCompile;
      };

		/**
		 * 
		 */
		AngularAdapter.prototype.initalizeFormatConstraints = function() {
			console.debug("Format constraints initialized");

			var self = this;

			/**
			 * Problems:
			 *
			 * A timeout had to be introduced to wait for Angular to
			 * complete DOM operations.
			 */
			this.angularModule
			.directive(
				'sdTableData',
				function () {
				return {
					restrict : "A",
					transclude : "element",
					compile : function (element, attrs,
						linker) {
						var aoColumnDefs = [{
								sDefaultContent : "-",
								sClass : "",
								aTargets : ["_all"]
							}
						];

						return {
							post : function (scope, element,
								attributes, controller) {
								// Parse expression

								if(attrs.options){
				                      aoColumnDefs = aoColumnDefs.concat(options);
				                }
								
								var expression = attrs.sdTableData;
								var match = expression
									.match(/^\s*(.+)\s+in\s+(.*?)\s*(\s+track\s+by\s+(.+)\s*)?$/),
								trackByExp,
								trackByExpGetter,
								trackByIdExpFn,
								trackByIdArrayFn,
								trackByIdObjFn,
								lhs,
								rhs,
								valueIdentifier,
								keyIdentifier /*
								 * ,
								 * hashFnLocals = {
								 * $id :
								 * hashKey }
								 */
							;

								if (!match) {
									throw "Expected expression in form of '_item_ in _collection_[ track by _id_]' but got '{0}'.";
								}

								lhs = match[1];
								rhs = match[2];
								trackByExp = match[4];

								if (trackByExp) {
									trackByExpGetter = 0 /*
										 * scope
										 * .$parse(trackByExp)
										 */
								;
									trackByIdExpFn = function (
										key, value,
										index) {
										// assign key,
										// value, and $index
										// to the locals so
										// that they can be
										// used in hash
										// functions
										if (keyIdentifier)
											hashFnLocals[keyIdentifier] = key;
										hashFnLocals[valueIdentifier] = value;
										hashFnLocals.$index = index;
										return trackByExpGetter(
											$scope,
											hashFnLocals);
									};
								} else {
									trackByIdArrayFn = function (
										key, value) {
										return hashKey(value);
									};
									trackByIdObjFn = function (
										key) {
										return key;
									};
								}

								match = lhs
									.match(/^(?:([\$\w]+)|\(([\$\w]+)\s*,\s*([\$\w]+)\))$/);
								if (!match) {
									throw ngRepeatMinErr(
										'iidexp',
										"'_item_' in '_item_ in _collection_' should be an identifier or '(_key_, _value_)' expression, but got '{0}'.",
										lhs);
								}
								valueIdentifier = match[3]
									 || match[1];
								keyIdentifier = match[2];

								var elements = [];
								var parent = element
									.parent();
								var table = jQuery(parent
										.parent());

								scope
								.$watch(
									rhs,
									function (
										value) {
									if (value == null
										 || value.length == 0) {
										return;
									}

									if (table.fnDestroy != null) {
										table
										.fnDestroy();
										console
										.log("Destroyed");
									}

									var i,
									block,
									childScope;

									// check
									// if
									// elements
									// have
									// already
									// been
									// rendered

									if (elements.length > 0) {
										// if
										// so
										// remove
										// them
										// from
										// DOM,
										// and
										// destroy
										// their
										// scope
										for (i = 0; i < elements.length; i++) {
											elements[i].el
											.remove();
											elements[i].scope
											.$destroy();
										}

										elements = [];
									}

									for (n = 0; n < value.length; ++n) {
										var rowScope = scope
											.$new();

										rowScope[lhs] = value[n];
										rowScope.$index = n;
										rowScope.$first = (n === 0);
										rowScope.$last = (n === (value.length - 1));
										rowScope.$middle = !(rowScope.$first || rowScope.$last);
										rowScope.$odd = !(rowScope.$even = (n & 1) === 0);

										linker(
											rowScope,
											function (
												clone) {
											parent
											.append(clone); // Add
											// to
											// DOM
											jQuery(
												clone)
											.prop(
												"id",
												"sdTableRowIndex"
												 + n);

											block = {};
											block.el = clone;
											block.scope = rowScope;
											elements
											.push(block);
										});
									}

									document.body.style.cursor = "wait";

									// There
									// might
									// be a
									// way
									// to
									// synchronize
									// against
									// Angular
									// JS
									// operations;
									// using
									// timeout
									// meanwhile

									window
									.setTimeout(
										function () {
										if (attributes.sdTableSelection) {
											// Clear
											// selection

											scope
											.$eval(attributes.sdTableSelection).length = 0;
											table
											.find(
												"tbody tr")
											.removeClass(
												"selectedRow");

											// Unbind
											// events

											table
											.find(
												"tbody tr")
											.unbind(
												"click");

											// Bind
											// click
											// events

											table
											.find(
												"tbody tr")
											.click(
												function (
													event) {
												var selection = scope
													.$eval(attributes.sdTableSelection);
												var indexString = jQuery(
														this)
													.prop(
														"id");

												indexString = indexString
													.substring(indexString
														.indexOf("sdTableRowIndex") + 15)

													var index = parseInt(indexString);

												if (event.ctrlKey) {
													var indexInSelection;

													if ((indexInSelection = jQuery
																.inArray(
																	value[index],
																	selection)) > -1) {
														selection
														.splice(
															indexInSelection,
															1);
													} else {
														selection
														.push(value[index]);
													}

													jQuery(
														this)
													.toggleClass(
														"selectedRow");
												} else {
													table
													.find(
														"tbody tr")
													.removeClass(
														"selectedRow");
													jQuery(
														this)
													.addClass(
														"selectedRow");

													selection.length = 0;
													selection
													.push(value[index]);
												}

												console
												.log("Selection");
												console
												.log(selection);

												scope
												.$apply();
											});
										}

										// Mark
										// first
										// row

										table
										.find(
											"tbody tr")
										.last()
										.addClass(
											"lastRow");

										// Create
										// Datatables

										try {
											table
											.dataTable({
												aoColumnDefs : aoColumnDefs
											});
										} catch (x) {
											console
											.log("Cannot create data table");
											console
											.log(x);
										}

										document.body.style.cursor = "default";
									},
										100);
								});
							}
						};
					}
				};
			});
			
			
		   this.angularModule.controller("ReportDefinitionController", function($compile) {
            angularCompile = $compile;
         });

			this.angularModule
					.directive(
							'required',
							function() {
								console.log("required parsed");
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"required");

													if (viewValue == null
															|| viewValue == "") {
														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "required",
																	message : "Value required ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														ctrl.$setValidity(
																'required',
																false);

														return undefined;
													} else {
														ctrl.$setValidity(
																'required',
																true);

														return viewValue;
													}
												});
									}
								};
							});

			var INTEGER_REGEXP = /^\-?\d*$/;

			this.angularModule
					.directive(
							'sdInteger',
							function() {
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"sdInteger");

													if (INTEGER_REGEXP
															.test(viewValue)) {
														ctrl.$setValidity(
																'sdInteger',
																true);

														return viewValue;
													} else {
														ctrl.$setValidity(
																'sdInteger',
																false);

														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "sdInteger",
																	message : "Invalid Integer Format ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														return undefined;
													}
												});
									}
								};
							});

			var DECIMAL_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;

			this.angularModule
					.directive(
							'sdDecimal',
							function() {
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"sdDecimal");

													if (DECIMAL_REGEXP
															.test(viewValue)) {
														ctrl.$setValidity(
																'sdDecimal',
																true);

														return parseFloat(viewValue
																.replace(',',
																		'.'));
													} else {
														ctrl.$setValidity(
																'sdDecimal',
																false);

														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "sdDecimal",
																	message : "Invalid Decimal Format ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														return undefined;
													}
												});
									}
								};
							});

			// Date Picker
			this.angularModule.directive('sdDatetime', function() {
				console.debug("sd-datetime parsed");
				return {
					restrict : "A",
					require : "ngModel",
					link : function(scope, element, attrs, controller) {
						var timeFormat = "hh:mm:ss:l";
						var dateFormat = "yy/mm/dd";
						if(attrs.timeFormat){
							timeFormat = attrs.timeFormat;
						}
						if(attrs.dateFormat){
							dateFormat = attrs.dateFormat;
						}
						element.datetimepicker({
							inline : true,
							timeFormat : timeFormat,
							dateFormat : dateFormat,
							onSelect : function(date) {
								scope.$apply(function () {
								   controller.$setViewValue(date);
								});
							}
						});
					}
				};
			});
			
			this.angularModule.directive('sdDate', function() {
				console.debug("sd-date parsed");
				return {
					restrict : "A",
					require : "ngModel",
					link : function(scope, element, attrs, controller) {
						element.datepicker({
							inline : true,
							dateFormat : 'yy-mm-dd', // I18N
							onSelect : function(date) {
								scope.$apply(function () {
								   controller.$setViewValue(date);
                       });
							}
						});

						// scope.$watch(ngModel, function(val) {
						// console.debug("val = " + val);
						//
						// if (val) {
						// element.datepicker("setDate", new Date(val));
						// } else {
						// element.datepicker("setDate", null);
						// }
						// });
					}
				};
			});

			// Allow for on blur semantics until Angular JS releases it (TODO this does not work)

			this.angularModule.directive('ngBlur', [ '$parse',
					function($parse) {
						return function(scope, element, attr) {
							var fn = $parse(attr['ngBlur']);
							element.bind('blur', function(event) {
								scope.$apply(function() {
									fn(scope, {
										$event : event
									});
								});
							});
						};
					} ]);

			// Data Table
			

			/*Angular directive to wrap the ace code editor and provide
			 *a basic set of functionality.
			 *Supports via attributes:
			 *	mode: defines the language we will support
			 *	theme: defines the css for the syntax highlighter
			 *	snippets: whether or not to support snippets (if available)
			 *	ng-Model: model we will bind our value to.*/
			this.angularModule.directive("ippAce",function(){
				
			    /*Check for our global ace object, if it isn't there then bail.*/
			    if(!ace){return;}
			    
				/*Hanging an object off of window.top.ace that we can use 
				 *for global state related to our module.*/
				if(ace.hasOwnProperty("ext_userDefined")===false){
					ace["ext_userDefined"]={};/*collect here for sameness*/
					ace.ext_userDefined.completers=[]; /*collection to keep track of completers we add*/
				}
				
				/*Our session completer with ace language utils compatible interface.*/
				var completerFac={
						/*Completer which allows the user to specify a keyword list attached
						 *to a drlEditor session via session.ext_userDefined.$keywordList*/
						getSessionCompleter: function(options){
							var metaName="Data",score=9999;
							if(options){
								metaName=options.metaName || metaName;
								score=options.score || score;
							}
							return {
							    getCompletions: function(editor, session, pos, prefix, callback) {
							        var keywords=[];
							        if(session.ext_userDefined && session.ext_userDefined.$keywordList){
							        	keywords=session.ext_userDefined.$keywordList;
							        }
							        var t=session.getTextRange({
							        	"start":{row: pos.row,column:pos.column-1},
							        	"end":{row: pos.row,column:pos.column}
							        });
							        keywords = keywords.filter(function(w) {
							            return w.lastIndexOf(prefix, 0) == 0;
							        });
							        callback(null, keywords.map(function(word) {
							            return {
							                "name": word,
							                "value": word,
							                "score": score,
							                "meta": metaName
							            };
							        }));
							    }
							};
						}
				};
				var completer=completerFac.getSessionCompleter();
				var isPresent=false,
					temp,
					compString=completer.getCompletions.toString(),
					langTools,
					compLength=ace.ext_userDefined.completers.length;
				
				while(compLength--){
					temp=ace.ext_userDefined.completers[compLength];
					if(temp===compString){
						isPresent=true;
						console.log("Repeater found, will not be added.");
						break;
					}
				}
				
				if(isPresent===false){
					langTools=ace.define.modules["ace/ext/language_tools"];
					if(langTools){
						langTools.addCompleter(completer);
						ace.ext_userDefined.completers.push(completer.getCompletions.toString());
					}
					else{
						ace.config.loadModule("ace/ext/language_tools",function(){
							langTools=ace.define.modules["ace/ext/language_tools"];	
							langTools.addCompleter(completer);
							ace.ext_userDefined.completers.push(completer.getCompletions.toString());
						});
					}
				}

				
			    return {
			      restrict: 'EA', /*Elements and attributes*/
			      require: '?ngModel',
			      link: function (scope, elm, attrs, ngModel) {
			          var options, 
			          	  editor, 
			          	  session, 
			          	  langTools,
			          	  keywords;
			          
			          /*Setting up language tool options for editor*/
			          options={
			  					"enableSnippets": !!attrs.snippets,
			  					"enableBasicAutocompletion": !!attrs.autocomplete
			          };

			          /*Setting options from user attributes*/
			          editor = window.ace.edit(elm[0]); 
			          session = editor.getSession();
			          session.setMode("ace/mode/" + attrs.mode);
			          editor.setTheme("ace/theme/" + attrs.theme);
			          
			          /*Set our session keywords, we support either an array or comma-delimited string*/
			          keywords=attrs.keywords;
			          if(keywords){
				          if( Object.prototype.toString.call( keywords ) !== '[object Array]' ) {
				        	    keywords=keywords.split(",");
				          }
				          if(session.hasOwnProperty("ext_userDefined")===false){
								session["ext_userDefined"]={};
							}
						  session["ext_userDefined"].$keywordList=keywords;
			          }
			          /*Get a reference to languageTools module, if it exists*/
			          langTools=ace.define.modules["ace/ext/language_tools"];
			          
			          /*if module is loaded go ahead and set our options, otherwise load it 
			           *and set options on the callback.*/
			          if(langTools){
			              editor.setOptions(options);
			          }
			          else{
			            ace.config.loadModule("ace/ext/language_tools",function(){
			              editor.setOptions(options);
			            });
			          }
			          
			          /*set our initial value to that of our bound model*/
			          ngModel.$render = function () {
			            session.setValue(ngModel.$viewValue);
			          };
			          
			          /*any time our editors session registers a change event,
			           *set the resultant value on our bound model.*/
			          session.on('change',function(){
			            scope.$apply(function(){
			              ngModel.$setViewValue(session.getValue());
			            });
			          });
			        }
			      };
			});
			/****Angular Ace Directive END *****/
			
		};
	}
}