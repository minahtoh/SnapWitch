package com.example.snapwitch.ui.presentation

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snapwitch.ui.models.FeatureUsageDaily
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shape.chartShape
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SnapCharts(
    featureUsageList: List<FeatureUsageDaily>,
    featureName:String
) {
    if (featureUsageList.isEmpty()) {
        Text(
            text = "No data available",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // Extract X-axis labels (dates) and Y-axis values (usage count)
    val labels = featureUsageList.map { it.day }
    val values = featureUsageList.map { it.count.toFloat() }



    val columnStyle = LineComponent(
        color = MaterialTheme.colorScheme.onSecondary.toArgb(),
        thicknessDp = 15f, // Column width
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp).chartShape()
    )



    // Create an Animatable for each column
    val animatables = remember {
        List(featureUsageList.size) { Animatable(0f) }
    }

    // Start sequential animation
    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, animatable ->
            // Start each animation after a delay
            launch {
                delay(index * 500L) // 200ms delay between columns
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    // Calculate current values based on animation progress
    val currentValues = featureUsageList.mapIndexed { index, value ->
        value.count * animatables[index].value
    }

    // Create the entry model
    val columnData = remember(currentValues) {
        entryModelOf(
            currentValues.mapIndexed { index, value ->
                FloatEntry(index.toFloat(), value)
            }
        )
    }



   // val columnChartEntryModel = entryModelOf(*animatedValues.toTypedArray())


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = "$featureName Usage Over Time",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, top = 8.dp)
        )

        Chart(
            chart = columnChart(
                columns = listOf(columnStyle)
            ),
            model = columnData,
            startAxis = startAxis(
                valueFormatter = { value, _ -> value.toInt().toString() }, // Format Y-axis as whole numbers

            ),
            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    val index = value.toInt().coerceIn(0, featureUsageList.size - 1)
                    featureUsageList.getOrNull(index)?.day ?: ""
                },
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth().graphicsLayer { alpha = 1f }
        )

        // Debugging - Show raw data
        featureUsageList.forEach {
            Text(text = "${it.day}: ${it.count}", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(15.dp))
        }
    }
}