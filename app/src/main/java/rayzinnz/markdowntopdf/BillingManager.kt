package rayzinnz.markdowntopdf

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val activity: Activity) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val productList = listOf(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId("coffee_tip_3")
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
    )

    private var coffeeProductDetails: ProductDetails? = null

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing setup finished")
                    queryProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("BillingManager", "Billing service disconnected")
            }
        })
    }

    private fun queryProducts() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                coffeeProductDetails = productDetailsList.find { it.productId == "coffee_tip_3" }
                if (coffeeProductDetails != null) {
                    _isReady.value = true
                    Log.d("BillingManager", "Coffee product details found: ${coffeeProductDetails?.name}")
                }
            }
        }
    }

    fun launchPurchaseFlow() {
        val details = coffeeProductDetails ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingManager", "User canceled the purchase")
        } else {
            Log.e("BillingManager", "Error updating purchases: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingManager", "Purchase acknowledged")
                    }
                }
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
