/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Stephan.Born (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.upgrade.jobs;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.defaults.DefaultElementsInitializer;
import org.eclipse.stardust.model.xpdl.builder.spi.ModelInitializer;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.DiagramUtil;
import org.eclipse.stardust.ui.web.modeler.upgrade.UpgradeJob;

public class M3_1_0from1_0_0UpgradeJob extends UpgradeJob
{
   private static final Logger trace = LogManager.getLogger(M3_1_0from1_0_0UpgradeJob.class);

   /**
    * the upgrade will change version number to this version
    */
   private static final Version VERSION = new Version(3, 1, 0);

   public M3_1_0from1_0_0UpgradeJob()
   {
      super();
   }

   protected Logger getLogger()
   {
      return trace;
   }

   public Version getVersion()
   {
      return VERSION;
   }

   /**
    * on upgrade, check for each single element if a change is needed, to prevent duplicates
    */
   public ModelType upgradeModel(ModelType model)
   {
      ModelInitializer initializer = new DefaultElementsInitializer();
      initializer.initializeModel(model);
      checkModelUUIDs(model);
      checkConnections(model);
      return model;
   }

   private void checkConnections(ModelType model)
   {
      for(ProcessDefinitionType process : model.getProcessDefinition())
      {
         for(DiagramType diagram : process.getDiagram())
         {
            PoolSymbol defaultPool = DiagramUtil.getDefaultPool(diagram);
            if(defaultPool != null)
            {
               List<IConnectionSymbol> elements = CollectionUtils.newArrayList();
               for(LaneSymbol lane : defaultPool.getLanes())
               {
                  if(!lane.getConnections().isEmpty())
                  {
                     for (Iterator i = lane.getConnections().valueListIterator(); i.hasNext();)
                     {
                        IConnectionSymbol connection = (IConnectionSymbol) i.next();
                        elements.add(connection);
                     }
                  }

                  if(elements.size() > 0)
                  {
                     for(IConnectionSymbol element : elements)
                     {
                        EStructuralFeature feature = element.eContainingFeature();
                        if(feature != null)
                        {
                           List list = (List) defaultPool.eGet(feature);
                           list.add(element);
                        }
                     }

                     lane.getConnections().removeAll(elements);
                  }
               }
            }
         }
      }
   }

   private void checkModelUUIDs(ModelType model)
   {
      if (AttributeUtil.getAttribute(model, PredefinedConstants.MODEL_ELEMENT_UUID) == null)
      {
         UUID modelUUID = UUID.randomUUID();
         AttributeUtil.setAttribute(model, PredefinedConstants.MODEL_ELEMENT_UUID, modelUUID.toString());
      }
   }
}