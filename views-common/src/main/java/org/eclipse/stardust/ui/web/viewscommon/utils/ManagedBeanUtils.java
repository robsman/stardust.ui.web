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

import javax.faces.context.FacesContext;

/**
 * JSF managed bean utilities
 * 
 * @author rsauer
 * 
 */
public abstract class ManagedBeanUtils
{
   public static Object getManagedBean(String beanId)
   {
      return getManagedBean(FacesContext.getCurrentInstance(), beanId);
   }
   
   public static Object getManagedBean(FacesContext context, String beanId)
   {
      Object bean = context.getApplication().getVariableResolver().resolveVariable(
            context, beanId);

      return bean;
   }

   public static Object findRequestBean(String beanId)
   {
      return findRequestBean(FacesContext.getCurrentInstance(), beanId);
   }

   public static Object findRequestBean(FacesContext context, String beanId)
   {
      return context.getExternalContext().getRequestMap().get(beanId);
   }

   public static Object findApplicationBean(String beanId)
   {
      return findApplicationBean(FacesContext.getCurrentInstance(), beanId);
   }
   
   public static Object findApplicationBean(FacesContext context, String beanId)
   {
      return context.getExternalContext().getApplicationMap().get(beanId);
   }

   private ManagedBeanUtils()
   {
   }
}
