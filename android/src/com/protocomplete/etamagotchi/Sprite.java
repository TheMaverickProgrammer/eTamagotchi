package com.protocomplete.etamagotchi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;

class Sprite {
  int posX, posY, velX, velY, height, width;
  int subX, subY, subW, subH;
  Bitmap source;
  RenderView target;

  public Sprite(RenderView view, Bitmap bitmap) {
    height = bitmap.getHeight();
    width = bitmap.getWidth();
    target = view;
    source = bitmap;
    posX = posY = velX = velY = 0;
    subX = subY = 0;
    subW = width;
    subH = height;
  }

  public Sprite(RenderView view, Bitmap bitmap, int subX, int subY, int subW, int subH) {
    height = subW;
    width = subH;
    target = view;
    source = bitmap;
    posX = posY = velX = velY = 0;
    this.subX = subX;
    this.subY = subY;
    this.subW = subW;
    this.subH = subH;
  }

  public void onDraw(Canvas canvas) {
    update();
    Rect surface = new Rect(subX, subY, subW, subH);
    Rect output = new Rect(posX, posY, posX+width, posY+height);
    canvas.drawBitmap(source, surface, output, null);
  }

  public void onDraw(Canvas canvas, Paint paint) {
    update();
    Rect surface = new Rect(subX, subY, subW, subH);
    Rect output = new Rect(posX, posY, posX+width, posY+height);
    canvas.drawBitmap(source, surface, output, paint);
  }


  public void update() {
    posX += velX;
    posY += velY;
  }

  public void setVelX(int x) {
    velX = x;
  }

  public void setVelY(int y) {
    velY = y;
  }

  public void setPosX(int x) {
    posX = x;
  }

  public void setPosY(int y) {
    posY = y;
  }
}
