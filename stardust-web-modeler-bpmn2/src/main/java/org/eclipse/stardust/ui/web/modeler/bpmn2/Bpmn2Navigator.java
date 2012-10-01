package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Iterator;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;

public class Bpmn2Navigator implements ModelNavigator<Definitions>
{
   private static final Bpmn2Package PKG_BPMN2 = Bpmn2Package.eINSTANCE;

   private Bpmn2Binding bpmn2Binding;

   public Bpmn2Navigator()
   {
   }

   public Bpmn2Navigator(Bpmn2Binding bpmn2Binding)
   {
      setBinding(bpmn2Binding);
   }

   void setBinding(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   public Process findProcess(Definitions model, String processId)
   {
      for (RootElement candidate : model.getRootElements())
      {
         if ((candidate instanceof Process) && !isEmpty(candidate.getId())
               && candidate.getId().equals(processId))
         {
            return (Process) candidate;
         }
      }
      return null;
   }

   @Override
   public EObject findProcessFromDiagramElement(EObject diagramElement)
   {
      // TODO Auto-generated method stub

      assert (diagramElement instanceof DiagramElement);

      if ((diagramElement instanceof BPMNPlane))
      {

      }

      return null;
   }

   @Override
   public EObject findElementByUuid(Definitions model, String uuid)
   {
      return bpmn2Binding.findElementByUuid(model, uuid);
   }

   @Override
   public EObject findElementByOid(Definitions model, long oid)
   {
      return bpmn2Binding.findElementByOid(model, oid);
   }
}
