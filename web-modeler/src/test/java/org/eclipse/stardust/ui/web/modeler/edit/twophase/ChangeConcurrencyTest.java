/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.edit.twophase;

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newBpmModel;
import static org.eclipse.stardust.model.xpdl.builder.process.BpmProcessDefinitionBuilder.newProcessDefinition;

import java.util.Collections;
import java.util.ConcurrentModificationException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.compare.diff.merge.service.MergeService;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.junit.Test;

public class ChangeConcurrencyTest
{

   @Test
   public void createProcess() throws InterruptedException

   {
      ModelType m = newBpmModel().withIdAndName("test", "test").build();

      EditingSession editSession = new EditingSession();
      editSession.trackModel(m);
      editSession.beginEdit();
      newProcessDefinition(m).withIdAndName("PD_1", "Test Process 1").build();
      editSession.endEdit();

      /**
       * workingCopy - The copy on which the changes are applied
       * workingCopyOriginal - A copy of the status before changes are applied
       */
      ModelType m_a = (ModelType)org.eclipse.emf.ecore.util.EcoreUtil.copy(m);

      /**
       * Start working on the Copy. Change the process name
       */
      EditingSession anotherSession = new EditingSession();
      anotherSession.trackModel(m_a);
      anotherSession.beginEdit();
      m_a.getProcessDefinition().get(0).setName("VollHorst");
      anotherSession.endEdit();

      editSession.beginEdit();
      newProcessDefinition(m).withIdAndName("ID_20", "another Test Process").build();
      editSession.endEdit();

      ModelType m_strich = (ModelType)org.eclipse.emf.ecore.util.EcoreUtil.copy(m);

      MatchModel wCtoOWC = MatchService.doMatch((EObject) m_a, (EObject) m_strich, (EObject) m, Collections.<String, Object> emptyMap());
      DiffModel diffWorkCopyAndOrig = DiffService.doDiff(wCtoOWC, true);
      EList<DiffElement> differences1 = diffWorkCopyAndOrig.getOwnedElements();



      try
      {
         getSubdiffs(differences1, "", false);
         MergeService.merge(differences1, true);
         System.out.println("Merged working copy m_a to copy m with following change: Process name was changed to " +m.getProcessDefinition().get(0).getName());
         System.out.println("Merged working copy m_strich to copy m with following change: Process name was changed to " +m.getProcessDefinition().get(1).getName());

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void getSubdiffs(EList<DiffElement> subdif, String indent, Boolean conflicting)
   {
      if (subdif.isEmpty())
      {
         return;
      }
      else if (conflicting)
      {
         System.out.println("Conflict detected. Stopping Execution.");
         throw new ConcurrentModificationException();

      }
      else
      {
         for (DiffElement de : subdif)
         {
            System.out.println(indent + de.isConflicting());
            System.out.println(indent + de.getKind());
            getSubdiffs(de.getSubDiffElements(), indent + "  ", de.isConflicting());

         }
      }
   }

}
