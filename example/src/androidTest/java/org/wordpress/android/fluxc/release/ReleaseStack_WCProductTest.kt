package org.wordpress.android.fluxc.release

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wordpress.android.fluxc.TestUtils
import org.wordpress.android.fluxc.action.WCProductAction
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.model.WCProductModel
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.FetchSingleProductPayload
import org.wordpress.android.fluxc.store.WCProductStore.OnProductChanged
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class ReleaseStack_WCProductTest : ReleaseStack_WCBase() {
    internal enum class TestEvent {
        NONE,
        FETCHED_SINGLE_PRODUCT
    }

    @Inject internal lateinit var productStore: WCProductStore

    private var nextEvent: TestEvent = TestEvent.NONE
    private val productModel = WCProductModel(8).apply {
        remoteProductId = 1537
        dateCreated = "2018-04-20T15:45:14Z"
    }
    private var lastEvent: OnProductChanged? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mReleaseStackAppComponent.inject(this)
        init()
        nextEvent = TestEvent.NONE
    }

    @Throws(InterruptedException::class)
    @Test
    fun testFetchSingleProduct() {
        nextEvent = TestEvent.FETCHED_SINGLE_PRODUCT
        mCountDownLatch = CountDownLatch(1)
        mDispatcher.dispatch(
                WCProductActionBuilder
                        .newFetchSingleProductAction(FetchSingleProductPayload(sSite, productModel.remoteProductId))
        )
        assertTrue(mCountDownLatch.await(TestUtils.DEFAULT_TIMEOUT_MS.toLong(), MILLISECONDS))

        // Verify results
        val fetchedProduct = productStore.getSingleProductByRemoteId(sSite, productModel.remoteProductId)
        assertTrue(fetchedProduct != null && fetchedProduct.remoteProductId == productModel.remoteProductId)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductChanged(event: OnProductChanged) {
        event.error?.let {
            when (event.causeOfChange) {
                WCProductAction.FETCH_SINGLE_PRODUCT -> {
                    assertEquals(TestEvent.FETCHED_SINGLE_PRODUCT, nextEvent)
                    mCountDownLatch.countDown()
                    return
                } else -> throw AssertionError("OnProductChanged has unexpected error: " + it.type)
            }
        }

        lastEvent = event

        when (event.causeOfChange) {
            WCProductAction.FETCH_SINGLE_PRODUCT -> {
                assertEquals(TestEvent.FETCHED_SINGLE_PRODUCT, nextEvent)
                mCountDownLatch.countDown()
            }
            else -> throw AssertionError("Unexpected cause of change: " + event.causeOfChange)
        }
    }
}
