package com.brohit.truecalc.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun InvestmentBreakdownChart(
    totalAmount: Double,
    interest: Double,
    principal: Double,
    title: String,
    modifier: Modifier = Modifier
) {
    val data by remember(totalAmount, interest, principal) {
        mutableStateOf(
            listOf(
                Pie(
                    label = "Invested (${(principal / totalAmount * 100).toInt()}%)",
                    data = principal,
                    color = Color(0xFF6200EA), // Purple
                    selectedColor = Color(0xFF3700B3) // Dark Purple
                ),
                Pie(
                    label = "Interest (${(interest / totalAmount * 100).toInt()}%)",
                    data = interest,
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
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        PieChart(
            modifier = Modifier.size(200.dp),
            data = data,
            style = Pie.Style.Stroke(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
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
                        slice.label ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}