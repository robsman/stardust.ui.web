/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

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
		AngularAdapter.prototype.initialize = function(angular) {
			this.angular = angular;
			this.angularModule = angular.module('angularApp', []);

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

		/**
		 * 
		 */
		AngularAdapter.prototype.initalizeFormatConstraints = function() {
			console.debug("Format constraints initialized");

			var self = this;

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

			this.angularModule.directive('sdDate', function() {
				console.debug("sd-date parsed");
				return {
					restrict : "A",
					require : "ngModel",
					link : function(scope, element, attrs, controller) {
						element.datepicker({
							inline : true,
							dateFormat : 'dd.mm.yy', // I18N
							onSelect : function() {
								var date = new Date(element
										.datepicker("getDate"));

								console.debug("Date set to " + date);
								console.debug("Controller");
								console.debug(controller);
								console.debug("Scope");
								console.debug(scope);

								controller.$modelValue = date.toISOString();
								scope.$apply();

								console.debug("Controller");
								console.debug(controller);
								console.debug("Scope");
								console.debug(scope);
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

			this.angularModule.directive('dataTable', function() {
				return function(scope, element, attrs) {
					console.log("Initialize data table");

					options = {
						"bStateSave" : true,
						"iCookieDuration" : 2419200, /* 1 month */
						"bJQueryUI" : true,
						"bPaginate" : false,
						"bLengthChange" : false,
						"bFilter" : false,
						"bInfo" : false,
						"bDestroy" : true
					};

					var dataTable = element.dataTable(options);

					scope.$watch(attrs.rowData, function(value) {
						var val = value || null;

						if (val) {
							dataTable.fnClearTable();
							dataTable.fnAddData(scope.$eval(attrs.rowData));
						}
					});
				};
			});
			

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