package com.protocomplete.etamagotchi;

public class Monster {
  int ID, hp, maxHP, minDamage, maxDamage;
  int wins, losses; // pvp
  String name;

  Monster(int ID, String name, int hp, int maxHP, int minDamage, int maxDamage, int wins, int losses) {
    this.ID = ID;
    this.name = name;
    this.hp = hp;
    this.maxHP = maxHP;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.wins = wins;
    this.losses = losses;
  }

  public int getID() {
    return this.ID;
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
