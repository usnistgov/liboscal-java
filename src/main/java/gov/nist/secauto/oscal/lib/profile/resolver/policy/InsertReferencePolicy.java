/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import com.vladsch.flexmark.util.sequence.BasedSequence;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class InsertReferencePolicy
    extends AbstractCustomReferencePolicy<InsertAnchorNode> {
  private static final Logger LOGGER = LogManager.getLogger(InsertReferencePolicy.class);

  public InsertReferencePolicy() {
    super(IIdentifierParser.IDENTITY_PARSER);
  }

  @Override
  protected List<IEntityItem.ItemType> getEntityItemTypes(@NonNull InsertAnchorExtension.InsertAnchorNode insert) {
    String type = insert.getType().toString();

    if (!"param".equals(type)) {
      throw new UnsupportedOperationException("unrecognized insert type: " + type);
    }
    return CollectionUtil.singletonList(IEntityItem.ItemType.PARAMETER);
  }

  @Override
  public String getReferenceText(@NonNull InsertAnchorExtension.InsertAnchorNode insert) {
    return insert.getIdReference().toString();
  }

  @Override
  public void setReferenceText(@NonNull InsertAnchorExtension.InsertAnchorNode insert, @NonNull String newReference) {
    insert.setIdReference(ObjectUtils.notNull(BasedSequence.of(newReference)));
  }

  @Override
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull InsertAnchorExtension.InsertAnchorNode insert,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.atError().log(
          "The '{}' insert at '{}' should reference a '{}' identified by '{}'."
              + " The index did not contain the identifier.",
          insert.getType().toString(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          itemTypes.stream()
              .map(type -> type.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }

}
