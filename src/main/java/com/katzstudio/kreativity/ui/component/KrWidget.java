package com.katzstudio.kreativity.ui.component;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.katzstudio.kreativity.ui.KrCanvas;
import com.katzstudio.kreativity.ui.KrPadding;
import com.katzstudio.kreativity.ui.KrWidgetToStringBuilder;
import com.katzstudio.kreativity.ui.event.KrEnterEvent;
import com.katzstudio.kreativity.ui.event.KrEvent;
import com.katzstudio.kreativity.ui.event.KrExitEvent;
import com.katzstudio.kreativity.ui.event.KrFocusEvent;
import com.katzstudio.kreativity.ui.event.KrKeyEvent;
import com.katzstudio.kreativity.ui.event.KrMouseEvent;
import com.katzstudio.kreativity.ui.event.KrScrollEvent;
import com.katzstudio.kreativity.ui.layout.KrAbsoluteLayout;
import com.katzstudio.kreativity.ui.layout.KrLayout;
import com.katzstudio.kreativity.ui.listener.KrFocusListener;
import com.katzstudio.kreativity.ui.listener.KrKeyboardListener;
import com.katzstudio.kreativity.ui.listener.KrMouseListener;
import com.katzstudio.kreativity.ui.listener.KrWidgetListener;
import com.katzstudio.kreativity.ui.render.KrRenderer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Kreativity Components
 */
@SuppressWarnings({"UnusedParameters", "unused"})
public class KrWidget {

    public static final String FOCUS_PROPERTY = "property.focus";

    @Getter private float x;

    @Getter private float y;

    @Getter float width;

    @Getter private float height;

    @Getter private final ArrayList<KrWidget> children = Lists.newArrayList();

    @Getter private KrWidget parent;

    @Getter @Setter private KrLayout layout = new KrAbsoluteLayout();

    private Vector2 userPreferredSize;

    @Getter private boolean isFocused;

    @Getter @Setter private boolean isVisible;

    @Getter @Setter private boolean isEnabled;

    @Getter @Setter private String name;

    @Getter @Setter private KrPadding padding = new KrPadding(0);

    @Setter private KrCanvas canvas;

    private final List<KrKeyboardListener> keyboardListeners = Lists.newArrayList();

    private final List<KrMouseListener> mouseListeners = Lists.newArrayList();

    private final List<KrFocusListener> focusListeners = Lists.newArrayList();

    private final List<KrWidgetListener> widgetListeners = Lists.newArrayList();

    @Setter private Vector2 minSize;

    @Setter private Vector2 maxSize;

    @Setter private Vector2 preferredSize;

    @Getter private boolean isValid = true;

    @Getter private boolean isFocusable = false;

    public KrWidget() {
    }

    public KrWidget(String name) {
        this.name = name;
    }

    public int getChildCount() {
        return children.size();
    }

    public KrWidget getChild(int index) {
        return children.get(index);
    }

    public void ensureUniqueStyle() {
    }

    public void add(KrWidget child) {
        add(child, null);
    }

    public void add(KrWidget child, Object layoutConstraint) {
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Widget already has a parent: " + child.getParent());
        }
        children.add(child);
        child.setParent(this);
        child.setCanvas(this.canvas);

        layout.addWidget(child, layoutConstraint);

        invalidate();
    }

    public void remove(KrWidget child) {

        layout.removeWidget(child);

        child.setCanvas(null);
        child.setParent(null);
        children.remove(child);

        if (child.isFocused) {
            getCanvas().clearFocus();
        }

        invalidate();
    }

    private void setParent(KrWidget parent) {
        this.parent = parent;
    }

    public void setPosition(float x, float y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            invalidate();
        }
    }

    public void setPosition(Vector2 position) {
        this.setPosition(position.x, position.y);
    }

    public void setSize(float w, float h) {
        if (this.width != w || this.height != h) {
            this.width = w;
            this.height = h;
            invalidate();
        }
    }

    public void setSize(Vector2 size) {
        setSize(size.x, size.y);
    }

    public void setBounds(float x, float y, float width, float height) {
        if (this.x == x && this.y == y && this.width == width && this.height == height) {
            return;
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        invalidate();
    }

    public void setBounds(Vector2 position, Vector2 size) {
        this.setBounds(position.x, position.y, size.x, size.y);
    }

    public void setBounds(Rectangle bounds) {
        this.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Object getStyle() {
        return null;
    }

    public void validate() {
        layout.setGeometry(new Rectangle(0, 0, getWidth(), getHeight()));
        isValid = true;
    }

    public void invalidate() {
        isValid = false;
        notifyWidgetInvalidated();
        invalidateParent();
    }

    public void invalidateParent() {
        if (parent != null) {
            parent.invalidate();
        }
    }

    public void draw(KrRenderer renderer) {
        drawSelf(renderer);
        drawChildren(renderer);
    }

    public void update(float deltaSeconds) {
        if (!isValid) {
            validate();
        }
        updateChildren(deltaSeconds);
    }

    public void updateChildren(float deltaSeconds) {
        children.forEach(child -> child.update(deltaSeconds));
    }

    protected void drawSelf(KrRenderer renderer) {
    }

    protected void drawChildren(KrRenderer renderer) {
        renderer.translate(getX(), getY());
        for (KrWidget child : children) {
            child.draw(renderer);
        }
        renderer.translate(-getX(), -getY());
    }

    public Vector2 calculatePreferredSize() {
        return layout.getPreferredSize();
    }

    public boolean isMaxSizeSet() {
        return maxSize != null;
    }

    public Vector2 getMaxSize() {
        if (isMaxSizeSet()) {
            return maxSize;
        }

        if (!(layout instanceof KrAbsoluteLayout)) {
            return layout.getMaxSize();
        }

        return new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public boolean isMinSizeSet() {
        return minSize != null;
    }

    public Vector2 getMinSize() {
        if (isMinSizeSet()) {
            return minSize;
        }

        if (!(layout instanceof KrAbsoluteLayout)) {
            return layout.getMinSize();
        }

        return calculatePreferredSize();
    }

    public boolean isPreferredSizeSet() {
        return preferredSize != null;
    }

    public Vector2 getPreferredSize() {
        if (isPreferredSizeSet()) {
            return preferredSize;
        }

        if (!(layout instanceof KrAbsoluteLayout)) {
            return layout.getPreferredSize();
        }

        return calculatePreferredSize();
    }

    private Rectangle getScreenBounds() {
        float offsetX = 0;
        float offsetY = 0;
        if (parent != null) {
            Rectangle parentBounds = parent.getScreenBounds();
            offsetX = parentBounds.x;
            offsetY = parentBounds.y;
        }
        return new Rectangle(offsetX + getX(), offsetY + getY(), getWidth(), getHeight());
    }

    public Rectangle getGeometry() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public Vector2 getSize() {
        return new Vector2(getWidth(), getHeight());
    }

    public KrWidget getTopLevelAncestor() {
        KrWidget widget = this;
        if (widget.parent != null) {
            return widget.parent.getTopLevelAncestor();
        } else {
            return this;
        }
    }

    public KrCanvas getCanvas() {
        if (canvas != null) {
            return canvas;
        }

        if (parent != null) {
            return parent.getCanvas();
        }

        return null;
    }

    public boolean requestFocus() {
        KrCanvas canvas = getCanvas();
        return canvas != null && canvas.requestFocus(this);
    }

    public void clearFocus() {
        KrCanvas canvas = getCanvas();
        if (canvas != null) {
            canvas.clearFocus();
        }
    }

    public boolean acceptsTabInput() {
        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean handle(KrEvent event) {
        if (event instanceof KrMouseEvent) {
            KrMouseEvent mouseEvent = (KrMouseEvent) event;
            switch ((mouseEvent).getType()) {
                case MOVED:
                    return mouseMoveEvent(mouseEvent);
                case PRESSED:
                    return mousePressedEvent(mouseEvent);
                case RELEASED:
                    return mouseReleasedEvent(mouseEvent);
            }
        }

        if (event instanceof KrKeyEvent) {
            KrKeyEvent keyEvent = (KrKeyEvent) event;
            switch (keyEvent.getType()) {
                case PRESSED:
                    return keyPressedEvent(keyEvent);
                case RELEASED:
                    return keyReleasedEvent(keyEvent);
            }
        }

        if (event instanceof KrScrollEvent) {
            return scrolledEvent((KrScrollEvent) event);
        }

        if (event instanceof KrEnterEvent) {
            return enterEvent((KrEnterEvent) event);
        }

        if (event instanceof KrExitEvent) {
            return exitEvent((KrExitEvent) event);
        }

        if (event instanceof KrFocusEvent) {
            KrFocusEvent focusEvent = (KrFocusEvent) event;
            switch (focusEvent.getType()) {
                case FOCUS_GAINED:
                    return focusGainedEvent(focusEvent);
                case FOCUS_LOST:
                    return focusLostEvent(focusEvent);
            }
        }

        return false;
    }

    protected boolean keyPressedEvent(KrKeyEvent event) {
        notifyKeyPressed(event);
        return event.handled();
    }

    protected boolean keyReleasedEvent(KrKeyEvent event) {
        notifyKeyReleased(event);
        return event.handled();
    }

    protected boolean scrolledEvent(KrScrollEvent event) {
        notifyMouseScrolled(event);
        return event.handled();
    }

    protected boolean mouseMoveEvent(KrMouseEvent event) {
        notifyMouseMoved(event);
        return event.handled();
    }

    protected boolean mousePressedEvent(KrMouseEvent event) {
        notifyMousePressed(event);
        return event.handled();
    }

    protected boolean mouseReleasedEvent(KrMouseEvent event) {
        notifyMouseReleased(event);
        return event.handled();
    }

    protected boolean enterEvent(KrEnterEvent event) {
        notifyMouseEnter(event);
        return event.handled();
    }

    protected boolean exitEvent(KrExitEvent event) {
        notifyMouseExit(event);
        return event.handled();
    }

    protected boolean focusGainedEvent(KrFocusEvent event) {
        isFocused = true;
        notifyFocusGained(event);
        return event.handled();
    }

    protected boolean focusLostEvent(KrFocusEvent event) {
        isFocused = false;
        notifyFocusLost(event);
        return event.handled();
    }

    public void addKeyboardListener(KrKeyboardListener listener) {
        keyboardListeners.add(listener);
    }

    public void removeKeyboardListener(KrKeyboardListener listener) {
        keyboardListeners.remove(listener);
    }

    protected void notifyKeyPressed(KrKeyEvent event) {
        keyboardListeners.forEach(l -> l.keyPressed(event));
    }

    protected void notifyKeyReleased(KrKeyEvent event) {
        keyboardListeners.forEach(l -> l.keyReleased(event));
    }

    public void addMouseListener(KrMouseListener mouseListener) {
        mouseListeners.add(mouseListener);
    }

    public void removeMouseListener(KrMouseListener mouseListener) {
        mouseListeners.remove(mouseListener);
    }

    protected void notifyMouseScrolled(KrScrollEvent event) {
        mouseListeners.forEach(l -> l.scrolled(event));
    }

    protected void notifyMouseMoved(KrMouseEvent event) {
        mouseListeners.forEach(l -> l.mouseMoved(event));
    }

    protected void notifyMousePressed(KrMouseEvent event) {
        mouseListeners.forEach(l -> l.mousePressed(event));
    }

    protected void notifyMouseReleased(KrMouseEvent event) {
        mouseListeners.forEach(l -> l.mouseReleased(event));
    }

    protected void notifyMouseEnter(KrEnterEvent event) {
        mouseListeners.forEach(l -> l.enter(event));
    }

    protected void notifyMouseExit(KrExitEvent event) {
        mouseListeners.forEach(l -> l.exit(event));
    }

    public void addFocusListener(KrFocusListener focusListener) {
        focusListeners.add(focusListener);
    }

    public void removeFocusListener(KrFocusListener focusListener) {
        focusListeners.add(focusListener);
    }

    protected void notifyFocusGained(KrFocusEvent event) {
        focusListeners.forEach(l -> l.focusGained(event));
    }

    protected void notifyFocusLost(KrFocusEvent event) {
        focusListeners.forEach(l -> l.focusLost(event));
    }

    public void setFocusable(boolean focusable) {
        if (this.isFocusable != focusable) {
            this.isFocusable = focusable;
            notifyWidgetPropertyChanged(FOCUS_PROPERTY, !isFocusable, isFocusable);
        }
    }

    public void addWidgetListener(KrWidgetListener listener) {
        widgetListeners.add(listener);
    }

    public void removeWidgetListener(KrWidgetListener listener) {
        widgetListeners.remove(listener);
    }

    protected void notifyWidgetPropertyChanged(String propertyName, Object oldValue, Object newValue) {
        widgetListeners.forEach(listener -> listener.propertyChanged(propertyName, oldValue, newValue));
    }

    protected void notifyWidgetInvalidated() {
        widgetListeners.forEach(KrWidgetListener::invalidated);
    }

    protected final Vector2 expandSizeWithPadding(Vector2 size, KrPadding padding) {
        return new Vector2(size.x + padding.getHorizontalPadding(), size.y + padding.getVerticalPadding());
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    public KrWidgetToStringBuilder toStringBuilder() {
        return KrWidgetToStringBuilder.builder().name(name).bounds(getGeometry()).enabled(true).visible(true);
    }
}
