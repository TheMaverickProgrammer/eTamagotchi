package com.protocomplete.etamagotchi;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Context;
import android.view.View;
import android.util.Log;
import android.content.res.Configuration;
import com.protocomplete.etamagotchi.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StatsActivity extends Activity {
   String msg = "Android : ";

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.stats);

      Intent intent = getIntent();
      String data = intent.getStringExtra("data");

      final TextView textName = (TextView) findViewById(R.id.textName);
      textName.setText(data);
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
    Log.d(msg, "The onResume() event");
   }

   /** Called when another activity is taking focus. */
   @Override
   protected void onPause() {
    super.onPause();
    Log.d(msg, "The onPause() event");
   }

   /** Called when the activity is no longer visible. */
   @Override
   protected void onStop() {
    super.onStop();
    Log.d(msg, "The onStop() event");
   }

   /** Called just before the activity is destroyed. */
   @Override
   public void onDestroy() {
    super.onDestroy();
    Log.d(msg, "The onDestroy() event");
   }
}
