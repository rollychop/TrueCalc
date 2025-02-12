package com.brohit.truecalc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorState
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie


@Composable
fun EMIPieChart(
    state: EmiCalculatorState,
    modifier: Modifier = Modifier
) {
    val totalPaid = state.tp
    val totalInterest = state.ti
    val totalPrincipal = totalPaid - totalInterest
    val ip = (totalInterest / totalPaid * 100).toInt()
    var data by remember(state) {
        mutableStateOf(
            listOf(
                Pie(
                    label = "Interest (${ip}%)",
                    data = totalInterest,
                    color = Color(0xFFFF5722), // Orange
                    selectedColor = Color(0xFFD84315) // Darker Orange
                ),
                Pie(
                    label = "Principal (${100 - ip}%)",
                    data = totalPrincipal,
                    color = Color(0xFF03DAC5), // Teal
                    selectedColor = Color(0xFF018786) // Dark Teal
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Breakdown of EMI",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        PieChart(
            modifier = Modifier.size(200.dp),
            data = data,
            onPieClick = {
                data = data.map { pie ->
                    if (it == pie) {
                        pie.copy(selected = !pie.selected)
                    } else pie.copy(selected = false)
                }
            },
            selectedScale = 1.1f,
            scaleAnimEnterSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            colorAnimEnterSpec = tween(300),
            colorAnimExitSpec = tween(300),
            scaleAnimExitSpec = tween(300),
            spaceDegreeAnimExitSpec = tween(300),
            style = Pie.Style.Stroke()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.forEach { slice ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (slice.selected) slice.selectedColor else slice.color,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(slice.label ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
