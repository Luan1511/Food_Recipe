package com.example.baseproject3_foodrecipe.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

class FoodRecognitionHelper(private val context: Context) {
    private var tfliteModel: MappedByteBuffer? = null
    private var tflite: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var isInitialized = false

    // Kích thước đầu vào của mô hình - từ thông tin model
    private val inputWidth = 640
    private val inputHeight = 640
    private val inputChannels = 3

    // Thông tin đầu ra của mô hình
    private val outputBoxes = 25200  // Số lượng box dự đoán
    private val numClasses = 6       // Số lượng lớp (từ output shape [1, 25200, 11])

    companion object {
        private const val TAG = "FoodRecognitionHelper"
        private const val MODEL_PATH = "food_recognition_model.tflite"
        private const val LABEL_PATH = "food_labels.txt"
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val IOU_THRESHOLD = 0.5f
        private const val MAX_DETECTIONS = 10
    }

    init {
        try {
            initializeModel()
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing TFLite model", e)
        }
    }

    @Throws(IOException::class)
    private fun initializeModel() {
        try {
            // Tải mô hình
            tfliteModel = FileUtil.loadMappedFile(context, MODEL_PATH)

            // Cấu hình Interpreter với các tùy chọn
            val options = Interpreter.Options().apply {
                setNumThreads(4) // Sử dụng 4 thread để tăng hiệu suất
            }

            tflite = Interpreter(tfliteModel!!, options)

            // Tải nhãn
            loadLabels()

            // Kiểm tra kích thước đầu vào và đầu ra của mô hình
            val inputTensor = tflite!!.getInputTensor(0)
            val outputTensor = tflite!!.getOutputTensor(0)

            Log.d(TAG, "Model input shape: ${inputTensor.shape().joinToString()}")
            Log.d(TAG, "Model output shape: ${outputTensor.shape().joinToString()}")

            isInitialized = true
            Log.d(TAG, "Model initialized successfully with ${labels.size} labels")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model or labels", e)
            throw IOException("Error loading model or labels", e)
        }
    }

    private fun loadLabels() {
        try {
            val labelsInput = context.assets.open(LABEL_PATH)
            val reader = BufferedReader(InputStreamReader(labelsInput))
            labels = reader.readLines()
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels", e)
            // Tạo một số nhãn mặc định nếu không thể tải file
            labels = listOf(
                "apple", "banana", "bread", "broccoli", "burger", "cake"
            )
        }
    }

    fun detectFood(bitmap: Bitmap): List<Detection> {
        if (!isInitialized) {
            Log.e(TAG, "TFLite interpreter not initialized")
            return emptyList()
        }

        try {
            // Chuẩn bị ảnh đầu vào
            val inputBuffer = prepareInputImage(bitmap)

            // Chuẩn bị buffer đầu ra
            val outputBuffer = Array(1) {
                Array(outputBoxes) {
                    FloatArray(numClasses + 5) // 4 box coords + 1 objectness score + numClasses
                }
            }

            // Chạy suy luận
            tflite!!.run(inputBuffer, outputBuffer)

            // Xử lý kết quả
            return processDetections(outputBuffer[0], bitmap.width, bitmap.height)

        } catch (e: Exception) {
            Log.e(TAG, "Error during inference", e)
            return emptyList()
        }
    }

    private fun prepareInputImage(bitmap: Bitmap): ByteBuffer {
        // Resize bitmap để khớp với kích thước đầu vào của mô hình
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        // Tạo buffer với kích thước chính xác
        val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * inputChannels * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Mảng để lưu trữ giá trị pixel
        val intValues = IntArray(inputWidth * inputHeight)

        // Lấy tất cả các pixel từ bitmap
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0,
            scaledBitmap.width, scaledBitmap.height)

        // Chuyển đổi mỗi pixel thành giá trị float và thêm vào buffer
        for (i in 0 until inputWidth * inputHeight) {
            val pixel = intValues[i]

            // Trích xuất các kênh màu (RGB)
            val r = (pixel shr 16 and 0xFF)
            val g = (pixel shr 8 and 0xFF)
            val b = (pixel and 0xFF)

            // Chuẩn hóa giá trị pixel về khoảng [0, 1]
            byteBuffer.putFloat(r / 255.0f)
            byteBuffer.putFloat(g / 255.0f)
            byteBuffer.putFloat(b / 255.0f)
        }

        return byteBuffer
    }

    private fun processDetections(outputBoxes: Array<FloatArray>, imageWidth: Int, imageHeight: Int): List<Detection> {
        val detections = mutableListOf<Detection>()

        // Xử lý từng box dự đoán
        for (i in 0 until outputBoxes.size) {
            val confidence = outputBoxes[i][4] // objectness score

            // Lọc các box có độ tin cậy thấp
            if (confidence < CONFIDENCE_THRESHOLD) continue

            // Tìm lớp có xác suất cao nhất
            var maxClassScore = 0f
            var detectedClassIndex = -1

            for (c in 0 until numClasses) {
                val score = outputBoxes[i][5 + c]
                if (score > maxClassScore) {
                    maxClassScore = score
                    detectedClassIndex = c
                }
            }

            // Tính điểm cuối cùng
            val classConfidence = maxClassScore * confidence

            // Lọc theo ngưỡng tin cậy
            if (classConfidence < CONFIDENCE_THRESHOLD) continue

            // Lấy tọa độ box
            val x = outputBoxes[i][0]
            val y = outputBoxes[i][1]
            val w = outputBoxes[i][2]
            val h = outputBoxes[i][3]

            // Chuyển đổi từ tọa độ YOLO sang tọa độ thực tế
            val left = (x - w / 2) * imageWidth
            val top = (y - h / 2) * imageHeight
            val right = (x + w / 2) * imageWidth
            val bottom = (y + h / 2) * imageHeight

            // Tạo đối tượng Detection
            val detection = Detection(
                label = if (detectedClassIndex >= 0 && detectedClassIndex < labels.size)
                    labels[detectedClassIndex] else "unknown",
                confidence = classConfidence,
                boundingBox = RectF(left, top, right, bottom)
            )

            detections.add(detection)
        }

        // Áp dụng non-maximum suppression để loại bỏ các box trùng lặp
        return nms(detections)
    }

    private fun nms(detections: List<Detection>): List<Detection> {
        // Sắp xếp các detection theo độ tin cậy giảm dần
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selectedDetections = mutableListOf<Detection>()

        // Mảng đánh dấu các detection đã bị loại bỏ
        val isSuppress = BooleanArray(sortedDetections.size)

        for (i in sortedDetections.indices) {
            // Nếu detection này đã bị loại bỏ, bỏ qua
            if (isSuppress[i]) continue

            // Thêm detection này vào danh sách kết quả
            selectedDetections.add(sortedDetections[i])

            // Nếu đã đủ số lượng detection tối đa, dừng lại
            if (selectedDetections.size >= MAX_DETECTIONS) break

            // Loại bỏ các detection khác có IoU cao với detection này
            for (j in i + 1 until sortedDetections.size) {
                if (isSuppress[j]) continue

                val iou = calculateIoU(sortedDetections[i].boundingBox, sortedDetections[j].boundingBox)
                if (iou > IOU_THRESHOLD) {
                    isSuppress[j] = true
                }
            }
        }

        return selectedDetections
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val areaBox1 = (box1.right - box1.left) * (box1.bottom - box1.top)
        val areaBox2 = (box2.right - box2.left) * (box2.bottom - box2.top)

        if (areaBox1 <= 0 || areaBox2 <= 0) return 0f

        val intersectLeft = max(box1.left, box2.left)
        val intersectTop = max(box1.top, box2.top)
        val intersectRight = min(box1.right, box2.right)
        val intersectBottom = min(box1.bottom, box2.bottom)

        val intersectArea = max(0f, intersectRight - intersectLeft) *
                max(0f, intersectBottom - intersectTop)

        return intersectArea / (areaBox1 + areaBox2 - intersectArea)
    }

    fun close() {
        tflite?.close()
        tfliteModel = null
        isInitialized = false
    }

    data class Detection(
        val label: String,
        val confidence: Float,
        val boundingBox: RectF
    ) {
        // Định dạng nhãn để hiển thị
        val displayName: String
            get() = label.split("_").joinToString(" ") { it.capitalize() }

        // Định dạng độ tin cậy dưới dạng phần trăm
        val confidencePercentage: String
            get() = String.format("%.1f%%", confidence * 100)
    }
}

// Extension function để viết hoa chữ cái đầu
private fun String.capitalize(): String {
    return if (isNotEmpty()) this[0].uppercase() + substring(1) else this
}
