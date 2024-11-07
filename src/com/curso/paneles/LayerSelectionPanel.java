/*
 *
 * Copyright (c) 1999-2024 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package com.curso.paneles;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.ILcdLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

/**
 * Radio button panel to select from several (usually similar) layers.
 * Only the selected layer is kept in the view.
 *
 * @param <L> the type of layer
 */
public abstract class LayerSelectionPanel<L extends ILcdLayer> extends JPanel {

  private final Container fContainerForExtraContent;
  private Component fCurrentOverlayComponent;
  private L fCurrentLayer;
  private final ButtonGroup fGroup;

  /**
   * @param aContainerForExtraContent allows adding and removing layer-specific GUI components, such as a coordinate readout, legend, etc..
   */
  protected LayerSelectionPanel(Container aContainerForExtraContent) {
    super();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    fContainerForExtraContent = aContainerForExtraContent;
    fGroup = new ButtonGroup();
  }

  public void addLayer(String aLabel, final L aLayer) {
    addLayer(aLabel, aLayer, fGroup.getButtonCount() == 0);
  }

  public void addLayer(String aLabel, final L aLayer, boolean aSelectLayer) {
    addLayer(aLabel, aLayer, null, TLcdOverlayLayout.Location.SOUTH, aSelectLayer);
  }

  public void addLayer(final String aLabel, final L aLayer, final JComponent aExtraContent, final Object aContentConstraint) {
    addLayer(aLabel, aLayer, aExtraContent, aContentConstraint, fGroup.getButtonCount() == 0);
  }

  private void addLayer(final String aLabel, final L aLayer, final JComponent aExtraContent, final Object aContentConstraint, final boolean aSelectLayer) {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        JRadioButton radio = new JRadioButton();
        AbstractAction layerSelectionAction = new LayerSelectionAction(aLabel, aLayer, aExtraContent, aContentConstraint);
        radio.setAction(layerSelectionAction);
        fGroup.add(radio);
        add(radio);
        revalidate();
        repaint();
        if (aSelectLayer) {
          radio.setSelected(true);
          layerSelectionAction.actionPerformed(null);
        }
      }
    });
  }

  public void clear() {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        new LayerSelectionAction("Clear", null, null, null).actionPerformed(null);
        for (AbstractButton button : Collections.list(fGroup.getElements())) {
          remove(button);
          fGroup.remove(button);
        }
        revalidate();
        repaint();
      }
    });
  }

  protected abstract void changeLayer(L aOldLayer, L aNewLayer);

  private class LayerSelectionAction extends AbstractAction {
    private final L fLayer;
    private final JComponent fExtraContent;
    private final Object fContentConstraint;

    public LayerSelectionAction(String aLabel, L aLayer, JComponent aExtraContent, Object aContentConstraint) {
      super(aLabel);
      fLayer = aLayer;
      fExtraContent = aExtraContent;
      fContentConstraint = aContentConstraint;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (fLayer == fCurrentLayer) {
        // Do nothing
        return;
      }
      changeLayer(fCurrentLayer, fLayer);
      fCurrentLayer = fLayer;
      if (fCurrentOverlayComponent != null) {
        fContainerForExtraContent.remove(fCurrentOverlayComponent);
      }
      if (fExtraContent != null) {
        fContainerForExtraContent.add(fExtraContent, fContentConstraint);
      }
      if (fContainerForExtraContent instanceof JComponent component) {
          component.revalidate();
        component.repaint();
      }
      fCurrentOverlayComponent = fExtraContent;
    }
  }
}
