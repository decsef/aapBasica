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

import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import java.awt.*;

public class LspLayerSelectionPanel extends LayerSelectionPanel<ILspLayer> {

  private final ILspAWTView fView;
  private final boolean fFitOnSelection;
  private final Component fComponentForFailMessage;

  public LspLayerSelectionPanel(ILspAWTView aView) {
    this(aView.getOverlayComponent(), aView);
  }

  public LspLayerSelectionPanel(Container aContainerForExtraContent, ILspAWTView aView) {
    this(aContainerForExtraContent, aView, false, null);
  }

  public LspLayerSelectionPanel(ILspAWTView aView, boolean aFitOnSelection) {
    this(aView.getOverlayComponent(), aView);
  }

  private LspLayerSelectionPanel(Container aContainerForExtraContent, ILspAWTView aView, boolean aFitOnSelection, Component aComponentForFailMessage) {
    super(aContainerForExtraContent);
    fView = aView;
    fFitOnSelection = aFitOnSelection;
    fComponentForFailMessage = aComponentForFailMessage;
  }

  @Override
  protected void changeLayer(ILspLayer aOldLayer, ILspLayer aNewLayer) {
    int index = Math.max(0, fView.layerCount() - 1);
    if (fView.containsLayer(aOldLayer)) {
      index = fView.indexOf(aOldLayer);
      aOldLayer.clearSelection(ILcdFireEventMode.FIRE_NOW);
      fView.removeLayer(aOldLayer);
    }
    if (aNewLayer != null) {
      fView.addLayer(aNewLayer);
      fView.moveLayerAt(index, aNewLayer);
/*      if (fFitOnSelection) {
        FitUtil.fitOnLayers(fComponentForFailMessage, fView, aOldLayer != null, aNewLayer);
      }*/
    }
  }
}
