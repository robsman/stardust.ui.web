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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.ui.web.modeler.cap.MergerUtil.MergerEntry;



public class LaneUtils
{
   // must be done recursive!, copy raw lane
   public static List createLaneHierarchyCopy(LaneSymbol symbol, Copier copier)
   {
      List allLanes = new ArrayList();
      Map entries = new HashMap();

      EList lanes = symbol.getChildLanes();
      // empty, no lanes
      if(lanes.isEmpty())
      {
         return allLanes;
      }
      for(int i = 0; i < lanes.size(); i++)
      {
         LaneSymbol laneSymbol = (LaneSymbol) lanes.get(i);
         MergerEntry symbolEntry = new MergerEntry(laneSymbol, copier.copy(laneSymbol));
         // check for child lanes, if no childlanes add as object else as hashmap
         EList childLanes = laneSymbol.getChildLanes();
         if(childLanes.isEmpty())
         {
            allLanes.add(symbolEntry);
         }
         else
         {
            List children = createLaneHierarchyCopy(laneSymbol, copier);
            entries.put(symbolEntry, children);
         }
      }
      if(!entries.isEmpty())
      {
         allLanes.add(entries);
      }
      return allLanes;
   }

   // must be done recursive!
   public static List createLaneHierarchy(LaneSymbol symbol)
   {
      List allLanes = new ArrayList();
      Map entries = new HashMap();

      EList lanes = symbol.getChildLanes();
      // empty, no lanes
      if(lanes.isEmpty())
      {
         return allLanes;
      }
      // if lane containes both lanes and nodes return null;
      if(!symbol.getNodes().isEmpty())
      {
         return null;
      }
      for(int i = 0; i < lanes.size(); i++)
      {
         LaneSymbol laneSymbol = (LaneSymbol) lanes.get(i);
         // check for child lanes, if no childlanes add as object else as hashmap
         EList childLanes = laneSymbol.getChildLanes();
         if(childLanes.isEmpty())
         {
            allLanes.add(laneSymbol);
         }
         else
         {
            List children = createLaneHierarchy(laneSymbol);
            entries.put(laneSymbol, children);
         }
      }
      if(!entries.isEmpty())
      {
         allLanes.add(entries);
      }
      return allLanes;
   }

   public static boolean containsLane(List checkHierarchy, LaneSymbol checkLane)
   {
      for(int i = 0; i < checkHierarchy.size(); i++)
      {
         Object child = checkHierarchy.get(i);
         if(child instanceof LaneSymbol)
         {
            if(child.equals(checkLane))
            {
               return true;
            }
         }
         else
         {
            Map children = (Map) child;
            Iterator it = children.entrySet().iterator();
            while(it.hasNext())
            {
               Map.Entry entry = (Entry) it.next();
               LaneSymbol entryLane = (LaneSymbol) entry.getKey();
               if(entryLane.equals(checkLane))
               {
                  return true;
               }
               List entryHierarchy = (List) entry.getValue();
               if(containsLane(entryHierarchy, checkLane))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }
}