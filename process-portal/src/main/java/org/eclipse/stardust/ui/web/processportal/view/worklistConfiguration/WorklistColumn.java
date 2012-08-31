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
package org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistColumn
{
   private String title;
   private String name;
   private boolean visible;

   public WorklistColumn(String name, String title, boolean visible)
   {
      super();
      this.name = name;
      this.title = title;
      this.visible = visible;
   }

   public WorklistColumn clone()
   {
      return new WorklistColumn(name, title, visible);
   }

   public String getTitle()
   {
      return title;
   }

   public String getName()
   {
      return name;
   }

   public void setVisible(boolean visible)
   {
      this.visible = visible;
   }

   public boolean isVisible()
   {
      return visible;
   }

}
