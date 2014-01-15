package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Resource;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;

public class Bpmn2Navigator implements ModelNavigator<Definitions>
{
   private static final Bpmn2Package PKG_BPMN2 = Bpmn2Package.eINSTANCE;

   private final Bpmn2Binding bpmn2Binding;

   public Bpmn2Navigator(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   public Process findProcess(Definitions model, String processId)
   {
      return findRootElement(model, processId, Process.class);
   }

   public Resource findResource(Definitions model, String resourceId)
   {
      return findRootElement(model, resourceId, Resource.class);
   }

   public static <T extends RootElement> T findRootElement(Definitions model, String elementId,
         Class<T> targetType)
   {
      for (RootElement candidate : model.getRootElements())
      {
         if ((targetType.isInstance(candidate)) && !isEmpty(candidate.getId())
               && candidate.getId().equals(elementId))
         {
            return targetType.cast(candidate);
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
