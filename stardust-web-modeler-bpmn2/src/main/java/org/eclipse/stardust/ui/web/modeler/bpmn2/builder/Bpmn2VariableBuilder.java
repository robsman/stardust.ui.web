package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;

import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.DataStore;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;

public class Bpmn2VariableBuilder
{
   public void attachVariable(Process process, DataObject variable)
   {
      process.getFlowElements().add(variable);
   }

   public void attachVariable(Definitions model, DataStore variable)
   {
      model.getRootElements().add(variable);
   }

   public DataStore createGlobalPrimitiveVariable(Definitions model, DataJto jto)
   {
      DataStore variable = bpmn2Factory().createDataStore();

      // TODO split into one method per concrete data type
      if (ModelerConstants.PRIMITIVE_DATA_TYPE_KEY.equals(jto.dataType))
      {
         // TODO store type ID
      }
      else
      {
         throw new IllegalArgumentException("Unsupported data type: " + jto.dataType);
      }

      variable.setName(jto.name);
      variable.setId( !isEmpty(jto.id) ? jto.id : Bpmn2Utils.createInternalId());

      return variable;
   }

   public DataObject createXsdVariable(Definitions model, DataJto jto)
   {
      DataObject variable = bpmn2Factory().createDataObject();

      // TODO split into one method per concrete data type
      if (ModelerConstants.STRUCTURED_DATA_TYPE_KEY.equals(jto.dataType))
      {
         for (RootElement rootElement : model.getRootElements())
         {
            if ((rootElement instanceof ItemDefinition)
                  && jto.structuredDataTypeFullId.equals(((ItemDefinition) rootElement).getId()))
            {
               variable.setItemSubjectRef((ItemDefinition) rootElement);
               break;
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Unsupported data type: " + jto.dataType);
      }

      variable.setName(jto.name);
      variable.setId( !isEmpty(jto.id) ? jto.id : Bpmn2Utils.createInternalId());

      return variable;
   }
}
