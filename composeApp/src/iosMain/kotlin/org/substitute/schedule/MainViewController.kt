package org.substitute.schedule

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.createHttpClient
import org.substitute.schedule.utils.IOSSecureStorage

fun MainViewController() = ComposeUIViewController { App(
    client = DsbApiClient(createHttpClient(Darwin.create())),
    storage = IOSSecureStorage()

) }