package com.katzstudio.kreativity.ui;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Strings;
import com.katzstudio.kreativity.ui.component.KrLabel;
import com.katzstudio.kreativity.ui.component.KrWidget;
import com.katzstudio.kreativity.ui.event.KrEvent;
import com.katzstudio.kreativity.ui.event.KrMouseEvent;
import com.katzstudio.kreativity.ui.layout.KrBorderLayout;
import com.katzstudio.kreativity.ui.render.KrColorBrush;
import com.katzstudio.kreativity.ui.render.KrRenderer;
import com.katzstudio.kreativity.ui.style.KrWidgetStyle;
import com.katzstudio.kreativity.ui.util.KrTimer;
import com.katzstudio.kreativity.ui.util.KrUpdateListener;
import lombok.Getter;
import lombok.Setter;

import static com.katzstudio.kreativity.ui.KrToolkit.animations;
import static com.katzstudio.kreativity.ui.KrToolkit.getDefaultToolkit;

/**
 * The tooltip manager registers tooltips for each components, and makes sure the
 * correct tooltip is displayed when necessary.
 */

public class KrTooltipManager implements KrUpdateListener {

    @Getter @Setter private float tooltipDelay = 0.5f;

    @Getter private final KrTooltipWidget tooltipWidget;

    private boolean tooltipReady = false;

    private final KrTimer delayTimer;

    public KrTooltipManager(KrCanvas canvas) {
        canvas.addInputListener(this::onEventDispatched);
        tooltipWidget = new KrTooltipWidget();
        tooltipWidget.setVisible(false);

        delayTimer = new KrTimer(tooltipDelay, this::onDelayFinished);
    }

    private void onDelayFinished() {
        if (tooltipReady) {
            tooltipWidget.setOpacity(0);
            tooltipWidget.setVisible(true);
            animations().setOpacity(tooltipWidget, 1);
        }
    }

    private void onEventDispatched(KrWidget widget, KrEvent event) {
        if (event instanceof KrMouseEvent && ((KrMouseEvent) event).getType() == KrMouseEvent.Type.MOVED) {
            hideTooltip();

            if (hasTooltipText(widget)) {
                prepareTooltip(((KrMouseEvent) event).getScreenPosition(), widget.getTooltipText());
            } else if (hasTooltipWidget(widget)) {
                prepareTooltip(((KrMouseEvent) event).getScreenPosition(), widget.getTooltipWidget());
            }

            delayTimer.restart();
        }
    }

    private boolean hasTooltipText(KrWidget widget) {
        return widget.getTooltipText() != null && !Strings.isNullOrEmpty(widget.getTooltipText());
    }

    private boolean hasTooltipWidget(KrWidget widget) {
        return widget.getTooltipWidget() != null;
    }

    private void prepareTooltip(Vector2 screenPosition, String tooltipText) {
        tooltipWidget.setPosition(screenPosition.add(0, 20));
        tooltipWidget.setText(tooltipText);
        tooltipReady = true;
    }

    private void prepareTooltip(Vector2 screenPosition, KrWidget tooltip) {
        tooltipWidget.setPosition(screenPosition.add(0, 20));
        tooltipWidget.setWidget(tooltip);
        tooltipReady = true;
    }

    private void hideTooltip() {
        tooltipWidget.setVisible(false);
        tooltipReady = false;
    }

    @Override
    public void update(float deltaSeconds) {
        // pass
    }

    private class KrTooltipWidget extends KrWidget<KrWidgetStyle> {

        private final KrLabel textLabel;

        KrTooltipWidget() {
            textLabel = new KrLabel("");
            textLabel.setTextAlignment(KrAlignment.MIDDLE_CENTER);

            setStyle(getDefaultToolkit().getSkin().getWidgetStyle());

            setPadding(new KrPadding(6, 6, 6, 6));
            setLayout(new KrBorderLayout());
            add(textLabel);
        }

        void setText(String text) {
            if (!(getChild(0) == textLabel)) {
                removeAll();
                add(textLabel);
            }
            textLabel.setText(text);
            setSize(getPreferredSize());
        }

        void setWidget(KrWidget tooltipWidget) {
            if (!(getChild(0) == tooltipWidget)) {
                removeAll();
                add(tooltipWidget);
            }
            setSize(getPreferredSize());
        }

        @Override
        protected void drawSelf(KrRenderer renderer) {
            renderer.setBrush(new KrColorBrush(0x202020, 0.95f));
            int radius = 3;
            renderer.fillRoundedRect(0, 0, (int) getWidth(), (int) getHeight(), radius);
        }
    }
}
