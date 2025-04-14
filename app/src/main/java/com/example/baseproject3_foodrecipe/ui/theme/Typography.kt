package com.example.baseproject3_foodrecipe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.baseproject3_foodrecipe.R

val RobotoRegular = FontFamily(Font(R.font.roboto_regular))
val RobotoBold = FontFamily(Font(R.font.roboto_bold))

val FoodRecipeTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = RobotoBold,
        fontSize = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoRegular,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoBold,
        fontSize = 14.sp
    )
)
