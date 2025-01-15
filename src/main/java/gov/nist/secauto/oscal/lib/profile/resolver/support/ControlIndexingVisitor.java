/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IRootAssemblyNodeItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;

import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A visitor that walks a catalog visiting controls and parameters.
 */
public class ControlIndexingVisitor
    extends AbstractIndexingVisitor<IIndexer, Void> {

  public ControlIndexingVisitor(@NonNull Set<ItemType> itemTypesToIndex) {
    super(itemTypesToIndex);
  }

  @SuppressWarnings("null")
  @Override
  protected IIndexer getIndexer(IIndexer state) {
    return state;
  }

  @Override
  protected Void newDefaultResult(IIndexer state) {
    return null;
  }

  @Override
  protected Void aggregateResults(Void first, Void second, IIndexer state) {
    return null;
  }

  public void visitProfile(@NonNull IRootAssemblyNodeItem root, @NonNull IIndexer index) {
    visitMetadata(root, index);
    visitBackMatter(root, index);
  }
}
