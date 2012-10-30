package me.sensor.accelerometer;

import java.io.*;
import java.net.*;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AccelerometerActivity extends Activity {
    
	// private parameters
	private Float x;
    private Float y;
    private Float z;
	
    private long size;
    private File log;
    private DataOutputStream dos;
    
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    
    private Socket socket;
    private String name = "143.89.210.169";
    final private int port = 9999;
    
    private int state = 0;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // lock orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        
        // get two buttons
        Button start = (Button)findViewById(R.id.button1);
        Button end = (Button)findViewById(R.id.button2);
        
        // get radio button
        final CheckBox net = (CheckBox)findViewById(R.id.checkBox1);
        
        // for display
        final TextView view = (TextView)findViewById(R.id.textView4);
        final EditText x_view = (EditText)findViewById(R.id.editText1);
        final EditText y_view = (EditText)findViewById(R.id.editText2);
        final EditText z_view = (EditText)findViewById(R.id.editText3);
        
        final EditText IPADD = (EditText)findViewById(R.id.editText4);
        IPADD.setText(name);
        
        final ProgressBar progress1 = (ProgressBar)findViewById(R.id.progressBar1);
        final ProgressBar progress2 = (ProgressBar)findViewById(R.id.progressBar2);
        final ProgressBar progress3 = (ProgressBar)findViewById(R.id.progressBar3);
        
        // create a manager to get the service
        final SensorManager sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        
        // define sensor event listener
        final SensorEventListener listen = new SensorEventListener()
        {     
            public void onAccuracyChanged(Sensor sensor, int accuracy) {} 
            public void onSensorChanged(SensorEvent event) {
            	// get the axis value from the sensor
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                // show the values
                x_view.setText(x.toString());
                y_view.setText(y.toString());
                z_view.setText(z.toString());
                // show the values on the progress bar
                progress1.setProgress(50+(int)(x*5));
                progress2.setProgress(50+(int)(y*5));
                progress3.setProgress(50+(int)(z*5));
                // print data in log file
                try {
                	dos.writeChars(x.toString());
                	dos.writeChars(" ");
                	dos.writeChars(y.toString());
                	dos.writeChars(" ");
                	dos.writeChars(z.toString());
                	dos.writeChars(" ");
                	Float acc = (float) Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2)+Math.pow(z, 2));
                	dos.writeChars(acc.toString());
                	dos.writeChars("\n"); 
                }
                catch (IOException e) {
                	Log.e("File operation", "Can't write data.");
                }               
            }
        };
        
        // when start button is pressed
        start.setOnClickListener(new OnClickListener()
        {
			public void onClick(View v) {
				// flow control
				if (state == 1) {
	        		return;
	        	} 
				// create sensor manager object
				sensorMan.registerListener(listen,
				sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
				view.setText(R.string.hello2);
				state = 1; 
				// create a new log in the local directory
		        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		        File dir = new File(path + "/AccData");
		        if (!dir.exists()) {
		        	dir.mkdir();
		        }
		        log = new File(path + "/AccData/log.txt");
		        try {
		        	FileOutputStream fos = new FileOutputStream(log); 
		        	dos = new DataOutputStream(fos);
		        }
		        catch (IOException e) {
		        	Log.e("File operation", "Can't get the FileOutputStream.");
		        	return;
		        }
			}
        });
        
        // when end button is pressed
        end.setOnClickListener(new OnClickListener()
        {
			public void onClick(View v) {
				// flow control 
				if (state == 2) {
	        		return;
				}
				// delete sensor manager object
				sensorMan.unregisterListener(listen);
				view.setText(R.string.hello);
				state = 2;
				// network usage control
				if (!net.isChecked()) {
					return;
				}
				// close the file
		        try {
		        	dos.close();
		        	size = log.length();
		        	Log.d("File", "File size is " + size);
		        	FileInputStream fis = new FileInputStream(log);
		        	bis = new BufferedInputStream(fis);
		        }
		        catch (IOException e) {
		        	Log.e("File operation", "Can't read data.");
		        	return;
		        }
		        // open socket
		        try {
		        	Log.d("Socket", name);
		        	socket = new Socket(name, port);
		        }
		        catch (IOException e) {
		        	Log.e("Socket", "Can't open socket.");
		        	return;
		        }
		        // header and buffer
		        byte [] header = new byte [] {	(byte)(size >>> 56), (byte)(size >>> 48), 
		        								(byte)(size >>> 40), (byte)(size >>> 32),
		        								(byte)(size >>> 24), (byte)(size >>> 16), 
		        								(byte)(size >>> 8), (byte)(size)};
		        byte [] buffer = new byte [8];
		        // data transfer
		        try {
		        	OutputStream os = socket.getOutputStream();
		        	bos = new BufferedOutputStream(os);
		        	bos.write(header, 0, 8);
		        	for (int k=0; k<size/buffer.length; k++)
		        	{
		        		bis.read(buffer, 0, buffer.length);
		        		bos.write(buffer, 0, buffer.length);
		        	}
		        	byte [] end = new byte [(int)(size)-(int)(size/buffer.length)*buffer.length];
		        	bis.read(end, 0, end.length);
		        	bos.write(end, 0, end.length);
		        	bos.flush();
		        }
		        catch (IOException e) {
		        	Log.e("File operation", "Can't transmit file.");
		        	return;
		        }
			}
			
        });
        
        net.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				if (net.isChecked()) {
					name = IPADD.getText().toString();
					net.setChecked(true);
					Log.d("UI", "check");
				}
				else {
					net.setChecked(false);
					Log.d("UI", "uncheck");
				}
			}
        });
    }
}