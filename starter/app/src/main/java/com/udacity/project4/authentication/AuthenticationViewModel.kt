package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.udacity.project4.R

enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}

class AuthenticationViewModel : ViewModel() {
    
    //authenticationState variable
    val authenticationState = FirebaseUserLiveData().map { firebaseUser ->
        if (firebaseUser != null) AuthenticationState.AUTHENTICATED
        else AuthenticationState.UNAUTHENTICATED
    }
    
    /**
     * Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
     */
    fun getAuthIntent(): Intent {
        // a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        val authPickerLayout = AuthMethodPickerLayout.Builder(R.layout.custom_auth_layout)
            .setGoogleButtonId(R.id.button_sing_google)
            .setEmailButtonId(R.id.button_sing_email)
            .build()
        
        // Add option to sign in / register with their email or Google account.
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())
        
        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(authPickerLayout)
            .setIsSmartLockEnabled(false)
            .build()
    }
}