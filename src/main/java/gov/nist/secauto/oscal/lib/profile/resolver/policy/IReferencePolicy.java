/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IReferencePolicy<T> {
  @NonNull
  IReferencePolicy<Object> IGNORE_POLICY = new IReferencePolicy<>() {

    @Override
    public boolean handleReference(
        @NonNull IModelNodeItem<?, ?> contextItem,
        @NonNull Object reference,
        @NonNull ReferenceCountingVisitor.Context referenceVisitorContext) {
      return true;
    }
  };

  /**
   * Get a reference policy that will ignore processing the reference.
   *
   * @param <T>
   *          the type of the reference object
   * @return the policy
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <T> IReferencePolicy<T> ignore() {
    return (IReferencePolicy<T>) IGNORE_POLICY;
  }

  /**
   * Handle the provided {@code reference}.
   *
   * @param contextItem
   *          the nodes containing the reference
   * @param reference
   *          the reference object to process
   * @param referenceVisitorContext
   *          used to lookup and resolve items
   * @return {@code true} if the reference was handled, or {@code false} otherwise
   * @throws ProfileResolutionEvaluationException
   *           if there was an error handing the reference
   */
  boolean handleReference(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull T reference,
      @NonNull ReferenceCountingVisitor.Context referenceVisitorContext);
}
