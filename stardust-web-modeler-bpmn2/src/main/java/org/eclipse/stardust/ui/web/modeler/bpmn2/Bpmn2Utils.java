package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.getExtensionAttribute;

import java.util.List;
import java.util.UUID;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.di.Diagram;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class Bpmn2Utils
{
   private static final Bpmn2Package PKG_BPMN2 = Bpmn2Package.eINSTANCE;

   private static final Bpmn2Factory F_BPMN2 = Bpmn2Factory.eINSTANCE;

   private static final BpmnDiFactory F_BPMN2DI = BpmnDiFactory.eINSTANCE;

   private static final DcFactory F_BPMN2DC = DcFactory.eINSTANCE;

   public static Bpmn2Package bpmn2Package()
   {
      return PKG_BPMN2;
   }

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

   public static String getModelUuid(Definitions model)
   {
      String modelUuid = getExtensionAttribute(model, ModelerConstants.UUID_PROPERTY);
      if (isEmpty(modelUuid))
      {
         modelUuid = model.getId();
      }

      return modelUuid;
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
      return findContainer(element, Definitions.class);
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
      EObject currentElement = element;
      while (null != currentElement)
      {
         EObject currentContainer = currentElement.eContainer();
         if (currentContainer instanceof ChangeDescriptionImpl)
         {
            // substitute with real container (the one containing the element before it was detached)
            currentContainer = ((ChangeDescriptionImpl) currentContainer).getOldContainer(currentElement);
         }

         if (containerType.isInstance(currentContainer))
         {
            return containerType.cast(currentContainer);
         }
         else
         {
            // navigate one level up
            currentElement = currentContainer;
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
