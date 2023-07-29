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

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IControlSelectionFilter extends Function<IControl, Pair<Boolean, Boolean>> {

  @NonNull
  Pair<Boolean, Boolean> NON_MATCH = ObjectUtils.notNull(Pair.of(false, false));
  @NonNull
  Pair<Boolean, Boolean> MATCH = ObjectUtils.notNull(Pair.of(true, true));

  @NonNull
  IControlSelectionFilter ALL_MATCH = new IControlSelectionFilter() {
    @Override
    public Pair<Boolean, Boolean> apply(IControl control) {
      return MATCH;
    }
  };

  @NonNull
  IControlSelectionFilter NONE_MATCH = new IControlSelectionFilter() {
    @Override
    public Pair<Boolean, Boolean> apply(IControl control) {
      return NON_MATCH;
    }
  };

  @NonNull
  static IControlSelectionFilter matchIds(@NonNull String... identifiers) {
    return new IControlSelectionFilter() {
      private Set<String> keys = Arrays.stream(identifiers).collect(Collectors.toUnmodifiableSet());

      @Override
      public @NonNull Pair<Boolean, Boolean> apply(IControl control) {
        return ObjectUtils.notNull(Pair.of(keys.contains(control.getId()), false));
      }

    };
  }

  /**
   * Determines if the control is matched by this filter. This method returns a {@link Pair} where the
   * first member of the pair indicates if the control matches, and the second indicates if the match
   * applies to child controls as well.
   *
   * @param control
   *          the control to check for a match
   * @return a pair indicating the status of the match ({@code true} for a match or {@code false}
   *         otherwise), and if a match applies to child controls
   */
  @NonNull
  @Override
  Pair<Boolean, Boolean> apply(IControl control);
}
