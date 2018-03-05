package com.protocomplete.etamagotchi;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;


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
  public static Monster read(String path) {
    try {
    	File fXmlFile = new File(path);
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

    	Node monster = doc.getElementByTagName("Monster");

      if (monster.getNodeType() == Node.ELEMENT_NODE) {
  			Element el = (Element) monster;

  			int ID = Integer.toInt(el.getElementByTagName("ID").getTextContent());
        String name = el.getElementByTagName("Name");

        Element stats = el.getElementByTagName("Stats");
        Element health = stats.getElementByTagName("Health");
        Element damage = stats.getElementByTagName("Damage");
        Element p2p = el.getElementByTagName("P2P");
        Element kdr = p2p.getElementByTagName("KDR");

        int hp = Integer.toInt(health.getElementByTagName("Current").getTextContent());
        int maxHP = Integer.toInt(health.getElementByTagName("Max").getTextContent());
        int minDmg = Integer.toInt(damage.getElementByTagName("Min").getTextContent());
        int maxDmg = Integer.toInt(damage.getElementByTagName("Max").getTextContent());
        int wins = Integer.toInt(kdr.getElementByTagName("Wins").getTextContent());
        int losses = Integer.toInt(kdr.getElementByTagName("Losses").getTextContent());

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
