/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.ParameterConstraint;
import gov.nist.secauto.oscal.lib.model.ParameterGuideline;
import gov.nist.secauto.oscal.lib.model.ParameterSelection;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.metadata.IProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractParameter implements IParameter {

  @Override
  public Stream<String> getParameterReferences() {

    // handle prop name="aggregates"
    Stream<String> aggregatesIds = CollectionUtil.listOrEmpty(getProps()).stream()
        .filter(Objects::nonNull)
        .filter(prop -> prop.isNamespaceEqual(IProperty.OSCAL_NAMESPACE) && "aggregates".equals(prop.getName()))
        .map(Property::getValue);

    // handle select/choice/insert
    ParameterSelection selection = getSelect();

    Stream<String> selectInsertIds;
    if (selection == null) {
      selectInsertIds = Stream.empty();
    } else {
      selectInsertIds = CollectionUtil.listOrEmpty(selection.getChoice()).stream()
          .filter(Objects::nonNull)
          .flatMap(choice -> choice.getInserts(insert -> "param".equals(insert.getType().toString())).stream()
              .map(insert -> insert.getIdReference().toString()));
    }
    Stream<String> retval = Stream.concat(aggregatesIds, selectInsertIds)
        .filter(Objects::nonNull)
        .distinct();
    assert retval != null;
    return retval;
  }

  @NonNull
  public static Builder builder(@NonNull String id) {
    return new Builder(id);
  }

  public static class Builder {
    @NonNull
    private final String id;

    private String clazz;
    private final List<Property> props = new LinkedList<>();
    private final List<Link> links = new LinkedList<>();
    private MarkupLine label;
    private MarkupMultiline usage;
    private final List<ParameterConstraint> constraints = new LinkedList<>();
    private final List<ParameterGuideline> guidelines = new LinkedList<>();
    private List<String> values = new LinkedList<>();
    private ParameterSelection selection;
    private MarkupMultiline remarks;

    public Builder(@NonNull String id) {
      this.id = ObjectUtils.requireNonNull(id);
    }

    @NonNull
    public Builder clazz(@NonNull String value) {
      this.clazz = ObjectUtils.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder prop(@NonNull Property value) {
      this.props.add(ObjectUtils.requireNonNull(value));
      return this;
    }

    @NonNull
    public Builder link(@NonNull Link value) {
      this.links.add(ObjectUtils.requireNonNull(value));
      return this;
    }

    @NonNull
    public Builder label(@NonNull String markdown) {
      return label(MarkupLine.fromMarkdown(Objects.requireNonNull(markdown)));
    }

    @NonNull
    public Builder label(@NonNull MarkupLine value) {
      this.label = ObjectUtils.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder usage(@NonNull String markdown) {
      return usage(MarkupMultiline.fromMarkdown(ObjectUtils.requireNonNull(markdown)));
    }

    @NonNull
    public Builder usage(@NonNull MarkupMultiline value) {
      this.usage = ObjectUtils.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder constraint(@NonNull ParameterConstraint value) {
      this.constraints.add(ObjectUtils.requireNonNull(value));
      return this;
    }

    @NonNull
    public Builder guideline(@NonNull ParameterGuideline value) {
      this.guidelines.add(ObjectUtils.requireNonNull(value));
      return this;
    }

    @SuppressWarnings("null")
    @NonNull
    public Builder values(@NonNull String... values) {
      return values(Arrays.asList(values));
    }

    @NonNull
    public Builder values(@NonNull Collection<String> values) {
      this.values = new ArrayList<>(values);
      return this;
    }

    @NonNull
    public Builder select(@NonNull ParameterSelection value) {
      this.selection = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder remarks(@NonNull MarkupMultiline value) {
      this.remarks = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Parameter build() {
      Parameter retval = new Parameter();
      retval.setId(id);

      if (clazz != null) {
        retval.setClazz(clazz);
      }
      if (!props.isEmpty()) {
        retval.setProps(props);
      }
      if (!links.isEmpty()) {
        retval.setLinks(links);
      }
      if (label != null) {
        retval.setLabel(label);
      }
      if (usage != null) {
        retval.setUsage(usage);
      }
      if (!constraints.isEmpty()) {
        retval.setConstraints(constraints);
      }
      if (!guidelines.isEmpty()) {
        retval.setGuidelines(guidelines);
      }
      if (!values.isEmpty()) {
        retval.setValues(values);
      }
      if (selection != null) {
        retval.setSelect(selection);
      }
      if (remarks != null) {
        retval.setRemarks(remarks);
      }
      return retval;
    }
  }
}
