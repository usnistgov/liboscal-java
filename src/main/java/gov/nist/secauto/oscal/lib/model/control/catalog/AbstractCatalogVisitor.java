/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractCatalogVisitor<RESULT, CONTEXT> implements ICatalogVisitor<RESULT, CONTEXT> {

  /**
   * Get a new initial/default result for the visitor.
   *
   * @return the result
   */
  protected abstract RESULT defaultResult();

  /**
   * Aggregate two results into one.
   *
   * @param previous
   *          the first result
   * @param current
   *          the next result
   * @return the result produced by combining the two results
   */
  protected RESULT aggregateResult(RESULT previous, RESULT current) {
    return current;
  }

  @Override
  public RESULT visitCatalog(Catalog catalog, CONTEXT context) {
    RESULT result = CollectionUtil.listOrEmpty(catalog.getGroups()).stream()
        .filter(Objects::nonNull)
        .map(childGroup -> visitGroup(ObjectUtils.notNull(childGroup), context))
        .reduce(defaultResult(), this::aggregateResult);
    result = CollectionUtil.listOrEmpty(catalog.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(result, this::aggregateResult);
    return CollectionUtil.listOrEmpty(catalog.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, this::aggregateResult);
  }

  @Override
  public RESULT visitGroup(@NonNull CatalogGroup group, CONTEXT context) {
    RESULT result = CollectionUtil.listOrEmpty(group.getGroups()).stream()
        .filter(Objects::nonNull)
        .map(childGroup -> visitGroup(ObjectUtils.notNull(childGroup), context))
        .reduce(defaultResult(), this::aggregateResult);
    result = CollectionUtil.listOrEmpty(group.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(result, this::aggregateResult);
    return CollectionUtil.listOrEmpty(group.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, this::aggregateResult);
  }

  @Override
  public RESULT visitControl(Control control, CONTEXT context) {
    RESULT result = CollectionUtil.listOrEmpty(control.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(defaultResult(), this::aggregateResult);
    return CollectionUtil.listOrEmpty(control.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, this::aggregateResult);
  }

  @Override
  public RESULT visitParameter(Parameter parameter, CONTEXT context) {
    return defaultResult();
  }

}
