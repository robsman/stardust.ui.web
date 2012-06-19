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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ErrorWarningTreeItem
{
   private ErrorWarningTreeItem parent;
   private Set<ErrorWarningTreeItem> childrens = new HashSet<ErrorWarningTreeItem>();
   private String element;
   private String elementId;
   private String message;
   private String modelId;
   private Type type;

   public Set<ErrorWarningTreeItem> getChildrens()
   {
      return childrens;
   }

   public String getElement()
   {
      return element;
   }

   public String getElementId()
   {
      return elementId;
   }

   public String getMessage()
   {
      return message;
   }

   public String getModelId()
   {
      return modelId;
   }

   public ErrorWarningTreeItem getParent()
   {
      return parent;
   }

   public Type getType()
   {
      return type;
   }

   public void setChildrens(Set<ErrorWarningTreeItem> childrens)
   {
      this.childrens = childrens;
   }

   public void setElement(String element)
   {
      this.element = element;
   }

   public void setElementId(String elementId)
   {
      this.elementId = elementId;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public void setModelId(String modelId)
   {
      this.modelId = modelId;
   }

   public void setParent(ErrorWarningTreeItem parent)
   {
      this.parent = parent;
   }

   public void setType(Type type)
   {
      this.type = type;
   }

   public static enum Type {
      ERROR, NONE, WARNING;
   }

   @Override
   public boolean equals(final Object other)
   {

      return other instanceof ErrorWarningTreeItem ? Arrays.equals(this.getInfo(),
            ((ErrorWarningTreeItem) other).getInfo()) : false;
   }

   @Override
   public int hashCode()
   {
      return Arrays.hashCode(this.getInfo());
   }

   private String[] getInfo()
   {
      return new String[] {this.getElementId(), this.getModelId(), this.getMessage()};
   }

}
