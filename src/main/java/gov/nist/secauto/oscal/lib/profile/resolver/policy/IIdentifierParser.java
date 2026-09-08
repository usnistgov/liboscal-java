/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
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
