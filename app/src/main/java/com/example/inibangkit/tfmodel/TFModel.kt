package com.example.inibangkit.tfmodel

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import com.example.inibangkit.ml.BestFloat16
import com.example.inibangkit.ml.BestFloat16.Outputs
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class TFModel constructor(ctx: AppCompatActivity) {
    lateinit var model: BestFloat16
    private var tensorImage = TensorImage(DataType.FLOAT32)
    private var imageProcessor = ImageProcessor.Builder().add(
        ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR)
    ).build()

    private var isProcessing = false

    init {
        model = BestFloat16.newInstance(ctx)
    }

    fun predict(image: Bitmap): Outputs {
        // TODO: do stuff
        isProcessing = true
        tensorImage.load(image)
        tensorImage = imageProcessor.process((tensorImage))

        var output = model.process(tensorImage)

        isProcessing = false
        return output
    }

    fun isCurrentlyProcessing(): Boolean {
        return isProcessing
    }
}