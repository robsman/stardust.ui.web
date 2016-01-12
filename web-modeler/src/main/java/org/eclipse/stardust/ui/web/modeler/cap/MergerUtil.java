/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.cap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.Code;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.GenericLinkConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElementNodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipantSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IdentifiableReference;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ParameterMappingType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.extensions.FormalParameterMappingsType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParametersType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;

public class MergerUtil
{
   public static StoreObject storage;
   public static void setStorage(StoreObject storage)
   {
      MergerUtil.storage = storage;
   }

   /**
    * Merger Entry contains a key and a value - key is the raw object, value is the copy
    */
   static class MergerEntry implements Map.Entry<EObject, EObject>
   {
      private final EObject key;
      private EObject value;
   
      MergerEntry(EObject key, EObject value) 
      {
         this.key = key;
         this.value = value;
      }
   
      public EObject getKey()
      {
         return key;
      }
   
      public EObject getValue()
      {
         return value;
      }
   
      public EObject setValue(EObject value)      
      {
         EObject oldValue = this.value;
         this.value = value;
         return oldValue;
      }      
   }

   // collect global elements from trigger, only if not same model
   public static void getObjectsFromTrigger(TriggerType trigger, Map globalElements, Copier copier)
   {
      EList attributes = trigger.getAttribute();
      for(int a = 0; a < attributes.size(); a++)
      {
         AttributeType attribute = (AttributeType) attributes.get(a);
         IdentifiableReference reference = attribute.getReference();
         if(reference != null)
         {
            EObject element = reference.getIdentifiable();
            if(element != null && element instanceof IModelParticipant)
            {
               if(!globalElements.containsKey(element))
               {
                  globalElements.put(element, copier.copy(element));                     
               }   
               if(element instanceof ConditionalPerformerType)
               {
                  DataType dataType = ((ConditionalPerformerType) element).getData();
                  if(dataType != null)
                  {                  
                     if(!globalElements.containsKey(dataType))
                     {
                        globalElements.put(dataType, copier.copy(dataType)); 
                        checkAuthorizations(dataType, globalElements, copier);
                     }   
                  }
                  String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) element, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
                  if(!StringUtils.isEmpty(dataId))
                  {
                     if(dataType == null || !dataType.getId().equals(dataId))
                     {
                        DataType realmData = (DataType) ModelUtils.findElementById(storage.getOriginalModelCopy().getData(), dataId);
                        if(realmData != null && !globalElements.containsKey(realmData))
                        {
                           globalElements.put(realmData, copier.copy(realmData));  
                           checkAuthorizations(realmData, globalElements, copier);
                        }
                     }                     
                  }
               }                                                
            }
         }
      }
      EList mappings = trigger.getParameterMapping();
      for(int m = 0; m < mappings.size(); m++)
      {
         ParameterMappingType mappingType = (ParameterMappingType) mappings.get(m);
         DataType data = mappingType.getData();
         if(!globalElements.containsKey(data))
         {
            globalElements.put(data, copier.copy(data));                     
            checkAuthorizations(data, globalElements, copier);
         }                  
      }      
   }   
   
   // collect global elements from activity, called only if not same model!
   public static void getObjectsFromActivity(ActivityType activity, Map globalElements, Map processes, Map elements, Copier copier)
   {
      IModelParticipant performer = activity.getPerformer();
      if(performer != null && !globalElements.containsKey(performer))
      {
         globalElements.put(performer, copier.copy(performer)); 
      }   
      IModelParticipant qualityControlPerformer = activity.getQualityControlPerformer();
      if(qualityControlPerformer != null && !globalElements.containsKey(qualityControlPerformer))
      {
         globalElements.put(qualityControlPerformer, copier.copy(qualityControlPerformer)); 
      }         
      
      if(performer != null && performer instanceof ConditionalPerformerType)
      {
         DataType dataType = ((ConditionalPerformerType) performer).getData();
         if(dataType != null)
         {                  
            if(!globalElements.containsKey(dataType))
            {
               globalElements.put(dataType, copier.copy(dataType));                        
               checkAuthorizations(dataType, globalElements, copier);
            }   
         }
         String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) performer, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
         if(!StringUtils.isEmpty(dataId))
         {
            if(dataType == null || !dataType.getId().equals(dataId))
            {
               DataType realmData = (DataType) ModelUtils.findElementById(storage.getOriginalModelCopy().getData(), dataId);
               if(realmData != null && !globalElements.containsKey(realmData))
               {
                  globalElements.put(realmData, copier.copy(realmData));  
                  checkAuthorizations(realmData, globalElements, copier);
               }
            }                     
         }
      }
      ApplicationType application = activity.getApplication();
      if(application != null && !globalElements.containsKey(application))
      {
         globalElements.put(application, copier.copy(application));                                             
      }
      EList dataMappings = activity.getDataMapping();
      for(Iterator dm = dataMappings.iterator(); dm.hasNext();)
      {
         DataMappingType dataMapping = (DataMappingType) dm.next();
         DataType dataType = dataMapping.getData();
         if(dataType != null && !globalElements.containsKey(dataType))
         {
            globalElements.put(dataType, copier.copy(dataType));                                    
            checkAuthorizations(dataType, globalElements, copier);
         }
      }   
      EList eventHandler = activity.getEventHandler();
      for(int e = 0; e < eventHandler.size(); e++)
      {
         EventHandlerType eventHandlerType = (EventHandlerType) eventHandler.get(e);
         EList eventAction = eventHandlerType.getEventAction();
         for(int a = 0; a < eventAction.size(); a++)
         {
            EventActionType eventActionType = (EventActionType) eventAction.get(a);
            EventActionTypeType type = eventActionType.getType();
            if(type != null && !elements.containsKey(type))
            {
               elements.put(type, copier.copy(type));            
            }
            if(eventActionType instanceof IExtensibleElement)
            {
               AttributeType attribute = AttributeUtil.getAttribute((IExtensibleElement) eventActionType, 
                     PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
               if (attribute != null)
               {
                  if (attribute.getReference() != null)
                  {
                     IIdentifiableModelElement result = (IIdentifiableModelElement) attribute.getReference().getIdentifiable();
                     if(result != null && processes != null && !processes.containsKey(result))
                     {
                        processes.put(result, copier.copy(result));                                 
                     }
                  }
               }                        
            }
         }                  
      }
      ProcessDefinitionType subProcess = activity.getImplementationProcess(); 
      if(subProcess != null && processes != null && !processes.containsKey(subProcess))
      {
         processes.put(subProcess, copier.copy(subProcess));                  
      }  
      
      EList<Code> validQualityCodes = activity.getValidQualityCodes();
      for(Code code : validQualityCodes)
      {
         if(!elements.containsKey(code))
         {
            elements.put(code, copier.copy(code));
         }         
      }      
   }
   
   // for each symbol (IModelElementNodeSymbol)
   static void getObjectsFromSymbol(IModelElementNodeSymbol symbol, Map elements, Map globalElements, Map processes, Map activities, 
         Copier copier, boolean isSameModel, boolean isSelected)
   {
      IIdentifiableModelElement modelElement = symbol.getModelElement();
      if(!isSameModel)
      {
         checkAuthorizations(modelElement, globalElements, copier);
      }
      
      if(modelElement != null)
      {               
         if(symbol instanceof StartEventSymbol)
         {
            if(!isSameModel)
            {
               MergerUtil.getObjectsFromTrigger((TriggerType) modelElement, globalElements, copier);
            }
         }
         
         // we must also copy the activities (by value!)
         if(symbol instanceof ActivitySymbolType)
         {
            if(!isSameModel)
            {
               if(activities != null && !activities.containsKey(modelElement))
               {
                  activities.put(modelElement, copier.copy(modelElement));                         
               }
               MergerUtil.getObjectsFromActivity((ActivityType) modelElement, globalElements, processes, elements, copier);
            }
         }
         
         // must always collect the processes also
         if(symbol instanceof ProcessSymbolType)
         {
            if(!processes.containsKey(modelElement))
            {
               processes.put(modelElement, copier.copy(modelElement));                     
            }                        
         }         
         // collect global objects
         else if(symbol instanceof IModelParticipantSymbol
               || symbol instanceof ApplicationSymbolType
               || symbol instanceof DataSymbolType)
         {
            if(isSelected)
            {
               // for dialog by value or by reference
               if(!elements.containsKey(modelElement))
               {
                  elements.put(modelElement, copier.copy(modelElement));
               }               
            }
            else
            {
               if(!globalElements.containsKey(modelElement))
               {
                  globalElements.put(modelElement, copier.copy(modelElement));                        
               }               
            }            
            if(!isSameModel)
            {            
               if(modelElement instanceof ConditionalPerformerType)
               {
                  DataType dataType = ((ConditionalPerformerType) modelElement).getData();
                  if(dataType != null)
                  {                  
                     if(!globalElements.containsKey(dataType))
                     {
                        globalElements.put(dataType, copier.copy(dataType));                        
                     }   
                  }
                  String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) modelElement, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
                  if(!StringUtils.isEmpty(dataId))
                  {
                     if(dataType == null || !dataType.getId().equals(dataId))
                     {
                        DataType realmData = (DataType) ModelUtils.findElementById(storage.getOriginalModelCopy().getData(), dataId);
                        if(realmData != null && !globalElements.containsKey(realmData))
                        {
                           globalElements.put(realmData, copier.copy(realmData));  
                        }
                     }                     
                  }
               }                                 
            }
         }
         else 
         {
            if(!elements.containsKey(modelElement))
            {
               // elements like trigger, activities
               elements.put(modelElement, copier.copy(modelElement));
            }
         }
      }      
   }   
   
   ////////////////////
      
   // check as a process contains activities, etc.      
   // is called only if not same model - for each process
   static void getObjectsFromProcess(EObject raw, Map elements, Map globalElements, Copier copier)
   {      
      FormalParameterMappingsType formalParameterMappings = ((ProcessDefinitionType) raw).getFormalParameterMappings();
      FormalParametersType referencedParametersType = ((ProcessDefinitionType) raw).getFormalParameters();
      if(referencedParametersType != null && formalParameterMappings != null)
      {      
         for (Iterator<FormalParameterType> i = referencedParametersType.getFormalParameter().iterator(); i.hasNext();) {
            FormalParameterType referencedParameterType = i.next();
            DataType mappedData = formalParameterMappings.getMappedData(referencedParameterType);         
            if(mappedData != null)
            {
               if(!globalElements.containsKey(mappedData))
               {
                  globalElements.put(mappedData, copier.copy(mappedData));                     
                  MergerUtil.checkAuthorizations(mappedData, globalElements, copier);
               }                           
            }
         }
      }
      
      EList dataPathList = ((ProcessDefinitionType) raw).getDataPath();
      for(int d = 0; d < dataPathList.size(); d++)
      {
         DataPathType dataPath = (DataPathType) dataPathList.get(d);
         DataType data = dataPath.getData();
         if(data != null)
         {
            if(!globalElements.containsKey(data))
            {
               globalElements.put(data, copier.copy(data));                     
               MergerUtil.checkAuthorizations(data, globalElements, copier);
            }            
         }
      }                  
      EList diagrams = ((ProcessDefinitionType) raw).getDiagram();
      for(int d = 0; d < diagrams.size(); d++)
      {
         DiagramType diagram = (DiagramType) diagrams.get(d);
         MergerUtil.getObjectsFromDiagram(diagram, elements, globalElements, null, null, copier, false);            
      }            
      EList processActivities = ((ProcessDefinitionType) raw).getActivity();
      for(int i = 0; i < processActivities.size(); i++)
      {
         ActivityType activity = (ActivityType) processActivities.get(i);
         MergerUtil.checkAuthorizations(activity, globalElements, copier);
         MergerUtil.getObjectsFromActivity(activity, globalElements, null, elements, copier);
      }
      EList processTrigger = ((ProcessDefinitionType) raw).getTrigger();
      for(int i = 0; i < processTrigger.size(); i++)
      {
         TriggerType trigger = (TriggerType) processTrigger.get(i);
         MergerUtil.getObjectsFromTrigger(trigger, globalElements, copier);
      }
   }

   // should only be called fromn diagram merger if copy lanes (lanesymbol) 
   static void getObjectsFromLane(EObject raw, Map elements, Map globalElements, Map processes, Map activities, Copier copier, boolean isSameModel)
   {
      LaneSymbol lane = (LaneSymbol) raw; 
      IModelParticipant laneParticipant = lane.getParticipantReference();      
      if(!isSameModel)
      {      
         if(laneParticipant != null)
         {
            if(!globalElements.containsKey(laneParticipant))
            {
               globalElements.put(laneParticipant, copier.copy(laneParticipant));                        
            }   
            if(laneParticipant instanceof ConditionalPerformerType)
            {
               DataType dataType = ((ConditionalPerformerType) laneParticipant).getData();
               if(dataType != null)
               {                  
                  if(!globalElements.containsKey(dataType))
                  {
                     globalElements.put(dataType, copier.copy(dataType));                        
                  }   
               }
               String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) laneParticipant, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
               if(!StringUtils.isEmpty(dataId))
               {
                  if(dataType == null || !dataType.getId().equals(dataId))
                  {
                     DataType realmData = (DataType) ModelUtils.findElementById(storage.getOriginalModelCopy().getData(), dataId);
                     if(realmData != null && !globalElements.containsKey(realmData))
                     {
                        globalElements.put(realmData, copier.copy(realmData));  
                     }
                  }                     
               }
            }                     
         }            
      }
      for(Iterator iter = ((LaneSymbol) lane).getNodes().valueListIterator(); iter.hasNext();)
      {
         EObject symbol = (EObject) iter.next();                     
         if(symbol instanceof IModelElementNodeSymbol)
         {
            getObjectsFromSymbol((IModelElementNodeSymbol) symbol, elements, globalElements, processes, activities, copier, isSameModel, false);
         }
      }
      EList genericLinks = ((LaneSymbol) lane).getGenericLinkConnection();
      for (int i = 0; i < genericLinks.size(); i++)
      {
         GenericLinkConnectionType link = (GenericLinkConnectionType) genericLinks.get(i);
         EObject modelElement = link.getLinkType();            
         if(!globalElements.containsKey(modelElement))
         {            
            globalElements.put(modelElement, copier.copy(modelElement));                     
         }
      } 
   }
      
   static void getObjectsFromDiagram(EObject raw, Map elements, Map globalElements, Map processes, Map activities, Copier copier, boolean isSameModel)
   {      
      List pools = ((DiagramType) raw).getPoolSymbols();            
      for(int p = 0; p < pools.size(); p++)
      {
         PoolSymbol pool = (PoolSymbol) pools.get(p); 
         for(Iterator iter = ((PoolSymbol) pool).getNodes().valueListIterator(); iter.hasNext();)
         {
            EObject element = (EObject) iter.next();
            if(element instanceof IModelElementNodeSymbol)
            {
               getObjectsFromSymbol((IModelElementNodeSymbol) element, elements, globalElements, processes, activities, copier, isSameModel, false);
            }               
         }         
         EList genericLinks = ((PoolSymbol) pool).getGenericLinkConnection();
         for (int i = 0; i < genericLinks.size(); i++)
         {
            GenericLinkConnectionType link = (GenericLinkConnectionType) genericLinks.get(i);
            EObject modelElement = link.getLinkType();   
            if(!globalElements.containsKey(modelElement))
            {            
               globalElements.put(modelElement, copier.copy(modelElement));                     
            }
         }
         EList lanes = ((PoolSymbol) pool).getLanes();
         for(int l = 0; l < lanes.size(); l++)
         {
            LaneSymbol lane = (LaneSymbol) lanes.get(l);             
            getObjectsFromLane(lane, elements, globalElements, processes, activities, copier, isSameModel);
         }
      }            
      for(Iterator iter = ((DiagramType) raw).getNodes().valueListIterator(); iter.hasNext();)
      {
         EObject element = (EObject) iter.next();         
         if(element instanceof IModelElementNodeSymbol)
         {
            getObjectsFromSymbol((IModelElementNodeSymbol) element, elements, globalElements, processes, activities, copier, isSameModel, false);
         }
      }
      EList genericLinks = ((DiagramType) raw).getGenericLinkConnection();
      for (int i = 0; i < genericLinks.size(); i++)
      {
         GenericLinkConnectionType link = (GenericLinkConnectionType) genericLinks.get(i);
         EObject modelElement = link.getLinkType(); 
         if(!globalElements.containsKey(modelElement))
         {            
            globalElements.put(modelElement, copier.copy(modelElement));                     
         }
      }      
   }   
   
   ////////////////////
   
   // merge processes
   static void mergeProcesses(Map processes, Map processesToMerge)
   {
      Iterator it = processesToMerge.entrySet().iterator(); 
      while (it.hasNext()) 
      {
         Map.Entry entry = (Map.Entry) it.next();
         ProcessDefinitionType process = (ProcessDefinitionType) entry.getKey();
         EObject copy = (EObject) entry.getValue();         
         // if not already in processes add it
         if(!processes.containsKey(process))
         {
            processes.put(process, copy);
         }
      }
   }   
   
   // collect all subProcesses recursive, check that no duplicates are copied
   static Map collectSubProcesses(ProcessDefinitionType scanProcess, Copier copier, Map currentProcesses)
   {
      // all subProcesses
      Map allSubProcesses = new HashMap();
      Map processes = new HashMap();
      EList activities = scanProcess.getActivity();      
      for(int i = 0; i < activities.size(); i++)
      {
         ActivityType activity = (ActivityType) activities.get(i);         
         ProcessDefinitionType subProcess = ((ActivityType) activity).getImplementationProcess(); 
         if(subProcess != null 
               && !processes.containsKey(subProcess)
               && !currentProcesses.containsKey(subProcess))
         {
            Map subProcesses = null;
            MergerEntry subProcessEntry = new MergerEntry(subProcess, copier.copy(subProcess));            
            // add to local collection and global collection
            processes.put(subProcessEntry.getKey(), subProcessEntry.getValue()); 
            if(!currentProcesses.containsKey(subProcess))
            {
               // concurrent modification exception
               // currentProcesses.put(subProcessEntry.getKey(), subProcessEntry.getValue());
            }            
            subProcesses = MergerUtil.collectSubProcesses(subProcess, copier, currentProcesses);
            if(subProcesses != null)
            {
               MergerUtil.mergeProcesses(allSubProcesses, subProcesses);
            }         
         }                              
      }
      MergerUtil.mergeProcesses(processes, allSubProcesses);      
      if(processes.isEmpty())
      {
         return null;
      }      
      return processes;
   }
   
   // does not work for diagram 
   // would be better to use equals!
   // method makes no sense because we have element already
   static Map.Entry getEntryFromMap(Map map, EObject element)
   {
      Iterator it = map.entrySet().iterator();
      while(it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         // compare raw
         EObject raw = (EObject) entry.getKey();
         if(raw.equals(element))
         {
            return entry;
         }
      }
      return null;
   }  
   
   static EObject getElementFromList(List entries, EObject element)
   {
      for(int i = 0; i < entries.size(); i++)
      {
         Object object = entries.get(i);
         Map.Entry entry;
         if(object instanceof ContentDecorator)
         {
            entry = (Entry) ((ContentDecorator) object).getContent();
         }
         else
         {
            entry = (Entry) object;
         }
         // compare raw
         EObject raw = (EObject) entry.getKey();
         EObject copy = (EObject) entry.getValue();
         if(raw.equals(element))
         {
            return copy;            
         }         
      }
      return null;
   }
   
   // the user should decide to copy a type declaration or use the one with the same id 
   public static boolean containsType(List currentTypes, String idToCheck)
   {
      for(int t = 0; t < currentTypes.size(); t++)
      {
         TypeDeclarationType td = (TypeDeclarationType) currentTypes.get(t);         
         String currentId = td.getId();
         if(currentId.equals(idToCheck))
         {
            return true;
         }
      }            
      return false;
   }  
   
   // we see external references mostly after an xsd import
   public static void checkAllTypeDeclarationReferences(ModelType sourceModel, String typeId, Set xsdFiles, Set tempStructuredData)
   {
	   TypeDeclarationsType sourceDeclarations = sourceModel.getTypeDeclarations();
       TypeDeclarationType td = sourceDeclarations.getTypeDeclaration(typeId);
       if(td != null)
       {
    	   ExternalReferenceType externalReference = td.getExternalReference();
    	   // collect external references
    	   if(externalReference != null)
    	   {	
    		   String location = externalReference.getLocation();
    		   if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
    		   {
    			   String nextTypeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
    			   tempStructuredData.add(nextTypeId);
    			   // get/check all other type declarations
    			   MergerUtil.checkAllTypeDeclarationReferences(sourceModel, nextTypeId, xsdFiles, tempStructuredData);    			 
    		   }
    		   else if(!location.startsWith("http://")) //$NON-NLS-1$
    		   {
    			   xsdFiles.add(location);
    		   }
    	   }
       }
   }   

   public static void checkAllXSDImportReferences(ModelType sourceModel, String typeId, Set xsdFiles, Set tempStructuredData)
   {
	   TypeDeclarationsType sourceDeclarations = sourceModel.getTypeDeclarations();	   
	   TypeDeclarationType td = sourceDeclarations.getTypeDeclaration(typeId);
	   XSDSchema schema = td.getSchema();
	   if(schema != null)
	   {
		   List xsdImports = TypeDeclarationUtils.getImports(schema);
		   if(xsdImports != null)
		   {
			   Iterator it = xsdImports.iterator(); 
			   while (it.hasNext()) 
			   {
				   XSDImport xsdImport = (XSDImport) it.next();
				   String schemaLocation = xsdImport.getSchemaLocation();
				   if (schemaLocation != null)		   
				   {
					   if(schemaLocation.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
					   {
						   String nextTypeId = schemaLocation.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
						   tempStructuredData.add(nextTypeId);			   
						   checkAllXSDImportReferences(sourceModel, nextTypeId, xsdFiles, tempStructuredData);
					   }
					   else if(!schemaLocation.startsWith("http://")) //$NON-NLS-1$
					   {
						   xsdFiles.add(schemaLocation);			   
					   }		   
				   }		   
			   }
		   }		   
	   }	   
   }   
   
   public static void updateTypeDefinition(TypeDeclarationType declaration, ModelType targetModel, Map structuredDataChangedCache)
   {    
      XSDSchema clone = declaration.getSchema();
      Map<String, String> prefixes = clone.getQNamePrefixToNamespaceMap();
      List<String> addPrefixes = new ArrayList<String>();      
      
      Set<Entry<String, String>> set = new HashSet<Entry<String,String>>(prefixes.entrySet());
      
      for (Iterator itr = set.iterator(); itr.hasNext();)
      {
         Map.Entry entry = (Entry) itr.next();
         if (!entry.getKey().equals("xsd")) //$NON-NLS-1$
         {
            // elements that needs to be set with the new TypeDeclarationType
            Set elements = new HashSet();    

            String value = (String) entry.getValue();
            int idx = value.lastIndexOf("/") + 1; //$NON-NLS-1$
            // TypeDeclarationType   
            String elementName = value.substring(idx, value.length());
            
            idx = value.lastIndexOf("/" + elementName); //$NON-NLS-1$
            String value2 = value.substring(0, idx);
            idx = value2.lastIndexOf("/") + 1; //$NON-NLS-1$         
            // ModelType
            String modelId = value2.substring(idx, value2.length());
            
            // remove as this namespace points to old model
            if(!targetModel.getId().equals(modelId))
            {
               TypeDeclarationUtils.removeNameSpace(clone, elementName, modelId); 
               String newElementName = getValueByKey(structuredDataChangedCache, elementName);
               if(newElementName == null)
               {
                  newElementName = elementName;
               }
               
               String prefix = TypeDeclarationUtils.computePrefix(newElementName, clone.getQNamePrefixToNamespaceMap().keySet());
               if(!addPrefixes.contains(prefix))
               {
            	  String nameSpace = TypeDeclarationUtils.computeTargetNamespace(targetModel, newElementName);                
                  addPrefixes.add(prefix);               
                  clone.getQNamePrefixToNamespaceMap().put(prefix, nameSpace);
               }
            }
            
            // references to other TypeDeclarationType   
            if(!elementName.equals(declaration.getId()))
            {
               // if name/id changed by copy/paste
               String oldElementName = getKeyByValue(structuredDataChangedCache, elementName);
               if(oldElementName == null)
               {
                  oldElementName = elementName;
               }
               
               // search for elements
               TypeDeclarationUtils.findElementsForType(declaration, elements, oldElementName);
               Iterator it = elements.iterator();
               while (it.hasNext())
               {
                  XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) it.next();
                  elementDeclaration.setTypeDefinition(null);
               }               

               // search TypeDeclarationType in this ModelType  
               String newElementName = getValueByKey(structuredDataChangedCache, elementName);
               if(newElementName == null)
               {
                  newElementName = elementName;
               }
               
               XSDTypeDefinition definition = TypeDeclarationUtils.getTypeDefinition((TypeDeclarationsType) declaration.eContainer(), newElementName);
               if(definition != null)
               {
                  it = elements.iterator();
                  while (it.hasNext())
                  {
                     XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) it.next();
                     elementDeclaration.setTypeDefinition(definition);
                  }                                          
               }
            }
         }
      }
      clone.updateElement(true);      
   }
   
   public static String getKeyByValue(Map structuredDataChangedCache, String value)
   {
      for (Iterator itr = structuredDataChangedCache.entrySet().iterator(); itr.hasNext();)
      {
         Map.Entry entry = (Entry) itr.next();
         if (entry.getValue().equals(value))            
         {
            return (String) entry.getKey();
         }
      }
      return null;
   }   
   
   public static String getValueByKey(Map structuredDataChangedCache, String key)
   {
      for (Iterator itr = structuredDataChangedCache.entrySet().iterator(); itr.hasNext();)
      {
         Map.Entry entry = (Entry) itr.next();
         if (entry.getKey().equals(key))            
         {
            return (String) entry.getValue();
         }
      }
      return null;
   }      
   
   public static void checkAuthorizations(EObject modelElement, Map globalElements, Copier copier)
   {
      if(modelElement instanceof ProcessDefinitionType         
            || modelElement instanceof DataType         
            || modelElement instanceof ActivityType)
      {
         List participants = getAuthorizationParticipants((IExtensibleElement) modelElement);
         if(participants != null)
         {
            for (int i = 0; i < participants.size(); i++)
            {
               IModelParticipant participant = (IModelParticipant) participants.get(i);
               if(!globalElements.containsKey(participant))
               {
                  globalElements.put(participant, copier.copy(participant));                        
               }                              
            }         
         }      
      }
   }
   
   private static List getAuthorizationParticipants(IExtensibleElement element)
   {
      List participants = new ArrayList();
      
      List attributes = element.getAttribute();
      for (int i = 0; i < attributes.size(); i++)
      {
         AttributeType attribute = (AttributeType) attributes.get(i);
         if(attribute.getName().startsWith(AbstractMerger.AUTHORIZATION_SCOPE))
         {
            EObject model = AttributeUtil.getReferenceElement(attribute);
            if(model != null)
            {
               participants.add(model);
            }
         }
      }
      if(!participants.isEmpty())
      {
         return participants;
      }
      return null;
   }   
   
   public static Code containsQC(ModelType targetModel, Code raw)
   {
      String keyValue = raw.getCode();
      
      if(targetModel.getQualityControl() == null)
      {
         return null;         
      }
      
      EList<Code> codes = targetModel.getQualityControl().getCode();
      for(Code code : codes)
      {
         if(code.getCode().equals(keyValue))
         {
            return code;
         }
      }            
      
      return null;
   }

   public static ModelVariable findModelVariable(List<ModelVariable> list, ModelVariable var)
   {
      for(ModelVariable variable : list)
      {
         if(variable.getName().equals(var.getName()))
         {
            return variable;
         }         
      }
      return null;
   }
   
   public static List<ModelVariable> findMergedVariables(List<ModelVariable> oldList,List<ModelVariable> newList)
   {
      List<ModelVariable> merged = new ArrayList<ModelVariable>();
      
      if(newList.size() != oldList.size())
      {
         for(ModelVariable variable : newList)
         {
            if(findModelVariable(oldList, variable) == null)
            {
               merged.add(variable);
            }
            
         }         
      }
      
      return merged;
   }
}