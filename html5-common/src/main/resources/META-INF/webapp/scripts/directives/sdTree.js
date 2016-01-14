/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Subodh.Godbole (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Zachary.McCain
 */

(function(){

	  //============================================================================
	  //Extract module we will be adding our directives to.
	  //============================================================================
	  var mod = angular.module("bpm-common.directives");
	  
	  //============================================================================
	  //ngRepeat parsing code lifted from angular ngRepeat directive version 1.3.1
	  //Used to parse out our value identifier so we can return the tree item with
	  //our callback data. Also used for parsing the object path so we can extract
	  //the collection oject for reference during add or delete operations.
	  //============================================================================
	  var parseRepeatExpr = function(v){
	    
	      var expression =v;
	      var match = expression.match(/^\s*([\s\S]+?)\s+in\s+([\s\S]+?)(?:\s+as\s+([\s\S]+?))?(?:\s+track\s+by\s+([\s\S]+?))?\s*$/);
	      var result={};
	      
	      if (!match) {
	        return null;
	      }
	
	      result.lhs = match[1];
	      result.rhs = match[2];
	      result.aliasAs = match[3];
	      result.trackByExp = match[4];
	
	      match = result.lhs.match(/^(?:([\$\w]+)|\(([\$\w]+)\s*,\s*([\$\w]+)\))$/);
	
	      if (!match) {
	        throw ngRepeatMinErr('iidexp', "'_item_' in '_item_ in _collection_' should be an identifier or '(_key_, _value_)' expression, but got '{0}'.",
	             result.lhs);
	      }
	      result.valueIdentifier = match[3] || match[1];
	      result.keyIdentifier = match[2];
	      
	      return result;
	      
	  };
	  
	  
	  //============================================================================
	  // Root directive for a tree structure. All sdTreeNode directives are required
	  // To have this directive somewhere in its parent scope. Provides a common API
	  // to sdTreeNodes through which they can access callback functions provided
	  // by the user etc. 
	  //============================================================================
	  mod.directive("sdTree",function($timeout,$document){
	    
	    return{
	      scope:{
	        callback:"&sdaEventCallback",
	        treeInit: "&sdTree",
	        getMenuItems: "&sdaMenuCallback",
	        getIconClass: "&sdaIconCallback",
	        getDomAttachment: "&sdaDomCallback",
	        recurseFactory : "&sdaRecurseFactory"
	      },
	      controller: function(){
	        this.api={};
	      },
	      link:function(scope,elem,attrs,controller){
	        
	    	//Build an internal map of nodeIds to their actual element references.
		    //This will be incomplete for lazy loaded nodes
	        $timeout(function(){
	           controller.api.updateElementMap();
	        },0);  
	    	  
	        elem.addClass("sd-tree"); //Drives our child CSS structure
	        controller.treeRoot=scope; //Share our scope to descendant directives
	        controller.api.eventCallback=scope.callback; //user defined event cback
	        controller.api.iconCallback=scope.getIconClass;//user defined icon cback
	        controller.api.recurseFactory = scope.recurseFactory;
	        controller.api.id = scope.$id;//Share our unique id to descendants
	        controller.api.getMenuItems =scope.getMenuItems; //Retrieve menuItems
	        controller.childNodes ={}; //Each descendant sdTreeNode will map to here
	        controller.api.getDomAttachment = scope.getDomAttachment;//ref our dom attachement callback
	        
	        //======================================================================
	        //Builds a map of each nodeId hashed to a jquery element and an 
	        //angular scope. This function is called after the initial sdTree 
	        //compilation, but thereafter must be invoked manaully in order to
	        //keep the element map in synch with nodes added or removed after 
	        //inital tree compilation.
	        //======================================================================
	        controller.api.updateElementMap = function(){
	        	
	           var objScope,
                  elements,
                  tempElem;
	           
	           
	           
	           elements = $('[sda-node-id]',elem);
	           controller.elementalMap = {};
	           for(var i = 0 ; i < elements.length; i++){
	        	   tempElem = elements[i];
	        	   objScope = angular.element(tempElem).scope();
	        	   controller.elementalMap[objScope.nodeId] = {
	        			   "scope" : objScope, 
	        			   "elem" : $(tempElem)
	        	   };
	           }
	        };
	        
	        //======================================================================
	        //Reset the tree removing all match and hide classes that were added 
	        //by the filterTree function. Does not collapse nodes or update the 
	        //underlying elementalMap map.
	        //======================================================================
	        controller.api.resetFilter = function(){
	        	for(var key in controller.elementalMap){
	        		tempObj = controller.elementalMap[key];
	        		tempObj.elem.removeClass("match");
	        		tempObj.elem.removeClass("hide");
	        	}  
	        };
	        
	        //======================================================================
	        //Filters the tree, hiding and collapsing nodes which do not return true
	        //from the supplied comparator function. For nodes that return true then
	        //a match class is added.
	        //Param: comparatorFX - comparator function supplied by the caller, the 
	        //nodeItem is passed into the invocation and boolean True/False should
	        //be returned. True values are considered matches.
	        //Returns: array of matches.
	        //======================================================================
	        controller.api.filterTree = function(comparatorFx,forceUpdate){
	        	
	            var matches = [], //collection of matched nodes based on filter
	                childRecurse,
	                tempObj;
	            
	            //By default we do not force updates of the element map
	            forceUpdate = (forceUpdate===undefined)?false:forceUpdate;
	            
	            //ensure that we have an up-to-date map of the tree
	            if(forceUpdate===true){
	            	controller.api.updateElementMap();
	            }
	            
	            if(!angular.isFunction(comparatorFx)){
	              controller.api.resetFilter();
	              return;
	            }
	                
	            //Recursive funtion to show or hide all scopes beneath current scope
	            childRecurse = function(cs,isVisible){
	               for(; cs; cs= cs.$$nextSibling) {
	                      cs.isVisible=isVisible;
	                      childRecurse(cs.$$childHead);
	                }
	            };
	            
	            //step 1: reset entire tree, but look for matches along the way
	            for(var key in controller.elementalMap){
	                tempObj = controller.elementalMap[key];
	                tempObj.scope.isVisible=false;
	                tempObj.elem.removeClass("match");
	                tempObj.elem.removeClass("hide");
	                if(comparatorFx(tempObj.scope.nodeItem)===true){
	                  matches.push(tempObj);
	                }
	            }
	            
	            //step 2 ,iterate over matches expanding to the root
	            matches.forEach(function(v){
	              v.scope.isVisible = true;
	              v.elem.addClass("match");
	              childRecurse(v.scope.$$childHead,true);
	              v = v.scope.$parent;
	              while(v){
	                if( v.hasOwnProperty("isVisible")){v.isVisible=true;}
	                 v = v.$parent;
	              }
	            });
	            
	            //now hide all non visible nodes
	            for(key in controller.elementalMap){
	              tempObj = controller.elementalMap[key];
	              if(tempObj.scope.isVisible===false){
	                tempObj.elem.addClass("hide");
	              }
	            }
	
	            //collapse children
	            for(var i = matches.length-1;i>=0;i--){
	                childRecurse(matches[i].scope.$$childHead,false);
	            }
	            
	            //One final cleanup pass to take care of any children
	            //of matches that were matches themselves which were
	            //closed in the collapse children step
	            for(i = matches.length-1;i>=0;i--){
	                matches[i].scope.isVisible=true;
	            }
	            
	            //return array of matched nodeItems to invoker
	            return matches.map(function(tempObj){
	            	return tempObj.scope.nodeItem;
	            });

	        };
	        
	        
	        //======================================================================
	        //Empty all elements from the controllers childNodes collection.
	        //This assumes whoever invokes this is going to do an analagous action
	        //with the data from which the tree was instantiated. If not then expect
	        //really really bad results.
	        //Use case: Tree needs to be refreshed with new data so we need to make sure
	        //we purge the old hash map.
	        //======================================================================
	        controller.api.purgeChildNodes = function(){
	          while (controller.childNodes.length>0){
	        	  controller.childNodes.pop();
	          }
	        };
	        
	        //======================================================================
	        //Retrieve an item from the controller hashMap based on nodeId.
	        //Param- nodeId, Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.getItem = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId];
	          return nodeScope[nodeScope.repeater.lhs];
	        };
	        
	        //======================================================================
	        //Retrieve the nodeID path based on nodeId.
	        //Param- nodeId, Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.getPath = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId];
	          return nodeScope.path;
	        };
	        
	        //======================================================================
	        //Retrieve the nodeID of the parent Node relative to the nodeId param.
	        //Param- nodeId- Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.getParentNodeId = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId],
	              arrPath = nodeScope.path.split(","),
	              parentPath = (arrPath.length >1)?arrPath[arrPath.length-2]:arrPath[0];
	          
	          return parentPath;
	        };
	        
	        //======================================================================
	        // Retrieve the nodeItem on the parent scope of the node specified by 
	        // the nodeId param
	        //Param- nodeId- Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.getParentItem = function(nodeId){
	        	var nodeScope= controller.childNodes[nodeId];
	        	return nodeScope.$parent.nodeItem;
	        }
	        
	        //======================================================================
	        // Retrieve the parent nodeItem on the parent scope of the node specified by 
	        // the nodeId param
	        //Param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.getRootItem = function(nodeId){
	        	var nodeScope= controller.childNodes[nodeId],
        			arrPath =nodeScope.path.split(",");
	        	
	        	return controller.childNodes[arrPath[0]].nodeItem;
	        }
	        //======================================================================
	        // Whichever state the node is in (expand||collapse), do the opposite
	        //Param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.toggleNode = function(nodeId){
	        	$timeout(function(){
	        		var nodeScope= controller.childNodes[nodeId];
  	          		nodeScope.isVisible = !nodeScope.isVisible;
	        	},0);
	          
	        };
	        
	        //======================================================================
	        //Expand a node and that nodes parents
	        //Param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //Will expand parent nodes as well 
	        //======================================================================
	        controller.api.expandNode = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId],
	              parentNodes,
	              i;
	              
	          parentNodes = nodeScope.path.split(",");
	          
	          for(i=0;i<parentNodes.length-2;i++){
	            controller.api.expandNode(parentNodes[i]);
	          }
	          
	          if(!nodeScope.isLeaf){
	        	  nodeScope.isVisible = true;
	          }
	        };
	        
	        //======================================================================
	        //Collapse a node
	        //Param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.collapseNode = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId];
	          nodeScope.isVisible = false;
	        };
	        
	        //======================================================================
	        //Set a node to edit mode, allowing user to rename that node.
	        //Param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //======================================================================
	        controller.api.allowEdit = function(nodeId){
	          var nodeScope= controller.childNodes[nodeId];
	          nodeScope.allowEdit = true;
	        };
	        
	        //======================================================================
	        //Set the class on the node icon element of the tree node specified by
	        //the nodeId parameter.
	        //param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //param- class: css class name to apply to the nodeID parameter.
	        //======================================================================
	        controller.api.setNodeIcon = function(nodeId,className){
	          var nodeScope= controller.childNodes[nodeId];
	          nodeScope.iconClass = className;
	        };
	        
	        //======================================================================
	        //Add a child to a given node by explicitly passsing the name of the child
	        //collection of the node we will add the child to. Removes any ambiguity
	        //in instances where parse expressions fail due to lack of coverage (filters)
	        //for example. If childCollKey resolves to an array we will push onto that
	        //array, else we take the invoker in good faith and add the item as the keyed
	        //map to the nodeItem. Functionally this allows the user to actually modify
	        //their nodeItem directly through the API.
	        //param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //param- item: item to add to the nodes child collection
	        //param- childCollKey: value of the key mapped to the child collection
	        //		  which we will add the item to.
	        //======================================================================
	        controller.api.addChildByKey = function(nodeId,item,childCollKey){
	        	
	        	var node = controller.childNodes[nodeId];
	        	
	        	$timeout(function(){

		        	if(angular.isArray(node.nodeItem[childCollKey])){
		        		node.nodeItem[childCollKey].push(item);
		        		
		        		node.isLeaf=false;
			        	node.isVisible=true;
		        	}
		        	else{
		        		node.nodeItem[childCollKey]=item;
		        	}
		        	
	        	},0);
	        	
	        }
	        
	        //======================================================================
	        //Add a child to a given node by explicitly passsing the name of the child
	        //collection of the node we will add the child to. Removes any ambiguity
	        //in instances where parse expressions fail due to lack of coverage (filters)
	        //for example.
	        //param- nodeId: Id as specified through the sdTreeNode sdaNodeId attr
	        //param- item: item to add to the nodes child collection
	        //param- childCollKey: value of the key mapped to the child collection
	        //		  which we will add the item to.
	        //param-cmpfx- comparator function to compare each node in the child collection
	        //		        with the node passed in by the caller.
	        //======================================================================
	        controller.api.removeChildByKey = function(nodeId,item,childCollKey,cmpfx){
	        	
	        	var node = controller.childNodes[nodeId],
	        		that=this;
	        	
	        	node = node.$parent;//now convert to parent node 
	        	$timeout(function(){
	        		if(angular.isArray(node.nodeItem[childCollKey])){
			        	node.nodeItem[childCollKey]
			        	.forEach(function(v,i,a){
			        		if(cmpfx(item,v)){a.splice(i,1);}
			        	});
			        	if(node.nodeItem[childCollKey].length===0){
			        		node.isLeaf=true;
				        	node.isVisible=false;
			        	}
	        		}
	        	});
	        	
	        }
	        
	        //======================================================================
	        //Add an item to the tree.
	        //Param- nodeId: Id of the node we will add an item to
	        //Parma- item: item to add. Can be a single item or an array of items
	        //TODO- allow adding to hashMaps, only works for arrays right now
	        //WARNING- Only works when parent shared repeater expression with child
	        //         If not then use addChildByKey
	        //======================================================================
	        controller.api.addItem = function(nodeId,item){
	          
	          var node = controller.childNodes[nodeId],
	              collObj=node,
	              dataPath,
	              i;
	          	  
	          	  dataPath=node.repeater.rhs.split("|")[0].trim().split('.');
	              //dataPath=node.repeater.rhs.split("."),
	          
	          //Drill down through our object until we find our collection object    
	          for(var i = 0;i<dataPath.length;i++){
	            collObj= collObj[dataPath[i]];
	          }
	          
	          //Test if our collObj is an array or hashmap
	          if(angular.isArray(collObj)){
	            if(angular.isArray(item)){
	              for(i=0;i < item.length;i++){
	                collObj.push(item[i]);
	              }
	            }
	            else{
	              collObj.push(item);
	            }
	          }
	          else{
	            //TODO: implement hashmap
	            //Problem: what is the property to hash?????
	          }
	        };
	        
	        //======================================================================
	        //Given a nodeID containing a collection, remove the item matching
	        //the childHKey from that collection.
	        //param- parentNodeId: nodeId, as specified by attribute of the node
	        //        containing the collection we will delete from.
	        //param- childHKey: $$hashkey value, as computed by Angular, of the 
	        //        item we will be removing
	        //======================================================================
	        controller.api.removeItem = function(parentNodeId,childHkey){
	          var parentNode, //Node containing the collectio nwe will delete from.
	              childNode,  //child node we will delete
	              collObj,    //Collection object to delete from
	              objPath,    //RHS of repeater we will parse for colelction name
	              arr,        //
	              parentNodeHkey,
	              protoRepeater,
	              key;
	
	          //Pull parentNode from our hashMap
	          parentNode = controller.childNodes[parentNodeId];
	
	          if(parentNode){
	            
	            //Test for case where we have a node with no parent sdTreenode, this
	            //effectively means we have a root node and cannot expect to find
	            //a parent node registered as we are the parent node. We will rely
	            //on our ngRepeat expression and protoytipcal inheritance to operate
	            //on the correct collection.
	            parentNodeHkey = parentNode[parentNode.repeater.lhs].$$hashKey;
	            if(parentNodeHkey==childHkey){
	              protoRepeater=parseRepeatExpr(parentNode.attrs.ngRepeat);
	              objPath = protoRepeater.rhs;
	            }
	            //Otherwise we have a treeNode with the data to be removed in a 
	            //collection on a parent treeNode. Examine the parent tree nodes
	            //repeater to determine which collection to operate on.
	            else{
	              objPath = parentNode.repeater.rhs;
	            }
	            
	            //init our collection object as the parentNode as start drilling 
	            //towards our collection object.
	            collObj=parentNode;
	            
	            arr=objPath.split(".");
	            for(var i = 0;i<arr.length;i++){
	              collObj= collObj[arr[i]];
	            }
	           
	            //If collection is array, find and nuke
	            if(angular.isArray(collObj)){
	              for(i = 0;i<collObj.length;i++){
	                childNode=collObj[i];
	                if(childNode.$$hashKey == childHkey){
	                  collObj.splice(i,1);
	                }
	              }
	            }
	            //Else we have a hashMap so find and nuke.
	            else if(angular.isObject(collObj)){
	              for(key in collObj){
	                if(collObj[key].$$hashKey==childHkey){
	                  delete collObj[key];
	                }
	              }
	            }
	            return true;
	          }
	          else{
	            return false;
	          }
	        };//Remove Item function end
	        
	        //Build a public API to expose tree functionality to users.
	        var publicApi = {
	          
	          getItem     : controller.api.getItem,
	          getParentItem : controller.api.getParentItem,
	          getRootItem : controller.api.getRootItem,
	          removeItem  : controller.api.removeItem,
	          addItem     : controller.api.addItem,
	          addChildByKey : controller.api.addChildByKey,
	          expandNode  : controller.api.expandNode,
	          collapseNode: controller.api.collapseNode,
	          treeId      : controller.api.id,
	          childNodes  : controller.childNodes,
	          getPath     : controller.api.getPath,
	          getParentNodeId : controller.api.getParentNodeId,
	          setNodeIcon : controller.api.setNodeIcon,
	          removeChildByKey : controller.api.removeChildByKey,
	          purgeChildNodes : controller.api.purgeChildNodes,
	          filterTree  : controller.api.filterTree,
	          resetFilter   : controller.api.resetFilter
	        };
	        
	        scope.treeInit({api:publicApi});
	      }
	    };
	  });
	
	  
	  //============================================================================
	  // TreeNode Directive TODO:comment
	  //============================================================================
	  mod.directive("sdTreeNode",function($parse,$q,$timeout,$compile){
	    
	    return{
	      require:["sdTreeNode","^?sdTree","^?sdTreeNode"],
	      restrict: "A",
	      template: function(elem,attrs){
	
	        var build,
	        	dragdropStr="",
	        	lazyCompileString = "ng-show='isVisible'",
	            menuStr="";
	            
	        if(attrs.sdaMenuItems){
	            menuStr = 'sd-simple-menu="' +  attrs.sdaMenuItems + '" ';
	            menuStr += 'sda-menu-callback="menuCallback(data,e)" ';
	        }
	        
	        //based on the values passed as attributes for sda-draggable and sda-droppable,
	        //place our sd-data-drop and drag directives on our nodes.
	        attrs.sdaDraggable = attrs.sdaDraggable || 'false';
	        attrs.sdaDroppable = attrs.sdaDroppable || 'false';
	        
	        if(attrs.sdaDraggable==='true'){
	        	dragdropStr =" sd-data-drag sda-dragend='dragend($data,$event)' ";
	        }
	        
	        if(attrs.sdaDroppable==='true'){
	        	dragdropStr +=" sd-data-drop sda-drop='drop($data,$event)' ";
	        }
	        
	        if(attrs.sdaDroppableExpr){
	        	dragdropStr +="DROP_EXPR_TARGET";
	        };
	        
	        if(attrs.sdaLazyCompile && attrs.sdaLazyCompile.toUpperCase()==='TRUE'){
	        	lazyCompileString = "ng-if='isVisible'";
	        }
	        
	        //TODO: Factor out as many watches as you can from the template.
	        //TODO: isLeaf needs to work.
	        //TODO: data-hashKey has two watches on it
	        build=[
	          '<div data-hashKey="{{this[repeater.lhs].$$hashKey +\'-\'+  rootCtrl.api.id}}" ',
	          'ng-init="isVisible=false" class="title-section">',
	          '<div class="expand-section">',
	          '<i ng-click="invokeCallback(\'node-expand\',$event)" ',
	          'ng-class="{\'pi-arrow-r\':!isVisible && !isLeaf,',
	          '\'pi-arrow-d\':isVisible }" class="pi"></i>',
	          '</div>',
	          '<a href="" ' + dragdropStr + ' title="{{' + attrs.sdaTitle + '}}" ng-model="nodeItem"  ng-click="invokeCallback(\'node-click\',$event)"  class="tree-node">',
	          '<i ng-class="getIconClass() + \' \' + iconClass" class="js-icon pi pi-badge">' +
	          	'<i class="pi pi-badge-bg"></i>' +
	          	'<i class="pi pi-badge-icon"></i>' +
	          '</i>',
	          '<span ' + menuStr + ' ng-show="!allowEdit">',
	              "{{" + attrs.sdaLabel + "}}",
	          "</span>",
	          '<input ng-keypress="keyMonitor($event)" ng-blur="invokeCallback(\'node-rename-commit\',$event,this)" ',
	          'ng-show="allowEdit" value="{{' + attrs.sdaLabel +'}}" type="text"/>',
	          "</a><attach/></div>",
	          "<div " + lazyCompileString +  " >",
	           elem.html(),
	          "</div>"
	        ];
	        return build.join('');
	      },
	      
	      //Set up controller data we need to communicate to our recurse directive
	      controller: function($attrs,$scope,$element){
	        this.repeatExpression =  $attrs.sdaRecurseExpr || $attrs.ngRepeat;
	        this.nodeIdExpression =$attrs.sdaNodeId;
	        this.leafExpression =$attrs.sdaIsLeaf;
	        this.dropExpression = $attrs.sdaDroppable;
	        this.menuItems = $attrs.sdaMenuItems;
	      },
	      
	      compile: function(elem,attrs){
	        
	        var template = elem.html();
	        return {
	          
	          //Set template in prelink so it is available 
	          //lower down our  our scope chain. Remember that post evaluates 
	          //bottom up so if we set it there then it WON'T be available to scopes
	          //below us. We must set it in PRE!
	          pre: function(scope, iterStartElement, attrs, controllers){
	            var localCtrl=controllers[0];
	            var dropTargetElem;
	            var compDTElem;
	            localCtrl.template = template;
	            
	            //Modify iterStartElement by parsing new attribute sda-droppable-expr
	            //then do what the template function does when it finds sda-droppable = true;
	            if(attrs.sdaDroppableExpr){
	            	if($parse(attrs.sdaDroppableExpr)(scope)===true){
	            		
	            		//Manipulate DOM to add needed elements.
	            		dropTargetElem  = iterStartElement[0].querySelector("a[DROP_EXPR_TARGET]");
	            		dropTargetElem.removeAttribute("DROP_EXPR_TARGET");
	            		dropTargetElem.setAttribute("sd-data-drop","");
	            		dropTargetElem.setAttribute("sda-drop","drop($data,$event)");
	            		
	            		//This is actually the second compilation of this element meaning we have now
	            		//duplicated our ng-click handler but as our invoke scope method leverages
	            		//stopImmediatePropagation for events this should not be an issue.
	            		compDTElem = $compile(dropTargetElem)(scope);

	            	}
	            }
	            
	          },
	          
	          post: function(scope,elem,attrs,controllers){
	            
	            //Alias our controllers
	            var rootCtrl = controllers[1], 
	                localCtrl = controllers[0], 
	                parentTreeNodeCtrl = controllers[2],
	                nodeItem,//reference our tree item
	                dragElem,//draggable DOM element in our sdTree template
	                dropElem,//droppable DOM element in our sdTree template
	                domCallBackObj,
	                domCallBackPromise,
	                attachElem,
	                templateElem;
	                
	            
	            //Parse the leaf expression against our scope
	            var isLeaf = $parse(localCtrl.leafExpression)(scope);
	            scope.isLeaf = !!isLeaf;//allow truthiness for objects etc

	            //Parse out repeat expression
	            scope.repeater=parseRepeatExpr(localCtrl.repeatExpression);
	            
	            //extract nodeItem 
	            scope.nodeItem=scope[scope.repeater.lhs];
             
	            //Now we will invoke our dom callback which the user placed on our
	            //top level tree ctrl. We will pass the current nodeItem to the user and
	            //expect a return object of the form {template:'some html string',refScope:$scope}.
	            //We will compile the template string against the refScope and append it to our
	            //<attach/> DOM element on our elem variable. This allows the user of our 
	            //directive to attach arbitrary DOM structure to our tree node and compile
	            //it against an arbitrary scope, or even use the native nodeScope.
	            /*temp*******************************************************/
	            $timeout(function(){
	            	domCallBackPromise = rootCtrl.api.getDomAttachment({item:scope.nodeItem,nodeScope:scope});
	            	if(domCallBackPromise && domCallBackPromise.then){
		            	domCallBackPromise.then(function(domCallBackObj){
			            	if(domCallBackObj && domCallBackObj.template && domCallBackObj.refScope){
				            	templateElem = angular.element(domCallBackObj.template);
				            	$compile(templateElem)(domCallBackObj.refScope);
				            	attachElem = angular.element(elem[0].querySelector('attach'));
				            	attachElem.append(templateElem);
				            }
			            });
	            	};
	            },0);
	            
	            //We are going to setup our template here with event handlers and 
	            //add attributes using GOD (Good-Ole-Dom) so as to avoid as many watch
	            //expressions as much as possible. Doing some very UNAngular things
	            //here in order to provide as much scalability as possible
	            //================================================================
	            
	            //Put our rootCtrl on our scope so it can be accessed in our
	            //template HTML.
	            scope.rootCtrl = rootCtrl;
	            
	            //Check if user has supplied us a hint for 
	            //our target child collection. If so we need to use that hint
	            //as our RHS instead of the parsed RHS.
	            if(attrs.sdaTreeChildren){
	              scope.repeater.rhs =attrs.sdaTreeChildren;
	            }
	            
	            //Wrapper for our iconclass callback function specified
	            //by the user.
	            scope.getIconClass = function(d){
	              var nodeItem = this[this.repeater.lhs],
	                  deferred = $q.defer(),
	                  that=this;
	              if(nodeItem){
	            	  return rootCtrl.api.iconCallback({item:nodeItem});
	              }
	            };
	            
	            //Monitor our template input field for the enter key and blur that element when detected
	            scope.keyMonitor= function(e){
	                if(e.charCode===13 && e.target.blur){
	                	e.target.blur();
	                }
	             };
	             
	            //Create our own local allowEdit so we dont shadow the parents value
	            scope.allowEdit=false;
	            
	            //Very important that the user supplies us with a unique nodeID.
	            //All controller API actions are leveraging the existance of a
	            //unique nodeID.
	            //TODO: default to GUID when no nodeID supplied
	            scope.nodeId =''+ $parse(attrs.sdaNodeId)(scope) || "NA";
	            
	            //Build up our nodes tree path based on the nodeId.
	            //Test for the case where a baseNode (starting point for a recurse
	            //chain)
	            if(scope.path && scope.path !==scope.nodeId){
	              scope.path +=',' +scope.nodeId;
	            }else{
	              scope.path = scope.nodeId;
	            }
	
	            //Register on the rootCtrls hash map.
	            rootCtrl.childNodes[scope.nodeId] = scope;
	            
	            scope.dragend = function(src,e){
	            	console.log("DRAG END");
	            	scope.invokeCallback('node-dragend',e);
	            };
	            
	            scope.drop = function(src,e){
	            	console.log("DROP");
	            	scope.invokeCallback('node-drop',e);
	            };
	            
	            //Generic callback wrapper which utilizes the callback defined
	            //on the parent TreeNode directive to communicate back to the 
	            //users controller.
	            scope.invokeCallback = function(name,e){
	              if(e && e.preventDefault){
	                e.preventDefault();
	                e.stopImmediatePropagation();
	                e.stopPropagation();
	              }

	              //All invokes will return a defer as part of the return object
	              var deferred=$q.defer(),
	                  that=this,
	                  data={}, 
	                  op=name,
	                  arrPath,
	                  hashKey,
	                  parentPath,
	                  collObj,
	                  srcScope;
	              
	              srcScope = angular.element(e.srcElement || e.target).scope();
	              
	              this.attrs=attrs;
	              
	               
	              //We can't rely on the elem object to query from as in cases
	              //of sdTreeNodes stamped out from a treeCurse directive the 
	              //elem will be inherited from the last parent element that
	              //was not built through the treeCurse stamp process. To work
	              //around this we keep the input element updated with a js 
	              //selector attr synched with the proper scope.
	              var hk=this[this.repeater.lhs].$$hashKey +'-'+  rootCtrl.api.id,
	                  inputElem = elem[0].querySelector("[data-hashKey='" +hk +"'] input"),
	                  spanElem =  elem[0].querySelector("[data-hashKey='" +hk +"'] span");
	              
	              //Examine our node action and normalize the operation to
	              //a node-[event]. Create the proper action to perform on
	              //node resolution. Handle resolve and reject appropriately.
	              switch(name){
	                
	                //node-expand is the catch all for collapse or expand.
	                //callback is invoked with the context approrpiate event name
	                //based on the isVisible state. Requires resolution.
	                case 'node-expand':
	                  op = (this.isVisible)?'node-collapse':'node-expand';
	                  rootCtrl.api.setNodeIcon(srcScope.nodeId, "isDeferred");
	                  deferred.promise.then(function(){
	                    rootCtrl.api.toggleNode(srcScope.nodeId);
	                    rootCtrl.api.setNodeIcon(srcScope.nodeId, "");
	                  });
	                  break;
	                
	                case 'node-drop':
	                  if(navigator.appName === "Microsoft Internet Explorer"){
	                	  data.dropData =  JSON.parse(e.dataTransfer.getData("text"));
	                  }else{
	                	  data.dropData =  JSON.parse(e.dataTransfer.getData("text/plain"));
	                  }
	                  rootCtrl.api.setNodeIcon(srcScope.nodeId, "isDeferred");
	                  deferred.promise.then(function(){
	                    rootCtrl.api.setNodeIcon(srcScope.nodeId, "");
	                  });
	                  break;
	                case 'node-dragend':
	                  data.dragData = this.nodeItem;
	                  break;
	                //Remove a node from our tree. Requires resolution.
	                case 'menu-delete':
	                  op="node-delete";
	                  arrPath = this.path.split(",");
	                  hashKey = this[this.repeater.lhs].$$hashKey;
	                  parentPath = (arrPath.length >1)?arrPath[arrPath.length-2]:arrPath[0];
	                  rootCtrl.api.setNodeIcon(that.nodeId, "isDeferred");

	                  deferred.promise.then(function(){
	                    rootCtrl.api.removeItem(parentPath,hashKey);
	                    rootCtrl.api.setNodeIcon(that.nodeId, "");
	                  });
	                  break;
	                
	                //Initial rename event, user can reject to prevent UI switiching
	                //to input element. Requires resolution.
	                case 'menu-rename':
	                  op="node-rename";
	                  deferred.promise.then(function(){
	                    that.allowEdit = true;
	                    $timeout(function(){inputElem.focus();},0);
	                  });
	                  break;
	                
	                //when input element loses focus we invoke callback so user
	                //can expect the new value and resolve/reject it as needed.
	                //Requires resolution.
	                case 'node-rename-commit':
	                
	                  if(inputElem && inputElem.value && inputElem.value.trim().length >0){
	                	  data.newValue=inputElem.value;
	                  }
	                  else{
	                	  inputElem.value=spanElem.innerText;
	                  }
	                  rootCtrl.api.setNodeIcon(that.nodeId, "isDeferred");
	                  deferred.promise
	                  .then(function(){
	                	  rootCtrl.api.setNodeIcon(that.nodeId, "");
	                  })
	                  .catch(function(){
	                    //Reset to the initial value which is still in our span elem
	                    inputElem.value=spanElem.innerText;
	                  }).
	                  finally(function(){
	                    //always toggle edit mode off regardless.
	                    that.allowEdit=false;
	                  });
	                  break;
	                
	                //Adding a node to our tree. Requires resolution.
	                case 'menu-add':
	                  op="node-add";
	                  data.collectionPath=this.repeater.rhs;
	                  
	                  deferred.promise.then(function(obj){
	                    that.isVisible = true;
	                    rootCtrl.api.addItem(that.nodeId,obj);
	                    if(angular.isArray(obj)){
	                      obj=obj[0];
	                    }
	                    $timeout(function(){
	                      var selector='[data-hashKey="' + obj.$$hashKey + '-' +  
	                                    rootCtrl.api.id +'"]';
	                      var newObj = elem[0].querySelector(selector);
	                      var objScope = angular.element(newObj).scope();
	                      objScope.invokeCallback('menu-rename',null);
	                    },0);
	                  });
	                  break;
	                
	                
	              }
	              
	              //Inital set up of our return object
	              data.valueItem = this[this.repeater.valueIdentifier];
	              if(data.valueItem){
		              data.deferred =deferred;
		              data.treeEvent = op;
		              data.nodePath = this.path;
		              data.nodeId = this.nodeId;      
		              //this will not be the scope you might first assume for menu events,
		              //srcScope in that instance will be from the treeMenu directive not
		              //the sdTree node.
		              data.srcScope = angular.element(e.srcElement || e.target).scope();
		              //invoke user callback
		              rootCtrl.api.eventCallback({data:data,e:e});
	              }
	              return false;
	            };
	            
	            //Callback specific for our simple menu directive, reformats the
	            //menu item info into a common form to be used by our invokeCallback]
	            //function which will communicate the menu action to the user
	            //defined callback function.
	            scope.menuCallback=function(menuItem,e){
	              e.preventDefault();
	              e.stopPropagation();
	
	              this.invokeCallback('menu-' + menuItem.operation,e);
	   
	             return false;
	            };
	          }//post end
	        };//return end
	
	      }
	    };
	  });
	  
	  //============================================================================
	  // TreeNode Recursive Target, stamps itself out based on DOM string returned
	  // from the sdTree sda=recurse-factory invocation target. Unlike sdTreeCurse
	  // this will stamp out actual sdTreeNodes.
	  //============================================================================
	  mod.directive("sdTreeCurseFx",function($compile,$parse){
	    
	    return{
	      
	      require:["^sdTreeNode","^sdTree"],
	      
	      compile: function(telem,attrs,controllers){
	        
	        return{
	          
	        post: function(scope, elem, attrs, controllers) {

	            var rootCtrl = controllers[1], 
	                template, //Raw DOM string
	                compTemplate; //compiled element ready to be inserted
	            
	            template = rootCtrl.api.recurseFactory({item:scope});
	            compTemplate = $compile(template)(scope);
	            elem.replaceWith(compTemplate);
	            scope.elem = elem;

	          }//Post-Link end
	          
	        };//Compile End
	      }
	    };
	
	  });	  
	  
	  //============================================================================
	  // TreeNode Recursive Target, stamps itself out based on its parent sdTreeNode
	  // Template. A bit of a dupe of treeNode.
	  //============================================================================
	  
	  mod.directive("sdTreeCurse",function($compile,$parse){
	    
	    return{
	      
	      require:["^sdTreeNode","^sdTree"],
	      
	      compile: function(telem,attrs,controllers){
	        
	        return{
	          
	        post: function(scope, elem, attrs, controllers) {
	            //Reference our two controllers
	            var rootCtrl = controllers[1], 
	                parentTreeNodeCtrl = controllers[0];
	            
	            //TagName of recurse container element, need it to reconstruct the
	            //parent tag of our html. controller expressions must be used on this
	            //tag as well in order to accurately stamp out the tag.
	            var tagName; 
	            
	            //Pull TagName so we can rebuild the tree-curse container element.
	            tagName=elem[0].tagName;
	            
	            //We need to provide info to our soon to be stamped out tree node that
	            //it was preprocessed through a recurse directive.
	            scope.isRecursed = true;
	            
	            //Parse out repeat expression
	            scope.repeater=parseRepeatExpr(parentTreeNodeCtrl.repeatExpression);
	            
	            //extract nodeItem 
	            scope.nodeItem=scope[scope.repeater.lhs];
	            
	            //Create our own local allowEdit so we dont shadow the parents value
	            scope.allowEdit=false;
	            
	            //Set our nodeID based off of the sda-node-id attribute as it evaluates
	            //as an expression, or default to the scope.$id
	            scope.nodeId =''+ $parse(parentTreeNodeCtrl.nodeIdExpression)(scope) || scope.$id;
	            
	            var isLeaf = $parse(parentTreeNodeCtrl.leafExpression)(scope);
	            scope.isLeaf = !!isLeaf;

	            //Build up our nodes tree path based on the nodeId.
	            if(scope.path && scope.path !==scope.nodeId){
	              scope.path +=',' +scope.nodeId;
	              console.log(scope.path);
	              //TODO: why do we have duplicates in our path???
	              //scope.path =scope.path.split(",").filter(function(item,pos,self){
	             //   return self.indexOf(item) == pos;
	             // });
	            }else{
	              scope.path = scope.nodeId;
	            }
	            
	            //Register our scope by nodeID on the rootCtrls colelction.
	            rootCtrl.childNodes[scope.nodeId] = scope;
	    
	            //Build up our stampElement as an array, making sure to carry over
	            //our controller indicated expressions.
	            var build = [
	              '<', tagName, ' ng-repeat="' + parentTreeNodeCtrl.repeatExpression + '"',
	              ' sda-node-id="'+ parentTreeNodeCtrl.nodeIdExpression +'"',
	              ' sda-menu-items="' + parentTreeNodeCtrl.menuItems + '"',
	              ' sda-is-leaf="' + parentTreeNodeCtrl.leafExpression + '">',
	              '</', tagName, '>'];
	              
	    
	            var stampElem = angular.element(build.join(''));
	            var embeddedTemplate = angular.element(parentTreeNodeCtrl.template);
	            var hk=scope.nodeId;
	            embeddedTemplate[0].setAttribute('isRecursed',scope.nodeId);
	            embeddedTemplate.on("mouseover",function(){
	              alert("mo");
	            });
	            stampElem.append(embeddedTemplate);
	            
	            // We swap out the element for our new one and tell angular to do its
	            // thing with that.
	            $compile(stampElem)(scope);
	            elem.replaceWith(stampElem);

	          }//Post-Link end
	          
	        };//Compile End
	      }
	    };
	
	  });
	  
	  /*******************************************************************
	 * SIMPLE MENU DIRECTIVE
	 * Optimized so that it only compiles and attaches to the DOM on
	 * the oncontextmenu event. It will remove itself from the DOM
	 * on click so as to maintain the barest angular footprint possible.
	 * *****************************************************************/
	mod.directive("sdSimpleMenu",function($compile,$q,$timeout){
	  
	  //Our HTML we will append to our element.
	  var temp="<div ng-show='isVisible' class='menu'>" +
	           "<ul>" +
	           "<li ng-repeat='item in menuItems'><a ng-click='invokeCallback(item,$event)' " +
	           ">" +
	           "{{item.text}}" +
	           "</a></li>" +
	           "</ul>" +
	           "</div>";
	  
	  var lastMenu; //Variable we can close over to make sure we only
	  				//have a single menu open at a time;
	  
	  //Parsing function for our string representing our menu item tuples.
	  var parseMenuItems = function(v){
	    
	    var menuItems=v.replace(/^\s*\(/,'') //opening paren
	                   .replace(/\s*\)\s*$/,'') //closing paren
	                   .split(/\)\s*,\s*\(/);//split on ),(
	          
	    menuItems = menuItems.map(function(tuple){
	                  var temp,k,v;
	                  temp=tuple.split(",");
	                  if(temp.length>0){
	                    k=temp[0];
	                    v=(temp[1])?temp[1]:k;
	                  }
	                  return {operation:k,text:v};
	                });
	                
	    return menuItems || [];
	        
	  };
	  
	  return{
	      require:"^sdTree",
	      link:function(scope,elem,attrs,treeCtrl){
	
	        //Compile template seperately and set scope vars
	        var compHtml,
	            menuItems;

	        scope.menuItems=[]; //k,v pairs which are populated from an attr
	        
	        //Parse our string of menu items into objects we will hang on our scope.
	        //Should be tuples of the form (a,b),(c,d) where the key is the operation
	        //and the value is the text to display on the menu. Operation is the 
	        //value reported through the callback.
	        if(attrs.sdSimpleMenu){
	          scope.menuItems = parseMenuItems(attrs.sdSimpleMenu);
	        }
	        
	        scope.hasInvokedMenuItems = false;
	        
	        
	        //Block context menu, we will detect right clicks instead so-as-to
	        //workaround an issue in firefox.
	        elem.on("contextmenu",function(e){
	        	e.preventDefault();
		        e.stopPropagation();
		        return false;
	        });
	        
	        //Only on our contextmenu event will we actually compile our html
	        //and append to the DOM. This keeps our angular footprint as small as 
	        //possible. When the menu is closed the DOM element is removed.
	        elem.on("mouseup",function(e){
	        	
	          //unless a right click immediately return
	          if(e.which!==3){
	    		 return true;
	          };
	          
	          var treeNode = elem.scope(),
	              treeItem,
	              deferred,
	              data = {};
	             
	          deferred = $q.defer();
	          treeItem = treeCtrl.api.getItem(treeNode.nodeId);
	          
	          data.deferred = deferred;
	          data.item = treeItem;
	          
	          if(!scope.hasInvokedMenuItems || true){
	            treeCtrl.api.getMenuItems({item:data});
	            $timeout(function(){deferred.resolve("")},250);
	            scope.hasInvokedMenuItems=true;
	          }
	          else{
	            deferred.resolve("");
	          }
	          
	          deferred.promise.then(function(menuString){
	            var newItems;
	            if(menuString){
	              newItems = parseMenuItems(menuString);
	              scope.menuItems= newItems.concat(parseMenuItems(attrs.sdSimpleMenu));
	            }
	            if(scope.menuItems.length >1){
		            scope.isVisible=true;
		            scope.watchForMouseOut = false;
		            compHtml=$compile(temp)(scope);
		            
		            //Check for a valid lastMenu and remove it
		            if(lastMenu && lastMenu.remove){
			            lastMenu.remove();
			            lastMenu = null;
		          	}
		            
		            lastMenu = compHtml;//Assign current menu as lastMenu
		            
		            compHtml.on("mouseleave",function(){
	            		compHtml.remove();
		            });
		            
		            compHtml.on("click",function(e2){
		              compHtml.remove();
		              scope.invokeCallback(null,e2);
		            });

		            elem.append(compHtml);
	            }
	          });
	          
	          e.preventDefault();
	          e.stopPropagation();
	          return false;
	        });
	        
	        //Wrapper function for user callback for any menu operation.
	        //invokes our callback and removes menu.
	        scope.invokeCallback = function(menuItem,e){
	          e.preventDefault();
	          e.stopPropagation();
	          if(menuItem){
	            this.callback({data:menuItem,e:e});
	          }
	          compHtml.remove();
	          return false;
	        };
	
	      },
	    scope:{
	      callback: "&sdaMenuCallback"
	    }
	  };
	});

	

})();