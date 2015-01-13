/*
 * ngDialog - easy modals and popup windows
 * http://github.com/likeastore/ngDialog
 * (c) 2013 MIT License, https://likeastore.com
 */


			
			/*
			 * ngDialog - easy modals and popup windows
			 * http://github.com/likeastore/ngDialog
			 * (c) 2013 MIT License, https://likeastore.com
			 */

'use strict';

var module = angular.module('ngDialog', []);

var $el = angular.element;
var isDef = angular.isDefined;
var style = (document.body || document.documentElement).style;
var animationEndSupport = isDef(style.animation) || isDef(style.WebkitAnimation) || isDef(style.MozAnimation) || isDef(style.MsAnimation) || isDef(style.OAnimation);
var animationEndEvent = 'animationend webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend';
var forceBodyReload = false;

module.provider('ngDialog', function () {
	var defaults = this.defaults = {
		className: 'ngdialog-theme-default',
		plain: false,
		showClose: true,
		closeByDocument: true,
		closeByEscape: true,
		appendTo: false
	};

	this.setForceBodyReload = function (_useIt) {
		forceBodyReload = _useIt || false;
	};

	var globalID = 0, dialogsCount = 0, closeByDocumentHandler, defers = {};

	this.$get = ['$document', '$templateCache', '$compile', '$q', '$http', '$rootScope', '$timeout', '$window',
		function ($document, $templateCache, $compile, $q, $http, $rootScope, $timeout, $window) {
			var $body = $document.find('body');
			if (defaults.forceBodyReload) {
				$rootScope.$on('$locationChangeSuccess', function () {
					$body = $document.find('body');
				});
			}

			var privateMethods = {
				onDocumentKeydown: function (event) {
					if (event.keyCode === 27) {
						publicMethods.close();
					}
				},

				setBodyPadding: function (width) {
					var originalBodyPadding = parseInt(($body.css('padding-right') || 0), 10);
					$body.css('padding-right', (originalBodyPadding + width) + 'px');
					$body.data('ng-dialog-original-padding', originalBodyPadding);
				},

				resetBodyPadding: function () {
					var originalBodyPadding = $body.data('ng-dialog-original-padding');
					if (originalBodyPadding) {
						$body.css('padding-right', originalBodyPadding + 'px');
					} else {
						$body.css('padding-right', '');
					}
				},

				closeDialog: function ($dialog,bypassResolve) {
					var id = $dialog.attr('id');
					if (typeof window.Hammer !== 'undefined') {
						window.Hammer($dialog[0]).off('tap', closeByDocumentHandler);
					} else {
						$dialog.unbind('click');
					}

					if (dialogsCount === 1) {
						$body.unbind('keydown');
					}

					if (!$dialog.hasClass("ngdialog-closing")){
						dialogsCount -= 1;
					}

					if (animationEndSupport) {
						$dialog.unbind(animationEndEvent).bind(animationEndEvent, function () {
							$dialog.scope().$destroy();
							$dialog.remove();
							if (dialogsCount === 0) {
								$body.removeClass('ngdialog-open');
								privateMethods.resetBodyPadding();
							}
						}).addClass('ngdialog-closing');
					} else {
						$dialog.scope().$destroy();
						$dialog.remove();
						if (dialogsCount === 0) {
							$body.removeClass('ngdialog-open');
							privateMethods.resetBodyPadding();
						}
					}
					if (defers[id]) {

						defers[id].resolve({
							id: id,
							$dialog: $dialog,
							remainingDialogs: dialogsCount
						});

						delete defers[id];
					}
					$rootScope.$broadcast('ngDialog.closed', $dialog);
				}
			};

			var publicMethods = {

				/*
				 * @param {Object} options:
				 * - template {String} - id of ng-template, url for partial, plain string (if enabled)
				 * - plain {Boolean} - enable plain string templates, default false
				 * - scope {Object}
				 * - controller {String}
				 * - className {String} - dialog theme class
				 * - showClose {Boolean} - show close button, default true
				 * - closeByEscape {Boolean} - default true
				 * - closeByDocument {Boolean} - default true
				 *
				 * @return {Object} dialog
				 */
				open: function (opts,bypassResolve) {
					var self = this,
					    options = angular.copy(defaults),
					    contentClasses=['ngdialog-content'],
					    sHtml='',
					    overlayClasses=[];
					
					bypassResolve = bypassResolve || false;
					
					opts = opts || {};
					angular.extend(options, opts);

					globalID += 1;

					self.latestID = 'ngdialog' + globalID;

					var defer;
					defers[self.latestID] = defer = $q.defer();

					var scope = angular.isObject(options.scope) ? options.scope.$new() : $rootScope.$new();
					scope.__ngDialog={};
					var $dialog, $dialogParent;

					$q.when(loadTemplate(options.template)).then(function (template) {
						template = angular.isString(template) ?
							template :
							template.data && angular.isString(template.data) ?
								template.data :
								'';

						$templateCache.put(options.template, template);

						if (options.showClose) {
							template += '<div class="ngdialog-close"></div>';
						}

						if(options.showOverlay === true){
							overlayClasses.push('ngdialog-overlay');
							sHtml='<div class="' + overlayClasses.join(' ') + '"></div>';
						}
						else{
							contentClasses.push('no-overlay');
						}
						
						self.$result = $dialog = $el('<div id="ngdialog' + globalID + '" class="ngdialog"></div>');
						
						if(options.isMoveable === true){
							$dialog.attr('sd-moveable','true');
						}
						
						
						$dialog.html( sHtml +
								     '<div  class="'+ contentClasses.join(' ') + '">' + 
								     template + 
								     '</div>');

						if (options.controller && angular.isString(options.controller)) {
							$dialog.attr('ng-controller', options.controller);
						}

						if (options.className) {
							$dialog.addClass(options.className);
						}

						if (options.data && angular.isString(options.data)) {
							scope.ngDialogData = options.data.replace(/^\s*/, '')[0] === '{' ? angular.fromJson(options.data) : options.data;
						}
						
						if(options.userTemplate && angular.isString(options.userTemplate)){
							scope.userTemplate = options.userTemplate;
						}
						
						if(options.title && angular.isString(options.title)){
							scope.title = options.title;
						}
						
						if (options.appendTo && angular.isString(options.appendTo)) {
							$dialogParent = angular.element(document.querySelector(options.appendTo));
						} else {
							$dialogParent = $body;
						}

						scope.closeThisDialog = function() {
							privateMethods.closeDialog($dialog);
						};

						$timeout(function () {
							$compile($dialog)(scope);

							var widthDiffs = $window.innerWidth - $body.prop('clientWidth');
							$body.addClass('ngdialog-open');
							var scrollBarWidth = widthDiffs - ($window.innerWidth - $body.prop('clientWidth'));
							if (scrollBarWidth > 0) {
								privateMethods.setBodyPadding(scrollBarWidth);
							}
							$dialogParent.append($dialog);
							
							/* ZZM: Added basic position functionality
							 *      Only supports horizontal align
							 * USAGE: 
							 *      align:'left|top|right'
							 *      position: [x,y] --as coordinate
							 */
							 if (options.isCentered !== true && angular.isArray(options.position)) {
				                  var myBounds= $dialog[0].getBoundingClientRect(),
				                      posX = options.position[0],
				                      posY = options.position[1],
				                      align,
				                      style;
				                      
				                  if(options.align){
				                    align=options.align.split("-");
				                    switch(align[0]){
				                      case "right":
				                        posX = posX - myBounds.width;
				                        break;
				                      case "left":
				                        posX = posX;
				                        break;
				                      case "center":
				                        posX = posX - myBounds.width/2;
				                        break;
				                    }
				                  }
				                  
				                style="left:" + posX + "px;top:" + posY + "px;"
	  							$dialog.attr("style",style);
  							 } 
							/*Position functionality end*/
							 
							 /*ZZM: if callback function specified pass promise*/
							 if(!bypassResolve && options.onOpen && angular.isFunction(options.onOpen)){
								options.onOpen({'res' : {'promise' : defer.promise}});
							 }
							 
							$rootScope.$broadcast('ngDialog.opened', $dialog);
						});

						if (options.closeByEscape) {
							$body.bind('keydown', privateMethods.onDocumentKeydown);
						}

						closeByDocumentHandler = function (event) {
							var isOverlay = options.closeByDocument ? $el(event.target).hasClass('ngdialog-overlay') : false;
							var isCloseBtn = $el(event.target).hasClass('ngdialog-close');

							if (isOverlay || isCloseBtn) {
								publicMethods.close($dialog.attr('id'));
							}
						};

						if (typeof window.Hammer !== 'undefined') {
							window.Hammer($dialog[0]).on('tap', closeByDocumentHandler);
						} else {
							$dialog.bind('click', closeByDocumentHandler);
						}

						dialogsCount += 1;

						return publicMethods;
					});

					return {
						id: 'ngdialog' + globalID,
						closePromise: defer.promise,
						close: function() {
							privateMethods.closeDialog($dialog,true);
						}
					};

					function loadTemplate (tmpl) {
						if (!tmpl) {
							return 'Empty template';
						}

						if (angular.isString(tmpl) && options.plain) {
							return tmpl;
						}

						return $templateCache.get(tmpl) || $http.get(tmpl, { cache: true });
					}

					return defer.promise;
				},

				/*
				 * @param {Object} options:
				 * - template {String} - id of ng-template, url for partial, plain string (if enabled)
				 * - plain {Boolean} - enable plain string templates, default false
				 * - scope {Object}
				 * - controller {String}
				 * - className {String} - dialog theme class
				 * - showClose {Boolean} - show close button, default true
				 * - closeByEscape {Boolean} - default false
				 * - closeByDocument {Boolean} - default false
				 *
				 * @return {Object} dialog
				 */
				openConfirm: function (opts) {
					var defer = $q.defer();

					var options = {
						closeByEscape: false,
						closeByDocument: false
					};
					angular.extend(options, opts);

					options.scope = angular.isObject(options.scope) ? options.scope.$new() : $rootScope.$new();
					options.scope.confirm = function (value) {
						//ZZM: extend so we don't accidentally wipe the returned value on $destroy
						//this will happen with values returned that are on our ngDialog scope.
						defer.resolve(angular.extend({},value));
						openResult.close();
					};

					var openResult = publicMethods.open(options,true);
					openResult.closePromise.then(function () {
						defer.reject();
					});
					
					/*ZZM: if callback function specified pass promise*/
					 if(options.onOpen && angular.isFunction(options.onOpen)){
						options.onOpen({'res' : {'promise' : defer.promise}});
					 }
					
					return defer.promise;
				},

				/*
				 * @param {String} id
				 * @return {Object} dialog
				 */
				close: function (id) {
					var $dialog = $el(document.getElementById(id));

					if ($dialog.length) {
						privateMethods.closeDialog($dialog);
					} else {
						publicMethods.closeAll();
					}

					return publicMethods;
				},

				closeAll: function () {
					var $all = document.querySelectorAll('.ngdialog');

					angular.forEach($all, function (dialog) {
						privateMethods.closeDialog($el(dialog));
					});
				}
			};

			return publicMethods;
		}];
});

/*Base ngDialog Directive*/
module.directive('ngDialog', ['ngDialog', function (ngDialog) {
	return {
		restrict: 'A',
		scope : {
			ngDialogScope : '='
		},
		link: function (scope, elem, attrs) {
			elem.on('click', function (e) {
				e.preventDefault();

				var ngDialogScope = angular.isDefined(scope.ngDialogScope) ? scope.ngDialogScope : 'noScope';
				angular.isDefined(attrs.ngDialogClosePrevious) && ngDialog.close(attrs.ngDialogClosePrevious);

				ngDialog.open({
					template: attrs.ngDialog,
					className: attrs.ngDialogClass,
					controller: attrs.ngDialogController,
					scope: ngDialogScope ,
					data: attrs.ngDialogData,
					showClose: attrs.ngDialogShowClose === 'false' ? false : true,
					closeByDocument: attrs.ngDialogCloseByDocument === 'false' ? false : true,
					closeByEscape: attrs.ngDialogCloseByEscape === 'false' ? false : true
				});
			});
		}
	};
}]);

			
		