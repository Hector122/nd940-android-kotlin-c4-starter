package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    
    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 8888
    }
    
    // Get a reference to the ViewModel scoped to this Activity
    private val viewModel by viewModels<AuthenticationViewModel>()
    
    // a bonus is to customize the sign in flow to look nice using :
    //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    private val getAuthPickerLayout = AuthMethodPickerLayout.Builder(R.layout.custom_auth_layout)
        .setGoogleButtonId(R.id.button_sing_google)
        .setEmailButtonId(R.id.button_sing_email)
        .build()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val binding: ActivityAuthenticationBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        
        binding.buttonLogin.setOnClickListener {
            viewModel.launchSignInFlow(this, getAuthPickerLayout, SIGN_IN_RESULT_CODE)
        }

        

//          TODO: If the user was authenticated, send him to RemindersActivity
    
    
    }
    
    //TODO: remove.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                val name = FirebaseAuth.getInstance().currentUser?.displayName
                Log.i(TAG, "Successfully signed in user " + "${name}!")
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}
