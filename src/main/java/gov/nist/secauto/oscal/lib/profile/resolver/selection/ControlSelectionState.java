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

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ControlSelectionState implements IControlSelectionState {
  private static final MetapathExpression GROUP_CHILDREN = MetapathExpression.compile("group|descendant::control");

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
        boolean selected = GROUP_CHILDREN.evaluate(item).asStream()
            .map(child -> {
              return getSelectionState((IModelNodeItem<?, ?>) ObjectUtils.requireNonNull(child)).isSelected();
            })
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
