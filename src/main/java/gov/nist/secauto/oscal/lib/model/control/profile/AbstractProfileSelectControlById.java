/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.profile;

import gov.nist.secauto.oscal.lib.model.Matching;
import gov.nist.secauto.oscal.lib.model.ProfileSelectControlById;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractProfileSelectControlById implements IProfileSelectControlById {
  // TODO: move implementation from profile resolver selection code here

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean withChildControls; // false;
    private final List<String> withIds = new LinkedList<>();
    private final List<Pattern> matching = new LinkedList<>();

    @NonNull
    public Builder withChildControls(boolean value) {
      this.withChildControls = value;
      return this;
    }

    @NonNull
    public Builder withId(@NonNull String id) {
      withIds.add(id);
      return this;
    }

    @NonNull
    public Builder withIds(@NonNull Collection<String> ids) {
      withIds.addAll(ids);
      return this;
    }

    @NonNull
    public Builder matching(@NonNull Pattern pattern) {
      matching.add(pattern);
      return this;
    }

    @NonNull
    public ProfileSelectControlById build() {
      ProfileSelectControlById retval = new ProfileSelectControlById();
      retval.setWithChildControls(withChildControls ? "yes" : "no");
      retval.setWithIds(withIds);
      retval.setMatching(matching.stream()
          .map(pattern -> {
            Matching matching = new Matching();
            matching.setPattern(pattern.pattern());
            return matching;
          })
          .collect(Collectors.toList()));
      return retval;
    }
  }
}
