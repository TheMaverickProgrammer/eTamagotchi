package com.protocomplete.etamagotchi;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Monster {
  int ID, hp, maxHP, minDamage, maxDamage;
  int wins, losses; // pvp
  long lastFedTimestamp, lastCareTimestamp;
  String name;
  String birthday;

  Monster(int ID, String birthday, String name, long lastFedTimestamp, long lastCareTimestamp, int hp, int maxHP, int minDamage, int maxDamage, int wins, int losses) {
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
    this.lastCareTimestamp = lastCareTimestamp;
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

      // Get system clock time now
      Long now = new Date().getTime();

      // Find the difference
      Long diff = now - millis;

      // Convert into days
      int days = (int)(diff / 1000 / 60 / 60 / 24);

      return days;

    } catch (ParseException e) {
      e.printStackTrace();
    }

    return 0;
  }

  public long getLastFedTimestamp() {
    return this.lastFedTimestamp;
  }

  public long getLastCareTimestamp() {
    return this.lastCareTimestamp;
  }

  public int getHoursSinceLastFed() {
    long lastFedTimestamp = this.getLastFedTimestamp();
    long now = new Date().getTime();
    long diff= now - lastFedTimestamp;
    int hours = (int)(diff / 1000 / 60 / 60);

    return hours;
  }

  public int getHoursSinceLastCared() {
    long lastCareTimestamp = this.getLastCareTimestamp();
    long now = new Date().getTime();
    long diff= now - lastCareTimestamp;
    int hours = (int)(diff / 1000 / 60 / 60);

    return hours;
  }

  public boolean isEgg() {
    return (this.getDaysOld() == 0);
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
    this.lastFedTimestamp = new Date().getTime();
  }

  public void care() {
    this.lastCareTimestamp = new Date().getTime();
  }

  public void sleep() {
    updateHP(getHP() + 1);
  }

  public void starve() {
    updateHP(getHP() - 1);
  }

  public void updateHP(int newHP) {
    this.hp = newHP;

    if(this.hp > this.maxHP) {
      this.hp = this.maxHP;
    }

    if(this.hp <= 0) {
      this.hp = 0;
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
