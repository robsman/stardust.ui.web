package org.eclipse.stardust.ui.web.viewscommon.common;

import javax.faces.event.ActionEvent;

/**
 * @author Shrikant.Gangal
 * 
 */
public class ContextMenuItem
{
   private String value;
   private String icon;
   private boolean disabled;
   private IContextMenuActionHandler menuActionhandler;

   /**
    * @return
    */
   public String getValue()
   {
      return value;
   }

   /**
    * @param value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * @return
    */
   public String getIcon()
   {
      return icon;
   }

   /**
    * @param icon
    */
   public void setIcon(String icon)
   {
      this.icon = icon;
   }

   /**
    * @return
    */
   public boolean isDisabled()
   {
      return disabled;
   }

   /**
    * @param disabled
    */
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }

   /**
    * @return
    */
   public IContextMenuActionHandler getMenuActionhandler()
   {
      return menuActionhandler;
   }

   /**
    * @param menuActionhandler
    */
   public void setMenuActionhandler(IContextMenuActionHandler menuActionhandler)
   {
      this.menuActionhandler = menuActionhandler;
   }

   /**
    * @param event
    */
   public void invoke(ActionEvent event)
   {
      if (null != menuActionhandler)
      {
         menuActionhandler.handle(event);
      }
   }
}
