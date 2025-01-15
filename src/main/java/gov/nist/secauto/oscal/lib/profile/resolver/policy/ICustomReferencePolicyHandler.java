/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ICustomReferencePolicyHandler<TYPE> {
  @NonNull
  ICustomReferencePolicyHandler<?> IGNORE_INDEX_MISS_POLICY = new AbstractIndexMissPolicyHandler<>() {
    @Override
    public boolean handleIndexMiss(
        @NonNull ICustomReferencePolicy<Object> policy,
        @NonNull Object type,
        @NonNull List<IEntityItem.ItemType> itemTypes,
        @NonNull String identifier,
        @NonNull IReferenceVisitor<?> visitor) {
      // do nothing
      return true;
    }
  };

  /**
   * A callback used to handle the case where an identifier could not be parsed
   * from the reference text.
   *
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false}
   *         otherwise
   */
  default boolean handleIdentifierNonMatch(
      @NonNull ICustomReferencePolicy<TYPE> policy,
      @NonNull TYPE reference,
      @NonNull IReferenceVisitor<?> visitor) {
    return false;
  }

  /**
   * A callback used to handle the case where an identifier could be parsed from
   * the reference text, but the index didn't contain a matching entity.
   *
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param itemTypes
   *          the item types that were checked
   * @param identifier
   *          the parsed identifier
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false}
   *         otherwise
   */
  default boolean handleIndexMiss(
      @NonNull ICustomReferencePolicy<TYPE> policy,
      @NonNull TYPE reference,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull IReferenceVisitor<?> visitor) {
    return false;
  }

  /**
   * A callback used to handle the case where an identifier could be parsed and
   * the index contains a matching entity.
   *
   * @param policy
   *          the reference policy for this reference
   * @param reference
   *          the reference object
   * @param item
   *          the entity that is referenced
   * @param visitor
   *          the reference visitor used to resolve referenced objects
   * @return {@code true} if the reference is considered handled, or {@code false}
   *         otherwise
   */
  default boolean handleIndexHit(
      @NonNull ICustomReferencePolicy<TYPE> policy,
      @NonNull TYPE reference,
      @NonNull IEntityItem item,
      @NonNull IReferenceVisitor<?> visitor) {
    return false;
  }
}
