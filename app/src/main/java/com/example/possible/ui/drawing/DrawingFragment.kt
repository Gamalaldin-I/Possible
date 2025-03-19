package com.example.possible.ui.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.possible.databinding.FragmentDrawingBinding
import com.example.possible.repo.local.LettersAndNumbers
import com.example.possible.repo.remote.RetrofitBuilder
import com.example.possible.repo.remote.response.lettersNumbers.LetterApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class DrawingFragment : Fragment() {
    private lateinit var binding: FragmentDrawingBinding
    private var name = ""
    private var prediction = ""
    private var type = ""
    private var index = -1


    fun getInstance(index: Int, type: String): DrawingFragment {
        val fragment = DrawingFragment()
        val args = Bundle()
        args.putInt("index", index)
        args.putString("type", type)
        fragment.arguments = args
        return fragment
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDrawingBinding.inflate(inflater, container, false)
        type = arguments?.getString("type")!!
        index = arguments?.getInt("index")!!
        setNameOfLetter(type, index)
        binding.letterName.text = name
        return binding.root
    }

    // جعلها suspend حتى تنتظر تنفيذ المهمة وترجع النتيجة بعد الانتهاء
    suspend fun getResult(): Boolean {
        return sendTheImageToApi()
    }

    private fun setNameOfLetter(type: String, index: Int) {
        name = if (type == "letter") {
            LettersAndNumbers.letters[index].name
        } else {
            LettersAndNumbers.numbers[index].name
        }
    }

    private suspend fun sendTheImageToApi(): Boolean {
        return if (type == "letter") {
            sendToLetterPrediction()
        } else {
            sendToNumberPrediction()
        }
    }


    private suspend fun sendToLetterPrediction(): Boolean {
        val image = convertBitmapToMultipartBody(binding.drawer.getBitmap()!!, requireContext())
        val request = RetrofitBuilder.letterApiService.uploadLetterImage(image)
        return if (request.isSuccessful) {
            val responseBody = request.body()
            if (responseBody != null) {
                handleResponse(responseBody)
                withContext(Dispatchers.Main) {
                  //  Toast.makeText(requireContext(), prediction, Toast.LENGTH_SHORT).show()
                }
                name == prediction // إرجاع النتيجة بعد انتهاء المهمة
            } else {
                false
            }
        } else {
            false
        }
    }

    private suspend fun sendToNumberPrediction(): Boolean {
        val image = convertBitmapToMultipartBody(binding.drawer.getBitmap()!!, requireContext())
        val request = RetrofitBuilder.numberApiService.uploadNumberImage(image)
        return if (request.isSuccessful) {
            val responseBody = request.body()
            if (responseBody != null) {
                prediction = responseBody.predicted_class.toString()
                withContext(Dispatchers.Main){
                    //Toast.makeText(requireContext(), prediction, Toast.LENGTH_SHORT).show()
                }
                name == prediction // إرجاع النتيجة بعد انتهاء المهمة
            } else {
                false
            }
        } else {
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleResponse(response: LetterApiResponse) {
        when (response) {
            is LetterApiResponse.Success -> {
                prediction = response.character.toString()
            }
            is LetterApiResponse.Error -> {
                //Toast.makeText(requireContext(), "Prediction Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertBitmapToMultipartBody(bitmap: Bitmap, context: Context): MultipartBody.Part {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.BLACK) // تلوين الخلفية باللون الأسود
        canvas.drawBitmap(bitmap, 0f, 0f, null) // رسم الصورة الأصلية فوق الخلفية السوداء
        val file = File(context.cacheDir, "image.jpg")
        val outputStream = FileOutputStream(file)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, requestBody)
    }
    fun reset(){
        binding.drawer.clearCanvas()
    }
}


