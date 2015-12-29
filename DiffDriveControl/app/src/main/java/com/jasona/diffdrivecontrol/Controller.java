package com.jasona.diffdrivecontrol;

import android.content.Context;
//import android.graphics.Paint;
//import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.StrictMode;
//import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.util.DisplayMetrics;
//import android.content.Intent;
import android.view.MotionEvent;
//import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
//import java.util.Timer;


public class Controller extends AppCompatActivity {

    String str=null;
    byte[] send_data = new byte[1024];
    byte[] receive_data = new byte[1024];
    Button bt_open_port, bt_send_port;
    TextView txt1, txt_touch_x, txt_touch_y;
    EditText txt_ip, txt_port;
    DatagramSocket client_socket = null;
    int mPort = 2362;
    private SparseArray<PointF> mActivePointers;
    boolean pressedUp = false;
    //private SurfaceHolder holder;
    //Timer t = new Timer();
    //Paint paint = new Paint();
    //ImageView drawingImageView;

    /* calibration for use with other devices */
    double touch_center_yP, touch_center_xP;
    double touch_delta_yP, touch_delta_xP;
    double bt_left_center_xP, bt_right_center_xP, bt_radiusP;
    double bt_top_center_yP, bt_mid_center_yP, bt_bot_center_yP;
    double slide_up_center_yP, slide_right_center_xP;
    double slide_down_center_yP, slide_left_center_xP;
    double slide_delta_yP, slide_delta_xP;
    double conversion_y; double conversion_x;
    double device_width; double device_height;

    /* calibration for Galaxy S5 */
    //double touch_center_y = 379, touch_center_x =639;
    double touch_center_y = 670, touch_center_x = 967;
    double touch_delta_y = 180, touch_delta_x = 370;
    double bt_left_center_x = 84, bt_right_center_x = 1195, bt_radius = 64;
    double bt_top_center_y = 227, bt_mid_center_y = 379, bt_bot_center_y = 531;
    double slide_up_center_y = 675, slide_right_center_x = 1507;
    double slide_down_center_y = 665, slide_left_center_x = 400;
    double slide_delta_y = 180, slide_delta_x = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar() .setIcon(R.drawable.ic_launcher_arc);

        /* Gets the Screen width and height of current device and remaps pixels of touch
         * of Galaxy S5 to current device  */

        DisplayMetrics displayMetrics;
        displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels; conversion_y = device_width/1920;
        device_height = displayMetrics.heightPixels; conversion_x = device_height/1080;

        touch_center_yP = touch_center_y*conversion_y; touch_delta_yP = touch_delta_y*conversion_y;
        touch_center_xP = touch_center_x*conversion_x; touch_delta_xP = touch_delta_x*conversion_x;
        bt_left_center_x = bt_right_center_x*conversion_x; bt_right_center_xP = bt_right_center_x*conversion_x;
        bt_top_center_yP = bt_top_center_y*conversion_y; bt_mid_center_yP = bt_mid_center_y*conversion_y;
        bt_bot_center_yP = bt_bot_center_y*conversion_y; bt_radiusP = bt_radius*conversion_x;
        slide_up_center_yP = slide_up_center_y*conversion_y; slide_right_center_xP = slide_right_center_x*conversion_x;
        slide_down_center_yP = slide_down_center_y*conversion_y; slide_left_center_xP = slide_left_center_x*conversion_x;
        slide_delta_yP = slide_delta_y*conversion_y; slide_delta_xP = slide_delta_x*conversion_x;

        mActivePointers = new SparseArray<>();

        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy;
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
        }

        setContentView(R.layout.activity_controller);
        txt1 = (TextView)findViewById(R.id.message_center);
        bt_open_port = (Button) findViewById(R.id.button_open_port);
        bt_open_port.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                str = "temp";
                try {
                    client_open();
                } catch (IOException e){
                    // TODO Auto-generated catch block
                    txt1.setText(e.toString());
                    e.printStackTrace();
                }
            }
        });

        bt_send_port = (Button) findViewById(R.id.button_send_port);
        bt_send_port.setOnClickListener(new View.OnClickListener(){
           public void onClick(View v) {
               str = "temp";
               try {
                   client_send();
               } catch (IOException e) {
                   // TODO Auto-generated catch block
                   txt1.setText(e.toString());
                   e.printStackTrace();
               }
           }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // vibrator.vibrate(500); // 0.5 seconds

        int x = (int)event.getX();
        int y = (int)event.getY();
        txt_touch_x = (TextView)findViewById(R.id.touch_x_view);
        txt_touch_y = (TextView)findViewById(R.id.touch_y_view);
        txt_touch_x.setText(Integer.toString(x));
        txt_touch_y.setText(Integer.toString(y));
        vibrator.vibrate(20);
        //txt1.setText(Integer.toString(event.getPointerCount());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_UP:

                pressedUp = false;
        }

        txt1.setText("jasa");
        for (int size = event.getPointerCount(), i=0; i<size; i++) {
            txt1.setText("jasb");
            //PointF point = mActivePointers.get(event.getPointerId(i));
            txt1.setText("jasc");
            txt1.setText("jasd");
            float xx = event.getX(i);
            float yy = event.getY(i);

            boolean openFlag = false;
            if (!(client_socket == null)) {
                openFlag = (client_socket.getLocalPort() == Integer.parseInt(txt_port.getText().toString())) & !client_socket.isClosed();
            }
            if (openFlag) {
                String buff = parseTouch(xx, yy);
                if(buff != null) {
                    try {
                        client_send_buff(buff);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        txt1.setText(e.toString());
                        e.printStackTrace();
                    }
                }
            }

        }

        return false;
    }

    public void client_open() throws  IOException {
        txt_port = (EditText)findViewById(R.id.port_num);
        mPort = Integer.parseInt(txt_port.getText().toString());
        client_socket = new DatagramSocket(mPort);
        bt_open_port.setText("Port Open");
    }

    public void client_send() throws IOException {
        txt_ip = (EditText)findViewById(R.id.address_input);
        InetAddress IPAddress = InetAddress.getByName(txt_ip.getText().toString());
        str = "jas1";
        send_data = str.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data,str.length(), IPAddress, mPort);
        client_socket.send(send_packet);
    }

    public void client_send_buff(String buff) throws IOException {
        txt_ip = (EditText)findViewById(R.id.address_input);
        InetAddress IPAddress = InetAddress.getByName(txt_ip.getText().toString());
        str = buff;
        send_data = str.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data, str.length(), IPAddress, mPort);
        client_socket.send(send_packet);
        try {Thread.sleep(10);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void send_buff(String buff) {
        try {
            client_send_buff(buff);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            txt1.setText(e.toString());
            e.printStackTrace();
        }
    }

    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public String parseTouch(float x, float y) {
        //byte[] button = {0};
        //double[] xy = {0.0, 0.0};
        String theOut = null;

        /* main touch pad */
        if (
                x > (touch_center_xP - touch_delta_xP) &
                x < (touch_center_xP + touch_delta_xP) &
                y > (touch_center_yP - touch_delta_yP) &
                y < (touch_center_yP + touch_delta_yP))
        {
            //button = ("t").getBytes();
            double xx = (x - touch_center_xP)/touch_delta_xP;
            double yy = -(y - touch_center_yP)/touch_delta_yP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("touch").getBytes() );
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button L1 "*" */
        else if (
                    (Math.sqrt((x - bt_left_center_xP)*(x - bt_left_center_xP)
                    + (y - bt_top_center_yP)*(y - bt_top_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_left_center_xP)/bt_radiusP;
            double yy = -(y - bt_top_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(("* button").getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button L2 "&" */
        else if (
                (Math.sqrt((x - bt_left_center_xP)*(x - bt_left_center_xP)
                + (y - bt_mid_center_yP)*(y - bt_mid_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_left_center_xP)/bt_radiusP;
            double yy = -(y - bt_mid_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("& button").getBytes() );
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button L3 "$" */
        else if (
                (Math.sqrt((x - bt_left_center_xP)*(x - bt_left_center_xP)
                + (y - bt_bot_center_yP)*(y - bt_bot_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_left_center_xP)/bt_radiusP;
            double yy = -(y - bt_bot_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("$ button").getBytes() );
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button R1 "+" */
        else if (
                (Math.sqrt((x - bt_right_center_xP)*(x - bt_right_center_xP)
                + (y - bt_top_center_yP)*(y - bt_top_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_right_center_xP)/bt_radiusP;
            double yy = -(y - bt_top_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(("+ button").getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button R2 "@" */
        else if (
                (Math.sqrt((x - bt_right_center_xP)*(x - bt_right_center_xP)
                + (y - bt_mid_center_yP)*(y - bt_mid_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_right_center_xP)/bt_radiusP;
            double yy = -(y - bt_mid_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("@ button").getBytes() );
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* button R3 "#" */
        else if (
                (Math.sqrt((x - bt_right_center_xP)*(x - bt_right_center_xP)
                + (y - bt_bot_center_yP)*(y - bt_bot_center_yP)) < bt_radiusP)
                )
        {
            //button = ("t").getBytes();
            double xx = (x - bt_right_center_xP)/bt_radiusP;
            double yy = -(y - bt_bot_center_yP)/bt_radiusP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("# button").getBytes() );
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* Left Slider Up */ /* this section is incomplete */
        if (
                x > (slide_left_center_xP - slide_delta_xP) &
                x < (slide_left_center_xP + slide_delta_xP) &
                y > (slide_up_center_yP - slide_delta_yP) &
                y < (slide_up_center_yP + slide_delta_yP))
        {
            //button = ("t").getBytes();
            //double xx = (x - slide_left_center_xP)/slide_delta_xP;
            double yy = -(y - slide_up_center_yP)/slide_delta_yP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("Left Up").getBytes() );
                //outputStream.write( (" ").getBytes() );
                //outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* Left Slider Down */ /* this section is incomplete */
        if (
                x > (slide_left_center_xP - slide_delta_xP) &
                x < (slide_left_center_xP + slide_delta_xP) &
                y > (slide_down_center_yP - slide_delta_yP) &
                y < (slide_down_center_yP + slide_delta_yP))
        {
            //button = ("t").getBytes();
            //double xx = (x - slide_left_center_xP)/slide_delta_xP;
            double yy = -(y - slide_down_center_yP)/slide_delta_yP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("Left Up").getBytes() );
                outputStream.write( (" ").getBytes() );
                //outputStream.write( (String.format("%.3f", xx)).getBytes());
                //outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* Right Slider Up */ /* this section is incomplete */
        if (
                x > (slide_right_center_xP - slide_delta_xP) &
                x < (slide_right_center_xP + slide_delta_xP) &
                y > (slide_up_center_yP - slide_delta_yP) &
                y < (slide_up_center_yP + slide_delta_yP))
        {
            //button = ("t").getBytes();
            //double xx = (x - slide_right_center_xP)/slide_delta_xP;
            double yy = -(y - slide_up_center_yP)/slide_delta_yP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("Right Up").getBytes() );
                //outputStream.write( (" ").getBytes() );
                //outputStream.write( (String.format("%.3f", xx)).getBytes());
                outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        /* Right Slider Down */ /* this section is incomplete */
        if (
                x > (slide_right_center_xP - slide_delta_xP) &
                x < (slide_right_center_xP + slide_delta_xP) &
                y > (slide_down_center_yP - slide_delta_yP) &
                y < (slide_down_center_yP + slide_delta_yP))
        {
            //button = ("t").getBytes();
            //double xx = (x - slide_right_center_xP)/slide_delta_xP;
            double yy = -(y - slide_down_center_yP)/slide_delta_yP;

            try {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write( ("Right Up").getBytes() );
                outputStream.write( (" ").getBytes() );
                //outputStream.write( (String.format("%.3f", xx)).getBytes());
                //outputStream.write( (" ").getBytes() );
                outputStream.write( (String.format("%.3f", yy)).getBytes());
                //outputStream.write( toByteArray(xx) );
                //outputStream.write(toByteArray(yy));
                txt1.setText(outputStream.toString());
                //outputStream.toByteArray();
                theOut = outputStream.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                txt1.setText(e.toString());
                e.printStackTrace();
            }
        }

        return theOut;
    }
}
