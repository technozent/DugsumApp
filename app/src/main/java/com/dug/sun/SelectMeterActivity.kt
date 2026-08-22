package com.dug.sun

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectMeterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_meter)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvMeters: RecyclerView = findViewById(R.id.rvMeters)
        rvMeters.layoutManager = GridLayoutManager(this, 2)

        val meterList = mutableListOf<MeterAdapter.MeterItem>()
        for (i in 1..19) {
            val name = "m$i"
            val resId = resources.getIdentifier(name, "drawable", packageName)
            if (resId != 0) {
                meterList.add(MeterAdapter.MeterItem(name.uppercase(), resId))
            }
        }

        rvMeters.adapter = MeterAdapter(meterList) { selectedItem ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_meter_res_id", selectedItem.resId)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}