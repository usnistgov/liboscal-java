/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

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
  public R visitGroup(IAssemblyNodeItem item, R childResult, T state) {
    getIndexer(state).addGroup(item);
    return childResult;
  }

  @Override
  public R visitControl(IAssemblyNodeItem item, R childResult, T state) {
    getIndexer(state).addControl(item);
    return childResult;
  }

  @Override
  protected R visitParameter(@NonNull IAssemblyNodeItem parameterItem,
      @NonNull IAssemblyNodeItem catalogOrGroupOrControl, T state) {
    getIndexer(state).addParameter(parameterItem);
    return newDefaultResult(state);
  }

  @Override
  protected void visitPart(@NonNull IAssemblyNodeItem partItem,
      @NonNull IAssemblyNodeItem catalogOrGroupOrControl, T state) {
    getIndexer(state).addPart(partItem);
  }

  @Override
  protected void visitRole(IAssemblyNodeItem roleItem, IAssemblyNodeItem metadataItem, T state) {
    getIndexer(state).addRole(roleItem);
  }

  @Override
  protected void visitLocation(IAssemblyNodeItem locationItem, IAssemblyNodeItem metadataItem,
      T state) {
    getIndexer(state).addLocation(locationItem);
  }

  @Override
  protected void visitParty(IAssemblyNodeItem partyItem, IAssemblyNodeItem metadataItem, T state) {
    getIndexer(state).addParty(partyItem);
  }

  @Override
  protected void visitResource(IAssemblyNodeItem resourceItem, IRootAssemblyNodeItem rootItem, T state) {
    getIndexer(state).addResource(resourceItem);
  }
}
