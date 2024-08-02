package fi.aalto.cs.apluscourses.ui.browser

import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.writeText
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.ui.jcef.JBCefScrollbarsHelper
import fi.aalto.cs.apluscourses.services.exercise.ShowFeedback
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefAuthCallback
import org.cef.callback.CefCallback
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.cef.security.CefSSLInfo
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.Nls
import java.beans.PropertyChangeListener
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.swing.JComponent
import javax.swing.SwingUtilities

class BrowserEditor(private val project: Project, private val file: VirtualFile, initialLocation: String?) :
    UserDataHolderBase(), FileEditor,
    DocumentListener {

    companion object {
        private const val NAME = "APlusViewer"

        private const val HOST_NAME = "plus.cs.aalto.fi"
        private const val PROTOCOL = "https"
        private val SCROLLBAR_CSS = JBCefScrollbarsHelper.buildScrollbarsStyle()

//        private const val OVERLAY_SCROLLBARS_CSS_PATH = "/overlayscrollbars.css"
//        private const val OVERLAY_SCROLLBARS_JS_PATH = "/overlayscrollbars.browser.es6.js"

        private const val VIEWER_PATH = "/accounts/accounts/"
//        private const val IMAGE_PATH = "/image"
//        private const val SCROLLBARS_CSS_PATH = "/scrollbars.css"
//        private const val CHESSBOARD_CSS_PATH = "/chessboard.css"
//        private const val GRID_CSS_PATH = "/pixel_grid.css"

        private const val VIEWER_URL = "$PROTOCOL://$HOST_NAME$VIEWER_PATH"
//        private const val IMAGE_URL = "$PROTOCOL://$HOST_NAME$IMAGE_PATH"
//        private const val SCROLLBARS_STYLE_URL = "$PROTOCOL://$HOST_NAME$SCROLLBARS_CSS_PATH"
//        private const val CHESSBOARD_STYLE_URL = "$PROTOCOL://$HOST_NAME$CHESSBOARD_CSS_PATH"
//        private const val GRID_STYLE_URL = "$PROTOCOL://$HOST_NAME$GRID_CSS_PATH"

        private val ourCefClient = JBCefApp.getInstance().createClient()

        init {
//            Disposer.register(ApplicationManager.getApplication(), ourCefClient) TODO
        }

        fun isDebugMode() = true//RegistryManager.getInstance().`is`("ide.browser.jcef.svg-viewer.debug")
    }

    private val myDocument: Document = FileDocumentManager.getInstance().getDocument(file)!!
    private val myBrowser: JBCefBrowser =
        JBCefBrowserBuilder().setOffScreenRendering(false).setClient(ourCefClient)
            .setEnableOpenDevToolsMenuItem(isDebugMode()).build()

    //    private val myUIComponent: JCefImageViewerUI
//    private val myViewerStateJSQuery: JBCefJSQuery
    private val myRequestHandler: CefRequestHandler
//    private val myLoadHandler: CefLoadHandler

//    private var myState = ViewerState()
//    private var myEditorState: ImageFileEditorState = ImageFileEditorState(
//        OptionsManager.getInstance().options.editorOptions.transparencyChessboardOptions.isShowDefault,
//        OptionsManager.getInstance().options.editorOptions.gridOptions.isShowDefault,
//        1.0,
//        false
//    )

    override fun getComponent(): JComponent = myBrowser.component
    override fun getPreferredFocusedComponent(): JComponent = myBrowser.cefBrowser.uiComponent as JComponent
    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String = NAME

    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = true
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun getFile(): VirtualFile = file

    override fun getState(level: FileEditorStateLevel): FileEditorState = object : FileEditorState {
        override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean = true
    }
//        ImageFileEditorState(myState.chessboardEnabled, myState.gridEnabled, myState.zoom, !myState.realSize)

    override fun setState(state: FileEditorState) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { setState(state) }
            return
        }

//        if (state is ImageFileEditorState) {
//            val options = OptionsManager.getInstance().options.editorOptions.zoomOptions
//            isTransparencyChessboardVisible = state.isBackgroundVisible
//            isGridVisible = state.isGridVisible
//            if (myState.status == ViewerState.Status.INIT) {
//                myEditorState = state
//                return
//            }
//
//            if (!options.isSmartZooming) {
//                execute("setZoom(${state.zoomFactor});")
//            }
//        }
    }

    override fun dispose() {
        ourCefClient.removeRequestHandler(myRequestHandler, myBrowser.cefBrowser)
//        ourCefClient.removeLoadHandler(myLoadHandler, myBrowser.cefBrowser)
//        myViewerStateJSQuery.clearHandlers()
//        myDocument.removeDocumentListener(this)
    }

//    override fun documentChanged(event: DocumentEvent): Unit = execute("reload()")

    fun loadURL(url: String) {
        myBrowser.loadURL(url)
    }

    fun getToken() {
//        execute(
//            """
//fetch('/accounts/accounts/')
//    .then(response => {
//        if (!response.ok) {
//            throw new Error('Network response was not ok ' + response.statusText);
//        }
//        return response.text();
//    })
//    .then(htmlString => {
//        const parser = new DOMParser();
//        const doc = parser.parseFromString(htmlString, 'text/html');
//
//        const apiAccessTokenElement = doc.getElementById('api-access-token');
//        const apiAccessToken = apiAccessTokenElement.value;
//        console.log('API Access Token:', apiAccessToken);
//        window.cefQuery({
//            request: 'fetchApiAccessToken:' + apiAccessToken
//        });
//    })
//    .catch(error => {
//        console.error('There was a problem with the fetch operation:', error);
//    });"""
//        )
    }

//    fun setZoom(scale: Double, at: Point) {
//        execute("setZoom(${scale}, {'x': ${at.x}, 'y': ${at.y}});")
//    }

    //    override fun setTransparencyChessboardVisible(visible: Boolean) {
//        if (myState.status == ViewerState.Status.ERROR) return
//        execute("setChessboardVisible(" + (if (visible) "true" else "false") + ");")
//    }
//
//    override fun setGridVisible(visible: Boolean) {
//        if (myState.status == ViewerState.Status.ERROR) return
//        execute("setGridVisible(${if (visible) "true" else "false"});")
//    }
//
//    override fun setBorderVisible(visible: Boolean) {
//        execute("setBorderVisible(${if (visible) "true" else "false"});")
//    }
//
//    override fun isTransparencyChessboardVisible(): Boolean = myState.chessboardEnabled
//    override fun isEnabledForActionPlace(place: String): Boolean = ThumbnailViewActions.ACTION_PLACE != place
//    override fun getZoomModel(): ImageZoomModel = ZOOM_MODEL
//    override fun isGridVisible(): Boolean = myState.status == ViewerState.Status.OK && myState.gridEnabled
//    fun getZoom() = myState.zoom
    private fun execute(@Language("javascript") script: String) =
        myBrowser.cefBrowser.executeJavaScript(script, myBrowser.cefBrowser.url, 0)

//    private val ZOOM_MODEL: ImageZoomModel = object : ImageZoomModel {
//        override fun getZoomFactor(): Double = myState.zoom
//        override fun setZoomFactor(zoomFactor: Double) = execute("setZoom($zoomFactor);")
//        override fun fitZoomToWindow() = execute("fitToViewport();")
//        override fun zoomOut() = execute("zoomOut();")
//        override fun zoomIn() = execute("zoomIn();")
//        override fun setZoomLevelChanged(value: Boolean) {}
//        override fun canZoomOut(): Boolean = myState.status == ViewerState.Status.OK && myState.zoomOutPossible
//        override fun canZoomIn(): Boolean = myState.status == ViewerState.Status.OK && myState.zoomInPossible
//        override fun isZoomLevelChanged(): Boolean =
//            myState.status == ViewerState.Status.ERROR || !myState.fittedToViewport
//    }

//    private val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        project.service<ShowFeedback>().updateBrowserTitle(file, "A+")
        val config = CefMessageRouter.CefMessageRouterConfig("tokenQuery", "tokenQueryCancel")
        val messageRouter = CefMessageRouter.create(config)
        ourCefClient.cefClient.addMessageRouter(messageRouter)
        ourCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                println("onLoadEnd")
                frame.executeJavaScript(
                    """(function() {
fetch('/accounts/accounts/')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok ' + response.statusText);
        }
        return response.text();
    })
    .then(htmlString => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(htmlString, 'text/html');

        const apiAccessTokenElement = doc.getElementById('api-access-token');
        const apiAccessToken = apiAccessTokenElement.value;
        console.log('API Access Token:', apiAccessToken);
        window.tokenQuery({
            request: 'fetchApiAccessToken:' + apiAccessToken
        });
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
    })})()""", frame.url, 0
                )
//                WriteCommandAction.runWriteCommandAction(project) {
//                    file.rename(project, "${frame.name}.aplus")
//                }
            }
        }, myBrowser.cefBrowser)
        ourCefClient.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onTitleChange(browser: CefBrowser, title: String) {
                println("Page Title: $title")
                project.service<ShowFeedback>().updateBrowserTitle(file, title)
            }
        }, myBrowser.cefBrowser)
        messageRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser,
                frame: CefFrame,
                queryId: Long,
                request: String,
                persistent: Boolean,
                callback: CefQueryCallback
            ): Boolean {
                println("onQuery")
                if (request.startsWith("fetchApiAccessToken")) {
                    val result = request.removePrefix("fetchApiAccessToken:")
                    println("API Access Token: $result")
                    callback.success(result)
                    return true
                }
                return false
            }
        }, true)
//        myDocument.addDocumentListener(this)
        val resourceRequestHandler = object : CefResourceRequestHandlerAdapter() {

        }
        myRequestHandler = object : CefRequestHandlerAdapter() {
            override fun onBeforeBrowse(
                browser: CefBrowser,
                frame: CefFrame,
                request: CefRequest,
                userGesture: Boolean,
                isRedirect: Boolean
            ): Boolean {
//                p1
//                (browser as JBCefBrowser).get
                println("onBeforeBrowse")
//                getToken()
//                browser
                return false
            }

            override fun onOpenURLFromTab(
                browser: CefBrowser,
                frame: CefFrame,
                targetUrl: String,
                userGesture: Boolean
            ): Boolean {
//                TODO("Not yet implemented")
                println(targetUrl)
                return false
            }

//            override fun getResourceRequestHandler(
//                browser: CefBrowser,
//                frame: CefFrame,
//                request: CefRequest,
//                isNavigation: Boolean,
//                isDownload: Boolean,
//                requestInitiator: String,
//                disableDefaultHandling: BoolRef
//            ): CefResourceRequestHandler = resourceRequestHandler

//            override fun getAuthCredentials(
//                browser: CefBrowser,
//                originUrl: String,
//                isProxy: Boolean,
//                host: String,
//                port: Int,
//                realm: String,
//                scheme: String,
//                callback: CefAuthCallback
//            ): Boolean {
//                TODO("Not yet implemented")
//
//            }
        }

//        myRequestHandler.addResource(VIEWER_PATH) {
//            javaClass.getResourceAsStream("resources/image_viewer.html")?.let {
//                CefStreamResourceHandler(it, "text/html", this@BrowserEditor)
//            }
//        }
//
//        myRequestHandler.addResource(OVERLAY_SCROLLBARS_CSS_PATH) {
//            CefStreamResourceHandler(
//                ByteArrayInputStream(
//                    JBCefScrollbarsHelper.getOverlayScrollbarsSourceCSS().toByteArray(StandardCharsets.UTF_8)
//                ), "text/css", this
//            )
//        }
//
//        myRequestHandler.addResource(OVERLAY_SCROLLBARS_JS_PATH) {
//            CefStreamResourceHandler(
//                ByteArrayInputStream(
//                    JBCefScrollbarsHelper.getOverlayScrollbarsSourceJS().toByteArray(StandardCharsets.UTF_8)
//                ), "text/css", this
//            )
//        }
//
//        myRequestHandler.addResource(SCROLLBARS_CSS_PATH) {
//            CefStreamResourceHandler(
//                ByteArrayInputStream(
//                    JBCefScrollbarsHelper.getOverlayScrollbarStyle().toByteArray(StandardCharsets.UTF_8)
//                ), "text/css", this
//            )
//        }
//
//        myRequestHandler.addResource(CHESSBOARD_CSS_PATH) {
//            CefStreamResourceHandler(
//                ByteArrayInputStream(buildChessboardStyle().toByteArray(StandardCharsets.UTF_8)),
//                "text/css",
//                this
//            )
//        }
//
//        myRequestHandler.addResource(GRID_CSS_PATH) {
//            CefStreamResourceHandler(
//                ByteArrayInputStream(buildGridStyle().toByteArray(StandardCharsets.UTF_8)),
//                "text/css",
//                this
//            )
//        }
//
//        myRequestHandler.addResource(IMAGE_PATH) {
//            var stream: InputStream? = null
//            try {
//                stream = if (FileUtilRt.isTooLarge(myFile.length)) myFile.inputStream
//                else ByteArrayInputStream(myDocument.text.toByteArray(StandardCharsets.UTF_8))
//            } catch (e: IOException) {
//                Logger.getInstance(JCefImageViewer::class.java).warn("Failed to read the file", e)
//            }
//
//            var resourceHandler: CefStreamResourceHandler? = null
//            stream?.let {
//                try {
//                    resourceHandler = CefStreamResourceHandler(
//                        it, mimeType, this@JCefImageViewer,
//                        mapOf("Content-Security-Policy" to "script-src 'none'")
//                    )
//                } catch (_: IncorrectOperationException) { // The viewer has been disposed just return null that will reject all requests
//                }
//
//                return@addResource resourceHandler
//            }
//        }

        ourCefClient.addRequestHandler(myRequestHandler, myBrowser.cefBrowser)

//        myUIComponent = JCefImageViewerUI(myBrowser.component, this)
//        Disposer.register(this, myUIComponent)
        Disposer.register(this, myBrowser)

//        @Suppress("DEPRECATION")
//        myViewerStateJSQuery = JBCefJSQuery.create(myBrowser)
//        myViewerStateJSQuery.addHandler { s: String ->
//            val oldState = myState
//            try {
//                myState = jsonParser.decodeFromString(s)
//            } catch (_: Exception) {
//                SwingUtilities.invokeLater { myUIComponent.showError() }
//                return@addHandler JBCefJSQuery.Response(null, 255, "Failed to parse the viewer state")
//            }
//
//            val zoomOptions = OptionsManager.getInstance().options.editorOptions.zoomOptions
//            if (oldState.status == ViewerState.Status.INIT && zoomOptions.isSmartZooming) {
//                val zoomFactor = zoomOptions.getSmartZoomFactor(
//                    Rectangle(Point(0, 0), Dimension(myState.imageSize.width, myState.imageSize.height)),
//                    Dimension(myState.viewportSize.width, myState.viewportSize.height),
//                    5
//                )
//                execute("setZoom(${zoomFactor});")
//            }
//
//            SwingUtilities.invokeLater {
//                if (myState.status == ViewerState.Status.OK) {
//                    myUIComponent.setInfo(
//                        ImagesBundle.message(
//                            "image.info.svg", myState.imageSize.width, myState.imageSize.height,
//                            StringUtil.formatFileSize(myFile.length)
//                        )
//                    )
//                }
//
//                if (myState.status == ViewerState.Status.ERROR) {
//                    myUIComponent.showError()
//                } else {
//                    myUIComponent.showImage()
//                }
//            }
//            JBCefJSQuery.Response(null)
//        }
//
//        myLoadHandler = object : CefLoadHandlerAdapter() {
//            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
//                if (frame.isMain) {
//                    reloadStyles()
//                    execute("sendInfo = function(info_text) {${myViewerStateJSQuery.inject("info_text")};}")
//                    execute("setImageUrl('$IMAGE_URL');")
//                    isGridVisible = myEditorState.isGridVisible
//                    isTransparencyChessboardVisible = myEditorState.isBackgroundVisible
//                    setBorderVisible(isBorderVisible())
//                }
//            }
//        }
//        ourCefClient.addLoadHandler(myLoadHandler, myBrowser.cefBrowser)

        if (isDebugMode()) {
            myBrowser.loadURL("$VIEWER_URL?debug")
        } else {
            myBrowser.loadURL(VIEWER_URL)
        }

        val busConnection = ApplicationManager.getApplication().messageBus.connect(this)

//        busConnection.subscribe(EditorColorsManager.TOPIC, EditorColorsListener { reloadStyles() })
//        busConnection.subscribe(UISettingsListener.TOPIC, UISettingsListener { reloadStyles() })
        if (initialLocation != null) {
            myBrowser.loadURL(initialLocation)
        }
    }

//    @Serializable
//    private data class ViewerState(
//        val status: Status = Status.INIT,
//        val zoom: Double = 0.0,
//        val viewportSize: Size = Size(0, 0),
//        val imageSize: Size = Size(0, 0),
//        val zoomInPossible: Boolean = false,
//        val zoomOutPossible: Boolean = false,
//        val fittedToViewport: Boolean = false,
//        val realSize: Boolean = false,
//        val gridEnabled: Boolean = false,
//        val chessboardEnabled: Boolean = false
//    ) {
//        @Serializable
//        enum class Status { OK, ERROR, INIT }
//
//        @Serializable
//        data class Size(val width: Int, val height: Int)
//    }

//    private fun colorToCSS(color: Color) = "rgba(${color.red}, ${color.blue}, ${color.green}, ${color.alpha / 255.0})"

//    private fun buildChessboardStyle(): String {
//        val options = OptionsManager.getInstance().options.editorOptions.transparencyChessboardOptions
//        val cellSize = JBCefApp.normalizeScaledSize(options.cellSize)
//        val blackColor = colorToCSS(options.blackColor)
//        val whiteColor = colorToCSS(options.whiteColor)
//        return /*language=css*/ """
//      #chessboard {
//        position: absolute;
//        width: 100%;
//        height: 100%;
//        background: repeating-conic-gradient(${blackColor} 0% 25%, ${whiteColor} 0% 50%) 50% / ${cellSize * 2}px ${cellSize * 2}px;
//        background-position: 0 0;
//      }
//    """.trimIndent()
//    }
//
//    private fun buildGridStyle(): String {
//        val color = colorToCSS(OptionsManager.getInstance().options.editorOptions.gridOptions.lineColor)
//        return /*language=css*/ """
//      #pixel_grid {
//        position: absolute;
//        width: 100%;
//        height: 100%;
//        margin: 0;
//        background-image: linear-gradient(to right, ${color} 1px, transparent 1px),
//        linear-gradient(to bottom, ${color} 1px, transparent 1px);
//        background-size: 1px 1px;
//        mix-blend-mode: normal;
//      }
//      """.trimIndent()
//    }
//
//    private fun reloadStyles() {
//        execute(
//            """
//      loadScrollbarsStyle('$SCROLLBARS_STYLE_URL');
//      loadChessboardStyle('$CHESSBOARD_STYLE_URL');
//      loadPixelGridStyle('$GRID_STYLE_URL');
//    """.trimIndent()
//        )
//    }
}