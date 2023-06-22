package ai.passio.passiosdksandbox

import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.FoodRecognitionListener
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.fragment.PassioCameraFragment
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import ai.passio.passiosdksandbox.databinding.FragmentRecognizerBinding
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment

class RecognizerFragment : PassioCameraFragment() {

    private var binding: FragmentRecognizerBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecognizerBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        return binding?.root
    }

    override fun getPreviewView(): PreviewView = binding!!.recognizerPreviewView

    override fun onCameraPermissionDenied() {
        Toast.makeText(requireContext(), "PassioSDK can't work without the camera permission", Toast.LENGTH_LONG).show()
    }

    override fun onCameraReady() {
        val detectionConfig = FoodDetectionConfiguration().apply {
            detectVisual = true
            detectBarcodes = true
        }
        println("BCF Camera Ready")
        PassioSDK.instance.startFoodDetection(foodListener, detectionConfig)
    }

    override fun onStop() {
        PassioSDK.instance.stopFoodDetection()
        println("BCF onStop")
        super.onStop()
    }

    private val foodListener = object : FoodRecognitionListener {
        override fun onRecognitionResults(candidates: FoodCandidates, image: Bitmap?, nutritionFacts: PassioNutritionFacts?) {
            val visualCandidates = candidates.detectedCandidates!!
            val barcodeCandidates = candidates.barcodeCandidates!!

            if(visualCandidates.isNotEmpty()) {
                println("BCF Confidence: " + visualCandidates[0].confidence)
            } else {
                println("No Confidence")
            }

            // add check for confidence level here
            if ((visualCandidates.isEmpty() || !checkIfCandidatesMeetConfidenceLevel(candidates)) && barcodeCandidates.isEmpty()) {
                binding?.recognizerFoodResultView?.setSearching()
            } else if (barcodeCandidates.isNotEmpty()) {
                val bestBarcodeResult = barcodeCandidates.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: return
                binding?.recognizerFoodResultView?.setBarcodeResult(bestBarcodeResult.barcode)
            } else {
                val bestVisualCandidate = visualCandidates.maxByOrNull { it.confidence } ?: return
                binding?.recognizerFoodResultView?.setFoodResult(bestVisualCandidate.passioID, bestVisualCandidate)
            }
        }
    }

    private fun checkIfCandidatesMeetConfidenceLevel(
        candidates: FoodCandidates
    ): Boolean {
        val visualCandidates = candidates.detectedCandidates!!
        val confidenceLevel = 0.80

        for(candidate in visualCandidates){
            if(candidate.confidence > confidenceLevel){
                return true
            }
        }
        return false
    }
}