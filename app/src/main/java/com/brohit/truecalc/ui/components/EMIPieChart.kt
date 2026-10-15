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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.screens.emi.EmiCalculatorState
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie


@Composable
fun EMIPieChart(
    state: EmiCalculatorState,
    modifier: Modifier = Modifier,
    title: String? = "Breakdown of EMI",
) {
    val totalPaid = state.tp
    val totalInterest = state.ti
    val totalPrincipal = totalPaid - totalInterest
    val ip = (totalInterest / totalPaid * 100).toInt()

    val interestColor = MaterialTheme.colorScheme.error
    val principalColor = MaterialTheme.colorScheme.primary

    var data by remember(state, interestColor, principalColor) {
        mutableStateOf(
            listOf(
                Pie(
                    label = "Interest (${ip}%)",
                    data = totalInterest,
                    color = interestColor,
                    selectedColor = interestColor.copy(alpha = 0.8f)
                ),
                Pie(
                    label = "Principal (${100 - ip}%)",
                    data = totalPrincipal,
                    color = principalColor,
                    selectedColor = principalColor.copy(alpha = 0.8f)
                )
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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
            style = Pie.Style.Stroke(width = 40.dp)
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
                    Text(
                        text = slice.label ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
