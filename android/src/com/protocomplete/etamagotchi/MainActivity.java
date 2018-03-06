package com.protocomplete.etamagotchi;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.content.res.Configuration;
import com.protocomplete.etamagotchi.R;

public class MainActivity extends Activity {
   String msg = "Android : ";
   RenderView view;


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

      Log.d(msg, "The onCreate() event");
   }

   /** Called when the activity is about to become visible. */
   @Override
   protected void onStart() {
      super.onStart();
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
      view.saveMonster();
      super.onPause();
      view.onPause();
      Log.d(msg, "The onPause() event");
   }

   /** Called when the activity is no longer visible. */
   @Override
   protected void onStop() {
     view.saveMonster();
     super.onStop();
     Log.d(msg, "The onStop() event");
   }

   /** Called just before the activity is destroyed. */
   @Override
   public void onDestroy() {
     view.saveMonster();
     super.onDestroy();
     Log.d(msg, "The onDestroy() event");
   }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

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
  }
}
