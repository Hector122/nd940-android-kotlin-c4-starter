package com.udacity.project4.authentication

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI

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
    fun launchSignInFlow(activity: Activity, auht: AuthMethodPickerLayout, requestCode: Int) {
        // Add option to sign in / register with their email or Google account.
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder()
            .build(),
                AuthUI.IdpConfig.GoogleBuilder()
                    .build())
        
        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        activity.startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(auht)
            .build(), requestCode)
    }
}