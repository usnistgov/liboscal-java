/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.metadata;

import gov.nist.secauto.metaschema.core.qname.IEnhancedQName;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Property;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractProperty implements IProperty {

  @NonNull
  public static IEnhancedQName qname(@NonNull URI namespace, @NonNull String name) {
    return IEnhancedQName.of(ObjectUtils.notNull(namespace.toASCIIString()), name);
  }

  @NonNull
  public static IEnhancedQName qname(@NonNull String name) {
    return IEnhancedQName.of(ObjectUtils.notNull(IProperty.normalizeNamespace(null).toASCIIString()), name);
  }

  @SuppressWarnings("null")
  @NonNull
  public static Optional<Property> find(List<Property> props, @NonNull IEnhancedQName qname) {
    return CollectionUtil.listOrEmpty(props).stream().filter(prop -> qname.equals(prop.getQName())).findFirst();
  }

  protected AbstractProperty() {
    // only concrete classes should construct
  }

  public static List<Property> merge(@NonNull List<Property> original, @NonNull List<Property> additional) {
    return Stream.concat(original.stream(), additional.stream())
        .collect(Collectors.toCollection(LinkedList::new));
  }

  @Override
  public boolean isNamespaceEqual(@NonNull URI namespace) {
    return IProperty.normalizeNamespace(getNs()).equals(namespace);
  }

  @NonNull
  public IEnhancedQName getQName() {
    return qname(
        IProperty.normalizeNamespace(getNs()),
        ObjectUtils.requireNonNull(getName()));
  }

  @NonNull
  public static Builder builder(@NonNull String name) {
    return new Builder(name);
  }

  public static class Builder {
    @NonNull
    private final String name;

    private UUID uuid;
    private URI namespace;
    private String value;
    private String clazz;

    public Builder(@NonNull String name) {
      this.name = Objects.requireNonNull(name, "name");
    }

    @NonNull
    public Builder uuid(@NonNull UUID uuid) {
      this.uuid = Objects.requireNonNull(uuid);
      return this;
    }

    @SuppressWarnings("PMD.NullAssignment") // needed
    @NonNull
    public Builder namespace(@NonNull URI namespace) {
      if (OSCAL_NAMESPACE.equals(namespace)) {
        this.namespace = null;
      } else {
        this.namespace = Objects.requireNonNull(namespace);
      }
      return this;
    }

    @NonNull
    public Builder value(@NonNull String value) {
      this.value = Objects.requireNonNull(value);
      return this;
    }

    @NonNull
    public Builder clazz(@NonNull String clazz) {
      this.clazz = Objects.requireNonNull(clazz);
      return this;
    }

    @NonNull
    public Property build() {
      Property retval = new Property();
      retval.setName(name);
      retval.setValue(Objects.requireNonNull(value, "value"));
      if (uuid != null) {
        retval.setUuid(uuid);
      }
      if (namespace != null) {
        retval.setNs(namespace);
      }
      if (clazz != null) {
        retval.setClazz(clazz);
      }

      return retval;
    }
  }
}
