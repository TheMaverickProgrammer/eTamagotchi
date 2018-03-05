package com.protocomplete.etamagotchi;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class MonsterWriter {

	public static void write(Monster mon, String path) {
	  try {
  		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
  		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

  		// root elements
  		Document doc = docBuilder.newDocument();
  		Element rootElement = doc.createElement("Monster");
      // set attribute to Monster element
      Attr attr = doc.createAttribute("ID");
      attr.setValue("1.0");
      rootElement.setAttributeNode(attr);
  		doc.appendChild(rootElement);

  		// <Name>Agumon</Name>
  		Element name = doc.createElement("Name");
  		name.appendChild(doc.createTextNode(mon.getName()));
  		rootElement.appendChild(name);

  		// <Stats>
      Element stats = doc.createElement("Stats");
      Element health = doc.createElement("Health");
      Element current = doc.createElement("Current");
      current.appendChild(doc.createTextNode(mon.getHP()))

      Element maxhp = doc.createElement("Max");
      maxhp.appendChild(doc.createTextNode(mon.getMaxHP()));

      // <Health>
      //  <Current>6</Current>
      //  <Max>6</Max>
      // </Health>
      health.appendChild(maxhp); health.appendChild(current);
      stats.appendChild(health);

  		// nickname elements
  		Element damage = doc.createElement("Damage");
      Element mindmg = doc.createElement("Min");
      mindmg.appendChild(doc.createTextNode(mon.getMinDamage()));

      Element maxdmg = doc.createElement("Max");
      maxdmg.appendChild(doc.createTextNode(mon.getMaxDamage()));

      // <Damage>
      //  <Min> 1 </Min>
      //  <Max> 2 </Max>
      // </Damage>
      damage.appendChild(maxdmg); damage.appendChild(mindmg);
      stats.appendChild(damage);
      // </Stats>
      rootElement.appendChild(stats);

      //<P2P>
      Element p2p = doc.createElement("P2P");
      Element kdr = doc.createElement("KDR");

      Element wins = doc.createElement("Wins");
      wins.appendChild(doc.createTextNode(mon.getWins()));

      Element losses = doc.createElement("Losses");
      losses.appendChild(doc.createTextNode(mon.getLosses()));

      // <KDR>
      //  <Wins>5</Wins>
      //  <Losses>2</Losses>
      // </KDR>
      kdr.appendChild(losses); kdr.appendChild(wins);
      p2p.appendChild(kdr);

      // <History></History>
      Element history = doc.createElement("History");
      p2p.appendChild(history);

      // </P2P>
      rootElement.appendChild(p2p);
      // </Monster>

  		// write the content into xml file
  		TransformerFactory transformerFactory = TransformerFactory.newInstance();
  		Transformer transformer = transformerFactory.newTransformer();
  		DOMSource source = new DOMSource(doc);
  		StreamResult result = new StreamResult(new File(path));
  		transformer.transform(source, result);

      // Saved!
	  } catch (ParserConfigurationException pce) {
		  pce.printStackTrace();
	  } catch (TransformerException tfe) {
		  tfe.printStackTrace();
	  }
	}
}
