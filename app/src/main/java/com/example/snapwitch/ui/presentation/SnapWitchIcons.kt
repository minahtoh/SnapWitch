package com.example.snapwitch.ui.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview()
@Composable
fun SnapWitchArrowHead(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary,
    onClick: () -> Unit = {},
    clickable: Boolean = true
){

    Box(
        modifier = modifier
            .size(size)
            .background(color = backgroundColor)
            .clip(CircleShape)
            .clickable {
               if (clickable) onClick()
            }
            .drawBehind {
                val centerX = size.toPx() / 2
                val centerY = size.toPx() / 2
                val lineWidth = size.toPx() / 4
                drawCircle(
                    color = contentColor,
                    radius = size.toPx() / 2,
                    style = Stroke(
                        width = 20f,
                        )
                )
                drawLine(
                    start = Offset(centerX - lineWidth, centerY),
                    end = Offset(centerX + lineWidth, centerY),
                    strokeWidth = 4.dp.toPx(),
                    color = contentColor,
                    cap = StrokeCap.Round
                )
                drawLine(
                    start = Offset(centerX, centerY - lineWidth),
                    end = Offset(centerX + lineWidth, centerY),
                    strokeWidth = 4.dp.toPx(),
                    color = contentColor,
                    cap = StrokeCap.Round
                )
                drawLine(
                    start = Offset(centerX, centerY + lineWidth),
                    end = Offset(centerX + lineWidth, centerY),
                    strokeWidth = 4.dp.toPx(),
                    color = contentColor,
                    cap = StrokeCap.Round
                )
            },
        contentAlignment = Alignment.Center,

    ) {
    }
}

@Preview
@Composable
fun SnapWitchFailIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size/4
    val strokeWidth = size/8

    Canvas(modifier = modifier
        .size(size + 10.dp)
        .background(color = backgroundColor)
    ){
        drawCircle(
            radius = size.toPx() / 2,
            style = Stroke(
                width = strokeWidth.toPx()
            ),
            color = contentColor
        )
        drawLine(
            start = Offset(center.x - lineWidth.toPx(),center.y + lineWidth.toPx()),
            end = Offset(center.x + lineWidth.toPx(), center.y - lineWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            start = Offset(center.x + lineWidth.toPx(),center.y + lineWidth.toPx()),
            end = Offset(center.x - lineWidth.toPx(), center.y - lineWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
    }
}

@Preview
@Composable
fun SnapWitchSuccessIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {},
    clickable : Boolean = true,
){
    val lineWidth = size/4
    val strokeWidth = size/8
    var isGlowing by remember { mutableStateOf(false) }

    // Animate glow when isGlowing is true
    val glowAlpha by animateFloatAsState(
        targetValue = if (isGlowing) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "glow"
    )


    Canvas(modifier = Modifier
        .size(size + 10.dp)
        .background(color = backgroundColor)
        .clip(CircleShape)
        .clickable {
            if (clickable){
                onClick()
                isGlowing = true
                // Stop the glow after 2 seconds
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    isGlowing = false
                }
            }

        }
    ){
        val glowColor = Color.Cyan.copy(alpha = glowAlpha)

        if (isGlowing){
            drawCircle(
                radius = size.toPx() / 2,
                style = Stroke(
                    width = strokeWidth.toPx()
                ),
                color = glowColor,
                alpha = glowAlpha
            )
        } else{
            drawCircle(
                radius = size.toPx() / 2,
                style = Stroke(
                    width = strokeWidth.toPx()
                ),
                color = contentColor,
            )
        }



        drawLine(
            start = Offset(center.x - (lineWidth/2).toPx(),center.y + lineWidth.toPx()),
            end = Offset(center.x - lineWidth.toPx(), center.y),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            end = Offset(center.x - (lineWidth/2).toPx(),center.y + lineWidth.toPx()),
            start = Offset(center.x + (lineWidth).toPx(), center.y - (lineWidth/2).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )

    }
}


@Preview
@Composable
fun SnapWitchAddIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size/3
    val strokeWidth = size/8

    Canvas(
        modifier = Modifier
            .size(size)
            .background(backgroundColor)) {

        drawRoundRect(
            topLeft = Offset(0f ,  0f),
            size = Size(size.toPx(), size.toPx()) ,
            color = contentColor,
            style = Stroke(
                width = strokeWidth.toPx()
            ),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawLine(
            start = Offset(center.x - lineWidth.toPx(),center.y),
            end = Offset(center.x + lineWidth.toPx(), center.y),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            start = Offset(center.x ,center.y - lineWidth.toPx()),
            end = Offset(center.x , center.y + lineWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
    }
}

@Preview
@Composable
fun SnapWitchListIcon(
    modifier: Modifier = Modifier,
    onClick : ()-> Unit = {},
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size - 30.dp
    val strokeWidth = size/16
    Canvas(modifier = modifier
        .size(size)
        .clip(CircleShape)
        .clickable {
            onClick()
        }
        //.padding(10.dp)
    ){
        drawLine(
            start = Offset(center.x - lineWidth.toPx(), center.y + lineWidth.toPx()),
            end = Offset(center.x - lineWidth.toPx(), center.y - lineWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round

        )
        drawLine(
            start = Offset(center.x + lineWidth.toPx(), center.y + lineWidth.toPx()),
            end = Offset(center.x - lineWidth.toPx(), center.y + lineWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(center.x + ((lineWidth/3) * 2).toPx(), center.y + (lineWidth/2).toPx()),
            end = Offset(center.x - (lineWidth/2).toPx(), center.y + (lineWidth/2).toPx() ),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            start = Offset(center.x + (lineWidth/6).toPx(), center.y ),
            end = Offset(center.x - (lineWidth/2).toPx(), center.y ),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            start = Offset(center.x + (lineWidth/4).toPx(), center.y - (lineWidth/2).toPx()),
            end = Offset(center.x - (lineWidth/2).toPx(), center.y - (lineWidth/2).toPx() ),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
    }
}

@Preview
@Composable
fun SnapWitchNetworkIcon(
    modifier: Modifier = Modifier,
    onClick : ()-> Unit = {},
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size/2
    val strokeWidth = size/8
    val shiftX = (lineWidth / 2)

    Canvas(modifier = modifier.size(50.dp), ){
        drawLine(
                start = Offset(center.x - (lineWidth).toPx() + shiftX.toPx(),center.y - ((lineWidth * 2)/7).toPx()),
                end = Offset(center.x - (lineWidth).toPx() + shiftX.toPx(), center.y),
                color = contentColor,
                strokeWidth = strokeWidth.toPx()
            )
        drawLine(
                start = Offset(center.x - ((lineWidth*2)/3).toPx() + shiftX.toPx(),center.y - (lineWidth/2).toPx()),
                end = Offset(center.x - ((lineWidth*2)/3).toPx() + shiftX.toPx(), center.y),
                color = contentColor,
                strokeWidth = strokeWidth.toPx()
            )
        drawLine(
                start = Offset(center.x - ((lineWidth*2)/6).toPx() + shiftX.toPx(),center.y - ((lineWidth * 4)/5).toPx()),
                end = Offset(center.x - ((lineWidth*2)/6).toPx() + shiftX.toPx(), center.y),
                color = contentColor,
                strokeWidth = strokeWidth.toPx()
            )
        drawLine(
                start = Offset(center.x + shiftX.toPx() ,center.y - (lineWidth).toPx()),
                end = Offset(center.x + shiftX.toPx(), center.y),
                color = contentColor,
                strokeWidth = strokeWidth.toPx()
            )
    }
}

@Preview
@Composable
fun SnapWitchArrowIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
) {
    val lineWidth = size / 2
    val strokeWidth = size / 10
    val shiftX = (lineWidth / 2)

    Canvas(
        modifier = modifier
            .size(size)
            .clickable { onClick() }
    ) {

        val shifttX = size/10

        val path = Path().apply {
            // Top triangle
            moveTo(center.x - shiftX.toPx(), center.y - (lineWidth / 2).toPx())
            lineTo(center.x - (lineWidth / 2).toPx(), center.y)
            lineTo(center.x - shiftX.toPx(), center.y + (lineWidth / 2).toPx())

            // Bottom triangle
            moveTo(center.x - shiftX.toPx(), center.y + (lineWidth / 2).toPx())
            lineTo(center.x + (lineWidth / 2).toPx(), center.y)
            lineTo(center.x - shiftX.toPx(), center.y - (lineWidth / 2).toPx())
        }
        val path2 = Path().apply {
            // Top triangle
            moveTo(center.x - shiftX.toPx() + shifttX.toPx(), center.y - (lineWidth / 2).toPx())
            lineTo(center.x - (lineWidth / 2).toPx()+ shifttX.toPx(), center.y)
            lineTo(center.x - shiftX.toPx()+ shifttX.toPx(), center.y + (lineWidth / 2).toPx())

            // Bottom triangle
            moveTo(center.x - shiftX.toPx()+ shifttX.toPx(), center.y + (lineWidth / 2).toPx())
            lineTo(center.x + (lineWidth / 2).toPx()+ shifttX.toPx(), center.y)
            lineTo(center.x - shiftX.toPx()+ shifttX.toPx(), center.y - (lineWidth / 2).toPx())
        }
        drawCircle(
            radius = (size/8).toPx(),
            color = contentColor,
            style = Stroke(
                width = strokeWidth.toPx()
            )
        )
        drawPath(
            path = path,
            color = contentColor,
            style = Stroke(width = strokeWidth.toPx())
        )

        // Draw the vertical line connecting the triangles
        drawLine(
            start = Offset(center.x - shiftX.toPx(), center.y - (lineWidth / 2).toPx()),
            end = Offset(center.x - shiftX.toPx(), center.y + (lineWidth / 2).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )

        drawPath(
            path = path2,
            color = contentColor,
            style = Stroke(width = strokeWidth.toPx())
        )

        // Draw the vertical line connecting the triangles
        drawLine(
            start = Offset(center.x - shiftX.toPx() + (shifttX/2).toPx(), center.y - (lineWidth / 2).toPx()),
            end = Offset(center.x - shiftX.toPx()+ (shifttX/2).toPx(), center.y + (lineWidth / 2).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx()
        )
    }
}



@Preview
@Composable
fun SnapWitchClockIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val strokeWidth = size/10
    val lineWidth = size/4
    Canvas(
        modifier = modifier
            .size(60.dp)
            .background(color = backgroundColor)){
        drawCircle(
            radius = ((size+5.dp)/2).toPx(),
            color = contentColor,
            style = Stroke(
                width = (strokeWidth + 1.dp ).toPx()
            ),
        )
        drawLine(
            start = center,
            end = Offset(center.x - lineWidth.toPx(), center.y -( (lineWidth*5)/4).toPx()),
            color = contentColor,
            strokeWidth = (strokeWidth + (1/2).dp).toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = center,
            end = Offset(center.x +((lineWidth*3)/2).toPx(), center.y + ( (lineWidth*1)/6).toPx()),
            color = contentColor,
            strokeWidth = (strokeWidth).toPx(),
            cap = StrokeCap.Round
        )
    }
}