/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Matching;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControl;
import gov.nist.secauto.oscal.lib.model.control.profile.IProfileSelectControlById;

import org.apache.commons.lang3.tuple.Pair;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class DefaultControlSelectionFilterTest {
  @RegisterExtension
  final JUnit5Mockery context = new JUnit5Mockery() {
    {
      setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }
  };

  @Mock
  private IProfileSelectControlById selectControlByIdA;
  @Mock
  private Matching matchingA;
  @Mock
  private Matching matchingB;
  @Mock
  private IProfileSelectControlById selectControlByIdB;

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  @NonNull
  private IControlSelectionFilter newEmptyFilter() {
    context.checking(new Expectations() {
      {
        allowing(selectControlByIdA).getWithChildControls();
        will(returnValue("no"));
        allowing(selectControlByIdA).getWithIds();
        will(returnValue(Collections.emptyList()));
        allowing(selectControlByIdA).getMatching();
        will(returnValue(Collections.emptyList()));
      }
    });

    return new DefaultControlSelectionFilter(
        ObjectUtils.notNull(List.of(selectControlByIdA)));
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  @NonNull
  private IControlSelectionFilter newSingleSelectionFilter() {
    final List<String> withIds = List.of("test1", "test2");
    context.checking(new Expectations() {
      {
        allowing(selectControlByIdA).getWithChildControls();
        will(returnValue("no"));
        allowing(selectControlByIdA).getWithIds();
        will(returnValue(withIds));
        allowing(selectControlByIdA).getMatching();
        will(returnValue(List.of(matchingA)));
        allowing(matchingA).getPattern();
        will(returnValue("other*"));
      }
    });

    return new DefaultControlSelectionFilter(
        ObjectUtils.notNull(List.of(selectControlByIdA)));
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  @NonNull
  private IControlSelectionFilter newSingleSelectionWithChildFilter() {
    final List<String> withIds = List.of("test1", "test2");
    context.checking(new Expectations() {
      {
        allowing(selectControlByIdA).getWithChildControls();
        will(returnValue("yes"));
        allowing(selectControlByIdA).getWithIds();
        will(returnValue(withIds));
        allowing(selectControlByIdA).getMatching();
        will(returnValue(List.of(matchingA)));
        allowing(matchingA).getPattern();
        will(returnValue("other*"));
      }
    });

    return new DefaultControlSelectionFilter(
        ObjectUtils.notNull(List.of(selectControlByIdA)));
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  @NonNull
  private IControlSelectionFilter newMultipleSelectionFilter() {
    final List<String> withIdsA = List.of("test1", "test2", "example1");
    final List<String> withIdsB = List.of("test3", "test4");
    context.checking(new Expectations() {
      {
        allowing(selectControlByIdA).getWithChildControls();
        will(returnValue("yes"));
        allowing(selectControlByIdA).getWithIds();
        will(returnValue(withIdsA));
        allowing(selectControlByIdA).getMatching();
        will(returnValue(List.of(matchingA)));
        allowing(matchingA).getPattern();
        will(returnValue("other*"));

        allowing(selectControlByIdB).getWithChildControls();
        will(returnValue("no"));
        allowing(selectControlByIdB).getWithIds();
        will(returnValue(withIdsB));
        allowing(selectControlByIdB).getMatching();
        will(returnValue(List.of(matchingB)));
        allowing(matchingB).getPattern();
        will(returnValue("example1*"));
      }
    });

    return new DefaultControlSelectionFilter(
        ObjectUtils.notNull(List.of(selectControlByIdA, selectControlByIdB)));
  }

  /**
   * Test the filtering of an empty set of match criteria.
   */
  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  void testEmpty() {
    final IControl control1 = context.mock(IControl.class);
    context.checking(new Expectations() {
      {
        allowing(control1).getId();
        will(returnValue("test"));
      }
    });

    IControlSelectionFilter filter = newEmptyFilter();
    Pair<Boolean, Boolean> pair = filter.apply(control1);
    assertFalse(pair.getLeft());
    assertFalse(pair.getRight());
  }

  /**
   * Test the filtering of a single match criteria using "with-ids".
   */
  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  void testSingleSelectionWithIdsFilter() {
    final IControl control1 = context.mock(IControl.class, "control1");
    final IControl control2 = context.mock(IControl.class, "control2");
    context.checking(new Expectations() {
      {
        allowing(control1).getId();
        will(returnValue("test"));
        allowing(control2).getId();
        will(returnValue("test2"));
      }
    });

    IControlSelectionFilter filter = newSingleSelectionFilter();
    Pair<Boolean, Boolean> pair = filter.apply(control1);
    assertFalse(pair.getLeft());
    assertFalse(pair.getRight());

    pair = filter.apply(control2);
    assertTrue(pair.getLeft());
    assertFalse(pair.getRight());
  }

  /**
   * Test the filtering of a single match criteria using "with-ids" and
   * "with-child=yes".
   */
  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  void testSingleSelectionWithIdsWithChildFilter() {
    final IControl control1 = context.mock(IControl.class, "control1");
    final IControl control2 = context.mock(IControl.class, "control2");
    context.checking(new Expectations() {
      {
        allowing(control1).getId();
        will(returnValue("test"));
        allowing(control2).getId();
        will(returnValue("test2"));
      }
    });

    IControlSelectionFilter filter = newSingleSelectionWithChildFilter();
    Pair<Boolean, Boolean> pair = filter.apply(control1);
    assertFalse(pair.getLeft());
    assertFalse(pair.getRight());

    pair = filter.apply(control2);
    assertTrue(pair.getLeft());
    assertTrue(pair.getRight());
  }

  /**
   * Test the filtering of multiple match criteria.
   */
  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  void testMultipleSelectionFilter() {
    final IControl control1 = context.mock(IControl.class, "control1");
    final IControl control2 = context.mock(IControl.class, "control2");
    final IControl control3 = context.mock(IControl.class, "control3");
    final IControl control4 = context.mock(IControl.class, "control4");
    final IControl control5 = context.mock(IControl.class, "control5");
    final IControl control6 = context.mock(IControl.class, "control6");
    final IControl control7 = context.mock(IControl.class, "control7");
    context.checking(new Expectations() {
      {
        allowing(control1).getId();
        will(returnValue("test"));
        allowing(control2).getId();
        will(returnValue("test2"));
        allowing(control3).getId();
        will(returnValue("test4"));
        allowing(control4).getId();
        will(returnValue("other1"));
        allowing(control5).getId();
        will(returnValue("other"));
        allowing(control6).getId();
        will(returnValue("example1"));
        allowing(control7).getId();
        will(returnValue("example11"));
      }
    });

    IControlSelectionFilter filter = newMultipleSelectionFilter();
    Pair<Boolean, Boolean> pair = filter.apply(control1);
    assertFalse(pair.getLeft());
    assertFalse(pair.getRight());

    pair = filter.apply(control2);
    assertTrue(pair.getLeft());
    assertTrue(pair.getRight());

    pair = filter.apply(control3);
    assertTrue(pair.getLeft());
    assertFalse(pair.getRight());

    // test match patterns
    pair = filter.apply(control4);
    assertTrue(pair.getLeft());
    assertTrue(pair.getRight());

    pair = filter.apply(control5);
    assertTrue(pair.getLeft());
    assertTrue(pair.getRight());

    pair = filter.apply(control6);
    assertTrue(pair.getLeft());
    assertTrue(pair.getRight());

    pair = filter.apply(control7);
    assertTrue(pair.getLeft());
    assertFalse(pair.getRight());
  }
}
