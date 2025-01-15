/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IControlSelectionState {
  @NonNull
  IIndexer getIndex();

  boolean isSelected(@NonNull IModelNodeItem<?, ?> item);
}
