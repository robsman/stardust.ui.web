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
package org.eclipse.stardust.ui.web.viewscommon.beans;

import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;



/**
 * Backing bean for a workarea part of an ADF-based portal.
 * 
 * @author mgille
 *
 */
public abstract class PortalBackingBean
{
	//private transient JSFProcessExecutionPortal portal;
    
   // public final static String GLOBAL_PORTAL_MESSAGE_ID = "globalPortalMessage";
	
	// TODO Put in session
	private static Exception lastException;
	
	public static Object getManagedBean(String beanId)
	{
	   return ManagedBeanUtils.getManagedBean(beanId);
	}
	
    protected PortalBackingBean()
    {
       
    }
    
   /* protected Object findRequestBean(String beanId)
    {
       return ManagedBeanUtils.findRequestBean(beanId);
    }
	
    protected Object findApplicationBean(String beanId)
    {
       return ManagedBeanUtils.findApplicationBean(beanId);
    }*/
	
//    protected JSFProcessExecutionPortal getPortal() throws PortalException
//    {
//       return getPortal(true);
//    }
    
/*	protected JSFProcessExecutionPortal getPortal(boolean showError) throws PortalException
	{
       if(portal == null)
       {
          try
          {
             portal = getSessionPortal();
          }
          catch(PortalException e)
          {
             if(showError)
             {
                ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
             }
             throw e;
          }
       }
       return portal;
	}
    
    public static JSFProcessExecutionPortal getSessionPortal() throws PortalException
    {
       SessionContext ctx = SessionContext.findSessionContext();
       JSFProcessExecutionPortal portal = (JSFProcessExecutionPortal)ctx.lookup(ProcessPortalConstants.WORKFLOW_FACADE);
       if(portal == null)
       {
          throw new PortalException(PortalErrorClass.SESSION_EXPIRED);
       }
       return portal;
    }

	protected void logException(Exception exception)
	{
		lastException = exception;
	}
	
	protected Exception getLastException()
	{
		return lastException;
	} */   
}
