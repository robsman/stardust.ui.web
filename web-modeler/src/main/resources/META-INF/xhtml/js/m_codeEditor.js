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
		[ "jquery" ],
		function() {
			return {
				getCodeEditor : function(textArea) {
					return new CodeEditor(textArea);
				}
			};
		
			function getErrors(source) {
				var errors = {};
				/*var err, lineNumber;
				
				var options = {undef: true, smarttabs: true};
				var globalData = m_dataTraversal.getAllDataAsJavaScriptObjects(m_model.findModel('Loan'));
				var globals = {};
				for (var variable in globalData) {
					globals[variable] = true;
				}
				JSHINT(source, options, globals);
				for (var i = 0; i < JSHINT.errors.length; ++i) {
					err = JSHINT.errors[i];
					if (!err) continue;
					lineNumber = err.line - 1;
					if (!errors[lineNumber]) errors[lineNumber] = []; 
					errors[lineNumber].push(err.reason);
				}*/
				
				return errors;
			}

			var errorLineNumbers = [];
			function showErrors() {
				/*editor.operation(function(){
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
				});*/
				
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

			function CodeEditor(textArea) {
		
				// Set up code editor for JS code expression
				CodeMirror.commands.autocomplete = function(cm) {
					CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
				};

				var waiting;
				var editor = CodeMirror.fromTextArea(textArea, {
					mode: "javascript",
					theme: "eclipse",
					lineNumbers: true,
					lineWrapping: true,
					indentUnit: 4,
					matchBrackets: true,
					extraKeys: {"Ctrl-Space": "autocomplete"},
					onCursorActivity: function() {
						// Highlight selected text
						editor.matchHighlight("CodeMirror-matchhighlight");
						// Set active line
						editor.setLineClass(hlLine, null, null);
						hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
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
				var hlLine = editor.setLineClass(0, "activeline");
				setTimeout(showErrors, 100);
				
				var wrapper = editor.getWrapperElement();
		
				CodeEditor.prototype.setValue = function(val) {
					editor.setValue(val);
				};
		
				/*CodeEditor.prototype.setOption = function(id, value) {
					editor.setOption(id, value);
				};*/
		
				CodeEditor.prototype.disable = function() {
					editor.setOption("readOnly", "nocursor");
					jQuery(wrapper).addClass("CodeMirror-disabled");
				};

				CodeEditor.prototype.enable = function() {
					editor.setOption("readOnly", false);
					jQuery(wrapper).removeClass("CodeMirror-disabled");
				};
		
				CodeEditor.prototype.getWrapper = function() {
					return wrapper;
				};
		
				CodeEditor.prototype.setGlobalData = function(globalVariables) {
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