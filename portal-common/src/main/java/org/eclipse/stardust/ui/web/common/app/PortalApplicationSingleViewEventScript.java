package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;
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

         StringBuffer iframeCreateScripts = new StringBuffer();
         StringBuffer iframeCloseScripts = new StringBuffer();

         StringBuffer openViewScripts = new StringBuffer();
         StringBuffer closeViewScripts = new StringBuffer();
         
         StringBuffer openDialogScripts = new StringBuffer();
         StringBuffer closeDialogScripts = new StringBuffer();
         
         StringBuffer restOfTheScripts = new StringBuffer();

         Iterator<String> it = StringUtils.split(scripts, "\n");
         while (it.hasNext())
         {
            String script = it.next();
            if (script.contains("parent.BridgeUtils.FrameManager.createOrActivate"))
            {
               iframeCreateScripts.append(script).append("\n");
            }
            else if (script.contains("parent.BridgeUtils.FrameManager.close"))
            {
               iframeCloseScripts.append(script).append("\n");
            }
            else if (script.contains("parent.BridgeUtils.View.openView"))
            {
               openViewScripts.append(script).append("\n");
            }
            else if (script.contains("parent.BridgeUtils.View.closeView"))
            {
               closeViewScripts.append(script).append("\n");
            }
            else if (script.contains("parent.BridgeUtils.Dialog.open"))
            {
               openDialogScripts.append(script).append("\n");
            }
            else if (script.contains("parent.BridgeUtils.Dialog.close"))
            {
               closeDialogScripts.append(script).append("\n");
            }
            else
            {
               restOfTheScripts.append(script).append("\n");
            }
         }

         // Reorder
         StringBuffer allScripts = new StringBuffer();
         allScripts.append(iframeCloseScripts);
         allScripts.append(openViewScripts);
         allScripts.append(iframeCreateScripts);
         allScripts.append(closeDialogScripts);
         allScripts.append(openDialogScripts);
         allScripts.append(restOfTheScripts);
         allScripts.append(closeViewScripts);

         scripts = allScripts.toString();
      }

      return scripts;
   }
}
