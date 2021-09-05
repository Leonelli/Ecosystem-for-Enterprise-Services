package com.example.myapplication;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import com.google.gson.Gson;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;

import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class TokenActivity extends AppCompatActivity {
    private static final String TAG = "TokenActivity";
    private static final String KEY_USER_INFO = "userInfo";

    private AuthorizationService mAuthService;
    private AuthStateManager mStateManager;
    private final AtomicReference<JSONObject> mUserInfoJson = new AtomicReference<>();
    private Configuration mConfiguration;
    private ExecutorService mExecutor;
    public JSONObject my_json = new JSONObject();
    String accessTokenResponse;
    String response;
    boolean nessun_job = false;
    boolean fetching = false;
    private ListView lv;
    private ArrayList<Model> modelArrayList;
    private CustomAdapter customAdapter;
    private Button btnselect, btndeselect, btnnext, btndelete;
    private String[] joblist_array = new String[]{"job1", "job2", "job3", "job4"};
    private ArrayList<String> job_da_stampare = new ArrayList<String>();
    String id = "100";
    private boolean messaggioLetto = false;
    private String response_stampa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStateManager = AuthStateManager.getInstance(this);
        mConfiguration = Configuration.getInstance(this);
        mExecutor = Executors.newSingleThreadExecutor();
        mAuthService = new AuthorizationService(
                this,
                new AppAuthConfiguration.Builder()
                        .setConnectionBuilder(mConfiguration.getConnectionBuilder())
                        .build());
        setContentView(R.layout.activity_token);
        displayLoading("Restoring state...");
        if (savedInstanceState != null) {
            try {
                mUserInfoJson.set(new JSONObject(savedInstanceState.getString(KEY_USER_INFO)));
            } catch (JSONException ex) {
                Log.e(TAG, "Failed to parse saved user info JSON, discarding", ex);
            }
        }
        /*if(getIntent().getExtras().getBoolean("uscita_confermata")==true){
            //System.out.println("signOut(): "+getIntent().getExtras().getBoolean("uscita_confermata"));
            signOut();
        }*/
        if (messaggioLetto == false) {
            response_stampa = getIntent().getExtras().getString("response");
        }
        if (response_stampa != "" && response_stampa != null) {
            if (messaggioLetto == false) {
                AlertDiStampa(response_stampa);
                messaggioLetto = true;
            }
        }
        //controllo se è uscito una volta, da non riepresentare in caso di rotazione schermo
        ((TextView) findViewById(R.id.respone_stampa)).setText(response_stampa);
    }

    public void onResponse(Response response) {
        modelArrayList = getModel(false);
        customAdapter = new CustomAdapter(this, modelArrayList);
        //add this setter to your data object if not exist.
        customAdapter.notifyDataSetChanged();
        lv.setAdapter(customAdapter);
        //System.out.println("onResponseMethod");
    }

    /**
     * crea un dialogo quando si torna indietro per che chiedere la conferma per uscire dall'app
     */
    @Override
    public void onBackPressed() {
        createDialog();
    }

    /**
     * metodo per creare una finestra di dialogo che richiede la conferma per l'uscita dall'app
     */
    public void createDialog() {
        AlertDialog.Builder exit = new AlertDialog.Builder(this);
        exit.setMessage("Sicuro di voler uscire?");
        exit.setCancelable(false);

        exit.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TokenActivity.super.onBackPressed();
                signOut();
            }
        });
        exit.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        exit.create().show();
    }


    /**
     * metodo che permette di generare un modello checkbox - testo
     *
     * @param isSelect seleziona tutte le checkbox se true, nessuna se false
     * @return un array con gli elementi richiesti "parsati" per titolo e id con controllo sullo spoole (attenzione: l'ID potrebbe essere cambiato da Google)
     */
    private ArrayList<Model> getModel(boolean isSelect) {
        ArrayList<Model> list = new ArrayList<>();
        String jsonResult = response;
        int jobCounter = 0;
        try {
            JSONObject json = new JSONObject(jsonResult);
            //jobCounter = Integer.parseInt(json.getJSONObject("range").getString("jobsTotal"));
            jobCounter = Integer.parseInt(json.getJSONObject("range").getString("jobsCount"));
        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
            // Do something to recover ... or kill the app.
        }
        for (int i = 0; i < jobCounter; i++) {
            //System.out.println("jsonResult:" +jsonResult);
            System.out.println("jobCounter: "+jobCounter);
            try {
                JSONObject json = new JSONObject(jsonResult);
                //Log.e("json",jsonResult);
                //System.out.println("jsonResult: "+jsonResult);
                String title = json.getJSONArray("jobs").getJSONObject(i).getString("title");
                String JobID = (json.getJSONArray("jobs").getJSONObject(i).getString("id"));
                //System.out.println("title "+title);
                //System.out.println("JobID "+JobID);
                //Log.e("title",title);
                String stateOfJob = json.getJSONArray("jobs").getJSONObject(i).getJSONObject("uiState").getString("summary");
                String jobDestination = json.getJSONArray("jobs").getJSONObject(i).getString("printerid");
                //System.out.println("stateOfJob "+stateOfJob);
                //System.out.println("jobDestination "+jobDestination);
                if (stateOfJob.contentEquals("IN_PROGRESS") && jobDestination.contentEquals(Commons.spoolerID)) {
                    //System.out.println("Status recepito:" + json.getJSONArray("jobs").getJSONObject(i).getString("printerid"));
                    Model model = new Model();
                    model.setSelected(isSelect);
                    model.setAnimal(title + "\n" + "_" + JobID);
                    //test per select all
                    //customAdapter.job_selezionati.add(title+"\n"+"_"+JobID); //why not???
                    list.add(model);
                    if (isSelect == true) {
                        //customAdapter.job_selezionati.add(model);
                        //inserisci tutti gli elementi nella lista c
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON OBJECT", "unexpected JSON exception", e);
                // Do something to recover ... or kill the app.
            }
        }
        if (list == null || jobCounter == 0) {
            nessun_job = true;
            Model model = new Model();
            model.setSelected(isSelect);
            model.setAnimal("NON CI SONO JOB");
            list.add(model);
        } else {
            nessun_job = false;
        }
        return list;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        if (mStateManager.getCurrent().isAuthorized()) {
            displayAuthorized();
            return;
        }
        AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
        if (response != null || ex != null) {
            mStateManager.updateAfterAuthorization(response, ex);
        }
        if (response != null && response.authorizationCode != null) {
            // authorization code exchange is required
            mStateManager.updateAfterAuthorization(response, ex);
            exchangeAuthorizationCode(response);
        } else if (ex != null) {
            displayNotAuthorized("Authorization flow failed: " + ex.getMessage());
        } else {
            displayNotAuthorized("No authorization state retained - reauthorization required");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mUserInfoJson.get() != null) {
            state.putString(KEY_USER_INFO, mUserInfoJson.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthService.dispose();
    }

    /**
     * display/view che verrà stampata se il token/l'utente risulta non autorizzato
     *
     * @param explanation
     */
    @MainThread
    private void displayNotAuthorized(String explanation) {
        findViewById(R.id.not_authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.explanation)).setText(explanation);
        findViewById(R.id.reauth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    /**
     * barra di caricamento
     *
     * @param message che si vuole inserire nella load bar
     */
    @MainThread
    private void displayLoading(String message) {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.loading_description)).setText(message);
    }

    /**
     * view mostrata se l'utente è autorizzato e possiede un token valido
     */
    @MainThread
    private void displayAuthorized() {
        findViewById(R.id.authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);

        //tolgo tutti gli elementi non più utili terminati i controlli funzionali
        findViewById(R.id.refresh_token).setVisibility(View.GONE);
        findViewById(R.id.view_profile).setVisibility(View.GONE);
        findViewById(R.id.auth_granted).setVisibility(View.GONE);
        findViewById(R.id.select).setVisibility(View.GONE);

        findViewById(R.id.refresh_token_info).setVisibility(View.GONE);
        findViewById(R.id.access_token_info).setVisibility(View.GONE);
        findViewById(R.id.id_token_info).setVisibility(View.GONE);
        findViewById(R.id.userinfo_card).setVisibility(View.GONE);
        findViewById(R.id.next1).setVisibility(View.GONE);

        findViewById(R.id.container_button).setVisibility(View.GONE);
        AuthState state = mStateManager.getCurrent();
        //refresho il token se scaduto o non valido
        if (state.getRefreshToken() == null || state.getAccessToken() == null || state.getAccessTokenExpirationTime() < System.currentTimeMillis()) {
            refreshAccessToken();
        }
        TextView refreshTokenInfoView = (TextView) findViewById(R.id.refresh_token_info);
        /*refreshTokenInfoView.setText((state.getRefreshToken() == null)
                ? "No refresh token returned"
                : "Refresh token returned");*/
        TextView idTokenInfoView = (TextView) findViewById(R.id.id_token_info);
        /*idTokenInfoView.setText((state.getIdToken()) == null
                ? "No ID Token returned"
                : "ID Token returned");*/
        //Log.d("id_token: ", state.getIdToken());
        TextView accessTokenInfoView = (TextView) findViewById(R.id.access_token_info);
        if (state.getAccessToken() == null) {
            accessTokenInfoView.setText("No access token returned");
        } else {
            Long expiresAt = state.getAccessTokenExpirationTime();
            accessTokenResponse = state.getAccessToken();
            if (expiresAt == null) {
                accessTokenInfoView.setText("Access time has no defined expiry");
            } else if (expiresAt < System.currentTimeMillis()) {
                accessTokenInfoView.setText("Access token has expired ");
            } else {
                String template = "Access token expires at: %s";
                accessTokenInfoView.setText(String.format(template,
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt)));
                System.out.println("accessTokenResponse: " + accessTokenResponse + " - expiresAt: "+String.format(template,
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt)));
            }
        }

        Button refreshTokenButton = (Button) findViewById(R.id.refresh_token);
        refreshTokenButton.setVisibility(state.getRefreshToken() != null
                ? View.VISIBLE
                : View.GONE);
        refreshTokenButton.setOnClickListener((View view) -> refreshAccessToken());

        Button viewProfileButton = (Button) findViewById(R.id.view_profile);

        AuthorizationServiceDiscovery discoveryDoc =
                state.getAuthorizationServiceConfiguration().discoveryDoc;
        if ((discoveryDoc == null || discoveryDoc.getUserinfoEndpoint() == null)
                && mConfiguration.getUserInfoEndpointUri() == null) {
            viewProfileButton.setVisibility(View.GONE);
        } else {
            viewProfileButton.setVisibility(View.VISIBLE);
            viewProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchUserInfo();
                }
            });
            //provare se una volta scaduto il token serve riassegnarlo alla variabile
            //accessTokenResponse = state.getAccessToken();
            //System.out.println("token refreshed: " + accessTokenResponse);
        }
        Button Job = (Button) findViewById(R.id.job_list);
        Button Printer = (Button) findViewById(R.id.printer_list);
        Button Print = (Button) findViewById(R.id.submit);
        Button Next = (Button) findViewById(R.id.next);
        Button Delete = (Button) findViewById(R.id.delete);

        Job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresho il token se scaduto o non valido
                if (state.getRefreshToken() == null || state.getAccessToken() == null || state.getAccessTokenExpirationTime() < System.currentTimeMillis()) {
                    refreshAccessToken();
                }
                jobList();
            }
        });

        /*Printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printerList();
            }
        });
        Print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                print();
            }
        });*/

        ((Button) findViewById(R.id.sign_out)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        View userInfoCard = findViewById(R.id.userinfo_card);
        JSONObject userInfo = mUserInfoJson.get();
        if (userInfo == null) {
            userInfoCard.setVisibility(View.INVISIBLE);
        } else {
            try {
                String name = "???";
                if (userInfo.has("name")) {
                    name = userInfo.getString("name");
                }
                ((TextView) findViewById(R.id.userinfo_name)).setText(name);

                /*if (userInfo.has("picture")) {
                    GlideApp.with(TokenActivity.this)
                            .load(Uri.parse(userInfo.getString("picture")))
                            .fitCenter()
                            .into((ImageView) findViewById(R.id.userinfo_profile));
                }
*/
                ((TextView) findViewById(R.id.userinfo_json)).setText(mUserInfoJson.toString());
                //userInfoCard.setVisibility(View.VISIBLE);
            } catch (JSONException ex) {
                Log.e(TAG, "Failed to read userinfo JSON", ex);
            }
        }

        if (fetching == false) {
            Job.performClick();
            fetching = true;
        }

        lv = (ListView) findViewById(R.id.lv);
        btnselect = (Button) findViewById(R.id.select);
        btndeselect = (Button) findViewById(R.id.deselect);
        btnnext = (Button) findViewById(R.id.next);
        btndelete = (Button) findViewById(R.id.delete);

        if (nessun_job == true) {
            ((ScrollView) findViewById(R.id.ScrollView)).setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            btnnext.setEnabled(false);
            btnnext.setVisibility(View.GONE);
            btndelete.setEnabled(false);
            btndelete.setVisibility(View.GONE);
            btndeselect.setVisibility(View.GONE);
            btnselect.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.respone_stampa)).setText("Non ci sono job nella coda di stampa");
            ((TextView) findViewById(R.id.respone_stampa)).setVisibility(View.VISIBLE);

        } else {
            ((ScrollView) findViewById(R.id.ScrollView)).setVisibility(View.VISIBLE);
            lv.setVisibility(View.VISIBLE);
            btnnext.setEnabled(true);
            btnnext.setVisibility(View.VISIBLE);
            btndelete.setEnabled(true);
            btndelete.setVisibility(View.VISIBLE);
            btndeselect.setVisibility(View.VISIBLE);
            btnselect.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.respone_stampa)).setText("");
            ((TextView) findViewById(R.id.respone_stampa)).setVisibility(View.GONE);
        }
        //modelArrayList = getModel(false);
        //customAdapter = new CustomAdapter(this,modelArrayList);
        //lv.setAdapter(customAdapter);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelArrayList = getModel(true);
                customAdapter = new CustomAdapter(TokenActivity.this, modelArrayList);
                lv.setAdapter(customAdapter);
            }
        });
        btndeselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelArrayList = getModel(false);


                customAdapter = new CustomAdapter(TokenActivity.this, modelArrayList);
                lv.setAdapter(customAdapter);
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (customAdapter.job_selezionati.size() != 0) {
                    Intent intent = new Intent(TokenActivity.this, Stampa.class);

                    intent.putExtra("json", response);
                    intent.putExtra("accessToken", accessTokenResponse);
                    intent.putExtra("jobToDo", customAdapter.job_selezionati);
                    //String accessTokenResponse = getIntent().getExtras().getString("accessToken");
                    //intent.putExtra("accessToken",accessTokenResponse);
                    startActivity(intent);
                    //finish();
                } else {
                    AlertDiStampa("Non hai selezionato nessun lavoro da stampare!");
                }
            }
        });
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (customAdapter.job_selezionati.size() != 0) {
                    delete(accessTokenResponse,customAdapter.job_selezionati);

                } else {
                    AlertDiStampa("Non hai selezionato nessun lavoro da stampare!");
                }
            }
        });
    }


    @MainThread
    private void refreshAccessToken() {
        displayLoading("Refreshing access token");
        performTokenRequest(
                mStateManager.getCurrent().createTokenRefreshRequest(),
                this::handleAccessTokenResponse);
        accessTokenResponse = mStateManager.getCurrent().getAccessToken();
    }

    @MainThread
    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
        displayLoading("Exchanging authorization code");
        performTokenRequest(
                authorizationResponse.createTokenExchangeRequest(),
                this::handleCodeExchangeResponse);
    }

    @MainThread
    private void performTokenRequest(
            TokenRequest request,
            AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = mStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s)", ex);
            displayNotAuthorized("Client authentication method is unsupported");
            return;
        }

        mAuthService.performTokenRequest(
                request,
                clientAuthentication,
                callback);
    }

    @WorkerThread
    private void handleAccessTokenResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        mStateManager.updateAfterTokenResponse(tokenResponse, authException);
        runOnUiThread(this::displayAuthorized);
    }

    @WorkerThread
    private void handleCodeExchangeResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {

        mStateManager.updateAfterTokenResponse(tokenResponse, authException);
        if (!mStateManager.getCurrent().isAuthorized()) {
            final String message = "Authorization Code exchange failed"
                    + ((authException != null) ? authException.error : "");

            // WrongThread inference is incorrect for lambdas
            //noinspection WrongThread
            runOnUiThread(() -> displayNotAuthorized(message));
        } else {
            runOnUiThread(this::displayAuthorized);
        }
    }

    @MainThread
    private void fetchUserInfo() {
        displayLoading("Fetching user info");
        mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::fetchUserInfo);
    }

    @MainThread
    private void fetchUserInfo(String accessToken, String idToken, AuthorizationException ex) {
        if (ex != null) {
            Log.e(TAG, "Token refresh failed when fetching user info");
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
        }

        AuthorizationServiceDiscovery discovery =
                mStateManager.getCurrent()
                        .getAuthorizationServiceConfiguration()
                        .discoveryDoc;

        URL userInfoEndpoint;
        try {
            userInfoEndpoint =
                    mConfiguration.getUserInfoEndpointUri() != null
                            ? new URL(mConfiguration.getUserInfoEndpointUri().toString())
                            : new URL(discovery.getUserinfoEndpoint().toString());
        } catch (MalformedURLException urlEx) {
            Log.e(TAG, "Failed to construct user info endpoint URL", urlEx);
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
        }

        mExecutor.submit(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) userInfoEndpoint.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setInstanceFollowRedirects(false);
                String response = Okio.buffer(Okio.source(conn.getInputStream()))
                        .readString(Charset.forName("UTF-8"));


                mUserInfoJson.set(new JSONObject(response));
            } catch (IOException ioEx) {
                Log.e(TAG, "Network error when querying userinfo endpoint", ioEx);
                showSnackbar("Fetching user info failed");
            } catch (JSONException jsonEx) {
                Log.e(TAG, "Failed to parse userinfo response");
                showSnackbar("Failed to parse user info");
            }

            runOnUiThread(this::displayAuthorized);
        });
    }

    @MainThread
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
                message,
                Snackbar.LENGTH_SHORT)
                .show();
    }
    //private void jobListJSON() {
    //displayLoading("Fetching Job List");
    //mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::jobListJSON);
    //}

    private void jobList() {
        displayLoading("Fetching Job List");
        mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::jobList);
    }


    /**
     * richista http che richiede la lista dei Job da Google
     * @param authToken token di autenticazione
     * @param printerId Id della printer dalla quale si vogliono scaricare i lavori/stampe (Spooler)
     * @return
     * @throws IOException
     */
    private Response requestQueueJobs(String authToken, String printerId) throws IOException {
        System.err.println("StartRequestJobList:"+ new Timestamp(System.currentTimeMillis()));
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        //RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"" + printerId + "\"\r\n\r\n4f6b7646-f5de-6950-684d-e393434aff2e\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
        //status in progress added

        RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"status\"\r\n\r\nIN_PROGRESS\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"printerid\"\r\n\r\n"+printerId+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"limit\"\r\n\r\n"+Commons.limit+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");

        Request request = new Request.Builder()
                .url("https://www.google.com/cloudprint/jobs")
                .post(body)
                .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                .addHeader("X-CloudPrint-Proxy", "node-gcp")
                .addHeader("Authorization", "OAuth " + authToken)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "a7a08892-a007-4a02-85f0-88351dc02aa6")
                .build();
        return client.newCall(request).execute();

    }

    /**
     * metodo per il signout e distuzione dell'authtoken google
     */
    @MainThread
    private void signOut() {
        AuthState currentState = mStateManager.getCurrent();
        AuthState clearedState =
                new AuthState(currentState.getAuthorizationServiceConfiguration());
        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }
        mStateManager.replace(clearedState);
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    /**
     * metodo che attraverso un mExecutor esegue la chiamata http per ottenere i dati relativi alla lista dei job
     *
     * @param accessToken token d'autenticazione
     * @param s
     * @param ex
     */
    private void jobList(String accessToken, String s, AuthorizationException ex) {
        //controllo se il token è valido altrimenti lo refresho
        AuthState state = mStateManager.getCurrent();
        if (state.getRefreshToken() == null || state.getAccessToken() == null || state.getAccessTokenExpirationTime() < System.currentTimeMillis()) {
            refreshAccessToken();
        }
        if (ex != null) {
            Log.e(TAG, "Token refresh failed when fetching user info");
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
        }
        AuthorizationServiceDiscovery discovery =
                mStateManager.getCurrent()
                        .getAuthorizationServiceConfiguration()
                        .discoveryDoc;
        URL joburl;
        try {
            joburl =
                    mConfiguration.getUserInfoEndpointUri() != null
                            ? new URL(mConfiguration.getmCloudList().toString())
                            : new URL(discovery.getUserinfoEndpoint().toString());
        } catch (MalformedURLException urlEx) {
            Log.e(TAG, "Failed to construct user info endpoint URL", urlEx);
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
        }
        mExecutor.submit(() -> {
            try {
                Response responseQueueJob = requestQueueJobs(accessToken, Commons.spoolerID);
                System.err.println("EndRequestJobList:"+ new Timestamp(System.currentTimeMillis()));
                System.out.println("headerRrequest: " + responseQueueJob.headers().toString());
                response = responseQueueJob.body().string();
                System.out.println("RESPONSEJOB:"+response);
                onResponse(responseQueueJob);
                //Log.e("Response jobs",response);
                //System.out.println("Response jobs: "+response);
                /*Intent intent = new Intent(TokenActivity.this,job_list.class);
                intent.putExtra("json",response);
                intent.putExtra("accessToken",accessTokenResponse);
                startActivity(intent);
                finish();*/
                mUserInfoJson.set(new JSONObject(response));
            } catch (IOException ioEx) {
                Log.e(TAG, "Network error when querying Job List endpoint", ioEx);
                showSnackbar("Fetching Job List failed");
            } catch (JSONException jsonEx) {
                Log.e(TAG, "Failed to parse Job List response");
                showSnackbar("Failed to parse Job List");
            }
            runOnUiThread(this::displayAuthorized);
        });
    }


    //message dialog
    public void AlertDiStampa(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Response di stampa");
        // Icon Of Alert Dialog
        alertDialogBuilder.setIcon(R.drawable.unknown_user_48dp);
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //finish();
                Toast.makeText(getApplicationContext(), "Check your printer", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private Response requestDelete(String accessToken, String jobID) throws IOException {
        {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("", "")
                    .build();
            Request request = new Request.Builder()
                    .url("https://"+Commons.ipaddress + "/deletejob?access_token="+accessToken+"&jobid="+jobID)
                    .post(formBody)
                    .addHeader("User-Agent", "PostmanRuntime/7.16.3")
                    .addHeader("Accept", "*/*")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Postman-Token", "a7b3e960-1f3f-466d-b934-60ced6e0a1e9,0c98374a-1887-4578-b482-ab2d0ac213a2")
                    .addHeader("Host", "localhost:8080")
                    .addHeader("Content-Type", "multipart/form-data; boundary=--------------------------537641470182740649494269")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Content-Length", "436")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("cache-control", "no-cache")
                    .build();
            return client.newCall(request).execute();
        }
    }

    private void delete(String accessToken, ArrayList jobList) {
        displayLoading("Elimino i job...");
        mExecutor.submit(() -> {
            Iterator iterator;
            int printCode = 400;
            String response_message = "";
            try {
                //  showSnackbar("Trying to print your job");
                iterator = jobList.iterator();
                while (iterator.hasNext()) {
                    String jobID = iterator.next().toString();
                    Response responseDeResponse = requestDelete(accessToken, jobID);
                    printCode = responseDeResponse.code();
                    //print(accessTokenSC, accessTokenResponse, jobID, PrinterID);
                }

                if (jobList.size() > 0) {
                    if (printCode == 200) {
                        response_message = "I lavori sono stati eliminati correttamente.";
                    } else if (printCode != 200) {
                        //to fix it!!
                        response_message = "Problema di connessione con il server...";
                    }
                } else {
                    response_message = "Non hai selezionato nessun lavoro";
                }
            } catch (Exception e) {
                //Log.e(TAG, "Errore nella stampa dei Jobs", e);
                response_message = "delete fallito, riprovare." + e.getMessage();
            }
            Intent intent = new Intent(this, TokenActivity.class);
            intent.putExtra("response", response_message);
            startActivity(intent);
            finish();
        });
    }
}


