package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int sub = 5554;
    ArrayList<String> ports = new ArrayList<String>();
    ArrayList<Messages> allMsg = new ArrayList<Messages>();
    GroupMessengerList gl = new GroupMessengerList();
    int lastProposal = 0;
    int lastSeq = 0;
    String myPort="";

    private Uri mUri = null;
    int count = 0;


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ports.add(REMOTE_PORT0);
        ports.add(REMOTE_PORT1);
        ports.add(REMOTE_PORT2);
        ports.add(REMOTE_PORT3);
        ports.add(REMOTE_PORT4);

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_group_messenger);

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        TelephonyManager tel0 = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr0 = tel0.getLine1Number().substring(tel0.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr0) * 2));


        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);


        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }


        Button send = (Button) findViewById(R.id.button4);


        send.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                editText.setText("");

                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg);

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }

        });


    }




    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {


            ServerSocket serverSocket = sockets[0];
            HashMap<String,ArrayList<Double>> max = new HashMap<String, ArrayList<Double>>();
            int add = 0;

            while (true) {

                try {

                    String text = "";
                    String message;


                    //serverSocket.setSoTimeout(2000);
                    Socket socket1 = serverSocket.accept();
                    add = socket1.getPort();
                    //socket1.setSoTimeout(500);

                    BufferedReader read = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                    while ((message = read.readLine()) != null) {
                        text += message;
                    }

                    read.close();
                    socket1.close();

                    String [] str = text.split(":");




                    if(str[2].equals("FIRST")){

                        int  portStop = 0;

                       try {
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(str[1])*2);
                                //socket.setSoTimeout(1000);
                                //Socket socket = new Socket();
                                //portStop = Integer.parseInt(str[1])*2;
                                //socket.connect(new InetSocketAddress("10.0.2.2",portStop),2000);
                                TelephonyManager tel2 = (TelephonyManager) GroupMessengerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                                String portStr2= tel2.getLine1Number().substring(tel2.getLine1Number().length() - 4);

                                int com = Math.max(lastProposal,lastSeq)+1;
                                //Log.v("port",portStr2 + " " + (Integer.parseInt(portStr2) - sub) * 0.1);
                                double val = com +  (Integer.parseInt(portStr2) - sub) * 0.1;
                                //Log.e("double",val+"");
                                String s =  str[0] + ":" + Integer.parseInt(portStr2) + ":" + "PROPOSED" + ":" + String.valueOf(val);

                                Messages m = new Messages(str[0],val,str[1],false);
                                lastProposal = Math.max(Integer.valueOf((int) Math.floor(val)),lastSeq) + 1;
                                //Log.e("lastProp",lastProposal+"");
                                allMsg.add(m);

                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                out.write(s);
                                out.close();


                                Log.v("Proposed", s);

                                //socket.close();

                       } catch (UnknownHostException e) {
                           Log.e(TAG, "ClientTask UnknownHostException");
                       } catch(SocketTimeoutException s1){
                           Log.e("EXCEPTION","Caught3");
                           Log.e("PORT",portStop + "");

                       } catch(StreamCorruptedException s2){
                           Log.e("EXCEPTION","Caught31");
                       } catch (IOException e) {
                           Log.e(TAG, "ClientTask socket IOException");
                       }

                    }


                    else if (str[2].equals("PROPOSED")) {

                        if(max.containsKey(str[0])){

                            ArrayList<Double> l = max.get(str[0]);
                            l.add(Double.valueOf(str[3]));

                        }
                        else{
                            ArrayList<Double> l = new ArrayList<Double>();
                            l.add(Double.valueOf(str[3]));
                            max.put(str[0],l);
                        }


                        for (Map.Entry<String, ArrayList<Double>> entry : max.entrySet()) {
                            String key = entry.getKey();
                            ArrayList<Double> value = entry.getValue();

                            if(value.size() == 5){

                                 Log.e("size",value.size()+" " + key);
                                 double agree = Collections.max(value);
                                 Log.e("lastAgree",agree +"");
                                 lastSeq = Math.max(lastSeq,Integer.valueOf((int) Math.floor(agree)));


                                try {
                                    for (int i = 0; i < ports.size(); i++) {
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(ports.get(i)));
                                        //socket.setSoTimeout(1000);
                                        //Socket socket = new Socket();
                                        //socket.connect(new InetSocketAddress("10.0.2.2",Integer.parseInt(ports.get(i))),2000);

                                        TelephonyManager tel3 = (TelephonyManager) GroupMessengerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                                        String portStr3 = tel3.getLine1Number().substring(tel3.getLine1Number().length() - 4);
                                        String e = key +  ":" + portStr3 + ":" + "AGREED" + ":" + agree;

                                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                        out.write(e);
                                        out.close();

                                        Log.v("Agreed", e);


                                    }
                                    max.remove(key);

                                } catch (UnknownHostException e) {
                                    Log.e(TAG, "ClientTask UnknownHostException");
                                } catch(SocketTimeoutException s1){
                                    Log.e("EXCEPTION","Caught2");
                                } catch(StreamCorruptedException s2){
                                    Log.e("EXCEPTION","Caught21");
                                } catch (IOException e) {
                                    Log.e(TAG, "ClientTask socket IOException");
                                }
                            }
                        }

                    }

                    else if(str[2].equals("AGREED")) {

                            Messages m1 = null;
                            for(int i=0; i< allMsg.size();i++){

                                Messages m = allMsg.get(i);
                                if(m.msg.equals(str[0])){
                                    m1 = allMsg.get(i);
                                }

                            }
                            allMsg.remove(m1);
                            m1.seq = Double.parseDouble(str[3]);
                            m1.deliver = true;
                            allMsg.add(m1);
                            lastSeq = Math.max(lastSeq,Integer.valueOf((int) Math.floor(Double.parseDouble(str[3]))));


                            Collections.sort(allMsg, new Messages());

                            while(!allMsg.isEmpty()){

                                if(allMsg.get(0).deliver){
                                    publishProgress(allMsg.get(0).msg);
                                    max.remove(allMsg.get(0).msg);
                                    Log.v("Added", allMsg.get(0).msg + " " + allMsg.get(0).seq);
                                    allMsg.remove(0);
                                }

                                else{
                                    break;
                                }

                            }

                        }


                } catch(SocketTimeoutException s1){
                    Log.e("EXCEPTION","Caught4");
                    Log.e("PORT",add + "");
                } catch(StreamCorruptedException s2){
                    Log.e("EXCEPTION","Caught41");
                } catch (IOException e) {
                    e.printStackTrace();
                }
           }

        }


        protected void onProgressUpdate(String... strings) {


            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();


            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\t" + "received: " + strReceived + "\n");


            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */


            String string = strReceived;
            ContentValues keyValueToInsert = new ContentValues();

            keyValueToInsert.put("key", count);
            keyValueToInsert.put("value", string);

            count++;


            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            Uri newUri = getContentResolver().insert(mUri, keyValueToInsert);


            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            /*Have use documentation from Java oracle to create new socket and open OutputStream
              link to documentation on Reading and Writing from Sockets
              https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
            */




            try {
                for (int i = 0; i < ports.size(); i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(ports.get(i)));
                    //Socket socket = new Socket();
                    //socket.connect(new InetSocketAddress("10.0.2.2",Integer.parseInt(ports.get(i))),2000);
                    //socket.setSoTimeout(1000);

                    TelephonyManager tel1 = (TelephonyManager) GroupMessengerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                    String portStr1 = tel1.getLine1Number().substring(tel1.getLine1Number().length() - 4);
                    String msgToSend = msgs[0] + ":" + portStr1 + ":" + "FIRST";



                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.write(msgToSend);
                    out.close();
                    Log.v("1 Client", msgToSend );

                    //socket.close();

                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch(SocketTimeoutException s1){
                Log.e("EXCEPTION","Caught1");
            } catch(StreamCorruptedException s2){
                Log.e("EXCEPTION","Caught11");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }


            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



}