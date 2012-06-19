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
package org.eclipse.stardust.ui.web.viewscommon.user;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;




/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class UserDetailsBean extends PopupUIComponentBean
{
   private static final String IS_MODEL= "isModel";
    private static final long serialVersionUID = 1L;
    public final static String USER_OID = "userOid";
    private String userImageURI;
    private User user;
    private boolean model=true;

    /**
     *
     */
    public UserDetailsBean()
    {
       initialize();
    }

    public User getUser()
    {
        return user;
    }

    /**
     * @return the user's profile image.
     */
    public String getUserImageURI()
    {
        return userImageURI;
    }

    @Override
    public void initialize()
    {
        
    }
    
   /**
    * @return
    */
   public String getUserDisplayLabel()
   {
      return UserUtils.getUserDisplayLabel(user);
   }

   public final boolean isModel()
   {
      return model;
   }

   /**
     * Gets user OId and opens userDetails dialog
     *
     * @param ae
     * @throws PortalException
     */
    public void openUserDetailsDialog(ActionEvent ae) throws PortalException
    {
        Map<?, ?> param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Object obj = param.get(USER_OID);
        Long userOid = new Long(obj.toString());
        
        Object isModel = param.get(IS_MODEL);
        if(isModel!=null){
           model =Boolean.parseBoolean(isModel.toString());
        }else{
           model=true;
        }
        

        UserService userService = ServiceFactoryUtils.getUserService();

        if ((userOid != null) && (userService != null))
        {
            user = userService.getUser(userOid.longValue());
        }

        /* Sets the user image. If user is null will set to default image. */
        userImageURI = MyPicturePreferenceUtils.getUsersImageURI(user);
        super.openPopup();
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}
