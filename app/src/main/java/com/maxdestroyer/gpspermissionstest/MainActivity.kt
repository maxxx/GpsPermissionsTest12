package com.maxdestroyer.gpspermissionstest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Preconditions.checkState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

/**
 * Check GPS permissions system on Android 12
 */
class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_PERMISSIONS = 1000

    private lateinit var statusView : TextView

    private lateinit var fusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.statusView)

        findViewById<View>(R.id.firstView).setOnClickListener {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_PERMISSIONS)
        }
        findViewById<View>(R.id.secondView).setOnClickListener {
            // Expecting that at least coarse location already given by first button

            val isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            if (!isPermanentlyDenied) {
                // System will open app location settings screen automatically
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                // System does nothing on requestPermissions(), see https://issuetracker.google.com/issues/161831475#comment7. Force main app settings open.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", getPackageName(), null)
                startActivity(intent)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()

        checkState()
        getLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (permissions.isNotEmpty()) {
//            } else {
//            }
            checkState()
        }
    }

    private fun checkState() {
        val hasBg = hasBackgroundPermission(this)
        val hasCoarse = hasCoarsePermission(this)
        val hasPrecise = hasPrecisionPermission(this)

        statusView.setText("Status: coarse = $hasCoarse, precise = $hasPrecise, bg = $hasBg")
    }

    @SuppressLint("MissingPermission") // handled by hasCoarsePermission()
    private fun getLocation() {
        if (hasCoarsePermission(this)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    Toast.makeText(this, "Location received, accuracy = ${location?.accuracy}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hasBackgroundPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            true
        } else ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCoarsePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasPrecisionPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}