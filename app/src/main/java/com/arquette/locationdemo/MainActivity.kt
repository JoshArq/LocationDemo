package com.arquette.locationdemo

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arquette.locationdemo.dto.LocationDetails
import com.arquette.locationdemo.ui.theme.LocationDemoTheme

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

            val currentLocation by locationViewModel.getLocationLiveData().observeAsState()
            LocationDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LocationView(currentLocation)
                }
            }//theme

            //check for/ask for permission
            prepLocationUpdates()

        }//setContent
    }//onCreate

    private fun prepLocationUpdates(){
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
            ){
            requestLocationUpdates()
        } else{
            requestSinglePermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestLocationUpdates(){
        locationViewModel.startLocationUpdates()
    }

    private val requestSinglePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
        if(isGranted){
            requestLocationUpdates()
        }
        else{
            Toast.makeText(this, "GPS Unavailable", Toast.LENGTH_LONG).show()
        }
    }

}//Activity

@Composable
fun LocationView(currentLocation: LocationDetails?){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        currentLocation?.let{
            Text(text = "Latitude ${currentLocation.latitude}")
            Text(text = "Longitude ${currentLocation.longitude}")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocationDemoTheme {
        Greeting("Android")
    }
}