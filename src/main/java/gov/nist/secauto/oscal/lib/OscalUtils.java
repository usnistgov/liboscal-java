/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource.Rlink;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class OscalUtils {
  private static final Pattern INTERNAL_REFERENCE_FRAGMENT_PATTERN = Pattern.compile("^#(.+)$");

  private OscalUtils() {
    // disable construction
  }

  @SuppressWarnings("PMD.OnlyOneReturn") // readability
  public static boolean isInternalReference(@NonNull URI uri) {
    if (uri.isAbsolute()) {
      return false;
    }

    String schemeSpecificPart = uri.getSchemeSpecificPart();
    return uri.getScheme() == null && (schemeSpecificPart == null || schemeSpecificPart.isEmpty())
        && uri.getFragment() != null;
  }

  /**
   * Get the id based on a URI's fragment.
   *
   * @param fragment
   *          the URI to extract the identifier from
   * @return the identifier
   * @throws IllegalArgumentException
   *           if the fragment does not contain an identifier
   */
  @NonNull
  public static String internalReferenceFragmentToId(@NonNull URI fragment) {
    return internalReferenceFragmentToId(ObjectUtils.notNull(fragment.toString()));
  }

  /**
   * Get the id based on a URI's fragment.
   *
   * @param fragment
   *          the URI to extract the identifier from
   * @return the identifier
   * @throws IllegalArgumentException
   *           if the fragment does not contain an identifier
   */
  @NonNull
  public static String internalReferenceFragmentToId(@NonNull String fragment) {
    Matcher matcher = INTERNAL_REFERENCE_FRAGMENT_PATTERN.matcher(fragment);
    String retval;
    if (matcher.matches()) {
      retval = ObjectUtils.notNull(matcher.group(1));
    } else {
      throw new IllegalArgumentException(String.format("The fragment '%s' does not match the pattern '%s'", fragment,
          INTERNAL_REFERENCE_FRAGMENT_PATTERN.pattern()));
    }
    return retval;
  }

  @Nullable
  public static Rlink findMatchingRLink(@NonNull Resource resource, @Nullable String preferredMediaType) {
    // find a suitable rlink reference
    List<Rlink> rlinks = resource.getRlinks();

    Rlink retval = null;
    if (rlinks != null) {
      // check if there is a matching rlink for the mime type
      if (preferredMediaType != null) {
        // find preferred mime type first
        retval = rlinks.stream().filter(rlink -> preferredMediaType.equals(rlink.getMediaType())).findFirst()
            .orElse(null);
      } else {
        // use the first one instead
        retval = rlinks.stream().findFirst().orElse(null);
      }
    }
    return retval;
  }
}
