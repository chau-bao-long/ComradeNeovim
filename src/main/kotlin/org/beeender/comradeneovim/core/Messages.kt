package org.beeender.comradeneovim.core

import org.beeender.neovim.annotation.MessageConverterFun
import org.beeender.neovim.rpc.Notification
import org.beeender.neovim.rpc.Request

class ComradeBufEnterParams(val id: Int, val path: String) {
    companion object {
        @MessageConverterFun
        fun fromNotification(notification: Notification) : ComradeBufEnterParams {
            val map = notification.args.first() as Map<*, *>
            val id = map["id"] as Int
            val path =  map["path"] as String
            return ComradeBufEnterParams(id, path)
        }
    }
}

class ComradeBufWriteParams(val id: Int) {
    companion object {
        @MessageConverterFun
        fun fromMessage(request: Request) : ComradeBufWriteParams {
            val bufId = (request.args[0] as Map<*, *>)["id"] as Int
            return ComradeBufWriteParams(bufId)
        }
    }
}

class ComradeBufSyncCursorParams(val file: String, val offset: Int) {
    companion object {
        @MessageConverterFun
        fun fromMessage(request: Request) : ComradeBufSyncCursorParams {
            val map = request.args[0] as Map<*, *>
            val file = map["file"] as String
            val offset = map["offset"] as Int
            return ComradeBufSyncCursorParams(file, offset)
        }
    }
}

class ComradeQuickFixParams(val bufId:Int, val insightId: Int, val fixIndex: Int) {
    companion object {
        @MessageConverterFun
        fun fromMessage(request: Request) : ComradeQuickFixParams {
            val map = (request.args[0] as Map<*, *>)
            val buf = map["buf"] as Int
            val insight = map["insight"] as Int
            val fix = map["fix"] as Int
            return ComradeQuickFixParams(buf, insight, fix)
        }
    }
}
