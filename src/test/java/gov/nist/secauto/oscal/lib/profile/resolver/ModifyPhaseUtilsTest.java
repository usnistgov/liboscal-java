/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class ModifyPhaseUtilsTest {

  @Test
  void testMergeOrdering() {
    List<TestItem> originalItems = List.of(
        item("A"),
        item("id1", "B"),
        item("C"));

    List<TestItem> newItems = List.of(
        item("D"),
        item("id1", "E"),
        item("F"));

    List<TestItem> result
        = ModifyPhaseUtils.merge(originalItems, newItems, ModifyPhaseUtils.identifierKey(TestItem::getIdentifier));

    assertEquals(
        List.of("A", "C", "D", "E", "F"),
        result.stream()
            .map(item -> item.getValue())
            .collect(Collectors.toList()));
  }

  private static TestItem item(@NonNull String value) {
    return item(null, value);
  }

  private static TestItem item(@Nullable String identifier, @NonNull String value) {
    return new TestItem(identifier, value);
  }

  private static final class TestItem {
    @Nullable
    private final String identifier;
    @NonNull
    private final String value;

    private TestItem(@Nullable String identifier, @NonNull String value) {
      this.identifier = identifier;
      this.value = value;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return new StringBuffer()
          .append('[')
          .append(getIdentifier())
          .append(',')
          .append(getValue())
          .append(']')
          .toString();
    }
  }
}
