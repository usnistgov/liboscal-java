/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IIdentifierParser {
  @NonNull
  IIdentifierParser FRAGMENT_PARSER = new PatternIdentifierParser("^#([^#]+)(?:#.*)?$", 1);
  @NonNull
  IIdentifierParser IDENTITY_PARSER = new IIdentifierParser() {

    @Override
    public String parse(@NonNull String reference) {
      return reference;
    }

    @Override
    public String update(@NonNull String reference, @NonNull String newIdentifier) {
      return newIdentifier;
    }
  };

  /**
   * Parse the {@code referenceText} for the identifier.
   *
   * @param referenceText
   *          the reference text containing the identifier
   * @return the identifier, or {@code null} if the identifier could not be parsed
   */
  @Nullable
  String parse(@NonNull String referenceText);

  /**
   * Substitute the provided {@code newIdentifier} with the identifier in the
   * {@code referenceText}.
   *
   * @param referenceText
   *          the reference text containing the original identifier
   * @param newIdentifier
   *          the new identifier to replace the existing identifier
   * @return the updated reference text with the identifier replaced
   * @throws ProfileResolutionEvaluationException
   *           if the identifier could not be updated
   */
  @NonNull
  String update(@NonNull String referenceText, @NonNull String newIdentifier);
}
