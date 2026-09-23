package com.connectlens.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ConnectLens Application class.
 *
 * Annotated with @HiltAndroidApp which triggers Hilt's code generation and
 * sets up the application-level dependency injection component.
 *
 * No data is collected or transmitted at startup. All processing is local.
 */
@HiltAndroidApp
class ConnectLensApplication : Application()
