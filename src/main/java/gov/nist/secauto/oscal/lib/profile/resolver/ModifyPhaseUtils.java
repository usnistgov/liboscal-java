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

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ModifyPhaseUtils {
  private ModifyPhaseUtils() {
    // disable construction
  }

  public static <T> Function<? super T, String> identityKey() {
    return (item) -> Integer.toString(Objects.hashCode(item));
  }

  public static <T, R> Function<? super T, String> identifierKey(@NonNull Function<T, R> identifierFunction) {
    return (item) -> {
      R identifier = identifierFunction.apply(item);
      String retval;
      if (identifier == null) {
        retval = Integer.toString(Objects.hashCode(item));
      } else {
        retval = identifier.toString();
      }
      return retval;
    };
  }

  @SuppressWarnings("PMD.OnlyOneReturn") // readability
  public static <T> T mergeItem(@Nullable T original, @Nullable T additional) {
    if (additional == null) {
      return original;
    }

    return additional;
  }

  @SuppressWarnings("PMD.OnlyOneReturn") // readability
  public static <T> List<T> merge(@Nullable List<T> original, @Nullable List<T> additional,
      Function<? super T, String> keyFunction) {
    if (additional == null || additional.isEmpty()) {
      return original;
    }

    if (original == null || original.isEmpty()) {
      return additional;
    }

    // reverse the stream
    List<T> reversed = Stream.concat(
        CollectionUtil.listOrEmpty(original).stream(),
        CollectionUtil.listOrEmpty(additional).stream())
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            l -> {
              Collections.reverse(l);
              return l;
            }));

    // build a map of each unique identity
    Map<String, List<T>> identityMap = reversed.stream()
        .collect(Collectors.groupingBy(keyFunction, LinkedHashMap::new, Collectors.toList()));

    // build a reversed list of items, using the first item
    return identityMap.values().stream()
        .map(list -> list.stream().findFirst().get())
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            l -> {
              Collections.reverse(l);
              return l;
            }));
  }
}
