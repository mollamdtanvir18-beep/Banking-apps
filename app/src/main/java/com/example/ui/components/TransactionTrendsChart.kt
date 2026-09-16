package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyTrendItem
import com.example.ui.theme.*
import kotlin.math.max

enum class ChartTimeRange(val label: String, val months: Int) {
    LAST_6_MONTHS("6 Months", 6),
    LAST_3_MONTHS("3 Months", 3)
}

enum class ChartVisualizationType(val label: String) {
    BAR_COMPARISON("Bars"),
    TREND_CURVE("Trend")
}

enum class TrendHighlightFilter(val label: String) {
    BOTH("All"),
    DEPOSITS_ONLY("Deposits"),
    WITHDRAWALS_ONLY("Withdrawals")
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TransactionTrendsCard(
    monthlyTrends: List<MonthlyTrendItem>,
    modifier: Modifier = Modifier,
    onViewAllHistory: (() -> Unit)? = null
) {
    var selectedRange by remember { mutableStateOf(ChartTimeRange.LAST_6_MONTHS) }
    var chartType by remember { mutableStateOf(ChartVisualizationType.BAR_COMPARISON) }
    var activeFilter by remember { mutableStateOf(TrendHighlightFilter.BOTH) }

    // Filter displayed months according to selected range
    val displayedTrends = remember(monthlyTrends, selectedRange) {
        if (monthlyTrends.isEmpty()) {
            emptyList()
        } else {
            monthlyTrends.takeLast(selectedRange.months)
        }
    }

    // Default to the latest month selected
    var selectedMonthIndex by remember(displayedTrends) {
        mutableStateOf(if (displayedTrends.isNotEmpty()) displayedTrends.size - 1 else 0)
    }

    // Keep index within bounds if range changes
    LaunchedEffect(displayedTrends.size) {
        if (displayedTrends.isNotEmpty() && selectedMonthIndex >= displayedTrends.size) {
            selectedMonthIndex = displayedTrends.size - 1
        }
    }

    val selectedMonth = displayedTrends.getOrNull(selectedMonthIndex)

    // Calculate aggregate totals for the active range
    val totalDepositsInRange = remember(displayedTrends) {
        displayedTrends.sumOf { it.totalDeposits }
    }
    val totalWithdrawalsInRange = remember(displayedTrends) {
        displayedTrends.sumOf { it.totalWithdrawals }
    }
    val netCashflowInRange = totalDepositsInRange - totalWithdrawalsInRange
    val totalVolume = totalDepositsInRange + totalWithdrawalsInRange
    val depositRatio = if (totalVolume > 0) (totalDepositsInRange / totalVolume).toFloat() else 0.5f

    // Smooth animation on data/type switch
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(chartType, selectedRange) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 1. Header: Title & View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertChartOutlined,
                            contentDescription = "Trends",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Monthly Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Deposits vs. Withdrawals",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    }
                }

                // Chart Style Switcher (Bars / Spline curve)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ChartVisualizationType.values().forEach { type ->
                            val isSelected = chartType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PureWhite else Color.Transparent)
                                    .clickable { chartType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (type == ChartVisualizationType.BAR_COMPARISON) Icons.Default.BarChart else Icons.Default.ShowChart,
                                        contentDescription = type.label,
                                        tint = if (isSelected) EmeraldPrimary else Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = type.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) EmeraldDark else Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. High-Level Summary Stats (Deposits vs Withdrawals Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deposit Summary Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            activeFilter = if (activeFilter == TrendHighlightFilter.DEPOSITS_ONLY) {
                                TrendHighlightFilter.BOTH
                            } else {
                                TrendHighlightFilter.DEPOSITS_ONLY
                            }
                        },
                    color = if (activeFilter == TrendHighlightFilter.DEPOSITS_ONLY) EmeraldContainer else Slate50,
                    shape = RoundedCornerShape(16.dp),
                    border = if (activeFilter == TrendHighlightFilter.DEPOSITS_ONLY) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary)
                    } else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Text(
                                text = "Total Deposits",
                                fontSize = 11.sp,
                                color = Slate600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "৳${String.format("%,.0f", totalDepositsInRange)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        Text(
                            text = "+${(depositRatio * 100).toInt()}% of flow",
                            fontSize = 10.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Withdrawal Summary Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            activeFilter = if (activeFilter == TrendHighlightFilter.WITHDRAWALS_ONLY) {
                                TrendHighlightFilter.BOTH
                            } else {
                                TrendHighlightFilter.WITHDRAWALS_ONLY
                            }
                        },
                    color = if (activeFilter == TrendHighlightFilter.WITHDRAWALS_ONLY) Color(0xFFFEF2F2) else Slate50,
                    shape = RoundedCornerShape(16.dp),
                    border = if (activeFilter == TrendHighlightFilter.WITHDRAWALS_ONLY) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRed)
                    } else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                            )
                            Text(
                                text = "Total Withdrawals",
                                fontSize = 11.sp,
                                color = Slate600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "৳${String.format("%,.0f", totalWithdrawalsInRange)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Text(
                            text = "-${((1f - depositRatio) * 100).toInt()}% of flow",
                            fontSize = 10.sp,
                            color = ErrorRed.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual-tone Ratio Track (Visual representation of deposits vs withdrawals)
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Slate200)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(depositRatio.coerceIn(0.05f, 0.95f))
                                .fillMaxHeight()
                                .background(EmeraldPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - depositRatio).coerceIn(0.05f, 0.95f))
                                .fillMaxHeight()
                                .background(ErrorRed)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (netCashflowInRange >= 0) "Net Surplus: +৳${String.format("%,.0f", netCashflowInRange)}" else "Net Deficit: -৳${String.format("%,.0f", -netCashflowInRange)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netCashflowInRange >= 0) EmeraldDark else ErrorRed
                    )
                    Text(
                        text = "Tap bars to inspect",
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Time Frame & Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Range tabs: 6 Months vs 3 Months
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartTimeRange.values().forEach { range ->
                        val isSelected = selectedRange == range
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRange = range },
                            label = { Text(range.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = EmeraldPrimary,
                                borderColor = Slate200
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Quick Legend Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldPrimary))
                        Text("Deposits", fontSize = 10.sp, color = Slate600)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ErrorRed))
                        Text("Withdrawals", fontSize = 10.sp, color = Slate600)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Lightweight Chart Canvas (Grouped Bars or Area Splines)
            if (displayedTrends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transaction trends available for this period.", color = Slate400, fontSize = 13.sp)
                }
            } else {
                val density = LocalDensity.current
                val textMeasurer = rememberTextMeasurer()

                // Calculate max height for normalization
                val maxVal = remember(displayedTrends) {
                    val highest = displayedTrends.maxOfOrNull { max(it.totalDeposits, it.totalWithdrawals) } ?: 10000.0
                    if (highest <= 0.0) 10000.0 else highest * 1.15 // 15% headroom
                }

                val emeraldColor = EmeraldPrimary
                val emeraldLight = EmeraldLight
                val errorColor = ErrorRed
                val errorLight = Color(0xFFFB7185)
                val gridColor = Slate200
                val labelColor = Slate500
                val activeMonthColor = Slate900

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(displayedTrends) {
                                detectTapGestures { offset ->
                                    val count = displayedTrends.size
                                    if (count > 0) {
                                        val leftPadding = 38.dp.toPx()
                                        val chartAreaWidth = size.width - leftPadding
                                        val relativeX = (offset.x - leftPadding).coerceAtLeast(0f)
                                        val slotWidth = chartAreaWidth / count
                                        val clickedIndex = (relativeX / slotWidth).toInt().coerceIn(0, count - 1)
                                        selectedMonthIndex = clickedIndex
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val leftAxisPadding = 38.dp.toPx()
                        val bottomAxisPadding = 26.dp.toPx()
                        val chartWidth = width - leftAxisPadding
                        val chartHeight = height - bottomAxisPadding
                        val progress = animationProgress.value

                        // 4.1 Draw Horizontal Reference Gridlines (0%, 33%, 66%, 100%)
                        val gridLevels = 4
                        for (i in 0..gridLevels) {
                            val ratio = i.toFloat() / gridLevels
                            val y = chartHeight - (ratio * chartHeight)
                            val valueAtLevel = maxVal * ratio

                            // Gridline
                            drawLine(
                                color = gridColor.copy(alpha = 0.7f),
                                start = Offset(leftAxisPadding, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )

                            // Y-axis label text
                            val formattedLabel = if (valueAtLevel >= 1000) {
                                "${(valueAtLevel / 1000).toInt()}k"
                            } else {
                                valueAtLevel.toInt().toString()
                            }
                            val textLayoutResult = textMeasurer.measure(
                                text = AnnotatedString("৳$formattedLabel"),
                                style = TextStyle(fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Normal)
                            )
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(0f, y - textLayoutResult.size.height / 2)
                            )
                        }

                        val count = displayedTrends.size
                        val slotWidth = chartWidth / count

                        // 4.2 Grouped Bars Mode
                        if (chartType == ChartVisualizationType.BAR_COMPARISON) {
                            displayedTrends.forEachIndexed { index, item ->
                                val slotX = leftAxisPadding + (index * slotWidth)
                                val slotCenter = slotX + (slotWidth / 2)
                                val isSelected = index == selectedMonthIndex

                                // Draw selected month background column highlight
                                if (isSelected) {
                                    drawRoundRect(
                                        color = EmeraldContainer.copy(alpha = 0.65f),
                                        topLeft = Offset(slotX + 4.dp.toPx(), 0f),
                                        size = Size(slotWidth - 8.dp.toPx(), chartHeight),
                                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                    )
                                }

                                val barSpacing = 4.dp.toPx()
                                val barWidth = (slotWidth * 0.28f).coerceIn(10.dp.toPx(), 22.dp.toPx())

                                // Alpha based on active filter
                                val depAlpha = if (activeFilter == TrendHighlightFilter.WITHDRAWALS_ONLY) 0.2f else 1.0f
                                val withAlpha = if (activeFilter == TrendHighlightFilter.DEPOSITS_ONLY) 0.2f else 1.0f

                                // Deposit Bar (Left)
                                val depHeight = ((item.totalDeposits / maxVal) * chartHeight * progress).toFloat()
                                val depTop = chartHeight - depHeight
                                val depLeft = slotCenter - barWidth - (barSpacing / 2)

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            emeraldLight.copy(alpha = depAlpha),
                                            emeraldColor.copy(alpha = depAlpha)
                                        ),
                                        startY = depTop,
                                        endY = chartHeight
                                    ),
                                    topLeft = Offset(depLeft, depTop),
                                    size = Size(barWidth, depHeight.coerceAtLeast(2.dp.toPx())),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                // Withdrawal Bar (Right)
                                val withHeight = ((item.totalWithdrawals / maxVal) * chartHeight * progress).toFloat()
                                val withTop = chartHeight - withHeight
                                val withLeft = slotCenter + (barSpacing / 2)

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            errorLight.copy(alpha = withAlpha),
                                            errorColor.copy(alpha = withAlpha)
                                        ),
                                        startY = withTop,
                                        endY = chartHeight
                                    ),
                                    topLeft = Offset(withLeft, withTop),
                                    size = Size(barWidth, withHeight.coerceAtLeast(2.dp.toPx())),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                // Month Label at bottom
                                val monthLayout = textMeasurer.measure(
                                    text = AnnotatedString(item.monthLabel),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) EmeraldDark else labelColor
                                    )
                                )
                                drawText(
                                    textLayoutResult = monthLayout,
                                    topLeft = Offset(slotCenter - (monthLayout.size.width / 2), height - bottomAxisPadding + 6.dp.toPx())
                                )

                                // Selected indicator pin
                                if (isSelected) {
                                    drawCircle(
                                        color = EmeraldPrimary,
                                        radius = 2.5.dp.toPx(),
                                        center = Offset(slotCenter, height - 3.dp.toPx())
                                    )
                                }
                            }
                        } else {
                            // 4.3 Trend Spline Curve Mode (Smooth Area Chart)
                            val depositPoints = mutableListOf<Offset>()
                            val withdrawalPoints = mutableListOf<Offset>()

                            displayedTrends.forEachIndexed { index, item ->
                                val slotX = leftAxisPadding + (index * slotWidth)
                                val slotCenter = slotX + (slotWidth / 2)
                                val isSelected = index == selectedMonthIndex

                                val depY = (chartHeight - ((item.totalDeposits / maxVal) * chartHeight * progress)).toFloat()
                                val withY = (chartHeight - ((item.totalWithdrawals / maxVal) * chartHeight * progress)).toFloat()

                                depositPoints.add(Offset(slotCenter, depY))
                                withdrawalPoints.add(Offset(slotCenter, withY))

                                // Month label
                                val monthLayout = textMeasurer.measure(
                                    text = AnnotatedString(item.monthLabel),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) EmeraldDark else labelColor
                                    )
                                )
                                drawText(
                                    textLayoutResult = monthLayout,
                                    topLeft = Offset(slotCenter - (monthLayout.size.width / 2), height - bottomAxisPadding + 6.dp.toPx())
                                )
                            }

                            // Selected month scrubber line
                            if (selectedMonthIndex in displayedTrends.indices) {
                                val selectedSlotCenter = leftAxisPadding + (selectedMonthIndex * slotWidth) + (slotWidth / 2)
                                drawLine(
                                    color = EmeraldPrimary.copy(alpha = 0.5f),
                                    start = Offset(selectedSlotCenter, 0f),
                                    end = Offset(selectedSlotCenter, chartHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            }

                            // Helper function to build cubic spline curve
                            fun buildSplinePath(points: List<Offset>, isArea: Boolean, baseY: Float): Path {
                                val path = Path()
                                if (points.isEmpty()) return path

                                path.moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val cx = (p0.x + p1.x) / 2
                                    path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }

                                if (isArea) {
                                    path.lineTo(points.last().x, baseY)
                                    path.lineTo(points.first().x, baseY)
                                    path.close()
                                }
                                return path
                            }

                            // Draw Deposit Area and Line
                            if (activeFilter != TrendHighlightFilter.WITHDRAWALS_ONLY && depositPoints.isNotEmpty()) {
                                val depArea = buildSplinePath(depositPoints, true, chartHeight)
                                drawPath(
                                    path = depArea,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            emeraldColor.copy(alpha = 0.28f * progress),
                                            emeraldColor.copy(alpha = 0.02f)
                                        ),
                                        startY = 0f,
                                        endY = chartHeight
                                    )
                                )

                                val depStroke = buildSplinePath(depositPoints, false, chartHeight)
                                drawPath(
                                    path = depStroke,
                                    color = emeraldColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                depositPoints.forEachIndexed { i, pt ->
                                    val isSelected = i == selectedMonthIndex
                                    drawCircle(
                                        color = PureWhite,
                                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = emeraldColor,
                                        radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                                        center = pt
                                    )
                                    if (isSelected) {
                                        drawCircle(
                                            color = emeraldColor.copy(alpha = 0.35f),
                                            radius = 8.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            // Draw Withdrawal Area and Line
                            if (activeFilter != TrendHighlightFilter.DEPOSITS_ONLY && withdrawalPoints.isNotEmpty()) {
                                val withArea = buildSplinePath(withdrawalPoints, true, chartHeight)
                                drawPath(
                                    path = withArea,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            errorColor.copy(alpha = 0.22f * progress),
                                            errorColor.copy(alpha = 0.02f)
                                        ),
                                        startY = 0f,
                                        endY = chartHeight
                                    )
                                )

                                val withStroke = buildSplinePath(withdrawalPoints, false, chartHeight)
                                drawPath(
                                    path = withStroke,
                                    color = errorColor,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                withdrawalPoints.forEachIndexed { i, pt ->
                                    val isSelected = i == selectedMonthIndex
                                    drawCircle(
                                        color = PureWhite,
                                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = errorColor,
                                        radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Interactive Inspection Tooltip / Card for Selected Month
            selectedMonth?.let { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = item.fullMonthName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Slate800
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (item.netCashflow >= 0) EmeraldContainer else Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = if (item.netCashflow >= 0) "Net +৳${String.format("%,.0f", item.netCashflow)}" else "Net -৳${String.format("%,.0f", -item.netCashflow)}",
                                    color = if (item.netCashflow >= 0) EmeraldDark else ErrorRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Deposits column
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                )
                                Column {
                                    Text(text = "Deposited", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = "৳${String.format("%,.2f", item.totalDeposits)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldDark
                                    )
                                }
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(Slate200)
                            )

                            // Withdrawals column
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                )
                                Column {
                                    Text(text = "Withdrawn", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = "৳${String.format("%,.2f", item.totalWithdrawals)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional "View Full Ledger" link if provided
            if (onViewAllHistory != null) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onViewAllHistory,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View Detailed History →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}
