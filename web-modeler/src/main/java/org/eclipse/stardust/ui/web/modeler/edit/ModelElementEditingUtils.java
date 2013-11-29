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

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;

/**
 * @author Shrikant.Gangal
 *
 */
public class ModelElementEditingUtils
{
   public static void setPeriodAttribute(IExtensibleElement element, String stringValue, String unit)
   {
      short Y = 0;
      short M = 0;
      short D = 0;
      short h = 0;
      short m = 0;
      short s = 0;
      
      short value = 0;
      try
      {
         value = Short.parseShort(stringValue);
      }
      catch (NumberFormatException ex)
      {
         // TODO: log ?
      }
      
      if (!StringUtils.isEmpty(unit))
      {
         switch (unit.charAt(0))
         {
         case 'Y': Y = value; break;
         case 'M': M = value; break;
         case 'D': D = value; break;
         case 'h': h = value; break;
         case 'm': m = value; break;
         case 's': s = value; break;
         }
      }
      // TODO: else log ?
         
      Period period = new Period(Y, M, D, h, m, s);
      AttributeUtil.setAttribute(element, "carnot:engine:period",
            Period.class.getSimpleName(), period.toString());
   }

   /**
    * @param activity
    * @param symbol oids
    */
   public static void deleteEventSymbols(ActivityType activity, LaneSymbol parentLaneSymbol)
   {
      List<IntermediateEventSymbol> symbols = parentLaneSymbol.getIntermediateEventSymbols();
      for (long eventSymbolOid : EventMarshallingUtils.resolveHostedEvents(activity))
      {
         IModelElement eventSymbol = ModelUtils.findElementByOid(symbols, eventSymbolOid);
         if (eventSymbol instanceof IntermediateEventSymbol)
         {
            deleteSymbol((IntermediateEventSymbol) eventSymbol);
         }
      }
   }
   
   /**
    * @param symbol
    */
   public static void deleteTransitionConnections(IFlowObjectSymbol symbol)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(symbol);

      // delete transition
      deleteTransitions(symbol, processDefinition, symbol.getInTransitions());
      deleteTransitions(symbol, processDefinition, symbol.getOutTransitions());
      
      // delete connection symbol
      deleteConnectionSymbols(symbol.getInTransitions());
      deleteConnectionSymbols(symbol.getOutTransitions());
   }

   /**
    * @param symbol
    * @param processDefinition
    * @param transitions
    */
   private static void deleteTransitions(IFlowObjectSymbol symbol,
         ProcessDefinitionType processDefinition,
         EList<TransitionConnectionType> transitions)
   {
      for (TransitionConnectionType transition : transitions)
      {
         TransitionType transitionType = transition.getTransition();
         if (processDefinition.getTransition() != null)
         {
            processDefinition.getTransition().remove(transitionType);
         }

         if (transitionType != null)
         {
            if (transitionType.getFrom() != null
                  && transitionType.getFrom().getOutTransitions() != null)
            {
               transitionType.getFrom().getOutTransitions().remove(transitionType);
            }
            if (transitionType.getTo() != null
                  && transitionType.getTo().getInTransitions() != null)
            {
               transitionType.getTo().getInTransitions().remove(transitionType);
            }
         }
      }
   }
   
   
   /**
    * @param processDefinition
    * @param dataConnIter
    */
   public static void deleteDataMappingConnection(List<DataMappingConnectionType> dataConnIter)
   {
      for (int i = dataConnIter.size() - 1; i >= 0; i--)
      {
         deleteDataMappingConnection(dataConnIter.get(i));
      }
   }

   /**
    * @param processDefinition
    * @param dataMappingConnection
    */
   public static void deleteDataMappingConnection(DataMappingConnectionType dataMappingConnection)
   {
      List<DataMappingType> mappings = CollectionUtils.newArrayList();
      ActivitySymbolType activitySymbol = dataMappingConnection.getActivitySymbol();
      DataSymbolType dataSymbol = dataMappingConnection.getDataSymbol();
      if (activitySymbol != null && dataSymbol != null)
      {
         ActivityType activity = activitySymbol.getActivity();
         DataType data = dataSymbol.getData();
         for (DataMappingType mapping : activity.getDataMapping())
         {
            // (fh) ??? why not comparing data with data ?
            if (mapping.getData().getId()
                  .equals(data.getId()))
            {
               mappings.add(mapping);
            }
         }
      }
      
      for (DataMappingType mapping : mappings)
      {
         EcoreUtil.delete(mapping);
      }
      
      EcoreUtil.delete(dataMappingConnection);
   }

   public static void deleteConnectionSymbols(List<? extends IConnectionSymbol> connections)
   {
      for (int i = connections.size()- 1; i >= 0; i--)
      {
         EcoreUtil.delete(connections.get(i));
      }
   }

   public static void deleteIdentifiables(List<? extends IIdentifiableModelElement> identifiables)
   {
      for (int i = identifiables.size() - 1; i >= 0; i--)
      {
         deleteIdentifiable(identifiables.get(i));
      }
   }

   public static void deleteIdentifiable(IIdentifiableModelElement identifiable)
   {
      if (identifiable instanceof TransitionType)
      {
         // (fh) transitions have connection symbols
         deleteConnectionSymbols(((TransitionType) identifiable).getTransitionConnections());
      }
      else
      {
         deleteSymbols(identifiable);
      }
      EcoreUtil.delete(identifiable);
   }

   public static void deleteSymbols(IIdentifiableModelElement identifiable)
   {
      List<INodeSymbol> symbols = identifiable.getSymbols();
      for (int i = symbols.size() - 1; i >= 0; i--)
      {
         deleteSymbol(symbols.get(i));
      }
   }

   public static void deleteSymbol(INodeSymbol symbol)
   {
      deleteConnectionSymbols(symbol);
      EcoreUtil.delete(symbol);
   }

   @SuppressWarnings("unchecked")
   public static void deleteConnectionSymbols(INodeSymbol symbol)
   {
      List<EStructuralFeature> connectionFeatures = CollectionUtils.copyList(
            symbol.getInConnectionFeatures());
      connectionFeatures.addAll(symbol.getOutConnectionFeatures());
      for (EStructuralFeature feature : connectionFeatures)
      {
         Object connection = symbol.eGet(feature);
         if (connection instanceof IConnectionSymbol)
         {
            EcoreUtil.delete((IConnectionSymbol) connection);
         }
         else if (connection instanceof List)
         {
            deleteConnectionSymbols((List<IConnectionSymbol>) connection);
         }
      }
      deleteConnectionSymbols(symbol.getInLinks());
      deleteConnectionSymbols(symbol.getOutLinks());
   }
}
