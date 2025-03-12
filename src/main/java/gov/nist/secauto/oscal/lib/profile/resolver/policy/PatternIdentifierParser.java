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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public class PatternIdentifierParser implements IIdentifierParser {
  private final Pattern pattern;
  private final int identifierGroup;

  @SuppressWarnings("null")
  public PatternIdentifierParser(@NonNull String pattern, int identifierGroup) {
    this(Pattern.compile(pattern), identifierGroup);
  }

  public PatternIdentifierParser(@NonNull Pattern pattern, int identifierGroup) {
    this.pattern = Objects.requireNonNull(pattern, "pattern");
    this.identifierGroup = identifierGroup;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public int getIdentifierGroup() {
    return identifierGroup;
  }

  @Override
  public String parse(@NonNull String referenceText) {
    Matcher matcher = getPattern().matcher(referenceText);

    String retval = null;
    if (matcher.matches()) {
      retval = matcher.group(getIdentifierGroup());
    }
    return retval;
  }

  @Override
  public String update(@NonNull String referenceText, @NonNull String newIdentifier) {
    Matcher matcher = getPattern().matcher(referenceText);
    if (!matcher.matches()) {
      throw new ProfileResolutionEvaluationException(
          String.format("The original reference '%s' did not match the pattern '%s'.",
              referenceText, getPattern().pattern()));
    }

    return ObjectUtils.notNull(new StringBuilder(referenceText)
        .replace(
            matcher.start(getIdentifierGroup()),
            matcher.end(getIdentifierGroup()),
            newIdentifier)
        .toString());
  }
}
