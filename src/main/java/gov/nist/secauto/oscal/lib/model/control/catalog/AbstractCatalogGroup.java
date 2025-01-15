/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.control.AbstractParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractCatalogGroup
    implements ICatalogGroup {

  @NonNull
  @Override
  public Stream<String> getReferencedParameterIds() {

    // get parameters referenced by the group's parts
    Stream<String> insertIds = CollectionUtil.listOrEmpty(getParts()).stream()
        // Get the full part hierarchy
        .flatMap(part -> Stream.concat(Stream.of(part), part.getPartsRecursively()))
        // Get the inserts for each part
        .flatMap(part -> part.getInserts(insert -> "param".equals(insert.getType().toString())))
        // Get the param ids for each insert
        .map(insert -> insert.getIdReference().toString())
        .flatMap(ObjectUtils::filterNull);

    // get parameters referenced by the control's parameters
    Stream<String> parameterIds = CollectionUtil.listOrEmpty(getParams()).stream()
        .flatMap(ObjectUtils::filterNull)
        .flatMap(AbstractParameter::getParameterReferences);

    return ObjectUtils.notNull(
        Stream.concat(insertIds, parameterIds).distinct());
  }

  @NonNull
  public static Builder builder(@NonNull String id) {
    return new Builder(id);
  }

  public static class Builder {
    @NonNull
    private final String id;

    private String clazz;
    private MarkupLine title;
    private final List<Parameter> params = new LinkedList<>();
    private final List<Property> props = new LinkedList<>();
    private final List<Link> links = new LinkedList<>();
    private final List<ControlPart> parts = new LinkedList<>();
    private final List<CatalogGroup> groups = new LinkedList<>();
    private final List<Control> controls = new LinkedList<>();

    public Builder(@NonNull String id) {
      this.id = ObjectUtils.requireNonNull(id, "id");
    }

    @NonNull
    public Builder clazz(@NonNull String value) {
      this.clazz = ObjectUtils.requireNonNull(value, "value");
      return this;
    }

    @NonNull
    public Builder title(@NonNull String markdown) {
      this.title = MarkupLine.fromMarkdown(Objects.requireNonNull(markdown, "markdown"));
      return this;
    }

    @NonNull
    public Builder title(@NonNull MarkupLine value) {
      this.title = ObjectUtils.requireNonNull(value, "value");
      return this;
    }

    @NonNull
    public Builder param(@NonNull Parameter value) {
      this.params.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public Builder prop(@NonNull Property value) {
      this.props.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public Builder link(@NonNull Link value) {
      this.links.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public Builder part(@NonNull ControlPart value) {
      this.parts.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public Builder group(@NonNull CatalogGroup value) {
      this.groups.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public Builder control(@NonNull Control value) {
      this.controls.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NonNull
    public CatalogGroup build() {
      CatalogGroup retval = new CatalogGroup();
      retval.setId(id);

      if (title == null) {
        throw new IllegalStateException("a title must be provided");
      }
      retval.setTitle(title);

      if (clazz != null) {
        retval.setClazz(clazz);
      }
      if (!params.isEmpty()) {
        retval.setParams(params);
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
      if (!controls.isEmpty()) {
        retval.setControls(controls);
      }
      if (!groups.isEmpty()) {
        retval.setGroups(groups);
      }

      return retval;
    }
  }
}
