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

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.EnumSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractIndexingVisitor<T, R>
    extends AbstractCatalogEntityVisitor<T, R> {

  public AbstractIndexingVisitor() {
    this(ObjectUtils.notNull(EnumSet.allOf(IEntityItem.ItemType.class)));
  }

  public AbstractIndexingVisitor(@NonNull Set<IEntityItem.ItemType> itemTypesToIndex) {
    super(itemTypesToIndex);
  }

  @NonNull
  protected abstract IIndexer getIndexer(T state);

  @Override
  public R visitGroup(IRequiredValueModelNodeItem item, R childResult, T state) {
    getIndexer(state).addGroup(item);
    return childResult;
  }

  @Override
  public R visitControl(IRequiredValueModelNodeItem item, R childResult, T state) {
    getIndexer(state).addControl(item);
    return childResult;
  }

  @Override
  protected R visitParameter(@NonNull IRequiredValueModelNodeItem parameterItem,
      @NonNull IRequiredValueModelNodeItem catalogOrGroupOrControl, T state) {
    getIndexer(state).addParameter(parameterItem);
    return newDefaultResult(state);
  }

  @Override
  protected void visitPart(@NonNull IRequiredValueModelNodeItem partItem,
      @NonNull IRequiredValueModelNodeItem catalogOrGroupOrControl, T state) {
    getIndexer(state).addPart(partItem);
  }

  @Override
  protected void visitRole(IRequiredValueModelNodeItem roleItem, IRequiredValueModelNodeItem metadataItem, T state) {
    getIndexer(state).addRole(roleItem);
  }

  @Override
  protected void visitLocation(IRequiredValueModelNodeItem locationItem, IRequiredValueModelNodeItem metadataItem,
      T state) {
    getIndexer(state).addLocation(locationItem);
  }

  @Override
  protected void visitParty(IRequiredValueModelNodeItem partyItem, IRequiredValueModelNodeItem metadataItem, T state) {
    getIndexer(state).addParty(partyItem);
  }

  @Override
  protected void visitResource(IRequiredValueModelNodeItem resourceItem, IRootAssemblyNodeItem rootItem, T state) {
    getIndexer(state).addResource(resourceItem);
  }
}
