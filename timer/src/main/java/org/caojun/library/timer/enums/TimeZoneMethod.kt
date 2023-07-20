package org.caojun.library.timer.enums

enum class TimeZoneMethod {
    TimeZone,//TimeZone.getDefault()
    Calendar,//Calendar.getInstance().getTimeZone()
    GregorianCalendar,//GregorianCalendar().getTimeZone()
    System,//System.getProperty("user.timezone")
//    ZonedDateTime//ZonedDateTime.now().getZone()
}