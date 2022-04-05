package com.coba.mymapsapps

import android.location.Geocoder
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coba.mymapsapps.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var db : DatabaseReference
    private var dataId = ""
    private var defaultMaps = GoogleMap.MAP_TYPE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mFirebaseInstance = FirebaseDatabase.getInstance()
        db = mFirebaseInstance.getReference("data")
        mFirebaseInstance.getReference("app_title").setValue("Tugas 4 - Yulius - 672019014 (save place coordinates using Firebase)")

        binding.btnSave.setOnClickListener {
            savePlaces()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ms_default -> {
                defaultMaps = GoogleMap.MAP_TYPE_NORMAL

                // Reload Fragment
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

            R.id.ms_terrain -> {
                defaultMaps = GoogleMap.MAP_TYPE_TERRAIN

                // Reload Fragment
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

            R.id.ms_satellite -> {
                defaultMaps = GoogleMap.MAP_TYPE_SATELLITE

                // Reload Fragment
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

            R.id.ms_hybrid -> {
                defaultMaps = GoogleMap.MAP_TYPE_HYBRID

                // Reload Fragment
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(defaultMaps)
        // First View on Pati.
        val pati = LatLng(-6.7559, 111.038)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pati))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pati,15.0f))


            mMap.setOnMapClickListener(this)


    }

    override fun onMapClick(latLng: LatLng) {
        if(TextUtils.isEmpty(binding.etLongitude.editText?.text.toString()) && TextUtils.isEmpty(binding.etLatitude.editText?.text.toString())){
            mMap.addMarker(MarkerOptions().position(latLng).title(getFullyAddress(latLng)).icon(BitmapDescriptorFactory.defaultMarker(Random.nextDouble(0.0,360.0).toFloat())))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f))
            binding.apply {
                etLatitude.editText?.setText(latLng.latitude.toString(), TextView.BufferType.EDITABLE)
                etLongitude.editText?.setText(latLng.longitude.toString(), TextView.BufferType.EDITABLE)
            }
            mMap.uiSettings.setAllGesturesEnabled(false)
        }
    }

    private fun getFullyAddress(latLng: LatLng) : String?{
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1)
        return list[0].getAddressLine(0)
    }

    private fun savePlaces() {
        val latitude = binding.etLatitude.editText?.text.toString()
        val longitude = binding.etLongitude.editText?.text.toString()
        val placesData = PlacesData(latitude,longitude)

        if(TextUtils.isEmpty(dataId)) {
            dataId = db.push().key.toString()
            createNewData(placesData)
        } else {
            updateData(placesData)
        }
        mMap.uiSettings.setAllGesturesEnabled(true)
        toggleButton()
    }

    private fun toggleButton() {
        if(TextUtils.isEmpty(dataId)) binding.btnSave.text = getString(R.string.save_places) else binding.btnSave.text = getString(R.string.update_places)
    }

    private fun updateData(placesData: PlacesData) {
        if(!TextUtils.isEmpty(placesData.latitude))
            db.child(dataId).child("latitude").setValue(placesData.latitude)

        if(!TextUtils.isEmpty(placesData.longitude))
            db.child(dataId).child("longitude").setValue(placesData.longitude)

        binding.etLatitude.editText?.setText("")
        binding.etLongitude.editText?.setText("")
    }

    private fun createNewData(placesData: PlacesData) {
        db.child(dataId).setValue(placesData).addOnCompleteListener {
            binding.etLatitude.editText?.setText("")
            binding.etLongitude.editText?.setText("")
        }
    }

}