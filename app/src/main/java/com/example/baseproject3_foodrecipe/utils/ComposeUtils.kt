package com.example.baseproject3_foodrecipe.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val rows = mutableListOf<MutableList<Int>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<Int>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        // Measure and place children
        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints)

            // If adding this placeable would exceed the width, start a new row
            if (currentRow.isNotEmpty() && currentRowWidth + placeable.width + horizontalSpacingPx > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)

                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            // Add placeable to current row
            currentRow.add(index)
            currentRowWidth += placeable.width
            if (currentRow.size > 1) {
                currentRowWidth += horizontalSpacingPx
            }
            currentRowHeight = max(currentRowHeight, placeable.height)

            placeable
        }

        // Add the last row if it's not empty
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        // Calculate total height
        val totalHeight = rowHeights.sumOf { it } + (rows.size - 1).coerceAtLeast(0) * verticalSpacingPx

        // Set the size of the layout
        layout(constraints.maxWidth, totalHeight) {
            var y = 0

            // Place the rows
            rows.forEachIndexed { rowIndex, row ->
                val rowWidth = rowWidths[rowIndex]
                val rowHeight = rowHeights[rowIndex]

                // Calculate the starting x position based on the horizontal arrangement
                var x = when (horizontalArrangement) {
                    Arrangement.Start -> 0
                    Arrangement.Center -> (constraints.maxWidth - rowWidth) / 2
                    Arrangement.End -> constraints.maxWidth - rowWidth
                    Arrangement.SpaceEvenly -> 0 // Will be handled differently
                    Arrangement.SpaceBetween -> 0 // Will be handled differently
                    Arrangement.SpaceAround -> 0 // Will be handled differently
                    else -> 0
                }

                // Special handling for space arrangements
                val spaceBetween = if (row.size > 1 && (horizontalArrangement == Arrangement.SpaceEvenly ||
                            horizontalArrangement == Arrangement.SpaceBetween ||
                            horizontalArrangement == Arrangement.SpaceAround)) {
                    val totalWidth = placeables.slice(row).sumOf { it.width }
                    val availableSpace = constraints.maxWidth - totalWidth

                    when (horizontalArrangement) {
                        Arrangement.SpaceEvenly -> availableSpace / (row.size + 1)
                        Arrangement.SpaceBetween -> if (row.size > 1) availableSpace / (row.size - 1) else 0
                        Arrangement.SpaceAround -> availableSpace / (row.size * 2)
                        else -> horizontalSpacingPx
                    }
                } else {
                    horizontalSpacingPx
                }

                // Adjust starting x for SpaceEvenly and SpaceAround
                if (horizontalArrangement == Arrangement.SpaceEvenly) {
                    x = spaceBetween
                } else if (horizontalArrangement == Arrangement.SpaceAround) {
                    x = spaceBetween
                }

                // Place the items in the row
                row.forEachIndexed { index, placeableIndex ->
                    val placeable = placeables[placeableIndex]

                    // Calculate y position based on vertical alignment
                    val itemY = when (verticalAlignment) {
                        Alignment.Top -> y
                        Alignment.CenterVertically -> y + (rowHeight - placeable.height) / 2
                        Alignment.Bottom -> y + rowHeight - placeable.height
                        else -> y
                    }

                    placeable.placeRelative(x, itemY)

                    // Move to the next x position
                    x += placeable.width + spaceBetween
                }

                // Move to the next row
                y += rowHeight + verticalSpacingPx
            }
        }
    }
}

@Composable
fun CenteredRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
fun FixedWidthRow(
    width: Dp,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.width(width),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            content()
        }
    }
}
