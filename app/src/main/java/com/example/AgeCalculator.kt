package com.example

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.DayOfWeek

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,
    val zodiacSign: String,
    val dayOfWeekBorn: String,
    val remainingDaysToNextBirthday: Int,
    val isBirthdayToday: Boolean
)

object AgeCalculator {

    fun calculateAge(birthDate: LocalDate, today: LocalDate = LocalDate.now()): AgeResult {
        // Precise age period (years, months, days)
        val period = Period.between(birthDate, today)
        val years = if (period.isNegative) 0 else period.years
        val months = if (period.isNegative) 0 else period.months
        val days = if (period.isNegative) 0 else period.days

        // Total breakdowns using ChronoUnit
        val totalDays = if (birthDate.isAfter(today)) 0L else ChronoUnit.DAYS.between(birthDate, today)
        val totalHours = totalDays * 24L
        val totalMinutes = totalHours * 60L
        val totalSeconds = totalMinutes * 60L

        // Day of the week born
        val dayOfWeekBorn = getArabicDayOfWeek(birthDate.dayOfWeek)

        // Zodiac Sign
        val zodiacSign = getZodiacSign(birthDate.dayOfMonth, birthDate.monthValue)

        // Next Birthday calculation & remaining days
        val nextBirthday = calculateNextBirthday(birthDate, today)
        val remainingDays = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
        val isBirthdayToday = (birthDate.dayOfMonth == today.dayOfMonth && birthDate.monthValue == today.monthValue)

        return AgeResult(
            years = years,
            months = months,
            days = days,
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds,
            zodiacSign = zodiacSign,
            dayOfWeekBorn = dayOfWeekBorn,
            remainingDaysToNextBirthday = remainingDays,
            isBirthdayToday = isBirthdayToday
        )
    }

    private fun getArabicDayOfWeek(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "الإثنين"
            DayOfWeek.TUESDAY -> "الثلاثاء"
            DayOfWeek.WEDNESDAY -> "الأربعاء"
            DayOfWeek.THURSDAY -> "الخميس"
            DayOfWeek.FRIDAY -> "الجمعة"
            DayOfWeek.SATURDAY -> "السبت"
            DayOfWeek.SUNDAY -> "الأحد"
        }
    }

    private fun getZodiacSign(day: Int, month: Int): String {
        return when (month) {
            1 -> if (day < 20) "الجدي (Capricorn)" else "الدلو (Aquarius)"
            2 -> if (day < 19) "الدلو (Aquarius)" else "الحوت (Pisces)"
            3 -> if (day < 21) "الحوت (Pisces)" else "الحمل (Aries)"
            4 -> if (day < 20) "الحمل (Aries)" else "الثور (Taurus)"
            5 -> if (day < 21) "الثور (Taurus)" else "الجوزاء (Gemini)"
            6 -> if (day < 21) "الجوزاء (Gemini)" else "السرطان (Cancer)"
            7 -> if (day < 23) "السرطان (Cancer)" else "الأسد (Leo)"
            8 -> if (day < 23) "الأسد (Leo)" else "العذراء (Virgo)"
            9 -> if (day < 23) "العذراء (Virgo)" else "الميزان (Libra)"
            10 -> if (day < 23) "الميزان (Libra)" else "العقرب (Scorpio)"
            11 -> if (day < 22) "العقرب (Scorpio)" else "القوس (Sagittarius)"
            12 -> if (day < 22) "القوس (Sagittarius)" else "الجدي (Capricorn)"
            else -> "غير معروف"
        }
    }

    private fun calculateNextBirthday(birthDate: LocalDate, today: LocalDate): LocalDate {
        var nextBD = birthDate.withYear(today.year)
        if (nextBD.isBefore(today) || nextBD.isEqual(today)) {
            nextBD = nextBD.plusYears(1)
        }
        return nextBD
    }
}
