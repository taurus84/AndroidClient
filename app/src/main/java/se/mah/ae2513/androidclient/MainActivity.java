package se.mah.ae2513.androidclient;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The class creates a connection to a server where user chooses
 * ip-address and port number of the server.
 */
public class MainActivity extends ActionBarActivity implements Communicator  {

    private FragmentManager fm;
    private FragmentTransaction transaction;
    private Fragment_Connect fragCon;
    private Fragment_Update fragUp;
    private Fragment_Edit fragEdit;
    private Fragment_Start fragStart;
    private Controller controller;
    private boolean bool = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);
        createFragments();
        initializeController();
        setComponents();
    }

    private void initializeController() {

        controller = new Controller(this);
    }

    /**
     * Setting up buttons and editTexts
     */
    private void setComponents() {

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.connect_now) {
            controller.connectToServer();
            //connectToServer();
            return true;
        }else if (id == R.id.edit_ip_and_port ){
            fragmentIP();
        } else if (id == R.id.update_now) {
            update();
        } else if (id == R.id.mixer) {
            fragmentMixer();
        } else if (id == R.id.disconnect) {
            controller.closeConnection();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createFragments() {

        fragCon = new Fragment_Connect();
        fragUp = new Fragment_Update();
        fragEdit = new Fragment_Edit();
        fragStart = new Fragment_Start();
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.add(R.id.fr_id,fragEdit);
        transaction.commit();
        //transaction.add(R.id.fr_id,fragUp);
        //transaction.add(R.id.fr_id,fragEdit);
    }

    private void update() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fr_id, fragUp);
        transaction.addToBackStack(null);
        transaction.commit();
//        transaction.commitAllowingStateLoss();
    }

    private void fragmentIP() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fr_id, fragEdit);
        transaction.addToBackStack(null);
       transaction.commit();
//        transaction.commitAllowingStateLoss();
    }

    private void fragmentMixer() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fr_id, fragStart);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public Controller getController() {
        return controller;
    }

    //implemented method for interface Communication
    @Override
    public void doSomething() {

        setConnectedButton(bool);
        bool = !bool;

     /*   if(bool)
            findViewById(R.id.btnSave).setVisibility(View.INVISIBLE);
        else
            findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
        bool = !bool;
     */

    }

    //implemented method for interface Communication
    @Override
    public void connectNow() {
        controller.connectToServer();
    }

    //implemented method for interface Communication
    @Override
    public void sendMessage(String string) {

        controller.sendMessageToServer(string);
    }

    public void setConnectedButton(final boolean bool) {
        //to avoid exception android.view.ViewRoot$CalledFromWrongThreadException:
        //Only the original thread that created a view hierarchy can touch its views.
        //This method allows the action to be run in the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bool) {
                    findViewById(R.id.topLeftOff).setVisibility(View.GONE);
                    findViewById(R.id.topLeftOn).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.topLeftOff).setVisibility(View.VISIBLE);
                    findViewById(R.id.topLeftOn).setVisibility(View.GONE);
                }

            }
            });
    }

}



