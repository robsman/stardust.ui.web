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

/**
 * Service to handle our JSON structure for benchmarks. Provides the methods needed to
 * create and update our JSON clientside.
 */
(function(){
	
	'use strict';
	
	
	//Private: base structure of a benchmark
	var baseBenchmark =  
	{
		"metadata" : {
			"lastModified" : "",
			"modifiedBy" : ""
		},
		"content":
		{
		  "id": "",
		  "name": "Default Benchmark",
		  "description": "",
		  "categories": [],
		  "models": [],
		  "businessCalendar" : ""

		}
	};
	
	//models have no benchmark data
	var baseModel =
	{
		"id" : "",
		"processDefinitions" : []
	};
	
	//Activity with undefined benchmark data, builder should initialize as needed
	var baseProcessDefinition = 
	{
		"id": "",
		"categoryConditions" : [],
		"enableBenchmark": false,
		"expectedDuration" : "0.0",
		"activities": []
	};
	
	//Activity with undefined benchmark data, builder should initialize as needed
	var baseActivity = 
	{
		"id": "",
		"enableBenchmark": false,
		"expectedDuration" : "0.0",
		"categoryConditions" : []
	};
	
	//Top level category structure.
	var baseCategory = {
			"id" : "0000-0000-0000-0000",
			"name" : "Default Category",
			"color" : "#00FF00",
			"index" : 1
	}
	
	//Basic JSON structure of a Category
	//TODO: determine how categories will be generated considering ordinal ids???
	var baseCategoryData = 
	{
        "categoryId": "0000-0000-0000-0000",
        "type" : "freeform",
        "freeformExpression" : "",
        "details" : {
	        "condition": {
	          "lhs": { "id": "CURRENT_TIME", "type" : "attribute", "deref" : ""},
	          "operator": ">",
	          "rhs": { "id": "", "type" : "", "deref" : ""},
	          "offset": {
	        	  "applyOffset": false,
		          "useBusinessDays": true,
		          "amount": 10,
		          "unit": "d",
		          "time": "00:01"
		        }
	        }
         }
      };
	
	/**
	 * Constructor
	 */
	function benchmarkBuilderService(sdUtilService,sdI18nService){
		var i18N;
		
		i18N = sdI18nService.getInstance('benchmark-messages').translate;
		this.sdUtilService = sdUtilService;
		this.defaultCategoryName = i18N("views.main.category.defaultName");
	}
	
	
	/**
	 * TODO can this be removed???
	 * @param categories
	 * @returns
	 */
	benchmarkBuilderService.prototype.cloneCategories = function(categories){
		var clonedCats = {};
		angular.copy(categories, clonedCats);
		return clonedCats;
	};
	
	/**
	 * Return a RC4 compliant UUID
	 */
	benchmarkBuilderService.prototype.getUUID = function(){
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
	};
	
	/**
	 * Returns the base benchmark structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseBenchmark = function(){
		var baseCopy={};
		angular.copy(baseBenchmark, baseCopy);
		baseCopy.content.categories.push(this.getBaseCategory());
		baseCopy.content.id = this.getUUID();
		return baseCopy;
	}
	
	/**
	 * Cleanup our angular $$hashkeys and other transitory properties that
	 * were added to the benchmark by our controller and UI layer.
	 * @param benchmark
	 */
	benchmarkBuilderService.prototype.cleanAndClone = function(benchmark){
		var clone = {};
		var that = this;

		clone = angular.copy(benchmark);
		
		delete clone.isDirty;
		clone.categories.forEach(function(v,i){delete v.$$hashKey});
		
		//now loop over all models
		clone.models.forEach(function(model){
			delete model.$$hashKey;
			//then remove instanced data from all processes and definitions as well.
			model.processDefinitions.forEach(function(procDef){
				delete procDef.$$hashKey;
				//loop over all category conditions in our current process definition
				procDef.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
					delete catCond.$$hashKey;
					delete catCond.categoryRef;
					delete catCond.details.condition.lhs.name;
					delete catCond.details.condition.lhs.groupName;
					delete catCond.details.condition.rhs.name;
					delete catCond.details.condition.rhs.groupName;

					//convert date to benchmark time format "hh:mm"
					catCond.details.condition.offset.time = that.convertDateToBenchmarkTime(catCond.details.condition.offset.time );

				});
				
				procDef.activities.forEach(function(activity){
					delete activity.$$hashKey;
					activity.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
						delete catCond.$$hashKey;
						delete catCond.categoryRef;

						//convert date to benchmark time format "hh:mm"
						catCond.details.condition.offset.time = that.convertDateToBenchmarkTime(catCond.details.condition.offset.time );
					});
				});
				
			});//process definitions loop ends
		})//models loop ends
		
		return clone;
	};


	benchmarkBuilderService.prototype.convertDateToBenchmarkTime = function(v){
		if(!angular.isDate(v)){return "00:00";}

		var hours = v.getHours();
		var minutes = v.getMinutes();

		hours = (isNaN(hours))?0:hours;
		minutes = (isNaN(minutes))?0:minutes;
		
		hours = (hours < 10)? "0" + hours : "" + hours;
		minutes = (minutes < 10)? "0" + minutes : "" + minutes;
		return hours + ":" + minutes;
	};
	
	/**
	 * Returns the base Category structure of the top level benchmark.
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseCategory = function(){
		var baseCopy={};
		angular.copy(baseCategory, baseCopy);
		baseCopy.id = this.getUUID(); //apply a new UUID
		baseCopy.name  = this.defaultCategoryName; //apply our i18N default name
		return baseCopy;
	}
	
	/**
	 * Returns the base Model structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseModel = function(model){
		var baseCopy={};
		angular.copy(baseModel, baseCopy);
		return baseCopy;
	}
	
	/**
	 * Returns the base Process Definition structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseProcessDefinition = function(categories){
		var baseCopy={};
		angular.copy(baseProcessDefinition, baseCopy);
		
		//If not categories passed in then use a single default
		if(!categories){
			baseCopy.categoryConditions.push(this.getBaseCategoryData());
		}
		//else match the number of categories and link with categoryId; 
		else{
			this.populateCategories(baseCopy,categories);
		}
		
		return baseCopy;
	}
	
	/**
	 * Returns the base Activity structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseActivity = function(categories){
		var baseCopy={},
			tempCatBase,
			tempCatNew,
			i;
		
		angular.copy(baseActivity, baseCopy);
		
		//If not categories passed in then use a single default
		if(!categories){
			baseCopy.categoryConditions.push(this.getBaseCategoryData());
		}
		//else match the number of categories and link with categoryId; 
		else{
			this.populateCategories(baseCopy,categories);
		}
		
		return baseCopy;
	}
	
	/**
	 * Given an array of categories and an element containing a baseCopy element containing a categories
	 * array with N elements, populate the baseCopy's categories with N elements and seed with the 
	 * correct category Id.
	 * @param baseCopy
	 * @param categories
	 */
	benchmarkBuilderService.prototype.populateCategories = function(baseCopy,categories){
		var tempCatBase,
			tempCatNew,
			i;
		
		for(i=0;i<categories.length;i++){
			tempCatBase = categories[i];
			tempCatNew = this.getBaseCategoryData();
			tempCatNew.categoryId = tempCatBase.id;
			baseCopy.categoryConditions.push(tempCatNew);
		}
	}
	
	/**
	 * Returns the base Activity structure
	 * @returns
	 */
	benchmarkBuilderService.prototype.getBaseCategoryData = function(){
		var baseCopy={};
		angular.copy(baseCategoryData, baseCopy);
		return baseCopy;
	}
	
	
	/**
	 * internal helper function that iterates over the entire benchmark structure looking
	 * for categoryDetail instances which match the category.id. When it finds one it 
	 * executes the callback passing the array and the index the match was found in.
	 * @param benchmark
	 * @param category
	 * @param callback
	 */
	benchmarkBuilderService.prototype.__internalIterator = function(benchmark,category,callback){
		
		//var index = benchmark.categories.indexOf(category);
		//benchmark.categories.splice(index,1);

		//now loop over all models
		benchmark.models.forEach(function(model){
			
			//then remove instanced data from all processes and definitions as well.
			model.processDefinitions.forEach(function(procDef){
				
				//loop over all category conditions in our current process definition
				procDef.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
					if(catCond.categoryId===category.id){
						//catCondArray.splice(catIndex,1);
						callback(catCondArray,catIndex);
					}
				});
				
				procDef.activities.forEach(function(activity){
					//loop over all category conditions in our current activity
					activity.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
						if(catCond.categoryId===category.id){
							callback(catCondArray,catIndex);
						}
					});
				});
				
			});//process definitions loop ends
		})//models loop ends
	};
	
	/**
	 * Removes a category from the benchmarks top level category array as well as from 
	 * instances found in the benchmarks processes and definitions.
	 * @param benchmark - parent benchmark
	 * @param category - category to remove
	 */
	benchmarkBuilderService.prototype.removeCategory = function(benchmark,category){
		
		//First remove from the benchmarks top level Category collection
		var index = benchmark.categories.indexOf(category);
		
		benchmark.categories.splice(index,1);
		benchmark.categories.forEach(function(v,i){v.index=i+1;});
		
		//now loop over all models
		benchmark.models.forEach(function(model){
			
			//then remove instanced data from all processes and definitions as well.
			model.processDefinitions.forEach(function(procDef){
				
				//loop over all category conditions in our current process definition
				procDef.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
					if(catCond.categoryId===category.id){
						catCondArray.splice(catIndex,1);
					}
				});
				
				procDef.activities.forEach(function(activity){
					//loop over all category conditions in our current activity
					activity.categoryConditions.forEach(function(catCond,catIndex,catCondArray){
						if(catCond.categoryId===category.id){
							catCondArray.splice(catIndex,1);
						}
					});
				});
				
			});//process definitions loop ends
		})//models loop ends
		
		
	}
	
	/**
	 * Removes a category from the benchmarks top level category array as well as from 
	 * instances found in the benchmarks processes and definitions.
	 * @param categories - array of top level categories
	 * @param from - index of our target category we will move
	 * @param to - location we will move our target category to.
	 */
	benchmarkBuilderService.prototype.moveCategory = function(categories,from,to){
		//move target from->to
		categories.splice(to,0,categories.splice(from,1)[0]);
		
		//update indexes
		categories.forEach(function(v,i){v.index=i+1;});

	}
	
	/**
	 * Adds a category at a specified index to the benchmarks top level category array as 
	 * well as adding instances to the benchmarks processes and definitions. If doClone is set
	 * @param benchmark
	 * @param index
	 * @param doClone 
	 */
	benchmarkBuilderService.prototype.addCategory = function(benchmark,category){
		//first add to top level category collection
		var index,
			newCat,
			newCatData,
			names,
			that = this;
		
		index = benchmark.categories.indexOf(category);
		newCat = this.getBaseCategory();
		
		names = benchmark.categories.map(function(bm){return bm.name});
		newCat.name = this.sdUtilService.generateUniqueName(names,newCat.name);

		benchmark.categories.splice(index,0,newCat);
		
		//update indexes
		benchmark.categories.forEach(function(v,i){v.index=i+1;});
		
		//now loop over all models
		benchmark.models.forEach(function(model){
			
			//now loop over all process defintions 
			model.processDefinitions.forEach(function(procDef){
				
				newCatData = that.getBaseCategoryData();
				newCatData.categoryId = newCat.id;
				newCatData.categoryRef = newCat; //add transient reference property
				procDef.categoryConditions.push(newCatData);
				
				procDef.activities.forEach(function(activity){
					
					newCatData = that.getBaseCategoryData();
					newCatData.categoryId = newCat.id;
					newCatData.categoryRef = newCat; //add transient reference property
					activity.categoryConditions.push(newCatData);
				});
				
			});//process definitions loop ends
		})//models loop ends
	}
	
	/**
	 *First clones the top level category and performs an add but when adding to instances
	 *uses the sources instance data instead of the default data.
	 * @param benchmark
	 * @param index
	 */
	benchmarkBuilderService.prototype.cloneCategory = function(benchmark,category){
		
		//first add to top level category collection
		var index,
			newCat,
			newCatData,
			tempCat,
			names,
			that = this;
		
		names = benchmark.categories.map(function(cat){return cat.name});
		index = benchmark.categories.indexOf(category);
		newCat = this.getBaseCategory();
		newCat.name = this.sdUtilService.generateUniqueName(names,category.name);  //"Added BMark - "  + (new Date()).toTimeString().split("GMT")[0];
		benchmark.categories.splice(index,0,newCat);
		
		//update indexes
		benchmark.categories.forEach(function(v,i){v.index=i+1;});
		
		//now loop over all models
		benchmark.models.forEach(function(model){
			
			//now loop over all process defintions 
			model.processDefinitions.forEach(function(procDef){
				
				tempCat = procDef.categoryConditions
							.filter(function(v){return v.categoryId===category.id})[0];
				
				newCatData = angular.copy(tempCat);
				newCatData.categoryId = newCat.id;
				newCatData.categoryRef = newCat; //add transient reference property
				procDef.categoryConditions.push(newCatData);
				
				procDef.activities.forEach(function(activity){
					
					tempCat = activity.categoryConditions
								.filter(function(v){return v.categoryId===category.id})[0];
		
					newCatData = angular.copy(tempCat);
					newCatData.categoryId = newCat.id;
					newCatData.categoryRef = newCat; //add transient reference property
					activity.categoryConditions.push(newCatData);
				});
				
			});//process definitions loop ends
		})//models loop ends
	}
	
	//Angular dependency injection
	benchmarkBuilderService.$inject = ["sdUtilService","sdI18nService"];
	
	angular.module("benchmark-app.services")
	.service("benchmarkBuilderService",benchmarkBuilderService);
})();