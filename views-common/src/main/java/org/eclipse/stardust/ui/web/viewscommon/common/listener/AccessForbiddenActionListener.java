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
package org.eclipse.stardust.ui.web.viewscommon.common.listener;

import javax.faces.FacesException;
import javax.faces.el.EvaluationException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;


/**
 * @author fuhrmann
 * @version $Revision$
 */
public class AccessForbiddenActionListener implements ActionListener
{
   private ActionListener delegate;

   public AccessForbiddenActionListener(ActionListener delegate)
   {
      this.delegate = delegate;
   }

   /**
    * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
    */
   public void processAction(ActionEvent actionEvent) throws AbortProcessingException
   {
      try
      {
         delegate.processAction(actionEvent);
      }
      catch (FacesException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof EvaluationException)
         {
            cause = ((EvaluationException) cause).getCause();
         }
         if (cause instanceof AccessForbiddenException)
         {
            ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                  (AccessForbiddenException) cause);
         }
      }
   }

}
