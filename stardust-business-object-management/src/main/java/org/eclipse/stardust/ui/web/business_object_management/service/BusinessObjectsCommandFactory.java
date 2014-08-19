/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.business_object_management.service;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public final class BusinessObjectsCommandFactory {

    private static class InstanceCommand implements ServiceCommand {

        private enum Kind {
            CREATE, UPDATE, DELETE
        }

        private static final long serialVersionUID = 1L;

        private Kind kind;
        private long modelOid;
        private String businessObjectId;
        private Value initialValue;
        private Object primaryKey;

        private InstanceCommand(Kind kind, long modelOid,
                String businessObjectId, Value initialValue, Object primaryKey) {
            this.kind = kind;
            this.modelOid = modelOid;
            this.businessObjectId = businessObjectId;
            this.initialValue = initialValue;
            this.primaryKey = primaryKey;
        }

        public Serializable execute(ServiceFactory sf) {
            switch (kind)
            {
            case CREATE:
                return BusinessObjectUtils.createInstance(modelOid, businessObjectId, initialValue);
            case UPDATE:
                return BusinessObjectUtils.updateInstance(modelOid, businessObjectId, initialValue);
            case DELETE:
                BusinessObjectUtils.deleteInstance(modelOid, businessObjectId, primaryKey);
            }
            return null;
        }
    }

    private static class QueryCommand implements ServiceCommand {

        private static final long serialVersionUID = 1L;

        private BusinessObjectQuery query;

        private QueryCommand(BusinessObjectQuery query) {
            this.query = query;
        }

        @Override
        public Serializable execute(ServiceFactory sf) {
            return BusinessObjectUtils.getBusinessObjects(query);
        }

    }

    public static ServiceCommand find(BusinessObjectQuery query) {
        return new QueryCommand(query);
    }

    public static ServiceCommand create(long modelOid,
            String businessObjectId, Value initialValue) {
        return new InstanceCommand(InstanceCommand.Kind.CREATE, modelOid, businessObjectId, initialValue, null);
    }

    public static ServiceCommand update(long modelOid,
            String businessObjectId, Value initialValue) {
        return new InstanceCommand(InstanceCommand.Kind.UPDATE, modelOid, businessObjectId, initialValue, null);
    }

    public static ServiceCommand delete(long modelOid,
            String businessObjectId, Object primaryKey) {
        return new InstanceCommand(InstanceCommand.Kind.DELETE, modelOid, businessObjectId, null, primaryKey);
    }
}
