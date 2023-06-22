package ai.passio.passiosdksandbox

import ai.passio.passiosdk.core.icons.IconSize
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.util.Log
import android.widget.ImageView

fun ImageView.loadPassioIcon(
    passioID: PassioID,
    type: PassioIDEntityType = PassioIDEntityType.item,
    iconSize: IconSize = IconSize.PX90
) {
    this.tag = passioID
    val localImageResult = PassioSDK.instance.lookupIconFor(context, passioID, iconSize, type)
    setImageDrawable(localImageResult.first)

    if (localImageResult.second) {
        return
    }

    PassioSDK.instance.fetchIconFor(context, passioID, iconSize) { drawable ->
        if (drawable != null && this.tag == passioID) {
            setImageDrawable(drawable)
        }
    }
}