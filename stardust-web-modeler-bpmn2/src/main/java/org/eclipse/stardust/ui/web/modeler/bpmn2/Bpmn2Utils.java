package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;
import java.util.UUID;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.di.Diagram;
import org.eclipse.emf.ecore.EObject;

public class Bpmn2Utils
{
   private static final Bpmn2Factory F_BPMN2 = Bpmn2Factory.eINSTANCE;

   private static final BpmnDiFactory F_BPMN2DI = BpmnDiFactory.eINSTANCE;

   private static final DcFactory F_BPMN2DC = DcFactory.eINSTANCE;

   public static Bpmn2Factory bpmn2Factory()
   {
      return F_BPMN2;
   }

   public static BpmnDiFactory bpmn2DiFactory()
   {
      return F_BPMN2DI;
   }

   public static DcFactory bpmn2DcFactory()
   {
      return F_BPMN2DC;
   }

   public static String deriveElementIdFromName(String name)
   {
      StringBuilder idBuilder = new StringBuilder(name.length());
      boolean firstWord = true;
      boolean newWord = true;
      for (int i = 0; i < name.length(); ++i)
      {
         char nameChar = name.charAt(i);
         if (Character.isLetterOrDigit(nameChar))
         {
            if (newWord && !firstWord)
            {
               // append underscore for each first illegal character
               idBuilder.append('_');
            }
            idBuilder.append(Character.toUpperCase(nameChar));
            firstWord &= false;
            newWord = false;
         }
         else
         {
            newWord = true;
         }
      }

      return idBuilder.toString();
   }

   public static String createInternalId()
   {
      return UUID.randomUUID().toString();
   }

   public static Definitions findContainingModel(EObject element)
   {
      EObject parent = element.eContainer();
      while ((null != parent))
      {
         if (parent instanceof Definitions)
         {
            return (Definitions) parent;
         }
         else
         {
            parent = parent.eContainer();
         }
      }

      return null;
   }

   public static Process findContainingProcess(EObject element)
   {
      return findContainer(element, Process.class);
   }

   public static Diagram findContainingDiagram(EObject element)
   {
      return findContainer(element, Diagram.class);
   }

   public static <T extends EObject> T findContainer(EObject element, Class<T> containerType)
   {
      EObject parent = element.eContainer();
      while ((null != parent))
      {
         if (containerType.isInstance(parent))
         {
            return containerType.cast(parent);
         }
         else
         {
            parent = parent.eContainer();
         }
      }

      return null;
   }

   public static List<Process> findParticipatingProcesses(Collaboration collaboration)
   {
      List<Process> processes = newArrayList();
      for (Participant participant : collaboration.getParticipants())
      {
         Process referencedProcess = participant.getProcessRef();
         if (null != referencedProcess)
         {
            // is it a reference to a real, existing process?
            if (null != findContainingModel(referencedProcess))
            {
               if ( !processes.contains(referencedProcess))
               {
                  processes.add(referencedProcess);
               }
            }
         }
      }

      return processes;
   }
}
