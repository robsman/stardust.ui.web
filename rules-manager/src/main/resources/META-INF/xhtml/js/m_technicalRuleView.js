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
		[   "bpm-modeler/js/m_utils",
			"rules-manager/js/m_commandsDispatcher", 
			"bpm-modeler/js/m_jsfViewManager",
			"rules-manager/js/m_ruleSet", 
			"rules-manager/js/m_codeEditorAce",
			"rules-manager/js/m_i18nMapper",
			"rules-manager/js/m_ruleSetCommandDispatcher",
			"rules-manager/js/m_ruleSetCommand",
			"rules-manager/js/hotDecisionTable/m_typeParser",
			"rules-manager/js/m_autoCompleters"],
		function(m_utils,CommandsDispatcher, 
				 m_jsfViewManager, RuleSet,m_codeEditorAce,m_i18nMapper,
				 m_ruleSetCommandDispatcher,m_ruleSetCommand,
				 m_typeParser,m_autoCompleters) {
			return {
				initialize : function(uuid,techRuleID,options) {
					var ruleSet = RuleSet.findRuleSetByUuid(uuid);
					var techRule=ruleSet.findTechnicalRuleByUuid(techRuleID);
					var view = new TechnicalRuleView();
					view.initialize(ruleSet,techRule,options);
				}
			};


			function TechnicalRuleView() {

				TechnicalRuleView.prototype.initialize = function(ruleSet,techRule,options) {
					
					var themeMenuHandler,   /*Handler function to assign to any UI element that controls themes for ACE*/
						themeMenu,          /*string-domFrag for our theme options for ACE*/
						$themeMenu,         /*the jqueryui menu object built from themeMenu*/
						replaceMenu,		/*string-domFrag for our replace optiosn menu*/
						$replaceMenu,       /*menu containing replace options (Current || All)*/
						replaceMenuHandler, /*handler for click events for the replaceMenu toolbar button*/
						findFunc,		  	/*Function wrapper for DRL editor find function*/   
						fontSizeMenu,       /*string-domFrag for our fontsize options for ACE*/
						$fontSizeMenu,      /*Menu containing font size options for the drlEditor*/
						$drlEditorTextArea, /*JQUERY wrapped textarea of our DRLeditor.*/
						cnstCmd,            /*enumeration of our Contants representing command Factory events*/
						drlSession,         /*Instance of our DRL editors session, used to tag keywords to*/
						sessionCompleter,   /*Instance of a autocompleter from m_autoCompleters*/
						fontSizeMenuHandler;/*handler for click events on $fontSizeMenu*/
					
					
					/* Close over the local UI elements we will reference via JQUERY etc.
					 * The options object passed in to our initialize function should
					 * specify valid selectors for each value below.
					 * */
					var uiElements={
							uuidOutput: m_utils.jQuerySelect(options.selectors.uuidOutput),
							idOutput: m_utils.jQuerySelect(options.selectors.idOutput),
							nameInput: m_utils.jQuerySelect(options.selectors.nameInput),
							drlEditor: undefined,
							drlEditorTextArea: undefined,
							drlEditorReplaceOptionVal: m_utils.jQuerySelect(options.selectors.drlEditorReplaceOptionVal),
							drlEditorReplaceOption: m_utils.jQuerySelect(options.selectors.drlEditorReplaceOption),
							drlEditorReplaceVal: m_utils.jQuerySelect(options.selectors.drlEditorReplaceVal),
							drlEditorReplaceAction: m_utils.jQuerySelect(options.selectors.drlEditorReplaceAction),
							drlEditorFindVal: m_utils.jQuerySelect(options.selectors.drlEditorFindVal),
							drlFindForwards: m_utils.jQuerySelect(options.selectors.drlFindForwards),
							drlFindBackwards: m_utils.jQuerySelect(options.selectors.drlFindBackwards),
							gotoLineNo: m_utils.jQuerySelect(options.selectors.gotoLineNo),
							selectedFontSize: m_utils.jQuerySelect(options.selectors.selectedFontSize),
							fontDropdown: m_utils.jQuerySelect(options.selectors.fontDropdown),
							drlSelectedTheme: m_utils.jQuerySelect(options.selectors.drlSelectedTheme),
							themeDropdown: m_utils.jQuerySelect(options.selectors.themeDropdown),
							drlTabControl: undefined,
							codeTab: m_utils.jQuerySelect(options.selectors.codeTab),
							findLabel: m_utils.jQuerySelect(options.selectors.findLabel),
							lineNumberLabel: m_utils.jQuerySelect(options.selectors.lineNumberLabel),
							replaceLabel: m_utils.jQuerySelect(options.selectors.replaceLabel),
							replaceMenu: m_utils.jQuerySelect(options.selectors.replaceMenu),
							descriptionTextarea: m_utils.jQuerySelect(options.selectors.descriptionTextarea),
							optReplaceCurrent: m_utils.jQuerySelect(options.selectors.optReplaceCurrent),
							optReplaceAll : m_utils.jQuerySelect(options.selectors.optReplaceAll ),
							idLabel : m_utils.jQuerySelect(options.selectors.idLabel ),
							nameLabel : m_utils.jQuerySelect(options.selectors.nameLabel ),
							descriptionLabel : m_utils.jQuerySelect(options.selectors.descriptionLabel ),
							uuidLabel : m_utils.jQuerySelect(options.selectors.uuidLabel ),
							stringifyParamDefs : m_utils.jQuerySelect(options.selectors.stringifyParamDefs )
					};
					
					/*Map uiElements to their resource text values*/
					m_i18nMapper.map(options,uiElements,true);
					
					/*For brevity , access command constants using shorthand*/
					cnstCMD=m_ruleSetCommand.commands;
					
				    /*initialize tabs control*/
					uiElements.drlTabControl= m_utils.jQuerySelect(options.selectors.tabs).tabs();

					//initialize ACE editor for expert mode drl scripting
					//Take note on the next line we are grabbing the DOM element, not the JQUERY object.
					uiElements.drlEditorTextArea=m_utils.jQuerySelect(options.selectors.drlEditor)[0];
					uiElements.drlEditor=m_codeEditorAce.getDrlEditor(uiElements.drlEditorTextArea);
					
					/*Loop through each Fact/ParameterDefinition and construct Keywords 
					 *for each path within the model hierarchy.*/
					var pDef,k,completerStrings=[];
					for( k in ruleSet.parameterDefinitions){
						if(ruleSet.parameterDefinitions.hasOwnProperty(k)){
							pDef=ruleSet.parameterDefinitions[k];
							completerStrings=completerStrings.concat(m_typeParser.parseParamDefToStringFrags(pDef));
						}
					}
					
					/*Set the data our autoCompleter will need.*/
					uiElements.drlEditor.setSessionData("ruleSetKeywords",completerStrings);
					
					/*retrieve an instance of our autocompleter*/
					sessionCompleter=m_autoCompleters.getSessionCompleter(
							"ruleSetKeywords",
							{metaName:"Data",score:9999});
					
					/*Listen for our module loaded events. Specifically, for our
					 *language tools being loaded.*/
					$(uiElements.drlEditor).on("moduleLoaded",function(event,module){
						if(module.name==="ace/ext/language_tools"){
							uiElements.drlEditor.addCompleter(sessionCompleter);
						}
					});
					
					/*Now, load our language tools extension. This will enable snippets and autocomplete.
					 *This will generate a moduleLoaded event for ace/ext/language_tools 
					 *even if the module is already loaded.*/
					uiElements.drlEditor.loadLanguageTools();
					
					/*****Menu Building Section for our DRL editors associated toolbar*****/
					
					/*Add menu for text replacement options (all||current)*/
					replaceMenu=$("#replaceMenu");
					$replaceMenu=$(replaceMenu)
						.menu()
						.appendTo(m_utils.jQuerySelect("body"))
						.position({
					        my: "left top",
					        at: "left bottom",
					        of: uiElements.drlEditorReplaceOption
					      })
					     .on( "menuselect", function( event, ui ) {
						    	var selectedReplaceOption=ui.item.text();
						    	uiElements.drlEditorReplaceOptionVal.text(selectedReplaceOption);
						    	$replaceMenu.hide();
						      })
						.hide();
					replaceMenuHandler=function(){
						$replaceMenu.show();
					};
					uiElements.drlEditorReplaceOption.on("click",replaceMenuHandler);
					/*End section for text replacement options menu builder*/
					
					
					/*Add drl editor replace functionality*/
					uiElements.drlEditorReplaceAction.on("click",function(){
						var replaceOption=uiElements.drlEditorReplaceOptionVal.text();
						if(replaceOption==="Replace Current"){
							uiElements.drlEditor.editor.replace(uiElements.drlEditorReplaceVal.val());
						}
						else{
							uiElements.drlEditor.editor.replaceAll(uiElements.drlEditorReplaceVal.val());
						}
					});
					
					/*Add DRL editor find function*/
					findFunc=function(doBackwards){
						var findMe=uiElements.drlEditorFindVal.val();
						uiElements.drlEditor.editor.find(findMe,{
						    backwards: doBackwards || false,
						    wrap: false,
						    caseSensitive: false,
						    wholeWord: false,
						    regExp: false,
						    skipCurrent:true
						});
					};
					
					uiElements.drlFindBackwards.on("click",function(){
						findFunc(true);
					});
					uiElements.drlFindForwards.on("click",function(){
						findFunc(false);
					});
					
					/*Add line number gotoHandler*/
					uiElements.gotoLineNo.keypress(function(event){
						var currentChar=String.fromCharCode(event.which);
						var currentVal=uiElements.gotoLineNo.val() + currentChar;
						uiElements.drlEditor.editor.gotoLine(1*currentVal);
					});
					
					/*Add font-size menu for ACE editor*/
					fontSizeMenu="<ul style='position:absolute;z-index:9999;'>" + 
					                "<li><a href='#'>8pt</a></li>" +
					                "<li><a href='#'>10pt</a></li>" +
					                "<li><a href='#'>12pt</a></li>" +
					                "<li><a href='#'>14pt</a></li>" +
					                "<li><a href='#'>16pt</a></li>" +
				                "</ul>";
					
					$fontSizeMenu=$(fontSizeMenu)
					    .menu()
		                .appendTo(m_utils.jQuerySelect("body"))
		                .position({
						        my: "left top",
						        at: "left bottom",
						        of: uiElements.fontDropdown
						      })
						.on( "menuselect", function( event, ui ) {
					    	var selectedSize=ui.item.text();
					        $fontSizeMenu.hide();
					        m_utils.jQuerySelect(uiElements.drlEditorTextArea).css("fontSize",selectedSize);
					        uiElements.selectedFontSize.text(selectedSize);
					        event.stopPropagation();
					      })
		                .hide();
					
					fontSizeMenuHandler=function(){
						$fontSizeMenu.show();
					};
					uiElements.fontDropdown.on("click",fontSizeMenuHandler);
					
					/*Add theme menu and handler for the ACE editor*/
					themeMenu="<ul style='position:absolute;z-index:9999;'>" + 
					                 "<li><a href='#'>chrome</a></li>" +
					                 "<li><a href='#'>cobalt</a></li>" +
					                 "<li><a href='#'>eclipse</a></li>" +
					                 "<li><a href='#'>idle_fingers</a></li>" +
					                 "<li><a href='#'>xcode</a></li>" +
					               "</ul>";
					
					$themeMenu=$(themeMenu)
						.menu()
						.position({
					        my: "left top",
					        at: "left bottom",
					        of: uiElements.themeDropdown
					      })
					     .appendTo(m_utils.jQuerySelect("body"))
					     .on( "menuselect", function( event, ui ) {
						    	var selectedTheme=ui.item.text();
						    	uiElements.drlEditor.editor.setTheme("ace/theme/" + selectedTheme);
						        $themeMenu.hide();
						        uiElements.drlSelectedTheme.text(selectedTheme);
						        event.stopPropagation();
						      })
					     .hide();

					themeMenuHandler=function(){
					       $themeMenu.show();
					    };
					uiElements.themeDropdown.on("click",themeMenuHandler);
					var view = this;
					
					/*Binding UIElement to incoming events from our top level command processor*/
					m_ruleSetCommandDispatcher.register(uiElements.nameInput,cnstCMD.ruleRenameCmd);
					uiElements.nameInput.on(cnstCMD.ruleRenameCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(techRule.uuid ===uuid && uiElements.nameInput.val()!=newVal){
							uiElements.nameInput.val(newVal);
						}
					});
					
					/*Binding UIElement to incoming events from our top level command processor*/
					$drlEditorTextArea=$(uiElements.drlEditorTextArea);
					m_ruleSetCommandDispatcher.register($drlEditorTextArea,cnstCMD.ruleScriptChangeCmd);
					$drlEditorTextArea.on(cnstCMD.ruleScriptChangeCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(techRule.uuid ===uuid && $drlEditorTextArea.val()!=newVal){
							uiElements.drlEditor.setValue(newVal);
						}
					});
					
					/*Binding UIElement to incoming events from our top level command processor*/
					m_ruleSetCommandDispatcher.register(uiElements.descriptionTextarea,cnstCMD.ruleDescriptionCmd);
					uiElements.descriptionTextarea.on(cnstCMD.ruleDescriptionCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(techRule.uuid ===uuid && uiElements.descriptionTextarea.val()!=newVal){
							uiElements.descriptionTextarea.val(newVal);
						}
					});
					
					/*bind our nameInput control to the actual name of the technical rule in our ruleset* */
					uiElements.nameInput.change({view : this}, function(event) {
						var oldName = techRule.name;
						techRule.name = uiElements.nameInput.val();
						ruleSet.state.isDirty=true;
						var cmd=m_ruleSetCommand.ruleRenameCmd(
								ruleSet,techRule,techRule.name,event);
						m_ruleSetCommandDispatcher.trigger(cmd);
					});
					
					/*bind our description textarea to the descritpion attribute on our technicalRule*/
					uiElements.descriptionTextarea.val(techRule.description);
					uiElements.descriptionTextarea.on("change",function(event){
						techRule.description=uiElements.descriptionTextarea.val();
						ruleSet.state.isDirty=true;
						var cmd=m_ruleSetCommand.ruleDescriptionCmd(
								ruleSet,techRule,techRule.description,event);
						m_ruleSetCommandDispatcher.trigger(cmd);
					});
					
					/*binding ruleset technical rule drl to change events on our drlEditor textarea*/
					uiElements.drlEditor.editor.on("blur",function(event){
						var tempVal=uiElements.drlEditor.getValue();
						ruleSet.state.isDirty=true;
						techRule.setDRL(tempVal);
						var cmd=m_ruleSetCommand.ruleScriptChangeCmd(
								ruleSet,techRule,tempVal,event);
						m_ruleSetCommandDispatcher.trigger(cmd);
					});
					
					uiElements.stringifyParamDefs.on("click",function(){
						var typeDrl=ruleSet.generateDRLTypes();
						var currentVal=uiElements.drlEditor.getValue();
						uiElements.drlEditor.setValue(typeDrl + "\n\n" + currentVal);
					});
					
					/*by convention: is this function neccesary?*/
					this.activate(ruleSet,techRule,uiElements);

				};

				this.activate = function(ruleSet,techRule,uiElements) {
					var drlText="";
					this.ruleSet = ruleSet;
					this.technicalRule=techRule;
					uiElements.uuidOutput.empty();
					uiElements.uuidOutput.append(this.technicalRule.uuid);
					uiElements.idOutput.empty();
					uiElements.idOutput.append(this.technicalRule.id);
					uiElements.nameInput.val(this.technicalRule.name);
					uiElements.drlEditor.setValue(this.technicalRule.getDRL());
				};
				
			}
		});