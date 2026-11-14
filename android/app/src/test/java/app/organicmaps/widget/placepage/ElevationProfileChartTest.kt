package app.organicmaps.widget.placepage

import android.app.Application
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ElevationProfileChartTest {
    private val finished = mutableListOf<Float>()
    private lateinit var chart: ElevationProfileChart

    @Before
    fun showChart() {
        val context = RuntimeEnvironment.getApplication()
        chart = ElevationProfileChart(context)
        chart.data = LineData(LineDataSet(listOf(Entry(0f, 0f), Entry(100f, 10f)), "profile"))
        // The chart asks its parent to keep the gesture.
        FrameLayout(context).addView(chart)
        chart.measure(exactly(WIDTH), exactly(HEIGHT))
        chart.layout(0, 0, WIDTH, HEIGHT)
        chart.highlightValue(Highlight(START_X, 0f, 0), false)
        chart.setOnSelectionFinishedListener { finished.add(it) }
    }

    @Test
    fun cancelledDragRestoresItsStartWithoutReporting() {
        drag(MotionEvent.ACTION_CANCEL)
        assertEquals(START_X, chart.highlightX, 0f)
        assertEquals(emptyList<Float>(), finished)
        assertFalse(chart.isSelectionInProgress)
    }

    @Test
    fun releasedDragReportsItsNewSelectionOnce() {
        drag(MotionEvent.ACTION_UP)
        assertNotEquals(START_X, chart.highlightX, 0f)
        assertEquals(listOf(chart.highlightX), finished)
        assertFalse(chart.isSelectionInProgress)
    }

    private fun drag(lastAction: Int) {
        val y = HEIGHT / 2f
        touch(MotionEvent.ACTION_DOWN, WIDTH * 0.25f, y)
        touch(MotionEvent.ACTION_MOVE, WIDTH * 0.75f, y)
        assertTrue(chart.isSelectionInProgress)
        assertNotEquals(START_X, chart.highlightX, 0f)
        touch(lastAction, WIDTH * 0.75f, y)
    }

    private fun touch(action: Int, x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(time, time, action, x, y, 0)
        chart.onTouchEvent(event)
        event.recycle()
    }

    private fun exactly(size: Int) = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

    private companion object {
        const val WIDTH = 1000
        const val HEIGHT = 300
        const val START_X = 25f
    }
}
