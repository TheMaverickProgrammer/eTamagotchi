package com.protocomplete.etamagotchi;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.*; // DEBUG


/***
XML Representation For version 1.0
****

<eTamagotchi version="1.0">
  <Monster>
    <ID>11</ID>
    <Name>Agumon</Name>
    <Stats>
      <Health>
        <Current>6</Current>
        <Max>6</Max>
      </Health>
      <Damage>
        <Min>2</Min>
        <Max>3</Max>
      </Damage>
    </Stats>
    <P2P>
      <KDR>
        <Wins>5</Wins>
        <Losses>2</Losses>
      </KDR>
      <History>
        ... list of previous monsters battled and their data could go here ...
      </History>
    </P2P>
  </Monster>
*/

public class MonsterReader {
  public static Monster read(File dir, String path) {
    try {
      File file = new File(dir, "saves");
      System.out.print("reading file from: " + file.getPath() + "\n");

      if(!file.exists()) {
        file.mkdir();
      }

      File fXmlFile = new File(file, path);
      System.out.print("reading file from: " + fXmlFile.getPath() + "\n");

    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	doc.getDocumentElement().normalize();

      String v1 = "1.0";
      String version = doc.getDocumentElement().getAttribute("version");

    	if(v1 != version) {
        return null;
      }

    	Node monster = doc.getElementsByTagName("Monster").item(0);

      if (monster.getNodeType() == Node.ELEMENT_NODE) {
  			Element el = (Element) monster;

  			int ID = Integer.parseInt(el.getElementsByTagName("ID").item(0).getTextContent());
        String name = el.getElementsByTagName("Name").item(0).getTextContent();

        Element stats = (Element) el.getElementsByTagName("Stats").item(0);
        Element health = (Element) stats.getElementsByTagName("Health").item(0);
        Element damage = (Element) stats.getElementsByTagName("Damage").item(0);
        Element p2p = (Element) el.getElementsByTagName("P2P").item(0);
        Element kdr = (Element) p2p.getElementsByTagName("KDR").item(0);

        int hp = Integer.parseInt(health.getElementsByTagName("Current").item(0).getTextContent());
        int maxHP = Integer.parseInt(health.getElementsByTagName("Max").item(0).getTextContent());
        int minDmg = Integer.parseInt(damage.getElementsByTagName("Min").item(0).getTextContent());
        int maxDmg = Integer.parseInt(damage.getElementsByTagName("Max").item(0).getTextContent());
        int wins = Integer.parseInt(kdr.getElementsByTagName("Wins").item(0).getTextContent());
        int losses = Integer.parseInt(kdr.getElementsByTagName("Losses").item(0).getTextContent());

        // ... read in the rest of the xml data...
        // NOTE: could read in a list of special move sets, stat buffs, unique moods, ...

        return new Monster(ID, name, hp, maxHP, minDmg, maxDmg, wins, losses);
      }

      return null;
    } catch (Exception e) {
      e.printStackTrace();
     return null;
    }
  }
}
