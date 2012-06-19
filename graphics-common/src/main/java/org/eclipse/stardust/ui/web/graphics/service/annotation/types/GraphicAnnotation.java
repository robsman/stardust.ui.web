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
package org.eclipse.stardust.ui.web.graphics.service.annotation.types;

/**
 * @author Shrikant.Gangal
 *
 */
public class GraphicAnnotation
{
   private String id;
   
   private String type;
   
   private String action;
   
   private String user;
   
   private String lastuseraction;
   
   private String lastactiontimestamp;
   
   private AnnotationProperties props;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getAction()
   {
      return action;
   }

   public void setAction(String action)
   {
      this.action = action;
   }

   public AnnotationProperties getProps()
   {
      return props;
   }

   public void setProps(AnnotationProperties props)
   {
      this.props = props;
   }

   public String getUser()
   {
      return user;
   }

   public void setUser(String user)
   {
      this.user = user;
   }

   public String getLastuseraction()
   {
      return lastuseraction;
   }

   public void setLastuseraction(String lastuseraction)
   {
      this.lastuseraction = lastuseraction;
   }

   public String getLastactiontimestamp()
   {
      return lastactiontimestamp;
   }

   public void setLastactiontimestamp(String lastactiontimestamp)
   {
      this.lastactiontimestamp = lastactiontimestamp;
   }
}
