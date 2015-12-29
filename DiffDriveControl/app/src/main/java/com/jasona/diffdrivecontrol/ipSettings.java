package com.jasona.diffdrivecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;


public class ipSettings extends Activity {

    Button settings_done;
    Button bt_open_port, bt_send_port;

    EditText address_input, txt_ip;
    EditText comm_port_input, txt_cport;
    //EditText video_port_input;    //when video gets implemented

    DatagramSocket client_socket = null;

    int comm_ip_port = 2362;
    //int video_ip_port = 80;   //when video gets implemented

    String ip_txt = "192.168.1.10";
    String str = null;

    byte[] send_data = new byte[1024];
    //byte[] receive_data = new byte[1024]; //when receiving data is implemented


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_settings);

        Bundle extras = getIntent().getExtras();

        address_input = (EditText) findViewById(R.id.address_input);
        comm_port_input = (EditText) findViewById(R.id.comm_port_input);

        if (extras !=null) {
            ip_txt = extras.getString(ip_txt);
            comm_ip_port = extras.getInt("comm_ip_port", comm_ip_port);

            address_input.setText(String.valueOf(ip_txt));
            comm_port_input.setText(String.valueOf(comm_ip_port));
        }

        bt_open_port = (Button) findViewById(R.id.button_open_port);
        bt_open_port.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                str = "temp";
                try{
                    client_open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bt_send_port = (Button) findViewById(R.id.button_send_port);
        bt_send_port.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                str = "temp";
                try{
                    client_send();
                } catch (IOException e) {
                    //txt1.setText(e.toString());
                    e.printStackTrace();
                }
            }
        });

        settings_done = (Button) findViewById(R.id.settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        String s;

                        s = address_input.getText().toString();
                        ip_txt = s;

                        s = comm_port_input.getText().toString();
                        if (!"".equals(s)) {
                            comm_ip_port = Integer.parseInt(s);
                        }
                    }
                }
        );

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

    public void client_open() throws IOException {
        txt_cport = (EditText)findViewById(R.id.comm_port_input);
        comm_ip_port = Integer.parseInt(txt_cport.getText().toString());
        client_socket = new DatagramSocket(comm_ip_port);
        bt_open_port.setText("Port Open");
    }

    public void client_send() throws IOException {
        txt_ip = (EditText)findViewById(R.id.address_input);
        InetAddress IPAddress = InetAddress.getByName(txt_ip.getText().toString());
        str="dan1";
        send_data = str.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data,str.length(), IPAddress, comm_ip_port);
        client_socket.send(send_packet);
    }

    public void client_send_buff (String buff) throws IOException {
        txt_ip = (EditText)findViewById(R.id.address_input);
        InetAddress IPAddress = InetAddress.getByName(txt_ip.getText().toString());
        str = buff;
        send_data = str.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data,str.length(), IPAddress, comm_ip_port);
        client_socket.send(send_packet);
        try {Thread.sleep(10);
        } catch (InterruptedException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
