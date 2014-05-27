package org.eclipse.stardust.ui.web.modeler.xpdl.edit.diagram;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class MoveNodeSymbolHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "nodeSymbol.move")
   public void handleMoveNode(ModelType model, INodeSymbol nodeSymbol, JsonObject request)
   {
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      LaneSymbol newParentSymbol = null;
      // New parent swimlane Id is passed to get Parent Lane to set
      // laneOffsetX,laneOffsetY, also used when parent changes
      String parentID = extractString(request, ModelerConstants.PARENT_SYMBOL_ID_PROPERTY);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(nodeSymbol);
      if ( !(nodeSymbol instanceof LaneSymbol))
      {
         newParentSymbol = getModelBuilderFacade().findLaneSymbolById(processDefinition,
               parentID);

         if (null != newParentSymbol)
         {
            laneOffsetX = new Long(newParentSymbol.getXPos()).intValue();
            laneOffsetY = new Long(newParentSymbol.getYPos()).intValue();
         }

      }

      nodeSymbol.setXPos(extractInt(request, ModelerConstants.X_PROPERTY) - laneOffsetX);
      nodeSymbol.setYPos(extractInt(request, ModelerConstants.Y_PROPERTY) - laneOffsetY);

      if (hasNotJsonNull(request, ModelerConstants.WIDTH_PROPERTY))
      {
         nodeSymbol.setWidth(extractInt(request, ModelerConstants.WIDTH_PROPERTY));
      }
      if (hasNotJsonNull(request, ModelerConstants.HEIGHT_PROPERTY))
      {
         nodeSymbol.setHeight(extractInt(request, ModelerConstants.HEIGHT_PROPERTY));
      }

      // Type property is used to identify the symbol type, used while changing
      // parentSymbol on move from one lane to another.
      if (hasNotJsonNull(request, ModelerConstants.TYPE_PROPERTY))
      {
         String symbolType = request.get(ModelerConstants.TYPE_PROPERTY).getAsString();
         if (null != symbolType)
         {
            LaneSymbol parentLane = (LaneSymbol) nodeSymbol.eContainer();
            if (symbolType.equals(ModelerConstants.ACTIVITY_SYMBOL)
                  || symbolType.equals(ModelerConstants.GATEWAY_SYMBOL))
            {
               if (parentLane.getElementOid() != newParentSymbol.getElementOid())
               {
                  // If the parent is changed, remove reference from old parent
                  parentLane.getActivitySymbol().remove(nodeSymbol);
                  ActivitySymbolType activitySymbol = (ActivitySymbolType) nodeSymbol;
                  // Set the Performer for Activ
                  if (null != activitySymbol.getActivity().getPerformer())
                  {
                     activitySymbol.getActivity().setPerformer(LaneParticipantUtil.getParticipant(newParentSymbol));
                  }
                  newParentSymbol.getActivitySymbol()
                        .add((ActivitySymbolType) nodeSymbol);
               }
            }
            else if (symbolType.equals(ModelerConstants.EVENT_SYMBOL))
            {
               StartEventSymbol startSymbol = getModelBuilderFacade().findStartEventSymbol(
                     parentLane, nodeSymbol.getElementOid());
               if (null != startSymbol)
               {
                  if (parentLane.getElementOid() != newParentSymbol.getElementOid())
                  {
                     parentLane.getStartEventSymbols().remove(nodeSymbol);

                     newParentSymbol.getStartEventSymbols().add(
                           (StartEventSymbol) nodeSymbol);
                  }
               }
               else
               {
                  if (parentLane.getElementOid() != newParentSymbol.getElementOid())
                  {
                     parentLane.getEndEventSymbols().remove(nodeSymbol);

                     newParentSymbol.getEndEventSymbols()
                           .add((EndEventSymbol) nodeSymbol);
                  }
               }
            }
            else if (symbolType.equals(ModelerConstants.DATA_SYMBOL))
            {
               if (parentLane.getElementOid() != newParentSymbol.getElementOid())
               {
                  parentLane.getDataSymbol().remove(nodeSymbol);

                  newParentSymbol.getDataSymbol().add((DataSymbolType) nodeSymbol);
               }
            }
         }
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(springContext.getBean(ModelService.class)
            .getModelManagementStrategy());
   }
}
