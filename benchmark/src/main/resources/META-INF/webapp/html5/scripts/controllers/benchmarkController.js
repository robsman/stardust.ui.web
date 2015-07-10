/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Zachary Z McCain
 */

(function() {
	
	'use strict';
	
	/**
	 * constructor, initialize our properties we will
	 * tie to our dependency injected services and expose
	 * them to our prototypes.
	 * @param benchmarkService - Service to interact with our REST layer.
	 */
	function benchmarkController(benchmarkService, benchmarkBuilderService, 
								 sdLoggedInUserService, $scope, $timeout, 
								 sdDialogService,$interval,sdI18nService,sdUtilService){
		
		
		var that = this,   //Self reference
			sdI18nModeler; //
		
		//Injected dependencies we need in our functions
		this.benchmarkService = benchmarkService;
		this.benchmarkBuilderService = benchmarkBuilderService;
		this.$timeout = $timeout;
		this.sdDialogService = sdDialogService;
		this.$scope = $scope;
		this.$interval = $interval;
		this.i18N = sdI18nService.getInstance('benchmark-messages').translate;
		this.sdUtilService = sdUtilService;
		
		//Function level properties
		this.textMap = {};
		this.benchmarkInitialStates = {}; //map of benchmarks pulled from server and stringified.
		this.selectedBenchmark = undefined; //Currently selected benchmark from our data table
		this.benchmarks = []; //Design mode Benchmarks pulled from the server
		this.publishedBenchmarks = []; //Published Benchmarks pulled from the server
		this.models = []; //models used to populate our model tree
		this.treeApi = {};//model tree api returned to our callback
		this.selectedTab = "General"; //Default tab
		this.dataTableApi = {}; //Api retrieved from our dataTable directive
		this.showBenchmarks = true; //should we show the benchmarks data table
		this.benchmarkDataRows = []; //Array of objects which populate our category-benchmark table
		this.currentUser = sdLoggedInUserService.getUserInfo(); //current user
		this.benchmarkFilter ="Design"; //Filter value for our benchmark service call.
		this.benchmarkIsDirty = false; 
		this.lastSaveTime = Number.NEGATIVE_INFINITY;
		this.calendars = []; //timeoff calendars

		//by default load our design time benchmarks
		this.loadBenchmarks("DESIGN");
		
		//initializing text map using our i18n method
		this.initTextMap();
		
		//initialize our attribute operands we will use in our condition dropdowns (lhs and rhs).
		this.attributeOperands = [];
		this.attributeOperands.push(
				this.operandBuilder("attribute","","CURRENT_TIME",this.textMap.currentTime));
		this.attributeOperands.push(
				this.operandBuilder("attribute","","PROCESS_START_TIME",this.textMap.processStartTime));
		this.attributeOperands.push(
				this.operandBuilder("attribute","","ROOT_PROCESS_START_TIME",this.textMap.rootProcessStartTime));
		
		//initialize our menu structure that we will pass back to our category table.
		//we do this in our constructor so that the object reference evaluates correctly
		//during an angular digest. If our menu retrieval function always generated a new
		//menu object then the angular digests would never stop until it hit the digest limit
		//as each invocation would be returning a new object. By returning the same object we
		//avoid this and the considerable performance issues which arise.
		this.categoryMenu = [  
              {'value':this.textMap.add, 'action': 'ADD_CAT'},
			  {'value':this.textMap["delete"], 'action': 'KILL_CAT'},
			  {'value':this.textMap.moveRight, 'action': 'MOVER_CAT'},
			  {'value':this.textMap.moveLeft, 'action': 'MOVEL_CAT'},
			  {'value':this.textMap.clone, 'action': 'CLONE_CAT'}
		];
		
		//Load our time-off calendars
		benchmarkService.getCalendars()
		.then(function(data){
			that.calendars = data.calendars;
		})
		["catch"](function(err){
			//TODO: handle error
		});
		
		//retrieve all deployed models, add additional data to help
		//in building a tree structure.
		benchmarkService.getModels()
		.then(function(res){
			res.models.forEach(function(model){
				model.data = model.data.filter(function(v){
					return v.typeId == 'struct' || v.typeId == 'primitive';
				});
			});
			that.models = that.treeifyModels(res.models);
		})
		["catch"](function(err){
			//TODO: handle error
		});
	}
	
	/**
	 * More of a subroutine than a function in that all this does is compartmentalize
	 * the initialization of our i18N textmap.
	 */
	benchmarkController.prototype.initTextMap = function(){
		
		this.textMap.benchmarkDefinitions = this.i18N("views.main.benchmarkDataTable.title");
		this.textMap.addBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.add");
		this.textMap.delelteBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.delete");
		this.textMap.cloneBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.clone");
		this.textMap.saveBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.save");
		this.textMap.publishBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.publish");
		this.textMap.downloadBenchmark = this.i18N("views.main.benchmarkDataTable.toolbar.download");
		this.textMap.name = this.i18N("views.main.benchmarkDataTable.colhdr.name");
		this.textMap.description = this.i18N("views.main.benchmarkDataTable.colhdr.description");
		this.textMap.modifiedBy = this.i18N("views.main.benchmarkDataTable.colhdr.modifiedBy");
		this.textMap.lastModified = this.i18N("views.main.benchmarkDataTable.colhdr.lastModified");
		this.textMap.general = this.i18N("views.main.tabs.general.title");
		this.textMap.categories = this.i18N("views.main.tabs.general.categories");
		this.textMap.businessCalendar = this.i18N("views.main.tabs.general.form.businessCalendar");
		this.textMap.published = this.i18N("views.main.mode.option.published");
		this.textMap.design = this.i18N("views.main.mode.option.design");
		this.textMap.element = this.i18N("views.main.categoryDataTable.colhdr.element");
		this.textMap.options = this.i18N("views.main.categoryDataTable.colhdr.options");
		this.textMap.add =  this.i18N("views.main.categoryDataTable.colhdr.menu.add");
		this.textMap["delete"] =  this.i18N("views.main.categoryDataTable.colhdr.menu.delete");
		this.textMap.clone = this.i18N("views.main.categoryDataTable.colhdr.menu.clone");
		this.textMap.moveRight = this.i18N("views.main.categoryDataTable.colhdr.menu.moveRight");
		this.textMap.moveLeft = this.i18N("views.main.categoryDataTable.colhdr.menu.moveLeft");
		this.textMap.enableBenchmark= this.i18N("views.main.categoryDataTable.cell.enableBenchmark");
		this.textMap.currentTime= this.i18N("views.main.categoryDataTable.cell.lhs.currentTime");
		this.textMap.freeForm= this.i18N("views.main.categoryDataTable.cell.lhs.freeForm");
		this.textMap.laterThan= this.i18N("views.main.categoryDataTable.cell.operator.laterThan");
		this.textMap.before= this.i18N("views.main.categoryDataTable.cell.operator.before");
		this.textMap.businessDays= this.i18N("views.main.categoryDataTable.cell.dateType.businessDays");
		this.textMap.calendarDays = this.i18N("views.main.categoryDataTable.cell.dateType.calendarDays");
		this.textMap.applyOffset= this.i18N("views.main.categoryDataTable.cell.applyOffset");
		this.textMap.daysAt= this.i18N("views.main.categoryDataTable.cell.daysAt");
		this.textMap.deleteDialogQuery = this.i18N("views.main.dialog.delete.query");
		this.textMap.deleteDialogStatement = this.i18N("views.main.dialog.delete.statement");
		this.textMap.deleteDialogContinue = this.i18N("views.main.dialog.delete.continue");
		this.textMap.publishDialogStatement = this.i18N("views.main.dialog.publish.statement")
		this.textMap.publishSuccessStatement = this.i18N("views.main.dialog.publishSuccess.statement");
		this.textMap.saveSuccessStatement = this.i18N("views.main.dialog.saveSuccess.statement");
		this.textMap.defaultName = this.i18N("views.main.benchmark.defaultName");
		this.textMap.dataExpression = this.i18N("views.main.categoryDataTable.cell.type.dataExpression");
		this.textMap.processStartTime = this.i18N("views.main.categoryDataTable.cell.lhs.processStartTime");
		this.textMap.rootProcessStartTime = this.i18N("views.main.categoryDataTable.cell.lhs.rootProcessStartTime");
		this.textMap.attribute = this.i18N("views.main.categoryDataTable.cell.attribute");
		this.textMap.data = this.i18N("views.main.categoryDataTable.cell.data");
	};
	
	/**
	 * Helper function to return lhs or rhs compatible objects for use in our condition
	 * dropdowns.
	 * @param type - (data|attribute)
	 * @param dataRef - forward slash delimited path to designate hierarchial data
	 * @param id - for data, the fully qualified id, for attributes - the defined constant
	 * @param name - transient, not saved to server, used for UI display only
	 * @returns {___anonymous8171_8240}
	 */
	benchmarkController.prototype.operandBuilder = function(type,dataRef,id,name,groupName){
		groupName = groupName || this.textMap.attribute;
		return {
			"id" : id,
			"type" : type,
			"deref" : dataRef,
			"name" : name,
			"groupName" : groupName
		}
	}
	
	/**
	 * Initiate a file download of the benchmark to the users computer.
	 * this will always retrieve the benchmark from the server so as to get
	 * the current saved state.
	 * @param benchmark - benchmark to download
	 * @param - Design or Publish mode
	 */
	benchmarkController.prototype.downloadBenchmarkAsFile = function(benchmark,mode){
		if(!benchmark){return;}
		this.benchmarkService.downloadBenchmarkAsFile(benchmark,mode);
	}
	
	/**
	 * The model tree supports mutliple selection mechanics and this is the function which 
	 * handles that function. Single select is default unless the ctrl key is pressed in
	 * which case we operate as a multiselect tree. 
	 * @param node
	 * @param e
	 * @param bmDataRow
	 */
	benchmarkController.prototype.handleTreeSelection = function(node,e,bmDataRow){
		console.log(e.ctrlKey);
		
		var bmDataRowIndex=-1,
			temp,
			i;
		
		//Find if the node is currently on our benchmarkDataRows array
		for(i=0;i<this.benchmarkDataRows.length;i++){
			temp= this.benchmarkDataRows[i].treeNodeRef;
			if(temp.nodeId === node.valueItem.nodeId){
				bmDataRowIndex=i;
				break;
			}
		}
		
		//Multi-Select Mode
	    if(e.ctrlKey){
	    	//if node is already selected then remove
	      if(bmDataRowIndex >= 0){this.benchmarkDataRows.splice(bmDataRowIndex,1);}
	      //else push in onto our collection
	      else{this.benchmarkDataRows.push(bmDataRow);}
	    }
	    
	    //Single Select Mode
	    else{
	      //clear all previously selected rows.
	      while(this.benchmarkDataRows.pop()){}
	      //and push our new row.
	      if(bmDataRowIndex===-1){this.benchmarkDataRows.push(bmDataRow);}
	    }
	}
	
	/**
	 * Callback for ng-change directives on our UI which need to mark a 
	 * benchmark as having been modified. Optional secondary parameter
	 * this should only be called from the design mode data table as it
	 * makes no sense to mark a published benchmark as dirty.
	 */
	benchmarkController.prototype.markBenchmarkDirty = function(benchmark,refresh){
		if(!benchmark) return;
		benchmark.isDirty = true;
		if(refresh===true){
			//this.dataTableApi.refresh(true);
			this.refreshDataTable(this.dataTableApi,true,benchmark.id);
		}
	}
	
	/**
	 * Given a benchmark and a process definition Id, this function will search the 
	 * benchmarks process definitions to determine if that process is present.
	 * If actId is present as a parameter the search will traverse down the 
	 * process defintions activities when a match is found. 
	 * 
	 *FYI: because the JSON structure is built out top down, applying
	 *a benchmark to an activity neccesitates adding a default benchmark 
	 *to its parent process definition. By default default behcmarks are not 
	 *enabled but it will still result in that node reporting a benchmark present.
	 */
	benchmarkController.prototype.isBenchmarked = function(bm,pdId,actId){
		
		if(!bm || !pdId){return false;}
		
		//Short circuit search leveraging array.some
		return bm.models.some(function(model){
			return model.processDefinitions.some(function(procDef){
				if(actId && procDef.id === pdId){
					return procDef.activities.some(function(act){
						return act.id === actId;
					});
				}
				else{return procDef.id === pdId;}
			});
		});
		
	}
	
	/**
	 * Look up a benchmark in our function level benchmarks collection and 
	 * return the value of its isDirty property. Returns false if benchmark not
	 * found or if the isDirty property is undefined (falsey in latter case).
	 * @param benchmark
	 * @returns {Boolean}
	 */
	benchmarkController.prototype.isBenchmarkDirty = function(id){
		var result = false,
			i,
			benchmarkArr = this.benchmarks,
			benchmark,
			temp="";
		
		if(this.benchmarkFilter==="Published"){
			benchmarkArr = this.publishedBenchmarks;
		}
		
		for(i=0;i<benchmarkArr.length;i++){
			if(benchmarkArr[i].content.id===id){
				benchmark = benchmarkArr[i].content;
				break;
			}
		}
		
		if(benchmark){result = benchmark.isDirty;}
		
		return result;

	}
	
	
	/**
	 * Save the benchmark to the document repository. Only applicable for
	 * design mode benchmarks.
	 * @param benchmark
	 */
	benchmarkController.prototype.saveBenchmark = function(benchmark){
		var clone_benchmark,
			that=this;
		
		//Create clean copy of our benchmark to save server-side, we have to
		//remove transient properties such as angular $$hashKey and our book-keeping
		//properties like 'key' and 'isDirty'.
		clone_benchmark = this.benchmarkBuilderService.cleanAndClone(benchmark);
		
		this.benchmarkService.saveBenchmarks(clone_benchmark)
		.then(function(data){
			//Now mark our original benchmark as clean
			benchmark.isDirty = false;
			//and inform the user of their success!
			that.saveSuccessDialog.open();
		})
		["catch"](function(err){
			//TODO: handle error
		});
	};
	
	
	/**
	 * Handles the client side cleanup of a benchmark following the successful
	 * server-side deletion of that benchmark.
	 */
	benchmarkController.prototype.deleteLocalBenchmark = function(id){
		var index = -1,
			that = this,
			temp,
			i;
		
		//now locate the id in our benchmarks collection
		for(i=0;i<this.benchmarks.length;i++){
			temp = this.benchmarks[i];
			if(temp.content.id==id){
				index = i;
				break;
			}
		}
		
		//If we found a match then remove it from the array
		if(index > -1){
			this.$timeout(function(){
				that.benchmarks.splice(index,1);
				//that.dataTableApi.refresh(true);
				that.refreshDataTable(that.dataTableApi,true);
			},0);
		}
	}
	
	/**
	 * Callback for publish confirmation dialog
	 * @param res
	 */
	benchmarkController.prototype.onOpenPublishDialog = function(res){
		var that = this,
			name,
			id;
		
		name = this.selectedBenchmark.name;
		id = this.selectedBenchmark.id;
		
		if(id){
			res.promise.then(function(){
				that.benchmarkService.publishBenchmark(id)
				.then(function(data){
					that.publishSuccessDialog.open();
				})
				["catch"](function(err){
					//TODO: handle error
				});
			});
		}
	}
	
	/**
	 * Callback for the sdDialog delete confirmation directive.
	 * On confirmation will delete the benchmark from both the server
	 * and client stores.
	 * @param res
	 */
	benchmarkController.prototype.onOpenDeleteDialog = function(res){
		var that = this,
			id;

		if(!this.selectedBenchmark){return;}
		
		id = this.selectedBenchmark.id;
		
		res.promise.then(function(){
			
			that.benchmarkService.deleteBenchmark(id)
			.then(function(data){
				that.deleteLocalBenchmark(id);
				that.selectedBenchmark = undefined;
			})
			["catch"](function(err){
				//TODO: handle error
			});
			
		});
	};
	
	/**
	 * GIven the id of a benchmark, make a clone of the benchmark,
	 * change the name to the default benchmark name, save it to the server
	 * and update our local UI.
	 * @param id
	 */
	benchmarkController.prototype.cloneBenchmark = function(id){
		var benchmark,
			that = this;
		
		//retrieve benchmark to clone
		benchmark = this.getLocalBenchmark(id);
		if(!benchmark){return;}//guard clause
		
		//Make a clean copy to save to the server
		benchmark = this.benchmarkBuilderService.cleanAndClone(benchmark.content);
		benchmark.id = this.benchmarkBuilderService.getUUID();
		benchmark.name = "Cloned Benchmark"; //TODO i18N

		//Save to server and update locally on success
		this.benchmarkService.createBenchmarkDefinition(benchmark)
		.then(function(data){
			that.addToBenchmarks(data);
		});
		
	};
	
	/**
	 * Given a benchmarks id, this function will look in our 
	 * local objects this.benchmarks array and return the corresponding 
	 * benchmark.
	 */
	benchmarkController.prototype.getLocalBenchmark = function(id){
		var index = -1,
			temp,
			result = false,
			i;
	
		//now locate the id in our benchmarks collection
		for(i=0;i<this.benchmarks.length;i++){
			temp = this.benchmarks[i];
			if(temp.content.id==id){
				index = i;
				result = temp;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Deletion of a benchmark from the remote
	 * server repository and local store. Wraps our
	 * deleteDialog API to open the dialog when a benchmark is selected.
	 * @param id - id of benchmark to delete
	 */
	benchmarkController.prototype.deleteBenchmark = function(id){
		if(this.selectedBenchmark){
			this.deleteDialog.open();
		}
		return;
	};
	
	/**
	 * Wrapper for our publishDialog confirmation API.
	 * See callback 'onOpenPublishDialog' for actual
	 * publish implementation.
	 * @param benchmark
	 */
	benchmarkController.prototype.publishBenchmark = function(id){
		this.publishDialog.open();
		return;
	};
	
	/**
	 * when the user changes from design mode to published mode, or vice versa
	 * we will load benchmarks using the following logic.
	 * If changing from Publish to design we will only attempt to load benchmarks
	 * if the design benchmark array is empty. When changing from design to publish
	 * we will always reload the published benchmarks collection.
	 * @param status
	 */
	benchmarkController.prototype.onStatusChange = function(status){
		
		//on any status change clear all benchmarkData rows
		while(this.benchmarkDataRows.pop()){}
		
		status = status.toUpperCase();
		status = !status ? "DESIGN" : status;
		
		if(status==="DESIGN" && this.benchmarks.length > 0){
			return;
		}
		
		this.loadBenchmarks(status);
	};
	
	/**
	 * Load benchmarks from our Service, passing a status of
	 * 'Design' or 'Published' to filter.
	 * @param status - string ['Design' | 'Publish']
	 */
	benchmarkController.prototype.loadBenchmarks = function(status){
		var that = this,
			activeApi,
			refreshAttempts = 0,
			activeBenchmarkArr;
		
		//Default to Design
		status = !status ? "DESIGN" : status;
		status = status.toUpperCase();
		
		//ensure we clear out our selected benchmark
		this.selectedBenchmark = undefined;
		
		//Choose our active array and API so we can operate
		//agnostically later
		if(status==="DESIGN"){
			activeBenchmarkArr = this.benchmarks;
		}
		else{
			activeBenchmarkArr = this.publishedBenchmarks;
		}
		
		//clear out all elements on our active benchmark array as we are about to reload them.
		while(activeBenchmarkArr.pop()){};
		
		//Retrieve all benchmarks
		this.benchmarkService.getBenchmarkDefinitions(status)
		.then(function(data){
			var promise; //our interval promise
			
			//sort benchmark definitons by last Modified
			data.benchmarkDefinitions.sort(function(a,b){
				if(a.metadata.lastModifiedDate < b.metadata.lastModifiedDate){
					return 1;
				}
				
				if(a.metadata.lastModifiedDate > b.metadata.lastModifiedDate){
					return -1;
				}
				
				return 0;
			});
			
			//load benchmarks into our active benchmark array (based on mode, publish or design).
			//Add book-keeping keys to each benchmark. This is only done
			//so that we can use the datatable api to select a row programatically.
			//keys must be cleaned before they are sent to the server.
			data.benchmarkDefinitions.forEach(function(bm){
				bm.key = bm.content.id;
				activeBenchmarkArr.push(bm);
			});
			
			//Issue with the dataApi actually being available from the 
			//dataTable on inital page load. To work around this we test
			//for the api on an interval of 125 milliseconds for a max of
			//24 attempts (3 seconds);
			promise = that.$interval(function(){
				activeApi = (status==="DESIGN")?that.dataTableApi:that.dataTableApiPublished;
				if(refreshAttempts >24){
					that.$interval.cancel(promise);
				}
				if(activeApi.refresh ){
					that.$interval.cancel(promise);
					//activeApi.refresh(true);
					that.refreshDataTable(activeApi,true);
				}
				refreshAttempts++;
			},125);

		})
		["catch"](function(err){
			//TODO: handle error
		});
	};
	
	/**
	 * Handle all call-backs from our sdDropDownMenu attached to the category
	 * headers in our benchmark data rows table. Also marks the target benchmark
	 * as dirty.
	 * @param v - value item from the directive
	 * @param e - original event 
	 */
	benchmarkController.prototype.categoryMenuCallback = function(v,e){
		
		var categories,
			benchmark = this.selectedBenchmark,//v.item.bm,
			index,
		    category;
		
		if(!benchmark){v.deferred.resolve();return;}
		
		categories = benchmark.categories;// v.item.bm.categories;
		category = v.scopeRef.$parent.category;// v.item.cat;
		index = categories.indexOf(category);
		
		if(v.menuEvent==="menuItem.clicked"){
			benchmark.isDirty = true;//v.item.bm.isDirty = true;
			switch (v.item.action){
				case "KILL_CAT":
					this.benchmarkBuilderService.removeCategory(benchmark,category);
					break;
				case "ADD_CAT":
					this.benchmarkBuilderService.addCategory(benchmark,category);
					break;
				case "CLONE_CAT":
					this.benchmarkBuilderService.cloneCategory(benchmark,category);
					break;
				case "MOVER_CAT":
					if(index < benchmark.categories.length-1){
						this.benchmarkBuilderService
						.moveCategory(benchmark.categories,index,index + 1);
					}
					break;
				case "MOVEL_CAT":
					if(index > 0){
						this.benchmarkBuilderService
						.moveCategory(benchmark.categories,index,index - 1);
					}
					break;
				default:
					break;
			}
			
	      v.deferred.resolve();
	    }
	}
	
	/**
	 * As our category priority is implicit based upon the ordinal position in the benchmarks
	 * top level category array, we need a way to specify position for the category data which
	 * is linked by id within our benchmarkData properties of our procDefs and  activities.
	 * This function will be utilized to provide priority based positioning to a sort function 
	 * for category instances (@see getSortedCategories).
	 * @param item - The category from our benchmarkData categories
	 * @param source - The top level Category array for the benchmark, position is implicit here based on ordianl position.
	 * @returns {Number} - index position the benchmark category data should have based on its link to the parent category.
	 */
	benchmarkController.prototype.getCategoryPosition = function(item){
		var i,
      		pos=-1,
      		temp,
      		source;
		
		if(!this.selectedBenchmark){
			return;
		}
		
		source = this.selectedBenchmark.categories;
	      
		for(i=0;i<source.length;i++){
		  temp = source[i];
		  if(temp.id === item.categoryId){
		    pos=i;
		    break;
		  }
		  return pos;
		}
	  
	};
	
	/**
	 * Given a category array from benchmarkData compare the elements in that array
	 * with the ordinal positions of the top level benchmark Categories and return 
	 * a new array sorted to match those ordianl positions.
	 * @param categoryData
	 * @returns
	 */
	benchmarkController.prototype.getSortedCategories = function(categoryData){

		return categoryData.sort(function(a,b){
			
			var posA,
		      posB;
		  
			posA = fxc(a,a1);
			posB = fxc(b,a1);
		  
			if(posA < posB){return -1;}
			else if(posA===posB){return 0;}
			else{return 1;}
			
		});
		
	};
	
	
	/**
	 * callback for the data table to handle when a benchmark has
	 * been selected.
	 * @param d
	 */
	benchmarkController.prototype.benchmarkSelected = function(d){
		var that = this,
			benchmarkArr = this.benchmarks,
			bm;
		
		if(this.benchmarkFilter==="Published"){
			benchmarkArr = this.publishedBenchmarks;
		}
		
		if(d.action==="select"){
			bm = benchmarkArr.filter(function(v){return v.content.id===d.current.content.id})[0];
			this.selectedBenchmark = bm.content ;
			//this.selectedBenchmark = d.current.content;
			console.log("Benchmark Selected");
			console.log(JSON.stringify(this.selectedBenchmark));
		}
		else if(d.action==="deselect"){
			this.selectedBenchmark = undefined;
		}
		this.benchmarkDataRows=[];
	}
	
	
	/**
	 * Given an id and a nodeType find the corresponding item within the currently selected
	 * benchmark and set it as our selected benchmark instance.
	 * @param idChain - Based on the nodeIds of our model data as transformed by the treeify function.
	 * 		  nodeType model    = modelId
	 * 		  nodeType process  = {modelId}processId
	 *        nodeType activity = {modelId}processId:activityId
	 * @param nodeType - meta data for a node in our tree [model|process|activity]
	 */
	benchmarkController.prototype.generateSelectedCategories = function(idChain, nodeType){
		console.log(id + ":" + nodeType);
		//based on the node type use the idChain to extract the correct benchmark instance
		//from our selected benchmark.
	};
	
	/**
	 * Callback function tied to our model tree through which we will
	 * receive the trees API.
	 * @param api - PI returned for our tree directive
	 */
	benchmarkController.prototype.onTreeInit = function(api){
		this.treeApi = api;
	}
	
	/**
	 * Callback function tied to our model tree through which we will
	 * mediate all of our tree events.
	 * @param d - the treeNode which was the original target of the event.
	 * @param e - event object from original DOM event
	 */
	benchmarkController.prototype.treeCallback = function(d,e){
		//based on the nodeType interrogate the currently selected benchmark (if any)
		//to determine any existing benchmarks, if not present the empty ui.
		var hasBenchmark = true,
			parentPd;
		
		//If we are in publish mode we do not wish to show any build-outs for items
		//that do not have a benchmark. Items that do have benchmarks will show as 
		//usual just with the relevant UI disabled.
		if(this.benchmarkFilter === "Published"){
			if(d.valueItem.nodeType === "process"){
				hasBenchmark = this.isBenchmarked(this.selectedBenchmark,d.valueItem.id);
			}
			else if(d.valueItem.nodeType==="activity"){
				parentPd = this.treeApi.getParentItem(d.nodeId);
				hasBenchmark = this.isBenchmarked(this.selectedBenchmark,parentPd.id,d.valueItem.id);
			}
		}
		
		if(d.treeEvent==="node-click" && this.selectedBenchmark && hasBenchmark){
			//now build out!
			this.buildOutBenchmark(this.selectedBenchmark,d,e);
		}
		
		d.deferred.resolve();
		console.log(d);
	}
	
	/**
	 * Given a benchmark JSON structure and nodePath, filter the benchmarks model
	 * array for the model instance identifies in the nodePath. If no model instance
	 * is found then push a new model instance onto the array using the specified id.
	 * @param benchMark
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutModel = function(benchmark,modelId,treeModel){
		var model,
			searchArray;
		
		searchArray = benchmark.models.filter(function(v){
			return (v.id === modelId );
		});
		
		if(searchArray.length ===0){
			model = this.benchmarkBuilderService.getBaseModel();
			model.id = treeModel.id;
			benchmark.models.push(model);
		}
		else{
			model = searchArray[0];
		}
		
		return model;
	}
	
	/**
	 * Given a model filter its processDefinitions array for the processDefinition
	 * present in the nodePath parameter. If found return that value, if not found
	 * use our builder service to create a new structure and add it to the models
	 * array.
	 * @param model
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutProcDef = function(model,procDefId,treeProcDef){
		var searchArray,
			categories,
			procDef;
		
		searchArray = model.processDefinitions.filter(function(v){
			return (v.id === procDefId);
		});
		
		if(searchArray.length === 0){
			categories = this.selectedBenchmark.categories;
			procDef = this.benchmarkBuilderService.getBaseProcessDefinition(categories);
			procDef.id = treeProcDef.id;
			model.processDefinitions.push(procDef);
		}
		else{
			procDef = searchArray[0];
		}
		
		return procDef;
	}
	
	/**
	 * Given a procDef filter its activities array for the activity
	 * present in the nodePath parameter. If found return that value, if not found
	 * use our builder service to create a new structure and add it to the models
	 * @param procDef
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutActivity = function(procDef,activityId,treeActivity){
		var searchArray,
			categories,
			activity;
		
		searchArray = procDef.activities.filter(function(v){
			return (v.id === activityId);
		});
		
		if(searchArray.length === 0){
			categories = this.selectedBenchmark.categories;
			activity = this.benchmarkBuilderService.getBaseActivity(categories);
			activity.id = treeActivity.id;
			procDef.activities.push(activity);
		}
		else{
			activity = searchArray[0];
		}
		
		return activity;
	}
	
	/**
	 * Helper function to link an array of categoryConditions back to their parent categories.
	 * THis is used to add a runtime property to the categoryCondition to allow reference to
	 * its parent category. Use case for this is to allow the parent category be referenced when in 
	 * the context of an angular orderBy function callback as the context of the callback does
	 * not allow us to access our controller. By adding a reference on the catCondition we work
	 * around this. This property will not be transmitted to the server.
	 */
	benchmarkController.prototype.linkCategoryConditions = function(categories,catConditions){
		var i,
			tempCat;
		
		catConditions.forEach(function(v){
			for(i = 0; i < categories.length; i++){
				tempCat = categories[i];
				if(v.categoryId === tempCat.id){
					v.categoryRef = tempCat;
				}
			}
		});
	}
	
	/**
	 * Handles the process of building out the JSON structure for a benchmark
	 * based on a users click of a model tree node (captured as the item param).
	 * @param benchmark -
	 * @param item - this should be the item corresponding to an sd-tree click event
	 */
	benchmarkController.prototype.buildOutBenchmark = function(benchmark,item,e){
		var searchArray,
			bmarkDataRow,
			rhsDefault,
			model,
			parentModel,
			conditions,
			parentProcDef,
			procDef,
			activity,
			i;
		
		//Model level clicks should only build out a new model element
		// in the parent benchmark's models array, no benchmarkData at
		//this level.
		if(item.valueItem.nodeType==="model"){
			this.buildOutModel(benchmark,item.valueItem.id,item.valueItem);
		}
		
		//TODO: These two blocks of code need to be consolidated
		
		//procDef clicks should build out model/procDef structures as
		//needed and enter a row into our benchmarkDataRows collection.
		else if(item.valueItem.nodeType==="process"){
			
			//build out structure
			parentModel = this.treeApi.getParentItem(item.valueItem.nodeId);
			model = this.buildOutModel(benchmark,parentModel.id,parentModel);
			procDef = this.buildOutProcDef(model,item.valueItem.id,item.valueItem);
			
			//add a linking reference from our category in the process definition (which is just an id)
			//to the actual object held in our benchmark.categories at the top level.
			this.linkCategoryConditions(benchmark.categories,procDef.categoryConditions);
			
			//build a collection of objects for the rhs of our condition. This will include
			//attributes and data.
			conditions = this.buildDataConditions(parentModel.data);
			conditions=conditions.concat(this.attributeOperands);
			
			//Now we need to find our default model data's qualifiedId so as
			//to initialize the condition.lhs value of new buildout with this value.
			rhsDefault = parentModel.data.filter(function(v){return v.id==="CURRENT_DATE"})[0];
			procDef.categoryConditions.forEach(function(c){
				if(c.details.condition.rhs.id===""){
					c.details.condition.rhs.id=rhsDefault.qualifiedId;
					c.details.condition.rhs.type="data";
				}
			});
			
			bmarkDataRow = {
					"benchmark" : benchmark, 
					"modelData" : conditions,//parentModel.data,
					"element" : "Process Definition",
					"elementRef" : procDef,
					"treeNodeRef" : item,
					"nodePath" : "{" + model.id + "}" + procDef.id, 
					"breadCrumbs" : [parentModel.name,item.valueItem.name],
					"categoryConditions": procDef.categoryConditions};
			
			this.handleTreeSelection(item,e,bmarkDataRow);
		}
		
		//Activity clicks should build out model/procDef/activity structures as
		//needed and enter a row into our benchmarkDataRows collection.
		else if(item.valueItem.nodeType==="activity"){
			
			parentProcDef = this.treeApi.getParentItem(item.valueItem.nodeId);
			parentModel = this.treeApi.getParentItem(parentProcDef.nodeId);
			
			model = this.buildOutModel(benchmark,parentModel.id,parentModel);
			procDef = this.buildOutProcDef(model,parentProcDef.id,parentProcDef);
			activity = this.buildOutActivity(procDef,item.valueItem.id,item.valueItem);
			
			this.linkCategoryConditions(benchmark.categories, activity.categoryConditions);
			
			//build a collection of objects for the rhs of our condition. This will include
			//attributes and data.
			conditions = this.buildDataConditions(parentModel.data);
			conditions=conditions.concat(this.attributeOperands);
			
			//Now we need to find our default model data's qualifiedId so as
			//to initialize the condition.lhs value of new buidlout with this value.
			rhsDefault = parentModel.data.filter(function(v){return v.id==="CURRENT_DATE"})[0];
			activity.categoryConditions.forEach(function(c){
				if(c.details.condition.rhs.id===""){
					c.details.condition.rhs.id=rhsDefault.qualifiedId;
					c.details.condition.rhs.type="data";
				}
			});
			
			bmarkDataRow ={
				"benchmark" : benchmark, 
				"modelData" : conditions,
				"element" : "Activity",
				"elementRef" : activity,
				"treeNodeRef" : item,
				"breadCrumbs" : [parentModel.name,parentProcDef.name,item.valueItem.name],
				"nodePath" : "{" + model.id + "}" + procDef.id + ":" + activity.id, 
				"categoryConditions": activity.categoryConditions};
			
			this.handleTreeSelection(item,e,bmarkDataRow);
			
		}
	}
	
	/**
	 * Convert a collection of 
	 * @param modelData
	 * @returns
	 */
	benchmarkController.prototype.buildDataConditions = function(modelData){
		var that = this;
		var results = modelData.map(function(v){
			return{
				//permanent properites that are mapped to ngModel
				"id" : v.qualifiedId,
				"type" : "data",
				"deref": "",
				//temporary properties required by the UI and 
				//stripped before we save to the server
				"name" : v.name,
				"dataType" : v.typeId,
				"groupName" : that.textMap.data
			}
		});
		
		return results;
	};
	
	benchmarkController.prototype.updateCondition = function(operand,item){
		operand.id = item.id;
		operand.type = item.type;
	}
	
	/**
	 * Callback function tied to our model tree through which we will
	 * provide the icon css classes for our tree nodes.
	 * @param d - the data object tied to a node in our tree.
	 * @param e - null
	 * @returns {String}
	 */
	benchmarkController.prototype.iconCallback = function(d,e){
		var iconCss = "", //classes we will apply to the tree node
			parentPd,     //Parent process definition
			isSelected=false,   //if the benchmark element is on our benchmarkDataRows collection
			hasBenchmark = false;
		
		
		//TODO-ZZM: need appropriate icons
		if(d.nodeType === "model"){
			iconCss = "sc sc-spiral";
			if(this.selectedBenchmark){
				hasBenchmark = this.selectedBenchmark.models.some(function(model){
					return model.id === d.id;
				});
			}
		}
		else if(d.nodeType === "process"){
			hasBenchmark = this.isBenchmarked(this.selectedBenchmark,d.id);
			isSelected = this.isNodeOnDataRows(d);
			iconCss = "sc sc-cog";
		}
		else{
			parentPd = this.treeApi.getParentItem(d.nodeId);
			hasBenchmark = this.isBenchmarked(this.selectedBenchmark,parentPd.id,d.id);
			isSelected = this.isNodeOnDataRows(d);
			iconCss = "sc sc-cogs";
		}
		
		if(hasBenchmark){
			iconCss += " has-benchmark"
		}
		
		if(isSelected){
			iconCss += " selected"
		}
		return iconCss;
	};
	
	/**
	 * Designed to test a treeNode to determine if the treeNode has 
	 * a corresponding member in the benchmarkDataRows array.
	 * @param d
	 * @returns
	 */
	benchmarkController.prototype.isNodeOnDataRows = function(d){
		return this.benchmarkDataRows.some(function(dataRow){
			return dataRow.treeNodeRef.nodeId===d.nodeId;
		});
	};
	
	/**
	 * Get the default benchmark JSON and set it as our selected benchmark.
	 * Update default values for author/name/last modified
	 */
	benchmarkController.prototype.createBenchmark = function(){
		
		var bmark,
			uniqueName,
			testNames,
			that;
		
		bmark = this.benchmarkBuilderService.getBaseBenchmark();
		that = this;
		
		//create a simple string array of current benchmark names
		testNames = this.benchmarks.map(function(bm){
			return bm.content.name;
		});
		//now pass the array and our default name to sdUtils to return a unique name within the
		//names array we just constructed.
		uniqueName = this.sdUtilService.generateUniqueName(testNames,this.textMap.defaultName);
		
		bmark.content.name = uniqueName;
		
		
		this.benchmarkService.createBenchmarkDefinition(bmark.content)
		.then(function(data){
			that.addToBenchmarks(data);
		});

	};
	
	/**Given the modelData on a benchmarkRow test whether the qualified ID matching that
	 * data represents a primitive data type. This is used to drive the visibility of 
	 * the dataReferenceDref input field.
	 * 
	 * @param modelData
	 * @param qualifiedId
	 * @returns
	 */
	benchmarkController.prototype.isPrimitive = function(modelData,qualifiedId){
		var result = false,
			temp,
			i;
		
		//this will evalaute before the user has selected a valid qualifiedID
		if(!modelData || !qualifiedId){return true;}
		
		for(i=0;i<modelData.length;i++){
			temp = modelData[i];
			if(temp.qualifiedId === qualifiedId && temp.typeId==="primitive" ){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Refresh the dataTable
	 * @param dataTableApi - Data Table API to operate against
	 * @param retainPageIndex - Whether or not we should retain the current page
	 * @param selectionKey - Key of the item to autoselect at the end of the refresh.
	 */
	benchmarkController.prototype.refreshDataTable = function(dataTableApi,retainPageIndex,selectionKey){
		if(!dataTableApi){return;}
		retainPageIndex = retainPageIndex || false;
		dataTableApi.refresh(retainPageIndex || false);
		if(selectionKey){
			this.$timeout(function(){
				dataTableApi.setSelection({'key':selectionKey});
			},0);
		}
	};
	
	/**
	 * adds a benchmark to our controllers benchmarks array. Checks first for
	 * name collisions and appends a numeral to the name in that event.
	 */
	benchmarkController.prototype.addToBenchmarks = function(bmark){
		var that = this;
		bmark.key = bmark.content.id; //add key so we can select with dataTable api
		this.benchmarks.unshift(bmark); //add to front of benchmarks
		this.refreshDataTable(this.dataTableApi,true,bmark.key);
	}
	
	/**
	 * Given the raw data from our getModels call (for example)
	 * add additional properties to help guide our tree structure.
	 * @param data - model data from our service call
	 * @returns data - modified model data such that each level now
	 * 		contains a nodeId and a nodeType.
	 */
	benchmarkController.prototype.treeifyModels = function(data){
		var i,j,k,tempModel,tempProcess,tempActivity;
		
		for(i = 0;i < data.length;i++){
			tempModel = data[i];
			tempModel.nodeType = "model";
			tempModel.nodeId = tempModel.id + "-" + tempModel.oid;
			for(j = 0; j < tempModel.processDefinitions.length;j++){
				tempProcess = tempModel.processDefinitions[j];
				tempProcess.nodeType = "process";
				tempProcess.nodeId = "{" + tempModel.nodeId + "}" + tempProcess.id;
				for(k = 0;k<tempProcess.activities.length;k++){
					tempActivity = tempProcess.activities[k];
					tempActivity.nodeType = "activity";
					tempActivity.nodeId = tempProcess.nodeId + ":" + tempActivity.id;
				}//activity loop end
			}//process loop end
		}//model loop end
		
		return data;
	};
	
	//angular dependencies
	benchmarkController.$inject = ["benchmarkService",
	                               "benchmarkBuilderService",
	                               "sdLoggedInUserService",
	                               "$scope", 
	                               "$timeout",
	                               "sdDialogService",
	                               "$interval",
	                               "sdI18nService",
	                               "sdUtilService"];
	
	//add controller to our app
	angular.module("benchmark-app")
	.controller("benchmarkCtrl",benchmarkController);
	

	
})();