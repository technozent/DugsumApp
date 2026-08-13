package com.dug.sun


import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
class FullSCreenActivity  : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var draggableTextBox: DraggableTextBoxNew

    private lateinit var buttonIncrease: Button
    private lateinit var buttonDecrease: Button

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_full
        )

        // ---------------------------------------------------------
        // Views
        // ---------------------------------------------------------

        imageView =
            findViewById(R.id.imageView)

        draggableTextBox =
            findViewById(R.id.draggableTextBox)

        buttonIncrease =
            findViewById(R.id.buttonIncrease)

        buttonDecrease =
            findViewById(R.id.buttonDecrease)

        // ---------------------------------------------------------
        // Get text from previous Activity
        // ---------------------------------------------------------

        val text =
            intent.getStringExtra("TEXT")

        if (!text.isNullOrEmpty()) {

            draggableTextBox.setText(
                text
            )
        } else {

            draggableTextBox.setText(
                "100"
            )
        }

        // ---------------------------------------------------------
        // Get image from previous Activity
        // ---------------------------------------------------------



        // ---------------------------------------------------------
        // Increase rectangle size
        // ---------------------------------------------------------

        buttonIncrease.setOnClickListener {

            draggableTextBox.increaseSize(
                20
            )
        }

        // ---------------------------------------------------------
        // Decrease rectangle size
        // ---------------------------------------------------------

        buttonDecrease.setOnClickListener {

            draggableTextBox.decreaseSize(
                20
            )
        }
    }
}