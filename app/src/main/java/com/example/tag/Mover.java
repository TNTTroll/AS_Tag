package com.example.tag;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;


public class Mover extends androidx.appcompat.widget.AppCompatButton {
    int name;
    BitmapDrawable icon;
    int pos;

    public Mover(Context _context) {
        super(_context);
    }

    public Mover(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Mover(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setParam(int _name, BitmapDrawable _icon, int _pos) {
        name = _name;
        icon = _icon;
        pos = _pos;

        setIcon(_icon);
    }

    public int getName() {
        return name;
    }

    public void setIcon(BitmapDrawable _newIcon) {
        icon = _newIcon;

        this.setBackground( icon );
    }

    public BitmapDrawable getIcon() { return icon; }

    public void setPos(int newPos) { pos = newPos; }

    public int getPos() { return pos; }
}
