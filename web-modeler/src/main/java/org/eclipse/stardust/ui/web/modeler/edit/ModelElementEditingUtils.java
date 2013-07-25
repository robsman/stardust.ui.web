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

package org.eclipse.stardust.ui.web.modeler.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;

/**
 * @author Shrikant.Gangal
 *
 */
public class ModelElementEditingUtils
{
   /**
    * @param processDefinition
    * @param symbol
    */
   // TODO: (fh) remove because it is incorrect
   public static void deleteTransitionConnectionsForSymbol(
         ProcessDefinitionType processDefinition, IFlowObjectSymbol symbol)
   {
      if (null != symbol.getInTransitions())
      {
         deleteTransitionConnection(processDefinition, symbol.getInTransitions()
               .iterator());
      }

      if (null != symbol.getOutTransitions())
      {
         deleteTransitionConnection(processDefinition, symbol.getOutTransitions()
               .iterator());
      }
   }

   /**
    * @param processDefinition
    * @param connIter
    */
   // TODO: (fh) remove because it is incorrect
   public static void deleteTransitionConnection(ProcessDefinitionType processDefinition,
         Iterator<TransitionConnectionType> connIter)
   {
      while (connIter.hasNext())
      {
         deleteTransitionConnection(processDefinition, connIter.next());
         connIter.remove();
      }
   }

   /**
    * @param processDefinition
    * @param transitionConnection
    */
   // TODO: (fh) remove because it is incorrect
   public static void deleteTransitionConnection(ProcessDefinitionType processDefinition,
         TransitionConnectionType transitionConnection)
   {
      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getTransitionConnection()
            .remove(transitionConnection);

      if (transitionConnection.getTransition() != null)
      {
         processDefinition.getTransition().remove(transitionConnection.getTransition());
      }
   }

   /**
    * @param processDefinition
    * @param dataConnIter
    */
   public static void deleteDataMappingConnection(
         ProcessDefinitionType processDefinition,
         Iterator<DataMappingConnectionType> dataConnIter)
   {
      while (dataConnIter.hasNext())
      {
         deleteDataMappingConnection(processDefinition, dataConnIter.next());
         dataConnIter.remove();
      }
   }

   /**
    * @param processDefinition
    * @param dataMappingConnection
    */
   public static void deleteDataMappingConnection(
         ProcessDefinitionType processDefinition,
         DataMappingConnectionType dataMappingConnection)
   {
      List<DataMappingType> dataMapping = CollectionUtils.newArrayList();
      if (null != dataMappingConnection.getActivitySymbol()) {
         for (DataMappingType dataMappingType : dataMappingConnection.getActivitySymbol()
               .getActivity()
               .getDataMapping())
         {
            if (dataMappingType.getData()
                  .getId()
                  .equals(dataMappingConnection.getDataSymbol().getData().getId()))
            {
               dataMapping.add(dataMappingType);
            }
         }
         dataMappingConnection.getActivitySymbol()
         .getActivity()
         .getDataMapping()
         .removeAll(dataMapping);
      }

      if (null != dataMappingConnection.getDataSymbol()) {
         dataMappingConnection.getDataSymbol()
         .getData()
         .getDataMappings()
         .removeAll(dataMapping);
      }

      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getDataMappingConnection()
            .remove(dataMappingConnection);
   }

   public static void deleteConnections(List<? extends IConnectionSymbol> connections)
   {
      for (IConnectionSymbol connection : connections)
      {
         EObject container = connection.eContainer();
         if (container instanceof ISymbolContainer)
         {
            // disconnect
            connection.setSourceNode(null);
            connection.setTargetNode(null);
            
            @SuppressWarnings("unchecked")
            Collection<? extends IConnectionSymbol> containingFeature =
               (Collection<? extends IConnectionSymbol>)
               ((ISymbolContainer) container).eGet(connection.eContainingFeature());
            containingFeature.remove(connection);
         }
      }
   }

   /**
    * @param originalTransitions
    */
   public static void deleteTransitions(List<TransitionType> originalTransitions)
   {
      List<TransitionType> transitions = new ArrayList<TransitionType>();
      for (TransitionType transitionType : originalTransitions)
      {
         transitions.add(transitionType);
      }
      
      for (TransitionType transition : transitions)
      {
         deleteConnections(transition.getTransitionConnections());
   
         EObject container = transition.eContainer();
         if (container instanceof ProcessDefinitionType)
         {
            // disconnect
            transition.setFrom(null);
            transition.setTo(null);
            ((ProcessDefinitionType) container).getTransition().remove(transition);
         }
      }
   }
}
