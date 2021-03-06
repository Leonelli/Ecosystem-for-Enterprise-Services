package com.example.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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
import net.openid.appauth.browser.CustomTabManager;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicReference;


public class CieAuth extends AppCompatActivity {

    private static final String TAG = "CIEAUTH";
    private AuthStateManager_CIE mStateManager_CIE;
    //private Configuration mConfiguration;
    private ConfigurationCIE mConfigurationApp;
    private AuthorizationService mAuthService;
    private final AtomicReference<String> mClientId = new AtomicReference<>();
    @NonNull
    private BrowserMatcher mBrowserMatcher = AnyBrowserMatcher.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cie_auth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mStateManager_CIE = mStateManager_CIE.getInstance(this);
        //state = mStateManager.getCurrent();
        //mConfiguration = Configuration.getInstance(this);
        mConfigurationApp = ConfigurationCIE.getInstance(this);
        mAuthService = new AuthorizationService(
                this,
                new AppAuthConfiguration.Builder()
                        .setConnectionBuilder(mConfigurationApp.getConnectionBuilder())
                        .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeAppAuth();
    }

    private void initializeAppAuth() {
        //recreateAuthorizationService();
        // if we are not using discovery, build the authorization service configuration directly
        // from the static configuration values.
        if (mConfigurationApp.getDiscoveryUri() == null) {
            Log.i(TAG, "Creating auth config from res/raw/auth_config_sc.json");
            AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                    mConfigurationApp.getAuthEndpointUri(),
                    mConfigurationApp.getTokenEndpointUri(),
                    mConfigurationApp.getRegistrationEndpointUri());
            mStateManager_CIE.replace(new AuthState(config));
            initializeClient();
            return;
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        Log.i(TAG, "Retrieving OpenID discovery doc");
        AuthorizationServiceConfiguration.fetchFromUrl(
                mConfigurationApp.getDiscoveryUri(),
                this::handleConfigurationRetrievalResult);
    }

    private void initializeClient() {
        if (mConfigurationApp.getClientId() != null) {
            Log.i(TAG, "Using static client ID: " + mConfigurationApp.getClientId());
            // use a statically configured client ID
            mClientId.set(mConfigurationApp.getClientId());
            initializeAuthRequest();
        }
    }

    private void handleConfigurationRetrievalResult(
            AuthorizationServiceConfiguration config,
            AuthorizationException ex) {
        if (config == null) {
            Log.i(TAG, "Failed to retrieve discovery document", ex);
            return;
        }

        Log.i(TAG, "Discovery document retrieved");
        mStateManager_CIE.replace(new AuthState(config));
        initializeClient();
    }

    private void initializeAuthRequest() {
        createAuthRequest();
        //warmUpBrowser();
    }

   /* private void warmUpBrowser() {
        mAuthIntentLatch = new CountDownLatch(1);
        Log.i(TAG, "Warming up browser instance for auth request");
        CustomTabsIntent.Builder intentBuilder =
                mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri());
        mAuthIntent.set(intentBuilder.build());
        mAuthIntentLatch.countDown();
    }*/

    private void createAuthRequest() {
        AuthorizationRequest authRequest = new AuthorizationRequest.Builder(
                mStateManager_CIE.getCurrent().getAuthorizationServiceConfiguration(),
                mClientId.get(),
                ResponseTypeValues.CODE,
                mConfigurationApp.getRedirectUri())
                .setScope(mConfigurationApp.getScope())
                .build();
        System.err.println("StartRequestLetturaCie:"+ new Timestamp(System.currentTimeMillis()));

        mAuthService.performAuthorizationRequest(
                authRequest,
                CieTokenActivity.createPostAuthorizationIntent(
                        this,
                        authRequest,
                        null,
                        mConfigurationApp.getClientSecret()),
                mAuthService.createCustomTabsIntentBuilder()
                        .build());
    }


   /* static PendingIntent createPostAuthorizationIntent(
            @NonNull Context context,
            @NonNull AuthorizationRequest request,
            @Nullable AuthorizationServiceDiscovery discoveryDoc,
            @Nullable String clientSecret) {
        Intent intent = new Intent(context, TokenActivity.class);
        if (discoveryDoc != null) {
            intent.putExtra("authServiceDiscovery", discoveryDoc.docJson.toString());
        }

        if (clientSecret != null) {
            intent.putExtra("clientSecret", clientSecret);
        }

        return PendingIntent.getActivity(context, request.hashCode(), intent, 0);
    }*/


    private AuthorizationService createAuthorizationService() {
        Log.i(TAG, "Creating authorization service");
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setBrowserMatcher(mBrowserMatcher);
        builder.setConnectionBuilder(mConfigurationApp.getConnectionBuilder());

        return new AuthorizationService(this, builder.build());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        //System.out.println("CIE APP data reponse: "+ data);
        String idToken_SC=mStateManager_CIE.getCurrent().getIdToken();
        //System.out.println("idToken_SC: "+idToken_SC);
        Intent intent = new Intent(this, QrReader.class);
        intent.putExtras(data.getExtras());
        intent.putExtra("idToken_SC",idToken_SC );
        //System.out.println("DATA_MAIN ACTIVITY: "+data);
        startActivity(intent);
        finish();
    }


    @MainThread
    private void signOut() {
        AuthState currentState = mStateManager_CIE.getCurrent();
        AuthState clearedState =
                new AuthState(currentState.getAuthorizationServiceConfiguration());
        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }
        mStateManager_CIE.replace(clearedState);
    }
}
