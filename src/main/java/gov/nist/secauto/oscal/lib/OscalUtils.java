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

import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource.Base64;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource.Rlink;
import gov.nist.secauto.oscal.lib.resource.Base64Source;
import gov.nist.secauto.oscal.lib.resource.Source;
import gov.nist.secauto.oscal.lib.resource.URISource;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OscalUtils {
  public static final String OSCAL_VERSION = "1.0.0";
  private static final Pattern INTERNAL_REFERENCE_FRAGMENT_PATTERN = Pattern.compile("^#(.+)$");

  private OscalUtils() {
    // disable construction
  }

  public static boolean isInternalReference(@NotNull URI uri) {
    if (uri.isAbsolute()) {
      return false;
    }

    String schemeSpecificPart = uri.getSchemeSpecificPart();
    return uri.getScheme() == null && (schemeSpecificPart == null || schemeSpecificPart.isEmpty()) && uri.getFragment() != null;
  }

  @SuppressWarnings("null")
  public static String internalReferenceFragmentToId(@NotNull URI fragment) throws IllegalArgumentException {
    return internalReferenceFragmentToId(fragment.toString());
  }

  public static String internalReferenceFragmentToId(@NotNull String fragment) throws IllegalArgumentException {

    Matcher matcher = INTERNAL_REFERENCE_FRAGMENT_PATTERN.matcher(fragment);
    String retval;
    if (matcher.matches()) {
      retval = matcher.group(1);
    } else {
      throw new IllegalArgumentException(String.format("The fragment '%s' does not match the pattern '%s'", fragment,
          INTERNAL_REFERENCE_FRAGMENT_PATTERN.pattern()));
    }
    return retval;
  }

  @NotNull
  public static Source newSource(@NotNull Resource resource, @NotNull URI documentUri, String preferredMimeType) {
    Base64 base64 = resource.getBase64();

    Source retval;
    if (base64 != null) {
      // handle base64 encoded data
      UUID uuid = resource.getUuid();
      if (uuid == null) {
        throw new NullPointerException("resource has a null UUID");
      }
      @SuppressWarnings("null")
      @NotNull URI result = documentUri.resolve("#" + uuid);
      ByteBuffer buffer = base64.getValue();
      if (buffer == null) {
        throw new NullPointerException(String.format("null base64 value for resource '%s'", uuid));
      }
      retval = new Base64Source(result, buffer);
    } else {
      // find a suitable rlink reference
      List<Rlink> rlinks = resource.getRlinks();
      if (rlinks == null || rlinks.isEmpty()) {
        throw new IllegalArgumentException(String
            .format("Resource '%s' is unresolvable, since it does not have a rlink or base64", resource.getUuid()));
      } else {
        Rlink preferredRLink = null;

        // check if there is a matching rlink for the mime type
        if (preferredMimeType != null) {
          // find preferred mime type first
          preferredRLink = rlinks.stream().filter(rlink -> preferredMimeType.equals(rlink.getMediaType())).findFirst()
              .orElse(null);
        } else {
          // use the first one instead
          preferredRLink = rlinks.stream().findFirst().orElse(null);
        }

        if (preferredRLink == null) {
          throw new NullPointerException(
              String.format("Missing rlink for resource '%s'", resource.getUuid()));
        }

        URI rlinkHref = preferredRLink.getHref();
        if (rlinkHref == null) {
          throw new NullPointerException(
              String.format("rlink has a null href value for resource '%s'", resource.getUuid()));
        }
        retval = newSource(rlinkHref, documentUri);
      }
    }
    return retval;
  }

  @NotNull
  public static Source newSource(@NotNull URI source, @NotNull URI baseUri) {
    @SuppressWarnings("null")
    @NotNull URI result = baseUri.resolve(source);
    Source retval = new URISource(result);
    return retval;
  }
}
