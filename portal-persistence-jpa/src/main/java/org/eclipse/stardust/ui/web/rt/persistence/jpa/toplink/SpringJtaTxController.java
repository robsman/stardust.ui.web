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
package org.eclipse.stardust.ui.web.rt.persistence.jpa.toplink;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import oracle.toplink.essentials.transaction.JTATransactionController;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.jta.JtaTransactionObject;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author robert.sauer
 */
public class SpringJtaTxController extends JTATransactionController
{
   @Override
   protected TransactionManager acquireTransactionManager() throws Exception
   {
      TransactionStatus currentTxStatus = TransactionAspectSupport.currentTransactionStatus();
      if (currentTxStatus instanceof DefaultTransactionStatus)
      {
         Object transaction = ((DefaultTransactionStatus) currentTxStatus).getTransaction();
         if (transaction instanceof JtaTransactionObject)
         {
            UserTransaction userTransaction = ((JtaTransactionObject) transaction).getUserTransaction();
            
            if (userTransaction instanceof TransactionManager)
            {
               return (TransactionManager) userTransaction;
            }
         }
      }
      
      return null;
  }

}
