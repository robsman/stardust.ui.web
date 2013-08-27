/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 *
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
    "bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils",
    "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController" ],

function(m_utils, m_constants, m_model, m_i18nUtils, m_command,
    m_commandsController) {
  return {
    cloneModel : function(node) {
      var model = m_model.findModel(node.attr('id'));

      var cloneModelCmd = m_command.createCommandDescriptor('model.clone', '',
          {
            'modelId' : model.id,
          });
      m_commandsController.submitCommand(cloneModelCmd);
    }
  };
});