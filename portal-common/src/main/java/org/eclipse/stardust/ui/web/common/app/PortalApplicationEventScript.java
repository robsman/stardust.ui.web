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
package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class PortalApplicationEventScript implements Serializable
{
   private static final long serialVersionUID = 1175464661323396855L;

   public static final String BEAN_NAME = "ippPortalAppEventScript";
   private static final Logger trace = LogManager.getLogger(PortalApplicationEventScript.class);

   // Possibility to have more than one Scripts to be executed
   private ArrayList<String> eventScripts = new ArrayList<String>();
   private boolean onceRead = false;
   private boolean resetWindowWidth = false;

   /**
    * @return
    */
   public static PortalApplicationEventScript getInstance()
   {
      return (PortalApplicationEventScript) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * @return
    */
   public String getEventScripts()
   {
      StringBuffer es = new StringBuffer();
      es.append("InfinityBpm.Core.resizePortalMainWindow(" + System.currentTimeMillis() + ");");

      for (String eventScript : getCleanedupScripts())
      {
         es.append(eventScript);
         es.append("\n");
      }

      onceRead = true;

      String scripts = es.toString();

      if (resetWindowWidth)
      {
         scripts = "InfinityBpm.Core.resetWindowWidth();\n" + scripts;
         resetWindowWidth = false;
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Event Scripts: " + scripts);
      }

      return scripts;
   }

   /**
    * Cleans the scripts as necessary
    * @return
    */
   private List<String> getCleanedupScripts()
   {
      try
      {
         List<String> cleanScripts = new ArrayList<String>(eventScripts);

         for (int i = 0; i < cleanScripts.size(); ++i)
         {
            String script = cleanScripts.get(i).trim();
            if (script.startsWith("InfinityBpm.ProcessPortal.closeContentFrame('"))
            {
               int idxOpeningQuote = script.indexOf("'");
               int idxClosingQuote = script.indexOf("'", idxOpeningQuote + 1);

               String panelId = script.substring(idxOpeningQuote + 1, idxClosingQuote);
               for (int j = 0; j < i; ++j)
               {
                  String activateScript = cleanScripts.get(j).trim();
                  if (activateScript.startsWith("InfinityBpm.ProcessPortal.createOrActivateContentFrame('"+ panelId + "'"))
                  {
                     // If the panel is supposed to be closed later on, avoid creating it first hand (see CRNT-21613)
                     String disabledActivateScript = "/* " + activateScript + " */";
                     cleanScripts.set(j, disabledActivateScript);
                  }
               }
            }
         }

         return cleanScripts;
      }
      catch (Exception e)
      {
         trace.error("Unable to clean scripts", e);
         return eventScripts;
      }
   }

   /**
    * @param eventScript
    */
   public void addEventScript(String eventScript)
   {
      onceRead = false;
      this.eventScripts.add(eventScript);
   }

   /**
    * Called by Phase Listener
    */
   public void cleanEventScripts()
   {
      if (onceRead)
      {
         eventScripts = new ArrayList<String>();
      }
   }

   public void setResetWindowWidth(boolean resetWindowWidth)
   {
      this.resetWindowWidth = resetWindowWidth;
   }
}
