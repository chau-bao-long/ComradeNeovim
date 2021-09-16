package org.beeender.comradeneovim.virtualfile

import org.beeender.comradeneovim.buffer.SyncBufferManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.launch
import org.beeender.comradeneovim.ComradeNeovimPlugin
import org.beeender.comradeneovim.ComradeScope
import org.beeender.comradeneovim.buffer.SyncBuffer
import org.beeender.comradeneovim.buffer.SyncBufferManager
import org.beeender.comradeneovim.core.FUN_BUF_ENTER
import org.beeender.comradeneovim.core.FUN_BUF_MOVE_TO_OFFSET
import org.beeender.comradeneovim.core.NvimInstance
import org.beeender.comradeneovim.utils.isIntellijJarFile
import org.beeender.comradeneovim.utils.toVimJarFilePath
import java.util.IdentityHashMap


object VirtualFileManager : SyncBufferManagerListener {
    private val busMap = IdentityHashMap<SyncBuffer, MessageBusConnection>()
    private val syncBufferMap = IdentityHashMap<SyncBuffer, CaretListener>()
    private var isStarted: Boolean = false
    private val appBus = 
        ApplicationManager.getApplication().messageBus.connect(ComradeNeovimPlugin.instance)

    fun start() {
        if (!isStarted) {
            appBus.subscribe(SyncBufferManager.TOPIC, this)
            isStarted = true
        }
    }
    override fun bufferCreated(syncBuffer: SyncBuffer) {
        ApplicationManager.getApplication().assertIsDispatchThread()
        
        busMap.remove(syncBuffer)?.apply { this.disconnect() }
        busMap[syncBuffer] = listenFileOpened(syncBuffer.project, syncBuffer.nvimInstance)
        
        syncBufferMap.remove(syncBuffer)?.apply { removeCaretListener(this) }
        syncBufferMap[syncBuffer] = addCaretListener(syncBuffer.nvimInstance)
    }

    override fun bufferReleased(syncBuffer: SyncBuffer) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        busMap.remove(syncBuffer)?.apply { this.disconnect() }
        syncBufferMap.remove(syncBuffer)?.apply { removeCaretListener(this) }
    }

    private fun listenFileOpened(project: Project, nvimInstance: NvimInstance): MessageBusConnection {
        val bus = project.messageBus.connect(project)
        
        bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)
                val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
                val offset = editor.caretModel.offset

                ComradeScope.launch {
                    nvimInstance.run {
                        val vimFilePath = file.path.let {
                            if (isIntellijJarFile(it)) toVimJarFilePath(it) else it
                        }

                        client.api.callFunction(FUN_BUF_ENTER, listOf(vimFilePath, offset))
                    }
                }
            }
        })
        
        return bus
    }

    private fun addCaretListener(nvimInstance: NvimInstance): CaretListener {
        val listener = MyCaretListener(nvimInstance)
        EditorFactory.getInstance()
            .eventMulticaster
            .addCaretListener(listener, ApplicationManager.getApplication())

        return listener
    }
    
    private fun removeCaretListener(listener: CaretListener) {
        EditorFactory.getInstance()
            .eventMulticaster
            .removeCaretListener(listener)
    }
    
    class MyCaretListener(private val nvimInstance: NvimInstance): CaretListener {
        override fun caretPositionChanged(event: CaretEvent) {
            ComradeScope.launch {
                nvimInstance.run {
                    client.api.callFunction(
                        FUN_BUF_MOVE_TO_OFFSET,
                        listOf(event.caret!!.caretModel.offset)
                    )
                }
            }
        }
    }
}