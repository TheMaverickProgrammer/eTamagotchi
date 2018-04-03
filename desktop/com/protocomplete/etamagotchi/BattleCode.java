package com.protocomplete.etamagotchi;

import java.text.ParseException;

/***
Transforms IP addresses into shorter battle code
***/

public class BattleCode {
	public static String pack(String ip) {
    System.out.println("ip: " + ip);

    String[] tokens = ip.split("\\.");

    if(tokens.length != 4) {
      //TODO: throw new ParseException("Not a valid IP address!");
    }

    String result = new String();

    for(int i = 0; i < tokens.length; i++) {
      String hex = Integer.toHexString(Integer.parseInt(tokens[i]));
      System.out.println("hex: " + hex);

      if(hex.length() == 1) {
        hex = "0"+hex;
      }

      result += hex;
    }

    return result.toUpperCase();
  }

  public static String unpack(String code) {
    code = code.toLowerCase();
    String ip = new String();

    // Lump hex together until it goes over 255
    // as IPs cannot go over 255 we know we have
    // max two digits we need to convert
    String pair = new String();
    int pairSize = 0, index = 0;

    while(index < code.length()) {
        pair += code.charAt(index);
        pairSize++;
        index++;

      if(pairSize >= 2) {
        int base2 = Integer.parseInt(pair, 16);

        if(base2 > 255) {
          base2 = Integer.parseInt(Character.toString(pair.charAt(0)), 16);
          index--;
        }

        pair = "";

        if(ip.length() != 0) {
          ip += ".";
        }

        ip += "" + base2;

        pairSize = 0;
      }
    }

    return ip;
  }
}
