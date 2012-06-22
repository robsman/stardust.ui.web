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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
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
         if (trace.isDebugEnabled())
         {
            trace.debug("Performing Backing Bean In Data Mappings with old approach.");
         }

         for (Iterator i = jsfContext.getAllInDataMappings().iterator(); i.hasNext();)
         {
            DataMapping mapping = (DataMapping) i.next();

            String mappingId = mapping.getId();
            Object value = inData.get(mappingId);

            try
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Value " + value + " retrieved for mapping " + mappingId);
               }
               
               if (StringUtils.isEmpty(mapping.getApplicationPath()))
               {
                  JavaDataTypeUtils.evaluate(mapping.getApplicationAccessPoint().getId(),
                        targetObject, value);
               }
               else
               {
                  Object ap = JavaDataTypeUtils.evaluate(
                        mapping.getApplicationAccessPoint().getId(), targetObject);
                  JavaDataTypeUtils.evaluate(mapping.getApplicationPath(), ap, value);
               }
            }
            catch (InvocationTargetException e)
            {
               // PortalException pe = new PortalException(e);
               // pe.setSummary(Localizer.getString(ProcessLocalizerKey.FAILED_EVALUATING_DATA_MAPPING));
               trace.error("Failed evaluating data mapping: " + e);
               // throw pe;
            }
            catch (PublicException e)
            {
               // PortalException pe = new PortalException(e);
               // pe.setSummary(Localizer.getString(ProcessLocalizerKey.FAILED_EVALUATING_DATA_MAPPING)); 
               trace.error("Failed evaluating data mapping: " + e);
               // throw pe;
            }
         }
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

         if (trace.isDebugEnabled())
         {
            trace.debug("Performing Backing Bean Out Data Mappings with old approach.");
         }

         Map outData = CollectionUtils.newMap();

         for (Iterator i = jsfContext.getAllOutDataMappings().iterator(); i.hasNext();)
         {
            DataMapping mapping = (DataMapping) i.next();
            String mappingID = mapping.getId();

            if ("returnValue".equals(mapping.getApplicationAccessPoint().getId()))
            {
               outData.put(mappingID, returnValue);
            }
            else
            {
               try
               {
                  Object outValue;
                  if (StringUtils.isEmpty(mapping.getApplicationPath()))
                  {
                     outValue = JavaDataTypeUtils.evaluate(
                           mapping.getApplicationAccessPoint().getId(), targetObject);
                  }
                  else
                  {
                     Object ap = JavaDataTypeUtils.evaluate(
                           mapping.getApplicationAccessPoint().getId(), targetObject);
                     outValue = JavaDataTypeUtils.evaluate(
                           mapping.getApplicationPath(), ap);
                  }
                  
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Value " + outValue + " retrieved for mapping "
                           + mappingID);
                  }
                  outData.put(mappingID, outValue);
               }
               catch (InvocationTargetException e)
               {
                  PortalException pe = new PortalException(
                        ProcessPortalErrorClass.FAILED_EVALUATING_OUT_DATA_MAPPING, e);
                  StringBuffer msg = new StringBuffer("Failed evaluating out data mapping (id: ");
                  msg.append(mappingID).append("): ").append(e.getCause());
                  trace.warn(msg.toString());
                  throw pe;
               }
               catch (PublicException e)
               {
                  PortalException pe = new PortalException(
                        ProcessPortalErrorClass.FAILED_EVALUATING_OUT_DATA_MAPPING, e);
                  StringBuffer msg = new StringBuffer("Failed evaluating out data mapping (id: ");
                  msg.append(mappingID).append("): ").append(e.getCause());
                  trace.warn(msg.toString());
                  throw pe;
               }
            }
         }

         if (trace.isDebugEnabled())
         {
            trace.debug("OUT DATA = " + outData);
         }

         return outData;
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

   /**
    * @param jsfContext
    * @return
    */
   public static Object getBackingBean(ApplicationContext jsfContext)
   {
      String beanName = (String) jsfContext.getAttribute("jsf:managedBeanName");

      Object targetObject = PortalBackingBean.getManagedBean(beanName);

      if (null == targetObject)
      {
         targetObject = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(beanName);
      }

      if (null == targetObject)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         ValueBinding binding = context.getApplication().createValueBinding(
               MessageFormat.format("#'{'{0}'}'", new Object[] {beanName}));
         targetObject = binding.getValue(context);
      }

      return targetObject;
   }
}
