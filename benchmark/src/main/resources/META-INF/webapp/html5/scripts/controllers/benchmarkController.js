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
								 sdDialogService,$interval){
		
		//Self reference
		var that = this; 
		
		//Injected dependencies we need in our functions
		this.benchmarkService = benchmarkService;
		this.benchmarkBuilderService = benchmarkBuilderService;
		this.$timeout = $timeout;
		this.sdDialogService = sdDialogService;
		this.$scope = $scope;
		this.$interval = $interval;
		
		//Function level properties
		this.benchmarkInitialStates = {}; //map of benchmarks pulled from server and stringified.
		this.selectedBenchmark = undefined; //Currently selected benchmark from our data table
		this.benchmarks = []; //Design mode Benchmarks pulled from the server
		this.publishedBenchmarks = []; //Published Benchmarks pulled from the server
		this.models = []; //models used to populate our model tree
		this.treeApi = {};//model tree api returned to our callback
		this.selectedTab = "General"; //Default tab
		this.dataTableApi = {}; //Api retrieved from our dataTable directive
		this.showBenchmarks = true; //should we show the benchmarks data table
		this.benchmarkDataRows = []; //???
		this.currentUser = sdLoggedInUserService.getUserInfo(); //current user
		this.benchmarkFilter ="Design"; //Filter value for our benchmark service call.
		this.benchmarkIsDirty = false;
		this.lastSaveTime = Number.NEGATIVE_INFINITY;
		this.calendars = []; //timeoff calendars

		//TODO: wrap the following 3 calls up in a $q.all call
		
		this.loadBenchmarks("DESIGN");
		
		
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
		.then(function(data){
			that.models = that.treeifyModels(data.models);
		})
		["catch"](function(err){
			//TODO: handle error
		});
	}
	
	/**
	 * Callback for ng-change directives on our UI which need to mark a 
	 * benchmark as having been modified. Optional secondary parameter
	 * this should only be called from the desing mode data table
	 */
	benchmarkController.prototype.markBenchmarkDirty = function(benchmark,refresh){
		if(!benchmark) return;
		benchmark.isDirty = true;
		if(refresh===true){
			this.dataTableApi.refresh();
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
		
		//Short circuit search
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
				that.dataTableApi.refresh();
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
					//TODO: SUCCESS dialog
					that.deleteLocalBenchmark(id);
					that.publishSuccessDialog.open();
					that.selectedBenchmark = undefined;
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
					activeApi.refresh();
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
			benchmark = v.item.bm,
			index,
		    category;
		
		categories = v.item.bm.categories;
		category = v.item.cat;
		index = categories.indexOf(category);
		
		if(v.menuEvent==="menuItem.clicked"){
			v.item.bm.isDirty = true;
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
	
	benchmarkController.prototype.benchmarkSelected2 = function(bm){
		this.selectedBenchmark = bm.content;
		this.benchmarkDataRows=[];
	}
	
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
	 */
	benchmarkController.prototype.treeCallback = function(d){
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
			//TODO: support multi-select in the tree
			//Always clear out our category table rows.
			
			//while(this.benchmarkDataRows.pop()){}
			
			//now build out!
			this.buildOutBenchmark(this.selectedBenchmark,d);
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
	benchmarkController.prototype.buildOutBenchmark = function(benchmark,item){
		var searchArray,
			rhsDefault,
			model,
			parentModel,
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

			this.linkCategoryConditions(benchmark.categories,procDef.categoryConditions);
			
			//Now we need to find our default model data's qualifiedId so as
			//to initialize the condition.lhs value of new buidlout with this value.
			rhsDefault = parentModel.data.filter(function(v){return v.id==="CURRENT_DATE"})[0];
			procDef.categoryConditions.forEach(function(c){
				if(c.details.condition.rhs===""){
					c.details.condition.rhs=rhsDefault.qualifiedId;
				}
			});
			
			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"modelData" : parentModel.data,
				"element" : "Process Definition",
				"elementRef" : procDef,
				"treeNodeRef" : item,
				"dueDate" : procDef.dueDate,
				"nodePath" : "{" + model.id + "}" + procDef.id, 
				"breadCrumbs" : [parentModel.name,item.valueItem.name],
				"categoryConditions": procDef.categoryConditions});
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
			
			//Now we need to find our default model data's qualifiedId so as
			//to initialize the condition.lhs value of new buidlout with this value.
			rhsDefault = parentModel.data.filter(function(v){return v.id==="CURRENT_DATE"})[0];
			activity.categoryConditions.forEach(function(c){
				if(c.details.condition.rhs===""){
					c.details.condition.rhs=rhsDefault.qualifiedId;
				}
			});

			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"modelData" : parentModel.data,
				"element" : "Activity",
				"elementRef" : activity,
				"treeNodeRef" : item,
				"breadCrumbs" : [parentModel.name,parentProcDef.name,item.valueItem.name],
				"nodePath" : "{" + model.id + "}" + procDef.id + ":" + activity.id, 
				"categoryConditions": activity.categoryConditions});
		}
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
			iconCss = "sc sc-wrench";
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
			that;
		
		bmark = this.benchmarkBuilderService.getBaseBenchmark();
		that = this;
		
		//Default values
		//bmark.metadata.modifiedBy = this.currentUser.displayName;
		bmark.content.name = "Default Benchmark"; //TODO i18N
		//bmark.metadata.lastModified = (new Date()).toString();
		
		this.benchmarkService.createBenchmarkDefinition(bmark.content)
		.then(function(data){
			that.addToBenchmarks(data);
		});
		
		//this.addToBenchmarks(bmark);
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
	 * adds a benchmark to our controllers benchmarks array. Checks first for
	 * name collisions and appends a numeral to the name in that event.
	 */
	benchmarkController.prototype.addToBenchmarks = function(bmark){
		var that = this;
		bmark.key = bmark.content.id; //add key so we can select with dataTable api
		this.benchmarks.push(bmark);
		this.dataTableApi.refresh();
		this.$timeout(function(){
			that.dataTableApi.setSelection({key:bmark.key});
		},0);
		
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
			tempModel.nodeId = tempModel.id;
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
	                               "$interval"];
	
	//add controller to our app
	angular.module("benchmark-app")
	.controller("benchmarkCtrl",benchmarkController);
	

	
})();