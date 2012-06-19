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
package org.eclipse.stardust.ui.client.model;

import java.util.HashMap;

import org.eclipse.stardust.ui.client.common.ClientContext;
import org.eclipse.stardust.ui.client.model.impl.ClientImpl;


/**
 * TODO: MUST NOT BE A SINGLETON, multiple clients should run independently in memory.
 *       Current singleton implementation is required by the singleton implementation of ClientContext.
 *       Client and ClientContext must have a 1 to 1 relationship, so if there will be multiple
 *       instances of Client then multiple instances of ClientContext must exist too. 
 * 
 * @author herinean
 */
public abstract class Client
{
   private static Client instance;
   
   private boolean closed;

   private HashMap<Class<?>, HashMap<String, Object>> options = new HashMap<Class<?>, HashMap<String, Object>>();

   /**
    * Factory method to obtain a client.
    * 
    * Repeated calls to this method will return the same instance as long
    * as the instance is not closed.
    *  
    * After a client is closed, a subsequent call to this method will create
    * a new client.
    * 
    * @return the current active client.
    */
   public static Client getClient()
   {
      synchronized (Client.class)
      {
         if (instance == null || instance.closed)
         {
            instance = new ClientImpl(null);
         }
         return instance;
      }
   }
   
   public static Client getClient(ClientContext context)
   {
      return new ClientImpl(context);
   }
   
   /**
    * Gets the startable processes managed by this client.
    * 
    * This method asserts that the client is not closed.
    * 
    * @return the worklist of the user.
    */
//   public abstract StartableProcesses getStartableProcesses();

   /**
    * Gets the user worklist managed by this client.
    * 
    * This method asserts that the client is not closed.
    * 
    * @return the worklist of the user.
    */
//   public abstract Worklists getWorklists();
   
   public abstract Models getModels();
   
//   public abstract Users getUsers();
   
   /**
    * Gets the context associated to this client.
    * 
    * This method asserts that the client is not closed.
    * 
    * @return the current ClientContext
    */
   public abstract ClientContext getContext();

   /**
    * Forces an update of all objects managed by this client.
    * 
    * This method asserts that the client is not closed.
    */
   public abstract void update();

   /**
    * Close the client.
    * 
    * After the client is closed, no operations are possible any longer.
    * 
    * Close may be invoked on an already closed client, but it will have no effect.
    */
   public void close()
   {
      if (!closed)
      {
         synchronized (Client.class)
         {
            if (Client.instance == this)
            {
               Client.instance = null;
            }
            closed = true;
         }
      }
   }
   
   /**
    * Checks if the client is closed.
    * 
    * @return true if the client is closed.
    */
   public final boolean isClosed()
   {
      return closed;
   }
   
   public HashMap<String, Object> getOptions(Class<?> clazz)
   {
      HashMap<String, Object> result = options.get(clazz);
      if (result == null)
      {
         result = new HashMap<String, Object>();
         options.put(clazz, result);
      }
      return result;
   }
}
