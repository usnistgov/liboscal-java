/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
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
        .reduce(defaultResult(), (previous, current) -> aggregateResult(previous, current));
    result = CollectionUtil.listOrEmpty(catalog.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(result, (previous, current) -> aggregateResult(previous, current));
    return CollectionUtil.listOrEmpty(catalog.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, (previous, current) -> aggregateResult(previous, current));
  }

  @Override
  public RESULT visitGroup(@NonNull CatalogGroup group, CONTEXT context) {
    RESULT result = CollectionUtil.listOrEmpty(group.getGroups()).stream()
        .filter(Objects::nonNull)
        .map(childGroup -> visitGroup(ObjectUtils.notNull(childGroup), context))
        .reduce(defaultResult(), (previous, current) -> aggregateResult(previous, current));
    result = CollectionUtil.listOrEmpty(group.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(result, (previous, current) -> aggregateResult(previous, current));
    return CollectionUtil.listOrEmpty(group.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, (previous, current) -> aggregateResult(previous, current));
  }

  @Override
  public RESULT visitControl(Control control, CONTEXT context) {
    RESULT result = CollectionUtil.listOrEmpty(control.getControls()).stream()
        .filter(Objects::nonNull)
        .map(childControl -> visitControl(ObjectUtils.notNull(childControl), context))
        .reduce(defaultResult(), (previous, current) -> aggregateResult(previous, current));
    return CollectionUtil.listOrEmpty(control.getParams()).stream()
        .filter(Objects::nonNull)
        .map(childParameter -> visitParameter(ObjectUtils.notNull(childParameter), context))
        .reduce(result, (previous, current) -> aggregateResult(previous, current));
  }

  @Override
  public RESULT visitParameter(Parameter parameter, CONTEXT context) {
    return defaultResult();
  }

}
