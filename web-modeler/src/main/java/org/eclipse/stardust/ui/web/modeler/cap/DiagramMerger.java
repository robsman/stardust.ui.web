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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.Coordinates;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.FlowControlType;
import org.eclipse.stardust.model.xpdl.carnot.GatewaySymbol;
import org.eclipse.stardust.model.xpdl.carnot.GenericLinkConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.IConnectionSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElementNodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISymbolContainer;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LinkTypeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoutingType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.DiagramUtil;
import org.eclipse.stardust.ui.web.modeler.cap.MergerUtil.MergerEntry;



/**
 * @author grotjahn
 * @version $Revision: 66042 $
 */
public class DiagramMerger extends AbstractMerger
{
   public static int CREATE_ALL = 0;
   public static int CREATE_SYMBOLS = 1;
   public static int CREATE_SUBPROCESS = 2;

   private int pasteMode;

   private Copier transitionCopier = new EcoreUtil.Copier();
   // connections and transitions
   protected List connections = new ArrayList();
   protected List connectionObjects = new ArrayList();
   protected List checkActivities = new ArrayList();

   // symbols
   private List symbols = new ArrayList();
   private DiagramType sourceDiagram;

   // copy of lanes
   private boolean copyLanes = false;
   // leaf lanes, where we may have symbols
   private List lanes = new ArrayList();
   // symbols in the lanes (raw), copier already copies lanes with symbols
   private List rawSymbols = new ArrayList();
   private Map laneHierarchies = new HashMap();
   private ISwimlaneSymbol parentLane = null;

   // a dialog may popup asking if copy by reference or by value (value means creating new elements)
   public DiagramMerger(ModelType targetModel, List input, StoreObject storage,
         int pasteMode)
   {
      // super generates a name/id cache and sets the highest oid
      super(targetModel, storage);
      MergerUtil.setStorage(storage);

      this.pasteMode = pasteMode;


      isDiagram = true;
      storage.setIsDiagram(true);


      targetProcess = storage.getTargetProcess();
      sourceDiagram = storage.getSourceDiagram();

      if(input.get(0) instanceof LaneSymbol)
      {
         // user selected lanes only
         copyLanes = true;
         for (int i = 0; i < input.size(); i++)
         {
            LaneSymbol laneSymbol = (LaneSymbol) input.get(i);
            MergerEntry laneEntry = new MergerEntry(laneSymbol, copier.copy(laneSymbol));
            List hierarchy = LaneUtils.createLaneHierarchyCopy(laneSymbol, copier);
            // contains hierarchy and all copies already, incl. symbols
            laneHierarchies.put(laneEntry, hierarchy);
         }
         // 1st iterate over all lanes and check if dialog for name/id is needed
         // also collects lanes with symbols
         if(!iterateLanes(laneHierarchies, false))
         {
            return;
         }
         input = new ArrayList();
      }
      // symbolEntry lanes (raw/copy) that contain symbols
      if(!lanes.isEmpty())
      {
         for (int i = 0; i < lanes.size(); i++)
         {
            MergerEntry symbolEntry = (MergerEntry) lanes.get(i);
            EObject raw = (EObject) symbolEntry.getKey();
            MergerUtil.getObjectsFromLane(raw, elements, globalElements, processes, activities, copier, isSameModel);
         }
      }

      // analyze elements (and maybe create container for those elements - check activities)
      // input contains copies of the original model
      for (int i = 0; i < input.size(); i++)
      {
         // get modelElement of the symbol (if there is one)
         EObject symbol = (EObject) input.get(i);
         // symbol is already a copy - do not change raw!

         MergerEntry symbolEntry = new MergerEntry(symbol, copier.copy(symbol));
         if(symbol instanceof IModelElementNodeSymbol)
         {
            if(pasteMode == CREATE_ALL)
            {
               MergerUtil.getObjectsFromSymbol((IModelElementNodeSymbol) symbol, elements,
                     globalElements, processes, activities, copier, isSameModel, true);
            }
            else if(pasteMode == CREATE_SUBPROCESS)
            {
               if(symbol instanceof ActivitySymbolType
                     || symbol instanceof AbstractEventSymbol)
               {
                  IIdentifiableModelElement modelElement = ((IModelElementNodeSymbol) symbol).getModelElement();
                  if(modelElement != null && !elements.containsKey(modelElement))
                  {
                     // elements like trigger, activities
                     elements.put(modelElement, copier.copy(modelElement));
                  }
               }
            }
         }
         symbols.add(symbolEntry);
      }
      if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
      {
         if(pasteMode == CREATE_ALL)
         {
            collectProcessContent();
            // only if not same model
            if(!isSameModel)
            {
               checkForDialog(targetModel);
            }
         }
         collectElements();
      }
      copier.copyReferences();

      // symbolEntry lanes (raw/copy) that contain symbols
      if(copyLanes)
      {
         iterateLanes(laneHierarchies, true);
         // 1st iterate and collect all raw symbols from lanes
         if(!lanes.isEmpty())
         {
            for (int i = 0; i < lanes.size(); i++)
            {
               MergerEntry laneEntry = (MergerEntry) lanes.get(i);
               EObject raw = (EObject) laneEntry.getKey();
               LaneSymbol lane = (LaneSymbol) raw;
               for(Iterator iter = ((LaneSymbol) lane).getNodes().valueListIterator(); iter.hasNext();)
               {
                  EObject element = (EObject) iter.next();
                  if(element instanceof INodeSymbol)
                  {
                     rawSymbols.add(element);
                  }
               }
            }
         }
         // iterate over all symbols from copy
         // create MergerEntry pairs so we can use the same code
         if(!lanes.isEmpty())
         {
            for (int i = 0; i < lanes.size(); i++)
            {
               MergerEntry laneEntry = (MergerEntry) lanes.get(i);
               EObject copy = (EObject) laneEntry.getValue();

               LaneSymbol lane = (LaneSymbol) copy;
               for(Iterator iter = ((LaneSymbol) lane).getNodes().valueListIterator(); iter.hasNext();)
               {
                  EObject copySymbol = (EObject) iter.next();
                  EObject rawSymbol = null;
                  if(copySymbol instanceof INodeSymbol)
                  {
                     // now get raw from list and create pair
                     rawSymbol = CopyPasteUtil.getRawSymbolFromList(rawSymbols, (INodeSymbol) copySymbol);
                     if(rawSymbol != null)
                     {
                        MergerEntry symbolEntry = new MergerEntry(rawSymbol, copySymbol);
                        symbols.add(symbolEntry);
                     }
                  }
               }
            }
         }
      }

      // extra copier for transitions necessary when copy lanes
      collectConnections();
      transitionCopier.copyReferences();
   }

   // collect possible connections
   private void collectConnections()
   {
      List tmpConnections = new ArrayList();

      // later we will use equal to compare objects - so we need the element from the copy
      // !!! do not change this copy in the clipboard
      DiagramType sourceDiagramCopy = (DiagramType) CopyPasteUtil.getSameModelElement(sourceDiagram, storage.getOriginalModelCopy(), null);
      if(sourceDiagramCopy == null)
      {
         return;
      }

      List pools = ((DiagramType) sourceDiagramCopy).getPoolSymbols();
      for(int i = 0; i < pools.size(); i++)
      {
         PoolSymbol pool = (PoolSymbol) pools.get(i);
         for(Iterator iter = ((PoolSymbol) pool).getConnections().valueListIterator(); iter.hasNext();)
         {
            EObject element = (EObject) iter.next();
            EObject copy = transitionCopier.copy(element);
            MergerEntry connectionEntry = new MergerEntry(element, copy);
            if(element instanceof IConnectionSymbol)
            {
               INodeSymbol rawSourceSymbol = ((IConnectionSymbol) element).getSourceNode();
               INodeSymbol rawTargetSymbol = ((IConnectionSymbol) element).getTargetNode();
               INodeSymbol sourceSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawSourceSymbol);
               INodeSymbol targetSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawTargetSymbol);

               if(sourceSymbol != null && targetSymbol != null)
               {
                  connections.add(connectionEntry);
                  if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
                  {
                     if(element instanceof TransitionConnectionType)
                     {
                        TransitionType transition = ((TransitionConnectionType) element).getTransition();
                        if(!tmpConnections.contains(transition))
                        {
                           if(transition != null)
                           {
                              MergerEntry transitionEntry = new MergerEntry(transition, transitionCopier.copy(transition));
                              connectionObjects.add(transitionEntry);
                              tmpConnections.add(transition);
                           }
                        }
                     }
                     if(element instanceof GenericLinkConnectionType && !isSameModel)
                     {
                        EObject link = ((GenericLinkConnectionType) element).getLinkType();
                        if(!tmpConnections.contains(link))
                        {
                           MergerEntry linkEntry = new MergerEntry(link, transitionCopier.copy(link));
                           connectionObjects.add(linkEntry);
                           tmpConnections.add(link);
                        }
                     }
                  }
               }
            }
         }
         EList lanes = ((PoolSymbol) pool).getLanes();
         for(int l = 0; l < lanes.size(); l++)
         {
            LaneSymbol lane = (LaneSymbol) lanes.get(l);
            for(Iterator iter = ((LaneSymbol) lane).getConnections().valueListIterator(); iter.hasNext();)
            {
               EObject element = (EObject) iter.next();
               EObject copy = transitionCopier.copy(element);
               MergerEntry connectionEntry = new MergerEntry(element, copy);
               if(element instanceof IConnectionSymbol)
               {
                  INodeSymbol rawSourceSymbol = ((IConnectionSymbol) element).getSourceNode();
                  INodeSymbol rawTargetSymbol = ((IConnectionSymbol) element).getTargetNode();
                  INodeSymbol sourceSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawSourceSymbol);
                  INodeSymbol targetSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawTargetSymbol);

                  if(sourceSymbol != null && targetSymbol != null)
                  {
                     connections.add(connectionEntry);
                     if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
                     {
                        if(element instanceof TransitionConnectionType)
                        {
                           TransitionType transition = ((TransitionConnectionType) element).getTransition();
                           if(!tmpConnections.contains(transition))
                           {
                              if(transition != null)
                              {
                                 if(transition != null)
                                 {
                                    MergerEntry transitionEntry = new MergerEntry(transition, transitionCopier.copy(transition));
                                    connectionObjects.add(transitionEntry);
                                    tmpConnections.add(transition);
                                 }
                              }
                           }
                        }
                        if(element instanceof GenericLinkConnectionType && !isSameModel)
                        {
                           EObject link = ((GenericLinkConnectionType) element).getLinkType();
                           if(!tmpConnections.contains(link))
                           {
                              MergerEntry linkEntry = new MergerEntry(link, transitionCopier.copy(link));
                              connectionObjects.add(linkEntry);
                              tmpConnections.add(link);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      for(Iterator iter = ((DiagramType) sourceDiagramCopy).getConnections().valueListIterator(); iter.hasNext();)
      {
         EObject element = (EObject) iter.next();
         EObject copy = transitionCopier.copy(element);
         MergerEntry connectionEntry = new MergerEntry(element, copy);
         if(element instanceof IConnectionSymbol)
         {
            INodeSymbol rawSourceSymbol = ((IConnectionSymbol) element).getSourceNode();
            INodeSymbol rawTargetSymbol = ((IConnectionSymbol) element).getTargetNode();
            INodeSymbol sourceSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawSourceSymbol);
            INodeSymbol targetSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawTargetSymbol);

            if(sourceSymbol != null && targetSymbol != null)
            {
               connections.add(connectionEntry);
               if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
               {
                  if(element instanceof TransitionConnectionType)
                  {
                     TransitionType transition = ((TransitionConnectionType) element).getTransition();
                     if(!tmpConnections.contains(transition))
                     {
                        if(transition != null)
                        {
                           MergerEntry transitionEntry = new MergerEntry(transition, transitionCopier.copy(transition));
                           connectionObjects.add(transitionEntry);
                           tmpConnections.add(transition);
                        }
                     }
                  }
                  if(element instanceof GenericLinkConnectionType && !isSameModel)
                  {
                     EObject link = ((GenericLinkConnectionType) element).getLinkType();
                     if(!tmpConnections.contains(link))
                     {
                        MergerEntry linkEntry = new MergerEntry(link, transitionCopier.copy(link));
                        connectionObjects.add(linkEntry);
                        tmpConnections.add(link);
                     }
                  }
               }
            }
         }
      }
   }

   public void merge()
   {
      if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
      {
         if(!mergeGlobal())
         {
            return;
         }
      }
      for(int i = 0; i < connectionObjects.size(); i++)
      {
         Map.Entry entry =  (Entry) connectionObjects.get(i);
         if(!mergeElement(entry))
         {
            modelChanged = false;
            return;
         }
      }
      for(int i = 0; i < connectionObjects.size(); i++)
      {
         Map.Entry entry =  (Entry) connectionObjects.get(i);
         addModelElement(entry, false);
      }
      // create the symbols
      for(int i = 0; i < symbols.size();i++)
      {
         Map.Entry entry =  (Entry) symbols.get(i);
         createSymbol(entry, copyLanes);
      }

      // create the missing connections
      createConnections();
      isDiagram = false;
      // here we have the subprocesses
      Iterator it = processElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         if(!mergeElement(entry))
         {
            modelChanged = false;
            return;
         }
      }
      it = processElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         addModelElement(entry, false);
      }
      // remove join and split if necessary
      if(pasteMode == CREATE_ALL || pasteMode == CREATE_SUBPROCESS)
      {
         for(int i = 0; i < checkActivities.size(); i++)
         {
            boolean hasSplitSymbol = false;
            boolean hasJoinSymbol = false;

            ActivitySymbolType symbol = (ActivitySymbolType) checkActivities.get(i);
            EList gatewaySymbols = symbol.getGatewaySymbols();
            for(int g = 0; g < gatewaySymbols.size(); g++)
            {
               GatewaySymbol gatewaySymbol = (GatewaySymbol) gatewaySymbols.get(g);
               FlowControlType type = gatewaySymbol.getFlowKind();
               if(type.equals(FlowControlType.SPLIT_LITERAL))
               {
                  hasSplitSymbol = true;
               }
               if(type.equals(FlowControlType.JOIN_LITERAL))
               {
                  hasJoinSymbol = true;
               }
            }
            ActivityType activity = (ActivityType) symbol.getActivity();
            JoinSplitType join = activity.getJoin();
            if(join != null && !hasJoinSymbol)
            {
               activity.setJoin(null);
            }
            JoinSplitType split = activity.getSplit();
            if(split != null && !hasSplitSymbol)
            {
               activity.setSplit(null);
            }
         }
      }

      mergeConfigurationVariables();
   }

   private void createConnections()
   {
      Dimension coordinatesDifference = null;


      for(int i = 0; i < connections.size(); i++)
      {
         Map.Entry entry =  (Entry) connections.get(i);
         EObject raw = (EObject) entry.getKey();
         EObject copy = (EObject) entry.getValue();
         TransitionType transitionType = null;

         if(raw instanceof TransitionConnectionType)
         {
            RoutingType routingType = ((TransitionConnectionType) raw).getRouting();
            EList rawCoordinates = null;
            if(routingType.equals(RoutingType.EXPLICIT_LITERAL))
            {
               rawCoordinates = ((TransitionConnectionType) raw).getCoordinates();
            }

            TransitionType transition = ((TransitionConnectionType) raw).getTransition();
            if(transition != null)
            {
               IIdentifiableModelElement modelElement = getTargetModelElement(targetProcess, (IIdentifiableModelElement) transition);
               if(modelElement != null)
               {
                  transitionType = (TransitionType) modelElement;
                  ((TransitionConnectionType) copy).setTransition((TransitionType) modelElement);
               }
            }
            if(rawCoordinates != null && coordinatesDifference != null)
            {
               ((TransitionConnectionType) copy).getCoordinates().clear();
               // create new Coordinates and get new positions
               for(int c = 0; c < rawCoordinates.size(); c++)
               {
                  Coordinates rawCoordiante = (Coordinates) rawCoordinates.get(c);
                  Coordinates copyCoordinate = CarnotWorkflowModelPackage.eINSTANCE.getCarnotWorkflowModelFactory().createCoordinates();
                  copyCoordinate.setXPos(rawCoordiante.getXPos() - coordinatesDifference.width);
                  copyCoordinate.setYPos(rawCoordiante.getYPos() - coordinatesDifference.height);
                  ((TransitionConnectionType) copy).getCoordinates().add(copyCoordinate);
               }
            }
         }
         if(raw instanceof GenericLinkConnectionType)
         {
            LinkTypeType link = ((GenericLinkConnectionType) raw).getLinkType();
            if(link != null)
            {
               IIdentifiableModelElement modelElement = getTargetModelElement(targetProcess, (IIdentifiableModelElement) link);
               if(modelElement != null)
               {
                  ((GenericLinkConnectionType) copy).setLinkType((LinkTypeType) modelElement);
               }
            }
         }
         if(raw instanceof IConnectionSymbol)
         {
            INodeSymbol rawSourceSymbol = ((IConnectionSymbol) raw).getSourceNode();
            INodeSymbol rawTargetSymbol = ((IConnectionSymbol) raw).getTargetNode();
            INodeSymbol sourceSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawSourceSymbol);
            INodeSymbol targetSymbol = (INodeSymbol) MergerUtil.getElementFromList(symbols, rawTargetSymbol);

            if(sourceSymbol != null && targetSymbol != null)
            {
               ((IConnectionSymbol) copy).setSourceNode(sourceSymbol);
               ((IConnectionSymbol) copy).setTargetNode(targetSymbol);
            }

            // pool is the container in this case
            ISymbolContainer container = (ISymbolContainer) storage.getTargetObject();
            EStructuralFeature feature = raw.eContainingFeature();
            if(feature != null)
            {
               List list = (List) container.eGet(feature);
               // add element
               list.add(copy);
            }
            if(transitionType != null && sourceSymbol != null && targetSymbol != null)
            {
               // set transition activities
               ActivitySymbolType sourceActivitySymbol = null;
               ActivitySymbolType targetActivitySymbol = null;

               if(sourceSymbol instanceof GatewaySymbol)
               {
                  sourceActivitySymbol = ((GatewaySymbol) sourceSymbol).getActivitySymbol();
               }
               else if(sourceSymbol instanceof ActivitySymbolType)
               {
                  sourceActivitySymbol = (ActivitySymbolType) sourceSymbol;
               }
               if(targetSymbol instanceof GatewaySymbol)
               {
                  targetActivitySymbol = ((GatewaySymbol) targetSymbol).getActivitySymbol();
               }
               else if(targetSymbol instanceof ActivitySymbolType)
               {
                  targetActivitySymbol = (ActivitySymbolType) targetSymbol;
               }

               if(sourceActivitySymbol != null && targetActivitySymbol != null)
               {
                  ActivityType fromActivity = ((ActivitySymbolType) sourceActivitySymbol).getActivity();
                  ActivityType toActivity = ((ActivitySymbolType) targetActivitySymbol).getActivity();

                  transitionType.setFrom(fromActivity);
                  transitionType.setTo(toActivity);
               }
            }
         }
      }
   }

   // open name/id dialog if necessary, store all leave lanes (if raw has nodes)
   private boolean iterateLanes(Map hierarchies, boolean create)
   {
      LaneSymbol storeparentLane = null;
      Iterator it = hierarchies.entrySet().iterator();
      while(it.hasNext())
      {
         Map.Entry entry = (Entry) it.next();
         // copy and raw
         MergerEntry symbolEntry = (MergerEntry) entry.getKey();
         EObject raw = (EObject) symbolEntry.getKey();
         EObject copy = (EObject) symbolEntry.getValue();

         List checkHierarchy = (List) entry.getValue();
         storeparentLane = (LaneSymbol) parentLane;
         if(!create)
         {
            if(checkElementInModel(symbolEntry))
            {
               // the element copy will change id and name here
               if(!openDialog(symbolEntry))
               {
                  return false;
               }
               else
               {
                  changedCache.put(symbolEntry.getKey(), symbolEntry.getValue());
               }
            }
         }
         else
         {
            ((LaneSymbol) copy).getChildLanes().clear();
            createSymbol(symbolEntry, false);
         }
         if(!((LaneSymbol) raw).getNodes().isEmpty() && !create)
         {
            lanes.add(symbolEntry);
         }
         for(int i = 0; i < checkHierarchy.size(); i++)
         {
            parentLane = (ISwimlaneSymbol) copy;
            Object object = checkHierarchy.get(i);
            if(object instanceof Map)
            {
               Map childHierarchy = (Map) object;
               if(!iterateLanes(childHierarchy, create))
               {
                  return false;
               }
            }
            else
            {
               symbolEntry = (MergerEntry) object;
               raw = (EObject) symbolEntry.getKey();
               copy = (EObject) symbolEntry.getValue();
               if(!create)
               {
                  if(checkElementInModel(symbolEntry))
                  {
                     // the element copy will change id and name here
                     if(!openDialog(symbolEntry))
                     {
                        return false;
                     }
                     else
                     {
                        changedCache.put(symbolEntry.getKey(), symbolEntry.getValue());
                     }
                  }
               }
               else
               {
                  ((LaneSymbol) copy).getChildLanes().clear();
                  createSymbol(symbolEntry, false);
               }
               if(!((LaneSymbol) raw).getNodes().isEmpty() && !create)
               {
                  lanes.add(symbolEntry);
               }
            }
         }
         parentLane = storeparentLane;
      }
      if(!create)
      {
         parentLane = null;
      }
      return true;
   }

   // get Difference of absolute location in target for explicit routing
   private Dimension getLocationDifference()
   {

      return null;
   }

   // symbols must be created after all elements have been created (?)
   // and new model elements must be assigned after creation
   // find the right place, we need the old mouse location, oid must be changed!
   protected void createSymbol(Entry symbolEntry, boolean onlyChange)
   {

   }
}