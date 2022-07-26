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

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.oscal.lib.model.Link;

import javax.annotation.Nonnull;

import java.net.URI;
import java.util.Objects;

public abstract class AbstractLink implements ILink {

  @Nonnull
  public static Builder builder(@Nonnull URI href) {
    return new Builder(href);
  }

  public static class Builder {
    @Nonnull
    private final URI href;
    private String rel;
    private String mediaType;
    private MarkupLine text;

    @SuppressWarnings("null")
    public Builder(@Nonnull URI href) {
      this.href = Objects.requireNonNull(href, "href");
    }

    @SuppressWarnings("null")
    @Nonnull
    public Builder relation(@Nonnull String relation) {
      this.rel = Objects.requireNonNull(relation, "rel");
      return this;
    }

    @SuppressWarnings("null")
    @Nonnull
    public Builder value(@Nonnull String mediaType) {
      this.mediaType = Objects.requireNonNull(mediaType, "mediaType");
      return this;
    }

    @SuppressWarnings("null")
    @Nonnull
    public Builder clazz(@Nonnull MarkupLine text) {
      this.text = Objects.requireNonNull(text, "text");
      return this;
    }

    @Nonnull
    public Link build() {
      Link retval = new Link();
      retval.setHref(href);
      if (rel != null) {
        retval.setRel(rel);
      }
      if (mediaType != null) {
        retval.setMediaType(mediaType);
      }
      if (text != null) {
        retval.setText(text);
      }

      return retval;
    }
  }
}
