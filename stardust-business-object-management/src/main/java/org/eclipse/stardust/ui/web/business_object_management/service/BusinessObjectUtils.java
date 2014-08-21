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
import java.sql.Clob;
import java.sql.ResultSet;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean.DataValueChangeListener;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.ui.web.business_object_management.service.BusinessObject.Definition;
import org.eclipse.stardust.ui.web.business_object_management.service.BusinessObject.Value;

public class BusinessObjectUtils
{
    private static final String BUSINESS_OBJECT_ATT = PredefinedConstants.MODEL_SCOPE + "BusinessObject";

    public static BusinessObjects getBusinessObjects(BusinessObjectQuery query)
    {
        final ModelManager modelManager = ModelManagerFactory.getCurrent();

        BusinessObjectQuery.Policy policy = (BusinessObjectQuery.Policy) query.getPolicy(
                BusinessObjectQuery.Policy.class);

        final boolean withDescription = policy == null ? false : policy.hasOption(BusinessObjectQuery.Option.WITH_DESCRIPTION);
        final boolean withValues = policy == null ? false : policy.hasOption(BusinessObjectQuery.Option.WITH_VALUES);

        BusinessObjectQueryEvaluator queryEvaluator = new BusinessObjectQueryEvaluator(query);
        Set<IData> allData = collectData(modelManager, queryEvaluator);
        if (allData.isEmpty())
        {
            return new BusinessObjects(query, Collections.<BusinessObject>emptyList());
        }

        if (withValues && allData.size() > 1)
        {
            // TODO: check separately model and data
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("query","Model or data not specified."));
        }

        final Map<IData, List<BusinessObject.Value>> values = withValues
                ? fetchValues(allData.iterator().next(), queryEvaluator.getPkValue(), query.getFilter()) : null;
                if (values != null)
                {
                    allData = values.keySet();
                }

                Functor<IData, BusinessObject> transformer = new Functor<IData, BusinessObject>() {
                    public BusinessObject execute(IData source)
                    {
                        List<Definition> items = null;
                        if (withDescription)
                        {
                            BusinessObject bo = getBusinessObject(source);
                            if (!withValues)
                            {
                                return bo;
                            }
                            items = bo.getItems();
                        }
                        return new BusinessObjectDetails(source.getModel().getModelOID(), source.getId(), source.getName(), items, values == null ? null : values.get(source));
                    }
                };

                return new BusinessObjects(query, CollectionUtils.newListFromIterator(
                        new TransformingIterator<IData, BusinessObject>(allData.iterator(), transformer)));
    }

    private static Set<IData> collectData(final ModelManager modelManager, BusinessObjectQueryEvaluator queryEvaluator)
    {
        Set<IData> allData = CollectionUtils.newSet();

        Long modelOID = queryEvaluator.getModelOid();
        if (modelOID != null)
        {
            IModel model = modelManager.findModel(modelOID);
            if (model != null)
            {
                addModelData(allData, model, queryEvaluator);
            }
        }
        else
        {
            for (Iterator<IModel> models = modelManager.getAllModels(); models.hasNext();)
            {
                addModelData(allData, models.next(), queryEvaluator);
            }
        }
        return allData;
    }

    private static void addModelData(Set<IData> allData, IModel model, BusinessObjectQueryEvaluator queryEvaluator)
    {
        if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
        {
            for (Iterator<IData> data = model.getData().iterator(); data.hasNext();)
            {
                IData item = data.next();
                if (queryEvaluator.accept(item))
                {
                    allData.add(item);
                }
            }
        }
    }

    private static Map<IData, List<Value>> fetchValues(IData data, Object pkValue, FilterAndTerm term)
    {
        ProcessInstanceQuery pi = ProcessInstanceQuery.findAll();
        copyDataFilters(term, pi.getFilter());
        ProcessInstanceQueryEvaluator eval = new ProcessInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
        ParsedQuery parsedQuery = eval.parseQuery();
        PredicateTerm parsedTerm = filter(parsedQuery.getPredicateTerm());

        Map<IData, List<BusinessObject.Value>> values = CollectionUtils.newMap();

        long modelOID = data.getModel().getModelOID();
        long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);
        String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
        int typeClassification = LargeStringHolderBigDataHandler.classifyType(data, pk);

        QueryDescriptor desc = QueryDescriptor
                .from(ProcessInstanceBean.class)
                .select(ProcessInstanceBean.FR__OID, ClobDataBean.FR__STRING_VALUE);
        Join dvJoin = desc
                .innerJoin(DataValueBean.class)
                        .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);
        desc
                .innerJoin(ClobDataBean.class)
                        .on(dvJoin.fieldRef(DataValueBean.FIELD__NUMBER_VALUE), ClobDataBean.FIELD__OID);
        Join sdvJoin = desc
                .innerJoin(StructuredDataValueBean.class)
                        .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
        Join sdJoin = desc
                .innerJoin(StructuredDataBean.class)
                        .on(sdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
                        .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
                        .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);

        FieldRef pkValueField = null;
        switch (typeClassification)
        {
        case BigData.STRING_VALUE:
            pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE);
            break;
        case BigData.NUMERIC_VALUE:
            pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE);
            break;
        default:
            // (fh) throw internal exception ?
        }

        List<PredicateTerm> predicates = CollectionUtils.newList();
        predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
        predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID));
        predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID));
        predicates.add(Predicates.isEqual(sdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk));
        if (pkValue instanceof Number)
        {
            predicates.add(Predicates.isEqual(pkValueField, ((Number) pkValue).longValue()));
        }
        else if (pkValue != null)
        {
            predicates.add(Predicates.isEqual(pkValueField, pkValue.toString()));
        }
        if (parsedTerm != null)
        {
            predicates.add(parsedTerm);
        }

        desc.where(new AndTerm(predicates.toArray(new PredicateTerm[predicates.size()])));

        Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
        System.err.println(session.getDMLManager(desc.getType()).prepareSelectStatement(desc, true, null, true));

        ResultSet resultSet = session.executeQuery(desc);
        try
        {
            while (resultSet.next())
            {
                long piOid = resultSet.getLong(1);
                Clob clob = resultSet.getClob(2);

                Document document = DocumentBuilder.buildDocument(clob.getCharacterStream());
                boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);
                final IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
                StructuredDataConverter converter = new StructuredDataConverter(xPathMap);

                List<BusinessObject.Value> list = values.get(data);
                if (list == null)
                {
                    list = CollectionUtils.newList();
                    values.put(data, list);
                }
                Object value = converter.toCollection(document.getRootElement(), "", namespaceAware);
                list.add(new BusinessObjectDetails.ValueDetails(piOid, (Serializable) value));
            }
        }
        catch (Exception e)
        {
            throw new PublicException(e);
        }
        finally
        {
            QueryUtils.closeResultSet(resultSet);
        }

        return values;
    }

    private static IProcessInstance findUnboundProcessInstance(IData data, Object pkValue)
    {
        long modelOID = data.getModel().getModelOID();
        long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);
        String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
        int typeClassification = LargeStringHolderBigDataHandler.classifyType(data, pk);

        QueryDescriptor desc = QueryDescriptor.from(ProcessInstanceBean.class);
        Join dvJoin = desc.innerJoin(DataValueBean.class).on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);
        Join sdvJoin = desc.innerJoin(StructuredDataValueBean.class).on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
        Join sdJoin = desc.innerJoin(StructuredDataBean.class).on(sdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
                .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
                .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);

        FieldRef pkValueField = null;
        switch (typeClassification)
        {
        case BigData.STRING_VALUE:
            pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE);
            break;
        case BigData.NUMERIC_VALUE:
            pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE);
            break;
        default:
            // (fh) throw internal exception ?
        }

        desc.where(Predicates.andTerm(
                Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1),
                Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID),
                Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID),
                Predicates.isEqual(sdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk),
                pkValue instanceof Number
                ? Predicates.isEqual(pkValueField, ((Number) pkValue).longValue())
                        : Predicates.isEqual(pkValueField, pkValue.toString())));

        Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
        System.err.println(session.getDMLManager(desc.getType()).prepareSelectStatement(desc, true, null, true));
        return session.findFirst(ProcessInstanceBean.class, desc.getQueryExtension());
    }

    private static Object getPK(IData data, Value value)
    {
        String pkId = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
        Serializable sz = value.getValue();
        if (sz instanceof Map)
        {
            Object pk = ((Map<?, ?>) sz).get(pkId);
            if (pk != null)
            {
                return pk;
            }
        }
        throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("primary key"));
    }

    public static BusinessObject createInstance(long modelOid, String businessObjectId, Value initialValue)
            throws ObjectNotFoundException, InvalidArgumentException
    {
        if (initialValue == null)
        {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("initialValue"));
        }
        lockData(modelOid, businessObjectId);
        IData data = findDataForUpdate(modelOid, businessObjectId);
        IProcessInstance pi = findUnboundProcessInstance(data, getPK(data, initialValue));
        if (pi != null)
        {
            // TODO throw correct exception like ObjectExistsException
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_VALUE.raise("initialValue"));
        }
        pi = ProcessInstanceBean.createUnboundInstance((IModel) data.getModel());
        return updateBusinessObjectInstance(modelOid, businessObjectId, initialValue, data, pi);
    }

    private static void lockData(long modelOid, String businessObjectId) {
        QueryDescriptor lockQuery = QueryDescriptor.from(AuditTrailDataBean.class)
                .where(Predicates.isEqual(AuditTrailDataBean.FR__MODEL, modelOid))
                .where(Predicates.isEqual(AuditTrailDataBean.FR__ID, businessObjectId));
        AuditTrailDataBean atdb = (AuditTrailDataBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
                .findFirst(AuditTrailDataBean.class, lockQuery.getQueryExtension());
        atdb.lock();
    }

    public static BusinessObject updateInstance(long modelOid, String businessObjectId, Value value)
            throws ObjectNotFoundException, InvalidArgumentException
    {
        if (value == null)
        {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("value"));
        }
        IData data = findDataForUpdate(modelOid, businessObjectId);
        IProcessInstance pi = findUnboundProcessInstance(data, getPK(data, value));
        if (pi == null)
        {
            // TODO throw correct error case
            throw new ObjectNotFoundException((ErrorCase) null);
        }
        pi.lock();
        return updateBusinessObjectInstance(modelOid, businessObjectId, value, data, pi);
    }

    public static void deleteInstance(long modelOid, String businessObjectId,
            Object pkValue)
    {
        IData data = findDataForUpdate(modelOid, businessObjectId);
        IProcessInstance pi = findUnboundProcessInstance(data, pkValue);
        if (pi == null)
        {
            // TODO throw correct error case
            throw new ObjectNotFoundException((ErrorCase) null);
        }
        pi.lock();
        ProcessInstanceUtils.deleteProcessInstances(Collections.singletonList(pi.getOID()),
                (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL));
    }

    private static BusinessObject updateBusinessObjectInstance(long modelOid,
            String businessObjectId, Value newValue, IData data, IProcessInstance pi)
    {
        pi.setOutDataValue(data, null, newValue.getValue());
        Serializable dataValue = (Serializable) pi.getInDataValue(data, null);
        BusinessObjectDetails.Value value = new BusinessObjectDetails.ValueDetails(pi.getOID(), dataValue);
        return new BusinessObjectDetails(modelOid, businessObjectId, data.getName(), null, Collections.singletonList(value));
    }

    private static IData findDataForUpdate(long modelOid, String businessObjectId)
            throws ObjectNotFoundException, InvalidArgumentException
    {
        final ModelManager modelManager = ModelManagerFactory.getCurrent();
        IModel model = modelManager.findModel(modelOid);
        if (model == null)
        {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOid));
        }
        IData data = model.findData(businessObjectId);
        if (data == null)
        {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(businessObjectId));
        }
        return data;
    }

    private static PredicateTerm filter(PredicateTerm source)
    {
        // TODO: provide generic filtering like PredicateTerm.filter(Predicate)
        if (source instanceof MultiPartPredicateTerm)
        {
            if (((MultiPartPredicateTerm) source).getParts().isEmpty())
            {
                return null;
            }

            MultiPartPredicateTerm target = null;
            if (source instanceof AndTerm)
            {
                target = new AndTerm();
            }
            else if (source instanceof OrTerm)
            {
                target = new OrTerm();
            }
            else if (source instanceof AndNotTerm)
            {
                target = new AndNotTerm();
            }
            else if (source instanceof OrNotTerm)
            {
                target = new OrNotTerm();
            }

            for (PredicateTerm term : ((MultiPartPredicateTerm) source).getParts())
            {
                term = filter(term);
                if (term != null)
                {
                    target.add(term);
                }
            }

            return target.getParts().isEmpty() ? null : target;
        }
        else // Comparison term
        {
            FieldRef fr = ((ComparisonTerm) source).getLhsField();
            Class<?> type = fr.getBoundType();
            if (ProcessInstanceBean.class.equals(type) || ModelPersistorBean.class.equals(type))
            {
                return null;
            }
        }
        return source;
    }

    private static void copyDataFilters(FilterTerm source, FilterTerm target)
    {
        for (Object part : source.getParts())
        {
            if (part instanceof FilterAndTerm)
            {
                copyDataFilters((FilterAndTerm) part, target.addAndTerm());
            }
            else if (part instanceof FilterAndNotTerm)
            {
                copyDataFilters((FilterAndNotTerm) part, target.addAndNotTerm());
            }
            else if (part instanceof FilterOrTerm)
            {
                copyDataFilters((FilterOrTerm) part, target.addOrTerm());
            }
            else if (part instanceof FilterOrNotTerm)
            {
                copyDataFilters((FilterOrNotTerm) part, target.addOrNotTerm());
            }
            else if (part instanceof DataFilter)
            {
                target.add((DataFilter) part);
            }
        }
    }

    public static BusinessObject getBusinessObject(IData source)
    {
        BusinessObject businessObject = source.getRuntimeAttribute(BUSINESS_OBJECT_ATT);
        if (businessObject == null && hasBusinessObject(source))
        {
            synchronized (source)
            {
                businessObject = source.getRuntimeAttribute(BUSINESS_OBJECT_ATT);
                if (businessObject == null)
                {
                    businessObject = createBusinessObject(source);
                    source.setRuntimeAttribute(BUSINESS_OBJECT_ATT, businessObject);
                }
            }
        }
        return businessObject;
    }

    private static BusinessObject createBusinessObject(IData source)
    {
        List<Definition> items = null;
        IXPathMap map = DataXPathMap.getXPathMap(source);
        TypedXPath root = map.getRootXPath();
        if (root != null)
        {
            items = createDescriptions(source, root.getChildXPaths(), true);
        }
        return new BusinessObjectDetails(source.getModel().getModelOID(), source.getId(), source.getName(), items, null);
    }

    private static List<Definition> createDescriptions(IData source, List<TypedXPath> xPaths, boolean top)
    {
        List<Definition> items = CollectionUtils.newList(xPaths.size());
        for (TypedXPath xPath : xPaths)
        {
            boolean primaryKey = top ? xPath.getId().equals(source.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT)) : false;
            items.add(new BusinessObjectDetails.DefinitionDetails(xPath.getId(), xPath.getType(),
                StringUtils.isEmpty(xPath.getXsdTypeName())
                    ? null
                    : StringUtils.isEmpty(xPath.getXsdTypeNs())
                        ? new QName(xPath.getXsdTypeName())
                        : new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName()),
            xPath.isList(),
            xPath.getAnnotations().isIndexed(), primaryKey ,
            xPath.getType() == BigData.NULL ? createDescriptions(source, xPath.getChildXPaths(), false) : null));
        }
        return items;
    }

    public static boolean hasBusinessObject(IData data)
    {
        if (data == null)
        {
            return false;
        }
        PluggableType type = data.getType();
        return type != null
                && PredefinedConstants.STRUCTURED_DATA.equals(type.getId())
                && data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT) != null;
    }

    public static void registerBusinessObjectsListener() {
        ProcessInstanceBean.dataValueChangeListener = new BusinessObjectsListener();
    }

    private static class BusinessObjectsListener implements DataValueChangeListener {

        @Override
        public void onDataValueChanged(IDataValue dv) {
            IProcessInstance pi = dv.getProcessInstance();
            try {
                if (pi.getProcessDefinition() != null) {
                    IData data = dv.getData();
                    if (hasBusinessObject(data)) {
                        Value value = wrapValue((Serializable) pi.getInDataValue(data, null));
                        IProcessInstance upi = findUnboundProcessInstance(data, getPK(data, value));
                        if (upi == null) {
                            lockData(data.getModel().getModelOID(), data.getId());
                            System.err.println("Creating BO from regular PI data.");
                            upi = ProcessInstanceBean.createUnboundInstance((IModel) data.getModel());
                        } else {
                            upi.lock();
                            System.err.println("Updating BO from regular PI data.");
                        }
                        upi.setOutDataValue(data, null, value.getValue());
                    }
                }
            } catch (Exception ex) {
                // (fh) do nothing
            }
        }
    }

    public static Value wrapValue(final Serializable value) {
        return new Value() {
            public long getProcessInstanceOid() {return -1;}
            public Serializable getValue() {return value;}

        };
    }
}
