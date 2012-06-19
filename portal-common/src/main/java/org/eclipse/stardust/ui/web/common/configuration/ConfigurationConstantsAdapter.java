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
package org.eclipse.stardust.ui.web.common.configuration;

import java.io.Serializable;

/**
 * @author Subodh.Godbole
 *
 */
public class ConfigurationConstantsAdapter implements ConfigurationConstants, Serializable
{
   private static final long serialVersionUID = 1L;

   private static ConfigurationConstantsAdapter instance;

   /**
    * 
    */
   public static synchronized ConfigurationConstantsAdapter getInstance()
   {
      if(instance == null)
      {
         instance = new ConfigurationConstantsAdapter();
      }
      
      return instance;
   }
   
   public int getDEFAULT_MAX_TAB_DISPLAY()
   {
      return DEFAULT_MAX_TAB_DISPLAY;
   }
   
   public int getDEFAULT_PAGE_SIZE()
   {
      return DEFAULT_PAGE_SIZE;
   }
   
   public int getDEFAULT_MAX_PAGES()
   {
      return DEFAULT_MAX_PAGES;
   }
   
   public int getDEFAULT_FAST_STEP()
   {
      return DEFAULT_FAST_STEP;
   }

   public int getLOWER_LIMIT_MAX_TAB_DISPLAY()
   {
      return LOWER_LIMIT_MAX_TAB_DISPLAY;
   }

   public int getLOWER_LIMIT_PAGE_SIZE()
   {
      return LOWER_LIMIT_PAGE_SIZE;
   }

   public int getLOWER_LIMIT_MAX_PAGES()
   {
      return LOWER_LIMIT_MAX_PAGES;
   }

   public int getLOWER_LIMIT_FAST_STEP()
   {
      return LOWER_LIMIT_FAST_STEP;
   }
   
   public int getUPPER_LIMIT_MAX_TAB_DISPLAY()
   {
      return UPPER_LIMIT_MAX_TAB_DISPLAY;
   }
   
   public int getUPPER_LIMIT_PAGE_SIZE()
   {
      return UPPER_LIMIT_PAGE_SIZE;
   }
   
   public int getUPPER_LIMIT_MAX_PAGES()
   {
      return UPPER_LIMIT_MAX_PAGES;
   }
   
   public int getUPPER_LIMIT_FAST_STEP()
   {
      return UPPER_LIMIT_FAST_STEP;
   }
}
