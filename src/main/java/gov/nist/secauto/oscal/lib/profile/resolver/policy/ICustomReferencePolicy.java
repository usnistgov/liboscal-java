/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ICustomReferencePolicy<TYPE> extends IReferencePolicy<TYPE> {

  /**
   * Get the parser to use to parse an entity identifier from the reference text.
   *
   * @return the parser
   */
  @NonNull
  IIdentifierParser getIdentifierParser();

  /**
   * Retrieve the reference text from the {@code reference} object.
   *
   * @param reference
   *          the reference object
   * @return the reference text or {@code null} if there is no text
   */
  String getReferenceText(@NonNull TYPE reference);

  /**
   * Update the reference text used in the {@code reference} object.
   *
   * @param reference
   *          the reference object
   * @param newReferenceText
   *          the reference text replacement
   */
  void setReferenceText(@NonNull TYPE reference, @NonNull String newReferenceText);
}
