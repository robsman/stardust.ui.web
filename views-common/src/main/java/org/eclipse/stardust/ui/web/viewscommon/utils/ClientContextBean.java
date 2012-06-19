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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.client.common.ClientContext;
import org.eclipse.stardust.ui.client.model.Client;
import org.eclipse.stardust.ui.client.model.impl.ClientImpl;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.springframework.beans.factory.InitializingBean;


/**
 * Central ClientContext for the ICEfaces client
 * 
 * @author roland.stamm
 * 
 */
public class ClientContextBean implements InitializingBean
{
   public static final Logger trace = LogManager.getLogger(ClientContextBean.class);
   private Client client; 

   private ClientContext clientContext;

   /**
    * 
    */
   public ClientContextBean()
   {
   }

   /**
    * @throws Exception
    */
   public void afterPropertiesSet() throws Exception
   {
      client = createClient(this.clientContext);

      if (client != null)
      {
         // If client was created with new ClientImpl(null) no ClientContext was injected
         // by Spring. In this case the ClientImpl will locate the ClientContext by
         // itself.
         setClientContext(client.getContext());
      }
   }

   public static ClientContextBean getCurrentInstance()
   {
      return (ClientContextBean)  FacesUtils.getBeanFromContext("clientContextBean");
   }

   /**
    * @param event
    */
   public void logoutAction(ActionEvent event)
   {
      this.close();
   }

   /**
    * @param event
    */
   public void refreshAction(ActionEvent event)
   {
      this.refresh();
   }

   /**
    * 
    */
   private void close()
   {
      if (this.client != null)
      {
         client.close();
      }

    
      client = null;
      // TODO close clientContext if needed
   }

   /**
    * 
    */
   private Client createClient(ClientContext context)
   {
      Client client = null;
      try
      {
         client = new ClientImpl(context);
         client.update();
      }
      catch (Exception e)
      {
         this.close();
         trace.error(e);
         System.err.println("CLIENT CREATION FAILED: " + e.getMessage());
         FacesMessage msg = new FacesMessage("CLIENT CREATION FAILED: " + e.getMessage());
         msg.setSeverity(FacesMessage.SEVERITY_ERROR);
         FacesContext.getCurrentInstance().addMessage("", msg);
         return null;
      }

      return client;
   }

   /**
    * 
    */
   private void refresh()
   {
      // not needed anymore
      // client.getWorklists().update();
      // client.getStartableProcesses().update();
      // WorklistTableBean.getCurrentInstance().update();
   }

   public Client getClient()
   {
      return client;
   }

   public ClientContext getClientContext()
   {
      return clientContext;
   }

   public void setClientContext(ClientContext clientContext)
   {
      this.clientContext = clientContext;
   }

  

}
