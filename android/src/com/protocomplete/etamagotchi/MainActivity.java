package com.protocomplete.etamagotchi;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Context;
import android.view.View;
import android.text.InputType;
import android.view.View.OnClickListener;
import android.util.Log;
import android.content.res.Configuration;
import android.content.DialogInterface;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import com.protocomplete.etamagotchi.R;

public class MainActivity extends Activity {
   String msg = "Android : ";
   RenderView view;

   /** Special class implementations */
   public String getLocalIpAddress(){
      try {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
          NetworkInterface intf = en.nextElement();

          for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {

            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!inetAddress.isLoopbackAddress()) {
              return inetAddress.getHostAddress();
            }
          }
        }
      } catch (Exception ex) {
         Log.e("IP Address", ex.toString());
     }

     return null;
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // view = new RenderView(this);

      setContentView(R.layout.main);

      view = (RenderView)findViewById(R.id.view);

      Button feedButton = (Button)findViewById(R.id.feedButton);

      feedButton.setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            // Code here executes on main thread after user presses button
            view.feedMonster();
          }
        }
      );

      Button battleButton = (Button)findViewById(R.id.battleButton);

      battleButton.setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            // Let the user choose to host or join and delegate that
            final CharSequence battleTypes[] = new CharSequence[] {"Host", "Join", "Train"};

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose Battle");
            builder.setItems(battleTypes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on builder[which]
                    if(battleTypes[which].equals("Host")) {
                      // Toast the IP address...

                      // insert at 0 == prepend a string
                      String text = "Monster location is " + getLocalIpAddress();
                      int duration = 40*1000; //Toast.LENGTH_SHORT;

                      Toast toast = Toast.makeText(MainActivity.this, (CharSequence)text, duration);
                      toast.show();

                      view.hostBattle();
                    } else if(battleTypes[which].equals("Join")) {
                      // Request an IP address...

                      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                      builder.setTitle("Join a P2P battle");

                      // Set up the input
                      final EditText input = new EditText(MainActivity.this);
                      // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                      input.setInputType(InputType.TYPE_CLASS_TEXT);
                      builder.setView(input);

                      // Set up the buttons
                      builder.setPositiveButton("FIGHT!", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              String ip = input.getText().toString();
                              view.joinBattle(ip);
                          }
                      });
                      builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              dialog.cancel();
                          }
                      });

                      builder.show();

                    } else if(battleTypes[which].equals("Train")) {
                      // Do training...
                      view.train();
                    }
                }
            });
            builder.show();
          }
        }
      );

      Log.d(msg, "The onCreate() event");
   }

   /** Called when the activity is about to become visible. */
   @Override
   protected void onStart() {
      super.onStart();
    //  view.loadMonster();
      Log.d(msg, "The onStart() event");
   }

   /** Called when the activity has become visible. */
   @Override
   protected void onResume() {
    super.onResume();
    view.onResume();
    Log.d(msg, "The onResume() event");
   }

   /** Called when another activity is taking focus. */
   @Override
   protected void onPause() {
    super.onPause();
    view.saveMonster();
    view.onPause();
    Log.d(msg, "The onPause() event");
   }

   /** Called when the activity is no longer visible. */
   @Override
   protected void onStop() {
    super.onStop();
    view.saveMonster();
    Log.d(msg, "The onStop() event");
   }

   /** Called just before the activity is destroyed. */
   @Override
   public void onDestroy() {
    view.saveMonster();
    super.onDestroy();
    Log.d(msg, "The onDestroy() event");
   }

  /*@Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    setContentView(R.layout.main);

    view = (RenderView)findViewById(R.id.view);

    Button feedButton = (Button)findViewById(R.id.feedButton);

    // view.loadMonster();

    feedButton.setOnClickListener(
      new OnClickListener() {
        public void onClick(View v) {
          // Code here executes on main thread after user presses button
          view.feedMonster();
        }
      }
    );
  }*/
}
