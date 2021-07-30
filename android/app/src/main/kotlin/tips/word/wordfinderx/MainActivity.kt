package tips.word.wordfinderx

import android.graphics.Camera
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.findContours
import java.io.File
import java.lang.reflect.Array.set

class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "tips.word.wordfinderx/image"
        private const val METHOD_GET_LIST = "find_board"
    }

    private lateinit var channel: MethodChannel

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { methodCall: MethodCall, result: MethodChannel.Result ->
            if (methodCall.method == METHOD_GET_LIST) {
                val imagePath = methodCall.argument<String>("filePath").toString()

                Log.e("Path",imagePath.toString())

                val result1 :Mat = findBoard(imagePath)
                val outputDir = cacheDir.absolutePath
                val cannyFilename = outputDir + File.separator +  "." +"png"

                Imgcodecs.imwrite(cannyFilename, result1)

                Log.e("OutputPath",cannyFilename)

                result.success(cannyFilename)
            }
            else
                result.notImplemented()
        }
    }



    fun findBoard(path: String):Mat{

        val image = Imgcodecs.imread(path)
        Log.e("Image","Image Read");
        val dstImage = image
        image.copyTo(dstImage)

        val image_w = image.cols()
        val image_h = image.cols()

        val left = arrayOf(image_w, 0, 0, 0)
        val right = arrayOf(0, 0, 0, 0)
        val top = arrayOf(0, image_h, 0, 0)
        val top_2nd = top
        val bottom = arrayOf(0, 0, 0, 0)

        val grayImage= dstImage

        Log.e("Gray","Generating Gray Image")


      //  Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_GRAY2BGR555)



        Log.e("Gray","Generated")

        val blurImage = dstImage
        Log.e("Blur","Generating Blur Image")

        Imgproc.GaussianBlur(grayImage, blurImage, Size(3.0, 3.0), 0.0)

        return blurImage

        Log.e("Blur","Generated")


        val edges = dstImage;

        Imgproc.Canny(blurImage, edges, 30.0, 90.0)

        try {
            val letters = null;
            edges.copyTo(letters)

            //Imgproc.adaptiveThreshold(image, dstImage, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2)


            val hierarchy = Mat()
            val contours: List<MatOfPoint> = ArrayList()

            findContours(letters, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)

            //find biggest

            //find biggest
            for (x in contours.indices) {
                val rect: MatOfPoint? = null
                Imgproc.boundingRect(rect)
                val w = rect?.width()
                val h = rect?.height()

            }
        }catch (e: Exception){
            Log.e("EXCEPTION",e.toString())
            return blurImage
        }

        return blurImage


    }
}