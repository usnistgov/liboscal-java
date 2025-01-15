/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Matching;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControl;
import gov.nist.secauto.oscal.lib.model.control.profile.IProfileSelectControlById;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultControlSelectionFilter implements IControlSelectionFilter {
  private static final Logger LOGGER = LogManager.getLogger(DefaultControlSelectionFilter.class);

  @NonNull
  private final List<Selection> selections;

  /**
   * Construct a new selection filter based on the provided list of select
   * criteria.
   *
   * @param selections
   *          a list of select criteria
   */
  @SuppressWarnings("null")
  public DefaultControlSelectionFilter(@NonNull List<? extends IProfileSelectControlById> selections) {
    this.selections = selections.stream()
        // ignore null entries
        .filter(Objects::nonNull)
        // create a selection object for the selection
        .map(Selection::new)
        .collect(Collectors.toUnmodifiableList());
  }

  @NonNull
  @Override
  public Pair<Boolean, Boolean> apply(IControl control) {
    String id = control.getId();
    if (id == null) {
      throw new ProfileResolutionEvaluationException("control is missing an identifier");
    }
    return match(id);
  }

  /**
   * Checks if the provided control identifier matches the criteria defined by
   * this object.
   *
   * @param id
   *          the control identifier to match
   * @return a {@link Pair} whose first member is {@code true} for a match or
   *         {@code false} otherwise, and whose second member is {@code true} if
   *         the match applies to any child controls or {@code false} otherwise
   */
  @SuppressWarnings("null")
  @NonNull
  protected Pair<Boolean, Boolean> match(String id) {
    return selections.parallelStream()
        .map(selection -> selection.match(id))
        // filter out non-matches
        .filter(Pair::getLeft)
        // aggregate matches
        .reduce((first, second) -> {
          Pair<Boolean, Boolean> result;
          if (first.getLeft() || second.getLeft()) {
            // at least one matches
            boolean withChild = first.getLeft() && first.getRight() || second.getLeft() && second.getRight();
            result = Pair.of(true, withChild);
          } else {
            result = NON_MATCH;
          }
          return result;
        })
        .orElse(NON_MATCH);
  }

  @SuppressWarnings("PMD.ImplicitSwitchFallThrough")
  private static Pattern toPattern(@NonNull Matching matching) {
    String pattern = ObjectUtils.requireNonNull(matching.getPattern());
    String regex = pattern.chars().boxed().map(ch -> (char) ch.intValue()).map(ch -> {

      String value;
      switch (ch) {
      case '*':
        value = ".*";
        break;
      case '?':
        value = ".";
        break;
      case '.':
      case '+':
      case '\\':
      case '[':
      case ']':
      case '{':
      case '}':
      case '(':
      case ')':
      case '^':
      case '$':
        value = "\\" + ch;
        break;
      default:
        value = String.valueOf(ch);
      }
      return value;
    }).collect(Collectors.joining("", "^", "$"));

    if (LOGGER.isTraceEnabled()) {
      LOGGER.atTrace().log("regex: {}", regex);
    }
    return Pattern.compile(regex);
  }

  private static class Selection {

    private final boolean withChildControls;
    private final Set<String> identifiers;
    private final List<Pattern> patterns;

    public Selection(IProfileSelectControlById selection) {
      // process with-child-controls
      // default is "no"
      this.withChildControls = "yes".equals(selection.getWithChildControls());

      // process with-ids
      List<String> ids = selection.getWithIds();
      if (ids == null) {
        ids = Collections.emptyList();
      }
      this.identifiers = ids.stream()
          .filter(Objects::nonNull)
          .collect(Collectors.toUnmodifiableSet());

      // process with-ids
      List<Matching> matching = selection.getMatching();
      if (matching == null) {
        matching = Collections.emptyList();
      }
      this.patterns = matching.stream()
          .filter(Objects::nonNull)
          .map(DefaultControlSelectionFilter::toPattern)
          .collect(Collectors.toUnmodifiableList());
    }

    public boolean isWithChildControls() {
      return withChildControls;
    }

    @NonNull
    protected Pair<Boolean, Boolean> match(String id) {
      // first check for direct match
      boolean result = identifiers.stream().anyMatch(controlIdentifier -> controlIdentifier.equals(id));
      if (!result) {
        // next check for pattern match
        result = patterns.stream().anyMatch(pattern -> pattern.asMatchPredicate().test(id));
      }
      return ObjectUtils.notNull(Pair.of(result, isWithChildControls()));
    }
  }
}
