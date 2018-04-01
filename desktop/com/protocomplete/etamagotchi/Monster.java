package com.protocomplete.etamagotchi;

import java.text.SimpleDateFormat;

public class Monster {
  int ID, hp, maxHP, minDamage, maxDamage;
  int wins, losses; // pvp
  long lastFedTimestamp;
  String name;
  String birthday;

  Monster(int ID, String birthday, String name, long lastFedTimestamp, int hp, int maxHP, int minDamage, int maxDamage, int wins, int losses) {
    this.ID = ID;
    this.birthday = birthday;
    this.name = name;
    this.hp = hp;
    this.maxHP = maxHP;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.wins = wins;
    this.losses = losses;
    this.lastFedTimestamp = lastFedTimestamp;
  }

  public int getID() {
    return this.ID;
  }

  public String getBirthday() {
    return this.birthday;
  }

  public int getDaysOld() {
    // Convert birthday string into timestamp
    try {
      Long millis = new SimpleDateFormat("MM/dd/yyyy").parse(this.birthday).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }

    // Get system clock time now
    Long now = Date.getTime();

    // Find the difference
    Long diff = now - millis;

    // Convert into days
    int days = diff * 1000 * 60 * 60 * 24;

    return days;
  }

  public long getLastFedTimestamp() {
    return this.lastFedTimestamp;
  }

  public String getName() {
    return this.name;
  }

  public int getHP() {
    return this.hp;
  }

  public int getMaxHP() {
    return this.maxHP;
  }

  public int getMinDamage() {
    return this.minDamage;
  }

  public int getMaxDamage() {
    return this.maxDamage;
  }

  public int getWins() {
    return this.wins;
  }

  public int getLosses() {
    return this.losses;
  }

  public void eat() {
    updateHP(getHP() + 1);
    this.lastFedTimestamp = Date.getTime();
  }

  public void sleep() {

  }

  public void updateHP(int newHP) {
    this.hp = newHP;

    if(this.hp > this.maxHP) {
      this.hp = this.maxHP;
    }
  }

  // NOTE: you can make the monster dance, change their mood, randomize a stat?
  public void victory() {
    this.wins++;
  }

  // NOTE: you can make the monster's mood worse, temporarily decrease a stat...
  public void defeat() {
    this.losses++;
  }
}
