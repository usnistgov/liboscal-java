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

package gov.nist.secauto.oscal.lib.model.control.profile;

import gov.nist.secauto.oscal.lib.model.ProfileSelectControlById;
import gov.nist.secauto.oscal.lib.model.ProfileSelectControlById.Matching;

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
