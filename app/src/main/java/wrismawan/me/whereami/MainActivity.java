package wrismawan.me.whereami;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.ConnectionConfiguration;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICE_RESOLUTION_REQUEST = 1000;
    private Location mLocation;

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestLocationUpdates = false;

    //UI Elements
    private TextView lblAddress, lblCity, lblCountry;
    private Button btnCheckLocation;

    private LocationRequest mLocationRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()){
            buildGoogleApiClient();
        }

        lblAddress = (TextView) findViewById(R.id.txt_address);
        lblCity = (TextView) findViewById((R.id.txt_city));
        lblCountry = (TextView) findViewById((R.id.txt_country));

        btnCheckLocation = (Button) findViewById(R.id.btn_check);

        btnCheckLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    displayLocaltion();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocaltion() throws IOException {
        String addressline = "";
        String city = "";
        String country = "";

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null){
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();

            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            addresses = gcd.getFromLocation(latitude, longitude,1);

            for(Address add : addresses){
                Log.v("Location",add.toString());
            }

            if (addresses != null && addresses.size() > 0){
                addressline = addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getAddressLine(1);
                city = addresses.get(0).getAddressLine(2);
                country = addresses.get(0).getAddressLine(3);
            }

            lblAddress.setText(addressline);
            lblCity.setText(city);
            lblCountry.setText(country);

        } else {
            lblAddress.setText("Oops. We couldn't get the location");
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            displayLocaltion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }
}
