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
package org.eclipse.stardust.ui.web.viewscommon.wizard;

/**
 * Class represent wizard page
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public abstract class WizardPage implements WizardPageEventHandler
{
   private String title;
   private final String name;
   private final String path;
   private String toolbar;

   public WizardPage(String name, String path)
   {
      this.name = name;
      this.path = path;
      this.title = "";
   }

   public WizardPage(String name, String path, String title)
   {
      this.name = name;
      this.path = path;
      this.title = title;
   }

   public String getTitle()
   {
      return title;
   }

   public String getName()
   {
      return name;
   }

   public String getPath()
   {
      return path;
   }

   public String getToolbar()
   {
      return toolbar;
   }

   public void setToolbar(String toolbar)
   {
      this.toolbar = toolbar;
   }

}
