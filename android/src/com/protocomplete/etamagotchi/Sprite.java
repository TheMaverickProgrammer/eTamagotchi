package com.protocomplete.etamagotchi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Matrix;

class Sprite {
  int posX, posY, velX, velY, height, width;
  int subX, subY, subW, subH;
  int scale;
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
    scale = 1;
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
    scale = 1;
  }

  public void onDraw(Canvas canvas) {
    update();
    Rect surface = new Rect(subX, subY, subX+subW, subY+subH);
    Rect output = new Rect(posX, posY, posX+(subW*scale), posY+(subH*scale));
    canvas.drawBitmap(source, surface, output, null);
  }

  public void onDraw(Canvas canvas, Paint paint) {
    update();
    Rect surface = new Rect(subX, subY, subX+subW, subY+subH);
    Rect output = new Rect(posX, posY, posX+(subW*scale), posY+(subH*scale));
    canvas.drawBitmap(source, surface, output, paint);
  }


  public void update() {
    posX += velX;
    posY += velY;
  }

  public void setScale(int scale) {
    this.scale = scale;
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

  public Sprite flipX() {
    Matrix matrix = new Matrix();
    matrix.preScale(-1.0f, 1.0f);
    Sprite flipped = new Sprite(target, Bitmap.createBitmap(source, subX, subY, subW, subH, matrix, true));
    flipped.setScale(this.scale);
    return flipped;
  }

  public Sprite flipY() {
    Matrix matrix = new Matrix();
    matrix.preScale(1.0f, -1.0f);
    Sprite flipped = new Sprite(target, Bitmap.createBitmap(source, subX, subY, subW, subH, matrix, true));
    flipped.setScale(this.scale);
    return flipped;
  }
}
