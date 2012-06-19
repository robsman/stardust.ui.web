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
package org.eclipse.stardust.ui.web.viewscommon.common.application;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaAccessPointProvider;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaConstants;
import org.eclipse.stardust.engine.core.pojo.utils.JavaApplicationTypeHelper;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * @author fherinean
 * @version $Revision$
 */
public class JSFAccessPointProvider implements AccessPointProvider
{
   private static final Logger trace = LogManager
         .getLogger(PlainJavaAccessPointProvider.class);

   /**
    * Returns all intrinsic access points which are computed appropriate to the names of
    * the application's class, method and constructor.
    * 
    * @param context
    *           The attribute names and values with the application's class, method and
    *           constructor name.
    * @param typeAttributes
    * @return An iterator over the calculated {@link AccessPoint}s. An empty iterator if
    *         no access points could be calculated.
    */
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      String className = (String) context.get(PredefinedConstants.CLASS_NAME_ATT);
      Class clazz = null;
      
      try
      {
         clazz = Reflect.getClassFromClassName(className);
      }
      catch (Exception e)
      {
         trace.warn("Couldn't create access points for java type, class '" + className
               + "' not found.");
         return Collections.EMPTY_LIST.iterator();
      }
      catch (NoClassDefFoundError e)
      {
         trace.warn("Couldn't create access points for java type, class '" + className
               + "' could not be loaded sucessfully.");
         return Collections.EMPTY_LIST.iterator();
      }
      
            
      Map result = JavaApplicationTypeHelper
            .calculateClassAccessPoints(clazz, true, true);

      String methodName = (String) context.get(PredefinedConstants.METHOD_NAME_ATT);
      try
      {
         Method method = Reflect.decodeMethod(clazz, methodName);

         result.putAll(JavaApplicationTypeHelper.calculateMethodAccessPoints(method,
               PlainJavaConstants.METHOD_PARAMETER_PREFIX, true));
      }
      catch (Exception e)
      {
         trace.warn("", e);
      }

      return result.values().iterator();
   }
}
