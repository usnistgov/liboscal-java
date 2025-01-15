/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.support;

import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractEntityItem implements IEntityItem {

  @NonNull
  private final String originalIdentifier;
  @NonNull
  private IModelNodeItem<?, ?> instance;
  @NonNull
  private final ItemType itemType;
  @NonNull
  private final URI source;
  private int referenceCount; // 0 by default
  // private boolean resolved; // false by default

  protected AbstractEntityItem(@NonNull Builder builder) {
    this.itemType = ObjectUtils.requireNonNull(builder.itemType, "itemType");
    this.originalIdentifier = ObjectUtils.requireNonNull(builder.originalIdentifier, "originalIdentifier");
    this.instance = ObjectUtils.requireNonNull(builder.instance, "instance");
    this.source = ObjectUtils.requireNonNull(builder.source, "source");
  }

  @Override
  @NonNull
  public String getOriginalIdentifier() {
    return originalIdentifier;
  }

  @Override
  @NonNull
  public abstract String getIdentifier();

  // @NonNull
  // public String getIdentifier() {
  // final String checkedReassignedIdentifier = reassignedIdentifier;
  // return checkedReassignedIdentifier == null ? originalIdentifier :
  // checkedReassignedIdentifier;
  // }

  @Override
  @NonNull
  public IModelNodeItem<?, ?> getInstance() {
    return instance;
  }

  @Override
  public void setInstance(IModelNodeItem<?, ?> item) {
    instance = item;
  }

  @Override
  @NonNull
  @SuppressWarnings("unchecked")
  public <T> T getInstanceValue() {
    return ObjectUtils.notNull((T) getInstance().getValue());
  }

  @Override
  @NonNull
  public ItemType getItemType() {
    return itemType;
  }

  @Override
  @NonNull
  public URI getSource() {
    return source;
  }

  @Override
  public int getReferenceCount() {
    return referenceCount;
  }

  // public boolean isResolved() {
  // return resolved;
  // }
  //
  // public void markResolved() {
  // resolved = true;
  // }

  @Override
  public void incrementReferenceCount() {
    referenceCount += 1;
  }

  @Override
  public int resetReferenceCount() {
    int retval = referenceCount;
    referenceCount = 0;
    return retval;
  }

  static final class Builder {
    private String originalIdentifier;
    private String reassignedIdentifier;
    private IModelNodeItem<?, ?> instance;
    private ItemType itemType;
    private URI source;

    @NonNull
    public Builder instance(@NonNull IModelNodeItem<?, ?> item, @NonNull ItemType itemType) {
      this.instance = item;
      this.itemType = itemType;
      return this;
    }

    // @NonNull
    // public Builder reassignedIdentifier(@NonNull UUID identifier) {
    // // no need to normalize, since UUIDs are formatted lower case
    // return reassignedIdentifier(identifier.toString());
    // }

    @NonNull
    public Builder reassignedIdentifier(@NonNull String identifier) {
      this.reassignedIdentifier = identifier;
      return this;
    }
    //
    // @NonNull
    // public Builder originalIdentifier(@NonNull UUID identifier) {
    // // no need to normalize, since UUIDs are formatted lower case
    // return originalIdentifier(identifier.toString());
    // }

    @NonNull
    public Builder originalIdentifier(@NonNull String identifier) {
      this.originalIdentifier = identifier;
      return this;
    }

    @NonNull
    public Builder source(@NonNull URI source) {
      this.source = source;
      return this;
    }

    @NonNull
    public IEntityItem build() {
      return reassignedIdentifier == null ? new OriginalEntityItem(this) : new ReassignedEntityItem(this);
    }
  }

  static final class OriginalEntityItem
      extends AbstractEntityItem {

    protected OriginalEntityItem(@NonNull Builder builder) {
      super(builder);
    }

    @Override
    public String getIdentifier() {
      return getOriginalIdentifier();
    }

    @Override
    public boolean isIdentifierReassigned() {
      return false;
    }
  }

  static final class ReassignedEntityItem
      extends AbstractEntityItem {
    @NonNull
    private final String reassignedIdentifier;

    protected ReassignedEntityItem(@NonNull Builder builder) {
      super(builder);
      this.reassignedIdentifier = ObjectUtils.requireNonNull(builder.reassignedIdentifier);
    }

    @Override
    public String getIdentifier() {
      return reassignedIdentifier;
    }

    @Override
    public boolean isIdentifierReassigned() {
      return true;
    }
  }
}
