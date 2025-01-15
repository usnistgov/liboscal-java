/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractIndexMissPolicyHandler<TYPE> implements ICustomReferencePolicyHandler<TYPE> {
  @Override
  public abstract boolean handleIndexMiss(
      @NonNull ICustomReferencePolicy<TYPE> policy,
      @NonNull TYPE type,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull IReferenceVisitor<?> visitor);
}
