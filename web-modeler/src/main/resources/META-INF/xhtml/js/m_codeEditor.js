/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This

 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "jquery", "jshint" ],
		function(jquery, jshint) {
			// Interface
			return {
				getCodeEditor : function(textArea) {
					return new CodeEditor(textArea);
				}
			};
		
			function CodeEditor(textArea) {
		
				var editor = null;
				var wrapper = null;
				
				var globalVariables = null;
				var hlLine = null;
				
				var EDITOR_STYLECLASS_ENABLED = "CodeMirror-enabled";
				var EDITOR_STYLECLASS_DISABLED = "CodeMirror-disabled";
				var EDITOR_STYLECLASS_MATCH_HIGHLIGHT = "CodeMirror-matchhighlight";
				var EDITOR_STYLECLASS_ACTIVELINE = "activeline";
				
				var EDITOR_READONLY = "readOnly";
				var EDITOR_NOCURSOR = "nocursor";
				
				// Set up code editor for JS code expression
				CodeMirror.commands.autocomplete = function(cm) {
					CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
				};

				var waiting;
				editor = CodeMirror.fromTextArea(textArea, {
					mode: "javascript",
					theme: "eclipse",
					lineNumbers: true,
					lineWrapping: true,
					indentUnit: 4,
					matchBrackets: true,
					extraKeys: {"Ctrl-Space": "autocomplete"},
					onCursorActivity: function() {
						// Highlight selected text
						editor.matchHighlight(EDITOR_STYLECLASS_MATCH_HIGHLIGHT);
						// Set active line
						editor.setLineClass(hlLine, null, null);
						hlLine = editor.setLineClass(editor.getCursor().line, null, EDITOR_STYLECLASS_ACTIVELINE);
					},
					onBlur: function() {
						editor.save();
						// Programmatically invoke the change handler on the hidden text area
						// as it will not be invoked automatically
						jQuery(editor.getTextArea()).change();
					},
					onChange: function() {
						clearTimeout(waiting);
						waiting = setTimeout(showErrors, 500);
					}
				});
				
				wrapper = editor.getWrapperElement();
				hlLine = editor.setLineClass(0, EDITOR_STYLECLASS_ACTIVELINE);
				setTimeout(showErrors, 100);
				
		
				function getErrors(source) {
					var errors = {};
					var err, lineNumber;
					
					var options = {undef: true, smarttabs: true};
					var globals = {};
					for (var variable in globalVariables) {
						globals[variable] = true;
					}
					JSHINT(source, options, globals);
					for (var i = 0; i < JSHINT.errors.length; ++i) {
						err = JSHINT.errors[i];
						if (!err) continue;
						lineNumber = err.line - 1;
						if (!errors[lineNumber]) errors[lineNumber] = []; 
						errors[lineNumber].push(err.reason);
					}
					
					return errors;
				}

				function showErrors() {
					var errorLineNumbers = [];
					editor.operation(function(){
						for (var i = 0; i < errorLineNumbers.length; ++i)
							editor.clearMarker(errorLineNumbers[i]);
						errorLineNumbers.length = 0;
					
						var html;
						var errors = getErrors(editor.getValue());
						for (var lineNumber in errors) {
							html = '<div class="gutter-warning"><div class="tooltip"><ul>';
							for (var i = 0; i < errors[lineNumber].length; i++)
								html += '<li>' + errors[lineNumber][i] + '</li>';
							html += '</ul></div> %N%</div>';
							
							errorLineNumbers.push(Number(lineNumber));
							editor.setMarker(Number(lineNumber), html);
						}
					});
					
					jQuery('.gutter-warning').hover(function() {
						// Hover over code
						var title = jQuery(this).attr('title');
						jQuery(this).data('tipText', title).removeAttr('title');

						var tooltip = jQuery(this).children('.tooltip :first'); 
						// tooltip.position({at: 'right top', of: jQuery(this), my: 'right top'});
						tooltip.css({ marginLeft: '+=45px', marginTop: '+=15px' });
						tooltip.fadeIn('slow');
					}, function() {
							// Hover out code
							jQuery(this).attr('title', jQuery(this).data('tipText'));
							var tooltip = jQuery(this).children('.tooltip :first');
							tooltip.hide();
							tooltip.css({ marginLeft: '-=45px', marginTop: '-=15px' });
					});
				
					var info = editor.getScrollInfo();
					var after = editor.charCoords({line: editor.getCursor().line + 1, ch: 0}, "local").top;
					if (info.top + info.clientHeight < after)
						editor.scrollTo(null, after - info.clientHeight + 3);
				}

				CodeEditor.prototype.setValue = function(val) {
					editor.setValue(val);
				};
		
				CodeEditor.prototype.disable = function() {
					editor.setOption(EDITOR_READONLY, EDITOR_NOCURSOR);
					jQuery(wrapper).addClass(EDITOR_STYLECLASS_ENABLED);
				};

				CodeEditor.prototype.enable = function() {
					editor.setOption(EDITOR_READONLY, false);
					jQuery(wrapper).removeClass(EDITOR_STYLECLASS_DISABLED);
				};
		
				CodeEditor.prototype.getWrapper = function() {
					return wrapper;
				};
		
				CodeEditor.prototype.setGlobalVariables = function(data) {
					globalVariables = data;
					
					// Bind the Model Data as top "window" level objects to be used for Code Editor auto-complete
					for (var key in globalVariables) {
						window[key] = globalVariables[key];
					}
				};
		
				CodeEditor.prototype.save = function() {
					editor.save();
				};
				
				CodeEditor.prototype.refresh = function() {
					editor.refresh();
				};
			}
		}
	);