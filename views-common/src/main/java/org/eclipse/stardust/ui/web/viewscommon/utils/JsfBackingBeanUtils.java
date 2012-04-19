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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.PortalBackingBean;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalErrorClass;


/**
 * @author sauer
 * @version $Revision: $
 */
public class JsfBackingBeanUtils
{

   private static final Logger trace = LogManager.getLogger(JsfBackingBeanUtils.class);

   public static void performBackingBeanInDataMappings(ApplicationContext jsfContext,
         Map inData)
   {
      // Retrieve backing bean
      String beanName = (String) jsfContext.getAttribute("jsf:managedBeanName");

      // TODO rsauer resetting bean
      FacesContext context = FacesContext.getCurrentInstance();
      ValueBinding binding = context.getApplication().createValueBinding(
            "#{" + beanName + "}");
      if(null != binding)
      {
         binding.setValue(context, null);
      }

      Object targetObject = ManagedBeanUtils.getManagedBean(context, beanName);

      if (null == targetObject)
      {
         binding = context.getApplication().createValueBinding("#{" + beanName + "}");
         targetObject = (binding != null) ? binding.getValue(context) : null;
      }

      try
      {
         ReflectionUtils.invokeMethod(targetObject, "setData", inData);
      }
      catch (Exception e)
      {
         // PortalException pe = new PortalException(e);
         // pe.setSummary(Localizer.getString(ProcessLocalizerKey.FAILED_EVALUATING_DATA_MAPPING)); 
         trace.error("Failed evaluating data mapping: " + e);
         // throw pe;
      }
   }
   
   public static Map performBackingBeanOutDataMappings(ApplicationContext jsfContext)
         throws PortalException
   {
      try
      {
         String methodName = (String) jsfContext.getAttribute(PredefinedConstants.METHOD_NAME_ATT);
         String beanName = (String) jsfContext.getAttribute("jsf:managedBeanName");

         Object targetObject = PortalBackingBean.getManagedBean(beanName);
         
         if (null == targetObject)
         {
            targetObject = FacesContext.getCurrentInstance()
                  .getExternalContext()
                  .getSessionMap()
                  .get(beanName);
         }

         if(null == targetObject)
         {
            FacesContext context = FacesContext.getCurrentInstance();
            ValueBinding binding = context.getApplication().createValueBinding(
                  MessageFormat.format("#'{'{0}'}'", new Object[] {beanName }));
            targetObject = binding.getValue(context);
         }

         // TODO completion methods with parameters allowed?

         Object returnValue = null;
         try
         {
            Class beanType = targetObject.getClass();
            Method completionMethod = Reflect.decodeMethod(beanType, methodName);
            
            returnValue = completionMethod.invoke(targetObject, (Object[]) null);
         }
         catch (Throwable e)
         {
            if (e.getCause() instanceof ValidatorException)
            {
               throw (ValidatorException)e.getCause();
            }
            trace.error("Failed invoking completion method: " + e);
            throw new PortalException(
                  ProcessPortalErrorClass.FAILED_INVOKING_COMPLETION_METHOD, e);
         }

         if (returnValue instanceof Map)
         {
            return (Map)returnValue;
         }
         else
         {
            StringBuffer msg = new StringBuffer("Failed evaluating out data mapping. Out Data Retrived is not an instance of Map");
            msg.append("\nData: " + returnValue);
            trace.warn(msg.toString());

            PortalException pe = new PortalException(ProcessPortalErrorClass.FAILED_EVALUATING_OUT_DATA_MAPPING);
            throw pe;
         }
      }
      catch (ValidatorException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         PortalException pe = e instanceof PortalException ?
               (PortalException)e : new PortalException(
                     ProcessPortalErrorClass.FAILED_INVOKING_COMPLETION_METHOD, e);
         throw pe;
      }
   }
   
}
