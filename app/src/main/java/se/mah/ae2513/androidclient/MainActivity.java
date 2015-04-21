package se.mah.ae2513.androidclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The class creates a connection to a server where user chooses
 * ip-address and port number of the server.
 */
public class MainActivity extends Activity implements Communicator  {

    private Entity entity = Entity.getInstance();
    private FragmentManager fm;
    private FragmentTransaction transaction;
    private Fragment_Mixer fragMix;
    private Fragment_Login fragLogin;
    private TCPClient client;
    private Timer timer;
    private TextView tvLogin, tvUpdate;
    private boolean myDrink;
    private final int SHORT = 1, LONG = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_window);
        createFragments();
        initializeComponents();
        //setTextTesting(ip);
        entity.setIpNbr(getIntent().getStringExtra("ipnumber"));
        entity.setPortNbr(Integer.parseInt(getIntent().getStringExtra("port")));
        entity.setUsername(getIntent().getStringExtra("username"));
        entity.setPassword(getIntent().getStringExtra("password"));

    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToServer();
        startTimer();
        while(!client.isConnected()) {
            if(client.isConnectFailed()) {
                break;
            }
        }
       if(client.isConnected() && !client.isConnectFailed()) {
           login();
       }

    }

    private void initializeComponents() {
        tvLogin = (TextView) findViewById(R.id.tvSignIn);
        tvUpdate = (TextView) findViewById(R.id.tvUpdate);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvLogin.getText().equals("Sign in")) {
                    //testLogin();
                    fragmentLogin();
                } else {
                    confirmSignOut();
                    //closeConnection();
                    //finish();
                }
            }
        });
        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFluidsFromServer();

            }
        });
    }

    private void confirmSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign out!")
                .setMessage("Are you sure you want to sign out?")
                .setCancelable(true)

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeConnection();
                        finish();
                    }
                });

        builder.create().show();        // create and show the alert dialog
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //login();
    }

    private void setTvLogOut() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLogin.setText("Sign out");
                tvUpdate.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setTvLogIn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLogin.setText("Sign in");
                tvUpdate.setVisibility(View.GONE);
            }
        });
    }

    public void connectToServer() {
        //message STOP returned from server if socket closes
        if(client != null) {
            Toast.makeText(getApplicationContext(), "New Connection", Toast.LENGTH_SHORT).show();
            client.sendMessage("STOP");
            setTvLogIn();
        }
        client = new TCPClient(entity.getIpNbr(),entity.getPortNbr(), this);
        client.start();
    }

    private void login() {
        String message = "LOGIN " + entity.getUsername() +
                        ":" + entity.getPassword();
        sendMessage(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.mixer:
                //fragmentMixer();
                closeConnection();
                break;
            case R.id.testButton:
                setTvLogOut();
                break;
            case R.id.logInOut:
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * Creating all fragments to be used in the program.
     */
    private void createFragments() {

        fragMix = new Fragment_Mixer();
        fragLogin = new Fragment_Login();
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.add(R.id.fr_id, fragMix);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*<<<<<<<<<<fragments>>>>>>>>>>>>>>>>>>>>>>>*/

    private void fragmentLogin() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fr_id, fragLogin);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fragmentMixer() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fr_id, fragMix);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void closeConnection(){
        if(timer != null)
            timer.cancel();
        if(client != null) {
            sendMessage("STOP");
        }
    }

    /**
     * Method receives messages from server and setting the
     * serverstatus to entity. Depending on messages the button
     * on fragment mixer will either active or inactive.
     * @param message
     */
    public void msgFromServer(String message) {

        if(message.split(" ")[0].equals("ERROR")) {
            String errorType = message.split(" ")[1];
            if(errorType.equals("NOCONNECTION")) {
                entity.setServerStatus("No Arduino");
                showOrderButton(false);
            } else if(errorType.equals("BUSY")) {
                entity.setServerStatus("BUSY");
                showOrderButton(false);
            } else if(errorType.equals("NOLOGIN")) {
                entity.setServerStatus("Not logged in");
                showOrderButton(false);
            }
        } else if(message.split(" ")[0].equals("LOGIN")) {
            String login = message.split(" ")[1];
            if(login.equals("BAD")) {
                entity.setServerStatus("Not logged in");


                makeToast("Wrong username or password", LONG);
            } else if(login.equals("OK")) {
                entity.setServerStatus("Loggin in...");
                entity.setLoggedIn(true);
                updateFluidsFromServer();
                makeToast("Login successful", SHORT);
                setTvLogOut();
            }
        } else if(message.contains("AVAILABLE")) {
            if(myDrink) {
                entity.setServerStatus("Finished!");
                myDrink = !myDrink;
            } else {
                entity.setServerStatus("Order Drink");
                showOrderButton(true);
            }
        } else if(message.contains("GROGOK")) {
            //setTextOnButtonWithString("Wait..");
            myDrink = !myDrink;
            entity.setServerStatus("Wait...");

            showOrderButton(false);
        } else if(message.contains("INGREDIENTS")) {
            if(fragMix.isVisible()) {
                showOrderButton(true);
            }
            setLiquids(message);
            //makeToast("Fluids updated", SHORT);

        }
        setTextOnButton();
    }

    public void setLiquids(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] liquids = string.split(":")[1].split(",");
                if(checkLiquidsSameSame(liquids)) {
                    makeToast("No new liquids", SHORT);
                } else {
                    entity.setLiquids(liquids);
                    if (fragMix.isVisible())
                        fragMix.setTextLiquids();
                    makeToast("Fluids updated!", SHORT);
                }

            }
        });
    }
    private boolean checkLiquidsSameSame(String[] liquids) {
        if(liquids[0].equals(entity.getLiquids(0)) &&
                liquids[1].equals(entity.getLiquids(1)) &&
                liquids[2].equals(entity.getLiquids(2)) &&
                liquids[3].equals(entity.getLiquids(3))) {
            return true;
        }
        return false;
    }

    public void showOrderButton(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fragMix.isVisible())
                    fragMix.showButton(show);
            }
        });
    }

    public void updateFluidsFromServer() {

        if(client != null) {
            client.sendMessage("INGREDIENTS");
        }
    }

    //implemented method for interface Communication
    @Override
    public void doSomething() {
    }

    //implemented method for interface Communication
    @Override
    public void sendMessage(String message) {

        if(client != null) {
            client.sendMessage(message);
        }
    }

    public void sendAvareqToServer() {
        sendMessage("AVAREQ");
    }
    public void startTimer() {
        timer = new Timer();
        timer.schedule(new CheckServer(), 2000,5000);
        login();
        //timer.schedule(new ButtonSwitcher(), 0, 2000);
    }

    public void connectionDown() {
        if(timer != null)
            timer.cancel();
        finish();
    }

    private class CheckServer extends TimerTask {
        @Override
        public void run() {
            sendAvareqToServer();

        }
    }

    private void setTextOnButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fragMix.isVisible()) {
                    //String text = rand.nextInt(Integer.MAX_VALUE) + 100000 + "";
                    fragMix.setButtonText(entity.getServerStatus());
                }
            }
        });
    }
    private void makeToast(final String message, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(length == SHORT) {
                    Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,0);
                    toast.show();
                } else if(length == LONG ) {
                    Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,0);
                    toast.show();
                }

            }
        });
    }

    private void setTextOnButtonWithString(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fragMix.isVisible()) {
                    fragMix.setButtonText(string);
                }
            }
        });
    }
}