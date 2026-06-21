package com.example

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        AgeCalculatorApp(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgeCalculatorApp(
    modifier: Modifier = Modifier,
    viewModel: AgeCalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val ageResult by viewModel.ageResult.collectAsState()
    
    val scrollState = rememberScrollState()
    val arabicDateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ar"))
    }

    val showDatePicker = {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val picker = DatePickerDialog(
            context,
            { _, sYear, sMonth, sDay ->
                val date = LocalDate.of(sYear, sMonth + 1, sDay)
                viewModel.setDate(date)
            },
            selectedDate?.year ?: year,
            selectedDate?.monthValue?.minus(1) ?: month,
            selectedDate?.dayOfMonth ?: day
        )
        picker.datePicker.maxDate = System.currentTimeMillis()
        picker.show()
    }

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = if (MaterialTheme.colorScheme.primary == Color(0xFF0288D1)) {
                        listOf(Color(0xFFE0F7FA), Color(0xFFF0F7FF))
                    } else {
                        listOf(Color(0xFF0A192F), Color(0xFF15305B))
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Bento Header Section
            BentoHeader()

            Spacer(modifier = Modifier.height(20.dp))

            // Bento Input Picker
            InputBentoCard(
                selectedDateText = selectedDate?.format(arabicDateFormatter) ?: "انقر لتحديد تاريخ ميلادك...",
                onSelectClick = showDatePicker,
                onCalculateClick = { viewModel.calculateAge() },
                onResetClick = { viewModel.reset() },
                hasSelectedDate = selectedDate != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bento-styled Results
            AnimatedVisibility(
                visible = ageResult != null,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                ageResult?.let { res ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bento block 1: Primary Colored Display Banner
                        MainAgeBentoBanner(res)

                        // Bento block 2: Dual Card Row (Zodiac & Day of Week Born)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ZodiacBentoCard(res, modifier = Modifier.weight(1f))
                            BirthDayBentoCard(res, modifier = Modifier.weight(1f))
                        }

                        // Bento block 3: Countdown row
                        CountdownBentoCard(res)

                        // Bento block 4: Multi-column detail stats (Days, Hours, Minutes, Seconds)
                        DetailedBentoStatsGrid(res)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions footer panel inside the Bento theme
                        ActionBentoFooter(
                            onShareClick = {
                                val dobStr = selectedDate?.format(arabicDateFormatter) ?: ""
                                val shareText = context.getString(
                                    R.string.share_text_template,
                                    dobStr,
                                    res.years,
                                    res.months,
                                    res.days,
                                    res.zodiacSign,
                                    res.dayOfWeekBorn,
                                    if (res.isBirthdayToday) "اليوم هو يوم ميلادك! 🎉" else "${res.remainingDaysToNextBirthday} يوم"
                                )
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "شارك النتيجة عبر:")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            }

            // Empty state helper when no age is computed yet
            if (ageResult == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Empty State Icon",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.no_date_selected),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App logo inside gorgeous bento-style sky card wrapper
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_age_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column {
            Text(
                text = stringResource(id = R.string.main_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "اكتشف تفاصيل رحلتك عبر الزمن",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun InputBentoCard(
    selectedDateText: String,
    onSelectClick: () -> Unit,
    onCalculateClick: () -> Unit,
    onResetClick: () -> Unit,
    hasSelectedDate: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(32.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(id = R.string.choose_birth_date).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Date Selection input area - Bento Sky-themed style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectClick() }
                    .padding(horizontal = 18.dp)
                    .testTag("date_select_box"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedDateText,
                        color = if (hasSelectedDate) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calender Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Bento Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCalculateClick,
                    enabled = hasSelectedDate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(58.dp)
                        .testTag("calculate_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Calculate Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.calculate_btn),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (hasSelectedDate) {
                    OutlinedButton(
                        onClick = onResetClick,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(58.dp)
                            .testTag("reset_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تصفير",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAgeBentoBanner(res: AgeResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // High aesthetic overlay visual decoration (circle)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-20).dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "عمرك الحالي",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = res.years.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سنة",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = res.months.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "أشهر",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "و ${res.days} يوماً بالضبط",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ZodiacBentoCard(res: AgeResult, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(26.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getZodiacEmoji(res.zodiacSign),
                fontSize = 42.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Text(
                text = "برجك الفلكي",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = res.zodiacSign.substringBefore(" ("),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BirthDayBentoCard(res: AgeResult, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(26.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "يوم الميلاد",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = res.dayOfWeekBorn,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CountdownBentoCard(res: AgeResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFFFF3E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎂", fontSize = 22.sp)
                }
                
                Text(
                    text = "المتبقي لعيد ميلادك",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                if (res.isBirthdayToday) {
                    Text(
                        text = "اليوم! 🎉",
                        color = Color(0xFFE91E63),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Text(
                        text = res.remainingDaysToNextBirthday.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "يوم",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedBentoStatsGrid(res: AgeResult) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "إجمالي تفاصيل العمر",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .padding(bottom = 12.dp, start = 4.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniBentoGridCard(
                label = "أيام",
                value = formatLargeNumber(res.totalDays),
                modifier = Modifier.weight(1f),
                badgeColor = Color(0xFF4CAF50)
            )
            MiniBentoGridCard(
                label = "ساعات",
                value = formatLargeNumber(res.totalHours),
                modifier = Modifier.weight(1f),
                badgeColor = Color(0xFFFF9800)
            )
            MiniBentoGridCard(
                label = "دقائق",
                value = formatLargeNumber(res.totalMinutes),
                modifier = Modifier.weight(1f),
                badgeColor = Color(0xFF00BCD4)
            )
        }
    }
}

@Composable
fun MiniBentoGridCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    badgeColor: Color
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .border(
                1.dp,
                badgeColor.copy(alpha = 0.08f),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionBentoFooter(onShareClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High polish sharing button matching bottom action aesthetics
        Button(
            onClick = onShareClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(22.dp)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share App Content",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.share_btn),
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

fun getZodiacEmoji(zodiac: String): String {
    return when {
        zodiac.contains("Aries", ignoreCase = true) -> "♈"
        zodiac.contains("Taurus", ignoreCase = true) -> "♉"
        zodiac.contains("Gemini", ignoreCase = true) -> "♊"
        zodiac.contains("Cancer", ignoreCase = true) -> "♋"
        zodiac.contains("Leo", ignoreCase = true) -> "♌"
        zodiac.contains("Virgo", ignoreCase = true) -> "♍"
        zodiac.contains("Libra", ignoreCase = true) -> "♎"
        zodiac.contains("Scorpio", ignoreCase = true) -> "♏"
        zodiac.contains("Sagittarius", ignoreCase = true) -> "♐"
        zodiac.contains("Capricorn", ignoreCase = true) -> "♑"
        zodiac.contains("Aquarius", ignoreCase = true) -> "♒"
        zodiac.contains("Pisces", ignoreCase = true) -> "♓"
        else -> "✨"
    }
}

fun formatLargeNumber(number: Long): String {
    return java.text.NumberFormat.getNumberInstance(Locale.US).format(number)
}
