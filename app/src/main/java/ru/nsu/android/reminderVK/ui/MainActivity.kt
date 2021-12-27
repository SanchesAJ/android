package ru.nsu.android.reminderVK.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import ru.nsu.android.reminderVK.R
import ru.nsu.android.reminderVK.databinding.ActivityMainBinding
import com.vk.api.sdk.utils.VKUtils
import com.vk.api.sdk.utils.VKUtils.getCertificateFingerprint


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VK.login(this, arrayListOf(VKScope.WALL))

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)
        if (navHostFragment != null) {
            navController = navHostFragment.findNavController()
        }

        NavigationUI.setupActionBarWithNavController(this, navController)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
               // TODO( User passed authorization)
            }

            override fun onLoginFailed(authException: VKAuthException) {
                // TODO(User didn't pass authorization)
                print(0)
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        // "If you do not have a DrawerLayout, you should call NavController.navigateUp() directly."
        return navController.navigateUp()
    }
}
