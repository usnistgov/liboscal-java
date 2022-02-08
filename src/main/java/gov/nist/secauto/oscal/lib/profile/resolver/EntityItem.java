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

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class EntityItem {

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

  @NotNull
  private final String identifier;
  @NotNull
  private final Object instance;
  @NotNull
  private final ItemType itemType;
  @NotNull
  private final URI source;
  private int referenceCount; // 0 by default

  public static Builder builder() {
    return new Builder();
  }

  public String getIdentifier() {
    return identifier;
  }

  public Object getInstance() {
    return instance;
  }

  @NotNull
  public ItemType getItemType() {
    return itemType;
  }

  public URI getSource() {
    return source;
  }

  public int getReferenceCount() {
    return referenceCount;
  }

  public void incrementReferenceCount() {
    referenceCount += 1;
  }

  @SuppressWarnings("null")
  private EntityItem(@NotNull Builder builder) {
    this.identifier = Objects.requireNonNull(builder.identifier, "identifier");
    this.instance = Objects.requireNonNull(builder.instance, "instance");
    this.itemType = Objects.requireNonNull(builder.itemType, "itemType");
    this.source = Objects.requireNonNull(builder.source, "source");
  }

  public static class Builder {
    private String identifier;
    private Object instance;
    private ItemType itemType;
    private URI source;

    public Builder instance(@NotNull Object instance, @NotNull UUID identifier) {
      return instance(instance, identifier.toString());
    }

    @SuppressWarnings("null")
    public Builder instance(@NotNull Object instance, @NotNull String identifier) {
      this.identifier = Objects.requireNonNull(identifier, "identifier");
      this.instance = Objects.requireNonNull(instance, "instance");
      return this;
    }

    public Builder itemType(ItemType itemType) {
      this.itemType = Objects.requireNonNull(itemType, "itemType");
      return this;
    }

    public Builder source(URI source) {
      this.source = Objects.requireNonNull(source, "source");
      return this;
    }

    @NotNull
    public EntityItem build() {
      return new EntityItem(this);
    }
  }
}
