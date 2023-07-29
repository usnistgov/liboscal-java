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
  // return checkedReassignedIdentifier == null ? originalIdentifier : checkedReassignedIdentifier;
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
    return (T) getInstance().getValue();
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
