package com.example.myapplication;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.browser.AnyBrowserMatcher;
import net.openid.appauth.browser.BrowserMatcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QrReader extends AppCompatActivity {

    SurfaceView cameraPreview;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    final int RequestPermissionID=1001;
    private ExecutorService mExecutor;
    private static final String TAG = "QRreader";
    private String idPrinterQR;
    private int REQUEST_CODE=700;
    private boolean qrLetto= false;

    /**
     * controlla i permessi per accedere alla fotocamera dell'utente
     * se corretti allora avvia la camera per la lettura del QRCode
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionID:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                    //CIE instance
                    //initializeAppAuth();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);
        findViewById(R.id.loading_container_qr).setVisibility(View.GONE);


        cameraPreview = (SurfaceView) findViewById(R.id.camerapreview);
        textView = (TextView) findViewById(R.id.textview);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480).setAutoFocusEnabled(true)
                .build();
        cameraPreview.setZOrderMediaOverlay(true);
        mExecutor = Executors.newSingleThreadExecutor();



        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(QrReader.this,
                            Manifest.permission.CAMERA)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        ActivityCompat.requestPermissions(QrReader.this,
                                new String[]{Manifest.permission.CAMERA}, RequestPermissionID);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    //if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(QrReader.this,
                    //      new String[]{Manifest.permission.CAMERA},RequestPermissionID);
                    //return;
                    //}
                    try {
                        cameraSource.start(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        /**
         * legge il codice QR e chiama una activity che si occuper?? di autenticare l'utente attraverso un secondo fattore, la CIE
         */
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size()!=0) textView.post(new Runnable() {
                    @Override
                    public void run() {

                        if (qrLetto==false) {
                            qrLetto=true;

                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(0);
                            //textView.setText(qrCodes.valueAt(0).displayValue);
                            //commentati ora nell'ulitma versione con dialog di conferma stampante
                            //textView.setText("Richiesta di autenticazione in corso...");
                            //displayLoading("Richiesta di autenticazione...");
                            //textView.setText("Mando i job in stampa...");
                            idPrinterQR = qrCodes.valueAt(0).displayValue;
                            //test
                            //String accessTokenResponse = getIntent().getExtras().getString("accessToken");
                            Intent intent = new Intent(QrReader.this, Stampa.class);
                            intent.putExtra("stampance_scelta_attraverso", "QR");
                            intent.putExtra("barcode",idPrinterQR );
                            //intent.putExtra("accessTokenResponse", accessTokenResponse);
                            //System.out.println("idPrinterQR:"+idPrinterQR);
                            //System.out.println("QR letto");
                            setResult(RESULT_OK, intent);
                            finish();
                            //Intent intentCie = new Intent(QrReader.this, CieAuth.class);
                            //startActivityForResult(intentCie, REQUEST_CODE);
                        }
                    }
                });
            }
        });
    }


    @MainThread
    private void displayLoading(String message) {
        findViewById(R.id.loading_container_qr).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.loading_description_qr)).setText(message);
        findViewById(R.id.camerapreview).setVisibility(View.GONE);
        findViewById(R.id.textview).setVisibility(View.GONE);


    }

}
