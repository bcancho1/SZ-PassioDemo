package ai.passio.passiosdksandbox

import ai.passio.passiosdk.core.config.PassioConfiguration
import ai.passio.passiosdk.core.config.PassioMode
import ai.passio.passiosdk.passiofood.PassioSDK
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
private const val REQUEST_CODE_PERMISSIONS = 789

class MainActivity : AppCompatActivity() {

    private val permissionFragment = PermissionFragment()
    private val recognizerFragment = RecognizerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            onCameraPermissionGranted()
        }

        val config = PassioConfiguration(
            this.applicationContext,
            "ox3AMO5iOd7ZoDDBZSwkJvMx1gWZgUSW5OV7UCJKm1PU",
        ).apply {
            sdkDownloadsModels = true
            debugMode = -333
        }

        PassioSDK.instance.configure(config) { passioStatus ->
            Log.d("PAS-Main", "Status: $passioStatus")
            when (passioStatus.mode) {
                PassioMode.NOT_READY -> onPassioSDKError(passioStatus.debugMessage)
                PassioMode.IS_BEING_CONFIGURED -> onPassioSDKError(passioStatus.debugMessage)
                PassioMode.FAILED_TO_CONFIGURE -> onPassioSDKError(passioStatus.debugMessage)
                PassioMode.IS_DOWNLOADING_MODELS -> {}
                PassioMode.IS_READY_FOR_DETECTION -> onPassioSDKReady()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // PassioSDK.instance.shutDownPassioSDK()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                onCameraPermissionGranted()
            } else {
                onCameraPermissionDenied()
            }
        }
    }

    private fun hasPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun onCameraPermissionDenied() {
        loadFragment(permissionFragment)
    }

    private fun onCameraPermissionGranted() {
        loadFragment(recognizerFragment)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainFragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun onPassioSDKError(errorMessage: String?) {
        Log.e(this::class.java.simpleName, errorMessage ?: "")
    }

    private fun onPassioSDKReady() {
        Log.i(this::class.java.simpleName, "Passio SDK ready")
    }
}