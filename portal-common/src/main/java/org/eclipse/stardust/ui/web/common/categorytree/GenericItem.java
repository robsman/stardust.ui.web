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
package org.eclipse.stardust.ui.web.common.categorytree;

import java.io.Serializable;

/**
 * @author subodh.godbole
 *
 */
public class GenericItem implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String id;
   private String label;
   private Object itemObject;
   
   private String icon;

   /**
    * @param id
    * @param label
    * @param itemObject
    */
   public GenericItem(String id, String label, Object itemObject)
   {
      this.id = id;
      this.label = label;
      this.itemObject = itemObject;
   }

   /**
    * @param id
    * @param label
    */
   public GenericItem(String id, String label)
   {
      this(id, label, null);
   }


   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }
   
   public String getLabel()
   {
      return label;
   }
   
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   public Object getItemObject()
   {
      return itemObject;
   }
   
   public void setItemObject(Object itemObject)
   {
      this.itemObject = itemObject;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String icon)
   {
      this.icon = icon;
   }
}
