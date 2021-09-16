package org.beeender.comradeneovim.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

fun getProject(): Project {
    val projects = ProjectManager.getInstance().openProjects
    if (projects.isEmpty()) {
        throw RuntimeException("Project not found. Please open a project on intellij.")
    }

    return projects[0]
}

fun getVirtualFile(filePath: String): VirtualFile? {
    if (isVimJarFilePath(filePath)) {
        return VirtualFileManager
            .getInstance()
            .refreshAndFindFileByUrl(toIntellijJarFilePath(filePath))
    }
    val application = ApplicationManager.getApplication()
    val file = File(FileUtil.toSystemDependentName(filePath))
    if (!file.exists()) {
        throw RuntimeException("File not found: $filePath.")
    }
    val virtualFileRef = Ref<VirtualFile>()
    application.invokeAndWait {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) 
            ?: throw RuntimeException("Virtual file not found: $filePath.")
        virtualFileRef.set(virtualFile)
    }

    return virtualFileRef.get()
}