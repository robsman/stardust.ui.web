package org.eclipse.stardust.ui.web.common.app.tags;

/**
 * This is an ugly hack. This interface is detected based on <em>BindingAware</em> being
 * part of the interface name.
 * 
 * This interface and related code is required to resolve binding related issues, 
 * Please refer CRNT-23091, CRNT-15481 for more information
 * 
 * @author robert.sauer
 * @see 
 *      org.apache.myfaces.shared_impl.util.RestoreStateUtils.getBindingMethod(UIComponent)
 */
interface MyFacesRestoreStateUtilsBindingAware
{
   void handleBindings();
}