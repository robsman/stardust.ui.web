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
	function benchmarkController(benchmarkService, benchmarkBuilderService, sdLoggedInUserService, $scope, $timeout){
		
		//Self reference
		var that = this; 
		
		//Injected dependencies we need in our functions
		this.benchmarkService = benchmarkService;
		this.benchmarkBuilderService = benchmarkBuilderService;
		this.$timeout = $timeout;
		
		//Function level properties
		this.benchmarkInitialStates = {}; //map of benchmarks pulled from server and stringified.
		this.selectedBenchmark = undefined; //Currently selected benchmark from our data table
		this.benchmarks = []; //Benchmarks pulled from the server
		this.benchmarks2 = []; //Benchmarks pulled from the server
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
		
		//Retrieve all benchmarks
		benchmarkService.getBenchmarkDefinitions()
		.then(function(data){
			
			that.benchmarks = data.benchmarkDefinitions;
			that.benchmarks.forEach(function(v){
				that.recordBenchmarkState(v.content);
			});
			$timeout(function(){
				that.dataTableApi.refresh();
			},0);
		})
		["catch"](function(err){
			//TODO: handle error
		});
		
		
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
	 * In order to support a dirtyState bit for each benchmark we will stringify the state 
	 * of the benchmark and rely on a string compare to determine if a benchmark has been
	 * modified. This function handles recording that state on our functions initial state
	 * collection.
	 * @param benchmark
	 */
	benchmarkController.prototype.recordBenchmarkState = function(benchmark){
		this.benchmarkInitialStates[benchmark.id]=JSON.stringify(benchmark);
	}
	
	
	/**
	 * Compare a benchmarks string representation against its state as recorded in the 
	 * benchmarkInitial state collection. If the benchmark is, for any reason, not in the
	 * initial states collection then we will return false.
	 * @param benchmark
	 * @returns {Boolean}
	 */
	benchmarkController.prototype.isBenchmarkDirty = function(id){
		var result = false,
			i,
			benchmark,
			temp="";
		
		for(i=0;i<this.benchmarks.length;i++){
			if(this.benchmarks[i].content.id===id){
				benchmark = this.benchmarks[i].content;
				break;
			}
		}
		
		if(benchmark && this.benchmarkInitialStates[benchmark.id] ){
			temp = JSON.stringify(benchmark);
			//console.log(JSON.stringify(benchmark));
			//console.log(this.benchmarkInitialStates[benchmark.id]);
			result = !(this.benchmarkInitialStates[benchmark.id] == temp);
		}
		return result;
	}
	
	/**
	 * Save the benchmark to the document repository
	 * @param benchmark
	 */
	benchmarkController.prototype.saveBenchmark = function(benchmark){
		var that = this;
		benchmark = this.benchmarkBuilderService.cleanAndClone(benchmark);
		
		this.benchmarkService.saveBenchmarks(benchmark)
		.then(function(data){
			//Update benchmark intial state
			that.recordBenchmarkState(benchmark);
			alert("saved");
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
		
		//Cleanup initial state cache
		if(this.benchmarkInitialStates.hasOwnProperty(id)){
			delete this.benchmarkInitialStates[id];
		}
		
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
	 * Deletion of a benchmark from the remote
	 * server repository.
	 * @param id - id of benchmark to delete
	 */
	benchmarkController.prototype.deleteBenchmark = function(id){
		var that = this;
		
		this.benchmarkService.deleteBenchmark(id)
		.then(function(data){
			that.deleteLocalBenchmark(id);
			alert("deleted");
		})
		["catch"](function(err){
			//TODO: handle error
		});
	};
	
	/**
	 * Publish the benchmark
	 * @param benchmark
	 */
	benchmarkController.prototype.publishBenchmark = function(id){
		debugger;
		this.benchmarkService.publishBenchmark(id)
		.then(function(data){
			//TODO: Dirty-bit implementation and clearing.
			alert("published");
		})
		["catch"](function(err){
			//TODO: handle error
		});
	};
	
	/**
	 * Load benchmarks from our Service, passing a status of
	 * 'Design' or 'Published' to filter.
	 * @param status - string ['Design' | 'Publish']
	 */
	benchmarkController.prototype.loadBenchmarks = function(status){
		var that = this;
		
		//Default to Design
		status = !status ? "Design" : status;
		
		//load models and reset UI
		this.benchmarkService.getBenchmarkStubs(status)
		.then(function(data){ 
			that.selectedBenchmark = undefined;
			that.benchmarks = data;
			that.dataTableApi.refresh();
		});
	};
	
	/**
	 * Handle all call-backs from our sdDropDownMenu attached to the category
	 * headers in our benchmark data rows table.
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
			bm;
		
		if(d.action==="select"){
			bm=this.benchmarks.filter(function(v){return v.content.id===d.current.content.id})[0];
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
		if(d.treeEvent==="node-click" && this.selectedBenchmark){
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
			
			//TODO: support multi-select in the tree
			while(this.benchmarkDataRows.pop()){}
			
			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"modelData" : parentModel.data.filter(function(v){
					return v.engineType==="Calendar" && v.typeId=="primitive"}),
				"element" : "Process Definition",
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
			
			//TODO: support multi-select in the tree
			while(this.benchmarkDataRows.pop()){}
			
			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"modelData" : parentModel.data.filter(function(v){
					return v.engineType==="Calendar" && v.typeId=="primitive"}),
				"element" : "Activity",
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
		var iconCss = "";
		
		//TODO-ZZM: need appropriate icons
		if(d.nodeType === "model"){
			iconCss = "sc sc-wrench";
		}
		else if(d.nodeType === "process"){
			iconCss = "sc sc-cog";
		}
		else{
			iconCss = "sc sc-cogs";
		}
		
		return iconCss;
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
	
	/**
	 * adds a benchmark to our controllers benchmarks array. Checks first for
	 * name collisions and appends a numeral to the name in that event.
	 */
	benchmarkController.prototype.addToBenchmarks = function(bmark){
		this.benchmarks.push(bmark);
		this.dataTableApi.refresh();
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
	                               "$timeout"];
	
	//add controller to our app
	angular.module("benchmark-app")
	.controller("benchmarkCtrl",benchmarkController);
	

	
})();