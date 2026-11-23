package com.example.vecurrency.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vecurrency.ui.navigation.Screen
import com.example.vecurrency.ui.theme.AppStrings
import com.example.vecurrency.ui.theme.UsdBlue

@Composable
fun FloatingNavigationBar(
    screens: List<Screen>,
    selectedIndex: Int,
    strings: AppStrings,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(20.dp)
    val indicatorShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 12.dp, shape = barShape, clip = false)
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEachIndexed { index, screen ->
                val selected = selectedIndex == index
                val title = when (screen) {
                    Screen.Dashboard -> strings.tabInicio
                    Screen.Calculator -> strings.tabCalculadora
                    Screen.About -> strings.tabHistorial
                }

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.20f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "IconScaleAnimation"
                )

                val animatedIconTint by animateColorAsState(
                    targetValue = if (selected) UsdBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 300),
                    label = "IconTintAnimation"
                )

                val animatedTextColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 300),
                    label = "TextColorAnimation"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = if (selected) 24.dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "NavIndicatorWidth"
                )

                val bgAlpha by animateFloatAsState(
                    targetValue = if (selected) 0.12f else 0.0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "NavBgAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(indicatorShape)
                        .background(UsdBlue.copy(alpha = bgAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelectPage(index) }
                        )
                        .semantics {
                            contentDescription = "Pestaña $title"
                            role = Role.Tab
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = title,
                            tint = animatedIconTint,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = animatedTextColor
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(indicatorWidth)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) UsdBlue else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
