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

import org.eclipse.stardust.ui.client.common.ClientContext;
import org.eclipse.stardust.ui.client.model.Client;
import org.eclipse.stardust.ui.client.model.Models;


/**
 * @author herinean
 */
public class ClientImpl extends Client
{
   private ClientContext context;

   private ModelsImpl models;

   public ClientImpl(ClientContext context)
   {
      this.context = context == null ? ClientContext.getClientContext() : context;

      models = new ModelsImpl(this);

   }

   public Models getModels()
   {
      return models;
   }

   public ClientContext getContext()
   {
      return context;
   }
   
   @Override
   public void update()
   {
      //nothing to update
   }

   public void close()
   {
      if ( !isClosed())
      {
         super.close();
         context.close();
         context = null;
         // models.close();
         models = null;
      }
   }
}
