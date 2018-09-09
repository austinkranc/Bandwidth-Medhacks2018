package com.bandwidth.testmh3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.round;
import android.hardware.SensorEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    FirebaseDatabase database;
    DatabaseReference myTempRef;
    DatabaseReference myWidthRef;
    DatabaseReference myHumidityRef;
    String width;
    String temp;
    String humidy;


    //https://testmh3-abcf9.firebaseio.com/


    //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 //   SharedPreferences.Editor editor = preferences.edit();

/*External Testing
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e("hi", "Directory not created");
        }
        return file;
    }*/

    int graphing;

    @BindView(R.id.buttonelle) Button button1;
    @BindView(R.id.warm) Button warm;
    @BindView(R.id.warmer) Button warmer;
    @BindView(R.id.warmest) Button warmest;
    //@BindView(R.id.textelle) TextView text1;
    @BindView (R.id.ptlist) LinearLayout scrolly;
    @BindView(R.id.textView3) TextView t3;
    @BindView(R.id.header) TextView hdr;
    @BindView(R.id.leg_csa) ImageView leg1;
    @BindView(R.id.leg_temp) ImageView leg2;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    GraphView graph;

    DataPoint[] points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graphing = 0;
        points = new DataPoint[20];
        for(int i=0;i<20;i++){
            points[i] = new DataPoint(i,0);
        }
        graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.INVISIBLE);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);


        Log.d("VVV","VVVVVVV");
        database = FirebaseDatabase.getInstance();

        //Log.d("OOO",database.toString());
        myTempRef = database.getReference("/values/temp");
        myWidthRef = database.getReference("/values/width");
        myHumidityRef = database.getReference("/values/humidity");

        /**Shared pref testing
         */


        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("key_name1", true);           // Saving boolean - true/false
        editor.putInt("key_name2", 5);        // Saving integer
        editor.putFloat("key_name3", 6.0f);    // Saving float
        editor.putLong("key_name4", 99999);      // Saving long
        editor.putString("key_name5", "string value");  // Saving string
        editor.commit();

     //   myTempRef.setValue("5");
     //   myWidthRef.setValue("10");

        final AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        myTempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("HI", "Value is: " + value);
                temp = value;
                button1.setText(width +" "+ temp) ;
                if(graphing == 1){
                    t3.setText("TEMPERATURE\n"+temp + " °F");
                }

                if(Float.parseFloat(temp) <= 74){
                        warm.setVisibility(View.VISIBLE);
                } else if (Float.parseFloat(temp) <= 75){
                    warmer.setVisibility(View.VISIBLE);
                }else{
                    warmest.setVisibility(View.VISIBLE);


                    builder3.setTitle("WARNING: REGIONAL TEMPERATURE INCREASE");


                    builder3.show();

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("HEY", "Failed to read value.", error.toException());
            }
        });
        myWidthRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("WHI", "Value is: " + value);
                width = value;
                button1.setText(width +" "+ temp) ;
                if(graphing==0){
                    t3.setText("CROSS SECTIONAL AREA\n"+width + " sq in\nPERCENT CHANGE\n"+(abs(1-Float.parseFloat(width)/7)*10000)/100 + "%");
                }
                if(Float.parseFloat(width)>=7.3){
                    builder3.setTitle("WARNING: PATIENT LEG SWOLEN");


                    builder3.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("WHEY", "Failed to read value.", error.toException());
            }
        });


        myHumidityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("HHI", "Value is: " + value);
                humidy = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("HHEY", "Failed to read value.", error.toException());
            }
        });

        ButterKnife.bind(this);
        warm.setVisibility(View.INVISIBLE);
        warmer.setVisibility(View.INVISIBLE);
        warmest.setVisibility(View.INVISIBLE);

    }

    @OnClick(R.id.csa)
    public void ccc(View view){
        graphing = 0;
        t3.setText("CROSS SECTIONAL AREA\n"+width + " sq in\nPERCENT CHANGE\n"+(abs(1-Float.parseFloat(width)/7)*10000)/100 + "%");
        leg1.setVisibility(View.VISIBLE);
        leg2.setVisibility(View.INVISIBLE);
        warm.setVisibility(View.INVISIBLE);
        warmer.setVisibility(View.INVISIBLE);
        warmest.setVisibility(View.INVISIBLE);
        graph.setVisibility(View.INVISIBLE);
    }
    float x;
    float y;
    float z;



    @OnClick(R.id.temp)
    public void ttt(View view){
        graphing = 1;
        t3.setText("TEMPERATURE\n"+temp + " °F");
        leg1.setVisibility(View.INVISIBLE);
        leg2.setVisibility(View.VISIBLE);
        graph.setVisibility(View.INVISIBLE);

        if(Float.parseFloat(temp) <= 74){
            warm.setVisibility(View.VISIBLE);
            warmer.setVisibility(View.INVISIBLE);
            warmest.setVisibility(View.INVISIBLE);
        } else if (Float.parseFloat(temp) <= 75){
            warmer.setVisibility(View.VISIBLE);
            warm.setVisibility(View.INVISIBLE);
            warmest.setVisibility(View.INVISIBLE);
        }else{
            warmest.setVisibility(View.VISIBLE);
            warmer.setVisibility(View.INVISIBLE);
            warm.setVisibility(View.INVISIBLE);
        }
    }



    @OnClick(R.id.buttonelle)
    public void submit(View view) {
        button1.setText(pref.getString("key_name5", null)) ;
        String textString = " ";
        StringBuilder text = new StringBuilder();


        button1.setText(width +" "+ temp) ;


    }


    String m_Text = "";

    String [] names = new String[30];
    Button btn;
    int cont = 0;

    @OnClick(R.id.addpt)
    public void adder(View view){

        btn = new Button(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder.setTitle("Patient ID:");
        builder2.setTitle("Band ID:");
// Set up the input
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder2.setView(input2);

// Set up the buttons
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();

                scrolly.addView(btn);
                btn.setText(m_Text);
                names[cont]=m_Text;
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        hdr.setText(names[cont].toUpperCase());
                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Log.d("HI","THERESKY");
        builder.show();
        builder2.show();
        cont++;




    }
    LineGraphSeries<DataPoint> series;
    int xcount=0;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
        }


        points[xcount]= new DataPoint(xcount,abs(x)+abs(y)+abs(z)-12.81);
        xcount++;
        xcount%=20;
        if(xcount==0){
            graph.removeAllSeries();
            for(int i=0;i<20;i++){
                points[i] = new DataPoint(i,0);
            }
        }


        if(graphing == 2){

            series = new LineGraphSeries<>(points);
            graph.addSeries(series);
            t3.setText("X axis: "+x+"\nY axis: "+y+"\nZ axis: "+z);


        }
    }

    Boolean isnew = true;

    @OnClick(R.id.movement)
    public void mmm(View view){
        t3.setText(x+" "+y+" "+z);
        leg1.setVisibility(View.INVISIBLE);
        leg2.setVisibility(View.INVISIBLE);
        warm.setVisibility(View.INVISIBLE);
        warmer.setVisibility(View.INVISIBLE);
        warmest.setVisibility(View.INVISIBLE);
        graph.setVisibility(View.VISIBLE);


        // set manual X bounds
        if(isnew) {
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(15);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(20);

            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);
        isnew = false;
        }
        graphing = 2;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }


    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

