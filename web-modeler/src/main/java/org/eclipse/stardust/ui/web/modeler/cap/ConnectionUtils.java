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
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EObjectEList;
import org.eclipse.emf.ecore.util.EcoreEList;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.GatewaySymbol;
import org.eclipse.stardust.model.xpdl.carnot.IConnectionSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.merge.MergeUtils;


public class ConnectionUtils
{
   public static List getTransitions(List activities)
   {
      List tmpTransitions = new ArrayList();
      List transitions = new ArrayList();
      for (Iterator it = activities.iterator(); it.hasNext();)
      {
         ActivityType activity = (ActivityType) it.next();   
         tmpTransitions.addAll(activity.getInTransitions());
         tmpTransitions.addAll(activity.getOutTransitions());
      }
      
      if(!tmpTransitions.isEmpty())
      {
         for(Iterator iter = tmpTransitions.iterator(); iter.hasNext();)
         {
            TransitionType transition = (TransitionType) iter.next();            
            if(activities.contains(transition.getFrom())
                  || activities.contains(transition.getTo()))
            {
               if(!transitions.contains(transition))
               {
                  transitions.add(transition);
               }               
            }            
         }
      }
      
      return transitions;
   }
   
   // delete transitions expect the ones in the list
   public static void deleteTransitions(ActivityType activity, List transitionList)
   {
      List inTransitions = new ArrayList();
      inTransitions.addAll(activity.getInTransitions());
      if(!inTransitions.isEmpty())
      {
         for(Iterator iter = inTransitions.iterator(); iter.hasNext();)
         {
            TransitionType transition = (TransitionType) iter.next();            
            if(transitionList == null || !transitionList.contains(transition))
            {
               transition.setFrom(null);
               transition.setTo(null);
               MergeUtils.deleteElement(transition, null);
            }
         }
      }      
      List outTransitions = new ArrayList();
      outTransitions.addAll(activity.getOutTransitions());
      if(!outTransitions.isEmpty())
      {
         for(Iterator iter = outTransitions.iterator(); iter.hasNext();)
         {
            TransitionType transition = (TransitionType) iter.next();
            if(transitionList == null || !transitionList.contains(transition))
            {
               transition.setFrom(null);
               transition.setTo(null);
               MergeUtils.deleteElement(transition, null);
            }
         }
      }            
   } 
   
   public static void deleteConnectionsFromSymbol(INodeSymbol symbol, List connectionList)
   {
      List connectionFeatures = new ArrayList();
      connectionFeatures.addAll(symbol.getInConnectionFeatures());
      connectionFeatures.addAll(symbol.getOutConnectionFeatures());
      for (Iterator iterator = connectionFeatures.iterator(); iterator.hasNext();)
      {
         EStructuralFeature feature = (EStructuralFeature) iterator.next();
         Object object = symbol.eGet(feature);
         if(object != null)
         {       
            removeConnection(object, connectionList);
         }
      }
      List links = new ArrayList();
      if(!symbol.getInLinks().isEmpty())
      {
         links.add(symbol.getInLinks());
      }
      if(!symbol.getOutLinks().isEmpty())
      {
         links.add(symbol.getOutLinks());
      }
      for (Iterator iterator = links.iterator(); iterator.hasNext();)
      {
         Object object = iterator.next();
         if(object != null)
         {       
            removeConnection(object, connectionList);
         }         
      }
      
      symbol.getInLinks().clear();
      symbol.getOutLinks().clear();
      symbol.getReferingFromConnections().clear();
      symbol.getReferingToConnections().clear();       
   }

   private static void removeConnection(Object object, List connectionList)
   {
      if(object instanceof EObjectEList)
      {
         List data = ((EcoreEList) object).basicList();
         for (Iterator it = data.iterator(); it.hasNext();)
         {
            EObject eObject = (EObject) it.next();
            if(connectionList == null || !connectionList.contains(eObject))
            {
               if(eObject instanceof IConnectionSymbol)
               {
                  ((IConnectionSymbol) eObject).setTargetNode(null);
                  ((IConnectionSymbol) eObject).setSourceNode(null);                     
                  MergeUtils.deleteElement((IModelElement) eObject, null);
               }
            }
         }
      }
      else if(object instanceof EObject)
      {
         if(connectionList == null || !connectionList.contains(object))
         {
            if(object instanceof IConnectionSymbol)
            {
               ((IConnectionSymbol) object).setTargetNode(null);
               ((IConnectionSymbol) object).setSourceNode(null);                     
               MergeUtils.deleteElement((IModelElement) object, null);
            }
         }
      }
   }
   
   // we must check that we have only one connection
   public static Object getInConnectionFromSymbol(INodeSymbol symbol, TransitionType transition)
   {
      IConnectionSymbol connection = null;
      
      List connectionFeatures = new ArrayList();
      connectionFeatures.addAll(symbol.getInConnectionFeatures());
      for (Iterator iterator = connectionFeatures.iterator(); iterator.hasNext();)
      {
         EStructuralFeature feature = (EStructuralFeature) iterator.next();
         Object object = symbol.eGet(feature);
         if(object != null)
         {                   
            if(object instanceof EObjectEList)
            {
               List data = ((EcoreEList) object).basicList();
               for (Iterator it = data.iterator(); it.hasNext();)
               {
                  EObject eObject = (EObject) it.next();
                  if(eObject instanceof TransitionConnectionType)
                  {
                     TransitionType inTransition = ((TransitionConnectionType) eObject).getTransition();
                     if(transition == null && inTransition == null
                           || transition != null && transition.equals(inTransition))
                     {                     
                        INodeSymbol source = ((IConnectionSymbol) eObject).getSourceNode();
                        if(transition == null && source instanceof AbstractEventSymbol
                              || transition != null && source instanceof ActivitySymbolType
                              || transition != null && source instanceof GatewaySymbol)
                        {
                           if(connection == null)
                           {
                              connection = (IConnectionSymbol) eObject;
                           }
                           else
                           {
                              return Boolean.FALSE;
                           }                                                
                        }
                     }
                  }
               }
            }
            else if(object instanceof EObject)
            {
               if(object instanceof TransitionConnectionType)
               {
                  TransitionType inTransition = ((TransitionConnectionType) object).getTransition();
                  if(transition == null && inTransition == null
                        || transition != null && transition.equals(inTransition))
                  {                     
                     INodeSymbol source = ((IConnectionSymbol) object).getSourceNode();
                     if(transition == null && source instanceof AbstractEventSymbol
                           || transition != null && source instanceof ActivitySymbolType
                           || transition != null && source instanceof GatewaySymbol)
                     {
                        if(connection == null)
                        {
                           connection = (IConnectionSymbol) object;
                        }
                        else
                        {
                           return Boolean.FALSE;
                        }                                                
                     }
                  }
               }
            }
         }
      }
      return connection;
   }
   
   // we must check that we have only one connection
   public static Object getOutConnectionFromSymbol(INodeSymbol symbol, TransitionType transition)
   {
      IConnectionSymbol connection = null;
      
      List connectionFeatures = new ArrayList();
      connectionFeatures.addAll(symbol.getOutConnectionFeatures());
      for (Iterator iterator = connectionFeatures.iterator(); iterator.hasNext();)
      {
         EStructuralFeature feature = (EStructuralFeature) iterator.next();
         Object object = symbol.eGet(feature);
         if(object != null)
         {                   
            if(object instanceof EObjectEList)
            {
               List data = ((EcoreEList) object).basicList();
               for (Iterator it = data.iterator(); it.hasNext();)
               {
                  EObject eObject = (EObject) it.next();
                  if(eObject instanceof TransitionConnectionType)
                  {
                     TransitionType inTransition = ((TransitionConnectionType) eObject).getTransition();
                     if(transition == null && inTransition == null
                           || transition != null && transition.equals(inTransition))
                     {                     
                        INodeSymbol target = ((IConnectionSymbol) eObject).getTargetNode();
                        if(transition == null && target instanceof AbstractEventSymbol
                              || transition != null && target instanceof ActivitySymbolType
                              || transition != null && target instanceof GatewaySymbol)
                        {
                           if(connection == null)
                           {
                              connection = (IConnectionSymbol) eObject;
                           }
                           else
                           {
                              return Boolean.FALSE;
                           }                                                
                        }
                     }
                  }
               }
            }
            else if(object instanceof EObject)
            {
               if(object instanceof TransitionConnectionType)
               {
                  TransitionType inTransition = ((TransitionConnectionType) object).getTransition();
                  if(transition == null && inTransition == null
                        || transition != null && transition.equals(inTransition))
                  {                     
                     INodeSymbol target = ((IConnectionSymbol) object).getTargetNode();
                     if(transition == null && target instanceof AbstractEventSymbol
                           || transition != null && target instanceof ActivitySymbolType
                           || transition != null && target instanceof GatewaySymbol)
                     {
                        if(connection == null)
                        {
                           connection = (IConnectionSymbol) object;
                        }
                        else
                        {
                           return Boolean.FALSE;
                        }                                                
                     }
                  }
               }
            }
         }
      }
      return connection;
   }   
}