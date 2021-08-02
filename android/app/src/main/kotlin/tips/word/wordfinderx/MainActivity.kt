package tips.word.wordfinderx

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
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.findContours
import java.io.File
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

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

                Log.e("Path", imagePath.toString())

                val result1 :Mat = findBoard(imagePath)
                val outputDir = cacheDir.absolutePath
                val cannyFilename = outputDir + File.separator + "image"+ UUID.randomUUID().toString() +  "." +"png"

                Imgcodecs.imwrite(cannyFilename, result1)

                Log.e("OutputPath", cannyFilename)

                result.success(cannyFilename)
            }
            else
                result.notImplemented()
        }
    }

    fun getSquareSize(contours: List<MatOfPoint>): Int {
        var values: ArrayList<Int> = ArrayList<Int>();
        for (x in contours.indices) {

            val value : MatOfPoint = contours[x]

            val rect: Rect = Imgproc.boundingRect(value)

            val w = rect.width
            val h = rect.height
            val x = rect.x;
            val y = rect.y;

            values.add(w)
            values.add(h)
        }
        return max(values.toSet().count(), values.count())
    }



    fun findBoard(path: String):Mat{

        val image = Imgcodecs.imread(path)
        val image2 = Imgcodecs.imread(path)
        Log.e("Image", "Image Read");
        val dstImage = image
        image.copyTo(dstImage)

        val image_w = image.width()
        val image_h = image.height()

        val left = arrayOf(image_w, 0, 0, 0)
        val right = arrayOf(0, 0, 0, 0)
        val top = arrayOf(0, image_h, 0, 0)
        val top_2nd = top
        val bottom = arrayOf(0, 0, 0, 0)

        val grayImage= dstImage

        Log.e("Gray", "Generating Gray Image")


      //  Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_GRAY2BGR555)



        Log.e("Gray", "Generated")

        val blurImage = dstImage
        Log.e("Blur", "Generating Blur Image")

        Imgproc.GaussianBlur(grayImage, blurImage, Size(3.0, 3.0), 0.0)



        Log.e("Blur", "Generated")


        val edges: Mat  = image2
       // val edges : Mat = grayImage

        Imgproc.Canny(blurImage, edges, 30.0, 90.0)

        //return edges

        Log.e("CANNY", "Generated")

        try {
            val letters = edges;
            edges.copyTo(letters)

            //Imgproc.adaptiveThreshold(image, dstImage, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2)


            Log.e("copied", "Generated")
            val hierarchy = Mat()
            val contours: List<MatOfPoint> = ArrayList()

            findContours(letters, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)



            Log.e("Contours", "Generated")
            //find biggest

            val squareSize = getSquareSize(contours)

            try {
                //find biggest
                for (l in contours.indices) {

                    try {

                        val value: MatOfPoint = contours[l]

                        val rect: Rect = Imgproc.boundingRect(value)

                        Log.e("value", "Generated")
                        val w = rect.width
                        val h = rect.height
                        val x = rect.x
                        val y = rect.y

                        val dif_w = abs(squareSize - w)
                        val dif_h = abs(squareSize - h)

                        val tolerance = (squareSize / 10).toInt() + 1

                        Log.e("tolerance", "Generated")
                        if (dif_w <= tolerance && dif_h <= tolerance) {
                            if (x < left[0]) {
                                left[0] = rect.width
                                left[1] = rect.height
                                left[2] = rect.x
                                left[3] = rect.y
                            }

                            if (x + w > right[0] + right[1]) {
                                right[0] = rect.width
                                right[1] = rect.height
                                right[2] = rect.x
                                right[3] = rect.y
                            }

                            if (y < top[1]) {
                                top_2nd[0] = rect.width
                                top_2nd[1] = rect.height
                                top_2nd[2] = rect.x
                                top_2nd[3] = rect.y
                                top[0] = rect.width
                                top[1] = rect.height
                                top[2] = rect.x
                                top[3] = rect.y;
                            } else if (y < top_2nd[1]) {
                                top_2nd[0] = rect.width
                                top_2nd[1] = rect.height
                                top_2nd[2] = rect.x
                                top_2nd[3] = rect.y
                            }

                            if (y + h > bottom[1] + bottom[3]) {
                                bottom[0] = rect.width
                                bottom[1] = rect.height
                                bottom[2] = rect.x
                                bottom[3] = rect.y
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("loopexception", e.toString())
                    }
                }

            }catch (e: Exception){
                Log.e("outexception", e.toString())
            }
            Log.e("loop", "done")

         var xo = left[0]
         var yo = top_2nd[1];
         var xf = right[0]+right[2]
         var yf = bottom[1]+bottom[3]

         val divCells = (xf- xo)/squareSize

         var cells = 0

         if(divCells>15)
             cells =15
         else if(divCells <11)
             cells = 9
         else
             cells = 11

         val innerSpace = ((xf-xo)-squareSize*cells)/ (cells-1)

            xo -= (innerSpace / 2) //boardleft
            xf += (innerSpace / 2) //board right
            yo -= (innerSpace / 2) //board top
            yf += (innerSpace / 2) //board bottom
            Log.e("secondlast", "done")


            Log.e("finalvalues",yo.toString()+" "+yf.toString()+" "+xo.toString()+" "+xf.toString())


            return Mat(image, Rect(yo,yf,xo,xf))




        }catch (e: Exception){
            Log.e("EXCEPTION", e.toString())
            return blurImage
        }

        return blurImage


    }
}