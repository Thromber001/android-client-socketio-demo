package com.chaisync.android_client_socketio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket sock;
    private TextView textViewFromServer;
    private Button sendButton;
    private EditText editTextFromClient;

    public MainActivity(){
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewFromServer = (TextView) findViewById(R.id.ServerResponseTV);
        sendButton = (Button) findViewById(R.id.buttonSendToServer);
        editTextFromClient = (EditText) findViewById(R.id.nameInputText);

        // Listen for button click to fire message from client to server
        sendButton.setOnClickListener(new View.OnClickListener(){
          public void onClick(View v){
              try {
                  attemptSend();
              } catch (JSONException e){
                  throw new RuntimeException();
              }

              // Loopback demo
              //textViewFromServer.setText(editTextFromClient.getText());
          }
        });

        // Start socket.io
        ChaisyncApplication app = (ChaisyncApplication) getApplication();
        sock = app.getSocket();

        // Configure socket.io events
        sock.on(Socket.EVENT_CONNECT, onConnect);
        sock.on(Socket.EVENT_DISCONNECT, onDisconnect);
        sock.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        sock.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeout);
        sock.on("new message", onNewMessage);

        // Connect to server
        sock.connect();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data;
                    try{
                        data = new JSONObject ((String) args[0]);
                        Log.d("CSdebug", data.toString());
                    } catch (JSONException e){
                        Log.d("CSdebug", "Data from server error caused exception: " + args[0]);
                        return;
                    }

                    String firstname;
                    String lastname;
                    try {
                        firstname = data.getString("firstname");
                        lastname = data.getString("lastname");
                    } catch (JSONException e) {
                        return;
                    }
                    textViewFromServer.setText(firstname+ " " + lastname);
                }
            });
        }
    };

    private void attemptSend() throws JSONException{
        // Put the client message into a JSON object
        String message = editTextFromClient.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            editTextFromClient.requestFocus();
            return;
        }
        editTextFromClient.setText("");

        JSONObject data = new JSONObject();
        data.put("firstname", message);
        data.put("lastname", "client");
        Log.d("CSdebug","attempt send: " + data.toString());

        // Attempt to send the message
        sock.emit("send message", data);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Event");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Disconnect Event");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Error Event");
        }
    };

    private Emitter.Listener onConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Timeout Event");
        }
    };
}
