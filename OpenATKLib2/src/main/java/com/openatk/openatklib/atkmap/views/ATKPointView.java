package com.openatk.openatklib.atkmap.views;

import android.graphics.Bitmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;

public class ATKPointView {
    private ATKPointClickListener clickListener;
    private boolean cluster = false;
    private boolean disabled = false;
    private ATKPointDragListener dragListener;
    private boolean draggable = false;
    private int iconHeight;
    private int iconWidth;
    private GoogleMap map;
    private Marker marker;
    private MarkerOptions markerOptions;
    private ATKPoint point;
    private Object userData;

    public ATKPointView(GoogleMap map2, ATKPoint point2, ATKPointViewOptions opts) {
        this.map = map2;
        this.point = point2;
        this.markerOptions = new MarkerOptions().position(point2.position);
        drawPoint();
    }

    public ATKPointView(GoogleMap map2, ATKPoint point2) {
        this.map = map2;
        this.point = point2;
        this.markerOptions = new MarkerOptions().position(point2.position);
        drawPoint();
    }

    public ATKPoint getAtkPoint() {
        return this.point;
    }

    public void setAtkPoint(ATKPoint point2) {
        this.point = point2;
        drawPoint();
    }

    public void update() {
        drawPoint();
    }

    public void remove() {
        if (this.marker != null) {
            this.marker.remove();
        }
        this.marker = null;
    }

    public void hide() {
        this.markerOptions.visible(false);
        if (this.marker != null) {
            this.marker.setVisible(false);
        }
    }

    public void show() {
        this.markerOptions.visible(true);
        if (this.marker != null) {
            this.marker.setVisible(true);
        }
    }

    public void setIcon(BitmapDescriptor icon, int width, int height) {
        this.iconHeight = height;
        this.iconWidth = width;
        this.markerOptions.icon(icon);
        if (this.marker != null) {
            this.marker.setIcon(icon);
        }
    }

    public void setIcon(Bitmap bitmapIcon) {
        this.iconHeight = bitmapIcon.getHeight();
        this.iconWidth = bitmapIcon.getWidth();
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
        this.markerOptions.icon(icon);
        if (this.marker != null) {
            this.marker.setIcon(icon);
        }
    }

    public int getIconWidth() {
        return this.iconWidth;
    }

    public int getIconHeight() {
        return this.iconHeight;
    }

    public void setAnchor(float horizontal, float vertical) {
        this.markerOptions.anchor(horizontal, vertical);
        if (this.marker != null) {
            this.marker.setAnchor(horizontal, vertical);
        }
    }

    public float getAnchorU() {
        return this.markerOptions.getAnchorU();
    }

    public float getAnchorV() {
        return this.markerOptions.getAnchorV();
    }

    public void setOnClickListener(ATKPointClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDragListener(ATKPointDragListener listener) {
        this.dragListener = listener;
    }

    public Boolean wasClicked(Marker clickedMarker) {
        if (this.marker == null || !this.marker.equals(clickedMarker)) {
            return null;
        }
        Boolean consumed = Boolean.valueOf(false);
        if (this.clickListener != null) {
            return Boolean.valueOf(this.clickListener.onPointClick(this));
        }
        return consumed;
    }

    public Boolean dragStart() {
        if (this.dragListener != null) {
            return Boolean.valueOf(this.dragListener.onPointDragStart(this));
        }
        return null;
    }

    public Boolean dragEnd() {
        if (this.dragListener != null) {
            return Boolean.valueOf(this.dragListener.onPointDragEnd(this));
        }
        return null;
    }

    public Boolean drag() {
        if (this.dragListener != null) {
            return Boolean.valueOf(this.dragListener.onPointDrag(this));
        }
        return null;
    }

    public void cluster(boolean cluster2) {
        this.cluster = cluster2;
    }

    public void disableDrawing(boolean disabled2) {
        this.disabled = disabled2;
        if (disabled2) {
            remove();
        }
    }

    private void drawPoint() {
        if (this.point.position != null && !this.disabled) {
            this.markerOptions.position(this.point.position);
            if (this.marker == null) {
                this.marker = this.map.addMarker(this.markerOptions);
            } else {
                this.marker.setPosition(this.point.position);
            }
        }
    }

    public void setSuperDraggable(boolean draggable2) {
        this.draggable = draggable2;
    }

    public boolean getSuperDraggable() {
        return this.draggable;
    }

    public void setData(Object data) {
        this.userData = data;
    }

    public Object getData() {
        return this.userData;
    }

    public boolean isVisible() {
        return this.marker.isVisible();
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
        } else {
            hide();
        }
    }

    public boolean getVisible() {
        if (this.marker != null) {
            return this.marker.isVisible();
        }
        return false;
    }

    public String getTitle() {
        return this.marker.getTitle();
    }

    public void setTitle(String title) {
        this.marker.setTitle(title);
    }

    public void hideTitle() {
        this.marker.hideInfoWindow();
    }

    public void setPosition(LatLng position) {
        if (this.point != null) {
            this.point.position = position;
            update();
        }
    }

    public void setDraggable(Boolean draggable2) {
        this.marker.setDraggable(draggable2.booleanValue());
    }
}
