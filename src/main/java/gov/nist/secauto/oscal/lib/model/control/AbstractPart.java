/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Property;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractPart implements IPart {

  @Override
  @NonNull
  public Stream<InsertAnchorExtension.InsertAnchorNode> getInserts(@NonNull Predicate<InsertAnchorNode> filter) {
    MarkupMultiline prose = getProse();

    @NonNull
    Stream<InsertAnchorExtension.InsertAnchorNode> retval;
    if (prose == null) {
      retval = ObjectUtils.notNull(Stream.empty());
    } else {
      List<InsertAnchorExtension.InsertAnchorNode> result = prose.getInserts(filter);
      retval = ObjectUtils.notNull(result.stream());
    }
    return retval;
  }

  public Stream<IPart> getPartsRecursively() {
    return Stream.concat(
        Stream.of(this),
        CollectionUtil.listOrEmpty(getParts()).stream()
            .flatMap(AbstractPart::getPartsRecursively));
  }

  @NonNull
  public static Builder builder(@NonNull String name) {
    return new Builder(name);
  }

  public static class Builder {
    private String id;
    @NonNull
    private final String name;
    private URI namespace;
    private String clazz;
    private MarkupMultiline prose;
    private MarkupLine title;
    private final List<Property> props = new LinkedList<>();
    private final List<Link> links = new LinkedList<>();
    private final List<ControlPart> parts = new LinkedList<>();

    public Builder(@NonNull String name) {
      this.name = Objects.requireNonNull(name);
    }

    @SuppressWarnings("PMD.ShortMethodName")
    @NonNull
    public Builder id(@NonNull String value) {
      this.id = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder namespace(@NonNull URI value) {
      this.namespace = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder clazz(@NonNull String value) {
      this.clazz = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder title(@NonNull String markdown) {
      return title(MarkupLine.fromMarkdown(Objects.requireNonNull(markdown)));
    }

    @NonNull
    public Builder title(@NonNull MarkupLine value) {
      this.title = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder prose(@NonNull String markdown) {
      return prose(MarkupMultiline.fromMarkdown(Objects.requireNonNull(markdown)));
    }

    @NonNull
    public Builder prose(@NonNull MarkupMultiline value) {
      this.prose = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder prop(@NonNull Property value) {
      this.props.add(Objects.requireNonNull(value));
      return this;
    }

    @NonNull
    public Builder link(@NonNull Link value) {
      this.links.add(Objects.requireNonNull(value));
      return this;
    }

    @NonNull
    public Builder part(@NonNull ControlPart value) {
      this.parts.add(Objects.requireNonNull(value));
      return this;
    }

    @NonNull
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
