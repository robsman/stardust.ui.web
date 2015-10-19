/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.misc;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class EndPointDTO
{
   public String uri;
   public String httpMethod;
   public String method;
   public String path;

   public String description;
   public String requestDescription;
   public String responseDescription;
   
   public String relativePath = "";

   public Map<String, ParameterDTO> queryParams = new HashMap<String, ParameterDTO>();
   public Map<String, ParameterDTO> pathParams = new HashMap<String, ParameterDTO>();
   public Map<String, ParameterDTO> defaultParams = new HashMap<String, ParameterDTO>();

   public static class ParameterDTO
   {
      public static enum Type {
         Query, Path, Default
      }

      public Type type = Type.Default;
      public String javaType;
      public String defaultValue;
      public String name;

      /**
       * @param parameter
       * @param parameterAnnotations
       */
      public ParameterDTO(@SuppressWarnings("rawtypes") Class parameter, Annotation[] parameterAnnotations)
      {
         this.javaType = parameter.getName();
         this.name = "PostedData";

         for (Annotation annotation : parameterAnnotations)
         {
            if (annotation instanceof PathParam)
            {
               this.type = Type.Path;
               this.name = ((PathParam) annotation).value();
            }
            else if (annotation instanceof QueryParam)
            {
               this.type = Type.Query;
               this.name = ((QueryParam) annotation).value();
            }
            else if (annotation instanceof DefaultValue)
            {
               this.defaultValue = ((DefaultValue) annotation).value();
            }
         }
      }
   }
}
