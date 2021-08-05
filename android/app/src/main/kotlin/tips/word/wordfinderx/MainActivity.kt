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
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
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

                val result1 : Mat = findBoard(imagePath)
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

        //check if its valid

        val values: ArrayList<Int> = ArrayList()
        for (x in contours.indices) {

            val value : MatOfPoint = contours[x]

            val rect: Rect = Imgproc.boundingRect(value)

            val w = rect.width
            val h = rect.height

            values.add(w)
            values.add(h)
        }

        //good to go ?
        //let me check again
        // okay. let's check squaresize
        var mostFrequent = 0
        var rlt = 0

        for(x in values.indices){
            val el = values[x]

            var frequency = 0
            for(y in values.indices) {
                if(el == values[y]){
                    frequency++
                }
            }

            if(frequency>mostFrequent) {
                mostFrequent = frequency
                rlt = values[x]
            }


        }


        return rlt
    }

    fun getContourPrecedence(contour: Mat ,cols : Int):Int{
        val toleranceFactor = 10
        val origin = Imgproc.boundingRect(contour)


        return ((origin.y/ toleranceFactor)*toleranceFactor)*cols+origin.x


    }



    fun findBoard(path: String):Mat{

        val image = Imgcodecs.imread(path)
        val image2 = Imgcodecs.imread(path)
        val tmp_img = Imgcodecs.imread(path)
        val ori_img = Imgcodecs.imread(path)


        val image_w = image.cols()
        val image_h = image.rows()

        val left = arrayOf(image_w, 0, 0, 0)
        val right = arrayOf(0, 0, 0, 0)
        val top = arrayOf(0, image_h, 0, 0)
        val top_2nd = top
        val bottom = arrayOf(0, 0, 0, 0)

        val grayImage = tmp_img



        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY)
        val blurImage = image
        Imgproc.GaussianBlur(grayImage, blurImage, Size(3.0, 3.0), 0.0)
        val edges: Mat  = image2
        Imgproc.Canny(blurImage, edges, 30.0, 90.0)

        try {
            var letters = tmp_img
            Imgproc.rectangle(letters, Point(0.0, 0.0),Point(image_w.toDouble(), image_h.toDouble()), Scalar(0.0,0.0, 0.0),Imgproc.FILLED)
            val hierarchy = Mat()

            var contours: List<MatOfPoint> = ArrayList()

            findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)
            for (l in contours.indices) {

                try {

                    val value: MatOfPoint = contours[l]

                    val rect: Rect = Imgproc.boundingRect(value)

                    val w = rect.width
                    val h = rect.height
                    val x = rect.x
                    val y = rect.y
                    val rect_aspect = w/(h).toDouble()

                    var contour_squareness = Imgproc.contourArea(value)/(w*h).toDouble()

                    if(rect_aspect>0.90 && rect_aspect<1.1 && w>image_w/20.0 && w<image_w/10.0 && contour_squareness>0.85 && contour_squareness<1.15 ) {
                        Imgproc.rectangle(letters, Point(x.toDouble(), y.toDouble()), Point((x + w - 1).toDouble(), (y + h - 1).toDouble()), Scalar(255.0, 255.0, 255.0), -1)
                    }


                } catch (e: Exception) {
                    Log.e("loop1", e.toString())
                }
            }

            var tempContours: List<MatOfPoint> = ArrayList()

            findContours(letters, tempContours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)


            tempContours = tempContours.sortedBy { getContourPrecedence(it,edges.width())}

            val squareSize = getSquareSize(tempContours)

            try {
                //find biggest
                for (l in tempContours.indices) {

                    try {

                        val value: MatOfPoint = tempContours[l]

                        val rect: Rect = Imgproc.boundingRect(value)


                        val w = rect.width
                        val h = rect.height
                        val x = rect.x
                        val y = rect.y

                        val dif_w = abs(squareSize - w)
                        val dif_h = abs(squareSize - h)

                        val tolerance = (squareSize / 10).toInt() + 1


                        if (dif_w <= tolerance && dif_h <= tolerance) {
                            if (x < left[0]) {
                                left[0] = x
                                left[1] = y
                                left[2] = rect.width
                                left[3] = rect.height
                            }

                            if (x + w > right[0] + right[2]) {
                                right[0] = x
                                right[1] = y
                                right[2] = rect.width
                                right[3] = rect.height
                            }

                            if (y < top[1]) {
                                top_2nd[0] = top[0]
                                top_2nd[1] = top[1]
                                top_2nd[2] = top[2]
                                top_2nd[3] = top[3]
                                top[0] = x
                                top[1] = y
                                top[2] = rect.width
                                top[3] = rect.height
                            } else if (y < top_2nd[1]) {
                                top_2nd[0] = x
                                top_2nd[1] = y
                                top_2nd[2] = rect.width
                                top_2nd[3] = rect.height
                            }

                            if (y + h > bottom[1] + bottom[3]) {
                                bottom[0] = x
                                bottom[1] = y
                                bottom[2] = rect.width
                                bottom[3] = rect.height
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("loopexc", e.toString())
                    }
                }

            }catch (e: Exception){
                Log.e("outexc", e.toString())
            }

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

            return Mat(ori_img, Rect(xo,yo, xf-xo, yf-yo))


        }catch (e: Exception){
            Log.e("EXCEPTION", e.toString())
            return edges
        }

        return edges


    }


    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                    //imageMat = Mat()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
}