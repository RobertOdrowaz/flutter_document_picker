package com.sidlatau.flutterdocumentpicker

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterDocumentPickerPlugin : MethodCallHandler {
    private var delegate: FlutterDocumentPickerDelegate? = null

    companion object {
        const val TAG = "flutter_document_picker"
        private const val ARG_ALLOWED_FILE_EXTENSIONS = "allowedFileExtensions"
        private const val ARG_ALLOWED_MIME_TYPES = "allowedMimeTypes"
        private const val ARG_INVALID_FILENAME_SYMBOLS = "invalidFileNameSymbols"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            if (registrar.activity() == null) {
                // When a background flutter view tries to register the plugin, the registrar has no activity.
                // We stop the registration process as this plugin is foreground only.
                return
            }

            val plugin = FlutterDocumentPickerPlugin()
            plugin.setup(registrar.messenger(), registrar, null)
        }
    }

    private fun setup(messenger: BinaryMessenger, registrar: Registrar?,
                      activityBinding: ActivityPluginBinding?) {
        var delegate: FlutterDocumentPickerDelegate? = null

        if (registrar != null) {
            delegate = FlutterDocumentPickerDelegate(
                    activity = registrar.activity()
            )
            registrar.addActivityResultListener(delegate)
        } else if (activityBinding != null) {
            delegate = FlutterDocumentPickerDelegate(
                    activity = activityBinding.activity
            )
            activityBinding.addActivityResultListener(delegate)
        }

        this.delegate = delegate

        val channel = MethodChannel(messenger, "flutter_document_picker")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "pickDocument") {
            delegate?.pickDocument(
                    result,
                    allowedFileExtensions = parseArray(call, ARG_ALLOWED_FILE_EXTENSIONS),
                    allowedMimeTypes = parseArray(call, ARG_ALLOWED_MIME_TYPES),
                    invalidFileNameSymbols = parseArray(call, ARG_INVALID_FILENAME_SYMBOLS)
            )
        } else {
            result.notImplemented()
        }
    }


    private fun parseArray(call: MethodCall, arg: String): Array<String>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<String>>(arg)?.toTypedArray()
        }
        return null
    }
}
