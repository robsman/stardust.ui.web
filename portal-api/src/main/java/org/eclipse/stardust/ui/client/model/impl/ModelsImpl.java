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
package org.eclipse.stardust.ui.client.model.impl;

import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.client.event.StatusEvent;
import org.eclipse.stardust.ui.client.model.Models;
import org.eclipse.stardust.ui.client.model.ProcessFilters;



public class ModelsImpl extends AbstractWorkflowCollection<DeployedModelDescription> implements Models
{
   private static HashMap<Short, DeployedModelDescription[]> descriptions = new HashMap<Short, DeployedModelDescription[]>();
   private static HashMap<Short, DeployedModel[]> models = new HashMap<Short, DeployedModel[]>();
   private static HashMap<Short, ProcessFiltersImpl> filters = new HashMap<Short, ProcessFiltersImpl>();
   
   private ClientImpl client;
   private Short partition;
   
   public ModelsImpl(ClientImpl client)
   {
      assert client != null;
      assert !client.isClosed();
      
      this.client = client;
   }

   public Model getModel(int modelOid)
   {
      assert !client.isClosed();
      
      if (items == null)
      {
         update();
      }
      if (items != null)
      {
         for (int i = 0; i < items.length; i++)
         {
            if (items[i].getModelOID() == modelOid)
            {
               return fetchModel(items[i], i);
            }
         }
      }
      return null;
   }


   public Model getActiveModel()
   {
      assert !client.isClosed();
      
      if (items == null)
      {
         update();
      }
      if (items != null)
      {
         for (int i = 0; i < items.length; i++)
         {
            if (items[i].isActive())
            {
               return fetchModel(items[i], i);
            }
         }
      }
      return null;
   }

   private Model fetchModel(DeployedModelDescription description, int index)
   {
      Model[] theModels = models.get(getPartition());
      if (theModels[index] == null)
      {
         // TODO notify model event observers
         int oid = description.getModelOID();
         try
         {
        	//FIXME clientContext pushMessage()
//            client.getContext().pushMessage("Reading model [" + oid + "] : " + description.getName());
            theModels[index] = client.getContext().getServiceFactory().getQueryService().getModel(oid, false);
         }
         finally
         {
        	//FIXME clientContext popMessage()
//            client.getContext().popMessage();
         }
      }
      return theModels[index];
   }

   private DeployedModelDescription[] getDescriptions()
   {
      synchronized (ModelsImpl.descriptions)
      {
         DeployedModelDescription[] descriptions = ModelsImpl.descriptions.get(getPartition());
         if (descriptions == null)
         {
            try
            {
            	//FIXME clientContext pushMessage()
//               client.getContext().pushMessage("Fetching models information...");
               @SuppressWarnings("unchecked")
               List<DeployedModelDescription> deployedModels = client.getContext().getServiceFactory().getQueryService().getModels(DeployedModelQuery.findAll());
               descriptions = deployedModels.toArray(new DeployedModelDescription[deployedModels.size()]);
            }
            finally
            {
            	//FIXME clientContext popMessage()
//               client.getContext().popMessage();
            }
            ModelsImpl.descriptions.put(getPartition(), descriptions);
            ModelsImpl.models.put(getPartition(), new DeployedModel[descriptions.length]);
         }
         return descriptions;
      }
   }

   @Override
   public void update()
   {
      items = getDescriptions();
      observers.notifyObservers(StatusEvent.updated(this));
   }

   public ProcessFilters getProcessFilters()
   {
      synchronized (ModelsImpl.filters)
      {
         ProcessFiltersImpl filter = ModelsImpl.filters.get(getPartition());
         if (filter == null)
         {
            filter = new ProcessFiltersImpl(client);
            ModelsImpl.filters.put(getPartition(), filter);
         }
         return filter;
      }
   }

   private short getPartition()
   {
      if (partition == null)
      {
         partition = client.getContext().getUser().getRealm().getPartitionOid();
      }
      return partition;
   }
}
