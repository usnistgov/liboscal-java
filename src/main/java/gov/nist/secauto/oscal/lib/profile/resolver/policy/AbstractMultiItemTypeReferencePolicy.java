/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractMultiItemTypeReferencePolicy<TYPE>
    extends AbstractCustomReferencePolicy<TYPE> {

  @NonNull
  private final List<IEntityItem.ItemType> itemTypes;

  public AbstractMultiItemTypeReferencePolicy(
      @NonNull IIdentifierParser identifierParser,
      @NonNull List<IEntityItem.ItemType> itemTypes) {
    super(identifierParser);
    this.itemTypes = CollectionUtil.requireNonEmpty(itemTypes, "itemTypes");
  }

  @Override
  protected List<IEntityItem.ItemType> getEntityItemTypes(@NonNull TYPE type) {
    return itemTypes;
  }
}
