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
package org.eclipse.stardust.ui.web.viewscommon.common.event;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;


/**
 * This class does event controlling meant for Same Session (User) and not across sessions (multiple users)
 * @author subodh.godbole
 */
public class IppEventController implements Serializable
{
   private static final Logger trace = LogManager.getLogger(IppEventController.class);
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "ippEventController";

   private transient List<NoteEventObserver> noteObservers = new Vector<NoteEventObserver>();
   private transient List<DocumentEventObserver> documentObservers = new Vector<DocumentEventObserver>();

   /**
    * @return
    */
   public static IppEventController getInstance()
   {
      return (IppEventController) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * @param observer
    */
   public void registerObserver(NoteEventObserver observer)
   {
      noteObservers.add(observer);
   }
   
   /**
    * @param observer
    */
   public void unregisterObserver(NoteEventObserver observer)
   {
      noteObservers.remove(observer);
   }

   /**
    * @param observer
    */
   public void registerObserver(DocumentEventObserver observer)
   {
      documentObservers.add(observer);
   }
   
   /**
    * @param observer
    */
   public void unregisterObserver(DocumentEventObserver observer)
   {
      documentObservers.remove(observer);
   }

   /**
    * @param noteEvent
    */
   public void notifyEvent(NoteEvent noteEvent)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Notifying Note Event. Observer Count = " + noteObservers.size());
      }

      for (NoteEventObserver observer : noteObservers)
      {
         try
         {
            observer.handleEvent(noteEvent);
         }
         catch(Exception e)
         {
            // Consume it, and continue with next observer
            trace.error("Error while notifying Note Event", e);
         }
      }
   }

   /**
    * @param documentEvent
    */
   public void notifyEvent(DocumentEvent documentEvent)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Notifying Document Event. Observer Count = " + documentObservers.size());
      }
      
      List<DocumentEventObserver> documentObserversClone = CollectionUtils.copyList(documentObservers);
      
      for (DocumentEventObserver observer : documentObserversClone)
      {
         try
         {
            observer.handleEvent(documentEvent);
         }
         catch(Exception e)
         {
            // Consume it, and continue with next observer
            trace.error("Error while notifying Document Event", e);
         }
      }
   }

   /**
    * 
    */
   public void destroy()
   {
      noteObservers = null;
      documentObservers = null;
   }
}
