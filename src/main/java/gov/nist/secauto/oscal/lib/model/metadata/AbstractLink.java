/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Link;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractLink implements ILink {

  public static List<Link> merge(@NonNull List<Link> original, @NonNull List<Link> additional) {
    return Stream.concat(original.stream(), additional.stream())
        .collect(Collectors.toCollection(LinkedList::new));
  }

  @NonNull
  public static Builder builder(@NonNull URI href) {
    return new Builder(href);
  }

  public static class Builder {
    @NonNull
    private final URI href;
    private String rel;
    private String mediaType;
    private MarkupLine text;

    public Builder(@NonNull URI href) {
      this.href = ObjectUtils.requireNonNull(href, "href");
    }

    @NonNull
    public Builder relation(@NonNull String relation) {
      this.rel = ObjectUtils.requireNonNull(relation, "rel");
      return this;
    }

    @NonNull
    public Builder value(@NonNull String mediaType) {
      this.mediaType = ObjectUtils.requireNonNull(mediaType, "mediaType");
      return this;
    }

    @NonNull
    public Builder clazz(@NonNull MarkupLine text) {
      this.text = ObjectUtils.requireNonNull(text, "text");
      return this;
    }

    @NonNull
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
