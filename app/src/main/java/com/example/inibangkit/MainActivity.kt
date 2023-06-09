package com.example.inibangkit

import android.annotation.SuppressLint
import android.content.AsyncQueryHandler
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.inibangkit.ml.BestFloat16
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage

import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer

class MainActivity : AppCompatActivity() {
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )
    val paint = Paint()
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var cameraManager: CameraManager
    lateinit var textureview: TextureView
    lateinit var model: BestFloat16

    var tensorImage = TensorImage(DataType.FLOAT32)
//    private lateinit var tfliteModel: Interpreter
//    private lateinit var probabilityProcessor: TensorProcessor
//    private lateinit var outputArray: Array<Array<FloatArray>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = BestFloat16.newInstance(this)

        setContentView(R.layout.activity_main)
        get_permission()
//        val modelPath = "best_float16.tflite" // Change to the name of your model file
//        val tfliteOptions = Interpreter.Options()
//        tfliteModel = Interpreter(FileUtil.loadMappedFile(this, modelPath), tfliteOptions)

//        outputArray = Array(1) { Array(13) { FloatArray(8400) } }


//        imageProcessor =
//            ImageProcessor.Builder().add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR)).build()

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)

        textureview = findViewById(R.id.textureView)
        textureview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                bitmap = textureview.bitmap!!


                tensorImage.load(bitmap)

// Preprocess the image
                tensorImage = imageProcessor.process(tensorImage)

//                probabilityProcessor = TensorProcessor.Builder().add(NormalizeOp(0f, 255f)).build()
                //val byteBuffer = probabilityProcessor.process(tensorImage.tensorBuffer.floatBuffer)

////                val tensorProcessorInput = tensorImage.tensorBuffer
////                tensorProcessorInput.loadBuffer(tensorImage.buffer)
////                val byteBuffer = tensorProcessorInput.buffer
//                val tensorProcessorInput = TensorBuffer.createDynamic(DataType.FLOAT32)
//                tensorProcessorInput.loadBuffer(tensorImage.buffer, tensorImage.tensorBuffer.shape)
//                val byteBuffer = tensorProcessorInput.buffer
//
//                // Run inference
//                tfliteModel.run(byteBuffer, outputArray)
//
//                val detections = getDetectionsFromOutput(outputArray[0])



//                val outputs = model.process(image)
//                val outputlist = outputs.outputAsCategoryList[0]
//
//                for (output in outputlist) {
//                    val label = output.label
//                    val score = output.score
//                    val displayName = output.displayName
//                }


////                Log.d("AAAAAAAAAAAAAAAAAA", output.toString())
//                val locations = outputs.locationsAsTensorBuffer.floatArray
//                val classes = outputs.classesAsTensorBuffer.floatArray
//                val scores = outputs.scoresAsTensorBuffer.floatArray
//                val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

//                val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                val canvas = Canvas(mutable)
//
//                val h = mutable.height
//                val w = mutable.width
//                paint.textSize = h / 15f
//                paint.strokeWidth = h / 85f
//                var x = 0
//                for (detection in detections){
//                    if (detection.score >0.5){
//                        paint.setColor(colors[detection.classIndex % colors.size])
//                        paint.style = Paint.Style.STROKE
//                        canvas.drawRect(
//                            RectF(
//                                detection.boundingBox.left * w,
//                                detection.boundingBox.top * h,
//                                detection.boundingBox.right * w,
//                                detection.boundingBox.bottom * h
//                            ),
//                            paint
//                        )
//                        paint.style = Paint.Style.FILL
//                        canvas.drawText(
//                            "${detection.classIndex} ${detection.score}",
//                            detection.boundingBox.left * w,
//                            detection.boundingBox.top * h,
//                            paint
//                        )
//                    }
//
//                }
//                imageView.setImageBitmap(mutable)



            }

        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

    }
    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                var surfaceTexture = textureview.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }
    fun get_permission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            get_permission()
        }
    }

//    data class Detection(
//        val boundingBox: RectF,
//        val classIndex: Int,
//        val score: Float
//    )

//    private fun getDetectionsFromOutput(outputArray: Array<FloatArray>): List<Detection> {
//        val detections = mutableListOf<Detection>()
//        for (i in outputArray.indices step 4) {
//            val boundingBox = RectF(
//                outputArray[i][0],
//                outputArray[i][1],
//                outputArray[i][2],
//                outputArray[i][3]
//            )
//            val classIndex = outputArray[i][4].toInt()
//            val score = outputArray[i][5]
//            detections.add(Detection(boundingBox, classIndex, score))
//        }
//        return detections
//    }
}