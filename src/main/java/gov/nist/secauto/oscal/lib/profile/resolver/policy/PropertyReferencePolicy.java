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

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Locale;

public class PropertyReferencePolicy
    extends AbstractMultiItemTypeReferencePolicy<Property> {
  private static final Logger LOGGER = LogManager.getLogger(PropertyReferencePolicy.class);

  @SuppressWarnings("null")
  @NotNull
  public static PropertyReferencePolicy create(@NotNull IIdentifierParser identifierParser,
      @NotNull ItemType itemType) {
    return create(identifierParser, List.of(itemType));
  }

  @NotNull
  public static PropertyReferencePolicy create(@NotNull IIdentifierParser identifierParser,
      @NotNull List<ItemType> itemTypes) {
    return new PropertyReferencePolicy(identifierParser, itemTypes);
  }

  @SuppressWarnings("null")
  public PropertyReferencePolicy(@NotNull IIdentifierParser identifierParser, @NotNull List<ItemType> itemTypes) {
    super(identifierParser, itemTypes);
  }

  @Override
  public String getReferenceText(@NotNull Property property) {
    return property.getValue();
  }

  @Override
  public void setReferenceText(@NotNull Property property, @NotNull String newValue) {
    property.setValue(newValue);
  }

  @Override
  protected void handleUnselected(
      @NotNull Property property,
      @NotNull EntityItem item,
      @NotNull IReferenceVisitor visitor) {
    URI linkHref = URI.create(property.getValue());
    URI sourceUri = item.getSource();

    URI resolved = sourceUri.resolve(linkHref);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("remapping orphaned URI '{}' to '{}'", linkHref.toString(), resolved.toString());
    }
    property.setValue(resolved.toString());
  }

  @Override
  protected boolean handleIndexMiss(
      @NotNull Property property,
      @NotNull List<@NotNull ItemType> itemTypes,
      @NotNull String identifier,
      @NotNull IReferenceVisitor visitor) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log(
          "property '{}' should reference a {} identified by '{}', but the identifier was not found in the index.",
          property.getQName(),
          itemTypes.stream()
              .map(en -> en.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }
}
