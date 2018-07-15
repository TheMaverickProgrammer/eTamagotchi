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
import javax.xml.transform.OutputKeys;

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

	public static void write(Monster mon, File dir, String path) {
	  try {
  		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
  		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

  		// root element <eTamagotchi version="x.x.x">
  		Document doc = docBuilder.newDocument();
  		Element rootElement = doc.createElement("eTamagotchi");
      // set attribute to Monster element
      Attr attr = doc.createAttribute("version");
      attr.setValue("1.0");
      rootElement.setAttributeNode(attr);
  		doc.appendChild(rootElement);

			// <Monster>
			// 	<ID>23</ID>
			Element monster = doc.createElement("Monster");
			Element ID = doc.createElement("ID");
			ID.appendChild(doc.createTextNode(Integer.toString(mon.getID())));
			monster.appendChild(ID);
			// </ID>


  		// <Name>Agumon</Name>
  		Element name = doc.createElement("Name");
  		name.appendChild(doc.createTextNode(mon.getName()));
  		monster.appendChild(name);

  		// <Stats>
      Element stats = doc.createElement("Stats");
      Element health = doc.createElement("Health");
      Element current = doc.createElement("Current");
      current.appendChild(doc.createTextNode(Integer.toString(mon.getHP())));

      Element maxhp = doc.createElement("Max");
      maxhp.appendChild(doc.createTextNode(Integer.toString(mon.getMaxHP())));

      // <Health>
      //  <Current>6</Current>
      //  <Max>6</Max>
      // </Health>
      health.appendChild(maxhp); health.appendChild(current);
      stats.appendChild(health);

  		// nickname elements
  		Element damage = doc.createElement("Damage");
      Element mindmg = doc.createElement("Min");
      mindmg.appendChild(doc.createTextNode(Integer.toString(mon.getMinDamage())));

      Element maxdmg = doc.createElement("Max");
      maxdmg.appendChild(doc.createTextNode(Integer.toString(mon.getMaxDamage())));

      // <Damage>
      //  <Min> 1 </Min>
      //  <Max> 2 </Max>
      // </Damage>
      damage.appendChild(maxdmg); damage.appendChild(mindmg);
      stats.appendChild(damage);
      // </Stats>
      monster.appendChild(stats);

      //<P2P>
      Element p2p = doc.createElement("P2P");
      Element kdr = doc.createElement("KDR");

      Element wins = doc.createElement("Wins");
      wins.appendChild(doc.createTextNode(Integer.toString(mon.getWins())));

      Element losses = doc.createElement("Losses");
      losses.appendChild(doc.createTextNode(Integer.toString(mon.getLosses())));

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
      monster.appendChild(p2p);
      // </Monster>
			rootElement.appendChild(monster);
			// </eTamagotchi>

  		// write the content into xml file
  		TransformerFactory transformerFactory = TransformerFactory.newInstance();
  		Transformer transformer = transformerFactory.newTransformer();
  		DOMSource source = new DOMSource(doc);

      File file = new File(dir, "saves"); // overwrite

      if(!file.exists()) {
        file.mkdir();
      }

  		StreamResult result = new StreamResult(new File(file, path));
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
  		transformer.transform(source, result);

      // Saved!
	  } catch (ParserConfigurationException pce) {
		  pce.printStackTrace();
	  } catch (TransformerException tfe) {
		  tfe.printStackTrace();
	  }
	}
}
