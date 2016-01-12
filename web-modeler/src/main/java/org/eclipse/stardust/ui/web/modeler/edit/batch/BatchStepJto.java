package org.eclipse.stardust.ui.web.modeler.edit.batch;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.ui.web.modeler.edit.jto.ChangeDescriptionJto;

public class BatchStepJto extends ChangeDescriptionJto
{
   public String commandId;

   public List<VarSpecJto> variables = newArrayList();
}