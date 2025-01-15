/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ControlSelectionState implements IControlSelectionState {
  private static final IMetapathExpression GROUP_CHILDREN = IMetapathExpression.compile(
      "group|descendant::control",
      OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);

  @NonNull
  private final IIndexer index;
  @NonNull
  private final IControlFilter filter;
  @NonNull
  private final Map<IModelNodeItem<?, ?>, SelectionState> itemSelectionState = new ConcurrentHashMap<>();

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "provides intentional access to index state")
  public ControlSelectionState(@NonNull IIndexer index, @NonNull IControlFilter filter) {
    this.index = index;
    this.filter = filter;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "provides intentional access to index state")
  public IIndexer getIndex() {
    return index;
  }

  @NonNull
  public IControlFilter getFilter() {
    return filter;
  }

  @Override
  public boolean isSelected(@NonNull IModelNodeItem<?, ?> item) {
    return getSelectionState(item).isSelected();
  }

  @NonNull
  protected SelectionState getSelectionState(@NonNull IModelNodeItem<?, ?> item) {
    SelectionState retval = itemSelectionState.get(item);
    if (retval == null) {
      Object itemValue = ObjectUtils.requireNonNull(item.getValue());

      if (itemValue instanceof Control) {
        Control control = (Control) itemValue;

        // get the parent control if the parent is a control
        IAssemblyNodeItem parentItem = ObjectUtils.requireNonNull(item.getParentContentNodeItem());
        Object parentValue = parentItem.getValue();
        Control parentControl = parentValue instanceof Control ? (Control) parentValue : null;

        boolean defaultMatch = false;
        if (parentControl != null) {
          SelectionState parentSelectionState = getSelectionState(parentItem);
          defaultMatch = parentSelectionState.isSelected() && parentSelectionState.isWithChildren();
        }

        Pair<Boolean, Boolean> matchResult = getFilter().match(control, defaultMatch);
        boolean selected = matchResult.getLeft();
        boolean withChildren = matchResult.getRight();

        retval = new SelectionState(selected, withChildren);

      } else if (itemValue instanceof CatalogGroup) {
        // get control selection status
        boolean selected = GROUP_CHILDREN.evaluate(item).stream()
            .map(child -> getSelectionState((IModelNodeItem<?, ?>) ObjectUtils.requireNonNull(child)).isSelected())
            .reduce(false, (first, second) -> first || second);

        retval = new SelectionState(selected, false);
      } else {
        throw new IllegalStateException(
            String.format("Selection not supported for type '%s' at path '%s'",
                itemValue.getClass().getName(),
                item.toPath(IPathFormatter.METAPATH_PATH_FORMATER)));
      }
      itemSelectionState.put(item, retval);
    }
    return retval;
  }

  private static final class SelectionState {
    private final boolean selected;
    private final boolean withChildren;

    private SelectionState(boolean selected, boolean withChildren) {
      this.selected = selected;
      this.withChildren = withChildren;
    }

    public boolean isSelected() {
      return selected;
    }

    public boolean isWithChildren() {
      return selected && withChildren;
    }
  }
}
