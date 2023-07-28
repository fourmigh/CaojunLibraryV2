package org.caojun.library.tensorflow

import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.caojun.library.tensorflow.view.AutoFitTextureView

class TensorFlowSampleActivity : AppCompatActivity() {

    private lateinit var tensorFlowManger: TensorFlowManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tfeyes)

        val textureView = findViewById<AutoFitTextureView>(R.id.textureView)
        val imageView = findViewById<ImageView>(R.id.imageView)

        tensorFlowManger = TensorFlowManger(this, object : TensorFlowManger.Listener {
            override fun onDisappear() {
                Log.d("TensorFlowManger", "onDisappear")
            }

            override fun onEye(left: Boolean, open: Boolean) {
                Log.d("TensorFlowManger", "onEye: left[${left}], open[${open}]")
            }

            override fun onError() {
                finish()
            }

            override fun onMouth(open: Boolean) {
            }

            override fun onSpeakOver() {
            }
        }, textureView, imageView)
        tensorFlowManger.onCreate()
    }

    override fun onResume() {
        super.onResume()
        tensorFlowManger.onResume()
    }

    override fun onPause() {
        super.onPause()
        tensorFlowManger.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        tensorFlowManger.onDestroy()
    }
}