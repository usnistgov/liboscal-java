/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.IncludeAll;
import gov.nist.secauto.oscal.lib.model.ProfileImport;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControl;
import gov.nist.secauto.oscal.lib.model.control.profile.IProfileSelectControlById;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IControlFilter {
  @NonNull
  IControlFilter ALWAYS_MATCH = new IControlFilter() {
    @Override
    public @NonNull
    Pair<Boolean, Boolean> match(@NonNull IControl control, boolean defaultMatch) {
      return IControlSelectionFilter.MATCH;
    }

    @Override
    public @NonNull
    IControlSelectionFilter getInclusionFilter() {
      return IControlSelectionFilter.ALL_MATCH;
    }

    @Override
    public @NonNull
    IControlSelectionFilter getExclusionFilter() {
      return IControlSelectionFilter.NONE_MATCH;
    }
  };

  @NonNull
  IControlFilter NONE_MATCH = new IControlFilter() {

    @Override
    public @NonNull
    Pair<Boolean, Boolean> match(@NonNull IControl control, boolean defaultMatch) {
      return IControlSelectionFilter.NON_MATCH;
    }

    @Override
    public @NonNull
    IControlSelectionFilter getInclusionFilter() {
      return IControlSelectionFilter.NONE_MATCH;
    }

    @Override
    public @NonNull
    IControlSelectionFilter getExclusionFilter() {
      return IControlSelectionFilter.NONE_MATCH;
    }
  };

  /**
   * Construct a new filter instance based on the provided profile import
   * statement.
   *
   * @param profileImport
   *          an OSCAL profile import statement
   * @return a new control filter
   */
  @NonNull
  static IControlFilter newInstance(@NonNull ProfileImport profileImport) {
    return new Filter(profileImport);
  }

  @NonNull
  static IControlFilter newInstance(@NonNull IControlSelectionFilter includes,
      @NonNull IControlSelectionFilter excludes) {
    return new Filter(includes, excludes);
  }

  /**
   * Determines if the control is matched by this filter. This method returns a
   * {@link Pair} where the first member of the pair indicates if the control
   * matches, and the second indicates if the match applies to child controls as
   * well.
   *
   * @param control
   *          the control to check for a match
   * @return a pair indicating the status of the match ({@code true} for a match
   *         or {@code false} otherwise), and if a match applies to child controls
   */
  @NonNull
  default Pair<Boolean, Boolean> match(@NonNull IControl control) {
    return match(control, false);
  }

  /**
   * Determines if the control is matched by this filter. This method returns a
   * {@link Pair} where the first member of the pair indicates if the control
   * matches, and the second indicates if the match applies to child controls as
   * well.
   *
   * @param control
   *          the control to check for a match
   * @param defaultMatch
   *          the match status to use if the filter doesn't have an explicit hit
   * @return a pair indicating the status of the match ({@code true} for a match
   *         or {@code false} otherwise), and if a match applies to child controls
   */
  @NonNull
  Pair<Boolean, Boolean> match(@NonNull IControl control, boolean defaultMatch);

  @NonNull
  IControlSelectionFilter getInclusionFilter();

  @NonNull
  IControlSelectionFilter getExclusionFilter();

  class Filter implements IControlFilter {
    @NonNull
    private final IControlSelectionFilter inclusionFilter;
    @NonNull
    private final IControlSelectionFilter exclusionFilter;

    public Filter(@NonNull ProfileImport profileImport) {
      IncludeAll includeAll = profileImport.getIncludeAll();

      if (includeAll == null) {
        List<? extends IProfileSelectControlById> selections = profileImport.getIncludeControls();
        if (selections == null) {
          this.inclusionFilter = IControlSelectionFilter.NONE_MATCH;
        } else {
          this.inclusionFilter = new DefaultControlSelectionFilter(selections);
        }
      } else {
        this.inclusionFilter = IControlSelectionFilter.ALL_MATCH;
      }

      List<? extends IProfileSelectControlById> selections = profileImport.getExcludeControls();
      if (selections == null) {
        this.exclusionFilter = IControlSelectionFilter.NONE_MATCH;
      } else {
        this.exclusionFilter = new DefaultControlSelectionFilter(selections);
      }

    }

    public Filter(@NonNull IControlSelectionFilter includes, @NonNull IControlSelectionFilter excludes) {
      this.inclusionFilter = includes;
      this.exclusionFilter = excludes;
    }

    @Override
    @NonNull
    public IControlSelectionFilter getInclusionFilter() {
      return inclusionFilter;
    }

    @Override
    @NonNull
    public IControlSelectionFilter getExclusionFilter() {
      return exclusionFilter;
    }

    @Override
    public Pair<Boolean, Boolean> match(@NonNull IControl control, boolean defaultMatch) {
      @NonNull
      Pair<Boolean, Boolean> result = getInclusionFilter().apply(control);
      boolean left = ObjectUtils.notNull(result.getLeft());
      if (left) {
        // this is a positive include match. Is it excluded?
        Pair<Boolean, Boolean> excluded = getExclusionFilter().apply(control);
        if (ObjectUtils.notNull(excluded.getLeft())) {
          // the effective result is a non-match
          result = IControlSelectionFilter.NON_MATCH;
        }
      } else {
        result = defaultMatch ? IControlSelectionFilter.MATCH : IControlSelectionFilter.NON_MATCH;
      }
      return result;
    }

  }

}
