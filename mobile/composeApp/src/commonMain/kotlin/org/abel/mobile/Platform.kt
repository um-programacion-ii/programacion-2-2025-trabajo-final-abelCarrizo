package org.abel.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform