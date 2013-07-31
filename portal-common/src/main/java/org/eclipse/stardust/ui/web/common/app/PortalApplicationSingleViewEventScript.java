package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class PortalApplicationSingleViewEventScript implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * @param scripts
    * @return
    */
   public static String wrapIntoRunScript(String scripts)
   {
      if (StringUtils.isNotEmpty(scripts))
      {
         scripts = "function() {" + scripts + "}";
         scripts = "parent.BridgeUtils.runScript(" + scripts + ", " + Math.random() + ")";
      }
      return scripts;
   }

   /**
    * @return
    */
   public String getWrappedEventScripts()
   {
      return wrapIntoRunScript(getEventScripts(false));
   }

   /**
    * @param syncLPs
    * @return
    */
   public String getEventScripts(boolean syncLPs)
   {
      // Get from Portal Application, Process it and return
      PortalApplication portalApp = PortalApplication.getInstance();
      String scripts = portalApp.getEventScripts();

      if (syncLPs)
      {
         scripts += "parent.BridgeUtils.View.syncLaunchPanels(" + Math.random() + ");";
      }

      if (StringUtils.isNotEmpty(scripts))
      {
         scripts = StringUtils.replace(scripts, "InfinityBpm.ProcessPortal.createOrActivateContentFrame",
               "parent.BridgeUtils.FrameManager.createOrActivate");
         scripts = StringUtils.replace(scripts, "InfinityBpm.ProcessPortal.deactivateContentFrame",
               "parent.BridgeUtils.FrameManager.deactivate");
         scripts = StringUtils.replace(scripts, "InfinityBpm.ProcessPortal.closeContentFrame",
               "parent.BridgeUtils.FrameManager.close");

         ArrayList<String> iframeCreateScripts = new ArrayList<String>();
         ArrayList<String> iframeCloseScripts = new ArrayList<String>();

         ArrayList<String> openViewScripts = new ArrayList<String>();
         ArrayList<String> closeViewScripts = new ArrayList<String>();
         
         ArrayList<String> openDialogScripts = new ArrayList<String>();
         ArrayList<String> closeDialogScripts = new ArrayList<String>();
         
         ArrayList<String> restOfTheScripts = new ArrayList<String>();

         Iterator<String> it = StringUtils.split(scripts, "\n");
         while (it.hasNext())
         {
            String script = it.next();
            if (script.contains("parent.BridgeUtils.FrameManager.createOrActivate"))
            {
               addScript(iframeCreateScripts, script);
            }
            else if (script.contains("parent.BridgeUtils.FrameManager.close"))
            {
               addScript(iframeCloseScripts, script);
            }
            else if (script.contains("parent.BridgeUtils.View.openView"))
            {
               addScript(openViewScripts, script);
            }
            else if (script.contains("parent.BridgeUtils.View.closeView"))
            {
               addScript(closeViewScripts, script);
            }
            else if (script.contains("parent.BridgeUtils.Dialog.open"))
            {
               addScript(openDialogScripts, script);
            }
            else if (script.contains("parent.BridgeUtils.Dialog.close"))
            {
               addScript(closeDialogScripts, script);
            }
            else
            {
               addScript(restOfTheScripts, script);
            }
         }

         // Reorder
         StringBuffer allScripts = new StringBuffer();
         allScripts.append(toStringBuffer(iframeCloseScripts));
         allScripts.append(toStringBuffer(openViewScripts));
         allScripts.append(toStringBuffer(iframeCreateScripts));
         allScripts.append(toStringBuffer(closeDialogScripts));
         allScripts.append(toStringBuffer(openDialogScripts));
         allScripts.append(toStringBuffer(restOfTheScripts));
         allScripts.append(toStringBuffer(closeViewScripts));

         scripts = allScripts.toString();
      }

      return scripts;
   }
   
   /**
    * @param list
    * @param script
    */
   private void addScript(ArrayList<String> list, String script)
   {
      if (list.size() >= 1)
      {
         if (list.get(list.size() - 1).equals(script))
         {
            return;
         }
      }

      list.add(script);
   }
   
   /**
    * @param list
    * @return
    */
   private StringBuffer toStringBuffer(ArrayList<String> list)
   {
      StringBuffer sb = new StringBuffer();
      for (String str : list)
      {
         sb.append(str).append("\n");
      }
      return sb;
   }
}
