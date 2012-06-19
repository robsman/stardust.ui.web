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
package org.eclipse.stardust.ui.web.common.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.UiElementType;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;


/**
 * This Class reads out the messages from UI Elements Like LaunchPanel, MenuSection,
 * ToolbarSection and exposes like Map for usage in xhtmls
 * 
 * @author Subodh.Godbole
 */
public class UiElementMessage implements Map<String, String>, Serializable
{
   private static final long serialVersionUID = 1L;

   private UiElement uiElement;

   /**
    * @param uiElement
    */
   public UiElementMessage(UiElement uiElement)
   {
      this.uiElement = uiElement;
   }

   /**
    * @param type
    * @param name
    */
   public UiElementMessage(UiElementType type, String name)
   {
      if (UiElementType.LAUNCH_PANEL == type)
         uiElement = PortalUiController.getInstance().getLaunchPanel(name);
      else if (UiElementType.MENU_SECTION == type)
         uiElement = PortalUiController.getInstance().getMenuSection(name);
      else if (UiElementType.TOOLBAR_SECTION == type)
         uiElement = PortalUiController.getInstance().getToolbarSection(name);
      else if (UiElementType.VIEW_DEFINITION == type)
         uiElement = PortalUiController.getInstance().getViewDefinition(name);

      if (uiElement == null)
         throw new IllegalStateException(type + " with name '" + name
               + "' not found in any perspective");
   }

   public String getString(String key)
   {
      return get(key);
   }

   public boolean hasKey(String key)
   {
      return uiElement.hasMessage(key, null);
   }

   public String get(Object key)
   {
      return uiElement.getMessage((String) key, null);
   }

   public String getString(String key, String... params)
   {

      List<String> paramList = new ArrayList(Arrays.asList(params));

      String value = getString(key);
      if (value.indexOf("{") >= 0)
      {
         int numberOfOccurances = (value.split("\\{")).length;
         while (params.length < numberOfOccurances - 1)
         {

            paramList.add("");
            params = (String[]) paramList.toArray(new String[paramList.size()]);

         }

         int i = 0;
         while (value.indexOf("{") >= 0)
         {
            value = value.replace(value.substring(value.indexOf("{"),
                  value.indexOf("}") + 1), params[i++]);
         }

      }

      return value;
   }

   public void clear()
   {}

   public boolean containsKey(Object key)
   {
      return false;
   }

   public boolean containsValue(Object value)
   {
      return false;
   }

   public Set entrySet()
   {
      return null;
   }

   public boolean isEmpty()
   {
      return false;
   }

   public Set keySet()
   {
      return null;
   }

   public String put(String key, String value)
   {
      return null;
   }

   public void putAll(Map t)
   {}

   public String remove(Object key)
   {
      return null;
   }

   public int size()
   {
      return 0;
   }

   public Collection values()
   {
      return null;
   }
}
