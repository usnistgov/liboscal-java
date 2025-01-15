/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
