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

package gov.nist.secauto.oscal.lib.model.control;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Property;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractPart implements IPart {

  @SuppressWarnings({ "null" })
  @Override
  @NotNull
  public Stream<InsertAnchorNode> getInserts(@NotNull Predicate<InsertAnchorNode> filter) {
    MarkupMultiline prose = getProse();

    @NotNull
    Stream<InsertAnchorNode> retval;
    if (prose == null) {
      retval = Stream.empty();
    } else {
      List<InsertAnchorNode> result = prose.getInserts(filter);
      retval = result.stream();
    }
    return retval;
  }

  public Stream<@NotNull IPart> getPartsRecursively() {
    return Stream.concat(
        Stream.of(this),
        CollectionUtil.listOrEmpty(getParts()).stream()
            .flatMap(part -> part.getPartsRecursively()));
  }

  @NotNull
  public static Builder builder(@NotNull String name) {
    return new Builder(name);
  }

  public static class Builder {
    private String id;
    @NotNull
    private final String name;
    private URI namespace;
    private String clazz;
    private MarkupMultiline prose;
    private MarkupLine title;
    private final List<Property> props = new LinkedList<>();
    private final List<Link> links = new LinkedList<>();
    private final List<ControlPart> parts = new LinkedList<>();

    @SuppressWarnings("null")
    public Builder(@NotNull String name) {
      this.name = Objects.requireNonNull(name, "name");
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder id(@NotNull String value) {
      this.id = Objects.requireNonNull(value, "value");
      return this;
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder namespace(@NotNull URI value) {
      this.namespace = Objects.requireNonNull(value, "value");
      return this;
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder clazz(@NotNull String value) {
      this.clazz = Objects.requireNonNull(value, "value");
      return this;
    }

    @NotNull
    public Builder title(@NotNull String markdown) {
      return title(MarkupLine.fromMarkdown(Objects.requireNonNull(markdown, "markdown")));
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder title(@NotNull MarkupLine value) {
      this.title = Objects.requireNonNull(value, "value");
      return this;
    }

    @NotNull
    public Builder prose(@NotNull String markdown) {
      return prose(MarkupMultiline.fromMarkdown(Objects.requireNonNull(markdown, "markdown")));
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder prose(@NotNull MarkupMultiline value) {
      this.prose = Objects.requireNonNull(value, "value");
      return this;
    }

    @NotNull
    public Builder prop(@NotNull Property value) {
      this.props.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NotNull
    public Builder link(@NotNull Link value) {
      this.links.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NotNull
    public Builder part(@NotNull ControlPart value) {
      this.parts.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NotNull
    public ControlPart build() {
      ControlPart retval = new ControlPart();

      retval.setName(name);

      if (id != null) {
        retval.setId(id);
      }
      if (namespace != null) {
        retval.setNs(namespace);
      }
      if (clazz != null) {
        retval.setClazz(clazz);
      }
      if (prose != null) {
        retval.setProse(prose);
      }
      if (title != null) {
        retval.setTitle(title);
      }
      if (!props.isEmpty()) {
        retval.setProps(props);
      }
      if (!links.isEmpty()) {
        retval.setLinks(links);
      }
      if (!parts.isEmpty()) {
        retval.setParts(parts);
      }
      return retval;
    }
  }
}
