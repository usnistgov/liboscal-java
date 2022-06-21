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

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Property;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings("PMD")
public abstract class AbstractCatalogGroup
    implements ICatalogGroup, IGroupContainer {
  
  @SuppressWarnings("null")
  @NotNull
  @Override
  public Stream<@NotNull String> getReferencedParameterIds() {

    // get parameters referenced by the group's parts
    Stream<@NotNull String> insertIds = CollectionUtil.listOrEmpty(getParts()).stream()
        // Get the full part hierarchy
        .flatMap(part -> Stream.concat(Stream.of(part), part.getPartsRecursively()))
        // Get the inserts for each part
        .flatMap(part -> part.getInserts(insert -> "param".equals(insert.getType().toStringOrNull())))
        // Get the param ids for each insert
        .map(insert -> insert.getIdReference().toStringOrNull())
        .flatMap(ObjectUtils::filterNull);

    // get parameters referenced by the control's parameters
    Stream<@NotNull String> parameterIds = CollectionUtil.listOrEmpty(getParams()).stream()
        .flatMap(ObjectUtils::filterNull)
        .flatMap(param -> param.getParameterReferences());

    return Stream.concat(insertIds, parameterIds)
        .distinct();
  }
  
  @NotNull
  public static Builder builder(@NotNull String id) {
    return new Builder(id);
  }

  public static class Builder {
    @NotNull
    private final String id;

    private String clazz;
    private MarkupLine title;
    private final List<Parameter> params = new LinkedList<>();
    private final List<Property> props = new LinkedList<>();
    private final List<Link> links = new LinkedList<>();
    private final List<ControlPart> parts = new LinkedList<>();
    private final List<CatalogGroup> groups = new LinkedList<>();
    private final List<Control> controls = new LinkedList<>();

    @SuppressWarnings("null")
    public Builder(@NotNull String id) {
      this.id = Objects.requireNonNull(id, "id");
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder clazz(@NotNull String value) {
      this.clazz = Objects.requireNonNull(value, "value");
      return this;
    }

    @NotNull
    public Builder title(@NotNull String markdown) {
      this.title = MarkupLine.fromMarkdown(Objects.requireNonNull(markdown, "markdown"));
      return this;
    }

    @SuppressWarnings("null")
    @NotNull
    public Builder title(@NotNull MarkupLine value) {
      this.title = Objects.requireNonNull(value, "value");
      return this;
    }

    @NotNull
    public Builder param(@NotNull Parameter value) {
      this.params.add(Objects.requireNonNull(value, "value"));
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
    public Builder group(@NotNull CatalogGroup value) {
      this.groups.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NotNull
    public Builder control(@NotNull Control value) {
      this.controls.add(Objects.requireNonNull(value, "value"));
      return this;
    }

    @NotNull
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
