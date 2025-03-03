package com.example.snapwitch.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snapwitch.ui.models.FeatureUsageDaily
import com.example.snapwitch.viewmodel.SnapWitchViewModel

@Composable
fun StatisticsScreen(
    snapWitchViewModel: SnapWitchViewModel,
    onBackIconClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val context = LocalContext.current
        val featureMap by snapWitchViewModel.featureUsageMap.collectAsState()

        snapWitchViewModel.getFeatureUsageStats(context)

        val sampleData = listOf(
            FeatureUsageDaily("02/27", "Bluetooth",3),
            FeatureUsageDaily("02/28", "Bluetooth",7),
            FeatureUsageDaily("03/01", "Bluetooth",5),
            FeatureUsageDaily("03/02", "Bluetooth",2),
            FeatureUsageDaily("03/03", "Bluetooth",9),
        )

        val bluetoothUsage = featureMap["Bluetooth"] ?: emptyList()
        val dataUsage = featureMap["Network"] ?: emptyList()

        StatisticsTopBar(
            onBackIconClick = onBackIconClick
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            SnapCharts(featureUsageList = sampleData, featureName = "Sample")
            Spacer(modifier = Modifier.height(15.dp))
            SnapCharts(featureUsageList = bluetoothUsage, featureName = "Bluetooth Scheduler")
            Spacer(modifier = Modifier.height(15.dp))
            SnapCharts(featureUsageList = dataUsage, featureName = "Network Scheduler" )
        }

    }
}

@Preview(widthDp = 320)
@Composable
fun StatisticsTopBar(
    onBackIconClick : () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceTint)
            .padding(15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SnapWitchArrowHead(
            modifier = Modifier.rotate(180f),
            contentColor = MaterialTheme.colorScheme.inversePrimary,
            backgroundColor = MaterialTheme.colorScheme.surfaceTint,
            onClick = onBackIconClick,
            size = 30.dp
        )
        Text(
            text = "Usage Stats",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        SnapWitchStatisticsIcon(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
