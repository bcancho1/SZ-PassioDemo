package ai.passio.passiosdksandbox

import ai.passio.passiosdk.passiofood.DetectedCandidate
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioIDAttributes
import ai.passio.passiosdksandbox.databinding.FoodResultLayoutBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.lang.Math.round
import java.util.*

class FoodResultView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var currentResult: String? = null
    private var binding: FoodResultLayoutBinding? = null

    init {
        binding = FoodResultLayoutBinding.inflate(LayoutInflater.from(context), this)
        background = ContextCompat.getDrawable(context, R.drawable.rounded_corners_16dp_black_50)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

    fun setFoodResult(passioID: PassioID, bestVisualCandidate: DetectedCandidate) {
        if (passioID == currentResult) {
            return
        }

        val passioIDAttributes = PassioSDK.instance.lookupPassioAttributesFor(passioID) ?: return
        // call api to send detection information here
        renderResult(passioIDAttributes, bestVisualCandidate)
    }

    fun setBarcodeResult(barcode: String) {
        if (barcode == currentResult) {
            return
        }

        PassioSDK.instance.fetchPassioIDAttributesForBarcode(barcode) { passioIDAttributes ->
            if (passioIDAttributes == null) {
                return@fetchPassioIDAttributesForBarcode
            }
            // call api to send detection information here
            renderResult(passioIDAttributes, null)
        }
    }

    private fun renderResult(passioIDAttributes: PassioIDAttributes, detectedCandidate: DetectedCandidate?) {
        currentResult = passioIDAttributes.passioID
        val name = passioIDAttributes.name
        var nutrition: Boolean = false
        if (passioIDAttributes.passioFoodItemData != null) {
            nutrition = true
        }

        if (detectedCandidate != null) {
            binding?.let {
                it.foodResultImage.loadPassioIcon(
                    passioIDAttributes.passioID,
                    passioIDAttributes.entityType
                )
                it.foodResultName.text =
                    name.capitalize(Locale.ROOT) + "\nConfidence: " + String.format(
                        "%.2f",
                        detectedCandidate.confidence
                    ) + "\nNutrition facts:" + nutrition
                it.foodResultProgress.alpha = 0f
                it.foodResultImage.alpha = 1f
            }
        } else {
            binding?.let {
                it.foodResultImage.loadPassioIcon(
                    passioIDAttributes.passioID,
                    passioIDAttributes.entityType
                )
                it.foodResultName.text =
                    name.capitalize(Locale.ROOT) + "\n Barcode"
                it.foodResultProgress.alpha = 0f
                it.foodResultImage.alpha = 1f
            }
        }
    }

    fun setSearching() {
        currentResult = null
        binding?.let {
            it.foodResultName.text = context.getString(R.string.food_searching)
            it.foodResultProgress.alpha = 1f
            it.foodResultImage.alpha = 0f
        }
    }
}