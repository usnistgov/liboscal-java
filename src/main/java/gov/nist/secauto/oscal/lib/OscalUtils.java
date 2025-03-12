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
