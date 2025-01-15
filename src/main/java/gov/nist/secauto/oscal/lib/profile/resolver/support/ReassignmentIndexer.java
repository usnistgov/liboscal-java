/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ReassignmentIndexer
    extends BasicIndexer {
  @NonNull
  private final IIdentifierMapper mapper;

  public ReassignmentIndexer(@NonNull IIdentifierMapper mapper) {
    this.mapper = mapper;
  }

  @NonNull
  protected IIdentifierMapper getMapper() {
    return mapper;
  }

  @Override
  protected AbstractEntityItem.Builder newBuilder(
      IModelNodeItem<?, ?> item,
      ItemType itemType,
      String identifier) {
    AbstractEntityItem.Builder builder = super.newBuilder(item, itemType, identifier);

    String reassignment = getMapper().mapByItemType(itemType, identifier);
    if (!identifier.equals(reassignment)) {
      builder.reassignedIdentifier(reassignment);
    }
    return builder;
  }

  @Override
  public IEntityItem getEntity(ItemType itemType, String identifier, boolean normalize) {
    // reassign the identifier
    String reassignment = getMapper().mapByItemType(itemType, identifier);
    // lookup using the reassigned identifier
    return super.getEntity(itemType, reassignment, normalize);
  }
}
