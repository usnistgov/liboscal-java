/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
    return item -> Integer.toString(Objects.hashCode(item));
  }

  public static <T, R> Function<? super T, String> identifierKey(@NonNull Function<T, R> identifierFunction) {
    return item -> {
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
