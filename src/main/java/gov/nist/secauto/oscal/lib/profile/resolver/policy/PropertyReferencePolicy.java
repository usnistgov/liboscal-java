/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class PropertyReferencePolicy
    extends AbstractMultiItemTypeReferencePolicy<Property> {
  private static final Logger LOGGER = LogManager.getLogger(PropertyReferencePolicy.class);

  @NonNull
  public static PropertyReferencePolicy create(@NonNull IIdentifierParser identifierParser,
      @NonNull IEntityItem.ItemType itemType) {
    return create(identifierParser, ObjectUtils.notNull(List.of(itemType)));
  }

  @NonNull
  public static PropertyReferencePolicy create(@NonNull IIdentifierParser identifierParser,
      @NonNull List<IEntityItem.ItemType> itemTypes) {
    return new PropertyReferencePolicy(identifierParser, itemTypes);
  }

  public PropertyReferencePolicy(
      @NonNull IIdentifierParser identifierParser,
      @NonNull List<IEntityItem.ItemType> itemTypes) {
    super(identifierParser, itemTypes);
  }

  @Override
  public String getReferenceText(@NonNull Property property) {
    return property.getValue();
  }

  @Override
  public void setReferenceText(@NonNull Property property, @NonNull String newValue) {
    property.setValue(newValue);
  }

  @Override
  protected void handleUnselected(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull Property property,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    URI linkHref = URI.create(property.getValue());
    URI sourceUri = item.getSource();

    URI resolved = sourceUri.resolve(linkHref);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atTrace().log("At path '{}', remapping orphaned URI '{}' to '{}'",
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          linkHref.toString(),
          resolved.toString());
    }
    property.setValue(resolved.toString());
  }

  @Override
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull Property property,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log(
          "The property '{}' at '{}' should reference a {} identified by '{}',"
              + " but the identifier was not found in the index.",
          property.getQName(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          itemTypes.stream()
              .map(en -> en.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }
}
