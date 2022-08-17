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

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.model.common.datatype.adapter.UuidAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControlContainer;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.IReferenceVisitor;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class EntityItem {
  private static final MetapathExpression CONTAINER_METAPATH
      = MetapathExpression.compile("(ancestor::control|ancestor::group)[1])");

  public enum ItemType {
    ROLE,
    LOCATION,
    PARTY,
    GROUP,
    CONTROL,
    PART,
    PARAMETER,
    RESOURCE;
  }

  @NonNull
  private final String originalIdentifier;
  @NonNull
  private final String identifier;
  @NonNull
  private final IRequiredValueModelNodeItem instance;
  @NonNull
  private final ItemType itemType;
  @NonNull
  private final URI source;
  private int referenceCount; // 0 by default
  private boolean resolved; // false by default

  public static Builder builder() {
    return new Builder();
  }

  private EntityItem(@NonNull Builder builder) {
    this.originalIdentifier = builder.originalIdentifier == null ? builder.identifier : builder.originalIdentifier;
    this.identifier = Objects.requireNonNull(builder.identifier, "identifier");
    this.instance = Objects.requireNonNull(builder.instance, "instance");
    this.itemType = Objects.requireNonNull(builder.itemType, "itemType");
    this.source = Objects.requireNonNull(builder.source, "source");
  }

  @NonNull
  public String getOriginalIdentifier() {
    return originalIdentifier;
  }

  @NonNull
  public String getIdentifier() {
    return identifier;
  }

  @NonNull
  public IRequiredValueModelNodeItem getInstance() {
    return instance;
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public <T> T getInstanceValue() {
    return (T) instance.getValue();
  }

  @NonNull
  public ItemType getItemType() {
    return itemType;
  }

  @NonNull
  public URI getSource() {
    return source;
  }

  public boolean isIdentifierChanged() {
    return !getOriginalIdentifier().equals(getIdentifier());
  }

  public int getReferenceCount() {
    return referenceCount;
  }

  public boolean isResolved() {
    return resolved;
  }

  public void markResolved() {
    resolved = true;
  }

  public void incrementReferenceCount() {
    referenceCount += 1;
  }

  public boolean isSelected(@NonNull Index index) {
    boolean retval;
    switch (getItemType()) {
    case CONTROL:
      Control control = getInstanceValue();
      retval = index.isSelected(control);
      break;
    case GROUP:
      CatalogGroup group = getInstanceValue();
      retval = index.isSelected(group);
      break;
    case PART: {
      IRequiredValueModelNodeItem instance = getInstance();
      IRequiredValueModelNodeItem containerItem = CONTAINER_METAPATH.evaluateAs(instance, ResultType.NODE);
      retval = index.isSelected((IControlContainer) containerItem.getValue());
      break;
    }
    case PARAMETER:
    case LOCATION:
    case PARTY:
    case RESOURCE:
    case ROLE:
      // always "selected"
      retval = true;
      break;
    default:
      throw new UnsupportedOperationException(getItemType().name());
    }
    return retval;
  }

  public void accept(@NonNull IReferenceVisitor visitor) {
    IRequiredValueModelNodeItem instance = getInstance();
    switch (getItemType()) {
    case CONTROL:
      visitor.visitControl(instance);
      break;
    case GROUP:
      visitor.visitGroup(instance);
      break;
    case LOCATION:
      visitor.visitLocation(instance);
      break;
    case PARAMETER:
      visitor.visitParameter(instance);
      break;
    case PART:
      visitor.visitPart(instance);
      break;
    case PARTY:
      visitor.visitParty(instance);
      break;
    case RESOURCE:
      visitor.visitResource(instance);
      break;
    case ROLE:
      visitor.visitRole(instance);
      break;
    default:
      throw new UnsupportedOperationException(getItemType().name());
    }
  }

  @NonNull
  public static String normalizeIdentifier(@NonNull ItemType type, @NonNull String identifier) {
    String retval = identifier;
    switch (type) {
    case LOCATION:
    case PARTY:
    case RESOURCE:
      if (UuidAdapter.UUID_PATTERN.matches(identifier)) {
        // normalize the UUID
        retval = identifier.toLowerCase(Locale.ROOT);
      }
      break;
    default:
      // do nothing
      break;
    }

    return retval;
  }

  public static class Builder {
    private String originalIdentifier; // NOPMD - builder method
    private String identifier;
    private IRequiredValueModelNodeItem instance; // NOPMD - builder method
    private ItemType itemType;
    private URI source; // NOPMD - builder method

    public Builder instance(@NonNull IRequiredValueModelNodeItem item, @NonNull ItemType itemType,
        @NonNull UUID identifier) {
      return instance(item, itemType, ObjectUtils.notNull(identifier.toString()));
    }

    public Builder instance(@NonNull IRequiredValueModelNodeItem item, @NonNull ItemType itemType,
        @NonNull String identifier) {
      this.identifier = normalizeIdentifier(itemType, ObjectUtils.requireNonNull(identifier, "identifier"));
      this.instance = Objects.requireNonNull(item, "item");
      this.itemType = itemType;
      return this;
    }

    public Builder originalIdentifier(@NonNull String identifier) {
      this.originalIdentifier = normalizeIdentifier(itemType, identifier);
      return this;
    }

    public Builder source(@NonNull URI source) {
      this.source = source;
      return this;
    }

    @NonNull
    public EntityItem build() {
      return new EntityItem(this);
    }
  }
}
