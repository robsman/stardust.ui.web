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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import java.util.List;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;



/**
 * @author Yogesh.Manware
 * 
 */

public class CorrespondenceAddressPickerDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "correspondenceAddressPickerDialog";
   private String label;
   private List<DataPathValue> enteredValues;
   private List<DataPathValue> dataPathContacts;
   private ProcessInstance processInstance;
   private ICallbackHandler iCallbackHandler;

   /**
    * 
    */
   private CorrespondenceAddressPickerDialog()
   {
      super("correspondenceView");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {}

   /**
    * @return current instance
    */
   public static CorrespondenceAddressPickerDialog getCurrentInstance()
   {
      return (CorrespondenceAddressPickerDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * action handler method
    */
   public void close()
   {
      enteredValues.clear();
      closePopup();
   }

   /**
    * action handler method
    */
   public void done()
   {
      fireCallback(EventType.APPLY);
   }

    /**
    * @param event
    */
   public void selectContact(ActionEvent event)
   {
      DataPathValue dataPathValue = (DataPathValue) event.getComponent().getAttributes().get("dataPathValue");

      for (DataPathValue contact : dataPathContacts)
      {
         if (contact.equals(dataPathValue))
            contact.setSelected(true);
      }
      if (!enteredValues.contains(dataPathValue))
      {
         enteredValues.add(dataPathValue);
      }
   }

   /**
    * @param event
    */
   public void removeContact(ActionEvent event)
   {
      DataPathValue dataPathValue = (DataPathValue) event.getComponent().getAttributes().get("dataPathValue");
      for (DataPathValue contact : dataPathContacts)
      {
         if (contact.equals(dataPathValue))
            contact.setSelected(false);
      }
      enteredValues.remove(dataPathValue);
   }

   /**
    * @param eventType
    */
   private void fireCallback(EventType eventType)
   {
      closePopup();
      if (iCallbackHandler != null)
      {
         iCallbackHandler.handleEvent(eventType);
      }
   }
   
   public String getLabel()
   {
      return label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public ICallbackHandler getICallbackHandler()
   {
      return iCallbackHandler;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }

   public List<DataPathValue> getEnteredValues()
   {
      return enteredValues;
   }

   public void setEnteredValues(List<DataPathValue> enteredValues)
   {
      this.enteredValues = enteredValues;
   }

   public List<DataPathValue> getDataPathContacts()
   {
      return dataPathContacts;
   }

   public void setDataPathContacts(List<DataPathValue> dataPathContacts)
   {
      this.dataPathContacts = dataPathContacts;
   }
   
   public int getEnteredValuesSize(){
      return enteredValues.size(); 
   }
}